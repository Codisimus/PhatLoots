
package PhatLoots;

import com.griefcraft.lwc.LWCPlugin;
import org.bukkit.event.server.ServerListener;
import com.codisimus.phatloots.register.payment.Methods;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * Checks for plugins whenever one is enabled
 *
 */
public class PluginListener extends ServerListener {
    public PluginListener() { }
    protected static boolean useOP;

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
    private void linkPermissions() {
        //Return if we have already have a permissions plugin
        if (PhatLootsMain.permissions != null)
            return;

        //Return if PermissionsEx is not enabled
        if (!PhatLootsMain.pm.isPluginEnabled("PermissionsEx"))
            return;

        //Return if OP permissions will be used
        if (useOP)
            return;

        PhatLootsMain.permissions = PermissionsEx.getPermissionManager();
        System.out.println("[PhatLoots] Successfully linked with PermissionsEx!");
    }

    /**
     * Find and link an Economy plugin
     *
     */
    private void linkEconomy() {
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
    private void linkChestLocker() {
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