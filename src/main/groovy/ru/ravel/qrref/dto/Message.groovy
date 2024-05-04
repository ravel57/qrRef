package ru.ravel.qrref.dto

import groovy.transform.builder.Builder
import lombok.Getter
import lombok.Setter

@Setter
@Getter
@Builder
class Message {
	String key
	ru.ravel.qrref.enums.MessageType messageType
	String message
}
