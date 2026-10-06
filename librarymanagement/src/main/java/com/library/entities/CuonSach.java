package com.library.entities;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "CuonSach")
public class CuonSach implements Serializable {

    @Id
    @Column(name = "MaCuonSach", length = 50)
    private String maCuonSach;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDauSach")
    private DauSach dauSach;

    @Column(name = "TinhTrangValLy", length = 100)
    private String tinhTrangValLy;

    @Column(name = "TrangThai", length = 30)
    private String trangThai;


    public CuonSach() {
    }


    // Getters & Setters

    public String getMaCuonSach() {
        return maCuonSach;
    }

    public void setMaCuonSach(String maCuonSach) {
        this.maCuonSach = maCuonSach;
    }

    public DauSach getDauSach() {
        return dauSach;
    }

    public void setDauSach(DauSach dauSach) {
        this.dauSach = dauSach;
    }

    public String getTinhTrangValLy() {
        return tinhTrangValLy;
    }

    public void setTinhTrangValLy(String tinhTrangValLy) {
        this.tinhTrangValLy = tinhTrangValLy;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
