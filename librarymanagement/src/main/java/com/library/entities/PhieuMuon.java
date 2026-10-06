package com.library.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "PhieuMuon")
public class PhieuMuon implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaPhieuMuon")
    private Integer maPhieuMuon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaThe")
    private TheDocGia theDocGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaThuThu")
    private NguoiDung thuThu;

    @Column(name = "NgayMuon")
    private LocalDateTime ngayMuon;

    @Column(name = "NgayHenTra")
    private LocalDate ngayHenTra;

    @Column(name = "TrangThai", length = 30)
    private String trangThai;


    public PhieuMuon() {
    }


    // Getters & Setters

    public Integer getMaPhieuMuon() {
        return maPhieuMuon;
    }

    public void setMaPhieuMuon(Integer maPhieuMuon) {
        this.maPhieuMuon = maPhieuMuon;
    }

    public TheDocGia getTheDocGia() {
        return theDocGia;
    }

    public void setTheDocGia(TheDocGia theDocGia) {
        this.theDocGia = theDocGia;
    }

    public NguoiDung getThuThu() {
        return thuThu;
    }

    public void setThuThu(NguoiDung thuThu) {
        this.thuThu = thuThu;
    }

    public LocalDateTime getNgayMuon() {
        return ngayMuon;
    }

    public void setNgayMuon(LocalDateTime ngayMuon) {
        this.ngayMuon = ngayMuon;
    }

    public LocalDate getNgayHenTra() {
        return ngayHenTra;
    }

    public void setNgayHenTra(LocalDate ngayHenTra) {
        this.ngayHenTra = ngayHenTra;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}