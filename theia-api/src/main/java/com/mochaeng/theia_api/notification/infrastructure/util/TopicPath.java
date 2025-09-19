package com.mochaeng.theia_api.notification.infrastructure.util;

import java.util.UUID;

public final class TopicPath {

    private TopicPath() {}

    public static String documentProgress(UUID documentID) {
        return "/topic/documents/%s/progress".formatted(documentID);
    }
}
