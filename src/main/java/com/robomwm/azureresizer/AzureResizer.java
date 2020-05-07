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
    private boolean triggerUpgrade;
    private boolean upgraded;
    private VirtualMachineController virtualMachineController;
    private BukkitTask restartTask;

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

        if (!upgraded)
            restartTask = new Restarter("Upgrading server, you can rejoin in a couple minutes, and there will be less lag.", "Server upgrade will occur in two minutes.")
                    .scheduleRestart(this, "10:00");
        else
            restartTask = new Restarter("Server downgrading to reduce costs. If you see this message and you regularly play Minecraft at this time, please let us know in the chat at http://r.robomwm.com/mememap", "Server downgrading in two minutes to reduce costs. If you see this message and you regularly play Minecraft at this time, please let us know in the chat!")
                    .scheduleRestart(this, "01:00");
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
        if (restartTask == null || !triggerUpgrade)
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
            virtualMachineController.downgrade();
            getLogger().info("Downgrading server");
        }
        else
        {
            virtualMachineController.upgrade();
            getLogger().info("Upgrading server");
        }

        try
        {
            Thread.sleep(7000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void setTriggerUpgrade(boolean triggerUpgrade)
    {
        this.triggerUpgrade = triggerUpgrade;
    }
}
