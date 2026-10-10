package ru.ravel.qrref.service

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap


/**
 * Файлы, переданные в чате «Обмен». В отличие от одноразовых файлов вкладки «Отправка» их можно скачать
 * несколько раз (отправитель видит своё сообщение так же, как собеседник); удаляются по истечении срока.
 */
@Service
class RoomFileService {

	static final long TTL_MILLIS = 60L * 60 * 1000

	static class RoomFile {
		String name
		String contentType
		long size
		Path path
		long expiresAt
	}

	private final KeyService keyService
	private final Path directory = Files.createTempDirectory("qrref-room")
	private final ConcurrentHashMap<String, RoomFile> files = new ConcurrentHashMap<>()

	RoomFileService(KeyService keyService) {
		this.keyService = keyService
	}

	/** Сохраняет файл и возвращает его секретный идентификатор. */
	String store(MultipartFile multipartFile) {
		removeExpired()
		String id = keyService.generateKey(40)
		Path path = directory.resolve(id)
		multipartFile.transferTo(path)
		files.put(id, new RoomFile(
				name: safeName(multipartFile.getOriginalFilename()),
				contentType: multipartFile.getContentType() ?: "application/octet-stream",
				size: multipartFile.getSize(),
				path: path,
				expiresAt: System.currentTimeMillis() + TTL_MILLIS
		))
		return id
	}

	RoomFile find(String id) {
		removeExpired()
		return id ? files.get(id) : null
	}

	private void removeExpired() {
		long now = System.currentTimeMillis()
		files.entrySet().removeIf { entry ->
			boolean expired = entry.value.expiresAt < now
			if (expired) Files.deleteIfExists(entry.value.path)
			return expired
		}
	}

	/** Имя приходит от клиента: убираем пути и управляющие символы. */
	static String safeName(String original) {
		String name = (original ?: "").replace("\\", "/")
		name = name.substring(name.lastIndexOf('/') + 1)
		name = name.replaceAll(/[\p{Cntrl}]/, "").trim()
		if (!name || name == "." || name == "..") name = "file"
		return name.length() > 200 ? name.substring(0, 200) : name
	}
}
