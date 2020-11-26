package com.robomwm.azureresizer;

import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created on 11/19/2020.
 *
 * @author RoboMWM
 */
public class RestartRunnable implements Runnable
{
    private int minutesToWait;
    private AzureResizer plugin;
    private String warnMessage;
    private String kickMessage;

    public RestartRunnable(AzureResizer plugin, int minutesToWait, String warnMessage, String kickMessage)
    {
        this.minutesToWait = minutesToWait;
        this.plugin = plugin;
        this.warnMessage = warnMessage;
        this.kickMessage = kickMessage;
    }

    @Override
    public void run()
    {
        try
        {
            long milliseconds = minutesToWait * 60000L;
            plugin.getLogger().info("Scheduling a restart to occur in " + minutesToWait + " minutes. In milliseconds, " + milliseconds);
            plugin.getLogger().info("warn: " + warnMessage);
            plugin.getLogger().info("kick: " + kickMessage);

            try
            {
                Thread.sleep(milliseconds);
            }
            catch (InterruptedException e)
            {
                plugin.getLogger().info("Restart thread was interrupted (a player joined, prolly. If a player did NOT just join, then this is an error!!)");
                return;
            }

            try
            {
                plugin.setTriggerUpgrade();
                plugin.getLogger().info("Attempting a restart to resize...");
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "restartnow memes");
                    }
                }.runTask(plugin);
                Thread.sleep(20L * 60000L);
            }
            catch (InterruptedException e)
            {
                plugin.getLogger().severe("Post-restart sleep was interrupted!! This means the restart command above did not go through and a player somehow logged in!!");
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "communicationconnector Post-restart sleep was interrupted!");
                    }
                }.runTask(plugin);
            }
            catch (IllegalPluginAccessException e)
            {
                plugin.getLogger().severe("Post-restart sleep was interrupted due to hang on server shutdown!!");
            }
            finally
            {
                plugin.forceResize();
                plugin.getLogger().severe("Server has not stopped due to an error or for over 20 minutes! Forcing a resize now!!");
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "communicationconnector Server is stuck or something, forcing a resize.");
                    }
                }.runTask(plugin);
            }
        }
        catch (Throwable rock)
        {
            plugin.getLogger().severe("Restart thread failed!");
            rock.printStackTrace();
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "communicationconnector Restart thread failed due to " + rock.getMessage());
                }
            }.runTask(plugin);
            return;
        }
    }
}
