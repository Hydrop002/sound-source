package org.utm.soundsource;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class SoundSourceRenderer implements SoundEventListener {

    private final List<SoundSource> sources = Lists.newArrayList();
    private static final Random rand = new Random();
    public boolean isRender = true;

    @Override
    public void onPlaySound(SoundInstance sound, WeighedSoundEvents accessor, float range) {
        Component text = accessor.getSubtitle();
        if (text == null) {
            text = Component.literal(sound.getSound().getLocation().toString());
        }
        this.sources.add(new SoundSource(text, new Vec3(sound.getX(), sound.getY(), sound.getZ())));
    }

    public void render(Camera camera, PoseStack stack, MultiBufferSource.BufferSource bufferSource) {
        Font font = Minecraft.getInstance().font;
        Vec3 cameraPos = camera.getPosition();
        Quaternionf cameraRot = camera.rotation();
        Iterator<SoundSource> it = this.sources.iterator();
        while (it.hasNext()) {
            SoundSource source = it.next();
            double time = (Util.getMillis() - source.startTime) / 1000.0;
            float scale = (float) (time < 0.5 ? hermite(0, 1, time * 2) : time > 2.5 ? hermite(1, 0, (time - 2.5) * 2) : 1);
            if (time >= 3) {
                it.remove();
                continue;
            }
            if (!this.isRender) continue;
            int width = font.width(source.text);
            stack.pushPose();
            stack.translate(source.pos.x - cameraPos.x, source.pos.y - cameraPos.y, source.pos.z - cameraPos.z);
            stack.mulPose(cameraRot);
            stack.scale(scale, scale, scale);
            VertexConsumer consumer = bufferSource.getBuffer(RenderTypeDialog.DIALOG);
            consumer.vertex(stack.last(), -width * 0.0125F - 0.035F, 0.435F, 0.01F).color(source.color).endVertex();
            consumer.vertex(stack.last(), -width * 0.0125F - 0.035F, 0.165F, 0.01F).color(source.color).endVertex();
            consumer.vertex(stack.last(), width * 0.0125F + 0.035F, 0.165F, 0.01F).color(source.color).endVertex();
            consumer.vertex(stack.last(), width * 0.0125F + 0.035F, 0.435F, 0.01F).color(source.color).endVertex();
            bufferSource.endLastBatch();
            consumer = bufferSource.getBuffer(RenderTypeDialog.DIALOG);
            consumer.vertex(stack.last(), -width * 0.0125F - 0.025F, 0.425F, 0.005F).color(0xF0100010).endVertex();
            consumer.vertex(stack.last(), -width * 0.0125F - 0.025F, 0.175F, 0.005F).color(0xF0100010).endVertex();
            consumer.vertex(stack.last(), width * 0.0125F + 0.025F, 0.175F, 0.005F).color(0xF0100010).endVertex();
            consumer.vertex(stack.last(), width * 0.0125F + 0.025F, 0.425F, 0.005F).color(0xF0100010).endVertex();
            bufferSource.endLastBatch();
            consumer = bufferSource.getBuffer(RenderTypeDialog.DIALOG);
            consumer.vertex(stack.last(), 0.0F, 0.0F, 0.0F).color(0xF0100010).endVertex();
            consumer.vertex(stack.last(), -0.04F, 0.175F, 0.0F).color(0xF0100010).endVertex();
            consumer.vertex(stack.last(), 0.04F, 0.175F, 0.0F).color(0xF0100010).endVertex();
            bufferSource.endLastBatch();
            stack.scale(-0.025F, -0.025F, 0.025F);
            font.drawInBatch(source.text, -width / 2.0F, -16.0F, -1, false, stack.last().pose(), bufferSource, Font.DisplayMode.SEE_THROUGH, 0, 0xF000F0);
            stack.popPose();
        }
    }

    private static double hermite(double a, double b, double t) {
        t = (3 - 2 * t) * t * t;
        return a * (1 - t) + b * t;
    }

    private static class SoundSource {
        public Component text;
        public Vec3 pos;
        public long startTime;
        public int color;
        public SoundSource(Component text, Vec3 pos) {
            this.text = text;
            this.pos = pos;
            this.startTime = Util.getMillis();
            this.color = rand.nextInt(16777216) - 16777216;
        }
    }

    private static class RenderTypeDialog extends RenderType {
        public static final RenderType DIALOG = create(
                "dialog",
                DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.TRIANGLE_FAN,
                1536,
                false,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(POSITION_COLOR_SHADER)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(NO_DEPTH_TEST)
                        .setCullState(NO_CULL)
                        .createCompositeState(false)
        );
        public RenderTypeDialog(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
            super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
        }
    }

}
