package me.ryanhamshire.GriefPrevention.tasks;

import me.ryanhamshire.GriefPrevention.enums.CustomLogEntryTypes;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.events.ClaimExpirationEvent;
import me.ryanhamshire.GriefPrevention.models.Claim;
import me.ryanhamshire.GriefPrevention.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

public class CleanupUnusedClaimTask implements Runnable
{
    Claim claim;
    PlayerData ownerData;
    OfflinePlayer ownerInfo;

    public CleanupUnusedClaimTask(Claim claim, PlayerData ownerData, OfflinePlayer ownerInfo)
    {
        this.claim = claim;
        this.ownerData = ownerData;
        this.ownerInfo = ownerInfo;
    }

    @Override
    public void run()
    {


        //determine area of the default chest claim
        int areaOfDefaultClaim = 0;
        if (GriefPrevention.instance.config_claims_automaticClaimsForNewPlayersRadius >= 0)
        {
            areaOfDefaultClaim = (int) Math.pow(GriefPrevention.instance.config_claims_automaticClaimsForNewPlayersRadius * 2 + 1, 2);
        }

        //if this claim is a chest claim and those are set to expire
        if (claim.getArea() <= areaOfDefaultClaim && GriefPrevention.instance.config_claims_chestClaimExpirationDays > 0)
        {
            //if the owner has been gone at least a week, and if he has ONLY the new player claim, it will be removed
            Calendar sevenDaysAgo = Calendar.getInstance();
            sevenDaysAgo.add(Calendar.DATE, -GriefPrevention.instance.config_claims_chestClaimExpirationDays);
            boolean newPlayerClaimsExpired = sevenDaysAgo.getTime().after(new Date(ownerInfo.getLastPlayed()));
            if (newPlayerClaimsExpired && ownerData.getClaims().size() == 1)
            {
                if (expireEventCanceled())
                    return;
                claim.removeSurfaceFluids(null);
                GriefPrevention.instance.dataStore.deleteClaim(claim, true, true);

                //if configured to do so, restore the land to natural
                if (GriefPrevention.instance.creativeRulesApply(claim.getLesserBoundaryCorner()) || GriefPrevention.instance.config_claims_survivalAutoNatureRestoration)
                {
                    GriefPrevention.instance.restoreClaim(claim, 0);
                }

                GriefPrevention.AddLogEntry(" " + claim.getOwnerName() + "'s new player claim expired.", CustomLogEntryTypes.AdminActivity);
            }
        }

        //if configured to always remove claims after some inactivity period without exceptions...
        else if (GriefPrevention.instance.config_claims_expirationDays > 0)
        {
            Calendar earliestPermissibleLastLogin = Calendar.getInstance();
            earliestPermissibleLastLogin.add(Calendar.DATE, -GriefPrevention.instance.config_claims_expirationDays);

            if (earliestPermissibleLastLogin.getTime().after(new Date(ownerInfo.getLastPlayed())))
            {
                if (expireEventCanceled())
                    return;
                //make a copy of this player's claim list
                Vector<Claim> claims = new Vector<>(ownerData.getClaims());

                //delete them
                GriefPrevention.instance.dataStore.deleteClaimsForPlayer(claim.ownerID, true);
                GriefPrevention.AddLogEntry(" All of " + claim.getOwnerName() + "'s claims have expired.", CustomLogEntryTypes.AdminActivity);
                GriefPrevention.AddLogEntry("earliestPermissibleLastLogin#getTime: " + earliestPermissibleLastLogin.getTime(), CustomLogEntryTypes.Debug, true);
                GriefPrevention.AddLogEntry("ownerInfo#getLastPlayed: " + ownerInfo.getLastPlayed(), CustomLogEntryTypes.Debug, true);

                for (Claim claim : claims)
                {
                    //if configured to do so, restore the land to natural
                    if (GriefPrevention.instance.creativeRulesApply(claim.getLesserBoundaryCorner()) || GriefPrevention.instance.config_claims_survivalAutoNatureRestoration)
                    {
                        GriefPrevention.instance.restoreClaim(claim, 0);
                    }
                }
            }
        }
        else if (GriefPrevention.instance.config_claims_unusedClaimExpirationDays > 0 && GriefPrevention.instance.creativeRulesApply(claim.getLesserBoundaryCorner()))
        {
            //avoid scanning large claims and administrative claims
            if (claim.isAdminClaim() || claim.getWidth() > 25 || claim.getHeight() > 25) return;

            //otherwise scan the claim content
            int minInvestment = 400;

            long investmentScore = claim.getPlayerInvestmentScore();

            if (investmentScore < minInvestment)
            {
                //if the owner has been gone at least a week, and if he has ONLY the new player claim, it will be removed
                Calendar sevenDaysAgo = Calendar.getInstance();
                sevenDaysAgo.add(Calendar.DATE, -GriefPrevention.instance.config_claims_unusedClaimExpirationDays);
                boolean claimExpired = sevenDaysAgo.getTime().after(new Date(ownerInfo.getLastPlayed()));
                if (claimExpired)
                {
                    if (expireEventCanceled())
                        return;
                    GriefPrevention.instance.dataStore.deleteClaim(claim, true, true);
                    GriefPrevention.AddLogEntry("Removed " + claim.getOwnerName() + "'s unused claim @ " + GriefPrevention.getfriendlyLocationString(claim.getLesserBoundaryCorner()), CustomLogEntryTypes.AdminActivity);

                    //restore the claim area to natural state
                    GriefPrevention.instance.restoreClaim(claim, 0);
                }
            }
        }
    }

    public boolean expireEventCanceled()
    {
        //see if any other plugins don't want this claim deleted
        ClaimExpirationEvent event = new ClaimExpirationEvent(this.claim);
        Bukkit.getPluginManager().callEvent(event);
        return event.isCancelled();
    }
}
