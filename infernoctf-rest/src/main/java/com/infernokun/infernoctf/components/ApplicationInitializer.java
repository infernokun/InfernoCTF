package com.infernokun.infernoctf.components;

import com.infernokun.infernoctf.config.InfernoCTFConfig;
import com.infernokun.infernoctf.models.entities.User;
import com.infernokun.infernoctf.models.enums.Role;
import com.infernokun.infernoctf.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
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
        String username = infernoCTFConfig.getDefaultAdminUsername();
        try {
            userService.findUserByUsername(username);
        } catch (UsernameNotFoundException ex) {
            // Unset means generate one and print it once, so an unconfigured deployment
            // never ships with a known credential.
            String password = infernoCTFConfig.getDefaultAdminPassword();
            boolean generated = password == null || password.isBlank();
            if (generated) {
                password = randomPassword();
            }

            User admin = new User();
            admin.setRole(Role.ADMIN);
            admin.setUsername(username);
            admin.setEmail(username + "@infernoctf.local");
            admin.setPassword(password);
            userService.registerUser(admin);

            if (generated) {
                log.warn("""
                        No INFERNOCTF_DEFAULTADMINPASSWORD was set, so a random bootstrap password \
                        was generated for the '{}' account: {}
                        Log in, change it, and set the variable. This is printed once and cannot be \
                        recovered.""", username, password);
            } else {
                log.info("Created the default admin account '{}'.", username);
            }
        }
    }

    private static String randomPassword() {
        byte[] raw = new byte[24];
        new SecureRandom().nextBytes(raw);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
    }
}
