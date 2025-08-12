package com.kanami.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class NinjatoAnimationHandler {
    public static void playSlashAnimation(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.networkHandler.sendPacket(
                    new EntityAnimationS2CPacket(player, 0)
            );

            // 可选：添加粒子效果
            spawnSlashParticles(serverPlayer);
        }
    }

    private static void spawnSlashParticles(ServerPlayerEntity player) {
        Vec3d look = player.getRotationVector();
        Vec3d pos = player.getPos().add(0, 1.5, 0).add(look.multiply(2.0));

        // 这里可添加自定义粒子效果逻辑
    }
}