package com.library.entities;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "NguoiDung")
public class NguoiDung implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaND")
    private Integer maND;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaVaiTro")
    private VaiTro vaiTro;

    @Column(name = "HoTen", length = 100)
    private String hoTen;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "SoDienThoai", length = 15)
    private String soDienThoai;

    @Column(name = "MatKhau", length = 255)
    private String matKhau;

    @Column(name = "TrangThai")
    private Boolean trangThai;


    public NguoiDung() {
    }


    // Getters & Setters

    public Integer getMaND() {
        return maND;
    }

    public void setMaND(Integer maND) {
        this.maND = maND;
    }

    public VaiTro getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(VaiTro vaiTro) {
        this.vaiTro = vaiTro;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }
}
