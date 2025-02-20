package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ParkingSpotDAO {
    private static final Logger logger = LogManager.getLogger("ParkingSpotDAO");

    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    /**
     * Récupère le prochain emplacement de stationnement disponible pour un type de véhicule donné.
     *
     * @param parkingType Le type de stationnement (voiture ou moto).
     * @return L'identifiant du prochain emplacement de stationnement disponible, ou -1 si aucun n'est disponible.
     */
    public int getNextAvailableSlot(ParkingType parkingType) {
        Connection con = null;
        int result = -1;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.GET_NEXT_PARKING_SPOT);
            // Remplissage du paramètre de la requête SQL pour obtenir le prochain emplacement disponible.
            ps.setString(1, parkingType.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getInt(1);
            }
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
        } catch (Exception ex) {
            logger.error("Error fetching next available slot", ex);
        } finally {
            dataBaseConfig.closeConnection(con);
        }
        return result;
    }

    /**
     * Met à jour la disponibilité d'un emplacement de stationnement.
     *
     * @param parkingSpot L'objet ParkingSpot contenant les informations de l'emplacement à mettre à jour.
     * @return true si la mise à jour a réussi, false sinon.
     */
    public boolean updateParking(ParkingSpot parkingSpot) {
        // Met à jour la disponibilité de cet emplacement de stationnement.
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_PARKING_SPOT);
            // Remplissage des paramètres de la requête SQL pour mettre à jour un emplacement de stationnement.
            ps.setBoolean(1, parkingSpot.isAvailable());
            ps.setInt(2, parkingSpot.getId());
            int updateRowCount = ps.executeUpdate();
            dataBaseConfig.closePreparedStatement(ps);
            return (updateRowCount == 1);
        } catch (Exception ex) {
            logger.error("Error updating parking info", ex);
            return false;
        } finally {
            dataBaseConfig.closeConnection(con);
        }
    }
}