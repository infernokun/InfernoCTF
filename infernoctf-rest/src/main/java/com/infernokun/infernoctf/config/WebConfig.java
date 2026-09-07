package com.infernokun.infernoctf.config;

import com.infernokun.infernoctf.logger.InfernoCTFLogger;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Deliberately no @EnableWebMvc: it disables Boot's MVC auto-configuration. CORS lives in
// SecurityConfig so it applies inside the security filter chain.
@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<InfernoCTFLogger> loggingFilter() {
        FilterRegistrationBean<InfernoCTFLogger> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new InfernoCTFLogger());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
