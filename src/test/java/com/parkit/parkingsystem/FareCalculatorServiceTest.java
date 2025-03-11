package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }

    @Test
    public void calculateFareCar(){
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
    @Test
    void calculateFareCarWithLessThan30minutesParkingTime() {
        // Arrange : Création d'un ticket pour une durée inférieure à 30 minutes
        Date inTime = new Date();
        Date outTime = new Date(inTime.getTime() + (29 * 60 * 1000)); // 29 minutes plus tard
        Ticket ticket = new Ticket();
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));

        // Act : Calcul du tarif
        fareCalculatorService.calculateFare(ticket);

        // Assert : Vérifie que le tarif est gratuit (0)
        assertEquals(0, ticket.getPrice(), "Le parking doit être gratuit pour moins de 30 minutes pour une voiture");
    }
    @Test
    void calculateFareBikeWithLessThan30minutesParkingTime() {
        // Arrange : Création d'un ticket pour une durée inférieure à 30 minutes
        Date inTime = new Date();
        Date outTime = new Date(inTime.getTime() + (29 * 60 * 1000)); // 29 minutes plus tard
        Ticket ticket = new Ticket();
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.BIKE, false));

        // Act : Calcul du tarif
        fareCalculatorService.calculateFare(ticket);

        // Assert : Vérifie que le tarif est gratuit (0)
        assertEquals(0, ticket.getPrice(), "Le parking doit être gratuit pour moins de 30 minutes pour une moto");
    }
    @Test
    void calculateFareCarWithExactly30MinutesParkingTime() {
        // Arrange : durée exacte de 30 minutes
        Date inTime = new Date();
        Date outTime = new Date(inTime.getTime() + (30 * 60 * 1000)); // Exactement 30 minutes
        Ticket ticket = new Ticket();
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));

        // Act : Calcul du tarif
        fareCalculatorService.calculateFare(ticket);

        // Assert : Parking non gratuit (prix > 0)
        assertTrue(ticket.getPrice() > 0, "Le parking ne doit pas être gratuit pour exactement 30 minutes");
    }
    @Test
    void calculateFareCarWithDiscount() {
        // Arrange: création d'un ticket pour plus de 30 minutes (ex: 1 heure)
        Date inTime = new Date(System.currentTimeMillis() - (60 * 60 * 1000)); // 1h avant
        Date outTime = new Date();
        Ticket ticket = new Ticket();
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));

        // Act: Calcul du tarif avec le paramètre discount = true
        fareCalculatorService.calculateFare(ticket, true);

        // Assert: Vérifier que le prix est 95% du tarif plein (1h * tarif voiture)
        double expectedPrice = Fare.CAR_RATE_PER_HOUR * 0.95; // 5% de remise
        assertEquals(expectedPrice, ticket.getPrice(), 0.01, "Le tarif pour une voiture avec réduction doit être correct");
    }
    @Test
    void calculateFareBikeWithDiscount() {
        // Arrange: création d'un ticket pour plus de 30 minutes (ex: 1 heure)
        Date inTime = new Date(System.currentTimeMillis() - (60 * 60 * 1000)); // 1h avant
        Date outTime = new Date();
        Ticket ticket = new Ticket();
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.BIKE, false));

        // Act: Calcul du tarif avec le paramètre discount = true
        fareCalculatorService.calculateFare(ticket, true);

        // Assert: Vérifier que le prix est 95% du tarif plein (1h * tarif moto)
        double expectedPrice = Fare.BIKE_RATE_PER_HOUR * 0.95; // 5% de remise
        assertEquals(expectedPrice, ticket.getPrice(), 0.01, "Le tarif pour une moto avec réduction doit être correct");
    }

}
