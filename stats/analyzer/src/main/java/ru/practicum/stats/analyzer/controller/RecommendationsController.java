package ru.practicum.stats.analyzer.controller;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.grpc.stats.analyzer.RecommendationsControllerGrpc;
import ru.practicum.grpc.stats.recommendation.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;
import ru.practicum.grpc.stats.recommendation.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.recommendation.UserPredictionsRequestProto;
import ru.practicum.stats.analyzer.service.RecommendationsService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final RecommendationsService service;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("GetRecommendationsForUser: userId={}, maxResults={}",
                    request.getUserId(), request.getMaxResults());

            service.getRecommendationsForUser(request).forEach(responseObserver::onNext);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error in GetRecommendationsForUser", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("GetSimilarEvents: eventId={}, userId={}, maxResults={}",
                    request.getEventId(), request.getUserId(), request.getMaxResults());

            service.getSimilarEvents(request).forEach(responseObserver::onNext);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error in GetSimilarEvents", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("GetInteractionsCount: eventIds={}", request.getEventIdList());

            service.getInteractionsCount(request).forEach(responseObserver::onNext);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error in GetInteractionsCount", e);
            responseObserver.onError(e);
        }
    }
}
