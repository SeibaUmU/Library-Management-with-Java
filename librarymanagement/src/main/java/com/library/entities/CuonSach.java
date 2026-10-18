package com.library.entities;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "CuonSach")
public class CuonSach implements Serializable {

    @Id
    @Column(name = "MaCuonSach", length = 50)
    private String maCuonSach;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDauSach")
    private DauSach dauSach;

    @Column(name = "TinhTrangVatLy", length = 100)
    private String tinhTrangVatLy;

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

    public String getTinhTrangVatLy() {
        return tinhTrangVatLy;
    }

    public void setTinhTrangVatLy(String tinhTrangVatLy) {
        this.tinhTrangVatLy = tinhTrangVatLy;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
