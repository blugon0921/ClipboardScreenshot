package kr.blugon.clipboardscreenshot.mixin;

import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class HeadlessFalse {

    @Inject(method = "main", at = @At("HEAD"))
    private static void headlessFalse(CallbackInfo info) {
        System.setProperty("java.awt.headless", "false");
    }
}
