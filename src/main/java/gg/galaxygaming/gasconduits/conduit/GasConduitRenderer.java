package gg.galaxygaming.gasconduits.conduit;

import com.enderio.core.client.render.BoundingBox;
import com.enderio.core.client.render.RenderUtil;
import com.enderio.core.common.util.NNList;
import com.enderio.core.common.util.NNList.NNIterator;
import com.enderio.core.common.vecmath.Vector3d;
import com.enderio.core.common.vecmath.Vector4f;
import com.enderio.core.common.vecmath.Vertex;
import crazypants.enderio.base.conduit.IClientConduit;
import crazypants.enderio.base.conduit.IConduit;
import crazypants.enderio.base.conduit.IConduitBundle;
import crazypants.enderio.base.conduit.IConduitTexture;
import crazypants.enderio.base.conduit.geom.CollidableComponent;
import crazypants.enderio.base.conduit.geom.ConduitGeometryUtil;
import crazypants.enderio.conduits.render.DefaultConduitRenderer;
import gg.galaxygaming.gasconduits.client.GasRenderUtil;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class GasConduitRenderer extends DefaultConduitRenderer implements IResourceManagerReloadListener {

    private float downRatio;

    private float flatRatio;

    private float upRatio;

    private GasConduitRenderer() {
        super();
    }

    public static @Nonnull
    GasConduitRenderer create() {
        GasConduitRenderer result = new GasConduitRenderer();
        RenderUtil.registerReloadListener(result);
        return result;
    }

    @Override
    public boolean isRendererForConduit(@Nonnull IConduit conduit) {
        if (conduit instanceof GasConduit) {
            return true;
        }
        return false;
    }

    @Override
    protected @Nonnull
    BlockRenderLayer getConduitQuadsLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    protected void addTransmissionQuads(@Nonnull IConduitTexture tex, Vector4f color, @Nonnull BlockRenderLayer layer, @Nonnull IConduit conduit,
                                        @Nonnull CollidableComponent component, float selfIllum, @Nonnull List<BakedQuad> quads) {
        // Handled in dynamic render
    }

    @Override
    protected void renderConduitDynamic(@Nonnull IConduitTexture tex, @Nonnull IClientConduit.WithDefaultRendering conduit,
                                        @Nonnull CollidableComponent component, float brightness) {
        if (component.isDirectional()) {
            GasConduit lc = (GasConduit) conduit;
            GasStack gas = lc.getGasType();
            if (gas != null) {
                renderGasOutline(component, gas);
            }
        }
    }

    @Override
    public void renderDynamicEntity(@Nonnull TileEntitySpecialRenderer<?> conduitBundleRenderer, @Nonnull IConduitBundle te,
                                    @Nonnull IClientConduit.WithDefaultRendering conduit, double x, double y, double z, float partialTick, float worldLight) {
        calculateRatios((GasConduit) conduit);
        super.renderDynamicEntity(conduitBundleRenderer, te, conduit, x, y, z, partialTick, worldLight);
    }

    @Override
    protected void renderTransmissionDynamic(@Nonnull IConduit conduit, @Nonnull IConduitTexture tex, @Nullable Vector4f color,
                                             @Nonnull CollidableComponent component, float selfIllum) {

        final float filledRatio = ((GasConduit) conduit).getTank().getFilledRatio();
        if (filledRatio <= 0) {
            return;
        }

        if (component.isDirectional()) {
            TextureAtlasSprite sprite = tex.getSprite();
            BoundingBox[] cubes = toCubes(component.bound);
            for (BoundingBox cube : cubes) {
                if (cube != null) {

                    float shrink = 1 / 128f;
                    final EnumFacing componentDirection = component.getDirection();
                    float xLen = Math.abs(componentDirection.getXOffset()) == 1 ? 0 : shrink;
                    float yLen = Math.abs(componentDirection.getYOffset()) == 1 ? 0 : shrink;
                    float zLen = Math.abs(componentDirection.getZOffset()) == 1 ? 0 : shrink;

                    BoundingBox bb = cube.expand(-xLen, -yLen, -zLen);

                    // TODO: This leaves holes between conduits as it only render 4 sides instead of the needed 5-6 sides
                    drawDynamicSection(bb, sprite.getInterpolatedU(tex.getUv().x * 16), sprite.getInterpolatedU(tex.getUv().z * 16),
                            sprite.getInterpolatedV(tex.getUv().y * 16), sprite.getInterpolatedV(tex.getUv().w * 16), color, componentDirection, true);
                }
            }

        } else {
            // TODO: HL: I commented this out because component.getDirection() (the second to last parameter) is always null in
            // this else branch and drawDynamicSection() with isTransmission=true (last parameter) would NPE on it. (Not a
            // mistake in the component.dir encapsulation, this was that way before.)
            // drawDynamicSection(component.bound, tex.getMinU(), tex.getMaxU(), tex.getMinV(), tex.getMaxV(), color, component.getDir(), true);
        }
    }

    public static void renderGasOutline(@Nonnull CollidableComponent component, @Nonnull GasStack gas) {
        renderGasOutline(component, gas, 1 - ConduitGeometryUtil.getHeight(), 1f / 16f);
    }

    public static void renderGasOutline(@Nonnull CollidableComponent component, @Nonnull GasStack gasStack, double scaleFactor, float outlineWidth) {
        final Gas gas = gasStack.getGas();
        if (gas != null) {
            for (CachableRenderStatement elem : computeGasOutlineToCache(component, gas, scaleFactor, outlineWidth)) {
                elem.execute();
            }
        }
    }

    // TODO: (1) CollidableComponent is a bad key for a weak reference
    // (2) CachableRenderStatement is an outdated class, use HalfBakedList instead
    // (3) could this be done more efficiently (on the fly)?
    private static Map<CollidableComponent, Map<Gas, List<CachableRenderStatement>>> cache = new WeakHashMap<>();

    public static List<CachableRenderStatement> computeGasOutlineToCache(@Nonnull CollidableComponent component, @Nonnull Gas gas, double scaleFactor,
                                                                         float outlineWidth) {

        Map<Gas, List<CachableRenderStatement>> cache0 = cache.computeIfAbsent(component, k -> new HashMap<>());

        List<CachableRenderStatement> data = cache0.get(gas);
        if (data != null) {
            return data;
        }
        data = new ArrayList<>();
        cache0.put(gas, data);

        TextureAtlasSprite texture = GasRenderUtil.getStillTexture(gas);
        int color = gas.getTint();
        Vector4f colorv = new Vector4f((color >> 16 & 0xFF) / 255d, (color >> 8 & 0xFF) / 255d, (color & 0xFF) / 255d, 1);

        BoundingBox bbb;

        double width = outlineWidth;
        scaleFactor = scaleFactor - 0.05;
        final EnumFacing componentDirection = component.getDirection();
        double xScale = Math.abs(componentDirection.getXOffset()) == 1 ? width : scaleFactor;
        double yScale = Math.abs(componentDirection.getYOffset()) == 1 ? width : scaleFactor;
        double zScale = Math.abs(componentDirection.getZOffset()) == 1 ? width : scaleFactor;

        double offSize = (0.5 - width) / 2 - width / 2;
        double xOff = componentDirection.getXOffset() * offSize;
        double yOff = componentDirection.getYOffset() * offSize;
        double zOff = componentDirection.getZOffset() * offSize;

        bbb = component.bound.scale(xScale, yScale, zScale);
        bbb = bbb.translate(new Vector3d(xOff, yOff, zOff));

        for (NNIterator<EnumFacing> itr = NNList.FACING.fastIterator(); itr.hasNext(); ) {
            EnumFacing face = itr.next();
            if (face != componentDirection && face != componentDirection.getOpposite()) {
                List<Vertex> corners = bbb.getCornersWithUvForFace(face, texture.getMinU(), texture.getMaxU(), texture.getMinV(), texture.getMaxV());
                for (Vertex corner : corners) {
                    data.add(new CachableRenderStatement.AddVertexWithUV(corner.x(), corner.y(), corner.z(), corner.uv.x, corner.uv.y, colorv));
                }
            }
        }
        return data;
    }

    private interface CachableRenderStatement {
        void execute();

        class AddVertexWithUV implements CachableRenderStatement {
            private final double x, y, z, u, v;
            private final Vector4f color;

            private AddVertexWithUV(double x, double y, double z, double u, double v, Vector4f color) {
                this.x = x;
                this.y = y;
                this.z = z;
                this.u = u;
                this.v = v;
                this.color = color;
            }

            @Override
            public void execute() {
                Tessellator.getInstance().getBuffer().pos(x, y, z).tex(u, v).color(color.x, color.y, color.z, color.w).endVertex();
            }
        }
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    protected void setVerticesForTransmission(@Nonnull BoundingBox bound, @Nonnull EnumFacing id) {

        float yScale = getRatioForConnection(id);
        float scale = 0.7f;
        float xs = id.getXOffset() == 0 ? scale : 1;
        float ys = id.getYOffset() == 0 ? Math.min(yScale, scale) : yScale;
        float zs = id.getZOffset() == 0 ? scale : 1;

        double sizeY = bound.sizeY();
        bound = bound.scale(xs, ys, zs);
        double transY = (bound.sizeY() - sizeY) / 2;
        Vector3d translation = new Vector3d(0, transY + 0.025, 0);
        setupVertices(bound.translate(translation));
    }

    private void calculateRatios(GasConduit conduit) {
        ConduitTank tank = conduit.getTank();
        int totalAmount = tank.getStored();

        int upCapacity = 0;
        if (conduit.containsConduitConnection(EnumFacing.UP) || conduit.containsExternalConnection(EnumFacing.UP)) {
            upCapacity = GasConduit.VOLUME_PER_CONNECTION;
        }
        int downCapacity = 0;
        if (conduit.containsConduitConnection(EnumFacing.DOWN) || conduit.containsExternalConnection(EnumFacing.DOWN)) {
            downCapacity = GasConduit.VOLUME_PER_CONNECTION;
        }

        int flatCapacity = tank.getMaxGas() - upCapacity - downCapacity;

        int usedCapacity = 0;
        if (downCapacity > 0) {
            int inDown = Math.min(totalAmount, downCapacity);
            usedCapacity += inDown;
            downRatio = (float) inDown / downCapacity;
        }
        if (flatCapacity > 0 && usedCapacity < totalAmount) {
            int inFlat = Math.min(flatCapacity, totalAmount - usedCapacity);
            usedCapacity += inFlat;
            flatRatio = (float) inFlat / flatCapacity;
        } else {
            flatRatio = 0;
        }
        if (upCapacity > 0 && usedCapacity < totalAmount) {
            int inUp = Math.min(upCapacity, totalAmount - usedCapacity);
            upRatio = (float) inUp / upCapacity;
        } else {
            upRatio = 0;
        }

    }

    private float getRatioForConnection(EnumFacing id) {
        if (id == EnumFacing.UP) {
            return upRatio;
        }
        if (id == EnumFacing.DOWN) {
            return downRatio;
        }
        return flatRatio;
    }

    // TODO: ModelBakeEvent would be better
    @Override
    public void onResourceManagerReload(@Nonnull IResourceManager p_110549_1_) {
        cache.clear();
    }

}
