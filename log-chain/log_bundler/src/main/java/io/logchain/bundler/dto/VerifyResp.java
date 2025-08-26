package io.logchain.bundler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class VerifyResp {
    boolean isValid;
    String batchId;
    String root;
    List<String> leafHash;
}
