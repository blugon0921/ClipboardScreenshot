package kr.blugon.clipboardscreenshot.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.ScreenshotRecorder
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.image.BufferedImage
import kotlin.concurrent.thread

var wasKeyPressed = false
class ClipboardScreenshotClient : ClientModInitializer {
    val screenshotCopyKey: KeyBinding = KeyBindingHelper.registerKeyBinding(KeyBinding(
        "key.screenshot.copy",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_F2,
        "key.categories.misc"
    ))


    override fun onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            val window = client.window
            val isPressed = InputUtil.isKeyPressed(
                window.handle,
                KeyBindingHelper.getBoundKeyOf(screenshotCopyKey).code)
            if(!isPressed) {
                wasKeyPressed = false
                return@register
            }
            if (wasKeyPressed) return@register
            wasKeyPressed = true
            if (!GraphicsEnvironment.isHeadless()) {
                val buffer = MinecraftClient.getInstance().framebuffer
                ScreenshotRecorder.takeScreenshot(buffer) { image ->
                    thread {
                        copyToClipboard(image.toBufferedImage())
                        client.player?.sendMessage(Text.translatable("screenshot.copied"), false)
                    }
                }
            } else client.player?.sendMessage(Text.translatable("screenshot.clipboard.error"), false)
        }
    }

    fun NativeImage.toBufferedImage(): BufferedImage {
        return BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB).also {
            for (x in 0 until this.width) {
                for (y in 0 until this.height) {
                    it.setRGB(x, y, this.getColorArgb(x, y))
                }
            }
        }
    }

    fun copyToClipboard(image: BufferedImage) {
        val transferableImage = object: Transferable {
            override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(DataFlavor.imageFlavor)
            override fun isDataFlavorSupported(flavor: DataFlavor): Boolean = flavor == DataFlavor.imageFlavor
            override fun getTransferData(flavor: DataFlavor): Any {
                if (!isDataFlavorSupported(flavor)) throw UnsupportedFlavorException(flavor)
                return image
            }
        }
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(transferableImage, null)
    }
}
