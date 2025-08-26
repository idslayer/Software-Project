package io.logchain.anchor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Contract(
        name = "anchor",
        info = @Info(
                title = "Immutable Log Anchor Contract",
                description = "Stores Merkle-root anchors for batched logs",
                version = "1.0.0"
        )
)
@Default
public class AnchorContract implements ContractInterface {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String PutAnchor(final Context ctx,
                            final String batchId,
                            final String root,
                            final long count,
                            final long startTsMillis,
                            final long endTsMillis,
                            final long processMillis,
                            final String prevRoot,
                            final String txNote) {

        requireNonEmpty(batchId, "batchId");
        requireNonEmpty(root, "root");
        if (count <= 0) throw new ChaincodeException("count must be > 0");
        if (endTsMillis < startTsMillis) throw new ChaincodeException("endTs < startTs");

        ChaincodeStub stub = ctx.getStub();

        String key = Keys.anchorKey(ctx, batchId);
        byte[] existing = stub.getState(key);
        if (existing != null && existing.length > 0) {
            throw new ChaincodeException("Anchor already exists for batchId=" + batchId, "ALREADY_EXISTS");
        }

        Anchor anchor = new Anchor(
                ctx.getStub().getTxId(),
                batchId, root, count, startTsMillis, endTsMillis,
                nullIfEmpty(prevRoot),
                processMillis,
                nullIfEmpty(txNote)
        );

        // store
        stub.putState(key, serialize(anchor));

        // update "latest" pointer
        stub.putState(Keys.latestKey(ctx), batchId.getBytes(StandardCharsets.UTF_8));

        // emit event
        stub.setEvent("Anchored", serialize(anchor));

        return anchor.getTxId();
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean Exists(final Context ctx, final String batchId) {
        byte[] v = ctx.getStub().getState(Keys.anchorKey(ctx, batchId));
        return v != null && v.length > 0;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Anchor GetAnchor(final Context ctx, final String batchId) {
        byte[] v = ctx.getStub().getState(Keys.anchorKey(ctx, batchId));
        if (v == null || v.length == 0) {
            throw new ChaincodeException("Anchor not found for batchId=" + batchId, "NOT_FOUND");
        }
        return deserialize(v, Anchor.class);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String ListAnchors(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        List<Anchor> anchors = new ArrayList<>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");
        System.out.println("Listing anchors:");
        try {
            for (KeyValue kv : results) {
                String key = kv.getKey();
                System.out.println(key);
                Anchor anchor = deserialize(kv.getValue(), Anchor.class);
                anchors.add(anchor);
            }
        } catch (Exception e) {
            throw new ChaincodeException("Error listing anchors: " + e.getMessage());
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(anchors); // Return as JSON
        } catch (JsonProcessingException e) {
            throw new ChaincodeException("Error serializing anchor list: " + e.getMessage());
        }
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Anchor LatestAnchor(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        byte[] latestBatchIdBytes = stub.getState(Keys.latestKey(ctx));
        if (latestBatchIdBytes == null || latestBatchIdBytes.length == 0) {
            throw new ChaincodeException("No anchors yet", "EMPTY");
        }
        String batchId = new String(latestBatchIdBytes, StandardCharsets.UTF_8);
        return GetAnchor(ctx, batchId);
    }

    // -------- utilities --------
    private static byte[] serialize(Object o) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T deserialize(byte[] b, Class<T> cls) {
        try {
            return OBJECT_MAPPER.readValue(b, cls);
        } catch (Exception e) {
            throw new ChaincodeException("Deserialization error: " + e.getMessage());
        }
    }

    private static void requireNonEmpty(String s, String field) {
        if (s == null || s.isBlank()) throw new ChaincodeException(field + " must be non-empty");
    }

    private static String nullIfEmpty(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
