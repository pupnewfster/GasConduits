package gg.galaxygaming.gasconduits.client;

import com.enderio.core.client.gui.button.IconButton;
import crazypants.enderio.base.filter.gui.AbstractFilterGui;
import crazypants.enderio.base.filter.gui.ContainerFilter;
import crazypants.enderio.base.filter.gui.FilterGuiUtil;
import crazypants.enderio.base.gui.IconEIO;
import crazypants.enderio.base.integration.jei.GhostSlotTarget;
import crazypants.enderio.base.lang.Lang;
import gg.galaxygaming.gasconduits.client.utils.GasRenderUtil;
import gg.galaxygaming.gasconduits.common.filter.GasFilter;
import gg.galaxygaming.gasconduits.common.filter.IGasFilter;
import mekanism.api.gas.GasStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class GasFilterGui extends AbstractFilterGui {
    private static final int ID_WHITELIST = FilterGuiUtil.nextButtonId();

    private final IconButton whiteListB;
    @Nonnull
    private final GasFilter filter;

    private int xOffset;
    private int yOffset;

    public GasFilterGui(@Nonnull InventoryPlayer playerInv, @Nonnull ContainerFilter filterContainer, TileEntity te, @Nonnull IGasFilter filterIn) {
        super(playerInv, filterContainer, te, filterIn, "gas_filter");

        xOffset = 13;
        yOffset = 34;

        filter = (GasFilter) filterIn;

        whiteListB = new IconButton(this, ID_WHITELIST, xOffset + 98, yOffset + 1, IconEIO.FILTER_WHITELIST);
        whiteListB.setToolTip(Lang.GUI_ITEM_FILTER_WHITELIST.get());
    }

    @Override
    public void initGui() {
        createFilterSlots();
        super.initGui();
    }

    public void createFilterSlots() {
        filter.createGhostSlots(getGhostSlotHandler().getGhostSlots(), xOffset + 1, yOffset + 1, this::sendFilterChange);
    }

    @Override
    public void updateButtons() {
        super.updateButtons();
        whiteListB.onGuiInit();
        if (filter.isBlacklist()) {
            whiteListB.setIcon(IconEIO.FILTER_BLACKLIST);
            whiteListB.setToolTip(Lang.GUI_ITEM_FILTER_BLACKLIST.get());
        } else {
            whiteListB.setIcon(IconEIO.FILTER_WHITELIST);
            whiteListB.setToolTip(Lang.GUI_ITEM_FILTER_WHITELIST.get());
        }
    }

    @Override
    protected void actionPerformed(@Nonnull GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.id == ID_WHITELIST) {
            filter.setBlacklist(!filter.isBlacklist());
            sendFilterChange();
        }
    }

    @Override
    public void renderCustomOptions(int top, float par1, int par2, int par3) {
        int x = getGuiLeft() + xOffset;
        int y = getGuiTop() + yOffset;
        GlStateManager.color(1, 1, 1);
        bindGuiTexture();

        if (!filter.isEmpty()) {
            for (int i = 0; i < filter.size(); i++) {
                GasStack g = filter.getGasStackAt(i);
                if (g != null) {
                    renderGas(g, x + (i * 18), y);
                }
            }
        }
    }

    private void renderGas(GasStack g, int x, int y) {
        ResourceLocation iconKey = g.getGas().getIcon();
        TextureAtlasSprite icon = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(iconKey.toString());
        if (icon != null) {
            GasRenderUtil.renderGuiTank(g, 1000, 1000, x + 1, y + 1, 0, 16, 16);
        }
    }

    @Override
    @Nonnull
    protected String getUnlocalisedNameForHeading() {
        return new TextComponentTranslation("gasconduits.gui.gas_filter").getUnformattedComponentText();
    }

    @Override
    @Nonnull
    public List<GhostSlotTarget<?>> getGhostTargets() {
        return getGhostSlotHandler().getGhostSlots().stream().map(slot -> new GhostSlotTarget<>(filter, slot, getGuiLeft(), getGuiTop(), this)).collect(Collectors.toList());
    }
}