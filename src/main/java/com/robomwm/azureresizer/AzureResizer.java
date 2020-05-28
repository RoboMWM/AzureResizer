package com.robomwm.azureresizer;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    private static boolean triggerUpgrade;
    public static boolean upgraded;
    private VirtualMachineController virtualMachineController;
    public static BukkitTask restartTask;

    @Override
    public void onEnable()
    {
        try
        {
            virtualMachineController = new VirtualMachineController(this);
            upgraded = virtualMachineController.isUpgraded();
        }
        catch (IOException | UnsupportedOperationException e)
        {
            e.printStackTrace();
            getLogger().severe("Something bad happened.");
            getPluginLoader().disablePlugin(this);
            return;
        }

        new ReactiveResize(this, virtualMachineController);

//        if (!upgraded)
//            restartTask = new Restarter("Upgrading server, you can rejoin in a couple minutes, and there will be less lag.", "Server upgrade will occur in two minutes.")
//                    .scheduleRestart(this, "08:00"); //9am DST
//        else
//            restartTask = new Restarter("Server downgrading to reduce costs. If you see this message and you regularly play Minecraft at this time, please let us know in the chat at http://r.robomwm.com/mememap", "Server downgrading in two minutes to reduce costs. If you see this message and you regularly play Minecraft at this time, please let us know in the chat!")
//                    .scheduleRestart(this, "19:30"); //8:30pm DST
        if (upgraded)
            restartTask = new Restarter("Server downgrading due to no players being on the server - so if you see this message then there's a problem! Please report this!",
                "No players detected on this server, will downgrade shortly. If you see this message then this is an error, please report this issue in chat right now! Thanks!")
                .scheduleRestart(this, 1800 * 20); //30 minutes
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        triggerUpgrade = !triggerUpgrade;
        sender.sendMessage("Restart flag set to " + triggerUpgrade);
        return true;
    }

    @Override
    public void onDisable()
    {
        if (!triggerUpgrade)
            return;

        File file = new File("AzureResizerUpgrade.metadata");
        try
        {
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(String.valueOf(System.currentTimeMillis()));
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (upgraded)
        {
//            virtualMachineController.downgrade();
            getLogger().info("Downgrading server");
        }
        else
        {
//            virtualMachineController.upgrade();
            getLogger().info("Upgrading server");
        }
    }

    public static void setTriggerUpgrade(boolean triggerUpgrade)
    {
        AzureResizer.triggerUpgrade = triggerUpgrade;
    }
}
