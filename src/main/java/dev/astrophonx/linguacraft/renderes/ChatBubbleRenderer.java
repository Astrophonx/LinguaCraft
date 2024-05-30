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
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = LinguaCraft.MODID, value = Dist.CLIENT)
public class ChatBubbleRenderer {

    private static final ResourceLocation CHAT_BUBBLE_TEXTURE = new ResourceLocation(LinguaCraft.MODID, "textures/gui/chat_bubble.png");

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

            // Calculate the height of the nameplate
            double nameplateHeight = player.getBbHeight() + 0.25D;

            poseStack.translate(0.0D, nameplateHeight, 0.0D); // Adjust position above nameplate
            poseStack.mulPose(renderManager.cameraOrientation());
            poseStack.scale(-0.025F, -0.025F, 0.025F);

            renderChatBubbles(poseStack, buffer, chatBubbles);

            poseStack.popPose();
        }
    }

    private static void renderChatBubbles(PoseStack poseStack, MultiBufferSource buffer, List<String> chatBubbles) {
        Font font = Minecraft.getInstance().font;
        int maxLineWidth = chatBubbles.stream().mapToInt(font::width).max().orElse(0);
        int lineHeight = 10; // Height of each line

        int backgroundPadding = 4;
        int backgroundHeight = chatBubbles.size() * lineHeight + backgroundPadding * 2;
        int backgroundWidth = maxLineWidth + backgroundPadding * 2;

        // Render background
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, CHAT_BUBBLE_TEXTURE);
        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(matrix, -backgroundWidth / 2.0F, -backgroundHeight, 0).uv(0, 1).color(255, 255, 255, 255).endVertex();
        bufferBuilder.vertex(matrix, -backgroundWidth / 2.0F, 0, 0).uv(0, 0).color(255, 255, 255, 255).endVertex();
        bufferBuilder.vertex(matrix, backgroundWidth / 2.0F, 0, 0).uv(1, 0).color(255, 255, 255, 255).endVertex();
        bufferBuilder.vertex(matrix, backgroundWidth / 2.0F, -backgroundHeight, 0).uv(1, 1).color(255, 255, 255, 255).endVertex();
        tesselator.end();

        // Render each line of chat
        int y = -backgroundHeight + backgroundPadding;
        for (String chatBubble : chatBubbles) {
            font.drawInBatch(chatBubble, -font.width(chatBubble) / 2, y, 0xFFFFFF, false, poseStack.last().pose(), buffer, false, 0, 15728880);
            y += lineHeight;
        }
    }
}
