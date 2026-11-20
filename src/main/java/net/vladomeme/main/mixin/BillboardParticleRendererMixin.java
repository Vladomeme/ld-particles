package net.vladomeme.main.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.BillboardParticleRenderer;
import net.minecraft.client.particle.BillboardParticleSubmittable;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Vec3d;
import net.vladomeme.main.ParticleDuck;
import net.vladomeme.main.config.LDPConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BillboardParticleRenderer.class)
public abstract class BillboardParticleRendererMixin {

	@Unique final LDPConfig config = LDPConfig.INSTANCE;

	@Unique boolean ldp$shouldUpdate = false;
	@Unique int ldp$frameCount = 0;

	//tick refresh rate & prepare for update
	@Inject(method = "render", at = @At(value = "HEAD"))
	private void ldp$render$HEAD(Frustum frustum, Camera camera, float tickProgress, CallbackInfoReturnable<Submittable> cir) {
		if (config.enabled && config.refreshRate != 0) {
			if (ldp$frameCount >= config.refreshRate) {
				ldp$shouldUpdate = true;
				ldp$frameCount = 0;
			}
			else ldp$frameCount++;
		}
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/particle/BillboardParticle;render(Lnet/minecraft/client/particle/BillboardParticleSubmittable;Lnet/minecraft/client/render/Camera;F)V"))
	private void ldp$render$render(BillboardParticle particle, BillboardParticleSubmittable submittable, Camera camera, float delta, Operation<Void> original) {
		if (!config.enabled) {
			original.call(particle, submittable, camera, delta);
			return;
		}

		if (config.refreshRate == 0) ldp$runInstant(particle, submittable, camera, delta, original);
		else ldp$runWithRefreshRate(particle, submittable, camera, delta, original);
	}

	//reset update variable at the end of the method
	@Inject(method = "render", at = @At(value = "TAIL"))
	private void ldp$render$TAIL(Frustum frustum, Camera camera, float tickProgress, CallbackInfoReturnable<Submittable> cir) {
		if (config.enabled && config.refreshRate != 0) ldp$shouldUpdate = false;
	}

	@Unique
	private void ldp$runInstant(BillboardParticle particle, BillboardParticleSubmittable submittable, Camera camera, float delta, Operation<Void> original) {
		Vec3d cameraPos = camera.getCameraPos();

		double distanceX = Math.abs(particle.x - cameraPos.x);
		double distanceY = Math.abs(particle.y - cameraPos.y) * config.horizontalScaleReverse;
		double distanceZ = Math.abs(particle.z - cameraPos.z);

		//planar check for particles outside of fade range
		if (distanceX > config.fadeEndRange || distanceZ > config.fadeEndRange || distanceY > config.fadeEndRange) {
			if (particle.alpha > config.maxOpacity) {
				//range not limited
				if (!config.limitEffectRange) {
					ldp$callWithOpacity(particle, submittable, camera, delta, original, config.maxOpacity);
					return;
				}
				//range limited, outside of range
				else if (distanceX > config.effectRange || distanceZ > config.effectRange || distanceY > config.effectRange) {
					original.call(particle, submittable, camera, delta);
					return;
				}
				//within range
				ldp$callWithOpacity(particle, submittable, camera, delta, original, config.maxOpacity);
				return;
			}
			original.call(particle, submittable, camera, delta);
			return;
		}

		double distanceSquared = distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ;

		//within fade start range
		if (distanceSquared < config.fadeStartRangeSquared) {
			if (config.renderNearbyParticles)
				ldp$callWithOpacity(particle, submittable, camera, delta, original, config.minOpacity);
			return;
		}

		//outside of fade range
		if (distanceSquared > config.fadeEndRangeSquared) {
			if (particle.alpha > config.maxOpacity) {
				ldp$callWithOpacity(particle, submittable, camera, delta, original, Math.min(particle.alpha, config.maxOpacity));
				return;
			}
			original.call(particle, submittable, camera, delta);
			return;
		}

		//within fade range
		if (!config.useInThirdPerson && camera.isThirdPerson()) {
			original.call(particle, submittable, camera, delta);
			return;
		}
		if (particle.alpha > config.minOpacity) {
			float opacity = config.minOpacity + ((float) (distanceSquared - config.fadeStartRangeSquared)) * config.opacityLengthMagicNumber;
			if (particle.alpha > opacity) {
				ldp$callWithOpacity(particle, submittable, camera, delta, original, opacity);
				return;
			}
		}
		original.call(particle, submittable, camera, delta);
	}

	@Unique
	private void ldp$runWithRefreshRate(BillboardParticle particle, BillboardParticleSubmittable submittable, Camera camera, float delta, Operation<Void> original) {
		ParticleDuck duck = (ParticleDuck) particle;

		if (!ldp$shouldUpdate) {
			if (!duck.ldp$isHidden()) {
				if (duck.ldp$isDefault())
					original.call(particle, submittable, camera, delta);
				else
					ldp$callWithOpacity(particle, submittable, camera, delta, original, duck.ldp$opacity());
			}
			return;
		}

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
					ldp$callWithOpacity(particle, submittable, camera, delta, original, config.maxOpacity);
					return;
				}
				//range limited, outside of range
				else if (distanceX > config.effectRange || distanceZ > config.effectRange || distanceY > config.effectRange) {
					duck.ldp$switchToDefault();
					original.call(particle, submittable, camera, delta);
					return;
				}
				//within range
				duck.ldp$setOpacity(config.maxOpacity);
				ldp$callWithOpacity(particle, submittable, camera, delta, original, config.maxOpacity);
				return;
			}
			duck.ldp$switchToDefault();
			original.call(particle, submittable, camera, delta);
			return;
		}

		double distanceSquared = distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ;

		//within fade start range
		if (distanceSquared < config.fadeStartRangeSquared) {
			if (config.renderNearbyParticles) {
				duck.ldp$setOpacity(config.minOpacity);
				ldp$callWithOpacity(particle, submittable, camera, delta, original, config.minOpacity);
				return;
			}
			duck.ldp$hide();
			return;
		}

		//outside of fade range
		if (distanceSquared > config.fadeEndRangeSquared) {
			if (particle.alpha > config.maxOpacity) {
				duck.ldp$setOpacity(config.maxOpacity);
				ldp$callWithOpacity(particle, submittable, camera, delta, original, Math.min(particle.alpha, config.maxOpacity));
				return;
			}
			duck.ldp$switchToDefault();
			original.call(particle, submittable, camera, delta);
			return;
		}

		//within fade range
		if (!config.useInThirdPerson && camera.isThirdPerson()) {
			duck.ldp$switchToDefault();
			original.call(particle, submittable, camera, delta);
			return;
		}
		if (particle.alpha > config.minOpacity) {
			float opacity = config.minOpacity + ((float) (distanceSquared - config.fadeStartRangeSquared)) * config.opacityLengthMagicNumber;
			if (particle.alpha > opacity) {
				duck.ldp$setOpacity(opacity);
				ldp$callWithOpacity(particle, submittable, camera, delta, original, opacity);
				return;
			}
		}
		duck.ldp$switchToDefault();
		original.call(particle, submittable, camera, delta);
	}

	@Unique
	private void ldp$callWithOpacity(BillboardParticle particle, BillboardParticleSubmittable submittable, Camera camera, float delta, Operation<Void> original, float opacity) {
		float oldOpacity = particle.alpha;
		particle.alpha = opacity;
		original.call(particle, submittable, camera, delta);
		particle.alpha = oldOpacity;
	}
}
