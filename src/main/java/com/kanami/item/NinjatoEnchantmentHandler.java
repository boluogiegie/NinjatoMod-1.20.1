package com.kanami.item;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class NinjatoEnchantmentHandler {

    public static float calculateFinalDamage(PlayerEntity player, ItemStack weapon, LivingEntity target, float baseDamage) {
        float damage = baseDamage;

        // 锋利
        int sharpnessLevel = EnchantmentHelper.getLevel(Enchantments.SHARPNESS, weapon);
        if (sharpnessLevel > 0) {
            damage += sharpnessLevel * 0.5F + 0.5F;
        }

        // 亡灵杀手
        int smiteLevel = EnchantmentHelper.getLevel(Enchantments.SMITE, weapon);
        if (smiteLevel > 0 && target.getGroup() == EntityGroup.UNDEAD) {
            damage += smiteLevel * 2.5F;
        }

        // 节肢杀手
        int baneLevel = EnchantmentHelper.getLevel(Enchantments.BANE_OF_ARTHROPODS, weapon);
        if (baneLevel > 0 && target.getGroup() == EntityGroup.ARTHROPOD) {
            damage += baneLevel * 2.5F;
            int slowDuration = 20 + player.getRandom().nextInt(10 * baneLevel);
            target.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOWNESS, slowDuration, 3
            ));
        }

        return damage;
    }

    public static void applyOnHitEnchantments(PlayerEntity player, ItemStack weapon, LivingEntity target, boolean wasAlive) {
        // 击退
        int knockbackLevel = EnchantmentHelper.getLevel(Enchantments.KNOCKBACK, weapon);
        if (knockbackLevel > 0) {
            target.takeKnockback(knockbackLevel * 0.5F,
                    Math.sin(player.getYaw() * 0.017453292F),
                    -Math.cos(player.getYaw() * 0.017453292F));
        }

        // 抢夺
        if (!target.isAlive() && wasAlive) {
            int lootingLevel = EnchantmentHelper.getLevel(Enchantments.LOOTING, weapon);
            if (lootingLevel > 0) {
                target.setAttacker(player);
            }
        }

        // 确保在所有附魔效果应用后调用此方法
        EnchantmentHelper.onTargetDamaged(player, target);
    }
}