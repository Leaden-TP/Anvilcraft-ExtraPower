package com.extra.power.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.anvilcraft.lib.recipe.component.BlockStatePredicate;
import com.extra.power.init.ModCriterionTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class NuclearCollectorTrigger extends SimpleCriterionTrigger<NuclearCollectorTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, BlockState state, @Nullable BlockEntity entity, int power, int heat) {
        super.trigger(player, t -> t.matches(player.level(), state, entity, power, heat));
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<BlockStatePredicate> collecting,
        Optional<MinMaxBounds.Ints> power,
        Optional<MinMaxBounds.Ints> heat
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
            BlockStatePredicate.CODEC.optionalFieldOf("collecting").forGetter(TriggerInstance::collecting),
            MinMaxBounds.Ints.CODEC.optionalFieldOf("power").forGetter(TriggerInstance::power),
            MinMaxBounds.Ints.CODEC.optionalFieldOf("heat").forGetter(TriggerInstance::heat)
        ).apply(instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> collectOn(BlockStatePredicate.Builder builder) {
            return ModCriterionTriggers.NUCLEAR_COLLECTOR_COLLECT.get().createCriterion(new TriggerInstance(
                Optional.empty(),
                Optional.of(builder.build()),
                Optional.empty(),
                Optional.empty()
            ));
        }

        public static Criterion<TriggerInstance> collectOn(BlockStatePredicate statePredicate) {
            return ModCriterionTriggers.NUCLEAR_COLLECTOR_COLLECT.get().createCriterion(new TriggerInstance(
                Optional.empty(),
                Optional.of(statePredicate),
                Optional.empty(),
                Optional.empty()
            ));
        }

        public static Criterion<TriggerInstance> powerOutput(MinMaxBounds.Ints power) {
            return ModCriterionTriggers.NUCLEAR_COLLECTOR_COLLECT.get().createCriterion(new TriggerInstance(
                Optional.empty(),
                Optional.empty(),
                Optional.of(power),
                Optional.empty()
            ));
        }

        public static Criterion<TriggerInstance> heatLevel(MinMaxBounds.Ints heat) {
            return ModCriterionTriggers.NUCLEAR_COLLECTOR_COLLECT.get().createCriterion(new TriggerInstance(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(heat)
            ));
        }

        public boolean matches(Level level, BlockState state, @Nullable BlockEntity entity, int power, int heat) {
            if (this.collecting.isPresent() && !this.collecting.get().test(level, state, entity)) {
                return false;
            }
            if (this.power.isPresent() && !this.power.get().matches(power)) {
                return false;
            }
            return this.heat.isEmpty() || this.heat.get().matches(heat);
        }
    }
}
