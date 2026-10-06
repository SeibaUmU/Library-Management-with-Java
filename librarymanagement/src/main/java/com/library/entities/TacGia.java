package com.library.entities;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "TacGia")
public class TacGia implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaTG")
    private Integer maTG;

    @Column(name = "TenTacGia", length = 100)
    private String tenTacGia;


    public TacGia() {
    }


    public Integer getMaTG() {
        return maTG;
    }

    public void setMaTG(Integer maTG) {
        this.maTG = maTG;
    }

    public String getTenTacGia() {
        return tenTacGia;
    }

    public void setTenTacGia(String tenTacGia) {
        this.tenTacGia = tenTacGia;
    }
}