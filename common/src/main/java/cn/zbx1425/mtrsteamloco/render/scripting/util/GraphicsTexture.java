package cn.zbx1425.mtrsteamloco.render.scripting.util;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.mixin.DynamicImageAccessor;
import cn.zbx1425.mtrsteamloco.mixin.NativeImageAccessor;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.awt.image.*;
import java.io.Closeable;
import java.nio.IntBuffer;
import java.util.UUID;

@SuppressWarnings("unused")
public class GraphicsTexture implements Closeable {

    private final DynamicTexture dynamicTexture;
    public final ResourceLocation identifier;

    public BufferedImage bufferedImage;
    public Graphics2D graphics;

    public final int width, height;

    public GraphicsTexture(int width, int height) {
        this.width = width;
        this.height = height;

        NativeImage backingNativeImage = new NativeImage(width, height, false);

        long pixelAddr = ((NativeImageAccessor)(Object)backingNativeImage).getPixels();
        IntBuffer target = MemoryUtil.memByteBuffer(pixelAddr, width * height * 4).asIntBuffer();
        DataBuffer dataBuffer = new IntBufDataBuffer(target, width * height);
        WritableRaster raster = Raster.createPackedRaster(dataBuffer, width, height, width,
                new int[] { 0xFF0000, 0xFF00, 0xFF, 0xFF000000 }, new Point(0, 0));
        bufferedImage = new BufferedImage(ColorModel.getRGBdefault(), raster, false, null);

        graphics = bufferedImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        dynamicTexture = new DynamicTexture(backingNativeImage);
        Minecraft.getInstance().execute(() -> {
            int prevTextureBinding = GL33.glGetInteger(GL33.GL_TEXTURE_BINDING_2D);
            dynamicTexture.bind();
            GL33.glTexParameteriv(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_SWIZZLE_RGBA,
                    new int[] { GL33.GL_BLUE, GL33.GL_GREEN, GL33.GL_RED, GL33.GL_ALPHA });
            GlStateManager._bindTexture(prevTextureBinding);
        });
        identifier = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, String.format("dynamic/graphics/%s", UUID.randomUUID()));
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().getTextureManager().register(identifier, dynamicTexture);
        });
    }

    public static BufferedImage createArgbBufferedImage(BufferedImage src) {
        BufferedImage newImage = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = newImage.createGraphics();
        graphics.drawImage(src, 0, 0, null);
        graphics.dispose();
        return newImage;
    }

    public void upload() {
        Minecraft.getInstance().execute(dynamicTexture::upload);
    }

    @Override
    public void close() {
        Minecraft.getInstance().execute(() -> {
            graphics.dispose();
            Minecraft.getInstance().getTextureManager().release(identifier);
        });
    }

    private static class IntBufDataBuffer extends DataBuffer {

        IntBuffer buffer;

        protected IntBufDataBuffer(IntBuffer buffer, int size) {
            super(DataBuffer.TYPE_INT, size);
            this.buffer = buffer;
        }

        @Override
        public int getElem(int bank, int i) {
            return buffer.get(i);
        }

        @Override
        public void setElem(int bank, int i, int val) {
            buffer.put(i, val);
        }
    }

}
