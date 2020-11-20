package com.robomwm.azureresizer;

/**
 * Created on 1/12/2019.
 *
 * @author RoboMWM
 */
public class Restarter
{
    private Thread restartTask;
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
            restartTask.interrupt();
            return true;
        }
        return false;
    }

    public Restarter scheduleRestartMinutes(AzureResizer plugin, int minutes)
    {
        if (restartTask != null)
            restartTask.interrupt();
        restartTask = new Thread(new RestartRunnable(plugin, minutes, warnMessage, kickMessage));
        restartTask.setPriority(Thread.MAX_PRIORITY);
        restartTask.start();
        return this;
    }
}
