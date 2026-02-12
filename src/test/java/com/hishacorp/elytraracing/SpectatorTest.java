package com.hishacorp.elytraracing;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.*;

public class SpectatorTest {

    private ServerMock server;
    private Elytraracing plugin;
    private RaceManager raceManager;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Elytraracing.class);
        raceManager = plugin.getRaceManager();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testSpectateRace() {
        PlayerMock player = server.addPlayer();
        player.setOp(true);
        String raceName = "testRace";

        // Create race
        server.execute("er", player, "create", raceName);
        player.nextMessage(); // consume "Race 'testRace' created!"
        player.nextMessage(); // consume "You have been given the ring tool..."

        // Join as spectator
        server.execute("er", player, "spectate", raceName);

        assertTrue(raceManager.isPlayerInRace(player));
        Race race = raceManager.getRace(raceName).orElseThrow();
        assertTrue(race.getSpectators().containsKey(player.getUniqueId()));
        assertFalse(race.getRacers().containsKey(player.getUniqueId()));

        assertTrue(player.getAllowFlight());
        assertTrue(player.isFlying());

        // Check scoreboard
        assertNotSame(server.getScoreboardManager().getMainScoreboard(), player.getScoreboard());

        // Check invisibility (MockBukkit might not fully support hidePlayer but we can check if it's called if we use a mock,
        // but here we just check it doesn't crash)
    }

    @Test
    public void testLeaveSpectating() {
        PlayerMock player = server.addPlayer();
        player.setOp(true);
        String raceName = "testRace";

        server.execute("er", player, "create", raceName);
        player.nextMessage();
        player.nextMessage();

        server.execute("er", player, "spectate", raceName);
        assertTrue(raceManager.isPlayerInRace(player));

        server.execute("er", player, "leave");

        assertFalse(raceManager.isPlayerInRace(player));
        Race race = raceManager.getRace(raceName).orElseThrow();
        assertFalse(race.getSpectators().containsKey(player.getUniqueId()));

        assertFalse(player.getAllowFlight());
        assertFalse(player.isFlying());

        // Check scoreboard removed
        // Note: VanillaScoreboardProvider.removeScoreboard sets a NEW scoreboard, so it won't be the main one,
        // but it will be different from the one it had during the race.
        // Actually, we can just check if it's a fresh scoreboard with no objectives.
        assertTrue(player.getScoreboard().getObjectives().isEmpty());
    }

    @Test
    public void testSpectatorCannotJoinAsRacer() {
        PlayerMock player = server.addPlayer();
        player.setOp(true);
        String raceName = "testRace";

        server.execute("er", player, "create", raceName);
        player.nextMessage();
        player.nextMessage();

        server.execute("er", player, "spectate", raceName);
        player.nextMessage();

        server.execute("er", player, "join", raceName);
        player.assertSaid("§cYou are already in a race.");
    }

    @Test
    public void testAdminCanToggleSpectatorVisibility() {
        PlayerMock admin = server.addPlayer();
        assertFalse(raceManager.canSeeSpectators(admin));

        raceManager.toggleSeeSpectators(admin);
        assertTrue(raceManager.canSeeSpectators(admin));

        raceManager.toggleSeeSpectators(admin);
        assertFalse(raceManager.canSeeSpectators(admin));
    }

    @Test
    public void testSpectatorVisibilityRespectsAdminPreference() {
        PlayerMock admin = server.addPlayer();
        PlayerMock spectator = server.addPlayer();
        admin.setOp(true);
        spectator.setOp(true);
        String raceName = "testRace";

        server.execute("er", admin, "create", raceName);
        admin.nextMessage();
        admin.nextMessage();

        raceManager.setSeeSpectators(admin, true);

        server.execute("er", spectator, "spectate", raceName);

        assertTrue(admin.canSee(spectator));

        raceManager.setSeeSpectators(admin, false);
        assertFalse(admin.canSee(spectator));

        raceManager.setSeeSpectators(admin, true);
        assertTrue(admin.canSee(spectator));
    }
}
