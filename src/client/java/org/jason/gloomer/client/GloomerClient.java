package org.jason.gloomer.client;

import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.component.OriginComponent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import org.jason.gloomer.mixin.ParticleManagerAccessor;

import java.util.Map;
import java.util.Queue;

public class GloomerClient implements ClientModInitializer {

    private static final double PARTICLE_REMOVE_RADIUS = 32.0;
    private static final double RADIUS_SQUARED = PARTICLE_REMOVE_RADIUS * PARTICLE_REMOVE_RADIUS;
    private static ComponentKey<OriginComponent> ORIGIN_COMPONENT;

    @Override
    public void onInitializeClient() {
        ORIGIN_COMPONENT = ComponentRegistry.getOrCreate(
                new Identifier("origins", "origin"),
                OriginComponent.class
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) return;

            ParticleManagerAccessor accessor = (ParticleManagerAccessor) client.particleManager;
            Map<ParticleTextureSheet, Queue<Particle>> particleMap = accessor.gloomer$getParticles();

            removeParticlesNearGloomers(particleMap);
        });
    }


    public static void removeParticlesNearGloomers(Map<ParticleTextureSheet, Queue<Particle>> particleMap) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;

        if (world == null || particleMap == null) return;

        for (PlayerEntity player : world.getPlayers()) {
            if (isGloomerOrigin(player)) {
//                player.sendMessage(Text.literal("Detected"));
                Vec3d gloomerPos = player.getPos();
                removeParticlesInRadius(particleMap, gloomerPos);
            }
        }
    }

    private static void removeParticlesInRadius(Map<ParticleTextureSheet, Queue<Particle>> particleMap, Vec3d center) {
        for (Queue<Particle> queue : particleMap.values()) {
            if (queue == null) continue;

            queue.removeIf(particle -> {
                if (particle == null) return false;

                double dx = particle.getBoundingBox().minX - center.x;
                double dy = particle.getBoundingBox().minY - center.y;
                double dz = particle.getBoundingBox().minZ - center.z;
                double distanceSquared = dx * dx + dy * dy + dz * dz;

                return distanceSquared <= RADIUS_SQUARED;
            });
        }
    }

    // Check if a player has the gloomer origin
    private static boolean isGloomerOrigin(PlayerEntity player) {

        try {
//            player.sendMessage(Text.literal("Seen player1"));
            if (ORIGIN_COMPONENT == null) return false;
//            player.sendMessage(Text.literal("Seen player2"));
            OriginComponent component = ORIGIN_COMPONENT.get(player);
//            player.sendMessage(Text.literal("Seen player3"));
            OriginLayer layer = OriginLayers.getLayer(new Identifier("origins", "origin"));
//            player.sendMessage(Text.literal("Seen player4"));
            if (layer == null) return false;
//            player.sendMessage(Text.literal("Seen player5"));
            Origin origin = component.getOrigin(layer);
//            player.sendMessage(Text.literal(origin.toString()));

            if (origin != null) {
                String originId = origin.getIdentifier().toString();
                return originId.equals("gloomer_pack:gloomer");
            }
        } catch (Exception e) {
            // Player might not have component yet
            player.sendMessage(Text.literal(e.getMessage()));
        }
        return false;
    }
}