package fr.miage.toulouse.callme.badgesms.service;

import fr.miage.toulouse.callme.badgesms.DTO.BadgeRequest;
import fr.miage.toulouse.callme.badgesms.clients.*;
import fr.miage.toulouse.callme.badgesms.entity.*;
import fr.miage.toulouse.callme.badgesms.repository.BadgeRepository;
import fr.miage.toulouse.callme.libcommun.ApiException;
import fr.miage.toulouse.callme.libcommun.Role;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UtilisateurClient utilisateurClient;

    public BadgeService(BadgeRepository badgeRepository, UtilisateurClient utilisateurClient) {
        this.badgeRepository = badgeRepository;
        this.utilisateurClient = utilisateurClient;
    }

    public Badge creerBadge(Long userId, BadgeRequest request) {
        verifierRoleUtilisateurCourant(userId);

        Badge badge = new Badge();
        badge.setDateCreation(LocalDateTime.now());
        badge.setStatut(Statut.DISPONIBLE);

        return badgeRepository.save(badge);
    }

    public List<Badge> listerBadges() {
        return badgeRepository.findAll();
    }

    public Badge getBadgeById(Long id) {
        return badgeRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Badge non existant"));
    }

    public Badge associerBadge(Long userId, Long idBadge, Long idPorteur) {
        verifierRoleUtilisateurCourant(userId);
        verifierPorteurExiste(idPorteur);

        Badge badge = getBadgeById(idBadge);

        if (badge.getStatut() == Statut.ASSOCIE) {
            throw new ApiException(HttpStatus.CONFLICT, "Ce badge est déjà associé à un porteur.");
        }

        badgeRepository.findByIdPorteur(idPorteur).ifPresent(b -> {
            throw new ApiException(HttpStatus.CONFLICT, "Ce porteur possède déjà un badge.");
        });

        badge.setIdPorteur(idPorteur);
        badge.setStatut(Statut.ASSOCIE);
        badge.setDateAssociation(LocalDateTime.now());

        return badgeRepository.save(badge);
    }

    public Badge dissocierBadge(Long userId, Long idBadge) {
        verifierRoleUtilisateurCourant(userId);

        Badge badge = getBadgeById(idBadge);

        if (badge.getStatut() != Statut.ASSOCIE) {
            throw new ApiException(HttpStatus.CONFLICT, "Ce badge n'est associé à aucun porteur.");
        }

        badge.setIdPorteur(null);
        badge.setStatut(Statut.DISPONIBLE);
        badge.setDateAssociation(null);

        return badgeRepository.save(badge);
    }

    public void supprimerBadge(Long userId, Long idBadge) {
        verifierRoleUtilisateurCourant(userId);

        Badge badge = getBadgeById(idBadge);
        badgeRepository.delete(badge);
    }

    private void verifierPorteurExiste(Long idPorteur) {
        boolean existe;

        try {
            existe = utilisateurClient.existsById(idPorteur);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Impossible de vérifier l'utilisateur auprès du microservice utilisateur.");
        }

        if (!existe) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Aucun utilisateur trouvé avec l'id " + idPorteur);
        }
    }

    /**
     * Vérifies que l'utilisateur (identifié par userId) a le rôle SECRETAIRE ou PRESIDENT
     * récupère le rôle via Feign depuis le microservice utilisateur
     */
    private void verifierRoleUtilisateurCourant(Long userId) {
        try {
            Role roleUtilisateur = utilisateurClient.getRoleUtilisateur(userId);
            
            if (roleUtilisateur == null || 
                (!roleUtilisateur.equals(Role.SECRETAIRE) && !roleUtilisateur.equals(Role.PRESIDENT))) {
                throw new ApiException(HttpStatus.FORBIDDEN, 
                    "Seul le (la) secrétaire ou le (la) président(e) peut effectuer cette action.");
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, 
                "Impossible de vérifier le rôle de l'utilisateur auprès du microservice utilisateur.");
        }
    }
}
