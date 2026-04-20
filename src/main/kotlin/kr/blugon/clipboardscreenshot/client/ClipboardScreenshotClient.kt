package kr.blugon.clipboardscreenshot.client

import com.mojang.blaze3d.platform.InputConstants
import com.mojang.blaze3d.platform.NativeImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.blugon.clipboardscreenshot.client.config.ClipboardScreenshotConfig
import kr.blugon.clipboardscreenshot.client.config.NotificationType
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.ChatFormatting
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

val config = ClipboardScreenshotConfig()
fun initClient() {
    if(!config.loadFromFile()) config.saveToFile()
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
            client.sendNotification(Component.translatable("clipboard_screenshot.clipboard.error").withStyle(ChatFormatting.RED))
            return@register
        }
        val instance = Minecraft.getInstance()
        val buffer = instance.mainRenderTarget
        Screenshot.takeScreenshot(buffer) { image ->
            CoroutineScope(Dispatchers.Default).launch {
                copyToClipboard(image.toBufferedImage())
                instance.execute {
                    client.sendNotification(Component.translatable("clipboard_screenshot.copied"))
                }
            }
        }
    }
}

fun Minecraft.sendNotification(text: Component) {
    when(config.notificationType.get()) {
        NotificationType.Chat -> (player?: return sendToast(text)).sendSystemMessage(text)
        NotificationType.Actionbar -> (player?: return sendToast(text)).sendOverlayMessage(text)
        NotificationType.Toast -> sendToast(text)
        NotificationType.None -> return
    }
}
fun Minecraft.sendToast(text: Component) {
    toastManager.addToast(SystemToast.multiline(this, SystemToast.SystemToastId.NARRATOR_TOGGLE, text, Component.literal("")))
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