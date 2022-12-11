package me.ryanhamshire.GriefPrevention.tasks;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.enums.ClaimsMode;
import me.ryanhamshire.GriefPrevention.enums.CustomLogEntryTypes;
import me.ryanhamshire.GriefPrevention.models.Claim;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;

import java.util.ArrayList;
import java.util.List;

//FEATURE: creative mode worlds get a regular entity cleanup

//this main thread task revisits the location of a partially chopped tree from several minutes ago
//if any part of the tree is still there and nothing else has been built in its place, remove the remaining parts
public class EntityCleanupTask implements Runnable
{
    //where to start cleaning in the list of entities
    private final double percentageStart;

    public EntityCleanupTask(double percentageStart)
    {
        this.percentageStart = percentageStart;
    }

    @Override
    public void run()
    {
        ArrayList<World> worlds = new ArrayList<>();
        for (World world : GriefPrevention.instance.getServer().getWorlds())
        {
            if (GriefPrevention.instance.config_claims_worldModes.get(world) == ClaimsMode.Creative)
            {
                worlds.add(world);
            }
        }

        for (World world : worlds)
        {
            List<Entity> entities = world.getEntities();

            //starting and stopping point.  each execution of the task scans 10% of the server's (loaded) entities
            int j = (int) (entities.size() * this.percentageStart);
            int k = (int) (entities.size() * (this.percentageStart + .1));
            Claim cachedClaim = null;
            for (; j < entities.size() && j < k; j++)
            {
                Entity entity = entities.get(j);

                boolean remove = false;
                if (entity instanceof Boat) //boats must be occupied
                {
                    Boat boat = (Boat) entity;
                    if (boat.isEmpty()) remove = true;
                }
                else if (entity instanceof Vehicle)
                {
                    Vehicle vehicle = (Vehicle) entity;

                    //minecarts in motion must be occupied by a player
                    if (vehicle.getVelocity().lengthSquared() != 0)
                    {
                        if (vehicle.isEmpty() || !(vehicle.getPassengers().get(0) instanceof Player))
                        {
                            remove = true;
                        }
                    }

                    //stationary carts must be on rails
                    else
                    {
                        Material material = world.getBlockAt(vehicle.getLocation()).getType();
                        if (material != Material.RAIL && material != Material.POWERED_RAIL && material != Material.DETECTOR_RAIL)
                        {
                            remove = true;
                        }
                    }
                }

                //all non-player entities must be in claims
                else if (!(entity instanceof Player))
                {
                    Claim claim = GriefPrevention.instance.dataStore.getClaimAt(entity.getLocation(), false, cachedClaim);
                    if (claim != null)
                    {
                        cachedClaim = claim;
                    }
                    else
                    {
                        remove = true;
                    }
                }

                if (remove)
                {
                    GriefPrevention.AddLogEntry("Removing entity " + entity.getType().name() + " @ " + entity.getLocation(), CustomLogEntryTypes.Debug, true);
                    entity.remove();
                }
            }
        }

        //starting and stopping point.  each execution of the task scans 5% of the server's claims
        List<Claim> claims = GriefPrevention.instance.dataStore.claims;
        int j = (int) (claims.size() * this.percentageStart);
        int k = (int) (claims.size() * (this.percentageStart + .05));
        for (; j < claims.size() && j < k; j++)
        {
            Claim claim = claims.get(j);

            //if it's a creative mode claim
            if (GriefPrevention.instance.creativeRulesApply(claim.getLesserBoundaryCorner()))
            {
                //check its entity count and remove any extras
                claim.allowMoreEntities(true);
            }
        }

        //schedule the next run of this task, in 3 minutes (20L is approximately 1 second)
        double nextRunPercentageStart = this.percentageStart + .05;
        if (nextRunPercentageStart > .99)
        {
            nextRunPercentageStart = 0;
        }

        EntityCleanupTask task = new EntityCleanupTask(nextRunPercentageStart);
        GriefPrevention.instance.getServer().getScheduler().scheduleSyncDelayedTask(GriefPrevention.instance, task, 20L * 60);
    }
}
