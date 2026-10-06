package com.library.repository;

import com.library.entities.NguoiDung;
import com.library.entities.TheDocGia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class NguoiDungRepository {

    // 1. Xác thực đăng nhập (bằng Email hoặc Số điện thoại)
    public NguoiDung authenticate(
            String account,
            String password
    ) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            String jpql =
                    "SELECT n FROM NguoiDung n "
                            + "WHERE (n.email = :acc "
                            + "OR n.soDienThoai = :acc) "
                            + "AND n.matKhau = :pass";

            TypedQuery<NguoiDung> query =
                    em.createQuery(
                            jpql,
                            NguoiDung.class
                    );

            query.setParameter("acc", account);
            query.setParameter("pass", password);

            return query.getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }


    // 2. Tìm Người dùng theo Mã ND
    public NguoiDung findById(Integer maND) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            return em.find(
                    NguoiDung.class,
                    maND
            );
        }
    }


    // 3. Tìm Thẻ độc giả theo Mã ND
    public TheDocGia findTheDocGiaByMaND(Integer maND) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            String jpql =
                    "SELECT t FROM TheDocGia t "
                            + "WHERE t.nguoiDung.maND = :maND";

            TypedQuery<TheDocGia> query =
                    em.createQuery(
                            jpql,
                            TheDocGia.class
                    );

            query.setParameter("maND", maND);

            return query.getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }


    // 4. Lưu NguoiDung mới
    public boolean saveNguoiDung(NguoiDung nd) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            em.persist(nd);

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


    // 5. Lưu Thẻ Độc Giả mới
    public boolean saveTheDocGia(TheDocGia the) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            em.persist(the);

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


    // 6. Cập nhật thông tin Người dùng / Đổi mật khẩu
    public boolean updateNguoiDung(NguoiDung nd) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            em.merge(nd);

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


    // 7. Cập nhật trạng thái Thẻ Độc Giả (Khóa / Mở khóa)
    public boolean updateTrangThaiThe(
            Integer maThe,
            String trangThaiMoi
    ) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            TheDocGia the =
                    em.find(
                            TheDocGia.class,
                            maThe
                    );

            if (the != null) {
                the.setTrangThai(trangThaiMoi);
                em.merge(the);
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
