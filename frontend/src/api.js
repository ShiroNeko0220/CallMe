import axios from 'axios'

const h = (role, userId) => {
  const headers = { 'Content-Type': 'application/json' }
  if (role) headers['X-Role'] = role
  if (userId) headers['X-Utilisateur-Id'] = userId
  return headers
}

export const api = {
  utilisateurs: {
    login:     (data)          => axios.post('/api/utilisateurs/login', data),
    lister:    (role)          => axios.get('/api/utilisateurs', { headers: h(role) }),
    consulter: (id)            => axios.get(`/api/utilisateurs/${id}`),
    creer:     (data, role, userId) => axios.post('/api/utilisateurs', data, { headers: h(role, userId) }),
    modifier:      (id, data, role, userId) => axios.patch(`/api/utilisateurs/${id}`, data, { headers: h(role, userId) }),
    modifierAdmin: (id, data, role, userId) => axios.patch(`/api/utilisateurs/${id}/admin`, data, { headers: h(role, userId) }),
    supprimer: (id, role)      => axios.delete(`/api/utilisateurs/${id}`, { headers: h(role) }),
  },

  cours: {
    lister:              ()           => axios.get('/api/cours'),
    listerParNiveau:     (niveau)     => axios.get(`/api/cours/niveau/${niveau}`),
    listerParEnseignant: (id)         => axios.get(`/api/cours/enseignant/${id}`),
    consulter:           (id)         => axios.get(`/api/cours/${id}`),
    creer:               (data, role, userId) => axios.post('/api/cours', data, { headers: h(role, userId) }),
    modifier:            (id, data, role, userId) => axios.patch(`/api/cours/${id}`, data, { headers: h(role, userId) }),
    supprimer:           (id, role)   => axios.delete(`/api/cours/${id}`, { headers: h(role) }),
  },

  competitions: {
    lister:          ()              => axios.get('/api/competitions'),
    listerParNiveau: (niveau)        => axios.get(`/api/competitions/niveau/${niveau}`),
    listerPourEleve: (eleveId)       => axios.get(`/api/competitions/eleve/${eleveId}`),
    listerParEnseignant: (enseignantId) => axios.get(`/api/competitions/enseignant/${enseignantId}`),
    consulter:       (id)            => axios.get(`/api/competitions/${id}`),
    creer:           (data, role, userId) => axios.post('/api/competitions', data, { headers: h(role, userId) }),
    modifier: (id, data, role, userId) => axios.patch(`/api/competitions/${id}`, data, { headers: h(role, userId) }),
    modifierResultat: (resultatId, data, role, userId) => axios.patch(`/api/competitions/resultats/${resultatId}`, data, { headers: h(role, userId) }),
    supprimer:       (id, role)      => axios.delete(`/api/competitions/${id}`, { headers: h(role) }),
    ajouterResultat: (competId, data, role, userId) =>
        axios.post(`/api/competitions/${competId}/resultats`, data, { headers: h(role, userId) }),
    listerResultats: (competId)      => axios.get(`/api/competitions/${competId}/resultats`),
    resultatsEleve:  (eleveId)       => axios.get(`/api/competitions/eleve/${eleveId}/resultats`),
    compterParNiveau: (niveau)       => axios.get(`/api/competitions/niveau/${niveau}/count`),
  },

  badges: {
    lister:    ()                        => axios.get('/api/badges'),
    consulter: (id)                      => axios.get(`/api/badges/${id}`),
    creer:     (role)                    => axios.post('/api/badges', {}, { headers: h(role) }),
    associer:  (idBadge, idPorteur, role) =>
        axios.patch(`/api/badges/${idBadge}/associer/${idPorteur}`, {}, { headers: h(role) }),
    dissocier: (idBadge, role)           =>
        axios.patch(`/api/badges/${idBadge}/dissocier`, {}, { headers: h(role) }),
    supprimer: (id, role)                =>
        axios.delete(`/api/badges/${id}`, { headers: h(role) }),
  },

  presences: {
    listerParEleve: (id)            => axios.get(`/api/presences/eleve/${id}`),
    listerParCours: (id)            => axios.get(`/api/presences/cours/${id}`),
    enregistrer:    (idBadge, idCours) =>
        axios.post('/api/presences/badger', { idBadge, idCours }),
  },

  statistiques: {
    globalCours:          (role)         => axios.get('/api/statistiques/cours', { headers: h(role) }),
    elevesParCours:       (idCours, role) => axios.get(`/api/statistiques/cours/${idCours}/eleves`, { headers: h(role) }),
    coursParEleve:        (eleveId, role, debut, fin) => axios.get(`/api/statistiques/eleves/${eleveId}/cours`, { headers: h(role), params: debut && fin ? { debut, fin } : {} }),
    competitionsParEleve: (eleveId, role, debut, fin) => axios.get(`/api/statistiques/eleves/${eleveId}/competitions`, { headers: h(role), params: debut && fin ? { debut, fin } : {} }),
    nbCompetitionsNiveau: (niveau, role)  => axios.get(`/api/statistiques/competitions/niveau/${niveau}/nombre`, { headers: h(role) }),
  },
}