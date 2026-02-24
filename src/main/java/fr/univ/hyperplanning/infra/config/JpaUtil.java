package fr.univ.hyperplanning.infra.config;


import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class JpaUtil {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("hyperplanningPU");

    private JpaUtil() {}

    public static EntityManager em() {

        return emf.createEntityManager();
    }

    public static void close() {

        emf.close();
    }
}