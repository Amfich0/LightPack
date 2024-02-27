package me.Amfich;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ColoredParticle;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import me.numin.spirits.ability.api.LightAbility;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class LightPunch extends LightAbility implements AddonAbility {
    @Attribute(Attribute.DAMAGE)
    private double DAMAGE = 1.5;
    @Attribute(Attribute.RANGE)
    private double RANGE = 18;
    @Attribute(Attribute.COOLDOWN)
    private long COOLDOWN = 4000;

    private Listener listener;
    private Permission perm;
    private Block block;
    private TempBlock temp;

    private Location location;
    private Vector direction;
    private double distanceTravelled;
    private Set<Entity> hurt;
    private  boolean controllable;

    public LightPunch(Player player) {
        super(player);

        location = player.getEyeLocation();
        direction = player.getLocation().getDirection();
        direction.multiply(0.8);
        distanceTravelled = 0;
        hurt = new HashSet<>();

        bPlayer.addCooldown(this);
        this.COOLDOWN = ConfigManager.getConfig().getLong("ExtraAbilities.Amfich.Spirit.LightSpirit.LightPunch.Cooldown");
        this.DAMAGE = ConfigManager.getConfig().getDouble("ExtraAbilities.Amfich.Spirit.LightSpirit.LightPunch.Damage");
        this.controllable = true;
        this.RANGE = ConfigManager.getConfig().getDouble("ExtraAbilities.Amfich.Spirit.LightSpirit.LightPunch.Range");
        start();
    }

    @Override
    public void progress() {
        if(!bPlayer.isOnline()) {
            remove();
        }
        if(player.isDead()) {
            remove();
        }
        if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
            remove();
            return;
        }
        if (this.controllable) {
            Vector untouchVector = this.direction.clone().multiply(1.23F);
            Location destinationLocation = this.location.clone().add(this.player.getLocation().getDirection().multiply(1.12F));
            Vector desiredVector = destinationLocation.toVector().subtract(this.location.clone().toVector());
            Vector steeringVector = desiredVector.subtract(untouchVector).multiply(0.25D);
            this.direction = this.direction.add(steeringVector);
        }


        if (location.getBlock().getType().isSolid()) {
            remove();
            return;
        }

        if (distanceTravelled > this.RANGE) {
            remove();
            return;
        }

        Effect();
        new ColoredParticle(Color.fromRGB(225, 221, 0), 1.86F).display(this.location, 4, (float) Math.random() / 2, (float) Math.random() / 2, (float) Math.random()  / 2);
        ParticleEffect.END_ROD.display(this.location, 2, 0.5, 0.5, 0.5, 0f);




        player.getLocation().getWorld().playSound(location, Sound.ENTITY_EVOKER_CAST_SPELL, 0.7F, 1.3F);
        if (ThreadLocalRandom.current().nextInt(6) == 0) {
            for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)) {
                if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId() && !(entity instanceof ArmorStand)) {
                    ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
                    ParticleEffect.FLASH.display(entity.getLocation(), 1, 0.5, 0.5, 0.5, 0f);
                    if (!hurt.contains(entity)) {
                        DamageHandler.damageEntity(entity, DAMAGE, this);
                        hurt.add(entity);
                        if (entity instanceof Player) {
                            Player ePlayer = (Player) entity;
                            BendingPlayer bEntity = BendingPlayer.getBendingPlayer(ePlayer);
                            if (bEntity.hasElement(Element.getElement("DarkSpirit"))) {
                                DamageHandler.damageEntity(entity, DAMAGE * 2, this);
                                ePlayer.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 30, 3));
                                ePlayer.getWorld().playSound(ePlayer.getLocation(), Sound.ENTITY_GUARDIAN_AMBIENT, 0.38F, 0.48f);
                            }
                        }
                    }
                }
            }
        }

        location.add(direction);
        distanceTravelled += direction.length();
    }

    private void Effect() {
        List<Entity> targets = GeneralMethods.getEntitiesAroundPoint(location, 1);
        for (Entity target : targets) {
            if (target.getUniqueId() == player.getUniqueId()) {
                continue;
            }

            if (!hurt.contains(target)) {
                DamageHandler.damageEntity(target, this.DAMAGE, this);
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
        return this.COOLDOWN;
    }

    @Override
    public void load() {
        listener = new LightPunchListener();
        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(listener, ProjectKorra.plugin);
        perm = new Permission("bending.ability.LightPuncher");
        perm.setDefault(PermissionDefault.OP);
        ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);

        ConfigManager.getConfig().addDefault("ExtraAbilities.Amfich.Spirit.LightSpirit.LightPunch.Damage", 1.5);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Amfich.Spirit.LightSpirit.LightPunch.Cooldown", 4000);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Amfich.Spirit.LightSpirit.LightPunch.Range", 18);

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
        return "LightPuncher";
    }
    @Override
    public String getDescription() {
        return "With this ability you can shoot light energy at players or mobs!";

    }
    @Override
    public String getInstructions() {
        return "LeftClick";
    }
}