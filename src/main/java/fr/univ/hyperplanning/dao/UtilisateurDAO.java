package fr.univ.hyperplanning.dao;

import fr.univ.hyperplanning.infra.config.JpaUtil;
import fr.univ.hyperplanning.model.enums.RoleUtilisateur;
import fr.univ.hyperplanning.model.utilisateurs.Etudiant;
import fr.univ.hyperplanning.model.utilisateurs.Utilisateur;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.sql.Date;
import java.time.LocalDate;

public class UtilisateurDAO {


    public Utilisateur findByUsername(String username) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery("SELECT u FROM Utilisateur u WHERE u.actif = true", Utilisateur.class)
                    .getResultList()
                    .stream()
                    .filter(u -> buildUsername(u).equals(username))
                    .findFirst()
                    .orElse(null);
        } finally {
            em.close();
        }
    }

    
    public Utilisateur findByEmail(String email) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                            "SELECT u FROM Utilisateur u WHERE u.email = :email AND u.actif = true",
                            Utilisateur.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    
    public boolean emailExiste(String email) {
        EntityManager em = JpaUtil.em();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(u) FROM Utilisateur u WHERE u.email = :email", Long.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    
    public Utilisateur inscrire(String nom, String prenom, String email, String motDePasseHash) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();

            Date today = Date.valueOf(LocalDate.now());

            Long nbEtudiants = em.createQuery("SELECT COUNT(e) FROM Etudiant e", Long.class)
                    .getSingleResult();
            String numeroEtudiant = String.format("ETU%04d", nbEtudiants + 1);

            Etudiant etu = new Etudiant();
            etu.setNom(nom.toUpperCase());
            etu.setPrenom(prenom);
            etu.setEmail(email);
            etu.setMotDePasseHash(motDePasseHash);
            etu.setRole(RoleUtilisateur.ETUDIANT);
            etu.setActif(true);
            etu.setDateCreation(today);
            etu.setDerniereConnexion(today);
            etu.setNumeroEtudiant(numeroEtudiant);
            etu.setAnneeEntree(LocalDate.now().getYear());

            em.persist(etu);
            em.getTransaction().commit();
            return etu;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }


    public void mettreAJourDerniereConnexion(Utilisateur utilisateur) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            Utilisateur u = em.find(Utilisateur.class, utilisateur.getId());
            u.setDerniereConnexion(Date.valueOf(LocalDate.now()));
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    
    public void changerMotDePasse(Utilisateur utilisateur, String nouveauHash) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            Utilisateur u = em.find(Utilisateur.class, utilisateur.getId());
            u.setMotDePasseHash(nouveauHash);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
    
    public static String buildUsername(Utilisateur u) {
        return u.getNom() + u.getPrenom() + u.getId();
    }
}
