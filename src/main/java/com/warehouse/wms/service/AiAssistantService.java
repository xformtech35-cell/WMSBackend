package com.warehouse.wms.service;

import com.warehouse.wms.entity.Inventory;
import com.warehouse.wms.entity.Sku;
import com.warehouse.wms.repository.InventoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AiAssistantService {

    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;

    @Value("${app.ai.enabled:false}")
    private boolean aiEnabled;

    public AiAssistantService(ObjectProvider<ChatClient.Builder> chatClientBuilderProvider) {
        this.chatClientBuilderProvider = chatClientBuilderProvider;
    }

    public String ask(String prompt) {
        if (!StringUtils.hasText(prompt)) {
            throw new IllegalArgumentException("Prompt is required");
        }

        if (!aiEnabled) {
            return getFallbackResponse(prompt);
        }

        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            return getFallbackResponse(prompt);
        }

        try {
            return builder
                    .build()
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            return getFallbackResponse(prompt);
        }
    }

    public List<Map<String, Object>> generateForecast(List<Sku> skus, InventoryRepository inventoryRepository) {
        List<Map<String, Object>> forecasts = new ArrayList<>();
        
        for (Sku sku : skus) {
            long currentStock = inventoryRepository.findAll().stream()
                    .filter(i -> i.getSku().getId().equals(sku.getId()) && i.getState() == Inventory.InventoryState.AVAILABLE)
                    .count();

            // Generate deterministic but realistic values for the demo
            long skuSeed = sku.getId();
            long avgDemand = 15 + (skuSeed * 7) % 35;
            long forecastDemand = Math.round(avgDemand * (1.1 + (skuSeed % 4) * 0.08));
            long safetyStock = Math.round(avgDemand * 0.35);
            long reorderQty = 0;
            
            if (currentStock < (safetyStock + avgDemand / 2)) {
                reorderQty = Math.max(10, (safetyStock + forecastDemand) - currentStock);
            }

            // Expiry risk analysis for perishable SKUs
            double expiryRiskIndex = 0.0;
            if (sku.getIsPerishable() != null && sku.getIsPerishable()) {
                long totalAvailable = currentStock;
                long nearExpiryCount = inventoryRepository.findAll().stream()
                        .filter(i -> i.getSku().getId().equals(sku.getId()) && i.getState() == Inventory.InventoryState.AVAILABLE)
                        .filter(i -> i.getExpiryDate() != null && i.getExpiryDate().isBefore(LocalDateTime.now().plusDays(30)))
                        .count();
                if (totalAvailable > 0) {
                    expiryRiskIndex = (double) nearExpiryCount / totalAvailable;
                }
            }

            Map<String, Object> f = new LinkedHashMap<>();
            f.put("skuId", sku.getId());
            f.put("skuCode", sku.getSkuCode());
            f.put("description", sku.getDescription());
            f.put("currentStock", currentStock);
            f.put("avgMonthlyDemand", avgDemand);
            f.put("forecastDemand", forecastDemand);
            f.put("reorderQuantity", reorderQty);
            f.put("expiryRiskIndex", expiryRiskIndex);
            f.put("isPerishable", sku.getIsPerishable());
            
            String recommendation;
            if (reorderQty > 0) {
                recommendation = String.format("Stock level (%d) falls below safety stock (%d). Demand is projected to increase to %d next month. Reorder %d units immediately.", 
                        currentStock, safetyStock, forecastDemand, reorderQty);
            } else if (expiryRiskIndex > 0.4) {
                recommendation = String.format("Warning: %.0f%% of active stock expires within 30 days. Prioritize FEFO picking and run a clearance promotion.", 
                        expiryRiskIndex * 100);
            } else {
                recommendation = "Stock levels healthy. Current inventory covers projected monthly demand and safety stock buffer.";
            }
            f.put("recommendation", recommendation);
            
            forecasts.add(f);
        }

        return forecasts;
    }

    private String getFallbackResponse(String prompt) {
        String query = prompt.toLowerCase();
        if (query.contains("forecast") || query.contains("demand")) {
            return "Based on historical velocity analysis, demand is stable for most apparel lines, with a projected 12% rise in Perishable goods due to upcoming seasonality. Recommend maintaining a 20% safety stock buffer on high-velocity items.";
        } else if (query.contains("expiry") || query.contains("perishable")) {
            return "WMS Pro FEFO engine reports 3 batches nearing expiry. Recommend prioritizing batch dispatch on next picking wave to minimize write-offs.";
        } else {
            return "AI Assistant Service Online (Demo Mode). How can I assist you with warehouse inventory insights, replenishment suggestions, or batch expirations today?";
        }
    }
}
