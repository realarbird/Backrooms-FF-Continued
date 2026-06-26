package com.sp.block.entity;

import com.sp.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class WoodenCrateBlockEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> inventory = NonNullList.withSize(27, ItemStack.EMPTY);

    private final ContainerOpenersCounter stateManager = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level world, BlockPos pos, BlockState state) {
            WoodenCrateBlockEntity.this.playSound(state, SoundEvents.BARREL_OPEN);
        }

        @Override
        protected void onClose(Level world, BlockPos pos, BlockState state) {
            WoodenCrateBlockEntity.this.playSound(state, SoundEvents.BARREL_CLOSE);
        }

        @Override
        protected void openerCountChanged(Level world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
        }

        @Override
        public boolean isOwnContainer(Player player) {
            if (player.containerMenu instanceof ChestMenu) {
                net.minecraft.world.Container inventory = ((ChestMenu)player.containerMenu).getContainer();
                return inventory == WoodenCrateBlockEntity.this;
            } else {
                return false;
            }
        }
    };

    public WoodenCrateBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.WOODEN_CRATE_BLOCK_ENTITY, blockPos, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput nbt) {
        super.saveAdditional(nbt);
        if (!this.trySaveLootTable(nbt)) {
            ContainerHelper.saveAllItems(nbt, this.inventory);
        }
    }

    @Override
    protected void loadAdditional(ValueInput nbt) {
        super.loadAdditional(nbt);
        this.inventory = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(nbt)) {
            ContainerHelper.loadAllItems(nbt, this.inventory);
        }
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> list) {
        this.inventory = list;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.spb-revamped.wooden_crate");
    }

    @Override
    protected AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
        return ChestMenu.threeRows(syncId, playerInventory, this);
    }

    @Override
    public void startOpen(ContainerUser user) {
        if (!this.isRemoved() && !user.getLivingEntity().isSpectator()) {
            this.stateManager.incrementOpeners(user.getLivingEntity(), this.getLevel(), this.getBlockPos(), this.getBlockState(), user.getContainerInteractionRange());
        }
    }

    @Override
    public void stopOpen(ContainerUser user) {
        if (!this.isRemoved() && !user.getLivingEntity().isSpectator()) {
            this.stateManager.decrementOpeners(user.getLivingEntity(), this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    public void tick() {
        if (!this.isRemoved()) {
            this.stateManager.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    void playSound(BlockState state, SoundEvent soundEvent) {
        this.level.playSound(null, this.getBlockPos(), soundEvent, SoundSource.BLOCKS, 0.5F, this.level.getRandom().nextFloat() * 0.1F + 0.9F);
    }
}
