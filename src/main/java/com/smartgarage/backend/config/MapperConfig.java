package com.smartgarage.backend.config;

import com.smartgarage.backend.mapper.BookingMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class MapperConfig {

    private final ApplicationContext context;

    public MapperConfig(ApplicationContext context) {
        this.context = context;
    }

    @PostConstruct
    public void init() {
        BookingMapper.setApplicationContext(context);
    }
}
