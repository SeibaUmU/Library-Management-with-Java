package com.library.dao;

import com.library.entities.SystemConfig;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class ConfigDAO {

    public SystemConfig getConfig() {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            SystemConfig config = em.find(SystemConfig.class, 1);

            if (config == null) {
                // Khởi tạo mặc định nếu chưa có cấu hình trong DB
                config = new SystemConfig();

                config.setConfigId(1);
                config.setMaxBorrowDays(14);
                config.setMaxBooksPerReader(5);
                config.setFinePerDay(5000.0);
                config.setMaxRenewTimes(2);
                config.setHoldKeepDays(3);

                saveConfig(config);
            }

            return config;
        }
    }


    public boolean saveConfig(SystemConfig config) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            em.merge(config);

            tx.commit();

            return true;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }

            return false;

        } finally {
            em.close();
        }
    }
}
