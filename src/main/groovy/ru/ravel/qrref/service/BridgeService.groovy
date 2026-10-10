package ru.ravel.qrref.service

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class BridgeService {

	private final ConcurrentHashMap<String, String> roomByKey = new ConcurrentHashMap<>()

	String roomOf(String key) {
		return roomByKey.get(key)
	}

	/**
	 * Связывает два ключа в одну комнату. Если любой из них уже был в другой комнате, та комната
	 * расформировывается целиком, иначе у бывшего собеседника остался бы «мёртвый» мост.
	 * Возвращает null, если ключи пустые или одинаковые.
	 */
	synchronized String createRoom(String a, String b) {
		if (!a || !b || a == b) return null
		dropRoom(a)
		dropRoom(b)
		def roomId = makeRoomId(a, b)
		roomByKey.put(a, roomId)
		roomByKey.put(b, roomId)
		return roomId
	}

	/** Выводит из комнаты всех её участников (а не только переданный ключ). */
	synchronized void dropRoom(String key) {
		def roomId = roomByKey.remove(key)
		if (roomId) {
			roomByKey.entrySet().removeIf { it.value == roomId }
		}
	}

	static String makeRoomId(String a, String b) {
		return (a <= b) ? "${a}_${b}" : "${b}_${a}"
	}
}
