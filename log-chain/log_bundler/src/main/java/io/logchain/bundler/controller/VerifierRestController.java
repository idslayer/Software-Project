package io.logchain.bundler.controller;

import io.logchain.bundler.ElasticService;
import io.logchain.bundler.MerkleAlgorithm;
import io.logchain.bundler.dto.VerifyResp;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/verifier")
@RequiredArgsConstructor
public class VerifierRestController {
    final ElasticService elasticService;
    final MerkleAlgorithm merkleVerifier;

    @GetMapping("/searchLogs")
    public ResponseEntity searchLogs(
            @RequestParam(required = false) String hash,
            @RequestParam(required = false) String message,
            @RequestParam long startTsMillis,
            @RequestParam long endTsMillis,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws Exception {
        var rs = elasticService.searchLogs(startTsMillis, endTsMillis, message, hash, page, size);
        return ResponseEntity.ok(rs);
    }

    @GetMapping("/verify")
    public ResponseEntity<VerifyResp> verifyMerkleProof(@RequestParam String logHash) {
        try {
            VerifyResp resp = merkleVerifier.verifyByHashAndBatchId(logHash);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

}
