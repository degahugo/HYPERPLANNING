package fr.univ.hyperplanning.persistence;

import fr.univ.hyperplanning.infra.config.JpaUtil;
import org.junit.jupiter.api.Test;

public class HibernateTest {
    @Test
    void boot_ok() {
        var em = JpaUtil.em();
        em.close();
    }
}