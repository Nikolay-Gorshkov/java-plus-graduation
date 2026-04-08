package ru.practicum.ewm.event.controller.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.client.dto.EventInternalDto;
import ru.practicum.ewm.event.service.EventService;

@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
public class InternalEventController {

    private final EventService eventService;

    @GetMapping("/{eventId}")
    public EventInternalDto getEvent(@PathVariable Long eventId) {
        return eventService.getInternalEvent(eventId);
    }

    @PostMapping("/{eventId}/confirmed-requests")
    public void changeConfirmedRequests(@PathVariable Long eventId,
                                        @RequestParam long delta) {
        eventService.changeConfirmedRequests(eventId, delta);
    }
}
