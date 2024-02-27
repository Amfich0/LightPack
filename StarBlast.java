package me.Amfich.StarBlast;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ColoredParticle;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Sound;
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

public class StarBlast extends LightAbility implements AddonAbility {
    @Attribute(Attribute.DAMAGE)
    private double DAMAGE = 1;
    @Attribute(Attribute.RANGE)
    private double RANGE = 25;
    @Attribute(Attribute.RANGE)
    private  long COOLDOWN = 3500;
    private int currPoint;

    private Listener listener;
    private boolean controllable;
    private Permission perm;

    private Location location;
    private Vector direction;
    private double distanceTravelled;
    private int points;
    private float size;
    private Set<Entity> hurt;
    private String description;

    public StarBlast(Player player) {
        super(player);

        location = player.getEyeLocation();
        direction = player.getLocation().getDirection();
        direction.multiply(0.8);
        distanceTravelled = 0;
        hurt = new HashSet<>();

        bPlayer.addCooldown(this);
        this.COOLDOWN = ConfigManager.getConfig().getLong("ExtraAbilities.Amfich.Spirit.LightSpirit.StarBlast.Cooldown");
        this.DAMAGE = ConfigManager.getConfig().getDouble("ExtraAbilities.Amfich.Spirit.LightSpirit.StarBlast.Damage");
        this.RANGE = ConfigManager.getConfig().getDouble("ExtraAbilities.Amfich.Spirit.LightSpirit.StarBlast.Range");
        this.controllable = true;
        this.description = ConfigManager.getConfig().getString("ExtraAbilities.Amfich.Spirit.LightSpirit.StarBlast.Description");


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


        if (location.getBlock().getType().isSolid()) {
            remove();
            return;
        }

        if (distanceTravelled > RANGE) {
            remove();
            return;
        }
        if (this.controllable) {
            Vector untouchVector = this.direction.clone().multiply(0.98F);
            Location destinationLocation = this.location.clone().add(this.player.getLocation().getDirection().multiply(0.98F));
            Vector desiredVector = destinationLocation.toVector().subtract(this.location.clone().toVector());
            Vector steeringVector = desiredVector.subtract(untouchVector).multiply(0.25D);
            this.direction = this.direction.add(steeringVector);
        }

        StarEffect();
        new ColoredParticle(Color.fromRGB(225, 221, 0), 2.13F).display(this.location, 4, (float) Math.random() / 2, (float) Math.random() / 2, (float) Math.random()  / 2);
        ParticleEffect.END_ROD.display(this.location, 2, 0.5, 0.5, 0.5, 0f);
        ParticleEffect.FLASH.display(this.location, 1, 0.5, 0.5, 0.5, 0f);
        Rings(60, 2.25F, 3);
        Rings(60, 2.5F, 3);
        Rings(60, 3.0F, 3);
        player.getLocation().getWorld().playSound(location, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.2F, 1.3F);
        for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)) {
            if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId() && !(entity instanceof ArmorStand)) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
                    if (!hurt.contains(entity)) {
                        DamageHandler.damageEntity(entity, DAMAGE, this);
                        hurt.add(entity);
                        if (entity instanceof Player) {
                            Player ePlayer = (Player) entity;
                            BendingPlayer bEntity = BendingPlayer.getBendingPlayer(ePlayer);
                            if (bEntity.hasElement(Element.getElement("DarkSpirit"))) {
                                DamageHandler.damageEntity(entity, DAMAGE * 2, this);
                                ePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 3));
                                ePlayer.getWorld().playSound(ePlayer.getLocation(), Sound.ENTITY_GUARDIAN_HURT, 0.78F, 0.98f);
                                ParticleEffect.CRIT_MAGIC.display(ePlayer.getLocation().add(-3, 2, 1), 1, 0.3, 2, 0.4, 0f);
                            }
                        }
                }
            }
        }

        if (ThreadLocalRandom.current().nextInt(6) == 0) {

        }

        location.add(direction);
        distanceTravelled += direction.length();
    }

    private void StarEffect() {
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
    private void Rings(int points, float size, int speed) {
        for (int i = 0; i < speed; i++) {
            this.currPoint += 360 / points;
            if (this.currPoint > 360)
                this.currPoint = 0;
            double angle = this.currPoint * Math.PI / 180.0D;
            double x = size * Math.cos(angle);
            double z = size * Math.sin(angle);
            Location loc = this.player.getLocation().add(x, 0.75D, z);
            ParticleEffect.ENCHANTMENT_TABLE.display(loc, 1, 0.0D, 0.0D, 0.0D, 0.04D);
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
        listener = new StarBlastListener();
        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(listener, ProjectKorra.plugin);
        perm = new Permission("bending.ability.StarBlast");
        perm.setDefault(PermissionDefault.OP);
        ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);

        ConfigManager.getConfig().addDefault("ExtraAbilities.Amfich.Spirit.LightSpirit.StarBlast.Damage", 1);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Amfich.Spirit.LightSpirit.StarBlast.Cooldown", 3500);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Amfich.Spirit.LightSpirit.StarBlast.Range", 25);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Amfich.Spirit.LightSpirit.StarBlast.controllable", true);

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
        return "StarBlast";
    }
    @Override
    public String getDescription() {
        return "Using the energy of light, you can summon stars to your enemies!";


    }
    @Override
    public String getInstructions() {
        return "LeftClick";
    }
}