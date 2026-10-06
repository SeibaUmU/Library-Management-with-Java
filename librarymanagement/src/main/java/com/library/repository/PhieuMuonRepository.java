package com.library.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.library.entities.ChiTietPhieuMuon;
import com.library.entities.CuonSach;
import com.library.entities.NguoiDung;
import com.library.entities.PhieuMuon;
import com.library.entities.PhieuPhat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

public class PhieuMuonRepository {

    // 1. Đếm số cuốn sách độc giả đang mượn chưa trả
    public long countActiveBorrowingByMaThe(Integer maThe) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {
            String jpql = "SELECT COUNT(ct) FROM ChiTietPhieuMuon ct " +
                          "WHERE ct.phieuMuon.theDocGia.maThe = :maThe AND ct.ngayTraThucTe IS NULL";

            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("maThe", maThe);

            return query.getSingleResult();
        }
    }

    // 2. Lập Phiếu mượn sách (Khóa Bi quan PESSIMISTIC_WRITE để chống tranh chấp)
    public boolean taoPhieuMuonSafe(
            PhieuMuon phieuMuon,
            List<String> danhSachMaCuonSach,
            int soNgayMuonToiDa
    ) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            LocalDate ngayHenTra =
                    LocalDate.now().plusDays(soNgayMuonToiDa);

            phieuMuon.setNgayHenTra(ngayHenTra);
            phieuMuon.setNgayMuon(LocalDateTime.now());
            phieuMuon.setTrangThai("DANG_MUON");

            em.persist(phieuMuon);

            for (String maCuonSach : danhSachMaCuonSach) {

                // 🔒 Khóa dòng CuonSach dưới SQL Server bằng PESSIMISTIC_WRITE
                CuonSach cs = em.find(
                        CuonSach.class,
                        maCuonSach,
                        LockModeType.PESSIMISTIC_WRITE
                );

                if (cs == null
                        || !"AVAILABLE".equalsIgnoreCase(cs.getTrangThai()) && !"Sẵn sàng".equalsIgnoreCase(cs.getTrangThai())) {

                    throw new RuntimeException(
                            "Cuốn sách "
                                    + maCuonSach
                                    + " không có sẵn hoặc đã bị mượn!"
                    );
                }

                // Cập nhật trạng thái cuốn sách thành BORROWED
                cs.setTrangThai("BORROWED");
                em.merge(cs);

                // Tạo ChiTietPhieuMuon
                ChiTietPhieuMuon ct = new ChiTietPhieuMuon();

                ct.setPhieuMuon(phieuMuon);
                ct.setCuonSach(cs);

                em.persist(ct);
            }

            tx.commit();

            return true;

        } catch (Exception e) {

            if (tx.isActive()) {
                tx.rollback();
            }

            System.err.println(
                    "⚠️ [Lỗi Lập Phiếu Mượn]: "
                            + e.getMessage()
            );

            return false;

        } finally {
            em.close();
        }
    }

    // 3. Xử lý Trả sách & Tự động Tính Tiền Phạt Trễ Hạn
    public double traSach(
            Integer maCTPM,
            Integer maThuThu,
            String tinhTrangKhiTra,
            double tienPhatNgay
    ) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        double tienPhat = 0.0;

        try {
            tx.begin();

            ChiTietPhieuMuon ct =
                    em.find(
                            ChiTietPhieuMuon.class,
                            maCTPM
                    );

            if (ct == null
                    || ct.getNgayTraThucTe() != null) {

                return -1.0;
                // Không tìm thấy hoặc đã trả rồi
            }

            LocalDateTime now = LocalDateTime.now();

            ct.setNgayTraThucTe(now);
            ct.setTrangThaiSachKhiTra(tinhTrangKhiTra);

            em.merge(ct);

            // Cập nhật trạng thái Cuốn sách vật lý về AVAILABLE
            CuonSach cs = ct.getCuonSach();

            cs.setTrangThai("AVAILABLE");
            cs.setTinhTrangValLy(tinhTrangKhiTra);

            em.merge(cs);

            // Kiểm tra trễ hạn so với NgayHenTra
            PhieuMuon pm = ct.getPhieuMuon();

            if (now.toLocalDate().isAfter(pm.getNgayHenTra())) {

                long soNgayTre =
                        ChronoUnit.DAYS.between(
                                pm.getNgayHenTra(),
                                now.toLocalDate()
                        );

                tienPhat = soNgayTre * tienPhatNgay;

                // Tự động tạo Phiếu Phạt
                NguoiDung thuThu =
                        em.find(
                                NguoiDung.class,
                                maThuThu
                        );

                PhieuPhat pp = new PhieuPhat();

                pp.setChiTietPhieuMuon(ct);
                pp.setThuThu(thuThu);
                pp.setNgayLapPhieu(now);
                pp.setLyDo(
                        "Trễ hạn "
                                + soNgayTre
                                + " ngày"
                );
                pp.setSoTienPhat(tienPhat);
                pp.setTrangThaiThanhToan(false);

                em.persist(pp);
            }

            tx.commit();

            return tienPhat;

        } catch (Exception e) {

            if (tx.isActive()) {
                tx.rollback();
            }

            e.printStackTrace();

            return -1.0;

        } finally {
            em.close();
        }
    }
}
