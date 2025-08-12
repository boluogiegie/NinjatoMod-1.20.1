package com.kanami.event;

import com.kanami.item.NinjatoAnimationHandler;
import com.kanami.item.NinjatoEnchantmentHandler;
import com.kanami.item.NinjatoItems;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NinjatoAttackEvents {
    private static final int HEAVY_ATTACK_COOLDOWN = 20; // 20 ticks = 1秒
    private static final float HEAVY_ATTACK_DAMAGE = 60.0F;
    private static final double ATTACK_RANGE = 4.0;
    private static final long HEAVY_ATTACK_MARK_DURATION = 500; // 毫秒
    // 状态跟踪
    private static final Map<UUID, Long> heavyAttackTargets = new HashMap<>();
    private static final Map<UUID, Long> soundCooldowns = new HashMap<>();
    private static final Map<UUID, Boolean> processingDamage = new HashMap<>();

    public static void register() {
        // 实体攻击事件
        AttackEntityCallback.EVENT.register(NinjatoAttackEvents::onAttackEntity);

        // 右键使用事件
        UseItemCallback.EVENT.register(NinjatoAttackEvents::onRightClickItem);

        // 伤害处理事件
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(NinjatoAttackEvents::onLivingHurt);

        // 客户端Tick事件
        ClientTickEvents.END_CLIENT_TICK.register(NinjatoAttackEvents::onClientTick);
    }

    // ===== 事件处理器 =====
    private static ActionResult onAttackEntity(PlayerEntity player, World world,
                                               Hand hand, Entity target, EntityHitResult hitResult) {
        return ActionResult.PASS;
    }

    private static TypedActionResult<ItemStack> onRightClickItem(PlayerEntity player, World world, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!isNinjato(stack) || player.getItemCooldownManager().isCoolingDown(stack.getItem())) {
            return TypedActionResult.pass(stack);
        }

        // 执行范围攻击
        performAreaAttack(player, stack);
        player.getItemCooldownManager().set(stack.getItem(), HEAVY_ATTACK_COOLDOWN);
        spawnHeavyAttackParticles(player);

        // 客户端动画
        if (world.isClient) {
            NinjatoAnimationHandler.playSlashAnimation(player);
        }

        return TypedActionResult.success(stack);
    }

    private static boolean onLivingHurt(LivingEntity entity, net.minecraft.entity.damage.DamageSource source, float amount) {
        if (source.getAttacker() instanceof PlayerEntity player && isNinjato(player.getMainHandStack())) {
            if (processingDamage.getOrDefault(entity.getUuid(), false)) {
                return true;
            }
            if (isHeavyAttackTarget(entity.getUuid())) {
                return true;
            }
            processingDamage.put(entity.getUuid(), true);
            try {
                return true;
            } finally {
                processingDamage.remove(entity.getUuid());
            }
        }
        return true;
    }

    private static void onClientTick(MinecraftClient client) {
        if (client.player == null) return;

        // 禁用原版横扫音效
        if (client.player.getMainHandStack().isOf(NinjatoItems.ninjato)) {
            client.getSoundManager().stopSounds(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP.getId(), SoundCategory.PLAYERS);
        }
    }

    // ===== 核心逻辑 =====
    private static void performAreaAttack(PlayerEntity player, ItemStack weapon) {
        Vec3d look = player.getRotationVector();
        Vec3d start = player.getEyePos();
        Vec3d end = start.add(look.multiply(ATTACK_RANGE));
        Box attackArea = new Box(start, end).expand(1.0);

        List<LivingEntity> targets = player.getWorld().getEntitiesByClass(
                LivingEntity.class,
                attackArea,
                e -> e != player && e.isAlive()
        );

        if (targets.isEmpty()) {
            NinjatoItems.NinjatoItem.playRandomHeavySwingSound(player);
        } else {
            for (LivingEntity target : targets) {
                markAsHeavyAttackTarget(target.getUuid());
                NinjatoItems.NinjatoItem.playRandomHeavyHitSound(player);
                processingDamage.put(target.getUuid(), true);
                try {
                    float finalDamage = NinjatoEnchantmentHandler.calculateFinalDamage(player, weapon, target, HEAVY_ATTACK_DAMAGE);
                    int fireAspectLevel = EnchantmentHelper.getLevel(Enchantments.FIRE_ASPECT, weapon);
                    if (fireAspectLevel > 0) {
                        target.setOnFireFor(fireAspectLevel * 4);
                    }
                    target.damage(player.getDamageSources().playerAttack(player), finalDamage);
                    NinjatoEnchantmentHandler.applyOnHitEnchantments(player, weapon, target, true);
                    spawnHitParticles(player, target);
                } finally {
                    processingDamage.remove(target.getUuid());
                }
            }
        }
    }

    // ===== 工具方法 =====
    private static void markAsHeavyAttackTarget(UUID entityId) {
        heavyAttackTargets.put(entityId, System.currentTimeMillis() + HEAVY_ATTACK_MARK_DURATION);
    }

    private static boolean isHeavyAttackTarget(UUID entityId) {
        Long expiration = heavyAttackTargets.get(entityId);
        if (expiration == null) return false;

        if (System.currentTimeMillis() > expiration) {
            heavyAttackTargets.remove(entityId);
            return false;
        }
        return true;
    }

    private static void spawnAttackParticles(PlayerEntity attacker, LivingEntity target) {
        if (attacker.getWorld() instanceof ServerWorld world) {
            world.spawnParticles(
                    ParticleTypes.FLASH,
                    target.getX(),
                    target.getY() + 1.0D,
                    target.getZ(),
                    1, 0, 0, 0, 0
            );
        }
    }

    private static void spawnHeavyAttackParticles(PlayerEntity player) {
        if (player.getWorld() instanceof ServerWorld world) {
            Vec3d look = player.getRotationVector();
            Vec3d pos = player.getEyePos().add(look.multiply(2.0));

            world.spawnParticles(
                    ParticleTypes.SWEEP_ATTACK,
                    pos.x, pos.y, pos.z,
                    1, 0, 0, 0, 0
            );
        }
    }

    private static void spawnHitParticles(PlayerEntity player, LivingEntity target) {
        if (player.getWorld() instanceof ServerWorld world) {
            double centerY = target.getY() + target.getHeight() / 2;

            // 暴击粒子
            world.spawnParticles(
                    ParticleTypes.CRIT,
                    target.getX(), centerY, target.getZ(),
                    15, 0.3, 0.3, 0.3, 0.5
            );

            // 附魔命中粒子
            world.spawnParticles(
                    ParticleTypes.ENCHANTED_HIT,
                    target.getX(), centerY, target.getZ(),
                    10, 0.2, 0.2, 0.2, 0.5
            );
        }
    }

    private static boolean isNinjato(ItemStack stack) {
        return stack.isOf(NinjatoItems.ninjato);
    }
}