package com.example.register.register.controller;

import com.example.register.register.service.ProcessService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;


@RestController
public class ProcessController {

    private final ProcessService processService;

    public ProcessController(ProcessService processService) {
        this.processService = processService;
    }

    @GetMapping("/api/processes")
    public List<String> getAllProcesses() {
        return processService.getAllProcesses();
    }

}
