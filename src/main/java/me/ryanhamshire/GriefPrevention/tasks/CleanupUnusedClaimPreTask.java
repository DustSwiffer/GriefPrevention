package me.ryanhamshire.GriefPrevention.tasks;

import me.ryanhamshire.GriefPrevention.enums.CustomLogEntryTypes;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.models.Claim;
import me.ryanhamshire.GriefPrevention.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

//asynchronously loads player data without caching it in the datastore, then
//passes those data to a claim cleanup task which might decide to delete a claim for inactivity

public class CleanupUnusedClaimPreTask implements Runnable
{
    private final UUID ownerID;

    public CleanupUnusedClaimPreTask(UUID uuid)
    {
        this.ownerID = uuid;
    }

    @Override
    public void run()
    {
        //get the data
        PlayerData ownerData = GriefPrevention.instance.dataStore.getPlayerDataFromStorage(ownerID);
        OfflinePlayer ownerInfo = Bukkit.getServer().getOfflinePlayer(ownerID);

        GriefPrevention.AddLogEntry("Looking for expired claims.  Checking data for " + ownerID, CustomLogEntryTypes.Debug, true);

        //expiration code uses last logout timestamp to decide whether to expire claims
        //don't expire claims for online players
        if (ownerInfo.isOnline())
        {
            GriefPrevention.AddLogEntry("Player is online. Ignoring.", CustomLogEntryTypes.Debug, true);
            return;
        }
        if (ownerInfo.getLastPlayed() <= 0)
        {
            GriefPrevention.AddLogEntry("Player is new or not in the server's cached userdata. Ignoring. getLastPlayed = " + ownerInfo.getLastPlayed(), CustomLogEntryTypes.Debug, true);
            return;
        }

        //skip claims belonging to exempted players based on block totals in config
        int bonusBlocks = ownerData.getBonusClaimBlocks();
        if (bonusBlocks >= GriefPrevention.instance.config_claims_expirationExemptionBonusBlocks || bonusBlocks + ownerData.getAccruedClaimBlocks() >= GriefPrevention.instance.config_claims_expirationExemptionTotalBlocks)
        {
            GriefPrevention.AddLogEntry("Player exempt from claim expiration based on claim block counts vs. config file settings.", CustomLogEntryTypes.Debug, true);
            return;
        }

        Claim claimToExpire = null;

        for (Claim claim : GriefPrevention.instance.dataStore.getClaims())
        {
            if (ownerID.equals(claim.ownerID))
            {
                claimToExpire = claim;
                break;
            }
        }

        if (claimToExpire == null)
        {
            GriefPrevention.AddLogEntry("Unable to find a claim to expire for " + ownerID, CustomLogEntryTypes.Debug, false);
            return;
        }

        //pass it back to the main server thread, where it's safe to delete a claim if needed
        Bukkit.getScheduler().scheduleSyncDelayedTask(GriefPrevention.instance, new CleanupUnusedClaimTask(claimToExpire, ownerData, ownerInfo), 1L);
    }
}