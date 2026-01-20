package com.hishacorp.elytraracing;

import com.hishacorp.elytraracing.scoreboard.ScoreboardManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

@ExtendWith(MockitoExtension.class)
public class ERCommandTest {

    private ServerMock server;
    private Elytraracing plugin;
    private PlayerMock player;

    @Mock
    private ScoreboardManager scoreboardManager;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Elytraracing.class);
        plugin.setScoreboardManager(scoreboardManager);
        player = server.addPlayer();
        player.setOp(true);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testCreateRaceCommand() {
        player.performCommand("er create test_race");
        assertTrue(plugin.getDatabaseManager().raceExists("test_race"));
    }

    @Test
    public void testDeleteRaceCommand() {
        player.performCommand("er create test_race");
        player.performCommand("er delete test_race");
        assertFalse(plugin.getDatabaseManager().raceExists("test_race"));
    }

    @Test
    public void testJoinRaceCommand() {
        player.performCommand("er join test_race");
        verify(scoreboardManager).showScoreboard(player);
    }

    @Test
    public void testLeaveRaceCommand() {
        player.performCommand("er leave");
        verify(scoreboardManager).removeScoreboard(player);
    }
}
