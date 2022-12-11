package me.ryanhamshire.GriefPrevention.tasks;

import me.ryanhamshire.GriefPrevention.enums.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.models.SiegeData;
import me.ryanhamshire.GriefPrevention.constants.TextMode;
import me.ryanhamshire.GriefPrevention.enums.Messages;
import me.ryanhamshire.GriefPrevention.models.Claim;
import org.bukkit.entity.Player;

import java.util.Collection;

//secures a claim after a siege looting window has closed
public class SecureClaimTask implements Runnable
{
    private final SiegeData siegeData;

    public SecureClaimTask(SiegeData siegeData)
    {
        this.siegeData = siegeData;
    }

    @Override
    public void run()
    {
        //for each claim involved in this siege
        for (int i = 0; i < this.siegeData.claims.size(); i++)
        {
            //lock the doors
            Claim claim = this.siegeData.claims.get(i);
            claim.doorsOpen = false;

            //eject bad guys
            Collection<? extends Player> onlinePlayers = GriefPrevention.instance.getServer().getOnlinePlayers();
            for (Player player : onlinePlayers)
            {
                if (claim.contains(player.getLocation(), false, false) && claim.checkPermission(player, ClaimPermission.Access, null) != null)
                {
                    GriefPrevention.sendMessage(player, TextMode.Err, Messages.SiegeDoorsLockedEjection);
                    GriefPrevention.instance.ejectPlayer(player);
                }
            }
        }
    }
}
