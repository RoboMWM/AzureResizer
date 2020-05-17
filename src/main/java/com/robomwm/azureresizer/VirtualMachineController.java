package com.robomwm.azureresizer;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.rest.LogLevel;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 4/13/2020.
 *
 * @author RoboMWM
 */
public class VirtualMachineController
{
    private Azure azure;
    private VirtualMachine vm;
    private VirtualMachineSizeTypes upgradedType = VirtualMachineSizeTypes.STANDARD_B1MS;
    private VirtualMachineSizeTypes freeType = VirtualMachineSizeTypes.STANDARD_B1S;

    public VirtualMachineController(Plugin plugin) throws IOException, UnsupportedOperationException
    {
        FileConfiguration configuration = plugin.getConfig();
        configuration.addDefault("resourceGroup", "blegh");
        configuration.addDefault("vmName", "ya");
        configuration.options().copyDefaults(true);
        plugin.saveConfig();
        File credsFile = new File(plugin.getDataFolder() + File.separator + "creds.properties");
        credsFile.getParentFile().mkdirs();
        if (!credsFile.exists())
        {
            copyFile(plugin, credsFile, "creds.properties");
            plugin.getLogger().warning("Fill out the configurations");
            throw new UnsupportedOperationException();
        }

        azure = Azure.configure().withLogLevel(LogLevel.BODY_AND_HEADERS).authenticate(credsFile).withDefaultSubscription();
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

    public void upgrade()
    {
        new ResizeThread(vm, upgradedType).start();
    }

    public void downgrade()
    {
        new ResizeThread(vm, freeType).start();
    }

    public boolean isUpgraded()
    {
        return vm.size() == upgradedType;
    }
}

class ResizeThread extends Thread
{
    private VirtualMachine vm;
    private VirtualMachineSizeTypes type;

    public ResizeThread(VirtualMachine vm, VirtualMachineSizeTypes type)
    {
        this.vm = vm;
        this.type = type;
    }

    @Override
    public void run()
    {
        vm.update().withSize(type).apply();
    }
}
