package com.library.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity 
@Table(name = "BorrowTicket")
public class BorrowTicket implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id 
    @Column(name="borrowId",length=50)
    private String borrowId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="username",nullable=false)
    private UserAccount reader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "librarianUsername",nullable=false)
    private UserAccount librarian;

    @Column(name="borrowDate",nullable=false)
    private LocalDateTime borrowDate;

    @Enumerated(EnumType.STRING)
    @Column(name="status",nullable=false,length=20)
    private BorrowStatus status; //BORROWING, COMPLETED

    @OneToMany(mappedBy="borrowTicket",cascade=CascadeType.ALL,fetch=FetchType.LAZY)
    private List<BorrowDetail> borrowDetails;

    public BorrowTicket() {
    }

    public String getBorrowId() {
        return borrowId;
    }

    public void setBorrowId(String borrowId) {
        this.borrowId = borrowId;
    }

    public UserAccount getReader() {
        return reader;
    }

    public void setReader(UserAccount reader) {
        this.reader = reader;
    }

    public UserAccount getLibrarian() {
        return librarian;
    }

    public void setLibrarian(UserAccount librarian) {
        this.librarian = librarian;
    }

    public LocalDateTime getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDateTime borrowDate) {
        this.borrowDate = borrowDate;
    }

    public BorrowStatus getStatus() {
        return status;
    }

    public void setStatus(BorrowStatus status) {
        this.status = status;
    }

    public List getBorrowDetails() {
        return borrowDetails;
    }

    public void setBorrowDetails(List borrowDetails) {
        this.borrowDetails = borrowDetails;
    }

    public enum BorrowStatus{BORROWING,COMPLETED}
    

}
