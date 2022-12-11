package me.ryanhamshire.GriefPrevention.models;

import org.bukkit.entity.Player;

import java.util.ArrayList;

//information about an ongoing siege
public class SiegeData
{
    public Player defender;
    public Player attacker;
    public ArrayList<Claim> claims;
    public int checkupTaskID;

    public SiegeData(Player attacker, Player defender, Claim claim)
    {
        this.defender = defender;
        this.attacker = attacker;
        this.claims = new ArrayList<>();
        this.claims.add(claim);
    }
}
