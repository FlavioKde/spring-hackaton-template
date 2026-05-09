package com.interhack.spring_hackaton_template.controller;

import com.interhack.spring_hackaton_template.dto.PackingTaskDTO;
import com.interhack.spring_hackaton_template.service.PackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packing")
@RequiredArgsConstructor
public class PackingController {

    private final PackingService packingService;

    @GetMapping("/tasks")
    public ResponseEntity<List<PackingTaskDTO>> getPendingTasks() {
        return ResponseEntity.ok(packingService.getPendingPackingTasks());
    }

    @GetMapping("/tasks/{routePlanId}")
    public ResponseEntity<PackingTaskDTO> getPackingTask(@PathVariable Long routePlanId) {
        return ResponseEntity.ok(packingService.getPackingTask(routePlanId));
    }

    @PostMapping("/pallets/{palletId}/loaded")
    public ResponseEntity<Void> markPalletLoaded(@PathVariable Long palletId) {
        packingService.markPalletLoaded(palletId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/pallets/{palletId}/unloaded")
    public ResponseEntity<Void> markPalletUnloaded(@PathVariable Long palletId) {
        packingService.markPalletUnloaded(palletId);
        return ResponseEntity.ok().build();
    }
}
