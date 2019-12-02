package com.ilab.checkupdatebypatch.bean;

public class Apk {
    private String oldMd5;
    private String newMd5;
    private String patchFile;

    public String getOldMd5() {
        return oldMd5;
    }

    public void setOldMd5(String oldMd5) {
        this.oldMd5 = oldMd5;
    }

    public String getNewMd5() {
        return newMd5;
    }

    public void setNewMd5(String newMd5) {
        this.newMd5 = newMd5;
    }

    public String getPatchFile() {
        return patchFile;
    }

    public void setPatchFile(String patchFile) {
        this.patchFile = patchFile;
    }
}
