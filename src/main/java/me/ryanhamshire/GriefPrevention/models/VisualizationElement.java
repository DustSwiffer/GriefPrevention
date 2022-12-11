package me.ryanhamshire.GriefPrevention.models;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

//represents a "fake" block sent to a player as part of a visualization
public class VisualizationElement
{
    public Location location;
    public BlockData visualizedBlock;
    public BlockData realBlock;

    public VisualizationElement(Location location, BlockData visualizedBlock, BlockData realBlock)
    {
        this.location = location;
        this.visualizedBlock = visualizedBlock;
        this.realBlock = realBlock;
    }
}
