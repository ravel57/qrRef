package ru.ravel.qrref.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
@EnableWebMvc
@ComponentScan
class MvcConfig implements WebMvcConfigurer {

	@Override
	void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("index")
		registry.addViewController("/privacy").setViewName("forward:/privacy.html")
	}


	@Override
	void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**")
			.addResourceLocations("classpath:/static/")
	}

}