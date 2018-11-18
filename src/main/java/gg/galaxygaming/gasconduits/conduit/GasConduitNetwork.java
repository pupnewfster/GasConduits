package gg.galaxygaming.gasconduits.conduit;

import crazypants.enderio.base.conduit.ConduitUtil;
import crazypants.enderio.base.conduit.ConduitUtil.UnloadedBlockException;
import crazypants.enderio.base.diagnostics.Prof;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class GasConduitNetwork extends AbstractTankConduitNetwork<GasConduit> {

    public GasConduitNetwork() {
        super(GasConduit.class);
    }

    private int ticksEmpty = 0;

    private int maxFlowsPerTick = 10;
    private int lastFlowIndex = 0;

    private int lastPushToken = 0;

    private boolean inputLocked = false;

    public boolean lockNetworkForFill() {
        if (inputLocked) {
            return false;
        }
        inputLocked = true;
        return true;
    }

    public void unlockNetworkFromFill() {
        inputLocked = false;
    }

    @Override
    public void tickEnd(ServerTickEvent event, @Nullable Profiler profiler) {
        List<GasConduit> cons = getConduits();
        if (cons.isEmpty()) {
            return;
        }

        if (isEmpty()) {
            if (!gasTypeLocked && gasType != null) {
                ticksEmpty++;
                if (ticksEmpty > 40) {
                    setGasType(null);
                    ticksEmpty = 0;
                }
            }
            return;
        }

        ticksEmpty = 0;
        long curTime = cons.get(0).getBundle().getEntity().getWorld().getTotalWorldTime();

        if (gasType != null && gasType.getGas() != null && !isEmpty()) {
            if (curTime % 2 == 0) {
                Prof.start(profiler, "flow");
                doFlow();
                Prof.stop(profiler);
            }
        }
    }

    void addedFromExternal(int res) {
    }

    void outputedToExternal(int filled) {
    }

    int getNextPushToken() {
        return ++lastPushToken;
    }

    private boolean doFlow() {

        int pushToken = getNextPushToken();
        List<FlowAction> actions = new ArrayList<>();
        for (int i = 0; i < Math.min(maxFlowsPerTick, getConduits().size()); i++) {

            if (lastFlowIndex >= getConduits().size()) {
                lastFlowIndex = 0;
            }
            flowFrom(getConduits().get(lastFlowIndex), actions, pushToken);
            ++lastFlowIndex;

        }
        for (FlowAction action : actions) {
            action.apply();
        }

        boolean result = !actions.isEmpty();

        // Flush any tanks with a tiny bit left
        List<GasConduit> toEmpty = new ArrayList<>();
        for (GasConduit con : getConduits()) {
            if (con != null && con.getTank().getStored() < 10) {
                toEmpty.add(con);
            } else {
                // some of the conduits have gas left in them so don't do the final drawGas yet
                return result;
            }

        }
        if (toEmpty.isEmpty()) {
            return result;
        }

        List<LocatedGasHandler> externals = new ArrayList<>();
        for (AbstractTankConduit con : getConduits()) {
            Set<EnumFacing> extCons = con.getExternalConnections();

            for (EnumFacing dir : extCons) {
                if (con.canOutputToDir(dir)) {
                    IGasHandler externalTank = con.getExternalHandler(dir);
                    if (externalTank != null) {
                        externals.add(new LocatedGasHandler(externalTank, con.getBundle().getLocation().offset(dir), dir.getOpposite()));
                    }
                }
            }
        }
        if (externals.isEmpty()) {
            return result;
        }

        for (GasConduit con : toEmpty) {
            drainConduitToNearestExternal(con, externals);
        }

        return result;
    }

    @Override
    public void setGasTypeLocked(boolean gasTypeLocked) {
        super.setGasTypeLocked(gasTypeLocked);
        if (!gasTypeLocked && isEmpty()) {
            setGasType(null);
        }
    }

    private boolean isEmpty() {
        for (GasConduit con : getConduits()) {
            if (con.tank.getStored() > 0) {
                return false;
            }
        }
        return true;
    }

    private void drainConduitToNearestExternal(@Nonnull GasConduit con, List<LocatedGasHandler> externals) {
        BlockPos conPos = con.getBundle().getLocation();
        GasStack toDrain = con.getTank().getGas();
        if (toDrain == null) {
            return;
        }
        int closestDistance = Integer.MAX_VALUE;
        LocatedGasHandler closestTank = null;
        for (LocatedGasHandler lh : externals) {
            int distance = (int) lh.pos.distanceSq(conPos);
            if (distance < closestDistance && con.canOutputToDir(lh.dir.getOpposite())) {
                int couldFill = lh.tank.receiveGas(lh.dir, toDrain.copy(), false);
                if (couldFill > 0) {
                    closestTank = lh;
                    closestDistance = distance;
                }
            }
        }

        if (closestTank != null) {
            int filled = closestTank.tank.receiveGas(closestTank.dir, toDrain.copy(), true);
            con.getTank().addAmount(-filled);
        }

    }

    private void flowFrom(@Nonnull GasConduit con, List<FlowAction> actions, int pushPoken) {

        ConduitTank tank = con.getTank();
        int totalAmount = tank.getStored();
        if (totalAmount <= 0) {
            return;
        }

        int maxFlowVolume = 20;

        // First flow all we can down, then balance the rest
        if (con.getConduitConnections().contains(EnumFacing.DOWN)) {
            BlockPos pos = con.getBundle().getLocation().offset(EnumFacing.DOWN);
            IGasConduit dc = ConduitUtil.getConduit(con.getBundle().getEntity().getWorld(), pos, IGasConduit.class);
            if (dc instanceof GasConduit && getConduits().contains(dc)) {
                GasConduit downCon = (GasConduit) dc;
                int filled = downCon.receiveGas(EnumFacing.UP, tank.getGas().copy(), false, false, pushPoken);
                int actual = filled;
                actual = Math.min(actual, tank.getStored());
                actual = Math.min(actual, downCon.getTank().getNeeded());
                tank.addAmount(-actual);
                downCon.getTank().addAmount(actual);
            }
        }

        totalAmount = tank.getStored();
        if (totalAmount <= 0) {
            return;
        }
        GasStack available = tank.getGas();
        int totalRequested = 0;
        int numRequests = 0;
        // Then to external connections
        for (EnumFacing dir : con.getExternalConnections()) {
            if (con.canOutputToDir(dir)) {
                IGasHandler extCon = con.getExternalHandler(dir);
                if (extCon != null) {
                    int amount = extCon.receiveGas(dir, available.copy(), false);
                    if (amount > 0) {
                        totalRequested += amount;
                        numRequests++;
                    }
                }
            }
        }

        if (numRequests > 0) {
            int amountPerRequest = Math.min(totalAmount, totalRequested) / numRequests;
            amountPerRequest = Math.min(maxFlowVolume, amountPerRequest);

            GasStack requestSource = available.copy();
            requestSource.amount = amountPerRequest;
            for (EnumFacing dir : con.getExternalConnections()) {
                if (con.canOutputToDir(dir)) {
                    IGasHandler extCon = con.getExternalHandler(dir);
                    if (extCon != null) {
                        int amount = extCon.receiveGas(dir, requestSource.copy(), true);
                        if (amount > 0) {
                            outputedToExternal(amount);
                            tank.addAmount(-amount);
                        }
                    }
                }
            }
        }

        totalAmount = tank.getStored();
        if (totalAmount <= 0) {
            return;
        }
        int totalCapacity = tank.getMaxGas();

        try {
            BlockPos pos = con.getBundle().getLocation();
            Collection<IGasConduit> connections = ConduitUtil.getConnectedConduits(con.getBundle().getEntity().getWorld(), pos.getX(), pos.getY(), pos.getZ(),
                    IGasConduit.class);
            for (IGasConduit n : connections) {
                GasConduit neighbour = (GasConduit) n;
                if (canFlowTo(con, neighbour)) { // can only flow within same network
                    totalAmount += neighbour.getTank().getStored();
                    totalCapacity += neighbour.getTank().getMaxGas();
                }
            }

            float targetRatio = (float) totalAmount / totalCapacity;
            int flowVolume = (int) Math.floor((targetRatio - tank.getFilledRatio()) * tank.getMaxGas());
            flowVolume = Math.min(maxFlowVolume, flowVolume);

            if (Math.abs(flowVolume) < 2) {
                return; // dont bother with transfers of less than a thousands of a bucket
            }

            for (IGasConduit n : connections) {
                GasConduit neigbour = (GasConduit) n;
                if (canFlowTo(con, neigbour)) { // can only flow within same network
                    flowVolume = (int) Math.floor((targetRatio - neigbour.getTank().getFilledRatio()) * neigbour.getTank().getMaxGas());
                    if (flowVolume != 0) {
                        actions.add(new FlowAction(con, neigbour, flowVolume));
                    }
                }
            }
        } catch (UnloadedBlockException e) {
            // NOP, should be impossible
        }

    }

    private boolean canFlowTo(GasConduit con, GasConduit neighbour) {
        if (con == null || neighbour == null) {
            return false;
        }
        if (neighbour.getNetwork() != this) {
            return false;
        }
        if (neighbour.getBundle().getLocation().getY() > con.getBundle().getLocation().getY()) {
            return false;
        }
        float nr = neighbour.getTank().getFilledRatio();
        if (nr >= con.getTank().getFilledRatio()) {
            return false;
        }
        return true;
    }

    static class FlowAction {
        final GasConduit from;
        final GasConduit to;
        final int amount;

        FlowAction(GasConduit fromIn, GasConduit toIn, int amountIn) {
            if (amountIn < 0) {
                to = fromIn;
                from = toIn;
                amount = -amountIn;
            } else {
                to = toIn;
                from = fromIn;
                amount = amountIn;
            }
        }

        void apply() {
            if (amount != 0) {

                // don't take more than it has
                int actual = Math.min(amount, from.getTank().getStored());
                // and don't add more than it can take
                actual = Math.min(actual, to.getTank().getNeeded());

                if (from != null && to != null) {
                    from.getTank().addAmount(-actual);
                    to.getTank().addAmount(actual);
                }

            }
        }

    }

    static class LocatedGasHandler {
        final IGasHandler tank;
        final BlockPos pos;
        final EnumFacing dir;

        LocatedGasHandler(IGasHandler tank, BlockPos pos, EnumFacing dir) {
            this.tank = tank;
            this.pos = pos;
            this.dir = dir;
        }
    }

}
