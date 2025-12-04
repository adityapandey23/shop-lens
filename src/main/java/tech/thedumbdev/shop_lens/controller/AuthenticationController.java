package tech.thedumbdev.shop_lens.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    @PostMapping("/sign-up")
    public void signUp() {
    }

    @PostMapping("/sign-in")
    public void signIn() {
    }

    @PostMapping("/refresh")
    public void refresh() {
    }

    @PostMapping("/email-verify")
    public void emailVerify() {

    }

}
