package com.library.repository;

import java.util.List;

import com.library.entities.CuonSach;
import com.library.entities.DauSach;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

public class DauSachRepository {

    // 1. Tìm kiếm Đầu sách theo từ khóa tên sách/tác giả/thể loại
    //    (Phân trang)
    public List<DauSach> search(
            String keyword,
            Integer maTL,
            int page,
            int pageSize
    ) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            StringBuilder jpql =
                    new StringBuilder(
                            "SELECT d FROM DauSach d WHERE 1=1"
                    );

            if (keyword != null
                    && !keyword.trim().isEmpty()) {

                jpql.append(
                        " AND (LOWER(d.tenSach) LIKE :kw "
                                + "OR LOWER(d.tacGia.tenTacGia) LIKE :kw)"
                );
            }

            if (maTL != null && maTL > 0) {
                jpql.append(
                        " AND d.theLoai.maTL = :maTL"
                );
            }

            TypedQuery<DauSach> query =
                    em.createQuery(
                            jpql.toString(),
                            DauSach.class
                    );

            if (keyword != null
                    && !keyword.trim().isEmpty()) {

                query.setParameter(
                        "kw",
                        "%" + keyword.toLowerCase() + "%"
                );
            }

            if (maTL != null && maTL > 0) {
                query.setParameter(
                        "maTL",
                        maTL
                );
            }

            query.setFirstResult(
                    (page - 1) * pageSize
            );

            query.setMaxResults(pageSize);

            return query.getResultList();
        }
    }


    // 2. Tìm Đầu sách theo ID (kèm danh sách cuốn sách)
    public DauSach findById(Integer maDauSach) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            return em.find(
                    DauSach.class,
                    maDauSach
            );
        }
    }


    // 3. Tìm Cuốn sách vật lý theo Mã vạch
    public CuonSach findCuonSachByMa(String maCuonSach) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            return em.find(
                    CuonSach.class,
                    maCuonSach
            );
        }
    }


    // 4. Lấy danh sách Cuốn sách vật lý thuộc 1 Đầu sách
    public List<CuonSach> getCuonSachByDauSach(
            Integer maDauSach
    ) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            String jpql =
                    "SELECT c FROM CuonSach c "
                            + "WHERE c.dauSach.maDauSach = :maDS";

            TypedQuery<CuonSach> query =
                    em.createQuery(
                            jpql,
                            CuonSach.class
                    );

            query.setParameter(
                    "maDS",
                    maDauSach
            );

            return query.getResultList();
        }
    }


    // 5. Thêm Đầu sách mới
    public boolean saveDauSach(DauSach dauSach) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            em.persist(dauSach);

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


    // 6. Nhập danh sách Cuốn sách vật lý vào kho
    public boolean saveCuonSachList(
            List<CuonSach> danhSachCuonSach
    ) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            for (CuonSach cs : danhSachCuonSach) {
                em.persist(cs);
            }

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
