# Create: Docking Pipe Connector — Implementation Plan

> Renamed to **Create: Docking Connectors+** (`docking_connectors_plus`) in 2.0.0; the
> workstreams below predate the rename and keep their historical mod id/namespace. See
> `KINETIC_DOCK_PLAN.md` for the rename and kinetic port work.

## Goal

NeoForge 1.21.1 Create 6.0.10-280 addon with a **hard dependency on Simulated**. Adds a docking connector variant with a `pipe` port on the back face (opposite the docking face) whose docked pair bridges Create fluid networks across sublevels, so a pump on one side can pull fluid from a tank on the other side without a pump there (target scenario `CD|DPT`).

Dependency decision: the distributed Simulated bundle is **Create: Aeronautics** (Modrinth `oWaK0Q19`, CF `676721`, see `reference/Simulated-Project/aeronautics-bundled/build.gradle:102-122`); the `simulated` mod id is present at runtime via the bundled jar. Compile/dev dependency uses the standalone maven artifact `dev.simulated_team.simulated:simulated-neoforge-1.21.1` (RyanHCode maven, versions `1.0.2-6` … `1.3.1`; reference source is `1.3.0`).

Open decisions (confirm at kickoff; defaults proposed):

- `mod_id` — `docking_pipe_connector`
- `mod_name` — `Create: Docking Pipe Connector`
- `mod_group_id` — `com.cake.docking_pipe_connector`
- initial `mod_version` — `1.0.0`
- CurseForge / Modrinth project IDs and slugs for the new (empty) project — TODO placeholders
- redistribution permission for Simulated code/assets — repo license is "Read attached LICENSE.md" (`reference/Simulated-Project/gradle.properties:6`)

## References

- `reference/Simulated-Project` — Simulated sources (1.3.0); docking connector + sublevel APIs
- `reference/sable` — Sable sources; existing Create compat mixins for sublevels
- `reference/create` — Create sources (6.0.10-280); fluid pump/propagator internals
- `Create-Fluid-Be-Gone/` — build/publish/versioning template
- `Aeronautical-Additions/build.gradle` — in-repo precedent for a hard Simulated dependency
- `release-guard/README.md` — `net.azmod.release` behavior and changelog format
- `agents.md` — code rules (fail fast, no swallowed exceptions, no FQNs when imports work, no final classes / unused private no-arg ctors)

## Workstream 1 — repo setup (subagent A)

Create the repo by cloning Fluid-Be-Gone's build surface file-for-file.

Files:

- `settings.gradle`, `gradlew`/wrapper, `.gitignore`, `.gitattributes`, `changelog.md`, `readme.md`, `logo_hr.png`, `logo_small.png`, `src/main/resources/logo.png` — copy from `Create-Fluid-Be-Gone/`.
- `build.gradle` — mirror `Create-Fluid-Be-Gone/build.gradle`:
  - plugins: `net.neoforged.moddev 2.0.140`, `me.modmuss50.mod-publish-plugin 2.1.1`, `net.azmod.release 0.2.1` (`:1-7`)
  - version scheme: `version = isSnapshotVersioning ? "${mod_version}-SNAPSHOT" : mod_version` (`:9-10`)
  - repositories block (`:24-84`)
  - `neoForge { ... runs ... }` (`:86-115`): data run gains `--existing-mod simulated`
  - `mods { docking_pipe_connector { ... } }` rename
  - `ProcessResources` expansion keys (`:156-176`)
  - `sourcesJar` + `jar` manifest (`:177-186`)
  - `publishMods` (`:188-219`) with new project IDs, `requires("create")` + `requires("create-aeronautics")` (CF) and `requires("create-aeronautics")` + `requires("sable")` (Modrinth); keep the commented discord block
- `gradle.properties` — FbG keys (`:1-25`) plus `simulated_version=1.3.0`, `sable_version=2.0.5`, `veil_version=4.0.1`; keep `mod_version` and snapshot handling.
- Dependencies — FbG style for create/ponder/flywheel/registrate (`:126-150`) **plus**:
  - `implementation "dev.simulated_team.simulated:simulated-neoforge-${minecraft_version}:${simulated_version}"`
  - `implementation "dev.ryanhcode.sable:sable-neoforge-${minecraft_version}:${sable_version}"`
  - `implementation("foundry.veil:veil-neoforge-${minecraft_version}:${veil_version}") { exclude group: "maven.modrinth"; exclude group: "me.fallenbreath" }` (pattern from `Aeronautical-Additions/build.gradle:129-151`)
  - Simulated 1.3.0 POM pulls sable/curios/jei/ponder at compile and flywheel/veil/cc-tweaked at runtime, so dev runs are self-sufficient.
- `src/main/resources/META-INF/neoforge.mods.toml` — copy `Create-Fluid-Be-Gone/src/main/resources/META-INF/neoforge.mods.toml:1-65`, then:
  - `create` required `[6.0.10,)`
  - `simulated` **required** `[1.3.0,)` (ordering `AFTER`, side `BOTH`)
  - `sable` required/after
  - `[[mixins]]` entry for `docking_pipe_connector.mixins.json`
- `src/main/java/.../<Mod>.java` — `@Mod` entrypoint, no-op for now.
- `src/main/resources/docking_pipe_connector.mixins.json` — model `Create-Fluid-Be-Gone/src/main/resources/fluid_be_gone.mixins.json:1-21`.
- `changelog.md` — ReleaseGuard format, H1 headings `# 1.21.1-1.0.0` (`release-guard/README.md:53-64`).

Acceptance:

- `./gradlew build` green.
- `./gradlew runClient` loads with Simulated present.
- Removing `simulated` from the runtime fails per `neoforge.mods.toml`.

Status (2026-09-11): **done**.

- `./gradlew clean build` green; jar packs the expanded `neoforge.mods.toml`, empty mixin config, and `logo.png`.
- `./gradlew runClient` loaded all mods (`create 6.0.10`, `simulated 1.3.0`, `sable 2.0.5`, `docking_pipe_connector 1.0.0`) with zero `[ERROR]` lines; `docking_pipe_connector.mixins.json` selected.
- `./gradlew runData` green (mod's Registrate provider ran, includes `--existing-mod simulated`).
- Negative test: excluding `dev.simulated_team.simulated` from `runtimeClasspath` aborts pre-load with `Mod docking_pipe_connector requires simulated 1.3.0 or above`.
- Repo additions vs FbG template: `dev.simulated_team.simulated` + `dev.ryanhcode.sable-companion` added to the RyanHCode maven `exclusiveContent` filter; added Curios (`maven.theillusivec4.top`) and CC:Tweaked (`maven.squiddev.cc`) repos for Simulated's transitive deps; `compileOnly` Registrate restored (Create dep is non-transitive). The azmod-modmaven-mirror `exclusiveContent` block (FbG form, conditional credentials) is the sole `maven.modrinth` source; the direct `api.modrinth.com` block was omitted because two overlapping `exclusiveContent` filters for the same group break resolution (each repo alone resolves; together the module is found in neither, with no repo even searched). Verified with fresh `GRADLE_USER_HOME`s and by purging the local compass artifacts: the mirror re-served both, proxying Modrinth.

## Workstream 2 — block parity, own registrate (subagent B)

Copy the docking connector implementation into the new mod's namespace and repoint pair detection at the new blocks so it only ever connects to its own kind.

Source copy set (`reference/Simulated-Project/simulated/common/src/main/java/dev/simulated_team/simulated/`):

- `content/blocks/docking_connector/` — `DockingConnectorBlock`, `DockingConnectorBlockEntity`, `DockingConnectorPair`, `DockingConnectorTank`, `DockingConnectorSoloInventory`, `DockingConnectorDuoInventory`, `DockingConnectorBattery`, `DockingConnectorRenderer`, `PairedDockingConnectorBlock`
- `compat/computercraft/wired/` — `DockingConnectorWiredElement`, `NoopDockingConnectorWiredElement`, `DockingConnectorWiredElementImpl`

Rename `DockingConnector*` → `DockingPipeConnector*`; paired block → `PairedDockingPipeConnectorBlock`.

Registration (model on `index/SimBlocks.java:546-577` and `index/SimBlockEntityTypes.java:112-119`):

- main block: `WrenchableDirectionalBlock`, `NON_MOVABLE` tag, stone properties, `SoundType.NETHERITE_BLOCK`, `isRedstoneConductor(false)`, `forceSolidOn`, cutoutMipped, noOcclusion, dynamicShape, display source
- paired marker block: `PairedDockingConnectorBlock` copy
- BE type with inventory/tank/battery capability registration (`SimInventoryService.registerTank`, `:115`) or direct `RegisterCapabilitiesEvent`
- blockstate generator/model matrix mirrored from `docking_connector.json`

Decoupling (every reference that must point at the new entries):

- `DockingConnectorBlockEntity.java:128-136, 161, 278-281` (front marker placement/branch checks)
- `DockingConnectorBlock.java:79-88` (paired removal), `:151-160` (`afterMove` re-pair)
- `PairedDockingConnectorBlock.java:65` (survival check), `:95-98` (`canSurvive`), `:122-124` clone item
- `DockingConnectorBlockEntity.java:82-115` (`getOtherConnector`/`initialize`), `:248-283` (`searchForPairs`), `:366-421` (`setDock`/`unDock`), `:624-634` (duo inventory)
- `SimBlocks.PAIRED_DOCKING_CONNECTOR` / `SimBlocks.DOCKING_CONNECTOR` / `SimBlockEntityTypes.DOCKING_CONNECTOR` → new registrate entries

Assets (copy + rename into new namespace):

- blockstates, `models/block/docking_connector/{block,block_powered,foot,item,main_piston_1,main_piston_2,side_piston_1,side_piston_2}.json`, item model
- textures `pipe_dock.png`, `pipe_dock_2.png`, `pipe_dock_powered.png` (not copies of the plain docking connector textures)
- loot table, mechanical-crafting recipe, lang entries
- mechanical-crafting recipe mirrors the docking connector's, but with `create:fluid_pipe` instead of `create:chute` and `c:plates/copper` instead of `c:plates/brass`
- the block item is inserted into Simulated's main creative tab directly after `simulated:docking_connector` (Registrate's default `SEARCH` tab injection must be disabled so the item is not added twice)
- `data/*/physics_block_properties/*.json` (mass/volume with `extended` override) with `selector` changed from `simulated:docking_connector`

Reuse from Simulated (verify public, all checked public): `SimSoundEvents`, `SimConfigService.server().blocks.dockingConnector*`, `SimDisplaySources.DOCKING_CONNECTOR_DISPLAY`, `MagnetBehaviour`/`MagnetMap`/`MagnetPair`/`SimMagnet`, `SingleTank`, `SingleBattery`, `SingleSlotContainer`, `AbstractContainer`, `SimMathUtils`.

Acceptance:

- two new connectors dock/undock; comparator, inventory, battery, CC behavior matches the original
- new connector never pairs with `simulated:docking_connector` and vice versa
- `./gradlew build` and `./gradlew runData` clean

Status (2026-09-11): **done**.

- All docking connector classes copied into `com.cake.docking_pipe_connector.content.blocks.docking_pipe_connector` with their own `MagnetMap`, pair class, and block/BE registrate entries, so the new connector only pairs with its own kind.
- Own `DockingPipeConnectorDisplaySource` (the Simulated one instanceof-checks its own BE) and `DockingPipeConnectorPartialModels`.
- CC wired-element copy compiles against `cc.tweaked:cc-tweaked-1.21.1-forge-api` (`compileOnly`); the full mod arrives at runtime transitively via Simulated.
- Textures use the supplied `pipe_dock` / `pipe_dock_2` / `pipe_dock_powered`; models were copied with texture references repointed to `docking_pipe_connector:block/...`.
- `DockingPipeConnectorRecipeGen` datagens the mechanical-crafting recipe: same pattern as the docking connector, `create:fluid_pipe` in place of chutes and `c:plates/copper` in place of brass plates, result count 2.
- Creative tab: at `FMLCommonSetupEvent` the item supplier is inserted into `SimulatedRegistrate.TAB_ITEMS` directly after the docking connector, and inherits its section. `CreateRegistrate`'s default `SEARCH` tab injection is disabled (`defaultCreativeTab(null)`) because it duplicated the item against the Simulated tab's search entries at tab rebuild (found via `runClient` crash `Itemstack ... already exists in the tab's list`).
- `./gradlew build` and `./gradlew runData` green; generated blockstate/loot/lang/recipe verified. `./gradlew runClient` loads the mod, joins a world and runs the integrated server with no mod-related errors. In-game docking (comparator/inventory/battery/CC) was not manually exercised.

## Workstream 3 — fluid bridge via mixins (subagent C)

Architecture (Plan A, full fidelity):

- `DockingPipeConnectorBlockEntity` participates in `FluidTransportBehaviour` with:
  - **back port** (opposite `FACING`): real pipe connection, `canHaveFlowToward` true, rendered pipe rim
  - **dock face** (`FACING`): virtual connection only while locked (`DockingConnectorBlockEntity.java:329-331`), implemented as a custom `LinkedPipeConnection extends PipeConnection` (`PipeConnection.java:34-58`; `interfaces` map is public at `FluidTransportBehaviour.java:41`, so the custom instance can replace the default after `initialize`/dock)
- `DockingFluidLink` API on the copied BE returning the partner BE's fluid behaviour, plus a virtual-position flag if cross-dock lookups need routing (mirror `util/extra_kinetics/ExtraKinetics.java:14-25` and `ExtraBlockPos.java:10-15`)

Mixin targets (Create 6.0.10-280; paths under `reference/create/src/main/java/com/simibubi/create/content/fluids/`):

1. `pump/PumpBlockEntity.java`
   - `distributePressureTo` BFS (`:119-228`): when current pos is a linked connector and the faced direction is the dock face, continue the frontier at the partner connector's back port; pull orientation passes through unchanged
   - `hasReachedValidEndpoint` (`:272-298`): dock face must not classify as endpoint/open end
2. `FluidPropagator.java`
   - `propagateChangedPipe` (`:49-98`): same cross-dock jump
   - leave `getPipe`/`getPipeConnections` (`:164-166`, `:190-196`) working normally for the connector's own behaviour
   - call `propagateChangedPipe` on dock/undock (pattern: `PumpBlockEntity.java:99-110`, `FluidValveBlock.java:96, 132`, `SmartFluidPipeBlock.java:99, 134`)
3. `PipeConnection.java`
   - `determineSource` (`:163-187`): dock-face connection returns a custom `FlowSource` resolving to the partner's back connection outward flow (`OtherPipe` semantics, `FlowSource.java:120-152`) instead of `Blocked`
4. `FluidNetwork.java`
   - `get`/`getFluidTransfer` (`:302-327`) and frontier walk in `tick()` (`:88-159`): resolve dock-face position to the partner connection so Layer III transfer crosses the pair

Lifecycle wiring in the copied BE:

- on lock (`setDock`, copy of `DockingConnectorBlockEntity.java:380-392`): connect link, `FluidPropagator.propagateChangedPipe` on back neighbour, `wipePressure`
- on unlock (`unDock`, `:403-421`): flush/reset affected flows (`FluidPropagator.resetAffectedFluidNetworks`), disconnect, re-propagate
- `initialize`/`remove`/NBT (`:96-115`, `:438-503`), sublevel load/unload (`sable$getConnectionDependencies`, `:546-558`)

Guard rails (agents.md): fail fast, no swallowed exceptions; no FQNs where imports work; null-safe when partner unloaded (`level.isLoaded`, `getOtherConnector()`); cache the link like `DockingConnectorTank.connectedTank` (`DockingConnectorTank.java:14, 42`).

Plan B (fallback if BFS mixins are too brittle): make the dock face a self-contained `IFluidHandler` endpoint whose handler proxies the partner side; lower fidelity (bypasses Create pressure semantics).

Acceptance scenarios:

1. `CD|DPT` — pump on B pulls from the creative tank on A with no pump on A
2. reverse direction; two pumps; closed `FluidValve` in-line; pipe filter on the port
3. undock mid-flow: no dupe/void; reconnect resumes; comparator state unaffected
4. save/reload while docked; sublevel unload/reload while docked; dedicated server
5. `./gradlew build`, `runClient`/`runServer` clean; mixins apply on Create 6.0.10-280 + Simulated 1.3.0

Status (2026-09-11): **implementation done; in-game acceptance scenarios pending manual verification**.

- Plan A implemented. `DockingPipeConnectorFluidBehaviour` joins the BE to `FluidTransportBehaviour`: the back port (opposite `FACING`) is always a real connection, the dock face (`FACING`) is one only while `LOCKED`.
- `DockingFluidLink` resolves a locked dock face to the partner connector (virtual dock-face-to-dock-face edge, so the connector body is traversed for free and `OtherPipe` semantics give the dock connection the partner's dock outward flow). Five mixins route Create's walks across that edge: `PumpBlockEntity` (`distributePressureTo`, `hasReachedValidEndpoint`, `searchForEndpointRecursively`), `FluidPropagator` (`propagateChangedPipe`, `resetAffectedFluidNetworks`, `isOpenEnd`), `PipeConnection.determineSource`, `FluidNetwork.tick`, `FlowSource.OtherPipe.manageSource`.
- Lifecycle: `setDock` wipes pressures and re-propagates changed pipes on lock; `unDock` resets affected fluid networks on both the dock and back faces, wipes pressures and re-propagates. Re-dock on load and `afterMove` re-pairing flow through the same `setDock`/`unDock` paths.
- Removed for good: item inventory (solo/duo + `Clearable` + block drops + capability registration), FE battery, CC:Tweaked wired element compat, and the inherited internal fluid tank, including their NBT. The `cc.tweaked` compileOnly API and `computercraft_version` property are gone; the squiddev maven repo stays because Simulated still pulls the CC runtime artifact transitively.
- `./gradlew build` green. All five mixins runtime-verified to apply against Create 6.0.10-280 on a dedicated server (the fluid classes load lazily, so they were force-loaded with a temporary debug flag that was removed afterwards).

## Sequencing

Subagent A → subagent B → subagent C. C depends on B's classes and is risk-heavy: checkpoint after each mixin target applies.

Repo deliverables: this `PLAN.md`, per-workstream status notes, final `./gradlew build` green.
