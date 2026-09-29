package com.library.entities;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "SystemConfig")
public class SystemConfig implements Serializable{
    private static final long serialVersionUID = 1L; 
    @Id 
    @Column(name = "configId") 
    private Integer configId = 1; // Luôn giữ ID = 1 cho duy nhất 1 bản ghi cấu hình 
    
    @Column(name = "maxBorrowDays", nullable = false) 
    private Integer maxBorrowDays; 
    
    @Column(name = "maxBooksPerReader", nullable = false) 
    private Integer maxBooksPerReader; 
    
    @Column(name = "finePerDay", nullable = false) 
    private Double finePerDay; 
    
    @Column(name = "maxRenewTimes", nullable = false) 
    private Integer maxRenewTimes; 
    
    @Column(name = "holdKeepDays", nullable = false) 
    private Integer holdKeepDays; 
    
    public SystemConfig() {}

    public Integer getConfigId() {
        return configId;
    }

    public void setConfigId(Integer configId) {
        this.configId = configId;
    }

    public Integer getMaxBorrowDays() {
        return maxBorrowDays;
    }

    public void setMaxBorrowDays(Integer maxBorrowDays) {
        this.maxBorrowDays = maxBorrowDays;
    }

    public Integer getMaxBooksPerReader() {
        return maxBooksPerReader;
    }

    public void setMaxBooksPerReader(Integer maxBooksPerReader) {
        this.maxBooksPerReader = maxBooksPerReader;
    }

    public Double getFinePerDay() {
        return finePerDay;
    }

    public void setFinePerDay(Double finePerDay) {
        this.finePerDay = finePerDay;
    }

    public Integer getMaxRenewTimes() {
        return maxRenewTimes;
    }

    public void setMaxRenewTimes(Integer maxRenewTimes) {
        this.maxRenewTimes = maxRenewTimes;
    }

    public Integer getHoldKeepDays() {
        return holdKeepDays;
    }

    public void setHoldKeepDays(Integer holdKeepDays) {
        this.holdKeepDays = holdKeepDays;
    }
}
