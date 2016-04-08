package me.andy1ne0.leakblock.bukkit;

import me.andy1ne0.leakblock.bukkit.events.PlayerLeakProxyEvent;
import me.andy1ne0.leakblock.bukkit.events.PlayerLeakProxyPreProcessEvent;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * All contents of this file, except where specified elsewhere, is the copyrighted property of Andrew Petersen.
 * Any works derived from this must only be done so if permission is given beforehand by the author (Andrew Petersen).
 * Failure to receive proper permission to use the works provided will result in termination of any agreements between the third party and Andrew Petersen.
 * If permission to use the contents of this file are granted to you, the client party, you must not take credit and/or claim these works as your own.
 * Doing so will void any contracts, or permission given, by Andrew Petersen, unless notified otherwise.
 * This file is provided as-is, with absolutely no warranty provided. By using this file, you understand this
 * - regardless of if you have been warned or not prior to using this file, any liabilities or harm caused by this file is your problem.
 * If this file is available for public download, by intention of the author, derirative works without the consent of Andrew Petersen are allowed.
 * However, once again, you must give credit to the author of the code.
 * If this has been downloaded or distributed without the permission of the author, all permissions granted above are voided.
 * It is also required that in these circumstances, you cease to use the file/contents.
 * Crated: Year: 2016, Month: 03, Day: 29, 20:59
 * Package Name: me.andy1ne0.leakblock
 * Project Name: ProxyBlock
 *
 * @author Andrew Petersen
 */

public class LeakBlock extends JavaPlugin implements Listener {

    private boolean asyncProcess = false;
    private String kickReason = null;
    private int kickDelayTime = 0;
    // private int timeout = 2000;
    private LeakBlock instance = null;
    private int failedAttempts = 0;
    private int maxFailedAttempts = 5;
    private boolean debugEnabled = false;

    public LeakBlock getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {

        instance = this;

        saveDefaultConfig();

        getServer().getLogger().info("[LeakBlock] Initializing... ");

        kickReason = getConfig().getString("kickMessage").equalsIgnoreCase("default")
                ? (ChatColor.RED + "Prohibited proxy detected. \nPlease rejoin without using a proxy/VPN/Alt-generating server. ")
                : getConfig().getString("kickMessage").replace("@@", "\n");

        asyncProcess = getConfig().getBoolean("asyncEnabled");

        maxFailedAttempts = getConfig().getInt("maximumFailedPings");

        debugEnabled = getConfig().getBoolean("debug");

        boolean updateCheck = getConfig().getBoolean("updatecheck");

        if (asyncProcess) {
            kickDelayTime = getConfig().getInt("kickDelayTime");
            getServer().getLogger().info("[LeakBlock] Player processing is being handled asynchronously, the configured kick delay is: " + kickDelayTime + ". ");
        } else {
            getServer().getLogger().info("[LeakBlock] Player processing is being handled synchronously. This may result in some lag. ");
        }

        // timeout = getConfig().getInt("timeout");
        // getServer().getServer().getLogger().info("[LeakBlock] The configured timeout for connections is "+timeout+". ");

        //Please do something about this, it is ugly. :3
        try {
            HttpPost post = new HttpPost("http://ip-api.com/json/8.8.8.8");
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse getData = httpClient.execute(post);
            InputStream in = getData.getEntity().getContent();
            BufferedReader inBuff = new BufferedReader(new InputStreamReader(in));
            StringBuilder conv = new StringBuilder();
            String s;
            while ((s = inBuff.readLine()) != null) {
                conv.append(s);
            }

            getServer().getLogger().info("[LeakBlock] Successfully pinged ip-api.com. ");

        } catch (IOException e) {
            getServer().getLogger().severe("[LeakBlock] Could not connect to ip-api.com. Plugin aborted. ");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }

        if (updateCheck) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        getServer().getLogger().info("[LeakBlock] Checking for updates... ");
                        HttpPost post = new HttpPost("https://raw.githubusercontent.com/andy1ne0/LeakBlock/master/src/main/resources/latestversion.txt");
                        HttpClient client = HttpClientBuilder.create().build();
                        HttpResponse getFromPost = client.execute(post);
                        InputStream in = getFromPost.getEntity().getContent();
                        BufferedReader read = new BufferedReader(new InputStreamReader(in));
                        StringBuilder conv = new StringBuilder();
                        String s;
                        while ((s = read.readLine()) != null) {
                            conv.append(s);
                        }
                        if (conv.toString().equalsIgnoreCase(getInstance().getDescription().getVersion())) {
                            getServer().getLogger().info("[LeakBlock] Your version is up to date. ");
                        } else {
                            getServer().getLogger().info("[LeakBlock] An update is available, or will be soon. Check the Spigot forums for more information. ");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(this);
        } else {
            getServer().getLogger().info("[LeakBlock] Update checking is disabled. ");
        }

        getServer().getPluginManager().registerEvents(this, this);

        new BukkitRunnable() {
            @Override
            public void run() {
                failedAttempts = 0;
            }
        }.runTaskTimer(this, 1200, 1200);

    }

    @EventHandler
    @SuppressWarnings("all")
    public void onPlayerLogin(final PlayerLoginEvent evt) {

        PlayerLeakProxyPreProcessEvent event = new PlayerLeakProxyPreProcessEvent(evt.getPlayer(), evt.getAddress());

        getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            evt.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            String kickReasonToSend = kickReason;
            if (event.isUsingCustomKickReason()) kickReasonToSend = event.getKickReason();
            evt.setKickMessage(kickReasonToSend);
            return;
        }

        if (asyncProcess) {

            new BukkitRunnable() {
                @Override
                @SuppressWarnings("all")
                public void run() {
                    try {
                        HttpPost post = new HttpPost("http://ip-api.com/json/" + evt.getAddress().getHostAddress());
                        HttpClient httpClient = HttpClientBuilder.create().build();
                        HttpResponse getData = httpClient.execute(post);
                        InputStream in = getData.getEntity().getContent();
                        BufferedReader inBuff = new BufferedReader(new InputStreamReader(in));
                        StringBuilder conv = new StringBuilder();
                        String s = null;
                        while ((s = inBuff.readLine()) != null) {
                            conv.append(s);
                        }

                        JSONObject json = new JSONObject(conv.toString());
                        if (json.getString("isp").equalsIgnoreCase("OVH SAS") && (json.getString("country").equalsIgnoreCase("France") || json.getString("country").equalsIgnoreCase("Italy"))) {

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    getServer().getPluginManager().callEvent(new PlayerLeakProxyEvent(evt.getPlayer(), evt.getAddress()));
                                    evt.getPlayer().kickPlayer(kickReason);
                                }
                            }.runTaskLater(getInstance(), kickDelayTime);

                        } else if (json.getString("status").equalsIgnoreCase("fail")) {
                            if (debugEnabled) {
                                getServer().getLogger().info("[LeakBlock] The connection to ip-api returned an error. ");
                                getServer().getLogger().info("[LeakBlock] Dump: " + conv.toString());
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                failedAttempts++;
                                if (failedAttempts >= maxFailedAttempts) {
                                    getServer().getLogger().info("[LeakBlock] Maximum failure limit reached. Plugin terminated. ");
                                    getServer().getPluginManager().disablePlugin(getInstance());
                                }
                            }
                        }.runTask(getInstance());
                    }
                }
            }.runTaskAsynchronously(this);

        } else {
            try {
                HttpPost post = new HttpPost("http://ip-api.com/json/" + evt.getAddress().getHostAddress());
                HttpClient httpClient = HttpClientBuilder.create().build();
                HttpResponse getData = httpClient.execute(post);
                InputStream in = getData.getEntity().getContent();
                BufferedReader inBuff = new BufferedReader(new InputStreamReader(in));
                StringBuilder conv = new StringBuilder();
                String s = null;
                while ((s = inBuff.readLine()) != null) {
                    conv.append(s);
                }

                JSONObject json = new JSONObject(conv.toString());
                if (json.getString("isp").equalsIgnoreCase("OVH SAS") && (json.getString("country").equalsIgnoreCase("France") || json.getString("country").equalsIgnoreCase("Italy"))) {
                    getServer().getPluginManager().callEvent(new PlayerLeakProxyEvent(evt.getPlayer(), evt.getAddress()));
                    evt.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                    evt.setKickMessage(kickReason);
                } else if (json.getString("status").equalsIgnoreCase("fail")) {
                    if (debugEnabled) {
                        getServer().getLogger().info("[LeakBlock] The connection to ip-api returned an error. ");
                        getServer().getLogger().info("[LeakBlock] Dump: " + conv.toString());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                failedAttempts++;
                if (failedAttempts >= maxFailedAttempts) {
                    getServer().getLogger().info("[LeakBlock] Maximum failure limit reached. Plugin terminated. ");
                    getServer().getPluginManager().disablePlugin(this);
                }
            }
        }

    }

}
