package com.warehouse.wms.controller;

import com.warehouse.wms.repository.SkuRepository;
import com.warehouse.wms.repository.InventoryRepository;
import com.warehouse.wms.service.AiAssistantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@Validated
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('INVENTORY_VIEW')")
public class AiController {

    private final AiAssistantService aiAssistantService;
    private final SkuRepository skuRepository;
    private final InventoryRepository inventoryRepository;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@Valid @RequestBody ChatRequest request) {
        String response = aiAssistantService.ask(request.prompt());
        return ResponseEntity.ok(Map.of("response", response));
    }

    @GetMapping("/forecast")
    public ResponseEntity<List<Map<String, Object>>> getForecast() {
        var skus = skuRepository.findAll();
        var forecasts = aiAssistantService.generateForecast(skus, inventoryRepository);
        return ResponseEntity.ok(forecasts);
    }

    public record ChatRequest(@NotBlank(message = "prompt is required") String prompt) {
    }
}
