# Yarn (1.20.1) → Mojmap (26.2) Porting Map

Canonical reference for the full grind. Every FQN in the "verified" tables below was checked to
**exist** in the 26.2 merged-deobf jar
(`~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged-deobf/26.2/minecraft-merged-deobf-26.2.jar`).
When a method/field name is uncertain, `javap -cp <jar> <mojmapFQN>` is the ground truth.

This codebase imports **263 unique** `net.minecraft`/`com.mojang.blaze3d` symbols. 26.2 is **mojmap,
unobfuscated** — compile names == runtime names; no refmap. Key 26.2 surprise: `ResourceLocation`
is named **`Identifier`** (`net.minecraft.resources.Identifier`).

## Blocks / block state
| Yarn | Mojmap (26.2, verified) |
|---|---|
| block.Block | world.level.block.Block |
| block.BlockState | world.level.block.state.BlockState |
| block.Blocks | world.level.block.Blocks |
| block.BlockWithEntity | world.level.block.BaseEntityBlock |
| block.BlockRenderType | world.level.block.RenderShape |
| block.FacingBlock | world.level.block.DirectionalBlock |
| block.GlassBlock | world.level.block.**TransparentBlock** |
| block.HorizontalFacingBlock | world.level.block.HorizontalDirectionalBlock |
| block.SlabBlock | world.level.block.SlabBlock |
| block.ShapeContext | world.phys.shapes.CollisionContext |
| block.entity.BlockEntity | world.level.block.entity.BlockEntity |
| block.entity.BlockEntityTicker | world.level.block.entity.BlockEntityTicker |
| block.entity.BlockEntityType | world.level.block.entity.BlockEntityType |
| block.entity.LootableContainerBlockEntity | world.level.block.entity.RandomizableContainerBlockEntity |
| block.entity.StructureBlockBlockEntity | world.level.block.entity.StructureBlockEntity |
| block.entity.ViewerCountManager | world.level.block.entity.ContainerOpenersCounter |
| block.enums.BlockHalf | world.level.block.state.properties.Half |
| block.enums.WallMountLocation | world.level.block.state.properties.AttachFace |
| state.StateManager | world.level.block.state.StateDefinition |
| state.property.BooleanProperty | world.level.block.state.properties.BooleanProperty |
| state.property.IntProperty | world.level.block.state.properties.IntegerProperty |
| state.property.EnumProperty | world.level.block.state.properties.EnumProperty |
| state.property.DirectionProperty | (no class) use EnumProperty&lt;Direction&gt; / BlockStateProperties.FACING |
| state.property.Properties | world.level.block.state.properties.BlockStateProperties |
| sound.BlockSoundGroup | world.level.block.SoundType |

## Entities / AI / attributes / effects
| Yarn | Mojmap (26.2, verified) |
|---|---|
| entity.Entity | world.entity.Entity |
| entity.EntityDimensions | world.entity.EntityDimensions |
| entity.EntityType | world.entity.EntityType |
| entity.ItemEntity | world.entity.item.ItemEntity |
| entity.LightningEntity | world.entity.LightningBolt |
| entity.LivingEntity | world.entity.LivingEntity |
| entity.MovementType | world.entity.MoverType |
| entity.SpawnGroup | world.entity.MobCategory |
| entity.mob.MobEntity | world.entity.Mob |
| entity.mob.HostileEntity | world.entity.monster.Monster |
| entity.player.PlayerEntity | world.entity.player.Player |
| entity.player.PlayerInventory | world.entity.player.Inventory |
| entity.projectile.ArrowEntity | world.entity.projectile.**arrow.Arrow** |
| entity.ai.TargetPredicate | world.entity.ai.targeting.TargetingConditions |
| entity.ai.control.BodyControl | world.entity.ai.control.BodyRotationControl |
| entity.ai.control.LookControl | world.entity.ai.control.LookControl |
| entity.ai.goal.ActiveTargetGoal | world.entity.ai.goal.target.NearestAttackableTargetGoal |
| entity.ai.goal.Goal | world.entity.ai.goal.Goal |
| entity.ai.goal.MeleeAttackGoal | world.entity.ai.goal.MeleeAttackGoal |
| entity.ai.goal.TrackTargetGoal | world.entity.ai.goal.target.TargetGoal |
| entity.ai.goal.WanderAroundFarGoal | world.entity.ai.goal.WaterAvoidingRandomStrollGoal |
| entity.ai.pathing.LandPathNodeMaker | world.level.pathfinder.WalkNodeEvaluator |
| entity.ai.pathing.MobNavigation | world.entity.ai.navigation.GroundPathNavigation |
| entity.ai.pathing.PathNode | world.level.pathfinder.Node |
| entity.ai.pathing.PathNodeType | world.level.pathfinder.PathType |
| entity.attribute.DefaultAttributeContainer | world.entity.ai.attributes.AttributeSupplier |
| entity.attribute.EntityAttributeInstance | world.entity.ai.attributes.AttributeInstance |
| entity.attribute.EntityAttributeModifier | world.entity.ai.attributes.AttributeModifier |
| entity.attribute.EntityAttributes | world.entity.ai.attributes.Attributes |
| entity.damage.DamageSource | world.damagesource.DamageSource |
| entity.damage.DamageType | world.damagesource.DamageType |
| entity.damage.DamageTypes | world.damagesource.DamageTypes |
| entity.effect.StatusEffectInstance | world.effect.MobEffectInstance |
| entity.effect.StatusEffects | world.effect.MobEffects |
| predicate.entity.EntityPredicates | world.entity.EntitySelector |

## Items / inventory / food
| Yarn | Mojmap (26.2, verified) |
|---|---|
| item.Item | world.item.Item |
| item.Items | world.item.Items |
| item.ItemStack | world.item.ItemStack |
| item.BlockItem | world.item.BlockItem |
| item.ItemGroup | world.item.CreativeModeTab |
| item.ItemPlacementContext | world.item.context.BlockPlaceContext |
| item.FoodComponent | world.food.FoodProperties |
| client.item.TooltipContext | world.item.Item$TooltipContext (tooltip API reworked — see below) |
| inventory.Inventory | world.Container |
| inventory.SimpleInventory | world.SimpleContainer |
| inventory.Inventories | world.ContainerHelper |
| util.ItemScatterer | world.Containers |
| util.collection.DefaultedList | core.NonNullList |
| screen.ScreenHandler | world.inventory.AbstractContainerMenu |
| screen.GenericContainerScreenHandler | world.inventory.ChestMenu |
| screen.PlayerScreenHandler | world.inventory.InventoryMenu |

## World / worldgen / dimensions
| Yarn | Mojmap (26.2, verified) |
|---|---|
| world.World | world.level.Level |
| world.WorldAccess | world.level.LevelAccessor |
| world.BlockView | world.level.BlockGetter |
| world.BlockRenderView | client.renderer.block.BlockAndTintGetter (client pkg!) |
| world.HeightLimitView | world.level.LevelHeightAccessor |
| world.StructureWorldAccess | world.level.WorldGenLevel |
| world.ChunkRegion | server.level.WorldGenRegion |
| world.RaycastContext | world.level.ClipContext |
| world.Heightmap | world.level.levelgen.Heightmap |
| world.GameMode | world.level.GameType |
| world.GameRules | world.level.**gamerules.GameRules** |
| world.TeleportTarget | world.level.portal.TeleportTransition — ctor `(ServerLevel, Vec3 pos, Vec3 delta, float yRot, float xRot, PostTeleportTransition)`; use `TeleportTransition.DO_NOTHING`. Teleport via `entity.teleport(transition)`; `FabricDimensions` is GONE. |
| world.dimension.DimensionType | world.level.dimension.DimensionType |
| world.biome.Biome | world.level.biome.Biome |
| world.biome.source.BiomeSource | world.level.biome.BiomeSource |
| world.biome.source.BiomeAccess | world.level.biome.BiomeManager |
| world.chunk.Chunk | world.level.chunk.ChunkAccess |
| world.chunk.ChunkStatus | world.level.chunk.status.ChunkStatus |
| world.gen.GenerationStep | world.level.levelgen.GenerationStep |
| world.gen.StructureAccessor | world.level.StructureManager |
| world.gen.chunk.ChunkGenerator | world.level.chunk.ChunkGenerator |
| world.gen.chunk.ChunkGeneratorSettings | world.level.levelgen.NoiseGeneratorSettings |
| world.gen.chunk.VerticalBlockSample | world.level.NoiseColumn |
| world.gen.chunk.Blender | world.level.levelgen.blending.Blender |
| world.gen.noise.NoiseConfig | world.level.levelgen.RandomState |
| world.level.LevelProperties | world.level.storage.LevelData |
| structure.StructureTemplate | world.level.levelgen.structure.templatesystem.StructureTemplate |
| structure.StructureTemplateManager | world.level.levelgen.structure.templatesystem.StructureTemplateManager |
| structure.StructurePlacementData | world.level.levelgen.structure.templatesystem.StructurePlaceSettings |
| util.math.noise.PerlinNoiseSampler | world.level.levelgen.synth.ImprovedNoise |
| fluid.FluidState | world.level.material.FluidState |
| fluid.Fluids | world.level.material.Fluids |

## Math / util / registries / core
| Yarn | Mojmap (26.2, verified) |
|---|---|
| util.math.BlockPos | core.BlockPos |
| util.math.Vec3d | world.phys.Vec3 |
| util.math.Vec3i | core.Vec3i |
| util.math.Vec2f | world.phys.Vec2 |
| util.math.Box | world.phys.AABB |
| util.math.ChunkPos | world.level.ChunkPos |
| util.math.Direction | core.Direction |
| util.math.GlobalPos | core.GlobalPos |
| util.math.MathHelper | util.Mth |
| util.math.RotationAxis | com.mojang.math.Axis |
| util.math.random.Random | util.RandomSource |
| util.shape.VoxelShape | world.phys.shapes.VoxelShape |
| util.shape.VoxelShapes | world.phys.shapes.Shapes |
| util.hit.HitResult | world.phys.HitResult |
| util.hit.BlockHitResult | world.phys.BlockHitResult |
| util.Identifier | **resources.Identifier** |
| util.Util | util.Util (unchanged) |
| util.ActionResult | world.InteractionResult |
| util.Hand | world.InteractionHand |
| util.Arm | world.entity.HumanoidArm |
| util.BlockRotation | world.level.block.Rotation |
| util.BlockMirror | world.level.block.Mirror |
| util.Formatting | ChatFormatting |
| registry.Registry | core.Registry |
| registry.Registries | core.registries.BuiltInRegistries |
| registry.RegistryKeys | core.registries.Registries |
| registry.RegistryKey | resources.ResourceKey |
| registry.RegistryBuilder | core.RegistrySetBuilder |
| registry.RegistryWrapper | core.HolderLookup |
| registry.entry.RegistryEntry | core.Holder |
| nbt.NbtCompound | nbt.CompoundTag |
| nbt.NbtElement | nbt.Tag |
| network.PacketByteBuf | network.FriendlyByteBuf |
| text.Text | network.chat.Component |
| text.MutableText | network.chat.MutableComponent |
| sound.SoundEvent | sounds.SoundEvent |
| sound.SoundEvents | sounds.SoundEvents |
| sound.SoundCategory | sounds.SoundSource |
| particle.ParticleEffect | core.particles.ParticleOptions |
| particle.ParticleTypes | core.particles.ParticleTypes |

## Server / commands / networking packets
| Yarn | Mojmap (26.2, verified) |
|---|---|
| server.MinecraftServer | server.MinecraftServer |
| server.PlayerManager | server.players.PlayerList |
| server.world.ServerWorld | server.level.ServerLevel |
| server.world.ServerLightingProvider | server.level.ThreadedLevelLightEngine |
| server.network.ServerPlayerEntity | server.level.ServerPlayer |
| server.network.ServerPlayNetworkHandler | server.network.ServerGamePacketListenerImpl |
| server.command.ServerCommandSource | commands.CommandSourceStack |
| server.command.CommandManager | commands.Commands |
| command.CommandRegistryAccess | commands.CommandBuildContext (verified) |
| command.argument.EntityArgumentType | commands.arguments.EntityArgument (verified) |
| server.dedicated.MinecraftDedicatedServer | server.dedicated.DedicatedServer |
| server.dedicated.ServerPropertiesHandler | server.dedicated.DedicatedServerProperties |
| server.dedicated.AbstractPropertiesHandler | server.dedicated.Settings |
| network.packet.c2s.play.ClientCommandC2SPacket | network.protocol.game.ServerboundPlayerCommandPacket |
| network.packet.s2c.play.PlaySoundS2CPacket | network.protocol.game.ClientboundSoundPacket |
| network.packet.s2c.play.StopSoundS2CPacket | network.protocol.game.ClientboundStopSoundPacket |
| resource.ResourceManager | server.packs.resources.ResourceManager |
| resource.ResourcePackManager | server.packs.repository.PackRepository |
| resource.ResourceType | server.packs.PackType |

**Networking note:** 26.2 custom packets use `CustomPacketPayload` + `StreamCodec` registered via
`PayloadTypeRegistry` / `ClientPlayNetworking`/`ServerPlayNetworking.send(payload)`. The old
`PacketByteBuf`-channel API (`ClientPlayNetworking.send(Identifier, buf)`) is gone — Phase 7 rewrite.
**See the verified "Networking — CustomPacketPayload / StreamCodec (Phase 7)" section below for the exact API.**

**Command note (verified):** registration callback sig is `register(CommandDispatcher<CommandSourceStack>, CommandBuildContext, Commands.CommandSelection)`. `Commands.literal/argument`; `EntityArgument.players()`/`getPlayers(ctx, name)`; `source.getWorld()`→`source.getLevel()` (returns `ServerLevel`); `getEntityOrThrow()`→`getEntityOrException()`. **Permissions: `hasPermissionLevel(int)` is GONE** → `.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))` (the `Commands.LEVEL_*` constants are `PermissionCheck`; `Commands.hasPermission(...)` returns a `PermissionProviderCheck` which implements `Predicate<CommandSourceStack>`). Op level 2 == `LEVEL_GAMEMASTERS`. `FabricDimensions.teleport(...)` is GONE → `entity.teleport(new TeleportTransition(ServerLevel, Vec3 pos, Vec3 delta, float yRot, float xRot, TeleportTransition.DO_NOTHING))`.

## Client — stable (verified)
| Yarn | Mojmap (26.2, verified) |
|---|---|
| client.MinecraftClient | client.Minecraft |
| client.option.KeyBinding | client.KeyMapping |
| client.network.AbstractClientPlayerEntity | client.player.AbstractClientPlayer |
| client.network.ClientPlayerEntity | client.player.LocalPlayer |
| client.world.ClientWorld | client.multiplayer.ClientLevel |
| client.network.ClientPlayNetworkHandler | client.multiplayer.ClientPacketListener |
| client.gui.screen.Screen | client.gui.screens.Screen |
| client.gui.widget.ButtonWidget | client.gui.components.Button |
| client.render.entity.EntityRenderer | client.renderer.entity.EntityRenderer |
| client.render.entity.LivingEntityRenderer | client.renderer.entity.LivingEntityRenderer |
| client.render.block.entity.BlockEntityRenderer | client.renderer.blockentity.BlockEntityRenderer |
| client.render.entity.model.EntityModelLayer | client.model.geom.ModelLayerLocation |
| client.util.math.MatrixStack | com.mojang.blaze3d.vertex.PoseStack |
| client.render.VertexConsumer | com.mojang.blaze3d.vertex.VertexConsumer |
| client.render.Camera | client.Camera |
| client.render.GameRenderer | client.renderer.GameRenderer |
| client.render.WorldRenderer | client.renderer.LevelRenderer |

## Client — render pipeline (⚠ Phase 6, verify each against the new deferred API)
26.2 replaced the immediate-mode/`MultiBufferSource` model with a **deferred `submit()` pipeline**.
These need individual rework, not a 1:1 rename:
- `client.gui.DrawContext` → **`client.gui.GuiGraphicsExtractor`** (`.fill()`/`.text()`; HUD via `HudElementRegistry`).
- `client.render.RenderLayer` / `RenderLayers` → `client.renderer.rendertype.RenderType` / `RenderTypes`.
- `client.render.VertexConsumerProvider` (MultiBufferSource) → **gone**; deferred submit uses a node collector.
- Entity renderers override `submit(state, PoseStack, SubmitNodeCollector, CameraRenderState)`, not `render(...)`.
- `client.render.{Tessellator,BufferBuilder,VertexFormat,VertexFormats,VertexFormatElement,RenderPhase,OverlayTexture,LightmapTextureManager,Frustum,WorldRenderer$*}` — all touched by the rewrite; most were powering Veil-coupled rendering and are dropped/rebuilt Mac-safe.
- `client.gl.{ShaderProgram,GlUniform,VertexBuffer}` → Veil/GLSL-era; **removed** in the Mac-safe rebuild.
- `com.mojang.blaze3d.systems.{RenderSystem,VertexSorter}` → RenderSystem exists but API shifted; verify per call.
- Model/feature renderer classes (`EntityRenderDispatcher`, `MobEntityRenderer`, `PlayerEntityRenderer`, `feature.*`, `model.*`) → map under `client.renderer.entity.*` + GeckoLib 5 (Phase 4) — verify per file.

## Wildcard imports (replace with explicit mojmap imports)
- `net.minecraft.block.*` → `net.minecraft.world.level.block.*` (+ state subpkgs)
- `net.minecraft.state.property.*` → `net.minecraft.world.level.block.state.properties.*`
- `net.minecraft.client.model.*` → `net.minecraft.client.model.*` / `client.model.geom.*` (verify)
- `net.minecraft.client.render.*` → `net.minecraft.client.renderer.*` (Phase 6)

## Common method/idiom renames (verify per call via javap)
- `Identifier(ns, path)` → `Identifier.fromNamespaceAndPath(ns, path)`.
- `World/Level.getBlockState/setBlockState/getBlockEntity` — same names.
- `PlayerEntity.getMainHandStack()` → `Player.getMainHandItem()`; `getStackInHand(hand)` → `getItemInHand(hand)`.
- `World.isClient` → `Level.isClientSide`.
- `Entity.getPos()` → `position()`; `getBlockPos()` → `blockPosition()`.
- `Text.literal/translatable` → `Component.literal/translatable`.
- `MathHelper.x` → `Mth.x`.
- Blocks/items register with `.setId(ResourceKey)` before construction; items also need `assets/<ns>/items/<id>.json`.
- `ServerPlayer.level()` returns `ServerLevel`; `GameProfile` is a record (`id()`/`name()`).
- Permissions: `CommandSourceStack` predicates (e.g. `::isPlayer`), not `hasPermission(int)`.

## Discovered during porting (verified by porting agents — TRUST THESE)
### Registries / keys
- `ResourceKey.location()` is named **`.identifier()`** here (+ `.registry()`); construct via `ResourceKey.create(Registries.X, Identifier)`.
- Yarn `RegistryKeys.WORLD` (the `Registry<Level>`/dimension key) → **`Registries.DIMENSION`** (not a "WORLD" key).
- DamageSource lookup: `world.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key)`.

### Fabric API helpers REMOVED in 26.2 (use vanilla)
- `FabricItemSettings` → `Item.Properties`; `FabricBlockSettings` → `BlockBehaviour.Properties` (`.ofFullCopy(...)`); `FabricEntityTypeBuilder` → `EntityType.Builder.of(...).build(ResourceKey)`; `FabricItemGroup` → `FabricCreativeModeTab.builder()`.
- Fabric `GameRuleRegistry`/`GameRuleFactory` → `GameRuleBuilder.forBoolean(false).category(GameRuleCategory.PLAYER).buildAndRegister(Identifier)`; field type `GameRules.Key<...>` → **`GameRule<Boolean>`** (`net.minecraft.world.level.gamerules.{GameRule,GameRuleCategory}`).
- STILL present: `FabricBlockEntityTypeBuilder`, `FabricDefaultAttributeRegistry`, `FabricBlockEntityTypeBuilder`.

### Blocks / items
- Blocks/items: `.setId(ResourceKey)` before construction; register via `Registry.register(BuiltInRegistries.X, id, obj)`.
- `BlockBehaviour.Properties` renames: `.hardness(f)`→`.destroyTime(f)`, `.noBlockBreakParticles()`→`.noTerrainParticles()`, `.solid()`→`.forceSolidOn()`, `.collidable(false)`→`.noCollision()`, `.nonOpaque()`→`.noOcclusion()`, `.sounds(x)`→`.sound(x)`.
- `Blocks.WHITE_WOOL` → `Blocks.WOOL.white()` (colored variants are a `ColorCollection<Block>`: `.white()`/`.pick(DyeColor)`).
- `FoodComponent` → `FoodProperties` holds NO effects now: `Builder` = `.nutrition(int).saturationModifier(float)`; effects via a `Consumable` component: `Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(MobEffectInstance, prob))`, attach with `Item.Properties.food(props, consumable)`.
- **Item tooltip (VERIFIED via javap on `net.minecraft.world.item.Item`, Phase 6):** Yarn `appendTooltip(ItemStack, World, List<Text>, TooltipContext)` → mojmap **`appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag)`**. Lines are pushed via `tooltipAdder.accept(Component)` (NOT added to a List). `TooltipContext` is the nested `world.item.Item$TooltipContext` (NOT the old `client.item.TooltipContext`); also new param `world.item.component.TooltipDisplay`. `Text.translatable`→`Component.translatable`, `.formatted(Formatting)`→`.withStyle(ChatFormatting...)` (varargs — both styles in one call). `Item.Settings` ctor arg → `Item.Properties`.
- Creative tab: `FabricCreativeModeTab.builder().title(Component).displayItems((params, output) -> output.accept(item))`.

### Sounds / attributes / models
- `SoundEvent.of(id)` → **`SoundEvent.createVariableRangeEvent(id)`**.
- `AttributeModifier` ctor: **`(Identifier, double, Operation)`** (no UUID/String); `Operation.MULTIPLY_TOTAL` → **`ADD_MULTIPLIED_TOTAL`**.
- `EntityModelLayer(Identifier, String)` → `ModelLayerLocation(Identifier, String)`.

### Networking (Phase 7 — full rewrite, not rename)
- `PacketByteBufs` / `ServerPlayNetworking.send(player, Identifier, buf)` channel API is **GONE**. 26.2: `ServerPlayNetworking.send(ServerPlayer, CustomPacketPayload)` only; packets = `CustomPacketPayload` + `StreamCodec` registered via `PayloadTypeRegistry`.
- `ServerPlayer.networkHandler` field → **`.connection`**; `.sendPacket(...)` → `.send(...)`.
- `ClientboundSoundPacket(Holder<SoundEvent>, SoundSource, x, y, z, vol, pitch, seed)`; `RegistryEntry.of(x)` → `Holder.direct(x)`.

### Events / client
- `ServerEntityWorldChangeEvents` → **`ServerEntityLevelChangeEvents`** (`AFTER_PLAYER_CHANGE_WORLD` → `AFTER_PLAYER_CHANGE_LEVEL`).
- `KeyBinding`→`KeyMapping`; `InputUtil`→`com.mojang.blaze3d.platform.InputConstants`; keybind category is a registered **`KeyMapping.Category`** (`Category.register(Identifier)`), not a String. `getBoundKeyLocalizedText()`→`getTranslatedKeyMessage()`.
- **Keybind registration (VERIFIED via fabric-api 0.152.2+26.2 jar, Phase 6):** fabric-api's keybind module was renamed `fabric-key-binding-api-v1` → **`fabric-key-mapping-api-v1`**; class `net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper` → **`net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper`**; method `registerKeyBinding(KeyBinding)` → **`registerKeyMapping(KeyMapping)`** (also `getBoundKeyOf(KeyMapping)`). `KeyMapping` ctor `(String, InputConstants.Type, int, KeyMapping.Category)` is unchanged.
- **Client sound subsystem (VERIFIED via javap, Phase 6):** `client.sound.SoundInstance` → `client.resources.sounds.SoundInstance`; `client.sound.SoundSystem` → `client.sounds.SoundEngine`; `SoundInstance.getId()` → **`getIdentifier()`** (returns `resources.Identifier`); `SoundEngine.stop(SoundInstance)` public (unchanged). Yarn `SoundSystem.sounds` (`Multimap<SoundSource,SoundInstance>`) → **`SoundEngine.instanceBySource`** (same shape; PRIVATE → needs access-widener `accessible field net/minecraft/client/sounds/SoundEngine instanceBySource Lcom/google/common/collect/Multimap;`). `SoundManager.soundEngine` field also private (needs AW). `client.option.SimpleOption<T>` → **`client.OptionInstance<T>`**; `client.util.VideoMode` → **`com.mojang.blaze3d.platform.VideoMode`**.
- `Text.copyContentOnly()`→`.plainCopy()`; `.formatted(...)`→`.withStyle(...)`.
- `MinecraftClient.getSession().getUsername()` → `Minecraft.getUser().getName()`.

### GeckoLib 5 (Phase 4 — VERIFIED via javap on geckolib-5.5.2.jar)
- Package relocated **`software.bernie.geckolib` → `com.geckolib`**. `GeckoLibUtil.addCustomBakedModelFactory(...)` and `GeckoLib.initialize()` are GONE in 5.5.2.
- **Package moves (server/animatable side):** `software.bernie.geckolib.animatable.GeoEntity` → `com.geckolib.animatable.GeoEntity`; `...core.animatable.GeoAnimatable` → `com.geckolib.animatable.GeoAnimatable`; `...core.animatable.instance.AnimatableInstanceCache` → `com.geckolib.animatable.instance.AnimatableInstanceCache`; `...core.animation.AnimatableManager` → **`com.geckolib.animatable.manager.AnimatableManager`** (note `.manager.`); `...core.animation.AnimationController` → `com.geckolib.animation.AnimationController`; `...core.animation.RawAnimation` → `com.geckolib.animation.RawAnimation`; `GeckoLibUtil` → `com.geckolib.util.GeckoLibUtil`.
- **Animatable cache:** `GeckoLibUtil.createInstanceCache(this)` UNCHANGED; `getAnimatableInstanceCache()` UNCHANGED sig.
- **`registerControllers`:** sig is now `registerControllers(AnimatableManager.ControllerRegistrar)` (same method name; `ControllerRegistrar` is `com.geckolib.animatable.manager.AnimatableManager$ControllerRegistrar`, a record with `.add(AnimationController<?>...)`).
- **`AnimationController` ctor DROPPED the leading animatable arg.** v4 `new AnimationController<>(this, "name", transitionTicks, stateHandler)` → v5 **`new AnimationController<>("name", transitionTicks, stateHandler)`** (ctors: `(handler)`, `(String,handler)`, `(String,int,handler)`).
- **`AnimationStateHandler` now takes `AnimationTest<A>` (was `AnimationState<A>`).** `handle(AnimationTest<A>)` returns `PlayState`. On the test object: **`test.setAndContinue(RawAnimation)`** (returns PlayState), `test.setAnimation(RawAnimation)`, `test.isMoving()`, `test.isCurrentAnimation(...)`, `test.controller()`. v4's `state.resetCurrentAnimation(); return null` → **`return PlayState.STOP`** (`com.geckolib.animation.object.PlayState.{CONTINUE,PAUSE,STOP}`).
- **`RawAnimation`:** `RawAnimation.begin()` UNCHANGED; chaining `.then("anim", LoopType.X)` UNCHANGED but **`Animation.LoopType` → `com.geckolib.animation.object.LoopType`** (constants `PLAY_ONCE`, `LOOP`, `HOLD_ON_LAST_FRAME`, `DEFAULT`; also `.thenLoop/.thenPlay/.thenPlayAndHold`).
- `GeoEntity` default methods `triggerAnim(String,String)`/`stopTriggeredAnim(String,String)` UNCHANGED.
- **GeoBone/model-baking pipeline (DEFER to phase6 — render-coupled):** `software.bernie.geckolib.cache.object.GeoBone` → `com.geckolib.cache.model.GeoBone`, but v5 `GeoBone` is a **completely redesigned abstract class** with `final` fields + record-style accessors (`name()`/`pivotX()`/`baseRotX()`, no `getName()`/`setPos()`/`setRot()`/`getCubes()`/`getChildBones()`/`updateRotation()`/`getModelSpaceMatrix()`/`setWorldSpaceMatrix()`) and an abstract `render(RenderPassInfo,PoseStack,VertexConsumer,...)`. `BakedModelFactory`, `loading.json.raw.{Bone,Cube}`, `loading.object.{GeometryTree,BoneStructure,BakedModelFactory}`, `util.RenderUtils`, `GeoModel.getBone()` are all GONE/relocated. The Mowzie IK-on-GeckoLib bridge (`MowzieGeoBone`/`MowzieModelFactory`/`GeoModelAccessor`) cannot be ported until the GeckoLib-5 renderer rework (phase6).
- **Renderer/model classes** (`GeoEntityRenderer`, `GeoModel`, `DefaultedEntityGeoModel`) live under `com.geckolib.{renderer,model}` and use `GeoRenderState`/`RenderPassInfo` — phase6.
- **GeoItem render-provider (VERIFIED via javap, Phase 6):** the item-side render hook changed. v4 `createRenderer(Consumer<Object>)` returning a `software.bernie.geckolib.animatable.client.RenderProvider` whose `getCustomRenderer()` yields a `BuiltinModelItemRenderer` → v5 **`createGeoRenderer(Consumer<GeoRenderProvider>)`** (`com.geckolib.animatable.client.GeoRenderProvider`) whose **`getGeoItemRenderer()`** yields a `com.geckolib.renderer.GeoItemRenderer<?>`. **`GeoItem.makeRenderer(this)` and the manual `getRenderProvider()` override are GONE** — `SingletonGeoAnimatable` provides a default `getRenderProvider()` that caches the renderer. Item cache via `GeckoLibUtil.createInstanceCache(this)` (was `new SingletonAnimatableInstanceCache(this)`). `GeoItemRenderer(GeoModel)` ctor unchanged. `GeoModel.getModelResource/getTextureResource` for an item model take a **`GeoRenderState`** (not the live item); `getAnimationResource` still takes the animatable.

### Entity layer (Phase 4 — VERIFIED vs Mob/Monster/LivingEntity/Entity + goals/nav — TRUST THESE)
**Class renames:** `MobEntity`→**`Mob`**; `HostileEntity`→**`monster.Monster`**; `PlayerEntity`→`player.Player`. Attributes: **`createHostileAttributes()`→`Monster.createMonsterAttributes()`** (`createMobAttributes()` unchanged); **`EntityAttributes.GENERIC_X` → `Attributes.X`** (NO `GENERIC_` prefix: `MAX_HEALTH`,`MOVEMENT_SPEED`,`KNOCKBACK_RESISTANCE`,`FOLLOW_RANGE`,`ATTACK_DAMAGE`; each is a `Holder<Attribute>`); `DefaultAttributeContainer.Builder`→**`AttributeSupplier.Builder`** (`.add(Holder<Attribute>,double)`).
**Entity method renames:** `getWorld()`/`world`→**`level()`**; `World.isClient`→`level().isClientSide()`; `getPos()`→`position()`; `getBlockPos()`→`blockPosition()`; `setPosition(x,y,z)`→`setPos(x,y,z)`; `getVelocity()`/`setVelocity(Vec3)`→`getDeltaMovement()`/`setDeltaMovement(Vec3)`; `getYaw()`/`setYaw()`→`getYRot()`/`setYRot()`; `getPitch()`/`setPitch()`→`getXRot()`/`setXRot()`; `getBodyYaw()`→**`getVisualRotationYInDegrees()`** (Entity-level; LivingEntity field `bodyYaw`→`yBodyRot`); `getHeadYaw()`/`headYaw`→`getYHeadRot()`/`yHeadRot`; `handSwingProgress`→`attackAnim`; `prevX/prevY/prevZ`→**`xo/yo/zo`**; `age`→**`tickCount`**; `getUuid()`→`getUUID()`; `getServer()` (entity) GONE → **`level().getServer()`**; **`getRotationVector()` now returns `Vec2`** (direction vector is `getViewVector(float)`); `canSee(e)`→`hasLineOfSight(e)`; `getMainHandStack()`→`getMainHandItem()`; `sendEntityStatus(e,b)`→**`level().broadcastEntityEvent(e,b)`**; `handleStatus(byte)`→**`handleEntityEvent(byte)`**; `onRemoved()` (no-arg client hook)→**`onClientRemoval()`**; `getRandom()` unchanged (RandomSource); `getEyeY()` unchanged.
**Damage / kill:** `damage(DamageSource,float)`→**`hurtServer(ServerLevel,DamageSource,float)`** — and **`Entity.hurtServer` is now `public abstract`** (a non-LivingEntity `extends Entity` must implement it; return `false` for no handling). `source.getAttacker()`→**`source.getEntity()`**. `onKilledOther(ServerWorld,LivingEntity)`→**`killedEntity(ServerLevel,LivingEntity,DamageSource)`** (extra DamageSource param). `tryAttack(e)`→**`doHurtTarget(ServerLevel,Entity)`**; `swingHand(Hand)`→`swing(InteractionHand)`; `sidewaysSpeed`→`xxa` (`forwardSpeed`→`zza`).
**Save/sync (Entity, like BlockEntity):** `initDataTracker()`→**`defineSynchedData(SynchedEntityData.Builder)`** (now abstract, takes Builder); `writeCustomDataToNbt(NbtCompound)`→**`addAdditionalSaveData(ValueOutput)`**; `readCustomDataFromNbt(NbtCompound)`→**`readAdditionalSaveData(ValueInput)`** (both abstract on Entity). `DataTracker`→`SynchedEntityData`; `TrackedData`→`EntityDataAccessor`.
**Head turn:** `getMaxLookYawChange()`→**`getMaxHeadYRot()`**; `getMaxLookPitchChange()`→**`getMaxHeadXRot()`**. The Yarn `Mob.turnHead(float,float)` (returned float) is GONE — `Mob.tickHeadTurn(float)` now just delegates to `BodyRotationControl.clientTick()`; 360-degree head freedom is best done via overriding `getMaxHeadYRot/XRot`.
**Sounds:** `playSoundFromEntity(player,entity,snd,cat,vol,pitch)`→**`level().playSound(player, entity, snd, SoundSource, vol, pitch)`** (entity-to-entity overload); `SoundCategory`→`sounds.SoundSource`.
**LookControl (subclass field/method renames):** field `entity`→**`mob`**; `x/y/z`→`wantedX/wantedY/wantedZ`; `maxYawChange`→`yMaxRotSpeed`; `maxPitchChange`→`xMaxRotAngle`; `lookAtTimer`→`lookAtCooldown`; `getTargetYaw()`→**`getYRotD()`**, `getTargetPitch()`→**`getXRotD()`** (both `protected Optional<Float>`); `clampHeadYaw()`→**`clampHeadRotationToBody()`**; base `lookAt(Entity,float,float)`→**`setLookAt(Entity,float,float)`**; the Yarn helper `changeAngle(from,to,max)`→**`Mth.rotateIfNecessary(from,to,max)`**. ctor `LookControl(Mob)`.
**Goals:** `canStart()`→**`canUse()`**; `shouldContinue()`→**`canContinueToUse()`**; `start()/stop()/tick()` unchanged. `targetSelector.add(p,g)`/`goalSelector.add(p,g)`→**`.addGoal(int,Goal)`**; `initGoals()`→**`registerGoals()`**. Goal helpers: `getTickCount(n)`→`adjustedTickDelay(int)` (instance); `toGoalTicks(n)`→**`reducedTickDelay(int)`** (static). `ActiveTargetGoal<T>(mob,cls,bool)`→**`NearestAttackableTargetGoal<T>(Mob,cls,bool)`** (also `(Mob,cls,int,bool,bool,TargetingConditions$Selector)` — pass `null` for the old predicate arg). `TrackTargetGoal(mob,bool,bool)`→**`target.TargetGoal(Mob,bool,bool)`**: field `this.target`→**`this.targetMob`**, `this.maxTimeWithoutVisibility`→**`this.unseenMemoryTicks`**, `this.mob` unchanged. `WanderAroundFarGoal(mob,d)`→**`WaterAvoidingRandomStrollGoal(PathfinderMob,d)`**. **MeleeAttackGoal:** its `path`, `lastCanUseCheck`, and `getSquaredMaxAttackDistance` are now PRIVATE/GONE — a subclass overriding `canUse()` must mirror them locally (vanilla reach formula: `mob.getBbWidth()*2*mob.getBbWidth()*2 + target.getBbWidth()`); `findPathTo(e,d)`→`createPath(e,d)`; `startMovingTo(x,y,z,s)`→`moveTo(x,y,z,s)`.
**Navigation:** `MobNavigation`→**`GroundPathNavigation(Mob,Level)`**; nav field `this.entity`→**`this.mob`** (protected), `this.speed`→**`this.speedModifier`** (protected); `isIdle()`→**`isDone()`**; `getMoveControl().moveTo(x,y,z,s)`→**`setWantedPosition(x,y,z,s)`**; `startMovingTo`→`moveTo`; `findPathTo`→`createPath`. `MobNavigation.adjustTargetY(Vec3d)` has NO mojmap equivalent (use target's actual Y).
**PathNodeMaker (`LandPathNodeMaker`→`WalkNodeEvaluator`):** override `getStart(BlockPos)`→**`protected Node getStartNode(BlockPos)`**; `getSuccessors(PathNode[],PathNode)`→**`public int getNeighbors(Node[],Node)`**; `getFeetY(BlockPos)`→**`protected double getFloorLevel(BlockPos)`**. World access field `this.cachedWorld` GONE → **`this.currentContext`** (`PathfindingContext`: `.getBlockState(BlockPos)`, `.level()`→CollisionGetter). `Node` is `world.level.pathfinder.Node` (extends Vec3i, `public final int x/y/z`): `node.penalty`→**`node.costMalus`**, `node.visited`→**`node.closed`**, `node.type` (`PathType`). `PathNodeType`→`PathType` (`PathType.WALKABLE`). `state.isSolidBlock(world,pos)`→**`state.isCollisionShapeFullBlock(BlockGetter,pos)`**.
**Player query:** `World.getPlayers(TargetPredicate, source, Box)` is **REMOVED**. Replace with `((ServerLevel)level()).getEntitiesOfClass(Player.class, box, predicate)` filtering each via **`TargetingConditions.test(ServerLevel, attacker, target)`**. `TargetPredicate`→**`TargetingConditions`**: `.createNonAttackable()`→`forNonCombat()`; `.DEFAULT`→`DEFAULT`; `forCombat()` exists; `.setBaseMaxDistance(d)`→`.range(d)`; `.ignoreVisibility()`→`.ignoreLineOfSight()`; `.ignoreDistanceScalingFactor()` GONE (drop); `.setPredicate(pred)`→`.selector((LivingEntity,ServerLevel)->bool)` (`TargetingConditions$Selector`). `EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR`→**`EntitySelector.NO_CREATIVE_OR_SPECTATOR`** (`Predicate<Entity>`). `getClosestPlayer(e,d)`→**`getNearestPlayer(e,d)`** (EntityGetter). `Box`→`AABB`; `.expand(d)`→`.inflate(d)`; `.offset(x,y,z)`→`.move(x,y,z)`; `new Box(Vec3,Vec3)`→`new AABB(Vec3,Vec3)`.
**Raycast / Vec3 / projectiles (also used by IK math):** `Vec3d`→`world.phys.Vec3`: `dotProduct`→`dot`, `crossProduct`→`cross`, `squaredDistanceTo`→`distanceToSqr`, `multiply(double)`→**`scale(double)`** (3-arg `multiply(x,y,z)` UNCHANGED), `lengthSquared()`→`lengthSqr()`, `isInRange(v,d)`→**`closerThan(Position,d)`**, `offset(Direction,d)`→**`relative(Direction,d)`**, `negate()`→**`reverse()`**, `rotateY(f)`→**`yRot(float)`**, `Vec3d.ZERO`→`Vec3.ZERO`. `RaycastContext`→**`ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,Entity)`**; `world.raycast(ctx)`→**`level.clip(ctx)`** (BlockGetter); `hitResult.getPos()`→**`getLocation()`**; `RaycastContext.ShapeType.COLLIDER`→`ClipContext.Block.COLLIDER`; `RaycastContext.FluidHandling.NONE`→`ClipContext.Fluid.NONE`. `new ArrowEntity(world,x,y,z)`→**`new Arrow(level,x,y,z, ItemStack.EMPTY, ItemStack.EMPTY)`** (`projectile.arrow.Arrow`; the old 4-arg ctor is gone). `RandomSource.nextBetween(a,b)`→**`nextIntBetweenInclusive(a,b)`**.

### Block layer (verified vs ChestBlock/BarrelBlock — TRUST THESE)
**`.is(...)` overloads removed:** `state.is(block)` → **`state.getBlock() == block`**; `stack.is(item)` → **`stack.getItem() == item`**; `fluidState.is(Fluids.WATER)` → **`fluidState.getType() == Fluids.WATER`** (only `is(TagKey,Predicate)`/`is(Predicate<Holder>)` survive).
**BlockEntity NBT (CONFIRMED):** `readNbt`/`writeNbt`/`saveAdditional(CompoundTag,...)` → **`protected void loadAdditional(net.minecraft.world.level.storage.ValueInput)`** / **`protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput)`**. `markDirty()`→`setChanged()`; `markRemoved()`→`setRemoved()`; `removed` field→`isRemoved()`; BE `world` field→`level` (`getWorld()`→`getLevel()`); `getPos()`→`getBlockPos()`; `getCachedState()`→`getBlockState()`. `LevelChunk.blockEntityNbts` (pending-NBT map) is GONE → use `level.removeBlockEntity(pos)`.
**Level idioms:** `Level.isClientSide` is a PRIVATE field — call **`level.isClientSide()`**. `setBlockState(pos,state)`→`setBlockAndUpdate`; `setBlockState(pos,state,flag)`→`setBlock(pos,state,flag)`; `Block.NOTIFY_LISTENERS`→`Block.UPDATE_CLIENTS`; `scheduleBlockTick`→`scheduleTick`; `isReceivingRedstonePower`→`hasNeighborSignal`; `getRegistryKey()`→`dimension()`; `Level.random` is protected → `getRandom()`.
**Block hooks (override these exact sigs):** `onUse(...)`→**`useWithoutItem(BlockState,Level,BlockPos,Player,BlockHitResult)`**→`InteractionResult`; `player.openHandledScreen`→`player.openMenu(MenuProvider)`; `onStateReplaced` drop-contents→**`affectNeighborsAfterRemoval(BlockState,ServerLevel,BlockPos,boolean)`**; `neighborUpdate`→**`neighborChanged(BlockState,Level,BlockPos,Block,@Nullable ...redstone.Orientation,boolean)`**; `scheduledTick(...,ServerWorld,...)`→**`tick(BlockState,ServerLevel,BlockPos,RandomSource)`**; `getStateForNeighborUpdate`→`updateShape(...,ScheduledTickAccess,...)`; `getOutlineShape`→`getShape`; `getAmbientOcclusionLightLevel`→`getShadeBrightness`.
**Placement/state idioms:** `getPlacementState(ItemPlacementContext)`→`getStateForPlacement(BlockPlaceContext)`; `appendProperties(StateManager.Builder)`→`createBlockStateDefinition(StateDefinition.Builder)`; `.with()`→`.setValue()`, `.get()`→`.getValue()`; `getDefaultState()`→`defaultBlockState()`, `setDefaultState`→`registerDefaultState`. Ctx: `getHorizontalPlayerFacing()`→`getHorizontalDirection()`, `getPlayerLookDirection()`→`getNearestLookingDirection()`, `getWorld()`→`getLevel()`, `getBlockPos()`→`getClickedPos()`, `getStack()`→`getItemInHand()`, `shouldCancelInteraction()`→`isSecondaryUseActive()`; `player.isSneaking()`→`isShiftKeyDown()`; `canPlaceAt(world,pos)`→`canSurvive(LevelReader,pos)`; `canReplace`→`canBeReplaced`.
**BaseEntityBlock:** abstract `codec()` → `simpleCodec(X::new)`; `BlockWithEntity.checkType`→`createTickerHelper`; `getTicker(World,...)`→`getTicker(Level,...)` (EntityBlock); `createBlockEntity`→`newBlockEntity`; `getRenderType`→`getRenderShape` (`RenderShape.MODEL`/`INVISIBLE`). Plain `extends Block` already has concrete `codec()`.
**Container BE (`RandomizableContainerBlockEntity`):** `size()`→`getContainerSize()`; `getInvStackList`/`setInvStackList`→`getItems`/`setItems`; `getContainerName`→`getDefaultName`; `createScreenHandler(int,PlayerInventory)`→`createMenu(int,Inventory)` (`ChestMenu.threeRows(id,inv,this)`); loot `serializeLootTable`/`deserializeLootTable`→`trySaveLootTable(ValueOutput)`/`tryLoadLootTable(ValueInput)`; inv NBT `Inventories.writeNbt/readNbt`→`ContainerHelper.saveAllItems(ValueOutput,list)`/`loadAllItems(ValueInput,list)`. `ContainerOpenersCounter` (was ViewerCountManager): BE `onOpen/onClose(Player)`→`startOpen/stopOpen(ContainerUser)` (Player implements ContainerUser); counter takes LivingEntity: `incrementOpeners(user.getLivingEntity(),level,pos,state,user.getContainerInteractionRange())`. `player.currentScreenHandler`→`containerMenu`; `ItemScatterer.spawn`→`Containers.dropContents(Level,BlockPos,Container)`.
**Properties:** `BooleanProperty.of`→`create`; `IntProperty.of`→`IntegerProperty.create`; `Properties.X`→`BlockStateProperties.X`; `DirectionProperty`→`EnumProperty<Direction>`; `WallMountLocation`→`AttachFace`(FLOOR/WALL/CEILING), `Properties.WALL_MOUNT_LOCATION`→`BlockStateProperties.ATTACH_FACE`; `FacingBlock`→`DirectionalBlock`, `HorizontalFacingBlock`→`HorizontalDirectionalBlock`.
**Misc:** `Block.createCuboidShape`→`Block.box`; `pos.down()`→`below()`; `pos.toCenterPos()`→`Vec3.atCenterOf(pos)`; `Random.create()`→`RandomSource.create()`, `nextBetween(a,b)`→`nextIntBetweenInclusive(a,b)`; `SoundCategory.BLOCKS`→`SoundSource.BLOCKS`; `SoundEvents.BLOCK_BARREL_OPEN`→`SoundEvents.BARREL_OPEN`.

### Worldgen (verified vs decompiled 26.2 `ChunkGenerator`/`BiomeSource`/`StructureTemplate` — TRUST THESE)
**`ChunkGenerator` registration is now `MapCodec`, NOT `Codec`:** `BuiltInRegistries.CHUNK_GENERATOR` is `Registry<MapCodec<? extends ChunkGenerator>>`. So generator `CODEC` fields → `MapCodec<X>` built with **`RecordCodecBuilder.mapCodec(...)`** (not `.create`); abstract `getCodec()` (returned `Codec`) → **`protected MapCodec<? extends ChunkGenerator> codec()`**. `Registry.register(BuiltInRegistries.CHUNK_GENERATOR, Identifier, mapCodec)`.
**`ChunkGenerator` abstract method signatures (26.2, exact):**
- `populateNoise(Executor,Blender,NoiseConfig,StructureAccessor,Chunk)` → **`CompletableFuture<ChunkAccess> fillFromNoise(Blender, RandomState, StructureManager, ChunkAccess)`** (no Executor).
- `getMinimumY()` → **`int getMinY()`**; `getSeaLevel()` stays (now abstract); `getWorldHeight()` → **`int getGenDepth()`**.
- `getHeight(int,int,Heightmap.Type,HeightLimitView,NoiseConfig)` → **`int getBaseHeight(int,int,Heightmap.Types,LevelHeightAccessor,RandomState)`** (note `Heightmap.Type`→**`Heightmap.Types`**, `world.level.levelgen.Heightmap`).
- `getColumnSample(int,int,HeightLimitView,NoiseConfig)` → **`NoiseColumn getBaseColumn(int,int,LevelHeightAccessor,RandomState)`** (`VerticalBlockSample`→`world.level.NoiseColumn`, ctor `new NoiseColumn(int, BlockState[])` unchanged).
- `getDebugHudText(List<String>,NoiseConfig,BlockPos)` → **`addDebugScreenInfo(List<String>, RandomState, BlockPos)`**.
- `carve(ChunkRegion,long,NoiseConfig,BiomeAccess,StructureAccessor,Chunk,GenerationStep.Carver)` → **`applyCarvers(WorldGenRegion, long, RandomState, BiomeManager, StructureManager, ChunkAccess)`** — *the `GenerationStep.Carver` param is GONE (6 args now)*.
- `buildSurface(ChunkRegion,StructureAccessor,NoiseConfig,Chunk)` → **`buildSurface(WorldGenRegion, StructureManager, RandomState, ChunkAccess)`**.
- `populateEntities(ChunkRegion)` → **`spawnOriginalMobs(WorldGenRegion)`**.
**`BiomeSource` (26.2):** `CODEC` (static `Codec<BiomeSource>`) unchanged for `.fieldOf("biome_source")`; abstract `protected MapCodec<? extends BiomeSource> codec()`; `collectPossibleBiomes()`→`Stream<Holder<Biome>>`; `getNoiseBiome(int,int,int,Climate.Sampler)`. Mod uses only vanilla BiomeSource via `BiomeSource.CODEC`.
**ChunkGeneratorSettings codec:** `ChunkGeneratorSettings.REGISTRY_CODEC` → **`NoiseGeneratorSettings.CODEC`** (the `Codec<Holder<NoiseGeneratorSettings>>`; `DIRECT_CODEC` is the non-Holder one). Field type `RegistryEntry<ChunkGeneratorSettings>` → **`Holder<NoiseGeneratorSettings>`**.
**Structure placement:** `StructureTemplate.place(world,pos,pivot,StructurePlacementData,Random,flags)` → **`.placeInWorld(ServerLevelAccessor, BlockPos, BlockPos, StructurePlaceSettings, RandomSource, int)`** (`WorldGenLevel` satisfies `ServerLevelAccessor`). `StructurePlacementData` → **`StructurePlaceSettings`** (`setMirror/setRotation/setIgnoreEntities/getRotation` all unchanged, fluent). `server.getStructureTemplateManager()` → **`server.getStructureManager()`**; `StructureTemplateManager.getTemplate(id)` → **`.get(Identifier)`** (returns `Optional<StructureTemplate>`).
**Worldgen types:** `StructureWorldAccess`→`world.level.WorldGenLevel`; `ChunkRegion`→`server.level.WorldGenRegion`; `Chunk`→`world.level.chunk.ChunkAccess`; `StructureAccessor`→`world.level.StructureManager`; `BiomeAccess`→`world.level.biome.BiomeManager`; `NoiseConfig`→`world.level.levelgen.RandomState`; `Blender`→`world.level.levelgen.blending.Blender`; `HeightLimitView`→`world.level.LevelHeightAccessor`; `PerlinNoiseSampler`→`world.level.levelgen.synth.ImprovedNoise` (ctor `(RandomSource)`, `.sample(x,y,z)`→**`.noise(x,y,z)`**).
**ChunkAccess/ChunkPos idioms:** `chunk.setBlockState(pos,state,boolean moved)` → use 2-arg **`chunk.setBlockState(pos, state)`** (3rd arg is now `int` flags, not boolean). `chunkPos.getStartX()`→**`getMinBlockX()`**, `getStartZ()`→**`getMinBlockZ()`**; `chunkPos.getBlockPos(x,y,z)`→**`getBlockAt(x,y,z)`**. `WorldGenLevel.setBlockState(pos,state,flags)`→**`setBlock(pos,state,flags)`**. `world.toServerWorld()`→**`world.getLevel()`** (ServerLevelAccessor, returns ServerLevel); `world.getDimension()`→**`world.dimensionType()`**.
**`BlockPos.Mutable` → `BlockPos.MutableBlockPos`** (nested class renamed; `new BlockPos.MutableBlockPos()`, `.set(int,int,int)`/`.set(double,double,double)`/`.set(Vec3i)` all present).
**`Vec3i.isWithinDistance(Vec3i,d)` → `Vec3i.closerThan(Vec3i, double)`.**
**Colored wool/etc:** `Blocks.<COLOR>_WOOL` → `Blocks.WOOL.<color>()` where `<color>` ∈ {white,orange,magenta,lightBlue,yellow,lime,pink,gray,lightGray,cyan,purple,blue,brown,green,red,black} (a `ColorCollection<Block>`; `.pick(DyeColor)` also works). Same pattern for `Blocks.CONCRETE`, `STAINED_GLASS`, etc.
**Identifier:** no `new Identifier(ns,path)` and no `Identifier.of(ns,path)` — use **`Identifier.fromNamespaceAndPath(ns, path)`** (also `.parse`, `.tryParse`).
**`CompoundTag` getters return `Optional` now** — for a plain value use the `...Or(key, default)` variants: `getInt`→**`getIntOr(k,0)`**, `getString`→**`getStringOr(k,"")`**, `getFloat`→**`getFloatOr(k,0f)`**, `getBoolean`→**`getBooleanOr(k,false)`** (the bare `getInt`/`getString`/... now return `Optional<T>`). Setters `putInt/putString/putFloat/putBoolean` unchanged.

### Entity / sound packet (verified, used by world/events)
- `World.getPlayers()` (on EntityGetter) → **`Level.players()`** (`List<? extends Player>`).
- `EntityType.create(World)` → **`create(Level, EntitySpawnReason)`** (`EntitySpawnReason.{EVENT,TRIGGERED,MOB_SUMMONED,COMMAND}`; `world.entity.EntitySpawnReason`).
- `entity.refreshPositionAndAngles(x,y,z,yaw,pitch)` → **`entity.snapTo(double,double,double,float,float)`** (also `snapTo(Vec3)`, `snapTo(BlockPos,float,float)`); `world.spawnEntity(e)` → **`world.addFreshEntity(e)`** (LevelWriter); `Vec3d.rotateY(f)`→**`Vec3.yRot(float)`**; `BlockState.blocksMovement()`→**`.blocksMotion()`**; `entity.getPos()`→`position()`; `Entity.fallDistance` is now a **`double`** field.
- Sound packet: `PlaySoundS2CPacket(RegistryEntry.of(snd), SoundCategory.X, x,y,z, vol,pitch, seed)` → **`new ClientboundSoundPacket(Holder.direct(snd), SoundSource.X, double x, double y, double z, float vol, float pitch, long seed)`**; send via `((ServerPlayer)player).connection.send(packet)` (`networkHandler.sendPacket`→`connection.send`; `connection.send` is on `ServerCommonPacketListenerImpl`). `SoundEvent`→`net.minecraft.sounds.SoundEvent`; `SoundCategory`→`net.minecraft.sounds.SoundSource`.
- `ServerPlayer` respawn API reworked: `getSpawnPointPosition()`/`getSpawnAngle()`/`isSpawnForced()` and `Player.findRespawnPosition(...)` are GONE → `getRespawnConfig()`/`RespawnConfig` + `findRespawnPositionAndUseSpawnBlock(...)`/`TeleportTransition`. World spawn: `world.getSpawnPos()` → **`serverLevel.getRespawnData().pos()`** (`LevelData.RespawnData`). `server.getWorld(key)`→**`server.getLevel(ResourceKey<Level>)`**. (InfiniteGrass respawn lookup left as `// PORT: phase6`.)

### Cardinal Components 8 (verified vs cardinal-components-{base,entity,level}-8.0.0 — TRUST THESE)
- Packages are **`org.ladysnake.cca.api.v3.*`** (NOT the old `dev.onyxstudios.cca.*`). `ComponentProvider` is now **`org.ladysnake.cca.api.v3.component.ComponentProvider`**.
- `ComponentKey<C>` via `ComponentRegistry.getOrCreate(Identifier, C.class)` — UNCHANGED. Registration: entity comps in `EntityComponentInitializer#registerEntityComponentFactories` (`registry.registerForPlayers(KEY, X::new, RespawnCopyStrategy.ALWAYS_COPY)`, `registry.registerFor(Entity.class, KEY, X::new)`); level comps in **`org.ladysnake.cca.api.v8.level.LevelComponentInitializer#registerLevelComponentFactories`** (`registry.register(KEY, X::new)`) — note the level packages are `...api.v8.level.*` while the rest stay `...api.v3.*`.
- **NBT is `ValueInput`/`ValueOutput`, NOT NbtCompound:** override **`void readData(net.minecraft.world.level.storage.ValueInput)`** / **`void writeData(net.minecraft.world.level.storage.ValueOutput)`** (the v3 `readFromNbt`/`writeToNbt(NbtCompound)` are gone). Read: `tag.getIntOr(k,def)`, `tag.getBooleanOr(k,def)`, `tag.read(k, CODEC).orElse(def)`, `tag.child(k).ifPresent(...)`. Write: `tag.putInt/putBoolean(k,v)`, `tag.store(k, CODEC, v)`, `tag.storeNullable(k, CODEC, v)`, `tag.child(k)` (returns a child `ValueOutput`). UUID via `net.minecraft.core.UUIDUtil.CODEC`; bridge a legacy `CompoundTag` payload via `tag.read(k, CompoundTag.CODEC)` / `tag.store(k, CompoundTag.CODEC, ct)`. Item lists: `ContainerHelper.saveAllItems(tag.child(k), NonNullList)` / `loadAllItems(child, NonNullList)`.
- Interfaces: `AutoSyncedComponent` (`org.ladysnake.cca.api.v3.component.sync`); ticking `ServerTickingComponent#serverTick()`, `ClientTickingComponent#clientTick()`, `CommonTickingComponent` (`org.ladysnake.cca.api.v3.component.tick`).
- Sync: `KEY.sync(holder)` (full sync) and `KEY.syncWith(ServerPlayer, ComponentProvider provider)` (per-player; cast the player holder: `KEY.syncWith((ServerPlayer)player, (ComponentProvider)player)`). `KEY.get(Object)` / `KEY.getNullable(Object)` / `KEY.maybeGet(Object)` accept any holder (player/entity/level).

### Networking — CustomPacketPayload / StreamCodec (Phase 7 — VERIFIED, full rewrite)
- The `PacketByteBuf`-channel API is **GONE**: `PacketByteBufs`, `ClientPlayNetworking.send(Identifier,buf)`, `ServerPlayNetworking.send(player,Identifier,buf)`, and the old 5-arg `(server,player,handler,buf,responseSender)` / `(client,handler,buf,responseSender)` receiver signatures no longer exist.
- **Each packet = a `record X(...) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload`** with:
  - `static final CustomPacketPayload.Type<X> TYPE = new CustomPacketPayload.Type<>(Identifier)` (ctor takes only an Identifier);
  - `static final StreamCodec<RegistryFriendlyByteBuf, X> CODEC` built with **`StreamCodec.composite(codec1, X::field1, ..., X::new)`** (composite arities 1–12; field codecs from `net.minecraft.network.codec.ByteBufCodecs.{BOOL,INT,VAR_INT,FLOAT,DOUBLE,STRING_UTF8,...}`; empty payload → `StreamCodec.unit(new X())`);
  - **`@Override public Type<? extends CustomPacketPayload> type()`** (the override is named `type()`, NOT `getType()`).
- Holder<SoundEvent> over the wire: use **`SoundEvent.STREAM_CODEC`** (`StreamCodec<RegistryFriendlyByteBuf, Holder<SoundEvent>>`); generic registry holder is `ByteBufCodecs.holderRegistry(Registries.SOUND_EVENT)`.
- **Register payload TYPES (both sides, common init):** ⚠ in fabric-networking-api-v1 **6.3.3+72073ef09e (26.2)** the registry methods are **`PayloadTypeRegistry.serverboundPlay()`** (C2S) / **`PayloadTypeRegistry.clientboundPlay()`** (S2C) — the `playC2S()`/`playS2C()` names do NOT exist in this revision (VERIFIED via javap). So: `PayloadTypeRegistry.serverboundPlay().register(X.TYPE, X.CODEC)` / `PayloadTypeRegistry.clientboundPlay().register(X.TYPE, X.CODEC)` (also `serverboundConfiguration()`/`clientboundConfiguration()`). `serverboundPlay()` is typed `<RegistryFriendlyByteBuf>`, `clientboundPlay()` `<RegistryFriendlyByteBuf>` too.
- **Send:** `ServerPlayNetworking.send(ServerPlayer, CustomPacketPayload)` (S2C) / `ClientPlayNetworking.send(CustomPacketPayload)` (C2S).
- **Receive:** `ServerPlayNetworking.registerGlobalReceiver(X.TYPE, (payload, context) -> ...)` with `ServerPlayNetworking.Context` (`context.server()`, `context.player()` → `ServerPlayer`, `context.responseSender()`); `ClientPlayNetworking.registerGlobalReceiver(X.TYPE, (payload, context) -> ...)` with `ClientPlayNetworking.Context` (`context.client()` → `Minecraft`, `context.player()` → `LocalPlayer`). Thread-confine work via `context.server().execute(...)` / `context.client().execute(...)`.
- Client sound playback: `PositionedSoundInstance.master(snd, pitch, vol)` → **`SimpleSoundInstance.forUI(SoundEvent, float pitch, float volume)`** (`net.minecraft.client.resources.sounds.SimpleSoundInstance`); play via `Minecraft.getSoundManager().play(SoundInstance)`. `buf.readRegistryEntry(...)`/`SoundEvent.fromBuf` are gone (use the codec).
- `Level.getEntityById(int)` → **`Level.getEntity(int)`** (also `getEntity(UUID)`/`getEntityInAnyDimension(UUID)`); client `MinecraftClient` receiver → `Minecraft`.

### Custom tickable sound instances (Phase 7 — VERIFIED via javap on merged-deobf 26.2)
- Base class: `client.sound.MovingSoundInstance` → **`net.minecraft.client.resources.sounds.AbstractTickableSoundInstance`** (extends `AbstractSoundInstance`, implements `TickableSoundInstance`). Ctor: `(SoundEvent, SoundSource, RandomSource)`. (`client.sound.SoundInstance` → `client.resources.sounds.SoundInstance`; `client.sound.PositionedSoundInstance` → `client.resources.sounds.SimpleSoundInstance`; `client.sound.SoundManager` → `client.sounds.SoundManager`; `client.sound.SoundSystem` → `client.sounds.SoundEngine`.)
- Random for ctor: **`SoundInstance.createUnseededRandom()`** (was Yarn `SoundInstance.createRandom()`) → returns `net.minecraft.util.RandomSource`.
- **Protected fields renamed on `AbstractSoundInstance`**: `repeat` → **`looping`**, `repeatDelay` → **`delay`**; unchanged: `volume`, `pitch`, `x`, `y`, `z`, `relative`, `attenuation`, `sound`, `source`, `random`.
- **Method renames**: `setDone()` → **`stop()`** (protected final on `AbstractTickableSoundInstance`); Yarn `MovingSoundInstance.shouldAlwaysPlay()` → **`canStartSilent()`** (default `false`, override `true` to play when out of range — confirmed vs `BeeSoundInstance`); Yarn `MovingSoundInstance.isRepeatable()` → **`canPlaySound()`** (default `true`). Getters unchanged: `getVolume()/getPitch()/getX()/getY()/getZ()`; "is done" query is `isStopped()`.
- Block-position source coords: Yarn `blockEntity.getPos().toCenterPos()` → **`Vec3.atCenterOf(blockEntity.getBlockPos())`** (BlockEntity `getPos()`→`getBlockPos()`, `getWorld()`→`getLevel()`).
- Proximity: Yarn `BlockPos.isWithinDistance(Position, double)` has **no `Position` overload on `Vec3i`/`BlockPos`** (only `closerThan(Vec3i, double)`). Use **`player.position().closerThan(Vec3.atCenterOf(blockPos), d)`** (`Vec3.closerThan(Position, double)` exists).
- `player.getWorld().getRegistryKey()` → **`player.level().dimension()`** (`ResourceKey<Level>`).

### Simple Voice Chat plugin API (Phase 7 — VERIFIED vs simple-voice-chat-fabric-2.6.20+26.2 + voicechat-api-2.6.20)
- Plugin contract `de.maxhenkel.voicechat.api.VoicechatPlugin` and all `api.events.*` (OpenALSoundEvent, MicrophonePacketEvent, PlayerConnected/DisconnectedEvent, VoicechatServerStarted/StoppedEvent), `VoicechatConnection`, `api.Player`/`api.Entity`/`api.ServerPlayer`, `EventRegistration.registerEvent(Class,Consumer[,int])`, `OpusDecoder` — **all stable, no changes**. `Event.cancel()`, `PacketEvent.getSenderConnection()/getPacket()`, `ServerEvent.getVoicechat()`, `MicrophonePacket.getOpusEncodedData()`, `api.Entity.getUuid()`, `api.Player.getPlayer()→Object` all present. Keep SVC-API `getUuid()` calls as-is (NOT the MC `getUUID()` rename).
- **MOVED:** `de.maxhenkel.voicechat.voice.common.Utils.dbToPerc(double)` / `Utils.getHighestAudioLevel(short[])` → **`de.maxhenkel.voicechat.voice.common.AudioUtils.dbToPerc(double)` / `AudioUtils.getHighestAudioLevel(short[])`** (both `static`, return `double`). `Utils` still exists but no longer has these.
- MC-side renames inside the plugin: `EntityPredicates.VALID_LIVING_ENTITY` → **`net.minecraft.world.entity.EntitySelector.LIVING_ENTITY_STILL_ALIVE`**; `level.getEntitiesByClass(...)` → **`getEntitiesOfClass(...)`**; `Box`/`getBoundingBox().expand(n)` → **`AABB`/`.inflate(n)`**; `net.minecraft.util.math.random.Random` → `RandomSource` (`Random.create()`→`RandomSource.create()`, `nextBetween(a,b)`→`nextIntBetweenInclusive(a,b)`); MC `getUuid()`→`getUUID()`.

### Easing helper (Phase 4/7)
- Veil `foundry.veil.api.client.util.Easings` is replaced by the in-repo shim **`com.sp.util.Easings`** (`Easings.Easing.<name>.ease(float)`); switch sound/render call sites to the local class.

### Phase 5 mixin injection points (VERIFIED via javap on merged-deobf 26.2 — TRUST THESE)
**Entity / LivingEntity / Player (server-logic):**
- `Entity`: `world` field → private `Level level` (also `Level level()` accessor); `damage(DamageSource,float)` → **`hurtServer(ServerLevel,DamageSource,float)`** (abstract); `isTouchingWater()` → **`isInWater()`**; the no-arg Yarn `playSwimSound()V` host method is renamed **`waterSwimSound()V`** (it still calls `playSwimSound(F)` — `@ModifyArg` target unchanged); `noClip` field → **`noPhysics`** (public, on Entity); `playStepSound` stores a local `SoundType` in slot 3 (`@Local SoundType` works); `BlockSoundGroup` → `world.level.block.SoundType`.
- `Player`: `onDeath(DamageSource)` → **`die(DamageSource)`**; `setPosition(x,y,z)` → `setPos(double,double,double)`, `setPosition(Vec3)` → `setPos(Vec3)`; `setCameraEntity` → `setCamera`; `getLastDeathPos()` → **`getLastDeathLocation()`** (`Optional<GlobalPos>`, on Player; `GlobalPos.pos()`/`.dimension()`); `DamageTypes.OUT_OF_WORLD` → **`DamageTypes.FELL_OUT_OF_WORLD`**; `DamageSource.isOf(key)` → `is(ResourceKey)`.
- `LivingEntity`: `fall` → **`checkFallDamage(double,boolean,BlockState,BlockPos)`** (calls `ServerLevel.sendParticles(ParticleOptions,DDDIDDDD)I`); `ServerWorld.spawnParticles` → `ServerLevel.sendParticles`; `ParticleEffect` → `core.particles.ParticleOptions`.
**Server props / weather / lightning:**
- `ServerPropertiesHandler` → `DedicatedServerProperties extends Settings<DedicatedServerProperties>`; `Settings(Properties)` ctor; `getInt(String,int)` → **`get(String,int)`** (protected).
- `LevelProperties.getLifecycle` is GONE on `PrimaryLevelData` → accessor is **`worldGenSettingsLifecycle()`** (field `worldGenSettingsLifecycle`).
- **Weather refactored:** `PrimaryLevelData`/`LevelData` no longer have `getRainTime/isRaining/getThunderTime/isThundering` (no class in the jar even mentions them). Weather is now `Level.rainLevel/thunderLevel` floats + `Level.isRaining()/isThundering()/getRainLevel(f)/setRainLevel(f)` and `WeatherAttributes`. **`AlwaysRainLevelPropertiesMixin` cannot be ported 1:1 — flagged, deferred to Phase 6.**
- `LightningEntity` → **`LightningBolt`**: `tick()` and private `spawnFire(int)` survive; `age` → `tickCount`; no-arg `kill()` → use **`discard()`** (`Entity.kill()` now needs a `ServerLevel`); `getWorld()` → `level()`.
- Skeleton-horse trap spawn moved from `ServerLevel.tickChunk` to **`ServerLevel.tickThunder(LevelChunk)`**; `spawnEntity` → **`addFreshEntity`** (in tickThunder ordinal 0 = trap horse, ordinal 1 = lightning bolt).
**Respawn (server):**
- `PlayerManager` → `PlayerList`; `respawnPlayer(ServerPlayer,boolean)` → **`respawn(ServerPlayer,boolean,Entity$RemovalReason)`**. The old per-step calls (`getSpawnPointPosition`/`MinecraftServer.getWorld(key)`/`Player.findRespawnPosition`/`refreshPositionAndAngles`/`setSpawnPoint`) are GONE — `respawn` now makes a single **`ServerPlayer.findRespawnPositionAndUseSpawnBlock(boolean, TeleportTransition$PostTeleportTransition)`** call returning a `TeleportTransition` (wrap that one INVOKE to override the spawn). `refreshPositionAndAngles` → `snapTo`. `TeleportTransition` ctor `(ServerLevel, Vec3 pos, Vec3 delta, float yRot, float xRot, PostTeleportTransition)`.
**Stamina / sprint:**
- `ServerPlayNetworkHandler` → `ServerGamePacketListenerImpl`; `onClientCommand(ClientCommandC2SPacket)` → **`handlePlayerCommand(ServerboundPlayerCommandPacket)`** (calls `ServerPlayer.setSprinting(Z)` twice — ordinals 0/1 hold).
**Skin-stolen (server):**
- `ServerPlayer.setCameraEntity` → `setCamera`. **Yarn `canBeSpectated(ServerPlayerEntity)` → `broadcastToPlayer(ServerPlayer)`** (overridden on ServerPlayer, returns boolean). The Yarn `attack` `setCamera` redirect has **no 26.2 target** (neither Player.attack nor ServerPlayer call setCamera in any attack path) — dropped + flagged.
**AI / body control:**
- `BodyControl` → `BodyRotationControl`; field `entity` → **`mob`**; `slowlyAdjustBody` GONE → the body-rotation step is private **`rotateBodyIfNecessary()`** (called from `clientTick()`); cancel it to keep body from snapping to head.
- `Entity.adjustMovementForCollisions(Vec3d)` → private **`collide(Vec3)`** (for PlatformCollisionMixin if revived).
**Client GUI / input (non-render):**
- `ChatScreen`: host `sendMessage` → **`handleChatInput(String,boolean)`** (calls `ClientPacketListener.sendCommand` then `sendChat`); `ClientPlayNetworkHandler` → `ClientPacketListener`; `sendChatMessage`/`sendChatCommand` → `sendChat`/`sendCommand`. `Screen.client` field → **`minecraft`**; `Screen(Component)` ctor.
- `GameMenuScreen` → `PauseScreen`; `initWidgets` → private **`createPauseMenu()`**; `ButtonWidget` → `Button` (`Button.builder(Component,OnPress)`, `Button.Builder(Component,OnPress)` public ctor, `Button.OnPress.onPress(Button)`). Among `Button.builder` calls (3; the rest use `SpriteIconButton.builder`): **ordinal 0 = returnToGame, 1 = options, 2 = disconnect** (the old "leave" button is now ordinal **2**). `Formatting` → `ChatFormatting`; `.formatted(...)` → `.withStyle(ChatFormatting...)`.
- `Minecraft`: `setScreen(Screen)` is GONE → **`setScreenAndShow(Screen)`**; `handleInputEvents` → private **`handleKeybinds()`** (the inventory open routes through **`Gui.setScreen(Screen)`** — the sole setScreen call there, ordinal 0; redirect receiver is `Gui`, not `Minecraft`).
- `DeathScreen`: `ButtonWidget` → `Button`; `ticksSinceDeath` field → **`delayTicker`**; `setButtonsActive(boolean)` private; `requestRespawn()` → **`respawn()`** (`LocalPlayer`); `init` Button.builder ordinals unchanged (0=respawn, 1=titleScreen). `LocalPlayer.getWorld().getRegistryKey()` → `level().dimension()`.
- `ClientPlayerEntity` → `LocalPlayer`; `isCamera` → protected **`isControlledCamera()`**.
- `ClientPlayerInteractionManager` → `MultiPlayerGameMode`; `attackEntity(PlayerEntity,Entity)` → **`attack(Player,Entity)`**.
- `SoundEvent.getId()` → **`location()`**.
**Targets gone / reworked (deferred — flagged for Phase 6 / review):**
- `ParticleManager.addBlockBreakingParticles` (now `ParticleEngine`) — method GONE (block-crack reworked) → RemoveBlockBreakParticlesMixin deferred.
- `DebugHud.render` (now `DebugScreenOverlay`) — deferred-HUD `extractRenderState` → DisableF3Mixin deferred.
- `KeyboardInput.jumping` field (now `client.player.KeyboardInput`) — input reworked (`calculateImpulse`), field gone → DisableJumpMixin deferred.
- `PlayerInventory.scrollInHotbar` (now `Inventory`) — GONE; selection is `getSelectedSlot()/setSelectedSlot()` (scroll handling moved to MouseHandler/Minecraft) → PlayerInventoryMixin deferred.
- `client.util.Monitor.populateVideoModes` → `Monitor` is now a **record** built by static `tryCreate(long)`; `videoModes` is a final record component (no instance populate method) → MonitorMixin deferred. `VideoMode` → `com.mojang.blaze3d.platform.VideoMode` (getWidth/getHeight/getRefreshRate unchanged).

### Phase 6c HUD overlay (VERIFIED via javap on fabric-rendering-v1-25.1.6+46a6d00c9e + merged-deobf 26.2 — TRUST THESE)
- **`HudElement`** (`net.fabricmc.fabric.api.client.rendering.v1.hud`) is a functional interface with exactly: `void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor, net.minecraft.client.DeltaTracker)`. (NOT `render(...)`.)
- **`HudElementRegistry`** (same pkg): `static void addLast(Identifier, HudElement)`, `addFirst(Identifier, HudElement)`, `attachElementBefore/After(Identifier, Identifier, HudElement)`, `removeElement(Identifier)`, `replaceElement(Identifier, Function<HudElement,HudElement>)`. All take **`net.minecraft.resources.Identifier`**. Register from a `ClientModInitializer`.
- **`GuiGraphicsExtractor`** (`net.minecraft.client.gui`, the 26.2 `DrawContext`/`GuiGraphics` successor):
  - `int guiWidth()`, `int guiHeight()`; `org.joml.Matrix3x2fStack pose()`; `enableScissor(int,int,int,int)`/`disableScissor()`.
  - `fill(int x1,int y1,int x2,int y2,int argb)` and a `fill(RenderPipeline,...)` overload; `fillGradient(int x1,int y1,int x2,int y2,int argbTop,int argbBottom)`. **No `setShaderColor`** — bake alpha into the ARGB int.
  - `text(Font, String|Component|FormattedCharSequence, int x, int y, int argb[, boolean shadow])`; `centeredText(...)`; `textWithWordWrap(...)`; `textWithBackdrop(...)`.
  - `blit(Identifier id, int x, int y, int w, int h, float u0, float u1, float v0, float v1)` — **normalized** UVs (0..1); values >1 wrap (texture repeats) — used for tiling/scrolling. Note the arg order is **u0,u1,v0,v1** (both us, then both vs), NOT u0,v0,u1,v1.
  - `blit(RenderPipeline pipeline, Identifier id, int x, int y, float u, float v, int w, int h, int texW, int texH, int color)` — the **pixel-coord + tint** form (successor to `DrawContext.drawTexture(tex,x,y,u,v,w,h)`); use `RenderPipelines.GUI_TEXTURED` and an ARGB `color` for alpha tinting.
  - `blitSprite(RenderPipeline, Identifier|TextureAtlasSprite, ...)` for atlas sprites.
- **`Matrix3x2fStack`** (`org.joml`, what `pose()` returns): `pushMatrix()`/`popMatrix()` (NOT `push()/pop()`), plus inherited `Matrix3x2f.translate(float,float)`/`scale(float[,float])` (2D — no z).
- `RenderPipelines` GUI pipelines (`net.minecraft.client.renderer.RenderPipelines`): `GUI`, `GUI_TEXTURED`, `GUI_TEXTURED_PREMULTIPLIED_ALPHA`, `GUI_TEXT`, `GUI_INVERT`, `GUI_NAUSEA_OVERLAY`, etc.
- `Minecraft.getInstance().font` (`net.minecraft.client.gui.Font`): `int width(String|FormattedText|FormattedCharSequence)`, public final `int lineHeight`. `Minecraft.player` is `LocalPlayer`; `Minecraft.level` is the client `Level`.
- **`Options#hudHidden` is GONE in 26.2** — there is no public hide-gui boolean on `Options`/`Minecraft`/`Gui`. The hide-gui flag is now a **parameter** to `Gui.extractRenderState(DeltaTracker, boolean hideGui, boolean ...)`. Code that did `client.options.hudHidden = true` must route through a shared flag + a `Gui.extractRenderState` mixin (flagged for Phase 6f; used by SkinwalkerJumpscare + CutsceneManager).
- `DeltaTracker`: `float getGameTimeDeltaTicks()`, `getGameTimeDeltaPartialTick(boolean)`, `getRealtimeDeltaTicks()`; constants `DeltaTracker.ZERO`/`ONE`.

### Phase 6d dynamic light (VERIFIED via javap on merged-deobf 26.2 — TRUST THESE)
- **Vanilla light source:** `Blocks.LIGHT` exists (`net.minecraft.world.level.block.Blocks.LIGHT`, type `Block`; the concrete class is `net.minecraft.world.level.block.LightBlock`). `LightBlock.LEVEL` is an `IntegerProperty` (0..15), `LightBlock.MAX_LEVEL` is the int max (15), `LightBlock.WATERLOGGED` `BooleanProperty`, `LightBlock.LIGHT_EMISSION` is a `ToIntFunction<BlockState>`. Place a transient light via `level.setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, n), Block.UPDATE_CLIENTS)` and clear with `Blocks.AIR.defaultBlockState()`.
- **State-aware fixture emission:** `BlockBehaviour.Properties.lightLevel(ToIntFunction<BlockState>)` exists — used to drive fixture light from the block's ON/BLACKOUT state instead of per-tick light objects (e.g. `.lightLevel(s -> (s.getValue(FluorescentLightBlock.ON) && !s.getValue(FluorescentLightBlock.BLACKOUT)) ? 15 : 0)`).
- **Raycast for flashlight placement:** `ClipContext` is `net.minecraft.world.level.ClipContext` (NOT `world.phys`); `level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity))` → `BlockHitResult`; `Entity.getEyePosition(float)` + `Entity.getViewVector(float)` (returns `Vec3`); `BlockPos.containing(Vec3)`. `FabricLoader.getInstance().isModLoaded("lambdynlights")` for the soft-dep.
- **`BlockEntityRenderer` is now render-state-based** (`net.minecraft.client.renderer.blockentity.BlockEntityRenderer<T extends BlockEntity, S extends BlockEntityRenderState>`): `S createRenderState()`, `default void extractRenderState(T, S, float, Vec3, ModelFeatureRenderer$CrumblingOverlay)`, **`abstract void submit(S state, PoseStack, SubmitNodeCollector, CameraRenderState)`** (replaces `render(...)`), `default boolean shouldRenderOffScreen()`, `default int getViewDistance()`, `default boolean shouldRender(T, Vec3)`. (`render-state class` = `net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState`.)
- **Lightmap refactor (A5):** there is NO `LightmapTextureManager`/`LightTexture` in 26.2. The lightmap is now `net.minecraft.client.renderer.Lightmap` (GPU upload via `render(LightmapRenderState)`), the skylight/blocklight color is computed in **`net.minecraft.client.renderer.LightmapRenderStateExtractor.extract(LightmapRenderState, float)`**, and the per-frame data lives on **`net.minecraft.client.renderer.state.LightmapRenderState`** (public fields: `skyLightColor`/`blockLightTint`/`ambientColor` are `Vector3fc`; `skyFactor`/`blockFactor`/`brightness`/... floats). `skyLightColor` is computed from `Camera.attributeProbe().getValue(EnvironmentAttributes.SKY_LIGHT_COLOR, partialTick)` (an RGB24 int) via `ARGB.vector3fFromRGB24(int)` → a concrete `Vector3f`. **The old `@Redirect` of `Vector3f.lerp(Vector3fc,F)` (ordinal 0) in `LightmapTextureManager.update` has NO equivalent — that method contains zero `lerp` calls now.** Retarget: `@Inject(method="extract", at=@At("TAIL"))` and multiply `state.skyLightColor` (cast to `Vector3f`) in place by the day-cycle tint, gated on the dimension key. (`net.minecraft.world.attribute.{EnvironmentAttribute,EnvironmentAttributes,EnvironmentAttributeProbe}`; `Camera.attributeProbe()`.)

### Phase 6/7 compile tail (VERIFIED via javac against the AW-processed merged-deobf 26.2 jar — TRUST THESE)
- **`MinecraftServer.getStructureTemplateManager()` → `getStructureManager()`** (returns `world.level.levelgen.structure.templatesystem.StructureTemplateManager` — class name unchanged, only the getter renamed). Used by `Level1MazeGenerator.spawnRandomRooms`.
- **`Level.getGameRules()` is GONE; `getGameRules()` lives on `ServerLevel`** (returns the relocated `net.minecraft.world.level.gamerules.GameRules`; query with `gameRules.get(GameRule<T>)` where the rule key is `net.minecraft.world.level.gamerules.GameRule<T>`). Server-side `player.level()` call sites must cast to `ServerLevel` first (e.g. PlayerComponent.serverTick).
- **`ChunkPos.x`/`ChunkPos.z`** are `private final int` (read via `chunk.getPos().x`/`.z` in the chunk generators + custom levels) — public accessor methods **`int x()`/`int z()`** also exist, but to stay byte-for-byte faithful to the 1.20.1 field access the port widens the fields via the access widener instead.
- **GeckoLib 5 `GeoEntityRenderer` render-state factory:** the no-arg **`createRenderState()` is `final`** (returns null stub) and CANNOT be overridden. To supply a custom render-state subclass, override **`R createRenderState(T entity, Void relatedObject)`** (from `GeoRendererInternals`) and `return new MyRenderState()`. The base default returns a plain `LivingEntityRenderState`/`EntityRenderState`. (`SkinWalkerRenderer` overrides the 2-arg form; `WalkerRenderer` uses the plain `EntityRenderState` so it needs no override.) `CustomBoneTextureGeoLayer<T,O,R>` overrides are `boolean shouldRenderBone(R)` and `protected RenderType getRenderType(R, Identifier)` (2-arg).

### Phase 6/7 build wiring (VERIFIED — TRUST THESE)
- **Access widener namespace:** this mojmap (Mojang-mappings) Loom 1.17-SNAPSHOT setup requires the AW header **`accessWidener v2 official`**, NOT `named`. Loom's `fabric-loom:access-widener` jar processor rejects `named` here ("Expected official namespace for access widener entry, found: named"). In a mojmap workspace the official namespace already uses the deobfuscated Mojang names, so AW entries are written with normal mojmap names (e.g. `accessible field net/minecraft/world/level/ChunkPos x I`).
- **Mod compile deps use plain Gradle configs, NOT Loom's `mod*` configs.** This project declares mod deps with `implementation`/`compileOnly` (no `modImplementation`/`modCompileOnly`); `modCompileOnly` is unrecognized and fails config. Soft compile-only deps → `compileOnly "maven.modrinth:<slug>:<version>"`.
- **Sound Physics Remastered 26.2 compat:** `maven.modrinth:sound-physics-remastered:fabric-1.5.1+26.2` (Modrinth maven) provides `com.sonicether.soundphysics.config.blocksound.{BlockDefinition,BlockSoundConfigBase}` and `config.SoundTypes` (so `SoundTypesMixin`/`SFRReflectivityMixin` resolve), but **`com.sonicether.soundphysics.config.AllowedSoundConfig` NO LONGER EXISTS** in 1.5.1 (the config pkg now has `SoundPhysicsConfig`/`SoundTypes`/`OcclusionConfig`/`ReflectivityConfig`/`SoundRateConfig`/`ReverbParams` + `blocksound.*`). `AllowedSoundsConfigMixin` targets the removed class → 6f/Phase-5 must retarget or drop it.
- **ModMenu 26.2:** `maven.modrinth:modmenu:20.0.0-beta.4` resolves fine; `com.terraformersmc.modmenu.api.{ModMenuApi,ConfigScreenFactory}` and `ModMenuApi.getModConfigScreenFactory()` are present and unchanged (the earlier errors were a stale classpath cache, not an API change).

### Phase 6f render-tier mixins (VERIFIED via javap on merged-deobf 26.2 + javac — TRUST THESE)
Final verdicts: ~400 mixin compile errors → 0. 20 mixins DROPPED, 9 REWRITTEN. mixins.json: client 34→15, common 18→16. No access-widener entries needed (CameraMixin uses `@Invoker` for the protected `setRotation`).
- **`net.minecraft.client.Camera`** (was `client.render.Camera`): `update(BlockView,Entity,bool,bool,float)` → **`update(DeltaTracker)`**. `setRotation(float yaw,float pitch)` is **protected** and only expresses yaw+pitch — its bytecode does `rotation.rotationYXZ(PI - yaw·deg, -pitch·deg, 0)` (Z/roll hardcoded 0) and sets `matrixPropertiesDirty |= 3`. **`rotation()` is public and returns the live mutable `Quaternionf` field** (verified: `getfield;areturn`), and `getViewRotationMatrix(Matrix4f)` rebuilds the cached view matrix from `rotation.conjugate()` whenever dirty. So: apply shake via `@Invoker setRotation`, then apply roll by `camera.rotation().rotateZ(deg·Mth.DEG_TO_RAD)` at `update` @TAIL — the already-set dirty flag makes the next render-pass `getViewRotationMatrix` pick up the roll. Reproduces the old `GameRenderer.renderWorld` `POSITIVE_Z` view-matrix tilt without touching the camera basis vectors. `yRot()`/`xRot()` are the yaw/pitch getters (were `getYaw`/`getPitch`).
- **`EntityRenderer.renderLabelIfPresent(...VertexConsumerProvider...)` GONE** (name tags are now `EntityRenderState.nameTag` + `submitNameDisplay`). The surviving gate **`protected boolean shouldShowName(Entity, double)`** (desc `(Lnet/minecraft/world/entity/Entity;D)Z`) — `@Inject HEAD cancellable`, `cir.setReturnValue(false)` to hide. `Entity.level().dimension()` gives the `ResourceKey<Level>` for the backrooms check. (`AbstractClientPlayerEntity` → `net.minecraft.client.player.AbstractClientPlayer`.)
- **`HeldItemRenderer` → `net.minecraft.client.renderer.ItemInHandRenderer`**; `renderArmHoldingItem(...VertexConsumerProvider...)` GONE. The first-person hand pass is funnelled through **`public void submitHandsWithItems(float, PoseStack, SubmitNodeCollector, LocalPlayer, int)`** — `@Inject HEAD cancellable` to hide hands (cancels arm+item+map, a superset of the old arm-holding-item-only redirect).
- **`ParticleManager.addBlockBreakingParticles(BlockPos,Direction)` GONE** (`ParticleEngine` has no such method). Block-crack particles now spawn from **`Level.addDestroyBlockEffect(BlockPos, BlockState)`** (overridden on `ClientLevel`) — `@Inject HEAD cancellable` there to suppress.
- **`GameOptions` → `Options`; `SimpleOption<T>` → `OptionInstance<T>`** (`getValue/setValue` → **`get()`/`set(T)`**). Fluent getter renames: `getEntityShadows` → **`entityShadows()`** (`OptionInstance<Boolean>`), `getFovEffectScale` → **`fovEffectScale()`** (`OptionInstance<Double>`). **`getGraphicsMode`/`GraphicsMode` GONE** → **`graphicsPreset()`** returns `OptionInstance<net.minecraft.client.GraphicsPreset>` (same `FAST`/`FANCY`/`FABULOUS`/`CUSTOM` values). `@ModifyReturnValue` works on the fluent accessors.
- **`DebugHud` → `net.minecraft.client.gui.components.DebugScreenOverlay`**; immediate `render(...)` GONE → **`extractRenderState(GuiGraphicsExtractor)`** (`@Inject HEAD cancellable` suppresses the whole overlay).
- **`Minecraft`:** `handleInputEvents` → **`handleKeybinds()`**; perspective toggle now calls **`Options.setCameraType(net.minecraft.client.CameraType)`** (`Perspective` → `CameraType`, `FIRST_PERSON` value present). `disconnect(Screen)` → **`disconnect(Screen, boolean)`** (also a 3-arg form). Per-frame loop: `render(Z)`/`render(...)` → **`runTick(boolean)`** which invokes **`GameRenderer.render(DeltaTracker, boolean)`** (was `render(FJZ)`). `ProgressScreen`/`Screen` → `net.minecraft.client.gui.screens.*`.
- **`LevelProperties`/`PrimaryLevelData` lost ALL weather state** — `getRainTime`/`isRaining`/`getThunderTime`/`isThundering` no longer exist on the level-data classes. Weather moved to **`net.minecraft.world.level.Level`**: `boolean isRaining()`, `boolean isThundering()`, `float getRainLevel(float)`, `float getThunderLevel(float)`, `setRainLevel(float)` (inherited by `ClientLevel` + `ServerLevel`). Retarget weather-forcing `@WrapMethod`s to `Level`.
- **`Monitor` → `com.mojang.blaze3d.platform.Monitor` is an immutable `record`** (components `monitorName/monitor/videoModes/currentMode/x/y`), built once by `static tryCreate(long)`; **no `populateVideoModes`/`refreshVideoModes` instance method** to inject into. The VHS 4:3 resolution-enumeration feature (MonitorMixin + VideoOptionsScreenMixin) has no host method → DROPPED (delta: the fullscreen-resolution dropdown shows vanilla modes only; no custom 4:3 VHS list).
- **`Inventory.scrollInHotbar` GONE** (selection is `getSelectedSlot()`/`setSelectedSlot(int)`; scroll handling moved to `Minecraft`/`MouseHandler`). **`KeyboardInput.jumping` field GONE** — `client.player.KeyboardInput.tick` builds an immutable `new net.minecraft.world.entity.player.Input(forward,backward,left,right,jump,shift,sprint)` record (no settable jump field). Both → DROPPED (PlayerInventoryMixin scroll-time tracking; DisableJumpMixin in-backrooms jump suppression — delta: players can jump normally in non-water backrooms; candidate Phase-7 re-hook via the `Input` ctor or a `Player.jumpFromGround` gate).

### Phase 3 datagen API (VERIFIED via javap on the fabric-data-generation-api-v1 25.4.2+ jar + the merged-deobf 26.2 jar; full `./gradlew build` is GREEN — TRUST THESE)
Datagen API (`net.fabricmc.fabric.api.datagen.v1.*`):
- `DataGeneratorEntrypoint.buildRegistry(net.minecraft.registry.RegistryBuilder)` → **`buildRegistry(net.minecraft.core.RegistrySetBuilder)`**. `onInitializeDataGenerator(FabricDataGenerator)` unchanged; `FabricDataGenerator.createPack()` + `Pack.addProvider(...)` unchanged.
- **`FabricDataOutput` is GONE → `net.fabricmc.fabric.api.datagen.v1.FabricPackOutput`** (extends vanilla `net.minecraft.data.PackOutput`). It is what every provider ctor now takes. The `Pack.Factory<T>` is `T create(FabricPackOutput)`; `Pack.RegistryDependentFactory<T>` is `T create(FabricPackOutput, CompletableFuture<HolderLookup.Provider>)`.
- **`FabricModelProvider` MOVED packages**: `net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider` → **`net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider`** (it's now a *client* datagen provider, extends `net.minecraft.client.data.models.ModelProvider`). Ctor takes `FabricPackOutput`. Abstract methods: `generateBlockStateModels(net.minecraft.client.data.models.BlockModelGenerators)` and `generateItemModels(net.minecraft.client.data.models.ItemModelGenerators)` (were `net.minecraft.data.client.{BlockStateModelGenerator,ItemModelGenerator}`).
- `FabricDynamicRegistryProvider`: ctor `(FabricPackOutput, CompletableFuture<HolderLookup.Provider>)`; `configure(net.minecraft.core.HolderLookup$Provider, Entries)` (was `RegistryWrapper.WrapperLookup`). `Entries.addAll(HolderLookup.RegistryLookup<T>)` — feed it `registries.lookupOrThrow(Registries.X)`.

Vanilla model-generator API (`net.minecraft.data.client.*` → **`net.minecraft.client.data.models.*`**):
- `BlockStateModelGenerator` → **`BlockModelGenerators`**; `ItemModelGenerator` → **`ItemModelGenerators`**.
- The convenience helpers went **private** in 26.2 (1.20.1 exposed them publicly): `registerSimpleCubeAll(Block)` → **`createTrivialCube(Block)`** (private); `registerCubeAllModelTexturePool(Block)` (returned a `BlockTexturePool`) → **`family(Block)`** (private, returns the package-private `BlockModelGenerators$BlockFamilyProvider`); the pool's `.slab(Block)` → **`BlockFamilyProvider.slab(Block)`** (public on a package-private class). To call them from a mod provider, widen via the AW: `accessible method .../BlockModelGenerators createTrivialCube (Lnet/minecraft/world/level/block/Block;)V`, `accessible method .../BlockModelGenerators family (L.../Block;)L.../BlockModelGenerators$BlockFamilyProvider;`, `accessible class .../BlockModelGenerators$BlockFamilyProvider`.
- `TexturedModel.Provider` constants (CUBE/COLUMN/CUBE_TOP_BOTTOM/...) live at `net.minecraft.client.data.models.model.TexturedModel`.
- **`runDatagen` runtime blocker (NOT a datagen-code issue):** the datagen client crashes at mod-load with "requires fabric-key-binding-api-v1, which is missing" because `fabric-api` is declared with plain `implementation` (not `modImplementation`) — Loom never exposes the bundled fabric-api modules as loadable mods, so no fabric-* module resolves at any runtime run (datagen/client/server). `./gradlew build` (compile/remap/AW/jar) is unaffected and GREEN. The existing `src/main/generated/` output (from a prior run) stands. Phase-8 fix: switch fabric-api (and the runtime mod deps) to Loom's `mod*` configs, OR otherwise surface the fabric modules on the dev runtime classpath.

### Phase 3 data-JSON schema migration (26.2) — VERIFIED against vanilla 26.2 data extracted from the merged jar
- **`data/<ns>/loot_tables/` → `data/<ns>/loot_table/`** (datapack dir renamed singular; vanilla ships `data/minecraft/loot_table/`). The runtime loot-table id is unchanged.
- **`dimension_type` schema fully reworked.** Old flat 1.20.1 keys `bed_works`/`has_raids`/`piglin_safe`/`respawn_anchor_works`/`natural`/`ultrawarm`/`effects`/`fixed_time` are GONE from the top level. 26.2: gameplay/visual flags move into an **`attributes`** map with namespaced keys (`minecraft:gameplay/bed_rule` {can_set_spawn,can_sleep,explodes}, `minecraft:gameplay/can_start_raid`, `minecraft:gameplay/respawn_anchor_works`, `minecraft:gameplay/piglins_zombify`, `minecraft:visual/*`, `minecraft:audio/*`); **`fixed_time:N` → `has_fixed_time:true`** + a celestial **`timelines`** tag (e.g. `#minecraft:in_overworld`); **`has_ender_dragon_fight`** is now expected; `monster_spawn_light_level` uniform provider dropped its `{"value":{...}}` wrapper (now `max_inclusive`/`min_inclusive` inline; a 0..0 uniform is just the int `0`). Survivors at top level: `ambient_light`, `coordinate_scale`, `has_ceiling`, `has_skylight`, `height`, `infiniburn`, `logical_height`, `min_y`, `monster_spawn_block_light_limit`. Sky preset moved to `skybox`/attributes (the old `"effects":"minecraft:overworld"` is gone). Reference: vanilla `dimension_type/{overworld,the_end,the_nether,overworld_caves}.json`.
- **`worldgen/biome`: `carvers` is a LIST now** (`"carvers": []`), was a `{}` map in 1.20.1. A `mood_sound` with an empty `sound` string is invalid (must be a real sound-event id) → drop it.
- **`damage_type`** schema is unchanged (`effects`/`exhaustion`/`message_id`/`scaling` all still valid — `smiler.json`/`acid_water.json` need no migration).
- **Item model-definition layer (NEW, required):** every item needs **`assets/<ns>/items/<id>.json`** = `{"model":{"type":"minecraft:model","model":"<ns>:item/<id>"}}` (block items can point at `<ns>:block/<id>`). Verified against vanilla `assets/minecraft/items/{stick,apple,stone}.json`. Without it the item renders as the missing-model cube.
- **Block `render_type` / cutout+translucent — NO `render_type` JSON key in 26.2.** `BlockRenderLayerMap` (the old `SPBRevampedClient.putBlock(...)` list) is GONE and has **no successor in `fabric-rendering-v1`**; there is also **no `render_type` model-json extension** in 26.2 Fabric (none of `fabric-model-loading-api-v1`, `fabric-renderer-api-v1` read that key). Vanilla 26.2 instead auto-detects cutout from the parent template (e.g. `block/cross`, `block/door_*`) and opts into translucency via **`"force_translucent": true`** on the *texture* entry of a `cube_all`-style model. The mod's transparent blocks (24 from the 1.20.1 list, below) use custom element-based models, so the exact 26.2 mechanism per block must be settled with the client running → deferred to Phase 8. **The 1.20.1 render-tier list (from `SPBRevampedClient` @ commit 3da1551, mapped to model files):** CUTOUT — `bottom_trim` (models wall_trim_1/3/corner/sides), `wall_text_1..8`+`wall_text_99`, `arrow1..4` (WALL_ARROW_1..4), `wall_small_1/2`, `wall_drawing_door`, `wall_drawing_window`, `rug1`/`rug2` (RUG_1/2), `red_metal_casing`. TRANSLUCENT — `window`. SPECIAL — `pool_sky` (POOLROOMS_SKY_BLOCK) used a custom `getPoolroomsSky()` layer and shares the `fluorescent_light` model with `poolrooms_window` + the real `fluorescent_light` block (which is rendered by a BlockEntityRenderer), so it needs a dedicated model before any tier hint can be applied — do NOT touch `fluorescent_light.json` directly.
