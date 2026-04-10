package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.korshunov.statsclient.StatsClient;
import ru.practicum.ewm.client.dto.EventInternalDto;
import ru.practicum.ewm.client.event.EventServiceClient;
import ru.practicum.ewm.client.user.UserServiceClient;
import ru.practicum.ewm.error.ConflictException;
import ru.practicum.ewm.error.NotFoundException;
import ru.practicum.ewm.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.event.status.StateEvent;
import ru.practicum.ewm.request.dto.RequestDTO;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserServiceClient userServiceClient;
    private final EventServiceClient eventServiceClient;
    private final RequestMapper requestMapper;
    private final StatsClient statsClient;

    @Transactional
    @Override
    public RequestDTO addRequestCurrentUser(Long userId, Long eventId) {
        userServiceClient.getUser(userId);
        EventInternalDto event = eventServiceClient.getEvent(eventId);

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Данный запрос существует.");
        }
        if (userId.equals(event.getInitiatorId())) {
            throw new ConflictException("Невозможно создать запрос на участие в своем же событии.");
        }
        if (!StateEvent.PUBLISHED.equals(event.getState())) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии.");
        }
        if (event.getParticipantLimit() != 0
                && Objects.requireNonNullElse(event.getConfirmedRequests(), 0L) >= event.getParticipantLimit()) {
            throw new ConflictException("У события достигнут лимит запросов на участие.");
        }

        Request request = Request.builder()
                .created(LocalDateTime.now())
                .requesterId(userId)
                .eventId(eventId)
                .build();

        if (!event.getRequestModeration() || Objects.equals(event.getParticipantLimit(), 0L)) {
            request.setRequestStatus(RequestStatus.CONFIRMED);
            eventServiceClient.changeConfirmedRequests(eventId, 1);
        } else {
            request.setRequestStatus(RequestStatus.PENDING);
        }

        Request savedRequest = requestRepository.save(request);
        try {
            statsClient.collectRegister(userId, eventId);
        } catch (Exception exception) {
            log.warn("Не удалось отправить событие регистрации в collector: {}", exception.getMessage());
        }
        return requestMapper.toRequestDTO(savedRequest);
    }

    @Override
    public List<RequestDTO> getRequestsCurrentUser(Long userId) {
        userServiceClient.getUser(userId);
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(requestMapper::toRequestDTO)
                .toList();
    }

    @Transactional
    @Override
    public RequestDTO cancelRequestCurrentUser(Long userId, Long requestId) {
        userServiceClient.getUser(userId);
        Request request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Запрос с id - %d не найден.", requestId)));

        if (RequestStatus.CONFIRMED.equals(request.getRequestStatus())) {
            throw new ConflictException("Нельзя отменить уже подтвержденную заявку на участие.");
        }
        request.setRequestStatus(RequestStatus.CANCELED);

        return requestMapper.toRequestDTO(requestRepository.save(request));
    }

    @Override
    public List<RequestDTO> getEventRequests(Long userId, Long eventId) {
        userServiceClient.getUser(userId);
        ensureEventOwnedByUser(userId, eventId);
        return requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::toRequestDTO)
                .toList();
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateEventRequests(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        userServiceClient.getUser(userId);
        EventInternalDto event = ensureEventOwnedByUser(userId, eventId);
        List<Request> requestList = requestRepository.findAllByIdInAndEventId(request.getRequestIds(), eventId);

        if (requestList.isEmpty()) {
            throw new NotFoundException("Запросы(Request) с переданными ids: "
                    + request.getRequestIds() + " не найдены:");
        }
        long confirmedCounter = Objects.requireNonNullElse(event.getConfirmedRequests(), 0L);

        if (!Objects.equals(event.getParticipantLimit(), 0L)
                && confirmedCounter >= event.getParticipantLimit()
                && request.getStatus().equals(RequestStatus.CONFIRMED)) {
            throw new ConflictException("У события достигнут лимит запросов на участие.");
        }

        for (Request req : requestList) {
            if (!req.getRequestStatus().equals(RequestStatus.PENDING)) {
                throw new ConflictException("Заявку c Id: " + req.getId()
                        + " можно одобрить, если у нее статус: " + RequestStatus.PENDING);
            }
        }

        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();
        if ((!event.getRequestModeration() || Objects.equals(event.getParticipantLimit(), 0L))
                && request.getStatus().equals(RequestStatus.CONFIRMED)) {
            for (Request req : requestList) {
                req.setRequestStatus(RequestStatus.CONFIRMED);
                confirmedRequests.add(req);
                confirmedCounter++;
            }
        } else if ((!event.getRequestModeration() || Objects.equals(event.getParticipantLimit(), 0L))
                && request.getStatus().equals(RequestStatus.REJECTED)) {
            for (Request req : requestList) {
                req.setRequestStatus(RequestStatus.REJECTED);
                rejectedRequests.add(req);
            }
        } else if (request.getStatus().equals(RequestStatus.REJECTED)) {
            for (Request req : requestList) {
                req.setRequestStatus(RequestStatus.REJECTED);
                rejectedRequests.add(req);
            }
        } else {
            for (Request req : requestList) {
                if (Objects.equals(confirmedCounter, event.getParticipantLimit())) {
                    req.setRequestStatus(RequestStatus.REJECTED);
                    rejectedRequests.add(req);
                } else {
                    req.setRequestStatus(RequestStatus.CONFIRMED);
                    confirmedRequests.add(req);
                    confirmedCounter++;
                }
            }
        }

        requestRepository.saveAll(requestList);
        long delta = confirmedRequests.size();
        if (delta > 0) {
            eventServiceClient.changeConfirmedRequests(eventId, delta);
        }

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequests.stream().map(requestMapper::toRequestDTO).toList())
                .rejectedRequests(rejectedRequests.stream().map(requestMapper::toRequestDTO).toList())
                .build();
    }

    private EventInternalDto ensureEventOwnedByUser(Long userId, Long eventId) {
        EventInternalDto event = eventServiceClient.getEvent(eventId);
        if (!userId.equals(event.getInitiatorId())) {
            throw new NotFoundException("Event c id - " + eventId + " не найден у пользователя с id - " + userId);
        }
        return event;
    }

    @Override
    public boolean hasUserParticipation(Long userId, Long eventId) {
        userServiceClient.getUser(userId);
        eventServiceClient.getEvent(eventId);
        return requestRepository.existsByRequesterIdAndEventIdAndRequestStatusIn(
                userId,
                eventId,
                List.of(RequestStatus.CONFIRMED)
        );
    }
}
