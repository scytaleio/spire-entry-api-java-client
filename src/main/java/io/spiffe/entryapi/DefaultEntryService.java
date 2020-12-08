package io.spiffe.entryapi;

import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.spiffe.api.entry.v1.EntryGrpc;
import io.spiffe.api.entry.v1.EntryOuterClass;
import io.spiffe.spiffeid.SpiffeId;
import io.spiffe.types.SelectorOuterClass.Selector;
import io.spiffe.types.Spiffeid;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class DefaultEntryService implements EntryService {

    private EntryGrpc.EntryBlockingStub entryBlockingStub;
    private String serverAddress;

    private DefaultEntryService(EntryGrpc.EntryBlockingStub entryBlockingStub) {
        this.entryBlockingStub = entryBlockingStub;
    }

    public static EntryService newEntryService(String serverAddress, String agentSocketAddress) {
        val entryBlockingStub = createEntryBlockingStub(serverAddress, agentSocketAddress);
        return new DefaultEntryService(entryBlockingStub);
    }

    @Override
    public CreateResult createEntry(EntryDef entryDef) {

        validateParameters(entryDef);

        val createEntryRequest = buildBatchCreateEntryRequest(entryDef);

        val entryResponse = entryBlockingStub.batchCreateEntry(createEntryRequest);

        val result = entryResponse.getResults(0);

        return CreateResult
                .builder()
                .code(result.getStatus().getCode())
                .message(result.getStatus().getMessage())
                .entry(result.getEntry().toString())
                .build();
    }

    private void validateParameters(EntryDef entryDef) {
        if (StringUtils.isBlank(entryDef.spiffeId)) {
            throw new IllegalArgumentException("spiffeID cannot be empty");
        }

        if (entryDef.selectors == null || entryDef.selectors.isEmpty()) {
            throw new IllegalArgumentException("at least one selector is required");
        }
    }

    private EntryOuterClass.BatchCreateEntryRequest buildBatchCreateEntryRequest(EntryDef entryDef) {

        val entry = buildEntry(entryDef);

        return EntryOuterClass.BatchCreateEntryRequest
                .newBuilder()
                .addEntries(entry)
                .build();
    }

    private io.spiffe.types.EntryOuterClass.Entry buildEntry(EntryDef entryDef) {
        Spiffeid.SPIFFEID spiffeIdField = buildSpiffeIdField(entryDef.spiffeId);

        Spiffeid.SPIFFEID parentIdField = null;
        if (StringUtils.isNotBlank(entryDef.parentId)) {
            parentIdField = buildSpiffeIdField(entryDef.parentId);
        }

        val selectorsField = buildSelectors(entryDef.selectors);
        return io.spiffe.types.EntryOuterClass.Entry
                .newBuilder()
                .setSpiffeId(spiffeIdField)
                .setParentId(parentIdField)
                .addAllFederatesWith(entryDef.federatedTrustDomains)
                .addAllDnsNames(entryDef.dns)
                .addAllSelectors(selectorsField)
                .setAdmin(entryDef.admin)
                .setTtl(entryDef.ttl)
                .setExpiresAt(entryDef.entryExpiry)
                .setDownstream(entryDef.downstream)
                .build();
    }

    private Iterable<Selector> buildSelectors(List<String> selectors) {
        return selectors
                .stream()
                .map(this::buildSelector)
                .collect(Collectors.toList());
    }

    private Selector buildSelector(String selector) {

        // parse type and value from the a string (e.g.: from 'unix:uid:1001', type -> unix, value: 'uid:1001')
        String type = selector.substring(0, selector.indexOf(':'));
        String value = selector.substring(selector.indexOf(':') + 1);

        return Selector
                .newBuilder()
                .setType(type)
                .setValue(value)
                .build();
    }

    private Spiffeid.SPIFFEID buildSpiffeIdField(String id) {
        val parsedSpiffeId = SpiffeId.parse(id);
        return Spiffeid.SPIFFEID
                .newBuilder()
                .setTrustDomain(parsedSpiffeId.getTrustDomain().getName())
                .setPath(parsedSpiffeId.getPath())
                .build();
    }

    private static EntryGrpc.EntryBlockingStub createEntryBlockingStub(String serverAddress, String agentSocketAddress) {
        SslContext sslContext = SslContextBuilder.createSslContext(agentSocketAddress);

        ManagedChannel tcpChannel =
                NettyChannelBuilder
                        .forTarget(serverAddress)
                        .sslContext(sslContext)
                        .build();

        return EntryGrpc.newBlockingStub(tcpChannel);
    }

    public static class CreateResult {

        @Setter(AccessLevel.NONE)
        int code;

        @Setter(AccessLevel.NONE)
        String message;

        @Setter(AccessLevel.NONE)
        String entry;

        @Builder
        public CreateResult(int code, String message, String entry) {
            this.code = code;
            this.message = message;
            this.entry = entry;
        }
    }

    public static class EntryDef {

        @Setter(AccessLevel.NONE)
        boolean admin;

        @Setter(AccessLevel.NONE)
        List<String> dns;

        @Setter(AccessLevel.NONE)
        boolean downstream;

        @Setter(AccessLevel.NONE)
        int entryExpiry;

        @Setter(AccessLevel.NONE)
        List<String> federatedTrustDomains;

        @Setter(AccessLevel.NONE)
        String parentId;

        @Setter(AccessLevel.NONE)
        List<String> selectors;

        @Setter(AccessLevel.NONE)
        String spiffeId;

        @Setter(AccessLevel.NONE)
        int ttl;

        @Builder
        public EntryDef(boolean admin, List<String> dns, boolean downstream, int entryExpiry,
                        List<String> federatedTrustDomains, String parentId, List<String> selectors,
                        String spiffeId, int ttl) {
            this.admin = admin;
            this.dns = dns;
            this.downstream = downstream;
            this.entryExpiry = entryExpiry;
            this.federatedTrustDomains = federatedTrustDomains;
            this.parentId = parentId;
            this.selectors = selectors;
            this.spiffeId = spiffeId;
            this.ttl = ttl;
        }
    }
}
