package ru.ravel.qrref.config

import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.ViewResolver
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.view.InternalResourceViewResolver
import org.springframework.web.servlet.view.JstlView


@Configuration
@EnableWebMvc
@ComponentScan
class MvcConfig implements WebMvcConfigurer {

	@Override
	void configureViewResolvers(ViewResolverRegistry registry) {
		registry.jsp("/WEB-INF/jsp/", ".jsp")
	}


	@Override
	void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable()
	}


	@Bean
	WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> enableDefaultServlet() {
		return (factory) -> factory.setRegisterDefaultServlet(true)
	}


	@Override
	void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/webapp/**")
				.addResourceLocations("/webapp/")
				.setCachePeriod(null)
	}


	@Bean
	ViewResolver jspViewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver()
		resolver.setViewClass(JstlView.class)
		resolver.setPrefix("/WEB-INF/jsp/")
		resolver.setSuffix(".jsp")
		return resolver
	}
}