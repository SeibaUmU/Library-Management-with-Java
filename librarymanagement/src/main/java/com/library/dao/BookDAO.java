package com.library.dao;

import java.util.List;

import com.library.entities.BookItem;
import com.library.entities.BookTitle; 

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery; 

public class BookDAO {
    //1. tim kiem dau sach theo tu khoa (ten sach, tac gia, the loai) co phan trang
    public List<BookTitle> searchBookTitles(String keyword, String category, int page, int pageSize){
        try (EntityManager em = EntityManagerUtil.getEntityManager()){
            StringBuilder jpql = new StringBuilder("SELECT b FROM BookTitle b WHERE 1=1");
            if (keyword !=null && !keyword.trim().isEmpty()){
                jpql.append(" AND (LOWER(b.title) LIKE :kw OR LOWER(b.author) LIKE :kw OR LOWER(b.isbn) LIKE :kw)");
            }
            if (category !=null && !category.trim().isEmpty()){
                jpql.append(" AND b.category = :cat");
            }

            TypedQuery<BookTitle> query = em.createQuery(jpql.toString(),BookTitle.class);
            if (keyword != null && !keyword.trim().isEmpty()) { 
                query.setParameter("kw", "%" + keyword.toLowerCase() + "%"); 
            } 
            if (category != null && !category.trim().isEmpty()) { 
                query.setParameter("cat", category); 
            } 
            query.setFirstResult((page - 1)* pageSize); 
            query.setMaxResults(pageSize); 
            return query.getResultList();
        }
    }

    // 2. Tìm Đầu sách theo ISBN (kèm danh sách cuốn sách vật lý) 
    public BookTitle findByIsbn(String isbn) { 
        try (EntityManager em = EntityManagerUtil.getEntityManager()) { 
            return em.find(BookTitle.class, isbn); 
        } 
    }
    // 3. Tìm Cuốn sách vật lý theo Mã vạch (bookItemId) 
    public BookItem findBookItemById(String bookItemId) { 
        try (EntityManager em = EntityManagerUtil.getEntityManager()) { 
            return em.find(BookItem.class, bookItemId); 
        } 
    }
    // 4. Thêm Đầu sách mới 
    public boolean saveBookTitle(BookTitle bookTitle) { 
        EntityManager em = EntityManagerUtil.getEntityManager(); 
        EntityTransaction tx = em.getTransaction(); 
        try { 
            tx.begin(); 
            em.persist(bookTitle); 
            tx.commit(); 
            return true; 
        } catch (Exception e) { 
            if (tx.isActive()) tx.rollback(); 
            e.printStackTrace(); 
            return false; 
        } finally { em.close(); } 
    }
    // 5\. Thêm Danh sách Cuốn sách vật lý (BookItem) &amp; Tăng số lượng tồn kho
    public boolean addBookItems(String isbn, List<BookItem> items){
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            BookTitle title = em.find(BookTitle.class,isbn);
            if (title==null) return false;

            for (BookItem item:items){
                item.setBookTitle(title);
                em.persist(item);
            }

            title.setTotalQuantity(title.getTotalQuantity() + items.size());
            title.setAvailableQuantity(title.getAvailableQuantity()+items.size());
            em.merge(title);

            tx.commit();
            return true;
        } catch (Exception e){
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
}