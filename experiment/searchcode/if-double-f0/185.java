package net.canarymod.api.entity.living.humanoid.npchelpers;

import net.canarymod.api.entity.living.humanoid.EntityNonPlayableCharacter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;


public class EntityNPCLookHelper {

    private EntityNonPlayableCharacter a;
    private float b;
    private float c;
    private boolean d;
    private double e;
    private double f;
    private double g;

    public EntityNPCLookHelper(EntityNonPlayableCharacter entityNPC) {
        this.a = entityNPC;
    }

    public void a(Entity entity, float f0, float f1) {
        this.e = entity.t;
        if (entity instanceof EntityLivingBase) {
            this.f = entity.u + (double) entity.g();
        }
        else {
            this.f = (entity.D.b + entity.D.e) / 2.0D;
        }

        this.g = entity.v;
        this.b = f0;
        this.c = f1;
        this.d = true;
    }

    public void a(double d0, double d1, double d2, float f0, float f1) {
        this.e = d0;
        this.f = d1;
        this.g = d2;
        this.b = f0;
        this.c = f1;
        this.d = true;
    }

    public void a() {
        this.a.A = 0.0F;
        if (this.d) {
            this.d = false;
            double d0 = this.e - this.a.t;
            double d1 = this.f - (this.a.u + (double) this.a.g());
            double d2 = this.g - this.a.v;
            double d3 = (double) MathHelper.a(d0 * d0 + d2 * d2);
            float f0 = (float) (Math.atan2(d2, d0) * 180.0D / 3.1415927410125732D) - 90.0F;
            float f1 = (float) (-(Math.atan2(d1, d3) * 180.0D / 3.1415927410125732D));

            this.a.A = this.a(this.a.A, f1, this.c);
            this.a.aP = this.a(this.a.aP, f0, this.b);
        }
        else {
            this.a.aP = this.a(this.a.aP, this.a.aN, 10.0F);
        }

        float f2 = MathHelper.g(this.a.aP - this.a.aN);

        if (!this.a.getPathNavigate().g()) {
            if (f2 < -75.0F) {
                this.a.aP = this.a.aN - 75.0F;
            }

            if (f2 > 75.0F) {
                this.a.aP = this.a.aN + 75.0F;
            }
        }

    }

    private float a(float f0, float f1, float f2) {
        float f3 = MathHelper.g(f1 - f0);

        if (f3 > f2) {
            f3 = f2;
        }

        if (f3 < -f2) {
            f3 = -f2;
        }

        return f0 + f3;
    }
}

