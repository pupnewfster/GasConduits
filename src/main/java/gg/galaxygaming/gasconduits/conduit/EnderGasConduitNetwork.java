package gg.galaxygaming.gasconduits.conduit;

import com.enderio.core.common.util.RoundRobinIterator;
import crazypants.enderio.base.conduit.item.FunctionUpgrade;
import crazypants.enderio.base.conduit.item.ItemFunctionUpgrade;
import crazypants.enderio.conduits.conduit.AbstractConduitNetwork;
import gg.galaxygaming.gasconduits.GasConduitConfig;
import gg.galaxygaming.gasconduits.common.IGasFilter;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTankInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.*;

public class EnderGasConduitNetwork extends AbstractConduitNetwork<IGasConduit, EnderGasConduit> {

    List<NetworkTank> tanks = new ArrayList<>();
    Map<NetworkTankKey, NetworkTank> tankMap = new HashMap<>();

    Map<NetworkTank, RoundRobinIterator<NetworkTank>> iterators;

    boolean filling;

    public EnderGasConduitNetwork() {
        super(EnderGasConduit.class, IGasConduit.class);
    }

    public void connectionChanged(@Nonnull EnderGasConduit con, @Nonnull EnumFacing conDir) {
        NetworkTankKey key = new NetworkTankKey(con, conDir);
        NetworkTank tank = new NetworkTank(con, conDir);

        // Check for later
        boolean sort = false;
        NetworkTank oldTank = tankMap.get(key);
        if (oldTank != null && oldTank.priority != tank.priority) {
            sort = true;
        }

        tanks.remove(tank); // remove old tank, NB: =/hash is only calced on location and dir
        tankMap.remove(key);
        tanks.add(tank);
        tankMap.put(key, tank);

        // If the priority has been changed, then sort the list to match
        if (sort) {
            tanks.sort((arg0, arg1) -> arg1.priority - arg0.priority);
        }
    }

    public boolean extractFrom(@Nonnull EnderGasConduit con, @Nonnull EnumFacing conDir) {
        NetworkTank tank = getTank(con, conDir);
        if (!tank.isValid()) {
            return false;
        }

        //TODO
        GasTankInfo[] tankInfo = tank.externalTank.getTankInfo();
        int stored = 0;
        Gas type = null;
        for (GasTankInfo info : tankInfo) {
            stored += info.getStored();
            if (info.getGas() != null) {
                type = info.getGas().getGas();
            }
        }

        if (type == null) {
            return false;
        }
        GasStack drained = new GasStack(type, stored);

        //GasStack drained = tank.externalTank.stored;
        if (drained == null || drained.amount <= 0 || !matchedFilter(drained, con, conDir, true)) {
            return false;
        }

        drained = drained.copy();
        drained.amount = Math.min(drained.amount, GasConduitConfig.tier3_extractRate * getExtractSpeedMultiplier(tank) / 2);
        int amountAccepted = fillFrom(tank, drained.copy(), true);
        if (amountAccepted <= 0) {
            return false;
        }
        drained.amount = amountAccepted;
        drained = tank.externalTank.drawGas(tank.conDir.getOpposite(), drained.amount, true);
        if (drained == null || drained.amount <= 0) {
            return false;
        }
        // if(drained.amount != amountAccepted) {
        // Log.warn("EnderGasConduit.extractFrom: Extracted gas volume is not equal to inserted volume. Drained=" + drained.amount + " filled="
        // + amountAccepted + " Gas: " + drained + " Accepted=" + amountAccepted);
        // }
        return true;
    }

    @Nonnull
    private NetworkTank getTank(@Nonnull EnderGasConduit con, @Nonnull EnumFacing conDir) {
        return tankMap.get(new NetworkTankKey(con, conDir));
    }

    public int fillFrom(@Nonnull EnderGasConduit con, @Nonnull EnumFacing conDir, GasStack resource, boolean doFill) {
        return fillFrom(getTank(con, conDir), resource, doFill);
    }

    public int fillFrom(@Nonnull NetworkTank tank, GasStack resource, boolean doFill) {

        if (filling) {
            return 0;
        }

        try {

            filling = true;

            if (resource == null || !matchedFilter(resource, tank.con, tank.conDir, true)) {
                return 0;
            }

            resource = resource.copy();
            resource.amount = Math.min(resource.amount, GasConduitConfig.tier3_maxIO * getExtractSpeedMultiplier(tank) / 2);
            int filled = 0;
            int remaining = resource.amount;
            // TODO: Only change starting pos of iterator is doFill is true so a false then true returns the same

            for (NetworkTank target : getIteratorForTank(tank)) {
                if ((!target.equals(tank) || tank.selfFeed) && target.acceptsOuput && target.isValid() && target.inputColor == tank.outputColor
                        && matchedFilter(resource, target.con, target.conDir, false)) {
                    int vol = target.externalTank.receiveGas(target.conDir.getOpposite(), resource.copy(), doFill);
                    remaining -= vol;
                    filled += vol;
                    if (remaining <= 0) {
                        return filled;
                    }
                    resource.amount = remaining;
                }
            }
            return filled;

        } finally {
            if (!tank.roundRobin) {
                getIteratorForTank(tank).reset();
            }
            filling = false;
        }
    }

    private int getExtractSpeedMultiplier(NetworkTank tank) {
        int extractSpeedMultiplier = 2;

        ItemStack upgradeStack = tank.con.getFunctionUpgrade(tank.conDir);
        if (!upgradeStack.isEmpty()) {
            FunctionUpgrade upgrade = ItemFunctionUpgrade.getFunctionUpgrade(upgradeStack);
            if (upgrade == FunctionUpgrade.EXTRACT_SPEED_UPGRADE) {
                extractSpeedMultiplier += FunctionUpgrade.LIQUID_MAX_EXTRACTED_SCALER * Math.min(upgrade.maxStackSize, upgradeStack.getCount());
            } else if (upgrade == FunctionUpgrade.EXTRACT_SPEED_DOWNGRADE) {
                extractSpeedMultiplier = 1;
            }
        }

        return extractSpeedMultiplier;
    }

    private boolean matchedFilter(GasStack drained, @Nonnull EnderGasConduit con, @Nonnull EnumFacing conDir, boolean isInput) {
        if (drained == null) {
            return false;
        }
        IGasFilter filter = con.getFilter(conDir, isInput);
        if (filter == null || filter.isEmpty()) {
            return true;
        }
        return filter.matchesFilter(drained);
    }

    private RoundRobinIterator<NetworkTank> getIteratorForTank(@Nonnull NetworkTank tank) {
        if (iterators == null) {
            iterators = new HashMap<>();
        }
        RoundRobinIterator<NetworkTank> res = iterators.get(tank);
        if (res == null) {
            res = new RoundRobinIterator<>(tanks);
            iterators.put(tank, res);
        }
        return res;
    }

    public GasTankInfo[] getTankProperties(@Nonnull EnderGasConduit con, @Nonnull EnumFacing conDir) {
        List<GasTankInfo> res = new ArrayList<>(tanks.size());
        NetworkTank tank = getTank(con, conDir);
        for (NetworkTank target : tanks) {
            if (!target.equals(tank) && target.isValid()) {
                res.addAll(Arrays.asList(target.externalTank.getTankInfo()));
            }
        }
        return res.toArray(new GasTankInfo[res.size()]);
    }

    static class NetworkTankKey {

        EnumFacing conDir;
        BlockPos conduitLoc;

        public NetworkTankKey(@Nonnull EnderGasConduit con, @Nonnull EnumFacing conDir) {
            this(con.getBundle().getLocation(), conDir);
        }

        public NetworkTankKey(@Nonnull BlockPos conduitLoc, @Nonnull EnumFacing conDir) {
            this.conDir = conDir;
            this.conduitLoc = conduitLoc;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((conDir == null) ? 0 : conDir.hashCode());
            result = prime * result + ((conduitLoc == null) ? 0 : conduitLoc.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            NetworkTankKey other = (NetworkTankKey) obj;
            if (conDir != other.conDir) {
                return false;
            }
            if (conduitLoc == null) {
                if (other.conduitLoc != null) {
                    return false;
                }
            } else if (!conduitLoc.equals(other.conduitLoc)) {
                return false;
            }
            return true;
        }

    }

}
