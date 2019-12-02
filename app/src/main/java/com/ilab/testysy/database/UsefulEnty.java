package com.ilab.testysy.database;

import org.greenrobot.greendao.annotation.*;

@Entity
public class UsefulEnty {
    @Id(autoincrement = true)
    private Long id;

    @Property(nameInDb = "useful")
    private String useful;

    @Generated(hash = 335076232)
    public UsefulEnty(Long id, String useful) {
        this.id = id;
        this.useful = useful;
    }

    @Generated(hash = 1639793901)
    public UsefulEnty() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUseful() {
        return this.useful;
    }

    public void setUseful(String useful) {
        this.useful = useful;
    }


}
