package me.ryanhamshire.GriefPrevention.models;

public class CreateClaimResult
{
    //whether or not the creation succeeded (it would fail if the new claim overlapped another existing claim)
    public boolean succeeded;

    //when succeeded, this is a reference to the new claim
    //when failed, this is a reference to the pre-existing, conflicting claim
    public Claim claim;
}
