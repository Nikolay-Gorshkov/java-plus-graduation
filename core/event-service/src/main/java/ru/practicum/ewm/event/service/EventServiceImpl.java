package ru.practicum.ewm.event.service;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.korshunov.statsclient.RecommendedEvent;
import ru.korshunov.statsclient.StatsClient;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.client.dto.EventInternalDto;
import ru.practicum.ewm.client.request.RequestServiceClient;
import ru.practicum.ewm.client.user.UserServiceClient;
import ru.practicum.ewm.error.ConflictException;
import ru.practicum.ewm.error.NotFoundException;
import ru.practicum.ewm.event.dto.EventAdminParamDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventPublicParamsDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequestDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.mapper.LocationMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.LocationRepository;
import ru.practicum.ewm.event.status.SortForParamPublicEvent;
import ru.practicum.ewm.event.status.StateEvent;
import ru.practicum.ewm.event.status.StateForUpdateEvent;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.util.PageRequestUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final UserServiceClient userServiceClient;
    private final RequestServiceClient requestServiceClient;
    private final EventMapper eventMapper;
    private final CategoryMapper categoryMapper;
    private final LocationMapper locationMapper;
    private final CategoryService categoryService;
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final StatsClient statsClient;

    @Transactional
    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        UserShortDto user = userServiceClient.getUser(userId);
        Category category = categoryMapper.toCategory(categoryService.getCategory(newEventDto.getCategory()));
        Location location = locationRepository.save(locationMapper.toLocation(newEventDto.getLocation()));
        Event event = eventMapper.toEvent(newEventDto, user, category, location);
        applyDefaultEventState(event);
        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> findEventByUserId(Long userId, int from, int size) {
        userServiceClient.getUser(userId);
        Pageable pageable = PageRequestUtil.of(from, size, Sort.by("id").ascending());
        return enrichEvents(eventRepository.findEventByUserId(userId, pageable).getContent()).stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }

    @Override
    public EventFullDto findEventByIdAndEventId(Long userId, Long eventId) {
        userServiceClient.getUser(userId);
        Event event = findEventWithOutDto(userId, eventId);
        return eventMapper.toEventFullDto(enrichEvent(event));
    }

    @Transactional
    @Override
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        userServiceClient.getUser(userId);
        Event event = findEventWithOutDto(userId, eventId);

        if (StateEvent.PUBLISHED.equals(event.getState())) {
            throw new ConflictException("Данный Event невозможно изменить, поскольку он уже опубликован");
        } else if (updateEventUserRequest.getStateAction() != null
                && StateEvent.CANCELED.equals(event.getState())
                && updateEventUserRequest.getStateAction().equals(StateForUpdateEvent.SEND_TO_REVIEW)) {
            event.setState(StateEvent.PENDING);
        } else if (updateEventUserRequest.getStateAction() != null
                && StateEvent.PENDING.equals(event.getState())
                && updateEventUserRequest.getStateAction().equals(StateForUpdateEvent.CANCEL_REVIEW)) {
            event.setState(StateEvent.CANCELED);
        }

        if (updateEventUserRequest.getEventDate() != null
                && updateEventUserRequest.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            event.setEventDate(updateEventUserRequest.getEventDate());
        }

        Event updatedEvent = updateCategoryAndLocation(updateEventUserRequest, event);
        eventMapper.toUpdateEvent(updateEventUserRequest, updatedEvent);
        return eventMapper.toEventFullDto(enrichEvent(eventRepository.save(updatedEvent)));
    }

    @Override
    public Event findEventById(Long eventId) {
        return eventRepository.findEventById(eventId).orElseThrow(() ->
                new NotFoundException("Event c id - " + eventId + " не найден"));
    }

    @Transactional
    @Override
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequestDto updateEventAdminRequestDto) {
        Event event = findEventById(eventId);
        if (updateEventAdminRequestDto.getEventDate() != null
                && !updateEventAdminRequestDto.getEventDate().isAfter(LocalDateTime.now().plusHours(1))) {
            throw new ConflictException("Дата начала изменяемого события должна быть " +
                    "не ранее чем за час от текущего времени. Текущая дата события: "
                    + updateEventAdminRequestDto.getEventDate());
        }
        if (!StateEvent.PENDING.equals(event.getState())) {
            throw new ConflictException("Статус у события, которое планируется опубликовать/отклонить, " +
                    "должен быть PENDING. Текущий статус: " + event.getState());
        }
        if (updateEventAdminRequestDto.getStateAction() != null) {
            if (updateEventAdminRequestDto.getStateAction().equals(StateForUpdateEvent.PUBLISH_EVENT)) {
                event.setState(StateEvent.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            if (updateEventAdminRequestDto.getStateAction().equals(StateForUpdateEvent.REJECT_EVENT)) {
                event.setState(StateEvent.CANCELED);
            }
        }

        Event updatedEvent = updateCategoryAndLocation(updateEventAdminRequestDto, event);
        eventMapper.toUpdateEvent(updateEventAdminRequestDto, updatedEvent);
        return eventMapper.toEventFullDto(enrichEvent(eventRepository.save(updatedEvent)));
    }

    @Override
    public List<EventFullDto> findEventByParamsAdmin(EventAdminParamDto eventParamDto) {
        BooleanBuilder booleanBuilder = EventRepository.PredicatesForParamAdmin.build(eventParamDto);
        Pageable pageable = PageRequestUtil.of(eventParamDto.getFrom(),
                eventParamDto.getSize(), Sort.by("id").ascending());

        return enrichEvents(eventRepository.findAll(booleanBuilder, pageable).getContent()).stream()
                .map(eventMapper::toEventFullDto)
                .toList();
    }

    @Override
    public List<EventShortDto> findEventByParamsPublic(EventPublicParamsDto eventPublicParamsDto) {
        if (eventPublicParamsDto.getRangeEnd() != null && eventPublicParamsDto.getRangeStart() != null
                && eventPublicParamsDto.getRangeEnd().isBefore(eventPublicParamsDto.getRangeStart())) {
            throw new IllegalStateException("Дата RangeEnd не должна быть раньше даты RangeStart. RangeStart:"
                    + eventPublicParamsDto.getRangeStart() + ". RangeEnd:" + eventPublicParamsDto.getRangeEnd());
        }

        BooleanBuilder booleanBuilder = EventRepository.PredicatesForParamPublic.build(eventPublicParamsDto);
        List<Event> events = loadPublicEvents(booleanBuilder, eventPublicParamsDto);
        return events.stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }

    @Override
    public EventFullDto findPublicEventById(Long eventId, Long userId) {
        userServiceClient.getUser(userId);
        Event event = findEventById(eventId);
        if (!StateEvent.PUBLISHED.equals(event.getState())) {
            throw new NotFoundException("Событие не доступно. Статус события: " + event.getState());
        }
        try {
            statsClient.collectView(userId, eventId);
        } catch (Exception exception) {
            log.warn("Не удалось отправить VIEW в collector: {}", exception.getMessage());
        }
        return eventMapper.toEventFullDto(enrichEvent(event));
    }

    @Override
    public List<EventShortDto> getRecommendationsForUser(Long userId, int maxResults) {
        userServiceClient.getUser(userId);
        List<RecommendedEvent> recommendations;
        try {
            recommendations = statsClient.getRecommendationsForUser(userId, maxResults);
        } catch (Exception exception) {
            log.warn("Не удалось получить рекомендации из analyzer: {}", exception.getMessage());
            return List.of();
        }
        if (recommendations.isEmpty()) {
            return List.of();
        }

        List<Long> eventIds = recommendations.stream().map(RecommendedEvent::eventId).toList();
        Map<Long, Event> eventsById = enrichEvents(eventRepository.findEventsByIds(eventIds)).stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));

        List<EventShortDto> result = new ArrayList<>();
        for (RecommendedEvent recommendation : recommendations) {
            Event event = eventsById.get(recommendation.eventId());
            if (event != null && StateEvent.PUBLISHED.equals(event.getState())) {
                result.add(eventMapper.toEventShortDto(event));
            }
        }
        return result;
    }

    @Transactional
    @Override
    public void likeEvent(Long eventId, Long userId) {
        userServiceClient.getUser(userId);
        Event event = findEventById(eventId);
        if (!StateEvent.PUBLISHED.equals(event.getState())) {
            throw new NotFoundException("Событие не доступно. Статус события: " + event.getState());
        }
        if (!requestServiceClient.hasUserParticipation(userId, eventId)) {
            throw new IllegalStateException("Пользователь может лайкнуть только мероприятие, в котором участвовал");
        }
        statsClient.collectLike(userId, eventId);
    }

    @Override
    public Event findEventWithOutDto(Long userId, Long eventId) {
        return eventRepository.findEventByUserIdAndEventId(eventId, userId).orElseThrow(() ->
                new NotFoundException("Event c id - " + eventId + " не найден у пользователя с id - " + userId));
    }

    @Override
    public List<Event> findEventsByids(List<Long> eventsIds) {
        List<Event> events = eventRepository.findEventsByIds(eventsIds);
        if (events.isEmpty()) {
            throw new NotFoundException("Events c ids - " + eventsIds + " не найдены");
        }
        return enrichEvents(events);
    }

    @Transactional
    @Override
    public void saveEventWithRequest(Event event) {
        eventRepository.save(event);
    }

    @Override
    public EventInternalDto getInternalEvent(Long eventId) {
        Event event = findEventById(eventId);
        return EventInternalDto.builder()
                .id(event.getId())
                .initiatorId(event.getInitiatorId())
                .initiatorName(event.getInitiatorName())
                .confirmedRequests(event.getConfirmedRequests())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .build();
    }

    @Transactional
    @Override
    public void changeConfirmedRequests(Long eventId, long delta) {
        Event event = findEventById(eventId);
        long currentValue = Objects.requireNonNullElse(event.getConfirmedRequests(), 0L);
        event.setConfirmedRequests(Math.max(0L, currentValue + delta));
        eventRepository.save(event);
    }

    @Transactional
    private Event updateCategoryAndLocation(UpdateEventUserRequest updateEventUserRequest, Event event) {
        if (updateEventUserRequest.getCategory() != null) {
            Category category = categoryMapper.toCategory(
                    categoryService.getCategory(updateEventUserRequest.getCategory()));
            event.setCategory(category);
        }
        if (updateEventUserRequest.getLocation() != null) {
            Location location = locationRepository.save(locationMapper.toLocation(updateEventUserRequest.getLocation()));
            event.setLocation(location);
        }
        return event;
    }

    private List<Event> loadPublicEvents(BooleanBuilder booleanBuilder, EventPublicParamsDto params) {
        SortForParamPublicEvent sort = params.getSort();
        if (SortForParamPublicEvent.RATING.equals(sort) || SortForParamPublicEvent.VIEWS.equals(sort)) {
            return sortPublicEventsInMemory(booleanBuilder, params,
                    Comparator.comparing((Event event) -> Objects.requireNonNullElse(event.getRating(), 0.0))
                            .reversed()
                            .thenComparing(Event::getId, Comparator.reverseOrder()));
        }

        String sortProperty = SortForParamPublicEvent.EVENT_DATE.equals(sort) ? "eventDate" : "id";
        Pageable pageable = PageRequestUtil.of(params.getFrom(), params.getSize(), Sort.by(sortProperty).descending());
        return enrichEvents(eventRepository.findAll(booleanBuilder, pageable).getContent());
    }

    private List<Event> sortPublicEventsInMemory(BooleanBuilder booleanBuilder,
                                                 EventPublicParamsDto params,
                                                 Comparator<Event> comparator) {
        List<Event> events = StreamSupport.stream(eventRepository.findAll(booleanBuilder).spliterator(), false)
                .collect(Collectors.toCollection(ArrayList::new));
        enrichEvents(events).sort(comparator);
        return paginate(events, params.getFrom(), params.getSize());
    }

    private List<Event> paginate(List<Event> events, int from, int size) {
        if (from >= events.size()) {
            return List.of();
        }
        int toIndex = Math.min(events.size(), from + size);
        return List.copyOf(events.subList(from, toIndex));
    }

    private List<Event> enrichEvents(List<Event> events) {
        if (events.isEmpty()) {
            return List.of();
        }
        applyRatings(events);
        return events;
    }

    private Event enrichEvent(Event event) {
        enrichEvents(new ArrayList<>(List.of(event)));
        return event;
    }

    private void applyRatings(List<Event> events) {
        try {
            Map<Long, Double> ratings = statsClient.getInteractionsCount(events.stream().map(Event::getId).toList());
            for (Event event : events) {
                event.setRating(ratings.getOrDefault(event.getId(), 0.0));
            }
        } catch (Exception exception) {
            log.warn("Не удалось получить рейтинг событий: {}", exception.getMessage());
            for (Event event : events) {
                event.setRating(0.0);
            }
        }
    }

    private void applyDefaultEventState(Event event) {
        if (event.getCreatedOn() == null) {
            event.setCreatedOn(LocalDateTime.now());
        }
        if (event.getState() == null) {
            event.setState(StateEvent.PENDING);
        }
        if (event.getConfirmedRequests() == null) {
            event.setConfirmedRequests(0L);
        }
        if (event.getRating() == null) {
            event.setRating(0.0);
        }
    }
}
