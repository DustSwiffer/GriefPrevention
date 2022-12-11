package me.ryanhamshire.GriefPrevention.tasks;

import me.ryanhamshire.GriefPrevention.enums.CustomLogEntryTypes;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

//players can be "trapped" in a portal frame if they don't have permission to break
//solid blocks blocking them from exiting the frame
//if that happens, we detect the problem and send them back through the portal.
public class CheckForPortalTrapTask extends BukkitRunnable
{
    GriefPrevention instance;
    //player who recently teleported via nether portal
    private final Player player;

    //where to send the player back to if he hasn't left the portal frame
    private final Location returnLocation;

    public CheckForPortalTrapTask(Player player, GriefPrevention plugin, Location locationToReturn)
    {
        this.player = player;
        this.instance = plugin;
        this.returnLocation = locationToReturn;
        player.setMetadata("GP_PORTALRESCUE", new FixedMetadataValue(instance, locationToReturn));
    }

    @Override
    public void run()
    {
        if (player.isOnline() && player.getPortalCooldown() >= 10 && player.hasMetadata("GP_PORTALRESCUE"))
        {
            GriefPrevention.AddLogEntry("Rescued " + player.getName() + " from a nether portal.\nTeleported from " + player.getLocation() + " to " + returnLocation.toString(), CustomLogEntryTypes.Debug);
            player.teleport(returnLocation);
            player.removeMetadata("GP_PORTALRESCUE", instance);
        }
        instance.portalReturnTaskMap.remove(player.getUniqueId());
    }
}
