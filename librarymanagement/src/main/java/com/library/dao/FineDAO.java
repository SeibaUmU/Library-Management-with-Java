package com.library.dao;

import java.util.List;

import com.library.entities.FineTicket;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

public class FineDAO {

    public List<FineTicket> getUnpaidFinesByUser(String username) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            String jpql = "SELECT f FROM FineTicket f "
                    + "WHERE f.reader.username = :uname "
                    + "AND f.paidStatus = false";

            TypedQuery<FineTicket> query =
                    em.createQuery(jpql, FineTicket.class);

            query.setParameter("uname", username);

            return query.getResultList();
        }
    }


    public boolean payFine(Long fineId) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            FineTicket fine = em.find(FineTicket.class, fineId);

            if (fine != null) {
                fine.setPaidStatus(true);
                em.merge(fine);
            }

            tx.commit();

            return true;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }

            return false;

        } finally {
            em.close();
        }
    }
}
