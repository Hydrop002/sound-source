package org.utm.soundsource;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;
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
    public static final Lazy<KeyMapping> key = Lazy.of(() -> new KeyMapping(
            "key.soundsource.show",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.misc"
    ));

    public SoundSourceMod(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::registerBindings);
        NeoForge.EVENT_BUS.register(this);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        Minecraft.getInstance().getSoundManager().addListener(renderer);
    }

    public void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(key.get());
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

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        while (key.get().consumeClick()) {
            renderer.isRender = !renderer.isRender;
        }
    }
}
