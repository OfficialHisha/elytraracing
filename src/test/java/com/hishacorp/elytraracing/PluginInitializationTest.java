package com.hishacorp.elytraracing;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PluginInitializationTest {
    private ServerMock server;
    private Elytraracing plugin;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Elytraracing.class);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testPluginInitialization() {
        // Assert that the plugin is enabled
        assertTrue(plugin.isEnabled());

        // Assert that the managers are not null
        assertNotNull(plugin.getGuiManager());
        assertNotNull(plugin.getDatabaseManager());
        assertNotNull(plugin.getInputManager());
        assertNotNull(plugin.getRaceManager());
    }
}
