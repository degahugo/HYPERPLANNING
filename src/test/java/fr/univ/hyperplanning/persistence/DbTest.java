package fr.univ.hyperplanning.persistence;

import fr.univ.hyperplanning.infra.config.JpaUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DbTest {

    @Test
    void connexion_ok() {
        var em = JpaUtil.em();
        try {
            int one = ((Number) em.createNativeQuery("select 1").getSingleResult()).intValue();
            assertEquals(1, one);
        } finally {
            em.close();
        }
    }
}