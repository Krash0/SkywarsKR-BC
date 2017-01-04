package SkywarsKR;

import java.util.HashMap;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	static JavaPlugin pl;

	static Arena arena;
	static HashMap<String, PlayerInfo> playersInfo;
	static ExtraConfig arenaConfig;

	static ExtraConfig feasts;
	static ExtraConfig chests;
	static ExtraConfig messages;
	static ExtraConfig kits;

	@Override
	public void onLoad() {
		pl = this;
		arenaConfig = new ExtraConfig(Main.pl, "arena.yml");
		feasts = new ExtraConfig(Main.pl, "feasts.yml");
		chests = new ExtraConfig(Main.pl, "chests.yml");
		messages = new ExtraConfig(Main.pl, "messages.yml");
		kits = new ExtraConfig(Main.pl, "kits.yml");
		playersInfo = new HashMap<String, PlayerInfo>();

		Utilities.getInstancia().loadArena();
	}

	@Override
	public void onEnable() {
		if (!Utilities.getInstancia().authentication()) {
			getServer().getConsoleSender().sendMessage("§7[SkyWarsKR v2] §8Invalid authentication.");
			getServer().getPluginManager().disablePlugin(this);
			getServer().shutdown();
			return;
		}
		pl = this;
		Utilities.getInstancia().createWorldsFolder();
		saveDefaultConfig();

		getServer().getPluginManager().registerEvents(new Events(), this);
		getServer().getPluginManager().registerEvents(new Skills(), this);
		getCommand("sw").setExecutor(new Commands());

		getServer().getConsoleSender().sendMessage("§7[SkyWarsKR v2] §8Activated.");
		getServer().getMessenger().registerOutgoingPluginChannel(Main.pl, "BungeeCord");
		Utilities.getInstancia().fixWorld();

		if (getConfig().getBoolean("MySQL.Enable")) {
			MySQLC.getInstancia().startMySQL();
		}

		getServer().getConsoleSender()
				.sendMessage("§7[SkyWarsKR v2] §8Database status: " + MySQLC.getInstancia().statusConnection());
	}

	@Override
	public void onDisable() {
		if (getConfig().getBoolean("MySQL.Enable")) {
			MySQLC.getInstancia().closeConnection();
		}
		Utilities.getInstancia().unloadArena();
		getServer().getConsoleSender().sendMessage("§7[SkyWarsKR v2] §8Disabled.");
	}
}