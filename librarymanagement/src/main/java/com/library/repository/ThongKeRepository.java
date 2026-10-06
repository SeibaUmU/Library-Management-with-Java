package com.library.repository;


import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class ThongKeRepository {

    @SuppressWarnings("unchecked")
    public List<Object[]> getTopSachMuonNhieuNhat(
            int limit
    ) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            String jpql =
                    "SELECT ct.cuonSach.dauSach.tenSach, COUNT(ct) "
                            + "FROM ChiTietPhieuMuon ct "
                            + "GROUP BY ct.cuonSach.dauSach.tenSach "
                            + "ORDER BY COUNT(ct) DESC";

            Query query = em.createQuery(jpql);

            query.setMaxResults(limit);

            return query.getResultList();
        }
    }
}
