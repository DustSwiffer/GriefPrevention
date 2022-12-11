package me.ryanhamshire.GriefPrevention.models;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

//basically, just a few data points from a block conveniently encapsulated in a class
//this is used only by the RestoreNature code
public class BlockSnapshot
{
    public Location location;
    public Material typeId;
    public BlockData data;

    public BlockSnapshot(Location location, Material typeId, BlockData data)
    {
        this.location = location;
        this.typeId = typeId;
        this.data = data;
    }
}
