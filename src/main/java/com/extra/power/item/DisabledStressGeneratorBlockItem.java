package com.extra.power.item;

import com.extra.power.block.just_block.DisabledStressGeneratorBlock;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

/** 未安装 Create 时的应力发电机物品：拒绝放置并提示安装机械动力 */
public class DisabledStressGeneratorBlockItem extends BlockItem {

    public DisabledStressGeneratorBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide()) {
            DisabledStressGeneratorBlock.showMissingCreate(context.getPlayer());
        }
        return InteractionResult.FAIL;
    }
}
