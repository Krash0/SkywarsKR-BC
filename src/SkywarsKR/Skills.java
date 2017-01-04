package SkywarsKR;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.metadata.FixedMetadataValue;

public class Skills implements Listener {
	public static Skills instancia;

	public static Skills getInstancia() {
		if (instancia == null) {
			instancia = new Skills();
		}
		return instancia;
	}

	boolean extraLife(Player player) {
		String skills = Utilities.getInstancia().getSkills(player.getName()).toLowerCase();
		if (skills != null) {
			if (skills.contains("extralife")) {
				if (player.hasMetadata("SWKitSkill")) {
					player.removeMetadata("SWKitSkill", Main.pl);
					return false;
				}

				int slot = Main.arena.players.get(player.getName().toLowerCase()).slot;
				Location TP = Utilities.getInstancia().getIsland(Main.arenaConfig.getConfig(), slot);
				player.teleport(TP);

				player.setMetadata("SWKitSkill", new FixedMetadataValue(Main.pl, ""));
				return true;
			}
		}
		return false;
	}

	@EventHandler
	void playerDamage(EntityDamageEvent e) {
		if (e.getEntity() == null || !(e.getEntity() instanceof Player)) {
			return;
		}

		Player player = (Player) e.getEntity();

		if (!Main.arena.players.containsKey(player.getName())) {
			return;
		}

		String skills = Utilities.getInstancia().getSkills(player.getName()).toLowerCase();
		if (skills != null) {
			// Stomper
			if (e.getCause() == DamageCause.FALL) {
				if (skills.contains("Stomper")) {
					for (Entity entity : e.getEntity().getNearbyEntities(2, 2, 2)) {
						if (entity instanceof Player) {
							Player playerStomped = (Player) entity;
							playerStomped.damage(e.getDamage());
						}
					}
					if (e.getDamage() > 4) {
						e.setDamage(4);
					}
					return;
				}
			}
		}
	}
}
