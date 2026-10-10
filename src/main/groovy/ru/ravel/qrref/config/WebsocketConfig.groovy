package ru.ravel.qrref.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer


@Configuration
@EnableWebSocketMessageBroker
class WebsocketConfig implements WebSocketMessageBrokerConfigurer {

	String url = System.getenv("url")

	@Override
	void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic")
		config.setApplicationDestinationPrefixes("/app")
	}

	@Override
	void registerStompEndpoints(StompEndpointRegistry registry) {
		// Свой домен (переменная окружения url) допускаем и по https, и по http; запросы с того же
		// хоста (с учётом заголовков nginx) проходят проверку Origin в любом случае.
		String[] origins = url ? ["https://${url}".toString(), "http://${url}".toString()] as String[] : [] as String[]
		registry.addEndpoint("/websocket").setAllowedOriginPatterns(origins).withSockJS()
	}

}