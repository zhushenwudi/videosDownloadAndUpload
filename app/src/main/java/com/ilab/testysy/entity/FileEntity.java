package com.ilab.testysy.entity;

public class FileEntity {
    private String uri;
    private String sn;
    private String time;
    private String length;
    private String fileId;
    private int retry;

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public FileEntity(String uri, String sn, String time, String length,String fileId,int retry) {
        this.uri = uri;
        this.sn = sn;
        this.time = time;
        this.length = length;
        this.fileId=fileId;
        this.retry = retry;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return "FileEntity{" +
                "uri='" + uri + '\'' +
                ", sn='" + sn + '\'' +
                ", time='" + time + '\'' +
                ", length='" + length + '\'' +
                ", fileId='" + fileId + '\'' +
                ", retry=" + retry +
                '}';
    }
}
