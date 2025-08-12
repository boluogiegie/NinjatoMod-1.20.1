package com.kanami.event;

import com.kanami.Ninjato;
import com.kanami.event.sound.NinjatoSounds;
import com.kanami.item.NinjatoItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NinjatoEvents {
    private static final Map<UUID, ItemStack> MAINHAND_CACHE = new HashMap<>();
    private static final Map<UUID, ItemStack> OFFHAND_CACHE = new HashMap<>();
    private static final Map<UUID, Long> LAST_SOUND_TIME = new HashMap<>();
    private static final Map<UUID, String> DIMENSION_CACHE = new HashMap<>();
    private static final long COOLDOWN = 300; // 冷却时间（毫秒）

    public static void registerEvents(){

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            UUID uuid = player.getUuid();
            updateCache(player);
            DIMENSION_CACHE.put(uuid, player.getWorld().getRegistryKey().getValue().toString());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID uuid = handler.player.getUuid();
            clearCache(uuid);
        });

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            String oldDim = DIMENSION_CACHE.get(oldPlayer.getUuid());
            String newDim = newPlayer.getWorld().getRegistryKey().getValue().toString();

            if (!newDim.equals(oldDim)) {
                updateCache(newPlayer);
            }
            DIMENSION_CACHE.put(newPlayer.getUuid(), newDim);
        });

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                checkEquipmentChange(player);
            }
        });
    }

    private static void updateCache(PlayerEntity player) {
        UUID uuid = player.getUuid();
        MAINHAND_CACHE.put(uuid, player.getMainHandStack().copy());
        OFFHAND_CACHE.put(uuid, player.getOffHandStack().copy());
    }

    private static void clearCache(UUID uuid) {
        MAINHAND_CACHE.remove(uuid);
        OFFHAND_CACHE.remove(uuid);
        LAST_SOUND_TIME.remove(uuid);
        DIMENSION_CACHE.remove(uuid);
    }

    private static void checkEquipmentChange(PlayerEntity player) {
        try {
            UUID uuid = player.getUuid();

            // 主手检查
            ItemStack oldMain = MAINHAND_CACHE.getOrDefault(uuid, ItemStack.EMPTY);
            ItemStack newMain = player.getMainHandStack();
            if (!ItemStack.areEqual(oldMain, newMain)) {
                handleEquipmentChange(player, oldMain, newMain, EquipmentSlot.MAINHAND);
                MAINHAND_CACHE.put(uuid, newMain.copy());
            }

            // 副手检查
            ItemStack oldOff = OFFHAND_CACHE.getOrDefault(uuid, ItemStack.EMPTY);
            ItemStack newOff = player.getOffHandStack();
            if (!ItemStack.areEqual(oldOff, newOff)) {
                handleEquipmentChange(player, oldOff, newOff, EquipmentSlot.OFFHAND);
                OFFHAND_CACHE.put(uuid, newOff.copy());
            }
        } catch (Exception e) {
            Ninjato.LOGGER.error("Equipment check failed", e);
        }
    }

    private static void handleEquipmentChange(PlayerEntity player, ItemStack oldStack, ItemStack newStack, EquipmentSlot slot) {
        if (isNinjato(newStack) && !isNinjato(oldStack)) {
            playEquipSound(player);
        }
    }

    private static void playEquipSound(PlayerEntity player) {
        if (player.getWorld().isClient()) return;

        long now = System.currentTimeMillis();
        UUID uuid = player.getUuid();

        if (now - LAST_SOUND_TIME.getOrDefault(uuid, 0L) > COOLDOWN) {
            player.getWorld().playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    NinjatoSounds.NINJATO_EQUIP,
                    SoundCategory.PLAYERS,
                    0.8F,
                    1.0F
            );
            LAST_SOUND_TIME.put(uuid, now);
        }
    }

    private static boolean isNinjato(ItemStack stack) {
        return stack.isOf(NinjatoItems.ninjato);
    }
}

