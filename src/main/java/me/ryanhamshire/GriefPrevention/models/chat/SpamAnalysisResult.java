package me.ryanhamshire.GriefPrevention.models.chat;

public class SpamAnalysisResult
{
    public String finalMessage;
    public boolean shouldWarnChatter = false;
    public boolean shouldBanChatter = false;
    public String muteReason;
}
