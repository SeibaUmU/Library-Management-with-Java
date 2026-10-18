package com.library.entities;

import java.io.Serializable;

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
    @Column(name = "ViTri", length = 100)
    private String viTri;

    @Column(name = "TongSoLuong")
    private Integer tongSoLuong = 0;

    @Column(name = "SoLuongKhaDung")
    private Integer soLuongKhaDung = 0;

    

    public DauSach() {
    }


    // Getters & Setters
    public String getViTri() { return viTri; }
    public void setViTri(String viTri) { this.viTri = viTri; }

    public Integer getTongSoLuong() { return tongSoLuong; }
    public void setTongSoLuong(Integer tongSoLuong) { this.tongSoLuong = tongSoLuong; }

    public Integer getSoLuongKhaDung() { return soLuongKhaDung; }
    public void setSoLuongKhaDung(Integer soLuongKhaDung) { this.soLuongKhaDung = soLuongKhaDung; }
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
