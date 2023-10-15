package com.joshzook.alternateimages.configurations;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.joshzook.alternateimages.utilties.BufferedImageSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.awt.image.BufferedImage;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizeJackson() {
        return (builder) -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(BufferedImage.class, new BufferedImageSerializer());
            builder.modules(module);
        };
    }
}
