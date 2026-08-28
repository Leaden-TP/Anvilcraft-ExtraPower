package com.extra.power.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * redstone link 方块物品。
 * <p>右键接收模式的 redstone link 时会在物品上记录目标位置（模仿铁砧工艺定日镜的磁盘记录），
 * 携带该记录放置的方块会自动进入蓝色模式并绑定该位置。
 */
public class RedstoneLinkItem extends BlockItem {
    private static final String KEY_DIM = "BoundDim";
    private static final String KEY_X = "BoundX";
    private static final String KEY_Y = "BoundY";
    private static final String KEY_Z = "BoundZ";

    public RedstoneLinkItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static boolean hasBoundData(ItemStack stack) {
        return getTag(stack).contains(KEY_X);
    }

    public static void writeBoundData(ItemStack stack, ResourceKey<Level> dimension, BlockPos pos) {
        CompoundTag tag = getTag(stack);
        tag.putString(KEY_DIM, dimension.location().toString());
        tag.putInt(KEY_X, pos.getX());
        tag.putInt(KEY_Y, pos.getY());
        tag.putInt(KEY_Z, pos.getZ());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Nullable
    public static BlockPos getBoundPos(ItemStack stack) {
        CompoundTag tag = getTag(stack);
        if (!tag.contains(KEY_X)) return null;
        return new BlockPos(tag.getInt(KEY_X), tag.getInt(KEY_Y), tag.getInt(KEY_Z));
    }

    @Nullable
    public static ResourceKey<Level> getBoundDimension(ItemStack stack) {
        CompoundTag tag = getTag(stack);
        if (!tag.contains(KEY_DIM)) return null;
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString(KEY_DIM)));
    }

    private static CompoundTag getTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasBoundData(stack);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag isAdvanced
    ) {
        super.appendHoverText(stack, context, tooltipComponents, isAdvanced);
        BlockPos pos = getBoundPos(stack);
        if (pos != null) {
            tooltipComponents.add(Component.translatable(
                    "item.anvilcraftextrapower.redstone_link.bound", pos.toShortString())
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
