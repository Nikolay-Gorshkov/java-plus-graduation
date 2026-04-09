package ru.practicum.ewm.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.status.StateEvent;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventInternalDto {
    private Long id;
    private Long initiatorId;
    private String initiatorName;
    private Long confirmedRequests;
    private Long participantLimit;
    private Boolean requestModeration;
    private StateEvent state;
}
