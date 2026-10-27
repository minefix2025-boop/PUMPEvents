package ru.pumpcraft.pumpevents;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Ravager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class PUMPEvents extends JavaPlugin implements Listener {
    private NamespacedKey huntKey;
    private final Map<UUID, Integer> pumpkinScores = new HashMap<>();
    private final Set<String> meteorBlocks = new HashSet<>();
    private final Set<String> rewardPumpkins = new HashSet<>();
    private BukkitTask pumpkinHuntTask;
    private int pumpkinHuntSecondsLeft;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        huntKey = new NamespacedKey(this, "hunt_pumpkin");
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("PUMPEvents enabled!");
    }

    @Override
    public void onDisable() {
        if (pumpkinHuntTask != null) pumpkinHuntTask.cancel();
        pumpkinScores.clear();
        getLogger().info("PUMPEvents disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "pumpevents" -> {
                sender.sendMessage(color("&6&lPUMPCraft &8» &ePUMPEvents commands:"));
                sender.sendMessage(color("&6/meteor <x> <y> <z> &7or &6/meteor @s"));
                sender.sendMessage(color("&6/pumpkincreate @s &7- create a reward pumpkin"));
                sender.sendMessage(color("&6/pumpkinhunt start|stop &7- collectible pumpkin event"));
                sender.sendMessage(color("&6/midnightpump &7- midnight event"));
                sender.sendMessage(color("&6/boss [@s|x y z] &7- summon the world boss"));
                sender.sendMessage(color("&7Pumpkin hunt: " + (pumpkinHuntTask == null ? "&cOFF" : "&aON (" + pumpkinHuntSecondsLeft + "s)")));
                return true;
            }
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
                sender.sendMessage(color("&6☠ &ePumpkin event triggered!"));
                return true;
            }
            case "pumpkinhunt" -> {
                if (args.length != 1) {
                    sender.sendMessage(color("&cUsage: /pumpkinhunt start|stop"));
                    return true;
                }
                if (args[0].equalsIgnoreCase("start")) {
                    startPumpkinHunt(sender);
                } else if (args[0].equalsIgnoreCase("stop")) {
                    stopPumpkinHunt("&cThe Pumpkin Hunt was stopped by an admin.");
                    sender.sendMessage(color("&7Pumpkin Hunt stopped."));
                } else {
                    sender.sendMessage(color("&cUsage: /pumpkinhunt start|stop"));
                }
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
            sender.sendMessage(color("&c@s works only for a player. From console specify coordinates."));
            return null;
        }
        if (args.length != 3) return null;
        try {
            double x = Double.parseDouble(args[0]);
            double y = Double.parseDouble(args[1]);
            double z = Double.parseDouble(args[2]);
            World world = sender instanceof Player player ? player.getWorld()
                    : (Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0));
            if (world == null || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                    || y < world.getMinHeight() || y >= world.getMaxHeight()) return null;
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
        // Visual-only event: no explosion is created.
    }


    @EventHandler
    public void onMeteorLand(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof FallingBlock falling)
                || falling.getBlockData().getMaterial() != Material.MAGMA_BLOCK
                || event.getTo() != Material.MAGMA_BLOCK) return;
        Location landed = event.getBlock().getLocation();
        Bukkit.getScheduler().runTask(this, () -> {
            if (landed.getBlock().getType() == Material.MAGMA_BLOCK) {
                meteorBlocks.add(locationKey(landed));
                landed.getWorld().spawnParticle(Particle.FLAME, landed.clone().add(0.5, 1, 0.5), 18, 0.3, 0.3, 0.3, 0.02);
            }
        });
    }

    @EventHandler
    public void onRewardBlockBreak(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        String key = locationKey(location);
        Player player = event.getPlayer();
        if (meteorBlocks.remove(key)) {
            event.setDropItems(false);
            rewardRandom(player, "meteor.rewards", java.util.List.of(
                    "DIAMOND", "EMERALD", "GOLD_INGOT", "IRON_INGOT", "EXPERIENCE_BOTTLE", "GOLDEN_APPLE"),
                    "&6Meteor loot");
            player.getWorld().spawnParticle(Particle.LAVA, location.clone().add(0.5, 0.5, 0.5), 25, 0.3, 0.3, 0.3, 0.02);
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.2f);
            return;
        }
        if (rewardPumpkins.remove(key)) {
            event.setDropItems(false);
            rewardRandom(player, "pumpkin.rewards", java.util.List.of(
                    "EMERALD", "GOLD_INGOT", "IRON_INGOT", "GOLDEN_CARROT", "GOLDEN_APPLE", "DIAMOND"),
                    "&6Pumpkin loot");
            player.getWorld().spawnParticle(Particle.SOUL, location.clone().add(0.5, 0.5, 0.5), 20, 0.3, 0.4, 0.3, 0.02);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.4f);
        }
    }

    private String locationKey(Location location) {
        return location.getWorld().getUID() + ":" + location.getBlockX() + ":"
                + location.getBlockY() + ":" + location.getBlockZ();
    }

    private void rewardRandom(Player player, String configPath, java.util.List<String> defaults, String label) {
        java.util.List<String> pool = getConfig().getStringList(configPath);
        if (pool.isEmpty()) pool = defaults;
        String chosen = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
        Material material = Material.matchMaterial(chosen);
        if (material == null || !material.isItem()) material = Material.EMERALD;
        int amount = ThreadLocalRandom.current().nextInt(1, 4);
        if (material == Material.DIAMOND || material == Material.GOLDEN_APPLE
                || material == Material.NETHERITE_SCRAP || material == Material.ENCHANTED_GOLDEN_APPLE
                || material == Material.TOTEM_OF_UNDYING) amount = 1;
        ItemStack reward = new ItemStack(material, amount);
        java.util.Map<Integer, ItemStack> leftovers = player.getInventory().addItem(reward);
        leftovers.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        player.sendMessage(color(label + " &8» &eYou received &a" + amount + "x " + material.name().replace('_', ' ')));
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
            rewardPumpkins.add(locationKey(blockLocation));
            world.spawnParticle(Particle.SOUL, blockLocation.clone().add(0.5, 1.0, 0.5), 35, 0.5, 0.7, 0.5, 0.03);
            world.playSound(blockLocation, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 0.7f);
            Bukkit.broadcastMessage(color("&6☠ &eA mysterious pumpkin appeared in &f"
                    + world.getName() + "&e!"));
        } else {
            world.spawnParticle(Particle.SOUL, location.clone().add(0, 1, 0), 25, 0.5, 0.7, 0.5, 0.03);
            Bukkit.broadcastMessage(color("&6☠ &eA mysterious pumpkin event was triggered! The block was left untouched."));
        }
    }

    private void startPumpkinHunt(CommandSender sender) {
        if (pumpkinHuntTask != null) {
            sender.sendMessage(color("&cPumpkin Hunt is already running."));
            return;
        }
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            sender.sendMessage(color("&cAt least one player must be online to start the hunt."));
            return;
        }
        pumpkinScores.clear();
        for (Player player : Bukkit.getOnlinePlayers()) pumpkinScores.put(player.getUniqueId(), 0);
        pumpkinHuntSecondsLeft = Math.max(30, Math.min(600, getConfig().getInt("pumpkin-hunt.duration-seconds", 120)));
        Bukkit.broadcastMessage(color("&6&l☠ PUMPKIN HUNT! &eFind the glowing pumpkins! You have "
                + pumpkinHuntSecondsLeft + " seconds. Collect pumpkins for random loot!"));
        for (Player player : Bukkit.getOnlinePlayers()) spawnHuntPumpkins(player, 3);
        pumpkinHuntTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (pumpkinHuntSecondsLeft <= 0) {
                stopPumpkinHunt("&6Pumpkin Hunt has ended!");
                return;
            }
            if (pumpkinHuntSecondsLeft % 20 == 0) {
                for (Player player : Bukkit.getOnlinePlayers()) spawnHuntPumpkins(player, 1);
            }
            if (pumpkinHuntSecondsLeft == 60 || pumpkinHuntSecondsLeft == 30 || pumpkinHuntSecondsLeft == 10) {
                Bukkit.broadcastMessage(color("&ePumpkin Hunt ends in &6" + pumpkinHuntSecondsLeft + " seconds&e!"));
            }
            pumpkinHuntSecondsLeft--;
        }, 20L, 20L);
    }

    private void spawnHuntPumpkins(Player player, int amount) {
        if (!player.isOnline()) return;
        World world = player.getWorld();
        for (int i = 0; i < amount; i++) {
            int dx = ThreadLocalRandom.current().nextInt(-12, 13);
            int dz = ThreadLocalRandom.current().nextInt(-12, 13);
            int x = player.getLocation().getBlockX() + dx;
            int z = player.getLocation().getBlockZ() + dz;
            int y = world.getHighestBlockYAt(x, z) + 1;
            if (y >= world.getMaxHeight() || y < world.getMinHeight()) continue;
            Location at = new Location(world, x + 0.5, y, z + 0.5);
            if (!at.getBlock().isPassable() || !at.clone().add(0, 1, 0).getBlock().isPassable()) continue;
            ItemStack itemStack = new ItemStack(Material.CARVED_PUMPKIN);
            ItemMeta meta = itemStack.getItemMeta();
            if (meta == null) continue;
            meta.setDisplayName(color("&6&lHUNT PUMPKIN"));
            meta.setLore(java.util.List.of(color("&7A collectible pumpkin for the PUMPCraft event"), color("&ePick it up for emeralds!")));
            meta.getPersistentDataContainer().set(huntKey, PersistentDataType.BYTE, (byte) 1);
            itemStack.setItemMeta(meta);
            Item item = world.dropItem(at, itemStack);
            item.setGlowing(true);
            item.setUnlimitedLifetime(true);
            item.setVelocity(new Vector(0, 0, 0));
        }
    }

    private void stopPumpkinHunt(String message) {
        if (pumpkinHuntTask == null) return;
        pumpkinHuntTask.cancel();
        pumpkinHuntTask = null;
        Bukkit.broadcastMessage(color(message));
        pumpkinScores.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(3)
                .forEach(entry -> {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    String name = player == null ? Bukkit.getOfflinePlayer(entry.getKey()).getName() : player.getName();
                    if (name != null && entry.getValue() > 0) {
                        Bukkit.broadcastMessage(color("&e#" + (1 + pumpkinScores.entrySet().stream()
                                .filter(other -> other.getValue() > entry.getValue()).count())
                                + " &6" + name + " &7collected &a" + entry.getValue() + " &7pumpkins!"));
                    }
                });
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item item && isHuntPumpkin(item)) item.remove();
            }
        }
        pumpkinScores.clear();
    }

    private boolean isHuntPumpkin(Item item) {
        ItemMeta meta = item.getItemStack().getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(huntKey, PersistentDataType.BYTE);
    }

    @EventHandler
    public void onHuntPumpkinPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player) || !isHuntPumpkin(event.getItem())) return;
        if (pumpkinHuntTask == null) {
            event.setCancelled(true);
            event.getItem().remove();
            return;
        }
        event.setCancelled(true);
        event.getItem().remove();
        int reward = Math.max(0, Math.min(64, getConfig().getInt("pumpkin-hunt.emeralds-per-pumpkin", 2)));
        if (getConfig().getBoolean("pumpkin-hunt.random-rewards", true)) {
            rewardRandom(player, "pumpkin-hunt.rewards", java.util.List.of(
                    "EMERALD", "EMERALD", "GOLD_INGOT", "IRON_INGOT", "GOLDEN_CARROT", "DIAMOND", "EXPERIENCE_BOTTLE"),
                    "&6Hunt reward");
        } else if (reward > 0) {
            player.getInventory().addItem(new ItemStack(Material.EMERALD, reward))
                    .values().forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
        }
        pumpkinScores.merge(player.getUniqueId(), 1, Integer::sum);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.5f);
        player.spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 12, 0.4, 0.5, 0.4, 0.02);
        player.sendMessage(color("&6+1 Hunt Pumpkin &8| &eTotal: " + pumpkinScores.get(player.getUniqueId())));
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
            double health = Math.max(40.0, Math.min(2048.0, getConfig().getDouble("boss.max-health", 300.0)));
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
        event.getDrops().add(new ItemStack(Material.GOLD_INGOT, 16));
        event.getDrops().add(new ItemStack(Material.DIAMOND, 3));
        Player killer = ravager.getKiller();
        if (killer != null) {
            rewardRandom(killer, "boss.rewards", java.util.List.of(
                    "DIAMOND", "DIAMOND", "EMERALD", "GOLDEN_APPLE", "NETHERITE_SCRAP", "TOTEM_OF_UNDYING"),
                    "&4Boss reward");
        }
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }
}
