package org.jason.gloomer.mixin;

import org.jason.gloomer.client.GloomerClient;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    /**
     * Inject into the tick method to remove particles every frame.
     * We access the particles field via the accessor method instead of @Shadow.
     */
    @Inject(method = "tick()V", at = @At("RETURN"))
    private void onTickEnd(CallbackInfo ci) {
        // Cast to our accessor interface to get the particles
        GloomerClient.removeParticlesNearGloomers(
                ((ParticleManagerAccessor) this).gloomer$getParticles()
        );
    }
}