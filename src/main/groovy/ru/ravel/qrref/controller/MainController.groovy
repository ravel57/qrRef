package ru.ravel.qrref.controller

import com.ibm.icu.text.Transliterator
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.ravel.qrref.dto.Message
import ru.ravel.qrref.dto.MessageType
import ru.ravel.qrref.service.KeyService
import ru.ravel.qrref.service.QrService
import ru.ravel.qrref.service.SocketService

import java.nio.file.Files
import java.nio.file.Path

@Controller
@CrossOrigin
class MainController {

    QrService qrService
    KeyService keyService
    SocketService socketService
    Logger logger = LoggerFactory.getLogger(this.class)

    Map<String, File> fileMap = new HashMap<>()


    MainController(QrService qrService, KeyService keyService, SocketService socketService) {
        this.qrService = qrService
        this.keyService = keyService
        this.socketService = socketService
    }

    @GetMapping("/")
    String main() {
        return "index"
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
                response.addHeader("Content-Disposition", "attachment; filename=" + translateFileName(path))
                response.addHeader("Access-Control-Expose-Headers", "Content-Disposition")
                Files.copy(path, response.getOutputStream())
                response.getOutputStream().flush()
                path.toFile().delete()
            }
        } catch (IOException e) {
            logger.error(e.message)
        }
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
}