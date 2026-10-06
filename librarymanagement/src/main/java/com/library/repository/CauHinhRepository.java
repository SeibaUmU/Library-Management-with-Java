package com.library.repository;

import com.library.entities.CauHinh;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class CauHinhRepository {

    public CauHinh getCauHinh() {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            CauHinh ch =
                    em.find(
                            CauHinh.class,
                            1
                    );

            if (ch == null) {

                ch = new CauHinh();

                ch.setMaCauHinh(1);
                ch.setSoSachToiDa(5);
                ch.setSoNgayMuonToiDa(14);
                ch.setTienPhatNgay(5000.0);

                saveCauHinh(ch);
            }

            return ch;
        }
    }

    public boolean saveCauHinh(CauHinh ch) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            em.merge(ch);

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
