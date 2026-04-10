package ru.practicum.ewm.client.request;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.ewm.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.RequestDTO;

import java.util.List;

@FeignClient(name = "request-service")
public interface RequestServiceClient {

    @GetMapping("/internal/users/{userId}/events/{eventId}/requests")
    List<RequestDTO> getEventRequests(@PathVariable("userId") Long userId,
                                      @PathVariable("eventId") Long eventId);

    @PostMapping("/internal/users/{userId}/events/{eventId}/requests")
    EventRequestStatusUpdateResult updateEventRequests(@PathVariable("userId") Long userId,
                                                       @PathVariable("eventId") Long eventId,
                                                       @RequestBody EventRequestStatusUpdateRequest request);

    @GetMapping("/internal/users/{userId}/events/{eventId}/requests/exists")
    boolean hasUserParticipation(@PathVariable("userId") Long userId,
                                 @PathVariable("eventId") Long eventId);
}
