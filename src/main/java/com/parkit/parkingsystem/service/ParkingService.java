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

    private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

    private InputReaderUtil inputReaderUtil;
    private ParkingSpotDAO parkingSpotDAO;
    private TicketDAO ticketDAO;

    public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO) {
        this.inputReaderUtil = inputReaderUtil;
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;
    }

    public void processIncomingVehicle() {
        try {
            ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
            if (parkingSpot != null && parkingSpot.getId() > 0) {
                String vehicleRegNumber = getVehichleRegNumber();
                parkingSpot.setAvailable(false);
                parkingSpotDAO.updateParking(parkingSpot); // Marquer la place comme occupée

                // Création d'un ticket
                Date inTime = new Date();
                Ticket ticket = new Ticket();
                ticket.setParkingSpot(parkingSpot);
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(0);
                ticket.setInTime(inTime);
                ticket.setOutTime(null);

                ticketDAO.saveTicket(ticket);

                // Vérification d'utilisateur récurrent
                int previousEntries = ticketDAO.getNbTicket(vehicleRegNumber);
                if (previousEntries > 1) { // L'utilisateur est récurrent s'il a déjà un ticket
                    System.out.println("Heureux de vous revoir ! En tant qu’utilisateur régulier de notre parking, vous allez obtenir une remise de 5%.");
                }

                System.out.println("Veuillez vous garer sur la place numéro:" + parkingSpot.getId());
                System.out.println("Notez bien votre numéro de place.");
            }
        } catch (Exception e) {
            logger.error("Impossible de traiter l'entrée du véhicule", e);
        }
    }

    private String getVehichleRegNumber() throws Exception {
        System.out.println("Veuillez saisir votre numéro de plaque d’immatriculation et appuyez sur Entrée:");
        return inputReaderUtil.readVehicleRegistrationNumber();
    }

    public ParkingSpot getNextParkingNumberIfAvailable() {
        int parkingNumber;
        ParkingSpot parkingSpot = null;
        try {
            ParkingType parkingType = getVehichleType();
            parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
            if (parkingNumber > 0) {
                parkingSpot = new ParkingSpot(parkingNumber, parkingType, true);
            } else {
                throw new Exception("Aucune place de parking disponible pour le type de véhicule spécifié: " + parkingType);
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de la prochaine place de parking disponible", e);
        }
        return parkingSpot;
    }

    private ParkingType getVehichleType() {
        System.out.println("Veuillez sélectionner le type de véhicule");
        System.out.println("1 CAR");
        System.out.println("2 BIKE");
        int input = 0;
        try {
            input = inputReaderUtil.readSelection();
        } catch (Exception e) {
            logger.error("Erreur de lecture de la sélection de type de véhicule", e);
        }
        switch (input) {
            case 1:
                return ParkingType.CAR;
            case 2:
                return ParkingType.BIKE;
            default:
                System.out.println("Sélection invalide de type de véhicule. Veuillez entrer 1 pour voiture et 2 pour moto.");
                throw new IllegalArgumentException("Entrée invalide pour le type de véhicule");
        }
    }

    public void processExitingVehicle() {
        try {
            String vehicleRegNumber = getVehichleRegNumber();
            Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
            Date outTime = new Date();
            ticket.setOutTime(outTime);

            // Vérifie si l'utilisateur est récurrent
            int previousEntries = ticketDAO.getNbTicket(vehicleRegNumber);
            boolean discount = previousEntries > 1;

            // Calcule les frais avec remise si éligible
            fareCalculatorService.calculateFare(ticket, discount);

            if (ticketDAO.updateTicket(ticket)) {
                ParkingSpot parkingSpot = ticket.getParkingSpot();
                parkingSpot.setAvailable(true);
                parkingSpotDAO.updateParking(parkingSpot); // Libère la place de parking
                System.out.println("Merci de votre visite.");
                System.out.println("Veuillez payer le montant de: " + ticket.getPrice());
                System.out.println("Place de parking libérée!");
            } else {
                System.out.println("Impossible de mettre à jour les informations sur le ticket. Veuillez contacter un administrateur.");
            }
        } catch (Exception e) {
            logger.error("Impossible de traiter la sortie du véhicule", e);
        }
    }
}
