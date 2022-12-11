package me.ryanhamshire.GriefPrevention.tasks;

import me.ryanhamshire.GriefPrevention.Visualization;
import me.ryanhamshire.GriefPrevention.events.VisualizationEvent;
import me.ryanhamshire.GriefPrevention.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;

//applies a visualization for a player by sending him block change packets
public class VisualizationReversionTask implements Runnable
{
    private final Visualization visualization;
    private final Player player;
    private final PlayerData playerData;

    public VisualizationReversionTask(Player player, PlayerData playerData, Visualization visualization)
    {
        this.visualization = visualization;
        this.playerData = playerData;
        this.player = player;
    }

    @Override
    public void run()
    {
        //don't do anything if the player's current visualization is different from the one scheduled to revert
        if (playerData.currentVisualization != visualization) return;

        // alert plugins of a visualization
        Bukkit.getPluginManager().callEvent(new VisualizationEvent(player, null, Collections.emptySet()));

        Visualization.Revert(player);
    }
}
