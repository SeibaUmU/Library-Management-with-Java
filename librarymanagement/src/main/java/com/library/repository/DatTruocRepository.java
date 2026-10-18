package com.library.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.library.common.NghiepVuException;
import com.library.common.TrangThai;
import com.library.entities.ChiTietPhieuMuon;
import com.library.entities.CuonSach;
import com.library.entities.DatTruoc;
import com.library.entities.DauSach;
import com.library.entities.TheDocGia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

public class DatTruocRepository {

        /*
         * public boolean taoDatTruoc(DatTruoc dt) {
         * EntityManager em = EntityManagerUtil.getEntityManager();
         * EntityTransaction tx = em.getTransaction();
         * 
         * try {
         * tx.begin();
         * 
         * em.persist(dt);
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
         * return false;
         * 
         * } finally {
         * em.close();
         * }
         * }
         */

        private static final List<String> DANG_HIEU_LUC = List.of(
                        TrangThai.DATTRUOC_WAITING,
                        TrangThai.DATTRUOC_RESERVED,
                        TrangThai.DATTRUOC_NOTIFIED);

        // Danh sách chờ (WAITING) của một đầu sách, cũ nhất trước (thay hàm cũ)
        public List<DatTruoc> getDanhSachChoByMaDauSach(Integer maDauSach) {
                return getByDauSachVaTrangThai(maDauSach, TrangThai.DATTRUOC_WAITING);
        }

        // GET_WAITING_RESERVATIONS: maDauSach = null thì lấy mọi đầu sách
        public List<DatTruoc> getByDauSachVaTrangThai(
                        Integer maDauSach,
                        String trangThai) {
                try (EntityManager em = EntityManagerUtil.getEntityManager()) {

                        StringBuilder jpql = new StringBuilder(
                                        "SELECT d FROM DatTruoc d "
                                                        + "JOIN FETCH d.theDocGia t "
                                                        + "JOIN FETCH t.nguoiDung "
                                                        + "JOIN FETCH d.dauSach ds "
                                                        + "LEFT JOIN FETCH d.cuonSach "
                                                        + "WHERE d.trangThai = :tt");

                        if (maDauSach != null) {
                                jpql.append(" AND ds.maDauSach = :maDS");
                        }

                        jpql.append(" ORDER BY d.ngayDat ASC, d.maDatTruoc ASC");

                        TypedQuery<DatTruoc> query = em.createQuery(jpql.toString(), DatTruoc.class);

                        query.setParameter("tt", trangThai);

                        if (maDauSach != null) {
                                query.setParameter("maDS", maDauSach);
                        }

                        return query.getResultList();
                }
        }

        // CANCEL_RESERVATION / ASSIGN_RESERVATION
        public DatTruoc findByIdFetch(Integer maDatTruoc) {
                try (EntityManager em = EntityManagerUtil.getEntityManager()) {

                        List<DatTruoc> ds = em.createQuery(
                                        "SELECT d FROM DatTruoc d "
                                                        + "JOIN FETCH d.theDocGia t "
                                                        + "JOIN FETCH t.nguoiDung "
                                                        + "JOIN FETCH d.dauSach "
                                                        + "LEFT JOIN FETCH d.cuonSach "
                                                        + "WHERE d.maDatTruoc = :id",
                                        DatTruoc.class)
                                        .setParameter("id", maDatTruoc)
                                        .getResultList();

                        return ds.isEmpty() ? null : ds.get(0);
                }
        }

        // GET_MY_RESERVATIONS: trangThai = null thì lấy WAITING / RESERVED / NOTIFIED
        public List<DatTruoc> getByThe(Integer maThe, String trangThai) {
                List<String> tts = trangThai != null ? List.of(trangThai) : DANG_HIEU_LUC;

                try (EntityManager em = EntityManagerUtil.getEntityManager()) {

                        return em.createQuery(
                                        "SELECT d FROM DatTruoc d "
                                                        + "JOIN FETCH d.dauSach "
                                                        + "LEFT JOIN FETCH d.cuonSach "
                                                        + "WHERE d.theDocGia.maThe = :maThe "
                                                        + "AND d.trangThai IN (:tts) "
                                                        + "ORDER BY d.ngayDat DESC",
                                        DatTruoc.class)
                                        .setParameter("maThe", maThe)
                                        .setParameter("tts", tts)
                                        .getResultList();
                }
        }

        // RESERVE_BOOK: độc giả đã có lượt đặt còn hiệu lực cho đầu sách này chưa?
        public boolean existsHieuLuc(Integer maThe, Integer maDauSach) {
                try (EntityManager em = EntityManagerUtil.getEntityManager()) {

                        Long n = em.createQuery(
                                        "SELECT COUNT(d) FROM DatTruoc d "
                                                        + "WHERE d.theDocGia.maThe = :maThe "
                                                        + "AND d.dauSach.maDauSach = :maDS "
                                                        + "AND d.trangThai IN (:tts)",
                                        Long.class)
                                        .setParameter("maThe", maThe)
                                        .setParameter("maDS", maDauSach)
                                        .setParameter("tts", DANG_HIEU_LUC)
                                        .getSingleResult();

                        return n > 0;
                }
        }

        // UC-01 quy tắc 5b: số lượt WAITING của NGƯỜI KHÁC trên đầu sách
        public long countWaitingCuaNguoiKhac(Integer maDauSach, Integer maThe) {
                try (EntityManager em = EntityManagerUtil.getEntityManager()) {

                        return em.createQuery(
                                        "SELECT COUNT(d) FROM DatTruoc d "
                                                        + "WHERE d.dauSach.maDauSach = :maDS "
                                                        + "AND d.theDocGia.maThe <> :maThe "
                                                        + "AND d.trangThai = :tt",
                                        Long.class)
                                        .setParameter("maDS", maDauSach)
                                        .setParameter("maThe", maThe)
                                        .setParameter("tt", TrangThai.DATTRUOC_WAITING)
                                        .getSingleResult();
                }
        }

        // UC-09 điều kiện 3c: đầu sách có người đang chờ (WAITING / RESERVED)
        public long countChoXuLyByDauSach(Integer maDauSach) {
                try (EntityManager em = EntityManagerUtil.getEntityManager()) {

                        return em.createQuery(
                                        "SELECT COUNT(d) FROM DatTruoc d "
                                                        + "WHERE d.dauSach.maDauSach = :maDS "
                                                        + "AND d.trangThai IN (:tts)",
                                        Long.class)
                                        .setParameter("maDS", maDauSach)
                                        .setParameter(
                                                        "tts",
                                                        List.of(
                                                                        TrangThai.DATTRUOC_WAITING,
                                                                        TrangThai.DATTRUOC_RESERVED))
                                        .getSingleResult();
                }
        }

        // Vị trí trong hàng chờ (1 = đầu hàng)
        // Vị trí trong hàng chờ: 1 = đầu hàng
        public long getViTriHangCho(
                        Integer maDatTruoc,
                        Integer maDauSach,
                        LocalDateTime ngayDat) {
                try (EntityManager em = EntityManagerUtil.getEntityManager()) {

                        Long truoc = em.createQuery(
                                        "SELECT COUNT(d) FROM DatTruoc d "
                                                        + "WHERE d.dauSach.maDauSach = :maDS "
                                                        + "AND d.trangThai = :tt "
                                                        + "AND (d.ngayDat < :ngay "
                                                        + "OR (d.ngayDat = :ngay AND d.maDatTruoc < :id))",
                                        Long.class)
                                        .setParameter("maDS", maDauSach)
                                        .setParameter("tt", TrangThai.DATTRUOC_WAITING)
                                        .setParameter("ngay", ngayDat)
                                        .setParameter("id", maDatTruoc)
                                        .getSingleResult();

                        return truoc + 1;
                }
        }

        // ===== Tạo lượt đặt trước (RESERVE_BOOK) =====
        public DatTruoc taoLuotDat(Integer maThe, Integer maDauSach) {
                EntityManager em = EntityManagerUtil.getEntityManager();
                EntityTransaction tx = em.getTransaction();

                try {
                        tx.begin();

                        DatTruoc dt = new DatTruoc();
                        dt.setTheDocGia(em.getReference(TheDocGia.class, maThe));
                        dt.setDauSach(em.getReference(DauSach.class, maDauSach));
                        // Cắt về giây để vị trí hàng chờ ổn định
                        dt.setNgayDat(LocalDateTime.now().withNano(0));
                        dt.setTrangThai(TrangThai.DATTRUOC_WAITING);

                        em.persist(dt);

                        tx.commit();

                        return dt;

                } catch (Exception e) {
                        if (tx.isActive()) {
                                tx.rollback();
                        }
                        e.printStackTrace();
                        throw new NghiepVuException("Không thể đặt trước do lỗi hệ thống!");

                } finally {
                        em.close();
                }
        }

        // ===== Gợi ý cuốn để giữ chỗ (GET_WAITING_RESERVATIONS) =====
        public static class CuonGoiY {
                public String maCuonSach;
                public LocalDate ngayHenTra;
        }

        // Với mỗi đầu sách: cuốn đang mượn có hạn trả gần nhất
        public Map<Integer, CuonGoiY> getCuonGoiY(Collection<Integer> maDauSachList) {
                Map<Integer, CuonGoiY> ketQua = new HashMap<>();

                if (maDauSachList == null || maDauSachList.isEmpty()) {
                        return ketQua;
                }

                try (EntityManager em = EntityManagerUtil.getEntityManager()) {

                        List<Object[]> rows = em.createQuery(
                                        "SELECT cs.dauSach.maDauSach, cs.maCuonSach, ct.ngayHenTra "
                                                        + "FROM ChiTietPhieuMuon ct JOIN ct.cuonSach cs "
                                                        + "WHERE ct.ngayTraThucTe IS NULL "
                                                        + "AND cs.trangThai = :tt "
                                                        + "AND cs.dauSach.maDauSach IN (:ids) "
                                                        + "ORDER BY ct.ngayHenTra ASC, ct.maCTPM ASC",
                                        Object[].class)
                                        .setParameter("tt", TrangThai.CUON_BORROWED)
                                        .setParameter("ids", maDauSachList)
                                        .getResultList();

                        for (Object[] r : rows) {
                                Integer maDS = ((Number) r[0]).intValue();

                                if (!ketQua.containsKey(maDS)) {
                                        CuonGoiY g = new CuonGoiY();
                                        g.maCuonSach = (String) r[1];
                                        g.ngayHenTra = (LocalDate) r[2];
                                        ketQua.put(maDS, g);
                                }
                        }
                }

                return ketQua;
        }

        // ===== Hủy lượt đặt (CANCEL_RESERVATION, và hủy do quá hạn giữ sách) =====

        // Cuốn đang giữ cho lượt vừa bị hủy: chuyển cho lượt WAITING kế tiếp,
        // nếu không còn ai thì trả về trạng thái trước đó.
        // - cuonDaVeQuay = true : cuốn đang nằm ở quầy (lượt NOTIFIED)
        // - cuonDaVeQuay = false: cuốn vẫn đang được mượn (lượt RESERVED)
        // Trả về lượt vừa được chuyển sang NOTIFIED (cần gửi thông báo), hoặc null.
        private static DatTruoc chuyenChoNguoiKe(
                        EntityManager em,
                        CuonSach cs,
                        Integer maDauSach,
                        boolean cuonDaVeQuay,
                        LocalDateTime now) {
                List<DatTruoc> ke = em.createQuery(
                                "SELECT d FROM DatTruoc d "
                                                + "WHERE d.dauSach.maDauSach = :maDS AND d.trangThai = :tt "
                                                + "ORDER BY d.ngayDat ASC, d.maDatTruoc ASC",
                                DatTruoc.class)
                                .setParameter("maDS", maDauSach)
                                .setParameter("tt", TrangThai.DATTRUOC_WAITING)
                                .setMaxResults(1)
                                .getResultList();

                if (!ke.isEmpty()) {
                        DatTruoc tiep = ke.get(0);
                        tiep.setCuonSach(cs);

                        if (cuonDaVeQuay) {
                                tiep.setTrangThai(TrangThai.DATTRUOC_NOTIFIED);
                                tiep.setNgayThongBao(now);
                                return tiep;
                        }

                        tiep.setTrangThai(TrangThai.DATTRUOC_RESERVED);
                        return null;
                }

                if (cuonDaVeQuay) {
                        cs.setTrangThai(TrangThai.CUON_AVAILABLE);
                        PhieuMuonRepository.capNhatSoLuong(em, maDauSach, 1, 0);
                } else {
                        cs.setTrangThai(TrangThai.CUON_BORROWED);
                }

                return null;
        }

        // maTheChu != null: độc giả chỉ được hủy lượt của chính mình (null = thủ thư /
        // hệ thống).
        // Trả về mã lượt đặt kế tiếp vừa được thông báo (nếu có) để gửi Email.
        public Integer huyLuotDat(Integer maDatTruoc, Integer maTheChu) {
                EntityManager em = EntityManagerUtil.getEntityManager();
                EntityTransaction tx = em.getTransaction();

                try {
                        tx.begin();

                        DatTruoc dt = em.find(
                                        DatTruoc.class,
                                        maDatTruoc,
                                        LockModeType.PESSIMISTIC_WRITE);

                        if (dt == null) {
                                throw new NghiepVuException(
                                                "Không tìm thấy lượt đặt trước mã: " + maDatTruoc);
                        }

                        if (maTheChu != null
                                        && !maTheChu.equals(dt.getTheDocGia().getMaThe())) {

                                throw new NghiepVuException(
                                                "FORBIDDEN",
                                                "Bạn không có quyền hủy lượt đặt trước này");
                        }

                        String tt = dt.getTrangThai();

                        if (TrangThai.DATTRUOC_COMPLETED.equals(tt)
                                        || TrangThai.DATTRUOC_CANCELLED.equals(tt)) {

                                throw new NghiepVuException("Lượt đặt trước đã kết thúc");
                        }

                        Integer thongBaoKeTiep = null;

                        if ((TrangThai.DATTRUOC_RESERVED.equals(tt)
                                        || TrangThai.DATTRUOC_NOTIFIED.equals(tt))
                                        && dt.getCuonSach() != null) {

                                CuonSach cs = em.find(
                                                CuonSach.class,
                                                dt.getCuonSach().getMaCuonSach(),
                                                LockModeType.PESSIMISTIC_WRITE);

                                DatTruoc tiep = chuyenChoNguoiKe(
                                                em,
                                                cs,
                                                dt.getDauSach().getMaDauSach(),
                                                TrangThai.DATTRUOC_NOTIFIED.equals(tt),
                                                LocalDateTime.now());

                                if (tiep != null) {
                                        thongBaoKeTiep = tiep.getMaDatTruoc();
                                }
                        }

                        dt.setTrangThai(TrangThai.DATTRUOC_CANCELLED);

                        tx.commit();

                        return thongBaoKeTiep;

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
                        throw new NghiepVuException("Không thể hủy đặt trước do lỗi hệ thống!");

                } finally {
                        em.close();
                }
        }

        // ===== Thủ thư gán cuốn cho lượt chờ (ASSIGN_RESERVATION) =====

        public static class KetQuaGan {
                public Integer maDatTruoc;
                public String trangThai; // RESERVED hoặc NOTIFIED
                public String maCuonSach;
                public LocalDate ngayHenTraCuaCuon; // null khi gán thẳng cuốn đã sẵn sàng
                public boolean daThongBao; // true: cần gửi thông báo (NOTIFIED)
        }

        private static CuonSach timCuonSanSang(
                        EntityManager em,
                        Integer maDauSach,
                        String maCuonChon) {
                String jpql = "SELECT c FROM CuonSach c "
                                + "WHERE c.dauSach.maDauSach = :maDS AND c.trangThai = :tt"
                                + (maCuonChon != null ? " AND c.maCuonSach = :ma" : "")
                                + " ORDER BY c.maCuonSach";

                TypedQuery<CuonSach> q = em.createQuery(jpql, CuonSach.class)
                                .setParameter("maDS", maDauSach)
                                .setParameter("tt", TrangThai.CUON_AVAILABLE);

                if (maCuonChon != null) {
                        q.setParameter("ma", maCuonChon);
                }

                List<CuonSach> ds = q.setMaxResults(1).getResultList();

                return ds.isEmpty() ? null : ds.get(0);
        }

        // Dòng mượn (chưa trả) của cuốn BORROWED có hạn trả gần nhất
        private static ChiTietPhieuMuon timDongMuonDeGiuCho(
                        EntityManager em,
                        Integer maDauSach,
                        String maCuonChon) {
                String jpql = "SELECT ct FROM ChiTietPhieuMuon ct "
                                + "JOIN FETCH ct.phieuMuon pm "
                                + "JOIN ct.cuonSach cs "
                                + "WHERE ct.ngayTraThucTe IS NULL "
                                + "AND cs.dauSach.maDauSach = :maDS "
                                + "AND cs.trangThai = :tt"
                                + (maCuonChon != null ? " AND cs.maCuonSach = :ma" : "")
                                + " ORDER BY ct.ngayHenTra ASC, ct.maCTPM ASC";

                TypedQuery<ChiTietPhieuMuon> q = em.createQuery(jpql, ChiTietPhieuMuon.class)
                                .setParameter("maDS", maDauSach)
                                .setParameter("tt", TrangThai.CUON_BORROWED);

                if (maCuonChon != null) {
                        q.setParameter("ma", maCuonChon);
                }

                List<ChiTietPhieuMuon> ds = q.setMaxResults(1).getResultList();

                return ds.isEmpty() ? null : ds.get(0);
        }

        public KetQuaGan ganCuonChoLuotDat(Integer maDatTruoc, String maCuonChon) {
                EntityManager em = EntityManagerUtil.getEntityManager();
                EntityTransaction tx = em.getTransaction();

                try {
                        tx.begin();

                        DatTruoc dt = em.find(
                                        DatTruoc.class,
                                        maDatTruoc,
                                        LockModeType.PESSIMISTIC_WRITE);

                        if (dt == null) {
                                throw new NghiepVuException(
                                                "Không tìm thấy lượt đặt trước mã: " + maDatTruoc);
                        }

                        if (!TrangThai.DATTRUOC_WAITING.equals(dt.getTrangThai())) {
                                throw new NghiepVuException(
                                                "Lượt đặt trước không ở trạng thái WAITING");
                        }

                        Integer maDauSach = dt.getDauSach().getMaDauSach();

                        // FIFO: không được bỏ qua lượt WAITING cũ hơn của cùng đầu sách
                        Long truoc = em.createQuery(
                                        "SELECT COUNT(d) FROM DatTruoc d "
                                                        + "WHERE d.dauSach.maDauSach = :maDS "
                                                        + "AND d.trangThai = :tt "
                                                        + "AND (d.ngayDat < :ngay "
                                                        + "OR (d.ngayDat = :ngay AND d.maDatTruoc < :id))",
                                        Long.class)
                                        .setParameter("maDS", maDauSach)
                                        .setParameter("tt", TrangThai.DATTRUOC_WAITING)
                                        .setParameter("ngay", dt.getNgayDat())
                                        .setParameter("id", maDatTruoc)
                                        .getSingleResult();

                        if (truoc > 0) {
                                throw new NghiepVuException(
                                                "Phải xử lý lượt đặt trước cũ hơn của đầu sách này trước");
                        }

                        LocalDateTime now = LocalDateTime.now();
                        KetQuaGan kq = new KetQuaGan();
                        kq.maDatTruoc = maDatTruoc;

                        CuonSach sanSang = timCuonSanSang(em, maDauSach, maCuonChon);

                        if (sanSang != null) {
                                // Trường hợp đặc biệt: đã có cuốn AVAILABLE (phát sinh sau khi đặt)
                                CuonSach cs = em.find(
                                                CuonSach.class,
                                                sanSang.getMaCuonSach(),
                                                LockModeType.PESSIMISTIC_WRITE);

                                if (!TrangThai.CUON_AVAILABLE.equals(cs.getTrangThai())) {
                                        throw new NghiepVuException(
                                                        "Cuốn sách không còn sẵn sàng, vui lòng thử lại");
                                }

                                cs.setTrangThai(TrangThai.CUON_RESERVED);
                                PhieuMuonRepository.capNhatSoLuong(em, maDauSach, -1, 0);

                                dt.setCuonSach(cs);
                                dt.setTrangThai(TrangThai.DATTRUOC_NOTIFIED);
                                dt.setNgayThongBao(now);

                                kq.trangThai = TrangThai.DATTRUOC_NOTIFIED;
                                kq.maCuonSach = cs.getMaCuonSach();
                                kq.daThongBao = true;

                        } else {
                                ChiTietPhieuMuon ct = timDongMuonDeGiuCho(em, maDauSach, maCuonChon);

                                if (ct == null) {
                                        throw new NghiepVuException(
                                                        "Đầu sách không có cuốn nào đang được mượn để giữ chỗ");
                                }

                                CuonSach cs = em.find(
                                                CuonSach.class,
                                                ct.getCuonSach().getMaCuonSach(),
                                                LockModeType.PESSIMISTIC_WRITE);

                                if (!TrangThai.CUON_BORROWED.equals(cs.getTrangThai())) {
                                        throw new NghiepVuException(
                                                        "Cuốn sách không còn ở trạng thái đang mượn, vui lòng thử lại");
                                }

                                // Cuốn RESERVED: tự động vô hiệu hóa gia hạn (RENEW_BOOK kiểm tra điều này)
                                cs.setTrangThai(TrangThai.CUON_RESERVED);

                                dt.setCuonSach(cs);
                                dt.setTrangThai(TrangThai.DATTRUOC_RESERVED);

                                kq.trangThai = TrangThai.DATTRUOC_RESERVED;
                                kq.maCuonSach = cs.getMaCuonSach();
                                kq.ngayHenTraCuaCuon = ct.getNgayHenTra() != null
                                                ? ct.getNgayHenTra()
                                                : ct.getPhieuMuon().getNgayHenTra();
                        }

                        tx.commit();

                        return kq;

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
                        throw new NghiepVuException("Không thể gán cuốn sách do lỗi hệ thống!");

                } finally {
                        em.close();
                }
        }

        // GET_BORROW_HISTORY: trong danh sách đầu sách này, đầu sách nào có người chờ
        public Set<Integer> getDauSachCoNguoiCho(Collection<Integer> maDauSachList) {
                Set<Integer> ketQua = new HashSet<>();

                if (maDauSachList == null || maDauSachList.isEmpty()) {
                        return ketQua;
                }

                try (EntityManager em = EntityManagerUtil.getEntityManager()) {

                        ketQua.addAll(
                                        em.createQuery(
                                                        "SELECT DISTINCT d.dauSach.maDauSach FROM DatTruoc d "
                                                                        + "WHERE d.dauSach.maDauSach IN (:ids) "
                                                                        + "AND d.trangThai IN (:tts)",
                                                        Integer.class)
                                                        .setParameter("ids", maDauSachList)
                                                        .setParameter(
                                                                        "tts",
                                                                        List.of(
                                                                                        TrangThai.DATTRUOC_WAITING,
                                                                                        TrangThai.DATTRUOC_RESERVED))
                                                        .getResultList());
                }

                return ketQua;
        }

        // Lượt đặt đang gắn với một cuốn vật lý (khi trả sách / khi mượn)
        public DatTruoc findByCuonSach(String maCuonSach, String trangThai) {
                try (EntityManager em = EntityManagerUtil.getEntityManager()) {

                        List<DatTruoc> ds = em.createQuery(
                                        "SELECT d FROM DatTruoc d "
                                                        + "JOIN FETCH d.theDocGia "
                                                        + "WHERE d.cuonSach.maCuonSach = :ma "
                                                        + "AND d.trangThai = :tt "
                                                        + "ORDER BY d.ngayDat ASC",
                                        DatTruoc.class)
                                        .setParameter("ma", maCuonSach)
                                        .setParameter("tt", trangThai)
                                        .setMaxResults(1)
                                        .getResultList();

                        return ds.isEmpty() ? null : ds.get(0);
                }
        }

        // UC-10: các lượt NOTIFIED đã quá hạn giữ sách
        public List<DatTruoc> getNotifiedQuaHan(int soNgayGiu) {
                LocalDateTime gioiHan = LocalDateTime.now().minusDays(soNgayGiu);

                try (EntityManager em = EntityManagerUtil.getEntityManager()) {

                        return em.createQuery(
                                        "SELECT d FROM DatTruoc d "
                                                        + "JOIN FETCH d.theDocGia "
                                                        + "JOIN FETCH d.dauSach "
                                                        + "LEFT JOIN FETCH d.cuonSach "
                                                        + "WHERE d.trangThai = :tt "
                                                        + "AND d.ngayThongBao < :gh",
                                        DatTruoc.class)
                                        .setParameter("tt", TrangThai.DATTRUOC_NOTIFIED)
                                        .setParameter("gh", gioiHan)
                                        .getResultList();
                }
        }
}