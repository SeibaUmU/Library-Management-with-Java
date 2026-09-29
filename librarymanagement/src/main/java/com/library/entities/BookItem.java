package com.library.entities;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
@Entity
@Table(name = "BookItem")
public class BookItem implements Serializable {
    private static final long serialVersionID = 1L;

    @Id 
    @Column(name = "bookItem",length=50)
    private String bookItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "isbn", nullable=false)
    private BookTitle bookTitle;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable=false,length=20)
    private ItemStatus status; //Available, Borrowed, Reserved

    public BookItem() {}

    public String getBookItemId() {
        return bookItemId;
    }

    public void setBookItemId(String bookItemId) {
        this.bookItemId = bookItemId;
    }

    public BookTitle getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(BookTitle bookTitle) {
        this.bookTitle = bookTitle;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public enum ItemStatus{AVAILABLE,BORROWED,RESERVED}
    
}
