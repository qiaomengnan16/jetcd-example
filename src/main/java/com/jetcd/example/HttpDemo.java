package com.jetcd.example;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.kv.GetResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class HttpDemo {

    public static Client getClient() {
        return Client.builder()
                .endpoints("http://127.0.01:12379")
                .user(ByteSequence.from("username".getBytes()))
                .password(ByteSequence.from("password".getBytes()))
                .build();
    }


    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        Client client = getClient();
        KV kvClient = client.getKVClient();
        GetResponse response = kvClient.get(ByteSequence.from("hello", StandardCharsets.UTF_8)).get();
        System.out.println(response.getKvs().get(0).getValue().toString(Charset.defaultCharset()));
    }

}
