package com.library.dao;

import java.util.List;

import com.library.entities.UserAccount;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class UserDAO {
    //1. Xac thuc dang nhap
    public UserAccount authenticate(String username, String password) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {
            String jpql = "SELECT u FROM UserAccount u WHERE u.username = :username AND u.password = :password";
            TypedQuery<UserAccount> query = em.createQuery(jpql, UserAccount.class);
            query.setParameter("username", username);
            query.setParameter("password", password);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    //2.tim theo username
    public UserAccount findByUsername(String username) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {
            return em.find(UserAccount.class, username);
        }
    }
    
    
    //3. tao moi tai khoan (Doc gia/ Thu Thu)
    public boolean save(UserAccount user) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(user);
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
    // 4. Cập nhật thông tin / Đổi mật khẩu / Khóa thẻ
    public boolean update(UserAccount user) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.merge(user);
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

    public List<UserAccount> getAllUsers() {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {
            String jpql = "SELECT u FROM UserAccount u ORDER BY u.username ASC";
            return em.createQuery(jpql, UserAccount.class).getResultList();
        }
    }
    
    public void delete(String username) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            UserAccount user = em.find(UserAccount.class, username);
            if (user != null) {
                em.remove(user);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}

