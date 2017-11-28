package dimdoors.common.blocks;


import dimdoors.common.core.DDTeleporter;
import dimdoors.common.core.DimLink;
import dimdoors.common.core.LinkType;
import dimdoors.common.core.PocketManager;
import dimdoors.common.core.RiftRegenerator;
import dimdoors.common.items.ItemDDKey;
import dimdoors.common.tileentity.TileEntityDimDoor;
import dimdoors.common.util.DimensionPos;
import dimdoors.registry.DimSounds;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Random;

public abstract class BaseDimDoor extends BlockDoor implements IDimDoor, ITileEntityProvider {

	public BaseDimDoor(Material material) {
		super(material);
	}

	protected static boolean isEntityFacingDoor(IBlockState state, EntityLivingBase entity) {
		// Although any entity has the proper fields for this check,
		// we should only apply it to living entities since things
		// like Minecarts might come in backwards.
		return (state.getValue(FACING) == EnumFacing.fromAngle(entity.rotationYaw));
	}

	/*@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) { TODO
		upperTextures = new IIcon[2];
		lowerTextures = new IIcon[2];
		upperTextures[0] = iconRegister.registerIcon(mod_pocketDim.modid + ":" + this.getUnlocalizedName() + "_upper");
		lowerTextures[0] = iconRegister.registerIcon(mod_pocketDim.modid + ":" + this.getUnlocalizedName() + "_lower");
		upperTextures[1] = new IconFlipped(upperTextures[0], true, false);
		lowerTextures[1] = new IconFlipped(lowerTextures[0], true, false);
	}*/

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
		this.enterDimDoor(world, pos, entity);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

		ItemStack stack = player.inventory.getCurrentItem();
		/*if (stack.getItem() instanceof ItemDDKey) { TODO
			return false;
		}*/

		if (!checkCanOpen(world, pos, player)) {
			return false;
		}


		BlockPos blockpos = state.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER ? pos : pos.down();
		IBlockState iblockstate = pos.equals(blockpos) ? state : world.getBlockState(blockpos);

		if (iblockstate.getBlock() != this) {
			return false;
		} else {
			state = iblockstate.cycleProperty(OPEN);
			world.setBlockState(blockpos, state, 10);
			world.markBlockRangeForRenderUpdate(blockpos, pos);
			world.playEvent(player, state.getValue(OPEN) ? 1006 : 1012, pos, 0);
			return true;
		}
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		this.placeLink(world, pos);
		world.setTileEntity(pos, this.createNewTileEntity(world, getMetaFromState(state)));
		this.updateAttachedTile(world, pos);
	}

	/**
	 * Retrieves the block texture to use based on the display side. Args: iBlockAccess, pos, side
	 */
	/*@Override TODO
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess blockAccess, BlockPos pos, int side) {
		if (side != 1 && side != 0) {
			int fullMetadata = func_150012_g(blockAccess, pos);
			int orientation = fullMetadata & 3;
			boolean reversed = false;

			if (isDoorOpen(fullMetadata)) {
				if (orientation == 0 && side == 2) {
					reversed = !reversed;
				} else if (orientation == 1 && side == 5) {
					reversed = !reversed;
				} else if (orientation == 2 && side == 3) {
					reversed = !reversed;
				} else if (orientation == 3 && side == 4) {
					reversed = !reversed;
				}
			} else {
				if (orientation == 0 && side == 5) {
					reversed = !reversed;
				} else if (orientation == 1 && side == 3) {
					reversed = !reversed;
				} else if (orientation == 2 && side == 4) {
					reversed = !reversed;
				} else if (orientation == 3 && side == 2) {
					reversed = !reversed;
				}

				if ((fullMetadata & 16) != 0) {
					reversed = !reversed;
				}
			}
			if (isUpperDoorBlock(fullMetadata)) {
				return this.upperTextures[reversed ? 1 : 0];
			}
			return this.lowerTextures[reversed ? 1 : 0];
		}
		return this.lowerTextures[0];
	}*/

	//Called to update the render information on the tile entity. Could probably implement a data watcher,
	//but this works fine and is more versatile I think.
	public BaseDimDoor updateAttachedTile(World world, BlockPos pos) {
		/*DimDoors.proxy.updateDoorTE(this, world, pos); TODO
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileEntityDimDoor) {
			int metadata = world.getBlockMetadata(pos);
			TileEntityDimDoor dimTile = (TileEntityDimDoor) tile;
			dimTile.openOrClosed = isDoorOnRift(world, pos) && isUpperDoorBlock(metadata);
			dimTile.orientation = this.func_150012_g(world, pos) & 7;
		}*/
		return this;
	}

	@Override
	public boolean isDoorOnRift(World world, BlockPos pos) {
		return this.getLink(world, pos) != null;
	}

	public DimLink getLink(World world, BlockPos pos) {
		DimLink link = PocketManager.getLink(new DimensionPos(pos, world.provider.getDimension()));
		if (link != null) {
			return link;
		}

		if (isUpperDoorBlock(world.getBlockState(pos))) {
			link = PocketManager.getLink(new DimensionPos(pos.down(), world.provider.getDimension()));
			if (link != null) {
				return link;
			}
		} else {
			link = PocketManager.getLink(new DimensionPos(pos.up(), world.provider.getDimension()));
			if (link != null) {
				return link;
			}
		}
		return null;
	}

	/**
	 * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
	 * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
	 */
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		this.updateAttachedTile(world, pos);
	}

	/*@Override
	public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
		this.setDoorRotation(func_150012_g(par1IBlockAccess, par2, par3, par4));
	}*/

	/*public void setDoorRotation(int par1) {
		float var2 = 0.1875F;
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F);
		int var3 = par1 & 3;
		boolean var4 = (par1 & 4) != 0;
		boolean var5 = (par1 & 16) != 0;

		if (var3 == 0) {
			if (var4) {
				if (!var5) {
					this.setBlockBounds(0.001F, 0.0F, 0.0F, 1.0F, 1.0F, var2);
				} else {
					this.setBlockBounds(0.001F, 0.0F, 1.0F - var2, 1.0F, 1.0F, 1.0F);
				}
			} else {
				this.setBlockBounds(0.0F, 0.0F, 0.0F, var2, 1.0F, 1.0F);
			}
		} else if (var3 == 1) {
			if (var4) {
				if (!var5) {
					this.setBlockBounds(1.0F - var2, 0.0F, 0.001F, 1.0F, 1.0F, 1.0F);
				} else {
					this.setBlockBounds(0.0F, 0.0F, 0.001F, var2, 1.0F, 1.0F);
				}
			} else {
				this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, var2);
			}
		} else if (var3 == 2) {
			if (var4) {
				if (!var5) {
					this.setBlockBounds(0.0F, 0.0F, 1.0F - var2, .99F, 1.0F, 1.0F);
				} else {
					this.setBlockBounds(0.0F, 0.0F, 0.0F, .99F, 1.0F, var2);
				}
			} else {
				this.setBlockBounds(1.0F - var2, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
			}
		} else if (var3 == 3) {
			if (var4) {
				if (!var5) {
					this.setBlockBounds(0.0F, 0.0F, 0.0F, var2, 1.0F, 0.99F);
				} else {
					this.setBlockBounds(1.0F - var2, 0.0F, 0.0F, 1.0F, 1.0F, 0.99F);
				}
			} else {
				this.setBlockBounds(0.0F, 0.0F, 1.0F - var2, 1.0F, 1.0F, 1.0F);
			}
		}
	}*/

	/**
	 * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
	 * their own) Args: pos, neighbor blockID
	 */
	/*@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		IBlockState state = world.getBlockState(pos);
		if (isUpperDoorBlock(state)) {
			if (world.getBlockState(pos.down()) != this) {
				world.setBlockToAir(pos);
			}
			if (!neighbor.isAir(world, pos) && neighbor != this) {
				this.onNeighborBlockChange(world, x, y - 1, z, neighbor);
			}
		} else {
			if (world.getBlock(x, y + 1, z) != this) {
				world.setBlockToAir(pos);
				if (!world.isRemote) {
					this.dropBlockAsItem(world, pos, metadata, 0);
				}
			} else if (this.getLockStatus(world, pos) <= 1) {
				boolean powered = world.isBlockIndirectlyGettingPowered(pos) || world.isBlockIndirectlyGettingPowered(x, y + 1, z);
				if ((powered || !neighbor.isAir(world, pos) && neighbor.canProvidePower()) && neighbor != this) {
					this.func_150014_a(world, pos, powered);
				}
			}
		}
	}*/

	/**
	 * only called by clickMiddleMouseButton , and passed to inventory.setCurrentItem (along with isCreative)
	 */
	@Nonnull
	@Override
	public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, EntityPlayer player) {
		return new ItemStack(getDoorItem());
	}

	/**
	 * Returns the ID of the items to drop on destruction.
	 */
	@Nonnull
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return isUpperDoorBlock(state) ? Items.AIR : getDoorItem();
	}

	@Override
	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
		return new ItemStack(getDoorItem());
	}

	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int metadata) {
		return new TileEntityDimDoor();
	}

	@Override
	public void enterDimDoor(World world, BlockPos pos, Entity entity) {
		// FX entities dont exist on the server
		if (world.isRemote) {
			return;
		}
		IBlockState state = world.getBlockState(pos.down());
		// Check that this is the top block of the door
		if (state.getBlock() == this) {
			boolean canUse = isDoorOpen(state);
			if (canUse && entity instanceof EntityPlayer) {
				// Dont check for non-player entites
				canUse = isEntityFacingDoor(state, (EntityLivingBase) entity);
			}
			if (canUse) {
				// Teleport the entity through the link, if it exists
				DimLink link = PocketManager.getLink(new DimensionPos(pos, world.provider.getDimension()));
				if (link != null && (link.linkType() != LinkType.PERSONAL || entity instanceof EntityPlayer)) {
					try {
						DDTeleporter.traverseDimDoor(world, link, entity, this);
					} catch (Exception e) {
						System.err.println("Something went wrong teleporting to a dimension:");
						e.printStackTrace();
					}
				}

				// Close the door only after the entity goes through
				// so players don't have it slam in their faces.
				this.toggleDoor(world, pos, false);
			}
		} else if (world.getBlockState(pos.up()) == this) {
			enterDimDoor(world, pos.up(), entity);
		}
	}

	public boolean isUpperDoorBlock(IBlockState state) {
		return state.getValue(HALF) == EnumDoorHalf.UPPER;
	}

	public boolean isDoorOpen(IBlockState state) {
		return state.getValue(OPEN);
	}

	/**
	 * 0 if link is no lock;
	 * 1 if there is a lock;
	 * 2 if the lock is locked.
	 */
	public byte getLockStatus(World world, BlockPos pos) {
		byte status = 0;
		DimLink link = getLink(world, pos);
		if (link != null && link.hasLock()) {
			status++;
			if (link.getLockState()) {
				status++;
			}
		}
		return status;
	}

	public boolean checkCanOpen(World world, BlockPos pos) {
		return this.checkCanOpen(world, pos, null);
	}

	public boolean checkCanOpen(World world, BlockPos pos, EntityPlayer player) {
		DimLink link = getLink(world, pos);
		if (link == null || player == null) {
			return link == null;
		}
		if (!link.getLockState()) {
			return true;
		}

		for (ItemStack item : player.inventory.mainInventory) {
			if (item.getItem() instanceof ItemDDKey) {
				if (link.tryToOpen(item)) {
					return true;
				}
			}
		}
		player.playSound(DimSounds.doorlocked, 1F, 1F);
		return false;
	}

	@Override
	public TileEntity initDoorTE(World world, BlockPos pos) {
		TileEntity te = this.createNewTileEntity(world, 0);
		world.setTileEntity(pos, te);
		return te;
	}

	@Override
	public void breakBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		// This function runs on the server side after a block is replaced
		// We MUST call super.breakBlock() since it involves removing tile entities
		super.breakBlock(world, pos, state);

		// Schedule rift regeneration for this block if it was replaced
		if (world.getBlockState(pos) != state) {
			RiftRegenerator.scheduleFastRegeneration(pos, world);
		}
	}
}