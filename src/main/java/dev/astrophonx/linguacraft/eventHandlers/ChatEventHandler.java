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

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        LOGGER.info("onServerChat called: " + event.getMessage());
        String message = event.getMessage();
        ServerPlayer player = event.getPlayer();
        UUID playerUUID = player.getUUID();

        // Add chat bubble
        playerChatBubbles.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(message);

        // Schedule removal of the chat bubble after 5 seconds
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
        }, 5000);
    }

    public static Map<UUID, List<String>> getPlayerChatBubbles() {
        return playerChatBubbles;
    }
}
