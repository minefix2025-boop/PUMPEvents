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

- `/meteor <x> <y> <z>` — starts a meteor visual event at coordinates in your current world (console uses the first loaded world).
- `/meteor @s` — starts the event at your current position.
- `/pumpkincreate @s` — creates a carved pumpkin at your position when the block can safely be replaced.
- `/midnightpump` — sets loaded worlds to midnight, adds a short darkness effect, and optionally spawns a nearby zombie for each online player.
- `/boss` — spawns the PUMP World Boss at your position.
- `/boss @s` — same as above.
- `/boss <x> <y> <z>` — spawns the boss at coordinates in your current world (console uses the first loaded world).

## Configuration

Edit `plugins/PUMPEvents/config.yml` to adjust the meteor visual count, pumpkin block replacement, midnight duration/mob spawning, and boss health/damage/speed/reward message.

The meteor event is visual-only: it does not create a terrain-damaging explosion. Always test new plugins on a backup or test server before deploying to a live server.
