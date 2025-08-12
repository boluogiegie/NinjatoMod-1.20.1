package com.kanami.event;

import com.kanami.Ninjato;
import com.kanami.event.sound.NinjatoSounds;
import com.kanami.item.NinjatoItems;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NinjatoSoundEvents {
    private static final Map<UUID, Long> SOUND_COOLDOWNS = new HashMap<>();
    private static final long SOUND_COOLDOWN_MS = 100;
    private static final Random RANDOM = Random.create();

    private static final Map<UUID, Long> RIGHT_CLICK_TIMES = new HashMap<>();
    private static final long RIGHT_CLICK_DURATION_MS = 500;

    private static boolean lastAttackState = false;
    private static int miningCount = 0;
    private static final int MINING_THRESHOLD = 3;

    public static void registerClientEvents() {
        // 客户端Tick检测攻击状态
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) return;

            boolean isAttacking = client.options.attackKey.isPressed();
            ItemStack stack = player.getMainHandStack();

            // 轻击挥动检测
            if (isAttacking && !lastAttackState && stack.getItem() == NinjatoItems.ninjato) {
                boolean isHeavy = isHeavyAttack(player);
                if (isHeavyAttack(player)) {
                    NinjatoItems.NinjatoItem.playRandomHeavySwingSound(player); // 重击挥动音效
                } else {
                    NinjatoItems.NinjatoItem.playRandomLightSwingSound(player); // 轻击挥动音效
                }
            }
            lastAttackState = isAttacking;

            // 挖掘状态检测
            handleMiningState(player, stack);
        });
    }

    public static void registerServerEvents() {
        // 使用物品回调（检测右键）
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (hand == Hand.MAIN_HAND && isNinjato(player.getStackInHand(hand))) {
                RIGHT_CLICK_TIMES.put(player.getUuid(), System.currentTimeMillis());
                stack.getOrCreateNbt().putBoolean("HeavyAttack", true);
            }
            return TypedActionResult.pass(stack);
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand == Hand.MAIN_HAND && player.getStackInHand(hand).getItem() == NinjatoItems.ninjato) {
                boolean isHeavy = isHeavyAttack(player);
                if (isHeavy) {
                    entity.damage(player.getDamageSources().playerAttack(player),
                            NinjatoItems.NinjatoItem.HEAVY_ATTACK_DAMAGE);
                    NinjatoItems.NinjatoItem.playRandomHeavyHitSound(player);
                } else {
                    NinjatoItems.NinjatoItem.playRandomLightHitSound(player);
                }
            }
            return ActionResult.PASS;
        });
    }

    private static boolean isHeavyAttack(PlayerEntity player) {
        Long clickTime = RIGHT_CLICK_TIMES.get(player.getUuid());
        if (clickTime == null) return false;
        return System.currentTimeMillis() - clickTime < RIGHT_CLICK_DURATION_MS;
    }

    private static ActionResult handleAttack(PlayerEntity player, World world, Hand hand,
                                             Entity target, EntityHitResult hitResult) {
        if (hand == Hand.MAIN_HAND && isNinjato(player.getStackInHand(hand))) {
            UUID uuid = player.getUuid();
            long now = System.currentTimeMillis();

            // 判断是否为重击
            boolean isHeavy = RIGHT_CLICK_TIMES.getOrDefault(uuid, 0L) + RIGHT_CLICK_DURATION_MS > now;

            if (!world.isClient) {
                world.playSound(
                        null, player.getX(), player.getY(), player.getZ(),
                        isHeavy ?
                                getRandomSound(NinjatoSounds.HEAVY_HIT_1, NinjatoSounds.HEAVY_HIT_2, NinjatoSounds.HEAVY_HIT_3) :
                                getRandomSound(NinjatoSounds.LIGHT_HIT_1, NinjatoSounds.LIGHT_HIT_2),//重击音效
                        SoundCategory.PLAYERS, 1.0f, isHeavy ? 0.9f : 1.1f
                );
            }

            // 清除右键状态
            RIGHT_CLICK_TIMES.remove(uuid);
        }
        return ActionResult.PASS;
    }

    private static void handleHeavyAttack(PlayerEntity player, World world) {
        if (!world.isClient) {
            world.playSound(
                    null, player.getX(), player.getY(), player.getZ(),
                    getRandomSound(NinjatoSounds.HEAVY_HIT_1, NinjatoSounds.HEAVY_HIT_2, NinjatoSounds.HEAVY_HIT_3),
                    SoundCategory.PLAYERS, 1.2f, 0.8f
            );
        }
    }

    private static void handleMiningState(PlayerEntity player, ItemStack stack) {
        boolean isMining = player.isUsingItem() && player.getActiveItem() == stack;

        if (isMining && isNinjato(stack)) {
            miningCount++;
            if (miningCount < MINING_THRESHOLD) {
                playRandomSwingSound(player);
            }
        } else {
            miningCount = 0;
        }
    }

    private static void playRandomSwingSound(PlayerEntity player) {
        if (!player.getWorld().isClient) {
            NinjatoItems.NinjatoItem.playRandomLightSwingSound(player);
        }
    }

    private static SoundEvent getRandomSound(SoundEvent... options) {
        return options[RANDOM.nextInt(options.length)];
    }

    private static boolean isNinjato(ItemStack stack) {
        return stack.isOf(NinjatoItems.ninjato);
    }

}