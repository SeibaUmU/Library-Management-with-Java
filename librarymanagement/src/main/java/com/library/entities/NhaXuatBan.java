package com.library.entities;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "NhaXuatBan")
public class NhaXuatBan implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNXB")
    private Integer maNXB;

    @Column(name = "TenNXB", length = 100)
    private String tenNXB;


    public NhaXuatBan() {
    }


    public Integer getMaNXB() {
        return maNXB;
    }

    public void setMaNXB(Integer maNXB) {
        this.maNXB = maNXB;
    }

    public String getTenNXB() {
        return tenNXB;
    }

    public void setTenNXB(String tenNXB) {
        this.tenNXB = tenNXB;
    }
}
