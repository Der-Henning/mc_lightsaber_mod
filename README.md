# Lightsabers (Fabric)

Minecraft Java Fabric mod targeting **1.21.11** with lightsabers, blaster, recipes, sounds, and Jedi armor.

## Prerequisites

- Java 21
- Gradle

## Run in dev

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

Use Fabric Loader for Minecraft `1.21.11` and place these jars in your client `mods/` folder:

- this mod jar from `build/libs/`
- `fabric-api`

Optional visual mods (client-only):

- `sodium`
- `iris` (for shader packs)
- `lambdynamiclights`

## Install (Server)

Yes, this mod works on a Fabric server.

Use Fabric Loader for Minecraft `1.21.11` and place these jars in the server `mods/` folder:

- this mod jar from `build/libs/`
- `fabric-api`

Players joining the server must also have the same mod (and compatible version) installed on their client.

Do not install client-only graphics mods on the server:

- `sodium`
- `iris`
- shader packs
- `lambdynamiclights` (keep client-side unless you intentionally use a server-safe setup)
