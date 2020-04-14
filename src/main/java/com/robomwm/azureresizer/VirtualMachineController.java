package com.robomwm.azureresizer;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.rest.LogLevel;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Created on 4/13/2020.
 *
 * @author RoboMWM
 */
public class VirtualMachineController
{
    private Azure azure;
    private VirtualMachine vm;

    public VirtualMachineController(Plugin plugin) throws IOException, UnsupportedOperationException
    {
        File credsFile = new File(plugin.getDataFolder() + File.separator + "creds.properties");
        credsFile.getParentFile().mkdirs();
        if (!credsFile.exists())
        {
            copyFile(plugin, credsFile, "creds.properties");
            plugin.getLogger().warning("Fill out the configurations");
            throw new UnsupportedOperationException();
        }

        azure = Azure.configure().withLogLevel(LogLevel.BASIC).authenticate(credsFile).withDefaultSubscription();
        FileConfiguration configuration = plugin.getConfig();
        vm = azure.virtualMachines().getByResourceGroup(configuration.getString("resourceGroup"), configuration.getString("vmName"));
    }

    private void copyFile(Plugin plugin, File file, String name)
    {
        try (InputStream in = plugin.getResource(name)) {
            Files.copy(in, file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void upgrade()
    {
        //TODO resize up
    }

    private void downgrade()
    {
        //TODO resize down
    }

    private boolean isUpgraded()
    {
        //TODO return if == vm type that upgrade() resizes to
    }
}
