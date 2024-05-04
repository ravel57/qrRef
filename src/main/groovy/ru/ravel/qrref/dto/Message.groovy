package ru.ravel.qrref.dto

import groovy.transform.builder.Builder
import lombok.Data

@Data
@Builder
class Message {
	String key
	MessageType messageType
	String message
}
