package com.kanami;

import com.kanami.item.NinjatoItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;

public class NinjatoClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BuiltinItemRendererRegistry.INSTANCE.register(
                NinjatoItems.ninjato,
                (stack, mode, matrices, vertexConsumers, light, overlay) -> {
                    ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer();
                    renderer.renderItem(
                            stack,
                            ModelTransformationMode.THIRD_PERSON_RIGHT_HAND,
                            light,
                            overlay,
                            matrices,
                            vertexConsumers,
                            null,
                            0
                    );
                }
        );
    }
}