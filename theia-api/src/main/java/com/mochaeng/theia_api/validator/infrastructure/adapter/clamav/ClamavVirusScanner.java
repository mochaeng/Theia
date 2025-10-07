package com.mochaeng.theia_api.validator.infrastructure.adapter.clamav;

import com.mochaeng.theia_api.ingestion.domain.model.Document;
import com.mochaeng.theia_api.validator.application.port.out.VirusScannerPort;
import io.vavr.control.Either;
import io.vavr.control.Try;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ClamavVirusScanner implements VirusScannerPort {

    private static final int CHUNK_SIZE = 2048;
    private static final int TIMEOUT_MS = 5000;

    private static final int PONG_REPLY_LENGTH = 4;

    @Value("${clamav.host:localhost}")
    private String clamavHost;

    @Value("${clamav.port:3310}")
    private int clamavPort;

    @Override
    public Either<ScanError, ScanResult> scan(byte[] content) {
        Try.withResources(this::createSocket).of(socket -> {

            var out = new DataOutputStream(socket.getOutputStream());
            var in = new DataInputStream(socket.getInputStream());

        })
//        return Try.withResources(() -> createSocket().get())
//            .of(this::waitingForPong)
//            .get()
//            .toEither()
//            .mapLeft(ex ->
//                new ScanError(
//                    "failed to send ping to clamav: " + ex.getMessage()
//                )
//            );
    }

    private Socket createSocket() throws IOException {
        var socket = new Socket();
        socket.connect(
            new InetSocketAddress(clamavHost, clamavPort),
            TIMEOUT_MS
        );
        socket.setSoTimeout(TIMEOUT_MS);
        return socket;
    }

    private void streamFile(DataOutputStream out, byte[] content) throws IOException {

    }

    private Try<Boolean> waitingForPong(Socket socket) {
        return Try.withResources(socket::getOutputStream).of(out -> {
                out.write("zPING\0".getBytes(StandardCharsets.UTF_8));
                out.flush();

                var reply = socket
                    .getInputStream()
                    .readNBytes(PONG_REPLY_LENGTH);

                return Arrays.equals(
                    reply,
                    "PONG".getBytes(StandardCharsets.UTF_8)
                );
            });
    }

    private void writeFullBytes(DataOutputStream out, byte[] data,  )
}
