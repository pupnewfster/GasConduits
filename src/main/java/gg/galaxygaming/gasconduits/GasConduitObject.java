package gg.galaxygaming.gasconduits;

import com.enderio.core.common.util.NullHelper;
import crazypants.enderio.api.IModTileEntity;
import crazypants.enderio.base.EnderIOTab;
import crazypants.enderio.base.init.IModObjectBase;
import crazypants.enderio.base.init.ModObjectRegistry;
import crazypants.enderio.base.init.RegisterModObject;
import gg.galaxygaming.gasconduits.common.ItemGasFilter;
import gg.galaxygaming.gasconduits.conduit.ItemGasConduit;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum GasConduitObject implements IModObjectBase {
    itemGasConduit(ItemGasConduit.class),
    itemGasFilter(ItemGasFilter.class);

    public static void registerBlocksEarly(@Nonnull RegisterModObject event) {
        event.register(GasConduitObject.class);
    }

    @Nonnull
    final String unlocalisedName;

    @Nullable
    protected Block block;
    @Nullable
    protected Item item;

    @Nonnull
    protected final Class<?> clazz;
    @Nullable
    protected final String blockMethodName, itemMethodName;
    @Nullable
    protected final IModTileEntity modTileEntity;

    GasConduitObject(@Nonnull Class<?> clazz) {
        this(clazz, "create", null);
    }

    GasConduitObject(@Nonnull Class<?> clazz, @Nonnull String methodName, @Nullable IModTileEntity modTileEntity) {
        this.unlocalisedName = ModObjectRegistry.sanitizeName(NullHelper.notnullJ(name(), "Enum.name()"));
        this.clazz = clazz;
        if (Block.class.isAssignableFrom(clazz)) {
            this.blockMethodName = methodName;
            this.itemMethodName = null;
        } else if (Item.class.isAssignableFrom(clazz)) {
            this.blockMethodName = null;
            this.itemMethodName = methodName;
        } else {
            throw new RuntimeException("Clazz " + clazz + " unexpectedly is neither a Block nor an Item.");
        }
        this.modTileEntity = modTileEntity;
    }

    @Nonnull
    @Override
    public Class<?> getClazz() {
        return clazz;
    }

    @Override
    public void setItem(@Nullable Item obj) {
        this.item = obj;
    }

    @Override
    public void setBlock(@Nullable Block obj) {
        this.block = obj;
    }

    @Nonnull
    @Override
    public String getUnlocalisedName() {
        return unlocalisedName;
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(GasConduitsConstants.MOD_ID, getUnlocalisedName());
    }

    @Nullable
    @Override
    public Block getBlock() {
        return block;
    }

    @Nullable
    @Override
    public Item getItem() {
        return item;
    }

    @Override
    @Nullable
    public IModTileEntity getTileEntity() {
        return modTileEntity;
    }

    @Nonnull
    @Override
    public final <B extends Block> B apply(@Nonnull B blockIn) {
        blockIn.setCreativeTab(EnderIOTab.tabEnderIOConduits);
        return IModObjectBase.super.apply(blockIn);
    }

    @Override
    @Nullable
    public String getBlockMethodName() {
        return blockMethodName;
    }

    @Override
    @Nullable
    public String getItemMethodName() {
        return itemMethodName;
    }
}