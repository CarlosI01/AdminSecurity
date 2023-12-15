package ec.com.cognoware.adminSecurity.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/hello-1")
    @PreAuthorize("hasRole('admin_client_rol')")
    public String helloAdmin(){
        return "Hello Sprig Boot With Keycloak with ADMIN";
    }

    @GetMapping("/hello-2")
    @PreAuthorize("hasRole('client_user') or hasRole('client_user')")
    public String helloUser(){
        return "Hello Sprig Boot With Keycloak with USER";
    }

}
