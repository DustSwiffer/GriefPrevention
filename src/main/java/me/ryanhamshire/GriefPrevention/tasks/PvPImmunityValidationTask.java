package me.ryanhamshire.GriefPrevention.tasks;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.constants.TextMode;
import me.ryanhamshire.GriefPrevention.enums.Messages;
import me.ryanhamshire.GriefPrevention.models.PlayerData;
import org.bukkit.entity.Player;

//sends a message to a player
//used to send delayed messages, for example help text triggered by a player's chat
public class PvPImmunityValidationTask implements Runnable
{
    private final Player player;

    public PvPImmunityValidationTask(Player player)
    {
        this.player = player;
    }

    @Override
    public void run()
    {
        if (!player.isOnline()) return;

        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
        if (!playerData.pvpImmune) return;

        //check the player's inventory for anything
        if (!GriefPrevention.isInventoryEmpty(player))
        {
            //if found, cancel invulnerability and notify
            playerData.pvpImmune = false;
            GriefPrevention.sendMessage(player, TextMode.Warn, Messages.PvPImmunityEnd);
        }
        else
        {
            //otherwise check again in one minute
            GriefPrevention.instance.getServer().getScheduler().scheduleSyncDelayedTask(GriefPrevention.instance, this, 1200L);
        }
    }
}
