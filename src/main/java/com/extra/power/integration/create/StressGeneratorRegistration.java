package com.extra.power.integration.create;

import com.extra.power.block.just_block.DisabledStressGeneratorBlock;
import com.extra.power.integration.create.client.StressGeneratorRenderer;
import com.extra.power.item.DisabledStressGeneratorBlockItem;
import dev.anvilcraft.lib.v2.registrum.util.entry.BlockEntityEntry;
import dev.anvilcraft.lib.v2.registrum.util.entry.BlockEntry;
import dev.dubhe.anvilcraft.data.AnvilCraftDatagen;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import dev.dubhe.anvilcraft.init.item.ModItemTags;
import dev.dubhe.anvilcraft.util.DataGenUtil;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Objects;

import static com.extra.power.init.AnvilCraftExtrapower.REGISTRATE;

/**
 * 应力发电机方块与方块实体的注册。
 *
 * <p>方块与物品**始终**注册：未安装 Create 时注册为“不可用”的版本
 * （{@link DisabledStressGeneratorBlock}，无法放置并提示安装机械动力），
 * 以免掉落物、方块标签与模型资源因方块缺失而失效。
 * 方块实体、渲染器与铁砧行为只在安装 Create 时注册。</p>
 */
public final class StressGeneratorRegistration {

    public static final BlockEntry<Block> STRESS_GENERATOR = REGISTRATE
        .block("stress_generator", StressGeneratorRegistration::createBlock)
        .lang("Stress Generator")
        .initialProperties(() -> Blocks.IRON_BLOCK)
        .properties(p -> p.strength(5.0f, 1200f).noOcclusion())
        .blockstate(DataGenUtil::noExtraModelOrState)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .recipe((ctx, provider) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ctx.get(),1)
                        .pattern(" I ")
                        .pattern("BMB")
                        .pattern(" I ")
                        .define('M', ModBlocks.MAGNETO_ELECTRIC_CORE_BLOCK)
                        .define('B', ModItemTags.BRASS_PLATES)
                        .define('I',  Blocks.IRON_BARS)
                        .unlockedBy("hasb", AnvilCraftDatagen.has(ModItemTags.BRASS_PLATES))
                        .unlockedBy("hasi", AnvilCraftDatagen.has(Blocks.IRON_BARS))
                        .unlockedBy("hasm", AnvilCraftDatagen.has(ModBlocks.MAGNETO_ELECTRIC_CORE_BLOCK))
                        .save(provider);
            })
        .item((block, properties) -> CreateCompat.isLoaded()
            ? new BlockItem(block, properties)
            : new DisabledStressGeneratorBlockItem(block, properties))
        .build()
        .register();

    private StressGeneratorRegistration() {
    }

    private static Block createBlock(Block.Properties properties) {
        if (CreateCompat.isLoaded()) return CreateParts.createBlock(properties);
        return new DisabledStressGeneratorBlock(properties);
    }

    /** 触碰本类即完成方块与物品的注册（静态字段初始化），方块实体则在安装 Create 时一并注册 */
    public static void register() {
        if (CreateCompat.isLoaded()) Objects.requireNonNull(CreateParts.BLOCK_ENTITY);
    }

    /** 方块的方块实体类型（仅安装 Create 时可用） */
    public static BlockEntityType<? extends StressGeneratorBlockEntity> blockEntityType() {
        return CreateParts.BLOCK_ENTITY.get();
    }

    /** 仅在安装 Create 时才会被加载的部分 */
    private static final class CreateParts {
        private static final BlockEntityEntry<StressGeneratorBlockEntity> BLOCK_ENTITY = REGISTRATE
            .blockEntity("stress_generator", StressGeneratorBlockEntity::createBlockEntity)
            .validBlock(STRESS_GENERATOR)
            .renderer(() -> StressGeneratorRenderer::new)
            .register();

        private static Block createBlock(Block.Properties properties) {
            return new StressGeneratorBlock(properties);
        }
    }
}
