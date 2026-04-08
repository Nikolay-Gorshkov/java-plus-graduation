package ru.practicum.ewm.request.service;

import ru.practicum.ewm.request.dto.RequestDTO;
import ru.practicum.ewm.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.event.dto.EventRequestStatusUpdateResult;

import java.util.List;

public interface RequestService {

    RequestDTO addRequestCurrentUser(Long userId, Long eventId);

    List<RequestDTO> getRequestsCurrentUser(Long userId);

    RequestDTO cancelRequestCurrentUser(Long userId, Long requestId);

    List<RequestDTO> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequests(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest request);
}
