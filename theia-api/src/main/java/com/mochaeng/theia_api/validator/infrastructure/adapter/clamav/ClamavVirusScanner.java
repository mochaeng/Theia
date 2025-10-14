package com.mochaeng.theia_api.validator.infrastructure.adapter.clamav;

import com.mochaeng.theia_api.validator.application.port.out.VirusScannerPort;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ClamavVirusScanner implements VirusScannerPort {

    private static final int CHUNK_SIZE = 1024;
    private static final int TIMEOUT_MS = 5000;

    private static final int PONG_REPLY_LENGTH = 4;

    @Value("${clamav.host:localhost}")
    private String clamavHost;

    @Value("${clamav.port:3310}")
    private int clamavPort;

    @Override
    public Either<ScanError, ScanResult> scan(byte[] content) {
        try (
            var socket = createSocket();
            var out = new DataOutputStream(socket.getOutputStream());
            var in = new BufferedInputStream(socket.getInputStream())
        ) {
            //            var eicar =
            //                "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes(
            //                    StandardCharsets.UTF_8
            //                );

            var err = streamFile(out, content);
            if (!err.isEmpty()) {
                return Either.left(
                    new ScanError("failed to stream file: " + err.get())
                );
            }

            var response = readResponse(in);
            if (response.isLeft()) {
                return Either.left(
                    new ScanError(
                        "failed to read response: " + response.getLeft()
                    )
                );
            }

            return Either.right(response.get());
        } catch (Exception ex) {
            return Either.left(
                new ScanError("unexpected error: " + ex.getMessage())
            );
        }
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

    private Option<String> streamFile(DataOutputStream out, byte[] content) {
        try {
            out.write("zINSTREAM\0".getBytes(StandardCharsets.UTF_8));

            var offset = 0;
            while (offset < content.length) {
                var chunkSize = Math.min(CHUNK_SIZE, content.length - offset);
                var sizeBytes = ByteBuffer.allocate(4)
                    .order(ByteOrder.BIG_ENDIAN)
                    .putInt(chunkSize)
                    .array();

                out.write(sizeBytes);
                out.write(content, offset, chunkSize);
                offset += chunkSize;
            }

            out.write(new byte[] { 0, 0, 0, 0 });
            out.flush();

            return Option.none();
        } catch (Exception e) {
            return Option.of(e.getMessage());
        }
    }

    private Either<String, ScanResult> readResponse(InputStream in) {
        try {
            var response = in.readAllBytes();
            var text = new String(response, StandardCharsets.UTF_8).trim();
            var parsed = parseTextResponse(text);
            if (parsed.isLeft()) {
                return Either.left(parsed.getLeft());
            }
            return parsed;
        } catch (Exception ex) {
            return Either.left(ex.getMessage());
        }
    }

    private Either<String, ScanResult> parseTextResponse(String response) {
        if (response.startsWith("stream:") && response.endsWith("FOUND")) {
            var colonIdx = response.indexOf(':');
            var foundIdx = response.lastIndexOf("FOUND");
            var virusSignature = response
                .substring(colonIdx + 1, foundIdx)
                .trim();
            return Either.right(new ScanResult(Option.of(virusSignature)));
        }
        if (response.startsWith("stream:") && response.endsWith("OK")) {
            return Either.right(new ScanResult(Option.none()));
        }

        return Either.left(
            "unexpected response format from clamav: " + response
        );
    }
}
