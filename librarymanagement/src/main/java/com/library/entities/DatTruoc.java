package com.library.entities;

import java.io.Serializable;
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
    // Cuốn sách vật lý được khóa cho lượt đặt này (null khi còn WAITING)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaCuonSach")
    private CuonSach cuonSach;

    @Column(name = "NgayThongBao")
    private LocalDateTime ngayThongBao;

    

    public DatTruoc() {
    }


    // Getters & Setters
    public CuonSach getCuonSach() { return cuonSach; }
    public void setCuonSach(CuonSach cuonSach) { this.cuonSach = cuonSach; }

    public LocalDateTime getNgayThongBao() { return ngayThongBao; }
    public void setNgayThongBao(LocalDateTime ngayThongBao) { this.ngayThongBao = ngayThongBao; }
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
