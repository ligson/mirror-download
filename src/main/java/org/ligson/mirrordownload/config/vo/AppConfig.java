package org.ligson.mirrordownload.config.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AppConfig {

    private App app;

    public App getApp() {
        return app;
    }

    @JsonProperty("app")
    public void setApp(App app) {
        this.app = app;
    }
}

