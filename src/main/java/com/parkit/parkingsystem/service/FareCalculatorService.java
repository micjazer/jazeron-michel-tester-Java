package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime());
        }

        // Conversion en millisecondes pour gérer les cas longue durée
        long inTimeMillis = ticket.getInTime().getTime();
        long outTimeMillis = ticket.getOutTime().getTime();

        // Durée en minutes
        long durationMinutes = (outTimeMillis - inTimeMillis) / (1000 * 60); // 1000ms = 1s, 60s = 1min

        // Calcul du tarif (exemple : voiture ou moto)
        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice((durationMinutes / 60.0) * Fare.CAR_RATE_PER_HOUR);
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