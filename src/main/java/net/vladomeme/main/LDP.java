package net.vladomeme.main;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

public class LDP implements ClientModInitializer {

    public static Logger LOGGER = LoggerFactory.getLogger("LDP");

    @Override
    public void onInitializeClient() {
        LOGGER.info("LD Particles loaded. {}", getLoadMessage());
    }

    private String getLoadMessage() {
        String[] messages = new String[]{
                "What does LD stand for?",
                "Can't see shit in this mist?"
        };
        return messages[ThreadLocalRandom.current().nextInt(messages.length)];
    }
}
