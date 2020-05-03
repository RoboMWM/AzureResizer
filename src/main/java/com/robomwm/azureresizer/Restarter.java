package com.robomwm.azureresizer;

import com.microsoft.azure.management.compute.VirtualMachine;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created on 1/12/2019.
 *
 * @author RoboMWM
 */
public class Restarter
{
    private BukkitTask restartTask;
    private String kickMessage;
    private String warnMessage;

    public Restarter(String kickMessage, String warnMessage)
    {
        this.kickMessage = kickMessage;
        this.warnMessage = warnMessage;
    }

    public boolean cancelRestart()
    {
        if (restartTask != null)
        {
            restartTask.cancel();
            return true;
        }
        return false;
    }

    public void scheduleRestart(AzureResizer plugin, String time)
    {
        try
        {
            //Java's time/calendar/whatever API is stoopid
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
            calendar.setTime(dateFormat.parse(time));
            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
            currentCalendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
            currentCalendar.set(Calendar.SECOND, 0);

            long restartTime = currentCalendar.getTimeInMillis();

            //If it returns a time we already passed, advance it to the next day
            if (System.currentTimeMillis() > restartTime)
                restartTime += 86400000L; //86400000ms = 1 day

            long ticksToScheduleRestart = (restartTime - System.currentTimeMillis()) / 50;

            scheduleRestart(plugin, ticksToScheduleRestart);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    private void scheduleRestart(AzureResizer plugin, long ticks)
    {
        if (restartTask != null)
            restartTask.cancel();

        int totalSeconds = (int)ticks / 20;
        int totalMinutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        int hours = seconds / 60;
        int minutes = hours % 60;

        plugin.getLogger().info("Scheduling a restart to occur in " + ticks + " ticks. (" + hours + ":" + minutes + ":" + seconds + " or " + totalMinutes + ")");

        restartTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                plugin.setTriggerUpgrade(true);
                if (plugin.getServer().getOnlinePlayers().size() > 0)
                {
                    plugin.getLogger().warning("Was going to restart, but players were on the server.");
                    plugin.getServer().broadcastMessage(warnMessage);
                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            for (Player player : plugin.getServer().getOnlinePlayers())
                                player.kickPlayer(kickMessage);
                            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "restart memes");
                        }
                    }.runTaskLater(plugin, 2400L);
                }
                else
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "restart memes");
            }
        }.runTaskLater(plugin, ticks);
    }
}
