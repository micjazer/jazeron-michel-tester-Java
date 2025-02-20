package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitaires de la classe ParkingService avec Mocks.
 * Couverture attendue : Supérieure à 90% pour la classe ParkingService.
 */
@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    @Mock
    private InputReaderUtil inputReaderUtil;

    @Mock
    private ParkingSpotDAO parkingSpotDAO;

    @Mock
    private TicketDAO ticketDAO;

    @InjectMocks
    private ParkingService parkingService;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private PrintStream originalOut;

    /**
     * Méthode exécutée avant chaque test pour rediriger la sortie standard.
     * Cela permet de capturer les messages de sortie produits pendant les tests.
     */
    @BeforeEach
    public void setUpStreams() {
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
    }

    /**
     * Méthode exécutée après chaque test pour restaurer la sortie standard.
     * Cela garantit que les tests suivants commencent avec un état propre.
     */
    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        outContent.reset();
    }

    /**
     * Test 1 : Vérifie le déroulement normal de processIncomingVehicle().
     * Simule l'entrée d'un véhicule et vérifie que le ticket est sauvegardé
     * et que la place de parking est mise à jour comme non disponible.
     */
    @Test
    public void testProcessIncomingVehicle() throws Exception {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(0);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        parkingService.processIncomingVehicle();

        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
        verify(parkingSpotDAO, times(1))
            .updateParking(argThat(spot -> spot.getId() == 1 && !spot.isAvailable()));
        String output = outContent.toString();
        assertTrue(output.contains("Votre place de parking est réservée"));
    }

    /**
     * Test 2 : Vérifie le comportement de processExitingVehicle() lorsque la mise à jour du ticket échoue.
     * Simule la sortie d'un véhicule et s'assure que le message "Merci de votre visite" est affiché.
     */
    @Test
    public void processExitingVehicleTestUnableUpdate() throws Exception {
        String vehicleReg = "ABCDEF";
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(vehicleReg);

        Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber(vehicleReg);
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ParkingSpot spot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket.setParkingSpot(spot);

        when(ticketDAO.getTicket(vehicleReg)).thenReturn(ticket);
        when(ticketDAO.getNbTicket(vehicleReg)).thenReturn(1);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        parkingService.processExitingVehicle();

        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
        String output = outContent.toString();
        assertTrue(output.contains("Merci de votre visite"));
    }

    /**
     * Test 3 : Vérifie que getNextParkingNumberIfAvailable retourne un spot disponible (id = 1).
     * S'assure également que le message "Vérification des places disponibles" est affiché.
     */
    @Test
    public void testGetNextParkingNumberIfAvailable() {
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        Object result = parkingService.getNextParkingNumberIfAvailable();

        assertNotNull(result);
        assertEquals(1, result);
        String output = outContent.toString();
        assertTrue(output.contains("Vérification des places disponibles"));
    }

    /**
     * Test 4 : Vérifie que getNextParkingNumberIfAvailable retourne null quand aucune place n'est disponible.
     * Vérifie également que le message "Aucune place disponible." est affiché.
     */
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(-1);

        Object result = parkingService.getNextParkingNumberIfAvailable();

        assertNull(result);
        String output = outContent.toString();
        assertTrue(output.contains("Aucune place disponible."));
    }

    /**
     * Test 5 : Vérifie que getNextParkingNumberIfAvailable renvoie null en cas d'argument invalide.
     * Simule une exception IllegalArgumentException pour tester la robustesse de la méthode.
     */
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR))
            .thenThrow(new IllegalArgumentException("Invalid parking type"));

        Object result = parkingService.getNextParkingNumberIfAvailable();

        assertNull(result);
    }
}
