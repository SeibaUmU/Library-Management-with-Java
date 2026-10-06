package com.library.entities; 
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(name = "VaiTro") 
public class VaiTro implements Serializable { 
    @Id 
    @Column(name = "MaVaiTro") 
    private Integer maVaiTro; 
    @Column(name = "TenVaiTro", length = 50) 
    private String tenVaiTro; 
    public VaiTro() {} 
    // Getters &amp; Setters... 
    public Integer getMaVaiTro() { return maVaiTro; } 
    public void setMaVaiTro(Integer maVaiTro) { this.maVaiTro = maVaiTro; } 
    public String getTenVaiTro() { return tenVaiTro; } 
    public void setTenVaiTro(String tenVaiTro) { this.tenVaiTro = tenVaiTro; } 
}