package com.jetcd.example;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.ClientBuilder;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.kv.PutResponse;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.SSLException;

public class HttpsDemo {

    public static SslContext openSslContext() throws SSLException, FileNotFoundException {
        // 证书、客户端证书、客户端私钥
        InputStream trustManagerFile = HttpsDemo.class.getResourceAsStream("/etcd-CAcert");
        InputStream keyCertChainFile = HttpsDemo.class.getResourceAsStream("/etcd-Cert");
        InputStream KeyFile = HttpsDemo.class.getResourceAsStream("/etcd-Key-pk8");
        // 这里必须要设置alpn,否则会提示ALPN must be enabled and list HTTP/2 as a supported protocol.错误; 这里主要设置了传输协议以及传输过程中的错误解决方式
        ApplicationProtocolConfig alpn = new ApplicationProtocolConfig(ApplicationProtocolConfig.Protocol.ALPN,
                ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                ApplicationProtocolNames.HTTP_2);
        SslContext context = SslContextBuilder
                .forClient()
                // 设置alpn
                .applicationProtocolConfig(alpn)
                // 设置使用的那种ssl实现方式
                .sslProvider(SslProvider.OPENSSL)
                // 设置ca证书
                .trustManager(trustManagerFile)
                // 设置客户端证书
                .keyManager(keyCertChainFile, KeyFile)
                .build();
        return context;
    }

    public static Client etcdClient() throws SSLException, FileNotFoundException {
        ClientBuilder builder = Client.builder();
        // 设置服务器地址,这里是列表
        builder.endpoints("https://127.0.0.1:2379");
        builder.sslContext(openSslContext());
        return builder.build();
    }

    public static void main(String[] args)
            throws InterruptedException, ExecutionException, TimeoutException, FileNotFoundException, SSLException {
        Client client = etcdClient();
        KV kvClient = client.getKVClient();
        CompletableFuture<PutResponse> future = kvClient.put(ByteSequence.from("hello", StandardCharsets.UTF_8),
                ByteSequence.from("world", StandardCharsets.UTF_8));
        System.out.println(future.get().toString());
    }

}
