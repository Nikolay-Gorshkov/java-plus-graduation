package ru.practicum.ewm.event.mapper;

import org.mapstruct.*;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.status.StateEvent;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @BeanMapping(qualifiedByName = "event")
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "user.id", target = "initiatorId")
    @Mapping(source = "user.name", target = "initiatorName")
    @Mapping(target = "category", expression = "java(category)")
    @Mapping(target = "location", expression = "java(location)")
    @Mapping(target = "compilations", ignore = true)
    Event toEvent(NewEventDto newEventDto, UserShortDto user, Category category, Location location);


    @Named("event")
    @AfterMapping
    default void setDefaultCreatedOn(@MappingTarget Event event) {
        event.setCreatedOn(LocalDateTime.now());
        event.setState(StateEvent.PENDING);
        event.setConfirmedRequests(0L);
        event.setRating(0.0);
    }

    @Mapping(target = "initiator", expression = "java(toUserShortDto(event))")
    EventFullDto toEventFullDto(Event event);

    @Mapping(target = "initiator", expression = "java(toUserShortDto(event))")
    EventShortDto toEventShortDto(Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, qualifiedByName = "updateEvent")
    @Mapping(target = "category", ignore = true)
    void toUpdateEvent(UpdateEventUserRequest updateEventUserRequest, @MappingTarget Event event);

    default UserShortDto toUserShortDto(Event event) {
        return UserShortDto.builder()
                .id(event.getInitiatorId())
                .name(event.getInitiatorName())
                .build();
    }
}
