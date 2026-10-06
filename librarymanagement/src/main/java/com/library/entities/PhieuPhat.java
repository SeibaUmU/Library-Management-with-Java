package com.library.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "PhieuPhat")
public class PhieuPhat implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaPhieuPhat")
    private Integer maPhieuPhat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaCTPM")
    private ChiTietPhieuMuon chiTietPhieuMuon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaThuThu")
    private NguoiDung thuThu;

    @Column(name = "NgayLapPhieu")
    private LocalDateTime ngayLapPhieu;

    @Column(name = "LyDo", length = 200)
    private String lyDo;

    @Column(name = "SoTienPhat")
    private Double soTienPhat;

    @Column(name = "TrangThaiThanhToan")
    private Boolean trangThaiThanhToan;


    public PhieuPhat() {
    }


    // Getters & Setters

    public Integer getMaPhieuPhat() {
        return maPhieuPhat;
    }

    public void setMaPhieuPhat(Integer maPhieuPhat) {
        this.maPhieuPhat = maPhieuPhat;
    }

    public ChiTietPhieuMuon getChiTietPhieuMuon() {
        return chiTietPhieuMuon;
    }

    public void setChiTietPhieuMuon(
            ChiTietPhieuMuon chiTietPhieuMuon
    ) {
        this.chiTietPhieuMuon = chiTietPhieuMuon;
    }

    public NguoiDung getThuThu() {
        return thuThu;
    }

    public void setThuThu(NguoiDung thuThu) {
        this.thuThu = thuThu;
    }

    public LocalDateTime getNgayLapPhieu() {
        return ngayLapPhieu;
    }

    public void setNgayLapPhieu(LocalDateTime ngayLapPhieu) {
        this.ngayLapPhieu = ngayLapPhieu;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public Double getSoTienPhat() {
        return soTienPhat;
    }

    public void setSoTienPhat(Double soTienPhat) {
        this.soTienPhat = soTienPhat;
    }

    public Boolean getTrangThaiThanhToan() {
        return trangThaiThanhToan;
    }

    public void setTrangThaiThanhToan(Boolean trangThaiThanhToan) {
        this.trangThaiThanhToan = trangThaiThanhToan;
    }
}
