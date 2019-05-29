package me.Albert.SimpleQuestions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;




public class Main extends JavaPlugin implements Listener {
	private YamlConfiguration config;
	private File file = new File(this.getDataFolder() , "config.yml");;
	private String answer = null;
	private boolean cd = true;
	
	@Override
	public void onEnable(){	
		new Metrics(this);
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		org.bukkit.Bukkit.getConsoleSender().sendMessage("§b[SimpleQuestions] Loaded");
		this.saveDefaultConfig();
		this.config = YamlConfiguration.loadConfiguration(this.file);
		this.Ask();
		answer = null;
		cd = true;
		}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!new File(this.getDataFolder(),"config.yml").exists()) {
		saveResource("config.yml", true);
		sender.sendMessage("§b[SimpleQuestions] config regenerated");
		this.cancel();
		this.Ask();
		} else
		this.config = YamlConfiguration.loadConfiguration(this.file);
		sender.sendMessage("§b[SimpleQuestions] config reloaded");
		this.cancel();
		this.Ask();
		return true;
	}
	
	List<String> question = new ArrayList<String>();
	@SuppressWarnings("deprecation")
	public void Ask() {
		Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
		    @Override
		    public void run() {
		    	if (cd == true) {
		    	for(String key : config.getConfigurationSection("Questions").getKeys(false)) {
		    		question.add(key);
		    		}
		    	Random random = new Random();
		    	int n = random.nextInt(question.size());
		    	String q = (String) question.get(n);
		    	String ask = ChatColor.translateAlternateColorCodes('&', config.getString("Questions."+q+".ask"));
		    	answer = config.getString("Questions."+q+".answer");
		        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', config.getString("prefix")) + ask);
		        cd = false;
		        cancel();
		        timeOut();
		    	}
		    }
		},20L * config.getInt("delay"), 20L * config.getInt("delay")); //0 Tick initial delay, 20 Tick (1 Second) between repeats
		
		
	}
	
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		if (e.getMessage().equalsIgnoreCase(answer)) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			    @Override
			    public void run() {
			    	fetchReward(e.getPlayer());
			    	Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', config.getString("correct"))
							.replace("%player%", e.getPlayer().getName()));
			    }
			}, 10L); //20 Tick (1 Second) delay before run() is called
			answer = null;
			cd = true;
			this.Ask();
		}
		
	}
	public void cancel() {
		Bukkit.getServer().getScheduler().cancelTasks(this);
	}
	
	@SuppressWarnings("deprecation")
	public void timeOut() {
		Bukkit.getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
		    @Override
		    public void run() {
		        if (answer != null) {
		        	Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', config.getString("nobody_answer")
		        			.replace("%answer%", answer)));
		        	answer = null;
		        	cd = true;
		        	Ask();
		        }
		    }
		}, 20L *config.getInt("timeout")); //0 Tick initial delay, 20 Tick (1 Second) between repeats

	}
	
	
	public void fetchReward(Player p) {
		
		List<String> reward = new ArrayList<String>();
		int totalchance = 1;
		for (String key : config.getConfigurationSection("Rewards").getKeys(false)) {
			reward.add(key);
		}
		for (int i=0; i <reward.size();i++) {
			totalchance+=config.getInt("Rewards."+reward.get(i)+".chance");
		}
		Random random = new Random();
		int chance = random.nextInt(totalchance);
		for (String key : reward) {
			if (chance <= config.getInt("Rewards."+key+".chance")) {
				ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
				List<String> commands = config.getStringList("Rewards."+key+".commands");
				for (String cmd : commands) {
					Bukkit.dispatchCommand(console, cmd.replace("%player%", p.getName()));
				}
				break;
			} else chance -= config.getInt("Rewards."+key+".chance");
		}
		
	}
	
	
	
	
		
	

}
