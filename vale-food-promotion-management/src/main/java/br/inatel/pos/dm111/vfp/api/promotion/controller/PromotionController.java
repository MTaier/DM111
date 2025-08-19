package br.inatel.pos.dm111.vfp.api.promotion.controller;

import br.inatel.pos.dm111.vfp.api.core.ApiException;
import br.inatel.pos.dm111.vfp.api.promotion.PromotionRequest;
import br.inatel.pos.dm111.vfp.api.promotion.PromotionResponse;
import br.inatel.pos.dm111.vfp.api.promotion.service.PromotionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/valefood/promotions")
public class PromotionController {

    private static final Logger log = LoggerFactory.getLogger(PromotionController.class);

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping
    public ResponseEntity<List<PromotionResponse>> searchPromotions(
            @RequestParam(name = "restaurantId", required = false) String restaurantId) throws ApiException {
        if (restaurantId != null && !restaurantId.isBlank()) {
            return ResponseEntity.ok(promotionService.searchPromotionsByRestaurant(restaurantId));
        }
        return ResponseEntity.ok(promotionService.searchPromotions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponse> searchPromotion(@PathVariable String id) throws ApiException {
        return ResponseEntity.ok(promotionService.searchPromotion(id));
    }

    @PostMapping
    public ResponseEntity<PromotionResponse> createPromotion(@RequestBody PromotionRequest request)
            throws ApiException {
        var response = promotionService.createPromotion(request);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromotionResponse> updatePromotion(@PathVariable String id,
            @RequestBody PromotionRequest request) throws ApiException {
        var response = promotionService.updatePromotion(request, id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removePromotion(@PathVariable String id) throws ApiException {
        promotionService.removePromotion(id);
        return ResponseEntity.noContent().build();
    }
}