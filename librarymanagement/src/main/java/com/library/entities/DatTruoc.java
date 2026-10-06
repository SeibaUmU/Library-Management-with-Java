package com.library.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "DatTruoc")
public class DatTruoc implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDatTruoc")
    private Integer maDatTruoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaThe")
    private TheDocGia theDocGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDauSach")
    private DauSach dauSach;

    @Column(name = "NgayDat")
    private LocalDateTime ngayDat;

    @Column(name = "TrangThai", length = 30)
    private String trangThai;


    public DatTruoc() {
    }


    // Getters & Setters

    public Integer getMaDatTruoc() {
        return maDatTruoc;
    }

    public void setMaDatTruoc(Integer maDatTruoc) {
        this.maDatTruoc = maDatTruoc;
    }

    public TheDocGia getTheDocGia() {
        return theDocGia;
    }

    public void setTheDocGia(TheDocGia theDocGia) {
        this.theDocGia = theDocGia;
    }

    public DauSach getDauSach() {
        return dauSach;
    }

    public void setDauSach(DauSach dauSach) {
        this.dauSach = dauSach;
    }

    public LocalDateTime getNgayDat() {
        return ngayDat;
    }

    public void setNgayDat(LocalDateTime ngayDat) {
        this.ngayDat = ngayDat;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
