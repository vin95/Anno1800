package com.anno1800.cards;

import com.anno1800.game.cards.ExpeditionCard;
import com.anno1800.game.residents.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ExpeditionCard.
 * Covers slot placement (animal / artifact), point calculation, and capacity limits.
 */
@DisplayName("ExpeditionCard Tests")
class ExpeditionCardTest {

    /** Karte: Artisan (3) für Tier, Engineer (4) für Artefakt – 2 VP / 3 VP */
    private ExpeditionCard card;

    @BeforeEach
    void setUp() {
        card = new ExpeditionCard(3, 4, 2, 3);
    }

    // =========================================================================
    // Anfangszustand
    // =========================================================================

    @Test
    @DisplayName("Neue Karte hat kein Tier-Besucher")
    void testInitiallyNoAnimal() {
        assertFalse(card.hasAnimal());
    }

    @Test
    @DisplayName("Neue Karte hat kein Artefakt-Besucher")
    void testInitiallyNoArtifact() {
        assertFalse(card.hasArtifact());
    }

    @Test
    @DisplayName("Neue Karte gibt 0 Punkte")
    void testInitialPointsZero() {
        assertEquals(0, card.calculatePoints());
    }

    // =========================================================================
    // Tier-Slot (Animal)
    // =========================================================================

    @Test
    @DisplayName("Richtigen Tier-Besucher (Artisan, Level 3) einsetzen – Erfolg")
    void testPlaceCorrectAnimalVisitor() {
        Artisan artisan = new Artisan();
        boolean placed = card.placeAnimalVisitor(artisan);
        assertTrue(placed);
        assertTrue(card.hasAnimal());
    }

    @Test
    @DisplayName("Falschen Tier-Besucher (Farmer, Level 1) ablehnen")
    void testRejectWrongLevelAnimalVisitor() {
        Farmer farmer = new Farmer(); // Level 1, Karte benötigt 3
        boolean placed = card.placeAnimalVisitor(farmer);
        assertFalse(placed);
        assertFalse(card.hasAnimal());
    }

    @Test
    @DisplayName("Zweiten Tier-Besucher ablehnen, wenn Slot belegt")
    void testCannotPlaceAnimalTwice() {
        Artisan first = new Artisan();
        Artisan second = new Artisan();
        card.placeAnimalVisitor(first);
        boolean placedSecond = card.placeAnimalVisitor(second);
        assertFalse(placedSecond, "Zweiter Tier-Besucher sollte abgelehnt werden");
    }

    // =========================================================================
    // Artefakt-Slot (Artifact)
    // =========================================================================

    @Test
    @DisplayName("Richtigen Artefakt-Besucher (Engineer, Level 4) einsetzen – Erfolg")
    void testPlaceCorrectArtifactVisitor() {
        Engineer engineer = new Engineer();
        boolean placed = card.placeArtifactVisitor(engineer);
        assertTrue(placed);
        assertTrue(card.hasArtifact());
    }

    @Test
    @DisplayName("Falschen Artefakt-Besucher (Farmer, Level 1) ablehnen")
    void testRejectWrongLevelArtifactVisitor() {
        Farmer farmer = new Farmer(); // Level 1, Karte benötigt 4
        boolean placed = card.placeArtifactVisitor(farmer);
        assertFalse(placed);
        assertFalse(card.hasArtifact());
    }

    @Test
    @DisplayName("Zweiten Artefakt-Besucher ablehnen, wenn Slot belegt")
    void testCannotPlaceArtifactTwice() {
        Engineer first = new Engineer();
        Engineer second = new Engineer();
        card.placeArtifactVisitor(first);
        boolean placedSecond = card.placeArtifactVisitor(second);
        assertFalse(placedSecond);
    }

    // =========================================================================
    // Punkteberechnung
    // =========================================================================

    @Test
    @DisplayName("Nur Tier-Slot gefüllt: 2 Punkte")
    void testAnimalSlotGivesTwoPoints() {
        card.placeAnimalVisitor(new Artisan());
        assertEquals(2, card.calculatePoints());
    }

    @Test
    @DisplayName("Nur Artefakt-Slot gefüllt: 3 Punkte")
    void testArtifactSlotGivesThreePoints() {
        card.placeArtifactVisitor(new Engineer());
        assertEquals(3, card.calculatePoints());
    }

    @Test
    @DisplayName("Beide Slots gefüllt: 5 Punkte (2+3)")
    void testBothSlotsFilledGivesFivePoints() {
        card.placeAnimalVisitor(new Artisan());
        card.placeArtifactVisitor(new Engineer());
        assertEquals(5, card.calculatePoints());
    }

    @Test
    @DisplayName("Karte mit zwei Investor-Slots (Level 5): je 4 Punkte")
    void testInvestorLevelCardPoints() {
        ExpeditionCard investorCard = new ExpeditionCard(5, 5, 4, 4);
        investorCard.placeAnimalVisitor(new Investor());
        investorCard.placeArtifactVisitor(new Investor());
        assertEquals(8, investorCard.calculatePoints());
    }

    @Test
    @DisplayName("placeResident(resident, 1) füllt Tier-Slot")
    void testPlaceResidentSlot1FillsAnimal() {
        Artisan artisan = new Artisan();
        card.placeResident(artisan, 1);
        assertTrue(card.hasAnimal());
    }

    @Test
    @DisplayName("placeResident(resident, 2) füllt Artefakt-Slot")
    void testPlaceResidentSlot2FillsArtifact() {
        Engineer engineer = new Engineer();
        card.placeResident(engineer, 2);
        assertTrue(card.hasArtifact());
    }
}
