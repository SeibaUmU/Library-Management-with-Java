package com.library.repository;

import java.util.List;

import com.library.common.BadRequestException;
import com.library.entities.NhaXuatBan;
import com.library.entities.TacGia;
import com.library.entities.TheLoai;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class DanhMucRepository {

    public static final String LOAI_TAC_GIA = "AUTHOR";
    public static final String LOAI_NXB = "PUBLISHER";
    public static final String LOAI_THE_LOAI = "CATEGORY";

    // Mã kết quả của xoa()
    public static final int XOA_OK = 0;
    public static final int XOA_KHONG_TIM_THAY = 1;
    public static final int XOA_DANG_DUNG = 2;
    public static final int XOA_LOI = -1;

    // ===== Đọc (GET_CATEGORIES) =====

    public List<TacGia> getAllTacGia() {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT t FROM TacGia t ORDER BY t.tenTacGia",
                    TacGia.class
            ).getResultList();
        }
    }

    public List<NhaXuatBan> getAllNhaXuatBan() {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT n FROM NhaXuatBan n ORDER BY n.tenNXB",
                    NhaXuatBan.class
            ).getResultList();
        }
    }

    public List<TheLoai> getAllTheLoai() {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT t FROM TheLoai t ORDER BY t.tenTL",
                    TheLoai.class
            ).getResultList();
        }
    }

    // ===== Hỗ trợ =====

    private static Class<?> entityClass(String loai) {
        switch (loai) {
            case LOAI_TAC_GIA:  return TacGia.class;
            case LOAI_NXB:      return NhaXuatBan.class;
            case LOAI_THE_LOAI: return TheLoai.class;
            default:
                throw new BadRequestException("Thiếu hoặc sai tham số: loai");
        }
    }

    private static String usageJpql(String loai) {
        switch (loai) {
            case LOAI_TAC_GIA:
                return "SELECT COUNT(d) FROM DauSach d WHERE d.tacGia.maTG = :id";
            case LOAI_NXB:
                return "SELECT COUNT(d) FROM DauSach d WHERE d.nhaXuatBan.maNXB = :id";
            default:
                return "SELECT COUNT(d) FROM DauSach d WHERE d.theLoai.maTL = :id";
        }
    }

    // ===== Thêm: trả về mã mới, hoặc null nếu lỗi =====

    public Integer them(String loai, String ten, Integer namXuatBan) {
        entityClass(loai); // kiểm tra loai hợp lệ trước khi mở transaction

        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Integer id;

            if (LOAI_TAC_GIA.equals(loai)) {
                TacGia tg = new TacGia();
                tg.setTenTacGia(ten);
                em.persist(tg);
                id = tg.getMaTG();

            } else if (LOAI_NXB.equals(loai)) {
                NhaXuatBan nxb = new NhaXuatBan();
                nxb.setTenNXB(ten);
                nxb.setNamXuatBan(namXuatBan);
                em.persist(nxb);
                id = nxb.getMaNXB();

            } else {
                TheLoai tl = new TheLoai();
                tl.setTenTL(ten);
                em.persist(tl);
                id = tl.getMaTL();
            }

            tx.commit();

            return id;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            return null;

        } finally {
            em.close();
        }
    }

    // ===== Sửa: chỉ đổi trường nào được gửi. false = không tìm thấy / lỗi =====

    public boolean sua(String loai, Integer id, String ten, Integer namXuatBan) {
        Class<?> cls = entityClass(loai);

        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Object e = em.find(cls, id);

            if (e == null) {
                tx.rollback();
                return false;
            }

            boolean coTen = ten != null && !ten.trim().isEmpty();

            if (e instanceof TacGia) {
                if (coTen) ((TacGia) e).setTenTacGia(ten);

            } else if (e instanceof NhaXuatBan) {
                if (coTen) ((NhaXuatBan) e).setTenNXB(ten);
                if (namXuatBan != null) ((NhaXuatBan) e).setNamXuatBan(namXuatBan);

            } else if (e instanceof TheLoai) {
                if (coTen) ((TheLoai) e).setTenTL(ten);
            }

            tx.commit();

            return true;

        } catch (Exception ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            ex.printStackTrace();
            return false;

        } finally {
            em.close();
        }
    }

    // ===== Xóa: không xóa khi đang có đầu sách dùng =====

    public int xoa(String loai, Integer id) {
        Class<?> cls = entityClass(loai);

        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Object e = em.find(cls, id);

            if (e == null) {
                tx.rollback();
                return XOA_KHONG_TIM_THAY;
            }

            Long dangDung = em.createQuery(usageJpql(loai), Long.class)
                    .setParameter("id", id)
                    .getSingleResult();

            if (dangDung > 0) {
                tx.rollback();
                return XOA_DANG_DUNG;
            }

            em.remove(e);

            tx.commit();

            return XOA_OK;

        } catch (Exception ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            ex.printStackTrace();
            return XOA_LOI;

        } finally {
            em.close();
        }
    }
}
