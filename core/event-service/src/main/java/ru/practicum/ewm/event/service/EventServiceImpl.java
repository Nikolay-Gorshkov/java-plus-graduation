package ru.practicum.ewm.event.service;

import com.querydsl.core.BooleanBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.korshunov.statsclient.StatsClient;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.client.dto.EventInternalDto;
import ru.practicum.ewm.client.rating.RatingServiceClient;
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
import ru.practicum.ewm.rating.dto.RatingSummaryDto;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.util.PageRequestUtil;
import statsdto.HitDto;
import statsdto.StatDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final RatingServiceClient ratingServiceClient;
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
    public List<EventShortDto> findEventByParamsPublic(EventPublicParamsDto eventPublicParamsDto,
                                                       HttpServletRequest request) {
        if (eventPublicParamsDto.getRangeEnd() != null && eventPublicParamsDto.getRangeStart() != null
                && eventPublicParamsDto.getRangeEnd().isBefore(eventPublicParamsDto.getRangeStart())) {
            throw new IllegalStateException("Дата RangeEnd не должна быть раньше даты RangeStart. RangeStart:"
                    + eventPublicParamsDto.getRangeStart() + ". RangeEnd:" + eventPublicParamsDto.getRangeEnd());
        }

        BooleanBuilder booleanBuilder = EventRepository.PredicatesForParamPublic.build(eventPublicParamsDto);
        List<Event> events = loadPublicEvents(booleanBuilder, eventPublicParamsDto);
        addViewEvent(request);

        return events.stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }

    @Override
    public EventFullDto findPublicEventById(Long eventId, HttpServletRequest request) {
        Event event = findEventById(eventId);
        if (!StateEvent.PUBLISHED.equals(event.getState())) {
            throw new NotFoundException("Событие не доступно. Статус события: " + event.getState());
        }
        addViewEvent(request);
        return eventMapper.toEventFullDto(enrichEvent(event));
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
        if (SortForParamPublicEvent.RATING.equals(sort)) {
            return sortPublicEventsInMemory(booleanBuilder, params,
                    Comparator.comparing((Event event) -> Objects.requireNonNullElse(event.getRating(), 0L))
                            .reversed()
                            .thenComparing(Event::getId, Comparator.reverseOrder()));
        }
        if (SortForParamPublicEvent.VIEWS.equals(sort)) {
            return sortPublicEventsInMemory(booleanBuilder, params,
                    Comparator.comparing((Event event) -> Objects.requireNonNullElse(event.getViews(), 0L))
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
        applyViews(events);
        applyRatings(events);
        return events;
    }

    private Event enrichEvent(Event event) {
        enrichEvents(new ArrayList<>(List.of(event)));
        return event;
    }

    private void applyViews(List<Event> events) {
        Map<Long, String> eventsUri = events.stream()
                .collect(Collectors.toMap(Event::getId, e -> "/events/" + e.getId()));

        try {
            List<StatDto> statDtos = statsClient.getStats(
                    LocalDateTime.of(2020, 1, 1, 0, 0),
                    LocalDateTime.of(2050, 1, 1, 0, 0),
                    List.copyOf(eventsUri.values()),
                    true
            );

            Map<Long, StatDto> statDtoMap = statDtos.stream()
                    .filter(stat -> stat.getUri().substring(stat.getUri().lastIndexOf("/") + 1).matches("\\d+"))
                    .collect(Collectors.toMap(
                            stat -> Long.parseLong(stat.getUri().substring(stat.getUri().lastIndexOf("/") + 1)),
                            Function.identity()
                    ));

            for (Event event : events) {
                StatDto stat = statDtoMap.get(event.getId());
                event.setViews(stat != null ? stat.getHits() : 0L);
            }
        } catch (Exception exception) {
            log.warn("Не удалось получить статистику просмотров: {}", exception.getMessage());
            for (Event event : events) {
                event.setViews(0L);
            }
        }
    }

    private void applyRatings(List<Event> events) {
        try {
            Map<Long, RatingSummaryDto> ratings = ratingServiceClient.getSummaries(
                            events.stream().map(Event::getId).toList())
                    .stream()
                    .collect(Collectors.toMap(RatingSummaryDto::getEventId, Function.identity()));

            for (Event event : events) {
                RatingSummaryDto ratingSummary = ratings.get(event.getId());
                event.setRating(ratingSummary != null ? ratingSummary.getRating() : 0L);
            }
        } catch (Exception exception) {
            log.warn("Не удалось получить рейтинг событий: {}", exception.getMessage());
            for (Event event : events) {
                event.setRating(0L);
            }
        }
    }

    private void addViewEvent(HttpServletRequest httpServletRequest) {
        try {
            statsClient.addHit(HitDto.builder()
                    .app("event-service")
                    .uri(httpServletRequest.getRequestURI())
                    .ip(httpServletRequest.getRemoteAddr())
                    .build());
        } catch (Exception exception) {
            log.warn("Не удалось сохранить hit в stats-service: {}", exception.getMessage());
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
        if (event.getViews() == null) {
            event.setViews(0L);
        }
        if (event.getRating() == null) {
            event.setRating(0L);
        }
    }
}
