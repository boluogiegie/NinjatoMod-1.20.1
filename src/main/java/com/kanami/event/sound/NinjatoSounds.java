package com.kanami.event.sound;

import com.kanami.Ninjato;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class NinjatoSounds {
    public static final SoundEvent NINJATO_EQUIP = register("nachuwuqi");

    public static final SoundEvent LIGHT_SWING_1 = register("qingji1");
    public static final SoundEvent LIGHT_SWING_2 = register("qingji2");
    public static final SoundEvent LIGHT_SWING_3 = register("qingji3");
    public static final SoundEvent LIGHT_SWING_4 = register("qingji4");
    public static final SoundEvent LIGHT_SWING_5 = register("qingji5");
    public static final SoundEvent LIGHT_SWING_6 = register("qingji6");

    public static final SoundEvent LIGHT_HIT_1 = register("qingjimingzhong1");
    public static final SoundEvent LIGHT_HIT_2 = register("qingjimingzhong2");

    public static final SoundEvent HEAVY_SWING_1 = register("zhongji1");
    public static final SoundEvent HEAVY_SWING_2 = register("zhongji2");
    public static final SoundEvent HEAVY_SWING_3 = register("zhongji3");

    public static final SoundEvent HEAVY_HIT_1 = register("zhongjimingzhong1");
    public static final SoundEvent HEAVY_HIT_2 = register("zhongjimingzhong2");
    public static final SoundEvent HEAVY_HIT_3 = register("zhongjimingzhong3");

    private static SoundEvent register(String name) {
        Identifier id = new Identifier(Ninjato.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void initialize() {}

}