package net.vladomeme.main.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.network.chat.Component;

public class LDPModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")) {
            return LDPConfig.INSTANCE::create;
        }
        return parent -> new AlertScreen(() -> Minecraft.getInstance().setScreen(parent),
                Component.nullToEmpty("LD Particles"), Component.nullToEmpty("Mod requires YACL to be able to show the config."));
    }
}
