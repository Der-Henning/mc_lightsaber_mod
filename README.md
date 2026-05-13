# Lightsabers (Fabric)

Minecraft Java Fabric mod targeting **26.1.2** with lightsabers, blaster, recipes, sounds, and Jedi armor.

> Note: This project was vibe coded using Codex.

## Prerequisites

- Java 25
- Gradle
- Internet connection for one-time dev dependency/shader downloads

## Development Setup (Codex / Local)

Run once to install optional visual runtime dependencies into `run/mods` and `run/shaderpacks`:

```bash
./scripts/setup-codex-dev.sh
```

The setup script installs:

- `sodium-fabric-0.8.11+mc26.1.2.jar`
- `iris-fabric-1.10.9+mc26.1.1.jar`
- `lambdynamiclights-4.10.2+26.1.2.jar`
- `modmenu-18.0.0-beta.1.jar`
- `ComplementaryUnbound_r5.7.1.zip` shaderpack

Optional:

```bash
./scripts/setup-codex-dev.sh --no-shaderpack
```

## Run In Dev

Terminal:

```bash
./run-client.sh
```

VS Code run action:

- `Terminal` -> `Run Task...` -> `Run Mod Client`

## Build

```bash
gradle build
```

Built jar output: `build/libs/`.

## Install (Client)

Use Fabric Loader for Minecraft `26.1.2` and place these jars in your client `mods/` folder:

- this mod jar from `build/libs/`
- `fabric-api`

Optional visual mods (client-only):

- `sodium`
- `iris` (for shader packs)
- `lambdynamiclights`
- `modmenu` (recommended)

## Install (Server)

Yes, this mod works on a Fabric server.

Use Fabric Loader for Minecraft `26.1.2` and place these jars in the server `mods/` folder:

- this mod jar from `build/libs/`
- `fabric-api`

Players joining the server must also have the same mod (and compatible version) installed on their client.

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
