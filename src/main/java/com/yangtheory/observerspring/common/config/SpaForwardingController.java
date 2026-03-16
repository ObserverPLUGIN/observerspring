package com.yangtheory.observerspring.common.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardingController {

    @GetMapping({
            "/",
            "/login",
            "/register",
            "/reset-password",
            "/verify-email",
            "/players",
            "/main-preview",
            "/login-preview",
            "/register-preview",
            "/reset-password-preview",
            "/verify-email-preview"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
