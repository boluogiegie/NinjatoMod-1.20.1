package com.kanami.item;

import com.kanami.Ninjato;
import com.kanami.event.sound.NinjatoSounds;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NinjatoItems {
    //注册忍锋
    public static final Item ninjato = register("ninjato", new NinjatoItem(ToolMaterials.NETHERITE,
            NinjatoTiers.ninjato,
            25,//基础伤害30
            -2.0F,//攻击间隔为0.5s
            (new Item.Settings().fireproof().maxCount(1))));//防火

    public static Item register(String id, Item item) {
        return Registry.register(Registries.ITEM, RegistryKey.of(Registries.ITEM.getKey(), new Identifier(Ninjato.MOD_ID, id)), item);
    }

    //添加忍锋至创造的战斗类物品栏，排在下界合金剑后面
    public static void addItemsToItemGroup(FabricItemGroupEntries entries) {
        int netheriteIndex = -1;
        List<ItemStack> sortedEntries = new ArrayList<>(entries.getDisplayStacks());
        for (int i = 0; i < sortedEntries.size(); i++) {
            if (sortedEntries.get(i).getItem() == Items.NETHERITE_SWORD) {
                netheriteIndex = i;
                break;
            }
        }
        if(netheriteIndex != -1) {
            entries.addAfter(Items.NETHERITE_SWORD, ninjato);
        } else {
            entries.add(ninjato);
        }
    }

    //注册物品栏事件
    public static void registerItems() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(NinjatoItems::addItemsToItemGroup);
    }

    public static class NinjatoItem extends SwordItem {
        private static final UUID ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
        private static final Random RANDOM = Random.create();
        public static final float LIGHT_ATTACK_DAMAGE = 30.0F;
        public static final float HEAVY_ATTACK_DAMAGE = 60.0F;

        public NinjatoItem(ToolMaterial material, NinjatoTiers ninjato, int attackDamage, float attackSpeed, Settings settings) {
            super(material, attackDamage, attackSpeed, settings);
        }

        @Override
        public boolean isDamageable() {
            return false;
        }

        @Override
        public boolean isItemBarVisible(ItemStack stack) {
            return false;
        }

        @Override
        public int getItemBarStep(ItemStack stack) {
            return 0;
        }

        @Override
        public int getItemBarColor(ItemStack stack) {
            return 0x00FFFFFF;
        }

        @Override
        public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
            super.appendTooltip(stack, world, tooltip, context);
            stack.getOrCreateNbt().putBoolean("Unbreakable", true);
            tooltip.add(Text.translatable("右键施放重击，冷却1秒").formatted(Formatting.BLUE));
        }

        @Override
        public boolean isEnchantable(ItemStack stack) {
            return true;
        }

        @Override
        public int getEnchantability() {
            return NinjatoTiers.ninjato.getEnchantability();
        }

        @Override
        public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            if (!(attacker instanceof PlayerEntity)) {
                return false;
            }
            boolean result = super.postHit(stack, target, attacker);
            PlayerEntity player = (PlayerEntity) attacker;
            boolean isHeavy = isHeavyAttack(player);
            if (isHeavy) {
                playRandomHeavyHitSound(attacker);
            } else {
                playRandomLightHitSound(attacker);
            }
            return true;
        }

        private boolean isHeavyAttack(PlayerEntity player) {
            ItemStack stack = player.getMainHandStack();
            if (stack.hasNbt() && stack.getNbt().contains("HeavyAttack")) {
                stack.getNbt().remove("HeavyAttack");
                return true;
            }
            return false;
        }

        @Override
        public void onCraft(ItemStack stack, World world, PlayerEntity player) {
            super.onCraft(stack, world, player);
            stack.getOrCreateNbt().putBoolean("Unbreakable", true);
            if (!world.isClient) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        NinjatoSounds.NINJATO_EQUIP, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
        }

        // ===== 音效系统 =====
        public static void playRandomLightSwingSound(LivingEntity entity) {
            if (entity.getWorld().isClient) return;
            SoundEvent[] sounds = {
                    NinjatoSounds.LIGHT_SWING_1,
                    NinjatoSounds.LIGHT_SWING_2,
                    NinjatoSounds.LIGHT_SWING_3,
                    NinjatoSounds.LIGHT_SWING_4,
                    NinjatoSounds.LIGHT_SWING_5,
                    NinjatoSounds.LIGHT_SWING_6
            };
            playSound(entity, sounds[RANDOM.nextInt(sounds.length)]);
        }

        public static void playRandomLightHitSound(LivingEntity entity) {
            if (entity.getWorld().isClient) return;
            SoundEvent[] sounds = {
                    NinjatoSounds.LIGHT_HIT_1,
                    NinjatoSounds.LIGHT_HIT_2
            };
            playSound(entity, sounds[RANDOM.nextInt(sounds.length)]);
        }

        public static void playRandomHeavySwingSound(LivingEntity entity) {
            if (entity.getWorld().isClient) return;
            SoundEvent[] sounds = {
                    NinjatoSounds.HEAVY_SWING_1,
                    NinjatoSounds.HEAVY_SWING_2,
                    NinjatoSounds.HEAVY_SWING_3
            };
            playSound(entity, sounds[RANDOM.nextInt(sounds.length)]);
        }

        public static void playRandomHeavyHitSound(LivingEntity entity) {
            if (entity.getWorld().isClient) return;
            SoundEvent[] sounds = {
                    NinjatoSounds.HEAVY_HIT_1,
                    NinjatoSounds.HEAVY_HIT_2,
                    NinjatoSounds.HEAVY_HIT_3
            };
            playSound(entity, sounds[RANDOM.nextInt(sounds.length)]);
        }

        private static void playSound(LivingEntity entity, SoundEvent sound) {
            if (entity.getWorld().isClient()) return;
            entity.getWorld().playSound(
                    null,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    sound,
                    SoundCategory.PLAYERS,
                    1.0F,
                    1.0F + (RANDOM.nextFloat() - 0.5F) * 0.2F
            );
        }
    }
}