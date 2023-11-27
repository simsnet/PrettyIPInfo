package in.glennjam.PrettyIPInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.md_5.bungee.api.ChatColor;

public class PrettyIPInfo extends JavaPlugin implements Listener {

	public HashMap<String, Long> cooldowns = new HashMap<String, Long>();

	@Override
	public void onEnable() {
		// Save the default config if it doesn't exist
		this.saveDefaultConfig();
		
		// Register the player join listener
		getServer().getPluginManager().registerEvents(this, this);
		
		// Tell console that the plugin has been enabled
		getLogger().info("IPINFO >>> Plugin is enabled.");
		
		// Warn console that the IPInfo token hasn't been added
		if (this.getConfig().getString("token") == null) {
			getLogger().warning("IPINFO >>> Your IPInfo token has not been set!  Please review the config for more info.");
		}
		
		// Tell console that player IPs will not be saved since config option was set to false
		if (this.getConfig().getBoolean("save-ip-on-join") == true) {
			getLogger().info("IPINFO >>> Player IP addresses will be saved upon joining.");
			getLogger().info("IPINFO >>> To change this, set \"save-ip-on-join\" to FALSE in the configuration.");
		} else {
			getLogger().info("IPINFO >>> Player IP addresses will not be saved upon joining.");
			getLogger().info("IPINFO >>> To change this, set \"save-ip-on-join\" to TRUE in the configuration.");
		}
	}

	@Override
	public void onDisable() {
		getLogger().info("IPINFO >>> Problems?  Suggestions?  DM @Simsnet#1754 on Discord!");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("ipinfo")) {
			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("player")) {
					if (args.length > 1) {
						if (this.getServer().getPlayer(args[1]) != null) {
							// Start process
							sender.sendMessage("Please wait...");
							sender.sendMessage("");
							long start = System.currentTimeMillis();

							// Get IPInfo token from config.yml
							final String token = this.getConfig().getString("token");

							// Get player IP as a string variable
							final Player p = Bukkit.getServer().getPlayer(args[1]);
							String pip = p.getAddress().getHostString();

							// Start connection
							try {
								// Make the URL
								URL url = new URL("https://ipinfo.io/" + pip + "/json?token=" + token);

								// Make the connection
								HttpURLConnection conn = (HttpURLConnection) url.openConnection();

								// Read the data from the connection
								BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

								// Init String "data", build the full response output since we have to do it line-by-line
								String data = null;
								StringBuilder response = new StringBuilder();
								while ((data = in.readLine()) != null) {
									response.append(data);
								}

								// Init string "responseString" and convert the response object to a string
								String responseString = response.toString();

								// Init Gson and convert the JSON string to individual objects that can be queried as strings
								Gson gson = new Gson();
								JsonObject jsonobject = gson.fromJson(responseString, JsonObject.class);

								// Print output
								sender.sendMessage(ChatColor.AQUA + "Player: " + ChatColor.GREEN + p.getName());

								if (jsonobject.get("ip") != null) {
									sender.sendMessage(ChatColor.AQUA + "IP: " + ChatColor.GREEN + jsonobject.get("ip").getAsString());
								}

								if (jsonobject.get("hostname") != null) {
									sender.sendMessage(ChatColor.AQUA + "Hostname: " + ChatColor.GREEN + jsonobject.get("hostname").getAsString());
								}

								if (jsonobject.get("city") != null) {
									sender.sendMessage(ChatColor.AQUA + "City: " + ChatColor.GREEN + jsonobject.get("city").getAsString());
								}
								if (jsonobject.get("region") != null) {
									sender.sendMessage(ChatColor.AQUA + "State/Region: " + ChatColor.GREEN + jsonobject.get("region").getAsString());
								}

								if (jsonobject.get("country") != null) {
									sender.sendMessage(ChatColor.AQUA + "Country: " + ChatColor.GREEN + jsonobject.get("country").getAsString());
								}

								if (jsonobject.get("loc") != null) {
									sender.sendMessage(ChatColor.AQUA + "Coordinates: " + ChatColor.GREEN + jsonobject.get("loc").getAsString());
								}

								if (jsonobject.get("org") != null) {
									sender.sendMessage(ChatColor.AQUA + "ISP (Organization): " + ChatColor.GREEN + jsonobject.get("org").getAsString());
								}

								if (jsonobject.get("postal") != null) {
									sender.sendMessage(ChatColor.AQUA + "Postal Code: " + ChatColor.GREEN + jsonobject.get("postal").getAsString());
								}
								if (jsonobject.get("timezone") != null) {
									sender.sendMessage(ChatColor.AQUA + "Timezone: " + ChatColor.GREEN + jsonobject.get("timezone").getAsString());
								}

								// Finish process
								in.close();
								long finish = System.currentTimeMillis();
								long ms = finish - start;
								sender.sendMessage("");
								sender.sendMessage("Complete! (took " + ms + "ms)");
								Bukkit.getLogger().info("[PrettyIPInfo Logger] " + sender.getName() + " requested information for player " + p.getName().toString() + " (" + pip + ").");
							}
							catch (IOException e) {
								sender.sendMessage(e.toString());
							}
							return true;
						} else if (this.getServer().getPlayer(args[1]) == null) {
							sender.sendMessage(ChatColor.RED + "That player is not online.  Attempting to use saved IP.");
							if (this.getConfig().getString("ips." + args[1]) != null) {
								// Start process
								sender.sendMessage("Please wait...");
								sender.sendMessage("");
								long start = System.currentTimeMillis();

								// Get IPInfo token from config.yml
								final String token = this.getConfig().getString("token");

								// Get saved player IP from the config file
								String pip = this.getConfig().getString("ips." + args[1]);

								// Start connection
								try {
									// Make the URL
									URL url = new URL("https://ipinfo.io/" + pip + "/json?token=" + token);

									// Make the connection
									HttpURLConnection conn = (HttpURLConnection) url.openConnection();

									// Read the data from the connection
									BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

									// Init String "data", build the full response output since we have to do it line-by-line
									String data = null;
									StringBuilder response = new StringBuilder();
									while ((data = in.readLine()) != null) {
										response.append(data);
									}

									// Init string "responseString" and convert the response object to a string
									String responseString = response.toString();

									// Init Gson and convert the JSON string to individual objects that can be queried as strings
									Gson gson = new Gson();
									JsonObject jsonobject = gson.fromJson(responseString, JsonObject.class);

									// Print output
									sender.sendMessage(ChatColor.AQUA + "Player: " + ChatColor.GREEN + args[1]);

									if (jsonobject.get("ip") != null) {
										sender.sendMessage(ChatColor.AQUA + "IP: " + ChatColor.GREEN + jsonobject.get("ip").getAsString());
									}

									if (jsonobject.get("hostname") != null) {
										sender.sendMessage(ChatColor.AQUA + "Hostname: " + ChatColor.GREEN + jsonobject.get("hostname").getAsString());
									}

									if (jsonobject.get("city") != null) {
										sender.sendMessage(ChatColor.AQUA + "City: " + ChatColor.GREEN + jsonobject.get("city").getAsString());
									}
									if (jsonobject.get("region") != null) {
										sender.sendMessage(ChatColor.AQUA + "State/Region: " + ChatColor.GREEN + jsonobject.get("region").getAsString());
									}

									if (jsonobject.get("country") != null) {
										sender.sendMessage(ChatColor.AQUA + "Country: " + ChatColor.GREEN + jsonobject.get("country").getAsString());
									}

									if (jsonobject.get("loc") != null) {
										sender.sendMessage(ChatColor.AQUA + "Coordinates: " + ChatColor.GREEN + jsonobject.get("loc").getAsString());
									}

									if (jsonobject.get("org") != null) {
										sender.sendMessage(ChatColor.AQUA + "ISP (Organization): " + ChatColor.GREEN + jsonobject.get("org").getAsString());
									}

									if (jsonobject.get("postal") != null) {
										sender.sendMessage(ChatColor.AQUA + "Postal Code: " + ChatColor.GREEN + jsonobject.get("postal").getAsString());
									}
									if (jsonobject.get("timezone") != null) {
										sender.sendMessage(ChatColor.AQUA + "Timezone: " + ChatColor.GREEN + jsonobject.get("timezone").getAsString());
									}

									// Finish process
									in.close();
									long finish = System.currentTimeMillis();
									long ms = finish - start;
									sender.sendMessage("");
									sender.sendMessage("Complete! (took " + ms + "ms)");
									Bukkit.getLogger().info("[PrettyIPInfo Logger] " + sender.getName() + " requested information for offline player " + args[1] + " (" + pip + ").");
								}
								catch (IOException e) {
									sender.sendMessage(e.toString());
								}
								return true;
							} else if (this.getConfig().getString("ips." + args[1]) == null) {
								sender.sendMessage(ChatColor.RED + "That player has never joined the server.");
								Bukkit.getLogger().info("[PrettyIPInfo Logger] " + sender.getName() + " specified a player that hasn't joined the server.");
								return true;
							}
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Please enter a player name.");
						Bukkit.getLogger().info("[PrettyIPInfo Logger] " + sender.getName() + " did not enter a player name.");
						return true;
					}
				}

				if (args[0].equalsIgnoreCase("ip")) {
					if (args.length > 1) {
						String ip = args[1];
						String regex = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$";

						Pattern regexpattern = Pattern.compile(regex);
						Matcher matcher = regexpattern.matcher(ip);

						if (matcher.find()) {
							// Start process
							sender.sendMessage("Please wait...");
							sender.sendMessage("");
							long start = System.currentTimeMillis();

							// Get IPInfo token from config.yml
							final String token = this.getConfig().getString("token");

							// Start connection
							try {
								// Make the URL
								URL url = new URL("https://ipinfo.io/" + ip + "/json?token=" + token);

								// Make the connection
								HttpURLConnection conn = (HttpURLConnection) url.openConnection();

								// Read the data from the connection
								BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

								// Init String "data", build the full response output since we have to do it line-by-line
								String data = null;
								StringBuilder response = new StringBuilder();
								while ((data = in.readLine()) != null) {
									response.append(data);
								}

								// Init string "responseString" and convert the response object to a string
								String responseString = response.toString();

								// Init Gson and convert the JSON string to individual objects that can be queried as strings
								Gson gson = new Gson();
								JsonObject jsonobject = gson.fromJson(responseString, JsonObject.class);

								// Print output
								if (jsonobject.get("ip") != null) {
									sender.sendMessage(ChatColor.AQUA + "IP: " + ChatColor.GREEN + jsonobject.get("ip").getAsString());
								}

								if (jsonobject.get("hostname") != null) {
									sender.sendMessage(ChatColor.AQUA + "Hostname: " + ChatColor.GREEN + jsonobject.get("hostname").getAsString());
								}

								if (jsonobject.get("city") != null) {
									sender.sendMessage(ChatColor.AQUA + "City: " + ChatColor.GREEN + jsonobject.get("city").getAsString());
								}
								if (jsonobject.get("region") != null) {
									sender.sendMessage(ChatColor.AQUA + "State/Region: " + ChatColor.GREEN + jsonobject.get("region").getAsString());
								}

								if (jsonobject.get("country") != null) {
									sender.sendMessage(ChatColor.AQUA + "Country: " + ChatColor.GREEN + jsonobject.get("country").getAsString());
								}

								if (jsonobject.get("loc") != null) {
									sender.sendMessage(ChatColor.AQUA + "Coordinates: " + ChatColor.GREEN + jsonobject.get("loc").getAsString());
								}

								if (jsonobject.get("org") != null) {
									sender.sendMessage(ChatColor.AQUA + "ISP (Organization): " + ChatColor.GREEN + jsonobject.get("org").getAsString());
								}

								if (jsonobject.get("postal") != null) {
									sender.sendMessage(ChatColor.AQUA + "Postal Code: " + ChatColor.GREEN + jsonobject.get("postal").getAsString());
								}
								if (jsonobject.get("timezone") != null) {
									sender.sendMessage(ChatColor.AQUA + "Timezone: " + ChatColor.GREEN + jsonobject.get("timezone").getAsString());
								}

								// Finish process
								in.close();
								long finish = System.currentTimeMillis();
								long ms = finish - start;
								sender.sendMessage("");
								sender.sendMessage("Complete! (took " + ms + "ms)");
								Bukkit.getLogger().info("[PrettyIPInfo Logger] " + sender.getName() + " requested information for the IP address " + ip + ".");
							}
							catch (IOException e) {
								sender.sendMessage(e.toString());
							}
							return true;
						} else {
							sender.sendMessage(ChatColor.RED + "That is not a valid IP address.");
							Bukkit.getLogger().info("[PrettyIPInfo Logger] " + sender.getName() + " did not specify a valid IP address.");
							return true;
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Please enter an IP address.");
						Bukkit.getLogger().info("[PrettyIPInfo Logger] " + sender.getName() + " did not enter an IP address.");
						return true;
					}
				}

				if (args[0].equalsIgnoreCase("reload")) {
					long start = System.currentTimeMillis();
					this.reloadConfig();
					if (this.getConfig().getString("token") == null) {
						sender.sendMessage(ChatColor.RED + "Your IPInfo token has not been set!  Please review the config for more info.");
					}
					long finish = System.currentTimeMillis();
					long ms = finish - start;
					sender.sendMessage(ChatColor.GREEN + "Reload complete! (took " + ms + "ms)");
					Bukkit.getLogger().info("[PrettyIPInfo Logger] " + sender.getName() + " reloaded the plugin configuration in " + ms + "ms.");
					return true;
				}
			} else {
				sender.sendMessage(ChatColor.AQUA + "IPInfo v3.0 by TheRealSimShady");
				sender.sendMessage(ChatColor.GREEN + "Available options:");
				sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "/ipinfo player <username>");
				sender.sendMessage(ChatColor.GRAY + "  Get information for a player's IP address.");
				sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "/ipinfo ip <ip address>");
				sender.sendMessage(ChatColor.GRAY + "  View information for a specified IP address.");
				sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "/ipinfo reload");
				sender.sendMessage(ChatColor.GRAY + "  Reload the configuration.");
				Bukkit.getLogger().info("[PrettyIPInfo Logger] " + sender.getName() + " opened the help menu.");
				return true;
			}
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		final List<String> l1 = new ArrayList<String>();
		final List<String> l2 = new ArrayList<String>();
		if (cmd.getName().equalsIgnoreCase("ipinfo") && sender instanceof Player) {
			if (args.length > 1) {
				if (args[0].equalsIgnoreCase("player")) { 
					for (final Player p : getServer().getOnlinePlayers()) {
						l2.add(p.getName());
					}
				} else if (args[0].equalsIgnoreCase("ip")) {
					l2.add("#.#.#.#");
				}
				return l2;
			}
			else {
				l1.add("player");
				l1.add("ip");
				l1.add("reload");
			}
		}
		return l1;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		if (this.getConfig().getBoolean("save-ip-on-join") == true) {
			Player p = e.getPlayer();
			getLogger().info("IPINFO >>> Saving " + p.getName().toString() + "'s IP address to configuration...");
			String pip = p.getAddress().getHostString();
			this.getConfig().set("ips." + p.getName().toString(), pip);
			this.saveConfig();
			getLogger().info("IPINFO >>> Saved IP address updated for player " + p.getName().toString() + " (" + pip + ").");
		} else {
			getLogger().info("IPINFO >>> " + e.getPlayer().getName().toString() + "'s IP address will not be saved.");
		}
	}
	
}
