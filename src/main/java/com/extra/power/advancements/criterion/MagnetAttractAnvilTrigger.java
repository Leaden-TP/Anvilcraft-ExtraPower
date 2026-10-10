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

/** 电磁铁成功吸引铁砧时触发，并携带铁砧与电磁铁的距离（格）。 */
public class MagnetAttractAnvilTrigger extends SimpleCriterionTrigger<MagnetAttractAnvilTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, int distance) {
        this.trigger(player, instance -> instance.matches(distance));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, MinMaxBounds.Ints distance)
        implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
            MinMaxBounds.Ints.CODEC.fieldOf("distance").forGetter(TriggerInstance::distance)
        ).apply(instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> distance(MinMaxBounds.Ints distance) {
            return ModCriterionTriggers.MAGNET_ATTRACT_ANVIL.get()
                .createCriterion(new TriggerInstance(Optional.empty(), distance));
        }

        public boolean matches(int distance) {
            return this.distance.matches(distance);
        }
    }
}
