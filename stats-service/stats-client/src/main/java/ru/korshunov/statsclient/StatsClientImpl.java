package ru.korshunov.statsclient;

import com.google.protobuf.Timestamp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import stats.service.collector.ActionTypeProto;
import stats.service.collector.UserActionControllerGrpc;
import stats.service.collector.UserActionProto;
import stats.service.dashboard.InteractionsCountRequestProto;
import stats.service.dashboard.RecommendationsControllerGrpc;
import stats.service.dashboard.RecommendedEventProto;
import stats.service.dashboard.SimilarEventsRequestProto;
import stats.service.dashboard.UserPredictionsRequestProto;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class StatsClientImpl implements StatsClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub collectorClient;

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub analyzerClient;

    @Override
    public void collectUserAction(long userId, long eventId, ActionType actionType) {
        Instant now = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();

        UserActionProto request = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(toProto(actionType))
                .setTimestamp(timestamp)
                .build();

        collectorClient.collectUserAction(request);
    }

    @Override
    public List<RecommendedEvent> getRecommendationsForUser(long userId, int maxResults) {
        UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        return asStream(analyzerClient.getRecommendationsForUser(request))
                .map(this::toRecommendedEvent)
                .toList();
    }

    @Override
    public List<RecommendedEvent> getSimilarEvents(long eventId, long userId, int maxResults) {
        SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        return asStream(analyzerClient.getSimilarEvents(request))
                .map(this::toRecommendedEvent)
                .toList();
    }

    @Override
    public Map<Long, Double> getInteractionsCount(List<Long> eventIds) {
        InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();
        return asStream(analyzerClient.getInteractionsCount(request))
                .collect(Collectors.toMap(RecommendedEventProto::getEventId, RecommendedEventProto::getScore));
    }

    private ActionTypeProto toProto(ActionType actionType) {
        return switch (actionType) {
            case VIEW -> ActionTypeProto.ACTION_VIEW;
            case REGISTER -> ActionTypeProto.ACTION_REGISTER;
            case LIKE -> ActionTypeProto.ACTION_LIKE;
        };
    }

    private RecommendedEvent toRecommendedEvent(RecommendedEventProto proto) {
        return new RecommendedEvent(proto.getEventId(), proto.getScore());
    }

    private <T> java.util.stream.Stream<T> asStream(Iterator<T> iterator) {
        return StreamSupport.stream(
                java.util.Spliterators.spliteratorUnknownSize(iterator, java.util.Spliterator.ORDERED),
                false
        );
    }
}
