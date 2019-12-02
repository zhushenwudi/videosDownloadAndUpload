package com.ilab.testysy.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Property;

@Entity
public class SuccessPicEnty {
    @Id(autoincrement = true)
    private Long id;

    @Index(unique = true)
    @Property(nameInDb = "fileId")
    private String fileId;


    @Generated(hash = 1684641509)
    public SuccessPicEnty() {
    }

    @Generated(hash = 206330322)
    public SuccessPicEnty(Long id, String fileId) {
        this.id = id;
        this.fileId = fileId;
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
