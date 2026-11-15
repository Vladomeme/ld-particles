package net.vladomeme.main.mixin;

import net.minecraft.client.particle.Particle;
import net.vladomeme.main.ParticleDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;


@Mixin(Particle.class)
public class ParticleMixin implements ParticleDuck {

    @Shadow public float alpha;

    @Unique boolean ldp$tracked = false;
    @Unique boolean ldp$hidden = false;
    @Unique float ldp$opacity;
    @Unique boolean ldp$useDefault = true;

    @Override
    public void ldp$track() {
        this.ldp$tracked = true;
    }

    @Override
    public boolean ldp$isTracked() {
        return ldp$tracked;
    }

    @Override
    public void ldp$hide() {
        ldp$hidden = true;
    }

    @Override
    public boolean ldp$isHidden() {
        return ldp$hidden;
    }

    @Override
    public void ldp$switchToDefault() {
        ldp$useDefault = true;
        ldp$hidden = false;
    }

    @Override
    public boolean ldp$isDefault() {
        return ldp$useDefault;
    }

    @Override
    public void ldp$setOpacity(float opacity) {
        this.ldp$opacity = opacity;
        this.ldp$useDefault = opacity == alpha;
        ldp$hidden = false;
    }

    @Override
    public float ldp$opacity() {
        return ldp$opacity;
    }
}
