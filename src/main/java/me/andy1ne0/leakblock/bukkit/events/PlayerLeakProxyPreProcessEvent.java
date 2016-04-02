package me.andy1ne0.leakblock.bukkit.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.net.InetAddress;

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
 * Crated: Year: 2016, Month: 04, Day: 01, 18:35
 * Package Name: me.andy1ne0.leakblock
 * Project Name: ProxyBlock
 *
 * @author Andrew Petersen
 */
public class PlayerLeakProxyPreProcessEvent extends Event implements Cancellable {

    private Player pl = null;
    private InetAddress address = null;
    private boolean isCancelled = false;
    private String kickReason = null;
    boolean usingCustomKickReason = false;

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public PlayerLeakProxyPreProcessEvent(Player pl, InetAddress ip){
        this.pl = pl;
        this.address = ip;
    }

    public Player getPlayer(){
        return pl;
    }

    public InetAddress getAddress(){
        return address;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCustomKickMessage(String s){
        kickReason = s;
        usingCustomKickReason = true;
    }

    public boolean isUsingCustomKickReason(){
        return  usingCustomKickReason;
    }

    public String getKickReason(){
        return kickReason;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }
}
