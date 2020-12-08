# SPIRE Entry API client 

Client to interact with the SPIRE Server Entry API to perform CRUD operations. 

The client uses the [java-spiffe](https://github.com/spiffe/java-spiffe) to fetch an Admin SVID
from a Workload API using a local UDS call, and then uses the SVID to establish a mTLS connection
with the SPIRE Server Entry API to make CRUD operations on the entries.

## Build

```
./gradlew build
```

The generated `jar` file will have a classifier `-osx-x86_64` if built in MacOS or `-linux-x86_64` if
built on Linux. Consider it for the usage examples below.

## Usage

### Create Entry Command

```
java -jar build/libs/entry-api-client-0.1.0-osx-x86_64.jar create-entry -h 
Usage: create-entry [-hV] [-admin] [-downstream]
                                 [-agentSocketAddress=<agentSocketAddress>]
                                 [-entryExpiry=<entryExpiry>]
                                 [-parentID=<parentId>]
                                 [-serverAddress=<serverAddress>]
                                 [-spiffeID=<spiffeId>] [-ttl=<ttl>]
                                 [-dns=<dns>]...
                                 [-federatesWith=<federatedTrustDomains>]...
                                 [-selector=<selectors>]...
      -admin                 If set, the SPIFFE ID in this entry will be
                               granted access to the Registration API
      -agentSocketAddress=<agentSocketAddress>
                             Address of the SPIRE Agent socket
                               Default: unix:/tmp/agent.sock
      -dns=<dns>             A DNS name that will be included in SVIDs issued
                               based on this entry, where appropriate. Can be
                               used more than once
      -downstream            A boolean value that, when set, indicates that the
                               entry describes a downstream SPIRE server
      -entryExpiry=<entryExpiry>
                             An expiry, from epoch in seconds, for the
                               resulting registration entry to be pruned
      -federatesWith=<federatedTrustDomains>
                             SPIFFE ID of a trust domain to federate with. Can
                               be used more than once
  -h, --help                 Show this help message and exit.
      -parentID=<parentId>   The SPIFFE ID of this record's parent. If not set,
                               the entry will be set to a node
      -selector=<selectors>  A colon-delimited type:value selector. Can be used
                               more than once
      -serverAddress=<serverAddress>
                             Address of the SPIRE Server Registration API
                               Default: 127.0.0.1:8081
      -spiffeID=<spiffeId>   The SPIFFE ID that this record represents
      -ttl=<ttl>             The lifetime, in seconds, for SVIDs issued based
                               on this registration entry
  -V, --version              Print version information and exit.

```

#### Example

```
java  -jar build/libs/entry-api-client-0.1.0-osx-x86_64.jar create-entry \
   -serverAddress spire-server:8081 \
   -agentSocketAddress unix:/tmp/agent.sock \
   -parentID spiffe://example.org/myagent \
   -spiffeID spiffe://example.org/service \
   -selector unix:uid:502 -selector unix:uid:503 \
   -dns service1 -dns service2
```

Output:

```
Result code: 0
Result message: OK

Entry:
 id: "270cc37b-dc5f-4e39-bdcb-4cf382e45e67"
spiffe_id {
  trust_domain: "example.org"
  path: "/service"
}
parent_id {
  trust_domain: "example.org"
  path: "/myagent"
}
selectors {
  type: "unix"
  value: "uid:502"
}
selectors {
  type: "unix"
  value: "uid:503"
}
dns_names: "service1"
dns_names: "service2"
```

## TODO

Implement the rest of the CRUD operations. 

