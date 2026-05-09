package com.interhack.spring_hackaton_template.controller;

import com.interhack.spring_hackaton_template.model.LoadPlan;
import com.interhack.spring_hackaton_template.service.LoadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loads")
@RequiredArgsConstructor
public class LoadController {

    private final LoadService loadService;

    @GetMapping("/route/{routePlanId}")
    public ResponseEntity<LoadPlan> getLoadPlan(@PathVariable Long routePlanId) {
        return ResponseEntity.ok(loadService.getLoadPlan(routePlanId));
    }
}
