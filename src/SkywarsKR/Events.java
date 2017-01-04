package SkywarsKR;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;

public class Events implements Listener {

	@EventHandler(priority = EventPriority.HIGH)
	void PlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		switch (Utilities.getInstancia().joinInArena(p)) {
		case 1:
			return;
		case 2:
			p.kickPlayer(Utilities.getInstancia().getMessage("Geral.Partida_ja_comecou"));
			return;
		case 4:
			p.kickPlayer(Utilities.getInstancia().getMessage("Geral.Partida_cheia"));
			return;
		case 5:
			if (Utilities.getInstancia().verifStart()) {
				Utilities.getInstancia().start();
			}
			return;
		}
	}

	@EventHandler
	void changeMotdh(ServerListPingEvent e) {
		if (Main.arena.activated) {
			if (Main.arena.playing) {
				e.setMotd("Jogando");
			} else if (Main.arena.countdown) {
				e.setMotd("Começando");
			} else {
				e.setMotd("Em espera");
			}
		} else {
			e.setMotd("§2Desativada");
		}
	}

	@EventHandler
	void PlayerInteract(PlayerInteractEvent e) {
		// Hotbar click
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
			Player p = e.getPlayer();
			if (Main.arena.players.containsKey(p.getName().toLowerCase()) && p.getItemInHand() != null) {
				if (Main.arena == null) {
					return;
				}
				if (Main.arena.playing) {
					return;
				}

				if (p.getItemInHand().getType() == Material.BLAZE_POWDER
						&& p.getItemInHand().getItemMeta().hasDisplayName()
						&& p.getItemInHand().getItemMeta().getDisplayName().contains("SAIR")) {
					Main.pl.getServer().dispatchCommand(p, "sw sair");
					return;
				}
				if (p.getItemInHand().getType() == Material.EMERALD && p.getItemInHand().getItemMeta().hasDisplayName()
						&& p.getItemInHand().getItemMeta().getDisplayName().contains("KITS")) {
					Utilities.getInstancia().openMenuKits(p);
					return;
				}
			}
		}

		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			// Set chests
			if (e.getClickedBlock().getType() == Material.CHEST) {
				if (e.getPlayer().hasMetadata("SWChests")) {
					if (e.getPlayer().getMetadata("SWChests").size() != 0) {
						e.setCancelled(true);
						Player p = e.getPlayer();
						if (Utilities.getInstancia().setChest(e.getClickedBlock())) {
							p.sendMessage("§8[SkyWarsKR v2] §7O chest foi setado com sucesso!");
						}
						return;
					}
				}

				if (e.getPlayer().hasMetadata("SWFeasts")) {
					if (e.getPlayer().getMetadata("SWFeasts").size() != 0) {
						e.setCancelled(true);
						Player p = e.getPlayer();
						if (Utilities.getInstancia().setFeast(e.getClickedBlock())) {
							p.sendMessage("§8[SkyWarsKR v2] §7O feast foi setado com sucesso!");
						}
						return;
					}
				}
				return;
			}
		}
	}

	@EventHandler
	void PlayerDeath(PlayerDeathEvent e) {
		if (e.getEntity() instanceof Player) {
			if (Utilities.getInstancia().playerDeath(e.getEntity(), e.getEntity().getKiller())) {
				e.setDeathMessage(null);
			}
		}
	}

	@EventHandler
	void EntityDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			if (e.getCause() == DamageCause.VOID) {
				return;
			}

			Player p = (Player) e.getEntity();
			if (Main.arena.players.containsKey(p.getName().toLowerCase())) {
				if (Main.arena.countdown) {
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	void PlayerQuit(PlayerQuitEvent e) {
		Utilities.getInstancia().exitArena(e.getPlayer());
	}

	@EventHandler
	void PlayerKick(PlayerKickEvent e) {
		Utilities.getInstancia().exitArena(e.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void chat(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();

		if (!Main.arena.players.containsKey(p.getName().toLowerCase())) {
			return;
		}

		if (Main.pl.getConfig().getString("Arena.Chat") == null) {
			return;
		}

		if (!Main.pl.getConfig().getBoolean("Arena.Chat.Ativado")) {
			return;
		}

		String Format = Main.pl.getConfig().getString("Arena.Chat.Formato");

		e.setCancelled(true);

		for (Player player : Main.pl.getServer().getOnlinePlayers()) {
			player.sendMessage(
					Format.replace("@player", p.getName()).replace("@msg", e.getMessage()).replace("&", "§"));
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void playerCommand(PlayerCommandPreprocessEvent e) {
		Player p = e.getPlayer();
		if (Main.arena.players.containsKey(p.getName().toLowerCase())) {
			if (p.hasPermission("SkywarsKR.Admin") || p.isOp()) {
				return;
			}
			if (!e.getMessage().equalsIgnoreCase("/SW sair")) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void inventoryClick(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();

		if (!e.getInventory().getName().contains("KIT")) {
			return;
		}

		e.setCancelled(true);

		if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
			return;
		}

		if (!Main.arena.players.containsKey(p.getName().toLowerCase())) {
			return;
		}

		ItemStack itemClicked = e.getCurrentItem();
		if (itemClicked.getItemMeta() != null && itemClicked.getItemMeta().getDisplayName() != null) {
			String displayName = ChatColor.stripColor(itemClicked.getItemMeta().getDisplayName());

			if (displayName.contains("Voltar")) {
				Utilities.getInstancia().openMenuKits(p);
				return;
			}

			String kitName = ChatColor.stripColor(displayName);

			if (e.getClick() == ClickType.LEFT) {
				Utilities.getInstancia().openKitInfo(p, kitName.toLowerCase());
				return;
			}

			if (Main.kits.getConfig().getString("Kits." + kitName.toLowerCase()) == null) {
				return;
			}

			int pointsI = Main.kits.getConfig().getInt("Kits." + kitName.toLowerCase() + ".Pontos");
			int killsI = Main.kits.getConfig().getInt("Kits." + kitName.toLowerCase() + ".Matou_jogadores");
			int winsI = Main.kits.getConfig().getInt("Kits." + kitName.toLowerCase() + ".Vitorias");

			int points = Main.playersInfo.get(p.getName().toLowerCase()).points;
			int kills = Main.playersInfo.get(p.getName().toLowerCase()).kills;
			int wins = Main.playersInfo.get(p.getName().toLowerCase()).wins;

			if (!p.hasPermission("SkywarsKR.kit." + kitName.toLowerCase())) {
				boolean VIP = Main.kits.getConfig().getBoolean("Kits." + kitName + ".Apenas_vips");
				if (VIP == true) {
					if (!p.hasPermission("SkywarsKR.Vip")) {
						p.sendMessage(Utilities.getInstancia().getMessage("Geral.Kit_sem_permissao"));
						p.closeInventory();
						return;
					}
				} else {
					if (pointsI > points) {
						p.sendMessage(Utilities.getInstancia().getMessage("Geral.Kit_pontos_insuficientes")
								.replace("@points", "" + pointsI));
						p.closeInventory();
						return;
					}

					if (killsI > kills) {
						p.sendMessage(Utilities.getInstancia().getMessage("Geral.Kit_kills_insuficientes")
								.replace("@kills", "" + killsI));
						p.closeInventory();
						return;
					}

					if (winsI > wins) {
						p.sendMessage(Utilities.getInstancia().getMessage("Geral.Kit_wins_insuficientes")
								.replace("@wins", "" + winsI));
						p.closeInventory();
						return;
					}
				}
			}

			Utilities.getInstancia().setKit(p.getName(), kitName.replace(" ", "_"));
			p.closeInventory();
			p.sendMessage("§3Você escolheu o KIT: §b" + kitName + ".");
		}
	}

	@EventHandler
	public void Food(FoodLevelChangeEvent e) {
		Player p = (Player) e.getEntity();
		if (!Main.arena.players.containsKey(p.getName().toLowerCase())) {
			return;
		}

		if (Main.arena == null) {
			return;
		}
		if (Main.arena.playing) {
			return;
		}
		e.setCancelled(true);
	}

	@EventHandler
	public void DropItem(PlayerDropItemEvent e) {
		Player p = e.getPlayer();
		if (!Main.arena.players.containsKey(p.getName().toLowerCase())) {
			return;
		}

		if (Main.arena == null) {
			return;
		}
		if (Main.arena.playing) {
			return;
		}
		e.setCancelled(true);
	}
}