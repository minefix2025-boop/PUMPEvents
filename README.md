# PUMPEvents

A lightweight event plugin for **PUMPCraft**, targeting Paper/Purpur 1.21.6 and Java 21.

## Build

Install JDK 21 and Maven, then run:

```bash
mvn --batch-mode clean package
```

The plugin JAR is created at `target/PUMPEvents-1.2.0.jar`. Put it in the server's `plugins` folder and restart the server.

## Commands

All commands require `pumpevents.admin` (operators receive it by default).

- `/pumpevents` — show command help and Pumpkin Hunt status.
- `/meteor <x> <y> <z>` or `/meteor @s` — starts a meteor event. When its magma blocks land, break them to receive random loot.
- `/pumpkincreate @s` — creates a special pumpkin; break it for random loot.
- `/pumpkinhunt start` — starts a timed collectible event. Glowing pumpkins spawn near online players and award random loot.
- `/pumpkinhunt stop` — ends the hunt, announces top collectors, and removes remaining event pumpkins.
- `/midnightpump` — sets loaded worlds to midnight, adds a short darkness effect, and optionally spawns a nearby zombie for each online player.
- `/boss`, `/boss @s`, or `/boss <x> <y> <z>` — spawns the PUMP World Boss. Defeating it drops 16 gold ingots, 3 diamonds, and gives its killer one random bonus reward.

## Reward settings

Edit `plugins/PUMPEvents/config.yml` and restart the server after changes.

- `meteor.rewards`: loot pool for landed meteor blocks.
- `pumpkin.rewards`: loot pool for `/pumpkincreate` pumpkins.
- `pumpkin-hunt.rewards`: loot pool for collected hunt pumpkins.
- `pumpkin-hunt.random-rewards`: enable random loot; set false to award the configured emerald amount instead.
- `boss.rewards`: random bonus loot for the player who dealt the final blow.
- Repeating a material in a list increases its chance. Regular rewards give 1–3 items; rare/high-value items such as diamonds, golden apples, netherite scrap and totems give one.

Meteor blocks and reward pumpkins are tracked while the server is running. The meteor does not create an explosion. Test new plugin versions on a backup/test server before deploying to a live server.
