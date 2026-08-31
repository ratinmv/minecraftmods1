package com.ratinmv.worldlearns;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod(WorldLearnsMod.MOD_ID)
public final class WorldLearnsMod {
    public static final String MOD_ID = "worldlearns";
    private static final Map<UUID, LearningProfile> PROFILES = new ConcurrentHashMap<>();

    public WorldLearnsMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    private static LearningProfile profile(Player p) {
        return PROFILES.computeIfAbsent(p.getUUID(), id -> new LearningProfile());
    }

    @SubscribeEvent
    public void onPlayerAttack(LivingAttackEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (event.getEntity() instanceof Mob) profile(player).attacks++;
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        Entity killer = event.getSource().getEntity();
        if (killer instanceof Player player) {
            LearningProfile p = profile(player);
            p.mobKills++;
            p.lastMobType = mob.getType().toString();
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        LearningProfile old = PROFILES.remove(event.getOriginal().getUUID());
        if (old != null) PROFILES.put(event.getEntity().getUUID(), old);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.getServer().getTickCount() % 20 != 0) return;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (Player player : level.players()) {
                LearningProfile p = profile(player);
                int threat = Math.min(6, p.attacks / 12 + p.mobKills / 20);
                if (threat <= 0) continue;

                // Generic adaptation: every Mob, including mobs supplied by other mods,
                // receives the same safe, class-agnostic behavior based on its target.
                // No entity-specific casts are used, so unknown modded mobs are supported.
                double radius = 10.0 + threat * 2.0;
                for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(radius))) {
                    if (!mob.isAlive()) continue;
                    if (mob.distanceToSqr(player) > radius * radius) continue;

                    // Repeated combat makes nearby mobs more alert and persistent.
                    if (mob.getTarget() == null && mob.canAttack(player)) {
                        mob.setTarget(player);
                    }
                }
            }
        }
    }

    private static final class LearningProfile {
        int attacks;
        int mobKills;
        String lastMobType = "";
    }
}
