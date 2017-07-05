package fr.ng.ibalix.levelmetiers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class LevelMetiers extends JavaPlugin {
	
	public Permission permission;
	public Economy econ = null;
	
	@Override
	public void onEnable() {	
		getLogger().info("Plugin LevelMetiers activé");
		
		setupPermissions();
		setupEconomy();
		
		// Génération config
		this.saveDefaultConfig();
		
		// Initialisation des Commandes
		this.getCommand("upgrade").setExecutor(new LevelMetiersCommandExecutor(this));
		this.getCommand("levelmetiersreload").setExecutor(new LevelMetiersCommandExecutor(this));
		this.getCommand("join").setExecutor(new LevelMetiersCommandExecutor(this));
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
}
