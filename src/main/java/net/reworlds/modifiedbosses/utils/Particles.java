package net.reworlds.modifiedbosses.utils;

import com.google.common.collect.Lists;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.List;

public class Particles {

    public static void sphere(Location location, Color color, double radius) {
        for (double phi = 0; phi <= Math.PI; phi += Math.PI / 30) {
            double y = radius * Math.cos(phi) + 1.5;
            for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / 30) {
                double x = radius * Math.cos(theta) * Math.sin(phi);
                double z = radius * Math.sin(theta) * Math.sin(phi);

                location.add(x, y, z);
                location.getWorld().spawnParticle(Particle.REDSTONE, location, 0, new Particle.DustOptions(color, 1));
                location.subtract(x, y, z);
            }
        }
    }

    public static List<Location> particleLine(Location first, Location second, Color color) {
        List<Location> locations = Lists.newArrayList();
        Vector vector = direction(first, second);
        for (double i = 1; i <= first.distance(second); i += 0.5) {
            vector.multiply(i);
            first.add(vector);
            first.getWorld().spawnParticle(Particle.REDSTONE, first, 0, new Particle.DustOptions(color, 1));
            locations.add(first.clone());
            first.subtract(vector);
            vector.normalize();
        }
        return locations;
    }

    public static Vector direction(Location first, Location second) {
        Vector from = first.toVector();
        Vector to = second.toVector();
        return to.subtract(from);
    }

    public static List<Location> circle(Location location, Particle particle, double radius) {
        List<Location> list = Lists.newArrayList();
        for (double t = 0; t <= 2 * Math.PI * radius; t += 0.2) {
            double x = (radius * Math.cos(t)) + location.getX();
            double z = location.getZ() + radius * Math.sin(t);
            Location spawn = new Location(location.getWorld(), x, location.getY() + 0.3, z);
            spawn.getWorld().spawnParticle(particle, spawn, 0);
            list.add(spawn);
        }
        return list;
    }

    public static void circle(Location location, Color color, double radius) {
        for (double t = 0; t <= 2 * Math.PI * radius; t += 0.2) {
            double x = (radius * Math.cos(t)) + location.getX();
            double z = location.getZ() + radius * Math.sin(t);
            Location spawn = new Location(location.getWorld(), x, location.getY() + 0.3, z);
            spawn.getWorld().spawnParticle(Particle.REDSTONE, spawn, 0, new Particle.DustOptions(color, 1));
        }
        return;
    }
}
