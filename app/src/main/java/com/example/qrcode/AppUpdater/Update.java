package com.example.qrcode.AppUpdater;

public class Update {
    private String version;
    private String releaseNotes;
    private String storeUrl;

    public Update(String latestVersion, String releaseNotes, String storeUrl) {
        this.version = latestVersion;
        this.storeUrl = storeUrl;
        this.releaseNotes = releaseNotes;
    }

    public String getLatestVersion() {
        return version;
    }

    public void setLatestVersion(String latestVersion) {
        this.version = latestVersion;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

    public String getStoreUrl() {
        return storeUrl;
    }

    public void setStoreUrl(String storeUrl) {
        this.storeUrl = storeUrl;
    }
}
