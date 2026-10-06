package com.library.repository;

import java.util.List;

import com.library.entities.DatTruoc;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

public class DatTruocRepository {

    public boolean taoDatTruoc(DatTruoc dt) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            em.persist(dt);

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

    public List<DatTruoc> getDanhSachChoByMaDauSach(
            Integer maDauSach
    ) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            String jpql =
                    "SELECT d FROM DatTruoc d "
                            + "WHERE d.dauSach.maDauSach = :maDS "
                            + "AND d.trangThai = 'WAITING' "
                            + "ORDER BY d.ngayDat ASC";

            TypedQuery<DatTruoc> query =
                    em.createQuery(
                            jpql,
                            DatTruoc.class
                    );

            query.setParameter("maDS", maDauSach);

            return query.getResultList();
        }
    }
}