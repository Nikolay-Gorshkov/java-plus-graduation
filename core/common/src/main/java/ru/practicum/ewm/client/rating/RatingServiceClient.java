package ru.practicum.ewm.client.rating;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.ewm.rating.dto.RatingSummaryDto;

import java.util.List;

@FeignClient(name = "rating-service")
public interface RatingServiceClient {

    @PostMapping("/internal/ratings/summary")
    List<RatingSummaryDto> getSummaries(@RequestBody List<Long> eventIds);
}
