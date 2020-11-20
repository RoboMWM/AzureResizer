package com.robomwm.azureresizer;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Created on 5/24/2020.
 *
 * @author RoboMWM
 */
public class ReactiveResize implements Listener
{
    AzureResizer azureResizer;

    public ReactiveResize(AzureResizer azureResizer)
    {
        this.azureResizer = azureResizer;
        azureResizer.getServer().getPluginManager().registerEvents(this, azureResizer);
    }

    @EventHandler(ignoreCancelled = true)
    private void onLogin(AsyncPlayerPreLoginEvent login)
    {
        if (AzureResizer.upgraded)
            return;
        login.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Server was sleeping and will now wake up. You can join within a couple minutes!");
        azureResizer.setTriggerUpgrade();
        azureResizer.forceResize();
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event)
    {
        if (!azureResizer.upgraded)
        {
            event.getPlayer().sendMessage("Idk how you made it in here but uh the server isn't upgraded so yea things are prolly gonna be very laggy.");
            event.getPlayer().sendMessage("The server was supposed to upgrade itself before it let you join but apparently that didn't happen...");
            azureResizer.getServer().dispatchCommand(azureResizer.getServer().getConsoleSender(), "communicationconnector Uh someone somhow joined without the server being upgraded??");
            return;
        }

        if (azureResizer.restarterInstance != null)
        {
            azureResizer.restarterInstance.cancelRestart();
            azureResizer.restarterInstance = null;
        }
    }

    @EventHandler
    private void onLeave(PlayerQuitEvent event)
    {
        if (Bukkit.getOnlinePlayers().size() > 1)
            return;

        if (azureResizer.restarterInstance != null)
        {
            azureResizer.restarterInstance.cancelRestart();
            azureResizer.getLogger().severe("uh there was an already scheduled restart so ur logic wrong");
            azureResizer.getLogger().severe("when onquit runs there are " + Bukkit.getOnlinePlayers().size());
        }

        azureResizer.restarterInstance = new Restarter("Server downgrading due to no players being on the server - so if you see this message then there's a problem! Please report this!",
                "No players detected on this server, will downgrade shortly. If you see this message then this is an error, please report this issue in chat right now! Thanks!")
                .scheduleRestartMinutes(azureResizer, 60);
    }
}
