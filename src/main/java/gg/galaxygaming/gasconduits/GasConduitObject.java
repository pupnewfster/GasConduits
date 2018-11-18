package gg.galaxygaming.gasconduits;

import crazypants.enderio.api.IModTileEntity;
import crazypants.enderio.base.EnderIOTab;
import crazypants.enderio.base.init.IModObjectBase;
import crazypants.enderio.base.init.ModObjectRegistry;
import crazypants.enderio.base.init.RegisterModObject;
import gg.galaxygaming.gasconduits.conduit.ItemGasConduit;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GasConduitObject implements IModObjectBase {
    private static GasConduitObject instance;

    public static void registerBlocksEarly(@Nonnull RegisterModObject event) {
        event.register(instance = new GasConduitObject());
    }

    final @Nonnull
    String unlocalisedName;

    protected @Nullable
    Block block;
    protected @Nullable
    Item item;

    protected final @Nonnull
    Class<?> clazz;
    protected final @Nullable
    String blockMethodName, itemMethodName;
    protected final @Nullable
    IModTileEntity modTileEntity;

    private GasConduitObject() {
        this.unlocalisedName = ModObjectRegistry.sanitizeName("item_gas_conduit");
        this.clazz = ItemGasConduit.class;
        if (Block.class.isAssignableFrom(this.clazz)) {
            this.blockMethodName = "create";
            this.itemMethodName = null;
        } else if (Item.class.isAssignableFrom(this.clazz)) {
            this.blockMethodName = null;
            this.itemMethodName = "create";
        } else {
            throw new RuntimeException("Clazz " + this.clazz + " unexpectedly is neither a Block nor an Item.");
        }
        this.modTileEntity = null;
    }

    @Override
    public @Nonnull
    Class<?> getClazz() {
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

    @Override
    public final @Nonnull
    <B extends Block> B apply(@Nonnull B blockIn) {
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

    public static GasConduitObject getInstance() {
        return instance;
    }
}