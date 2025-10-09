package kr.blugon.clipboardscreenshot.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.toast.SystemToast
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

var wasKeyPressed = false
val screenshotCopyKey: KeyBinding = KeyBindingHelper.registerKeyBinding(KeyBinding(
    "key.screenshot.copy",
    InputUtil.Type.KEYSYM,
    GLFW.GLFW_KEY_F2,
    KeyBinding.Category.MISC
))

fun initClient() {
    ClientTickEvents.END_CLIENT_TICK.register { client ->
        val isPressed = InputUtil.isKeyPressed(
            client.window,
            KeyBindingHelper.getBoundKeyOf(screenshotCopyKey).code
        )
        if(!isPressed) {
            wasKeyPressed = false
            return@register
        }
        if (wasKeyPressed) return@register
        wasKeyPressed = true
        if(GraphicsEnvironment.isHeadless()) {
            client.sendMessageOrToast(Text.translatable("screenshot.clipboard.error"))
            return@register
        }
        val instance = MinecraftClient.getInstance()
        val buffer = instance.framebuffer
        ScreenshotRecorder.takeScreenshot(buffer) { image ->
            CoroutineScope(Dispatchers.Default).launch {
                copyToClipboard(image.toBufferedImage())
                instance.execute {
                    client.sendMessageOrToast(Text.translatable("screenshot.copied"))
                }
            }
        }
    }
}

fun MinecraftClient.sendMessageOrToast(text: Text) {
    if(player == null) toastManager.add(
        SystemToast.create(this, SystemToast.Type.NARRATOR_TOGGLE, text, Text.of(""))
    ) else player!!.sendMessage(text, false)
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