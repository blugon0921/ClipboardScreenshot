package kr.blugon.clipboardscreenshot.client

import com.mojang.blaze3d.platform.InputConstants
import com.mojang.blaze3d.platform.NativeImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.Screenshot
import net.minecraft.client.gui.components.toasts.SystemToast
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.image.BufferedImage

var wasKeyPressed = false
val screenshotCopyKey: KeyMapping = KeyMappingHelper.registerKeyMapping(KeyMapping(
        "key.screenshot.copy",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_F2,
    KeyMapping.Category.MISC
    )
)

fun initClient() {
    ClientTickEvents.END_CLIENT_TICK.register { client ->
        val isPressed = InputConstants.isKeyDown(
            client.window,
            KeyMappingHelper.getBoundKeyOf(screenshotCopyKey).value
        )
        if(!isPressed) {
            wasKeyPressed = false
            return@register
        }
        if (wasKeyPressed) return@register
        wasKeyPressed = true
        if(GraphicsEnvironment.isHeadless()) {
            client.sendMessageOrToast(Component.translatable("screenshot.clipboard.error"))
            return@register
        }
        val instance = Minecraft.getInstance()
        val buffer = instance.mainRenderTarget
        Screenshot.takeScreenshot(buffer) { image ->
            CoroutineScope(Dispatchers.Default).launch {
                copyToClipboard(image.toBufferedImage())
                instance.execute {
                    client.sendMessageOrToast(Component.translatable("screenshot.copied"))
                }
            }
        }
    }
}

fun Minecraft.sendMessageOrToast(text: Component) {
    if(player == null) toastManager.addToast(
        SystemToast.multiline(this, SystemToast.SystemToastId.NARRATOR_TOGGLE, text, Component.literal(""))
    ) else player!!.sendSystemMessage(text)
}

fun NativeImage.toBufferedImage(): BufferedImage {
    return BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB).also {
        for (x in 0 until this.width) {
            for (y in 0 until this.height) {
                it.setRGB(x, y, this.getPixel(x, y))
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