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
@Table(name = "Reservation")
public class Reservation implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "reservationId") 
    private Long reservationId; 
    
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "username", nullable = false) 
    private UserAccount reader; 
    
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "isbn", nullable = false) 
    private BookTitle bookTitle; 
    
    @Column(name = "reservedDate", nullable = false) 
    private LocalDateTime reservedDate; 
    
    @Enumerated(EnumType.STRING) 
    @Column(name = "status", nullable = false, length = 20) 
    private ReservationStatus status; // WAITING, RESERVED, NOTIFIED, CANCELLED, COMPLETED 

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public UserAccount getReader() {
        return reader;
    }

    public void setReader(UserAccount reader) {
        this.reader = reader;
    }

    public BookTitle getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(BookTitle bookTitle) {
        this.bookTitle = bookTitle;
    }

    public LocalDateTime getReservedDate() {
        return reservedDate;
    }

    public void setReservedDate(LocalDateTime reservedDate) {
        this.reservedDate = reservedDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
    
    public enum ReservationStatus { WAITING, RESERVED, NOTIFIED, CANCELLED, COMPLETED } 
    public Reservation() {}
}
