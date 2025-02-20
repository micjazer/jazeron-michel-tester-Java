package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
// Classe de test pour la classe FareCalculatorService.
// Cette classe teste les différentes fonctionnalités de calcul de tarifs du parking en prenant en compte divers scénarios.
// Scénarios couverts : calcul normal, cas spéciaux comme les entrées futures, parkings courts, parkings prolongés, et réduction pour utilisateur régulier.
// Framework utilisé : JUnit 5.
public class FareCalculatorServiceTest {

    // Instance statique de FareCalculatorService utilisée dans les tests.
    private static FareCalculatorService fareCalculatorService;

    // Objet Ticket utilisé dans chaque test pour simuler un ticket parking.
    private Ticket ticket;

    @BeforeAll
    public static void setUp() {
        // Méthode exécutée une fois avant tous les tests de cette classe.
        // Initialisation commune : une instance unique de FareCalculatorService.
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    public void setUpPerTest() {
        // Méthode exécutée avant chaque test.
        // Cela réinitialise un nouvel objet Ticket afin d'éviter tout impact entre les tests.
        ticket = new Ticket();
    }

    @Test
    public void calculateFareCar(){
        // Test le calcul des frais pour un véhicule de type CAR (voiture).
        // Cas testé : une voiture garée pendant une heure.
        // Prépare un ticket avec InTime et OutTime représentant une heure de stationnement.
        // Vérifie que le tarif calculé correspond au tarif attendu pour une heure de stationnement pour une voiture.
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR);
    }

    @Test
    public void calculateFareBike(){
        // Test le calcul des frais pour un véhicule de type BIKE (moto).
        // Cas testé : une moto garée pendant une heure.
        // Prépare un ticket avec les temps spécifiques et vérifie que le tarif est correct pour une moto.

        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR);
    }

    @Test
    public void calculateFareUnkownType(){
        // Test le comportement avec un véhicule de type inconnu.
        // Cas testé : lorsqu'un type de véhicule inconnu est utilisé, une exception IllegalArgumentException doit être levée.
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithFutureInTime(){
        // Test le comportement si l'heure d'entrée du ticket (InTime) est dans le futur par rapport à l'heure de sortie (OutTime).
        // Cas testé : une exception IllegalArgumentException doit être levée.
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() + (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime(){
        // Test le calcul du tarif pour une moto stationnée pendant moins d'une heure.
        // Cas testé : une moto garée pendant 45 minutes.
        // La méthode doit retourner un tarif proportionnel à la durée (par exemple, 75% du tarif standard).

        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice() );
    }

    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime(){
        // Test le calcul du tarif pour une voiture stationnée pendant moins d'une heure.
        // Cas testé : une voiture garée pendant 30 minutes.
        // La méthode doit retourner un tarif proportionnel à la durée (par exemple, 50% du tarif standard pour une demi-heure).

        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals( (0.75 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime(){
        // Test le calcul du tarif pour une voiture stationnée pendant plus de 24 heures.
        // Cas testé : une voiture garée pendant 24 heures et 30 minutes.
        // Vérifie si le tarif calculé est exact pour une durée prolongée (exemple : tarif pour 24 heures + demi-heure).

        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  24 * 60 * 60 * 1000) );//24 hours parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals( (24 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }

    // Vérifie que le stationnement est gratuit pour les durées inférieures à 30 minutes pour une voiture
    @Test
    public void calculateFareCareWithLessThan30minutesParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (20 * 60 * 1000)); // 20 minutes parking time
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);
        assertEquals(0, ticket.getPrice());
    }

    // Vérifie que le stationnement est gratuit pour les durées inférieures à 30 minutes pour une moto
    @Test
    public void calculateFareBikeWithLessThan30minutesParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (20 * 60 * 1000)); // 20 minutes parking time
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);
        assertEquals(0, ticket.getPrice());
    }
    //Vérifie si la durée du test inférieure à 30 minutes
    private boolean isParkingTimeTooShort(double duration) {
        return duration < 0.5;
    }

    @Test
    public void calculateFareCarWithDiscount() {
        // Test pour vérifier l'application d'une réduction de 5% pour un utilisateur récurrent avec une voiture.
        // Cas testé : une voiture garée pendant 1 heure, avec un ticket ayant l'attribut "discount" à true.
        // La méthode doit renvoyer un tarif représentant 95% du prix standard pour une heure de parking.

        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000)); // 1 heure
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // Appliquer une remise de 5 %
        fareCalculatorService.calculateFare(ticket, true);
        assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR * 0.95);
    }

    @Test
    public void calculateFareBikeWithDiscount() {
        // Test pour vérifier l'application d'une réduction de 5% pour un utilisateur récurrent avec une moto.
        // Cas testé : une moto garée pendant 1 heure, avec un ticket ayant l'attribut "discount" à true.
        // La méthode doit renvoyer un tarif correspondant à 95% du prix standard pour une heure de parking.

        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000)); // 1 heure
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // Applique une remise de 5 %
        fareCalculatorService.calculateFare(ticket, true);
        assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR * 0.95);
    }
}