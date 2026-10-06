package com.library.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "ChiTietPhieuMuon")
public class ChiTietPhieuMuon implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaCTPM")
    private Integer maCTPM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaPhieuMuon")
    private PhieuMuon phieuMuon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaCuonSach")
    private CuonSach cuonSach;

    @Column(name = "NgayTraThucTe")
    private LocalDateTime ngayTraThucTe;

    @Column(name = "TrangThaiSachKhiTra", length = 50)
    private String trangThaiSachKhiTra;


    public ChiTietPhieuMuon() {
    }


    // Getters & Setters

    public Integer getMaCTPM() {
        return maCTPM;
    }

    public void setMaCTPM(Integer maCTPM) {
        this.maCTPM = maCTPM;
    }

    public PhieuMuon getPhieuMuon() {
        return phieuMuon;
    }

    public void setPhieuMuon(PhieuMuon phieuMuon) {
        this.phieuMuon = phieuMuon;
    }

    public CuonSach getCuonSach() {
        return cuonSach;
    }

    public void setCuonSach(CuonSach cuonSach) {
        this.cuonSach = cuonSach;
    }

    public LocalDateTime getNgayTraThucTe() {
        return ngayTraThucTe;
    }

    public void setNgayTraThucTe(LocalDateTime ngayTraThucTe) {
        this.ngayTraThucTe = ngayTraThucTe;
    }

    public String getTrangThaiSachKhiTra() {
        return trangThaiSachKhiTra;
    }

    public void setTrangThaiSachKhiTra(String trangThaiSachKhiTra) {
        this.trangThaiSachKhiTra = trangThaiSachKhiTra;
    }
}
