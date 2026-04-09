package ru.practicum.ewm.client.event;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.client.dto.EventInternalDto;

@FeignClient(name = "event-service")
public interface EventServiceClient {

    @GetMapping("/internal/events/{eventId}")
    EventInternalDto getEvent(@PathVariable("eventId") Long eventId);

    @PostMapping("/internal/events/{eventId}/confirmed-requests")
    void changeConfirmedRequests(@PathVariable("eventId") Long eventId,
                                 @RequestParam("delta") long delta);
}
