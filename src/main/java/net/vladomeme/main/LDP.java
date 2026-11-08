package net.vladomeme.main;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LDP implements ClientModInitializer {

    public static Logger LOGGER = LoggerFactory.getLogger("LDP");

    @Override
    public void onInitializeClient() {
        LOGGER.info("LD Particles loaded. What does LD stand for?");
    }
}
