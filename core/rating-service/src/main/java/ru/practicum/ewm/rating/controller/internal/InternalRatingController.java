package ru.practicum.ewm.rating.controller.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.rating.dto.RatingSummaryDto;
import ru.practicum.ewm.rating.service.RatingService;

import java.util.List;

@RestController
@RequestMapping("/internal/ratings")
@RequiredArgsConstructor
public class InternalRatingController {

    private final RatingService ratingService;

    @PostMapping("/summary")
    public List<RatingSummaryDto> getSummaries(@RequestBody List<Long> eventIds) {
        return ratingService.getSummaries(eventIds);
    }
}
