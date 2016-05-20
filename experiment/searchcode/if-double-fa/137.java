/*     */ import com.google.common.base.Charsets;
/*     */ import com.google.common.collect.Lists;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.util.concurrent.GenericFutureListener;
/*     */ import java.io.ByteArrayInputStream;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Random;
/*     */ import net.minecraft.server.MinecraftServer;
/*     */ import org.apache.commons.lang3.StringUtils;
/*     */ import org.apache.logging.log4j.LogManager;
/*     */ import org.apache.logging.log4j.Logger;
/*     */ 
/*     */ public class na
/*     */   implements ie
/*     */ {
/*  45 */   private static final Logger c = LogManager.getLogger();
/*     */   public final ef a;
/*     */   private final MinecraftServer d;
/*     */   public mp b;
/*     */   private int e;
/*     */   private int f;
/*     */   private boolean g;
/*     */   private int h;
/*     */   private long i;
/*  57 */   private static Random j = new Random();
/*     */   private long k;
/*     */   private int l;
/*     */   private int m;
/*  63 */   private oq n = new oq();
/*     */   private double o;
/*     */   private double p;
/*     */   private double q;
/* 114 */   private boolean r = true;
private a tf_1;
private b tf_2;
/*     */ 
/*     */   public na(MinecraftServer paramMinecraftServer, ef paramef, mp parammp)
/*     */   {
/*  66 */     this.d = paramMinecraftServer;
/*  67 */     this.a = paramef;
/*  68 */     paramef.a(this);
/*  69 */     this.b = parammp;
/*  70 */     parammp.a = this;
/*     */   }
/*     */ 
/*     */   public void a()
/*     */   {
/*  75 */     this.g = false;
/*  76 */     this.e += 1;
/*     */ 
/*  78 */     this.d.a.a("keepAlive");
/*  79 */     if (this.e - this.k > 40L) {
/*  80 */       this.k = this.e;
/*  81 */       this.i = d();
/*  82 */       this.h = ((int)this.i);
/*  83 */       a(new gn(this.h));
/*     */     }
/*     */ 
/*  86 */     if (this.l > 0) {
/*  87 */       this.l -= 1;
/*     */     }
/*  89 */     if (this.m > 0) {
/*  90 */       this.m -= 1;
/*     */     }
/*     */ 
/*  93 */     this.d.a.c("playerTick");
/*  94 */     this.d.a.b();
/*     */   }
/*     */ 
/*     */   public ef b()
/*     */   {
/*  99 */     return this.a;
/*     */   }
/*     */ 
/*     */   public void c(String paramString) {
/* 103 */     fh localfh = new fh(paramString);
/* 104 */     this.a.a(new gj(localfh), new GenericFutureListener[] { new nb(this, localfh) });
/*     */ 
/* 110 */     this.a.g();
/*     */   }
/*     */ 
/*     */   public void a(jb paramjb)
/*     */   {
/* 118 */     this.b.a(paramjb.c(), paramjb.d(), paramjb.e(), paramjb.f());
/*     */   }
/*     */ 
/*     */   public void a(iu paramiu)
/*     */   {
/* 123 */     mm localmm = this.d.a(this.b.ap);
/*     */ 
/* 125 */     this.g = true;
/*     */ 
/* 127 */     if (this.b.i) return;
/* 128 */     if (!this.r) {
/* 129 */       double d1 = paramiu.d() - this.p;
/* 130 */       if ((paramiu.c() == this.o) && (d1 * d1 < 0.01D) && (paramiu.e() == this.q)) {
/* 131 */         this.r = true;
/*     */       }
/*     */     }
/*     */ 
/* 135 */     if (this.r) {
/* 136 */       if (this.b.m != null) {
/* 137 */         float f1 = this.b.y;
/* 138 */         float f2 = this.b.z;
/* 139 */         this.b.m.ab();
/* 140 */         double d3 = this.b.s;
/* 141 */         double d4 = this.b.t;
/* 142 */         double d5 = this.b.u;
/*     */ 
/* 144 */         if (paramiu.k()) {
/* 145 */           f1 = paramiu.g();
/* 146 */           f2 = paramiu.h();
/*     */         }
/*     */ 
/* 149 */         this.b.D = paramiu.i();
/* 150 */         this.b.i();
/* 151 */         this.b.V = 0.0F;
/* 152 */         this.b.a(d3, d4, d5, f1, f2);
/* 153 */         if (this.b.m != null) this.b.m.ab();
/* 154 */         this.d.af().d(this.b);
/*     */ 
/* 157 */         if (this.r) {
/* 158 */           this.o = this.b.s;
/* 159 */           this.p = this.b.t;
/* 160 */           this.q = this.b.u;
/*     */         }
/* 162 */         localmm.g(this.b);
/* 163 */         return;
/*     */       }
/*     */ 
/* 166 */       if (this.b.bl()) {
/* 167 */         this.b.i();
/* 168 */         this.b.a(this.o, this.p, this.q, this.b.y, this.b.z);
/* 169 */         localmm.g(this.b);
/* 170 */         return;
/*     */       }
/*     */ 
/* 173 */       double d2 = this.b.t;
/* 174 */       this.o = this.b.s;
/* 175 */       this.p = this.b.t;
/* 176 */       this.q = this.b.u;
/*     */ 
/* 178 */       double d3 = this.b.s;
/* 179 */       double d4 = this.b.t;
/* 180 */       double d5 = this.b.u;
/*     */ 
/* 182 */       float f3 = this.b.y;
/* 183 */       float f4 = this.b.z;
/*     */ 
/* 185 */       if ((paramiu.j()) && (paramiu.d() == -999.0D) && (paramiu.f() == -999.0D)) {
/* 186 */         paramiu.a(false);
/*     */       }
/*     */ 
/* 189 */       if (paramiu.j()) {
/* 190 */         d3 = paramiu.c();
/* 191 */         d4 = paramiu.d();
/* 192 */         d5 = paramiu.e();
/* 193 */         double d6 = paramiu.f() - paramiu.d();
/* 194 */         if ((!this.b.bl()) && ((d6 > 1.65D) || (d6 < 0.1D))) {
/* 195 */           c("Illegal stance");
/* 196 */           c.warn(this.b.b_() + " had an illegal stance: " + d6);
/* 197 */           return;
/*     */         }
/* 199 */         if ((Math.abs(paramiu.c()) > 32000000.0D) || (Math.abs(paramiu.e()) > 32000000.0D)) {
/* 200 */           c("Illegal position");
/* 201 */           return;
/*     */         }
/*     */       }
/* 204 */       if (paramiu.k()) {
/* 205 */         f3 = paramiu.g();
/* 206 */         f4 = paramiu.h();
/*     */       }
/*     */ 
/* 209 */       this.b.i();
/* 210 */       this.b.V = 0.0F;
/* 211 */       this.b.a(this.o, this.p, this.q, f3, f4);
/* 212 */       if (!this.r) {
/* 213 */         return;
/*     */       }
/*     */ 
/* 216 */       double d6 = d3 - this.b.s;
/* 217 */       double d7 = d4 - this.b.t;
/* 218 */       double d8 = d5 - this.b.u;
/*     */ 
/* 220 */       double d9 = Math.min(Math.abs(d6), Math.abs(this.b.v));
/* 221 */       double d10 = Math.min(Math.abs(d7), Math.abs(this.b.w));
/* 222 */       double d11 = Math.min(Math.abs(d8), Math.abs(this.b.x));
/*     */ 
/* 224 */       double d12 = d9 * d9 + d10 * d10 + d11 * d11;
/* 225 */       if ((d12 > 100.0D) && ((!this.d.L()) || (!this.d.K().equals(this.b.b_())))) {
/* 226 */         c.warn(this.b.b_() + " moved too quickly! " + d6 + "," + d7 + "," + d8 + " (" + d9 + ", " + d10 + ", " + d11 + ")");
/* 227 */         a(this.o, this.p, this.q, this.b.y, this.b.z);
/* 228 */         return;
/*     */       }
/*     */ 
/* 231 */       float f5 = 0.0625F;
/* 232 */       boolean bool1 = localmm.a(this.b, this.b.C.c().e(f5, f5, f5)).isEmpty();
/*     */ 
/* 234 */       if ((this.b.D) && (!paramiu.i()) && (d7 > 0.0D))
/*     */       {
/* 236 */         this.b.bi();
/*     */       }
/*     */ 
/* 239 */       this.b.d(d6, d7, d8);
/* 240 */       this.b.D = paramiu.i();
/*     */ 
/* 242 */       this.b.k(d6, d7, d8);
/*     */ 
/* 244 */       double d13 = d7;
/*     */ 
/* 246 */       d6 = d3 - this.b.s;
/* 247 */       d7 = d4 - this.b.t;
/* 248 */       if ((d7 > -0.5D) || (d7 < 0.5D)) {
/* 249 */         d7 = 0.0D;
/*     */       }
/* 251 */       d8 = d5 - this.b.u;
/* 252 */       d12 = d6 * d6 + d7 * d7 + d8 * d8;
/* 253 */       int i1 = 0;
/* 254 */       if ((d12 > 0.0625D) && (!this.b.bl()) && (!this.b.c.d())) {
/* 255 */         i1 = 1;
/* 256 */         c.warn(this.b.b_() + " moved wrongly!");
/*     */       }
/* 258 */       this.b.a(d3, d4, d5, f3, f4);
/*     */ 
/* 260 */       boolean bool2 = localmm.a(this.b, this.b.C.c().e(f5, f5, f5)).isEmpty();
/* 261 */       if ((bool1) && ((i1 != 0) || (!bool2)) && (!this.b.bl())) {
/* 262 */         a(this.o, this.p, this.q, f3, f4);
/* 263 */         return;
/*     */       }
/* 265 */       ayk localayk = this.b.C.c().b(f5, f5, f5).a(0.0D, -0.55D, 0.0D);
/* 266 */       if ((!this.d.aa()) && (!this.b.c.d()) && (!localmm.c(localayk))) {
/* 267 */         if (d13 >= -0.03125D) {
/* 268 */           this.f += 1;
/* 269 */           if (this.f > 80) {
/* 270 */             c.warn(this.b.b_() + " was kicked for floating too long!");
/* 271 */             c("Flying is not enabled on this server");
/*     */           }
/*     */         }
/*     */       }
/*     */       else {
/* 276 */         this.f = 0;
/*     */       }
/*     */ 
/* 279 */       this.b.D = paramiu.i();
/* 280 */       this.d.af().d(this.b);
/* 281 */       this.b.b(this.b.t - d2, paramiu.i());
/* 282 */     } else if (this.e % 20 == 0) {
/* 283 */       a(this.o, this.p, this.q, this.b.y, this.b.z);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void a(double paramDouble1, double paramDouble2, double paramDouble3, float paramFloat1, float paramFloat2) {
/* 288 */     this.r = false;
/* 289 */     this.o = paramDouble1;
/* 290 */     this.p = paramDouble2;
/* 291 */     this.q = paramDouble3;
/* 292 */     this.b.a(paramDouble1, paramDouble2, paramDouble3, paramFloat1, paramFloat2);
/* 293 */     this.b.a.a(new fl(paramDouble1, paramDouble2 + 1.620000004768372D, paramDouble3, paramFloat1, paramFloat2, false));
/*     */   }
/*     */ 
/*     */   public void a(iz paramiz)
/*     */   {
/* 298 */     mm localmm = this.d.a(this.b.ap);
/* 299 */     this.b.v();
/*     */ 
/* 301 */     if (paramiz.g() == 4) {
/* 302 */       this.b.a(false);
/* 303 */       return;
/* 304 */     }if (paramiz.g() == 3) {
/* 305 */       this.b.a(true);
/* 306 */       return;
/* 307 */     }if (paramiz.g() == 5) {
/* 308 */       this.b.bz();
/* 309 */       return;
/*     */     }
/*     */ 
/* 312 */     int i1 = 0;
/* 313 */     if (paramiz.g() == 0) i1 = 1;
/* 314 */     if (paramiz.g() == 1) i1 = 1;
/* 315 */     if (paramiz.g() == 2) i1 = 1;
/*     */ 
/* 317 */     int i2 = paramiz.c();
/* 318 */     int i3 = paramiz.d();
/* 319 */     int i4 = paramiz.e();
/* 320 */     if (i1 != 0) {
/* 321 */       double d1 = this.b.s - (i2 + 0.5D);
/*     */ 
/* 324 */       double d2 = this.b.t - (i3 + 0.5D) + 1.5D;
/* 325 */       double d3 = this.b.u - (i4 + 0.5D);
/* 326 */       double d4 = d1 * d1 + d2 * d2 + d3 * d3;
/* 327 */       if (d4 > 36.0D) {
/* 328 */         return;
/*     */       }
/* 330 */       if (i3 >= this.d.ad()) {
/* 331 */         return;
/*     */       }
/*     */     }
/*     */ 
/* 335 */     if (paramiz.g() == 0) {
/* 336 */       if (!this.d.a(localmm, i2, i3, i4, this.b)) this.b.c.a(i2, i3, i4, paramiz.f()); else
/* 337 */         this.b.a.a(new fy(i2, i3, i4, localmm));
/*     */     }
/* 339 */     else if (paramiz.g() == 2) {
/* 340 */       this.b.c.a(i2, i3, i4);
/* 341 */       if (localmm.a(i2, i3, i4).o() != avk.a) this.b.a.a(new fy(i2, i3, i4, localmm)); 
/*     */     }
/* 342 */     else if (paramiz.g() == 1) {
/* 343 */       this.b.c.c(i2, i3, i4);
/* 344 */       if (localmm.a(i2, i3, i4).o() != avk.a) this.b.a.a(new fy(i2, i3, i4, localmm));
/*     */     }
/*     */   }
/*     */ 
/*     */   public void a(jf paramjf)
/*     */   {
/* 350 */     mm localmm = this.d.a(this.b.ap);
/* 351 */     abu localabu = this.b.bm.h();
/* 352 */     int i1 = 0;
/* 353 */     int i2 = paramjf.c();
/* 354 */     int i3 = paramjf.d();
/* 355 */     int i4 = paramjf.e();
/* 356 */     int i5 = paramjf.f();
/* 357 */     this.b.v();
/*     */     Object localObject;
/* 359 */     if (paramjf.f() == 255) {
/* 360 */       if (localabu == null) return;
/* 361 */       this.b.c.a(this.b, localmm, localabu);
/* 362 */     } else if ((paramjf.d() < this.d.ad() - 1) || ((paramjf.f() != 1) && (paramjf.d() < this.d.ad()))) {
/* 363 */       if ((this.r) && (this.b.e(i2 + 0.5D, i3 + 0.5D, i4 + 0.5D) < 64.0D) && 
/* 364 */         (!this.d.a(localmm, i2, i3, i4, this.b))) {
/* 365 */         this.b.c.a(this.b, localmm, localabu, i2, i3, i4, i5, paramjf.h(), paramjf.i(), paramjf.j());
/*     */       }
/*     */ 
/* 369 */       i1 = 1;
/*     */     } else {
/* 371 */       localObject = new fi("build.tooHigh", new Object[] { Integer.valueOf(this.d.ad()) });
/* 372 */       ((fi)localObject).b().a(tf_1.m);
/* 373 */       this.b.a.a(new ga((fa)localObject));
/* 374 */       i1 = 1;
/*     */     }
/*     */ 
/* 377 */     if (i1 != 0) {
/* 378 */       this.b.a.a(new fy(i2, i3, i4, localmm));
/*     */ 
/* 380 */       if (i5 == 0) i3--;
/* 381 */       if (i5 == 1) i3++;
/* 382 */       if (i5 == 2) i4--;
/* 383 */       if (i5 == 3) i4++;
/* 384 */       if (i5 == 4) i2--;
/* 385 */       if (i5 == 5) i2++;
/*     */ 
/* 387 */       this.b.a.a(new fy(i2, i3, i4, localmm));
/*     */     }
/*     */ 
/* 390 */     localabu = this.b.bm.h();
/* 391 */     if ((localabu != null) && (localabu.b == 0)) {
/* 392 */       this.b.bm.a[this.b.bm.c] = null;
/* 393 */       localabu = null;
/*     */     }
/*     */ 
/* 396 */     if ((localabu == null) || (localabu.n() == 0)) {
/* 397 */       this.b.g = true;
/* 398 */       this.b.bm.a[this.b.bm.c] = abu.b(this.b.bm.a[this.b.bm.c]);
/* 399 */       localObject = this.b.bo.a(this.b.bm, this.b.bm.c);
/* 400 */       this.b.bo.b();
/* 401 */       this.b.g = false;
/*     */ 
/* 403 */       if (!abu.b(this.b.bm.h(), paramjf.g()))
/* 404 */         a(new gh(this.b.bo.d, ((zp)localObject).g, this.b.bm.h()));
/*     */     }
/*     */   }
/*     */ 
/*     */   public void a(fa paramfa)
/*     */   {
/* 411 */     c.info(this.b.b_() + " lost connection: " + paramfa);
/* 412 */     this.d.au();
/* 413 */     fi localfi = new fi("multiplayer.player.left", new Object[] { this.b.c_() });
/* 414 */     localfi.b().a(tf_1.o);
/* 415 */     this.d.af().a(localfi);
/* 416 */     this.b.n();
/* 417 */     this.d.af().e(this.b);
/*     */ 
/* 419 */     if ((this.d.L()) && (this.b.b_().equals(this.d.K()))) {
/* 420 */       c.info("Stopping singleplayer server as player logged out");
/* 421 */       this.d.q();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void a(fk paramfk)
/*     */   {
/*     */     Object localObject;
/* 426 */     if ((paramfk instanceof ga)) {
/* 427 */       ga localga = (ga)paramfk;
/* 428 */       localObject = this.b.u();
/*     */ 
/* 430 */       if (localObject == xs.c) return;
/* 431 */       if ((localObject == xs.b) && (!localga.d())) return;
/*     */     }
/*     */     try
/*     */     {
/* 435 */       this.a.a(paramfk, new GenericFutureListener[0]);
/*     */     } catch (Throwable localThrowable) {
/* 437 */       localObject = tf_2.a(localThrowable, "Sending packet");
/* 438 */       k localk = ((b)localObject).a("Packet being sent");
/*     */ 
/* 440 */       localk.a("Packet class", new nc(this, paramfk));
/*     */ 
/* 447 */       throw new s((b)localObject);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void a(jc paramjc)
/*     */   {
/* 453 */     if ((paramjc.c() < 0) || (paramjc.c() >= xo.i())) {
/* 454 */       c.warn(this.b.b_() + " tried to set an invalid carried item");
/* 455 */       return;
/*     */     }
/* 457 */     this.b.bm.c = paramjc.c();
/* 458 */     this.b.v();
/*     */   }
/*     */ 
/*     */   public void a(ii paramii)
/*     */   {
/* 463 */     if (this.b.u() == xs.c) {
/* 464 */       fi localObject = new fi("chat.cannotSend", new Object[0]);
/* 465 */       ((fi)localObject).b().a(tf_1.m);
/* 466 */       a(new ga((fa)localObject));
/* 467 */       return;
/*     */     }
/* 469 */     this.b.v();
/*     */ 
/* 471 */     Object localObject = paramii.c();
/* 472 */     localObject = StringUtils.normalizeSpace((String)localObject);
/* 473 */     for (int i1 = 0; i1 < ((String)localObject).length(); i1++) {
/* 474 */       if (!t.a(((String)localObject).charAt(i1))) {
/* 475 */         c("Illegal characters in chat");
/* 476 */         return;
/*     */       }
/*     */     }
/*     */ 
/* 480 */     if (((String)localObject).startsWith("/")) {
/* 481 */       d((String)localObject);
/*     */     } else {
/* 483 */       fi localfi = new fi("chat.type.text", new Object[] { this.b.c_(), localObject });
/* 484 */       this.d.af().a(localfi, false);
/*     */     }
/*     */ 
/* 487 */     this.l += 20;
/* 488 */     if ((this.l > 200) && (!this.d.af().d(this.b.b_())))
/* 489 */       c("disconnect.spam");
/*     */   }
/*     */ 
/*     */   private void d(String paramString)
/*     */   {
/* 494 */     this.d.H().a(this.b, paramString);
/*     */   }
/*     */ 
/*     */   public void a(ig paramig)
/*     */   {
/* 499 */     this.b.v();
/* 500 */     if (paramig.d() == 1)
/* 501 */       this.b.aZ();
/*     */   }
/*     */ 
/*     */   public void a(ja paramja)
/*     */   {
/* 507 */     this.b.v();
/* 508 */     if (paramja.d() == 1) {
/* 509 */       this.b.b(true);
/* 510 */     } else if (paramja.d() == 2) {
/* 511 */       this.b.b(false);
/* 512 */     } else if (paramja.d() == 4) {
/* 513 */       this.b.c(true);
/* 514 */     } else if (paramja.d() == 5) {
/* 515 */       this.b.c(false);
/* 516 */     } else if (paramja.d() == 3) {
/* 517 */       this.b.a(false, true, true);
/* 518 */       this.r = false;
/* 519 */     } else if (paramja.d() == 6)
/*     */     {
/* 521 */       if ((this.b.m != null) && ((this.b.m instanceof uz))) {
/* 522 */         ((uz)this.b.m).w(paramja.e());
/*     */       }
/*     */     }
/* 525 */     else if (paramja.d() == 7)
/*     */     {
/* 527 */       if ((this.b.m != null) && ((this.b.m instanceof uz)))
/* 528 */         ((uz)this.b.m).g(this.b);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void a(ir paramir)
/*     */   {
/* 554 */     mm localmm = this.d.a(this.b.ap);
/* 555 */     qr localqr = paramir.a(localmm);
/* 556 */     this.b.v();
/*     */ 
/* 558 */     if (localqr != null) {
/* 559 */       boolean bool = this.b.p(localqr);
/* 560 */       double d1 = 36.0D;
/* 561 */       if (!bool) {
/* 562 */         d1 = 9.0D;
/*     */       }
/*     */ 
/* 565 */       if (this.b.f(localqr) < d1)
/* 566 */         if (paramir.c() == is.a) {
/* 567 */           this.b.q(localqr);
/* 568 */         } else if (paramir.c() == is.b) {
/* 569 */           if (((localqr instanceof wb)) || ((localqr instanceof rh)) || ((localqr instanceof xt)) || (localqr == this.b)) {
/* 570 */             c("Attempting to attack an invalid entity");
/* 571 */             this.d.f("Player " + this.b.b_() + " tried to attack an invalid entity");
/* 572 */             return;
/*     */           }
/*     */ 
/* 575 */           this.b.r(localqr);
/*     */         }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void a(ij paramij)
/*     */   {
/* 583 */     this.b.v();
/* 584 */     ik localik = paramij.c();
/* 585 */     switch (nd.a[localik.ordinal()]) {
/*     */     case 1:
/* 587 */       if (this.b.i) {
/* 588 */         this.b = this.d.af().a(this.b, 0, true);
/* 589 */       } else if (this.b.r().N().t()) {
/* 590 */         if ((this.d.L()) && (this.b.b_().equals(this.d.K()))) {
/* 591 */           this.b.a.c("You have died. Game over, man, it's game over!");
/* 592 */           this.d.S();
/*     */         } else {
/* 594 */           kc localkc = new kc(this.b.b_());
/* 595 */           localkc.b("Death in Hardcore");
/*     */ 
/* 597 */           this.d.af().e().a(localkc);
/* 598 */           this.b.a.c("You have died. Game over, man, it's game over!");
/*     */         }
/*     */       } else {
/* 601 */         if (this.b.aR() > 0.0F) return;
/* 602 */         this.b = this.d.af().a(this.b, 0, false);
/*     */       }
/* 604 */       break;
/*     */     case 2:
/* 606 */       this.b.w().a(this.b);
/* 607 */       break;
/*     */     case 3:
/* 609 */       this.b.a(nt.f);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void a(ip paramip)
/*     */   {
/* 616 */     this.b.m();
/*     */   }
/*     */ 
/*     */   public void a(io paramio)
/*     */   {
/* 621 */     this.b.v();
/* 622 */     if ((this.b.bo.d == paramio.c()) && (this.b.bo.c(this.b))) {
/* 623 */       abu localabu = this.b.bo.a(paramio.d(), paramio.e(), paramio.h(), this.b);
/*     */ 
/* 625 */       if (abu.b(paramio.g(), localabu))
/*     */       {
/* 627 */         this.b.a.a(new gc(paramio.c(), paramio.f(), true));
/* 628 */         this.b.g = true;
/* 629 */         this.b.bo.b();
/* 630 */         this.b.l();
/* 631 */         this.b.g = false;
/*     */       }
/*     */       else {
/* 634 */         this.n.a(this.b.bo.d, Short.valueOf(paramio.f()));
/* 635 */         this.b.a.a(new gc(paramio.c(), paramio.f(), false));
/* 636 */         this.b.bo.a(this.b, false);
/*     */ 
/* 638 */         ArrayList localArrayList = new ArrayList();
/* 639 */         for (int i1 = 0; i1 < this.b.bo.c.size(); i1++) {
/* 640 */           localArrayList.add(((zp)this.b.bo.c.get(i1)).d());
/*     */         }
/* 642 */         this.b.a(this.b.bo, localArrayList);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void a(in paramin)
/*     */   {
/* 650 */     this.b.v();
/* 651 */     if ((this.b.bo.d == paramin.c()) && (this.b.bo.c(this.b))) {
/* 652 */       this.b.bo.a(this.b, paramin.d());
/* 653 */       this.b.bo.b();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void a(jd paramjd)
/*     */   {
/* 659 */     if (this.b.c.d())
/*     */     {
/* 661 */       int i1 = paramjd.c() < 0 ? 1 : 0;
/* 662 */       abu localabu = paramjd.d();
/*     */ 
/* 664 */       int i2 = (paramjd.c() >= 1) && (paramjd.c() < 36 + xo.i()) ? 1 : 0;
/* 665 */       int i3 = (localabu == null) || (localabu.b() != null) ? 1 : 0;
/* 666 */       int i4 = (localabu == null) || ((localabu.k() >= 0) && (localabu.b <= 64) && (localabu.b > 0)) ? 1 : 0;
/*     */ 
/* 668 */       if ((i2 != 0) && (i3 != 0) && (i4 != 0)) {
/* 669 */         if (localabu == null)
/* 670 */           this.b.bn.a(paramjd.c(), null);
/*     */         else {
/* 672 */           this.b.bn.a(paramjd.c(), localabu);
/*     */         }
/* 674 */         this.b.bn.a(this.b, true);
/* 675 */       } else if ((i1 != 0) && (i3 != 0) && (i4 != 0) && 
/* 676 */         (this.m < 200)) {
/* 677 */         this.m += 20;
/*     */ 
/* 679 */         wb localwb = this.b.a(localabu, true);
/* 680 */         if (localwb != null)
/* 681 */           localwb.e();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void a(im paramim)
/*     */   {
/* 690 */     Short localShort = (Short)this.n.a(this.b.bo.d);
/* 691 */     if ((localShort != null) && (paramim.d() == localShort.shortValue()) && (this.b.bo.d == paramim.c()) && (!this.b.bo.c(this.b)))
/* 692 */       this.b.bo.a(this.b, true);
/*     */   }
/*     */ 
/*     */   public void a(je paramje)
/*     */   {
/* 698 */     this.b.v();
/* 699 */     mm localmm = this.d.a(this.b.ap);
/* 700 */     if (localmm.d(paramje.c(), paramje.d(), paramje.e())) {
/* 701 */       ani localani = localmm.o(paramje.c(), paramje.d(), paramje.e());
/*     */ 
/* 703 */       if ((localani instanceof aod)) {
/* 704 */         aod localaod1 = (aod)localani;
/* 705 */         if ((!localaod1.a()) || (localaod1.b() != this.b)) {
/* 706 */           this.d.f("Player " + this.b.b_() + " just tried to change non-editable sign");
/*     */           return;
/*     */         }
/*     */       }
/*     */       int i2;
/*     */       int i3;
/* 711 */       for (int i1 = 0; i1 < 4; i1++) {
/* 712 */         i2 = 1;
/* 713 */         if (paramje.f()[i1].length() > 15)
/* 714 */           i2 = 0;
/*     */         else {
/* 716 */           for (i3 = 0; i3 < paramje.f()[i1].length(); i3++) {
/* 717 */             if (!t.a(paramje.f()[i1].charAt(i3)) && !ThaiFixes.isThaiChar(paramje.f()[i1].charAt(i3))) {
/* 718 */               i2 = 0;
/*     */             }
/*     */           }
/*     */         }
/* 722 */         if (i2 == 0) {
/* 723 */           paramje.f()[i1] = "!?";
/*     */         }
/*     */       }
/* 726 */       if ((localani instanceof aod)) {
/* 727 */         int i1 = paramje.c();
/* 728 */         i2 = paramje.d();
/* 729 */         i3 = paramje.e();
/* 730 */         aod localaod2 = (aod)localani;
/* 731 */         System.arraycopy(paramje.f(), 0, localaod2.a, 0, 4);
/* 732 */         localaod2.e();
/* 733 */         localmm.g(i1, i2, i3);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void a(it paramit)
/*     */   {
/* 740 */     if (paramit.c() == this.h) {
/* 741 */       int i1 = (int)(d() - this.i);
/* 742 */       this.b.h = ((this.b.h * 3 + i1) / 4);
/*     */     }
/*     */   }
/*     */ 
/*     */   private long d() {
/* 747 */     return System.nanoTime() / 1000000L;
/*     */   }
/*     */ 
/*     */   public void a(iy paramiy)
/*     */   {
/* 752 */     this.b.bE.b = ((paramiy.d()) && (this.b.bE.c));
/*     */   }
/*     */ 
/*     */   public void a(ih paramih)
/*     */   {
/* 757 */     ArrayList localArrayList = Lists.newArrayList();
/*     */ 
/* 759 */     for (Object str : this.d.a(this.b, paramih.c())) {
/* 760 */       localArrayList.add((String)str);
/*     */     }
/*     */ 
/* 763 */     this.b.a.a(new fz((String[])localArrayList.toArray(new String[localArrayList.size()])));
/*     */   }
/*     */ 
/*     */   public void a(il paramil)
/*     */   {
/* 768 */     this.b.a(paramil);
/*     */   }
/*     */ 
/*     */   public void a(iq paramiq)
/*     */   {
/*     */     abu localabu3;
/* 773 */     if ("MC|BEdit".equals(paramiq.c())) {
/*     */       try {
/* 775 */         abu localabu1 = new ep(Unpooled.wrappedBuffer(paramiq.e())).c();
/*     */ 
/* 777 */         if (!adc.a(localabu1.q())) {
/* 778 */           throw new IOException("Invalid book tag!");
/*     */         }
/*     */ 
/* 782 */         localabu3 = this.b.bm.h();
/* 783 */         if ((localabu1.b() == abv.bA) && (localabu1.b() == localabu3.b()))
/* 784 */           localabu3.a("pages", localabu1.q().c("pages", 8));
/*     */       }
/*     */       catch (Exception localException1)
/*     */       {
/* 788 */         c.error("Couldn't handle book info", localException1);
/*     */       }
/* 790 */     } else if ("MC|BSign".equals(paramiq.c())) {
/*     */       try {
/* 792 */         abu localabu2 = new ep(Unpooled.wrappedBuffer(paramiq.e())).c();
/*     */ 
/* 794 */         if (!add.a(localabu2.q())) {
/* 795 */           throw new IOException("Invalid book tag!");
/*     */         }
/*     */ 
/* 799 */         localabu3 = this.b.bm.h();
/* 800 */         if ((localabu2.b() == abv.bB) && (localabu3.b() == abv.bA)) {
/* 801 */           localabu3.a("author", new dt(this.b.b_()));
/* 802 */           localabu3.a("title", new dt(localabu2.q().j("title")));
/* 803 */           localabu3.a("pages", localabu2.q().c("pages", 8));
/* 804 */           localabu3.a(abv.bB);
/*     */         }
/*     */       }
/*     */       catch (Exception localException2) {
/* 808 */         c.error("Couldn't sign book", localException2);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/*     */       int i1;
/*     */       Object localObject1;
/* 810 */       if ("MC|TrSel".equals(paramiq.c())) {
/*     */         try {
/* 812 */           DataInputStream localDataInputStream1 = new DataInputStream(new ByteArrayInputStream(paramiq.e()));
/* 813 */           i1 = localDataInputStream1.readInt();
/*     */ 
/* 815 */           localObject1 = this.b.bo;
/* 816 */           if ((localObject1 instanceof zk))
/* 817 */             ((zk)localObject1).e(i1);
/*     */         }
/*     */         catch (Exception localException3) {
/* 820 */           c.error("Couldn't select trade", localException3);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/*     */         Object localObject2;
/* 822 */         if ("MC|AdvCdm".equals(paramiq.c())) {
/* 823 */           if (!this.d.ab())
/* 824 */             this.b.a(new fi("advMode.notEnabled", new Object[0]));
/* 825 */           else if ((this.b.a(2, "")) && (this.b.bE.d))
/*     */             try {
/* 827 */               ep localep = new ep(Unpooled.wrappedBuffer(paramiq.e()));
/* 828 */               i1 = localep.readByte();
/* 829 */               localObject1 = null;
/*     */ 
/* 831 */               if (i1 == 0) {
/* 832 */                 localObject2 = this.b.o.o(localep.readInt(), localep.readInt(), localep.readInt());
/* 833 */                 if ((localObject2 instanceof ano))
/* 834 */                   localObject1 = ((ano)localObject2).a();
/*     */               }
/* 836 */               else if (i1 == 1) {
/* 837 */                 localObject2 = this.b.o.a(localep.readInt());
/*     */ 
/* 839 */                 if ((localObject2 instanceof we)) {
/* 840 */                   localObject1 = ((we)localObject2).e();
/*     */                 }
/*     */               }
/*     */ 
/* 844 */               localObject2 = localep.c(localep.readableBytes());
/*     */ 
/* 846 */               if (localObject1 != null) {
/* 847 */                 ((afg)localObject1).a((String)localObject2);
/* 848 */                 ((afg)localObject1).e();
/* 849 */                 this.b.a(new fi("advMode.setCommand.success", new Object[] { localObject2 }));
/*     */               }
/*     */             } catch (Exception localException4) {
/* 852 */               c.error("Couldn't set command block", localException4);
/*     */             }
/*     */           else
/* 855 */             this.b.a(new fi("advMode.notAllowed", new Object[0]));
/*     */         }
/* 857 */         else if ("MC|Beacon".equals(paramiq.c())) {
/* 858 */           if ((this.b.bo instanceof yo))
/*     */             try {
/* 860 */               DataInputStream localDataInputStream2 = new DataInputStream(new ByteArrayInputStream(paramiq.e()));
/* 861 */               i1 = localDataInputStream2.readInt();
/* 862 */               int i2 = localDataInputStream2.readInt();
/*     */ 
/* 864 */               localObject2 = (yo)this.b.bo;
/* 865 */               zp localzp = ((yo)localObject2).a(0);
/* 866 */               if (localzp.e()) {
/* 867 */                 localzp.a(1);
/* 868 */                 anh localanh = ((yo)localObject2).e();
/* 869 */                 localanh.d(i1);
/* 870 */                 localanh.e(i2);
/* 871 */                 localanh.e();
/*     */               }
/*     */             } catch (Exception localException5) {
/* 874 */               c.error("Couldn't set beacon", localException5);
/*     */             }
/*     */         }
/* 877 */         else if (("MC|ItemName".equals(paramiq.c())) && 
/* 878 */           ((this.b.bo instanceof yl))) {
/* 879 */           yl localyl = (yl)this.b.bo;
/*     */ 
/* 881 */           if ((paramiq.e() == null) || (paramiq.e().length < 1)) {
/* 882 */             localyl.a("");
/*     */           } else {
/* 884 */             String str = t.a(new String(paramiq.e(), Charsets.UTF_8));
/* 885 */             if (str.length() <= 30)
/* 886 */               localyl.a(str);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void a(ek paramek1, ek paramek2)
/*     */   {
/* 895 */     if (paramek2 != ek.b)
/* 896 */       throw new IllegalStateException("Unexpected change in protocol!");
/*     */   }
/*     */ }

/* Location:           E:\newHole\ThaiFixes_1.7.4\1.7.4\1.7.4.jar
 * Qualified Name:     na
 * JD-Core Version:    0.6.2
 */
