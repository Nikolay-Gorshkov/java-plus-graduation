package ru.practicum.ewm.event.service;

import ru.practicum.ewm.client.dto.EventInternalDto;
import ru.practicum.ewm.event.dto.EventAdminParamDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventPublicParamsDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequestDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.model.Event;

import java.util.List;

public interface EventService {

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> findEventByUserId(Long userId, int from, int size);

    EventFullDto findEventByIdAndEventId(Long userId, Long eventId);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    Event findEventById(Long eventId);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequestDto updateEventAdminRequestDto);

    List<EventFullDto> findEventByParamsAdmin(EventAdminParamDto eventParamDto);

    List<EventShortDto> findEventByParamsPublic(EventPublicParamsDto eventPublicParamsDto);

    EventFullDto findPublicEventById(Long eventId, Long userId);

    List<EventShortDto> getRecommendationsForUser(Long userId, int maxResults);

    void likeEvent(Long eventId, Long userId);

    Event findEventWithOutDto(Long userId, Long eventId);

    void saveEventWithRequest(Event event);

    List<Event> findEventsByids(List<Long> eventsIds);

    EventInternalDto getInternalEvent(Long eventId);

    void changeConfirmedRequests(Long eventId, long delta);

}
