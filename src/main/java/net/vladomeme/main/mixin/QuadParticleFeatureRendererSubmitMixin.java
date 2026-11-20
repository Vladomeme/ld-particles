package net.vladomeme.main.mixin;

import net.minecraft.client.renderer.feature.QuadParticleFeatureRenderer;
import net.vladomeme.main.config.LDPConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(QuadParticleFeatureRenderer.Submit.class)
public class QuadParticleFeatureRendererSubmitMixin {

    @ModifyVariable(method = "<init>", at = @At(value = "HEAD"), argsOnly = true)
    private static boolean ldp$render$translucent(boolean translucent) {
        if (LDPConfig.INSTANCE.enabled) return true;
        return translucent;
    }
}
