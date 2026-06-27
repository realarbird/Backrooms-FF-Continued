package com.sp.cca_stuff;

import com.sp.SPBRevamped;
import com.sp.clientWrapper.ClientWrapper;
import com.sp.entity.custom.SmilerEntity;
import com.sp.init.*;
import com.sp.mixininterfaces.ServerPlayNetworkSprint;
import com.sp.sounds.voicechat.BackroomsVoicechatPlugin;
import com.sp.world.levels.BackroomsLevel;
import com.sp.world.levels.custom.Level2BackroomsLevel;
import com.sp.world.levels.custom.Level324Backroomslevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static com.sp.SPBRevamped.SLOW_SPEED_MODIFIER;

@SuppressWarnings("DataFlowIssue")
public class PlayerComponent implements AutoSyncedComponent, ClientTickingComponent, ServerTickingComponent {
    public final Player player;
    // 26.2: PlayerInventory split (main/armor/offHand DefaultedLists) is gone; Inventory is now a single
    // flat Container. The saved copy mirrors the whole inventory in one SimpleContainer sized to match.
    private SimpleContainer playerSavedInventory;
    private final Random random = new Random();

    private int smilerSpawnDelay = 80;

    private int stamina;
    private boolean tired;

    private int scrollingInInventoryTime;

    private boolean flashLightOn;
    private boolean shouldRender;
    private boolean isDoingCutscene;
    private boolean playingGlitchSound;
    private boolean shouldNoClip;
    private int teleportingTimer;
    private boolean isTeleporting;

    public int suffocationTimer;
    private boolean shouldDoStatic;

    private boolean isBeingCaptured;
    public boolean hasBeenCaptured;
    private boolean isBeingReleased;
    private Entity targetEntity;
    private int skinWalkerLookDelay;
    private boolean shouldBeMuted;
    private boolean isSpeaking;
    private int speakingBuffer;
    private float prevSpeakingTime;
    private boolean visibleToEntity;
    private int visibilityTimer;
    private int visibilityTimerCooldown;
    private boolean talkingTooLoud;
    private int talkingTooLoudTimer;
    private GameType prevGameMode;

    // PORT: phase6 — these client sound-instance fields are assigned by ClientWrapper (phase 6).
    // MovingSoundInstance (Yarn) maps to AbstractTickableSoundInstance in mojmap.
    public AbstractTickableSoundInstance DeepAmbience;
    public AbstractTickableSoundInstance GasPipeAmbience;
    public AbstractTickableSoundInstance WaterPipeAmbience;
    public AbstractTickableSoundInstance WarpAmbience;
    public AbstractTickableSoundInstance PoolroomsNoonAmbience;
    public AbstractTickableSoundInstance PoolroomsSunsetAmbience;
    public AbstractTickableSoundInstance GlitchAmbience;
    public AbstractTickableSoundInstance SmilerAmbience;
    public AbstractTickableSoundInstance WindAmbience;
    public AbstractTickableSoundInstance WindTunnelAmbience;

    private boolean canSeeActiveSkinWalker;
    private boolean prevFlashLightOn;

    public float glitchTimer;
    private boolean shouldGlitch;
    public int glitchTick;
    public boolean shouldInflictGlitchDamage;

    public BackroomsLevel.LevelTransition currentTransition = null;

    public PlayerComponent(Player player){
        this.stamina = 300;
        this.tired = false;
        this.scrollingInInventoryTime = 0;
        this.player = player;
        this.playerSavedInventory = new SimpleContainer(player.getInventory().getContainerSize());
        this.flashLightOn = false;
        this.shouldRender = true;
        this.shouldNoClip = false;
        this.shouldDoStatic = false;

        this.isDoingCutscene = false;

        this.isBeingCaptured = false;
        this.hasBeenCaptured = false;
        this.isBeingReleased = false;
        this.skinWalkerLookDelay = 60;
        this.shouldBeMuted = false;
        this.isSpeaking = false;
        this.speakingBuffer = 80;
        this.prevSpeakingTime = 0;
        this.visibleToEntity = false;
        this.visibilityTimer = 15;
        this.visibilityTimerCooldown = 0;

        this.talkingTooLoud = false;
        this.talkingTooLoudTimer = 20;

        this.suffocationTimer = 0;

        this.canSeeActiveSkinWalker = false;

        this.glitchTimer = 0.0f;
        this.shouldGlitch = false;
        this.glitchTick = 0;

        this.teleportingTimer = -1;
    }

    public void savePlayerInventory() {
        // 26.2: Inventory is a single flat Container; copy each non-empty slot into the saved mirror.
        net.minecraft.world.entity.player.Inventory inventory = this.player.getInventory();
        int size = inventory.getContainerSize();
        if (this.playerSavedInventory.getContainerSize() != size) {
            this.playerSavedInventory = new SimpleContainer(size);
        }
        for (int i = 0; i < size; i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (!itemStack.isEmpty()) {
                this.playerSavedInventory.setItem(i, itemStack);
            }
        }
    }

    public void loadPlayerSavedInventory() {
        net.minecraft.world.entity.player.Inventory inventory = this.player.getInventory();
        inventory.clearContent();
        int size = Math.min(inventory.getContainerSize(), this.playerSavedInventory.getContainerSize());
        for (int i = 0; i < size; i++) {
            ItemStack itemStack = this.playerSavedInventory.getItem(i);
            if (!itemStack.isEmpty()) {
                inventory.setItem(i, itemStack);
            }
        }

        this.playerSavedInventory.clearContent();
    }

    public int getTeleportingTimer() {
        return teleportingTimer;
    }

    public void setTeleportingTimer(int teleportingTimer) {
        this.teleportingTimer = teleportingTimer;
        this.justChanged();
    }

    public boolean isTeleporting() {
        return isTeleporting;
    }
    public void setTeleporting(boolean teleporting) {
        isTeleporting = teleporting;
    }

    public int getStamina() {
        return stamina;
    }
    public void setStamina(int stamina) {
        this.stamina = stamina;
    }

    public boolean isTired() {
        return tired;
    }
    public void setTired(boolean tired) {
        this.tired = tired;
    }

    public int getScrollingInInventoryTime() {
        return scrollingInInventoryTime;
    }
    public void setScrollingInInventoryTime(int scrollingInInventoryTime) {
        this.scrollingInInventoryTime = scrollingInInventoryTime;
    }

    public boolean isShouldRender() {
        return shouldRender;
    }
    public void setShouldRender(boolean shouldRender) {
        this.shouldRender = shouldRender;
    }

    public void setFlashLightOn(boolean set){
        this.flashLightOn = set;
    }
    public boolean isFlashLightOn() {
        return flashLightOn;
    }

    public boolean isDoingCutscene() {
        return isDoingCutscene;
    }
    public void setDoingCutscene(boolean doingCutscene) {
        isDoingCutscene = doingCutscene;
    }

    public boolean isTeleportingToPoolrooms() {
        BackroomsLevel level = BackroomsLevels.getLevel(this.player.level()).orElse(BackroomsLevels.OVERWORLD_REPRESENTING_BACKROOMS_LEVEL);
        return (level instanceof Level2BackroomsLevel || level instanceof Level324Backroomslevel) && this.teleportingTimer > 0;
    }

    public boolean shouldNoClip() {
        return shouldNoClip;
    }
    public void setShouldNoClip(boolean shouldNoClip) {
        this.shouldNoClip = shouldNoClip;
    }

    public boolean isShouldDoStatic() {
        return shouldDoStatic;
    }
    public void setShouldDoStatic(boolean shouldDoStatic) {
        this.shouldDoStatic = shouldDoStatic;
    }

    public boolean isBeingCaptured() {return isBeingCaptured;}
    public void setBeingCaptured(boolean beingCaptured) {isBeingCaptured = beingCaptured;}

    public boolean hasBeenCaptured() {return hasBeenCaptured;}
    public void setHasBeenCaptured(boolean hasBeenCaptured) {this.hasBeenCaptured = hasBeenCaptured;}

    public boolean isBeingReleased() {
        return isBeingReleased;
    }
    public void setBeingReleased(boolean beingReleased) {
        isBeingReleased = beingReleased;
    }

    public Entity getTargetEntity() {return targetEntity;}
    public void setTargetEntity(Entity targetEntity) {
        this.targetEntity = targetEntity;
    }

    public int getSkinWalkerLookDelay() {
        return skinWalkerLookDelay;
    }
    public void setSkinWalkerLookDelay(int skinWalkerLookDelay) {
        this.skinWalkerLookDelay = skinWalkerLookDelay;
    }
    public void subtractSkinWalkerLookDelay() {
        this.skinWalkerLookDelay -= 1;
    }

    public boolean shouldBeMuted() {return shouldBeMuted;}
    public void setShouldBeMuted(boolean shouldStayUnmuted) {this.shouldBeMuted = shouldStayUnmuted;}

    public boolean isSpeaking() {
        return isSpeaking;
    }
    public void setSpeaking(boolean speaking) {
        isSpeaking = speaking;
    }

    public boolean isVisibleToEntity() {return visibleToEntity;}
    public void setVisibleToEntity(boolean visibleToEntity) {this.visibleToEntity = visibleToEntity;}

    public boolean isTalkingTooLoud() {
        return talkingTooLoud;
    }
    public void setTalkingTooLoud(boolean talkingTooLoud) {
        this.talkingTooLoud = talkingTooLoud;
    }

    public void resetTalkingTooLoudTimer(){
        this.talkingTooLoudTimer = 20;
    }

    public GameType getPrevGameMode() {
        return prevGameMode;
    }
    public void setPrevGameMode(GameType prevGameMode) {
        this.prevGameMode = prevGameMode;
    }

    public boolean canSeeActiveSkinWalkerTarget() {return canSeeActiveSkinWalker;}
    public void setCanSeeActiveSkinWalkerTarget(boolean canSeeActiveSkinWalker) {this.canSeeActiveSkinWalker = canSeeActiveSkinWalker;}

    public float getGlitchTimer() {
        return glitchTimer;
    }

    public boolean shouldGlitch() {
        return shouldGlitch;
    }
    public void setShouldGlitch(boolean shouldGlitch) {
        this.shouldGlitch = shouldGlitch;
    }

    public void setShouldInflictGlitchDamage(boolean shouldInflictGlitchDamage) {
        this.shouldInflictGlitchDamage = shouldInflictGlitchDamage;
    }

    @Override
    public void readData(ValueInput tag) {
        this.stamina = tag.getIntOr("stamina", 300);
        this.flashLightOn = tag.getBooleanOr("flashLightOn", false);
        this.shouldRender = tag.getBooleanOr("shouldRender", true);
        this.isDoingCutscene = tag.getBooleanOr("isDoingCutscene", false);
        this.playingGlitchSound = tag.getBooleanOr("playingGlitchSound", false);
        this.shouldNoClip = tag.getBooleanOr("shouldNoClip", false);
        this.shouldDoStatic = tag.getBooleanOr("shouldDoStatic", false);
        this.isBeingCaptured = tag.getBooleanOr("isBeingCaptured", false);
        this.hasBeenCaptured = tag.getBooleanOr("hasBeenCaptured", false);
        this.isBeingReleased = tag.getBooleanOr("isBeingReleased", false);
        this.shouldBeMuted = tag.getBooleanOr("shouldBeMuted", false);
        this.shouldGlitch = tag.getBooleanOr("shouldGlitch", false);
        this.shouldInflictGlitchDamage = tag.getBooleanOr("shouldInflictGlitchDamage", false);
        this.teleportingTimer = tag.getIntOr("teleportingTimer", -1);

        // 26.2: saved inventory serialised as a flat item list via ContainerHelper into a child tag.
        NonNullList<ItemStack> items = NonNullList.withSize(this.playerSavedInventory.getContainerSize(), ItemStack.EMPTY);
        tag.child("inventory").ifPresent(child -> ContainerHelper.loadAllItems(child, items));
        for (int i = 0; i < items.size(); i++) {
            this.playerSavedInventory.setItem(i, items.get(i));
        }
    }

    @Override
    public void writeData(ValueOutput tag) {
        tag.putInt("stamina", this.stamina);
        tag.putBoolean("flashLightOn", this.flashLightOn);
        tag.putBoolean("shouldRender", this.shouldRender);
        tag.putBoolean("isDoingCutscene", this.isDoingCutscene);
        tag.putBoolean("playingGlitchSound", this.playingGlitchSound);
        tag.putBoolean("shouldNoClip", this.shouldNoClip);
        tag.putBoolean("shouldDoStatic", this.shouldDoStatic);
        tag.putBoolean("isBeingCaptured", this.isBeingCaptured);
        tag.putBoolean("hasBeenCaptured", this.hasBeenCaptured);
        tag.putBoolean("isBeingReleased", this.isBeingReleased);
        tag.putBoolean("shouldBeMuted", this.shouldBeMuted);
        tag.putBoolean("shouldGlitch", this.shouldGlitch);
        tag.putBoolean("shouldInflictGlitchDamage", this.shouldInflictGlitchDamage);
        tag.putInt("teleportingTimer", this.teleportingTimer);

        if (BackroomsLevels.isInBackrooms(this.player.level().dimension())) {
            ContainerHelper.saveAllItems(tag.child("inventory"), this.playerSavedInventory.getItems());
        }
    }

    public void sync() {
        InitializeComponents.PLAYER.sync(this.player);
    }

    @Override
    public void clientTick() {
        ClientWrapper.tickClientPlayerComponent(this);
    }

    @Override
    public void serverTick() {
        getPrevSettings();

        //*Update Stamina
        updateStamina();

        //*Damage if glitched enough from smilers
        if(this.shouldInflictGlitchDamage){
            this.player.hurtServer((ServerLevel) this.player.level(), ModDamageTypes.of(this.player.level(), ModDamageTypes.SMILER), 1.0f);
        }

        //*Is speaking
        if(BackroomsVoicechatPlugin.speakingTime.containsKey(this.player.getUUID()) && BackroomsVoicechatPlugin.speakingTime.get(this.player.getUUID()) == this.prevSpeakingTime) {
            if(this.isSpeaking()) {
                this.speakingBuffer--;
                if (this.speakingBuffer <= 0) {
                    this.setSpeaking(false);
                    this.speakingBuffer = 80;
                }
            }
        } else {
            this.speakingBuffer = 80;
        }

        //*Cast him to the Backrooms
        if (checkBackroomsTeleport()) return;

        Optional<BackroomsLevel> backroomsLevel = BackroomsLevels.getLevel(this.player.level());

        if (backroomsLevel.isPresent()) {
            BackroomsLevel level = backroomsLevel.get();

            List<BackroomsLevel.LevelTransition> teleports = level.checkForTransition(this, this.player.level());

            if (!teleports.isEmpty() && currentTransition == null) {
                currentTransition = teleports.get(0);
            }

            if (level == BackroomsLevels.LEVEL324_BACKROOMS_LEVEL && this.player.level().getBlockState(this.player.blockPosition().relative(Direction.DOWN, 3)).getBlock() == Blocks.WOOL.red()) {
                this.player.teleportTo(this.player.getX(), this.player.getY() - 64, this.player.getZ());
            }

            if (level == BackroomsLevels.LEVEL324_BACKROOMS_LEVEL && this.player.level().getBlockState(this.player.blockPosition().relative(Direction.DOWN, 3)).getBlock() == Blocks.WOOL.yellow()) {
                this.player.teleportTo(this.player.getX(), this.player.getY() + 64, this.player.getZ());
            }
        }

        if (currentTransition != null) {
            if (teleportingTimer == -1) {
                this.setTeleportingTimer(currentTransition.duration());
            }

            if (teleportingTimer == 0) {
                ServerLevel destination = this.player.level().getServer().getLevel(currentTransition.teleport().to().getWorldKey());
                Player transitionPlayer = currentTransition.teleport().playerComponent().player;
                Vec3 targetPos = currentTransition.teleport().pos();

                if (currentTransition.teleport().to() == BackroomsLevels.OVERWORLD_REPRESENTING_BACKROOMS_LEVEL) {
                    currentTransition.teleport().playerComponent().sync();

                    // 26.2: getGameRules() moved off Level onto ServerLevel; this runs server-side.
                    if (((ServerLevel) this.player.level()).getGameRules().get(ModGamerules.STUCK_IN_BACKROOMS)) {
                        destination = this.player.level().getServer().getLevel(BackroomsLevels.LEVEL0_BACKROOMS_LEVEL.getWorldKey());
                        targetPos = BackroomsLevels.LEVEL0_BACKROOMS_LEVEL.getSpawnPos();
                    }
                }

                currentTransition.teleport().to().transitionOut(currentTransition.teleport());
                transitionPlayer.teleport(new TeleportTransition(destination, targetPos, transitionPlayer.getDeltaMovement(), transitionPlayer.getYRot(), transitionPlayer.getXRot(), TeleportTransition.DO_NOTHING));
                currentTransition.teleport().to().transitionIn(currentTransition.teleport());

                currentTransition = null;
            }
        }

        if (teleportingTimer >= 0) {
            if (currentTransition != null) {
                currentTransition.callback().tick(currentTransition.teleport(), teleportingTimer);
            }
            this.setTeleportingTimer(teleportingTimer - 1);
        }

        if (BackroomsLevels.isInBackroomsLevel(player.level(), BackroomsLevels.LEVEL324_BACKROOMS_LEVEL) && player.position().subtract(0, 64, 0).lengthSqr() > 10000 && player.position().y > 60) {
            summonSmilers();
        }

        //*Update Entity Visibility
        updateEntityVisibility();

        if(BackroomsVoicechatPlugin.speakingTime.containsKey(this.player.getUUID())) {
            this.prevSpeakingTime = BackroomsVoicechatPlugin.speakingTime.get(this.player.getUUID());
        }

        shouldSync();
    }

    private void summonSmilers() {
        if (this.smilerSpawnDelay < 0) {
            SmilerEntity smiler = ModEntities.SMILER_ENTITY.create(this.player.level(), EntitySpawnReason.EVENT);
            if (smiler == null) {
                smilerSpawnDelay--;
                return;
            }

            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            float randomAngle = random.nextFloat() * 360.0f;
            Vec3 spawnPos = new Vec3(0, 0, 15).yRot(randomAngle).add(player.position());
            if (!this.player.level().getBlockState(mutable.set(spawnPos.x, spawnPos.y, spawnPos.z)).blocksMotion()) {
                smiler.snapTo(Math.floor(spawnPos.x) + 0.5f, spawnPos.y, Math.floor(spawnPos.z) + 0.5f, 0.0f, 0.0f);
                this.player.level().addFreshEntity(smiler);
                smilerSpawnDelay = 80;
            }
        }

        smilerSpawnDelay--;
    }

    private void updateStamina() {
        AttributeInstance attributeInstance = this.player.getAttribute(Attributes.MOVEMENT_SPEED);
        if(attributeInstance != null) {
            int prevStamina = this.stamina;
            if(!this.player.isCreative() && !this.player.isSpectator()){
                if(this.player.isSprinting()) {
                    this.stamina--;
                } else {
                    this.stamina = Math.min(this.stamina + 1, 300);
                }
            } else {
                this.stamina = 300;
            }

            if(this.stamina <= 0){
                this.stamina = 0;
                this.setTired(true);
            }

            if(this.isTired()){
                this.player.setSprinting(false);
                this.player.causeFoodExhaustion(0.05f);
                if(this.stamina > 200){
                    this.setTired(false);
                }
            } else if(!((ServerPlayNetworkSprint)((ServerPlayer)this.player).connection).getShouldStopSprinting()){
                this.player.setSprinting(true);
            }

            if ((!player.isShiftKeyDown() && !player.isSprinting()) || this.isTired()) {
                if(!attributeInstance.hasModifier(SLOW_SPEED_MODIFIER.id())) {
                    attributeInstance.addTransientModifier(SLOW_SPEED_MODIFIER);
                }
            } else if(attributeInstance.hasModifier(SLOW_SPEED_MODIFIER.id())) {
                attributeInstance.removeModifier(SLOW_SPEED_MODIFIER);
            }
            //*Mod by 20 to reduce packet count
            if(prevStamina != this.stamina && this.stamina % 20 == 0){
                //*Only sync with the specific player since other players don't need to know your stamina
                InitializeComponents.PLAYER.syncWith((ServerPlayer) this.player, (ComponentProvider) this.player);
            }

        }
    }

    private boolean checkBackroomsTeleport() {
        if (this.player.isInWall()) {
            if (this.player.level().dimension() == Level.OVERWORLD && !this.isDoingCutscene()) {
                suffocationTimer++;
                if (suffocationTimer == 1) {
                    SPBRevamped.sendPersonalPlaySoundPacket((ServerPlayer) this.player, ModSounds.GLITCH, 1.0f, 1.0f);
                    this.playingGlitchSound = true;
                }

                if (suffocationTimer == 40) {
                    ResourceKey<Level> registryKey = BackroomsLevels.LEVEL0_WORLD_KEY;
                    ServerLevel backrooms = this.player.level().getServer().getLevel(registryKey);
                    if (backrooms == null) {
                        return true;
                    }

                    this.savePlayerInventory();
                    this.sync();
                    this.player.getInventory().clearContent();
                    this.player.teleport(new TeleportTransition(backrooms, new Vec3(1.5, 22, 1.5), Vec3.ZERO, this.player.getYRot(), this.player.getXRot(), TeleportTransition.DO_NOTHING));
                    this.setDoingCutscene(true);
                    this.sync();
                    suffocationTimer = 0;
                }
            }
        } else {
            if (this.playingGlitchSound) {
                ClientboundStopSoundPacket stopSoundPacket = new ClientboundStopSoundPacket(Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "glitch"), null);
                ((ServerPlayer) this.player).connection.send(stopSoundPacket);
            }
            this.playingGlitchSound = false;
            suffocationTimer = 0;
        }
        return false;
    }

    private void updateEntityVisibility() {
        if(this.isTalkingTooLoud() && this.talkingTooLoudTimer >= 0){
            this.setVisibleToEntity(true);
            this.talkingTooLoudTimer--;
        } else if(this.talkingTooLoudTimer <= 0){
            this.setVisibleToEntity(false);
            this.setTalkingTooLoud(false);
            this.resetTalkingTooLoudTimer();
        }

        if(!this.isVisibleToEntity()) {
            // 26.2: Entity.horizontalSpeed/prevHorizontalSpeed are gone; moveDist tracks accumulated
            // horizontal movement and is reset each tick, so a non-zero value means the player moved.
            float speed = this.player.moveDist;

            if (!this.player.isShiftKeyDown() && speed != 0) {
                this.visibilityTimer--;
            }
            if (this.player.isSprinting()) {
                this.visibilityTimer--;
            }

            //reset timer if the player is not moving
            if(speed == 0){
                this.visibilityTimer = 15;
            }

            if (this.visibilityTimer <= 0) {
                this.visibilityTimerCooldown = 20;
                this.setVisibleToEntity(true);
            }
        } else {
            if(this.visibilityTimerCooldown > 0){
                this.visibilityTimerCooldown--;
            } else {
                this.visibilityTimer = 15;
                if(!this.isTalkingTooLoud()) {
                    this.setVisibleToEntity(false);
                }
            }
        }
    }

    public void justChanged() {
        this.sync();
    }

    private void shouldSync() {
        boolean sync = this.prevFlashLightOn != this.flashLightOn;

        if (sync) {
            this.sync();
        }
    }

    private void getPrevSettings() {
        this.prevFlashLightOn = this.flashLightOn;
    }
}
