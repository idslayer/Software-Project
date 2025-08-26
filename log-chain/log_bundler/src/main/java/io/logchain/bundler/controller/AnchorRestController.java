package io.logchain.bundler.controller;

import io.logchain.bundler.ElasticService;
import lombok.RequiredArgsConstructor;
import org.hyperledger.fabric.client.Contract;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/anchor")
@RequiredArgsConstructor
public class AnchorRestController {
    private final Contract contract;
    final ElasticService elasticService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            byte[] result = contract.evaluateTransaction("ListAnchors");
            return ResponseEntity.ok(new String(result, StandardCharsets.UTF_8));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> putAnchor(@RequestBody Map<String, Object> req) {
        try {
            String batchId = (String) req.get("batchId");
            String root = (String) req.get("root");
            long count = ((Number) req.get("count")).longValue();
            long startTsNanos = ((Number) req.get("startTsNanos")).longValue();
            long endTsNanos = ((Number) req.get("endTsNanos")).longValue();
            String prevRoot = (String) req.getOrDefault("prevRoot", "");
            String txNote = (String) req.getOrDefault("txNote", "");
            byte[] result = contract.submitTransaction("PutAnchor",
                    batchId,
                    root,
                    String.valueOf(count),
                    String.valueOf(startTsNanos),
                    String.valueOf(endTsNanos),
                    prevRoot,
                    txNote);
            return ResponseEntity.ok(new String(result, StandardCharsets.UTF_8));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/batch")
    public ResponseEntity<?> getAnchor(@RequestParam String batchId) {
        try {
            byte[] result = contract.evaluateTransaction("GetAnchor", batchId);
            return ResponseEntity.ok(new String(result, StandardCharsets.UTF_8));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{batchId}/exists")
    public ResponseEntity<?> exists(@PathVariable String batchId) {
        try {
            byte[] result = contract.evaluateTransaction("Exists", batchId);
            boolean exists = new String(result, StandardCharsets.UTF_8).equalsIgnoreCase("true");
            return ResponseEntity.ok(Map.of("exists", exists));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<?> latestAnchor() {
        try {
            byte[] result = contract.evaluateTransaction("LatestAnchor");
            return ResponseEntity.ok(new String(result, StandardCharsets.UTF_8));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
