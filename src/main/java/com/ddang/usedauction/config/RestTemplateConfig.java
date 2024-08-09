package com.ddang.usedauction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    // restTemplate: rest 방식으로 api를 호출할 수 있는 클래스(http 요청을 하는 클래스)
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
