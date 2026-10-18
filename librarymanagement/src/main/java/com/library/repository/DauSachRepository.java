package com.library.repository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.library.common.BadRequestException;
import com.library.common.NghiepVuException;
import com.library.common.TrangThai;
import com.library.entities.CuonSach;
import com.library.entities.DauSach;
import com.library.entities.NhaXuatBan;
import com.library.entities.TacGia;
import com.library.entities.TheLoai;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

public class DauSachRepository {
    private static String buildSearchWhere(
            String keyword,
            Integer maTL,
            Map<String, Object> p) {
        StringBuilder w = new StringBuilder(" WHERE 1=1");

        if (keyword != null && !keyword.trim().isEmpty()) {
            w.append(" AND (LOWER(d.tenSach) LIKE :kw"
                    + " OR LOWER(tg.tenTacGia) LIKE :kw"
                    + " OR LOWER(nxb.tenNXB) LIKE :kw"
                    + " OR LOWER(tl.tenTL) LIKE :kw)");
            p.put("kw", "%" + keyword.trim().toLowerCase() + "%");
        }

        if (maTL != null && maTL > 0) {
            w.append(" AND tl.maTL = :maTL");
            p.put("maTL", maTL);
        }

        return w.toString();
    }

    // 1. Tìm kiếm Đầu sách theo từ khóa tên sách/tác giả/thể loại
    // (Phân trang)
    public List<DauSach> search(
            String keyword,
            Integer maTL,
            int page,
            int pageSize) {
        if (page < 1)
            page = 1;
        if (pageSize < 1)
            pageSize = 10;

        Map<String, Object> params = new HashMap<>();
        String where = buildSearchWhere(keyword, maTL, params);

        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            TypedQuery<DauSach> query = em.createQuery(
                    "SELECT d FROM DauSach d "
                            + "LEFT JOIN FETCH d.tacGia tg "
                            + "LEFT JOIN FETCH d.nhaXuatBan nxb "
                            + "LEFT JOIN FETCH d.theLoai tl"
                            + where
                            + " ORDER BY d.maDauSach",
                    DauSach.class);

            for (Map.Entry<String, Object> e : params.entrySet()) {
                query.setParameter(e.getKey(), e.getValue());
            }

            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);

            return query.getResultList();
        }
    }

    public long countSearch(String keyword, Integer maTL) {
        Map<String, Object> params = new HashMap<>();
        String where = buildSearchWhere(keyword, maTL, params);

        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(d) FROM DauSach d "
                            + "LEFT JOIN d.tacGia tg "
                            + "LEFT JOIN d.nhaXuatBan nxb "
                            + "LEFT JOIN d.theLoai tl"
                            + where,
                    Long.class);

            for (Map.Entry<String, Object> e : params.entrySet()) {
                query.setParameter(e.getKey(), e.getValue());
            }

            return query.getSingleResult();
        }
    }

    // Cuốn sách kèm đầu sách + tác giả (GET_COPY_BY_BARCODE)
    public CuonSach findCuonSachFetch(String maCuonSach) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            List<CuonSach> ds = em.createQuery(
                    "SELECT c FROM CuonSach c "
                            + "LEFT JOIN FETCH c.dauSach d "
                            + "LEFT JOIN FETCH d.tacGia "
                            + "WHERE c.maCuonSach = :ma",
                    CuonSach.class)
                    .setParameter("ma", maCuonSach)
                    .getResultList();

            return ds.isEmpty() ? null : ds.get(0);
        }
    }

    // 2. Tìm Đầu sách theo Mã đầu sách
    public DauSach findById(Integer maDauSach) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            List<DauSach> ketQua = em.createQuery(
                    "SELECT d FROM DauSach d "
                            + "LEFT JOIN FETCH d.tacGia "
                            + "LEFT JOIN FETCH d.nhaXuatBan "
                            + "LEFT JOIN FETCH d.theLoai "
                            + "WHERE d.maDauSach = :id",
                    DauSach.class)
                    .setParameter("id", maDauSach)
                    .getResultList();

            return ketQua.isEmpty() ? null : ketQua.get(0);
        }
    }

    // 3. Tìm Cuốn sách vật lý theo Mã vạch
    public CuonSach findCuonSachByMa(String maCuonSach) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            return em.find(
                    CuonSach.class,
                    maCuonSach);
        }
    }

    // 4. Lấy danh sách Cuốn sách vật lý thuộc 1 Đầu sách
    public List<CuonSach> getCuonSachByDauSach(
            Integer maDauSach) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            String jpql = "SELECT c FROM CuonSach c "
                    + "WHERE c.dauSach.maDauSach = :maDS";

            TypedQuery<CuonSach> query = em.createQuery(
                    jpql,
                    CuonSach.class);

            query.setParameter(
                    "maDS",
                    maDauSach);

            return query.getResultList();
        }
    }

    // 5. Thêm Đầu sách mới
    /*
     * public boolean saveDauSach(DauSach dauSach) {
     * EntityManager em = EntityManagerUtil.getEntityManager();
     * EntityTransaction tx = em.getTransaction();
     * 
     * try {
     * tx.begin();
     * 
     * em.persist(dauSach);
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

    // 6. Nhập danh sách Cuốn sách vật lý vào kho
    public boolean saveCuonSachList(
            List<CuonSach> danhSachCuonSach) {
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
    // =====================================================================
    // UC-12: QUẢN LÝ KHO SÁCH
    // =====================================================================

    private static <T> T timHoacLoi(
            EntityManager em,
            Class<T> cls,
            Integer id,
            String ten) {
        T e = em.find(cls, id);

        if (e == null) {
            throw new NghiepVuException("Không tìm thấy " + ten + " mã: " + id);
        }

        return e;
    }

    private static void rollback(EntityTransaction tx) {
        if (tx.isActive()) {
            tx.rollback();
        }
    }

    private static String cat(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() > max ? s.substring(0, max) : s;
    }

    // Đọc [TongSoLuong, SoLuongKhaDung] mới nhất từ DB (sau UPDATE nguyên tử)
    private static int[] docBoDem(EntityManager em, Integer maDauSach) {
        Object[] r = em.createQuery(
                "SELECT COALESCE(d.tongSoLuong, 0), COALESCE(d.soLuongKhaDung, 0) "
                        + "FROM DauSach d WHERE d.maDauSach = :id",
                Object[].class)
                .setParameter("id", maDauSach)
                .getSingleResult();

        return new int[] {
                ((Number) r[0]).intValue(),
                ((Number) r[1]).intValue()
        };
    }

    // ----- Thêm đầu sách -----
    public DauSach themDauSach(
            String tenSach,
            double giaTien,
            Integer maTG,
            Integer maNXB,
            Integer maTL,
            String viTri) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            DauSach ds = new DauSach();
            ds.setTenSach(tenSach);
            ds.setGiaTien(giaTien);
            ds.setViTri(viTri);
            ds.setTongSoLuong(0);
            ds.setSoLuongKhaDung(0);

            if (maTG != null) {
                ds.setTacGia(timHoacLoi(em, TacGia.class, maTG, "tác giả"));
            }
            if (maNXB != null) {
                ds.setNhaXuatBan(timHoacLoi(em, NhaXuatBan.class, maNXB, "nhà xuất bản"));
            }
            if (maTL != null) {
                ds.setTheLoai(timHoacLoi(em, TheLoai.class, maTL, "thể loại"));
            }

            em.persist(ds);

            tx.commit();

            return ds;

        } catch (NghiepVuException e) {
            rollback(tx);
            throw e;

        } catch (Exception e) {
            rollback(tx);
            e.printStackTrace();
            throw new NghiepVuException("Không thể thêm đầu sách do lỗi hệ thống!");

        } finally {
            em.close();
        }
    }

    // ----- Sửa đầu sách: tham số null = giữ nguyên -----
    public void capNhatDauSach(
            Integer maDauSach,
            String tenSach,
            Double giaTien,
            Integer maTG,
            Integer maNXB,
            Integer maTL,
            String viTri) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            DauSach ds = timHoacLoi(em, DauSach.class, maDauSach, "đầu sách");

            if (tenSach != null) {
                ds.setTenSach(tenSach);
            }
            if (giaTien != null) {
                ds.setGiaTien(giaTien);
            }
            if (viTri != null) {
                ds.setViTri(viTri);
            }
            if (maTG != null) {
                ds.setTacGia(timHoacLoi(em, TacGia.class, maTG, "tác giả"));
            }
            if (maNXB != null) {
                ds.setNhaXuatBan(timHoacLoi(em, NhaXuatBan.class, maNXB, "nhà xuất bản"));
            }
            if (maTL != null) {
                ds.setTheLoai(timHoacLoi(em, TheLoai.class, maTL, "thể loại"));
            }

            tx.commit();

        } catch (NghiepVuException e) {
            rollback(tx);
            throw e;

        } catch (Exception e) {
            rollback(tx);
            e.printStackTrace();
            throw new NghiepVuException("Không thể cập nhật đầu sách do lỗi hệ thống!");

        } finally {
            em.close();
        }
    }

    // ----- Xóa đầu sách: chỉ khi chưa từng có giao dịch -----
    public void xoaDauSach(Integer maDauSach) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            DauSach ds = timHoacLoi(em, DauSach.class, maDauSach, "đầu sách");

            Long dangMuon = em.createQuery(
                    "SELECT COUNT(ct) FROM ChiTietPhieuMuon ct "
                            + "WHERE ct.cuonSach.dauSach.maDauSach = :id "
                            + "AND ct.ngayTraThucTe IS NULL",
                    Long.class)
                    .setParameter("id", maDauSach)
                    .getSingleResult();

            Long datTruocHieuLuc = em.createQuery(
                    "SELECT COUNT(d) FROM DatTruoc d "
                            + "WHERE d.dauSach.maDauSach = :id "
                            + "AND d.trangThai IN (:tts)",
                    Long.class)
                    .setParameter("id", maDauSach)
                    .setParameter(
                            "tts",
                            List.of(
                                    TrangThai.DATTRUOC_WAITING,
                                    TrangThai.DATTRUOC_RESERVED,
                                    TrangThai.DATTRUOC_NOTIFIED))
                    .getSingleResult();

            if (dangMuon > 0 || datTruocHieuLuc > 0) {
                throw new NghiepVuException(
                        "Không thể xóa tài liệu/danh mục đang trong giao dịch mượn/trả chưa kết thúc");
            }

            Long tongMuon = em.createQuery(
                    "SELECT COUNT(ct) FROM ChiTietPhieuMuon ct "
                            + "WHERE ct.cuonSach.dauSach.maDauSach = :id",
                    Long.class)
                    .setParameter("id", maDauSach)
                    .getSingleResult();

            Long tongDatTruoc = em.createQuery(
                    "SELECT COUNT(d) FROM DatTruoc d WHERE d.dauSach.maDauSach = :id",
                    Long.class)
                    .setParameter("id", maDauSach)
                    .getSingleResult();

            if (tongMuon > 0 || tongDatTruoc > 0) {
                throw new NghiepVuException(
                        "Đầu sách đã phát sinh giao dịch nên không thể xóa. "
                                + "Hãy cập nhật các cuốn sang tình trạng thanh lý.");
            }

            em.createQuery("DELETE FROM CuonSach c WHERE c.dauSach.maDauSach = :id")
                    .setParameter("id", maDauSach)
                    .executeUpdate();

            em.remove(ds);

            tx.commit();

        } catch (NghiepVuException e) {
            rollback(tx);
            throw e;

        } catch (Exception e) {
            rollback(tx);
            e.printStackTrace();
            throw new NghiepVuException("Không thể xóa đầu sách do lỗi hệ thống!");

        } finally {
            em.close();
        }
    }

    // ----- Nhập cuốn sách vật lý ("tất cả hoặc không") -----
    public static class KetQuaThemCuon {
        public int soCuonDaThem;
        public int tongSoLuong;
        public int soLuongKhaDung;
    }

    public KetQuaThemCuon themCuonSach(
            Integer maDauSach,
            List<String> maCuonSachList,
            String tinhTrangVatLy) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            DauSach ds = timHoacLoi(em, DauSach.class, maDauSach, "đầu sách");

            Set<String> daThem = new HashSet<>();

            for (String raw : maCuonSachList) {
                String ma = raw == null ? "" : raw.trim();

                if (ma.isEmpty() || ma.length() > 50) {
                    throw new BadRequestException(
                            "Thiếu hoặc sai tham số: maCuonSachList");
                }

                // Trùng trong cùng danh sách hoặc đã có trong kho
                if (!daThem.add(ma) || em.find(CuonSach.class, ma) != null) {
                    throw new NghiepVuException("Mã cuốn sách đã tồn tại: " + ma);
                }

                CuonSach cs = new CuonSach();
                cs.setMaCuonSach(ma);
                cs.setDauSach(ds);
                cs.setTinhTrangVatLy(cat(tinhTrangVatLy, 100));
                cs.setTrangThai(TrangThai.CUON_AVAILABLE);

                em.persist(cs);
            }

            PhieuMuonRepository.capNhatSoLuong(
                    em, maDauSach, daThem.size(), daThem.size());

            int[] boDem = docBoDem(em, maDauSach);

            tx.commit();

            KetQuaThemCuon kq = new KetQuaThemCuon();
            kq.soCuonDaThem = daThem.size();
            kq.tongSoLuong = boDem[0];
            kq.soLuongKhaDung = boDem[1];

            return kq;

        } catch (NghiepVuException | BadRequestException e) {
            rollback(tx);
            throw e;

        } catch (Exception e) {
            rollback(tx);
            e.printStackTrace();
            throw new NghiepVuException("Không thể nhập cuốn sách do lỗi hệ thống!");

        } finally {
            em.close();
        }
    }

    // ----- Cập nhật tình trạng cuốn sách vật lý -----
    public static class KetQuaCuon {
        public String maCuonSach;
        public String tinhTrangVatLy;
        public String trangThai;
        public int tongSoLuong;
        public int soLuongKhaDung;
    }

    // trangThaiMoi (tùy chọn): AVAILABLE | DAMAGED | LOST, chỉ đổi được khi cuốn
    // không ở BORROWED / RESERVED. tinhTrangVatLy (tùy chọn) đổi được bất cứ lúc
    // nào.
    public KetQuaCuon capNhatCuonSach(
            String maCuonSach,
            String tinhTrangVatLy,
            String trangThaiMoi) {
        if (trangThaiMoi != null
                && !TrangThai.CUON_AVAILABLE.equals(trangThaiMoi)
                && !TrangThai.CUON_DAMAGED.equals(trangThaiMoi)
                && !TrangThai.CUON_LOST.equals(trangThaiMoi)) {

            throw new BadRequestException("Thiếu hoặc sai tham số: trangThai");
        }

        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            CuonSach cs = em.find(
                    CuonSach.class,
                    maCuonSach,
                    LockModeType.PESSIMISTIC_WRITE);

            if (cs == null) {
                throw new NghiepVuException(
                        "Không tìm thấy cuốn sách mã: " + maCuonSach);
            }

            Integer maDauSach = cs.getDauSach().getMaDauSach();

            if (trangThaiMoi != null) {
                String cu = cs.getTrangThai();

                if (TrangThai.CUON_BORROWED.equals(cu)
                        || TrangThai.CUON_RESERVED.equals(cu)) {

                    throw new NghiepVuException(
                            "Cuốn sách đang được mượn/đặt trước, không thể đổi trạng thái");
                }

                // Tổng = số cuốn khác LOST; Khả dụng = số cuốn AVAILABLE
                int dTong = (TrangThai.CUON_LOST.equals(trangThaiMoi) ? 0 : 1)
                        - (TrangThai.CUON_LOST.equals(cu) ? 0 : 1);

                int dKhaDung = (TrangThai.CUON_AVAILABLE.equals(trangThaiMoi) ? 1 : 0)
                        - (TrangThai.CUON_AVAILABLE.equals(cu) ? 1 : 0);

                if (dTong != 0 || dKhaDung != 0) {
                    PhieuMuonRepository.capNhatSoLuong(em, maDauSach, dKhaDung, dTong);
                }

                cs.setTrangThai(trangThaiMoi);
            }

            if (tinhTrangVatLy != null) {
                cs.setTinhTrangVatLy(cat(tinhTrangVatLy, 100));
            }

            int[] boDem = docBoDem(em, maDauSach);

            tx.commit();

            KetQuaCuon kq = new KetQuaCuon();
            kq.maCuonSach = cs.getMaCuonSach();
            kq.tinhTrangVatLy = cs.getTinhTrangVatLy();
            kq.trangThai = cs.getTrangThai();
            kq.tongSoLuong = boDem[0];
            kq.soLuongKhaDung = boDem[1];

            return kq;

        } catch (NghiepVuException | BadRequestException e) {
            rollback(tx);
            throw e;

        } catch (Exception e) {
            rollback(tx);
            e.printStackTrace();
            throw new NghiepVuException("Không thể cập nhật cuốn sách do lỗi hệ thống!");

        } finally {
            em.close();
        }
    }
}
