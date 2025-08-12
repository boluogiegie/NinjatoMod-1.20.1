package com.kanami.mixin;

import com.kanami.item.NinjatoItems;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    // 防止任何耐久消耗
    @Inject(method = "damage(ILnet/minecraft/util/math/random/Random;Lnet/minecraft/server/network/ServerPlayerEntity;)Z",
            at = @At("HEAD"), cancellable = true)
    private void onDamage(int amount, Random random, ServerPlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        ItemStack self = (ItemStack)(Object)this;
        if (self.getItem() == NinjatoItems.ninjato) {
            cir.setReturnValue(false);
        }
    }

    // 防止设置耐久值
    @Inject(method = "setDamage", at = @At("HEAD"), cancellable = true)
    private void onSetDamage(int damage, CallbackInfo ci) {
        ItemStack self = (ItemStack)(Object)this;
        if (self.getItem() == NinjatoItems.ninjato) {
            ci.cancel();
        }
    }
}