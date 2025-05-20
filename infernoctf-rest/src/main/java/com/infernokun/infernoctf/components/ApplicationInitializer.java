package com.infernokun.infernoctf.components;

import com.infernokun.infernoctf.config.InfernoCTFConfig;
import com.infernokun.infernoctf.models.entities.User;
import com.infernokun.infernoctf.models.enums.Role;
import com.infernokun.infernoctf.services.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class ApplicationInitializer implements ApplicationRunner {

    private final UserService userService;
    private final InfernoCTFConfig infernoCTFConfig;

    public ApplicationInitializer(UserService userService, InfernoCTFConfig infernoCTFConfig) {
        this.userService = userService;
        this.infernoCTFConfig = infernoCTFConfig;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            User user = this.userService.findUserByUsername(infernoCTFConfig.getDefaultAdminUsername());
        } catch (UsernameNotFoundException ex) {
            User admin = new User();
            admin.setRole(Role.ADMIN);
            admin.setUsername(infernoCTFConfig.getDefaultAdminUsername());
            admin.setEmail(infernoCTFConfig.getDefaultAdminUsername() + "@infernoctf.local");
            admin.setPassword(infernoCTFConfig.getDefaultAdminPassword());

            this.userService.registerUser(admin);
        }
    }
}
