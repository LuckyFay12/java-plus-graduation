package ru.practicum.client;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.grpc.stats.action.UserActionProto;
import ru.practicum.grpc.stats.collector.UserActionControllerGrpc;

@Slf4j
@Component
public class CollectorClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub userActionClient;

    public void sendUserAction(UserActionProto userAction) {
        try {
            log.info("Отправка действия пользователя в Collector: userId={}, eventId={}, action={}",
                    userAction.getUserId(),
                    userAction.getEventId(),
                    userAction.getActionType());

            userActionClient.collectUserAction(userAction);
            log.debug("Действие пользователя успешно отправлено в Collector");

        } catch (StatusRuntimeException e) {
            log.error("Ошибка отправки действия пользователя в Collector: {}", e.getStatus().getDescription());
        }
    }
}