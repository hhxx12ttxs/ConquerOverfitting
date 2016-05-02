package net.minecraft.src;
// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode 

import java.util.List;
import java.util.Random;

public class EntityDolphin extends EntityCustomWM
{

    public EntityDolphin(World world)
    {
        super(world);
        texture = "/mob/dolphin.png";
        setSize(1.5F, 0.8F);
        b = 0.8F + rand.nextFloat();
        adult = false;
        tamed = false;
        dolphinspeed = 1.3D;
        maxhealth = 30;
        health = 30;
        temper = 50;
    }

    public void setTame()
    {
        tamed = true;
    }

    public double speed()
    {
        return dolphinspeed;
    }

    public int tametemper()
    {
        return temper;
    }

    public boolean istamed()
    {
        return tamed;
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
            if(i <= 35)
            {
                typeint = 1;
            } else
            if(i <= 60)
            {
                typeint = 2;
            } else
            if(i <= 85)
            {
                typeint = 3;
            } else
            if(i <= 96)
            {
                typeint = 4;
            } else
            if(i <= 98)
            {
                typeint = 5;
            } else
            {
                typeint = 6;
            }
        }
        if(!typechosen)
        {
            if(typeint == 1)
            {
                texture = "/mob/dolphin.png";
                dolphinspeed = 1.5D;
                temper = 50;
            } else
            if(typeint == 2)
            {
                texture = "/mob/dolphin2.png";
                dolphinspeed = 2.5D;
                temper = 100;
            } else
            if(typeint == 3)
            {
                texture = "/mob/dolphin3.png";
                dolphinspeed = 3.5D;
                temper = 150;
            } else
            if(typeint == 4)
            {
                texture = "/mob/dolphin4.png";
                dolphinspeed = 4.5D;
                temper = 200;
            } else
            if(typeint == 5)
            {
                texture = "/mob/dolphin5.png";
                dolphinspeed = 5.5D;
                temper = 250;
            } else
            if(typeint == 6)
            {
                texture = "/mob/dolphin6.png";
                dolphinspeed = 6.5D;
                temper = 300;
            }
        }
        typechosen = true;
    }

    public boolean interact(EntityPlayer entityplayer)
    {
        ItemStack itemstack = entityplayer.inventory.getCurrentItem();
        if(itemstack != null && itemstack.itemID == Item.fishRaw.shiftedIndex)
        {
            if(--itemstack.stackSize == 0)
            {
                entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, null);
            }
            if((temper -= 25) < 1)
            {
                temper = 1;
            }
            if((health += 15) > maxhealth)
            {
                health = maxhealth;
            }
            worldObj.playSoundAtEntity(this, "eating", 1.0F, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.2F);
            if(!adult)
            {
                b += 0.01F;
            }
            return true;
        }
        if(itemstack != null && itemstack.itemID == Item.fishCooked.shiftedIndex && tamed && adult)
        {
            if(--itemstack.stackSize == 0)
            {
                entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, null);
            }
            if((health += 25) > maxhealth)
            {
                health = maxhealth;
            }
            eaten = true;
            worldObj.playSoundAtEntity(this, "eating", 1.0F, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.2F);
            return true;
        }
        if(adult)
        {
            rotationYaw = entityplayer.rotationYaw;
            rotationPitch = entityplayer.rotationPitch;
            entityplayer.mountEntity(this);
            return true;
        } else
        {
            return false;
        }
    }

    public void onLivingUpdate()
    {
        super.onLivingUpdate();
        if(!adult && rand.nextInt(50) == 0)
        {
            b += 0.01F;
            if(b >= 1.5F)
            {
                adult = true;
            }
        }
        if(!hungry && rand.nextInt(100) == 0)
        {
            hungry = true;
        }
        if(deathTime == 0 && !tamed || hungry)
        {
            EntityItem entityitem = getClosestFish(this, 12D);
            if(entityitem != null)
            {
                MoveToNextEntity(entityitem);
                EntityItem entityitem1 = getClosestFish(this, 2D);
                if(rand.nextInt(20) == 0 && entityitem1 != null && deathTime == 0)
                {
                    entityitem1.setEntityDead();
                    if((temper -= 25) < 1)
                    {
                        temper = 1;
                    }
                    health = maxhealth;
                }
            }
        }
        if(!ReadyforParenting(this))
        {
            return;
        }
        int i = 0;
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(8D, 2D, 8D));
        for(int j = 0; j < list.size(); j++)
        {
            Entity entity = (Entity)list.get(j);
            if(entity instanceof EntityDolphin)
            {
                i++;
            }
        }

        if(i > 1)
        {
            return;
        }
        List list1 = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(4D, 2D, 4D));
        for(int k = 0; k < list.size(); k++)
        {
            Entity entity1 = (Entity)list1.get(k);
            if(!(entity1 instanceof EntityDolphin) || entity1 == this)
            {
                continue;
            }
            EntityDolphin entitydolphin = (EntityDolphin)entity1;
            if(!ReadyforParenting(this) || !ReadyforParenting(entitydolphin))
            {
                continue;
            }
            if(rand.nextInt(100) == 0)
            {
                gestationtime++;
            }
            if(gestationtime <= 50)
            {
                continue;
            }
            EntityDolphin entitydolphin1 = new EntityDolphin(worldObj);
            entitydolphin1.setPosition(posX, posY, posZ);
            worldObj.entityJoinedWorld(entitydolphin1);
            worldObj.playSoundAtEntity(this, "mob.chickenplop", 1.0F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
            eaten = false;
            entitydolphin.eaten = false;
            gestationtime = 0;
            entitydolphin.gestationtime = 0;
            int l = Genetics(this, entitydolphin);
            entitydolphin1.bred = true;
            entitydolphin1.b = 0.35F;
            entitydolphin1.adult = false;
            entitydolphin1.setType(l);
            break;
        }

    }

    public boolean ReadyforParenting(EntityDolphin entitydolphin)
    {
        return entitydolphin.riddenByEntity == null && entitydolphin.ridingEntity == null && entitydolphin.tamed && entitydolphin.eaten && entitydolphin.adult;
    }

    private int Genetics(EntityDolphin entitydolphin, EntityDolphin entitydolphin1)
    {
        if(entitydolphin.typeint == entitydolphin1.typeint)
        {
            return entitydolphin.typeint;
        }
        int i = entitydolphin.typeint + entitydolphin1.typeint;
        boolean flag = rand.nextInt(3) == 0;
        if(i < 5 && flag)
        {
            return i;
        }
        boolean flag1 = rand.nextInt(10) == 0;
        if((i == 5 || i == 6) && flag1)
        {
            return i;
        } else
        {
            return 0;
        }
    }

    private boolean MoveToNextEntity(Entity entity)
    {
        if(entity != null)
        {
            int i = MathHelper.floor_double(entity.posX);
            int j = MathHelper.floor_double(entity.posY);
            int k = MathHelper.floor_double(entity.posZ);
            faceItem(i, j, k, 30F);
            if(posX < (double)i)
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
            if(posZ < (double)k)
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

    public void faceItem(int i, int j, int k, float f)
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

    public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setBoolean("Tamed", tamed);
        nbttagcompound.setInteger("TypeInt", typeint);
        nbttagcompound.setBoolean("Adult", adult);
        nbttagcompound.setBoolean("Bred", bred);
        nbttagcompound.setFloat("Age", b);
        nbttagcompound.setInteger("counterEntity", counterEntity);
    }

    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        tamed = nbttagcompound.getBoolean("Tamed");
        typeint = nbttagcompound.getInteger("TypeInt");
        adult = nbttagcompound.getBoolean("Adult");
        bred = nbttagcompound.getBoolean("Bred");
        b = nbttagcompound.getFloat("Age");
        counterEntity = nbttagcompound.getInteger("counterEntity");
    }

    public EntityLiving getClosestTarget(Entity entity, double d)
    {
        double d1 = -1D;
        EntityLiving entityliving = null;
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(d, d, d));
        for(int i = 0; i < list.size(); i++)
        {
            Entity entity1 = (Entity)list.get(i);
            if(!(entity1 instanceof EntityShark))
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

    protected Entity findPlayerToAttack()
    {
        if(worldObj.difficultySetting > 0 && b >= 1.0F && mod_mocreatures.attackdolphins.get() && rand.nextInt(50) == 0)
        {
            EntityLiving entityliving = getClosestTarget(this, 12D);
            if(entityliving != null && entityliving.inWater)
            {
                return entityliving;
            }
        }
        return null;
    }

    public boolean attackEntityFrom(Entity entity, int i)
    {
        if(super.attackEntityFrom(entity, i) && worldObj.difficultySetting > 0)
        {
            if(riddenByEntity == entity || ridingEntity == entity)
            {
                return true;
            }
            if(entity != this)
            {
                playerToAttack = entity;
            }
            return true;
        } else
        {
            return false;
        }
    }

    protected void attackEntity(Entity entity, float f)
    {
        if((double)f < 3.5D && entity.boundingBox.maxY > boundingBox.minY && entity.boundingBox.minY < boundingBox.maxY && b >= 1.0F)
        {
            attackTime = 20;
            entity.attackEntityFrom(this, 5);
        }
    }

    protected int getDropItemId()
    {
        return Item.fishRaw.shiftedIndex;
    }

    public boolean getCanSpawnHere()
    {
        if(super.getCanSpawnHere())
        {
            if(counterEntity >= mod_mocreatures.maxDolphins.get())
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

    public void setEntityDead()
    {
        if((tamed || bred) && health > 0)
        {
            return;
        } else
        {
            counterEntity--;
            super.setEntityDead();
            return;
        }
    }

    protected String getLivingSound()
    {
        return "dolphin";
    }

    protected String getHurtSound()
    {
        return "dolphinhurt";
    }

    protected String getDeathSound()
    {
        return "dolphindying";
    }

    protected float getSoundVolume()
    {
        return 0.4F;
    }

    protected String getUpsetSound()
    {
        return "dolphinupset";
    }

    public int gestationtime;
    public boolean bred;
    public float b;
    public boolean adult;
    public boolean tamed;
    public int typeint;
    private double dolphinspeed;
    private int maxhealth;
    private int temper;
    private boolean eaten;
    public static int counterEntity;
    public boolean typechosen;
    public boolean hungry;
}

