package com.tt.mj;

import config.BeanConfig;
import config.WebMvcConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@Import({BeanConfig.class, WebMvcConfig.class})
public class MidjourneyApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MidjourneyApiApplication.class, args);
    }

}
