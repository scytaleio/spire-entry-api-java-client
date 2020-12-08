package io.spiffe.entryapi;

import lombok.val;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Collections;
import java.util.List;

@Command(name = "create-entry", mixinStandardHelpOptions = true)
public class CreateEntry implements Runnable {

    @Option(names= "-admin", description = "If set, the SPIFFE ID in this entry will be granted access to the Registration API")
    private boolean admin;

    @Option(names = "-dns", description = "A DNS name that will be included in SVIDs issued based on this entry, where appropriate. Can be used more than once")
    private List<String> dns = Collections.emptyList();

    @Option(names= "-downstream", description = "A boolean value that, when set, indicates that the entry describes a downstream SPIRE server")
    private boolean downstream;

    @Option(names= "-entryExpiry", description = "An expiry, from epoch in seconds, for the resulting registration entry to be pruned")
    private int entryExpiry;

    @Option(names = "-federatesWith", description = "SPIFFE ID of a trust domain to federate with. Can be used more than once")
    private List<String> federatedTrustDomains = Collections.emptyList();

    @Option(names = "-parentID", description = "The SPIFFE ID of this record's parent. If not set, the entry will be set to a node")
    private String parentId;

    @Option(names = "-selector", description = "A colon-delimited type:value selector. Can be used more than once")
    private List<String> selectors = Collections.emptyList();

    @Option(names = "-spiffeID", description = "The SPIFFE ID that this record represents")
    private String spiffeId;

    @Option(names = "-ttl", description = "The lifetime, in seconds, for SVIDs issued based on this registration entry")
    private int ttl;

    @Option(names = "-serverAddress", showDefaultValue = CommandLine.Help.Visibility.ALWAYS, description = "Address of the SPIRE Server Registration API", defaultValue = "127.0.0.1:8081")
    private String serverAddress;

    @Option(names = "-agentSocketAddress", showDefaultValue = CommandLine.Help.Visibility.ALWAYS, description = "Address of the SPIRE Agent socket", defaultValue = "unix:/tmp/agent.sock")
    private String agentSocketAddress;

    EntryService service;

    @Override
    public void run() {
        service = DefaultEntryService.newEntryService(serverAddress, agentSocketAddress);

       val entryDef = DefaultEntryService.EntryDef
                .builder()
                .admin(admin)
                .dns(dns)
                .downstream(downstream)
                .entryExpiry(entryExpiry)
                .federatedTrustDomains(federatedTrustDomains)
                .parentId(parentId)
                .selectors(selectors)
                .spiffeId(spiffeId)
                .ttl(ttl)
                .build();

       val result = service.createEntry(entryDef);

        System.out.printf("\nResult code: %s\n", result.code);
        System.out.printf("Result message: %s\n", result.message);
        System.out.printf("\nEntry:\n %s", result.entry);
    }
}
