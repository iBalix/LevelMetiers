package fr.ng.ibalix.levelmetiers;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class LevelMetiers extends JavaPlugin {
	
	public Permission permission;
	public Economy econ = null;
	public FileConfiguration cooldownConfig = null;
	public File cooldownFile = null;
	
	@Override
	public void onEnable() {	
		getLogger().info("Plugin LevelMetiers activé");
		if(!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}
		
		setupPermissions();
		setupEconomy();
		
		// Génération config
		this.saveDefaultConfig();
		
		// Load data config
		this.cooldownFile = new File(this.getDataFolder(), "cooldown.yml");
        if (!cooldownFile.exists()) {
        	try {
				cooldownFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
    	this.cooldownConfig = YamlConfiguration.loadConfiguration(cooldownFile);
		
		// Initialisation des Commandes
		this.getCommand("upgrade").setExecutor(new LevelMetiersCommandExecutor(this));
		this.getCommand("levelmetiersreload").setExecutor(new LevelMetiersCommandExecutor(this));
		this.getCommand("join").setExecutor(new LevelMetiersCommandExecutor(this));
		this.getCommand("setelite").setExecutor(new LevelMetiersCommandExecutor(this));
		this.getCommand("job").setExecutor(new LevelMetiersCommandExecutor(this));
	}
	
	// Activation du plugin	
	@Override
	public void onDisable() {
		getLogger().info("Plugin LevelMetiers désactivé");		
	}
	
	private void setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        permission = rsp.getProvider();
    }
	
	private void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        econ = rsp.getProvider();
    }
	
	public void saveCooldown() {
		try {
			cooldownConfig.save(cooldownFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
