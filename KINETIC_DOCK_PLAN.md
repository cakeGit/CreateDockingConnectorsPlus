# Create: Docking Connectors+ — Kinetic Docking Port Plan

## Goal

Rename this repo/mod from `docking_pipe_connector` / "Create: Docking Pipe Connector" to
`docking_connectors_plus` / "Create: Docking Connectors+", then add a **Docking Kinetic Port**:
a docking connector variant whose back face is a shaft port and whose docked pair bridges Create
kinetic (rotation + stress) networks across sublevels, with the same docking behavior as the
existing pipe variant.

The port transmits a **1:1 signed speed in each port's local frame**: an RSC set to +256 on one
side produces +256 on the other side, regardless of how the two sublevels are oriented in the
world. The two shafts may therefore appear to spin in opposite world directions when viewed by a
shared observer; that is expected and required by this design.

## Decisions (locked 2026-09-11)

- **Full mod-id rename** to `docking_connectors_plus`. Registry ids move namespaces; existing
  worlds using the old id break. Accepted (1.0.0 never released). Major version bump to `2.0.0`.
- **Extract the shared docking core** (magnet pairing, setDock/unDock, constraints, state machine,
  afterMove repair, NBT, connection deps) behind a shared interface + generic core used by both the
  pipe and kinetic variants. Do not copy the docking classes a third time.
- **Docking behavior is identical to the pipe variant.** No orientation gate, no alignment check:
  any locked configuration the normal connector supports is valid, including forced/odd relative
  orientations.
- **Kinetic link modifier is unconditionally `+1`** while the pair is locked. Kinetics are local
  (plot-space) data; Sable only rotates poses at render/physics time, so `+1` preserves the signed
  local speed and the "always dock with clockwise" standard. No `logicalPose` math is involved in
  the link or in the speed compatibility checks.
- **Opposing-sign coupling must never destroy the port.** Pre-check at link time; a
  `RotationPropagator.propagateNewSource` destroy-guard mixin as backstop. The pair stays docked
  with the clutch open (networks split), plays grinding particles while mismatched, shows a goggle
  warning, and merges the networks automatically once the directions match.
- **One new mixin** total (the destroy guard). Everything else uses Create/Simulated public hooks.

## References

- `reference/create` — Create sources; **note this checkout declares 6.0.11, the build pins
  `create_version=6.0.10-280`**. Verify mixin targets/signatures against the 6.0.10-280 sources
  jar (extracted at `/tmp/opencode/create610src`).
- `reference/Simulated-Project/simulated/...` — docking connector, magnets, Swivel Bearing/Plate.
- `reference/sable` — sublevel representation and pose movement.
- `PLAN.md` — the completed pipe connector workstreams (repo setup, block parity, fluid bridge).
- `agents.md` — code rules (fail fast, no swallowed exceptions, no FQNs when imports work, no
  final classes / unused private no-arg ctors, no comments).

## Key technical facts

- Sublevels are chunk plots inside the **same `Level`**; `level.getBlockEntity(partnerPos)`
  resolves across sublevels (`reference/sable/.../SubLevel.java:15-22`,
  `.../plot/ServerLevelPlot.java:229`, `.../ActiveSableCompanion.java:66-79`). Physics movement only
  changes the pose (`SubLevelPhysicsSystem.java:313-353`), so block/BE positions and kinetic
  networks stay valid while a sublevel moves.
- Kinetic networks are per-`Level` (`TorquePropagator.networks`), so a docked pair joins one
  `KineticNetwork` and stress/capacity merge automatically.
- Create's propagation consults three public `KineticBlockEntity` hooks:
  `addPropagationLocations` (`:556-570`), `isCustomConnection` (`:584-586`), and
  `propagateRotationTo` (`:541-544`); called from `RotationPropagator` at `:439`, `:407`, `:71-73`.
  Neighbour discovery also requires both blocks to implement `IRotate`
  (`RotationPropagator.java:45-51`, `:383-400`). `IRotate extends IWrenchable`
  (`IRotate.java:17`), so a `KineticBlock` gets wrench handling for free.
- Proven cross-sublevel kinetic precedent: Swivel Bearing/Plate uses exactly this pattern
  (`SwivelBearingBlockEntity.java:753-770`, `SwivelBearingPlateBlockEntity.java:72-88`) with no
  mixins.
- Destroy sites to guard, all inside `RotationPropagator.propagateNewSource` (`:219-300`):
  too-fast/flicker `:240-243`, incompatible directions `:246-249`, cycle guard `:272`.
  Incompatibility test is `sign(newSpeed) != sign(neighbourSpeed)` (`:232-233`); with modifier `+1`
  this is a direct local signed-speed comparison.
- Simulated precedent for a `RotationPropagator` mixin: `mixin/flicker_tally_removal/RotationPropagatorMixin.java`
  (priority `100000`, wraps `getFlickerScore`). Different invoke target, so the two can coexist.
- Magnet generics: `MagnetPair<T extends BlockEntity & SimMagnet>` (`MagnetPair.java:23`),
  `MagnetMap<T extends BlockEntity & SimMagnet>` (`MagnetMap.java:15`), `MagnetBehaviour` takes
  `SmartBlockEntity` + `MagnetMap<?>` (`MagnetBehaviour.java:17`). A shared `DockingConnector`
  interface extending `SimMagnet` satisfies the bound with an additional `BlockEntity` bound.

## Workstream 1 — repo/mod rename (`docking_connectors_plus`)

Status (2026-09-11): **done**.

- `gradle.properties`: `mod_id=docking_connectors_plus`, `mod_name=Create: Docking Connectors+`,
  `mod_group_id=com.cake.docking_connectors_plus`, `mod_version=2.0.0`, description covers both
  variants.
- Java package `com.cake.docking_pipe_connector` → `com.cake.docking_connectors_plus`; mod class
  `DockingPipeConnector` → `DockingConnectorsPlus`; per-variant `DockingPipeConnector*` class names
  kept.
- Mixin config renamed to `docking_connectors_plus.mixins.json` and repointed at the new package;
  `neoforge.mods.toml` picks it up via `${mod_id}`.
- Asset/data namespaces renamed to `docking_connectors_plus`; model texture references and
  physics-block-property selectors repointed; registry names
  (`docking_pipe_connector`, `paired_docking_pipe_connector`) unchanged.
- Old generated namespace and `.cache` deleted; `runData` regenerated everything under the new
  namespace.
- `build.gradle`: `mods` block and publish slugs/display renames done; publish project ids remain
  TODO placeholders. `readme.md`, `changelog.md` (`# 1.21.1-2.0.0` breaking rename entry) and the
  `PLAN.md` header note updated.
- Acceptance: `./gradlew clean build` and `runData` green; `runClient` loaded
  `Create: Docking Connectors+ 2.0.0 (docking_connectors_plus)`, selected
  `docking_connectors_plus.mixins.json` (5 mixins applied), zero `[ERROR]` lines.
  `grep -r docking_pipe_connector src` shows only registry names, per-variant class names, and
  model paths.

Files/tasks:

- `gradle.properties`: `mod_id=docking_connectors_plus`, `mod_name=Create: Docking Connectors+`,
  `mod_group_id=com.cake.docking_connectors_plus`, new `mod_description` covering both connectors,
  `mod_version=2.0.0`.
- Java package `com.cake.docking_pipe_connector` → `com.cake.docking_connectors_plus`; mod class
  `DockingPipeConnector` → `DockingConnectorsPlus`; constants updated. Keep per-variant class names
  (`DockingPipeConnector*` still describes the pipe variant).
- Mixin config `src/main/resources/docking_pipe_connector.mixins.json` →
  `docking_connectors_plus.mixins.json`; update `"package"`. `neoforge.mods.toml` already derives
  the config name from `${mod_id}`.
- Rename asset/data namespaces `assets|data/docking_pipe_connector` → `.../docking_connectors_plus`;
  registry names stay (`docking_pipe_connector`, `paired_docking_pipe_connector`), only the
  namespace changes.
- Delete `src/generated/resources/{assets,data}/docking_pipe_connector` and `.cache` entries;
  re-run data to regenerate under the new namespace.
- `build.gradle`: `mods { docking_connectors_plus { ... } }`; `publishMods` project ids/slugs
  (TODO placeholders) updated; keep commented discord block.
- `readme.md`, `changelog.md` (new `# 1.21.1-2.0.0` breaking entry per ReleaseGuard format),
  `PLAN.md` header note, logos if desired.
- Acceptance: `./gradlew clean build`, `runData`, `runClient` all green; no references to the old
  id/package remain (`grep -r docking_pipe_connector src` expect only registry names).

## Workstream 2 — extract shared docking core

Status (2026-09-11): **done** (builds; in-game pipe regression left to manual pass).

- New `content/blocks/docking_connector/shared` package:
  - `DockingConnector` `extends SimMagnet` — position/state/sublevel accessors, `isLocked`/`hasOtherConnector`
    defaults, visual accessors, `setDock`/`unDock`/`pairTo`, `onDockingLinkLocked`/`onDockingLinkBroken`
    hooks, paired-marker supplier, `POWERED`/`EXTENDED` constants.
  - `DockingConnectorState` enum (replaces the BE-nested enum).
  - `DockingConnectorCore<T extends SmartBlockEntity & DockingConnector>` — behaviour creation, pair
    search, partner validation, `pairTo`/`setDock`/`unDock`/`updateState`, constraint smoothing,
    connection dependencies, docking NBT, tip/magnet/orientation/moment helpers.
  - `DockingConnectorPair<T extends BlockEntity & DockingConnector>` — old pair generalized; static tip
    helpers take `DockingConnector`.
  - `DockingConnectorBlockOps` — marker removal, neighbor signal, shape, comparator, `afterMove`.
  - `PairedDockingConnectorBlock` — shared marker block base.
  - `DockingConnectorRenderer<T>` + `DockingConnectorModels` — shared piston/feet rendering with
    per-variant partial models (the kinetic variant supplies its own model set in Workstream 5).
- `DockingPipeConnectorBlockEntity` is thin now: keeps powered/extension/feet and the fluid behaviour,
  delegates docking to the core; the fluid lifecycle lives in the two hooks (`wipePressure` +
  `propagateChangedPipe` on lock, `resetAffectedFluidNetworks` on unlock).
- `DockingPipeConnectorPair` deleted; pipe block, paired block and renderer use the shared helpers.
  The five fluid mixin classes are untouched.
- Core is constructed in `addBehaviours` because `SmartBlockEntity` calls it before subclass field
  initializers.
- Acceptance: `./gradlew build` and `runData` green; in-game pipe docking/fluid scenarios pending manual
  verification.

New shared package `content/blocks/docking_connector/shared` (final location TBD):

- `DockingConnector` interface `extends SimMagnet`:
  - Accessors: `getOtherConnectorPosition` / setter, `getOtherConnectorSubLevelId` / setter,
    `DockingConnectorState getDockingState()` / setter, `isExtended()`, `isLocked()` (default from
    state), plus `getBlockPos()`/`getLevel()`/`getBlockState()` from `BlockEntity`
    (`getTheoreticalSpeed` where needed).
  - Variant hooks: `void onDockingLinkLocked()`, `void onDockingLinkBroken()`,
    `boolean isPairedMarker(BlockState)` / `Supplier<Block> getPairedMarkerBlock()`,
    `BlockPos getTipPosition()`.
- `DockingConnectorCore<T extends BlockEntity & DockingConnector>`: owns one `MagnetMap<T>` ref and
  holds all logic moved out of `DockingPipeConnectorBlockEntity`:
  - `createBehaviour()` (`MagnetBehaviour`), `getOtherConnector()`, `searchForPairs()`,
    `pairTo()`, `setDock()`, `unDock()`, `updateState()`, `attachConstraints()`,
    `removeConstraint()`, `sable$physicsTick` helper, `getConnectionDependencies()` helper,
    `writeDocking(tag)` / `readDocking(tag)` (`OtherConnector`, `OtherConnectorSubLevelId`),
    `getTipPosition()`/`getMagnetPosition()`/`magnetActive()` helpers.
  - Link lifecycle: `setDock` calls `onDockingLinkLocked()` after the constraint lock steps;
    `unDock` calls `onDockingLinkBroken()` before clearing the partner. Pipe maps these to
    `wipePressure` + `propagateChangedPipe` / `resetAffectedFluidNetworks`; kinetic maps them to the
    clutch logic (Workstream 3).
- `DockingConnectorPair<T extends BlockEntity & DockingConnector> extends MagnetPair<T>`: the
  current `DockingPipeConnectorPair` moved here; static relative-position/tip helpers generalized.
- `DockingConnectorRenderer<T>` base (optional but preferred): shared piston extension/feet
  rendering, per-variant partial models.
- Per-variant statics stay: `MAGNET_CONTROLLER` map and pair class instantiation via
  `DockingConnectorPair::new` (generics make the map per-variant already).
- `DockingPipeConnectorBlockEntity` becomes a thin `SmartBlockEntity implements DockingConnector`:
  keeps `extension`/`feet`/render state and the fluid behaviour, delegates docking to the core, and
  keeps its existing fluid lifecycle behavior via the two hooks.
- Block-level boilerplate (marker placement/removal, comparator, `neighborChanged`, shape,
  `afterMove`) is shared through a small `DockingConnectorBlockOps` static helper (or an interface
  with defaults) called by both block variants; superclass differences
  (`WrenchableDirectionalBlock` vs `DirectionalKineticBlock`) are preserved.

Acceptance: pipe connector behavior unchanged (docking/undocking, marker, comparator, fluid
bridge); `./gradlew build` + `runData` clean; existing fluid mixins untouched and still apply.

## Workstream 3 — Docking Kinetic Port block + BE

Status (2026-09-11): **done** (build/runData/runClient green; in-game kinetic scenarios left to manual pass).

- Registry id `docking_kinetic_connector` / display name **"Docking Kinetic Connector"** (`paired_docking_kinetic_connector` marker).
- `DockingConnectorExtension` added to the shared package: powered/extension/feet state + chaser tick moved
  out of `DockingPipeConnectorBlockEntity` (both variants delegate). This was an approved extra extraction;
  pipe behavior is a mechanical move, regression left to the manual pass.
- New `content/blocks/docking_kinetic_connector`: block, BE, paired marker block, renderer; kinetic
  blockstate/loot/tags/lang generated via `runData`; `physics_block_properties` added for both kinetic blocks.
- Leftover `DockingPipeConnector` index names consolidated: `DockingConnectorBlocks` (both variants +
  markers), `DockingConnectorBlockEntities`, `DockingConnectorPartialModels`, `DockingConnectorDisplaySources`;
  display source class is `DockingConnectorDisplaySource` (registry id `docking_connector_display`) and is
  shared by both variants. `DockingPipeConnectorCommonEvents` renamed to `DockingConnectorCommonEvents`.
- Creative tab: pipe connector then kinetic connector inserted directly after Simulated's docking connector
  under the same section.
- Mixin destroy guard landed in WS4; mismatch feedback was revised to keep the pair docked with
  grinding particles + a goggle warning and auto-merge once directions match (no cooldown/retry).
- Disconnect re-root fix: `addPropagationLocations` exposes the partner whenever the pair pointers are mutual
  (independent of `linkActive`), and `onDockingLinkBroken` clears both `linkActive` latches before
  `detachKinetics`, so `RotationPropagator.handleRemoved` can re-root the downstream side through the dying
  link without live propagation crossing it.
- Recipe and item placement left to Workstream 5.

Registration (mirror `DockingPipeConnectorBlocks` / `...BlockEntities`):

- `docking_kinetic_connector` (display name "Docking Kinetic Port" — registry id can flip to
  `docking_kinetic_port` if preferred), plus `paired_docking_kinetic_connector` marker.
- `NON_MOVABLE`, stone properties, netherite sound, `noOcclusion`, `dynamicShape`, display source,
  cutoutMipped, `isRedstoneConductor(false)`, `forceSolidOn`; item model, blockstate gen, loot,
  `data/create/tags/block/non_movable.json`, `mineable/pickaxe`, `physics_block_properties`.

Block `DockingKineticConnectorBlock extends DirectionalKineticBlock implements IBE<...>,
BlockSubLevelAssemblyListener`:

- Keeps `POWERED` + `EXTENDED`; `FACING` comes from `DirectionalKineticBlock`.
- `hasShaftTowards(...)`: `face == state.getValue(FACING).getOpposite()` (back port only); dock face
  never a real shaft face.
- `getRotationAxis(state)`: `state.getValue(FACING).getAxis()`.
- Placement/wrench: inherit `DirectionalKineticBlock` placement (snaps to an adjacent shaft on the
  back); override `onWrenched` to force-unDock first (SwivelBearingBlock pattern) because a FACING
  flip within the same axis is kinetically "equivalent" to `switchToBlockState`
  (`KineticBlockEntity.java:381-411`) and would otherwise leave a stale dock.
- Share marker/redstone/comparator/shape/`afterMove` logic with the pipe block via Workstream 2.

BE `DockingKineticConnectorBlockEntity extends KineticBlockEntity implements DockingConnector,
BlockEntitySubLevelActor`:

- Owns `DockingConnectorCore<Self>` and `MagnetBehaviour`; `calculateStressApplied()` returns `0`
  (passthrough, same as Swivel Bearing).
- Propagation hooks, gated on `linkActive` + mutual lock + `level.isLoaded(partnerPos)`:
  - `addPropagationLocations`: add partner pos.
  - `isCustomConnection(other)`: true iff `other == getOtherConnector()`.
  - `propagateRotationTo(target, ...)`: `1f` for the partner, else `super` (which handles the real
    back-face shaft connection, since the block is `IRotate`).
- Link/clutch state:
  - `linkActive` boolean, `speedMismatch` boolean (synced via the BE update tag).
  - `onDockingLinkLocked()` (server): if the partner exists and both `getTheoreticalSpeed()` values
    are non-zero with opposing signs, call `onSpeedMismatch()`; otherwise set `linkActive` on
    both ends, then `detachKinetics(); updateSpeed = true;` on both (ChainConveyor pattern) so the
    BFS merges/splits.
  - `onDockingLinkBroken()`: clear `linkActive` and `speedMismatch` on both ends,
    `detachKinetics(); updateSpeed = true;` on both.
  - `onSpeedMismatch()`: open the clutch on both ends (`linkActive = false`); flag `speedMismatch`
    on both and sync it. The pair stays docked; nothing is destroyed or undocked.
  - `tick()`: `super.tick()` + shared docking tick; client side plays swivel-bearing-style
    `ParticleTypes.CRIT` grinding particles at the port tip while `speedMismatch` is set.
  - `tickLink()`: while the pair is locked and mutually paired but not linked, re-run the
    compatibility check every tick; mismatched → `onSpeedMismatch()`, compatible →
    `activateLink()` (clears the flag and merges the networks).
  - `addToGoggleTooltip()`: when `speedMismatch` is set, shows a GOLD "Kinetic Networks Mismatch"
    header plus a GRAY_AND_WHITE cut description, in the same style as Create's/Simulated's
    contraption assembly warnings.
- Lifecycle: `initialize()` re-docks through the shared core (already existing behavior), which
  re-fires the link hook; `read`/`write` call `super` (kinetic NBT) plus core NBT; `remove()`
  detaches kinetics.

## Workstream 4 — destroy guard mixin (clutch backstop)

Status (2026-09-11): **done** (build green; mixin applied in `runClient` with `mixin.debug.verify`;
mismatch feedback updated to stay docked with grinding particles + goggle warning; in-game scenario 3
left to the manual pass).

`mixin/RotationPropagatorDestroyGuardMixin.java`:

- `@Mixin(RotationPropagator.class)`, `@WrapOperation` on
  `Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z` inside
  `propagateNewSource` (no ordinal — covers all three destroy sites).
- If `instance.getBlockEntity(pos) instanceof DockingKineticConnectorBlockEntity connector`, call
  `connector.onSpeedMismatch()` and return `false`; otherwise `original.call(...)`.
- Why needed: once a link exists, starting a new opposing generator makes the BFS reach the
  connector, at which point `currentTE` is the connector and Create tries to destroy it
  (`:232-249`). The pre-check at link time cannot see this.
- Register the mixin in `docking_connectors_plus.mixins.json`. `defaultRequire: 1` stays.
  Verify against 6.0.10-280 sources; keep the injector narrow so a Create update fails fast rather
  than silently missing. Coexists with Simulated's flicker mixin (different invoke target).

Verified:

- `javap` on the 6.0.10-280 jar shows exactly three
  `Level.destroyBlock:(Lnet/minecraft/core/BlockPos;Z)Z` call sites in `propagateNewSource`.
- `runClient` (quick-play into the dev world) logs
  `Mixing RotationPropagatorDestroyGuardMixin from docking_connectors_plus.mixins.json into
  com.simibubi.create.content.kinetics.RotationPropagator` next to Simulated's two
  `RotationPropagator` mixins, with zero `[ERROR]` lines.

## Workstream 5 — recipe

Status (2026-09-11): **done** (recipe gen renamed, kinetic recipe added, `runData` green).

- Mechanical crafting recipe in `DockingConnectorsPlusRecipeGen` (renamed from
  `DockingPipeConnectorRecipeGen`) for `docking_kinetic_connector`:
  `ICI` / ` S ` / `PAP` / `LTL` (I=iron sheet, C=cogwheel, S=shaft, P=piston,
  A=andesite casing, L=andesite alloy, T=electron tube), returns 1.

## Workstream 6 — validation (For human to do)

Status (2026-09-11): not started.

Acceptance scenarios:

1. Signed speed preserved: RSC +256 on A → +256 on B; RSC -256 on A → -256 on B; goggles read equal
   values. Port faces and sublevel orientations arbitrary.
2. Generators on both sides set to the same value merge cleanly (same-sign branch); different
   magnitudes merge with the faster source winning.
3. Opposing signs: dock two live networks with opposite speeds → port stays docked and is not
   destroyed, grinding particles play, goggle warning names the mismatch; aligning one side
   merges the networks.
4. Stress/capacity shared: generator on A powers a machine on B; overstress stalls both sides and
   recovers when capacity is added.
5. Undock mid-spin: both sides re-root independently, no dupe/void; re-dock resumes.
6. Save/reload while docked; sublevel unload/reload while docked (connection deps); dedicated
   server.
7. Physics movement while docked; sublevel assembly move (`afterMove` re-pair + kinetic re-attach).
8. Regression: pipe connector still docks/undocks and bridges fluids; existing five fluid mixins
   apply; new mixin applies with `mixin.debug.verify`/`verbose` in the client run.
9. `./gradlew clean build` and `runData` clean.

## Sequencing

1. Workstream 1 (rename) → build/runData/runClient green.
2. Workstream 2 (shared core) + pipe regression → runClient + fluid acceptance re-check.
3. Workstream 3 + 5 (kinetic block/BE/assets) → validate single-level first, then across
   sublevels.
4. Workstream 4 (mixin + clutch/particles/retry) → validate scenario 3.
5. Workstream 6 full pass; update statuses in this file.

## Risks / open items

- CurseForge/Modrinth project ids/slugs for the renamed project (TODO placeholders in
  `build.gradle`).
- Mixin target verified on Create 6.0.10-280 (reference checkout is 6.0.11).
- Generic extraction must satisfy `MagnetPair<T extends BlockEntity & SimMagnet>`; the shared
  interface must carry the `BlockEntity` bound and per-variant `MagnetMap` statics stay.
- Mismatch feedback is continuous (grinding particles + goggle warning) rather than
  time-limited; revisit if noisy.
- `force`/`pairTo` docking paths must run the same pre-check and failure handling as normal locks.
- Contraptions: kinetic connector stays `NON_MOVABLE`; sublevel assembly uses `afterMove`.
- Renderer scope: whether the kinetic port gets a rotating shaft visual or just the port model.
