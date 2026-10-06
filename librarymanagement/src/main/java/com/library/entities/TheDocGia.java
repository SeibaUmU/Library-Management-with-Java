package com.library.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "TheDocGia")
public class TheDocGia implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaThe")
    private Integer maThe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaND")
    private NguoiDung nguoiDung;

    @Column(name = "NgayCap")
    private LocalDate ngayCap;

    @Column(name = "NgayHetHan")
    private LocalDate ngayHetHan;

    @Column(name = "TrangThai", length = 20)
    private String trangThai;


    public TheDocGia() {
    }


    // Getters & Setters

    public Integer getMaThe() {
        return maThe;
    }

    public void setMaThe(Integer maThe) {
        this.maThe = maThe;
    }

    public NguoiDung getNguoiDung() {
        return nguoiDung;
    }

    public void setNguoiDung(NguoiDung nguoiDung) {
        this.nguoiDung = nguoiDung;
    }

    public LocalDate getNgayCap() {
        return ngayCap;
    }

    public void setNgayCap(LocalDate ngayCap) {
        this.ngayCap = ngayCap;
    }

    public LocalDate getNgayHetHan() {
        return ngayHetHan;
    }

    public void setNgayHetHan(LocalDate ngayHetHan) {
        this.ngayHetHan = ngayHetHan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}