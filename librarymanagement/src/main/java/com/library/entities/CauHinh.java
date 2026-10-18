package com.library.entities;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    @Column(name = "SoLanGiaHanToiDa")
    private Integer soLanGiaHanToiDa;

    @Column(name = "SoNgayGiuSachDatTruoc")
    private Integer soNgayGiuSachDatTruoc;

    
    public CauHinh() {
    }


    // Getters & Setters
    public Integer getSoLanGiaHanToiDa() { return soLanGiaHanToiDa; }
    public void setSoLanGiaHanToiDa(Integer soLanGiaHanToiDa) { this.soLanGiaHanToiDa = soLanGiaHanToiDa; }

    public Integer getSoNgayGiuSachDatTruoc() { return soNgayGiuSachDatTruoc; }
    public void setSoNgayGiuSachDatTruoc(Integer soNgayGiuSachDatTruoc) { this.soNgayGiuSachDatTruoc = soNgayGiuSachDatTruoc; }
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
