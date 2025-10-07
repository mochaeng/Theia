package com.mochaeng.theia_api.validator.application.port.out;

import io.vavr.control.Either;
import io.vavr.control.Option;

public interface VirusScannerPort {
    Either<ScanError, ScanResult> scan(byte[] content);

    record ScanResult(Option<String> signature) {}

    record ScanError(String message) {}
}
