package com.example.qrcode.Setting;

public class SettingBean {
    private String id;
    private boolean isCheck;
    private String note;

    public void setid(String id) {
        this.id = id;
    }
    public void setnote(String note) {
        this.note = note;
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
    public boolean getisCheck() {
        return isCheck;
    }
}
