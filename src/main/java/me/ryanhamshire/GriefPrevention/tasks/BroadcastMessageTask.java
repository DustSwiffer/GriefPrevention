package me.ryanhamshire.GriefPrevention.tasks;

import org.bukkit.Bukkit;

//sends a message to all online players
//used to send delayed messages, for example a quit message after the player has been gone a while 
public class BroadcastMessageTask implements Runnable
{
    private final String message;

    public BroadcastMessageTask(String message)
    {
        this.message = message;
    }

    @Override
    public void run()
    {
        Bukkit.getServer().broadcastMessage(this.message);
    }
}
