package me.ryanhamshire.GriefPrevention.enums;

import me.ryanhamshire.GriefPrevention.models.Claim;

/**
 * Enum representing the permissions available in a {@link Claim}.
 */
public enum ClaimPermission
{
    /**
     * ClaimPermission used for owner-based checks. Cannot be granted and grants all other permissions.
     */
    Edit(Messages.OnlyOwnersModifyClaims),
    /**
     * ClaimPermission used for building checks. Grants {@link #Inventory} and {@link #Access}.
     */
    Build(Messages.NoBuildPermission),
    /**
     * ClaimPermission used for inventory management checks. Grants {@link #Access}.
     */
    Inventory(Messages.NoContainersPermission),
    /**
     * ClaimPermission used for basic access.
     */
    Access(Messages.NoAccessPermission),
    /**
     *  Entry permission
     */
    Entry(Messages.NoEntryPermission),
    /**
     * ClaimPermission that allows users to grant ClaimPermissions. Uses a separate track from normal
     * permissions and does not grant any other permissions.
     */
    Manage(Messages.NoPermissionTrust);

    private final Messages denialMessage;

    ClaimPermission(Messages messages)
    {
        this.denialMessage = messages;
    }

    /**
     * @return the {@link Messages Message} used when alerting a user that they lack the ClaimPermission
     */
    public Messages getDenialMessage()
    {
        return denialMessage;
    }

    /**
     * Check if a ClaimPermission is granted by another ClaimPermission.
     *
     * @param other the ClaimPermission to compare against
     * @return true if this ClaimPermission is equal or lesser than the provided ClaimPermission
     */
    public boolean isGrantedBy(ClaimPermission other)
    {
        if (other == Manage || this == Manage) return other == this || other == Edit;
        // This uses declaration order to compare! If trust levels are reordered this method must be rewritten.
        return other != null && other.ordinal() <= this.ordinal();
    }

}
