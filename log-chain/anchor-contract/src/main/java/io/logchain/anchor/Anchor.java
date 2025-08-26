package io.logchain.anchor;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType()
public final class Anchor {
    @Property()
    private String txId;        // canonical string (UUID or offsets)
    @Property()
    private String batchId;        // canonical string (UUID or offsets)
    @Property()
    private String root;           // hex or base64 of 32B
    @Property()
    private long count;            // records in batch
    @Property()
    private long startTsMillis;     // first record ts
    @Property()
    private long endTsMillis;       // last record ts
    @Property()
    private String prevRoot;       // optional chaining
    @Property()
    private long anchorTsMillis;   // server time when anchored
    @Property()
    private String txNote;         // optional free-form note

    public Anchor() {
    }

    public Anchor(
            String txId,
            String batchId, String root, long count, long startTsNanos,
            long endTsNanos, String prevRoot, long anchorTsMillis, String txNote) {
        this.txId = txId;
        this.batchId = batchId;
        this.root = root;
        this.count = count;
        this.startTsMillis = startTsNanos;
        this.endTsMillis = endTsNanos;
        this.prevRoot = prevRoot;
        this.anchorTsMillis = anchorTsMillis;
        this.txNote = txNote;
    }

    public String getBatchId() {
        return batchId;
    }

    public String getRoot() {
        return root;
    }

    public long getCount() {
        return count;
    }

    public long getStartTsMillis() {
        return startTsMillis;
    }

    public long getEndTsMillis() {
        return endTsMillis;
    }

    public String getPrevRoot() {
        return prevRoot;
    }

    public long getAnchorTsMillis() {
        return anchorTsMillis;
    }

    public String getTxNote() {
        return txNote;
    }

    public void setBatchId(final String v) {
        this.batchId = v;
    }

    public void setRoot(final String v) {
        this.root = v;
    }

    public void setCount(final long v) {
        this.count = v;
    }

    public void setStartTsMillis(final long v) {
        this.startTsMillis = v;
    }

    public void setEndTsMillis(final long v) {
        this.endTsMillis = v;
    }

    public void setPrevRoot(final String v) {
        this.prevRoot = v;
    }

    public void setAnchorTsMillis(final long v) {
        this.anchorTsMillis = v;
    }

    public void setTxNote(final String v) {
        this.txNote = v;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }
}

