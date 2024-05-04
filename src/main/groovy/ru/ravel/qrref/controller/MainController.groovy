package ru.ravel.qrref.controller

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import ru.ravel.qrref.dto.Message
import ru.ravel.qrref.enums.MessageType
import ru.ravel.qrref.service.KeyService
import ru.ravel.qrref.service.QrService
import ru.ravel.qrref.service.SocketService

@Controller
@CrossOrigin
class MainController {

	@Autowired
	QrService qrService
	@Autowired
	KeyService keyService
	@Autowired
	SocketService socketService

	@GetMapping("/")
	String getRoot() {
		return "Main"
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
	ResponseEntity<Object> getQr(HttpServletResponse response,
								 @PathVariable("key") String key) {
		qrService.getQr(key, response)
		return new ResponseEntity<>(HttpStatus.OK)
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{key}")
	ResponseEntity<Object> postKey(@PathVariable("key") String key,
								   @RequestParam("text") String text) {
		Message message = Message.builder()
				.key(key)
				.message(text)
				.messageType(MessageType.TEXT)
				.build()
		socketService.sendStrToFront(message)
		return new ResponseEntity<>(HttpStatus.OK)
	}
}