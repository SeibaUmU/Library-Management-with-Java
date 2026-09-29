package com.library.dao;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class ReportDAO {
    //thong ke Top sach muon nhieu nhat
    @SuppressWarnings("unchecked")
    public List<Object[]> getTopBorrowedBooks(int limit){
        try (EntityManager em = EntityManagerUtil.getEntityManager()){
            String jpql = "SELECT bd.bookItem.bookTitle.title, COUNT(bd) FROM BorrowDetail bd " +
                        "GROUP BY bd.bookItem.bookTitle.title ORDER BY COUNT(bd) DESC";
            Query query = em.createQuery(jpql);
            query.setMaxResults(limit);
            return query.getResultList();         
        }
    }
}
