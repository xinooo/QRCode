package com.example.qrcode.Setting;

public class SettingBean {
    private String id;
    private boolean isCheck;
    private String note;
    private String id_tw;
    private String note_tw;

    public void setid(String id) {
        this.id = id;
    }
    public void setnote(String note) {
        this.note = note;
    }
    public void setid_tw(String id_tw) {
        this.id_tw = id_tw;
    }
    public void setnote_tw(String note_tw) {
        this.note_tw = note_tw;
    }
    public void setisCheck(boolean isCheck) {
        this.isCheck = isCheck;
    }
    public String getid() {
        return id;
    }
    public String getnote() {
        return note;
    }
    public String getid_tw() {
        return id_tw;
    }
    public String getnote_tw() {
        return note_tw;
    }
    public boolean getisCheck() {
        return isCheck;
    }
}
