package com.library.repository;

import java.util.List;

import com.library.entities.PhieuPhat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

public class PhieuPhatRepository {

    // 1. Lấy danh sách phiếu phạt chưa thanh toán của độc giả
    public List<PhieuPhat> getPhieuPhatChuaThanhToanByMaThe(
            Integer maThe
    ) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            String jpql =
                    "SELECT p FROM PhieuPhat p "
                            + "WHERE p.chiTietPhieuMuon.phieuMuon.theDocGia.maThe = :maThe "
                            + "AND p.trangThaiThanhToan = false";

            TypedQuery<PhieuPhat> query =
                    em.createQuery(
                            jpql,
                            PhieuPhat.class
                    );

            query.setParameter("maThe", maThe);

            return query.getResultList();
        }
    }

    // 2. Thanh toán phiếu phạt
    public boolean thanhToanPhieuPhat(
            Integer maPhieuPhat
    ) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            PhieuPhat pp =
                    em.find(
                            PhieuPhat.class,
                            maPhieuPhat
                    );

            if (pp != null) {
                pp.setTrangThaiThanhToan(true);
                em.merge(pp);
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