package com.library.entities;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "TheLoai")
public class TheLoai implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaTL")
    private Integer maTL;

    @Column(name = "TenTL", length = 50)
    private String tenTL;


    public TheLoai() {
    }


    public Integer getMaTL() {
        return maTL;
    }

    public void setMaTL(Integer maTL) {
        this.maTL = maTL;
    }

    public String getTenTL() {
        return tenTL;
    }

    public void setTenTL(String tenTL) {
        this.tenTL = tenTL;
    }
}
