package me.ryanhamshire.GriefPrevention.tasks;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.models.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

//sends a message to a player
//used to send delayed messages, for example help text triggered by a player's chat
public class SendPlayerMessageTask implements Runnable
{
    private final Player player;
    private final ChatColor color;
    private final String message;

    public SendPlayerMessageTask(Player player, ChatColor color, String message)
    {
        this.player = player;
        this.color = color;
        this.message = message;
    }

    @Override
    public void run()
    {
        if (player == null)
        {
            GriefPrevention.AddLogEntry(color + message);
            return;
        }

        //if the player is dead, save it for after his respawn
        if (this.player.isDead())
        {
            PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(this.player.getUniqueId());
            playerData.messageOnRespawn = this.color + this.message;
        }

        //otherwise send it immediately
        else
        {
            GriefPrevention.sendMessage(this.player, this.color, this.message);
        }
    }
}
