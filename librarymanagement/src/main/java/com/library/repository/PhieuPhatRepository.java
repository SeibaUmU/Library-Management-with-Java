package com.library.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.library.common.NghiepVuException;
import com.library.entities.ChiTietPhieuMuon;
import com.library.entities.NguoiDung;
import com.library.entities.PhieuPhat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

public class PhieuPhatRepository {
    public static final int THANH_TOAN_OK = 0;
    public static final int THANH_TOAN_KHONG_TIM_THAY = 1;
    public static final int THANH_TOAN_DA_NOP = 2;
    public static final int THANH_TOAN_LOI = -1;

    // 1. Lấy danh sách phiếu phạt chưa thanh toán của độc giả
    public List<PhieuPhat> getPhieuPhatChuaThanhToanByMaThe(
            Integer maThe) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            String jpql = "SELECT p FROM PhieuPhat p "
                    + "WHERE p.chiTietPhieuMuon.phieuMuon.theDocGia.maThe = :maThe "
                    + "AND p.trangThaiThanhToan = false";

            TypedQuery<PhieuPhat> query = em.createQuery(
                    jpql,
                    PhieuPhat.class);

            query.setParameter("maThe", maThe);

            return query.getResultList();
        }
    }

    // 2. Thanh toán phiếu phạt (thay hàm cũ)
    public int thanhToanPhieuPhat(Integer maPhieuPhat) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            PhieuPhat pp = em.find(PhieuPhat.class, maPhieuPhat);

            if (pp == null) {
                tx.rollback();
                return THANH_TOAN_KHONG_TIM_THAY;
            }

            if (Boolean.TRUE.equals(pp.getTrangThaiThanhToan())) {
                tx.rollback();
                return THANH_TOAN_DA_NOP;
            }

            pp.setTrangThaiThanhToan(true);

            tx.commit();
            return THANH_TOAN_OK;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            return THANH_TOAN_LOI;

        } finally {
            em.close();
        }
    }

    // 3. Đếm phiếu phạt chưa nộp của độc giả (UC-13)
    public long countChuaThanhToanByMaThe(Integer maThe) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT COUNT(p) FROM PhieuPhat p "
                            + "WHERE p.chiTietPhieuMuon.phieuMuon.theDocGia.maThe = :maThe "
                            + "AND p.trangThaiThanhToan = false",
                    Long.class)
                    .setParameter("maThe", maThe)
                    .getSingleResult();
        }
    }

    // 4. Tổng phạt của từng dòng mượn (cho tab "Lịch sử trả" - UC-08)
    public static class TongPhat {
        public double tongTien;
        public boolean daNopHet;
    }

    public Map<Integer, TongPhat> getTongPhatTheoCTPM(Collection<Integer> maCTPMList) {
        Map<Integer, TongPhat> ketQua = new HashMap<>();

        if (maCTPMList == null || maCTPMList.isEmpty()) {
            return ketQua;
        }

        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            List<Object[]> rows = em.createQuery(
                    "SELECT p.chiTietPhieuMuon.maCTPM, SUM(p.soTienPhat), "
                            + "SUM(CASE WHEN p.trangThaiThanhToan = true THEN 0 ELSE 1 END) "
                            + "FROM PhieuPhat p "
                            + "WHERE p.chiTietPhieuMuon.maCTPM IN (:ids) "
                            + "GROUP BY p.chiTietPhieuMuon.maCTPM",
                    Object[].class)
                    .setParameter("ids", maCTPMList)
                    .getResultList();

            for (Object[] r : rows) {
                TongPhat tp = new TongPhat();
                tp.tongTien = r[1] != null ? ((Number) r[1]).doubleValue() : 0.0;
                tp.daNopHet = ((Number) r[2]).longValue() == 0;
                ketQua.put(((Number) r[0]).intValue(), tp);
            }
        }

        return ketQua;
    }

    // 5. Danh sách phiếu phạt có lọc + phân trang (GET_FINES - UC-14)
    public List<PhieuPhat> search(
            Integer maThe,
            Boolean daThanhToan,
            int page,
            int pageSize) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            StringBuilder jpql = new StringBuilder(
                    "SELECT p FROM PhieuPhat p "
                            + "JOIN FETCH p.chiTietPhieuMuon ct "
                            + "JOIN FETCH ct.phieuMuon pm "
                            + "JOIN FETCH pm.theDocGia t "
                            + "JOIN FETCH t.nguoiDung "
                            + "LEFT JOIN FETCH p.cuonSach cs "
                            + "LEFT JOIN FETCH cs.dauSach "
                            + "WHERE 1=1");

            if (maThe != null) {
                jpql.append(" AND t.maThe = :maThe");
            }
            if (daThanhToan != null) {
                jpql.append(" AND p.trangThaiThanhToan = :paid");
            }

            jpql.append(" ORDER BY p.ngayLapPhieu DESC, p.maPhieuPhat DESC");

            TypedQuery<PhieuPhat> query = em.createQuery(jpql.toString(), PhieuPhat.class);

            if (maThe != null) {
                query.setParameter("maThe", maThe);
            }
            if (daThanhToan != null) {
                query.setParameter("paid", daThanhToan);
            }

            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);

            return query.getResultList();
        }
    }

    public long count(Integer maThe, Boolean daThanhToan) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            StringBuilder jpql = new StringBuilder(
                    "SELECT COUNT(p) FROM PhieuPhat p WHERE 1=1");

            if (maThe != null) {
                jpql.append(" AND p.chiTietPhieuMuon.phieuMuon.theDocGia.maThe = :maThe");
            }
            if (daThanhToan != null) {
                jpql.append(" AND p.trangThaiThanhToan = :paid");
            }

            TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);

            if (maThe != null) {
                query.setParameter("maThe", maThe);
            }
            if (daThanhToan != null) {
                query.setParameter("paid", daThanhToan);
            }

            return query.getSingleResult();
        }
    }

    // 6. Tổng tiền phạt chưa nộp (maThe = null: toàn hệ thống)
    public double tongChuaThanhToan(Integer maThe) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            StringBuilder jpql = new StringBuilder(
                    "SELECT COALESCE(SUM(p.soTienPhat), 0.0) FROM PhieuPhat p "
                            + "WHERE p.trangThaiThanhToan = false");

            if (maThe != null) {
                jpql.append(" AND p.chiTietPhieuMuon.phieuMuon.theDocGia.maThe = :maThe");
            }

            TypedQuery<Double> query = em.createQuery(jpql.toString(), Double.class);

            if (maThe != null) {
                query.setParameter("maThe", maThe);
            }

            return query.getSingleResult();
        }
    }

    private static String cat(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() > max ? s.substring(0, max) : s;
    }

    // Phiếu phạt kèm cuốn sách + độc giả (dựng response sau khi tạo)
    public PhieuPhat findByIdFetch(Integer maPhieuPhat) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            List<PhieuPhat> ds = em.createQuery(
                    "SELECT p FROM PhieuPhat p "
                            + "JOIN FETCH p.chiTietPhieuMuon "
                            + "LEFT JOIN FETCH p.cuonSach "
                            + "LEFT JOIN FETCH p.nguoiDung "
                            + "WHERE p.maPhieuPhat = :id",
                    PhieuPhat.class)
                    .setParameter("id", maPhieuPhat)
                    .getResultList();

            return ds.isEmpty() ? null : ds.get(0);
        }
    }

    // Dòng mượn này đã có phiếu phạt trễ hạn chưa? (tránh phạt trễ hạn hai lần)
    public boolean daCoPhieuTreHan(Integer maCTPM) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            Long n = em.createQuery(
                    "SELECT COUNT(p) FROM PhieuPhat p "
                            + "WHERE p.chiTietPhieuMuon.maCTPM = :id "
                            + "AND p.lyDo LIKE :lk",
                    Long.class)
                    .setParameter("id", maCTPM)
                    .setParameter("lk", "Trễ hạn%")
                    .getSingleResult();

            return n > 0;
        }
    }

    // Lập một phiếu phạt riêng lẻ (UC-14)
    public PhieuPhat taoPhieuPhat(
            Integer maCTPM,
            Integer maThuThu,
            String lyDo,
            double soTien,
            boolean daThanhToan) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            ChiTietPhieuMuon ct = em.find(ChiTietPhieuMuon.class, maCTPM);

            if (ct == null) {
                throw new NghiepVuException("Chi tiết phiếu mượn không tồn tại");
            }

            PhieuPhat pp = new PhieuPhat();
            pp.setChiTietPhieuMuon(ct);
            pp.setCuonSach(ct.getCuonSach());
            pp.setNguoiDung(ct.getPhieuMuon().getTheDocGia().getNguoiDung());
            pp.setThuThu(em.find(NguoiDung.class, maThuThu));
            pp.setNgayLapPhieu(LocalDateTime.now());
            pp.setLyDo(cat(lyDo, 200));
            pp.setSoTienPhat(soTien);
            pp.setTrangThaiThanhToan(daThanhToan);

            em.persist(pp);

            tx.commit();

            return pp;

        } catch (NghiepVuException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new NghiepVuException("Không thể lập phiếu phạt do lỗi hệ thống!");

        } finally {
            em.close();
        }
    }
}