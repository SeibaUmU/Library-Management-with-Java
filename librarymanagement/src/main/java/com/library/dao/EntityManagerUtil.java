package com.library.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class EntityManagerUtil {
    private static final String PERSISTENCE_UNIT_NAME = "LibraryPU"; 
    private static EntityManagerFactory factory;

    public static synchronized EntityManagerFactory getEntityManagerFactory(){
        if (factory == null || !factory.isOpen()){
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        }
        return factory;
    }

    public static EntityManager getEntityManager(){
        return getEntityManagerFactory().createEntityManager();
    }
    public static void close(){
        if (factory!=null && factory.isOpen()){
            factory.close();
        }
    }
}
