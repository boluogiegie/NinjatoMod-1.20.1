package com.kanami.event;

import com.kanami.event.sound.NinjatoSounds;
import com.kanami.item.NinjatoItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class NinjatoHeavyAttackSoundEvents {

    private static final Random RANDOM = Random.create();

    public static void registerEvents() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (source.getAttacker() instanceof PlayerEntity player) {
                ItemStack heldItem = player.getMainHandStack();

                if (heldItem.getItem() == NinjatoItems.ninjato) {
                    if (source.getName().equals("player.heavyAttack")) {
                        playRandomHeavyHitSound(player, player.getWorld());
                    }
                }
            }
            return true;
        });
    }

    private static void playRandomHeavyHitSound(PlayerEntity player, World world) {
        if (!world.isClient) {
            world.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    getRandomSound(NinjatoSounds.HEAVY_HIT_1, NinjatoSounds.HEAVY_HIT_2, NinjatoSounds.HEAVY_HIT_3),
                    SoundCategory.PLAYERS,
                    1.2F,
                    0.8F
            );
        }
    }

    private static SoundEvent getRandomSound(SoundEvent... options) {
        return options[RANDOM.nextInt(options.length)];
    }
}