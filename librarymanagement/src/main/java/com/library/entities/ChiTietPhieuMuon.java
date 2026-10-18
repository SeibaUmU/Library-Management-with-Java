package com.library.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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
    @Column(name = "NgayHenTra")
    private LocalDate ngayHenTra;

    @Column(name = "SoLanGiaHan")
    private Integer soLanGiaHan = 0;

    public ChiTietPhieuMuon() {
    }


    // Getters & Setters
    public LocalDate getNgayHenTra() { return ngayHenTra; }
    public void setNgayHenTra(LocalDate ngayHenTra) { this.ngayHenTra = ngayHenTra; }

    public Integer getSoLanGiaHan() { return soLanGiaHan; }
    public void setSoLanGiaHan(Integer soLanGiaHan) { this.soLanGiaHan = soLanGiaHan; }
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
