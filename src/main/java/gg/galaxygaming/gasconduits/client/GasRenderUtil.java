package gg.galaxygaming.gasconduits.client;

import com.enderio.core.client.render.RenderUtil;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class GasRenderUtil {
    public static @Nonnull TextureAtlasSprite getStillTexture(@Nonnull GasStack gasStack) {
        final Gas gas = gasStack.getGas();
        if (gas == null) {
            return RenderUtil.getMissingSprite();
        }
        return getStillTexture(gas);
    }

    public static @Nonnull TextureAtlasSprite getStillTexture(@Nonnull Gas gas) {
        ResourceLocation iconKey = gas.getIcon();
        final TextureAtlasSprite textureExtry = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(iconKey.toString());
        return textureExtry != null ? textureExtry : RenderUtil.getMissingSprite();
    }
}