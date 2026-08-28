package com.extra.power.util;

import net.minecraft.world.level.Level;

import java.util.List;


public class AnimationFunction {


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
