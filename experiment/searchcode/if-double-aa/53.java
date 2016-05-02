/*      */ import java.util.List;
/*      */ import java.util.Random;
/*      */ 
/*      */ public abstract class yw extends acq
/*      */ {
/*   43 */   public aak ap = new aak(this);
/*      */   public dd aq;
/*      */   public dd ar;
/*   47 */   protected ne as = new ne();
/*      */ 
/*   49 */   protected int at = 0;
/*      */ 
/*   51 */   public byte au = 0;
/*   52 */   public int av = 0;
/*      */   public float aw;
/*      */   public float ax;
/*   54 */   public boolean ay = false;
/*   55 */   public int az = 0;
/*      */   public String aA;
/*      */   public int aB;
/*      */   public String aC;
/*   60 */   public int aD = 0;
/*      */   public double aE;
/*      */   public double aF;
/*      */   public double aG;
/*      */   public double aH;
/*      */   public double aI;
/*      */   public double aJ;
/*      */   protected boolean aK;
/*      */   public uh aL;
/*      */   private int a;
/*      */   public float aM;
/*      */   public float aN;
/*      */   public float aO;
/*      */   private uh b;
/*      */   private uh c;
/*   75 */   public int aP = 20;
/*   76 */   protected boolean aQ = false;
/*      */   public float aR;
/*      */   public float aS;
/*   79 */   public qu aT = new qu();
/*      */   public int aU;
/*      */   public int aV;
/*      */   public float aW;
/*      */   private aan d;
/*      */   private int e;
/*   85 */   protected float aX = 0.1F;
/*   86 */   protected float aY = 0.02F;
/*      */ 
/*  608 */   public act aZ = null;
/*      */ 
/*      */   public yw(xd paramxd)
/*      */   {
/*   89 */     super(paramxd);
/*      */ 
/*   91 */     this.aq = new y(this.ap, !paramxd.F);
/*   92 */     this.ar = this.aq;
/*      */ 
/*   94 */     this.H = 1.62F;
/*   95 */     uh localuh = paramxd.x();
/*   96 */     c(localuh.a + 0.5D, localuh.b + 1, localuh.c + 0.5D, 0.0F, 0.0F);
/*      */ 
/*   98 */     this.bp = "humanoid";
/*   99 */     this.bo = 180.0F;
/*  100 */     this.W = 20;
/*      */ 
/*  102 */     this.bm = "/mob/char.png";
/*      */   }
/*      */ 
/*      */   public int d()
/*      */   {
/*  107 */     return 20;
/*      */   }
/*      */ 
/*      */   protected void b()
/*      */   {
/*  112 */     super.b();
/*      */ 
/*  114 */     this.ac.a(16, Byte.valueOf(0));
/*  115 */     this.ac.a(17, Byte.valueOf(0));
/*      */   }
/*      */ 
/*      */   public aan ah() {
/*  119 */     return this.d;
/*      */   }
/*      */ 
/*      */   public int ai() {
/*  123 */     return this.e;
/*      */   }
/*      */ 
/*      */   public boolean aj() {
/*  127 */     return this.d != null;
/*      */   }
/*      */ 
/*      */   public int al() {
/*  131 */     if (aj()) {
/*  132 */       return this.d.l() - this.e;
/*      */     }
/*  134 */     return 0;
/*      */   }
/*      */ 
/*      */   public void am() {
/*  138 */     if (this.d != null) {
/*  139 */       this.d.b(this.k, this, this.e);
/*      */     }
/*  141 */     an();
/*      */   }
/*      */ 
/*      */   public void an() {
/*  145 */     this.d = null;
/*  146 */     this.e = 0;
/*  147 */     if (!this.k.F)
/*  148 */       e(false);
/*      */   }
/*      */ 
/*      */   public boolean ao()
/*      */   {
/*  154 */     return (aj()) && (yr.e[this.d.c].c(this.d) == aaq.d);
/*      */   }
/*      */ 
/*      */   public void J_()
/*      */   {
/*  159 */     if (this.d != null) {
/*  160 */       aan localaan = this.ap.b();
/*  161 */       if (localaan != this.d) {
/*  162 */         an();
/*      */       } else {
/*  164 */         if ((this.e <= 25) && (this.e % 4 == 0)) {
/*  165 */           a(localaan, 5);
/*      */         }
/*  167 */         if ((--this.e == 0) && 
/*  168 */           (!this.k.F)) {
/*  169 */           ap();
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  175 */     if (this.aD > 0) this.aD -= 1;
/*  176 */     if (az()) {
/*  177 */       this.a += 1;
/*  178 */       if (this.a > 100) {
/*  179 */         this.a = 100;
/*      */       }
/*      */ 
/*  182 */       if (!this.k.F) {
/*  183 */         if (!bk())
/*  184 */           a(true, true, false);
/*  185 */         else if (this.k.m())
/*  186 */           a(false, true, true);
/*      */       }
/*      */     }
/*  189 */     else if (this.a > 0) {
/*  190 */       this.a += 1;
/*  191 */       if (this.a >= 110) {
/*  192 */         this.a = 0;
/*      */       }
/*      */     }
/*      */ 
/*  196 */     super.J_();
/*      */ 
/*  198 */     if ((!this.k.F) && 
/*  199 */       (this.ar != null) && (!this.ar.b(this))) {
/*  200 */       af();
/*  201 */       this.ar = this.aq;
/*      */     }
/*      */ 
/*  205 */     if (this.aT.b) {
/*  206 */       for (int i = 0; i < 8; i++);
/*      */     }
/*  209 */     if ((T()) && (this.aT.a)) {
/*  210 */       D();
/*      */     }
/*      */ 
/*  213 */     this.aE = this.aH;
/*  214 */     this.aF = this.aI;
/*  215 */     this.aG = this.aJ;
/*      */ 
/*  217 */     double d1 = this.o - this.aH;
/*  218 */     double d2 = this.p - this.aI;
/*  219 */     double d3 = this.q - this.aJ;
/*      */ 
/*  221 */     double d4 = 10.0D;
/*  222 */     if (d1 > d4) this.aE = (this.aH = this.o);
/*  223 */     if (d3 > d4) this.aG = (this.aJ = this.q);
/*  224 */     if (d2 > d4) this.aF = (this.aI = this.p);
/*  225 */     if (d1 < -d4) this.aE = (this.aH = this.o);
/*  226 */     if (d3 < -d4) this.aG = (this.aJ = this.q);
/*  227 */     if (d2 < -d4) this.aF = (this.aI = this.p);
/*      */ 
/*  229 */     this.aH += d1 * 0.25D;
/*  230 */     this.aJ += d3 * 0.25D;
/*  231 */     this.aI += d2 * 0.25D;
/*      */ 
/*  233 */     a(gv.k, 1);
/*      */ 
/*  235 */     if (this.j == null) {
/*  236 */       this.c = null;
/*      */     }
/*      */ 
/*  239 */     if (!this.k.F)
/*  240 */       this.as.a(this);
/*      */   }
/*      */ 
/*      */   protected void a(aan paramaan, int paramInt)
/*      */   {
/*  257 */     if (paramaan.m() == aaq.c) {
/*  258 */       this.k.a(this, "random.drink", 0.5F, this.k.r.nextFloat() * 0.1F + 0.9F);
/*      */     }
/*  260 */     if (paramaan.m() == aaq.b)
/*      */     {
/*  262 */       for (int i = 0; i < paramInt; i++) {
/*  263 */         bo localbo1 = bo.b((this.U.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
/*  264 */         localbo1.a(-this.v * 3.141593F / 180.0F);
/*  265 */         localbo1.b(-this.u * 3.141593F / 180.0F);
/*      */ 
/*  267 */         bo localbo2 = bo.b((this.U.nextFloat() - 0.5D) * 0.3D, -this.U.nextFloat() * 0.6D - 0.3D, 0.6D);
/*  268 */         localbo2.a(-this.v * 3.141593F / 180.0F);
/*  269 */         localbo2.b(-this.u * 3.141593F / 180.0F);
/*  270 */         localbo2 = localbo2.c(this.o, this.p + I(), this.q);
/*  271 */         this.k.a("iconcrack_" + paramaan.a().bQ, localbo2.a, localbo2.b, localbo2.c, localbo1.a, localbo1.b + 0.05D, localbo1.c);
/*      */       }
/*  273 */       this.k.a(this, "random.eat", 0.5F + 0.5F * this.U.nextInt(2), (this.U.nextFloat() - this.U.nextFloat()) * 0.2F + 1.0F);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void ap() {
/*  278 */     if (this.d != null) {
/*  279 */       a(this.d, 16);
/*      */ 
/*  281 */       int i = this.d.a;
/*  282 */       aan localaan = this.d.b(this.k, this);
/*  283 */       if ((localaan != this.d) || ((localaan != null) && (localaan.a != i))) {
/*  284 */         this.ap.a[this.ap.c] = localaan;
/*  285 */         if (localaan.a == 0) {
/*  286 */           this.ap.a[this.ap.c] = null;
/*      */         }
/*      */       }
/*  289 */       an();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void a(byte paramByte)
/*      */   {
/*  295 */     if (paramByte == 9)
/*  296 */       ap();
/*      */     else
/*  298 */       super.a(paramByte);
/*      */   }
/*      */ 
/*      */   protected boolean aq()
/*      */   {
/*  304 */     return (bb() <= 0) || (az());
/*      */   }
/*      */ 
/*      */   protected void af()
/*      */   {
/*  309 */     this.ar = this.aq;
/*      */   }
/*      */ 
/*      */   public void S() {
/*  313 */     this.aC = ("http://www.mccapes.com/MinecraftCloakss/" + this.aA + ".png");
/*  314 */     this.aa = this.aC;
/*      */   }
/*      */ 
/*      */   public void O()
/*      */   {
/*  319 */     double d1 = this.o; double d2 = this.p; double d3 = this.q;
/*      */ 
/*  321 */     super.O();
/*  322 */     this.aw = this.ax;
/*  323 */     this.ax = 0.0F;
/*      */ 
/*  325 */     k(this.o - d1, this.p - d2, this.q - d3);
/*      */   }
/*      */ 
/*      */   public void z()
/*      */   {
/*  330 */     this.H = 1.62F;
/*  331 */     a(0.6F, 1.8F);
/*  332 */     super.z();
/*  333 */     l(d());
/*  334 */     this.bD = 0;
/*      */   }
/*      */ 
/*      */   private int bj() {
/*  338 */     if (a(aad.e)) {
/*  339 */       return 6 - (1 + b(aad.e).c()) * 1;
/*      */     }
/*  341 */     if (a(aad.f)) {
/*  342 */       return 6 + (1 + b(aad.f).c()) * 2;
/*      */     }
/*  344 */     return 6;
/*      */   }
/*      */ 
/*      */   protected void y_() {
/*  348 */     int i = bj();
/*  349 */     if (this.ay) {
/*  350 */       this.az += 1;
/*  351 */       if (this.az >= i) {
/*  352 */         this.az = 0;
/*  353 */         this.ay = false;
/*      */       }
/*      */     } else {
/*  356 */       this.az = 0;
/*      */     }
/*      */ 
/*  359 */     this.bw = (this.az / i);
/*      */   }
/*      */ 
/*      */   public void e() {
/*  363 */     if (this.at > 0) this.at -= 1;
/*      */ 
/*  365 */     if ((this.k.q == 0) && (bb() < d()) && 
/*  366 */       (this.V % 20 * 12 == 0)) k(1);
/*      */ 
/*  368 */     this.ap.g();
/*  369 */     this.aw = this.ax;
/*      */ 
/*  371 */     super.e();
/*      */ 
/*  373 */     this.bt = this.aX;
/*  374 */     this.bu = this.aY;
/*  375 */     if (W()) {
/*  376 */       this.bt = (float)(this.bt + this.aX * 0.3D);
/*  377 */       this.bu = (float)(this.bu + this.aY * 0.3D);
/*      */     }
/*      */ 
/*  380 */     float f1 = gk.a(this.r * this.r + this.t * this.t);
/*  381 */     float f2 = (float)Math.atan(-this.s * 0.2000000029802322D) * 15.0F;
/*  382 */     if (f1 > 0.1F) f1 = 0.1F;
/*  383 */     if ((!this.z) || (bb() <= 0)) f1 = 0.0F;
/*  384 */     if ((this.z) || (bb() <= 0)) f2 = 0.0F;
/*  385 */     this.ax += (f1 - this.ax) * 0.4F;
/*  386 */     this.bG += (f2 - this.bG) * 0.8F;
/*      */ 
/*  389 */     if (bb() > 0) {
/*  390 */       List localList = this.k.b(this, this.y.b(1.0D, 0.0D, 1.0D));
/*  391 */       if (localList != null)
/*  392 */         for (int i = 0; i < localList.size(); i++) {
/*  393 */           nn localnn = (nn)localList.get(i);
/*  394 */           if (!localnn.G)
/*  395 */             n(localnn);
/*      */         }
/*      */     }
/*      */   }
/*      */ 
/*      */   private void n(nn paramnn)
/*      */   {
/*  403 */     paramnn.a(this);
/*      */   }
/*      */ 
/*      */   public int ar() {
/*  407 */     return this.av;
/*      */   }
/*      */ 
/*      */   public void a(md parammd) {
/*  411 */     super.a(parammd);
/*  412 */     a(0.2F, 0.2F);
/*  413 */     d(this.o, this.p, this.q);
/*  414 */     this.s = 0.1000000014901161D;
/*      */ 
/*  416 */     if (this.aA.equals("Notch")) {
/*  417 */       a(new aan(yr.j, 1), true);
/*      */     }
/*  419 */     this.ap.i();
/*      */ 
/*  421 */     if (parammd != null) {
/*  422 */       this.r = (-gk.b((this.bC + this.u) * 3.141593F / 180.0F) * 0.1F);
/*  423 */       this.t = (-gk.a((this.bC + this.u) * 3.141593F / 180.0F) * 0.1F);
/*      */     } else {
/*  425 */       this.r = (this.t = 0.0D);
/*      */     }
/*  427 */     this.H = 0.1F;
/*      */ 
/*  429 */     a(gv.y, 1);
/*      */   }
/*      */ 
/*      */   public void a(nn paramnn, int paramInt) {
/*  433 */     this.av += paramInt;
/*      */ 
/*  435 */     if ((paramnn instanceof yw))
/*  436 */       a(gv.A, 1);
/*      */     else
/*  438 */       a(gv.z, 1);
/*      */   }
/*      */ 
/*      */   protected int b(int paramInt)
/*      */   {
/*  444 */     int i = ais.a(this.ap);
/*  445 */     if ((i > 0) && 
/*  446 */       (this.U.nextInt(i + 1) > 0))
/*      */     {
/*  448 */       return paramInt;
/*      */     }
/*      */ 
/*  451 */     return super.b(paramInt);
/*      */   }
/*      */ 
/*      */   public fq as()
/*      */   {
/*  463 */     return a(this.ap.a(this.ap.c, 1), false);
/*      */   }
/*      */ 
/*      */   public fq a(aan paramaan) {
/*  467 */     return a(paramaan, false);
/*      */   }
/*      */ 
/*      */   public fq a(aan paramaan, boolean paramBoolean) {
/*  471 */     if (paramaan == null) return null;
/*      */ 
/*  473 */     fq localfq = new fq(this.k, this.o, this.p - 0.300000011920929D + I(), this.q, paramaan);
/*  474 */     localfq.c = 40;
/*      */ 
/*  476 */     float f1 = 0.1F;
/*      */     float f2;
/*  477 */     if (paramBoolean) {
/*  478 */       f2 = this.U.nextFloat() * 0.5F;
/*  479 */       float f3 = this.U.nextFloat() * 3.141593F * 2.0F;
/*  480 */       localfq.r = (-gk.a(f3) * f2);
/*  481 */       localfq.t = (gk.b(f3) * f2);
/*  482 */       localfq.s = 0.2000000029802322D;
/*      */     }
/*      */     else {
/*  485 */       f1 = 0.3F;
/*  486 */       localfq.r = (-gk.a(this.u / 180.0F * 3.141593F) * gk.b(this.v / 180.0F * 3.141593F) * f1);
/*  487 */       localfq.t = (gk.b(this.u / 180.0F * 3.141593F) * gk.b(this.v / 180.0F * 3.141593F) * f1);
/*  488 */       localfq.s = (-gk.a(this.v / 180.0F * 3.141593F) * f1 + 0.1F);
/*  489 */       f1 = 0.02F;
/*      */ 
/*  491 */       f2 = this.U.nextFloat() * 3.141593F * 2.0F;
/*  492 */       f1 *= this.U.nextFloat();
/*  493 */       localfq.r += Math.cos(f2) * f1;
/*  494 */       localfq.s += (this.U.nextFloat() - this.U.nextFloat()) * 0.1F;
/*  495 */       localfq.t += Math.sin(f2) * f1;
/*      */     }
/*      */ 
/*  498 */     a(localfq);
/*  499 */     a(gv.v, 1);
/*      */ 
/*  501 */     return localfq;
/*      */   }
/*      */ 
/*      */   protected void a(fq paramfq) {
/*  505 */     this.k.a(paramfq);
/*      */   }
/*      */ 
/*      */   public float a(pb parampb) {
/*  509 */     float f1 = this.ap.a(parampb);
/*  510 */     float f2 = f1;
/*      */ 
/*  512 */     int i = ais.b(this.ap);
/*  513 */     if ((i > 0) && (this.ap.b(parampb))) {
/*  514 */       f2 += i * i + 1;
/*      */     }
/*      */ 
/*  517 */     if (a(aad.e)) {
/*  518 */       f2 *= (1.0F + (b(aad.e).c() + 1) * 0.2F);
/*      */     }
/*  520 */     if (a(aad.f)) {
/*  521 */       f2 *= (1.0F - (b(aad.f).c() + 1) * 0.2F);
/*      */     }
/*      */ 
/*  524 */     if ((a(acn.g)) && (!ais.g(this.ap))) f2 /= 5.0F;
/*  525 */     if (!this.z) f2 /= 5.0F;
/*      */ 
/*  527 */     return f2;
/*      */   }
/*      */ 
/*      */   public boolean b(pb parampb) {
/*  531 */     return this.ap.b(parampb);
/*      */   }
/*      */ 
/*      */   public void a(ady paramady)
/*      */   {
/*  536 */     super.a(paramady);
/*  537 */     no localno = paramady.n("Inventory");
/*  538 */     this.ap.b(localno);
/*  539 */     this.aB = paramady.f("Dimension");
/*  540 */     this.aK = paramady.o("Sleeping");
/*  541 */     this.a = paramady.e("SleepTimer");
/*      */ 
/*  543 */     this.aW = paramady.h("XpP");
/*  544 */     this.aU = paramady.f("XpLevel");
/*  545 */     this.aV = paramady.f("XpTotal");
/*      */ 
/*  547 */     if (this.aK) {
/*  548 */       this.aL = new uh(gk.c(this.o), gk.c(this.p), gk.c(this.q));
/*  549 */       a(true, true, false);
/*      */     }
/*      */ 
/*  552 */     if ((paramady.c("SpawnX")) && (paramady.c("SpawnY")) && (paramady.c("SpawnZ"))) {
/*  553 */       this.b = new uh(paramady.f("SpawnX"), paramady.f("SpawnY"), paramady.f("SpawnZ"));
/*      */     }
/*      */ 
/*  556 */     this.as.a(paramady);
/*  557 */     this.aT.b(paramady);
/*      */   }
/*      */ 
/*      */   public void b(ady paramady) {
/*  561 */     super.b(paramady);
/*  562 */     paramady.a("Inventory", this.ap.a(new no()));
/*  563 */     paramady.a("Dimension", this.aB);
/*  564 */     paramady.a("Sleeping", this.aK);
/*  565 */     paramady.a("SleepTimer", (short)this.a);
/*  566 */     paramady.a("XpP", this.aW);
/*  567 */     paramady.a("XpLevel", this.aU);
/*  568 */     paramady.a("XpTotal", this.aV);
/*      */ 
/*  570 */     if (this.b != null) {
/*  571 */       paramady.a("SpawnX", this.b.a);
/*  572 */       paramady.a("SpawnY", this.b.b);
/*  573 */       paramady.a("SpawnZ", this.b.c);
/*      */     }
/*      */ 
/*  576 */     this.as.b(paramady);
/*  577 */     this.aT.a(paramady);
/*      */   }
/*      */ 
/*      */   public void a(io paramio)
/*      */   {
/*      */   }
/*      */ 
/*      */   public void c(int paramInt1, int paramInt2, int paramInt3)
/*      */   {
/*      */   }
/*      */ 
/*      */   public void a(int paramInt1, int paramInt2, int paramInt3)
/*      */   {
/*      */   }
/*      */ 
/*      */   public void b(nn paramnn, int paramInt)
/*      */   {
/*      */   }
/*      */ 
/*      */   public float I()
/*      */   {
/*  601 */     return 0.12F;
/*      */   }
/*      */ 
/*      */   protected void aa() {
/*  605 */     this.H = 1.62F;
/*      */   }
/*      */ 
/*      */   public boolean a(md parammd, int paramInt)
/*      */   {
/*  611 */     if ((this.aT.a) && (!parammd.g())) return false;
/*      */ 
/*  613 */     this.cd = 0;
/*  614 */     if (bb() <= 0) return false;
/*      */ 
/*  616 */     if ((az()) && (!this.k.F)) {
/*  617 */       a(true, true, false);
/*      */     }
/*      */ 
/*  620 */     nn localnn1 = parammd.a();
/*      */ 
/*  622 */     if (((localnn1 instanceof yy)) || ((localnn1 instanceof nm))) {
/*  623 */       if (this.k.q == 0) paramInt = 0;
/*  624 */       if (this.k.q == 1) paramInt = paramInt / 2 + 1;
/*  625 */       if (this.k.q == 3) paramInt = paramInt * 3 / 2;
/*      */     }
/*      */ 
/*  628 */     if (paramInt == 0) return false;
/*      */ 
/*  630 */     nn localnn2 = localnn1;
/*  631 */     if (((localnn2 instanceof nm)) && 
/*  632 */       (((nm)localnn2).c != null)) {
/*  633 */       localnn2 = ((nm)localnn2).c;
/*      */     }
/*      */ 
/*  636 */     if ((localnn2 instanceof acq))
/*      */     {
/*  638 */       a((acq)localnn2, false);
/*      */     }
/*      */ 
/*  641 */     a(gv.x, paramInt);
/*      */ 
/*  643 */     return super.a(parammd, paramInt);
/*      */   }
/*      */ 
/*      */   protected int b(md parammd, int paramInt)
/*      */   {
/*  648 */     int i = super.b(parammd, paramInt);
/*  649 */     if (i <= 0) {
/*  650 */       return 0;
/*      */     }
/*      */ 
/*  653 */     int j = ais.a(this.ap, parammd);
/*  654 */     if (j > 20) {
/*  655 */       j = 20;
/*      */     }
/*  657 */     if ((j > 0) && (j <= 20)) {
/*  658 */       int k = 25 - j;
/*  659 */       int m = i * k + this.bz;
/*  660 */       i = m / 25;
/*  661 */       this.bz = (m % 25);
/*      */     }
/*      */ 
/*  664 */     return i;
/*      */   }
/*      */ 
/*      */   protected boolean B_() {
/*  668 */     return false;
/*      */   }
/*      */ 
/*      */   protected void a(acq paramacq, boolean paramBoolean)
/*      */   {
/*  674 */     if (((paramacq instanceof yd)) || ((paramacq instanceof ui))) {
/*  675 */       return;
/*      */     }
/*      */ 
/*  678 */     if ((paramacq instanceof yo)) {
/*  679 */       localObject = (yo)paramacq;
/*  680 */       if ((((yo)localObject).G_()) && (this.aA.equals(((yo)localObject).ag()))) {
/*  681 */         return;
/*      */       }
/*      */     }
/*  684 */     if (((paramacq instanceof yw)) && (!B_()))
/*      */     {
/*  686 */       return;
/*      */     }
/*      */ 
/*  690 */     Object localObject = this.k.a(yo.class, wu.b(this.o, this.p, this.q, this.o + 1.0D, this.p + 1.0D, this.q + 1.0D).b(16.0D, 4.0D, 16.0D));
/*  691 */     for (nn localnn : (List)localObject) {
/*  692 */       yo localyo = (yo)localnn;
/*  693 */       if ((localyo.G_()) && (localyo.as() == null) && (this.aA.equals(localyo.ag())) && (
/*  694 */         (!paramBoolean) || (!localyo.af()))) {
/*  695 */         localyo.f(false);
/*  696 */         localyo.i(paramacq);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void h(int paramInt)
/*      */   {
/*  704 */     this.ap.g(paramInt);
/*      */   }
/*      */ 
/*      */   public int au()
/*      */   {
/*  709 */     return this.ap.h();
/*      */   }
/*      */ 
/*      */   protected void c(md parammd, int paramInt)
/*      */   {
/*  714 */     if ((!parammd.e()) && (ao())) {
/*  715 */       paramInt = 1 + paramInt >> 1;
/*      */     }
/*  717 */     paramInt = d(parammd, paramInt);
/*  718 */     paramInt = b(parammd, paramInt);
/*  719 */     c(parammd.f());
/*  720 */     this.bx -= paramInt;
/*      */   }
/*      */ 
/*      */   public void a(ahg paramahg)
/*      */   {
/*      */   }
/*      */ 
/*      */   public void a(az paramaz) {
/*      */   }
/*      */ 
/*      */   public void a(sc paramsc) {
/*      */   }
/*      */ 
/*      */   public void a(amc paramamc) {
/*      */   }
/*      */ 
/*      */   public void j(nn paramnn) {
/*  737 */     if (paramnn.c(this)) return;
/*  738 */     aan localaan = av();
/*  739 */     if ((localaan != null) && ((paramnn instanceof acq))) {
/*  740 */       localaan.a((acq)paramnn);
/*  741 */       if (localaan.a <= 0) {
/*  742 */         localaan.a(this);
/*  743 */         aw();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public aan av() {
/*  749 */     return this.ap.b();
/*      */   }
/*      */ 
/*      */   public void aw() {
/*  753 */     this.ap.a(this.ap.c, null);
/*      */   }
/*      */ 
/*      */   public double P() {
/*  757 */     return this.H - 0.5F;
/*      */   }
/*      */ 
/*      */   public void ax() {
/*  761 */     if ((!this.ay) || (this.az >= bj() / 2) || (this.az < 0)) {
/*  762 */       this.az = -1;
/*  763 */       this.ay = true;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void k(nn paramnn)
/*      */   {
/*  769 */     if (!paramnn.k_()) {
/*  770 */       return;
/*      */     }
/*      */ 
/*  773 */     int i = this.ap.a(paramnn);
/*      */ 
/*  775 */     if (a(aad.g)) {
/*  776 */       i += (3 << b(aad.g).c());
/*      */     }
/*  778 */     if (a(aad.t)) {
/*  779 */       i -= (2 << b(aad.t).c());
/*      */     }
/*      */ 
/*  782 */     int j = 0;
/*  783 */     int k = 0;
/*  784 */     if ((paramnn instanceof acq)) {
/*  785 */       k = ais.a(this.ap, (acq)paramnn);
/*  786 */       j += ais.b(this.ap, (acq)paramnn);
/*      */     }
/*  788 */     if (W()) {
/*  789 */       j++;
/*      */     }
/*      */ 
/*  792 */     if ((i > 0) || (k > 0))
/*      */     {
/*  794 */       int m = (this.M > 0.0F) && (!this.z) && (!p()) && (!H()) && (!a(aad.q)) && (this.j == null) && ((paramnn instanceof acq)) ? 1 : 0;
/*  795 */       if (m != 0) {
/*  796 */         i += this.U.nextInt(i / 2 + 2);
/*      */       }
/*  798 */       i += k;
/*      */ 
/*  800 */       boolean bool = paramnn.a(md.a(this), i);
/*  801 */       if (bool) {
/*  802 */         if (j > 0) {
/*  803 */           paramnn.c(-gk.a(this.u * 3.141593F / 180.0F) * j * 0.5F, 0.1D, gk.b(this.u * 3.141593F / 180.0F) * j * 0.5F);
/*      */ 
/*  805 */           this.r *= 0.6D;
/*  806 */           this.t *= 0.6D;
/*  807 */           d(false);
/*      */         }
/*      */ 
/*  810 */         if (m != 0) {
/*  811 */           d(paramnn);
/*      */         }
/*  813 */         if (k > 0) {
/*  814 */           i(paramnn);
/*      */         }
/*      */ 
/*  817 */         if (i >= 18) {
/*  818 */           a(dp.E);
/*      */         }
/*  820 */         l(paramnn);
/*      */       }
/*  822 */       aan localaan = av();
/*  823 */       if ((localaan != null) && ((paramnn instanceof acq))) {
/*  824 */         localaan.a((acq)paramnn, this);
/*  825 */         if (localaan.a <= 0) {
/*  826 */           localaan.a(this);
/*  827 */           aw();
/*      */         }
/*      */       }
/*  830 */       if ((paramnn instanceof acq)) {
/*  831 */         if (paramnn.M()) {
/*  832 */           a((acq)paramnn, true);
/*      */         }
/*  834 */         a(gv.w, i);
/*      */ 
/*  836 */         int n = ais.c(this.ap, (acq)paramnn);
/*  837 */         if (n > 0) {
/*  838 */           paramnn.e(n * 4);
/*      */         }
/*      */       }
/*      */ 
/*  842 */       c(0.3F);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void d(nn paramnn)
/*      */   {
/*      */   }
/*      */ 
/*      */   public void i(nn paramnn)
/*      */   {
/*      */   }
/*      */ 
/*      */   public void ag()
/*      */   {
/*      */   }
/*      */ 
/*      */   public abstract void ab();
/*      */ 
/*      */   public void b(aan paramaan)
/*      */   {
/*      */   }
/*      */ 
/*      */   public void A()
/*      */   {
/*  877 */     super.A();
/*  878 */     this.aq.a(this);
/*  879 */     if (this.ar != null)
/*  880 */       this.ar.a(this);
/*      */   }
/*      */ 
/*      */   public boolean N()
/*      */   {
/*  886 */     return (!this.aK) && (super.N());
/*      */   }
/*      */ 
/*      */   public ci d(int paramInt1, int paramInt2, int paramInt3)
/*      */   {
/*  894 */     if (!this.k.F) {
/*  895 */       if ((az()) || (!M())) {
/*  896 */         return ci.e;
/*      */       }
/*      */ 
/*  899 */       if (!this.k.t.e())
/*      */       {
/*  901 */         return ci.b;
/*      */       }
/*  903 */       if (this.k.m())
/*      */       {
/*  905 */         return ci.c;
/*      */       }
/*  907 */       if ((Math.abs(this.o - paramInt1) > 3.0D) || (Math.abs(this.p - paramInt2) > 2.0D) || (Math.abs(this.q - paramInt3) > 3.0D))
/*      */       {
/*  909 */         return ci.d;
/*      */       }
/*      */ 
/*  912 */       double d1 = 8.0D;
/*  913 */       double d2 = 5.0D;
/*  914 */       List localList = this.k.a(yy.class, wu.b(paramInt1 - d1, paramInt2 - d2, paramInt3 - d1, paramInt1 + d1, paramInt2 + d2, paramInt3 + d1));
/*  915 */       if (!localList.isEmpty()) {
/*  916 */         return ci.f;
/*      */       }
/*      */     }
/*      */ 
/*  920 */     a(0.2F, 0.2F);
/*  921 */     this.H = 0.2F;
/*  922 */     if (this.k.j(paramInt1, paramInt2, paramInt3))
/*      */     {
/*  925 */       int i = this.k.e(paramInt1, paramInt2, paramInt3);
/*  926 */       int j = pm.a(i);
/*  927 */       float f1 = 0.5F; float f2 = 0.5F;
/*      */ 
/*  929 */       switch (j) {
/*      */       case 0:
/*  931 */         f2 = 0.9F;
/*  932 */         break;
/*      */       case 2:
/*  934 */         f2 = 0.1F;
/*  935 */         break;
/*      */       case 1:
/*  937 */         f1 = 0.1F;
/*  938 */         break;
/*      */       case 3:
/*  940 */         f1 = 0.9F;
/*      */       }
/*      */ 
/*  943 */       d(j);
/*  944 */       d(paramInt1 + f1, paramInt2 + 0.9375F, paramInt3 + f2);
/*      */     } else {
/*  946 */       d(paramInt1 + 0.5F, paramInt2 + 0.9375F, paramInt3 + 0.5F);
/*      */     }
/*  948 */     this.aK = true;
/*  949 */     this.a = 0;
/*  950 */     this.aL = new uh(paramInt1, paramInt2, paramInt3);
/*  951 */     this.r = (this.t = this.s = 0.0D);
/*      */ 
/*  953 */     if (!this.k.F) {
/*  954 */       this.k.C();
/*      */     }
/*      */ 
/*  957 */     return ci.a;
/*      */   }
/*      */ 
/*      */   private void d(int paramInt)
/*      */   {
/*  962 */     this.aM = 0.0F;
/*  963 */     this.aO = 0.0F;
/*      */ 
/*  965 */     switch (paramInt) {
/*      */     case 0:
/*  967 */       this.aO = -1.8F;
/*  968 */       break;
/*      */     case 2:
/*  970 */       this.aO = 1.8F;
/*  971 */       break;
/*      */     case 1:
/*  973 */       this.aM = 1.8F;
/*  974 */       break;
/*      */     case 3:
/*  976 */       this.aM = -1.8F;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void a(boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3)
/*      */   {
/*  995 */     a(0.6F, 1.8F);
/*  996 */     aa();
/*      */ 
/*  998 */     uh localuh1 = this.aL;
/*  999 */     uh localuh2 = this.aL;
/* 1000 */     if ((localuh1 != null) && (this.k.a(localuh1.a, localuh1.b, localuh1.c) == pb.S.bO)) {
/* 1001 */       pm.a(this.k, localuh1.a, localuh1.b, localuh1.c, false);
/*      */ 
/* 1003 */       localuh2 = pm.f(this.k, localuh1.a, localuh1.b, localuh1.c, 0);
/* 1004 */       if (localuh2 == null) {
/* 1005 */         localuh2 = new uh(localuh1.a, localuh1.b + 1, localuh1.c);
/*      */       }
/* 1007 */       d(localuh2.a + 0.5F, localuh2.b + this.H + 0.1F, localuh2.c + 0.5F);
/*      */     }
/*      */ 
/* 1010 */     this.aK = false;
/* 1011 */     if ((!this.k.F) && (paramBoolean2)) {
/* 1012 */       this.k.C();
/*      */     }
/* 1014 */     if (paramBoolean1)
/* 1015 */       this.a = 0;
/*      */     else {
/* 1017 */       this.a = 100;
/*      */     }
/* 1019 */     if (paramBoolean3)
/* 1020 */       a(this.aL);
/*      */   }
/*      */ 
/*      */   private boolean bk()
/*      */   {
/* 1025 */     return this.k.a(this.aL.a, this.aL.b, this.aL.c) == pb.S.bO;
/*      */   }
/*      */ 
/*      */   public static uh a(xd paramxd, uh paramuh)
/*      */   {
/* 1030 */     ca localca = paramxd.z();
/* 1031 */     localca.c(paramuh.a - 3 >> 4, paramuh.c - 3 >> 4);
/* 1032 */     localca.c(paramuh.a + 3 >> 4, paramuh.c - 3 >> 4);
/* 1033 */     localca.c(paramuh.a - 3 >> 4, paramuh.c + 3 >> 4);
/* 1034 */     localca.c(paramuh.a + 3 >> 4, paramuh.c + 3 >> 4);
/*      */ 
/* 1037 */     if (paramxd.a(paramuh.a, paramuh.b, paramuh.c) != pb.S.bO) {
/* 1038 */       return null;
/*      */     }
/*      */ 
/* 1041 */     uh localuh = pm.f(paramxd, paramuh.a, paramuh.b, paramuh.c, 0);
/* 1042 */     return localuh;
/*      */   }
/*      */ 
/*      */   public float ay() {
/* 1046 */     if (this.aL != null) {
/* 1047 */       int i = this.k.e(this.aL.a, this.aL.b, this.aL.c);
/* 1048 */       int j = pm.a(i);
/*      */ 
/* 1050 */       switch (j) {
/*      */       case 0:
/* 1052 */         return 90.0F;
/*      */       case 1:
/* 1054 */         return 0.0F;
/*      */       case 2:
/* 1056 */         return 270.0F;
/*      */       case 3:
/* 1058 */         return 180.0F;
/*      */       }
/*      */     }
/* 1061 */     return 0.0F;
/*      */   }
/*      */ 
/*      */   public boolean az()
/*      */   {
/* 1066 */     return this.aK;
/*      */   }
/*      */ 
/*      */   public boolean aA() {
/* 1070 */     return (this.aK) && (this.a >= 100);
/*      */   }
/*      */ 
/*      */   public int aB() {
/* 1074 */     return this.a;
/*      */   }
/*      */ 
/*      */   public void b(String paramString)
/*      */   {
/*      */   }
/*      */ 
/*      */   public uh aC()
/*      */   {
/* 1100 */     return this.b;
/*      */   }
/*      */ 
/*      */   public void a(uh paramuh) {
/* 1104 */     if (paramuh != null)
/* 1105 */       this.b = new uh(paramuh);
/*      */     else
/* 1107 */       this.b = null;
/*      */   }
/*      */ 
/*      */   public void a(ajw paramajw)
/*      */   {
/* 1112 */     a(paramajw, 1);
/*      */   }
/*      */ 
/*      */   public void a(ajw paramajw, int paramInt)
/*      */   {
/*      */   }
/*      */ 
/*      */   protected void aD() {
/* 1120 */     super.aD();
/*      */ 
/* 1122 */     a(gv.u, 1);
/* 1123 */     if (W())
/* 1124 */       c(0.8F);
/*      */     else
/* 1126 */       c(0.2F);
/*      */   }
/*      */ 
/*      */   public void a_(float paramFloat1, float paramFloat2)
/*      */   {
/* 1133 */     double d1 = this.o; double d2 = this.p; double d3 = this.q;
/*      */ 
/* 1135 */     if (this.aT.b) {
/* 1136 */       double d4 = this.s;
/* 1137 */       float f = this.bu;
/* 1138 */       this.bu = 0.05F;
/* 1139 */       super.a_(paramFloat1, paramFloat2);
/* 1140 */       this.s = (d4 * 0.6D);
/* 1141 */       this.bu = f;
/*      */     } else {
/* 1143 */       super.a_(paramFloat1, paramFloat2);
/*      */     }
/*      */ 
/* 1146 */     i(this.o - d1, this.p - d2, this.q - d3);
/*      */   }
/*      */ 
/*      */   public void i(double paramDouble1, double paramDouble2, double paramDouble3)
/*      */   {
/* 1151 */     if (this.j != null)
/* 1152 */       return;
/*      */     int i;
/* 1154 */     if (a(acn.g)) {
/* 1155 */       i = Math.round(gk.a(paramDouble1 * paramDouble1 + paramDouble2 * paramDouble2 + paramDouble3 * paramDouble3) * 100.0F);
/* 1156 */       if (i > 0) {
/* 1157 */         a(gv.q, i);
/* 1158 */         c(0.015F * i * 0.01F);
/*      */       }
/* 1160 */     } else if (H()) {
/* 1161 */       i = Math.round(gk.a(paramDouble1 * paramDouble1 + paramDouble3 * paramDouble3) * 100.0F);
/* 1162 */       if (i > 0) {
/* 1163 */         a(gv.m, i);
/* 1164 */         c(0.015F * i * 0.01F);
/*      */       }
/* 1166 */     } else if (p()) {
/* 1167 */       if (paramDouble2 > 0.0D)
/* 1168 */         a(gv.o, (int)Math.round(paramDouble2 * 100.0D));
/*      */     }
/* 1170 */     else if (this.z) {
/* 1171 */       i = Math.round(gk.a(paramDouble1 * paramDouble1 + paramDouble3 * paramDouble3) * 100.0F);
/* 1172 */       if (i > 0) {
/* 1173 */         a(gv.l, i);
/* 1174 */         if (W())
/* 1175 */           c(0.09999999F * i * 0.01F);
/*      */         else
/* 1177 */           c(0.01F * i * 0.01F);
/*      */       }
/*      */     }
/*      */     else {
/* 1181 */       i = Math.round(gk.a(paramDouble1 * paramDouble1 + paramDouble3 * paramDouble3) * 100.0F);
/* 1182 */       if (i > 25)
/* 1183 */         a(gv.p, i);
/*      */     }
/*      */   }
/*      */ 
/*      */   private void k(double paramDouble1, double paramDouble2, double paramDouble3)
/*      */   {
/* 1189 */     if (this.j != null) {
/* 1190 */       int i = Math.round(gk.a(paramDouble1 * paramDouble1 + paramDouble2 * paramDouble2 + paramDouble3 * paramDouble3) * 100.0F);
/* 1191 */       if (i > 0)
/* 1192 */         if ((this.j instanceof ama)) {
/* 1193 */           a(gv.r, i);
/*      */ 
/* 1195 */           if (this.c == null)
/* 1196 */             this.c = new uh(gk.c(this.o), gk.c(this.p), gk.c(this.q));
/* 1197 */           else if (this.c.b(gk.c(this.o), gk.c(this.p), gk.c(this.q)) >= 1000.0D) {
/* 1198 */             a(dp.q, 1);
/*      */           }
/*      */         }
/* 1201 */         else if ((this.j instanceof ep)) {
/* 1202 */           a(gv.s, i);
/* 1203 */         } else if ((this.j instanceof qr)) {
/* 1204 */           a(gv.t, i);
/*      */         }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void e(float paramFloat)
/*      */   {
/* 1212 */     if (this.aT.c) return;
/*      */ 
/* 1214 */     if (paramFloat >= 2.0F) {
/* 1215 */       a(gv.n, (int)Math.round(paramFloat * 100.0D));
/*      */     }
/* 1217 */     super.e(paramFloat);
/*      */   }
/*      */ 
/*      */   public void b(acq paramacq) {
/* 1221 */     if ((paramacq instanceof yy))
/* 1222 */       a(dp.s);
/*      */   }
/*      */ 
/*      */   public int b(aan paramaan, int paramInt)
/*      */   {
/* 1228 */     int i = super.b(paramaan, paramInt);
/* 1229 */     if ((paramaan.c == yr.aR.bQ) && (this.aZ != null)) {
/* 1230 */       i = paramaan.b() + 16; } else {
/* 1231 */       if (paramaan.a().c())
/* 1232 */         return paramaan.a().a(paramaan.i(), paramInt);
/* 1233 */       if ((this.d != null) && (paramaan.c == yr.k.bQ)) {
/* 1234 */         int j = paramaan.l() - this.e;
/* 1235 */         if (j >= 18) {
/* 1236 */           return 133;
/*      */         }
/* 1238 */         if (j > 13) {
/* 1239 */           return 117;
/*      */         }
/* 1241 */         if (j > 0)
/* 1242 */           return 101;
/*      */       }
/*      */     }
/* 1245 */     return i;
/*      */   }
/*      */ 
/*      */   public void R()
/*      */   {
/* 1250 */     if (this.aP > 0) {
/* 1251 */       this.aP = 10;
/* 1252 */       return;
/*      */     }
/*      */ 
/* 1255 */     this.aQ = true;
/*      */   }
/*      */ 
/*      */   public void i(int paramInt) {
/* 1259 */     this.av += paramInt;
/* 1260 */     int i = 2147483647 - this.aV;
/* 1261 */     if (paramInt > i) {
/* 1262 */       paramInt = i;
/*      */     }
/*      */ 
/* 1265 */     this.aW += paramInt / aE();
/* 1266 */     this.aV += paramInt;
/* 1267 */     while (this.aW >= 1.0F) {
/* 1268 */       this.aW = ((this.aW - 1.0F) * aE());
/* 1269 */       bl();
/* 1270 */       this.aW /= aE();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void j(int paramInt) {
/* 1275 */     this.aU -= paramInt;
/* 1276 */     if (this.aU < 0)
/* 1277 */       this.aU = 0;
/*      */   }
/*      */ 
/*      */   public int aE()
/*      */   {
/* 1282 */     return 7 + (this.aU * 7 >> 1);
/*      */   }
/*      */ 
/*      */   private void bl()
/*      */   {
/* 1287 */     this.aU += 1;
/*      */   }
/*      */ 
/*      */   public void c(float paramFloat)
/*      */   {
/* 1300 */     if (this.aT.a) return;
/*      */ 
/* 1302 */     if (!this.k.F)
/* 1303 */       this.as.a(paramFloat);
/*      */   }
/*      */ 
/*      */   public ne aF()
/*      */   {
/* 1308 */     return this.as;
/*      */   }
/*      */ 
/*      */   public boolean a(boolean paramBoolean) {
/* 1312 */     return ((paramBoolean) || (this.as.c())) && (!this.aT.a);
/*      */   }
/*      */ 
/*      */   public boolean aG() {
/* 1316 */     return (bb() > 0) && (bb() < d());
/*      */   }
/*      */ 
/*      */   public void c(aan paramaan, int paramInt) {
/* 1320 */     if (paramaan == this.d) return;
/* 1321 */     this.d = paramaan;
/* 1322 */     this.e = paramInt;
/* 1323 */     if (!this.k.F)
/* 1324 */       e(true);
/*      */   }
/*      */ 
/*      */   public boolean e(int paramInt1, int paramInt2, int paramInt3)
/*      */   {
/* 1335 */     return true;
/*      */   }
/*      */ 
/*      */   protected int b(yw paramyw) {
/* 1339 */     int i = this.aU * 7;
/* 1340 */     if (i > 100) {
/* 1341 */       return 100;
/*      */     }
/* 1343 */     return i;
/*      */   }
/*      */ 
/*      */   protected boolean aH()
/*      */   {
/* 1349 */     return true;
/*      */   }
/*      */ 
/*      */   public void c(int paramInt)
/*      */   {
/*      */   }
/*      */ 
/*      */   public void d(yw paramyw)
/*      */   {
/* 1362 */     this.ap.a(paramyw.ap);
/*      */ 
/* 1364 */     this.bx = paramyw.bx;
/* 1365 */     this.as = paramyw.as;
/*      */ 
/* 1367 */     this.aU = paramyw.aU;
/* 1368 */     this.aV = paramyw.aV;
/* 1369 */     this.aW = paramyw.aW;
/*      */ 
/* 1371 */     this.av = paramyw.av;
/*      */   }
/*      */ 
/*      */   protected boolean e_()
/*      */   {
/* 1376 */     return !this.aT.b;
/*      */   }
/*      */ 
/*      */   public void aI()
/*      */   {
/*      */   }
/*      */ }

/* Location:           C:\Users\Anatoli\repos\twoleanfour-client\Cape Mod Source\
 * Qualified Name:     yw
 * JD-Core Version:    0.6.0
 */
