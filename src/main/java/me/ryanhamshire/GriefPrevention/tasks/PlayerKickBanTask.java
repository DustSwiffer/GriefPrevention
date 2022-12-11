package me.ryanhamshire.GriefPrevention.tasks;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.events.PlayerKickBanEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

//kicks or bans a player
//need a task for this because async threads (like the chat event handlers) can't kick or ban.
//but they CAN schedule a task to run in the main thread to do that job
public class PlayerKickBanTask implements Runnable
{
    //player to kick or ban
    private final Player player;

    //message to send player.
    private final String reason;

    //source of ban
    private final String source;

    //whether to ban
    private final boolean ban;

    public PlayerKickBanTask(Player player, String reason, String source, boolean ban)
    {
        this.player = player;
        this.reason = reason;
        this.source = source;
        this.ban = ban;
    }

    @Override
    public void run()
    {
        PlayerKickBanEvent kickBanEvent = new PlayerKickBanEvent(player, reason, source, ban);
        Bukkit.getPluginManager().callEvent(kickBanEvent);

        if (kickBanEvent.isCancelled())
        {
            return; // cancelled by a plugin
        }

        if (this.ban)
        {
            //ban
            GriefPrevention.banPlayer(this.player, this.reason, this.source);
        }
        else if (this.player.isOnline())
        {
            this.player.kickPlayer(this.reason);
        }
    }
}
