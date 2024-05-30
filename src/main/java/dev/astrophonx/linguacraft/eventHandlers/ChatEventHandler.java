package dev.astrophonx.linguacraft.eventHandlers;
import dev.astrophonx.linguacraft.LinguaCraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

@Mod.EventBusSubscriber(modid = LinguaCraft.MODID)
public class ChatEventHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<UUID, List<String>> playerChatBubbles = new HashMap<>();
    private static final int MAX_CHAT_BUBBLES = 3; // Maximum number of chat bubbles to display

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        LOGGER.info("onServerChat called: " + event.getMessage());
        String message = event.getMessage();
        ServerPlayer player = event.getPlayer();
        UUID playerUUID = player.getUUID();

        // Add chat bubble and limit the number of chat bubbles to MAX_CHAT_BUBBLES
        playerChatBubbles.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(message);
        if (playerChatBubbles.get(playerUUID).size() > MAX_CHAT_BUBBLES) {
            playerChatBubbles.get(playerUUID).remove(0); // Remove the oldest message if the limit is exceeded
        }

        // Schedule removal of the chat bubble after 7 seconds by default
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                List<String> messages = playerChatBubbles.get(playerUUID);
                if (messages != null && !messages.isEmpty()) {
                    messages.remove(0);
                    if (messages.isEmpty()) {
                        playerChatBubbles.remove(playerUUID);
                    }
                }
                LOGGER.info("Removed chat bubble for player: " + playerUUID);
            }
        }, 7000);
    }

    public static Map<UUID, List<String>> getPlayerChatBubbles() {
        return playerChatBubbles;
    }
}
