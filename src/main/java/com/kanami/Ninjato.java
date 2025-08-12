package com.kanami;

import com.kanami.event.NinjatoAttackEvents;
import com.kanami.event.NinjatoEvents;
import com.kanami.event.NinjatoHeavyAttackSoundEvents;
import com.kanami.event.NinjatoSoundEvents;
import com.kanami.event.sound.NinjatoSounds;
import com.kanami.item.NinjatoItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ninjato implements ModInitializer {
	public static final String MOD_ID = "ninjato";

	// 此记录器用于将文本写入控制台和日志文件。
	// 使用您的 mod id 作为记录器的名称被认为是最佳实践。
	// 这样，就可以清楚地知道哪个模组编写了信息、警告和错误。
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// 一旦 Minecraft 处于模组加载就绪状态，此代码就会运行。
		// 但是，某些东西（如资源）可能仍然未初始化。
		// 谨慎行事。

		NinjatoItems.registerItems();
		NinjatoSounds.initialize();
		NinjatoEvents.registerEvents();
		NinjatoAttackEvents.register();
		NinjatoHeavyAttackSoundEvents.registerEvents();
		NinjatoSoundEvents.registerClientEvents();
		NinjatoSoundEvents.registerServerEvents();
		LOGGER.info("Hello Fabric world!");
	}
}