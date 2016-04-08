package me.andy1ne0.leakblock.utils;

import org.bukkit.ChatColor;

/**
 * Created by kingCam on 4/8/16.
 */
public class ChatUtils {

    public static String format(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

}
