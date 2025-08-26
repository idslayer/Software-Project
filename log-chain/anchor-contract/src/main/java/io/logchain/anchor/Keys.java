package io.logchain.anchor;

import org.hyperledger.fabric.contract.Context;

final class Keys {
    private Keys() {}

    static String anchorKey(Context ctx, String batchId) {
        return ctx.getStub().createCompositeKey("ANCHOR", batchId).toString();
    }

    static String latestKey(Context ctx) {
        return ctx.getStub().createCompositeKey("ANCHOR", "LATEST").toString();
    }
}
