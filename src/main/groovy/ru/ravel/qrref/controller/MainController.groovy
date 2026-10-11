package ru.ravel.qrref.controller

import com.ibm.icu.text.Transliterator
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.ravel.qrref.dto.Message
import ru.ravel.qrref.dto.MessageType
import ru.ravel.qrref.service.BridgeService
import ru.ravel.qrref.service.KeyService
import ru.ravel.qrref.service.QrService
import ru.ravel.qrref.service.RoomFileService
import ru.ravel.qrref.service.SocketService

import java.nio.file.Files
import java.nio.file.Path


@Controller
@CrossOrigin
class MainController {

	private final QrService qrService
	private final KeyService keyService
	private final SocketService socketService
	private final BridgeService bridgeService
	private final RoomFileService roomFileService

	Logger logger = LoggerFactory.getLogger(this.class)

	Map<String, File> fileMap = new HashMap<>()


	MainController(QrService qrService, KeyService keyService, SocketService socketService, BridgeService bridgeService,
				   RoomFileService roomFileService) {
		this.qrService = qrService
		this.keyService = keyService
		this.socketService = socketService
		this.bridgeService = bridgeService
		this.roomFileService = roomFileService
	}

	@GetMapping("/")
	String main() {
		return "index"
	}


	@GetMapping("/getKey")
	ResponseEntity<Object> getKey(HttpSession session) {
		def existing = (session.getAttribute("qrref_key") ?: "").toString().trim()
		if (existing) {
			return ResponseEntity.ok().body(existing)
		}
		String key = keyService.generateKey(20)
		session.setAttribute("qrref_key", key)
		return ResponseEntity.ok().body(key)
	}


	/** Выдаёт сессии новый ключ; старый перестаёт принимать данные, а связь с собеседником разрывается. */
	@PostMapping("/resetKey")
	ResponseEntity<Object> resetKey(HttpSession session) {
		def old = (session.getAttribute("qrref_key") ?: "").toString().trim()
		if (old) {
			closeRoom(old)
		}
		String key = keyService.generateKey(20)
		session.setAttribute("qrref_key", key)
		return ResponseEntity.ok().body(key)
	}


	@GetMapping("/getQr/{key}")
	ResponseEntity<Object> getQr(HttpServletResponse response, @PathVariable("key") String key) {
		qrService.getQr(key, response)
		return new ResponseEntity<>(HttpStatus.OK)
	}


	@PostMapping(value = "/text/{key}")
	ResponseEntity<Object> postString(@PathVariable("key") String key, @RequestBody Map<String, String> map) {
		String text = map.text
		Message message = Message.builder()
				.key(key)
				.message(text)
				.messageType(MessageType.TEXT)
				.build()
		socketService.sendStrToFront(message)
		return new ResponseEntity<>(HttpStatus.OK)
	}


	@PostMapping(value = "/file/{key}", consumes = ["multipart/form-data"])
	@ResponseBody
	ResponseEntity<Object> postFile(@PathVariable("key") String key,
									@RequestParam(value = "file", required = false) MultipartFile multipartFile) {
		String generateKey = keyService.generateKey(45)
		File file = convert(multipartFile)
		fileMap.put(generateKey, file)
		Message message = Message.builder()
				.key(key)
				.message("file/$generateKey")
				.messageType(MessageType.FILE)
				.build()
		socketService.sendStrToFront(message)
		return new ResponseEntity<>(HttpStatus.OK)
	}


	@GetMapping("/file/{key}")
	void downloadFileBySecretKey(@PathVariable("key") String key, HttpServletResponse response) {
		try {
			Path path = fileMap[key].toPath()
			if (path != null & Files.exists(path)) {
				response.setContentType("application/other")
				response.setContentLengthLong(Files.size(path))
				response.setCharacterEncoding("UTF-8")
				response.addHeader("Content-Disposition", "attachment; filename=${translateFileName(path)}")
				response.addHeader("Access-Control-Expose-Headers", "Content-Disposition")
				Files.copy(path, response.getOutputStream())
				response.getOutputStream().flush()
				path.toFile().delete()
			}
		} catch (IOException e) {
			logger.error(e.message)
		}
	}

	@PostMapping("/bridge")
	ResponseEntity<Object> createBridge(@RequestBody Map<String, String> body) {
		String a = (body?.a ?: "").trim()
		String b = (body?.b ?: "").trim()

		if (!isValidKey(a) || !isValidKey(b)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: "bad_keys"])
		}
		if (a == b) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: "same_key"])
		}
		def roomId = bridgeService.createRoom(a, b)
		socketService.sendRoomToFront(a, roomId)
		socketService.sendRoomToFront(b, roomId)
		return ResponseEntity.ok([ok: true, room: roomId])
	}

	/**
	 * Комната ключа. «Ещё не сопряжено» — штатное состояние, а не ошибка, поэтому отвечаем 200 с пустым room:
	 * клиенты опрашивают этот адрес, а поток 404 заполняет логи и срабатывает на fail2ban/CrowdSec (бан по IP).
	 */
	@GetMapping("/room/{key}")
	ResponseEntity<Object> room(@PathVariable("key") String key) {
		def roomId = bridgeService.roomOf(key?.trim())
		return ResponseEntity.ok([room: roomId ?: ""])
	}

	@PostMapping(value = "/room/file/{key}", consumes = ["multipart/form-data"])
	ResponseEntity<Object> postRoomFile(@PathVariable("key") String key,
										@RequestParam(value = "file", required = false) MultipartFile multipartFile) {
		String k = (key ?: "").trim()
		def roomId = bridgeService.roomOf(k)
		if (!roomId) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body([error: "not_paired"])
		}
		if (multipartFile == null || multipartFile.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: "no_file"])
		}
		String id = roomFileService.store(multipartFile)
		def file = roomFileService.find(id)
		socketService.sendToRoom(roomId, [
				type: "file", from: k, name: file.name, size: file.size,
				path: "room/file/${id}".toString(), ts: System.currentTimeMillis()
		])
		return ResponseEntity.ok([ok: true, room: roomId])
	}

	@GetMapping("/room/file/{id}")
	void downloadRoomFile(@PathVariable("id") String id, HttpServletResponse response) {
		def file = roomFileService.find(id)
		if (file == null || !Files.exists(file.path)) {
			response.sendError(HttpStatus.NOT_FOUND.value())
			return
		}
		String encoded = URLEncoder.encode(file.name, "UTF-8").replace("+", "%20")
		String ascii = file.name.replaceAll(/[^\x20-\x7E]/, "_").replace("\"", "_")
		response.setContentType(file.contentType)
		response.setContentLengthLong(file.size)
		response.addHeader("Content-Disposition", "attachment; filename=\"${ascii}\"; filename*=UTF-8''${encoded}".toString())
		response.addHeader("Access-Control-Expose-Headers", "Content-Disposition")
		Files.copy(file.path, response.getOutputStream())
		response.getOutputStream().flush()
	}

	/** Завершает чат: комната расформировывается, оба участника получают сообщение `closed`. */
	@PostMapping("/room/leave/{key}")
	ResponseEntity<Object> leaveRoom(@PathVariable("key") String key) {
		closeRoom((key ?: "").trim())
		return ResponseEntity.ok([ok: true])
	}

	@PostMapping("/room/text/{key}")
	ResponseEntity<Object> postRoomText(@PathVariable("key") String key, @RequestBody Map<String, String> body) {
		String k = (key ?: "").trim()
		String text = (body?.text ?: "").toString()

		def roomId = bridgeService.roomOf(k)
		if (!roomId) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body([error: "not_paired"])
		}
		socketService.sendToRoom(roomId, [type: "text", from: k, text: text, ts: System.currentTimeMillis()])
		return ResponseEntity.ok([ok: true, room: roomId])
	}

	/** Ключ попадает в адрес STOMP-топика, поэтому пускаем только безопасные символы. */
	private static boolean isValidKey(String key) {
		return key ==~ /[A-Za-z0-9_-]{1,128}/
	}

	private File convert(MultipartFile multipartFile) {
		File file = new File(multipartFile.getOriginalFilename())
		file.createNewFile()
		try (OutputStream os = new FileOutputStream(file)) {
			os.write(multipartFile.getBytes())
		} catch (e) {
			logger.error(e.toString())
		}
		return file
	}

	static String translateFileName(Path file) {
		return Transliterator.getInstance("Russian-Latin/BGN")
				.transliterate(file.getFileName().toString().replace(" ", "_"))
	}

	/** Сообщает участникам комнаты, что чат закрыт, и расформировывает её. */
	private void closeRoom(String key) {
		def roomId = bridgeService.roomOf(key)
		if (roomId) {
			socketService.sendToRoom(roomId, [type: "closed", from: key, ts: System.currentTimeMillis()])
		}
		bridgeService.dropRoom(key)
	}
}