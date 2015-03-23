//
// https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/InventoryView.html
// https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/inventory/InventoryType.html
//

package che;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;



public class main 
extends JavaPlugin
implements Listener
{
	public String Prefix = "[TMI-FIX] ";
	//private File cfgFile;
	//private FileConfiguration cfg;
	private FileConfiguration config = null;
	private File configFile = null;
	
	private Boolean warnAdmin = true;
	private Boolean warnUsers = false;
	private Boolean Act_InvClose = false;
	private String Act_CMDS = "kick <player> No dupe !;say Dirty work complete...";
	//
	public void logToCnsole(String _msg){
		System.out.println(Prefix + _msg);
	}
	

	public void logToFile(String message)
    {
         try
        {
            File dataFolder = getDataFolder();
            if(!dataFolder.exists())
            {
                dataFolder.mkdir();
            }
             File saveTo = new File(getDataFolder(), "dupe.log");
            if (!saveTo.exists())
            {
                saveTo.createNewFile();
            }
 
            FileWriter fw = new FileWriter(saveTo, true);
             PrintWriter pw = new PrintWriter(fw);
             //Date CurrentDate = Calendar.getInstance().getTime();
             Date CurrentDate = new Date();
             SimpleDateFormat format1 = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
             
             pw.println("[" + format1.format(CurrentDate) + "] " + message);
             pw.flush();
             pw.close();
         } catch (IOException e)
        {
             e.printStackTrace();
         }
     }
	
	
	  public void onEnable()
	  {
		logToCnsole("=== TMI BugFix is enabled !");
	    getServer().getPluginManager().registerEvents(this, this);
	    
	    //this.cfgFile = new File(getDataFolder(), "config.yml");
	    //this.cfg = YamlConfiguration.loadConfiguration(this.cfgFile);
	    
	    loadCfg();
	    saveCfg();
	    
	  }
	  
	  public void onDisable()
	  {
		  logToCnsole("=== TMI BugFix is disabled!");
	  }
	
	  //
	  //
	  //
	  public boolean onCommand(CommandSender sender, Command _cmd, String cmdlabel, String[] args)
	  {
	    if (_cmd.getName().equalsIgnoreCase("tmifix"))
	    {
	    	if ((args.length == 1) && (args[0].equals("reload")))
	    	{ 	
	    		this.reloadConfig();
	    		loadCfg();
	    	    logToCnsole("config reloaded !");
	    	};
	    	return true;
	    }
	    
	    return false;
	  }
	  
	  
	  
	  private String GetInvName( String _i)
	  {
		  String _ret = "";
		switch(_i) {
			case "ANVIL": _ret = "наковальню"; break;
			case "BEACON": _ret = "маяк"; break;
			case "BREWING": _ret = "варочную стойку"; break;
			case "CHEST": _ret = "сундук"; break;
			case "CRAFTING": _ret = "инвентарь содания"; break;
			case "CREATIVE": _ret = "инвентарь креатив-режима"; break;
			case "DISPENSER": _ret = "раздатчик"; break;
			case "DROPPER": _ret = "выбрасыватель"; break;
			case "ENCHANTING": _ret = "стол зачарований"; break;
			case "ENDER_CHEST": _ret = "ендер-сундук"; break;
			case "FURNACE": _ret = "печку"; break;
			case "HOPPER": _ret = "воронку"; break;
			case "MERCHANT": _ret = "жителя"; break;
			case "PLAYER": _ret = "игрока"; break;
			case "WORKBENCH": _ret = "верстак"; break;
		}
		  
		return _ret;
	  }
	  

	  @EventHandler
	  public void onGameModeChange(PlayerGameModeChangeEvent e){
		  final Player p = e.getPlayer();
		  
		  // имеем право на обход контроля ? валим отсюдова
		  if (p.hasPermission("tmifix.bypass")) return;
		  
		  // а вот и хуй.. мы есть бомжЪ
		  
		  if(e.getNewGameMode() != GameMode.SURVIVAL) {
			  logToCnsole(p.getName()+ " change gamemode to " + e.getNewGameMode());
				Bukkit.getScheduler().runTaskLater(this, new Runnable() {
					public void run() {

						InventoryView _inv = p.getOpenInventory();
						String _invType = _inv.getType().toString().toUpperCase();
						logToCnsole(p.getName() + " opened inventory [" + _invType + "]");
						
						if ( _invType !="CREATIVE") { // dupe detected
							String ConsoleAlert =  p.getName() +
									" (" + p.getLocation().getWorld().getName() + " " +
										(int)p.getLocation().getX() + " " +
										(int)p.getLocation().getY() + " " +
										(int)p.getLocation().getZ() + ") " +
										": " + _invType;

							String ChatAlert =  p.getName() + " пытается дюпать через " + GetInvName(_invType) + " !";
							String DupePosition = 
									"(" + p.getLocation().getWorld().getName() + " " + 
										(int)p.getLocation().getX() + " " +
									    (int)p.getLocation().getY() + " " +
									    (int)p.getLocation().getZ() + ")";
							
							logToCnsole("DUPE !! "+ ConsoleAlert );
							logToFile(ConsoleAlert);

							// закрываем инвентарь
							if (Act_InvClose) {
								p.closeInventory();	
							}
							
							// выполняем команду
							if (Act_CMDS.length()>2) {
								//ActionCMD
								String[] cmds = Act_CMDS.split(";");
								if (cmds.length > 0) {
									for (String cmd : cmds) {
										cmd = cmd.replace("<player>", p.getName());
										Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
									}
								}
							}
							
							// оповещаем игроков о том, что обнаружен гондон
							if (warnUsers) { 
								Bukkit.broadcastMessage(ChatColor.RED + "АХТУНГ! " + ChatAlert );
								// издадим страшный звук...
								for (Player pl_ : Bukkit.getOnlinePlayers()) {
										pl_.getWorld().playSound(pl_.getLocation(),Sound.BLAZE_DEATH,1, 0);
									}
								return;
							}
							
							// оповещаем админов
							if (warnAdmin) { 
								Bukkit.broadcast(ChatColor.RED + "АХТУНГ! " + ChatAlert + " " + DupePosition, "tmifix.alert" ); 
							}

							
						}
						
					}
				}, 20L);
			  
			  
		  }
	  }
	  
	  
	  
	  
	  
	  private void loadCfg()
	  {
		  File configFile = new File(this.getDataFolder() , "config.yml");
		  config = YamlConfiguration.loadConfiguration(configFile);
		  
		logToCnsole("loading config...");
		warnAdmin = this.config.getBoolean("Warn.Admin", true);
		warnUsers = this.config.getBoolean("Warn.Users", false);
		Act_InvClose = this.config.getBoolean("Action.InvClose", false);
		Act_CMDS = this.config.getString("Action.Commands", Act_CMDS);
		//
			System.out.println(" >> warnAdmin: " + warnAdmin);
			System.out.println(" >> warnUsers: " + warnUsers);
			System.out.println(" >> Act_InvClose: " + Act_InvClose);
			System.out.println(" >> Act_CMDS: " + Act_CMDS);
		
	  }
	  
	  private void saveCfg()
	  {
		   	configFile = new File(this.getDataFolder(),"config.yml");
	        config = YamlConfiguration.loadConfiguration(configFile);
		//this.cfg.set("#", "=== TMI Bug Fix ====");
		//this.cfg.set("#", "Warn.Admin - send warning for admins with tmifix.alert permission");
		//this.cfg.set("#", "Warn.Users - Broadcast all users about some one dupping");
		this.config.set("Warn.Admin", Boolean.valueOf(warnAdmin));
	    this.config.set("Warn.Users", Boolean.valueOf(warnUsers));
	    this.config.set("Action.InvClose", Boolean.valueOf(Act_InvClose));
	    this.config.set("Action.Commands", String.valueOf(Act_CMDS));
	    try
	    {
	      this.config.save(this.configFile); 
	      logToCnsole("creating new config...");
	    }
	    catch (Exception localException) {}
	  }
	
}
