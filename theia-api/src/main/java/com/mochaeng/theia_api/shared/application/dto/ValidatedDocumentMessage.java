//package com.mochaeng.theia_api.shared.application.dto;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//public record ValidatedDocumentMessage(
//    UUID documentID,
//    String userID,
//    String bucket,
//    String key,
//    String contentType,
//    long fileSizeBytes,
//    LocalDateTime uploadedAt
//) {
//    public static ValidatedDocumentMessage from(
//        String bucket,
//        String key,
//        DocumentMessage message
//    ) {
//        return new ValidatedDocumentMessage(
//            message.documentID(),
//            message.userID(),
//            bucket,
//            key,
//            message.contentType(),
//            message.fileSizeBytes(),
//            LocalDateTime.now()
//        );
//    }
//}
