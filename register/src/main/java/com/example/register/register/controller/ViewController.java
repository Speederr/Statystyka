package com.example.register.register.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {


    @GetMapping("/processes")
    public String showProcessesPage() {
        return "processes";
    }
}
