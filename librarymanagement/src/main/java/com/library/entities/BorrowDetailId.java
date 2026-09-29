package com.library.entities;

import java.io.Serializable; 
import java.util.Objects; 
public class BorrowDetailId implements Serializable {
    private static final long serialVersionUID = 1L;
    private String borrowTicket; // Trùng tên thuộc tính @Id bên BorrowDetail 
    private String bookItem; // Trùng tên thuộc tính @Id bên BorrowDetail 
    public BorrowDetailId() {} 
    public BorrowDetailId(String borrowTicket, String bookItem) { 
        this.borrowTicket = borrowTicket; 
        this.bookItem = bookItem; } 
    @Override 
    public boolean equals(Object o) { 
        if (this == o) return true; 
        if (o == null || getClass() != o.getClass()) return false; 
        BorrowDetailId that = (BorrowDetailId) o; 
        return Objects.equals(borrowTicket, that.borrowTicket) &&
                Objects.equals(bookItem, that.bookItem); 
    } 
    @Override public int hashCode() { 
        return Objects.hash(borrowTicket, bookItem); 
    } 
    public String getBorrowTicket() { 
        return borrowTicket; 
    } 
    public void setBorrowTicket(String borrowTicket) { 
        this.borrowTicket = borrowTicket; 
    } 
    public String getBookItem() { 
        return bookItem; 
    } 
    public void setBookItem(String bookItem) { 
        this.bookItem = bookItem; 
    } 
}
