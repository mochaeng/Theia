package com.mochaeng.theia_api.validator.infrastructure.adapter.s3;

import com.mochaeng.theia_api.shared.infrastructure.s3.S3Helpers;
import com.mochaeng.theia_api.validator.application.port.out.FileStoragePort;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("s3FileStorageValidator")
@RequiredArgsConstructor
public class S3FileStorage implements FileStoragePort {

    private final S3Helpers s3Helpers;

    @Override
    public Either<FileStorageError, byte[]> download(
        String bucket,
        String key
    ) {
        var file = s3Helpers.download(bucket, key);
        if (file.isLeft()) {
            return Either.left(new FileStorageError(file.getLeft().message()));
        }

        return Either.right(file.get().content());
        //        return s3Helpers
        //            .download(bucket, key)
        //            .mapLeft(s3DownloadError ->
        //                new FileStorageError(s3DownloadError.message())
        //            )
        //            .map(S3Helpers.S3DownloadResult::content);
    }

    @Override
    public Either<FileStorageError, Void> upload(
        String bucket,
        String key,
        String contentType,
        byte[] content
    ) {
        return null;
    }
}
