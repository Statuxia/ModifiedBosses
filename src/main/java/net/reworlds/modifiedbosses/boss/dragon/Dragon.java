package net.reworlds.modifiedbosses.boss.dragon;

import net.reworlds.modifiedbosses.boss.Boss;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.LivingEntity;

public class Dragon extends Boss {

    public Dragon(LivingEntity boss) {
        super(boss);
        setAllowExplodeDamage(false);
        setClearPotionEffects(true);
        setRadius(200);
        setMaxDamagePerHit(20);
        setMinimumDamageToReward(50);
        setBossBar("Эндер-дракон", BarColor.RED, BarStyle.SEGMENTED_10);
//        setAttributes();
        setSettings();
        activate();
    }

    @Override
    protected void setSettings() {

    }

    @Override
    protected void rewardPlayers() {

    }

    @Override
    protected void loopTask() {

    }

}
