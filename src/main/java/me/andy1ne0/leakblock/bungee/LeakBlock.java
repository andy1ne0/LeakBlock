package me.andy1ne0.leakblock.bungee;

import com.google.common.io.ByteStreams;
import com.jaunt.JNode;
import com.jaunt.JauntException;
import com.jaunt.ResponseException;
import com.jaunt.UserAgent;
import me.andy1ne0.leakblock.bungee.events.PlayerLeakBungeeProxyEvent;
import me.andy1ne0.leakblock.bungee.events.PlayerLeakBungeeProxyPreProcessEvent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

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
 * Crated: Year: 2016, Month: 04, Day: 02, 09:33
 * Package Name: me.andy1ne0.leakblock.bungee
 * Project Name: LeakBlock
 *
 * @author Andrew Petersen
 */
public class LeakBlock extends Plugin implements Listener {

    private static Configuration config = null;
    private int kickDelayTime = 0, failedAttempts = 0, maxFailedAttempts = 5;
    private boolean asyncProcess = false, debugEnabled = false;
    private LeakBlock instance;
    private String kickReason;
    private File file;

    public static Configuration getConfig() {
        return config;
    }

    @Override
    public void onEnable(){

        instance = this;

        try {
            if(!getDataFolder().exists()) {
                if(!getDataFolder().mkdir()){
                    throw new RuntimeException("Could not create configuration folder!");
                }
            }
            file = new File(getDataFolder(), "config.yml");

            if(!file.exists()){
                if(!file.createNewFile()){
                    throw new RuntimeException("Could not create configuration file!");
                }
                //save config file directly from jar to preserve comments
                ByteStreams.copy(getResourceAsStream("config.yml"), new FileOutputStream(file));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        initConfig();

        getProxy().getLogger().info("[LeakBlock] Initializing... ");

        kickReason = getConfig().getString("kickMessage").equalsIgnoreCase("default")
                ? (ChatColor.RED+"Prohibited proxy detected. \nPlease rejoin without using a proxy/VPN/Alt-generating server. ")
                : getConfig().getString("kickMessage").replace("@@", "\n");

        asyncProcess = getConfig().getBoolean("asyncEnabled");

        maxFailedAttempts = getConfig().getInt("maximumFailedPings");

        debugEnabled = getConfig().getBoolean("debug");

        boolean updateCheck = getConfig().getBoolean("updatecheck");

        if(asyncProcess){
            kickDelayTime = getConfig().getInt("kickDelayTime");
            getProxy().getLogger().info("[LeakBlock] Player processing is being handled asynchronously, the configured kick delay is: "+kickDelayTime+". ");
        } else {
            getProxy().getLogger().info("[LeakBlock] Player processing is being handled synchronously. This may result in some lag. ");
        }

        try {
            UserAgent pingAgent = new UserAgent();
            pingAgent.sendGET("http://ip-api.com/");
            getInstance().getLogger().info("[LeakBlock] Successfully pinged ip-api.com. ");

        } catch (ResponseException e){
            getProxy().getLogger().severe("[LeakBlock] Could not connect to ip-api.com. Plugin aborted. ");
            e.printStackTrace();
            getProxy().getPluginManager().unregisterListeners(this);
            getProxy().getPluginManager().unregisterCommands(this);
        }

        if(updateCheck) {
            getProxy().getScheduler().runAsync(this, new Runnable() {
                @Override
                public void run() {
                    try {
                        getProxy().getLogger().info("[LeakBlock] Checking for updates... ");
                        URL url = new URL("https://raw.githubusercontent.com/andy1ne0/LeakBlock/master/src/main/resources/latestversion.txt");
                        Scanner s = new Scanner(url.openStream());
                        final String version = s.next();

                        if (version.equalsIgnoreCase(getInstance().getDescription().getVersion())) {
                            getProxy().getLogger().info("[LeakBlock] Your version is up to date. ");
                        } else {
                            getProxy().getLogger().info("[LeakBlock] An update is available, or will be soon. Check the Spigot forums for more information. ");
                            getProxy().getPluginManager().registerListener(getInstance(), new UpdateListener(version));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else getInstance().getLogger().info("[LeakBlock] Update checking is disabled. ");

        getProxy().getPluginManager().registerListener(this, this);

        getProxy().getScheduler().schedule(this, new Runnable() {
            @Override
            public void run() {
                failedAttempts = 0;
            }
        }, 60, TimeUnit.SECONDS);

    }

    @EventHandler
    @SuppressWarnings("all")
    public void onPlayerLogin(final LoginEvent evt){

        PlayerLeakBungeeProxyPreProcessEvent event = new PlayerLeakBungeeProxyPreProcessEvent(evt.getConnection().getUniqueId(), evt.getConnection().getName(), evt.getConnection().getAddress().getAddress());

        getProxy().getPluginManager().callEvent(event);

        if(event.isCancelled()){
            evt.setCancelled(true);
            String kickReasonToSend = kickReason;
            if(event.isUsingCustomKickReason()) kickReasonToSend = event.getKickReason();
            evt.setCancelReason(kickReasonToSend);
            return;
        }

        if(asyncProcess){
            getProxy().getScheduler().runAsync(this, new Runnable() {
                @Override
                @SuppressWarnings("all")
                public void run() {
                    try {
                        UserAgent agent = new UserAgent();
                        agent.sendGET("http://ip-api.com/json/" + evt.getConnection().getAddress().getAddress().getHostAddress());

                        JNode json = agent.json;
                        if (json.get("status").toString().equalsIgnoreCase("fail")) {
                            if(debugEnabled) {
                                getInstance().getLogger().info("[LeakBlock] The connection to ip-api returned an error. ");
                                getInstance().getLogger().info("[LeakBlock] Dump: " + agent.json);
                            }
                            return;
                        }
                        if (json.get("isp").toString().equalsIgnoreCase("OVH SAS")
                                && (json.get("country").toString().equalsIgnoreCase("France")
                                || json.get("country").toString().equalsIgnoreCase("Italy"))) {

                            getProxy().getPluginManager().callEvent(
                                    new PlayerLeakBungeeProxyEvent(evt.getConnection().getName(),
                                            evt.getConnection().getUniqueId(),
                                            evt.getConnection().getAddress().getAddress()));


                            getProxy().getScheduler().runAsync(getInstance(), new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(kickDelayTime);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    evt.getConnection().disconnect(kickReason);
                                }
                            });

                        }
                    } catch (ResponseException e){
                        e.printStackTrace();
                        failedAttempts++;
                        if(failedAttempts >= maxFailedAttempts){
                            getProxy().getLogger().info("[LeakBlock] Maximum failure limit reached. Plugin terminated. ");
                            getProxy().getPluginManager().unregisterCommands(getInstance());
                            getProxy().getPluginManager().unregisterListeners(getInstance());
                        }
                    } catch (JauntException e){
                        e.printStackTrace();
                    }
                }
            });

        } else {
            try {
                UserAgent agent = new UserAgent();
                agent.sendGET("http://ip-api.com/json/" + evt.getConnection().getAddress().getAddress().getHostAddress());
                JNode json = agent.json;
                if (json.get("status").toString().equalsIgnoreCase("fail")) {
                    if(debugEnabled) {
                        getInstance().getLogger().info("[LeakBlock] The connection to ip-api returned an error. ");
                        getInstance().getLogger().info("[LeakBlock] Dump: " + agent.json);
                    }
                    return;
                }
                if (json.get("isp").toString().equalsIgnoreCase("OVH SAS")
                        && (json.get("country").toString().equalsIgnoreCase("France")
                        || json.get("country").toString().equalsIgnoreCase("Italy"))) {
                    getProxy().getPluginManager().callEvent(new PlayerLeakBungeeProxyEvent(evt.getConnection().getName(), evt.getConnection().getUniqueId(), evt.getConnection().getAddress().getAddress()));
                    evt.setCancelled(true);
                    evt.setCancelReason(kickReason);
                }
            } catch (ResponseException e){
                e.printStackTrace();
                failedAttempts++;
                if(failedAttempts >= maxFailedAttempts){
                    getProxy().getLogger().info("[LeakBlock] Maximum failure limit reached. Plugin terminated. ");
                    getProxy().getPluginManager().unregisterListeners(this);
                    getProxy().getPluginManager().unregisterCommands(this);
                }
            } catch (JauntException e){
                e.printStackTrace();
            }
        }

    }

    public void saveConfig() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void initConfig(){
        try {
            file = new File(getDataFolder(), "config.yml");
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LeakBlock getInstance() {
        return instance;
    }

    public class UpdateListener implements Listener {
        public String version = null;

        public UpdateListener(String version){
            this.version = version;
        }

        @EventHandler
        public void onPlayerJoin(PostLoginEvent evt){
            boolean hasAdmin = false;
            for(String s: evt.getPlayer().getGroups()){
                if(s.toLowerCase().contains("admin"))
                    hasAdmin = true;
            }
            if(hasAdmin)
                evt.getPlayer().sendMessage(ChatColor.DARK_GREEN+"["+ChatColor.GREEN+"LeakBlock"+ChatColor.DARK_GREEN+"] "
                        +ChatColor.GRAY+"An update is available. Current version: "+getInstance().getDescription().getVersion()
                        +", latest version: "+version);
        }
    }

}
