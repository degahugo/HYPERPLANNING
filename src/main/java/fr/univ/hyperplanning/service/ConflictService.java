package fr.univ.hyperplanning.service;

import fr.univ.hyperplanning.model.ConflictInfo; // Importation du modèle
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConflictService {
    private final String url = "jdbc:postgresql://localhost:5432/hyperplanning";
    private final String user = "sa";
    private final String password = "secret";

    public List<ConflictInfo> getRoomConflicts() {
        List<ConflictInfo> list = new ArrayList<>();

        // On cherche les cours qui se chevauchent dans la même salle
        String sql = "SELECT c1.id as id1, c2.id as id2, c1.intitule, c1.salle_id " +
                "FROM cours c1, cours c2 " +
                "WHERE c1.id < c2.id " +
                "AND c1.salle_id = c2.salle_id " +
                "AND c1.datedebut < c2.datefin " +
                "AND c1.datefin > c2.datedebut";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String desc = "Conflit salle n°" + rs.getLong("salle_id") +
                        " entre les cours " + rs.getLong("id1") + " et " + rs.getLong("id2");

                // On ajoute chaque conflit trouvé à notre liste
                list.add(new ConflictInfo(rs.getLong("id1"), desc));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}