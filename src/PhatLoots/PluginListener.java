
package PhatLoots;

import com.griefcraft.lwc.LWCPlugin;
import org.bukkit.event.server.ServerListener;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijikokun.register.payment.Methods;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

/**
 * Checks for plugins whenever one is enabled
 *
 */
public class PluginListener extends ServerListener {
    public PluginListener() { }
    private Methods methods = new Methods();
    protected static Boolean useOP;

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        if (PhatLootsMain.permissions == null && !useOP) {
            Plugin permissions = PhatLootsMain.pm.getPlugin("Permissions");
            if (permissions != null) {
                PhatLootsMain.permissions = ((Permissions)permissions).getHandler();
                System.out.println("[PhatLoots] Successfully linked with Permissions!");
            }
        }
        if (Register.economy == null)
            System.err.println("[PhatLoots] Config file outdated, Please regenerate");
        else if (!Register.economy.equalsIgnoreCase("none") && !methods.hasMethod()) {
            try {
                methods.setMethod(PhatLootsMain.pm.getPlugin(Register.economy));
                if (methods.hasMethod()) {
                    Register.econ = methods.getMethod();
                    System.out.println("[PhatLoots] Successfully linked with "+
                            Register.econ.getName()+" "+Register.econ.getVersion()+"!");
                }
            }
            catch (Exception e) {
            }
        }
        if (PhatLootsMain.lwc == null) {
            Plugin lwc = PhatLootsMain.pm.getPlugin("LWC");
            if (lwc != null) {
                PhatLootsMain.lwc = ((LWCPlugin)lwc).getLWC();
                System.out.println("[PhatLoots] Successfully linked with LWC!");
            }
        }
    }
}