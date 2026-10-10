package ru.ravel.qrref.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import ru.ravel.qrref.dto.Message

@Service
class SocketService {

	private final SimpMessagingTemplate simpMessaging

	SocketService(SimpMessagingTemplate simpMessaging) {
		this.simpMessaging = simpMessaging
	}

	void sendStrToFront(Message message) {
		simpMessaging.convertAndSend("/topic/activity/${message.key}".toString(), message.message)
	}

	void sendRoomToFront(String key, String roomId) {
		simpMessaging.convertAndSend("/topic/bridge/${key}".toString(), roomId)
	}

	void sendToRoom(String roomId, Object payload) {
		simpMessaging.convertAndSend("/topic/room/${roomId}".toString(), payload)
	}
}