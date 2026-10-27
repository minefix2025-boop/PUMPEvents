# PUMPEvents

A lightweight event plugin for **PUMPCraft**, targeting Paper/Purpur 1.21.6 and Java 21.

## Build

Install JDK 21 and Maven, then run:

```bash
mvn clean package
```

The plugin jar is created at `target/PUMPEvents-1.0.0.jar`. Put it in the server's `plugins` folder and restart the server.

## Commands

All commands require the `pumpevents.admin` permission (operators receive it by default).

- `/pumpevents` — show command help and Pumpkin Hunt status.
- `/meteor <x> <y> <z>` — starts a meteor visual event at coordinates in your current world (console uses the first loaded world).
- `/meteor @s` — starts the event at your current position.
- `/pumpkincreate @s` — creates a carved pumpkin at your position when the block can safely be replaced.
- `/pumpkinhunt start` — starts a timed collectible event. Glowing hunt pumpkins spawn near online players; collecting one awards emeralds.
- `/pumpkinhunt stop` — ends the hunt, announces the top collectors, and removes remaining event pumpkins.
- `/midnightpump` — sets loaded worlds to midnight, adds a short darkness effect, and optionally spawns a nearby zombie for each online player.
- `/boss` — spawns the PUMP World Boss at your position.
- `/boss @s` — same as above.
- `/boss <x> <y> <z>` — spawns the boss at coordinates in your current world (console uses the first loaded world).

## Pumpkin Hunt settings

Edit `plugins/PUMPEvents/config.yml`:

- `pumpkin-hunt.duration-seconds`: 30–600 seconds.
- `pumpkin-hunt.emeralds-per-pumpkin`: 0–64 emeralds per pumpkin.
- `meteor.falling-block-count`: 1–12 visual falling blocks.
- `pumpkin.replace-blocks`: whether the decorative pumpkin may replace a non-air block.
- `midnight.duration-seconds` and `midnight.spawn-mobs`: midnight effects.
- `boss.max-health`, `boss.damage`, `boss.movement-speed`, and `boss.reward-message`: boss tuning.

The meteor event does not create an explosion. Test new plugin versions on a backup/test server before deploying to a live server.
