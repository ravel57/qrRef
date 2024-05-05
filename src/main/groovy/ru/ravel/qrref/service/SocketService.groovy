package ru.ravel.qrref.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import ru.ravel.qrref.dto.Message

@Service
class SocketService {

	@Autowired
	SimpMessagingTemplate simpMessaging

	void sendStrToFront(Message message) {
		simpMessaging.convertAndSend("/topic/activity/${message.key}".toString(), message.message)
	}
}