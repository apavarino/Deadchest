package me.crylonz.deadchest.scheduler;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SchedulerAdapterTest {
    private ServerMock server;

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void mockBukkitRuntimeStaysClassic() {
        server = MockBukkit.mock();
        Plugin plugin = MockBukkit.createMockPlugin();

        SchedulerAdapter schedulerAdapter = new SchedulerAdapter(plugin);

        assertFalse(schedulerAdapter.isFoliaLikeRuntime());
        assertFalse(schedulerAdapter.supportsRegionSchedulers());
    }

    @Test
    public void nullServerStaysClassic() {
        Plugin plugin = mock(Plugin.class);
        when(plugin.getServer()).thenReturn(null);

        assertFalse(FoliaDetection.isFolia(plugin));
    }
}
