package com.ilab.testysy.database;


import org.greenrobot.greendao.annotation.*;

@Entity
public class ErrorFile {

    @Id(autoincrement = true)
    private Long id;

    @Property(nameInDb = "fileName")
    private String fileName;

    @Property(nameInDb = "errorCount")
    private int count;

    @Generated(hash = 1207741446)
    public ErrorFile(Long id, String fileName, int count) {
        this.id = id;
        this.fileName = fileName;
        this.count = count;
    }

    @Generated(hash = 2059302521)
    public ErrorFile() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
