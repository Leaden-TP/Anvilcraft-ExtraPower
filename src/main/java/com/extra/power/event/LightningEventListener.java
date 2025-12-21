package com.extra.power.event;

import com.extra.power.block.ModBlock;
import com.extra.power.init.AnvilCraftExtrapower;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.api.event.LightningBoltStrikeEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import static dev.dubhe.anvilcraft.block.MagnetBlock.LIT;

@EventBusSubscriber(modid = AnvilCraftExtrapower.MODID)
public class LightningEventListener {
    @SubscribeEvent
    public static void onLightingStrike(LightningBoltStrikeEvent event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.LIGHTNING_ROD)) pos = pos.above();
        int depth = AnvilCraft.CONFIG.lightningStrikeDepth+1;
        int radius = AnvilCraft.CONFIG.lightningStrikeRadius+1;
        for (BlockPos blockPos : BlockPos.betweenClosed(pos.offset(radius, 0, radius), pos.offset(-radius, -depth, -radius))) {
            BlockState blockState = level.getBlockState(blockPos);
            if (blockState.is(ModBlock.POTATO_BATTERY)) {
                BlockState blockState1 = ModBlock.FLASHING_POTATO_BATTERY.get().defaultBlockState();
                level.setBlockAndUpdate(blockPos, blockState1);
            }
        }
    }
}
