package ru.practicum.ewm.stats.collector.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.AvroSerde;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import stats.service.collector.ActionTypeProto;
import stats.service.collector.UserActionControllerGrpc;
import stats.service.collector.UserActionProto;

import java.time.Instant;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserActionCollectorService extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final KafkaTemplate<Long, byte[]> kafkaTemplate;

    @Value("${stats.kafka.topics.user-actions}")
    private String userActionsTopic;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        UserActionAvro action = new UserActionAvro(
                request.getUserId(),
                request.getEventId(),
                toAvro(request.getActionType()),
                toInstant(request)
        );

        kafkaTemplate.send(userActionsTopic, action.userId(), AvroSerde.serialize(action));
        log.debug("Collected user action: user={}, event={}, action={}",
                action.userId(), action.eventId(), action.actionType());

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private Instant toInstant(UserActionProto request) {
        if (!request.hasTimestamp()) {
            return Instant.now();
        }
        return Instant.ofEpochSecond(request.getTimestamp().getSeconds(), request.getTimestamp().getNanos());
    }

    private ActionTypeAvro toAvro(ActionTypeProto actionType) {
        return switch (actionType) {
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            case ACTION_VIEW, UNRECOGNIZED -> ActionTypeAvro.VIEW;
        };
    }
}
