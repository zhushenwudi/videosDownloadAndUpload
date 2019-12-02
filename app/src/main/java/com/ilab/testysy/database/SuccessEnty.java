package com.ilab.testysy.database;

import org.greenrobot.greendao.annotation.*;

@Entity
public class SuccessEnty {
    @Id(autoincrement = true)
    private Long id;

    @Index(unique = true)
    @Property(nameInDb = "fileId")
    private String fileId;

    @Generated(hash = 1035899477)
    public SuccessEnty(Long id, String fileId) {
        this.id = id;
        this.fileId = fileId;
    }

    @Generated(hash = 2010396922)
    public SuccessEnty() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileId() {
        return this.fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
    

}
