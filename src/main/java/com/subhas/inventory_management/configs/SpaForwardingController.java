package com.subhas.inventory_management.configs;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class SpaForwardingController {

    @GetMapping(value = "/{path:[^\\.]*}")
    public String forwardRoot(@PathVariable String path) {
        return "forward:/index.html";
    }

    @GetMapping(value = "/{path:[^\\.]*}/**")
    public String forwardNested(@PathVariable String path) {
        return "forward:/index.html";
    }
}