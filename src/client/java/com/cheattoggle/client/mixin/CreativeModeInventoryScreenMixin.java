package com.cheattoggle.client.mixin;

import com.cheattoggle.CheatToggleMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeModeInventoryScreen.class)
public class CreativeModeInventoryScreenMixin {
	@Inject(method = "hasPermissions(Lnet/minecraft/world/entity/player/Player;)Z", at = @At("RETURN"), cancellable = true)
	private void onShouldShowOperatorTab(Player player, CallbackInfoReturnable<Boolean> cir) {
		Boolean pending = CheatToggleMod.pendingOperatorItemsValue;
		if (pending != null) {
			CheatToggleMod.pendingOperatorItemsValue = null;
			Minecraft.getInstance().options.operatorItemsTab().set(pending);
			Minecraft.getInstance().options.save();
		}
		cir.setReturnValue(Minecraft.getInstance().options.operatorItemsTab().get());
	}
}
