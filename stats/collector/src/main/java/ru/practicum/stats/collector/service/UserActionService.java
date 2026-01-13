package ru.practicum.stats.collector.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.stats.action.UserActionProto;
import ru.practicum.stats.collector.config.KafkaTopic;

import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserActionService {
    private final KafkaUserActionProducer kafkaProducer;

    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        ActionTypeAvro actionType;
        switch (request.getActionType()) {
            case ACTION_VIEW -> actionType = ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> actionType = ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> actionType = ActionTypeAvro.LIKE;
            default -> throw new IllegalArgumentException("Unknown action type: " + request.getActionType());
        }
        UserActionAvro avro = UserActionAvro.newBuilder()
                .setUserId(request.getUserId())
                .setActionType(actionType)
                .setEventId(request.getEventId())
                .setTimestamp(Instant.ofEpochSecond(request.getTimestamp().getSeconds(), request.getTimestamp().getNanos()))
                .build();
        kafkaProducer.send(avro, KafkaTopic.USER_ACTIONS);
    }
}