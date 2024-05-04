package ru.ravel.qrref.controller

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import ru.ravel.qrref.dto.Message
import ru.ravel.qrref.service.KeyService
import ru.ravel.qrref.service.QrService
import ru.ravel.qrref.service.SocketService

@Controller
@CrossOrigin
class MainController {

	QrService qrService
	KeyService keyService
	SocketService socketService

	MainController(QrService qrService, KeyService keyService, SocketService socketService) {
		this.qrService = qrService
		this.keyService = keyService
		this.socketService = socketService
	}

	@GetMapping("/")
	String main() {
		return "index"
	}

	@GetMapping("/favicon.ico")
	ResponseEntity<Object> getFavicon() {
		return new ResponseEntity<>(HttpStatus.OK)
	}

	@GetMapping("/getKey")
	ResponseEntity<Object> getKey() {
		String key = keyService.generateKey(20)
		return ResponseEntity.ok().body(key)
	}

	@GetMapping("/getQr/{key}")
	ResponseEntity<Object> getQr(HttpServletResponse response, @PathVariable("key") String key) {
		qrService.getQr(key, response)
		return new ResponseEntity<>(HttpStatus.OK)
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{key}")
	ResponseEntity<Object> postKey(@PathVariable("key") String key, @RequestParam("text") String text) {
		Message message = Message.builder()
				.key(key)
				.message(text)
				.messageType(MessageType.TEXT)
				.build()
		socketService.sendStrToFront(message)
		return new ResponseEntity<>(HttpStatus.OK)
	}
}