const dbName = "competition_db";
const competitionDb = db.getSiblingDB(dbName);

competitionDb.createCollection("competitions");
competitionDb.createCollection("resultats");

competitionDb.competitions.deleteMany({ _id: { $in: [
    "comp-n1-initiation", "comp-n2-technique", "comp-n3-choregraphie", "comp-n4-avance", "comp-n5-masterclass"
        ]}});

competitionDb.resultats.deleteMany({ _id: { $in: [
    "res-comp-n1-membre5", "res-comp-n2-membre6", "res-comp-n3-membre7", "res-comp-n4-membre8", "res-comp-n5-membre9"
        ]}});

const now = new Date();

function futureDate(days) {
    return new Date(now.getFullYear(), now.getMonth(), now.getDate() + days, 0, 0, 0);
}

function localTime(hour, minute) {
    return `${String(hour).padStart(2, "0")}:${String(minute).padStart(2, "0")}:00`;
}

competitionDb.competitions.insertMany([
    {
        _id: "comp-n1-initiation",
        titre: "Compétition découverte niveau 1",
        niveauCible: 1,
        date: futureDate(20),
        heureDebut: localTime(10, 0),
        duree: 60,
        lieu: "Gymnase A",
        enseignantId: NumberLong(3)
    },
    {
        _id: "comp-n2-technique",
        titre: "Challenge technique niveau 2",
        niveauCible: 2,
        date: futureDate(21),
        heureDebut: localTime(11, 0),
        duree: 75,
        lieu: "Gymnase B",
        enseignantId: NumberLong(3)
    },
    {
        _id: "comp-n3-choregraphie",
        titre: "Tournoi chorégraphie niveau 3",
        niveauCible: 3,
        date: futureDate(22),
        heureDebut: localTime(14, 0),
        duree: 90,
        lieu: "Salle C",
        enseignantId: NumberLong(3)
    },
    {
        _id: "comp-n4-avance",
        titre: "Open avancé niveau 4",
        niveauCible: 4,
        date: futureDate(23),
        heureDebut: localTime(15, 30),
        duree: 90,
        lieu: "Salle D",
        enseignantId: NumberLong(4)
    },
    {
        _id: "comp-n5-masterclass",
        titre: "Grand prix niveau 5",
        niveauCible: 5,
        date: futureDate(24),
        heureDebut: localTime(17, 0),
        duree: 120,
        lieu: "Grande Salle",
        enseignantId: NumberLong(4)
    }
]);

competitionDb.resultats.insertMany([
    {
        _id: "res-comp-n1-membre5",
        competitionId: "comp-n1-initiation",
        eleveId: NumberLong(5),
        enseignantId: NumberLong(3),
        note: NumberDecimal("7.5"),
        competitionDate: futureDate(20)
    },
    {
        _id: "res-comp-n2-membre6",
        competitionId: "comp-n2-technique",
        eleveId: NumberLong(6),
        enseignantId: NumberLong(3),
        note: NumberDecimal("8.0"),
        competitionDate: futureDate(21)
    },
    {
        _id: "res-comp-n3-membre7",
        competitionId: "comp-n3-choregraphie",
        eleveId: NumberLong(7),
        enseignantId: NumberLong(3),
        note: NumberDecimal("8.7"),
        competitionDate: futureDate(22)
    },
    {
        _id: "res-comp-n4-membre8",
        competitionId: "comp-n4-avance",
        eleveId: NumberLong(8),
        enseignantId: NumberLong(4),
        note: NumberDecimal("6.9"),
        competitionDate: futureDate(23)
    },
    {
        _id: "res-comp-n5-membre9",
        competitionId: "comp-n5-masterclass",
        eleveId: NumberLong(9),
        enseignantId: NumberLong(4),
        note: NumberDecimal("9.2"),
        competitionDate: futureDate(24)
    }
]);

competitionDb.competitions.createIndex({ niveauCible: 1 });
competitionDb.competitions.createIndex({ enseignantId: 1 });
competitionDb.resultats.createIndex({ competitionId: 1, eleveId: 1 }, { unique: true });
competitionDb.resultats.createIndex({ eleveId: 1 });
