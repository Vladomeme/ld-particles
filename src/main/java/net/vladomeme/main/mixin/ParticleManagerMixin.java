package net.vladomeme.main.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Vec3d;
import net.vladomeme.main.ParticleDuck;
import net.vladomeme.main.config.LDPConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

	@Unique final LDPConfig config = LDPConfig.INSTANCE;

	//Set opacity for newly added particles if non-instant refresh rate is used
	@Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At(value = "HEAD"))
	private void ldp$addParticle(Particle p, CallbackInfo ci) {
		if (!config.enabled || config.refreshRate == 0) return;

		if (!(p instanceof BillboardParticle particle)) return;

		ParticleDuck duck = (ParticleDuck) particle;

		Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
		Vec3d cameraPos = camera.getCameraPos();
		double distanceX = Math.abs(particle.x - cameraPos.x);
		double distanceY = Math.abs(particle.y - cameraPos.y) * config.horizontalScaleReverse;
		double distanceZ = Math.abs(particle.z - cameraPos.z);

		//planar check for particles outside of fade range
		if (distanceX > config.fadeEndRange || distanceZ > config.fadeEndRange || distanceY > config.fadeEndRange) {
			if (particle.alpha > config.maxOpacity) {
				//range not limited
				if (!config.limitEffectRange) {
					duck.ldp$setOpacity(config.maxOpacity);
					return;
				}
				//range limited, outside of range
				else if (distanceX > config.effectRange || distanceZ > config.effectRange || distanceY > config.effectRange) {
					return;
				}
				//within range
				duck.ldp$setOpacity(config.maxOpacity);
				return;
			}
			return;
		}

		double distanceSquared = distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ;

		//within fade start range
		if (distanceSquared < config.fadeStartRangeSquared) {
			if (config.renderNearbyParticles) {
				duck.ldp$setOpacity(config.minOpacity);
				return;
			}
			duck.ldp$hide();
			return;
		}

		//outside of fade range
		if (distanceSquared > config.fadeEndRangeSquared) {
			if (particle.alpha > config.maxOpacity)
				duck.ldp$setOpacity(config.maxOpacity);
			return;
		}

		//within fade range
		if (particle.alpha > config.minOpacity) {
			float opacity = config.minOpacity + ((float) (distanceSquared - config.fadeStartRangeSquared)) * config.opacityLengthMagicNumber;
			if (particle.alpha > opacity)
				duck.ldp$setOpacity(opacity);
		}
	}
}
