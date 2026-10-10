package com.extra.power.advancements.criterion;

import com.extra.power.init.ModCriterionTriggers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/** 铁砧砸击铁砧投影机时触发，并携带实际产生的虚影（幽灵铁砧）格数。 */
public class AnvilProjectionTrigger extends SimpleCriterionTrigger<AnvilProjectionTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, int blocks) {
        this.trigger(player, instance -> instance.matches(blocks));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, MinMaxBounds.Ints blocks)
        implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
            MinMaxBounds.Ints.CODEC.fieldOf("blocks").forGetter(TriggerInstance::blocks)
        ).apply(instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> blocks(MinMaxBounds.Ints blocks) {
            return ModCriterionTriggers.ANVIL_PROJECTION.get()
                .createCriterion(new TriggerInstance(Optional.empty(), blocks));
        }

        public boolean matches(int blocks) {
            return this.blocks.matches(blocks);
        }
    }
}
