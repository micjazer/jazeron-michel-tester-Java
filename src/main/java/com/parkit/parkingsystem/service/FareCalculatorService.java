package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        long inTimeMillis = ticket.getInTime().getTime();
        long outTimeMillis = ticket.getOutTime().getTime();
        double duration = (outTimeMillis - inTimeMillis) / (1000.0 * 60 * 60); // Convert to hours

        // Vérifiez si la durée du stationnement est inférieure à 30 minutes
        if (duration < 0.5) {
            ticket.setPrice(0);
            return;
        }

        double rate;
        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                rate = Fare.CAR_RATE_PER_HOUR;
                break;
            }
            case BIKE: {
                rate = Fare.BIKE_RATE_PER_HOUR;
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown Parking Type");
        }

        double price = duration * rate;
        if (discount) {
            price *= 0.95; // Apply 5% discount
        }
        ticket.setPrice(price);
    }

    // Méthode surchargée pour la compatibilité vers l'arrière
    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }
}
