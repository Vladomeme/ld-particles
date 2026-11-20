package net.vladomeme.main.mixin;

import net.minecraft.client.particle.BillboardParticleSubmittable;
import net.vladomeme.main.config.LDPConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BillboardParticleSubmittable.class)
public class BillboardParticleSubmittableMixin {

    @ModifyVariable(method = "render(Lnet/minecraft/client/particle/BillboardParticleSubmittable$Buffers;Lnet/minecraft/client/render/command/LayeredCustomCommandRenderer$VerticesCache;Lcom/mojang/blaze3d/systems/RenderPass;Lnet/minecraft/client/texture/TextureManager;Z)V",
            at = @At(value = "HEAD"), argsOnly = true)
    private static boolean ldp$render$translucent(boolean translucent) {
        if (LDPConfig.INSTANCE.enabled) return true;
        return translucent;
    }
}
