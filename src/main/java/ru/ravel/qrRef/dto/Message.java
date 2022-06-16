package ru.ravel.qrRef.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.ravel.qrRef.enums.messageType;


@Setter
@Getter
@Builder
public class Message {
    public String key;
    public messageType messageType;
    public String message;
}
