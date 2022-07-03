package ru.ravel.qrRef.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.ravel.qrRef.dto.Message;

@Service
public class SocketService {

    @Autowired
    private SimpMessagingTemplate simpMessaging;

    public void sendStrToFront(Message message) {
        simpMessaging.convertAndSend("/topic/activity/"+message.getKey(), message.getMessage());
    }
}
