package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime());
        }

        // Temps passé au parking en millisecondes
        long inTimeMillis = ticket.getInTime().getTime();
        long outTimeMillis = ticket.getOutTime().getTime();
        long durationMinutes = (outTimeMillis - inTimeMillis) / (1000 * 60); // Convertit en minutes

        // Gratuité pour les durées inférieures à 30 minutes
        if (durationMinutes < 30) {
            ticket.setPrice(0);
            return; // Quitte la méthode, car aucun calcul supplémentaire n'est nécessaire
        }

        // Calcul tarifaire habituel
        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice((durationMinutes / 60.0) * Fare.CAR_RATE_PER_HOUR); // En heures
                break;
            }
            case BIKE: {
                ticket.setPrice((durationMinutes / 60.0) * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown Parking Type");
        }
    }
}