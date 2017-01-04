package SkywarsKR;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ExtraConfig {
	JavaPlugin m = null;
	String configName;

	public ExtraConfig(JavaPlugin m, String configName) {
		this.m = m;
		this.configName = configName;
		setupConfig();
	}

	public FileConfiguration userfile;
	private File userfiled;

	public Plugin getPlugin() {
		return m;
	}

	public void reloadConfig() {
		setupConfig();
		return;
	}

	public File getDataFolder() {
		return getPlugin().getDataFolder();
	}

	public void updateConfig() {
		saveConfig();
	}

	public void reloadMe() {
		reloadConfig();
	}

	public void setupConfig() {
		if (!m.getDataFolder().exists()) {
			m.getDataFolder().mkdir();
		}
		this.userfiled = new File(m.getDataFolder().getAbsolutePath(), configName);

		if (configName.contains("/")) {
			String[] splitString = configName.split("/");
			File dir = new File(userfiled.getAbsolutePath().replace(splitString[splitString.length - 1], ""));
			if (!dir.exists()) {
				dir.mkdir();
			}
		}

		if (!this.userfiled.exists()) {
			try {
				if (m.getResource(configName) != null) {
					m.saveResource(configName, true);
				} else {
					this.userfiled.createNewFile();
				}
			} catch (IOException e) {
				Bukkit.getConsoleSender().sendMessage("§cNão foi possível criar o " + configName + "!");
			}
		}
		this.userfile = YamlConfiguration.loadConfiguration(this.userfiled);
	}

	public FileConfiguration getConfig() {
		return this.userfile;
	}

	public void saveConfig() {
		try {
			this.userfile.save(this.userfiled);
		} catch (Exception e) {
			Bukkit.getConsoleSender().sendMessage("§cNão foi possível salvar o " + configName + "!");
		}
	}
}