package net.vladomeme.main.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.math.Vec3d;
import net.vladomeme.main.config.LDPConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

	@Unique final LDPConfig config = LDPConfig.INSTANCE;

	@WrapOperation(method = "renderParticles", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/particle/ParticleTextureSheet;begin(Lnet/minecraft/client/render/Tessellator;Lnet/minecraft/client/texture/TextureManager;)Lnet/minecraft/client/render/BufferBuilder;"))
	private BufferBuilder ldp$renderParticles$changeRenderState(ParticleTextureSheet instance, Tessellator tessellator, TextureManager textureManager, Operation<BufferBuilder> original) {
		if (config.enabled && !MinecraftClient.isFabulousGraphicsOrBetter()) {
			return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT.begin(tessellator, textureManager);
		}
		return original.call(instance, tessellator, textureManager);
	}

	@WrapOperation(method = "renderParticles", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/particle/Particle;buildGeometry(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/Camera;F)V"))
	private void ldp$renderParticles$buildGeometry(Particle particle, VertexConsumer vertexConsumer, Camera camera, float delta, Operation<Void> original) {
		if (!config.enabled) {
			original.call(particle, vertexConsumer, camera, delta);
			return;
		}

		Vec3d cameraPos = camera.getPos();
		double distanceX = particle.x - cameraPos.x;
		double distanceY = particle.y - cameraPos.y;
		double distanceZ = particle.z - cameraPos.z;
		double distanceSquared = distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ;

		//closer than start range
		if (distanceSquared < config.fadeStartRangeSquared) {
			if (config.renderNearbyParticles)
				callWithOpacity(particle, vertexConsumer, camera, delta, original, config.minOpacity);
			return;
		}

		//further than end range
		if (distanceSquared > config.fadeEndRangeSquared) {
			if (!config.limitEffectRange || distanceSquared <= config.effectRangeSquared) {
				if (particle.alpha > config.maxOpacity) {
					callWithOpacity(particle, vertexConsumer, camera, delta, original, config.maxOpacity);
					return;
				}
			}
			original.call(particle, vertexConsumer, camera, delta);
			return;
		}

		//within fade range
		if (!config.useInThirdPerson && camera.isThirdPerson()) {
			original.call(particle, vertexConsumer, camera, delta);
			return;
		}
		if (particle.alpha > config.minOpacity) {
			float opacity = config.minOpacity + ((float) (distanceSquared - config.fadeStartRangeSquared)) / config.opacityLengthMagicNumber;
			if (particle.alpha > opacity) {
				callWithOpacity(particle, vertexConsumer, camera, delta, original, opacity);
				return;
			}
		}
		original.call(particle, vertexConsumer, camera, delta);
	}

	@Unique
	private void callWithOpacity(Particle particle, VertexConsumer vertexConsumer, Camera camera, float delta, Operation<Void> original, float opacity) {
		float oldOpacity = particle.alpha;
		particle.alpha = opacity;
		original.call(particle, vertexConsumer, camera, delta);
		particle.alpha = oldOpacity;
	}
}
