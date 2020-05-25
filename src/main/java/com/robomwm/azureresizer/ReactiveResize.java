package com.robomwm.azureresizer;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created on 5/24/2020.
 *
 * @author RoboMWM
 */
public class ReactiveResize implements Listener
{
    AzureResizer azureResizer;
    VirtualMachineController controller;

    public ReactiveResize(AzureResizer azureResizer, VirtualMachineController controller)
    {
        this.azureResizer = azureResizer;
        azureResizer.getServer().getPluginManager().registerEvents(this, azureResizer);
        this.controller = controller;
    }

    @EventHandler(ignoreCancelled = true)
    private void onLogin(AsyncPlayerPreLoginEvent login)
    {
        if (AzureResizer.upgraded)
            return;
        login.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Server was sleeping and will now wake up. You can join within a couple minutes!");
        AzureResizer.setTriggerUpgrade(true);
        controller.upgrade();
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event)
    {
        if (!AzureResizer.upgraded)
        {
            event.getPlayer().sendMessage("Idk how you made it in here but uh the server isn't upgraded so yea things are prolly gonna be very laggy.");
            event.getPlayer().sendMessage("The server was supposed to upgrade itself before it let you join but apparently that didn't happen...");
            return;
        }

        if (AzureResizer.restartTask != null)
        {
            AzureResizer.restartTask.cancel();
            AzureResizer.restartTask = null;
        }
    }

    @EventHandler
    private void onLeave(PlayerQuitEvent event)
    {
        if (Bukkit.getOnlinePlayers().size() > 1)
            return;

        if (AzureResizer.restartTask != null)
        {
            AzureResizer.restartTask.cancel();
            azureResizer.getLogger().warning("uh there was an already scheduled restart so ur logic wrong");
            azureResizer.getLogger().warning("when onquit runs there are " + Bukkit.getOnlinePlayers().size());
        }

        AzureResizer.restartTask = new Restarter("Server downgrading due to no players being on the server - so if you see this message then there's a problem! Please report this!",
                "No players detected on this server, will downgrade shortly. If you see this message then this is an error, please report this issue in chat right now! Thanks!")
                .scheduleRestart(azureResizer, 3600 * 20); //1 hour
    }
}
