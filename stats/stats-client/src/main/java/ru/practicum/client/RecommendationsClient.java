package ru.practicum.client;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.grpc.stats.analyzer.RecommendationsControllerGrpc;
import ru.practicum.grpc.stats.recommendation.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;
import ru.practicum.grpc.stats.recommendation.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.recommendation.UserPredictionsRequestProto;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@Component
public class RecommendationsClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub client;

    public Stream<RecommendedEventProto> getRecommendationsForUser(long userId, long maxResults) {
        try {
            UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();
            Iterator<RecommendedEventProto> iterator = client.getRecommendationsForUser(request);
            return asStream(iterator);
        } catch (StatusRuntimeException e) {
            log.error("Failed to get recommendations for user: userId={}", userId, e);
            throw new RuntimeException("gRPC call failed", e);
        }
    }

    public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, long maxResults) {
        try {
            SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                    .setEventId(eventId)
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();
            Iterator<RecommendedEventProto> iterator = client.getSimilarEvents(request);
            return asStream(iterator);
        } catch (StatusRuntimeException e) {
            log.error("Failed to get similar events: eventId={}, userId={}", eventId, userId, e);
            throw new RuntimeException("gRPC call failed", e);
        }
    }

    public Stream<RecommendedEventProto> getInteractionsCount(Set<Long> eventIds) {
        try {
            InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                    .addAllEventId(eventIds)
                    .build();
            Iterator<RecommendedEventProto> iterator = client.getInteractionsCount(request);
            return asStream(iterator);
        } catch (StatusRuntimeException e) {
            log.error("Failed to get interactions count: eventIds={}", eventIds, e);
            throw new RuntimeException("gRPC call failed", e);
        }
    }

    public Stream<RecommendedEventProto> getInteractionsCount(Long eventId) {
        return getInteractionsCount(Collections.singleton(eventId));
    }

    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false);
    }
}
