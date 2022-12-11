package me.ryanhamshire.GriefPrevention.tasks;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Visualization;
import me.ryanhamshire.GriefPrevention.models.PlayerData;
import me.ryanhamshire.GriefPrevention.models.VisualizationElement;
import org.bukkit.entity.Player;

//applies a visualization for a player by sending him block change packets
public class VisualizationApplicationTask implements Runnable
{
    private final Visualization visualization;
    private final Player player;
    private final PlayerData playerData;

    public VisualizationApplicationTask(Player player, PlayerData playerData, Visualization visualization)
    {
        this.visualization = visualization;
        this.playerData = playerData;
        this.player = player;
    }


    @Override
    public void run()
    {
        //for each element (=block) of the visualization
        for (int i = 0; i < visualization.elements.size(); i++)
        {
            VisualizationElement element = visualization.elements.get(i);

            //send the player a fake block change event
            if (!element.location.getChunk().isLoaded()) continue;  //cheap distance check
            player.sendBlockChange(element.location, element.visualizedBlock);
        }

        //remember the visualization applied to this player for later (so it can be inexpensively reverted)
        playerData.currentVisualization = visualization;

        //schedule automatic visualization reversion in 60 seconds.
        GriefPrevention.instance.getServer().getScheduler().scheduleSyncDelayedTask(
                GriefPrevention.instance,
                new VisualizationReversionTask(player, playerData, visualization),
                20L * 60);  //60 seconds
    }
}
