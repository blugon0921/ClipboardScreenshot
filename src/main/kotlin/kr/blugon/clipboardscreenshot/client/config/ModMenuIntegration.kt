package kr.blugon.clipboardscreenshot.client.config

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.YetAnotherConfigLib
import dev.isxander.yacl3.api.controller.EnumControllerBuilder
import dev.isxander.yacl3.dsl.descriptionBuilder
import kr.blugon.clipboardscreenshot.client.config
import net.minecraft.network.chat.Component.translatable

class ModMenuIntegration: ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { screen ->
            YetAnotherConfigLib.createBuilder().apply {
                title(translatable("clipboard_screenshot.config.title"))
                category(ConfigCategory.createBuilder().apply {
                    name(translatable("clipboard_screenshot.config.title"))
                    option(Option.createBuilder<NotificationType>().apply {
                        name(translatable("clipboard_screenshot.config.notification"))
                        descriptionBuilder {
                            text(translatable("clipboard_screenshot.config.notification.description"))
                        }
                        binding(NotificationType.Chat, { config.notificationType.get() }) {
                            config.notificationType.set(it)
                            config.saveToFile()
                        }
                        controller {
                            EnumControllerBuilder
                                .create(it)
                                .enumClass(NotificationType::class.java)
                        }
                    }.build())
                }.build())
            }.build().generateScreen(screen)
        }
    }
}