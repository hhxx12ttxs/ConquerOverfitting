package moCreatures.entities;
// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode 

import java.util.List;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityAnimal;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.Item;
import net.minecraft.src.Material;
import net.minecraft.src.MathHelper;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

import moCreatures.mod_mocreatures;

public class EntityBird extends EntityAnimal
{

    public EntityBird(World world)
    {
        super(world);
        texture = "/moCreatures/textures/birdblue.png";
        setSize(0.5F, 0.3F);
        health = 2;
        isCollidedVertically = true;
        wingb = 0.0F;
        wingc = 0.0F;
        wingh = 1.0F;
        fleeing = false;
        tamed = false;
        typeint = 0;
        typechosen = false;
        hasreproduced = false;
    }

    protected void fall(float f)
    {
    }

    public int getMaxSpawnedInChunk()
    {
        return 6;
    }

    public void onLivingUpdate()
    {
        super.onLivingUpdate();
        winge = wingb;
        wingd = wingc;
        wingc = (float)((double)wingc + (double)(onGround ? -1 : 4) * 0.29999999999999999D);
        if(wingc < 0.0F)
        {
            wingc = 0.0F;
        }
        if(wingc > 1.0F)
        {
            wingc = 1.0F;
        }
        if(!onGround && wingh < 1.0F)
        {
            wingh = 1.0F;
        }
        wingh = (float)((double)wingh * 0.90000000000000002D);
        if(!onGround && motionY < 0.0D)
        {
            motionY *= 0.80000000000000004D;
        }
        wingb += wingh * 2.0F;
        EntityLiving entityliving = getClosestEntityLiving(this, 4D);
        if(entityliving != null && !tamed && canEntityBeSeen(entityliving))
        {
            fleeing = true;
        }
        if(rand.nextInt(300) == 0)
        {
            fleeing = true;
        }
        if(fleeing)
        {
            if(FlyToNextTree())
            {
                fleeing = false;
            }
            int ai[] = ReturnNearestMaterialCoord(this, Material.leaves, Double.valueOf(16D));
            if(ai[0] == -1)
            {
                for(int i = 0; i < 2; i++)
                {
                    WingFlap();
                }

                fleeing = false;
            }
            if(rand.nextInt(50) == 0)
            {
                fleeing = false;
            }
        }
        if(!fleeing)
        {
            EntityItem entityitem = getClosestSeeds(this, 12D);
            if(entityitem != null)
            {
                FlyToNextEntity(entityitem);
                EntityItem entityitem1 = getClosestSeeds(this, 1.0D);
                if(rand.nextInt(50) == 0 && entityitem1 != null)
                {
                    entityitem1.setEntityDead();
                    tamed = true;
                }
            }
        }
    }

    protected void updatePlayerActionState()
    {
        if(onGround && rand.nextInt(10) == 0 && (motionX > 0.050000000000000003D || motionZ > 0.050000000000000003D || motionX < -0.050000000000000003D || motionZ < -0.050000000000000003D))
        {
            motionY = 0.25D;
        }
        if(!fleeing)
        {
            super.updatePlayerActionState();
        }
    }

    public void setEntityDead()
    {
        if(tamed && health > 0)
        {
            return;
        } else
        {
            counterEntity--;
            super.setEntityDead();
            return;
        }
    }

    @SuppressWarnings("rawtypes")
	private EntityItem getClosestSeeds(Entity entity, double d)
    {
        double d1 = -1D;
        EntityItem entityitem = null;
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(d, d, d));
        for(int i = 0; i < list.size(); i++)
        {
            Entity entity1 = (Entity)list.get(i);
            if(!(entity1 instanceof EntityItem))
            {
                continue;
            }
            EntityItem entityitem1 = (EntityItem)entity1;
            if(entityitem1.item.itemID != Item.seeds.shiftedIndex)
            {
                continue;
            }
            double d2 = entityitem1.getDistanceSq(entity.posX, entity.posY, entity.posZ);
            if((d < 0.0D || d2 < d * d) && (d1 == -1D || d2 < d1))
            {
                d1 = d2;
                entityitem = entityitem1;
            }
        }

        return entityitem;
    }

    @SuppressWarnings("rawtypes")
	private EntityLiving getClosestEntityLiving(Entity entity, double d)
    {
        double d1 = -1D;
        EntityLiving entityliving = null;
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(d, d, d));
        for(int i = 0; i < list.size(); i++)
        {
            Entity entity1 = (Entity)list.get(i);
            if(!(entity1 instanceof EntityLiving) || (entity1 instanceof EntityBird))
            {
                continue;
            }
            double d2 = entity1.getDistanceSq(entity.posX, entity.posY, entity.posZ);
            if((d < 0.0D || d2 < d * d) && (d1 == -1D || d2 < d1) && ((EntityLiving)entity1).canEntityBeSeen(entity))
            {
                d1 = d2;
                entityliving = (EntityLiving)entity1;
            }
        }

        return entityliving;
    }

    private boolean FlyToNextEntity(Entity entity)
    {
        if(entity != null)
        {
            int i = MathHelper.floor_double(entity.posX);
            int j = MathHelper.floor_double(entity.posY);
            int k = MathHelper.floor_double(entity.posZ);
            faceTreeTop(i, j, k, 30F);
            if(MathHelper.floor_double(posY) < j)
            {
                motionY += 0.14999999999999999D;
            }
            if(posX < entity.posX)
            {
                double d = entity.posX - posX;
                if(d > 0.5D)
                {
                    motionX += 0.050000000000000003D;
                }
            } else
            {
                double d1 = posX - entity.posX;
                if(d1 > 0.5D)
                {
                    motionX -= 0.050000000000000003D;
                }
            }
            if(posZ < entity.posZ)
            {
                double d2 = entity.posZ - posZ;
                if(d2 > 0.5D)
                {
                    motionZ += 0.050000000000000003D;
                }
            } else
            {
                double d3 = posZ - entity.posZ;
                if(d3 > 0.5D)
                {
                    motionZ -= 0.050000000000000003D;
                }
            }
            return true;
        } else
        {
            return false;
        }
    }

    private void WingFlap()
    {
        motionY += 0.050000000000000003D;
        if(rand.nextInt(30) == 0)
        {
            motionX += 0.20000000000000001D;
        }
        if(rand.nextInt(30) == 0)
        {
            motionX -= 0.20000000000000001D;
        }
        if(rand.nextInt(30) == 0)
        {
            motionZ += 0.20000000000000001D;
        }
        if(rand.nextInt(30) == 0)
        {
            motionZ -= 0.20000000000000001D;
        }
    }

    private boolean FlyToNextTree()
    {
        int ai[] = ReturnNearestMaterialCoord(this, Material.leaves, Double.valueOf(20D));
        int ai1[] = FindTreeTop(ai[0], ai[1], ai[2]);
        if(ai1[1] != 0)
        {
            int i = ai1[0];
            int j = ai1[1];
            int k = ai1[2];
            faceTreeTop(i, j, k, 30F);
            if(j - MathHelper.floor_double(posY) > 2)
            {
                motionY += 0.14999999999999999D;
            }
            int l = 0;
            int i1 = 0;
            if(posX < (double)i)
            {
                l = i - MathHelper.floor_double(posX);
                motionX += 0.050000000000000003D;
            } else
            {
                l = MathHelper.floor_double(posX) - i;
                motionX -= 0.050000000000000003D;
            }
            if(posZ < (double)k)
            {
                i1 = k - MathHelper.floor_double(posZ);
                motionZ += 0.050000000000000003D;
            } else
            {
                i1 = MathHelper.floor_double(posX) - k;
                motionZ -= 0.050000000000000003D;
            }
            double d = l + i1;
            if(d < 3D)
            {
                return true;
            }
        }
        return false;
    }

    public void setType(int i)
    {
        typeint = i;
        typechosen = false;
        chooseType();
    }

    public void chooseType()
    {
        if(typeint == 0)
        {
            int i = rand.nextInt(100);
            if(i <= 15)
            {
                typeint = 1;
            } else
            if(i <= 30)
            {
                typeint = 2;
            } else
            if(i <= 45)
            {
                typeint = 3;
            } else
            if(i <= 60)
            {
                typeint = 4;
            } else
            if(i <= 75)
            {
                typeint = 5;
            } else
            if(i <= 90)
            {
                typeint = 6;
            } else
            {
                typeint = 2;
            }
        }
        if(!typechosen)
        {
            if(typeint == 1)
            {
                texture = "/moCreatures/textures/birdwhite.png";
            } else
            if(typeint == 2)
            {
                texture = "/moCreatures/textures/birdblack.png";
            } else
            if(typeint == 3)
            {
                texture = "/moCreatures/textures/birdgreen.png";
            } else
            if(typeint == 4)
            {
                texture = "/moCreatures/textures/birdblue.png";
            } else
            if(typeint == 5)
            {
                texture = "/moCreatures/textures/birdyellow.png";
            } else
            if(typeint == 6)
            {
                texture = "/moCreatures/textures/birdred.png";
            }
        }
        typechosen = true;
    }

    public void faceTreeTop(int i, int j, int k, float f)
    {
        double d = (double)i - posX;
        double d1 = (double)k - posZ;
        double d2 = (double)j - posY;
        double d3 = MathHelper.sqrt_double(d * d + d1 * d1);
        float f1 = (float)((Math.atan2(d1, d) * 180D) / 3.1415927410125728D) - 90F;
        float f2 = (float)((Math.atan2(d2, d3) * 180D) / 3.1415927410125728D);
        rotationPitch = -b(rotationPitch, f2, f);
        rotationYaw = b(rotationYaw, f1, f);
    }

    private float b(float f, float f1, float f2)
    {
        float f3 = f1;
        for(f3 = f1 - f; f3 < -180F; f3 += 360F) { }
        for(; f3 >= 180F; f3 -= 360F) { }
        if(f3 > f2)
        {
            f3 = f2;
        }
        if(f3 < -f2)
        {
            f3 = -f2;
        }
        return f + f3;
    }

    private int[] FindTreeTop(int i, int j, int k)
    {
        int l = i - 5;
        int i1 = k - 5;
        int j1 = i + 5;
        int k1 = j + 7;
        int l1 = k + 5;
        for(int i2 = l; i2 < j1; i2++)
        {
label0:
            for(int j2 = i1; j2 < l1; j2++)
            {
                int k2 = worldObj.getBlockId(i2, j, j2);
                if(k2 == 0 || Block.blocksList[k2].blockMaterial != Material.wood)
                {
                    continue;
                }
                int l2 = j;
                do
                {
                    if(l2 >= k1)
                    {
                        continue label0;
                    }
                    int i3 = worldObj.getBlockId(i2, l2, j2);
                    if(i3 == 0)
                    {
                        return (new int[] {
                            i2, l2 + 2, j2
                        });
                    }
                    l2++;
                } while(true);
            }

        }

        return (new int[] {
            0, 0, 0
        });
    }

    public int[] ReturnNearestMaterialCoord(Entity entity, Material material, Double double1)
    {
        AxisAlignedBB axisalignedbb = entity.boundingBox.expand(double1.doubleValue(), double1.doubleValue(), double1.doubleValue());
        int i = MathHelper.floor_double(axisalignedbb.minX);
        int j = MathHelper.floor_double(axisalignedbb.maxX + 1.0D);
        int k = MathHelper.floor_double(axisalignedbb.minY);
        int l = MathHelper.floor_double(axisalignedbb.maxY + 1.0D);
        int i1 = MathHelper.floor_double(axisalignedbb.minZ);
        int j1 = MathHelper.floor_double(axisalignedbb.maxZ + 1.0D);
        for(int k1 = i; k1 < j; k1++)
        {
            for(int l1 = k; l1 < l; l1++)
            {
                for(int i2 = i1; i2 < j1; i2++)
                {
                    int j2 = worldObj.getBlockId(k1, l1, i2);
                    if(j2 != 0 && Block.blocksList[j2].blockMaterial == material)
                    {
                        return (new int[] {
                            k1, l1, i2
                        });
                    }
                }

            }

        }

        return (new int[] {
            -1, 0, 0
        });
    }

    protected int getDropItemId()
    {
        if(rand.nextInt(2) == 0)
        {
            return Item.feather.shiftedIndex;
        } else
        {
            return Item.seeds.shiftedIndex;
        }
    }

    public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setInteger("TypeInt", typeint);
        nbttagcompound.setBoolean("HasReproduced", hasreproduced);
        nbttagcompound.setBoolean("Tamed", tamed);
        nbttagcompound.setInteger("CounterEntity", counterEntity);
    }

    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        hasreproduced = nbttagcompound.getBoolean("HasReproduced");
        tamed = nbttagcompound.getBoolean("Tamed");
        typeint = nbttagcompound.getInteger("TypeInt");
        counterEntity = nbttagcompound.getInteger("CounterEntity");
    }

    protected String getLivingSound()
    {
        if(typeint == 1)
        {
            return "birdwhite";
        }
        if(typeint == 2)
        {
            return "birdblack";
        }
        if(typeint == 3)
        {
            return "birdgreen";
        }
        if(typeint == 4)
        {
            return "birdblue";
        }
        if(typeint == 5)
        {
            return "birdyellow";
        } else
        {
            return "birdred";
        }
    }

    protected String getHurtSound()
    {
        return "birdhurt";
    }

    protected String getDeathSound()
    {
        return "birddying";
    }

    public boolean getCanSpawnHere()
    {
        int i = MathHelper.floor_double(posX);
        int j = MathHelper.floor_double(boundingBox.minY);
        int k = MathHelper.floor_double(posZ);
        worldObj.getBlockId(i, j - 1, k);
        if(super.getCanSpawnHere())
        {
            if(counterEntity >= mod_mocreatures.maxBirdS.get())
            {
                return false;
            } else
            {
                counterEntity++;
                return true;
            }
        } else
        {
            return false;
        }
    }

    private boolean hasreproduced;
    public int typeint;
    public boolean typechosen;
    private boolean fleeing;
    public float wingb;
    public float wingc;
    public float wingd;
    public float winge;
    public float wingh;
    public boolean tamed;
    public static int counterEntity;
}

