package com.extra.power.block.just_block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 未安装机械动力（Create）时的应力发电机。
 *
 * <p>方块、物品、掉落物与标签仍然注册，以免资源与数据包因方块缺失而失效；
 * 但它不具备任何功能，也无法放置：尝试放置时会提示安装机械动力。</p>
 */
public class DisabledStressGeneratorBlock extends Block {
    /** 与 Create 版本共用同一组方块状态属性，以便共用 blockstate 与模型 */
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final BooleanProperty MOTOR = BooleanProperty.create("motor");

    public DisabledStressGeneratorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
            .setValue(AXIS, Direction.Axis.Y)
            .setValue(MOTOR, false));
    }

    /** 未安装 Create 时的提示信息 */
    public static Component missingCreateMessage() {
        return Component.translatable("message.anvilcraftextrapower.stress_generator.missing_create");
    }

    /** 在快捷栏上方提示玩家安装 Create */
    public static void showMissingCreate(Player player) {
        if (player == null) return;
        player.displayClientMessage(missingCreateMessage(), true);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, MOTOR);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        if (rotation != Rotation.CLOCKWISE_90 && rotation != Rotation.COUNTERCLOCKWISE_90) return state;
        Direction.Axis axis = state.getValue(AXIS);
        if (axis == Direction.Axis.X) return state.setValue(AXIS, Direction.Axis.Z);
        if (axis == Direction.Axis.Z) return state.setValue(AXIS, Direction.Axis.X);
        return state;
    }

    @Override
    protected InteractionResult useWithoutItem(
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        BlockHitResult hitResult
    ) {
        if (!level.isClientSide()) showMissingCreate(player);
        return InteractionResult.SUCCESS;
    }
}
