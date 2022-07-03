package ru.ravel.qrRef.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.ravel.qrRef.enums.messageType;


@Setter
@Getter
@Builder
public class Message {
    private String key;
    private messageType messageType;
    private String message;
}
