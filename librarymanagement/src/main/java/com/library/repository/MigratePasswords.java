package com.library.repository;

import java.util.List;

import com.library.common.PasswordUtil;
import com.library.entities.NguoiDung;

import jakarta.persistence.EntityManager;

public class MigratePasswords {

    public static void main(String[] args) {
        EntityManager em = EntityManagerUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            List<NguoiDung> tatCa = em.createQuery(
                    "SELECT n FROM NguoiDung n",
                    NguoiDung.class
            ).getResultList();

            int dem = 0;

            for (NguoiDung n : tatCa) {
                String mk = n.getMatKhau();

                if (mk != null && !mk.startsWith("$2a$")) {
                    n.setMatKhau(PasswordUtil.hash(mk));
                    dem++;
                }
            }

            em.getTransaction().commit();

            System.out.println("Đã băm " + dem + " mật khẩu.");

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();

        } finally {
            em.close();
            EntityManagerUtil.close();
        }
    }
}
