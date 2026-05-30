package org.memnar.backend.memnarjar.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // This tells Spring Boot: "If a browser asks for a .js or .css file, 
        // look for it inside the external 'libraries' folder in the project root."
        registry.addResourceHandler("/*.js", "/*.css")
                .addResourceLocations("file:./libraries/", "file:./libraries/gd3_mutmtx/");
    }
}