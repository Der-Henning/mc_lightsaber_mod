# Lightsabers (Fabric)

Minecraft Java Fabric mod targeting **26.3** with lightsabers, blaster, recipes, sounds, and Jedi armor.

> Note: This project was vibe coded using Codex.

## Prerequisites

- Java 25 (set `JAVA_HOME` to your JDK for direct Gradle commands)
- Fabric Loader 0.19.5 and Fabric API 0.162.0+26.3 for installed clients/servers
- Gradle 9.8.1 is downloaded by the included wrapper; a separate Gradle installation is unnecessary
- Internet connection for one-time dev dependency/shader downloads

## Development Setup (Codex / Local)

Run once to install optional visual runtime dependencies into `run/mods` and `run/shaderpacks`:

```bash
./scripts/setup-codex-dev.sh
```

The setup script installs:

- `sodium-fabric-0.9.2+mc26.3.jar`
- `iris-fabric-1.11.7+mc26.3.jar`
- `lambdynamiclights-4.13.0+26.3.jar`
- `modmenu-21.0.0.jar`
- `ComplementaryUnbound_r5.9.3.zip` shaderpack

Downloads are verified against their SHA-512 checksums before installation. Earlier managed mods and shaderpacks are archived in `run/disabled-mods` and `run/disabled-shaderpacks`. Existing shader enable/disable settings are preserved.

On macOS, install Java 25 and set it for Gradle:

```bash
brew install openjdk@25
export JAVA_HOME="$(brew --prefix openjdk@25)/libexec/openjdk.jdk/Contents/Home"
```

Optional:

```bash
./scripts/setup-codex-dev.sh --no-shaderpack
```

## Run In Dev

Terminal:

```bash
./run-client.sh
```

`run-client.sh` finds Java 25 from `JAVA_HOME`, `PATH`, macOS Java installations, or Homebrew. The Codex environment's `Run` action uses this script.

## Build

```bash
./gradlew build
```

Built jar output: `build/libs/`.

The build runs unit tests and isolated server GameTests covering dual-wield damage, blaster cooldowns/durability, and Jedi fall protection. Run the client rendering/input tests separately:

```bash
./gradlew runClientGameTest
./gradlew runClientGameTest -PvisualTests
```

Client tests create a separate temporary world and save screenshots under `build/run/clientGameTest/screenshots`. The `-PvisualTests` option also loads the installed dev mods, shaders, and their configuration.

On Windows use `gradlew.bat`. Build and release workflows use the same pinned Gradle wrapper, and releases upload the artifacts from the successful build.

## Controls

- Left click with a lightsaber: swing sound on every accepted attack, including misses.
- Two lightsabers: right mouse swings the physical right hand; left mouse swings the physical left hand, including with the left-handed main-arm setting. Each saber has its own swing, sound, damage, durability, and attack cooldown.
- Single lightsaber: normal left-click attack and right-click activation/parry controls.
- Sneak while facing a blaster bolt: deflect it; a perfectly timed use returns a stronger bolt toward its shooter.
- Sneak + right click while grounded: Force Jump, with a cooldown shared across saber colors.
- Sneak + right click while airborne: activate the eight-tick perfect-deflect window, including while dual wielding.
- Two blasters: independent cooldowns; use switches to the offhand while the main blaster cools down.
- Jedi tunic: ignores the first four points of fall damage, then reduces the remainder by 60%.

Wood ignition is disabled by default and controlled for everyone in a server/world with:

```mcfunction
/gamerule lightsabers:lightsaber_ignites_wood true
```

## Install (Client)

Use Fabric Loader for Minecraft `26.3` and place these jars in your client `mods/` folder:

- this mod jar from `build/libs/`
- `fabric-api`

Optional visual mods (client-only):

- `sodium`
- `iris` (for shader packs)
- `lambdynamiclights`
- `modmenu` (recommended)

### Lightsaber Glow

All six blade colors have a full-bright blade and a soft, color-matched aura that gently pulses every two seconds. The aura works without shaders and follows the model in either hand, third person, inventories, and dropped items. The hilt and player do not glow. Normal sabers no longer have a forced enchantment glint; enchanted sabers still shimmer on the blade only.

Iris shader bloom can enhance the halo. LambDynamicLights still provides steady nearby-world illumination; the pulse is a visual blade effect, not a server light source. No additional mods are required for the aura itself.

Regenerate the checked-in aura textures and models after changing their colors, pulse, or geometry with:

```bash
node scripts/generate-lightsaber-aura.mjs
```

## Install (Server)

Yes, this mod works on a Fabric server.

Use Fabric Loader for Minecraft `26.3` and place these jars in the server `mods/` folder:

- this mod jar from `build/libs/`
- `fabric-api`

Players joining the server must also have the same mod (and compatible version) installed on their client.

Back up existing worlds before opening them in a newer Minecraft version. Mod versions targeting 26.1.2 are incompatible with this build.

Do not install client-only graphics mods on the server:

- `sodium`
- `iris`
- shader packs
- `lambdynamiclights` (keep client-side unless you intentionally use a server-safe setup)

## Included Skin Asset

- Darth Vader skin source file: `external-resources/skins/darth_vader.png`
- Original page: [PlanetMinecraft - Darth Vader](https://www.planetminecraft.com/skin/darth-vader-5364105/)

### Use The Skin In-Game (Java)

1. Open Minecraft Launcher and start the Java Edition profile you use for this mod.
2. In the launcher, open `Skins`.
3. Add a new skin and select `external-resources/skins/darth_vader.png`.
4. Pick `Classic (4px arms)` unless you intentionally want slim arms.
