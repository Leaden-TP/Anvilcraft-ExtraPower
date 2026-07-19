package com.extra.power.block.blockentity;

import com.extra.power.api.entity.IEasyAnimation;
import com.extra.power.block.just_block.FrostControllerBlock;
import com.extra.power.block.just_block.UraniumRodBlock;
import com.extra.power.function.AnimationFunction;
import com.extra.power.network.UpdateAnimationStatePacket;
import dev.dubhe.anvilcraft.api.tooltip.providers.IHasAffectRange;
import dev.dubhe.anvilcraft.block.state.Vertical3PartHalf;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.extra.power.block.ModBlockEntity.FROST_CONTROLLER;


public class FrostControllerBlockEntity extends BlockEntity implements IHasAffectRange , IEasyAnimation {
    private int tickCounter = 0;
    private Double rotation = 0.0;
    @Getter
    private List<Double> action_state = new ArrayList<>(Arrays.asList(0.0));

    public FrostControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    public static FrostControllerBlockEntity createBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState blockState
    ) {
        return new FrostControllerBlockEntity(type, pos, blockState);
    }
    public FrostControllerBlockEntity(BlockPos pos, BlockState state) {
        super(FROST_CONTROLLER.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state, FrostControllerBlockEntity entity) {
        if (level.isClientSide()) return;
        entity.tickCounter++;

        if (this.action_state.get(0) >= 360){
            this.rotation = 0.0;
            this.action_state = Arrays.asList(0.0);
        }
        if (!state.getValue(FrostControllerBlock.ACTIVE)){
            this.rotation = 0.0;
        }
        List<Double> target_state = Arrays.asList(0.0);
        if (state.getValue(FrostControllerBlock.ACTIVE)) {
            this.rotation += 3;
            target_state = Arrays.asList(this.rotation);
        }
        this.action_state = AnimationFunction.trackTarget(action_state, target_state);
        syncAnimationState();

        if (entity.tickCounter >= 60) {

            if (state.getValue(FrostControllerBlock.HALF) != Vertical3PartHalf.MID || !state.getValue(FrostControllerBlock.ACTIVE)) {
                return;
            }
            entity.tickCounter = 0;
            if(checkRod(level,pos)>0)return;
            int radius = 2;
            List<BlockPos> waterPositions = new ArrayList<>();
            for (int x = -radius - 1; x <= radius + 1; x++) {
                for (int z = -radius - 1; z <= radius + 1; z++) {
                    for (int y = -radius; y <= radius; y++) {
                        BlockPos targetPos = pos.offset(x, y, z);
                        if (level.isOutsideBuildHeight(targetPos)) continue;
                        BlockState targetState = level.getBlockState(targetPos);

                        if (targetState.getBlock() == Blocks.WATER) {
                            waterPositions.add(targetPos);
                        }
                    }
                }
            }
            if (!waterPositions.isEmpty()) {
                BlockPos selectedPos = waterPositions.get(level.getRandom().nextInt(waterPositions.size()));
                level.setBlock(selectedPos, Blocks.ICE.defaultBlockState(), 3);
            }

        }

    }
    public static int checkRod(Level level, BlockPos pos) {
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        int rod = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
                    mpos.set(pos).move(i, j, k);
                    if (level.isOutsideBuildHeight(mpos)) continue;
                    BlockState blockState = level.getBlockState(mpos);
                    if (blockState.getBlock() instanceof UraniumRodBlock && blockState.getValue(UraniumRodBlock.HALF) == Vertical3PartHalf.MID) {
                        rod+=1;
                    }
                }
            }
        }
        return rod;
    }
    private void syncAnimationState() {
        if (level == null || level.isClientSide()) return;
        PacketDistributor.sendToPlayersTrackingChunk(
                (ServerLevel) level,
                level.getChunk(getBlockPos()).getPos(),
                new UpdateAnimationStatePacket(new ArrayList<>(this.action_state), getBlockPos())
        );
    }
    public void updateActionState(List<Double> newState) {
        if (level != null && level.isClientSide) {
            // 只在客户端更新
            for (int i = 0; i < Math.min(action_state.size(), newState.size()); i++) {
                action_state.set(i, newState.get(i));
            }
        }
    }
    @Override
    public AABB shape() {
        return AABB.ofSize(getBlockPos().getCenter(), 3, 3, 3);
    }
}