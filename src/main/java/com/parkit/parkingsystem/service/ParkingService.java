package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

public class ParkingService {

    private static final Logger logger = LogManager.getLogger("ParkingService");
    private static final FareCalculatorService fareCalculatorService = new FareCalculatorService();

    private final InputReaderUtil inputReaderUtil;
    private final ParkingSpotDAO parkingSpotDAO;
    private final TicketDAO ticketDAO;

    public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO) {
        this.inputReaderUtil = inputReaderUtil;
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;
    }

    public void processIncomingVehicle() {
        try {
            String vehicleRegNumber = getValidVehicleRegNumber();

            // Vérification du statut d'utilisateur récurrent
            boolean isRecurringUser = ticketDAO.getNbTicket(vehicleRegNumber) > 0;
            if (isRecurringUser) {
                System.out.println("Heureux de vous revoir ! En tant qu’utilisateur régulier de notre parking, vous allez obtenir une remise de 5%.");
            }

            ParkingType parkingType = getVehichleType();
            int parkingSpotId = parkingSpotDAO.getNextAvailableSlot(parkingType);
            ParkingSpot parkingSpot = new ParkingSpot(parkingSpotId, parkingType, true);
            parkingSpot.setAvailable(false);
            parkingSpotDAO.updateParking(parkingSpot);

            Ticket ticket = new Ticket();
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber(vehicleRegNumber);
            ticket.setPrice(0);
            ticket.setInTime(new Date());
            ticketDAO.saveTicket(ticket);

            System.out.println("Votre place de parking est réservée. Veuillez stationner votre véhicule.");
            System.out.println("Place allouée : " + parkingSpot.getId());
        } catch (Exception e) {
            logger.error("Impossible de traiter l'entrée du véhicule", e);
            System.out.println("Une erreur est survenue. Veuillez réessayer.");
        }
    }

    private String getValidVehicleRegNumber() throws Exception {
        String vehicleRegNumber = null;
        do {
            try {
                System.out.println("Veuillez saisir votre numéro d'immatriculation :");
                vehicleRegNumber = inputReaderUtil.readVehicleRegistrationNumber();
                if (vehicleRegNumber == null || vehicleRegNumber.trim().isEmpty()) {
                    throw new IllegalArgumentException("Le numéro d'immatriculation est vide. Veuillez réessayer.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Entrée invalide : " + e.getMessage());
            }
        } while (vehicleRegNumber == null || vehicleRegNumber.trim().isEmpty());
        return vehicleRegNumber;
    }

    private ParkingType getVehichleType() {
        System.out.println("Veuillez sélectionner le type de véhicule :");
        System.out.println("1 - VOITURE");
        System.out.println("2 - MOTO");
        int input = inputReaderUtil.readSelection();
        switch (input) {
            case 1:
                return ParkingType.CAR;
            case 2:
                return ParkingType.BIKE;
            default:
                System.out.println("Entrée incorrecte. Veuillez réessayer.");
                throw new IllegalArgumentException("Type de véhicule invalide.");
        }
    }

    public Object getNextParkingNumberIfAvailable() {
        try {
            System.out.println("Vérification des places disponibles...");
            int parkingSpot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
            if (parkingSpot == -1) {
                System.out.println("Aucune place disponible.");
                return null;
            }
            return parkingSpot;
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche d'une place disponible", e);
            return null;
        }
    }

    public void processExitingVehicle() {
        try {
            String vehicleRegNumber = getValidVehicleRegNumber();
            Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
            if (ticket == null) {
                System.out.println("Erreur : pas de ticket en cours pour ce véhicule.");
                return;
            }
            ticket.setOutTime(new Date());

            // Détermination du statut récurrent via getNbTicket()
            boolean isRecurringUser = ticketDAO.getNbTicket(vehicleRegNumber) > 0;
            fareCalculatorService.calculateFare(ticket, isRecurringUser);
            ticketDAO.updateTicket(ticket);

            ParkingSpot parkingSpot = ticket.getParkingSpot();
            parkingSpot.setAvailable(true);
            parkingSpotDAO.updateParking(parkingSpot);
            System.out.printf("Merci de votre visite. Le tarif à régler est de %.2f€.\n", ticket.getPrice());
        } catch (Exception e) {
            logger.error("Erreur lors du traitement de la sortie du véhicule", e);
            System.out.println("Une erreur est survenue lors de la sortie du véhicule.");
        }
    }
}
