package fr.univ.hyperplanning.infra.config;


import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class JpaUtil {

    private static EntityManagerFactory emf;

    private JpaUtil() {}

    public static synchronized EntityManager em() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("hyperplanningPU");
        }
        return emf.createEntityManager();
    }

    public static synchronized void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}