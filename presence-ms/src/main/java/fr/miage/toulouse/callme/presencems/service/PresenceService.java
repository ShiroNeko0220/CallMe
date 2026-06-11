package fr.miage.toulouse.callme.presencems.service;

import fr.miage.toulouse.callme.libcommun.ApiException;
import fr.miage.toulouse.callme.presencems.DTO.BadgeageRequest;
import fr.miage.toulouse.callme.presencems.DTO.PresenceResponse;
import fr.miage.toulouse.callme.presencems.clients.*;
import fr.miage.toulouse.callme.presencems.entity.Presence;
import fr.miage.toulouse.callme.presencems.repository.PresenceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PresenceService {
    private final PresenceRepository repository;
    private final BadgeClient badgeClient;
    private final CoursClient coursClient;
    private final UtilisateurClient utilisateurClient;

    public PresenceService(PresenceRepository repository, BadgeClient badgeClient, CoursClient coursClient, UtilisateurClient utilisateurClient) {
        this.repository = repository;
        this.badgeClient = badgeClient;
        this.coursClient = coursClient;
        this.utilisateurClient = utilisateurClient;
    }

    @Transactional
    public PresenceResponse badger(BadgeageRequest request) {
        BadgeClient.BadgeResponse badge = getBadge(request.getIdBadge());

        if (!"ASSOCIE".equals(badge.getStatut()) || badge.getIdPorteur() == null) {
            throw new ApiException(HttpStatus.CONFLICT, "Badge non associé à un élève");
        }

        CoursClient.CoursResponse cours = getCours(request.getIdCours());
        Integer niveauEleve = utilisateurClient.getNiveauUtilisateur(badge.getIdPorteur());

        if (niveauEleve == null || cours.getNiveauCible() == null || !niveauEleve.equals(cours.getNiveauCible())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "L'élève n'appartient pas au niveau du cours");
        }

        if (repository.existsByIdPorteurAndIdCours(badge.getIdPorteur(), cours.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Présence déjà enregistrée pour cet élève et ce cours");
        }

        Presence presence = new Presence();
        presence.setIdBadge(badge.getIdBadge());
        presence.setIdPorteur(badge.getIdPorteur());
        presence.setIdCours(cours.getId());
        presence.setDateBadgeage(LocalDateTime.now());
        return toDTO(repository.save(presence));
    }

    @Transactional(readOnly = true)
    public List<PresenceResponse> lister() { return repository.findAll().stream().map(this::toDTO).toList(); }

    @Transactional(readOnly = true)
    public Map<Long, Long> compterParTousCours() {
        return repository.countGroupByIdCours().stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));
    }

    public List<PresenceResponse> listerParEleve(Long idEleve, LocalDate debut, LocalDate fin) {
        if (debut != null && fin != null) {
            return repository.findByIdPorteurAndDateBadgeageBetween(idEleve, debut.atStartOfDay(), fin.atTime(LocalTime.MAX))
                    .stream().map(this::toDTO).toList();
        }
        return repository.findByIdPorteur(idEleve).stream().map(this::toDTO).toList();
    }

    public List<PresenceResponse> listerParCours(Long idCours) {
        return repository.findByIdCours(idCours).stream().map(this::toDTO).toList();
    }

    public long compterParCours(Long idCours) { return repository.countByIdCours(idCours); }

    private PresenceResponse toDTO(Presence p) {
        return PresenceResponse.builder()
                .idPresence(p.getIdPresence())
                .idBadge(p.getIdBadge())
                .idPorteur(p.getIdPorteur())
                .idCours(p.getIdCours())
                .dateBadgeage(p.getDateBadgeage())
                .build();
    }

    private BadgeClient.BadgeResponse getBadge(Long idBadge) {
        try { return badgeClient.getBadgeParId(idBadge); }
        catch (Exception e) { throw new ApiException(HttpStatus.NOT_FOUND, "Badge introuvable"); }
    }

    private CoursClient.CoursResponse getCours(Long idCours) {
        try { return coursClient.getCours(idCours); }
        catch (Exception e) { throw new ApiException(HttpStatus.NOT_FOUND, "Cours introuvable"); }
    }
}
