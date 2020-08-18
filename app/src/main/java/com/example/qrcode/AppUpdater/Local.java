package com.example.qrcode.AppUpdater;

public interface Local {
    String[] versions = {
            "目前版本", //TW
            "当前版本", //ZH
            "Current Version" //EN
    };
    interface ErrorMessage {
        String NETWORK_NOT_AVAILABLE = "NETWORK_NOT_AVAILABLE";
        String CONNECT_ERROR = "CONNECT_ERROR";
        String APP_PAGE_ERROR = "APP_PAGE_ERROR";
        String PARSE_ERROR = "PARSE_ERROR";
        String UPDATE_VARIES_BY_DEVICE = "UPDATE_VARIES_BY_DEVICE";
    }
}
