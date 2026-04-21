package pluginsmc.langdua.core.paper.hooks;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pluginsmc.langdua.core.paper.Core;

public class MythicMobsHook implements Listener {

    private final Core instance;

    public MythicMobsHook(Core instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onMechanicLoad(MythicMechanicLoadEvent event) {
        if (event.getMechanicName().equalsIgnoreCase("playcinematic")) {
            event.register(new CinematicMechanic(event.getConfig(), instance));
        }
    }
}

class CinematicMechanic implements ITargetedEntitySkill {
    private final String cinematicName;
    private final Core instance;

    public CinematicMechanic(MythicLineConfig config, Core instance) {
        this.instance = instance;
        this.cinematicName = config.getString(new String[]{"cinematic", "cine", "c"}, "");
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target.isPlayer() && !cinematicName.isEmpty()) {
            Player player = (Player) target.getBukkitEntity();
            Bukkit.getScheduler().runTask(instance, () -> {
                player.performCommand("cinematic play " + player.getName() + " " + cinematicName);
            });

            return SkillResult.SUCCESS;
        }
        return SkillResult.INVALID_TARGET;
    }
}