package com.library.dao;

import java.util.List;

import com.library.entities.Reservation;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

public class ReservationDAO {

    public boolean createReservation(Reservation reservation) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            em.persist(reservation);

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


    public List<Reservation> getWaitingListByIsbn(String isbn) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            String jpql = "SELECT r FROM Reservation r "
                    + "WHERE r.bookTitle.isbn = :isbn "
                    + "AND r.status = :st "
                    + "ORDER BY r.reservedDate ASC";

            TypedQuery<Reservation> query =
                    em.createQuery(jpql, Reservation.class);

            query.setParameter("isbn", isbn);
            query.setParameter(
                    "st",
                    Reservation.ReservationStatus.WAITING
            );

            return query.getResultList();
        }
    }
}
