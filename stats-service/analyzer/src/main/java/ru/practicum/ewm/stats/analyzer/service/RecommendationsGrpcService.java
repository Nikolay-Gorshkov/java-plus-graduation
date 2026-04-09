package ru.practicum.ewm.stats.analyzer.service;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import stats.service.dashboard.InteractionsCountRequestProto;
import stats.service.dashboard.RecommendationsControllerGrpc;
import stats.service.dashboard.RecommendedEventProto;
import stats.service.dashboard.SimilarEventsRequestProto;
import stats.service.dashboard.UserPredictionsRequestProto;

@GrpcService
@RequiredArgsConstructor
public class RecommendationsGrpcService extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationService recommendationService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        recommendationService.getRecommendationsForUser(request.getUserId(), request.getMaxResults())
                .forEach(score -> responseObserver.onNext(toProto(score)));
        responseObserver.onCompleted();
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        recommendationService.getSimilarEvents(request.getEventId(), request.getUserId(), request.getMaxResults())
                .forEach(score -> responseObserver.onNext(toProto(score)));
        responseObserver.onCompleted();
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        recommendationService.getInteractionsCount(request.getEventIdList())
                .forEach(score -> responseObserver.onNext(toProto(score)));
        responseObserver.onCompleted();
    }

    private RecommendedEventProto toProto(RecommendationService.EventScore score) {
        return RecommendedEventProto.newBuilder()
                .setEventId(score.eventId())
                .setScore(score.score())
                .build();
    }
}
