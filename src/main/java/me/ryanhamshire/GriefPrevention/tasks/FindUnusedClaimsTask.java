package me.ryanhamshire.GriefPrevention.tasks;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.enums.CustomLogEntryTypes;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

//FEATURE: automatically remove claims owned by inactive players which:
//...aren't protecting much OR
//...are a free new player claim (and the player has no other claims) OR
//...because the player has been gone a REALLY long time, and that expiration has been configured in config.yml

//runs every 1 minute in the main thread
public class FindUnusedClaimsTask implements Runnable
{
    private List<UUID> claimOwnerUUIDs;
    private Iterator<UUID> claimOwnerIterator;

    public FindUnusedClaimsTask()
    {
        refreshUUIDs();
    }

    @Override
    public void run()
    {
        //don't do anything when there are no claims
        if (claimOwnerUUIDs.isEmpty()) return;

        //wrap search around to beginning
        if (!claimOwnerIterator.hasNext())
        {
            refreshUUIDs();
            return;
        }

        GriefPrevention.instance.getServer().getScheduler().runTaskAsynchronously(GriefPrevention.instance, new CleanupUnusedClaimPreTask(claimOwnerIterator.next()));
    }

    public void refreshUUIDs()
    {
        // Fetch owner UUIDs from list of claims
        claimOwnerUUIDs = GriefPrevention.instance.dataStore.claims.stream().map(claim -> claim.ownerID)
                .distinct().filter(Objects::nonNull).collect(Collectors.toList());

        if (!claimOwnerUUIDs.isEmpty())
        {
            // Randomize order
            Collections.shuffle(claimOwnerUUIDs);
        }

        GriefPrevention.AddLogEntry("The following UUIDs own a claim and will be checked for inactivity in the following order:", CustomLogEntryTypes.Debug, true);

        for (UUID uuid : claimOwnerUUIDs)
            GriefPrevention.AddLogEntry(uuid.toString(), CustomLogEntryTypes.Debug, true);

        claimOwnerIterator = claimOwnerUUIDs.iterator();
    }
}
