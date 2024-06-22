package org.utm.soundsource;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(SoundSourceMod.MODID)
public class SoundSourceMod
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "soundsource";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final SoundSourceRenderer renderer = new SoundSourceRenderer();

    public SoundSourceMod(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::onClientSetup);
        NeoForge.EVENT_BUS.register(this);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        Minecraft.getInstance().getSoundManager().addListener(renderer);
    }

    @SubscribeEvent
    public void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            PoseStack stack = event.getPoseStack();
            //Frustum frustum = new Frustum(event.getProjectionMatrix(), stack.last().pose());
            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            renderer.render(camera, stack, bufferSource);
        }
    }
}
