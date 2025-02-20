package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class TicketDAO {

    private static final Logger logger = LogManager.getLogger("TicketDAO");

    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    /**
     * Sauvegarde un ticket dans la base de données.
     *
     * @param ticket Le ticket à sauvegarder, contenant les informations de stationnement.
     * @return true si le ticket a été sauvegardé avec succès, false sinon.
     */
    public boolean saveTicket(Ticket ticket){
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.SAVE_TICKET);
            // Remplissage des paramètres de la requête SQL pour insérer un ticket.
            ps.setInt(1, ticket.getParkingSpot().getId());
            ps.setString(2, ticket.getVehicleRegNumber());
            ps.setDouble(3, ticket.getPrice());
            ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
            ps.setTimestamp(5, (ticket.getOutTime() == null) ? null : (new Timestamp(ticket.getOutTime().getTime())));
            return ps.execute();
        } catch (Exception ex) {
            logger.error("Error saving ticket", ex);
        } finally {
            dataBaseConfig.closeConnection(con);
        }
        return false;
    }

    /**
     * Récupère un ticket de la base de données en fonction du numéro d'immatriculation du véhicule.
     *
     * @param vehicleRegNumber Le numéro d'immatriculation du véhicule.
     * @return Le ticket correspondant, ou null si aucun ticket n'est trouvé.
     */
    public Ticket getTicket(String vehicleRegNumber) {
        Connection con = null;
        Ticket ticket = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.GET_TICKET);
            // Remplissage du paramètre de la requête SQL pour récupérer un ticket.
            ps.setString(1, vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ticket = new Ticket();
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)), false);
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble(3));
                ticket.setInTime(rs.getTimestamp(4));
                ticket.setOutTime(rs.getTimestamp(5));
            }
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
        } catch (Exception ex) {
            logger.error("Error fetching ticket", ex);
        } finally {
            dataBaseConfig.closeConnection(con);
        }
        return ticket;
    }

    /**
     * Met à jour un ticket existant dans la base de données.
     *
     * @param ticket Le ticket à mettre à jour, contenant les nouvelles informations.
     * @return true si le ticket a été mis à jour avec succès, false sinon.
     */
    public boolean updateTicket(Ticket ticket) {
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
            // Remplissage des paramètres de la requête SQL pour mettre à jour un ticket.
            ps.setDouble(1, ticket.getPrice());
            ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
            ps.setInt(3, ticket.getId());
            ps.execute();
            return true;
        } catch (Exception ex) {
            logger.error("Error updating ticket", ex);
        } finally {
            dataBaseConfig.closeConnection(con);
        }
        return false;
    }
/*
    /**
     * Compte le nombre de tickets dans la base de données pour un numéro de plaque donné.
     *
     * @param vehicleRegNumber Le numéro de plaque du véhicule.
     * @return Le nombre de tickets associés à ce véhicule.
     */
/*    public int getNbTicket(String vehicleRegNumber) {
        Connection con = null;
        int ticketCount = 0;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.COUNT_TICKETS);
            // Remplissage du paramètre de la requête SQL pour compter les tickets.
            ps.setString(1, vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ticketCount = rs.getInt(1);
            }
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
        } catch (Exception ex) {
            logger.error("Error counting tickets for vehicle registration number: " + vehicleRegNumber, ex);
        } finally {
            dataBaseConfig.closeConnection(con);
        }
        return ticketCount;
  */
public int getNbTicket(String vehicleRegNumber) {
    int result = 0;
    try (Connection con = dataBaseConfig.getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM ticket WHERE VEHICLE_REG_NUMBER = ?")) {
        ps.setString(1, vehicleRegNumber);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                result = rs.getInt(1);
            }
        }
    } catch (Exception ex) {
        logger.error("Error fetching ticket count", ex);
    }
    return result;
    }
}



