package com.mypolicy.implementation.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class PortalController {

    @GetMapping(value = {"/insurer-portal", "/insurer-portal/"})
    public ResponseEntity<Resource> insurerPortal() throws IOException {
        Resource resource = new ClassPathResource("static/insurer-portal/index.html");
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }
}
