package com.ilab.testysy.database;

import org.greenrobot.greendao.annotation.*;

@Entity
public class UploadEnty {
    @Id(autoincrement = true)
    private Long id;

    @Unique
    @Property(nameInDb = "skipFile")
    private String fileName;

    @Property(nameInDb = "cause")
    private String cause;

    @Generated(hash = 1259857920)
    public UploadEnty(Long id, String fileName, String cause) {
        this.id = id;
        this.fileName = fileName;
        this.cause = cause;
    }

    @Generated(hash = 1327174740)
    public UploadEnty() {
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

    public String getCause() {
        return this.cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }
}
