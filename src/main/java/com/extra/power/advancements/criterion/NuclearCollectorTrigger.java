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

/** 集核器工作时触发并携带周围铀棒（供能）数量，判定方式与铁砧工艺《强热》的热收集器触发器类似。 */
public class NuclearCollectorTrigger extends SimpleCriterionTrigger<NuclearCollectorTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, int rods) {
        this.trigger(player, instance -> instance.matches(rods));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, MinMaxBounds.Ints rods)
        implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
            MinMaxBounds.Ints.CODEC.fieldOf("rods").forGetter(TriggerInstance::rods)
        ).apply(instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> rods(MinMaxBounds.Ints rods) {
            return ModCriterionTriggers.NUCLEAR_COLLECTOR.get()
                .createCriterion(new TriggerInstance(Optional.empty(), rods));
        }

        public boolean matches(int rods) {
            return this.rods.matches(rods);
        }
    }
}
