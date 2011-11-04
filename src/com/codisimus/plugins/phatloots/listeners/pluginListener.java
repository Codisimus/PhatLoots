package com.codisimus.plugins.phatloots.listeners;

import com.codisimus.plugins.phatloots.PhatLootsMain;
import com.codisimus.plugins.phatloots.Register;
import com.griefcraft.lwc.LWCPlugin;
import org.bukkit.event.server.ServerListener;
import com.codisimus.plugins.phatloots.register.payment.Methods;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * Checks for plugins whenever one is enabled
 *
 * @author Codisimus
 */
public class pluginListener extends ServerListener {
    public static boolean useBP;

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        linkPermissions();
        linkEconomy();
        linkChestLocker();
    }

    /**
     * Find and link a Permission plugin
     *
     */
    public void linkPermissions() {
        //Return if we have already have a permissions plugin
        if (PhatLootsMain.permissions != null)
            return;

        //Return if PermissionsEx is not enabled
        if (!PhatLootsMain.pm.isPluginEnabled("PermissionsEx"))
            return;

        //Return if OP permissions will be used
        if (useBP)
            return;

        PhatLootsMain.permissions = PermissionsEx.getPermissionManager();
        System.out.println("[PhatLoots] Successfully linked with PermissionsEx!");
    }

    /**
     * Find and link an Economy plugin
     *
     */
    public void linkEconomy() {
        //Return if we already have an Economy plugin
        if (Methods.hasMethod())
            return;

        //Return if no Economy is wanted
        if (Register.economy.equalsIgnoreCase("none"))
            return;

        //Set preferred plugin if there is one
        if (!Register.economy.equalsIgnoreCase("auto"))
            Methods.setPreferred(Register.economy);

        Methods.setMethod(PhatLootsMain.pm);

        //Reset Methods if the preferred Economy was not found
        if (!Methods.getMethod().getName().equalsIgnoreCase(Register.economy) && !Register.economy.equalsIgnoreCase("auto")) {
            Methods.reset();
            return;
        }

        Register.econ = Methods.getMethod();
        System.out.println("[PhatLoots] Successfully linked with "+Register.econ.getName()+" "+Register.econ.getVersion()+"!");
    }

    /**
     * Find and link a Chest Locking plugin
     *
     */
    public void linkChestLocker() {
        //Return if we already have a Chest Locking plugin
        if (PhatLootsMain.lwc != null)
            return;

        //Return if LWC is not enabled
        if (!PhatLootsMain.pm.isPluginEnabled("LWC"))
            return;

        Plugin lwc = PhatLootsMain.pm.getPlugin("LWC");
        PhatLootsMain.lwc = ((LWCPlugin)lwc).getLWC();
        System.out.println("[PhatLoots] Successfully linked with LWC!");
    }
}