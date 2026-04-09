package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.client.request.RequestServiceClient;
import ru.practicum.ewm.client.user.UserServiceClient;
import ru.practicum.ewm.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.RequestDTO;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventWithRequestImpl implements EventWithRequest {

    private final RequestServiceClient requestServiceClient;
    private final UserServiceClient userServiceClient;

    @Override
    public EventRequestStatusUpdateResult updateRequestUser(Long userId, Long eventId,
                                                            EventRequestStatusUpdateRequest request) {
        userServiceClient.getUser(userId);
        return requestServiceClient.updateEventRequests(userId, eventId, request);
    }

    @Override
    public List<RequestDTO> getEventRequest(Long userId, Long eventId) {
        userServiceClient.getUser(userId);
        return requestServiceClient.getEventRequests(userId, eventId);
    }
}
