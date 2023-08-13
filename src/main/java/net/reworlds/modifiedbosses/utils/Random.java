package net.reworlds.modifiedbosses.utils;

import org.bukkit.Location;

import java.util.concurrent.ThreadLocalRandom;

public class Random {

    public static Location random(Location location, boolean toCenter, double multiply) {
        Location clone = location.clone();
        if (toCenter) {
            clone.add(0.5, 0.5, 0.5);
        }

        return clone.add(randomize(multiply), randomize(multiply), randomize(multiply));
    }

    public static Location randomFlatY(Location location, boolean toCenter, double multiply) {
        Location clone = location.clone();
        if (toCenter) {
            clone.add(0.5, 0.5, 0.5);
        }

        return clone.add(randomize(multiply), 0, randomize(multiply));
    }

    public static double randomize(double multiply) {
        return ThreadLocalRandom.current().nextDouble(-0.5 * Math.abs(multiply), 0.6 * Math.abs(multiply));
    }
}
