package io.spiffe.entryapi;

import io.grpc.netty.GrpcSslContexts;
import io.netty.handler.ssl.SslContext;
import io.spiffe.bundle.BundleSource;
import io.spiffe.bundle.x509bundle.X509Bundle;
import io.spiffe.exception.SocketEndpointAddressException;
import io.spiffe.exception.X509ContextException;
import io.spiffe.provider.SpiffeKeyManager;
import io.spiffe.provider.SpiffeTrustManager;
import io.spiffe.svid.x509svid.X509SvidSource;
import io.spiffe.workloadapi.DefaultWorkloadApiClient;
import io.spiffe.workloadapi.WorkloadApiClient;
import io.spiffe.workloadapi.X509Context;
import lombok.val;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import java.io.IOException;

public class SslContextBuilder {

    private SslContextBuilder() {
    }

    public static SslContext createSslContext(String agentSocketAddress) {

        val x509Context = fetchX509Context(agentSocketAddress);

        X509SvidSource x509SvidSource = x509Context::getDefaultSvid;
        BundleSource<X509Bundle> x509BundleSource = trustDomain -> x509Context.getX509BundleSet().getBundleForTrustDomain(trustDomain);

        KeyManager keyManager = new SpiffeKeyManager(x509SvidSource);
        TrustManager trustManager = new SpiffeTrustManager(x509BundleSource);

        return buildSslContext(keyManager, trustManager);
    }

    private static X509Context fetchX509Context(String agentSocketAddress) {
       val clientOptions = DefaultWorkloadApiClient.ClientOptions
                .builder()
                .spiffeSocketPath(agentSocketAddress)
                .build();

        final X509Context x509Context;
        try (WorkloadApiClient workloadApiClient = DefaultWorkloadApiClient.newClient(clientOptions)) {
            x509Context = workloadApiClient.fetchX509Context();
        } catch (SocketEndpointAddressException | IOException | X509ContextException e) {
            throw new RuntimeException("Error creating WorkloadApi Client", e);
        }
        return x509Context;
    }

    private static SslContext buildSslContext(KeyManager keyManager, TrustManager trustManager) {
        SslContext sslContext;
        try {
            sslContext = GrpcSslContexts
                    .forClient()
                    .keyManager(keyManager)
                    .trustManager(trustManager)
                    .build();
        } catch (SSLException e) {
            throw new RuntimeException("Error creating SSL Context", e);
        }
        return sslContext;
    }
}
