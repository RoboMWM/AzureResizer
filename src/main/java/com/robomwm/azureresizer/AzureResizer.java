package com.robomwm.azureresizer;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created on 4/13/2020.
 *
 * @author RoboMWM
 */
public class AzureResizer extends JavaPlugin
{
    @Override
    public void onEnable()
    {
        new Restarter(this, "Upgrading server, you can rejoin in a couple minutes.");
    }
}
