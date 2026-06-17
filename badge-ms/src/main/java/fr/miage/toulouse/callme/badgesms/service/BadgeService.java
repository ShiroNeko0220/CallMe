package fr.miage.toulouse.callme.badgesms.service;

import fr.miage.toulouse.callme.badgesms.DTO.AlerteBadgeResponse;
import fr.miage.toulouse.callme.badgesms.DTO.BadgeRequest;
import fr.miage.toulouse.callme.badgesms.DTO.BadgeResponse;
import fr.miage.toulouse.callme.badgesms.clients.UtilisateurClient;
import fr.miage.toulouse.callme.badgesms.entity.AlerteBadge;
import fr.miage.toulouse.callme.badgesms.entity.Badge;
import fr.miage.toulouse.callme.badgesms.entity.Statut;
import fr.miage.toulouse.callme.badgesms.repository.AlerteBadgeRepository;
import fr.miage.toulouse.callme.badgesms.repository.BadgeRepository;
import fr.miage.toulouse.callme.libcommun.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final AlerteBadgeRepository alerteRepository;
    private final UtilisateurClient utilisateurClient;

    public BadgeService(BadgeRepository badgeRepository, AlerteBadgeRepository alerteRepository,
                        UtilisateurClient utilisateurClient) {
        this.badgeRepository = badgeRepository;
        this.alerteRepository = alerteRepository;
        this.utilisateurClient = utilisateurClient;
    }

    public BadgeResponse creerBadge(BadgeRequest request) {
        Badge badge = new Badge();
        badge.setDateCreation(LocalDateTime.now());
        badge.setStatut(Statut.DISPONIBLE);
        return toDTO(badgeRepository.save(badge));
    }

    public List<BadgeResponse> listerBadges() {
        return badgeRepository.findAll().stream().map(this::toDTO).toList();
    }

    public BadgeResponse getBadgeById(Long id) {
        return toDTO(findById(id));
    }

    public BadgeResponse getBadgeByPorteur(Long idPorteur) {
        return badgeRepository.findByIdPorteur(idPorteur)
                .map(this::toDTO)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Aucun badge associé à ce porteur"));
    }

    public BadgeResponse associerBadge(Long idBadge, Long idPorteur) {
        Badge badge = findById(idBadge);

        if (badge.getStatut() == Statut.ASSOCIE) {
            throw new ApiException(HttpStatus.CONFLICT, "Ce badge est déjà associé à un porteur.");
        }

        badgeRepository.findByIdPorteur(idPorteur).ifPresent(b -> {
            throw new ApiException(HttpStatus.CONFLICT, "Ce porteur possède déjà un badge.");
        });

        verifierPorteurExiste(idPorteur);

        badge.setIdPorteur(idPorteur);
        badge.setStatut(Statut.ASSOCIE);
        badge.setDateAssociation(LocalDateTime.now());

        BadgeResponse response = toDTO(badgeRepository.save(badge));

        // Résoudre les alertes en attente pour cet enseignant/membre
        alerteRepository.findByResolueOrderByDateCreationDesc(false).stream()
                .filter(a -> idPorteur.equals(a.getIdEnseignant()))
                .forEach(a -> { a.setResolue(true); alerteRepository.save(a); });

        return response;
    }

    public List<AlerteBadgeResponse> listerAlertes() {
        return alerteRepository.findByResolueOrderByDateCreationDesc(false)
                .stream().map(this::toAlerteDTO).toList();
    }

    private AlerteBadgeResponse toAlerteDTO(AlerteBadge a) {
        return AlerteBadgeResponse.builder()
                .id(a.getId())
                .typeActivite(a.getTypeActivite())
                .idActivite(a.getIdActivite())
                .titreActivite(a.getTitreActivite())
                .dateActivite(a.getDateActivite())
                .idEnseignant(a.getIdEnseignant())
                .dateCreation(a.getDateCreation())
                .resolue(a.isResolue())
                .build();
    }

    public BadgeResponse dissocierBadge(Long idBadge) {
        Badge badge = findById(idBadge);

        if (badge.getStatut() != Statut.ASSOCIE) {
            throw new ApiException(HttpStatus.CONFLICT, "Ce badge n'est associé à aucun porteur.");
        }

        badge.setIdPorteur(null);
        badge.setStatut(Statut.DISPONIBLE);
        badge.setDateAssociation(null);

        return toDTO(badgeRepository.save(badge));
    }

    public void supprimerBadge(Long idBadge) {
        Badge badge = findById(idBadge);
        badgeRepository.delete(badge);
    }

    private Badge findById(Long id) {
        return badgeRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Badge non existant"));
    }

    private BadgeResponse toDTO(Badge badge) {
        return BadgeResponse.builder()
                .idBadge(badge.getIdBadge())
                .idPorteur(badge.getIdPorteur())
                .statut(badge.getStatut().name())
                .dateCreation(badge.getDateCreation())
                .dateAssociation(badge.getDateAssociation())
                .build();
    }

    private void verifierPorteurExiste(Long idPorteur) {
        boolean existe;
        try {
            existe = utilisateurClient.existsById(idPorteur);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Impossible de vérifier le" +
                    " membre. Veuillez réessayer dans quelques instants.");
        }
        if (!existe) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Aucun utilisateur trouvé avec l'id " + idPorteur);
        }
    }
}