package SkywarsKR;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class Utilities {
	public static Utilities instancia;
	int countdown;

	private Utilities() {
	}

	public static Utilities getInstancia() {
		if (instancia == null) {
			instancia = new Utilities();
		}
		return instancia;
	}

	void loadArena() {
		String arenaName = Main.arenaConfig.getConfig().getString("Nome");
		Boolean activated = Main.arenaConfig.getConfig().getBoolean("Ativada");
		Integer minPlayers = Main.arenaConfig.getConfig().getInt("Minimo_de_jogadores");
		Integer Maximo_de_jogadores = Main.arenaConfig.getConfig().getInt("Maximo_de_jogadores");
		Main.arena = new Arena(arenaName, activated, minPlayers, Maximo_de_jogadores);
		String worldName = Main.arenaConfig.getConfig().getString("Mundo");
		if (worldName != null) {
			resetWorld(worldName);
		}
	}

	void unloadArena() {
		for (Player onlinePlayer : Main.pl.getServer().getOnlinePlayers()) {
			onlinePlayer.kickPlayer("§7Servidor reiniciando.");
		}
	}

	boolean deleteWorld(String worldName) {
		World world = Bukkit.getServer().getWorld(worldName);
		if (world == null) {
			return false;
		}

		File worldFile = world.getWorldFolder();
		if (!Bukkit.getServer().unloadWorld(world, false)) {
			return false;
		}

		for (File file : worldFile.listFiles()) {
			file.delete();
		}

		return true;
	}

	boolean resetWorld(String worldName) {
		File worldFile = new File(Main.pl.getDataFolder().getAbsolutePath() + "/worlds/" + worldName);

		if (!worldFile.exists()) {
			return false;
		}

		File newWorld = new File("world");

		if (newWorld.exists()) {
			if (!deleteWorld(worldName)) {
				for (File file : newWorld.listFiles()) {
					file.delete();
				}
			}
		}
		copyWorld(worldFile, newWorld);
		return true;
	}

	public void copyWorld(File source, File target) {
		try {
			ArrayList<String> ignore = new ArrayList<String>(Arrays.asList("uid.dat", "session.dat"));
			if (!ignore.contains(source.getName())) {
				if (source.isDirectory()) {
					if (!target.exists())
						target.mkdirs();
					String files[] = source.list();
					for (String file : files) {
						File srcFile = new File(source, file);
						File destFile = new File(target, file);
						copyWorld(srcFile, destFile);
					}
				} else {
					InputStream in = new FileInputStream(source);
					OutputStream out = new FileOutputStream(target);
					byte[] buffer = new byte[1024];
					int length;
					while ((length = in.read(buffer)) > 0)
						out.write(buffer, 0, length);
					in.close();
					out.close();
				}
			}
		} catch (IOException e) {

		}
	}

	void fixWorld() {
		World world = Main.pl.getServer().getWorld("world");
		world.setAutoSave(false);
		world.setMonsterSpawnLimit(0);

		world.setThunderDuration(0);
		world.setGameRuleValue("doDaylightCycle", "false");

		world.setTime(0L);

		setGlass(Material.GLASS);

		killAllMonsters(world);
		clearItens(world);
	}

	void createWorldsFolder() {
		File file = new File(Main.pl.getDataFolder().getAbsolutePath() + "/worlds/");
		if (!file.exists()) {
			file.mkdir();
		}
	}

	void start() {
		final Arena arena = Main.arena;
		if (!arena.countdown && !arena.playing) {
			arena.countdown = true;

			countdown = Main.pl.getConfig().getInt("Arena.Countdown");

			ScoreBoard(countdown);

			for (Player playerOnline : Main.pl.getServer().getOnlinePlayers()) {
				playerOnline.sendMessage(Utilities.getInstancia().getMessage("Arena.Anuncio_segundos")
						.replace("@segundos", "" + countdown));
			}

			// countdown
			new BukkitRunnable() {
				@Override
				public void run() {
					countdown = countdown - 1;
					ScoreBoard(countdown);
					if (countdown == -3) {
						arena.countdown = false;
						for (Player playerOnline : Main.pl.getServer().getOnlinePlayers()) {
							playerOnline.sendMessage(
									Utilities.getInstancia().getMessage("Arena.Acuncio_acabou_invencibilidade"));
						}
						cancel();
					}
					if (countdown == 0) {
						if (Main.arena.players.size() >= arena.minPlayers) {
							arena.playing = true;

							placeItemsInChests();
							placeItemsInFeasts();

							setGlass(Material.AIR);

							for (Player playerOnline : Main.pl.getServer().getOnlinePlayers()) {
								playerOnline.sendMessage(Utilities.getInstancia().getMessage("Arena.Anuncio_comecou"));
								playerOnline.sendMessage(
										Utilities.getInstancia().getMessage("Arena.Anuncio_invencibilidade"));
								fixPlayer(playerOnline, GameMode.SURVIVAL, true);
							}
							giveKits();
						} else {
							arena.countdown = false;

							for (Player playerOnline : Main.pl.getServer().getOnlinePlayers()) {
								playerOnline
										.sendMessage(Utilities.getInstancia().getMessage("Arena.Anuncio_nao_comecou"));
							}
						}
					}

					if (countdown <= 5 && countdown > 0) {
						for (Player playerOnline : Main.pl.getServer().getOnlinePlayers()) {
							playerOnline.sendMessage(Utilities.getInstancia().getMessage("Arena.Anuncio_segundos")
									.replace("@segundos", "" + countdown));
						}
					}
				}
			}.runTaskTimer(Main.pl, 20, 20);
		}
	}

	Player getPlayer(String playerName) {
		return Main.pl.getServer().getPlayer(playerName);
	}

	void verifWinner() {
		final Arena arena = Main.arena;
		if (!arena.finished && arena.playing) {
			if (arena.players.size() == 0) {
				arena.finished = true;
				saveStatus();
				Stop();
			}

			if (arena.players.size() == 1) {
				arena.finished = true;

				for (PlayerInGame playerInGame : arena.players.values()) {
					Player winner = getPlayer(playerInGame.name);
					String command = Main.pl.getConfig().getString("Arena.Executar_comando");
					if (command != null && !command.equals("")) {
						Bukkit.getServer().dispatchCommand(Main.pl.getServer().getConsoleSender(),
								command.replace("/", "").replace("@player", winner.getName()));
					}

					winner.sendMessage(getMessage("Geral.Jogador_ganhou"));
					editStatus(winner.getName(), 10, 0, 1);

					saveStatus();

					Fogos(winner.getLocation());
					connectInServer(winner);
					Stop();
					break;
				}
			}
		}
	}

	void Stop() {
		new BukkitRunnable() {
			@Override
			public void run() {
				Main.pl.getServer().shutdown();
			}
		}.runTaskLaterAsynchronously(Main.pl, 20 * 5L);
	}

	boolean verifStart() {
		if (Main.arena.players.size() >= Main.arena.minPlayers) {
			if (!Main.arena.countdown && !Main.arena.playing) {
				return true;
			}
		}
		return false;
	}

	boolean createNewKit(Player p, String kitName, int points, int kills, int wins, boolean OnlyForVIP) {
		if (p.getItemInHand() == null || p.getItemInHand().getType() == Material.AIR) {
			return false;
		}

		Main.kits.getConfig().set("Kits." + kitName.toLowerCase() + ".Nome", kitName);
		Main.kits.getConfig().set("Kits." + kitName.toLowerCase() + ".Icone",
				UtilitiesPKG.SerializeItemStack.serializeItemStack(p.getItemInHand()));
		Main.kits.getConfig().set("Kits." + kitName.toLowerCase() + ".Pontos", points);
		Main.kits.getConfig().set("Kits." + kitName.toLowerCase() + ".Matou_jogadores", kills);
		Main.kits.getConfig().set("Kits." + kitName.toLowerCase() + ".Vitorias", wins);
		Main.kits.getConfig().set("Kits." + kitName.toLowerCase() + ".Apenas_vips", OnlyForVIP);

		ArrayList<String> items = new ArrayList<String>();

		for (int i = 0; i < p.getInventory().getContents().length; i++) {
			ItemStack Item = p.getInventory().getContents()[i];
			if (Item != null && !Item.equals(p.getItemInHand())) {
				items.add(UtilitiesPKG.SerializeItemStack.serializeItemStack(Item));
			}
		}

		Main.kits.getConfig().set("Kits." + kitName.toLowerCase() + ".Itens", items);
		Main.kits.saveConfig();
		return true;
	}

	boolean addSkillInKit(String kitName, String skill) {
		String kit = Main.kits.getConfig().getString("Kits." + kitName.toLowerCase() + ".Nome");
		if (kit != null) {
			if (kitName.equalsIgnoreCase(kit)) {
				String skills = Main.kits.getConfig().getString("Kits." + kitName.toLowerCase() + ".Skills");
				if (skills == null) {
					skills = skill + "; ";
				} else {
					skills = skills + skill + "; ";
				}
				Main.kits.getConfig().set("Kits." + kitName.toLowerCase() + ".Skills", skills);
				Main.kits.saveConfig();
				return true;
			}
		}
		return false;
	}

	void openMenuKits(Player p) {
		Inventory kitsInventory = Bukkit.createInventory(null, 54, "§6KITS");

		if (Main.kits.getConfig().getString("Kits") != null) {
			Set<String> kits = Main.kits.getConfig().getConfigurationSection("Kits").getKeys(false);

			if (kits.size() == 0) {
				return;
			}

			for (String kitName : kits) {
				if (Main.kits.getConfig().getString("Kits." + kitName) != null) {
					String path = Main.kits.getConfig().getString("Kits." + kitName + ".Icone");
					ItemStack item = UtilitiesPKG.SerializeItemStack.deserializeItemStack(path);
					ItemMeta itemM = item.getItemMeta();
					itemM.setDisplayName(
							"§6" + Main.kits.getConfig().getString("Kits." + kitName + ".Nome").replace("_", " "));
					ArrayList<String> itemLore = new ArrayList<String>();
					int Points = Main.kits.getConfig().getInt("Kits." + kitName + ".Pontos");
					int Kills = Main.kits.getConfig().getInt("Kits." + kitName + ".Matou_jogadores");
					int Wins = Main.kits.getConfig().getInt("Kits." + kitName + ".Vitorias");
					boolean VIP = Main.kits.getConfig().getBoolean("Kits." + kitName + ".Apenas_vip");

					if (Points != 0) {
						itemLore.add(" §3Points: §b" + Points);
					}

					if (Kills != 0) {
						itemLore.add(" §3Kills: §b" + Kills);
					}

					if (Wins != 0) {
						itemLore.add(" §3Wins: §b" + Wins);
					}

					if (VIP == true) {
						itemLore.add(" §3[VIP]");
					}

					itemM.setLore(itemLore);
					item.setItemMeta(itemM);
					kitsInventory.addItem(item);
				}
			}
		}
		p.openInventory(kitsInventory);
		return;
	}

	void openKitInfo(Player p, String kitName) {
		if (Main.kits.getConfig().getString("Kits." + kitName.toLowerCase()) != null) {
			String nameKit = Main.kits.getConfig().getString("Kits." + kitName.toLowerCase() + ".Nome");
			Inventory kitInfo = Bukkit.createInventory(null, 54, "§6KIT: " + nameKit);

			ArrayList<String> items = (ArrayList<String>) Main.kits.getConfig()
					.getStringList("Kits." + kitName.toLowerCase() + ".Itens");
			for (String path : items) {
				ItemStack item = UtilitiesPKG.SerializeItemStack.deserializeItemStack(path);
				kitInfo.addItem(item);
			}
			ItemStack backItem = createItem(Material.EMERALD_BLOCK, "§6Voltar", null);
			kitInfo.setItem(45, backItem);
			p.openInventory(kitInfo);
		}
		return;
	}

	boolean setChest(Block chest) {
		Block block = chest;

		int N = 1;
		if (Main.arenaConfig.getConfig().getString("Baus") != null) {
			N = Main.arenaConfig.getConfig().getConfigurationSection("Baus").getKeys(false).size() + 1;
		}
		String location = UtilitiesPKG.SerializeLocation.serializeLocation(block.getLocation(), false);
		Main.arenaConfig.getConfig().set("Baus." + N, location);
		Main.arenaConfig.saveConfig();
		return true;
	}

	boolean setFeast(Block chest) {
		Block block = chest;

		int N = 1;
		if (Main.arenaConfig.getConfig().getString("Feasts") != null) {
			N = Main.arenaConfig.getConfig().getConfigurationSection("Feasts").getKeys(false).size() + 1;
		}

		String location = UtilitiesPKG.SerializeLocation.serializeLocation(block.getLocation(), false);
		Main.arenaConfig.getConfig().set("Feasts." + N, location);
		Main.arenaConfig.saveConfig();
		return true;
	}

	boolean setWorld(Player p, String worldName) {
		File worldFile = new File(Main.pl.getDataFolder().getAbsolutePath() + "/worlds/" + worldName);

		if (!worldFile.exists()) {
			return false;
		}

		Main.arenaConfig.getConfig().set("Mundo", worldName);
		Main.arenaConfig.saveConfig();

		Stop();

		return true;
	}

	boolean setIsland(Player p) {
		int quant = 1;

		if (Main.arenaConfig.getConfig().getString("Ilhas") != null) {
			quant = Main.arenaConfig.getConfig().getConfigurationSection("Ilhas").getKeys(false).size() + 1;
		}

		Main.arenaConfig.getConfig().set("Maximo_de_jogadores", quant);

		Location loc = p.getLocation().getBlock().getLocation().add(0.5D, 0, 0.5D);
		String location = UtilitiesPKG.SerializeLocation.serializeLocation(loc, false);
		Main.arenaConfig.getConfig().set("Ilhas." + quant, location);
		Main.arenaConfig.saveConfig();

		if (Main.arena != null) {
			Main.arena.maxPlayers = quant;
		}
		return true;
	}

	boolean setMinPlayers(int minPlayers) {
		Main.arenaConfig.getConfig().set("Minimo_de_jogadores", minPlayers);
		Main.arenaConfig.saveConfig();

		if (Main.arena != null) {
			Main.arena.minPlayers = minPlayers;
		}
		return true;
	}

	int setChestsMode(Player p) {
		if (p.hasMetadata("SWChests")) {
			p.removeMetadata("SWChests", Main.pl);
			return 1;
		}

		p.setMetadata("SWChests", new FixedMetadataValue(Main.pl, "X"));
		return 2;
	}

	int setFeastsMode(Player p) {
		if (p.hasMetadata("SWFeasts")) {
			p.removeMetadata("SWFeasts", Main.pl);
			return 1;
		}

		p.setMetadata("SWFeasts", new FixedMetadataValue(Main.pl, "X"));
		return 2;
	}

	int activatedArena(boolean activated) {
		if (activated) {
			if (Main.arenaConfig.getConfig().getString("Ilhas") == null) {
				return 1;
			}
			if (Main.arenaConfig.getConfig().getString("Mundo") == null) {
				return 2;
			}
			setGlass(Material.GLASS);
		}
		if (Main.arenaConfig == null) {
			Main.pl.getServer().getConsoleSender().sendMessage("@");
		}
		Main.arenaConfig.getConfig().set("Ativada", activated);
		if (Main.arena == null) {
			Main.pl.getServer().getConsoleSender().sendMessage("!");
		}
		Main.arena.activated = activated;
		Main.arenaConfig.saveConfig();
		return 3;
	}

	int joinInArena(Player player) {
		String playerName = player.getName().toLowerCase();

		if (!Main.arena.activated) {
			return 1;
		}

		if (Main.arena.playing) {
			return 2;
		}

		if (Main.arena.players.size() >= Main.arena.maxPlayers) {
			return 4;
		}

		int slot = getSlot();

		String[] splitStatus = getStatus(playerName).split(":");

		Main.arena.players.put(playerName, new PlayerInGame(playerName, slot));
		Main.playersInfo.put(playerName, new PlayerInfo(playerName, Integer.parseInt(splitStatus[0]),
				Integer.parseInt(splitStatus[1]), Integer.parseInt(splitStatus[2])));

		Location TP = getIsland(Main.arenaConfig.getConfig(), slot);

		if (TP != null && TP.getWorld() != null && TP.getChunk() != null) {
			TP.getWorld().loadChunk(TP.getChunk());
		}

		player.teleport(TP);

		player.sendMessage(getMessage("Geral.Voce_entrou_na_partida"));

		for (Player playerOnline : Main.pl.getServer().getOnlinePlayers()) {
			playerOnline.sendMessage(getMessage("Geral.Entrou_na_partida").replace("@player", player.getName())
					.replace("@Quant", "" + Main.arena.players.size())
					.replace("@maxPlayers", "" + Main.arena.maxPlayers));
		}

		fixPlayer(player, GameMode.ADVENTURE, true);
		giveitems(player);
		ScoreBoard(0);
		return 5;
	}

	boolean exitArena(Player player) {
		if (!Main.arena.players.containsKey(player.getName().toLowerCase())) {
			return false;
		}

		Main.arena.slots.remove(Main.arena.players.get(player.getName().toLowerCase()).slot);
		Main.arena.players.remove(player.getName().toLowerCase());

		if (!Main.arena.playing) {
			Main.playersInfo.remove(player.getName().toLowerCase());
		}

		for (Player playerOnline : Main.pl.getServer().getOnlinePlayers()) {
			playerOnline.sendMessage(getMessage("Geral.Saiu_da_partida").replace("@player", player.getName())
					.replace("@Quant", "" + Main.arena.players.size())
					.replace("@maxPlayers", "" + Main.arena.maxPlayers));
		}

		fixPlayer(player, GameMode.SURVIVAL, true);

		connectInServer(player);

		ScoreBoard(0);

		verifWinner();
		return true;
	}

	boolean playerDeath(Player playerDeath, Player playerKiller) {
		String playerDeathName = playerDeath.getName().toLowerCase();
		if (!Main.arena.players.containsKey(playerDeathName)) {
			return false;
		}

		fixPlayer(playerDeath, GameMode.SURVIVAL, false);

		// Skill extraLife
		if (Skills.getInstancia().extraLife(playerDeath)) {
			return true;
		}

		Main.arena.players.remove(playerDeathName);

		ScoreBoard(0);
		verifWinner();

		// Msg death
		if (playerKiller != null) {
			editStatus(playerKiller.getName(), 3, 1, 0);

			for (Player playerOnline : Main.pl.getServer().getOnlinePlayers()) {
				playerOnline.sendMessage(Utilities.getInstancia().getMessage("Geral.Jogador_matou_jogador")
						.replace("@assassino", playerKiller.getName()).replace("@player", playerDeath.getName()));
			}
		} else {
			for (Player playerOnline : Main.pl.getServer().getOnlinePlayers()) {
				playerOnline.sendMessage(Utilities.getInstancia().getMessage("Geral.Jogador_morreu").replace("@player",
						playerDeath.getName()));
			}
		}

		connectInServer(playerDeath);
		return true;
	}

	void connectInServer(Player p) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);

		try {
			out.writeUTF("Connect");
			if (Main.pl.getConfig().getString("BungeeCord.Lobby") == null) {
				Main.pl.getConfig().set("BungeeCord.Lobby", "Lobby");
				Main.pl.saveConfig();
			}
			out.writeUTF(Main.pl.getConfig().getString("BungeeCord.Lobby"));
		} catch (IOException ex) {

		}
		p.sendPluginMessage(Main.pl, "BungeeCord", b.toByteArray());
	}

	int getSlot() {
		Random random = new Random();
		int newRandom = random.nextInt(Main.arena.maxPlayers);

		while (Main.arena.slots.contains(newRandom + 1)) {
			newRandom = random.nextInt(Main.arena.maxPlayers);
		}
		Main.arena.slots.add(newRandom + 1);
		return newRandom + 1;
	}

	Location getIsland(FileConfiguration config, int id) {
		if (config.getString("Ilhas." + id) == null) {
			return null;
		}

		String location = config.getString("Ilhas." + id);
		Location loc = UtilitiesPKG.SerializeLocation.deserializeLocation(location, false);
		return loc;
	}

	void fixPlayer(Player p, GameMode gamemode, boolean clear) {
		p.setGameMode(gamemode);
		p.closeInventory();
		if (clear) {
			p.getInventory().clear();
			p.getInventory().setArmorContents(null);
			p.updateInventory();
		}
		p.setHealth(20);
		p.setFoodLevel(20);
		p.setExp(0F);
		p.setLevel(0);
	}

	ItemStack createItem(Material mateiral, String displayName, String lore) {
		ItemStack item = new ItemStack(mateiral);
		ItemMeta itemM = item.getItemMeta();
		itemM.setDisplayName(displayName);
		ArrayList<String> LoreItem = new ArrayList<String>();
		LoreItem.add(lore);
		itemM.setLore(LoreItem);
		item.setItemMeta(itemM);
		return item;
	}

	void giveitems(Player p) {
		p.getInventory().setItem(8, createItem(Material.BLAZE_POWDER, "§6SAIR", "§aClique para Sair"));
		p.getInventory().setItem(0, createItem(Material.EMERALD, "§6KITS", "§aClique para escolher um Kit"));
		p.updateInventory();
	}

	void killAllMonsters(World world) {
		for (LivingEntity entity : world.getLivingEntities()) {
			if (entity instanceof Monster) {
				entity.remove();
			}
		}
	}

	void Fogos(Location position) {
		final Firework f = (Firework) position.getWorld().spawn(position, Firework.class);
		final FireworkMeta fm = f.getFireworkMeta();
		fm.addEffect(FireworkEffect.builder().flicker(false).trail(true).with(FireworkEffect.Type.CREEPER)
				.withColor(Color.GREEN).withFade(Color.BLUE).build());
		fm.setPower(3);
		f.setFireworkMeta(fm);
	}

	void ScoreBoard(int time) {
		for (PlayerInGame playerInGame : Main.arena.players.values()) {
			Player player = getPlayer(playerInGame.name);
			Scoreboard statsBoard = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
			Objective stats = statsBoard.registerNewObjective("stats", "dummy");
			stats.setDisplaySlot(DisplaySlot.SIDEBAR);
			stats.setDisplayName("§6§lSkyWars");

			addLineSB(statsBoard, stats, "§3Players: §b" + Main.arena.players.size(), 0);

			if (time > 0) {
				addLineSB(statsBoard, stats, "§3Countdown: §b" + time, 1);
			}

			player.setScoreboard(statsBoard);

			// Exp info countdown
			float percentage = ((float) time / 25);
			player.setExp(percentage);
			player.setLevel(time);
		}
	}

	@SuppressWarnings("deprecation")
	public void addLineSB(Scoreboard sb, Objective obj, String linha, int score) {
		int n = linha.length();
		Score s = null;
		Team t;
		if (n <= 16) {
			s = obj.getScore(Bukkit.getOfflinePlayer(linha));
		} else if (n > 16 && n < 32) {
			t = sb.registerNewTeam("Nome-Time");
			t.setPrefix(linha.substring(0, 16));
			String nome = linha.substring(16);
			t.addPlayer(Bukkit.getOfflinePlayer(nome));
			s = obj.getScore(Bukkit.getOfflinePlayer(nome));
		} else if (n > 32) {
			t = sb.registerNewTeam("Nome-Time2");
			t.setPrefix(linha.substring(0, 16));
			t.setSuffix(linha.substring(32));
			String nome = linha.substring(16, 32);
			t.addPlayer(Bukkit.getOfflinePlayer(nome));
			s = obj.getScore(Bukkit.getOfflinePlayer(nome));
		}
		s.setScore(score);
	}

	void removeScoreBoard(Player p) {
		Scoreboard statsBoard = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
		Objective stats = statsBoard.registerNewObjective("stats", "dummy");
		stats.setDisplaySlot(DisplaySlot.SIDEBAR);
		p.setScoreboard(statsBoard);
	}

	void setGlass(Material material) {
		if (Main.arenaConfig.getConfig().getString("Ilhas") == null) {
			return;
		}
		int N = Main.arenaConfig.getConfig().getConfigurationSection("Ilhas").getKeys(false).size() + 1;

		for (int i = 1; i < N; i++) {
			Location glass = getIsland(Main.arenaConfig.getConfig(), i);
			if (glass != null) {
				glass = glass.add(0, -2, 0);
				glass.getBlock().setType(material);
			}
		}
	}

	void placeItemsInChests() {
		if (Main.arenaConfig.getConfig().getString("Baus") == null) {
			return;
		}

		int N = Main.arenaConfig.getConfig().getConfigurationSection("Baus").getKeys(false).size() + 1;

		for (int i = 1; i < N; i++) {
			if (Main.arenaConfig.getConfig().getString("Baus." + i) != null) {
				String location = Main.arenaConfig.getConfig().getString("Baus." + i);
				Location loc = UtilitiesPKG.SerializeLocation.deserializeLocation(location, false);

				Block block = loc.getBlock();
				if (!block.getType().name().toLowerCase().contains("chest")) {
					return;
				}

				Chest chest = (Chest) block.getState();
				chest.getInventory().clear();

				if (Main.chests.getConfig().getString("Itens") == null) {
					return;
				}

				ArrayList<String> items = (ArrayList<String>) Main.chests.getConfig().getStringList("Itens");
				for (String path : items) {
					ItemStack newItem = UtilitiesPKG.SerializeItemStack.deserealizeItem(path);
					if (newItem.getType() != Material.AIR) {
						chest.getInventory().addItem(newItem);
					}
				}
				chest.update();
			}
		}
	}

	void placeItemsInFeasts() {
		if (Main.arenaConfig.getConfig().getString("Feasts") == null) {
			return;
		}

		int N = Main.arenaConfig.getConfig().getConfigurationSection("Feasts").getKeys(false).size() + 1;

		for (int i = 1; i < N; i++) {
			if (Main.arenaConfig.getConfig().getString("Feasts." + i) != null) {
				String location = Main.arenaConfig.getConfig().getString("Feasts." + i);
				Location loc = UtilitiesPKG.SerializeLocation.deserializeLocation(location, false);

				Block block = loc.getBlock();
				if (!block.getType().name().toLowerCase().contains("chest")) {
					return;
				}

				Chest chest = (Chest) block.getState();
				chest.getInventory().clear();

				if (Main.feasts.getConfig().getString("Itens") == null) {
					return;
				}

				ArrayList<String> items = (ArrayList<String>) Main.feasts.getConfig().getStringList("Itens");
				for (String path : items) {
					ItemStack newItem = UtilitiesPKG.SerializeItemStack.deserealizeItem(path);
					if (newItem.getType() != Material.AIR) {
						chest.getInventory().addItem(newItem);
					}
				}
				chest.update();
			}
		}
	}

	void clearItens(World world) {
		for (Entity entity : world.getEntities()) {
			entity.remove();
		}
	}

	String getStatus(String playerName) {
		if (Main.pl.getConfig().getBoolean("MySQL.Enable")) {
			HashMap<String, String> playersInDatabase = MySQLC.getInstancia()
					.getStatusHashMap("SELECT * FROM players WHERE `Name` LIKE '%" + playerName + "%'");
			if (playersInDatabase.containsKey(playerName.toLowerCase())) {
				return playersInDatabase.get(playerName.toLowerCase());
			}
		}
		return "0:0:0";
	}

	void editStatus(String playerName, int points, int kills, int wins) {
		if (!Main.pl.getConfig().getBoolean("MySQL.Enable")) {
			return;
		}
		if (!Main.arena.players.containsKey(playerName.toLowerCase())) {
			return;
		}

		int pointsP = Main.playersInfo.get(playerName.toLowerCase()).points;
		int killsP = Main.playersInfo.get(playerName.toLowerCase()).kills;
		int winsP = Main.playersInfo.get(playerName.toLowerCase()).wins;

		Main.playersInfo.get(playerName.toLowerCase()).points = pointsP + (points);
		Main.playersInfo.get(playerName.toLowerCase()).kills = killsP + (kills);
		Main.playersInfo.get(playerName.toLowerCase()).wins = winsP + (wins);
	}

	void saveStatus() {
		if (!Main.pl.getConfig().getBoolean("MySQL.Enable")) {
			return;
		}
		String querySearch = "";

		for (PlayerInfo playerInfo : Main.playersInfo.values()) {
			String playerName = playerInfo.name;
			if (querySearch.equals("")) {
				querySearch = "SELECT * FROM players WHERE `Name` = '" + playerName + "'";
			} else {
				querySearch = querySearch + " OR `Name` = '" + playerName + "'";
			}
		}

		HashMap<String, String> playersInDatabase = MySQLC.getInstancia().getStatusHashMap(querySearch);

		ArrayList<String> queryUpdate = new ArrayList<String>();
		String queryInsert = "";

		for (PlayerInfo playerInfo : Main.playersInfo.values()) {
			String player = playerInfo.name;

			Integer Points = Main.playersInfo.get(player).points;
			Integer Kills = Main.playersInfo.get(player).kills;
			Integer Wins = Main.playersInfo.get(player).wins;

			if (playersInDatabase.containsKey(player.toLowerCase())) {
				queryUpdate.add("UPDATE `players` SET `Points` = '" + Points + "', `Kills` = '" + Kills
						+ "', `Wins` = '" + Wins + "' WHERE `Name` = '" + player + "'");
			} else {
				if (queryInsert.equals("")) {
					queryInsert = "INSERT INTO players (ID, Name, Points, Kills, Wins) VALUES (NULL, '" + player
							+ "', '0', '0', '0')";
				} else {
					queryInsert = queryInsert + ", (NULL, '" + player + "', '" + Points + "', '" + Kills + "', '" + Wins
							+ "')";
				}
			}
		}

		if (queryUpdate.size() != 0) {
			for (String query : queryUpdate) {
				Bukkit.getConsoleSender().sendMessage(query);
				MySQLC.getInstancia().executeUpdateMySQL(query);
			}
		}

		if (!queryInsert.equals("")) {
			Bukkit.getConsoleSender().sendMessage(queryInsert);
			MySQLC.getInstancia().executeUpdateMySQL(queryInsert);
		}
	}

	void giveKits() {
		for (PlayerInGame playerInGame : Main.arena.players.values()) {
			Player player = getPlayer(playerInGame.name);
			String kitName = getKit(playerInGame.name);
			if (kitName != null) {
				if (Main.kits.getConfig().getString("Kits." + kitName) != null) {
					int Points = Main.kits.getConfig().getInt("Kits." + kitName + ".Pontos");
					editStatus(player.getName().toLowerCase(), -Points, 0, 0);
					ArrayList<String> items = (ArrayList<String>) Main.kits.getConfig()
							.getStringList("Kits." + kitName + ".Itens");
					for (String path : items) {
						ItemStack item = UtilitiesPKG.SerializeItemStack.deserializeItemStack(path);
						player.getInventory().addItem(item);
					}
					player.updateInventory();
				}
			}
		}
	}

	void setKit(String playerName, String kitName) {
		Main.playersInfo.get(playerName.toLowerCase()).kitName = kitName.toLowerCase();
		getPlayer(playerName).removeMetadata("SWKitSkill", Main.pl);
	}

	String getKit(String playerName) {
		return Main.playersInfo.get(playerName.toLowerCase()).kitName;
	}

	String getSkills(String playerName) {
		String kitName = getKit(playerName.toLowerCase()).toLowerCase();
		if (kitName == null) {
			return null;
		}

		String skills = Main.kits.getConfig().getString("Kits." + kitName + ".Skills");

		if (skills == null) {
			return null;
		}
		return skills;
	}

	/*
	 * String debugString(String txt){ byte[] bytes = txt.getBytes(); try {
	 * return new String(bytes, "UTF-8"); } catch (UnsupportedEncodingException
	 * e) { return txt; } }
	 */

	String getMessage(String path) {
		String tag = Main.messages.getConfig().getString("Tag").replace("&", "§");
		String message = "Mensagem não encontrada.";
		if (Main.messages.getConfig().getString("Mensagens." + path) != null) {
			message = Main.messages.getConfig().getString("Mensagens." + path).replace("&", "§");
		}
		return tag + " " + message;
	}

	Boolean authentication() {
		//Today not
		return true;
	}
}