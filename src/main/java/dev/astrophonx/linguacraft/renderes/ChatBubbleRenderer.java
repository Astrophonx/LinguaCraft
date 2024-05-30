package dev.astrophonx.linguacraft.renderes;

import dev.astrophonx.linguacraft.LinguaCraft;
import dev.astrophonx.linguacraft.eventHandlers.ChatEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = LinguaCraft.MODID, value = Dist.CLIENT)
public class ChatBubbleRenderer {

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Post event) {
        Player player = event.getPlayer();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        EntityRenderDispatcher renderManager = Minecraft.getInstance().getEntityRenderDispatcher();

        UUID playerUUID = player.getUUID();
        List<String> chatBubbles = ChatEventHandler.getPlayerChatBubbles().get(playerUUID);
        if (chatBubbles != null && !chatBubbles.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.0D, player.getBbHeight() + 0.75D, 0.0D); // Adjust position above player
            poseStack.mulPose(renderManager.cameraOrientation());
            poseStack.scale(-0.025F, -0.025F, 0.025F);

            renderChatBubbles(poseStack, buffer, chatBubbles);

            poseStack.popPose();
        }
    }

    private static void renderChatBubbles(PoseStack poseStack, MultiBufferSource buffer, List<String> chatBubbles) {
        Font font = Minecraft.getInstance().font;
        int lineHeight = 10; // Height of each line
        int bubbleSpacing = 3; // Spacing between bubbles

        int backgroundPadding = 4;

        // Limit the chat bubbles to be rendered to the most recent MAX_CHAT_BUBBLES
        int numBubbles = Math.min(chatBubbles.size(), 3);
        int yOffset = 0;
        for (int i = 0; i < numBubbles; i++) {
            String chatBubble = chatBubbles.get(numBubbles - 1 - i); // Render the most recent message at the top
            int bubbleHeight = lineHeight + backgroundPadding * 2;
            int backgroundWidth = font.width(chatBubble) + backgroundPadding * 2;
            renderBackground(poseStack, backgroundWidth, bubbleHeight, yOffset);
            font.drawInBatch(chatBubble, -font.width(chatBubble) / 2, yOffset + backgroundPadding + (bubbleHeight - lineHeight) / 2 - 2, 0xFFFFFF, false, poseStack.last().pose(), buffer, false, 0, 15728880);
            yOffset -= (bubbleHeight + bubbleSpacing);
        }
    }

    private static void renderBackground(PoseStack poseStack, int width, int height, int y) {
        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix, -width / 2.0F, y, 0).color(0, 0, 0, 77).endVertex(); // 77 alpha for 0.3 transparency
        bufferBuilder.vertex(matrix, -width / 2.0F, y + height, 0).color(0, 0, 0, 77).endVertex();
        bufferBuilder.vertex(matrix, width / 2.0F, y + height, 0).color(0, 0, 0, 77).endVertex();
        bufferBuilder.vertex(matrix, width / 2.0F, y, 0).color(0, 0, 0, 77).endVertex();
        tesselator.end();
        RenderSystem.disableBlend();
    }

}
