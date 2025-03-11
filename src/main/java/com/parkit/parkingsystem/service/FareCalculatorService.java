package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime());
        }

        // Temps passé au parking en millisecondes
        long inTimeMillis = ticket.getInTime().getTime();
        long outTimeMillis = ticket.getOutTime().getTime();
        long durationMinutes = (outTimeMillis - inTimeMillis) / (1000 * 60); // Convertit en minutes

        // Parking gratuit pour moins de 30 minutes
        if (durationMinutes < 30) {
            ticket.setPrice(0);
            return;
        }

        // Calcul du tarif
        double price = 0;
        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                price = (durationMinutes / 60.0) * Fare.CAR_RATE_PER_HOUR;
                break;
            }
            case BIKE: {
                price = (durationMinutes / 60.0) * Fare.BIKE_RATE_PER_HOUR;
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown Parking Type");
        }

        // Applique la remise si applicable
        if (discount) {
            price *= 0.95; // Réduction de 5 %
        }

        ticket.setPrice(price);
    }
    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false); // Par défaut : aucune réduction
    }
}