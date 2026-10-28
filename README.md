# PUMPEvents

An event plugin for **PUMPCraft**, targeting Paper/Purpur 1.21.6 and Java 21.

## Build

Run `mvn --batch-mode clean package`. The plugin JAR is `target/PUMPEvents-1.3.0.jar`.

## Commands

All commands require `pumpevents.admin` (operators receive it by default).

- `/pumpevents` — show commands and Pumpkin Hunt status.
- `/meteor <x> <y> <z>` or `/meteor @s` — launch falling magma blocks; break landed event blocks for random loot.
- `/pumpkincreate @s` — create a special pumpkin; break it for random loot.
- `/pumpkinhunt start|stop` — timed collectible hunt with random rewards and leaderboard.
- `/midnightpump` — midnight atmosphere, darkness, and optional nearby zombies.
- `/boss [@s|x y z]` — summon the PUMP World Boss; defeat it for gold, diamonds, and a bonus reward.
- `/bloodmoon` — set night, start rain, add red particles and spawn a hostile wave around online players.
- `/pumpquake` — quake sound/particles, a small knockback effect, and a chance at random loot for each online player.
- `/lightningstorm` — visual-only lightning around online players; it does not damage players or start fires.
- `/supplydrop [@s|x y z]` — create a chest with randomized supplies. Coordinates use your current world; console uses the first loaded world.
- `/pumprain` — drop six glowing collectible pumpkins near each online player; picking one up grants random loot.
- `/mobraid [@s|x y z]` — spawn a wave of zombies and skeletons at the target.

## Loot configuration

Edit `plugins/PUMPEvents/config.yml`, then restart the server.

- `meteor.rewards` — loot from landed meteor blocks.
- `pumpkin.rewards` — loot from special `/pumpkincreate` pumpkins.
- `pumpkin-hunt.rewards` — Pumpkin Hunt loot.
- `pumpkin-rain.rewards` — Pumpkin Rain loot.
- `quake.rewards` — possible PUMP Quake loot.
- `boss.rewards` — boss killer's bonus reward.

Repeat a material in a reward list to make it more likely. Event tracking for falling meteor blocks and special pumpkins is in-memory; restart clears that tracking. Test new versions on a backup server before live deployment.
