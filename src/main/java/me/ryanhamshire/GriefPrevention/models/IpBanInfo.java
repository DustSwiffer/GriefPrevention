package me.ryanhamshire.GriefPrevention.models;

import java.net.InetAddress;

public class IpBanInfo
{
    public InetAddress address;
    public long expirationTimestamp;
    public String bannedAccountName;

    public IpBanInfo(InetAddress address, long expirationTimestamp, String bannedAccountName)
    {
        this.address = address;
        this.expirationTimestamp = expirationTimestamp;
        this.bannedAccountName = bannedAccountName;
    }
}
