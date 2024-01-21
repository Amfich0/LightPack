package me.Amfich;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ColoredParticle;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.Vector;

import me.numin.spirits.ability.api.LightAbility;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Обичная способность которой можна стреляться
 * @author Amfich
 */
public class SparkBlast extends LightAbility implements AddonAbility {

    private static final double DAMAGE = 1;
    private static final double RANGE = 25;
    private static final long COOLDOWN = 3500;

    private Listener listener;
    private Permission perm;

    private Location location;
    private Vector direction;
    private double distanceTravelled;
    private Set<Entity> hurt;

    public SparkBlast(Player player) {
        super(player);

        location = player.getEyeLocation();
        direction = player.getLocation().getDirection();
        direction.multiply(0.8);
        distanceTravelled = 0;
        hurt = new HashSet<>();

        bPlayer.addCooldown(this);

        start();
    }

    @Override
    public void progress() {
        if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
            remove();
            return;
        }

        if (location.getBlock().getType().isSolid()) {
            remove();
            return;
        }

        if (distanceTravelled > RANGE) {
            remove();
            return;
        }

        affectTargets();
        new ColoredParticle(Color.fromRGB(225, 221, 0), 2.13F).display(this.location, 4, (float) Math.random() / 2, (float) Math.random() / 2, (float) Math.random()  / 2);
        ParticleEffect.END_ROD.display(this.location, 3, 0.5, 0.5, 0.5, 0f);
        ParticleEffect.FLASH.display(this.location, 1, 0.5, 0.5, 0.5, 0f);

        player.getLocation().getWorld().playSound(location, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.7F, 1.3F);

        if (ThreadLocalRandom.current().nextInt(6) == 0) {

        }

        location.add(direction);
        distanceTravelled += direction.length();
    }

    private void affectTargets() {
        List<Entity> targets = GeneralMethods.getEntitiesAroundPoint(location, 1);
        for (Entity target : targets) {
            if (target.getUniqueId() == player.getUniqueId()) {
                continue;
            }

            if (!hurt.contains(target)) {
                DamageHandler.damageEntity(target, DAMAGE, this);
                hurt.add(target);

            }
        }
    }

    @Override
    public void remove() {
        super.remove();
        hurt.clear();
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean isSneakAbility() {
        return true;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public boolean isIgniteAbility() {
        return false;
    }

    @Override
    public boolean isExplosiveAbility() {
        return false;
    }

    @Override
    public long getCooldown() {
        return COOLDOWN;
    }

    @Override
    public void load() {
        listener = new SparkBlastListener();
        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(listener, ProjectKorra.plugin);
        perm = new Permission("bending.ability.SparkBlast");
        perm.setDefault(PermissionDefault.OP);
        ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);

        ConfigManager.getConfig().addDefault("ExtraAbilities.Amfich.Spirit.LightSpirit.SparkBlast.Damage", 1);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Amfich.Spirit.LightSpirit.SparkBlast.Cooldown", 3500);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Amfich.Spirit.LightSpirit.SparkBlast.Range", 25);

    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(listener);
        ProjectKorra.plugin.getServer().getPluginManager().removePermission(perm);
    }

    @Override
    public String getAuthor() {
        return "Amfich";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getName() {
        return "SparkBlast";
    }
    @Override
    public String getDescription() {
        return "Спомощью энергии света вы можете призивать звезди на своих врагов!";

    }
    @Override
    public String getInstructions() {
        return "ЛКМ";
    }
}