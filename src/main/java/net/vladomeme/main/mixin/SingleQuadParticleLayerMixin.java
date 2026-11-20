package net.vladomeme.main.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.RenderPipelines;
import net.vladomeme.main.config.LDPConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;


@Mixin(SingleQuadParticle.Layer.class)
public class SingleQuadParticleLayerMixin {

    @ModifyVariable(method = "<init>", at = @At(value = "HEAD"), argsOnly = true)
    private static boolean ldp$init$translucent(boolean translucent) {
        if (LDPConfig.INSTANCE.enabled) return true;
        return translucent;
    }

    @ModifyVariable(method = "<init>", at = @At(value = "HEAD"), argsOnly = true)
    private static RenderPipeline ldp$init$pipeline(RenderPipeline pipeline) {
        if (LDPConfig.INSTANCE.enabled) return RenderPipelines.TRANSLUCENT_PARTICLE;
        return pipeline;
    }
}
