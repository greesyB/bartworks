package com.github.bartimaeusnek.bartworks.common.tileentities.multis.mega;

import com.github.bartimaeusnek.bartworks.API.LoaderReference;
import com.github.bartimaeusnek.bartworks.util.BW_Tooltip_Reference;
import com.github.bartimaeusnek.bartworks.util.BW_Util;
import com.github.bartimaeusnek.bartworks.util.MegaUtils;
import com.github.bartimaeusnek.crossmod.tectech.TecTechEnabledMulti;
import com.github.bartimaeusnek.crossmod.tectech.helper.TecTechUtils;
import com.github.bartimaeusnek.crossmod.tectech.tileentites.tiered.LowPowerLaser;
import cpw.mods.fml.common.Optional;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.*;
import gregtech.api.util.GT_Utility;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static gregtech.api.enums.GT_Values.V;

@Optional.Interface(iface = "com.github.bartimaeusnek.crossmod.tectech.TecTechEnabledMulti", modid = "tectech", striprefs = true)
public abstract class GT_TileEntity_MegaMultiBlockBase<T extends GT_TileEntity_MegaMultiBlockBase<T>> extends GT_MetaTileEntity_EnhancedMultiBlockBase<T> implements TecTechEnabledMulti {

    protected GT_TileEntity_MegaMultiBlockBase(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_TileEntity_MegaMultiBlockBase(String aName) {
        super(aName);
    }

    long lEUt = 0;
    private int energyTier = -1;

    public ArrayList<Object> TTTunnels = new ArrayList<>();
    public ArrayList<Object> TTMultiAmp = new ArrayList<>();

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        this.lEUt = aNBT.getLong("lEUt");
    }

    @Override
    public void clearHatches() {
        super.clearHatches();
        this.energyTier = -1;
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setLong("lEUt", lEUt);
    }

    @SuppressWarnings("rawtypes")
    @Optional.Method(modid = "tectech")
    boolean areLazorsLowPowa() {
        Collection collection = this.getTecTechEnergyTunnels();
        if (!collection.isEmpty())
            for (Object tecTechEnergyMulti : collection)
                if (!(tecTechEnergyMulti instanceof LowPowerLaser))
                    return false;
        return true;
    }

    @Override
    @Optional.Method(modid = "tectech")
    public List<GT_MetaTileEntity_Hatch_Energy> getVanillaEnergyHatches() {
        return this.mEnergyHatches;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Optional.Method(modid = "tectech")
    public List getTecTechEnergyTunnels() {
        return TTTunnels;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Optional.Method(modid = "tectech")
    public List getTecTechEnergyMultis() {
        return TTMultiAmp;
    }

    @Override
    public boolean drainEnergyInput(long aEU) {
        if (LoaderReference.tectech)
            return TecTechUtils.drainEnergyMEBFTecTech(this, aEU);
        return MegaUtils.drainEnergyMegaVanilla(this, aEU);
    }

    @Override
    public long getMaxInputVoltage() {
        if (LoaderReference.tectech)
            return TecTechUtils.getMaxInputVoltage(this);
        return super.getMaxInputVoltage();
    }

    @Deprecated
    @Override
    protected void calculateOverclockedNessMulti(int aEUt, int aDuration, int mAmperage, long maxInputVoltage) {
        calculateOverclockedNessMultiInternal((long)aEUt, aDuration, maxInputVoltage, false);
    }

    @Deprecated
    @Override
    protected void calculatePerfectOverclockedNessMulti(int aEUt, int aDuration, int mAmperage, long maxInputVoltage) {
        calculateOverclockedNessMultiInternal((long)aEUt, aDuration, maxInputVoltage, true);
    }

    @Override
    public String[] getInfoData() {
        return LoaderReference.tectech ? this.getInfoDataArray(this) : super.getInfoData();
    }

    @Override
    public String[] getInfoDataArray(GT_MetaTileEntity_MultiBlockBase multiBlockBase) {
        int mPollutionReduction = 0;

        for (GT_MetaTileEntity_Hatch_Muffler tHatch : this.mMufflerHatches) {
            if (GT_MetaTileEntity_MultiBlockBase.isValidMetaTileEntity(tHatch)) {
                mPollutionReduction = Math.max(tHatch.calculatePollutionReduction(100), mPollutionReduction);
            }
        }

        long[] ttHatches = getCurrentInfoData();
        long storedEnergy = ttHatches[0];
        long maxEnergy = ttHatches[1];

        for (GT_MetaTileEntity_Hatch_Energy tHatch : this.mEnergyHatches) {
            if (GT_MetaTileEntity_MultiBlockBase.isValidMetaTileEntity(tHatch)) {
                storedEnergy += tHatch.getBaseMetaTileEntity().getStoredEU();
                maxEnergy += tHatch.getBaseMetaTileEntity().getEUCapacity();
            }
        }

        return new String[]{
            StatCollector.translateToLocal("GT5U.multiblock.Progress") + ": " +
                EnumChatFormatting.GREEN + GT_Utility.formatNumbers(this.mProgresstime / 20) + EnumChatFormatting.RESET + " s / " +
                EnumChatFormatting.YELLOW + GT_Utility.formatNumbers(this.mMaxProgresstime / 20) + EnumChatFormatting.RESET + " s",
            StatCollector.translateToLocal("GT5U.multiblock.energy") + ": " +
                EnumChatFormatting.GREEN + GT_Utility.formatNumbers(storedEnergy) + EnumChatFormatting.RESET + " EU / " +
                EnumChatFormatting.YELLOW + GT_Utility.formatNumbers(maxEnergy) + EnumChatFormatting.RESET + " EU",
            StatCollector.translateToLocal("GT5U.multiblock.usage") + ": " +
                EnumChatFormatting.RED + GT_Utility.formatNumbers(-this.lEUt) + EnumChatFormatting.RESET + " EU/t",
            StatCollector.translateToLocal("GT5U.multiblock.mei") + ": " +
                EnumChatFormatting.YELLOW + GT_Utility.formatNumbers(this.getMaxInputVoltage()) + EnumChatFormatting.RESET + " EU/t(*" + TecTechUtils.getMaxInputAmperage(this) + "A) " +
                StatCollector.translateToLocal("GT5U.machines.tier") + ": " +
                EnumChatFormatting.YELLOW + BW_Util.getTierNameFromVoltage(this.getMaxInputVoltage()) + EnumChatFormatting.RESET,
            StatCollector.translateToLocal("GT5U.multiblock.problems") + ": " +
                EnumChatFormatting.RED + (this.getIdealStatus() - this.getRepairStatus()) + EnumChatFormatting.RESET + " " +
                StatCollector.translateToLocal("GT5U.multiblock.efficiency") + ": " +
                EnumChatFormatting.YELLOW + (float) this.mEfficiency / 100.0F + EnumChatFormatting.RESET + " %",
            StatCollector.translateToLocal("GT5U.multiblock.pollution") + ": " +
                EnumChatFormatting.GREEN + mPollutionReduction + EnumChatFormatting.RESET + " %",
            BW_Tooltip_Reference.BW};
    }


    // Special overclocking to handle over MAX voltage
    protected byte calculateOverclockedNessMultiInternal(long aEUt, int aDuration, long maxInputVoltage, boolean perfectOC) {
        byte mTier = (byte) Math.max(0, BW_Util.getTier(maxInputVoltage)), overclockCount = 0;
        if (mTier == 0) {
            //Long time calculation
            long xMaxProgresstime = ((long) aDuration) << 1;
            if (xMaxProgresstime > Integer.MAX_VALUE - 1) {
                //make impossible if too long
                this.lEUt = Integer.MAX_VALUE - 1;
                this.mMaxProgresstime = Integer.MAX_VALUE - 1;
            } else {
                this.lEUt = aEUt >> 2;
                this.mMaxProgresstime = (int) xMaxProgresstime;
            }
        } else {
            //Long EUt calculation
            long xEUt = aEUt;
            //Isnt too low EUt check?
            long tempEUt = Math.max(xEUt, V[1]);

            this.mMaxProgresstime = aDuration;

            while (tempEUt <= BW_Util.getTierVoltage(mTier - 1)) {
                tempEUt <<= 2;//this actually controls overclocking
                //xEUt *= 4;//this is effect of everclocking
                this.mMaxProgresstime >>= perfectOC ? 2 : 1;//this is effect of overclocking
                xEUt = this.mMaxProgresstime <= 0 ? xEUt >> 1 : xEUt << 2;//U know, if the time is less than 1 tick make the machine use less power
                overclockCount++;
            }

            while (xEUt > maxInputVoltage && xEUt >= aEUt){
                //downclock one notch until we are good again, we have overshot.
                xEUt >>= 2;
                this.mMaxProgresstime <<= perfectOC ? 2 : 1;
                overclockCount--;
            }

            if (xEUt < aEUt){
                xEUt <<= 2;
                this.mMaxProgresstime >>= perfectOC ? 2 : 1;
                overclockCount++;
            }

            this.lEUt = xEUt;
            if (this.lEUt == 0)
                this.lEUt = 1;
            if (this.mMaxProgresstime <= 0)
                this.mMaxProgresstime = 1;//set time to 1 tick
        }
        return overclockCount;
    }


    protected void calculateOverclockedNessMulti(long aEUt, int aDuration, long maxInputVoltage){
        calculateOverclockedNessMultiInternal(aEUt, aDuration, maxInputVoltage, false);
    }

    protected void calculatePerfectOverclockedNessMulti(long aEUt, int aDuration, long maxInputVoltage){
        calculateOverclockedNessMultiInternal(aEUt, aDuration, maxInputVoltage, true);
    }

    @Override
    public boolean addEnergyInputToMachineList(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        if (LoaderReference.tectech) {
            int tier = TecTechUtils.addEnergyInputToMachineList(this, aTileEntity, aBaseCasingIndex, energyTier);
            if(energyTier == -1) energyTier = tier;
            return tier != -1;
        }
        else
        {
            if (aTileEntity == null) {
                return false;
            } else {
                IMetaTileEntity aMetaTileEntity = aTileEntity.getMetaTileEntity();
                if (aMetaTileEntity == null) {
                    return false;
                } else if (aMetaTileEntity instanceof GT_MetaTileEntity_Hatch_Energy) {
                    if(energyTier == -1)
                        energyTier = ((GT_MetaTileEntity_Hatch_Energy) aMetaTileEntity).mTier;
                    if(((GT_MetaTileEntity_Hatch_Energy) aMetaTileEntity).mTier != energyTier)
                        return false;
                    ((GT_MetaTileEntity_Hatch)aMetaTileEntity).updateTexture(aBaseCasingIndex);
                    return this.mEnergyHatches.add((GT_MetaTileEntity_Hatch_Energy)aMetaTileEntity);
                } else {
                    return false;
                }
            }
        }
    }

    @Override
    public void stopMachine() {
        this.lEUt = 0L;
        super.stopMachine();
    }

    @Override
    public boolean onRunningTick(ItemStack aStack) {
        if (this.lEUt > 0) {
            this.addEnergyOutput(this.lEUt * (long)this.mEfficiency / 10000L);
            return true;
        } else if (this.lEUt < 0 && !this.drainEnergyInput((-this.lEUt) * 10000L / (long)Math.max(1000, this.mEfficiency))) {
            this.stopMachine();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack itemStack) {
        return true;
    }

    @Override
    public int getDamageToComponent(ItemStack itemStack) {
        return 0;
    }

    @Override
    public boolean explodesOnComponentBreak(ItemStack itemStack) {
        return false;
    }

    @Override
    public int getMaxEfficiency(ItemStack itemStack) {
        return 10000;
    }

}
