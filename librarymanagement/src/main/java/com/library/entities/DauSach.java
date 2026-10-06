package com.library.entities;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "DauSach")
public class DauSach implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDauSach")
    private Integer maDauSach;

    @Column(name = "TenSach", length = 200)
    private String tenSach;

    @Column(name = "GiaTien")
    private Double giaTien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaTG")
    private TacGia tacGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNXB")
    private NhaXuatBan nhaXuatBan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaTL")
    private TheLoai theLoai;


    public DauSach() {
    }


    // Getters & Setters

    public Integer getMaDauSach() {
        return maDauSach;
    }

    public void setMaDauSach(Integer maDauSach) {
        this.maDauSach = maDauSach;
    }

    public String getTenSach() {
        return tenSach;
    }

    public void setTenSach(String tenSach) {
        this.tenSach = tenSach;
    }

    public Double getGiaTien() {
        return giaTien;
    }

    public void setGiaTien(Double giaTien) {
        this.giaTien = giaTien;
    }

    public TacGia getTacGia() {
        return tacGia;
    }

    public void setTacGia(TacGia tacGia) {
        this.tacGia = tacGia;
    }

    public NhaXuatBan getNhaXuatBan() {
        return nhaXuatBan;
    }

    public void setNhaXuatBan(NhaXuatBan nhaXuatBan) {
        this.nhaXuatBan = nhaXuatBan;
    }

    public TheLoai getTheLoai() {
        return theLoai;
    }

    public void setTheLoai(TheLoai theLoai) {
        this.theLoai = theLoai;
    }
}
