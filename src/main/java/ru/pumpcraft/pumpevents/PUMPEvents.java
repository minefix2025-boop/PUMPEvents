package ru.pumpcraft.pumpevents;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.Ravager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public final class PUMPEvents extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("PUMPEvents enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("PUMPEvents disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "meteor" -> {
                Location target = parseTarget(sender, args);
                if (target == null) {
                    sender.sendMessage(color("&cUsage: /meteor <x> <y> <z> or /meteor @s"));
                    return true;
                }
                startMeteor(target);
                sender.sendMessage(color("&6☄ Meteor event started at &e"
                        + target.getBlockX() + " " + target.getBlockY() + " " + target.getBlockZ()));
                return true;
            }
            case "pumpkincreate" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(color("&cUse /pumpkincreate @s in-game."));
                    return true;
                }
                if (args.length != 1 || !args[0].equalsIgnoreCase("@s")) {
                    sender.sendMessage(color("&cUsage: /pumpkincreate @s"));
                    return true;
                }
                createPumpkin(player.getLocation());
                sender.sendMessage(color("&6☠ &eEvent pumpkin created!"));
                return true;
            }
            case "midnightpump" -> {
                startMidnight();
                sender.sendMessage(color("&5Midnight Pumpkin event started."));
                return true;
            }
            case "boss" -> {
                Location target;
                if (args.length == 0) {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(color("&cFrom console use /boss <x> <y> <z>."));
                        return true;
                    }
                    target = player.getLocation();
                } else {
                    target = parseTarget(sender, args);
                    if (target == null) {
                        sender.sendMessage(color("&cUsage: /boss [@s] or /boss <x> <y> <z>"));
                        return true;
                    }
                }
                spawnBoss(target);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private Location parseTarget(CommandSender sender, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("@s")) {
            if (sender instanceof Player player) return player.getLocation().clone();
            sender.sendMessage(color("&c@ s works only for a player. From console specify coordinates."));
            return null;
        }
        if (args.length != 3) return null;
        try {
            double x = Double.parseDouble(args[0]);
            double y = Double.parseDouble(args[1]);
            double z = Double.parseDouble(args[2]);
            World world = sender instanceof Player player ? player.getWorld() : Bukkit.getWorlds().get(0);
            if (world == null || y < world.getMinHeight() || y >= world.getMaxHeight()) return null;
            return new Location(world, x, y, z);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private void startMeteor(Location target) {
        World world = target.getWorld();
        if (world == null) return;
        Bukkit.broadcastMessage(color("&6☄ &lPUMPCraft &eA meteor is falling at "
                + target.getBlockX() + ", " + target.getBlockY() + ", " + target.getBlockZ() + "!"));
        world.playSound(target, Sound.ENTITY_WITHER_SPAWN, 1.3f, 0.8f);
        world.spawnParticle(Particle.FLAME, target.clone().add(0, 1, 0), 90, 2.0, 1.5, 2.0, 0.08);
        world.spawnParticle(Particle.LAVA, target.clone().add(0, 1, 0), 35, 1.5, 1.0, 1.5, 0.02);
        int count = Math.max(1, Math.min(12, getConfig().getInt("meteor.falling-block-count", 5)));
        for (int i = 0; i < count; i++) {
            double dx = ThreadLocalRandom.current().nextDouble(-3.0, 3.0);
            double dz = ThreadLocalRandom.current().nextDouble(-3.0, 3.0);
            Location spawn = target.clone().add(dx, 12 + i * 1.5, dz);
            FallingBlock block = world.spawnFallingBlock(spawn, Material.MAGMA_BLOCK.createBlockData());
            block.setDropItem(false);
            block.setHurtEntities(false);
            block.setVelocity(new Vector(dx * 0.04, -0.45, dz * 0.04));
        }
        world.spawnParticle(Particle.EXPLOSION, target.clone().add(0, 1, 0), 2, 0.5, 0.5, 0.5, 0);
        // No terrain-damaging explosion is created.
    }

    private void createPumpkin(Location location) {
        World world = location.getWorld();
        if (world == null) return;
        Location blockLocation = location.getBlock().getLocation();
        Material existing = blockLocation.getBlock().getType();
        boolean replace = getConfig().getBoolean("pumpkin.replace-blocks", false);
        if (existing.isAir() || existing == Material.SHORT_GRASS || existing == Material.TALL_GRASS
                || existing == Material.FERN || replace) {
            blockLocation.getBlock().setType(Material.CARVED_PUMPKIN, false);
            world.spawnParticle(Particle.SOUL, blockLocation.clone().add(0.5, 1.0, 0.5), 35, 0.5, 0.7, 0.5, 0.03);
            world.playSound(blockLocation, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 0.7f);
            Bukkit.broadcastMessage(color("&6☠ &eA mysterious pumpkin appeared in &f"
                    + world.getName() + "&e!"));
        } else {
            world.spawnParticle(Particle.SOUL, location.clone().add(0, 1, 0), 25, 0.5, 0.7, 0.5, 0.03);
            Bukkit.broadcastMessage(color("&6☠ &eA mysterious pumpkin event was triggered!"));
        }
    }

    private void startMidnight() {
        Bukkit.broadcastMessage(color("&5&l☾ MIDNIGHT PUMPKIN ☽ &dThe world is falling into darkness!"));
        for (World world : Bukkit.getWorlds()) {
            world.setTime(18000L);
            world.playSound(world.getSpawnLocation(), Sound.ENTITY_ENDERMAN_AMBIENT, 1.0f, 0.6f);
        }
        int duration = Math.max(5, Math.min(120, getConfig().getInt("midnight.duration-seconds", 20)));
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, duration * 20, 0, false, true, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, duration * 20, 0, false, false, false));
            player.getWorld().spawnParticle(Particle.SOUL, player.getLocation().add(0, 1, 0), 45, 1.2, 1.0, 1.2, 0.04);
            if (getConfig().getBoolean("midnight.spawn-mobs", true)) {
                Location spawn = player.getLocation().clone().add(
                        ThreadLocalRandom.current().nextDouble(-4, 5), 0,
                        ThreadLocalRandom.current().nextDouble(-4, 5));
                if (spawn.getBlock().isPassable() && spawn.clone().add(0, 1, 0).getBlock().isPassable()) {
                    player.getWorld().spawnEntity(spawn, org.bukkit.entity.EntityType.ZOMBIE);
                }
            }
        }
    }

    private void spawnBoss(Location location) {
        World world = location.getWorld();
        if (world == null) return;
        Location spawn = location.clone();
        if (!spawn.getBlock().isPassable()) spawn.add(0, 1, 0);
        Ravager boss = world.spawn(spawn, Ravager.class, entity -> {
            entity.setCustomName(color(getConfig().getString("boss.name", "&6&lPUMP WORLD BOSS")));
            entity.setCustomNameVisible(true);
            entity.setPersistent(true);
            entity.setRemoveWhenFarAway(false);
            double health = Math.max(40.0, getConfig().getDouble("boss.max-health", 300.0));
            if (entity.getAttribute(Attribute.MAX_HEALTH) != null) {
                entity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
                entity.setHealth(health);
            }
            if (entity.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
                entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(
                        Math.max(1.0, getConfig().getDouble("boss.damage", 18.0)));
            }
            if (entity.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                entity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(
                        Math.max(0.1, getConfig().getDouble("boss.movement-speed", 0.32)));
            }
        });
        boss.setGlowing(true);
        world.spawnParticle(Particle.SOUL, spawn.clone().add(0, 1, 0), 100, 1.5, 1.0, 1.5, 0.08);
        world.playSound(spawn, Sound.ENTITY_RAVAGER_ROAR, 2.0f, 0.7f);
        Bukkit.broadcastMessage(color("&4&l⚔ WORLD BOSS! &cPUMP World Boss appeared at "
                + spawn.getBlockX() + ", " + spawn.getBlockY() + ", " + spawn.getBlockZ() + "!"));
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Ravager ravager)) return;
        if (ravager.getCustomName() == null
                || !ChatColor.stripColor(ravager.getCustomName()).equals("PUMP WORLD BOSS")) return;
        Bukkit.broadcastMessage(color(getConfig().getString("boss.reward-message",
                "&aThe PUMP World Boss has been defeated!")));
        event.getDrops().add(new org.bukkit.inventory.ItemStack(Material.GOLD_INGOT, 16));
        event.getDrops().add(new org.bukkit.inventory.ItemStack(Material.DIAMOND, 3));
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }
}
