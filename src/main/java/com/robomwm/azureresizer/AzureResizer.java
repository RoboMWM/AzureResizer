package com.robomwm.azureresizer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

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
    public Restarter restarterInstance;

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

        if (upgraded)
            restarterInstance = new Restarter("Server downgrading due to no players being on the server - so if you see this message then there's a problem! Please report this!",
                "No players detected on this server, will downgrade shortly. If you see this message then this is an error, please report this issue in chat right now! Thanks!")
                .scheduleRestartMinutes(this, 30);
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

    public void setTriggerUpgrade()
    {
        this.triggerUpgrade = true;
    }

    public void forceResize()
    {
        AzureResizer azureResizer = this;
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", "AzureResizer.jar");
        processBuilder.directory(azureResizer.getServer().getWorldContainer());
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectErrorStream(true);
        try
        {
            Process updateProcess = processBuilder.start();
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        BufferedReader output = new BufferedReader(new InputStreamReader(updateProcess.getInputStream()));
                        String outputLine;
                        while ((outputLine = output.readLine()) != null)
                        {
                            azureResizer.getLogger().info(outputLine);
                        }
                        azureResizer.getLogger().info("resize terminated...?");
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(azureResizer);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            azureResizer.getLogger().severe("Unable to insta-resize, performing regular resize-after-restart");
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    azureResizer.getServer().dispatchCommand(azureResizer.getServer().getConsoleSender(), "restartnow memes");
                }
            }.runTask(azureResizer);
        }
    }
}
