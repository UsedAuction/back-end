package com.ddang.usedauction.health;

import com.ddang.usedauction.auction.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final AuctionRepository auctionRepository;

    @GetMapping
    public ResponseEntity<String> healthCheck() {

        boolean isExist = auctionRepository.existsById(1L);
        if (isExist) {
            return ResponseEntity.ok("exist");
        } else {
            return ResponseEntity.ok("not exist");
        }
    }
}
