package com.extra.power.client;

import com.extra.power.client.event.GuiLayerRegistrationEventListener;
import com.extra.power.init.AnvilCraftExtrapower;
import com.extra.power.item.AutomaticCrossbowItem;
import dev.dubhe.anvilcraft.client.AnvilCraftClient;
import dev.dubhe.anvilcraft.client.init.ModModelLayers;
import dev.dubhe.anvilcraft.init.block.ModFluids;
import dev.dubhe.anvilcraft.init.item.ModItems;
import dev.dubhe.anvilcraft.item.weapon.AnvilRailgunItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.Nullable;

@Mod(value = AnvilCraftExtrapower.MODID, dist = Dist.CLIENT)
public class ModClient {
    public ModClient(IEventBus modBus, ModContainer container) {
        modBus.addListener(GuiLayerRegistrationEventListener::onRegister);
        modBus.addListener(ModClient::registerClientExtensions);
    }
    public static void registerClientExtensions(RegisterClientExtensionsEvent e) {
        e.registerItem(
                new EnergyWeaponExtensionImpl(),
                com.extra.power.init.ModItems.AUTOMATIC_CROSSBOW
        );
    }
    public static class EnergyWeaponExtensionImpl implements IClientItemExtensions {
        @Nullable
        @Override
        public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
            if (!entity.isUsingItem() || entity.getUseItem().getItem() != stack.getItem()) {
                return IClientItemExtensions.super.getArmPose(entity, hand, stack);
            }
            if (stack.getItem() instanceof AutomaticCrossbowItem && entity instanceof Player player
                    && AutomaticCrossbowItem.isLoading(player, stack, hand)) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
            return HumanoidModel.ArmPose.BOW_AND_ARROW;
        }
    }
}
