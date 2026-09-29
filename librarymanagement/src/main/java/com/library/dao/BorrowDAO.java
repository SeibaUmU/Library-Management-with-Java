package com.library.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.library.entities.BookItem;
import com.library.entities.BookTitle;
import com.library.entities.BorrowDetail;
import com.library.entities.BorrowDetailId;
import com.library.entities.BorrowTicket;
import com.library.entities.FineTicket;

import jakarta.persistence.EntityManager; 
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery; 

public class BorrowDAO {
    //1. Kiem tra so sach Doc gia dang muon ch tra
    public long getActiveBorrowCount(String username){
        try (EntityManager em = EntityManagerUtil.getEntityManager()){
            String jpql = "SELECT COUNT(bd) FROM BorrowDetail bd WHERE bd.borrowTicket.reader.username = :uname AND bd.status = :st";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("uname", username);
            query.setParameter("st", BorrowDetail.DetailStatus.BORROWING);
            return query.getSingleResult();
        }
    }
    //2.Lap phieu muon sach (Transaction: Them ticket, detial, doi trang thai bookitem, giam kho)
    public boolean createBorrowTicket(BorrowTicket ticket, List<String> bookItemIds, int maxBorrowDays){
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(ticket);
            LocalDate dueDate = LocalDate.now().plusDays(maxBorrowDays);
            
            for (String itemId:bookItemIds){
                BookItem item = em.find(BookItem.class,itemId,LockModeType.PESSIMISTIC_WRITE);
                if (item==null || item.getStatus() !=BookItem.ItemStatus.AVAILABLE){
                    throw new RuntimeException("Sách " + itemId + " không có sẵn để mượn.");
                }
                BookTitle title = em.find(BookTitle.class,item.getBookTitle().getIsbn(),LockModeType.PESSIMISTIC_WRITE);
                if (title.getAvailableQuantity() <=0){
                    throw new RuntimeException("Đầu sách "+title.getTitle()+" đã hết tồn kho!");
                }
                //Đổi trạng thái sách
                item.setStatus(BookItem.ItemStatus.BORROWED);
                em.merge(item);

                // Giảm số lượng sách khả dụng của đầu sách
                title.setAvailableQuantity(title.getAvailableQuantity() - 1); 
                em.merge(title); // Tạo Chi tiết phiếu mượn 
                BorrowDetail detail = new BorrowDetail(); 
                detail.setBorrowTicket(ticket); 
                detail.setBookItem(item); 
                detail.setDueDate(dueDate); 
                detail.setRenewCount(0); 
                detail.setStatus(BorrowDetail.DetailStatus.BORROWING); 
                em.persist(detail);  
            }
            tx.commit();
            return true;
        } catch (Exception e){
            if (tx.isActive()) tx.rollback();
            System.err.println("[Tranh chấp dữ liệu]: " + e.getMessage());
            return false;
        } finally {em.close();}
    }
    // 3. Xử lý Trả sách & Tự động Tính Tiền Phạt Trễ Hạn
    public double processReturnBook(
            String borrowId,
            String bookItemId,
            double finePerDay
    ) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        double fineAmount = 0.0;

        try {
            tx.begin();

            BorrowDetailId detailId = new BorrowDetailId(borrowId, bookItemId);
            BorrowDetail detail = em.find(BorrowDetail.class, detailId);

            if (detail == null
                    || detail.getStatus() == BorrowDetail.DetailStatus.RETURNED) {
                return -1.0; // Đã trả hoặc không tồn tại
            }

            LocalDate now = LocalDate.now();

            detail.setReturnDate(now);
            detail.setStatus(BorrowDetail.DetailStatus.RETURNED);

            // Kiểm tra trễ hạn
            if (now.isAfter(detail.getDueDate())) {
                long overdueDays = ChronoUnit.DAYS.between(
                        detail.getDueDate(),
                        now
                );

                fineAmount = overdueDays * finePerDay;

                // Tự động lập Phiếu Phạt Trễ Hạn
                FineTicket fine = new FineTicket();

                fine.setBorrowTicket(detail.getBorrowTicket());
                fine.setBookItem(detail.getBookItem());
                fine.setReader(detail.getBorrowTicket().getReader());
                fine.setFineReason(FineTicket.FineReason.OVERDUE);
                fine.setFineAmount(fineAmount);
                fine.setPaidStatus(false);
                fine.setCreatedDate(LocalDateTime.now());

                em.persist(fine);
            }

            // Cập nhật trạng thái cuốn sách vật lý
            // và tăng số lượng tồn kho
            BookItem item = detail.getBookItem();

            item.setStatus(BookItem.ItemStatus.AVAILABLE);
            em.merge(item);

            BookTitle title = item.getBookTitle();

            title.setAvailableQuantity(
                    title.getAvailableQuantity() + 1
            );

            em.merge(title);
            em.merge(detail);

            tx.commit();

            return fineAmount;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }

            e.printStackTrace();
            return -1.0;

        } finally {
            em.close();
        }
    }


    // 4. Gia hạn Sách
    public boolean renewBook(
            String borrowId,
            String bookItemId,
            int extraDays,
            int maxRenewTimes
    ) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            BorrowDetailId detailId = new BorrowDetailId(borrowId, bookItemId);
            BorrowDetail detail = em.find(BorrowDetail.class, detailId);

            if (detail == null
                    || detail.getRenewCount() >= maxRenewTimes
                    || detail.getStatus() != BorrowDetail.DetailStatus.BORROWING) {
                return false;
            }

            detail.setDueDate(
                    detail.getDueDate().plusDays(extraDays)
            );

            detail.setRenewCount(
                    detail.getRenewCount() + 1
            );

            em.merge(detail);

            tx.commit();

            return true;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }

            e.printStackTrace();
            return false;

        } finally {
            em.close();
        }
    }
}