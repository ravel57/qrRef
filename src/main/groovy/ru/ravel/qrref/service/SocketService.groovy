package ru.ravel.qrref.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class SocketService {

	@Autowired
	SimpMessagingTemplate simpMessaging

	void sendStrToFront(ru.ravel.qrref.dto.Message message) {
		simpMessaging.convertAndSend("/topic/activity/" + message.getKey(), message.getMessage())
	}
}