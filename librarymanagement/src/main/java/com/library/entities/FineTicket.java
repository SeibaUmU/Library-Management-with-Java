package com.library.entities;

import java.io.Serializable; 
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity 
@Table(name="FineTicket")
public class FineTicket implements Serializable{
    private static final long serialVersionUID = 1L;
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "fineId") 
    private Long fineId; 
    
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "borrowId", nullable = false) 
    private BorrowTicket borrowTicket; 
    
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "bookItemId", nullable = false) 
    private BookItem bookItem; 
    
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "username", nullable = false) 
    private UserAccount reader; 
    
    @Enumerated(EnumType.STRING) 
    @Column(name = "fineReason", nullable = false, length = 20) 
    private FineReason fineReason; // OVERDUE, DAMAGED, LOST 
    
    @Column(name = "fineAmount", nullable = false) 
    private Double fineAmount; 
    
    @Column(name = "paidStatus", nullable = false)
    private Boolean paidStatus = false; 
    
    @Column(name = "createdDate", nullable = false) 
    private LocalDateTime createdDate; 
    
    public enum FineReason { OVERDUE, DAMAGED, LOST } 
    
    public FineTicket() {}

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public Long getFineId() {
        return fineId;
    }

    public void setFineId(Long fineId) {
        this.fineId = fineId;
    }

    public BorrowTicket getBorrowTicket() {
        return borrowTicket;
    }

    public void setBorrowTicket(BorrowTicket borrowTicket) {
        this.borrowTicket = borrowTicket;
    }

    public BookItem getBookItem() {
        return bookItem;
    }

    public void setBookItem(BookItem bookItem) {
        this.bookItem = bookItem;
    }

    public UserAccount getReader() {
        return reader;
    }

    public void setReader(UserAccount reader) {
        this.reader = reader;
    }

    public FineReason getFineReason() {
        return fineReason;
    }

    public void setFineReason(FineReason fineReason) {
        this.fineReason = fineReason;
    }

    public Double getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(Double fineAmount) {
        this.fineAmount = fineAmount;
    }

    public Boolean getPaidStatus() {
        return paidStatus;
    }

    public void setPaidStatus(Boolean paidStatus) {
        this.paidStatus = paidStatus;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
