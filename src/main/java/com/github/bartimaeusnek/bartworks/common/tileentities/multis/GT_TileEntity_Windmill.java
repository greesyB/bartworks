/*
 * Copyright (c) 2018-2020 bartimaeusnek
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.bartimaeusnek.bartworks.common.tileentities.multis;

import com.github.bartimaeusnek.bartworks.MainMod;
import com.github.bartimaeusnek.bartworks.client.gui.BW_GUIContainer_Windmill;
import com.github.bartimaeusnek.bartworks.common.tileentities.classic.BW_RotorBlock;
import com.github.bartimaeusnek.bartworks.server.container.BW_Container_Windmill;
import com.github.bartimaeusnek.bartworks.util.BW_Util;
import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.alignment.IAlignmentLimits;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.SubTag;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_EnhancedMultiBlockBase;
import gregtech.api.objects.XSTR;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_Multiblock_Tooltip_Builder;
import gregtech.api.util.GT_OreDictUnificator;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.github.bartimaeusnek.bartworks.util.BW_Tooltip_Reference.MULTIBLOCK_ADDED_BY_BARTWORKS;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static gregtech.api.enums.GT_Values.V;

public class GT_TileEntity_Windmill extends GT_MetaTileEntity_EnhancedMultiBlockBase<GT_TileEntity_Windmill> {

    private static final IIcon[] iIcons = new IIcon[2];
    private static final IIconContainer[] iIconContainers = new IIconContainer[2];
    private static final ITexture[] iTextures = new ITexture[3];

    private static final XSTR localRandomInstance = new XSTR();
    private BW_RotorBlock rotorBlock;
    private int mDoor = 0;
    private int mHardenedClay = 0;

    public GT_TileEntity_Windmill(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    private GT_TileEntity_Windmill(String aName) {
        super(aName);
    }

    private static final String STRUCTURE_PIECE_MAIN = "main";
    private static final IStructureDefinition<GT_TileEntity_Windmill> STRUCTURE_DEFINITION = StructureDefinition.<GT_TileEntity_Windmill>builder()
        .addShape(STRUCTURE_PIECE_MAIN, transpose(new String[][]{
            {"       ", "       ", "       ", "   p   ", "       ", "       ", "       "},
            {"       ", "       ", "  ppp  ", "  p p  ", "  ppp  ", "       ", "       "},
            {"       ", " ppppp ", " p   p ", " p   p ", " p   p ", " ppppp ", "       "},
            {" ppppp ", "p     p", "p     p", "p     p", "p     p", "p     p", " ppppp "},
            {" ppspp ", "p     p", "p     p", "p     p", "p     p", "p     p", " ppppp "},
            {" ppppp ", "p     p", "p     p", "p     p", "p     p", "p     p", " ppppp "},
            {"       ", " ppppp ", " p   p ", " p   p ", " p   p ", " ppppp ", "       "},
            {"       ", "  ccc  ", " c   c ", " c   c ", " c   c ", "  ccc  ", "       "},
            {"       ", "  ccc  ", " c   c ", " c   c ", " c   c ", "  ccc  ", "       "},
            {"       ", "  ccc  ", " c   c ", " c   c ", " c   c ", "  ccc  ", "       "},
            {"       ", "  ccc  ", " c   c ", " c   c ", " c   c ", "  ccc  ", "       "},
            {" bb~bb ", "bbbbbbb", "bbbbbbb", "bbbbbbb", "bbbbbbb", "bbbbbbb", " bbbbb "},
        }))
        .addElement('p', ofBlockAnyMeta(Blocks.planks))
        .addElement('c', ofChain(
            onElementPass(t -> t.mHardenedClay++, ofBlock(Blocks.hardened_clay, 0)),
            ofTileAdder(GT_TileEntity_Windmill::addDispenserToOutputSet, Blocks.hardened_clay, 0),
            onElementPass(t -> t.mDoor++, ofBlock(Blocks.wooden_door, 0))
        ))
        .addElement('b', ofBlock(Blocks.brick_block, 0))
        .addElement('s', ofTileAdder(GT_TileEntity_Windmill::setRotorBlock, StructureLibAPI.getBlockHint(), 0))
        .build();

    @Override
    public IStructureDefinition<GT_TileEntity_Windmill> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected IAlignmentLimits getInitialAlignmentLimits() {
        return (d, r, f) -> d.offsetY == 0 && r.isNotRotated() && f.isNotFlipped();
    }

    @Override
    protected GT_Multiblock_Tooltip_Builder createTooltip() {
        GT_Multiblock_Tooltip_Builder tt = new GT_Multiblock_Tooltip_Builder();
        tt.
            addMachineType("Windmill").
            addInfo("Controller block for the Windmill").
            addInfo("A primitive Grinder powered by Kinetic energy").
            addInfo("The structure is too complex!").
            addInfo("Follow the StructureLib hologram projector to build the main structure.").
            addSeparator().
            beginStructureBlock(7, 12, 7, false).
            addController("Front bottom center").
            addCasingInfo("Hardened Clay block", 40).
            addOtherStructurePart("Dispenser", "Any Hardened Clay block").
            addOtherStructurePart("0-1 Wooden door", "Any Hardened Clay block").
            addStructureHint("Primitive Kinetic Shaftbox", 1).
            toolTipFinisher(MULTIBLOCK_ADDED_BY_BARTWORKS);
        return tt;
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack itemStack) {
        return true;
    }

    private final Set<TileEntityDispenser> tileEntityDispensers = new HashSet<>();

    @Override
    public boolean onRunningTick(ItemStack aStack) {
        if (this.mMaxProgresstime > 0)
            this.mProgresstime += this.rotorBlock.getGrindPower();
        return this.rotorBlock.getGrindPower() > 0;
    }

    @Override
    public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, byte aSide, ItemStack aStack) {
        return true;
    }

    public boolean recipe_fallback(ItemStack aStack) {
        //sight... fallback to the macerator recipes
        GT_Recipe.GT_Recipe_Map tMap = GT_Recipe.GT_Recipe_Map.sMaceratorRecipes;
        GT_Recipe tRecipe = tMap.findRecipe(this.getBaseMetaTileEntity(), false, false, V[1], null, aStack);
        if (tRecipe == null)
            return false;
        if (tRecipe.getOutput(0) != null) {
            // Decrease input stack by appropriate amount (Not always 1)
            tRecipe.isRecipeInputEqual(true, null, aStack);
            this.mOutputItems[0] = tRecipe.getOutput(0);

            if (new XSTR().nextInt(2) == 0) {
                if (tRecipe.getOutput(1) != null)
                    this.mOutputItems[1] = tRecipe.getOutput(1);
                else if (!BW_Util.checkStackAndPrefix(this.mOutputItems[0]) ||
                        !(
                                BW_Util.checkStackAndPrefix(this.mOutputItems[0]) && GT_OreDictUnificator.getAssociation(this.mOutputItems[0]).mMaterial.mMaterial.mSubTags.contains(SubTag.METAL) ||
                                        BW_Util.checkStackAndPrefix(this.mOutputItems[0]) && GT_OreDictUnificator.getAssociation(this.mOutputItems[0]).mMaterial.mMaterial.mSubTags.contains(SubTag.CRYSTAL) ||
                                        BW_Util.checkStackAndPrefix(this.mOutputItems[0]) && GT_OreDictUnificator.getAssociation(this.mOutputItems[0]).mMaterial.mMaterial.mSubTags.contains(SubTag.CRYSTALLISABLE)
                        )
                        ||
                        BW_Util.checkStackAndPrefix(this.mOutputItems[0]) && GT_OreDictUnificator.getAssociation(this.mOutputItems[0]).mMaterial.mMaterial == Materials.Flint ||
                        BW_Util.checkStackAndPrefix(this.mOutputItems[0]) && GT_OreDictUnificator.getAssociation(this.mOutputItems[0]).mMaterial.mMaterial == Materials.Sugar ||
                        BW_Util.checkStackAndPrefix(this.mOutputItems[0]) && GT_OreDictUnificator.getAssociation(this.mOutputItems[0]).mMaterial.mMaterial == Materials.Wheat ||
                        BW_Util.checkStackAndPrefix(this.mOutputItems[0]) && GT_OreDictUnificator.getAssociation(this.mOutputItems[0]).mMaterial.mMaterial == Materials.Wood ||
                        BW_Util.checkStackAndPrefix(this.mOutputItems[0]) && GT_OreDictUnificator.getAssociation(this.mOutputItems[0]).mMaterial.mMaterial == Materials.Ash ||
                        BW_Util.checkStackAndPrefix(this.mOutputItems[0]) && GT_OreDictUnificator.getAssociation(this.mOutputItems[0]).mMaterial.mMaterial == Materials.Snow ||
                        BW_Util.checkStackAndPrefix(this.mOutputItems[0]) && GT_OreDictUnificator.getAssociation(this.mOutputItems[0]).mMaterial.mMaterial == Materials.Stone ||
                        BW_Util.checkStackAndPrefix(this.mOutputItems[0]) && GT_OreDictUnificator.getAssociation(this.mOutputItems[0]).mMaterial.mMaterial == Materials.MeatRaw ||
                        BW_Util.checkStackAndPrefix(this.mOutputItems[0]) && GT_OreDictUnificator.getAssociation(this.mOutputItems[0]).mMaterial.mMaterial == Materials.MeatCooked
                )
                    this.mOutputItems[1] = tRecipe.getOutput(0);
            }
        }
        this.mMaxProgresstime = (tRecipe.mDuration * 2 * 100);
        return true;
    }

    @Override
    public boolean doRandomMaintenanceDamage() {
        return true;
    }

    private boolean hardOverride(int maxProgresstime, boolean randomise, ItemStack input, ItemStack... outputs) {
        input.stackSize -= 1;
        this.mMaxProgresstime = maxProgresstime;
        if (randomise) {
            if (localRandomInstance.nextInt(2) == 0)
                this.mOutputItems[0] = outputs[0];
            else
                this.mOutputItems[0] = outputs[1];
        } else {
            this.mOutputItems[0] = outputs[0];
            if (outputs.length == 2)
                this.mOutputItems[1] = outputs[1];
        }
        return true;
    }

    @Override
    public boolean checkRecipe(ItemStack itemStack) {

        if (itemStack == null || itemStack.getItem() == null)
            return false;

        if (this.mOutputItems == null)
            this.mOutputItems = new ItemStack[2];

        //Override Recipes that doesnt quite work well with OreUnificator
        //Items
        if (itemStack.getItem().equals(Items.wheat)) {
            return hardOverride(
                    30000,
                    false,
                    itemStack,
                    GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Wheat, 1L),
                    GT_OreDictUnificator.get(OrePrefixes.dustSmall, Materials.Wheat, 1L));
        } else if (itemStack.getItem().equals(Items.bone)) {
            return hardOverride(
                    30000,
                    true,
                    itemStack,
                    new ItemStack(Items.dye, 4, 15),
                    new ItemStack(Items.dye, 3, 15));
        }
        //Blocks
        else if (Block.getBlockFromItem(itemStack.getItem()).equals(Blocks.gravel)) {
            return hardOverride(
                    60000,
                    true,
                    itemStack,
                    new ItemStack(Items.flint, 2),
                    new ItemStack(Items.flint));
        } else if (Block.getBlockFromItem(itemStack.getItem()).equals(Blocks.cobblestone) || Block.getBlockFromItem(itemStack.getItem()).equals(Blocks.stone)) {
            return hardOverride(
                    120000,
                    true,
                    itemStack,
                    GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Stone, 2L),
                    GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Stone, 1L));
        } else if (Block.getBlockFromItem(itemStack.getItem()).equals(Blocks.sandstone)) {
            return hardOverride(
                    120000,
                    true,
                    itemStack,
                    new ItemStack(Blocks.sand, 3),
                    new ItemStack(Blocks.sand, 2));
        } else if (Block.getBlockFromItem(itemStack.getItem()).equals(Blocks.clay) || Block.getBlockFromItem(itemStack.getItem()).equals(Blocks.hardened_clay) || Block.getBlockFromItem(itemStack.getItem()).equals(Blocks.stained_hardened_clay)) {
            return hardOverride(
                    120000,
                    true,
                    itemStack,
                    Materials.Clay.getDust(2),
                    Materials.Clay.getDust(1));
        } else if (Block.getBlockFromItem(itemStack.getItem()).equals(Blocks.redstone_block)) {
            return hardOverride(
                    120000,
                    false,
                    itemStack,
                    Materials.Redstone.getDust(9));
        } else if (Block.getBlockFromItem(itemStack.getItem()).equals(Blocks.glass)) {
            return hardOverride(
                    120000,
                    false,
                    itemStack,
                    (GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Glass, 1L)));
        } else if (Block.getBlockFromItem(itemStack.getItem()).equals(Blocks.wool)) {
            return hardOverride(
                    120000,
                    true,
                    itemStack,
                    new ItemStack(Items.string, 3),
                    new ItemStack(Items.string, 2));
        } else if (Block.getBlockFromItem(itemStack.getItem()).equals(Blocks.glowstone)) {
            return hardOverride(
                    120000,
                    true,
                    itemStack,
                    new ItemStack(Items.glowstone_dust, 4),
                    new ItemStack(Items.glowstone_dust, 3));
        } else if (Block.getBlockFromItem(itemStack.getItem()).equals(Blocks.netherrack)) {
            return hardOverride(
                    120000,
                    true,
                    itemStack,
                    (GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Netherrack, 2L)),
                    (GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Netherrack, 1L)));
        } else if (Block.getBlockFromItem(itemStack.getItem()).equals(Blocks.log)) {
            return hardOverride(
                    120000,
                    true,
                    itemStack,
                    (GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Wood, 12L)),
                    (GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Wood, 6L)));
        } else if (Block.getBlockFromItem(itemStack.getItem()).equals(Blocks.log2)) {
            return hardOverride(
                    120000,
                    true,
                    itemStack,
                    (GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Wood, 12L)),
                    (GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Wood, 6L)));
        } else if (Block.getBlockFromItem(itemStack.getItem()).equals(Blocks.pumpkin)) {
            return hardOverride(
                    30000,
                    true,
                    itemStack,
                    new ItemStack(Items.pumpkin_seeds, 2),
                    new ItemStack(Items.pumpkin_seeds, 1));
        } else if (Block.getBlockFromItem(itemStack.getItem()).equals(Blocks.melon_block)) {
            return hardOverride(
                    30000,
                    true,
                    itemStack,
                    new ItemStack(Items.melon_seeds, 2),
                    new ItemStack(Items.melon_seeds, 1));
        }

        //null checks for GT shit
        if (GT_OreDictUnificator.getAssociation(itemStack) == null ||
                GT_OreDictUnificator.getAssociation(itemStack).mPrefix == null ||
                GT_OreDictUnificator.getAssociation(itemStack).mMaterial == null ||
                GT_OreDictUnificator.getAssociation(itemStack).mMaterial.mMaterial == null ||
                GT_OreDictUnificator.getAssociation(itemStack).mMaterial.mMaterial.getDust(1) == null
        )
            return this.recipe_fallback(itemStack); //fallback for all non-unificated Items

        //Ore Unificator shit for balance
        if (OrePrefixes.ingot.equals(GT_OreDictUnificator.getAssociation(itemStack).mPrefix) || OrePrefixes.gem.equals(GT_OreDictUnificator.getAssociation(itemStack).mPrefix)) {
            return hardOverride(
                    90000,
                    false,
                    itemStack,
                    (GT_OreDictUnificator.get(OrePrefixes.dust, GT_OreDictUnificator.getAssociation(itemStack).mMaterial.mMaterial, 1L)));
        } else if (OrePrefixes.ore.equals(GT_OreDictUnificator.getAssociation(itemStack).mPrefix)) {
            return hardOverride(
                    120000,
                    false,
                    itemStack,
                    (GT_OreDictUnificator.get(OrePrefixes.crushed, GT_OreDictUnificator.getAssociation(itemStack).mMaterial.mMaterial, 1L)));
        } else if (OrePrefixes.nugget.equals(GT_OreDictUnificator.getAssociation(itemStack).mPrefix)) {
            return hardOverride(
                    30000,
                    false,
                    itemStack,
                    (GT_OreDictUnificator.get(OrePrefixes.dustTiny, GT_OreDictUnificator.getAssociation(itemStack).mMaterial.mMaterial, 1L)));
        } else if (OrePrefixes.crushed.equals(GT_OreDictUnificator.getAssociation(itemStack).mPrefix)) {
            return hardOverride(
                    60000,
                    false,
                    itemStack,
                    (GT_OreDictUnificator.get(OrePrefixes.dustImpure, GT_OreDictUnificator.getAssociation(itemStack).mMaterial.mMaterial, 1L)));
        } else if (OrePrefixes.crushedPurified.equals(GT_OreDictUnificator.getAssociation(itemStack).mPrefix)) {
            return hardOverride(
                    60000,
                    false,
                    itemStack,
                    (GT_OreDictUnificator.get(OrePrefixes.dustPure, GT_OreDictUnificator.getAssociation(itemStack).mMaterial.mMaterial, 1L)));
        } else if (OrePrefixes.crushedCentrifuged.equals(GT_OreDictUnificator.getAssociation(itemStack).mPrefix)) {
            return hardOverride(
                    60000,
                    false,
                    itemStack,
                    (GT_OreDictUnificator.get(OrePrefixes.dust, GT_OreDictUnificator.getAssociation(itemStack).mMaterial.mMaterial, 1L)));
        } else if (OrePrefixes.block.equals(GT_OreDictUnificator.getAssociation(itemStack).mPrefix)) {
            return hardOverride(
                    120000,
                    false,
                    itemStack,
                    (GT_OreDictUnificator.get(OrePrefixes.dust, GT_OreDictUnificator.getAssociation(itemStack).mMaterial.mMaterial, (GT_OreDictUnificator.getAssociation(itemStack).mMaterial.mMaterial.mSubTags.contains(SubTag.METAL) || GT_OreDictUnificator.getAssociation(itemStack).mMaterial.mMaterial.mSubTags.contains(SubTag.CRYSTAL)) ? 9L : 1L)));
        } else if (
                OrePrefixes.stone.equals(GT_OreDictUnificator.getAssociation(itemStack).mPrefix) ||
                        OrePrefixes.stoneBricks.equals(GT_OreDictUnificator.getAssociation(itemStack).mPrefix) ||
                        OrePrefixes.stoneChiseled.equals(GT_OreDictUnificator.getAssociation(itemStack).mPrefix) ||
                        OrePrefixes.stoneCobble.equals(GT_OreDictUnificator.getAssociation(itemStack).mPrefix) ||
                        OrePrefixes.stoneCracked.equals(GT_OreDictUnificator.getAssociation(itemStack).mPrefix) ||
                        OrePrefixes.stoneMossy.equals(GT_OreDictUnificator.getAssociation(itemStack).mPrefix) ||
                        OrePrefixes.stoneMossyBricks.equals(GT_OreDictUnificator.getAssociation(itemStack).mPrefix) ||
                        OrePrefixes.stoneSmooth.equals(GT_OreDictUnificator.getAssociation(itemStack).mPrefix) ||
                        OrePrefixes.cobblestone.equals(GT_OreDictUnificator.getAssociation(itemStack).mPrefix)
        ) {
            return hardOverride(
                    120000,
                    true,
                    itemStack,
                    (GT_OreDictUnificator.get(OrePrefixes.dust, GT_OreDictUnificator.getAssociation(itemStack).mMaterial.mMaterial, 2L)),
                    (GT_OreDictUnificator.get(OrePrefixes.dust, GT_OreDictUnificator.getAssociation(itemStack).mMaterial.mMaterial, 1L)));
        }
        return this.recipe_fallback(itemStack); //2nd fallback
    }

    @Override
    public Object getClientGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
        return new BW_GUIContainer_Windmill(aPlayerInventory, aBaseMetaTileEntity);
    }

    @Override
    public Object getServerGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
        return new BW_Container_Windmill(aPlayerInventory, aBaseMetaTileEntity);
    }

    public boolean addDispenserToOutputSet(TileEntity aTileEntity) {
        if (aTileEntity instanceof TileEntityDispenser) {
            this.tileEntityDispensers.add((TileEntityDispenser) aTileEntity);
            return true;
        }
        return false;
    }

    public boolean setRotorBlock(TileEntity aTileEntity){
        if (aTileEntity instanceof BW_RotorBlock) {
            this.rotorBlock = (BW_RotorBlock) aTileEntity;
            return true;
        }
        return false;
    }

    @SuppressWarnings("ALL")
    @Override
    public boolean addOutput(ItemStack aStack) {
        if (GT_Utility.isStackInvalid(aStack))
            return false;

        for (TileEntityDispenser tHatch : this.tileEntityDispensers) {
            for (int i = tHatch.getSizeInventory() - 1; i >= 0; i--) {
                if (tHatch.getStackInSlot(i) == null || GT_Utility.areStacksEqual(tHatch.getStackInSlot(i), aStack) && aStack.stackSize + tHatch.getStackInSlot(i).stackSize <= 64) {
                    if (GT_Utility.areStacksEqual(tHatch.getStackInSlot(i), aStack)) {
                        ItemStack merge = tHatch.getStackInSlot(i).copy();
                        merge.stackSize = aStack.stackSize + tHatch.getStackInSlot(i).stackSize;
                        tHatch.setInventorySlotContents(i, merge);
                    } else {
                        tHatch.setInventorySlotContents(i, aStack.copy());
                    }

                    if (GT_Utility.areStacksEqual(tHatch.getStackInSlot(i), aStack)) {
                        aStack = null;
                        return true;
                    } else {
                        tHatch.setInventorySlotContents(i, null);
                        aStack = null;
                        return false;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack itemStack) {

        this.tileEntityDispensers.clear();
        this.mDoor = 0;
        this.mHardenedClay = 0;

        if(!checkPiece(STRUCTURE_PIECE_MAIN, 3, 11, 0))
            return false;

        if (this.tileEntityDispensers.isEmpty() || this.mDoor > 2 || this.mHardenedClay < 40)
            return false;

        this.mWrench = true;
        this.mScrewdriver = true;
        this.mSoftHammer = true;
        this.mHardHammer = true;
        this.mSolderingTool = true;
        this.mCrowbar = true;

        return true;
    }


    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        this.mProgresstime++;
        if (aBaseMetaTileEntity.isServerSide()) {
            if (this.mEfficiency < 0)
                this.mEfficiency = 0;
            if (--this.mUpdate == 0 || --this.mStartUpCheck == 0 || this.mStructureChanged) {
                checkStructure(true, aBaseMetaTileEntity);
                this.mUpdate = 100;
            }
            if (this.mStartUpCheck < 0) {
                if (this.mMachine) {
                    if (this.mMaxProgresstime > 0) {
                        if (this.onRunningTick(this.mInventory[1])) {
                            if (this.mMaxProgresstime > 0 && this.mProgresstime >= this.mMaxProgresstime) {
                                if (this.mOutputItems != null)
                                    for (ItemStack tStack : this.mOutputItems)
                                        if (tStack != null) {
                                            this.addOutput(tStack);
                                        }
                                this.mEfficiency = 10000;
                                this.mOutputItems = new ItemStack[2];
                                this.mProgresstime = 0;
                                this.mMaxProgresstime = 0;
                                this.mEfficiencyIncrease = 0;
                                if (aBaseMetaTileEntity.isAllowedToWork()) {
                                    if (this.checkRecipe(this.mInventory[1]))
                                        this.updateSlots();
                                }
                            }
                        }
                    } else {
                        if (aTick % 100 == 0 || aBaseMetaTileEntity.hasWorkJustBeenEnabled() || aBaseMetaTileEntity.hasInventoryBeenModified()) {
                            if (aBaseMetaTileEntity.isAllowedToWork()) {
                                if (this.checkRecipe(this.mInventory[1]))
                                    this.updateSlots();
                            }
                        }
                    }
                } else {
                    //this.mMachine = this.checkMachine(aBaseMetaTileEntity, this.mInventory[1]);
                    return;
                }
            } else {
                this.stopMachine();
            }
        }
        aBaseMetaTileEntity.setErrorDisplayID((aBaseMetaTileEntity.getErrorDisplayID() & ~127) | (this.mMachine ? 0 : 64));
        aBaseMetaTileEntity.setActive(this.mMaxProgresstime > 0);
    }

    @Override
    public int getCurrentEfficiency(ItemStack itemStack) {
        return 10000;
    }

    @Override
    public void updateSlots() {
        if (this.mInventory[1] != null && this.mInventory[1].stackSize <= 0) {
            this.mInventory[1] = null;
        }
    }

    @Override
    public int getMaxEfficiency(ItemStack itemStack) {
        return 10000;
    }

    @Override
    public int getPollutionPerTick(ItemStack itemStack) {
        return 0;
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
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new GT_TileEntity_Windmill(this.mName);
    }

    @Override
    public String[] getInfoData() {
        return new String[]{"Progress:", this.mProgresstime + " Grindings of " + this.mMaxProgresstime + " needed Grindings", "GrindPower:", this.rotorBlock.getGrindPower() + "KU/t"};
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister aBlockIconRegister) {
        GT_TileEntity_Windmill.iIcons[0] = Blocks.brick_block.getIcon(0, 0);
        GT_TileEntity_Windmill.iIconContainers[0] = new IIconContainer() {
            @Override
            public IIcon getIcon() {
                return GT_TileEntity_Windmill.iIcons[0];
            }

            @Override
            public IIcon getOverlayIcon() {
                return null;
            }

            @Override
            public ResourceLocation getTextureFile() {
                return new ResourceLocation("brick");
            }
        };

        GT_TileEntity_Windmill.iIcons[1] = aBlockIconRegister.registerIcon(MainMod.MOD_ID + ":windmill_top");
        GT_TileEntity_Windmill.iIconContainers[1] = new IIconContainer() {
            @Override
            public IIcon getIcon() {
                return GT_TileEntity_Windmill.iIcons[1];
            }

            @Override
            public IIcon getOverlayIcon() {
                return null;
            }

            @Override
            public ResourceLocation getTextureFile() {
                return new ResourceLocation(MainMod.MOD_ID + ":windmill_top");
            }
        };


    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, byte aSide, byte aFacing, byte aColorIndex, boolean aActive, boolean aRedstone) {

        ITexture[] ret = new ITexture[6];

        if (this.isClientSide()) {

            if (aFacing == aSide || aSide == 0) {
                GT_TileEntity_Windmill.iTextures[0] = TextureFactory.of(GT_TileEntity_Windmill.iIconContainers[0]);
                Arrays.fill(ret, GT_TileEntity_Windmill.iTextures[0]);
            } else if (aSide == 1) {
                GT_TileEntity_Windmill.iTextures[1] = TextureFactory.of(GT_TileEntity_Windmill.iIconContainers[1]);
                Arrays.fill(ret, GT_TileEntity_Windmill.iTextures[1]);
            } else {
                GT_TileEntity_Windmill.iTextures[2] = TextureFactory.of(Textures.BlockIcons.COVER_WOOD_PLATE);
                Arrays.fill(ret, GT_TileEntity_Windmill.iTextures[2]);
            }
        }
        return ret;
    }

    public boolean isClientSide() {
        if (this.getBaseMetaTileEntity().getWorld() != null)
            return this.getBaseMetaTileEntity().getWorld().isRemote ? FMLCommonHandler.instance().getSide() == Side.CLIENT : FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
        return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
    }

    @Override
    public void construct(ItemStack itemStack, boolean b) {
        buildPiece(STRUCTURE_PIECE_MAIN, itemStack, b, 3, 11, 0);
    }
}
