package pro.akii.ks.core.fieldforge.utils;

import org.bukkit.Location;

public class VectorMath {
    /**
     * Calculates the Euclidean distance between two locations.
     *
     * @param loc1 The first location.
     * @param loc2 The second location.
     * @return The distance between the locations.
     * @throws IllegalArgumentException If locations are in different worlds.
     */
    public static double distance(Location loc1, Location loc2) {
        if (loc1.getWorld() != loc2.getWorld()) {
            throw new IllegalArgumentException("Locations must be in the same world.");
        }
        return loc1.distance(loc2);
    }
}