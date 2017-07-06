package fr.ng.ibalix.levelmetiers;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.*;

public class LevelMetiersCommandExecutor implements CommandExecutor {

	private final LevelMetiers p;
	
	public LevelMetiersCommandExecutor(LevelMetiers p) {
		this.p = p;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {	
		
		/*
		 * Commande upgrade
		 * Check si le joueur doit upgrade
		 */
		if(cmd.getName().equalsIgnoreCase("upgrade")) {
			if(sender instanceof Player) {
				Player player = (Player) sender;	
				if(!player.isOp()) {
					if(args.length > 0) {
						
						String metierArg = args[0];
						
						if(hasMetier(player, metierArg)) {
							String metier = metierArg;
							
							int level = getLevel(player, metier);
							
							doUpgrade(player, metier, level);
						} else {
							player.sendMessage(ChatColor.RED + "Vous n'avez pas ce métier !");
						}
					} else {
						player.sendMessage(ChatColor.RED + "Précisez le métier que vous souhaitez upgrade, exemple: /upgrade mineur");
					}
				} else {
					player.sendMessage(ChatColor.RED + "Les OPs ne peuvent pas executer cette commande !");
				}
			}
			return true;
			// RELOAD
		} else if(cmd.getName().equalsIgnoreCase("levelmetiersreload")) {
			sender.sendMessage(ChatColor.GREEN + "La config a bien été reload !!");
			p.reloadConfig();
			return true;
			// JOIN
		} else if(cmd.getName().equalsIgnoreCase("join")) {
			if(sender instanceof Player) {
				Player player = (Player) sender;
				if(!player.isOp()) {
					if(args.length > 0) {
						String metierActuel = getMetier(player);						
						String metierNew = args[0];	
						
						if(!metierActuel.equals("aucun")) {
							if(!hasMetier(player, metierNew)) {
								if(getLevel(player, metierActuel) >= 3) {
									doChange(player, metierActuel, metierNew);
								} else {
									player.sendMessage(ChatColor.RED + "Vous devez atteindre le niveau 3 de votre métier actuel pour changer de métier !");
								}
							} else {
								player.sendMessage(ChatColor.RED + "Vous êtes déjà un "+metierActuel);
							}
						} else {	
							p.permission.playerAddGroup(player, metierNew);
							player.sendMessage(ChatColor.GREEN + "Vous êtes maintenant un "+metierNew);
						}
					} else {
						player.sendMessage(ChatColor.RED + "Précisez le métier que vous souhaitez acquérir, exemple: /join mineur");
					}
				} else {
					player.sendMessage(ChatColor.RED + "Les OPs ne peuvent pas executer cette commande !");
				}
			}
			return true;
		}		
		return false;
	} 

	private boolean hasMetier(Player player, String metierArg) {
		if(existMetier(metierArg.toLowerCase())) {
			Set<String> levels = p.getConfig().getConfigurationSection("metiers."+metierArg).getKeys(false);
			
			if(p.permission.playerInGroup(player, metierArg)) {
				return true;
			}
			
			for(String level : levels) {
				if(p.permission.playerInGroup(player, metierArg+level)) {
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}

	private void doChange(Player player, String metierActuel, String metierNew) {
		
		if(existMetier(metierNew)) {
			
			int levelOld = getLevel(player, metierActuel);
			
			if(player.hasPermission("levelmetiers.elite") && levelOld == 5) {
				player.sendMessage(ChatColor.GOLD + "Grâce à votre grade Elite vous conservez vos métier de niveau 5 !");
			} else {
				p.permission.playerRemoveGroup(player, metierActuel+levelOld);
			}
			
			int level = getLevel(player, metierNew);
			
			if(level > 1) {
				p.permission.playerAddGroup(player, metierNew + level);
				player.sendMessage(ChatColor.GREEN + "Vous êtes maintenant un "+metierNew+" de niveau "+level);
			} else {
				p.permission.playerAddGroup(player, metierNew);
				player.sendMessage(ChatColor.GREEN + "Vous êtes maintenant un "+metierNew);
			}
		} else {
			player.sendMessage(ChatColor.RED + "Ce métier n'existe pas !");
		}
	}

	private boolean existMetier(String metierNew) {
		Set<String> metiers = p.getConfig().getConfigurationSection("metiers").getKeys(false);
		
		for(String metier : metiers) {
			if(metier.toLowerCase().equals(metierNew.toLowerCase())) {	
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	private void doUpgrade(Player player, String metier, int level) {
		
		int levelUp = level+1;
		
		String output = ChatColor.GOLD + "--- Vérification de votre métier en cours ---\n";
		output += ChatColor.YELLOW + "Vous êtes actuellement "+metier+" de niveau "+level+"\n";
		
		if(p.getConfig().getList("metiers."+metier+"."+levelUp) != null) {
			
			List conditions = p.getConfig().getList("metiers."+metier+"."+levelUp);
			
			PlayerInventory inventory = player.getInventory();
			output += ChatColor.GOLD + "--- Vérification des conditions ---\n";
			
			boolean res = true;
			
			List<ItemStack> items = new ArrayList<ItemStack>();
			
			for (int i = 0; i < conditions.size(); i++) {
				
				String[] data = conditions.get(i).toString().split(":");
				
				int id = Integer.parseInt(data[0]);
				int meta = Integer.parseInt(data[1]);
				int qt = Integer.parseInt(data[2]);
				String nom = "";
				
				ItemStack item = new ItemStack(Material.getMaterial(id), qt, (byte)meta);
				
				items.add(item);
				
				if(data.length > 3) {
					nom = data[3];
				} else {
					nom = item.getData().getItemType().toString();
				}
				
				if(inventory.containsAtLeast(item, qt)) {
					output += ChatColor.GREEN + "OK --> " + qt + "x " + nom + "\n";
				} else {
					output += ChatColor.RED + "NO --> " + qt + "x " + nom + "\n";
					res = false;
				}
			}
			
			if(res) {
				
				for(ItemStack is : items) {
					inventory.removeItem(is);
				}				
				output += ChatColor.DARK_GREEN + "Félicitations vous passez au niveau "+levelUp+" !\n";
				
				int montant = 0;
				
				if(player.hasPermission("levelmetiers.elite")) {
					montant = levelUp * 1000;
				} else {
					montant = levelUp * 500;
				}
				double montantDouble = Double.parseDouble(montant + "");
				EconomyResponse r = p.econ.depositPlayer(player.getName(), montantDouble);
	            if(r.transactionSuccess()) {
	            	output += ChatColor.DARK_GREEN + "Vous venez de recevoir " + p.econ.format(r.amount) + "$";
	            } else {
	            	output += String.format("Erreur: %s", r.errorMessage);
	            }
				
				if(level != 1) {
					p.permission.playerRemoveGroup(player, metier+level);
					p.permission.playerRemove(player, "levelmetiers."+metier+"."+level);
				} else {
					p.permission.playerRemoveGroup(player, metier);
				}
				
				p.permission.playerAddGroup(player, metier+levelUp+"");
				p.permission.playerAdd(player, "levelmetiers."+metier+"."+levelUp);
				
			} else {
				output += ChatColor.DARK_RED + "Il vous manque encore des ressources !";
			}
			
			
		} else {
			output += ChatColor.GOLD + "Vous avez atteint le niveau maximum !\n";
		}
		
		player.sendMessage(output);
	}

	private int getLevel(Player player, String metier) {
		Set<String> levels = p.getConfig().getConfigurationSection("metiers."+metier).getKeys(false);
		
		List list = new ArrayList(levels);
		Collections.sort(list, Collections.reverseOrder());
		Set<String> levelsInverse = new LinkedHashSet(list);
		
		for(String level : levelsInverse) {
			if(player.hasPermission("levelmetiers."+metier+"."+level)) {
				int levelInt = Integer.parseInt(level);
				return levelInt;
			}
		}
		return 1;
	}

	private String getMetier(Player player) {
		Set<String> metiers = p.getConfig().getConfigurationSection("metiers").getKeys(false);
		
		for(String metier : metiers) {	
			if(player.hasPermission("levelmetiers."+metier)) {	
				return metier;
			}
		}			
		return "aucun";
	}
}
