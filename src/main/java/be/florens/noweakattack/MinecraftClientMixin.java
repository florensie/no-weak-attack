package be.florens.noweakattack;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

	@Shadow public ClientPlayerEntity player;
	@Shadow public HitResult crosshairTarget;
	@Shadow public ClientPlayerInteractionManager interactionManager;

	@Inject(method = "doAttack", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;attackEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;)V"))
	private void cancelAttack(CallbackInfo info) {
		if (!this.hasFinishedCooldown()) {
			info.cancel();
		}
	}

	// Makes it so you can hold down the attack button to keep attack an entity
	// Similar to how you hold down your attack button to keep mining a block
	@Inject(method = "handleBlockBreaking", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/hit/HitResult;getType()Lnet/minecraft/util/hit/HitResult$Type;"))
	private void handleEntityAttacking(boolean bl, CallbackInfo info) {
		if (bl && this.crosshairTarget != null && this.crosshairTarget.getType() == HitResult.Type.ENTITY && this.hasFinishedCooldown()) {
			this.interactionManager.attackEntity(this.player, ((EntityHitResult)this.crosshairTarget).getEntity());
		}
	}

	@Unique
	private boolean hasFinishedCooldown() {
		return this.player.getAttackCooldownProgress(0f) >= 1f;
	}
}
