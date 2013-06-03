package com.codisimus.plugins.phatloots;

import com.google.common.io.Files;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Basic implementation for multiple config files.
 *
 * @author Inflamedsebi
 *
 */
public class ConfigFileAccessor {
    private final JavaPlugin plugin;
    private Logger log;

    /**
     * Enables multiple yaml configuration files.
     *
     * @param plugin
     */
    public ConfigFileAccessor(JavaPlugin instance) {
        plugin = instance;
        log = plugin.getLogger();
    }

    /**
     * Does the same as getConfig() but for the given config file.
     *
     * @param configFile
     * @return FileConfiguration from configFile
     */
    public FileConfiguration loadConfigDefault(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);

        //Look for defaults in the jar
        InputStream defConfigStream = plugin.getResource(fileName);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            fileConfiguration.setDefaults(defConfig);
        }
        return fileConfiguration;
    }

    /**
     * Does the same as getConfig() but for the given config file.
     *
     * @param configFile
     * @return FileConfiguration from configFile
     */
    public FileConfiguration loadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        FileConfiguration fileConfiguration = new YamlConfiguration();
        try {
            file.createNewFile();
            fileConfiguration.loadFromString(Files.toString(file, Charset.forName("UTF-8")));
        } catch (Exception e) {
            log.severe("§4Could not load data from " + file);
            e.printStackTrace();
        }

        return fileConfiguration;
    }

    /**
     * Will save the given FileConfiguration to the given File.
     *
     * @param configFile
     * @param fileConfiguration
     */
    public void saveConfig(String fileName, FileConfiguration fileConfiguration) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (fileConfiguration != null && file != null) {
            try {
                String data = fileConfiguration.saveToString();
                OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
                out.write(data, 0, data.length());
                out.flush();
                out.close();
            } catch (IOException ex) {
                log.severe("§4Could not save data to " + file);
                ex.printStackTrace();
            }
        }
    }

    /**
     * Does the same as saveDefaultConfig() but for the given config file.
     *
     * @param configFile
     */
    public void saveDefaultConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            this.plugin.saveResource(fileName, false);
        }
    }

    /**
     * Checks if given byte array is valid UTF-8 encoded.
     *
     * @param bytes
     * @return true when valid UTF8 encoded
     */
    public static boolean isValidUTF8(final byte[] bytes) {
        try {
            Charset.availableCharsets().get("UTF-8").newDecoder().decode(ByteBuffer.wrap(bytes));
        } catch (CharacterCodingException e) {
            return false;
        }
        return true;
    }
}
