package com.robomwm.azureresizer;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

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
    private boolean successfulEnable;

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
            new Restarter("Upgrading server, you can rejoin in a couple minutes, and there will be less lag.", "Server upgrade will occur in two minutes.")
                    .scheduleRestart(this, "01:00");
        else
            new Restarter("Server downgrading to reduce costs. If you see this message and you regularly play Minecraft at this time, please let us know in the chat at http://r.robomwm.com/mememap", "Server downgrading in two minutes to reduce costs. If you see this message and you regularly play Minecraft at this time, please let us know in the chat!")
                    .scheduleRestart(this, "10:00");
        successfulEnable = true;
    }

    @Override
    public void onDisable()
    {
        if (!successfulEnable || !triggerUpgrade)
            return;

        File file = new File("AzureResizerUpgrade.metadata");
        try
        {
            file.createNewFile();
            new FileWriter(file).write(String.valueOf(System.currentTimeMillis()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (!upgraded)
            virtualMachineController.upgrade();
        else
            virtualMachineController.downgrade();
    }

    public void setTriggerUpgrade(boolean triggerUpgrade)
    {
        this.triggerUpgrade = triggerUpgrade;
    }
}
