package io.logchain.bundler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.logchain.bundler.dto.VerifyResp;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MerkleAlgorithm {
    final ElasticService elasticService;
    final ObjectMapper mapper;
    /**
     * The computeMerkleRoot method calculates a Merkle root from a list of leaf hashes:
     * It starts with the input list of leaf hashes.
     * In each round, it pairs adjacent hashes, concatenates them, and hashes the result to form the next level.
     * If the number of hashes is odd, the last hash is duplicated and paired with itself.
     * This process repeats until only one hash remains, which is the Merkle root.
     * This ensures a binary Merkle tree structure, even for odd-sized input lists.
     */
    public static String computeMerkleRoot(List<String> leaves) throws Exception {
        List<String> current = new ArrayList<>(leaves);
        while (current.size() > 1) {
            List<String> next = new ArrayList<>();
            for (int i = 0; i < current.size(); i += 2) {
                if (i + 1 < current.size()) {
                    next.add(computeHash(current.get(i) + current.get(i + 1)));
                } else {
                    next.add(computeHash(current.get(i) + current.get(i)));
                }
            }
            current = next;
        }
        return current.get(0);
    }

    public static String computeHash(String data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashBytes);
    }

    /**
     * Verifies if a given hash exists in the batch logs for a batchId.
     * Queries all logs in the batch using ElasticService, gets their hashes, and checks if the given hash is present.
     *
     * @param hashToVerify Base64-encoded hash to verify
     * @return true if hash is found in batch logs
     */
    public VerifyResp verifyByHashAndBatchId(String hashToVerify) throws Exception {
        List<Map<String, Object>> anchors = elasticService.queryAnchorInSameBatchByHash(hashToVerify);

        if (anchors.isEmpty()) {
            return VerifyResp.builder().isValid(false).build();
        }
        List<String> paths = anchors.stream().filter(it -> !it.get("log").equals(hashToVerify))
                .map(it -> it.get("log").toString()).toList();
        boolean isRight = false;
        List<Boolean> isRights = new ArrayList<>();
        for (Map anchor : anchors) {
            if (((Map) anchor.get("log")).get("hash").equals(hashToVerify)) {
                isRight = true;
                continue;
            }
            isRights.add(isRight);
        }
        List<String> leafs = anchors.stream()
                .map(it -> ((Map) it.get("log")).get("hash").toString())
                .toList();

        var anchor = anchors.get(0);

        String verifiedRoot = computeMerkleRoot(leafs);
        boolean isValid = Objects.equals(verifiedRoot, anchor.get("merkleRoot"));
        return VerifyResp.builder()
                .isValid(isValid)
                .root(anchor.get("merkleRoot").toString())
                .batchId(anchor.get("batchId").toString())
                .leafHash(paths)
                .build();
    }

}
