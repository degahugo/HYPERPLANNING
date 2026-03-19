module fr.univ.hyperplanning {

    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // JPA / Hibernate
    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    // Lombok
    requires static lombok;

    // Logging
    requires org.slf4j;

    // DataFaker
    requires net.datafaker;

    // Ouvre les packages controllers au module javafx.fxml (pour @FXML)
    opens fr.univ.hyperplanning.app.controllers to javafx.fxml;
    opens fr.univ.hyperplanning.app.controllers.Authentification to javafx.fxml;
    opens fr.univ.hyperplanning.app to javafx.fxml;
    // Ouvre les packages model à Hibernate (pour JPA)
    opens fr.univ.hyperplanning.model.utilisateurs to org.hibernate.orm.core;
    opens fr.univ.hyperplanning.model.pedagogie to org.hibernate.orm.core;
    opens fr.univ.hyperplanning.model.gestion to org.hibernate.orm.core;
    opens fr.univ.hyperplanning.model.enums to org.hibernate.orm.core;

    // Export principal
    exports fr.univ.hyperplanning.app;
}
