package com.extra.power.event;

import com.extra.power.init.block.ModBlock;
import com.extra.power.block.just_block.LlamaAnvilBlock;
import com.extra.power.init.AnvilCraftExtrapower;
import dev.dubhe.anvilcraft.api.event.AnvilEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = AnvilCraftExtrapower.MODID)
public class AnvilEventListener {
    @SubscribeEvent
    public static void onLand(AnvilEvent.OnLand event) {

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        final BlockState blockState = level.getBlockState(pos);

        if (blockState.is(ModBlock.LLAMA_ANVIL)) {
            if (event.getFallDistance() > 1) {
                if (level.getRandom().nextDouble() < 0.01) {
                    LlamaAnvilBlock.damage(level, pos);
                }
            }
        }
    }
}
