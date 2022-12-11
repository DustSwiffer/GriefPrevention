package me.ryanhamshire.GriefPrevention.util;

import me.ryanhamshire.GriefPrevention.models.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.models.PlayerData;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class InClaimCalculator  implements ContextCalculator<Player>
{
    private static final String inclaimKey = "griefprevention:in-claim";
    private static final String claimTypeKey = "griefprevention:claim";
    private DataStore dataStore;
    public InClaimCalculator(DataStore dataStore){
        this.dataStore = dataStore;
    }
    @Override
    public void calculate(Player target, ContextConsumer consumer) {
        Location loc = target.getLocation();
        PlayerData playerData = this.dataStore.getPlayerData(target.getUniqueId());

        Claim claim = this.dataStore.getClaimAt(loc, true, playerData.lastClaim);

        if(claim != null) {
            consumer.accept(inclaimKey, "true");
            if(claim.isAdminClaim()){
                consumer.accept(claimTypeKey, "admin");
            } else {
                if(claim.getOwnerName().equals(target.getName())) {
                    consumer.accept(claimTypeKey, "own");
                } else {
                    consumer.accept(claimTypeKey, "other");
                }
            }
        } else {
            consumer.accept(inclaimKey, "false");
            consumer.accept(claimTypeKey, "wild");
        }
    }
}
