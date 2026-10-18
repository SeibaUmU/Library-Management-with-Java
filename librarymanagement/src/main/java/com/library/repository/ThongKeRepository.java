package com.library.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.library.common.BadRequestException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class ThongKeRepository {

    // ===== Hỗ trợ dùng chung =====

    private static void locNgay(
            StringBuilder jpql,
            Map<String, Object> p,
            String field,
            LocalDate tuNgay,
            LocalDate denNgay) {
        if (tuNgay != null) {
            jpql.append(" AND ").append(field).append(" >= :tu");
            p.put("tu", tuNgay.atStartOfDay());
        }
        if (denNgay != null) {
            jpql.append(" AND ").append(field).append(" < :den");
            p.put("den", denNgay.plusDays(1).atStartOfDay());
        }
    }

    private static List<Object[]> chay(
            EntityManager em,
            String jpql,
            Map<String, Object> params) {
        TypedQuery<Object[]> q = em.createQuery(jpql, Object[].class);
        for (Map.Entry<String, Object> e : params.entrySet()) {
            q.setParameter(e.getKey(), e.getValue());
        }
        return q.getResultList();
    }

    // groupBy: MONTH | QUARTER | YEAR
    private static String bieuThucKy(String field, String groupBy) {
        switch (groupBy) {
            case "YEAR":
                return "year(" + field + ")";
            case "QUARTER":
                return "year(" + field + "), quarter(" + field + ")";
            case "MONTH":
                return "year(" + field + "), month(" + field + ")";
            default:
                throw new BadRequestException("Thiếu hoặc sai tham số: groupBy");
        }
    }

    private static int soCotKy(String groupBy) {
        return "YEAR".equals(groupBy) ? 1 : 2;
    }

    // Hàng kết quả bắt đầu bằng [năm] hoặc [năm, quý/tháng]
    private static String nhanKy(Object[] r, String groupBy) {
        int nam = ((Number) r[0]).intValue();

        if ("YEAR".equals(groupBy)) {
            return String.valueOf(nam);
        }

        int so = ((Number) r[1]).intValue();

        if ("QUARTER".equals(groupBy)) {
            return nam + "-Q" + so;
        }

        return String.format("%d-%02d", nam, so);
    }

    // ===== TOP_BOOKS: [maDauSach, tenSach, soLuotMuon(long)] =====
    public List<Object[]> getTopSach(
            LocalDate tuNgay,
            LocalDate denNgay,
            int limit) {
        Map<String, Object> params = new HashMap<>();

        StringBuilder jpql = new StringBuilder(
                "SELECT ds.maDauSach, ds.tenSach, COUNT(ct) "
                        + "FROM ChiTietPhieuMuon ct "
                        + "JOIN ct.phieuMuon pm "
                        + "JOIN ct.cuonSach cs "
                        + "JOIN cs.dauSach ds "
                        + "WHERE 1=1");

        locNgay(jpql, params, "pm.ngayMuon", tuNgay, denNgay);

        jpql.append(" GROUP BY ds.maDauSach, ds.tenSach ORDER BY COUNT(ct) DESC");

        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            TypedQuery<Object[]> q = em.createQuery(jpql.toString(), Object[].class);

            for (Map.Entry<String, Object> e : params.entrySet()) {
                q.setParameter(e.getKey(), e.getValue());
            }

            q.setMaxResults(limit);

            return q.getResultList();
        }
    }

    // ===== BORROW_STATS: [ky, soLuotMuon(long), soLuotTra(long)] theo kỳ tăng dần
    // =====
    public List<Object[]> thongKeMuonTra(
            LocalDate tuNgay,
            LocalDate denNgay,
            String groupBy) {
        Map<String, long[]> theoKy = new TreeMap<>();
        int cotDem = soCotKy(groupBy);

        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            // Lượt mượn: kỳ tính theo NgayMuon
            Map<String, Object> pMuon = new HashMap<>();
            String kyMuon = bieuThucKy("pm.ngayMuon", groupBy);

            StringBuilder jMuon = new StringBuilder(
                    "SELECT " + kyMuon + ", COUNT(ct) "
                            + "FROM ChiTietPhieuMuon ct "
                            + "JOIN ct.phieuMuon pm WHERE 1=1");
            locNgay(jMuon, pMuon, "pm.ngayMuon", tuNgay, denNgay);
            jMuon.append(" GROUP BY ").append(kyMuon);

            for (Object[] r : chay(em, jMuon.toString(), pMuon)) {
                theoKy.computeIfAbsent(nhanKy(r, groupBy), k -> new long[2])[0] += ((Number) r[cotDem]).longValue();
            }

            // Lượt trả: kỳ tính theo NgayTraThucTe
            Map<String, Object> pTra = new HashMap<>();
            String kyTra = bieuThucKy("ct.ngayTraThucTe", groupBy);

            StringBuilder jTra = new StringBuilder(
                    "SELECT " + kyTra + ", COUNT(ct) "
                            + "FROM ChiTietPhieuMuon ct "
                            + "WHERE ct.ngayTraThucTe IS NOT NULL");
            locNgay(jTra, pTra, "ct.ngayTraThucTe", tuNgay, denNgay);
            jTra.append(" GROUP BY ").append(kyTra);

            for (Object[] r : chay(em, jTra.toString(), pTra)) {
                theoKy.computeIfAbsent(nhanKy(r, groupBy), k -> new long[2])[1] += ((Number) r[cotDem]).longValue();
            }
        }

        List<Object[]> ketQua = new ArrayList<>();

        for (Map.Entry<String, long[]> e : theoKy.entrySet()) {
            ketQua.add(new Object[] {
                    e.getKey(), e.getValue()[0], e.getValue()[1]
            });
        }

        return ketQua;
    }

    // ===== OVERDUE_READERS:
    // [maThe, hoTen, email, soDienThoai, soSachQuaHan(long),
    // tienPhatChuaNop(double)] =====
    public List<Object[]> getDocGiaTreHanHoacNoPhat() {
        Map<Integer, Object[]> theoThe = new TreeMap<>();

        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            List<Object[]> treHan = em.createQuery(
                    "SELECT t.maThe, n.hoTen, n.email, n.soDienThoai, COUNT(ct) "
                            + "FROM ChiTietPhieuMuon ct "
                            + "JOIN ct.phieuMuon pm "
                            + "JOIN pm.theDocGia t "
                            + "JOIN t.nguoiDung n "
                            + "WHERE ct.ngayTraThucTe IS NULL AND ct.ngayHenTra < :today "
                            + "GROUP BY t.maThe, n.hoTen, n.email, n.soDienThoai",
                    Object[].class)
                    .setParameter("today", LocalDate.now())
                    .getResultList();

            for (Object[] r : treHan) {
                theoThe.put(
                        ((Number) r[0]).intValue(),
                        new Object[] {
                                r[0], r[1], r[2], r[3],
                                ((Number) r[4]).longValue(), 0.0
                        });
            }

            List<Object[]> noPhat = em.createQuery(
                    "SELECT t.maThe, n.hoTen, n.email, n.soDienThoai, SUM(pp.soTienPhat) "
                            + "FROM PhieuPhat pp "
                            + "JOIN pp.chiTietPhieuMuon ct "
                            + "JOIN ct.phieuMuon pm "
                            + "JOIN pm.theDocGia t "
                            + "JOIN t.nguoiDung n "
                            + "WHERE pp.trangThaiThanhToan = false "
                            + "GROUP BY t.maThe, n.hoTen, n.email, n.soDienThoai",
                    Object[].class).getResultList();

            for (Object[] r : noPhat) {
                int maThe = ((Number) r[0]).intValue();
                double tien = r[4] != null ? ((Number) r[4]).doubleValue() : 0.0;

                Object[] dong = theoThe.get(maThe);

                if (dong == null) {
                    dong = new Object[] { r[0], r[1], r[2], r[3], 0L, 0.0 };
                    theoThe.put(maThe, dong);
                }

                dong[5] = tien;
            }
        }

        return new ArrayList<>(theoThe.values());
    }

    // ===== FINE_REVENUE (tổng hợp): [tongSoPhieu(long), tongTienPhat(double),
    // daThu(double)] =====
    public Object[] tongHopDoanhThuPhat(LocalDate tuNgay, LocalDate denNgay) {
        Map<String, Object> params = new HashMap<>();

        StringBuilder jpql = new StringBuilder(
                "SELECT COUNT(pp), COALESCE(SUM(pp.soTienPhat), 0.0), "
                        + "COALESCE(SUM(CASE WHEN pp.trangThaiThanhToan = true "
                        + "THEN pp.soTienPhat ELSE 0.0 END), 0.0) "
                        + "FROM PhieuPhat pp WHERE 1=1");

        locNgay(jpql, params, "pp.ngayLapPhieu", tuNgay, denNgay);

        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            Object[] r = chay(em, jpql.toString(), params).get(0);

            return new Object[] {
                    ((Number) r[0]).longValue(),
                    ((Number) r[1]).doubleValue(),
                    ((Number) r[2]).doubleValue()
            };
        }
    }

    // ===== FINE_REVENUE (theo kỳ): [ky, soPhieu(long), tongTienPhat(double)] =====
    public List<Object[]> doanhThuPhatTheoKy(
            LocalDate tuNgay,
            LocalDate denNgay,
            String groupBy) {
        Map<String, Object> params = new HashMap<>();
        String ky = bieuThucKy("pp.ngayLapPhieu", groupBy);
        int cot = soCotKy(groupBy);

        StringBuilder jpql = new StringBuilder(
                "SELECT " + ky + ", COUNT(pp), COALESCE(SUM(pp.soTienPhat), 0.0) "
                        + "FROM PhieuPhat pp WHERE 1=1");

        locNgay(jpql, params, "pp.ngayLapPhieu", tuNgay, denNgay);
        jpql.append(" GROUP BY ").append(ky);

        Map<String, Object[]> theoKy = new TreeMap<>();

        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            for (Object[] r : chay(em, jpql.toString(), params)) {
                theoKy.put(
                        nhanKy(r, groupBy),
                        new Object[] {
                                nhanKy(r, groupBy),
                                ((Number) r[cot]).longValue(),
                                ((Number) r[cot + 1]).doubleValue()
                        });
            }
        }

        return new ArrayList<>(theoKy.values());
    }
}