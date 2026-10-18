package com.library.repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.library.common.PasswordUtil;
import com.library.common.Role;
import com.library.common.TrangThai;
import com.library.entities.NguoiDung;
import com.library.entities.TheDocGia;
import com.library.entities.VaiTro;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

public class NguoiDungRepository {

    // 1. Xác thực đăng nhập (bằng Email hoặc Số điện thoại)
    public NguoiDung authenticate(
            String account,
            String password) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            List<NguoiDung> ungVien = em.createQuery(
                    "SELECT n FROM NguoiDung n "
                            + "LEFT JOIN FETCH n.vaiTro "
                            + "WHERE n.email = :acc OR n.soDienThoai = :acc",
                    NguoiDung.class)
                    .setParameter("acc", account)
                    .getResultList();

            for (NguoiDung n : ungVien) {
                if (PasswordUtil.verify(password, n.getMatKhau())) {
                    return n;
                }
            }

            return null;
        }
    }

    // 2. Tìm Người dùng theo Mã ND
    public NguoiDung findById(Integer maND) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            return em.find(
                    NguoiDung.class,
                    maND);
        }
    }

    // 3. Tìm Thẻ độc giả theo Mã ND
    public TheDocGia findTheDocGiaByMaND(Integer maND) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            List<TheDocGia> ds = em.createQuery(
                    "SELECT t FROM TheDocGia t "
                            + "WHERE t.nguoiDung.maND = :maND "
                            + "ORDER BY t.maThe DESC",
                    TheDocGia.class)
                    .setParameter("maND", maND)
                    .setMaxResults(1)
                    .getResultList();

            return ds.isEmpty() ? null : ds.get(0);
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
    /*
     * public boolean updateTrangThaiThe(
     * Integer maThe,
     * String trangThaiMoi) {
     * EntityManager em = EntityManagerUtil.getEntityManager();
     * EntityTransaction tx = em.getTransaction();
     * 
     * try {
     * tx.begin();
     * 
     * TheDocGia the = em.find(
     * TheDocGia.class,
     * maThe);
     * 
     * if (the != null) {
     * the.setTrangThai(trangThaiMoi);
     * em.merge(the);
     * }
     * 
     * tx.commit();
     * 
     * return true;
     * 
     * } catch (Exception e) {
     * 
     * if (tx.isActive()) {
     * tx.rollback();
     * }
     * 
     * e.printStackTrace();
     * 
     * return false;
     * 
     * } finally {
     * em.close();
     * }
     * }
     */

    // 8. Kiểm tra trùng Email / SĐT (excludeMaND: loại trừ chính người đang sửa)
    public boolean existsEmailHoacSdt(
            String email,
            String soDienThoai,
            Integer excludeMaND) {
        boolean coEmail = email != null && !email.trim().isEmpty();
        boolean coSdt = soDienThoai != null && !soDienThoai.trim().isEmpty();

        if (!coEmail && !coSdt) {
            return false;
        }

        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            StringBuilder jpql = new StringBuilder(
                    "SELECT COUNT(n) FROM NguoiDung n WHERE (");

            if (coEmail) {
                jpql.append("n.email = :email");
            }
            if (coEmail && coSdt) {
                jpql.append(" OR ");
            }
            if (coSdt) {
                jpql.append("n.soDienThoai = :sdt");
            }
            jpql.append(")");

            if (excludeMaND != null) {
                jpql.append(" AND n.maND <> :ex");
            }

            TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);

            if (coEmail) {
                query.setParameter("email", email.trim());
            }
            if (coSdt) {
                query.setParameter("sdt", soDienThoai.trim());
            }
            if (excludeMaND != null) {
                query.setParameter("ex", excludeMaND);
            }

            return query.getSingleResult() > 0;
        }
    }

    // 9. Tìm người dùng theo mã VÀ vai trò (VD: thủ thư = 2, độc giả = 3)
    public NguoiDung findByIdAndVaiTro(Integer maND, int maVaiTro) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            List<NguoiDung> ds = em.createQuery(
                    "SELECT n FROM NguoiDung n "
                            + "WHERE n.maND = :id AND n.vaiTro.maVaiTro = :vt",
                    NguoiDung.class)
                    .setParameter("id", maND)
                    .setParameter("vt", maVaiTro)
                    .getResultList();

            return ds.isEmpty() ? null : ds.get(0);
        }
    }

    // 10. Danh sách theo vai trò, có tìm kiếm + phân trang (GET_LIBRARIANS)
    private static String buildWhereVaiTro(
            int maVaiTro,
            String keyword,
            Map<String, Object> p) {
        StringBuilder w = new StringBuilder(" WHERE n.vaiTro.maVaiTro = :vt");
        p.put("vt", maVaiTro);

        if (keyword != null && !keyword.trim().isEmpty()) {
            w.append(" AND (LOWER(n.hoTen) LIKE :kw"
                    + " OR LOWER(n.email) LIKE :kw"
                    + " OR n.soDienThoai LIKE :kw)");
            p.put("kw", "%" + keyword.trim().toLowerCase() + "%");
        }

        return w.toString();
    }

    public List<NguoiDung> searchByVaiTro(
            int maVaiTro,
            String keyword,
            int page,
            int pageSize) {
        Map<String, Object> params = new HashMap<>();
        String where = buildWhereVaiTro(maVaiTro, keyword, params);

        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            TypedQuery<NguoiDung> query = em.createQuery(
                    "SELECT n FROM NguoiDung n" + where + " ORDER BY n.maND",
                    NguoiDung.class);

            for (Map.Entry<String, Object> e : params.entrySet()) {
                query.setParameter(e.getKey(), e.getValue());
            }

            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);

            return query.getResultList();
        }
    }

    public long countByVaiTro(int maVaiTro, String keyword) {
        Map<String, Object> params = new HashMap<>();
        String where = buildWhereVaiTro(maVaiTro, keyword, params);

        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(n) FROM NguoiDung n" + where,
                    Long.class);

            for (Map.Entry<String, Object> e : params.entrySet()) {
                query.setParameter(e.getKey(), e.getValue());
            }

            return query.getSingleResult();
        }
    }

    // 11. Tạo tài khoản với vai trò cho trước (ADD_LIBRARIAN).
    // nd.matKhau phải được băm (PasswordUtil.hash) trước khi gọi.
    public boolean saveNguoiDungVoiVaiTro(NguoiDung nd, int maVaiTro) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            nd.setVaiTro(em.getReference(VaiTro.class, maVaiTro));

            if (nd.getTrangThai() == null) {
                nd.setTrangThai(true);
            }

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

    // 12. Cấp tài khoản độc giả: NguoiDung (vai trò 3) + TheDocGia trong 1
    // transaction
    // (ADD_READER). nd.matKhau phải được băm trước khi gọi.
    public boolean taoDocGia(NguoiDung nd, TheDocGia the) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            nd.setVaiTro(em.getReference(VaiTro.class, Role.READER_ID));

            if (nd.getTrangThai() == null) {
                nd.setTrangThai(true);
            }

            em.persist(nd);

            the.setNguoiDung(nd);

            if (the.getNgayCap() == null) {
                the.setNgayCap(LocalDate.now());
            }
            if (the.getTrangThai() == null) {
                the.setTrangThai(TrangThai.THE_ACTIVE);
            }

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
}
