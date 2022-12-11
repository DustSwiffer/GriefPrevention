package me.ryanhamshire.GriefPrevention.models;

import me.ryanhamshire.GriefPrevention.enums.Messages;

public class CustomizableMessage
{
    public Messages id;
    public String text;
    public String notes;

    public CustomizableMessage(Messages id, String text, String notes)
    {
        this.id = id;
        this.text = text;
        this.notes = notes;
    }
}