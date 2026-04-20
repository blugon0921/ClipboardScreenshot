package kr.blugon.clipboardscreenshot.client.config

import dev.isxander.yacl3.api.NameableEnum
import net.minecraft.network.chat.Component
import net.minecraft.util.StringRepresentable

enum class NotificationType: NameableEnum, StringRepresentable {
    Chat,
    Actionbar,
    Toast,
    None;

    override fun getSerializedName(): String = name.lowercase()

    override fun getDisplayName(): Component {
        return Component.translatable("clipboard_screenshot.config.notification." + name.lowercase())
    }
}