package kr.blugon.clipboardscreenshot.client.config

import dev.isxander.yacl3.config.v3.JsonFileCodecConfig
import dev.isxander.yacl3.config.v3.register
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.util.StringRepresentable


class ClipboardScreenshotConfig: JsonFileCodecConfig<ClipboardScreenshotConfig>(
    FabricLoader.getInstance().configDir.resolve("clipboard_screenshot.json")
) {
    val notificationType by register(default = NotificationType.Chat, StringRepresentable.fromEnum(NotificationType::values))
}