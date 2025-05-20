package com.infernokun.config;

import com.infernokun.logger.InfernoCTFLogger;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("OPTIONS", "HEAD", "PUT", "POST", "DELETE", "PATH")
                .allowCredentials(true)
                .allowedHeaders("*")
                .allowedOriginPatterns("*");
    }

    @Bean
    public FilterRegistrationBean<InfernoCTFLogger> loggingFilter() {
        FilterRegistrationBean<InfernoCTFLogger> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new InfernoCTFLogger());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
