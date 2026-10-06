package com.library.entities;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "CauHinh")
public class CauHinh implements Serializable {

    @Id
    @Column(name = "MaCauHinh")
    private Integer maCauHinh = 1;

    @Column(name = "SoSachToiDa")
    private Integer soSachToiDa;

    @Column(name = "SoNgayMuonToiDa")
    private Integer soNgayMuonToiDa;

    @Column(name = "TienPhatNgay")
    private Double tienPhatNgay;


    public CauHinh() {
    }


    // Getters & Setters

    public Integer getMaCauHinh() {
        return maCauHinh;
    }

    public void setMaCauHinh(Integer maCauHinh) {
        this.maCauHinh = maCauHinh;
    }

    public Integer getSoSachToiDa() {
        return soSachToiDa;
    }

    public void setSoSachToiDa(Integer soSachToiDa) {
        this.soSachToiDa = soSachToiDa;
    }

    public Integer getSoNgayMuonToiDa() {
        return soNgayMuonToiDa;
    }

    public void setSoNgayMuonToiDa(Integer soNgayMuonToiDa) {
        this.soNgayMuonToiDa = soNgayMuonToiDa;
    }

    public Double getTienPhatNgay() {
        return tienPhatNgay;
    }

    public void setTienPhatNgay(Double tienPhatNgay) {
        this.tienPhatNgay = tienPhatNgay;
    }
}
