package gg.galaxygaming.gasconduits.conduit;

import crazypants.enderio.conduits.conduit.AbstractConduitNetwork;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;

import javax.annotation.Nonnull;

public class AbstractTankConduitNetwork<T extends AbstractTankConduit> extends AbstractConduitNetwork<IGasConduit, T> {

    protected GasStack gasType;
    protected boolean gasTypeLocked = false;

    protected AbstractTankConduitNetwork(@Nonnull Class<T> cl) {
        super(cl, IGasConduit.class);
    }

    public GasStack getGasType() {
        return gasType;
    }

    @Override
    public void addConduit(@Nonnull T con) {
        super.addConduit(con);
        con.setGasType(gasType);
        if (con.gasTypeLocked && !gasTypeLocked) {
            setGasTypeLocked(true);
        }
    }

    public boolean setGasType(GasStack newType) {
        if (gasType != null && gasType.isGasEqual(newType)) {
            return false;
        }
        if (newType != null) {
            gasType = newType.copy();
            gasType.amount = 0;
        } else {
            gasType = null;
        }
        for (AbstractTankConduit conduit : getConduits()) {
            conduit.setGasType(gasType);
        }
        return true;
    }

    public void setGasTypeLocked(boolean gasTypeLocked) {
        if (this.gasTypeLocked == gasTypeLocked) {
            return;
        }
        this.gasTypeLocked = gasTypeLocked;
        for (AbstractTankConduit conduit : getConduits()) {
            conduit.setGasTypeLocked(gasTypeLocked);
        }
    }

    public boolean canAcceptGas(GasStack acceptable) {
        return areGassesCompatable(gasType, acceptable);
    }

    public static boolean areGassesCompatable(GasStack a, GasStack b) {
        if (a == null || b == null) {
            return true;
        }
        return a.isGasEqual(b);
    }

    public static boolean areGassesCompatable(Gas a, Gas b) {
        if (a == null || b == null) {
            return true;
        }
        return a == b;
    }

    public static boolean areGassesCompatable(GasStack a, Gas b) {
        return a == null || areGassesCompatable(a.getGas(), b);
    }

    public static boolean areGassesCompatable(Gas a, GasStack b) {
        return areGassesCompatable(b, a);
    }

    public int getTotalVolume() {
        int totalVolume = 0;
        for (T con : getConduits()) {
            totalVolume += con.getTank().getStored();
        }
        return totalVolume;
    }

}
