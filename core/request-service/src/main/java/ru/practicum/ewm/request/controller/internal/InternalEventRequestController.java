package ru.practicum.ewm.request.controller.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.RequestDTO;
import ru.practicum.ewm.request.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/internal/users/{userId}/events/{eventId}/requests")
@RequiredArgsConstructor
public class InternalEventRequestController {

    private final RequestService requestService;

    @GetMapping
    public List<RequestDTO> getEventRequests(@PathVariable Long userId,
                                             @PathVariable Long eventId) {
        return requestService.getEventRequests(userId, eventId);
    }

    @PostMapping
    public EventRequestStatusUpdateResult updateEventRequests(@PathVariable Long userId,
                                                              @PathVariable Long eventId,
                                                              @RequestBody EventRequestStatusUpdateRequest request) {
        return requestService.updateEventRequests(userId, eventId, request);
    }
}
