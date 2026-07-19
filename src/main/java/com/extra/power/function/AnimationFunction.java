package com.extra.power.function;

import com.extra.power.network.UpdateAnimationStatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;


public class AnimationFunction {
    private void syncAnimationState(BlockPos pos, List action_state , Level level ) {
        if (level == null || level.isClientSide()) return;
        PacketDistributor.sendToPlayersTrackingChunk(
                (ServerLevel) level,
                level.getChunk(pos).getPos(),
                new UpdateAnimationStatePacket(new ArrayList<>(action_state), pos)
        );
    }

    public void updateActionState(List<Double> newState , List action_state, Level level) {
        if (level != null && level.isClientSide()) {
            for (int i = 0; i < Math.min(action_state.size(), newState.size()); i++) {
                action_state.set(i, newState.get(i));
            }
        }
    }
    public static List trackTarget(List action_state, List target_state) {
        for (int i = 0; i < action_state.size(); i++) {
            double current = (double) action_state.get(i);
            double target = (double) target_state.get(i);
            double distance = Math.abs(current - target);

            if (distance <= 0.03) {
                action_state.set(i, target);
                continue;
            }

            double step = Math.clamp(distance / 10, 0.01, distance);
            if (current < target) {
                action_state.set(i, current + step);
            }
            else {
                action_state.set(i, current - step);
            }
        }
        return action_state;
    }
}
