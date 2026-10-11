package com.extra.power.util;

import net.minecraft.world.level.Level;

import java.util.List;


public class AnimationFunction {


    public void updateActionState(List<Double> newState, List<Double> actionState, Level level) {
        if (level != null && level.isClientSide()) {
            for (int i = 0; i < Math.min(actionState.size(), newState.size()); i++) {
                actionState.set(i, newState.get(i));
            }
        }
    }

    public static List<Double> trackTarget(List<Double> actionState, List<Double> targetState) {
        for (int i = 0; i < actionState.size(); i++) {
            double current = actionState.get(i);
            double target = targetState.get(i);
            double distance = Math.abs(current - target);

            if (distance <= 0.03) {
                actionState.set(i, target);
                continue;
            }

            double step = Math.clamp(distance / 10, 0.01, distance);
            if (current < target) {
                actionState.set(i, current + step);
            }
            else {
                actionState.set(i, current - step);
            }
        }
        return actionState;
    }
}
