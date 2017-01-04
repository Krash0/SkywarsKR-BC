package SkywarsKR;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§7Esse comando so pode ser executado por um player.");
			return false;
		}

		Player p = (Player) sender;
		if (cmd.equalsIgnoreCase("sw")) {
			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("sair")) {
					if (Utilities.getInstancia().exitArena(p)) {
						p.sendMessage(Utilities.getInstancia().getMessage("Geral.Voce_saiu_da_partida"));
						return true;
					}
				}

				if (args[0].equalsIgnoreCase("setworld")) {
					if (!p.hasPermission("SkywarsKR.Admin") && !p.isOp()) {
						return false;
					}
					if (args.length == 2) {
						String world = args[1];
						if (Utilities.getInstancia().setWorld(p, world)) {
							p.sendMessage(Utilities.getInstancia().getMessage("Admin.Mundo_setado").replace("@arena",
									Main.arena.name));
							return true;
						}
						p.sendMessage(
								Utilities.getInstancia().getMessage("Admin.Mundo_nao_existe").replace("@mundo", world));
						return false;
					}
					p.sendMessage("§7Use: /sw setworld [world]");
					return false;
				}

				if (args[0].equalsIgnoreCase("setisland")) {
					if (!p.hasPermission("SkywarsKR.Admin") && !p.isOp()) {
						return false;
					}
					if (args.length == 1) {
						if (Utilities.getInstancia().setIsland(p)) {
							p.sendMessage(Utilities.getInstancia().getMessage("Admin.Ilha_setada"));
							return true;
						}
					}
					p.sendMessage("§7Use: /sw setisland");
					return false;
				}

				if (args[0].equalsIgnoreCase("setchests")) {
					if (!p.hasPermission("SkywarsKR.Admin") && !p.isOp()) {
						return false;
					}
					if (args.length == 1) {
						switch (Utilities.getInstancia().setChestsMode(p)) {
						case 1:
							p.sendMessage(Utilities.getInstancia().getMessage("Admin.Entrou_modo_setar_baus"));
							return true;
						case 2:
							p.sendMessage(Utilities.getInstancia().getMessage("Admin.Saiu_modo_setar_baus"));
							return true;
						}
						return false;
					}
					p.sendMessage("§7Use: /sw setchests");
					return false;
				}

				if (args[0].equalsIgnoreCase("setfeasts")) {
					if (!p.hasPermission("SkywarsKR.Admin") && !p.isOp()) {
						return false;
					}
					if (args.length == 1) {
						switch (Utilities.getInstancia().setFeastsMode(p)) {
						case 1:
							p.sendMessage(Utilities.getInstancia().getMessage("Admin.Entrou_modo_setar_feasts"));
							return true;
						case 2:
							p.sendMessage(Utilities.getInstancia().getMessage("Admin.Saiu_modo_setar_feasts"));
							return true;
						}
						return false;
					}
					p.sendMessage("§7Use: /sw setfeasts");
					return false;
				}

				if (args[0].equalsIgnoreCase("setminplayers")) {
					if (!p.hasPermission("SkywarsKR.Admin") && !p.isOp()) {
						return false;
					}
					if (args.length == 2) {
						int minPlayers = Integer.parseInt(args[1]);
						if (Utilities.getInstancia().setMinPlayers(minPlayers)) {
							p.sendMessage(Utilities.getInstancia().getMessage("Admin.Minimo_de_jogadores_setado")
									.replace("@arena", Main.arena.name).replace("@minPlayers", "" + minPlayers));
							return true;
						}
					}
					p.sendMessage("§7Use: /sw setminplayers [minplayers]");
					return false;
				}

				if (args[0].equalsIgnoreCase("criarkit")) {
					if (!p.hasPermission("SkywarsKR.Admin") && !p.isOp()) {
						return false;
					}
					if (args.length == 6) {
						String kitName = args[1];
						int points = Integer.parseInt(args[2]);
						int kills = Integer.parseInt(args[3]);
						int wins = Integer.parseInt(args[4]);
						boolean onlyForVips = false;
						if (args[5].equalsIgnoreCase("True")) {
							onlyForVips = true;
						}
						if (Utilities.getInstancia().createNewKit(p, kitName, points, kills, wins, onlyForVips)) {
							p.sendMessage(
									Utilities.getInstancia().getMessage("Admin.Kit_criado").replace("@kit", kitName));
							return true;
						}
						p.sendMessage(Utilities.getInstancia().getMessage("Admin.Error_criar_kit"));
						return false;
					}
					p.sendMessage("§7Use: /sw criarkit [nome] [pontos] [kills] [wins] [allowforvips] (false/true)");
					return false;
				}

				if (args[0].equalsIgnoreCase("addskill")) {
					if (!p.hasPermission("SkywarsKR.Admin") && !p.isOp()) {
						return false;
					}
					if (args.length == 3) {
						String kitName = args[1];
						String skill = args[2];
						if (Utilities.getInstancia().addSkillInKit(kitName, skill)) {
							p.sendMessage(Utilities.getInstancia().getMessage("Admin.Skill_adicionada")
									.replace("@kit", kitName).replace("@skill", skill));
							return true;
						}
						p.sendMessage(Utilities.getInstancia().getMessage("Admin.Error_adicionar_skill").replace("@kit",
								kitName));
						return false;
					}
					p.sendMessage("§7Use: /sw addskill [kit] [skill]");
					return false;
				}

				if (args[0].equalsIgnoreCase("ativar")) {
					if (!p.hasPermission("SkywarsKR.Admin") && !p.isOp()) {
						return false;
					}
					if (args.length == 1) {
						switch (Utilities.getInstancia().activatedArena(true)) {
						case 1:
							p.sendMessage(Utilities.getInstancia().getMessage("Admin.Precisa_setar_ilhas"));
							return true;
						case 2:
							p.sendMessage(Utilities.getInstancia().getMessage("Admin.Precisa_setar_mundo"));
							return true;
						case 3:
							p.sendMessage(Utilities.getInstancia().getMessage("Admin.Arena_ativada").replace("@arena",
									Main.arena.name));
							return true;
						}
					}
					p.sendMessage("§7Use: /sw ativar");
					return false;
				}

				if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
					if (!p.hasPermission("SkywarsKR.Admin") && !p.isOp()) {
						return false;
					}

					Main.pl.reloadConfig();
					Main.chests.reloadConfig();
					Main.feasts.reloadConfig();
					Main.messages.reloadConfig();
					Main.kits.reloadConfig();
					Main.arenaConfig.reloadConfig();
					p.sendMessage(Utilities.getInstancia().getMessage("Admin.Configs_recarregadas"));
					return true;
				}

				if (args[0].equalsIgnoreCase("help")) {
					if (!p.hasPermission("SkywarsKR.Help") && !p.hasPermission("SkywarsKR.Admin") && !p.isOp()) {
						return false;
					}
					p.sendMessage("§8[SkyWarsKR v2]");
					p.sendMessage("§7> /sw sair - sair da partida.");
					p.sendMessage("§7> /sw setworld [world] - setar o mundo da arena.");
					p.sendMessage("§7> /sw setisland - setar uma ilha na arena.");
					p.sendMessage("§7> /sw setchests - entrar no modo de setar baús.");
					p.sendMessage("§7> /sw setfeasts - entrar no modo de setar feasts.");
					p.sendMessage("§7> /sw setminplayers [quant] - setar minimo de players para iniciar a partida.");
					p.sendMessage("§7> /sw ativar - ativar uma arena.");
					p.sendMessage("§7> /sw criarkit [name] [points] [kills] [wins] - criar um kit.");
					p.sendMessage("§7> /sw reload - recarregar as configurações.");
					return true;
				}
				return false;
			}
			p.sendMessage("§7Use: /sw help");
			return true;
		}
		return false;
	}
}
