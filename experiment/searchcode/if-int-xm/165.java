package com.runescape;// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) nonlb 

import com.jagex.Util;
import com.jagex.client.*;
import com.jagex.client.Graphics2D;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;

public class Client extends NetworkedGame {
  public static void main(String args[]) {
    Client client = new Client();
    client.jt = false;
    // if (args.length > 0 && args[0].equals("member"))
    client.isMember = true;
    cacheDirectory = "./cache/";
    client.aj(client.eu, client.fu + 11, "Runescape by Andrew Gower", false);
    client.qq = 10;
  }

  public void cj() {
    if (jt) {
      String host = getDocumentBase().getHost().toLowerCase();
      if (!host.endsWith("com.jagex.com") && !host.endsWith("com.jagex.co.uk")
          && !host.endsWith("runescape.com")
          && !host.endsWith("runescape.co.uk")
          && !host.endsWith("runescape.net") && !host.endsWith("runescape.org")
          && !host.endsWith("penguin") && !host.endsWith("puffin")) {
        invalidHost = true;
        return;
      }
    }
    s(et, ft);
    int i1 = 0;
    for (int j1 = 0; j1 < 99; j1++) {
      int k1 = j1 + 1;
      int i2 = (int) ((double) k1 + 300D * Math.pow(2D, (double) k1 / 7D));
      i1 += i2;
      sx[j1] = i1 & 0xffffffc;
    }

    try {
      String s2 = getParameter("member");
      int j2 = Integer.parseInt(s2);
      if (j2 == 1)
        isMember = true;
    } catch (Exception _ex) {
    }
    if (jt)
      super.port = 43594;
    super.vp = 0;
    NetworkedGame.xc = 1000;
    NetworkedGame.wc = Revision.hc;
    try {
      String poff = getParameter("poff");
      int portOffset = Integer.parseInt(poff);
      super.port += portOffset;
      System.out.println("Offset: " + portOffset);
    } catch (Exception _ex) {
    }
    wk();
    hu = 2000;
    iu = hu + 100;
    ju = iu + 50;
    ku = ju + 300;
    lu = 2510;
    mu = lu + 10;
    yt = getGraphics();
    pj(50);
    graphics = new m(eu, fu + 12, 2700, this);
    graphics.ls = this;
    graphics.sf(0, 0, eu, fu + 12);
    Menu.ig = false;
    Menu.jg = iu;
    cy = new Menu(graphics, 5);
    int l1 = ((Graphics2D) (graphics)).yj - 199;
    byte byte0 = 36;
    dy = cy.qc(l1, byte0 + 24, 196, 90, 1, 500, true);
    gy = new Menu(graphics, 5);
    hy = gy.qc(l1, byte0 + 40, 196, 126, 1, 500, true);
    ky = new Menu(graphics, 5);
    ly = ky.qc(l1, byte0 + 24, 196, 226, 1, 500, true);
    hm();
    km();
    zt = new Scene(graphics, 15000, 15000, 1000);
    zt.ei(eu / 2, fu / 2, eu / 2, fu / 2, eu, gu);
    zt.im = 2400;
    zt.jm = 2400;
    zt.km = 1;
    zt.lm = 2300;
    zt.di(-50, -10, -50);
    wu = new World(zt, graphics);
    wu.bib = hu;
    zl();
    loadModels();
    createLandscape();
    if (isMember)
      initSounds();
    kj(100, "Starting game...");
    kl();
    draw();
    ul();
    el();
    qk();
    vj();
    vl();
  }

  public void wk() {
    byte config[] = null;
    try {
      config = unpackArchive(cacheDirectory + "config" + Revision.ic + ".jag",
          "Configuration", 10);
    } catch (IOException ioexception) {
      System.out.println("Load error:" + ioexception);
    }
    Config.np(config, isMember);
  }

  public void hm() {
    byte media[] = null;
    try {
      media = unpackArchive(cacheDirectory + "media" + Revision.kc + ".jag",
          "2d graphics", 20);
    } catch (IOException ioexception) {
      System.out.println("Load error:" + ioexception);
    }
    byte abyte1[] = Util.in("index.dat", 0, media);
    graphics.og(hu, Util.in("inv1.dat", 0, media), abyte1, 1);
    graphics.og(hu + 1, Util.in("inv2.dat", 0, media), abyte1, 6);
    graphics.og(hu + 9, Util.in("bubble.dat", 0, media), abyte1, 1);
    graphics.og(hu + 10, Util.in("runescape.dat", 0, media), abyte1, 1);
    graphics.og(hu + 11, Util.in("splat.dat", 0, media), abyte1, 3);
    graphics.og(hu + 14, Util.in("icon.dat", 0, media), abyte1, 8);
    graphics.og(hu + 22, Util.in("hbar.dat", 0, media), abyte1, 1);
    graphics.og(hu + 23, Util.in("hbar2.dat", 0, media), abyte1, 1);
    graphics.og(hu + 24, Util.in("compass.dat", 0, media), abyte1, 1);
    graphics.og(hu + 25, Util.in("buttons.dat", 0, media), abyte1, 2);
    graphics.og(iu, Util.in("scrollbar.dat", 0, media), abyte1, 2);
    graphics.og(iu + 2, Util.in("corners.dat", 0, media), abyte1, 4);
    graphics.og(iu + 6, Util.in("arrows.dat", 0, media), abyte1, 2);
    graphics.og(ku, Util.in("projectile.dat", 0, media), abyte1, Config.zlb);
    int i1 = Config.jjb;
    for (int j1 = 1; i1 > 0; j1++) {
      int k1 = i1;
      i1 -= 30;
      if (k1 > 30)
        k1 = 30;
      graphics.og(ju + (j1 - 1) * 30,
          Util.in("objects" + j1 + ".dat", 0, media), abyte1, k1);
    }

    graphics.zg(hu);
    graphics.zg(hu + 9);
    for (int l1 = 11; l1 <= 26; l1++)
      graphics.zg(hu + l1);

    for (int i2 = 0; i2 < Config.zlb; i2++)
      graphics.zg(ku + i2);

    for (int j2 = 0; j2 < Config.jjb; j2++)
      graphics.zg(ju + j2);
  }

  public void km() {
    byte peopleAndMonsters[] = null;
    byte abyte1[] = null;
    try {
      peopleAndMonsters = unpackArchive(cacheDirectory + "entity" + Revision.nc
          + ".jag", "people and monsters", 30);
    } catch (IOException ioexception) {
      System.out.println("Load error:" + ioexception);
    }
    abyte1 = Util.in("index.dat", 0, peopleAndMonsters);
    byte memberGraphics[] = null;
    byte abyte3[] = null;
    if (isMember) {
      try {
        memberGraphics = unpackArchive(cacheDirectory + "entity" + Revision.nc
            + ".mem", "member graphics", 45);
      } catch (IOException ioexception1) {
        System.out.println("Load error:" + ioexception1);
      }
      abyte3 = Util.in("index.dat", 0, memberGraphics);
    }
    int animationFrames = 0;
    ngb = 0;
    ogb = ngb;
    label0: for (int j1 = 0; j1 < Config.rkb; j1++) {
      String s1 = Config.skb[j1];
      for (int k1 = 0; k1 < j1; k1++) {
        if (!Config.skb[k1].equalsIgnoreCase(s1))
          continue;
        Config.xkb[j1] = Config.xkb[k1];
        continue label0;
      }

      byte abyte7[] = Util.in(s1 + ".dat", 0, peopleAndMonsters);
      byte abyte4[] = abyte1;
      if (abyte7 == null && isMember) {
        abyte7 = Util.in(s1 + ".dat", 0, memberGraphics);
        abyte4 = abyte3;
      }
      if (abyte7 != null) {
        graphics.og(ogb, abyte7, abyte4, 15);
        animationFrames += 15;
        if (Config.vkb[j1] == 1) {
          byte abyte8[] = Util.in(s1 + "a.dat", 0, peopleAndMonsters);
          byte abyte5[] = abyte1;
          if (abyte8 == null && isMember) {
            abyte8 = Util.in(s1 + "a.dat", 0, memberGraphics);
            abyte5 = abyte3;
          }
          graphics.og(ogb + 15, abyte8, abyte5, 3);
          animationFrames += 3;
        }
        if (Config.wkb[j1] == 1) {
          byte abyte9[] = Util.in(s1 + "f.dat", 0, peopleAndMonsters);
          byte abyte6[] = abyte1;
          if (abyte9 == null && isMember) {
            abyte9 = Util.in(s1 + "f.dat", 0, memberGraphics);
            abyte6 = abyte3;
          }
          graphics.og(ogb + 18, abyte9, abyte6, 9);
          animationFrames += 9;
        }
        if (Config.ukb[j1] != 0) {
          for (int l1 = ogb; l1 < ogb + 27; l1++)
            graphics.zg(l1);
        }
      }
      Config.xkb[j1] = ogb;
      ogb += 27;
    }

    System.out.println("Loaded: " + animationFrames + " frames of animation");
  }

  public void zl() {
    byte abyte0[] = null;
    try {
      abyte0 = unpackArchive(
          cacheDirectory + "textures" + Revision.mc + ".jag", "Textures", 50);
    } catch (IOException ioexception) {
      System.out.println("Load error:" + ioexception);
    }
    byte abyte1[] = Util.in("index.dat", 0, abyte0);
    zt.ui(Config.okb, 7, 11);
    for (int i1 = 0; i1 < Config.okb; i1++) {
      String s1 = Config.pkb[i1];
      byte abyte2[] = Util.in(s1 + ".dat", 0, abyte0);
      graphics.og(lu, abyte2, abyte1, 1);
      graphics.yf(0, 0, 128, 128, 0xff00ff);
      graphics.xg(0, 0, lu);
      int j1 = ((Graphics2D) (graphics)).pk[lu];
      String s2 = Config.qkb[i1];
      if (s2 != null && s2.length() > 0) {
        byte abyte3[] = Util.in(s2 + ".dat", 0, abyte0);
        graphics.og(lu, abyte3, abyte1, 1);
        graphics.xg(0, 0, lu);
      }
      graphics.rf(mu + i1, 0, 0, j1, j1);
      int k1 = j1 * j1;
      for (int l1 = 0; l1 < k1; l1++)
        if (((Graphics2D) (graphics)).ik[mu + i1][l1] == 65280)
          ((Graphics2D) (graphics)).ik[mu + i1][l1] = 0xff00ff;

      graphics.fg(mu + i1);
      zt.vh(i1, ((Graphics2D) (graphics)).jk[mu + i1],
          ((Graphics2D) (graphics)).kk[mu + i1], j1 / 64 - 1);
    }

  }

  public void loadModels() {
    Config.matchModel("torcha2");
    Config.matchModel("torcha3");
    Config.matchModel("torcha4");
    Config.matchModel("skulltorcha2");
    Config.matchModel("skulltorcha3");
    Config.matchModel("skulltorcha4");
    Config.matchModel("firea2");
    Config.matchModel("firea3");
    Config.matchModel("fireplacea2");
    Config.matchModel("fireplacea3");
    // if (appletStarted()) {
    byte models[] = null;
    try {
      models = unpackArchive(cacheDirectory + "models" + Revision.lc + ".jag",
          "3d models", 60);
    } catch (IOException ioexception) {
      System.out.println("Load error:" + ioexception);
    }
    for (int j1 = 0; j1 < Config.rmb; j1++) {
      int k1 = Util.kn(Config.smb[j1] + ".ob3", models);
      if (k1 != 0)
        loadedModels[j1] = new Model(models, k1, true);
      else
        loadedModels[j1] = new Model(1, 1);
      if (Config.smb[j1].equals("giantcrystal"))
        loadedModels[j1].th = true;
    }

    return;
    // }
    /*
     * kj(70, "Loading 3d models"); for (int i1 = 0; i1 < Config.rmb; i1++) {
     * loadedModels[i1] = new Model("../gamedata/models/" + Config.smb[i1] +
     * ".ob2"); if (Config.smb[i1].equals("giantcrystal")) loadedModels[i1].th =
     * true; }
     */
  }

  public void createLandscape() {
    try {
      wu.maps = unpackArchive(cacheDirectory + "maps" + Revision.jc + ".jag",
          "map", 70);
      if (isMember)
        wu.memberMaps = unpackArchive(cacheDirectory + "maps" + Revision.jc
            + ".mem", "members map", 75);
      wu.landscape = unpackArchive(cacheDirectory + "land" + Revision.jc
          + ".jag", "landscape", 80);
      if (isMember) {
        wu.memberLandscape = unpackArchive(cacheDirectory + "land"
            + Revision.jc + ".mem", "members landscape", 85);
        return;
      }
    } catch (IOException ioexception) {
      System.out.println("Load error:" + ioexception);
    }
  }

  public void initSounds() {
    try {
      sounds = unpackArchive(cacheDirectory + "sounds" + Revision.oc + ".mem",
          "Sound effects", 90);
      audio = new AudioInputStream();
      return;
    } catch (Throwable throwable) {
      System.out.println("Unable to init sounds:" + throwable);
    }
  }

  public void kl() {
    pz = new Menu(graphics, 10);
    qz = pz.dc(5, 269, 502, 56, 1, 20, true);
    rz = pz.ec(7, 324, 498, 14, 1, 80, false, true);
    sz = pz.dc(5, 269, 502, 56, 1, 20, true);
    tz = pz.dc(5, 269, 502, 56, 1, 20, true);
    pz.nc(rz);
  }

  public void hj() {
    if (invalidHost)
      return;
    if (outOfMemory)
      return;
    try {
      kt++;
      if (du == 0) {
        super.wp = 0;
        il();
      }
      if (du == 1) {
        super.wp++;
        qm();
      }
      super.uq = 0;
      super.wq = 0;
      xt++;
      if (xt > 500) {
        xt = 0;
        int i1 = (int) (Math.random() * 4D);
        if ((i1 & 1) == 1)
          tt += ut;
        if ((i1 & 2) == 2)
          vt += wt;
      }
      if (tt < -50)
        ut = 2;
      if (tt > 50)
        ut = -2;
      if (vt < -50)
        wt = 2;
      if (vt > 50)
        wt = -2;
      if (lz > 0)
        lz--;
      if (mz > 0)
        mz--;
      if (nz > 0)
        nz--;
      if (oz > 0) {
        oz--;
        return;
      }
    } catch (OutOfMemoryError _ex) {
      fm();
      outOfMemory = true;
    }
  }

  public void nj() {
    if (invalidHost) {
      Graphics g1 = getGraphics();
      g1.setColor(Color.black);
      g1.fillRect(0, 0, 512, 356);
      g1.setFont(new Font("Helvetica", 1, 20));
      g1.setColor(Color.white);
      g1.drawString("Error - unable to load game!", 50, 50);
      g1.drawString("To play RuneScape make sure you play from", 50, 100);
      g1.drawString("http://www.runescape.com", 50, 150);
      pj(1);
      return;
    }
    if (outOfMemory) {
      Graphics g2 = getGraphics();
      g2.setColor(Color.black);
      g2.fillRect(0, 0, 512, 356);
      g2.setFont(new Font("Helvetica", 1, 20));
      g2.setColor(Color.white);
      g2.drawString("Error - out of memory!", 50, 50);
      g2.drawString("Close ALL unnecessary programs", 50, 100);
      g2.drawString("and windows before loading the game", 50, 150);
      g2.drawString("RuneScape needs about 48meg of spare RAM", 50, 200);
      pj(1);
      return;
    }
    try {
      if (du == 0) {
        graphics.al = false;
        rm();
      }
      if (du == 1) {
        graphics.al = true;
        yk();
        return;
      }
    } catch (OutOfMemoryError _ex) {
      fm();
      outOfMemory = true;
    }
  }

  public void oj() {
    disconnect();
    fm();
    if (audio != null)
      audio.stop();
  }

  public void fm() {
    try {
      if (graphics != null) {
        graphics.jg();
        graphics.ek = null;
        graphics = null;
      }
      if (zt != null) {
        zt.si();
        zt = null;
      }
      loadedModels = null;
      vw = null;
      ex = null;
      yv = null;
      zv = null;
      jw = null;
      kw = null;
      bw = null;
      if (wu != null) {
        wu.djb = null;
        wu.ejb = null;
        wu.fjb = null;
        wu.gjb = null;
        wu = null;
      }
      System.gc();
      return;
    } catch (Exception _ex) {
      return;
    }
  }

  public void uj(int arg0) {
    if (du == 0) {
      if (jdb == 0)
        kdb.od(arg0);
      if (jdb == 1)
        ndb.od(arg0);
      if (jdb == 2)
        wdb.od(arg0);
      if (jdb == 3)
        lfb.od(arg0);
    }
    if (du == 1) {
      if (qgb) {
        design.od(arg0);
        return;
      }
      if (zeb) {
        if (dfb == -1)
          afb.od(arg0);
        return;
      }
      if (scb == 0 && rcb == 0)
        pz.od(arg0);
      if (scb == 3 || scb == 4 || scb == 5)
        scb = 0;
    }
  }

  public void ij(int arg0, int arg1, int arg2) {
    rt[qt] = arg1;
    st[qt] = arg2;
    qt = qt + 1 & 0x1fff;
    for (int i1 = 10; i1 < 4000; i1++) {
      int j1 = qt - i1 & 0x1fff;
      if (rt[j1] == arg1 && st[j1] == arg2) {
        boolean flag = false;
        for (int k1 = 1; k1 < i1; k1++) {
          int l1 = qt - k1 & 0x1fff;
          int i2 = j1 - k1 & 0x1fff;
          if (rt[i2] != arg1 || st[i2] != arg2)
            flag = true;
          if (rt[l1] != rt[i2] || st[l1] != st[i2])
            break;
          if (k1 == i1 - 1 && flag && fdb == 0 && edb == 0) {
            logout();
            return;
          }
        }

      }
    }

  }

  public void qk() {
    du = 0;
    jdb = 0;
    username = "";
    ieb = "";
    feb = "Please enter a username:";
    geb = "*" + username + "*";
    vv = 0;
    hw = 0;
  }

  public void wl() {
    super.ar = "";
    super.br = "";
  }

  public void logout() {
    if (du == 0)
      return;
    if (fdb > 450) {
      appendMessage("@cya@You can't logout during combat!", 3);
      return;
    }
    if (fdb > 0) {
      appendMessage("@cya@You can't logout for 10 seconds after combat", 3);
      return;
    } else {
      super.stream.beginFrame(6);
      super.stream.endFrame();
      edb = 1000;
      return;
    }
  }

  public void nk(String arg0) {
    if (audio == null)
      return;
    if (uy) {
      return;
    } else {
      audio.play(sounds, Util.kn(arg0 + ".pcm", sounds),
          Util._mthdo(arg0 + ".pcm", sounds));
      return;
    }
  }

  public void ul() {
    afb = new Menu(graphics, 100);
    int i1 = 8;
    bfb = afb
        .createCentredLabel(
            256,
            i1,
            "@yel@Please provide 5 security questions in case you lose your password",
            1, true);
    i1 += 22;
    afb.createCentredLabel(
        256,
        i1,
        "If you ever lose your password, you will need these to prove you own your account.",
        1, true);
    i1 += 13;
    afb.createCentredLabel(
        256,
        i1,
        "Your answers are encrypted and are ONLY used for password recovery purposes.",
        1, true);
    i1 += 22;
    afb.createCentredLabel(
        256,
        i1,
        "@ora@IMPORTANT:@whi@ To recover your password you must give the EXACT same answers you",
        1, true);
    i1 += 13;
    afb.createCentredLabel(
        256,
        i1,
        "give here. If you think you might forget an answer, or someone else could guess the",
        1, true);
    i1 += 13;
    afb.createCentredLabel(
        256,
        i1,
        "answer, then press the 'different question' button to get a better question.",
        1, true);
    i1 += 35;
    for (int j1 = 0; j1 < 5; j1++) {
      afb.ad(170, i1, 310, 30);
      jfb[j1] = "~:" + ifb[j1];
      efb[j1] = afb.createCentredLabel(170, i1 - 7, (j1 + 1) + ": "
          + questions[ifb[j1]], 1, true);
      ffb[j1] = afb.yc(170, i1 + 7, 310, 30, 1, 80, false, true);
      afb.ad(370, i1, 80, 30);
      afb.createCentredLabel(370, i1 - 7, "Different", 1, true);
      afb.createCentredLabel(370, i1 + 7, "Question", 1, true);
      gfb[j1] = afb.md(370, i1, 80, 30);
      afb.ad(455, i1, 80, 30);
      afb.createCentredLabel(455, i1 - 7, "Enter own", 1, true);
      afb.createCentredLabel(455, i1 + 7, "Question", 1, true);
      hfb[j1] = afb.md(455, i1, 80, 30);
      i1 += 35;
    }

    afb.nc(ffb[0]);
    i1 += 10;
    afb.ad(256, i1, 250, 30);
    afb.createCentredLabel(256, i1, "Click here when finished", 4, true);
    cfb = afb.md(256, i1, 250, 30);
  }

  public void rk() {
    if (dfb != -1) {
      if (super.br.length() > 0) {
        jfb[dfb] = super.br;
        afb.kd(efb[dfb], (dfb + 1) + ": " + jfb[dfb]);
        afb.kd(ffb[dfb], "");
        dfb = -1;
      }
      return;
    }
    afb.pd(super.rq, super.sq, super.uq, super.tq);
    for (int i1 = 0; i1 < 5; i1++)
      if (afb.rd(gfb[i1])) {
        for (boolean flag = false; !flag;) {
          ifb[i1] = (ifb[i1] + 1) % questions.length;
          flag = true;
          for (int k1 = 0; k1 < 5; k1++)
            if (k1 != i1 && ifb[k1] == ifb[i1])
              flag = false;

        }

        jfb[i1] = "~:" + ifb[i1];
        afb.kd(efb[i1], (i1 + 1) + ": " + questions[ifb[i1]]);
        afb.kd(ffb[i1], "");
      }

    for (int j1 = 0; j1 < 5; j1++)
      if (afb.rd(hfb[j1])) {
        dfb = j1;
        super.ar = "";
        super.br = "";
      }

    if (afb.rd(cfb)) {
      for (int l1 = 0; l1 < 5; l1++) {
        String s1 = afb.pc(ffb[l1]);
        if (s1 == null || s1.length() < 3) {
          afb.kd(bfb, "@yel@Please provide a longer answer to question: "
              + (l1 + 1));
          return;
        }
      }

      for (int i2 = 0; i2 < 5; i2++) {
        String s2 = afb.pc(ffb[i2]);
        for (int k2 = 0; k2 < i2; k2++) {
          String s4 = afb.pc(ffb[k2]);
          if (s2.equalsIgnoreCase(s4)) {
            afb.kd(bfb, "@yel@Each question must have a different answer");
            return;
          }
        }
      }

      super.stream.beginFrame(208);
      for (int j2 = 0; j2 < 5; j2++) {
        String s3 = jfb[j2];
        if (s3 == null || s3.length() == 0)
          s3 = String.valueOf(j2 + 1);
        if (s3.length() > 50)
          s3 = s3.substring(0, 50);
        super.stream.putInt8(s3.length());
        super.stream.putLine(s3);
        super.stream.putInt64RSA(Util.encode47(afb.pc(ffb[j2])),
            super.serverSessionID, et, ft);
      }

      super.stream.endFrame();
      for (int l2 = 0; l2 < 5; l2++) {
        ifb[l2] = l2;
        jfb[l2] = "~:" + ifb[l2];
        afb.kd(ffb[l2], "");
        afb.kd(efb[l2], (l2 + 1) + ": " + questions[ifb[l2]]);
      }

      graphics.lf();
      zeb = false;
    }
  }

  public void fk() {
    graphics.wk = false;
    graphics.lf();
    afb.hc();
    if (dfb != -1) {
      int i1 = 150;
      graphics.yf(26, i1, 460, 60, 0);
      graphics.qf(26, i1, 460, 60, 0xffffff);
      i1 += 22;
      graphics.ug("Please enter your question", 256, i1, 4, 0xffffff);
      i1 += 25;
      graphics.ug(super.ar + "*", 256, i1, 4, 0xffffff);
    }
    graphics.xg(0, fu, hu + 22);
    graphics.jf(yt, 0, 0);
  }

  public void el() {
    lfb = new Menu(graphics, 100);
    int i1 = 10;
    mfb = lfb.createCentredLabel(256, i1,
        "@yel@To prove this is your account please provide the answers to", 1,
        true);
    i1 += 15;
    nfb = lfb
        .createCentredLabel(
            256,
            i1,
            "@yel@your security questions. You will then be able to reset your password",
            1, true);
    i1 += 35;
    for (int j1 = 0; j1 < 5; j1++) {
      lfb.ad(256, i1, 410, 30);
      wfb[j1] = lfb.createCentredLabel(256, i1 - 7, (j1 + 1) + ": question?",
          1, true);
      xfb[j1] = lfb.yc(256, i1 + 7, 310, 30, 1, 80, true, true);
      i1 += 35;
    }

    lfb.nc(xfb[0]);
    lfb.ad(256, i1, 410, 30);
    lfb.createCentredLabel(256, i1 - 7,
        "If you know it, enter a previous password used on this account", 1,
        true);
    previousPassword = lfb.yc(256, i1 + 7, 310, 30, 1, 80, true, true);
    i1 += 35;
    lfb.ad(151, i1, 200, 30);
    lfb.createCentredLabel(151, i1 - 7, "Choose a NEW password", 1, true);
    newPassword = lfb.yc(146, i1 + 7, 200, 30, 1, 80, true, true);
    lfb.ad(361, i1, 200, 30);
    lfb.createCentredLabel(361, i1 - 7, "Confirm new password", 1, true);
    confirmation = lfb.yc(366, i1 + 7, 200, 30, 1, 80, true, true);
    i1 += 35;
    lfb.ad(201, i1, 100, 30);
    lfb.createCentredLabel(201, i1, "Submit", 4, true);
    rfb = lfb.md(201, i1, 100, 30);
    lfb.ad(311, i1, 100, 30);
    lfb.createCentredLabel(311, i1, "Cancel", 4, true);
    sfb = lfb.md(311, i1, 100, 30);
  }

  public void processCharacterDesign(boolean arg0) {
    design = new Menu(graphics, 100);
    design.createCentredLabel(256, 10, "Design Your Character", 4, true);
    int i1 = 140;
    int j1 = 34;
    if (arg0) {
      i1 += 116;
      j1 -= 10;
    } else {
      design.ad(i1, j1, 200, 25);
      design.createCentredLabel(i1, j1, "Appearance", 4, false);
      j1 += 15;
    }
    design.createCentredLabel(i1 - 55, j1 + 110, "Front", 3, true);
    design.createCentredLabel(i1, j1 + 110, "Side", 3, true);
    design.createCentredLabel(i1 + 55, j1 + 110, "Back", 3, true);
    byte byte0 = 54;
    j1 += 145;
    design.jc(i1 - byte0, j1, 53, 41);
    design.createCentredLabel(i1 - byte0, j1 - 8, "Head", 1, true);
    design.createCentredLabel(i1 - byte0, j1 + 8, "Type", 1, true);
    design.lc(i1 - byte0 - 40, j1, Menu.jg + 7);
    keb = design.md(i1 - byte0 - 40, j1, 20, 20);
    design.lc((i1 - byte0) + 40, j1, Menu.jg + 6);
    leb = design.md((i1 - byte0) + 40, j1, 20, 20);
    design.jc(i1 + byte0, j1, 53, 41);
    design.createCentredLabel(i1 + byte0, j1 - 8, "Hair", 1, true);
    design.createCentredLabel(i1 + byte0, j1 + 8, "Color", 1, true);
    design.lc((i1 + byte0) - 40, j1, Menu.jg + 7);
    meb = design.md((i1 + byte0) - 40, j1, 20, 20);
    design.lc(i1 + byte0 + 40, j1, Menu.jg + 6);
    neb = design.md(i1 + byte0 + 40, j1, 20, 20);
    j1 += 50;
    design.jc(i1 - byte0, j1, 53, 41);
    design.createCentredLabel(i1 - byte0, j1, "Gender", 1, true);
    design.lc(i1 - byte0 - 40, j1, Menu.jg + 7);
    oeb = design.md(i1 - byte0 - 40, j1, 20, 20);
    design.lc((i1 - byte0) + 40, j1, Menu.jg + 6);
    peb = design.md((i1 - byte0) + 40, j1, 20, 20);
    design.jc(i1 + byte0, j1, 53, 41);
    design.createCentredLabel(i1 + byte0, j1 - 8, "Top", 1, true);
    design.createCentredLabel(i1 + byte0, j1 + 8, "Color", 1, true);
    design.lc((i1 + byte0) - 40, j1, Menu.jg + 7);
    qeb = design.md((i1 + byte0) - 40, j1, 20, 20);
    design.lc(i1 + byte0 + 40, j1, Menu.jg + 6);
    reb = design.md(i1 + byte0 + 40, j1, 20, 20);
    j1 += 50;
    design.jc(i1 - byte0, j1, 53, 41);
    design.createCentredLabel(i1 - byte0, j1 - 8, "Skin", 1, true);
    design.createCentredLabel(i1 - byte0, j1 + 8, "Color", 1, true);
    design.lc(i1 - byte0 - 40, j1, Menu.jg + 7);
    seb = design.md(i1 - byte0 - 40, j1, 20, 20);
    design.lc((i1 - byte0) + 40, j1, Menu.jg + 6);
    teb = design.md((i1 - byte0) + 40, j1, 20, 20);
    design.jc(i1 + byte0, j1, 53, 41);
    design.createCentredLabel(i1 + byte0, j1 - 8, "Bottom", 1, true);
    design.createCentredLabel(i1 + byte0, j1 + 8, "Color", 1, true);
    design.lc((i1 + byte0) - 40, j1, Menu.jg + 7);
    ueb = design.md((i1 + byte0) - 40, j1, 20, 20);
    design.lc(i1 + byte0 + 40, j1, Menu.jg + 6);
    veb = design.md(i1 + byte0 + 40, j1, 20, 20);
    if (!arg0) {
      i1 = 372;
      j1 = 35;
      design.ad(i1, j1, 200, 25);
      design.createCentredLabel(i1, j1, "Character Type", 4, false);
      j1 += 22;
      design.createCentredLabel(i1, j1,
          "Each character type has different starting", 0, true);
      j1 += 13;
      design.createCentredLabel(i1, j1,
          "bonuses. But the choice you make here", 0, true);
      j1 += 13;
      design.createCentredLabel(i1, j1,
          "isn't permanent, and will change depending", 0, true);
      j1 += 13;
      design.createCentredLabel(i1, j1, "on how you play the game.", 0, true);
      j1 += 73;
      design.jc(i1, j1, 215, 125);
      String as[] = { "Adventurer", "Warrior", "Wizard", "Ranger", "Miner" };
      xeb = design.cc(i1, j1 + 2, as, 3, true);
    }
    j1 += 82;
    if (arg0)
      j1 -= 35;
    design.ad(i1, j1, 200, 30);
    if (!arg0)
      design.createCentredLabel(i1, j1, "Start Game", 4, false);
    else
      design.createCentredLabel(i1, j1, "Accept", 4, false);
    web = design.md(i1, j1, 200, 30);
  }

  public void jk() {
    graphics.wk = false;
    graphics.lf();
    design.hc();
    int i1 = 140;
    int j1 = 50;
    if (rgb) {
      i1 += 116;
      j1 -= 25;
    }
    graphics.mg(i1 - 32 - 55, j1, 64, 102, Config.xkb[ugb], ahb[xgb]);
    graphics.wf(i1 - 32 - 55, j1, 64, 102, Config.xkb[tgb], ahb[wgb], chb[ygb],
        0, false);
    graphics.wf(i1 - 32 - 55, j1, 64, 102, Config.xkb[sgb], bhb[vgb], chb[ygb],
        0, false);
    graphics.mg(i1 - 32, j1, 64, 102, Config.xkb[ugb] + 6, ahb[xgb]);
    graphics.wf(i1 - 32, j1, 64, 102, Config.xkb[tgb] + 6, ahb[wgb], chb[ygb],
        0, false);
    graphics.wf(i1 - 32, j1, 64, 102, Config.xkb[sgb] + 6, bhb[vgb], chb[ygb],
        0, false);
    graphics.mg((i1 - 32) + 55, j1, 64, 102, Config.xkb[ugb] + 12, ahb[xgb]);
    graphics.wf((i1 - 32) + 55, j1, 64, 102, Config.xkb[tgb] + 12, ahb[wgb],
        chb[ygb], 0, false);
    graphics.wf((i1 - 32) + 55, j1, 64, 102, Config.xkb[sgb] + 12, bhb[vgb],
        chb[ygb], 0, false);
    graphics.xg(0, fu, hu + 22);
    graphics.jf(yt, 0, 0);
  }

  public void vk() {
    design.pd(super.rq, super.sq, super.uq, super.tq);
    if (design.rd(keb))
      do
        sgb = ((sgb - 1) + Config.rkb) % Config.rkb;
      while ((Config.ukb[sgb] & 3) != 1 || (Config.ukb[sgb] & 4 * zgb) == 0);
    if (design.rd(leb))
      do
        sgb = (sgb + 1) % Config.rkb;
      while ((Config.ukb[sgb] & 3) != 1 || (Config.ukb[sgb] & 4 * zgb) == 0);
    if (design.rd(meb))
      vgb = ((vgb - 1) + bhb.length) % bhb.length;
    if (design.rd(neb))
      vgb = (vgb + 1) % bhb.length;
    if (design.rd(oeb) || design.rd(peb)) {
      for (zgb = 3 - zgb; (Config.ukb[sgb] & 3) != 1
          || (Config.ukb[sgb] & 4 * zgb) == 0; sgb = (sgb + 1) % Config.rkb)
        ;
      for (; (Config.ukb[tgb] & 3) != 2 || (Config.ukb[tgb] & 4 * zgb) == 0; tgb = (tgb + 1)
          % Config.rkb)
        ;
    }
    if (design.rd(qeb))
      wgb = ((wgb - 1) + ahb.length) % ahb.length;
    if (design.rd(reb))
      wgb = (wgb + 1) % ahb.length;
    if (design.rd(seb))
      ygb = ((ygb - 1) + chb.length) % chb.length;
    if (design.rd(teb))
      ygb = (ygb + 1) % chb.length;
    if (design.rd(ueb))
      xgb = ((xgb - 1) + ahb.length) % ahb.length;
    if (design.rd(veb))
      xgb = (xgb + 1) % ahb.length;
    if (design.rd(web)) {
      super.stream.beginFrame(236);
      super.stream.putInt8(zgb);
      super.stream.putInt8(sgb);
      super.stream.putInt8(tgb);
      super.stream.putInt8(ugb);
      super.stream.putInt8(vgb);
      super.stream.putInt8(wgb);
      super.stream.putInt8(xgb);
      super.stream.putInt8(ygb);
      super.stream.putInt8(design.tc(xeb));
      super.stream.endFrame();
      graphics.lf();
      qgb = false;
    }
  }

  public void draw() {
    kdb = new Menu(graphics, 50);
    int i1 = 40;
    if (!isMember) {
      kdb.createCentredLabel(256, 200 + i1, "Click on an option", 5, true);
      kdb.ad(156, 240 + i1, 120, 35);
      kdb.ad(356, 240 + i1, 120, 35);
      kdb.createCentredLabel(156, 240 + i1, "New User", 5, false);
      kdb.createCentredLabel(356, 240 + i1, "Existing User", 5, false);
      ldb = kdb.md(156, 240 + i1, 120, 35);
      mdb = kdb.md(356, 240 + i1, 120, 35);
    } else {
      kdb.createCentredLabel(256, 200 + i1, "Welcome to RuneScape", 4, true);
      kdb.createCentredLabel(256, 215 + i1,
          "You need a member account to use this server", 4, true);
      kdb.ad(256, 250 + i1, 200, 35);
      kdb.createCentredLabel(256, 250 + i1, "Click here to login", 5, false);
      mdb = kdb.md(256, 250 + i1, 200, 35);
    }
    ndb = new Menu(graphics, 50);
    i1 = 70;
    odb = ndb.createCentredLabel(256, i1 + 8,
        "To create an account please enter all the requested details", 4, true);
    i1 += 25;
    ndb.ad(256, i1 + 17, 250, 34);
    ndb.createCentredLabel(256, i1 + 8, "Choose a Username", 4, false);
    sdb = ndb.yc(256, i1 + 25, 200, 40, 4, 12, false, false);
    ndb.nc(sdb);
    i1 += 40;
    ndb.ad(141, i1 + 17, 220, 34);
    ndb.createCentredLabel(141, i1 + 8, "Choose a Password", 4, false);
    tdb = ndb.yc(141, i1 + 25, 220, 40, 4, 20, true, false);
    ndb.ad(371, i1 + 17, 220, 34);
    ndb.createCentredLabel(371, i1 + 8, "Confirm Password", 4, false);
    udb = ndb.yc(371, i1 + 25, 220, 40, 4, 20, true, false);
    i1 += 40;
    i1 += 20;
    vdb = ndb.sc(60, i1, 14);
    ndb.kc(75, i1, "I have read and agree to the terms+conditions listed at:",
        4, true);
    i1 += 15;
    ndb.createCentredLabel(256, i1, "http://www.runescape.com/runeterms.html",
        4, true);
    i1 += 20;
    ndb.ad(156, i1 + 17, 150, 34);
    ndb.createCentredLabel(156, i1 + 17, "Submit", 5, false);
    rdb = ndb.md(156, i1 + 17, 150, 34);
    ndb.ad(356, i1 + 17, 150, 34);
    ndb.createCentredLabel(356, i1 + 17, "Cancel", 5, false);
    qdb = ndb.md(356, i1 + 17, 150, 34);
    wdb = new Menu(graphics, 50);
    i1 = 230;
    xdb = wdb.createCentredLabel(256, i1 - 10,
        "Please enter your username and password", 4, true);
    i1 += 28;
    wdb.ad(140, i1, 200, 40);
    wdb.createCentredLabel(140, i1 - 10, "Username:", 4, false);
    ydb = wdb.yc(140, i1 + 10, 200, 40, 4, 12, false, false);
    i1 += 47;
    wdb.ad(190, i1, 200, 40);
    wdb.createCentredLabel(190, i1 - 10, "Password:", 4, false);
    zdb = wdb.yc(190, i1 + 10, 200, 40, 4, 20, true, false);
    i1 -= 55;
    wdb.ad(410, i1, 120, 25);
    wdb.createCentredLabel(410, i1, "Ok", 4, false);
    aeb = wdb.md(410, i1, 120, 25);
    i1 += 30;
    wdb.ad(410, i1, 120, 25);
    wdb.createCentredLabel(410, i1, "Cancel", 4, false);
    beb = wdb.md(410, i1, 120, 25);
    i1 += 30;
    wdb.ad(410, i1, 160, 25);
    wdb.createCentredLabel(410, i1, "I've lost my password", 4, false);
    ceb = wdb.md(410, i1, 160, 25);
    wdb.nc(ydb);
  }

  public void rm() {
    vcb = false;
    graphics.wk = false;
    graphics.lf();
    if (jdb == 0 || jdb == 2) {
      int i1 = (kt * 2) % 3072;
      if (i1 < 1024) {
        graphics.xg(0, 10, 2500);
        if (i1 > 768)
          graphics.qg(0, 10, 2501, i1 - 768);
      } else if (i1 < 2048) {
        graphics.xg(0, 10, 2501);
        if (i1 > 1792)
          graphics.qg(0, 10, hu + 10, i1 - 1792);
      } else {
        graphics.xg(0, 10, hu + 10);
        if (i1 > 2816)
          graphics.qg(0, 10, 2500, i1 - 2816);
      }
    }
    if (jdb == 0)
      kdb.hc();
    if (jdb == 1)
      ndb.hc();
    if (jdb == 2)
      wdb.hc();
    if (jdb == 3)
      lfb.hc();
    graphics.xg(0, fu, hu + 22);
    graphics.jf(yt, 0, 0);
  }

  public void vl() {
    int i1 = 0;
    byte byte0 = 50;
    byte byte1 = 50;
    wu.xo(byte0 * 48 + 23, byte1 * 48 + 23, i1);
    wu.qo(loadedModels);
    char c1 = '\u2600';
    char c2 = '\u1900';
    char c3 = '\u044C';
    char c4 = '\u0378';
    zt.im = 4100;
    zt.jm = 4100;
    zt.km = 1;
    zt.lm = 4000;
    zt.ai(c1, -wu.oo(c1, c2), c2, 912, c4, 0, c3 * 2);
    zt.wi();
    graphics.ff();
    graphics.ff();
    graphics.yf(0, 0, 512, 6, 0);
    for (int j1 = 6; j1 >= 1; j1--)
      graphics.sg(0, j1, 0, j1, 512, 8);

    graphics.yf(0, 194, 512, 20, 0);
    for (int k1 = 6; k1 >= 1; k1--)
      graphics.sg(0, k1, 0, 194 - k1, 512, 8);

    graphics.xg(15, 15, hu + 10);
    graphics.rf(2500, 0, 0, 512, 200);
    graphics.fg(2500);
    c1 = '\u2400';
    c2 = '\u2400';
    c3 = '\u044C';
    c4 = '\u0378';
    zt.im = 4100;
    zt.jm = 4100;
    zt.km = 1;
    zt.lm = 4000;
    zt.ai(c1, -wu.oo(c1, c2), c2, 912, c4, 0, c3 * 2);
    zt.wi();
    graphics.ff();
    graphics.ff();
    graphics.yf(0, 0, 512, 6, 0);
    for (int l1 = 6; l1 >= 1; l1--)
      graphics.sg(0, l1, 0, l1, 512, 8);

    graphics.yf(0, 194, 512, 20, 0);
    for (int i2 = 6; i2 >= 1; i2--)
      graphics.sg(0, i2, 0, 194 - i2, 512, 8);

    graphics.xg(15, 15, hu + 10);
    graphics.rf(2501, 0, 0, 512, 200);
    graphics.fg(2501);
    for (int j2 = 0; j2 < 64; j2++) {
      zt.zh(wu.fjb[0][j2]);
      zt.zh(wu.ejb[1][j2]);
      zt.zh(wu.fjb[1][j2]);
      zt.zh(wu.ejb[2][j2]);
      zt.zh(wu.fjb[2][j2]);
    }

    c1 = '\u2B80';
    c2 = '\u2880';
    c3 = '\u01F4';
    c4 = '\u0178';
    zt.im = 4100;
    zt.jm = 4100;
    zt.km = 1;
    zt.lm = 4000;
    zt.ai(c1, -wu.oo(c1, c2), c2, 912, c4, 0, c3 * 2);
    zt.wi();
    graphics.ff();
    graphics.ff();
    graphics.yf(0, 0, 512, 6, 0);
    for (int k2 = 6; k2 >= 1; k2--)
      graphics.sg(0, k2, 0, k2, 512, 8);

    graphics.yf(0, 194, 512, 20, 0);
    for (int l2 = 6; l2 >= 1; l2--)
      graphics.sg(0, l2, 0, 194, 512, 8);

    graphics.xg(15, 15, hu + 10);
    graphics.rf(hu + 10, 0, 0, 512, 200);
    graphics.fg(hu + 10);
  }

  public void il() {
    if (super.sd > 0)
      super.sd--;
    if (jdb == 0) {
      kdb.pd(super.rq, super.sq, super.uq, super.tq);
      if (kdb.rd(ldb)) {
        jdb = 1;
        ndb.kd(sdb, "");
        ndb.kd(tdb, "");
        ndb.kd(udb, "");
        ndb.nc(sdb);
        ndb.vc(vdb, 0);
        ndb.kd(odb,
            "To create an account please enter all the requested details");
      }
      if (kdb.rd(mdb)) {
        jdb = 2;
        wdb.kd(xdb, "Please enter your username and password");
        wdb.kd(ydb, "");
        wdb.kd(zdb, "");
        wdb.nc(ydb);
        return;
      }
    } else if (jdb == 1) {
      ndb.pd(super.rq, super.sq, super.uq, super.tq);
      if (ndb.rd(sdb))
        ndb.nc(tdb);
      if (ndb.rd(tdb))
        ndb.nc(udb);
      if (ndb.rd(udb))
        ndb.nc(sdb);
      if (ndb.rd(qdb))
        jdb = 0;
      if (ndb.rd(rdb)) {
        if (ndb.pc(sdb) == null || ndb.pc(sdb).length() == 0
            || ndb.pc(tdb) == null || ndb.pc(tdb).length() == 0) {
          ndb.kd(odb,
              "@yel@Please fill in ALL requested information to continue!");
          return;
        }
        if (!ndb.pc(tdb).equalsIgnoreCase(ndb.pc(udb))) {
          ndb.kd(odb,
              "@yel@The two passwords entered are not the same as each other!");
          return;
        }
        if (ndb.pc(tdb).length() < 5) {
          ndb.kd(odb, "@yel@Your password must be at least 5 letters long");
          return;
        }
        if (ndb.tc(vdb) == 0) {
          ndb.kd(odb, "@yel@You must agree to the terms+conditions to continue");
          return;
        } else {
          ndb.kd(odb, "Please wait... Creating new account");
          rm();
          tj();
          String s1 = ndb.pc(sdb);
          String s3 = ndb.pc(tdb);
          createAccount(s1, s3);
          return;
        }
      }
    } else if (jdb == 2) {
      wdb.pd(super.rq, super.sq, super.uq, super.tq);
      if (wdb.rd(beb))
        jdb = 0;
      if (wdb.rd(ydb))
        wdb.nc(zdb);
      if (wdb.rd(zdb) || wdb.rd(aeb)) {
        username = wdb.pc(ydb);
        ieb = wdb.pc(zdb);
        login(username, ieb, false);
      }
      if (wdb.rd(ceb)) {
        username = wdb.pc(ydb);
        username = Util.formatString(username, 20);
        if (username.trim().length() == 0) {
          drawMessage("You must enter your username to recover your password",
              "");
          return;
        }
        drawMessage(NetworkedGame.messageTable[6],
            NetworkedGame.messageTable[7]);
        try {
          if (appletStarted())
            super.stream = new Connection(super.host, this, super.port);
          else
            super.stream = new Connection(super.host, null, super.port);
          super.stream.oe = NetworkedGame.xc;
          super.stream.readInt32();
          super.stream.beginFrame(4);
          super.stream.putInt64(Util.encode37(username));
          super.stream.flush();
          super.stream.readInt16();
          int i1 = super.stream.read();
          System.out.println("Getpq response: " + i1);
          if (i1 == 0) {
            drawMessage(
                "Sorry, the recovery questions for this user have not been set",
                "");
            return;
          }
          for (int j1 = 0; j1 < 5; j1++) {
            int k1 = super.stream.read();
            byte abyte0[] = new byte[5000];
            super.stream.fb(k1, abyte0);
            String s6 = new String(abyte0, 0, k1);
            if (s6.startsWith("~:")) {
              s6 = s6.substring(2);
              int j2 = 0;
              try {
                j2 = Integer.parseInt(s6);
              } catch (Exception _ex) {
              }
              s6 = questions[j2];
            }
            lfb.kd(wfb[j1], s6);
          }

          if (kfb) {
            drawMessage(
                "Sorry, you have already attempted 1 recovery, try again later",
                "");
            return;
          }
          jdb = 3;
          lfb.kd(mfb,
              "@yel@To prove this is your account please provide the answers to");
          lfb.kd(nfb,
              "@yel@your security questions. You will then be able to reset your password");
          for (int l1 = 0; l1 < 5; l1++)
            lfb.kd(xfb[l1], "");

          lfb.kd(previousPassword, "");
          lfb.kd(newPassword, "");
          lfb.kd(confirmation, "");
          return;
        } catch (Exception _ex) {
          drawMessage(NetworkedGame.messageTable[12],
              NetworkedGame.messageTable[13]);
        }
        return;
      }
    } else if (jdb == 3) {
      lfb.pd(super.rq, super.sq, super.uq, super.tq);
      if (lfb.rd(rfb)) {
        String s2 = lfb.pc(newPassword);
        String s4 = lfb.pc(confirmation);
        if (!s2.equalsIgnoreCase(s4)) {
          drawMessage(
              "@yel@The two new passwords entered are not the same as each other!",
              "");
          return;
        }
        if (s2.length() < 5) {
          drawMessage("@yel@Your new password must be at least 5 letters long",
              "");
          return;
        }
        drawMessage(NetworkedGame.messageTable[6],
            NetworkedGame.messageTable[7]);
        try {
          if (appletStarted())
            super.stream = new Connection(super.host, this, super.port);
          else
            super.stream = new Connection(super.host, null, super.port);
          super.stream.oe = NetworkedGame.xc;
          int i2 = super.stream.readInt32();
          String oldp = Util.formatString(lfb.pc(previousPassword), 20);
          String newp = Util.formatString(lfb.pc(newPassword), 20);
          super.stream.beginFrame(8);
          super.stream.putInt64(Util.encode37(username));
          super.stream.putInt32(getSessionID());
          super.stream.putLineRSA(oldp + newp, i2, et, ft);
          for (int i = 0; i < 5; i++)
            super.stream.putInt64RSA(Util.encode47(lfb.pc(xfb[i])), i2, et, ft);

          super.stream.flush();
          super.stream.read();
          int response = super.stream.read();
          System.out.println("Recover response: " + response);
          if (response == 0) {
            jdb = 2;
            drawMessage("Sorry, recovery failed. You may try again in 1 hour",
                "");
            kfb = true;
            return;
          }
          if (response == 1) {
            jdb = 2;
            drawMessage(
                "Your pass has been reset. You may now use the new pass to login",
                "");
            return;
          } else {
            jdb = 2;
            drawMessage("Recovery failed! Attempts exceeded?", "");
            return;
          }
        } catch (Exception _ex) {
          drawMessage(NetworkedGame.messageTable[12],
              NetworkedGame.messageTable[13]);
        }
      }
      if (lfb.rd(sfb))
        jdb = 0;
    }
  }

  public void drawMessage(String arg0, String arg1) {
    if (jdb == 1)
      ndb.kd(odb, arg0 + " " + arg1);
    if (jdb == 2)
      wdb.kd(xdb, arg0 + " " + arg1);
    if (jdb == 3) {
      lfb.kd(mfb, arg0);
      lfb.kd(nfb, arg1);
    }
    geb = arg1;
    rm();
    tj();
  }

  public void k() {
    edb = 0;
    appendMessage("@cya@Sorry, you can't logout at the moment", 3);
  }

  public void reconnect() {
    if (edb != 0) {
      f();
      return;
    } else {
      super.reconnect();
      return;
    }
  }

  public void f() {
    jdb = 0;
    du = 0;
    edb = 0;
  }

  public void e_() {
    qcb = 0;
    edb = 0;
    jdb = 0;
    du = 1;
    wl();
    graphics.lf();
    graphics.jf(yt, 0, 0);
    for (int i1 = 0; i1 < uw; i1++) {
      zt.zh(vw[i1]);
      wu.fp(ww[i1], xw[i1], yw[i1]);
    }

    for (int j1 = 0; j1 < dx; j1++) {
      zt.zh(ex[j1]);
      wu.fo(fx[j1], gx[j1], hx[j1], ix[j1]);
    }

    uw = 0;
    dx = 0;
    ow = 0;
    vv = 0;
    for (int k1 = 0; k1 < tv; k1++)
      yv[k1] = null;

    for (int l1 = 0; l1 < uv; l1++)
      zv[l1] = null;

    hw = 0;
    for (int i2 = 0; i2 < fw; i2++)
      jw[i2] = null;

    for (int j2 = 0; j2 < gw; j2++)
      kw[j2] = null;

    for (int k2 = 0; k2 < 50; k2++)
      qy[k2] = false;

    mt = 0;
    super.uq = 0;
    super.tq = 0;
    ubb = false;
    ccb = false;
  }

  public void u() {
    String s1 = ndb.pc(sdb);
    String s2 = ndb.pc(tdb);
    jdb = 2;
    wdb.kd(xdb, "Please enter your username and password");
    wdb.kd(ydb, s1);
    wdb.kd(zdb, s2);
    rm();
    tj();
    login(s1, s2, false);
  }

  public void qm() {
    j();
    if (edb > 0)
      edb--;
    if (super.wp > 4500 && fdb == 0 && edb == 0) {
      super.wp -= 500;
      logout();
      return;
    }
    if (bw.kr == 8 || bw.kr == 9)
      fdb = 500;
    if (fdb > 0)
      fdb--;
    if (qgb) {
      vk();
      return;
    }
    if (zeb) {
      rk();
      return;
    }
    for (int i1 = 0; i1 < vv; i1++) {
      Mob l1 = zv[i1];
      int k1 = (l1.currentWaypoint + 1) % 10;
      if (l1.mr != k1) {
        int j2 = -1;
        int k4 = l1.mr;
        int j6;
        if (k4 < k1)
          j6 = k1 - k4;
        else
          j6 = (10 + k1) - k4;
        int j7 = 4;
        if (j6 > 2)
          j7 = (j6 - 1) * 4;
        if (l1.waypointsX[k4] - l1.gr > cu * 3
            || l1.waypointsY[k4] - l1.hr > cu * 3
            || l1.waypointsX[k4] - l1.gr < -cu * 3
            || l1.waypointsY[k4] - l1.hr < -cu * 3 || j6 > 8) {
          l1.gr = l1.waypointsX[k4];
          l1.hr = l1.waypointsY[k4];
        } else {
          if (l1.gr < l1.waypointsX[k4]) {
            l1.gr += j7;
            l1.jr++;
            j2 = 2;
          } else if (l1.gr > l1.waypointsX[k4]) {
            l1.gr -= j7;
            l1.jr++;
            j2 = 6;
          }
          if (l1.gr - l1.waypointsX[k4] < j7 && l1.gr - l1.waypointsX[k4] > -j7)
            l1.gr = l1.waypointsX[k4];
          if (l1.hr < l1.waypointsY[k4]) {
            l1.hr += j7;
            l1.jr++;
            if (j2 == -1)
              j2 = 4;
            else if (j2 == 2)
              j2 = 3;
            else
              j2 = 5;
          } else if (l1.hr > l1.waypointsY[k4]) {
            l1.hr -= j7;
            l1.jr++;
            if (j2 == -1)
              j2 = 0;
            else if (j2 == 2)
              j2 = 1;
            else
              j2 = 7;
          }
          if (l1.hr - l1.waypointsY[k4] < j7 && l1.hr - l1.waypointsY[k4] > -j7)
            l1.hr = l1.waypointsY[k4];
        }
        if (j2 != -1)
          l1.kr = j2;
        if (l1.gr == l1.waypointsX[k4] && l1.hr == l1.waypointsY[k4])
          l1.mr = (k4 + 1) % 10;
      } else {
        l1.kr = l1.nextDirection;
      }
      if (l1.sr > 0)
        l1.sr--;
      if (l1.ur > 0)
        l1.ur--;
      if (l1.yr > 0)
        l1.yr--;
      if (gdb > 0) {
        gdb--;
        if (gdb == 0)
          appendMessage(
              "You have been granted another life. Be more careful this time!",
              3);
        if (gdb == 0)
          appendMessage(
              "You retain your skills. Your objects land where you died", 3);
      }
    }

    for (int j1 = 0; j1 < hw; j1++) {
      Mob l2 = kw[j1];
      int k2 = (l2.currentWaypoint + 1) % 10;
      if (l2.mr != k2) {
        int l4 = -1;
        int k6 = l2.mr;
        int k7;
        if (k6 < k2)
          k7 = k2 - k6;
        else
          k7 = (10 + k2) - k6;
        int l7 = 4;
        if (k7 > 2)
          l7 = (k7 - 1) * 4;
        if (l2.waypointsX[k6] - l2.gr > cu * 3
            || l2.waypointsY[k6] - l2.hr > cu * 3
            || l2.waypointsX[k6] - l2.gr < -cu * 3
            || l2.waypointsY[k6] - l2.hr < -cu * 3 || k7 > 8) {
          l2.gr = l2.waypointsX[k6];
          l2.hr = l2.waypointsY[k6];
        } else {
          if (l2.gr < l2.waypointsX[k6]) {
            l2.gr += l7;
            l2.jr++;
            l4 = 2;
          } else if (l2.gr > l2.waypointsX[k6]) {
            l2.gr -= l7;
            l2.jr++;
            l4 = 6;
          }
          if (l2.gr - l2.waypointsX[k6] < l7 && l2.gr - l2.waypointsX[k6] > -l7)
            l2.gr = l2.waypointsX[k6];
          if (l2.hr < l2.waypointsY[k6]) {
            l2.hr += l7;
            l2.jr++;
            if (l4 == -1)
              l4 = 4;
            else if (l4 == 2)
              l4 = 3;
            else
              l4 = 5;
          } else if (l2.hr > l2.waypointsY[k6]) {
            l2.hr -= l7;
            l2.jr++;
            if (l4 == -1)
              l4 = 0;
            else if (l4 == 2)
              l4 = 1;
            else
              l4 = 7;
          }
          if (l2.hr - l2.waypointsY[k6] < l7 && l2.hr - l2.waypointsY[k6] > -l7)
            l2.hr = l2.waypointsY[k6];
        }
        if (l4 != -1)
          l2.kr = l4;
        if (l2.gr == l2.waypointsX[k6] && l2.hr == l2.waypointsY[k6])
          l2.mr = (k6 + 1) % 10;
      } else {
        l2.kr = l2.nextDirection;
        if (l2.ir == 43)
          l2.jr++;
      }
      if (l2.sr > 0)
        l2.sr--;
      if (l2.ur > 0)
        l2.ur--;
      if (l2.yr > 0)
        l2.yr--;
    }

    for (int i2 = 0; i2 < vv; i2++) {
      Mob l3 = zv[i2];
      if (l3.hs > 0)
        l3.hs--;
    }

    if (ry) {
      if (nv - bw.gr < -500 || nv - bw.gr > 500 || ov - bw.hr < -500
          || ov - bw.hr > 500) {
        nv = bw.gr;
        ov = bw.hr;
      }
    } else {
      if (nv - bw.gr < -500 || nv - bw.gr > 500 || ov - bw.hr < -500
          || ov - bw.hr > 500) {
        nv = bw.gr;
        ov = bw.hr;
      }
      if (nv != bw.gr)
        nv += (bw.gr - nv) / (16 + (lv - 500) / 15);
      if (ov != bw.hr)
        ov += (bw.hr - ov) / (16 + (lv - 500) / 15);
      if (sy) {
        int i3 = pv * 32;
        int i5 = i3 - rv;
        byte byte0 = 1;
        if (i5 != 0) {
          qv++;
          if (i5 > 128) {
            byte0 = -1;
            i5 = 256 - i5;
          } else if (i5 > 0)
            byte0 = 1;
          else if (i5 < -128) {
            byte0 = 1;
            i5 = 256 + i5;
          } else if (i5 < 0) {
            byte0 = -1;
            i5 = -i5;
          }
          rv += ((qv * i5 + 255) / 256) * byte0;
          rv &= 0xff;
        } else {
          qv = 0;
        }
      }
    }
    if (super.sq > fu - 4) {
      if (super.rq > 15 && super.rq < 96 && super.uq == 1)
        uz = 0;
      if (super.rq > 110 && super.rq < 194 && super.uq == 1) {
        uz = 1;
        pz.bf[qz] = 0xf423f;
      }
      if (super.rq > 215 && super.rq < 295 && super.uq == 1) {
        uz = 2;
        pz.bf[sz] = 0xf423f;
      }
      if (super.rq > 315 && super.rq < 395 && super.uq == 1) {
        uz = 3;
        pz.bf[tz] = 0xf423f;
      }
      super.uq = 0;
      super.tq = 0;
    }
    pz.pd(super.rq, super.sq, super.uq, super.tq);
    if (uz > 0 && super.rq >= 494 && super.sq >= fu - 66)
      super.uq = 0;
    if (pz.rd(rz)) {
      String s1 = pz.pc(rz);
      pz.kd(rz, "");
      if (s1.startsWith("::")) {
        if (s1.equalsIgnoreCase("::lostcon") && !jt)
          super.stream.close();
        else if (s1.equalsIgnoreCase("::closecon") && !jt)
          disconnect();
        else
          sendCommand(s1.substring(2));
      } else if (s1.startsWith("reportabuse ")) {
        s1 = s1.substring(12);
        long l5 = Util.encode37(s1);
        super.stream.beginFrame(10);
        super.stream.putInt64(l5);
        super.stream.endFrame();
      } else {
        int j5 = Util.jn(s1);
        q(Util.uhb, j5);
        s1 = Util.nn(Util.uhb, 0, j5, true);
        bw.sr = 150;
        bw.rr = s1;
        appendMessage(bw.dr + ": " + s1, 2);
      }
    }
    if (uz == 0) {
      for (int j3 = 0; j3 < vz; j3++)
        if (xz[j3] > 0)
          xz[j3]--;
    }
    if (gdb != 0)
      super.uq = 0;
    if (zab || yz) {
      if (super.tq != 0)
        jbb++;
      else
        jbb = 0;
      if (jbb > 300)
        kbb += 50;
      else if (jbb > 150)
        kbb += 5;
      else if (jbb > 50)
        kbb++;
      else if (jbb > 20 && (jbb & 5) == 0)
        kbb++;
    } else {
      jbb = 0;
      kbb = 0;
    }
    if (super.uq == 1)
      mt = 1;
    else if (super.uq == 2)
      mt = 2;
    zt.wh(super.rq, super.sq);
    super.uq = 0;
    if (sy) {
      if (qv == 0 || ry) {
        if (super.kq) {
          pv = pv + 1 & 7;
          super.kq = false;
          if (!mv) {
            if ((pv & 1) == 0)
              pv = pv + 1 & 7;
            for (int k3 = 0; k3 < 8; k3++) {
              if (dm(pv))
                break;
              pv = pv + 1 & 7;
            }

          }
        }
        if (super.lq) {
          pv = pv + 7 & 7;
          super.lq = false;
          if (!mv) {
            if ((pv & 1) == 0)
              pv = pv + 7 & 7;
            for (int i4 = 0; i4 < 8; i4++) {
              if (dm(pv))
                break;
              pv = pv + 7 & 7;
            }

          }
        }
      }
    } else if (super.kq)
      rv = rv + 2 & 0xff;
    else if (super.lq)
      rv = rv - 2 & 0xff;
    if (mv && lv > 550)
      lv -= 4;
    else if (!mv && lv < 750)
      lv += 4;
    if (tu > 0)
      tu--;
    else if (tu < 0)
      tu++;
    zt.gi(17);
    ou++;
    if (ou > 5) {
      ou = 0;
      pu = pu + 1 & 3;
      qu = (qu + 1) % 3;
    }
    for (int j4 = 0; j4 < uw; j4++) {
      int k5 = ww[j4];
      int l6 = xw[j4];
      if (k5 >= 0 && l6 >= 0 && k5 < 96 && l6 < 96 && yw[j4] == 74)
        vw[j4].ve(1, 0, 0);
    }

    for (int i6 = 0; i6 < ihb; i6++) {
      lhb[i6]++;
      if (lhb[i6] > 50) {
        ihb--;
        for (int i7 = i6; i7 < ihb; i7++) {
          jhb[i7] = jhb[i7 + 1];
          khb[i7] = khb[i7 + 1];
          lhb[i7] = lhb[i7 + 1];
          mhb[i7] = mhb[i7 + 1];
        }

      }
    }

  }

  public void appendMessage(String message, int arg1) {
    if (arg1 == 2 || arg1 == 4 || arg1 == 6) {
      for (; message.length() > 5 && message.charAt(0) == '@'
          && message.charAt(4) == '@'; message = message.substring(5))
        ;
      int i1 = message.indexOf(":");
      if (i1 != -1) {
        String s1 = message.substring(0, i1);
        long l1 = Util.encode37(s1);
        for (int k1 = 0; k1 < super.jd; k1++)
          if (super.kd[k1] == l1)
            return;
      }
    }
    if (arg1 == 2)
      message = "@yel@" + message;
    if (arg1 == 3 || arg1 == 4)
      message = "@whi@" + message;
    if (arg1 == 6)
      message = "@cya@" + message;
    if (uz != 0) {
      if (arg1 == 4 || arg1 == 3)
        lz = 200;
      if (arg1 == 2 && uz != 1)
        mz = 200;
      if (arg1 == 5 && uz != 2)
        nz = 200;
      if (arg1 == 6 && uz != 3)
        oz = 200;
      if (arg1 == 3 && uz != 0)
        uz = 0;
      if (arg1 == 6 && uz != 3 && uz != 0)
        uz = 0;
    }
    for (int j1 = vz - 1; j1 > 0; j1--) {
      wz[j1] = wz[j1 - 1];
      xz[j1] = xz[j1 - 1];
    }

    wz[0] = message;
    xz[0] = 300;
    if (arg1 == 2)
      if (pz.bf[qz] == pz.cf[qz] - 4)
        pz.gc(qz, message, true);
      else
        pz.gc(qz, message, false);
    if (arg1 == 5)
      if (pz.bf[sz] == pz.cf[sz] - 4)
        pz.gc(sz, message, true);
      else
        pz.gc(sz, message, false);
    if (arg1 == 6) {
      if (pz.bf[tz] == pz.cf[tz] - 4) {
        pz.gc(tz, message, true);
        return;
      }
      pz.gc(tz, message, false);
    }
  }

  public void g(String arg0) {
    if (arg0.startsWith("@bor@")) {
      appendMessage(arg0, 4);
      return;
    }
    if (arg0.startsWith("@que@")) {
      appendMessage("@whi@" + arg0, 5);
      return;
    }
    if (arg0.startsWith("@pri@")) {
      appendMessage(arg0, 6);
      return;
    } else {
      appendMessage(arg0, 3);
      return;
    }
  }

  public Mob pk(int arg0, int arg1, int arg2, int arg3) {
    if (yv[arg0] == null) {
      yv[arg0] = new Mob();
      yv[arg0].er = arg0;
      yv[arg0].fr = 0;
    }
    Mob l1 = yv[arg0];
    boolean flag = false;
    for (int i1 = 0; i1 < wv; i1++) {
      if (aw[i1].er != arg0)
        continue;
      flag = true;
      break;
    }

    if (flag) {
      l1.nextDirection = arg3;
      int j1 = l1.currentWaypoint;
      if (arg1 != l1.waypointsX[j1] || arg2 != l1.waypointsY[j1]) {
        l1.currentWaypoint = j1 = (j1 + 1) % 10;
        l1.waypointsX[j1] = arg1;
        l1.waypointsY[j1] = arg2;
      }
    } else {
      l1.er = arg0;
      l1.mr = 0;
      l1.currentWaypoint = 0;
      l1.waypointsX[0] = l1.gr = arg1;
      l1.waypointsY[0] = l1.hr = arg2;
      l1.nextDirection = l1.kr = arg3;
      l1.jr = 0;
    }
    zv[vv++] = l1;
    return l1;
  }

  public Mob om(int arg0, int arg1, int arg2, int arg3, int arg4) {
    if (jw[arg0] == null) {
      jw[arg0] = new Mob();
      jw[arg0].er = arg0;
    }
    Mob l1 = jw[arg0];
    boolean flag = false;
    for (int i1 = 0; i1 < iw; i1++) {
      if (lw[i1].er != arg0)
        continue;
      flag = true;
      break;
    }

    if (flag) {
      l1.ir = arg4;
      l1.nextDirection = arg3;
      int j1 = l1.currentWaypoint;
      if (arg1 != l1.waypointsX[j1] || arg2 != l1.waypointsY[j1]) {
        l1.currentWaypoint = j1 = (j1 + 1) % 10;
        l1.waypointsX[j1] = arg1;
        l1.waypointsY[j1] = arg2;
      }
    } else {
      l1.er = arg0;
      l1.mr = 0;
      l1.currentWaypoint = 0;
      l1.waypointsX[0] = l1.gr = arg1;
      l1.waypointsY[0] = l1.hr = arg2;
      l1.ir = arg4;
      l1.nextDirection = l1.kr = arg3;
      l1.jr = 0;
    }
    kw[hw++] = l1;
    return l1;
  }

  public void n(int arg0, int arg1, byte arg2[]) {
    try {
      if (arg0 == 255) {
        wv = vv;
        for (int i1 = 0; i1 < wv; i1++)
          aw[i1] = zv[i1];

        int j8 = 8;
        cw = Util.readBits(arg2, j8, 10);
        j8 += 10;
        dw = Util.readBits(arg2, j8, 12);
        j8 += 12;
        int k14 = Util.readBits(arg2, j8, 4);
        j8 += 4;
        boolean flag1 = tm(cw, dw);
        cw -= bv;
        dw -= cv;
        int k23 = cw * cu + 64;
        int i27 = dw * cu + 64;
        if (flag1) {
          bw.currentWaypoint = 0;
          bw.mr = 0;
          bw.gr = bw.waypointsX[0] = k23;
          bw.hr = bw.waypointsY[0] = i27;
        }
        vv = 0;
        bw = pk(ew, k23, i27, k14);
        int k30 = Util.readBits(arg2, j8, 8);
        j8 += 8;
        for (int j35 = 0; j35 < k30; j35++) {
          Mob l38 = aw[j35 + 1];
          int j41 = Util.readBits(arg2, j8, 1);
          j8++;
          if (j41 != 0) {
            int j43 = Util.readBits(arg2, j8, 1);
            j8++;
            if (j43 == 0) {
              int l44 = Util.readBits(arg2, j8, 3);
              j8 += 3;
              int l45 = l38.currentWaypoint;
              int j46 = l38.waypointsX[l45];
              int k46 = l38.waypointsY[l45];
              if (l44 == 2 || l44 == 1 || l44 == 3)
                j46 += cu;
              if (l44 == 6 || l44 == 5 || l44 == 7)
                j46 -= cu;
              if (l44 == 4 || l44 == 3 || l44 == 5)
                k46 += cu;
              if (l44 == 0 || l44 == 1 || l44 == 7)
                k46 -= cu;
              l38.nextDirection = l44;
              l38.currentWaypoint = l45 = (l45 + 1) % 10;
              l38.waypointsX[l45] = j46;
              l38.waypointsY[l45] = k46;
            } else {
              int i45 = Util.readBits(arg2, j8, 4);
              if ((i45 & 0xc) == 12) {
                j8 += 2;
                continue;
              }
              l38.nextDirection = Util.readBits(arg2, j8, 4);
              j8 += 4;
            }
          }
          zv[vv++] = l38;
        }

        int k38 = 0;
        while (j8 + 24 < arg1 * 8) {
          int k41 = Util.readBits(arg2, j8, 11);
          j8 += 11;
          int k43 = Util.readBits(arg2, j8, 5);
          j8 += 5;
          if (k43 > 15)
            k43 -= 32;
          int j45 = Util.readBits(arg2, j8, 5);
          j8 += 5;
          if (j45 > 15)
            j45 -= 32;
          int l14 = Util.readBits(arg2, j8, 4);
          j8 += 4;
          int i46 = Util.readBits(arg2, j8, 1);
          j8++;
          int l23 = (cw + k43) * cu + 64;
          int j27 = (dw + j45) * cu + 64;
          pk(k41, l23, j27, l14);
          if (i46 == 0)
            mw[k38++] = k41;
        }
        if (k38 > 0) {
          super.stream.beginFrame(254);
          super.stream.putInt16(k38);
          for (int l41 = 0; l41 < k38; l41++) {
            Mob l43 = yv[mw[l41]];
            super.stream.putInt16(l43.er);
            super.stream.putInt16(l43.fr);
          }

          super.stream.endFrame();
          k38 = 0;
          return;
        }
      } else {
        if (arg0 == 254) {
          for (int j1 = 1; j1 < arg1;)
            if (Util.unsign(arg2[j1]) == 255) {
              int k8 = 0;
              int i15 = cw + arg2[j1 + 1] >> 3;
              int l19 = dw + arg2[j1 + 2] >> 3;
              j1 += 3;
              for (int i24 = 0; i24 < ow; i24++) {
                int k27 = (pw[i24] >> 3) - i15;
                int l30 = (qw[i24] >> 3) - l19;
                if (k27 != 0 || l30 != 0) {
                  if (i24 != k8) {
                    pw[k8] = pw[i24];
                    qw[k8] = qw[i24];
                    rw[k8] = rw[i24];
                    sw[k8] = sw[i24];
                  }
                  k8++;
                }
              }

              ow = k8;
            } else {
              int l8 = Util.readInt8(arg2, j1);
              j1 += 2;
              int j15 = cw + arg2[j1++];
              int i20 = dw + arg2[j1++];
              if ((l8 & 0x8000) == 0) {
                pw[ow] = j15;
                qw[ow] = i20;
                rw[ow] = l8;
                sw[ow] = 0;
                for (int j24 = 0; j24 < uw; j24++) {
                  if (ww[j24] != j15 || xw[j24] != i20)
                    continue;
                  sw[ow] = Config.hlb[yw[j24]];
                  break;
                }

                ow++;
              } else {
                l8 &= 0x7fff;
                int k24 = 0;
                for (int i28 = 0; i28 < ow; i28++)
                  if (pw[i28] != j15 || qw[i28] != i20 || rw[i28] != l8) {
                    if (i28 != k24) {
                      pw[k24] = pw[i28];
                      qw[k24] = qw[i28];
                      rw[k24] = rw[i28];
                      sw[k24] = sw[i28];
                    }
                    k24++;
                  } else {
                    l8 = -123;
                  }

                ow = k24;
              }
            }

          return;
        }
        if (arg0 == 253) {
          for (int k1 = 1; k1 < arg1;)
            if (Util.unsign(arg2[k1]) == 255) {
              int i9 = 0;
              int k15 = cw + arg2[k1 + 1] >> 3;
              int j20 = dw + arg2[k1 + 2] >> 3;
              k1 += 3;
              for (int l24 = 0; l24 < uw; l24++) {
                int j28 = (ww[l24] >> 3) - k15;
                int i31 = (xw[l24] >> 3) - j20;
                if (j28 != 0 || i31 != 0) {
                  if (l24 != i9) {
                    vw[i9] = vw[l24];
                    vw[i9].uh = i9;
                    ww[i9] = ww[l24];
                    xw[i9] = xw[l24];
                    yw[i9] = yw[l24];
                    zw[i9] = zw[l24];
                  }
                  i9++;
                } else {
                  zt.zh(vw[l24]);
                  wu.fp(ww[l24], xw[l24], yw[l24]);
                }
              }

              uw = i9;
            } else {
              int j9 = Util.readInt8(arg2, k1);
              k1 += 2;
              int l15 = cw + arg2[k1++];
              int k20 = dw + arg2[k1++];
              int i25 = 0;
              for (int k28 = 0; k28 < uw; k28++)
                if (ww[k28] != l15 || xw[k28] != k20) {
                  if (k28 != i25) {
                    vw[i25] = vw[k28];
                    vw[i25].uh = i25;
                    ww[i25] = ww[k28];
                    xw[i25] = xw[k28];
                    yw[i25] = yw[k28];
                    zw[i25] = zw[k28];
                  }
                  i25++;
                } else {
                  zt.zh(vw[k28]);
                  wu.fp(ww[k28], xw[k28], yw[k28]);
                }

              uw = i25;
              if (j9 != 60000) {
                int j31 = wu.io(l15, k20);
                int k35;
                int i39;
                if (j31 == 0 || j31 == 4) {
                  k35 = Config.elb[j9];
                  i39 = Config.flb[j9];
                } else {
                  i39 = Config.elb[j9];
                  k35 = Config.flb[j9];
                }
                int i42 = ((l15 + l15 + k35) * cu) / 2;
                int i44 = ((k20 + k20 + i39) * cu) / 2;
                int k45 = Config.dlb[j9];
                Model h2 = loadedModels[k45].qe();
                zt.uh(h2);
                h2.uh = uw;
                h2.ve(0, j31 * 32, 0);
                h2.zd(i42, -wu.oo(i42, i44), i44);
                h2.se(true, 48, 48, -50, -10, -50);
                wu.vo(l15, k20, j9);
                if (j9 == 74)
                  h2.zd(0, -480, 0);
                ww[uw] = l15;
                xw[uw] = k20;
                yw[uw] = j9;
                zw[uw] = j31;
                vw[uw++] = h2;
              }
            }

          return;
        }
        if (arg0 == 252) {
          int l1 = 1;
          mx = arg2[l1++] & 0xff;
          for (int k9 = 0; k9 < mx; k9++) {
            int i16 = Util.readInt8(arg2, l1);
            l1 += 2;
            nx[k9] = i16 & 0x7fff;
            px[k9] = i16 / 32768;
            if (Config.pjb[i16 & 0x7fff] == 0) {
              ox[k9] = Util.en(arg2, l1);
              if (ox[k9] >= 128)
                l1 += 4;
              else
                l1++;
            } else {
              ox[k9] = 1;
            }
          }

          return;
        }
        if (arg0 == 250) {
          int i2 = Util.readInt8(arg2, 1);
          int l9 = 3;
          for (int j16 = 0; j16 < i2; j16++) {
            int l20 = Util.readInt8(arg2, l9);
            l9 += 2;
            Mob l25 = yv[l20];
            byte byte6 = arg2[l9];
            l9++;
            if (byte6 == 0) {
              int k31 = Util.readInt8(arg2, l9);
              l9 += 2;
              if (l25 != null) {
                l25.ur = 150;
                l25.tr = k31;
              }
            } else if (byte6 == 1) {
              byte byte7 = arg2[l9];
              l9++;
              if (l25 != null) {
                String s3 = Util.nn(arg2, l9, byte7, true);
                boolean flag3 = false;
                for (int j42 = 0; j42 < super.jd; j42++)
                  if (super.kd[j42] == l25.cr)
                    flag3 = true;

                if (!flag3) {
                  l25.sr = 150;
                  l25.rr = s3;
                  appendMessage(l25.dr + ": " + l25.rr, 2);
                }
              }
              l9 += byte7;
            } else if (byte6 == 2) {
              int l31 = Util.unsign(arg2[l9]);
              l9++;
              int l35 = Util.unsign(arg2[l9]);
              l9++;
              int j39 = Util.unsign(arg2[l9]);
              l9++;
              if (l25 != null) {
                l25.vr = l31;
                l25.wr = l35;
                l25.xr = j39;
                l25.yr = 200;
                if (l25 == bw) {
                  ux[3] = l35;
                  vx[3] = j39;
                  wcb = false;
                  cdb = false;
                }
              }
            } else if (byte6 == 3) {
              int i32 = Util.readInt8(arg2, l9);
              l9 += 2;
              int i36 = Util.readInt8(arg2, l9);
              l9 += 2;
              if (l25 != null) {
                l25.es = i32;
                l25.gs = i36;
                l25.fs = -1;
                l25.hs = nu;
              }
            } else if (byte6 == 4) {
              int j32 = Util.readInt8(arg2, l9);
              l9 += 2;
              int j36 = Util.readInt8(arg2, l9);
              l9 += 2;
              if (l25 != null) {
                l25.es = j32;
                l25.fs = j36;
                l25.gs = -1;
                l25.hs = nu;
              }
            } else if (byte6 == 5) {
              if (l25 != null) {
                l25.fr = Util.readInt8(arg2, l9);
                l9 += 2;
                l25.cr = Util.on(arg2, l9);
                l9 += 8;
                l25.dr = Util.decode37(l25.cr);
                int k32 = Util.unsign(arg2[l9]);
                l9++;
                for (int k36 = 0; k36 < k32; k36++) {
                  l25.qr[k36] = Util.unsign(arg2[l9]);
                  l9++;
                }

                for (int k39 = k32; k39 < 12; k39++)
                  l25.qr[k39] = 0;

                l25.as = arg2[l9++] & 0xff;
                l25.bs = arg2[l9++] & 0xff;
                l25.cs = arg2[l9++] & 0xff;
                l25.ds = arg2[l9++] & 0xff;
                l25.zr = arg2[l9++] & 0xff;
                l25.ks = arg2[l9++] & 0xff;
              } else {
                l9 += 14;
                int l32 = Util.unsign(arg2[l9]);
                l9 += l32 + 1;
              }
            } else if (byte6 == 6) {
              byte byte8 = arg2[l9];
              l9++;
              if (l25 != null) {
                String s4 = Util.nn(arg2, l9, byte8, false);
                l25.sr = 150;
                l25.rr = s4;
                if (l25 == bw)
                  appendMessage(l25.dr + ": " + l25.rr, 5);
              }
              l9 += byte8;
            }
          }

          return;
        }
        if (arg0 == 249) {
          for (int j2 = 1; j2 < arg1;)
            if (Util.unsign(arg2[j2]) == 255) {
              int i10 = 0;
              int k16 = cw + arg2[j2 + 1] >> 3;
              int i21 = dw + arg2[j2 + 2] >> 3;
              j2 += 3;
              for (int j25 = 0; j25 < dx; j25++) {
                int l28 = (fx[j25] >> 3) - k16;
                int i33 = (gx[j25] >> 3) - i21;
                if (l28 != 0 || i33 != 0) {
                  if (j25 != i10) {
                    ex[i10] = ex[j25];
                    ex[i10].uh = i10 + 10000;
                    fx[i10] = fx[j25];
                    gx[i10] = gx[j25];
                    hx[i10] = hx[j25];
                    ix[i10] = ix[j25];
                  }
                  i10++;
                } else {
                  zt.zh(ex[j25]);
                  wu.fo(fx[j25], gx[j25], hx[j25], ix[j25]);
                }
              }

              dx = i10;
            } else {
              int j10 = Util.readInt8(arg2, j2);
              j2 += 2;
              int l16 = cw + arg2[j2++];
              int j21 = dw + arg2[j2++];
              byte byte5 = arg2[j2++];
              int i29 = 0;
              for (int j33 = 0; j33 < dx; j33++)
                if (fx[j33] != l16 || gx[j33] != j21 || hx[j33] != byte5) {
                  if (j33 != i29) {
                    ex[i29] = ex[j33];
                    ex[i29].uh = i29 + 10000;
                    fx[i29] = fx[j33];
                    gx[i29] = gx[j33];
                    hx[i29] = hx[j33];
                    ix[i29] = ix[j33];
                  }
                  i29++;
                } else {
                  zt.zh(ex[j33]);
                  wu.fo(fx[j33], gx[j33], hx[j33], ix[j33]);
                }

              dx = i29;
              if (j10 != 65535) {
                wu.hp(l16, j21, byte5, j10);
                Model h1 = cm(l16, j21, byte5, j10, dx);
                ex[dx] = h1;
                fx[dx] = l16;
                gx[dx] = j21;
                ix[dx] = j10;
                hx[dx++] = byte5;
              }
            }

          return;
        }
        if (arg0 == 248) {
          iw = hw;
          hw = 0;
          for (int k2 = 0; k2 < iw; k2++)
            lw[k2] = kw[k2];

          int k10 = 8;
          int i17 = Util.readBits(arg2, k10, 8);
          k10 += 8;
          for (int k21 = 0; k21 < i17; k21++) {
            Mob l26 = lw[k21];
            int j29 = Util.readBits(arg2, k10, 1);
            k10++;
            if (j29 != 0) {
              int k33 = Util.readBits(arg2, k10, 1);
              k10++;
              if (k33 == 0) {
                int l36 = Util.readBits(arg2, k10, 3);
                k10 += 3;
                int l39 = l26.currentWaypoint;
                int k42 = l26.waypointsX[l39];
                int j44 = l26.waypointsY[l39];
                if (l36 == 2 || l36 == 1 || l36 == 3)
                  k42 += cu;
                if (l36 == 6 || l36 == 5 || l36 == 7)
                  k42 -= cu;
                if (l36 == 4 || l36 == 3 || l36 == 5)
                  j44 += cu;
                if (l36 == 0 || l36 == 1 || l36 == 7)
                  j44 -= cu;
                l26.nextDirection = l36;
                l26.currentWaypoint = l39 = (l39 + 1) % 10;
                l26.waypointsX[l39] = k42;
                l26.waypointsY[l39] = j44;
              } else {
                int i37 = Util.readBits(arg2, k10, 4);
                if ((i37 & 0xc) == 12) {
                  k10 += 2;
                  continue;
                }
                l26.nextDirection = Util.readBits(arg2, k10, 4);
                k10 += 4;
              }
            }
            kw[hw++] = l26;
          }

          while (k10 + 31 < arg1 * 8) {
            int k25 = Util.readBits(arg2, k10, 11);
            k10 += 11;
            int k29 = Util.readBits(arg2, k10, 5);
            k10 += 5;
            if (k29 > 15)
              k29 -= 32;
            int l33 = Util.readBits(arg2, k10, 5);
            k10 += 5;
            if (l33 > 15)
              l33 -= 32;
            int j37 = Util.readBits(arg2, k10, 4);
            k10 += 4;
            int i40 = (cw + k29) * cu + 64;
            int l42 = (dw + l33) * cu + 64;
            int k44 = Util.readBits(arg2, k10, 9);
            k10 += 9;
            if (k44 >= Config.vjb)
              k44 = 24;
            om(k25, i40, l42, j37, k44);
          }
          return;
        }
        if (arg0 == 247) {
          int l2 = Util.readInt8(arg2, 1);
          int l10 = 3;
          for (int j17 = 0; j17 < l2; j17++) {
            int l21 = Util.readInt8(arg2, l10);
            l10 += 2;
            Mob l27 = jw[l21];
            int l29 = Util.unsign(arg2[l10]);
            l10++;
            if (l29 == 1) {
              int i34 = Util.readInt8(arg2, l10);
              l10 += 2;
              byte byte9 = arg2[l10];
              l10++;
              if (l27 != null) {
                String s5 = Util.nn(arg2, l10, byte9, false);
                l27.sr = 150;
                l27.rr = s5;
                if (i34 == bw.er)
                  appendMessage("@yel@" + Config.wjb[l27.ir] + ": " + l27.rr, 5);
              }
              l10 += byte9;
            } else if (l29 == 2) {
              int j34 = Util.unsign(arg2[l10]);
              l10++;
              int k37 = Util.unsign(arg2[l10]);
              l10++;
              int j40 = Util.unsign(arg2[l10]);
              l10++;
              if (l27 != null) {
                l27.vr = j34;
                l27.wr = k37;
                l27.xr = j40;
                l27.yr = 200;
              }
            }
          }

          return;
        }
        if (arg0 == 246) {
          ncb = true;
          int i3 = Util.unsign(arg2[1]);
          ocb = i3;
          int i11 = 2;
          for (int k17 = 0; k17 < i3; k17++) {
            int i22 = Util.unsign(arg2[i11]);
            i11++;
            pcb[k17] = new String(arg2, i11, i22);
            i11 += i22;
          }

          return;
        }
        if (arg0 == 245) {
          ncb = false;
          return;
        }
        if (arg0 == 244) {
          idb = true;
          ew = Util.readInt8(arg2, 1);
          xu = Util.readInt8(arg2, 3);
          yu = Util.readInt8(arg2, 5);
          dv = Util.readInt8(arg2, 7);
          zu = Util.readInt8(arg2, 9);
          yu -= dv * zu;
          return;
        }
        if (arg0 == 243) {
          int j3 = 1;
          for (int j11 = 0; j11 < 18; j11++)
            ux[j11] = Util.unsign(arg2[j3++]);

          for (int l17 = 0; l17 < 18; l17++)
            vx[l17] = Util.unsign(arg2[j3++]);

          for (int j22 = 0; j22 < 18; j22++) {
            wx[j22] = Util.readInt32(arg2, j3);
            j3 += 4;
          }

          yx = Util.unsign(arg2[j3++]);
          return;
        }
        if (arg0 == 242) {
          for (int k3 = 0; k3 < 5; k3++)
            xx[k3] = Util.unsign(arg2[1 + k3]);

          return;
        }
        if (arg0 == 241) {
          gdb = 250;
          return;
        }
        if (arg0 == 240) {
          int l3 = (arg1 - 1) / 4;
          for (int k11 = 0; k11 < l3; k11++) {
            int i18 = cw + Util.zn(arg2, 1 + k11 * 4) >> 3;
            int k22 = dw + Util.zn(arg2, 3 + k11 * 4) >> 3;
            int i26 = 0;
            for (int i30 = 0; i30 < ow; i30++) {
              int k34 = (pw[i30] >> 3) - i18;
              int l37 = (qw[i30] >> 3) - k22;
              if (k34 != 0 || l37 != 0) {
                if (i30 != i26) {
                  pw[i26] = pw[i30];
                  qw[i26] = qw[i30];
                  rw[i26] = rw[i30];
                  sw[i26] = sw[i30];
                }
                i26++;
              }
            }

            ow = i26;
            i26 = 0;
            for (int l34 = 0; l34 < uw; l34++) {
              int i38 = (ww[l34] >> 3) - i18;
              int k40 = (xw[l34] >> 3) - k22;
              if (i38 != 0 || k40 != 0) {
                if (l34 != i26) {
                  vw[i26] = vw[l34];
                  vw[i26].uh = i26;
                  ww[i26] = ww[l34];
                  xw[i26] = xw[l34];
                  yw[i26] = yw[l34];
                  zw[i26] = zw[l34];
                }
                i26++;
              } else {
                zt.zh(vw[l34]);
                wu.fp(ww[l34], xw[l34], yw[l34]);
              }
            }

            uw = i26;
            i26 = 0;
            for (int j38 = 0; j38 < dx; j38++) {
              int l40 = (fx[j38] >> 3) - i18;
              int i43 = (gx[j38] >> 3) - k22;
              if (l40 != 0 || i43 != 0) {
                if (j38 != i26) {
                  ex[i26] = ex[j38];
                  ex[i26].uh = i26 + 10000;
                  fx[i26] = fx[j38];
                  gx[i26] = gx[j38];
                  hx[i26] = hx[j38];
                  ix[i26] = ix[j38];
                }
                i26++;
              } else {
                zt.zh(ex[j38]);
                wu.fo(fx[j38], gx[j38], hx[j38], ix[j38]);
              }
            }

            dx = i26;
          }

          return;
        }
        if (arg0 == 239) {
          qgb = true;
          rgb = false;
          processCharacterDesign(false);
          return;
        }
        if (arg0 == 238) {
          int i4 = Util.readInt8(arg2, 1);
          if (yv[i4] != null)
            abb = yv[i4].dr;
          zab = true;
          hbb = false;
          ibb = false;
          bbb = 0;
          ebb = 0;
          return;
        }
        if (arg0 == 237) {
          zab = false;
          mbb = false;
          return;
        }
        if (arg0 == 236) {
          ebb = arg2[1] & 0xff;
          int j4 = 2;
          for (int l11 = 0; l11 < ebb; l11++) {
            fbb[l11] = Util.readInt8(arg2, j4);
            j4 += 2;
            gbb[l11] = Util.readInt32(arg2, j4);
            j4 += 4;
          }

          hbb = false;
          ibb = false;
          return;
        }
        if (arg0 == 235) {
          byte byte0 = arg2[1];
          if (byte0 == 1) {
            hbb = true;
            return;
          } else {
            hbb = false;
            return;
          }
        }
        if (arg0 == 234) {
          ubb = true;
          int k4 = 1;
          int i12 = arg2[k4++] & 0xff;
          byte byte4 = arg2[k4++];
          vbb = arg2[k4++] & 0xff;
          wbb = arg2[k4++] & 0xff;
          for (int l22 = 0; l22 < 40; l22++)
            xbb[l22] = -1;

          for (int j26 = 0; j26 < i12; j26++) {
            xbb[j26] = Util.readInt8(arg2, k4);
            k4 += 2;
            ybb[j26] = Util.readInt8(arg2, k4);
            k4 += 2;
            zbb[j26] = arg2[k4++];
          }

          if (byte4 == 1) {
            int j30 = 39;
            for (int i35 = 0; i35 < mx; i35++) {
              if (j30 < i12)
                break;
              boolean flag2 = false;
              for (int i41 = 0; i41 < 40; i41++) {
                if (xbb[i41] != nx[i35])
                  continue;
                flag2 = true;
                break;
              }

              if (nx[i35] == 10)
                flag2 = true;
              if (!flag2) {
                xbb[j30] = nx[i35] & 0x7fff;
                ybb[j30] = 0;
                zbb[j30] = 0;
                j30--;
              }
            }

          }
          if (acb >= 0 && acb < 40 && xbb[acb] != bcb) {
            acb = -1;
            bcb = -2;
            return;
          }
        } else {
          if (arg0 == 233) {
            ubb = false;
            return;
          }
          if (arg0 == 229) {
            byte byte1 = arg2[1];
            if (byte1 == 1) {
              ibb = true;
              return;
            } else {
              ibb = false;
              return;
            }
          }
          if (arg0 == 228) {
            sy = Util.unsign(arg2[1]) == 1;
            ty = Util.unsign(arg2[2]) == 1;
            uy = Util.unsign(arg2[3]) == 1;
            return;
          }
          if (arg0 == 227) {
            for (int l4 = 0; l4 < arg1 - 1; l4++) {
              boolean flag = arg2[l4 + 1] == 1;
              if (!qy[l4] && flag)
                nk("prayeron");
              if (qy[l4] && !flag)
                nk("prayeroff");
              qy[l4] = flag;
            }

            return;
          }
          if (arg0 == 226) {
            for (int i5 = 0; i5 < ny; i5++)
              py[i5] = arg2[i5 + 1] == 1;

            return;
          }
          if (arg0 == 224) {
            zeb = true;
            for (int j5 = 0; j5 < 5; j5++) {
              ifb[j5] = j5;
              jfb[j5] = "~:" + ifb[j5];
              afb.kd(ffb[j5], "");
              afb.kd(efb[j5], (j5 + 1) + ": " + questions[ifb[j5]]);
            }

            return;
          }
          if (arg0 == 222) {
            ccb = true;
            int k5 = 1;
            dcb = arg2[k5++] & 0xff;
            lcb = arg2[k5++] & 0xff;
            for (int j12 = 0; j12 < dcb; j12++) {
              ecb[j12] = Util.readInt8(arg2, k5);
              k5 += 2;
              fcb[j12] = Util.en(arg2, k5);
              if (fcb[j12] >= 128)
                k5 += 4;
              else
                k5++;
            }

            ol();
            return;
          }
          if (arg0 == 221) {
            ccb = false;
            return;
          }
          if (arg0 == 220) {
            int l5 = arg2[1] & 0xff;
            wx[l5] = Util.readInt32(arg2, 2);
            return;
          }
          if (arg0 == 219) {
            int i6 = Util.readInt8(arg2, 1);
            if (yv[i6] != null)
              zz = yv[i6].dr;
            yz = true;
            aab = 0;
            dab = 0;
            gab = false;
            hab = false;
            iab = false;
            jab = false;
            kab = false;
            lab = false;
            return;
          }
          if (arg0 == 218) {
            yz = false;
            mab = false;
            return;
          }
          if (arg0 == 217) {
            mbb = true;
            nbb = false;
            zab = false;
            int j6 = 1;
            lbb = Util.on(arg2, j6);
            j6 += 8;
            rbb = arg2[j6++] & 0xff;
            for (int k12 = 0; k12 < rbb; k12++) {
              sbb[k12] = Util.readInt8(arg2, j6);
              j6 += 2;
              tbb[k12] = Util.readInt32(arg2, j6);
              j6 += 4;
            }

            obb = arg2[j6++] & 0xff;
            for (int j18 = 0; j18 < obb; j18++) {
              pbb[j18] = Util.readInt8(arg2, j6);
              j6 += 2;
              qbb[j18] = Util.readInt32(arg2, j6);
              j6 += 4;
            }

            return;
          }
          if (arg0 == 216) {
            dab = arg2[1] & 0xff;
            int k6 = 2;
            for (int l12 = 0; l12 < dab; l12++) {
              eab[l12] = Util.readInt8(arg2, k6);
              k6 += 2;
              fab[l12] = Util.readInt32(arg2, k6);
              k6 += 4;
            }

            gab = false;
            hab = false;
            return;
          }
          if (arg0 == 215) {
            if (arg2[1] == 1)
              iab = true;
            else
              iab = false;
            if (arg2[2] == 1)
              jab = true;
            else
              jab = false;
            if (arg2[3] == 1)
              kab = true;
            else
              kab = false;
            if (arg2[4] == 1)
              lab = true;
            else
              lab = false;
            gab = false;
            hab = false;
            return;
          }
          if (arg0 == 214) {
            int l6 = 1;
            int i13 = arg2[l6++] & 0xff;
            int k18 = Util.readInt8(arg2, l6);
            l6 += 2;
            int i23 = Util.en(arg2, l6);
            if (i23 >= 128)
              l6 += 4;
            else
              l6++;
            if (i23 == 0) {
              dcb--;
              for (int k26 = i13; k26 < dcb; k26++) {
                ecb[k26] = ecb[k26 + 1];
                fcb[k26] = fcb[k26 + 1];
              }

            } else {
              ecb[i13] = k18;
              fcb[i13] = i23;
              if (i13 >= dcb)
                dcb = i13 + 1;
            }
            ol();
            return;
          }
          if (arg0 == 213) {
            int i7 = 1;
            int j13 = 1;
            int l18 = arg2[i7++] & 0xff;
            int j23 = Util.readInt8(arg2, i7);
            i7 += 2;
            if (Config.pjb[j23 & 0x7fff] == 0) {
              j13 = Util.en(arg2, i7);
              if (j13 >= 128)
                i7 += 4;
              else
                i7++;
            }
            nx[l18] = j23 & 0x7fff;
            px[l18] = j23 / 32768;
            ox[l18] = j13;
            if (l18 >= mx) {
              mx = l18 + 1;
              return;
            }
          } else {
            if (arg0 == 212) {
              int j7 = arg2[1] & 0xff;
              mx--;
              for (int k13 = j7; k13 < mx; k13++) {
                nx[k13] = nx[k13 + 1];
                ox[k13] = ox[k13 + 1];
                px[k13] = px[k13 + 1];
              }

              return;
            }
            if (arg0 == 211) {
              int k7 = 1;
              int l13 = arg2[k7++] & 0xff;
              ux[l13] = Util.unsign(arg2[k7++]);
              vx[l13] = Util.unsign(arg2[k7++]);
              wx[l13] = Util.readInt32(arg2, k7);
              k7 += 4;
              return;
            }
            if (arg0 == 210) {
              byte byte2 = arg2[1];
              if (byte2 == 1) {
                gab = true;
                return;
              } else {
                gab = false;
                return;
              }
            }
            if (arg0 == 209) {
              byte byte3 = arg2[1];
              if (byte3 == 1) {
                hab = true;
                return;
              } else {
                hab = false;
                return;
              }
            }
            if (arg0 == 208) {
              mab = true;
              nab = false;
              yz = false;
              int l7 = 1;
              oab = Util.on(arg2, l7);
              l7 += 8;
              sab = arg2[l7++] & 0xff;
              for (int i14 = 0; i14 < sab; i14++) {
                tab[i14] = Util.readInt8(arg2, l7);
                l7 += 2;
                uab[i14] = Util.readInt32(arg2, l7);
                l7 += 4;
              }

              pab = arg2[l7++] & 0xff;
              for (int i19 = 0; i19 < pab; i19++) {
                qab[i19] = Util.readInt8(arg2, l7);
                l7 += 2;
                rab[i19] = Util.readInt32(arg2, l7);
                l7 += 4;
              }

              vab = arg2[l7++] & 0xff;
              wab = arg2[l7++] & 0xff;
              xab = arg2[l7++] & 0xff;
              yab = arg2[l7++] & 0xff;
              return;
            }
            if (arg0 == 207) {
              String s1 = new String(arg2, 1, arg1 - 1);
              nk(s1);
              return;
            }
            if (arg0 == 206) {
              if (ihb < 50) {
                int i8 = arg2[1] & 0xff;
                int j14 = arg2[2] + cw;
                int j19 = arg2[3] + dw;
                mhb[ihb] = i8;
                lhb[ihb] = 0;
                jhb[ihb] = j14;
                khb[ihb] = j19;
                ihb++;
                return;
              }
            } else if (arg0 == 205) {
              if (!vcb) {
                daysSinceLogin = Util.readInt32(arg2, 1);
                adb = Util.readInt32(arg2, 5);
                playerIP = Util.readInt32(arg2, 9);
                bdb = (int) (Math.random() * 6D);
                wcb = true;
                vcb = true;
                lastIP = null;
                return;
              }
            } else {
              if (arg0 == 204) {
                ddb = new String(arg2, 1, arg1 - 1);
                cdb = true;
                return;
              }
              if (arg0 == 203) {
                qgb = true;
                rgb = true;
                processCharacterDesign(true);
              }
            }
          }
        }
      }
      return;
    } catch (RuntimeException runtimeexception) {
      if (gt < 3) {
        super.stream.beginFrame(17);
        super.stream.putLine(runtimeexception.toString());
        super.stream.endFrame();
        super.stream.beginFrame(17);
        super.stream.putLine("com.runescape.p-type:" + arg0
            + " com.runescape.p-size:" + arg1);
        super.stream.endFrame();
        super.stream.beginFrame(17);
        super.stream.putLine("rx:" + cw + " ry:" + dw + " num3l:" + uw);
        super.stream.endFrame();
        String s2 = "";
        for (int k19 = 0; k19 < 80 && k19 < arg1; k19++)
          s2 = s2 + arg2[k19] + " ";

        super.stream.beginFrame(17);
        super.stream.putLine(s2);
        super.stream.endFrame();
        gt++;
      }
    }
  }

  public void ol() {
    gcb = dcb;
    for (int i1 = 0; i1 < dcb; i1++) {
      hcb[i1] = ecb[i1];
      icb[i1] = fcb[i1];
    }

    for (int j1 = 0; j1 < mx; j1++) {
      if (gcb >= lcb)
        break;
      int k1 = nx[j1];
      boolean flag = false;
      for (int l1 = 0; l1 < gcb; l1++) {
        if (hcb[l1] != k1)
          continue;
        flag = true;
        break;
      }

      if (!flag) {
        hcb[gcb] = k1;
        icb[gcb] = 0;
        gcb++;
      }
    }

  }

  public boolean dm(int arg0) {
    int i1 = bw.gr / 128;
    int j1 = bw.hr / 128;
    for (int k1 = 2; k1 >= 1; k1--) {
      if (arg0 == 1
          && ((wu.ajb[i1][j1 - k1] & 0x80) == 128
              || (wu.ajb[i1 - k1][j1] & 0x80) == 128 || (wu.ajb[i1 - k1][j1
              - k1] & 0x80) == 128))
        return false;
      if (arg0 == 3
          && ((wu.ajb[i1][j1 + k1] & 0x80) == 128
              || (wu.ajb[i1 - k1][j1] & 0x80) == 128 || (wu.ajb[i1 - k1][j1
              + k1] & 0x80) == 128))
        return false;
      if (arg0 == 5
          && ((wu.ajb[i1][j1 + k1] & 0x80) == 128
              || (wu.ajb[i1 + k1][j1] & 0x80) == 128 || (wu.ajb[i1 + k1][j1
              + k1] & 0x80) == 128))
        return false;
      if (arg0 == 7
          && ((wu.ajb[i1][j1 - k1] & 0x80) == 128
              || (wu.ajb[i1 + k1][j1] & 0x80) == 128 || (wu.ajb[i1 + k1][j1
              - k1] & 0x80) == 128))
        return false;
      if (arg0 == 0 && (wu.ajb[i1][j1 - k1] & 0x80) == 128)
        return false;
      if (arg0 == 2 && (wu.ajb[i1 - k1][j1] & 0x80) == 128)
        return false;
      if (arg0 == 4 && (wu.ajb[i1][j1 + k1] & 0x80) == 128)
        return false;
      if (arg0 == 6 && (wu.ajb[i1 + k1][j1] & 0x80) == 128)
        return false;
    }

    return true;
  }

  public void ll() {
    if ((pv & 1) == 1 && dm(pv))
      return;
    if ((pv & 1) == 0 && dm(pv)) {
      if (dm(pv + 1 & 7)) {
        pv = pv + 1 & 7;
        return;
      }
      if (dm(pv + 7 & 7))
        pv = pv + 7 & 7;
      return;
    }
    int ai[] = { 1, -1, 2, -2, 3, -3, 4 };
    for (int i1 = 0; i1 < 7; i1++) {
      if (!dm(pv + ai[i1] + 8 & 7))
        continue;
      pv = pv + ai[i1] + 8 & 7;
      break;
    }

    if ((pv & 1) == 0 && dm(pv)) {
      if (dm(pv + 1 & 7)) {
        pv = pv + 1 & 7;
        return;
      }
      if (dm(pv + 7 & 7))
        pv = pv + 7 & 7;
      return;
    } else {
      return;
    }
  }

  public void yk() {
    if (gdb != 0) {
      graphics.ff();
      graphics.ug("Oh dear! You are dead...", eu / 2, fu / 2, 7, 0xff0000);
      al();
      graphics.jf(yt, 0, 0);
      return;
    }
    if (qgb) {
      jk();
      return;
    }
    if (zeb) {
      fk();
      return;
    }
    if (!wu.cjb)
      return;
    for (int i1 = 0; i1 < 64; i1++) {
      zt.zh(wu.fjb[av][i1]);
      if (av == 0) {
        zt.zh(wu.ejb[1][i1]);
        zt.zh(wu.fjb[1][i1]);
        zt.zh(wu.ejb[2][i1]);
        zt.zh(wu.fjb[2][i1]);
      }
      mv = true;
      if (av == 0 && (wu.ajb[bw.gr / 128][bw.hr / 128] & 0x80) == 0) {
        zt.uh(wu.fjb[av][i1]);
        if (av == 0) {
          zt.uh(wu.ejb[1][i1]);
          zt.uh(wu.fjb[1][i1]);
          zt.uh(wu.ejb[2][i1]);
          zt.uh(wu.fjb[2][i1]);
        }
        mv = false;
      }
    }

    if (pu != ru) {
      ru = pu;
      for (int j1 = 0; j1 < uw; j1++) {
        if (yw[j1] == 51) {
          int i2 = ww[j1];
          int j3 = xw[j1];
          int j5 = i2 - bw.gr / 128;
          int i7 = j3 - bw.hr / 128;
          byte byte0 = 7;
          if (i2 >= 0 && j3 >= 0 && i2 < 96 && j3 < 96 && j5 > -byte0
              && j5 < byte0 && i7 > -byte0 && i7 < byte0) {
            zt.zh(vw[j1]);
            String s1 = "torcha" + (pu + 1);
            int i14 = Config.matchModel(s1);
            Model h1 = loadedModels[i14].qe();
            zt.uh(h1);
            h1.se(true, 48, 48, -50, -10, -50);
            h1.yd(vw[j1]);
            h1.uh = j1;
            vw[j1] = h1;
          }
        }
        if (yw[j1] == 143) {
          int j2 = ww[j1];
          int k3 = xw[j1];
          int k5 = j2 - bw.gr / 128;
          int j7 = k3 - bw.hr / 128;
          byte byte1 = 7;
          if (j2 >= 0 && k3 >= 0 && j2 < 96 && k3 < 96 && k5 > -byte1
              && k5 < byte1 && j7 > -byte1 && j7 < byte1) {
            zt.zh(vw[j1]);
            String s2 = "skulltorcha" + (pu + 1);
            int j14 = Config.matchModel(s2);
            Model h2 = loadedModels[j14].qe();
            zt.uh(h2);
            h2.se(true, 48, 48, -50, -10, -50);
            h2.yd(vw[j1]);
            h2.uh = j1;
            vw[j1] = h2;
          }
        }
      }

    }
    if (qu != su) {
      su = qu;
      for (int k1 = 0; k1 < uw; k1++) {
        if (yw[k1] == 97) {
          int k2 = ww[k1];
          int i4 = xw[k1];
          int l5 = k2 - bw.gr / 128;
          int k7 = i4 - bw.hr / 128;
          byte byte2 = 9;
          if (k2 >= 0 && i4 >= 0 && k2 < 96 && i4 < 96 && l5 > -byte2
              && l5 < byte2 && k7 > -byte2 && k7 < byte2) {
            zt.zh(vw[k1]);
            String s3 = "firea" + (qu + 1);
            int k14 = Config.matchModel(s3);
            Model h3 = loadedModels[k14].qe();
            zt.uh(h3);
            h3.se(true, 48, 48, -50, -10, -50);
            h3.yd(vw[k1]);
            h3.uh = k1;
            vw[k1] = h3;
          }
        }
        if (yw[k1] == 274) {
          int l2 = ww[k1];
          int j4 = xw[k1];
          int i6 = l2 - bw.gr / 128;
          int i8 = j4 - bw.hr / 128;
          byte byte3 = 9;
          if (l2 >= 0 && j4 >= 0 && l2 < 96 && j4 < 96 && i6 > -byte3
              && i6 < byte3 && i8 > -byte3 && i8 < byte3) {
            zt.zh(vw[k1]);
            String s4 = "fireplacea" + (qu + 1);
            int l14 = Config.matchModel(s4);
            Model h4 = loadedModels[l14].qe();
            zt.uh(h4);
            h4.se(true, 48, 48, -50, -10, -50);
            h4.yd(vw[k1]);
            h4.uh = k1;
            vw[k1] = h4;
          }
        }
      }

    }
    zt.ki(xv);
    xv = 0;
    for (int l1 = 0; l1 < vv; l1++) {
      Mob l3 = zv[l1];
      if (l3.cs != 255) {
        int k4 = l3.gr;
        int j6 = l3.hr;
        int j8 = -wu.oo(k4, j6);
        int k9 = zt.lh(5000 + l1, k4, j8, j6, 145, 220, l1 + 10000);
        xv++;
        if (l3 == bw)
          zt.mh(k9);
        if (l3.kr == 8)
          zt.ni(k9, -30);
        if (l3.kr == 9)
          zt.ni(k9, 30);
      }
    }

    for (int i3 = 0; i3 < vv; i3++) {
      Mob l4 = zv[i3];
      if (l4.hs > 0) {
        Mob l6 = null;
        if (l4.gs != -1)
          l6 = jw[l4.gs];
        else if (l4.fs != -1)
          l6 = yv[l4.fs];
        if (l6 != null) {
          int k8 = l4.gr;
          int l9 = l4.hr;
          int j12 = -wu.oo(k8, l9) - 110;
          int i15 = l6.gr;
          int l15 = l6.hr;
          int i16 = -wu.oo(i15, l15) - Config.kkb[l6.ir] / 2;
          int j16 = (k8 * l4.hs + i15 * (nu - l4.hs)) / nu;
          int k16 = (j12 * l4.hs + i16 * (nu - l4.hs)) / nu;
          int l16 = (l9 * l4.hs + l15 * (nu - l4.hs)) / nu;
          zt.lh(ku + l4.es, j16, k16, l16, 32, 32, 0);
          xv++;
        }
      }
    }

    for (int i5 = 0; i5 < hw; i5++) {
      Mob l7 = kw[i5];
      int l8 = l7.gr;
      int i10 = l7.hr;
      int k12 = -wu.oo(l8, i10);
      int j15 = zt.lh(20000 + i5, l8, k12, i10, Config.jkb[l7.ir],
          Config.kkb[l7.ir], i5 + 30000);
      xv++;
      if (l7.kr == 8)
        zt.ni(j15, -30);
      if (l7.kr == 9)
        zt.ni(j15, 30);
    }

    for (int k6 = 0; k6 < ow; k6++) {
      int i9 = pw[k6] * cu + 64;
      int j10 = qw[k6] * cu + 64;
      zt.lh(40000 + rw[k6], i9, -wu.oo(i9, j10) - sw[k6], j10, 96, 64,
          k6 + 20000);
      xv++;
    }

    for (int j9 = 0; j9 < ihb; j9++) {
      int k10 = jhb[j9] * cu + 64;
      int l12 = khb[j9] * cu + 64;
      int k15 = mhb[j9];
      if (k15 == 0) {
        zt.lh(50000 + j9, k10, -wu.oo(k10, l12), l12, 128, 256, j9 + 50000);
        xv++;
      }
      if (k15 == 1) {
        zt.lh(50000 + j9, k10, -wu.oo(k10, l12), l12, 128, 64, j9 + 50000);
        xv++;
      }
    }

    graphics.wk = false;
    graphics.lf();
    graphics.wk = super.xq;
    if (av == 3) {
      int l10 = 40 + (int) (Math.random() * 3D);
      int i13 = 40 + (int) (Math.random() * 7D);
      zt.yi(l10, i13, -50, -10, -50);
    }
    egb = 0;
    yfb = 0;
    jgb = 0;
    if (ry) {
      if (sy && !mv) {
        int i11 = pv;
        ll();
        if (pv != i11) {
          nv = bw.gr;
          ov = bw.hr;
        }
      }
      zt.im = 3000;
      zt.jm = 3000;
      zt.km = 1;
      zt.lm = 2800;
      rv = pv * 32;
      int j11 = nv + tt;
      int j13 = ov + vt;
      zt.ai(j11, -wu.oo(j11, j13), j13, 912, rv * 4, 0, 2000);
    } else {
      if (sy && !mv)
        ll();
      if (!super.xq) {
        zt.im = 2400;
        zt.jm = 2400;
        zt.km = 1;
        zt.lm = 2300;
      } else {
        zt.im = 2200;
        zt.jm = 2200;
        zt.km = 1;
        zt.lm = 2100;
      }
      int k11 = nv + tt;
      int k13 = ov + vt;
      zt.ai(k11, -wu.oo(k11, k13), k13, 912, rv * 4, 0, lv * 2);
    }
    zt.wi();
    pl();
    if (tu > 0)
      graphics.xg(uu - 8, vu - 8, hu + 14 + (24 - tu) / 6);
    if (tu < 0)
      graphics.xg(uu - 8, vu - 8, hu + 18 + (24 + tu) / 6);
    if (!idb) {
      int l11 = 2203 - (dw + yu + cv);
      if (cw + xu + bv >= 2640)
        l11 = -50;
      if (l11 > 0) {
        int l13 = 1 + l11 / 6;
        graphics.xg(453, fu - 56, hu + 13);
        graphics.ug("Wilderness", 465, fu - 20, 1, 0xffff00);
        graphics.ug("Level: " + l13, 465, fu - 7, 1, 0xffff00);
        if (hdb == 0)
          hdb = 2;
      }
      if (hdb == 0 && l11 > -10 && l11 <= 0)
        hdb = 1;
    }
    if (uz == 0) {
      for (int i12 = 0; i12 < vz; i12++)
        if (xz[i12] > 0) {
          String s5 = wz[i12];
          graphics.drawString(s5, 7, fu - 18 - i12 * 12, 1, 0xffff00);
        }

    }
    pz.qd(qz);
    pz.qd(sz);
    pz.qd(tz);
    if (uz == 1)
      pz.ed(qz);
    else if (uz == 2)
      pz.ed(sz);
    else if (uz == 3)
      pz.ed(tz);
    Menu.ng = 2;
    pz.hc();
    Menu.ng = 0;
    graphics.qg(((Graphics2D) (graphics)).yj - 3 - 197, 3, hu, 128);
    gk();
    graphics.al = false;
    al();
    graphics.jf(yt, 0, 0);
  }

  public void al() {
    graphics.xg(0, fu - 4, hu + 23);
    int i1 = Graphics2D.kg(200, 200, 255);
    if (uz == 0)
      i1 = Graphics2D.kg(255, 200, 50);
    if (lz % 30 > 15)
      i1 = Graphics2D.kg(255, 50, 50);
    graphics.ug("All messages", 54, fu + 6, 0, i1);
    i1 = Graphics2D.kg(200, 200, 255);
    if (uz == 1)
      i1 = Graphics2D.kg(255, 200, 50);
    if (mz % 30 > 15)
      i1 = Graphics2D.kg(255, 50, 50);
    graphics.ug("Chat history", 155, fu + 6, 0, i1);
    i1 = Graphics2D.kg(200, 200, 255);
    if (uz == 2)
      i1 = Graphics2D.kg(255, 200, 50);
    if (nz % 30 > 15)
      i1 = Graphics2D.kg(255, 50, 50);
    graphics.ug("Quest history", 255, fu + 6, 0, i1);
    i1 = Graphics2D.kg(200, 200, 255);
    if (uz == 3)
      i1 = Graphics2D.kg(255, 200, 50);
    if (oz % 30 > 15)
      i1 = Graphics2D.kg(255, 50, 50);
    graphics.ug("Private history", 355, fu + 6, 0, i1);
  }

  public void yl(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5,
      int arg6) {
    int i1 = mhb[arg4];
    int j1 = lhb[arg4];
    if (i1 == 0) {
      int k1 = 255 + j1 * 5 * 256;
      graphics.zf(arg0 + arg2 / 2, arg1 + arg3 / 2, 20 + j1 * 2, k1,
          255 - j1 * 5);
    }
    if (i1 == 1) {
      int l1 = 0xff0000 + j1 * 5 * 256;
      graphics.zf(arg0 + arg2 / 2, arg1 + arg3 / 2, 10 + j1, l1, 255 - j1 * 5);
    }
  }

  public void vm(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5,
      int arg6) {
    int i1 = Config.njb[arg4] + ju;
    int j1 = Config.sjb[arg4];
    graphics.wf(arg0, arg1, arg2, arg3, i1, j1, 0, 0, false);
  }

  public void an(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5,
      int arg6) {
    Mob l1 = kw[arg4];
    int i1 = l1.kr + (rv + 16) / 32 & 7;
    boolean flag = false;
    int j1 = i1;
    if (j1 == 5) {
      j1 = 3;
      flag = true;
    } else if (j1 == 6) {
      j1 = 2;
      flag = true;
    } else if (j1 == 7) {
      j1 = 1;
      flag = true;
    }
    int k1 = j1 * 3 + dhb[(l1.jr / Config.lkb[l1.ir]) % 4];
    if (l1.kr == 8) {
      j1 = 5;
      i1 = 2;
      flag = false;
      arg0 -= (Config.nkb[l1.ir] * arg6) / 100;
      k1 = j1 * 3 + ehb[(kt / (Config.mkb[l1.ir] - 1)) % 8];
    } else if (l1.kr == 9) {
      j1 = 5;
      i1 = 2;
      flag = true;
      arg0 += (Config.nkb[l1.ir] * arg6) / 100;
      k1 = j1 * 3 + fhb[(kt / Config.mkb[l1.ir]) % 8];
    }
    for (int i2 = 0; i2 < 12; i2++) {
      int j2 = pgb[i1][i2];
      int i3 = Config.ekb[l1.ir][j2];
      if (i3 >= 0) {
        int k3 = 0;
        int l3 = 0;
        int i4 = k1;
        if (flag && j1 >= 1 && j1 <= 3 && Config.wkb[i3] == 1)
          i4 += 15;
        if (j1 != 5 || Config.vkb[i3] == 1) {
          int j4 = i4 + Config.xkb[i3];
          k3 = (k3 * arg2) / ((Graphics2D) (graphics)).pk[j4];
          l3 = (l3 * arg3) / ((Graphics2D) (graphics)).qk[j4];
          int k4 = (arg2 * ((Graphics2D) (graphics)).pk[j4])
              / ((Graphics2D) (graphics)).pk[Config.xkb[i3]];
          k3 -= (k4 - arg2) / 2;
          int l4 = Config.tkb[i3];
          int i5 = 0;
          if (l4 == 1) {
            l4 = Config.fkb[l1.ir];
            i5 = Config.ikb[l1.ir];
          } else if (l4 == 2) {
            l4 = Config.gkb[l1.ir];
            i5 = Config.ikb[l1.ir];
          } else if (l4 == 3) {
            l4 = Config.hkb[l1.ir];
            i5 = Config.ikb[l1.ir];
          }
          graphics.wf(arg0 + k3, arg1 + l3, k4, arg3, j4, l4, i5, arg5, flag);
        }
      }
    }

    if (l1.sr > 0) {
      cgb[yfb] = graphics.df(l1.rr, 1) / 2;
      if (cgb[yfb] > 150)
        cgb[yfb] = 150;
      dgb[yfb] = (graphics.df(l1.rr, 1) / 300) * graphics.ng(1);
      agb[yfb] = arg0 + arg2 / 2;
      bgb[yfb] = arg1;
      zfb[yfb++] = l1.rr;
    }
    if (l1.kr == 8 || l1.kr == 9 || l1.yr != 0) {
      if (l1.yr > 0) {
        int k2 = arg0;
        if (l1.kr == 8)
          k2 -= (20 * arg6) / 100;
        else if (l1.kr == 9)
          k2 += (20 * arg6) / 100;
        int j3 = (l1.wr * 30) / l1.xr;
        kgb[jgb] = k2 + arg2 / 2;
        lgb[jgb] = arg1;
        mgb[jgb++] = j3;
      }
      if (l1.yr > 150) {
        int l2 = arg0;
        if (l1.kr == 8)
          l2 -= (10 * arg6) / 100;
        else if (l1.kr == 9)
          l2 += (10 * arg6) / 100;
        graphics.xg((l2 + arg2 / 2) - 12, (arg1 + arg3 / 2) - 12, hu + 12);
        graphics.ug(String.valueOf(l1.vr), (l2 + arg2 / 2) - 1, arg1 + arg3 / 2
            + 5, 3, 0xffffff);
      }
    }
  }

  public void ml(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5,
      int arg6) {
    Mob l1 = zv[arg4];
    if (l1.cs == 255)
      return;
    int i1 = l1.kr + (rv + 16) / 32 & 7;
    boolean flag = false;
    int j1 = i1;
    if (j1 == 5) {
      j1 = 3;
      flag = true;
    } else if (j1 == 6) {
      j1 = 2;
      flag = true;
    } else if (j1 == 7) {
      j1 = 1;
      flag = true;
    }
    int k1 = j1 * 3 + dhb[(l1.jr / 6) % 4];
    if (l1.kr == 8) {
      j1 = 5;
      i1 = 2;
      flag = false;
      arg0 -= (5 * arg6) / 100;
      k1 = j1 * 3 + ehb[(kt / 5) % 8];
    } else if (l1.kr == 9) {
      j1 = 5;
      i1 = 2;
      flag = true;
      arg0 += (5 * arg6) / 100;
      k1 = j1 * 3 + fhb[(kt / 6) % 8];
    }
    for (int i2 = 0; i2 < 12; i2++) {
      int j2 = pgb[i1][i2];
      int j3 = l1.qr[j2] - 1;
      if (j3 >= 0) {
        int i4 = 0;
        int k4 = 0;
        int l4 = k1;
        if (flag && j1 >= 1 && j1 <= 3)
          if (Config.wkb[j3] == 1)
            l4 += 15;
          else if (j2 == 4 && j1 == 1) {
            i4 = -22;
            k4 = -3;
            l4 = j1 * 3 + dhb[(2 + l1.jr / 6) % 4];
          } else if (j2 == 4 && j1 == 2) {
            i4 = 0;
            k4 = -8;
            l4 = j1 * 3 + dhb[(2 + l1.jr / 6) % 4];
          } else if (j2 == 4 && j1 == 3) {
            i4 = 26;
            k4 = -5;
            l4 = j1 * 3 + dhb[(2 + l1.jr / 6) % 4];
          } else if (j2 == 3 && j1 == 1) {
            i4 = 22;
            k4 = 3;
            l4 = j1 * 3 + dhb[(2 + l1.jr / 6) % 4];
          } else if (j2 == 3 && j1 == 2) {
            i4 = 0;
            k4 = 8;
            l4 = j1 * 3 + dhb[(2 + l1.jr / 6) % 4];
          } else if (j2 == 3 && j1 == 3) {
            i4 = -26;
            k4 = 5;
            l4 = j1 * 3 + dhb[(2 + l1.jr / 6) % 4];
          }
        if (j1 != 5 || Config.vkb[j3] == 1) {
          int i5 = l4 + Config.xkb[j3];
          i4 = (i4 * arg2) / ((Graphics2D) (graphics)).pk[i5];
          k4 = (k4 * arg3) / ((Graphics2D) (graphics)).qk[i5];
          int j5 = (arg2 * ((Graphics2D) (graphics)).pk[i5])
              / ((Graphics2D) (graphics)).pk[Config.xkb[j3]];
          i4 -= (j5 - arg2) / 2;
          int k5 = Config.tkb[j3];
          int l5 = chb[l1.ds];
          if (k5 == 1)
            k5 = bhb[l1.as];
          else if (k5 == 2)
            k5 = ahb[l1.bs];
          else if (k5 == 3)
            k5 = ahb[l1.cs];
          graphics.wf(arg0 + i4, arg1 + k4, j5, arg3, i5, k5, l5, arg5, flag);
        }
      }
    }

    if (l1.sr > 0) {
      cgb[yfb] = graphics.df(l1.rr, 1) / 2;
      if (cgb[yfb] > 150)
        cgb[yfb] = 150;
      dgb[yfb] = (graphics.df(l1.rr, 1) / 300) * graphics.ng(1);
      agb[yfb] = arg0 + arg2 / 2;
      bgb[yfb] = arg1;
      zfb[yfb++] = l1.rr;
    }
    if (l1.ur > 0) {
      fgb[egb] = arg0 + arg2 / 2;
      ggb[egb] = arg1;
      hgb[egb] = arg6;
      igb[egb++] = l1.tr;
    }
    if (l1.kr == 8 || l1.kr == 9 || l1.yr != 0) {
      if (l1.yr > 0) {
        int k2 = arg0;
        if (l1.kr == 8)
          k2 -= (20 * arg6) / 100;
        else if (l1.kr == 9)
          k2 += (20 * arg6) / 100;
        int k3 = (l1.wr * 30) / l1.xr;
        kgb[jgb] = k2 + arg2 / 2;
        lgb[jgb] = arg1;
        mgb[jgb++] = k3;
      }
      if (l1.yr > 150) {
        int l2 = arg0;
        if (l1.kr == 8)
          l2 -= (10 * arg6) / 100;
        else if (l1.kr == 9)
          l2 += (10 * arg6) / 100;
        graphics.xg((l2 + arg2 / 2) - 12, (arg1 + arg3 / 2) - 12, hu + 11);
        graphics.ug(String.valueOf(l1.vr), (l2 + arg2 / 2) - 1, arg1 + arg3 / 2
            + 5, 3, 0xffffff);
      }
    }
    if (l1.ks == 1 && l1.ur == 0) {
      int i3 = arg5 + arg0 + arg2 / 2;
      if (l1.kr == 8)
        i3 -= (20 * arg6) / 100;
      else if (l1.kr == 9)
        i3 += (20 * arg6) / 100;
      int l3 = (16 * arg6) / 100;
      int j4 = (16 * arg6) / 100;
      graphics.nf(i3 - l3 / 2, arg1 - j4 / 2 - (10 * arg6) / 100, l3, j4,
          hu + 13);
    }
  }

  public void pl() {
    for (int i1 = 0; i1 < yfb; i1++) {
      int j1 = graphics.ng(1);
      int l1 = agb[i1];
      int k2 = bgb[i1];
      int j3 = cgb[i1];
      int i4 = dgb[i1];
      boolean flag = true;
      while (flag) {
        flag = false;
        for (int i5 = 0; i5 < i1; i5++)
          if (k2 + i4 > bgb[i5] - j1 && k2 - j1 < bgb[i5] + dgb[i5]
              && l1 - j3 < agb[i5] + cgb[i5] && l1 + j3 > agb[i5] - cgb[i5]
              && bgb[i5] - j1 - i4 < k2) {
            k2 = bgb[i5] - j1 - i4;
            flag = true;
          }

      }
      bgb[i1] = k2;
      graphics.ah(zfb[i1], l1, k2, 1, 0xffff00, 300);
    }

    for (int k1 = 0; k1 < egb; k1++) {
      int i2 = fgb[k1];
      int l2 = ggb[k1];
      int k3 = hgb[k1];
      int j4 = igb[k1];
      int l4 = (39 * k3) / 100;
      int j5 = (27 * k3) / 100;
      int k5 = l2 - j5;
      graphics.pg(i2 - l4 / 2, k5, l4, j5, hu + 9, 85);
      int l5 = (36 * k3) / 100;
      int i6 = (24 * k3) / 100;
      graphics.wf(i2 - l5 / 2, (k5 + j5 / 2) - i6 / 2, l5, i6, Config.njb[j4]
          + ju, Config.sjb[j4], 0, 0, false);
    }

    for (int j2 = 0; j2 < jgb; j2++) {
      int i3 = kgb[j2];
      int l3 = lgb[j2];
      int k4 = mgb[j2];
      graphics.uf(i3 - 15, l3 - 3, k4, 5, 65280, 192);
      graphics.uf((i3 - 15) + k4, l3 - 3, 30 - k4, 5, 0xff0000, 192);
    }

  }

  public int gl(int arg0) {
    int i1 = 0;
    for (int j1 = 0; j1 < mx; j1++)
      if (nx[j1] == arg0)
        if (Config.pjb[arg0] == 1)
          i1++;
        else
          i1 += ox[j1];

    return i1;
  }

  public boolean nm(int arg0, int arg1) {
    if (arg0 == 31 && (xm(197) || xm(615) || xm(682)))
      return true;
    if (arg0 == 32 && (xm(102) || xm(616) || xm(683)))
      return true;
    if (arg0 == 33 && (xm(101) || xm(617) || xm(684)))
      return true;
    if (arg0 == 34 && (xm(103) || xm(618) || xm(685)))
      return true;
    return gl(arg0) >= arg1;
  }

  public boolean xm(int arg0) {
    for (int i1 = 0; i1 < mx; i1++)
      if (nx[i1] == arg0 && px[i1] == 1)
        return true;

    return false;
  }

  public void zm(int arg0, int arg1, int arg2) {
    graphics.lg(arg0, arg1, arg2);
    graphics.lg(arg0 - 1, arg1, arg2);
    graphics.lg(arg0 + 1, arg1, arg2);
    graphics.lg(arg0, arg1 - 1, arg2);
    graphics.lg(arg0, arg1 + 1, arg2);
  }

  public void cl(int arg0, int arg1, int arg2, int arg3, boolean arg4) {
    xl(arg0, arg1, arg2, arg3, arg2, arg3, false, arg4);
  }

  public void im(int arg0, int arg1, int arg2, int arg3, boolean arg4) {
    if (xl(arg0, arg1, arg2, arg3, arg2, arg3, false, arg4)) {
      return;
    } else {
      xl(arg0, arg1, arg2, arg3, arg2, arg3, true, arg4);
      return;
    }
  }

  public void kk(int arg0, int arg1, int arg2, int arg3) {
    int i1;
    int j1;
    if (arg2 == 0 || arg2 == 4) {
      i1 = Config.elb[arg3];
      j1 = Config.flb[arg3];
    } else {
      j1 = Config.elb[arg3];
      i1 = Config.flb[arg3];
    }
    if (Config.glb[arg3] == 2 || Config.glb[arg3] == 3) {
      if (arg2 == 0) {
        arg0--;
        i1++;
      }
      if (arg2 == 2)
        j1++;
      if (arg2 == 4)
        i1++;
      if (arg2 == 6) {
        arg1--;
        j1++;
      }
      xl(cw, dw, arg0, arg1, (arg0 + i1) - 1, (arg1 + j1) - 1, false, true);
      return;
    } else {
      xl(cw, dw, arg0, arg1, (arg0 + i1) - 1, (arg1 + j1) - 1, true, true);
      return;
    }
  }

  public void zk(int arg0, int arg1, int arg2) {
    if (arg2 == 0) {
      xl(cw, dw, arg0, arg1 - 1, arg0, arg1, false, true);
      return;
    }
    if (arg2 == 1) {
      xl(cw, dw, arg0 - 1, arg1, arg0, arg1, false, true);
      return;
    } else {
      xl(cw, dw, arg0, arg1, arg0, arg1, true, true);
      return;
    }
  }

  public boolean xl(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5,
      boolean arg6, boolean arg7) {
    int i1 = wu.ho(arg0, arg1, arg2, arg3, arg4, arg5, ot, pt, arg6);
    if (i1 == -1)
      return false;
    i1--;
    arg0 = ot[i1];
    arg1 = pt[i1];
    i1--;
    if (arg7)
      super.stream.beginFrame(215);
    else
      super.stream.beginFrame(255);
    super.stream.putInt16(arg0 + bv);
    super.stream.putInt16(arg1 + cv);
    for (int j1 = i1; j1 >= 0 && j1 > i1 - 25; j1--) {
      super.stream.putInt8(ot[j1] - arg0);
      super.stream.putInt8(pt[j1] - arg1);
    }

    super.stream.endFrame();
    tu = -24;
    uu = super.rq;
    vu = super.sq;
    return true;
  }

  public boolean tm(int arg0, int arg1) {
    if (gdb != 0) {
      wu.cjb = false;
      return false;
    }
    idb = false;
    arg0 += xu;
    arg1 += yu;
    if (av == dv && arg0 > ev && arg0 < gv && arg1 > fv && arg1 < hv) {
      wu.cjb = true;
      return false;
    }
    graphics.ug("Loading... Please wait", 256, 192, 1, 0xffffff);
    al();
    graphics.jf(yt, 0, 0);
    int i1 = bv;
    int j1 = cv;
    int k1 = (arg0 + 24) / 48;
    int l1 = (arg1 + 24) / 48;
    av = dv;
    bv = k1 * 48 - 48;
    cv = l1 * 48 - 48;
    ev = k1 * 48 - 32;
    fv = l1 * 48 - 32;
    gv = k1 * 48 + 32;
    hv = l1 * 48 + 32;
    wu.xo(arg0, arg1, av);
    bv -= xu;
    cv -= yu;
    int i2 = bv - i1;
    int j2 = cv - j1;
    for (int k2 = 0; k2 < uw; k2++) {
      ww[k2] -= i2;
      xw[k2] -= j2;
      int l2 = ww[k2];
      int j3 = xw[k2];
      int i4 = yw[k2];
      Model h1 = vw[k2];
      try {
        int j5 = zw[k2];
        int j6;
        int i7;
        if (j5 == 0 || j5 == 4) {
          j6 = Config.elb[i4];
          i7 = Config.flb[i4];
        } else {
          i7 = Config.elb[i4];
          j6 = Config.flb[i4];
        }
        int j7 = ((l2 + l2 + j6) * cu) / 2;
        int k7 = ((j3 + j3 + i7) * cu) / 2;
        if (l2 >= 0 && j3 >= 0 && l2 < 96 && j3 < 96) {
          zt.uh(h1);
          h1.ge(j7, -wu.oo(j7, k7), k7);
          wu.vo(l2, j3, i4);
          if (i4 == 74)
            h1.zd(0, -480, 0);
        }
      } catch (RuntimeException runtimeexception) {
        System.out.println("Loc Error: " + runtimeexception.getMessage());
        System.out.println("Graphics2D:" + k2 + " obj:" + h1);
        runtimeexception.printStackTrace();
      }
    }

    for (int i3 = 0; i3 < dx; i3++) {
      fx[i3] -= i2;
      gx[i3] -= j2;
      int k3 = fx[i3];
      int j4 = gx[i3];
      int l4 = ix[i3];
      int k5 = hx[i3];
      try {
        wu.hp(k3, j4, k5, l4);
        Model h2 = cm(k3, j4, k5, l4, i3);
        ex[i3] = h2;
      } catch (RuntimeException runtimeexception1) {
        System.out.println("Bound Error: " + runtimeexception1.getMessage());
        runtimeexception1.printStackTrace();
      }
    }

    for (int l3 = 0; l3 < ow; l3++) {
      pw[l3] -= i2;
      qw[l3] -= j2;
    }

    for (int k4 = 0; k4 < vv; k4++) {
      Mob l5 = zv[k4];
      l5.gr -= i2 * cu;
      l5.hr -= j2 * cu;
      for (int i6 = 0; i6 <= l5.currentWaypoint; i6++) {
        l5.waypointsX[i6] -= i2 * cu;
        l5.waypointsY[i6] -= j2 * cu;
      }

    }

    for (int i5 = 0; i5 < hw; i5++) {
      Mob l6 = kw[i5];
      l6.gr -= i2 * cu;
      l6.hr -= j2 * cu;
      for (int k6 = 0; k6 <= l6.currentWaypoint; k6++) {
        l6.waypointsX[k6] -= i2 * cu;
        l6.waypointsY[k6] -= j2 * cu;
      }

    }

    wu.cjb = true;
    return true;
  }

  public Model cm(int arg0, int arg1, int arg2, int arg3, int arg4) {
    int i1 = arg0;
    int j1 = arg1;
    int k1 = arg0;
    int l1 = arg1;
    int i2 = Config.olb[arg3];
    int j2 = Config.plb[arg3];
    int k2 = Config.nlb[arg3];
    Model h1 = new Model(4, 1);
    if (arg2 == 0)
      k1 = arg0 + 1;
    if (arg2 == 1)
      l1 = arg1 + 1;
    if (arg2 == 2) {
      i1 = arg0 + 1;
      l1 = arg1 + 1;
    }
    if (arg2 == 3) {
      k1 = arg0 + 1;
      l1 = arg1 + 1;
    }
    i1 *= cu;
    j1 *= cu;
    k1 *= cu;
    l1 *= cu;
    int l2 = h1.oe(i1, -wu.oo(i1, j1), j1);
    int i3 = h1.oe(i1, -wu.oo(i1, j1) - k2, j1);
    int j3 = h1.oe(k1, -wu.oo(k1, l1) - k2, l1);
    int k3 = h1.oe(k1, -wu.oo(k1, l1), l1);
    int ai[] = { l2, i3, j3, k3 };
    h1.ne(4, ai, i2, j2);
    h1.se(false, 60, 24, -50, -10, -50);
    if (arg0 >= 0 && arg1 >= 0 && arg0 < 96 && arg1 < 96)
      zt.uh(h1);
    h1.uh = arg4 + 10000;
    return h1;
  }

  public void gk() {
    if (edb != 0)
      am();
    else if (wcb)
      mk();
    else if (cdb)
      jm();
    else if (hdb == 1)
      um();
    else if (ccb && fdb == 0)
      nl();
    else if (ubb && fdb == 0)
      rl();
    else if (mbb)
      em();
    else if (zab)
      lm();
    else if (mab)
      sm();
    else if (yz)
      bn();
    else if (scb != 0)
      sl();
    else if (rcb != 0) {
      hk();
    } else {
      if (ncb)
        hl();
      if (bw.kr == 8 || bw.kr == 9)
        xk();
      tk();
      boolean flag = !ncb && !vy;
      if (flag)
        az = 0;
      if (kx == 0 && flag)
        dl();
      if (kx == 1)
        tl(flag);
      if (kx == 2)
        ok(flag);
      if (kx == 3)
        ql(flag);
      if (kx == 4)
        ym(flag);
      if (kx == 5)
        lk(flag);
      if (kx == 6)
        wm(flag);
      if (!vy && !ncb)
        bl();
      if (vy && !ncb)
        pm();
    }
    mt = 0;
  }

  public void hl() {
    if (mt != 0) {
      for (int i1 = 0; i1 < ocb; i1++) {
        if (super.rq >= graphics.df(pcb[i1], 1) || super.sq <= i1 * 12
            || super.sq >= 12 + i1 * 12)
          continue;
        super.stream.beginFrame(237);
        super.stream.putInt8(i1);
        super.stream.endFrame();
        break;
      }

      mt = 0;
      ncb = false;
      return;
    }
    for (int j1 = 0; j1 < ocb; j1++) {
      int k1 = 65535;
      if (super.rq < graphics.df(pcb[j1], 1) && super.sq > j1 * 12
          && super.sq < 12 + j1 * 12)
        k1 = 0xff0000;
      graphics.drawString(pcb[j1], 6, 12 + j1 * 12, 1, k1);
    }

  }

  public void xk() {
    byte byte0 = 7;
    byte byte1 = 15;
    char c1 = '\257';
    if (mt != 0) {
      for (int i1 = 0; i1 < 5; i1++) {
        if (i1 <= 0 || super.rq <= byte0 || super.rq >= byte0 + c1
            || super.sq <= byte1 + i1 * 20 || super.sq >= byte1 + i1 * 20 + 20)
          continue;
        qcb = i1 - 1;
        mt = 0;
        super.stream.beginFrame(231);
        super.stream.putInt8(qcb);
        super.stream.endFrame();
        break;
      }

    }
    for (int j1 = 0; j1 < 5; j1++) {
      if (j1 == qcb + 1)
        graphics.uf(byte0, byte1 + j1 * 20, c1, 20, Graphics2D.kg(255, 0, 0),
            128);
      else
        graphics.uf(byte0, byte1 + j1 * 20, c1, 20,
            Graphics2D.kg(190, 190, 190), 128);
      graphics.rg(byte0, byte1 + j1 * 20, c1, 0);
      graphics.rg(byte0, byte1 + j1 * 20 + 20, c1, 0);
    }

    graphics.ug("Select combat style", byte0 + c1 / 2, byte1 + 16, 3, 0xffffff);
    graphics.ug("Controlled (+1 of each)", byte0 + c1 / 2, byte1 + 36, 3, 0);
    graphics.ug("Aggressive (+3 strength)", byte0 + c1 / 2, byte1 + 56, 3, 0);
    graphics.ug("Accurate   (+3 attack)", byte0 + c1 / 2, byte1 + 76, 3, 0);
    graphics.ug("Defensive  (+3 defense)", byte0 + c1 / 2, byte1 + 96, 3, 0);
  }

  public void mk() {
    char c1 = '\264';
    int i1 = 167 - c1 / 2;
    graphics.yf(56, 167 - c1 / 2, 400, c1, 0);
    graphics.qf(56, 167 - c1 / 2, 400, c1, 0xffffff);
    i1 += 20;
    graphics.ug("Welcome to RuneScape " + username, 256, i1, 4, 0xffff00);
    i1 += 30;
    graphics.ug("You last logged in " + daysSinceLogin / 1440 + " days, "
        + (daysSinceLogin / 60) % 24 + " hours ago", 256, i1, 1, 0xffffff);
    i1 += 15;
    if (lastIP == null) {
      lastIP = Util.inetNumberToASCII(playerIP);
      try {
        lastIP = InetAddress.getByName(lastIP).getHostName();
      } catch (Exception exception) {
        String s1 = exception.getMessage();
        int l1 = s1.indexOf("cannot connect to");
        if (l1 != -1)
          lastIP = s1.substring(l1 + 18);
      }
    }
    graphics.ug("from: " + lastIP, 256, i1, 1, 0xffffff);
    i1 += 15;
    i1 += 15;
    if (adb != 0) {
      int j1 = 1 + adb / 1440;
      if (j1 > 14)
        j1 = 14;
      String s2;
      if (j1 == 14)
        s2 = "Earlier today";
      else if (j1 == 13)
        s2 = "Yesterday";
      else
        s2 = (14 - j1) + " days ago";
      graphics.ug(s2 + " you requested new recovery questions", 256, i1, 1,
          0xff8000);
      i1 += 15;
      graphics.ug("If you do not remember making this request then", 256, i1,
          1, 0xff8000);
      i1 += 15;
      graphics.ug("cancel it and change your password immediately!", 256, i1,
          1, 0xff8000);
      i1 += 15;
      i1 += 15;
      int i2 = 0xffffff;
      if (super.sq > i1 - 12 && super.sq <= i1 && super.rq > 106
          && super.rq < 406)
        i2 = 0xff0000;
      graphics.ug("No that wasn't me - Cancel the request!", 256, i1, 1, i2);
      if (i2 == 0xff0000 && mt == 1) {
        super.stream.beginFrame(196);
        super.stream.endFrame();
        wcb = false;
      }
      i1 += 15;
      i2 = 0xffffff;
      if (super.sq > i1 - 12 && super.sq <= i1 && super.rq > 106
          && super.rq < 406)
        i2 = 0xff0000;
      graphics.ug("That's ok, activate the new questions in " + j1
          + " days time", 256, i1, 1, i2);
      if (i2 == 0xff0000 && mt == 1)
        wcb = false;
    } else {
      i1 += 7;
      graphics.ug("Security tip of the day", 256, i1, 1, 0xff0000);
      i1 += 15;
      if (bdb == 0) {
        graphics.ug("Don't tell ANYONE your password or recovery questions!",
            256, i1, 1, 0xffffff);
        i1 += 15;
        graphics.ug("Not even people claiming to be Jagex staff.", 256, i1, 1,
            0xffffff);
        i1 += 15;
      }
      if (bdb == 1) {
        graphics.ug("Never enter your password or recovery questions into ANY",
            256, i1, 1, 0xffffff);
        i1 += 15;
        graphics.ug(
            "website other than this one - Not even if it looks similar.", 256,
            i1, 1, 0xffffff);
        i1 += 15;
      }
      if (bdb == 2) {
        graphics.ug("Don't use RuneScape cheats, helpers, or automaters.", 256,
            i1, 1, 0xffffff);
        i1 += 15;
        graphics.ug("These programs WILL steal your password.", 256, i1, 1,
            0xffffff);
        i1 += 15;
      }
      if (bdb == 3) {
        graphics.ug("Watch out for fake emails, and fake staff. Real staff",
            256, i1, 1, 0xffffff);
        i1 += 15;
        graphics.ug(
            "will NEVER ask you for your password or recovery questions!", 256,
            i1, 1, 0xffffff);
        i1 += 15;
      }
      if (bdb == 4) {
        graphics.ug(
            "Use a password your friends won't guess. Do NOT use your name!",
            256, i1, 1, 0xffffff);
        i1 += 15;
        graphics.ug(
            "Choose a unique password which you haven't used anywhere else",
            256, i1, 1, 0xffffff);
        i1 += 15;
      }
      if (bdb == 5) {
        graphics.ug("If possible only play runescape from your own computer",
            256, i1, 1, 0xffffff);
        i1 += 15;
        graphics.ug(
            "Other machines could have been tampered with to steal your pass",
            256, i1, 1, 0xffffff);
        i1 += 15;
      }
      i1 += 22;
      int k1 = 0xffffff;
      if (super.sq > i1 - 12 && super.sq <= i1 && super.rq > 106
          && super.rq < 406)
        k1 = 0xff0000;
      graphics.ug("Click here to close window", 256, i1, 1, k1);
      if (mt == 1) {
        if (k1 == 0xff0000)
          wcb = false;
        if ((super.rq < 86 || super.rq > 426)
            && (super.sq < 167 - c1 / 2 || super.sq > 167 + c1 / 2))
          wcb = false;
      }
    }
    mt = 0;
  }

  public void jm() {
    char c1 = '\u0190';
    byte byte0 = 100;
    graphics.yf(256 - c1 / 2, 167 - byte0 / 2, c1, byte0, 0);
    graphics.qf(256 - c1 / 2, 167 - byte0 / 2, c1, byte0, 0xffffff);
    graphics.ah(ddb, 256, 137, 1, 0xffffff, c1 - 40);
    int i1 = 157 + byte0 / 2;
    int j1 = 0xffffff;
    if (super.sq > i1 - 12 && super.sq <= i1 && super.rq > 106
        && super.rq < 406)
      j1 = 0xff0000;
    graphics.ug("Click here to close window", 256, i1, 1, j1);
    if (mt == 1) {
      if (j1 == 0xff0000)
        cdb = false;
      if ((super.rq < 256 - c1 / 2 || super.rq > 256 + c1 / 2)
          && (super.sq < 167 - byte0 / 2 || super.sq > 167 + byte0 / 2))
        cdb = false;
    }
    mt = 0;
  }

  public void am() {
    graphics.yf(126, 137, 260, 60, 0);
    graphics.qf(126, 137, 260, 60, 0xffffff);
    graphics.ug("Logging out...", 256, 173, 5, 0xffffff);
  }

  public void um() {
    int i1 = 97;
    graphics.yf(86, 77, 340, 180, 0);
    graphics.qf(86, 77, 340, 180, 0xffffff);
    graphics.ug("Warning! Proceed with caution", 256, i1, 4, 0xff0000);
    i1 += 26;
    graphics.ug("If you go much further north you will enter the", 256, i1, 1,
        0xffffff);
    i1 += 13;
    graphics.ug("wilderness. This a very dangerous area where", 256, i1, 1,
        0xffffff);
    i1 += 13;
    graphics.ug("other players can attack you!", 256, i1, 1, 0xffffff);
    i1 += 22;
    graphics.ug("The further north you go the more dangerous it", 256, i1, 1,
        0xffffff);
    i1 += 13;
    graphics.ug("becomes, but the more treasure you will find.", 256, i1, 1,
        0xffffff);
    i1 += 22;
    graphics.ug("In the wilderness an indicator at the bottom-right", 256, i1,
        1, 0xffffff);
    i1 += 13;
    graphics.ug("of the screen will show the current level of danger", 256, i1,
        1, 0xffffff);
    i1 += 22;
    int j1 = 0xffffff;
    if (super.sq > i1 - 12 && super.sq <= i1 && super.rq > 181
        && super.rq < 331)
      j1 = 0xff0000;
    graphics.ug("Click here to close window", 256, i1, 1, j1);
    if (mt != 0) {
      if (super.sq > i1 - 12 && super.sq <= i1 && super.rq > 181
          && super.rq < 331)
        hdb = 2;
      if (super.rq < 86 || super.rq > 426 || super.sq < 77 || super.sq > 257)
        hdb = 2;
      mt = 0;
    }
  }

  public void sl() {
    if (mt != 0) {
      mt = 0;
      if (super.rq < 106 || super.sq < 150 || super.rq > 406 || super.sq > 210) {
        scb = 0;
        return;
      }
    }
    int i1 = 150;
    graphics.yf(106, i1, 300, 60, 0);
    graphics.qf(106, i1, 300, 60, 0xffffff);
    i1 += 22;
    if (scb == 6) {
      graphics.ug("Please enter your current password", 256, i1, 4, 0xffffff);
      i1 += 25;
      String s1 = "*";
      for (int j1 = 0; j1 < super.yq.length(); j1++)
        s1 = "X" + s1;

      graphics.ug(s1, 256, i1, 4, 0xffffff);
      if (super.zq.length() > 0) {
        tcb = super.zq;
        super.yq = "";
        super.zq = "";
        scb = 1;
        return;
      }
    } else if (scb == 1) {
      graphics.ug("Please enter your new password", 256, i1, 4, 0xffffff);
      i1 += 25;
      String s2 = "*";
      for (int k1 = 0; k1 < super.yq.length(); k1++)
        s2 = "X" + s2;

      graphics.ug(s2, 256, i1, 4, 0xffffff);
      if (super.zq.length() > 0) {
        ucb = super.zq;
        super.yq = "";
        super.zq = "";
        if (ucb.length() >= 5) {
          scb = 2;
          return;
        } else {
          scb = 5;
          return;
        }
      }
    } else if (scb == 2) {
      graphics.ug("Enter password again to confirm", 256, i1, 4, 0xffffff);
      i1 += 25;
      String s3 = "*";
      for (int l1 = 0; l1 < super.yq.length(); l1++)
        s3 = "X" + s3;

      graphics.ug(s3, 256, i1, 4, 0xffffff);
      if (super.zq.length() > 0)
        if (super.zq.equalsIgnoreCase(ucb)) {
          scb = 4;
          x(tcb, ucb);
          return;
        } else {
          scb = 3;
          return;
        }
    } else {
      if (scb == 3) {
        graphics.ug("Passwords do not match!", 256, i1, 4, 0xffffff);
        i1 += 25;
        graphics.ug("Press any key to close", 256, i1, 4, 0xffffff);
        return;
      }
      if (scb == 4) {
        graphics.ug("Ok, your request has been sent", 256, i1, 4, 0xffffff);
        i1 += 25;
        graphics.ug("Press any key to close", 256, i1, 4, 0xffffff);
        return;
      }
      if (scb == 5) {
        graphics.ug("Password must be at", 256, i1, 4, 0xffffff);
        i1 += 25;
        graphics.ug("least 5 letters long", 256, i1, 4, 0xffffff);
      }
    }
  }

  public void hk() {
    if (mt != 0) {
      mt = 0;
      if (rcb == 1
          && (super.rq < 106 || super.sq < 145 || super.rq > 406 || super.sq > 215)) {
        rcb = 0;
        return;
      }
      if (rcb == 2
          && (super.rq < 6 || super.sq < 145 || super.rq > 506 || super.sq > 215)) {
        rcb = 0;
        return;
      }
      if (rcb == 3
          && (super.rq < 106 || super.sq < 145 || super.rq > 406 || super.sq > 215)) {
        rcb = 0;
        return;
      }
      if (super.rq > 236 && super.rq < 276 && super.sq > 193 && super.sq < 213) {
        rcb = 0;
        return;
      }
    }
    int i1 = 145;
    if (rcb == 1) {
      graphics.yf(106, i1, 300, 70, 0);
      graphics.qf(106, i1, 300, 70, 0xffffff);
      i1 += 20;
      graphics.ug("Enter name to add to friends list", 256, i1, 4, 0xffffff);
      i1 += 20;
      graphics.ug(super.yq + "*", 256, i1, 4, 0xffffff);
      if (super.zq.length() > 0) {
        String s1 = super.zq.trim();
        super.yq = "";
        super.zq = "";
        rcb = 0;
        if (s1.length() > 0 && Util.encode37(s1) != bw.cr)
          addFriend(s1);
      }
    }
    if (rcb == 2) {
      graphics.yf(6, i1, 500, 70, 0);
      graphics.qf(6, i1, 500, 70, 0xffffff);
      i1 += 20;
      graphics.ug("Enter message to send to " + Util.decode37(jy), 256, i1, 4,
          0xffffff);
      i1 += 20;
      graphics.ug(super.ar + "*", 256, i1, 4, 0xffffff);
      if (super.br.length() > 0) {
        String s2 = super.br;
        super.ar = "";
        super.br = "";
        rcb = 0;
        int k1 = Util.jn(s2);
        sendPrivateMessage(jy, Util.uhb, k1);
        s2 = Util.nn(Util.uhb, 0, k1, true);
        g("@pri@You tell " + Util.decode37(jy) + ": " + s2);
      }
    }
    if (rcb == 3) {
      graphics.yf(106, i1, 300, 70, 0);
      graphics.qf(106, i1, 300, 70, 0xffffff);
      i1 += 20;
      graphics.ug("Enter name to add to ignore list", 256, i1, 4, 0xffffff);
      i1 += 20;
      graphics.ug(super.yq + "*", 256, i1, 4, 0xffffff);
      if (super.zq.length() > 0) {
        String s3 = super.zq.trim();
        super.yq = "";
        super.zq = "";
        rcb = 0;
        if (s3.length() > 0 && Util.encode37(s3) != bw.cr)
          ignoreUser(s3);
      }
    }
    int j1 = 0xffffff;
    if (super.rq > 236 && super.rq < 276 && super.sq > 193 && super.sq < 213)
      j1 = 0xffff00;
    graphics.ug("Cancel", 256, 208, 1, j1);
  }

  public void nl() {
    char c1 = '\u0198';
    char c2 = '\u014E';
    if (mcb > 0 && gcb <= 48)
      mcb = 0;
    if (mcb > 1 && gcb <= 96)
      mcb = 1;
    if (jcb >= gcb || jcb < 0)
      jcb = -1;
    if (jcb != -1 && hcb[jcb] != kcb) {
      jcb = -1;
      kcb = -2;
    }
    if (mt != 0) {
      mt = 0;
      int i1 = super.rq - (256 - c1 / 2);
      int k1 = super.sq - (170 - c2 / 2);
      if (i1 >= 0 && k1 >= 12 && i1 < 408 && k1 < 280) {
        int i2 = mcb * 48;
        for (int l2 = 0; l2 < 6; l2++) {
          for (int l6 = 0; l6 < 8; l6++) {
            int k7 = 7 + l6 * 49;
            int j8 = 28 + l2 * 34;
            if (i1 > k7 && i1 < k7 + 49 && k1 > j8 && k1 < j8 + 34 && i2 < gcb
                && hcb[i2] != -1) {
              kcb = hcb[i2];
              jcb = i2;
            }
            i2++;
          }

        }

        i1 = 256 - c1 / 2;
        k1 = 170 - c2 / 2;
        int itemSlot;
        if (jcb < 0)
          itemSlot = -1;
        else
          itemSlot = hcb[jcb];
        if (itemSlot != -1) {
          int j2 = icb[jcb];
          if (Config.pjb[itemSlot] == 1 && j2 > 1)
            j2 = 1;
          if (j2 >= 1 && super.rq >= i1 + 220 && super.sq >= k1 + 238
              && super.rq < i1 + 250 && super.sq <= k1 + 249) {
            super.stream.beginFrame(206);
            super.stream.putInt16(itemSlot);
            super.stream.putInt16(1);
            super.stream.endFrame();
          }
          if (j2 >= 5 && super.rq >= i1 + 250 && super.sq >= k1 + 238
              && super.rq < i1 + 280 && super.sq <= k1 + 249) {
            super.stream.beginFrame(206);
            super.stream.putInt16(itemSlot);
            super.stream.putInt16(5);
            super.stream.endFrame();
          }
          if (j2 >= 25 && super.rq >= i1 + 280 && super.sq >= k1 + 238
              && super.rq < i1 + 305 && super.sq <= k1 + 249) {
            super.stream.beginFrame(206);
            super.stream.putInt16(itemSlot);
            super.stream.putInt16(25);
            super.stream.endFrame();
          }
          if (j2 >= 100 && super.rq >= i1 + 305 && super.sq >= k1 + 238
              && super.rq < i1 + 335 && super.sq <= k1 + 249) {
            super.stream.beginFrame(206);
            super.stream.putInt16(itemSlot);
            super.stream.putInt16(100);
            super.stream.endFrame();
          }
          if (j2 >= 500 && super.rq >= i1 + 335 && super.sq >= k1 + 238
              && super.rq < i1 + 368 && super.sq <= k1 + 249) {
            super.stream.beginFrame(206);
            super.stream.putInt16(itemSlot);
            super.stream.putInt16(500);
            super.stream.endFrame();
          }
          if (j2 >= 2500 && super.rq >= i1 + 370 && super.sq >= k1 + 238
              && super.rq < i1 + 400 && super.sq <= k1 + 249) {
            super.stream.beginFrame(206);
            super.stream.putInt16(itemSlot);
            super.stream.putInt16(2500);
            super.stream.endFrame();
          }
          if (gl(itemSlot) >= 1 && super.rq >= i1 + 220 && super.sq >= k1 + 263
              && super.rq < i1 + 250 && super.sq <= k1 + 274) {
            super.stream.beginFrame(205);
            super.stream.putInt16(itemSlot);
            super.stream.putInt16(1);
            super.stream.endFrame();
          }
          if (gl(itemSlot) >= 5 && super.rq >= i1 + 250 && super.sq >= k1 + 263
              && super.rq < i1 + 280 && super.sq <= k1 + 274) {
            super.stream.beginFrame(205);
            super.stream.putInt16(itemSlot);
            super.stream.putInt16(5);
            super.stream.endFrame();
          }
          if (gl(itemSlot) >= 25 && super.rq >= i1 + 280
              && super.sq >= k1 + 263 && super.rq < i1 + 305
              && super.sq <= k1 + 274) {
            super.stream.beginFrame(205);
            super.stream.putInt16(itemSlot);
            super.stream.putInt16(25);
            super.stream.endFrame();
          }
          if (gl(itemSlot) >= 100 && super.rq >= i1 + 305
              && super.sq >= k1 + 263 && super.rq < i1 + 335
              && super.sq <= k1 + 274) {
            super.stream.beginFrame(205);
            super.stream.putInt16(itemSlot);
            super.stream.putInt16(100);
            super.stream.endFrame();
          }
          if (gl(itemSlot) >= 500 && super.rq >= i1 + 335
              && super.sq >= k1 + 263 && super.rq < i1 + 368
              && super.sq <= k1 + 274) {
            super.stream.beginFrame(205);
            super.stream.putInt16(itemSlot);
            super.stream.putInt16(500);
            super.stream.endFrame();
          }
          if (gl(itemSlot) >= 2500 && super.rq >= i1 + 370
              && super.sq >= k1 + 263 && super.rq < i1 + 400
              && super.sq <= k1 + 274) {
            super.stream.beginFrame(205);
            super.stream.putInt16(itemSlot);
            super.stream.putInt16(2500);
            super.stream.endFrame();
          }
        }
      } else if (gcb > 48 && i1 >= 70 && i1 <= 140 && k1 <= 12)
        mcb = 0;
      else if (gcb > 48 && i1 >= 140 && i1 <= 210 && k1 <= 12)
        mcb = 1;
      else if (gcb > 96 && i1 >= 210 && i1 <= 280 && k1 <= 12) {
        mcb = 2;
      } else {
        super.stream.beginFrame(207);
        super.stream.endFrame();
        ccb = false;
        return;
      }
    }
    int j1 = 256 - c1 / 2;
    int l1 = 170 - c2 / 2;
    graphics.yf(j1, l1, 408, 12, 192);
    int k2 = 0x989898;
    graphics.uf(j1, l1 + 12, 408, 17, k2, 160);
    graphics.uf(j1, l1 + 29, 8, 204, k2, 160);
    graphics.uf(j1 + 399, l1 + 29, 9, 204, k2, 160);
    graphics.uf(j1, l1 + 233, 408, 47, k2, 160);
    graphics.drawString("Bank", j1 + 1, l1 + 10, 1, 0xffffff);
    if (gcb > 48) {
      int i3 = 0xffffff;
      if (mcb == 0)
        i3 = 0xff0000;
      else if (super.rq > (j1 + 120) - 50 && super.sq >= l1
          && super.rq < (j1 + 190) - 50 && super.sq < l1 + 12)
        i3 = 0xffff00;
      graphics.drawString("<page 1>", (j1 + 120) - 40, l1 + 10, 1, i3);
      i3 = 0xffffff;
      if (mcb == 1)
        i3 = 0xff0000;
      else if (super.rq > (j1 + 190) - 50 && super.sq >= l1
          && super.rq < (j1 + 260) - 50 && super.sq < l1 + 12)
        i3 = 0xffff00;
      graphics.drawString("<page 2>", (j1 + 190) - 40, l1 + 10, 1, i3);
    }
    if (gcb > 96) {
      int j3 = 0xffffff;
      if (mcb == 2)
        j3 = 0xff0000;
      else if (super.rq > (j1 + 260) - 50 && super.sq >= l1
          && super.rq < (j1 + 330) - 50 && super.sq < l1 + 12)
        j3 = 0xffff00;
      graphics.drawString("<page 3>", (j1 + 260) - 40, l1 + 10, 1, j3);
    }
    int k3 = 0xffffff;
    if (super.rq > j1 + 320 && super.sq >= l1 && super.rq < j1 + 408
        && super.sq < l1 + 12)
      k3 = 0xff0000;
    graphics.yg("Close window", j1 + 406, l1 + 10, 1, k3);
    graphics.drawString("Number in bank in green", j1 + 7, l1 + 24, 1, 65280);
    graphics.drawString("Number held in blue", j1 + 289, l1 + 24, 1, 65535);
    int j7 = 0xd0d0d0;
    int l7 = mcb * 48;
    for (int k8 = 0; k8 < 6; k8++) {
      for (int l8 = 0; l8 < 8; l8++) {
        int j9 = j1 + 7 + l8 * 49;
        int k9 = l1 + 28 + k8 * 34;
        if (jcb == l7)
          graphics.uf(j9, k9, 49, 34, 0xff0000, 160);
        else
          graphics.uf(j9, k9, 49, 34, j7, 160);
        graphics.qf(j9, k9, 50, 35, 0);
        if (l7 < gcb && hcb[l7] != -1) {
          graphics.wf(j9, k9, 48, 32, ju + Config.njb[hcb[l7]],
              Config.sjb[hcb[l7]], 0, 0, false);
          graphics.drawString(String.valueOf(icb[l7]), j9 + 1, k9 + 10, 1,
              65280);
          graphics.yg(String.valueOf(gl(hcb[l7])), j9 + 47, k9 + 29, 1, 65535);
        }
        l7++;
      }

    }

    graphics.rg(j1 + 5, l1 + 256, 398, 0);
    if (jcb == -1) {
      graphics.ug("Select an object to withdraw or deposit", j1 + 204,
          l1 + 248, 3, 0xffff00);
      return;
    }
    int i9;
    if (jcb < 0)
      i9 = -1;
    else
      i9 = hcb[jcb];
    if (i9 != -1) {
      int i8 = icb[jcb];
      if (Config.pjb[i9] == 1 && i8 > 1)
        i8 = 1;
      if (i8 > 0) {
        graphics.drawString("Withdraw " + Config.kjb[i9], j1 + 2, l1 + 248, 1,
            0xffffff);
        int l3 = 0xffffff;
        if (super.rq >= j1 + 220 && super.sq >= l1 + 238 && super.rq < j1 + 250
            && super.sq <= l1 + 249)
          l3 = 0xff0000;
        graphics.drawString("One", j1 + 222, l1 + 248, 1, l3);
        if (i8 >= 5) {
          int i4 = 0xffffff;
          if (super.rq >= j1 + 250 && super.sq >= l1 + 238
              && super.rq < j1 + 280 && super.sq <= l1 + 249)
            i4 = 0xff0000;
          graphics.drawString("Five", j1 + 252, l1 + 248, 1, i4);
        }
        if (i8 >= 25) {
          int j4 = 0xffffff;
          if (super.rq >= j1 + 280 && super.sq >= l1 + 238
              && super.rq < j1 + 305 && super.sq <= l1 + 249)
            j4 = 0xff0000;
          graphics.drawString("25", j1 + 282, l1 + 248, 1, j4);
        }
        if (i8 >= 100) {
          int k4 = 0xffffff;
          if (super.rq >= j1 + 305 && super.sq >= l1 + 238
              && super.rq < j1 + 335 && super.sq <= l1 + 249)
            k4 = 0xff0000;
          graphics.drawString("100", j1 + 307, l1 + 248, 1, k4);
        }
        if (i8 >= 500) {
          int l4 = 0xffffff;
          if (super.rq >= j1 + 335 && super.sq >= l1 + 238
              && super.rq < j1 + 368 && super.sq <= l1 + 249)
            l4 = 0xff0000;
          graphics.drawString("500", j1 + 337, l1 + 248, 1, l4);
        }
        if (i8 >= 2500) {
          int i5 = 0xffffff;
          if (super.rq >= j1 + 370 && super.sq >= l1 + 238
              && super.rq < j1 + 400 && super.sq <= l1 + 249)
            i5 = 0xff0000;
          graphics.drawString("2500", j1 + 370, l1 + 248, 1, i5);
        }
      }
      if (gl(i9) > 0) {
        graphics.drawString("Deposit " + Config.kjb[i9], j1 + 2, l1 + 273, 1,
            0xffffff);
        int j5 = 0xffffff;
        if (super.rq >= j1 + 220 && super.sq >= l1 + 263 && super.rq < j1 + 250
            && super.sq <= l1 + 274)
          j5 = 0xff0000;
        graphics.drawString("One", j1 + 222, l1 + 273, 1, j5);
        if (gl(i9) >= 5) {
          int k5 = 0xffffff;
          if (super.rq >= j1 + 250 && super.sq >= l1 + 263
              && super.rq < j1 + 280 && super.sq <= l1 + 274)
            k5 = 0xff0000;
          graphics.drawString("Five", j1 + 252, l1 + 273, 1, k5);
        }
        if (gl(i9) >= 25) {
          int l5 = 0xffffff;
          if (super.rq >= j1 + 280 && super.sq >= l1 + 263
              && super.rq < j1 + 305 && super.sq <= l1 + 274)
            l5 = 0xff0000;
          graphics.drawString("25", j1 + 282, l1 + 273, 1, l5);
        }
        if (gl(i9) >= 100) {
          int i6 = 0xffffff;
          if (super.rq >= j1 + 305 && super.sq >= l1 + 263
              && super.rq < j1 + 335 && super.sq <= l1 + 274)
            i6 = 0xff0000;
          graphics.drawString("100", j1 + 307, l1 + 273, 1, i6);
        }
        if (gl(i9) >= 500) {
          int j6 = 0xffffff;
          if (super.rq >= j1 + 335 && super.sq >= l1 + 263
              && super.rq < j1 + 368 && super.sq <= l1 + 274)
            j6 = 0xff0000;
          graphics.drawString("500", j1 + 337, l1 + 273, 1, j6);
        }
        if (gl(i9) >= 2500) {
          int k6 = 0xffffff;
          if (super.rq >= j1 + 370 && super.sq >= l1 + 263
              && super.rq < j1 + 400 && super.sq <= l1 + 274)
            k6 = 0xff0000;
          graphics.drawString("2500", j1 + 370, l1 + 273, 1, k6);
        }
      }
    }
  }

  public void rl() {
    if (mt != 0) {
      mt = 0;
      int i1 = super.rq - 52;
      int j1 = super.sq - 44;
      if (i1 >= 0 && j1 >= 12 && i1 < 408 && j1 < 246) {
        int k1 = 0;
        for (int i2 = 0; i2 < 5; i2++) {
          for (int i3 = 0; i3 < 8; i3++) {
            int l3 = 7 + i3 * 49;
            int l4 = 28 + i2 * 34;
            if (i1 > l3 && i1 < l3 + 49 && j1 > l4 && j1 < l4 + 34
                && xbb[k1] != -1) {
              acb = k1;
              bcb = xbb[k1];
            }
            k1++;
          }

        }

        if (acb >= 0) {
          int j3 = xbb[acb];
          if (j3 != -1) {
            if (ybb[acb] > 0 && i1 > 298 && j1 >= 204 && i1 < 408 && j1 <= 215) {
              int i4 = wbb + zbb[acb];
              if (i4 < 10)
                i4 = 10;
              int i5 = (i4 * Config.ojb[j3]) / 100;
              super.stream.beginFrame(217);
              super.stream.putInt16(xbb[acb]);
              super.stream.putInt32(i5);
              super.stream.endFrame();
            }
            if (gl(j3) > 0 && i1 > 2 && j1 >= 229 && i1 < 112 && j1 <= 240) {
              int j4 = vbb + zbb[acb];
              if (j4 < 10)
                j4 = 10;
              int j5 = (j4 * Config.ojb[j3]) / 100;
              super.stream.beginFrame(216);
              super.stream.putInt16(xbb[acb]);
              super.stream.putInt32(j5);
              super.stream.endFrame();
            }
          }
        }
      } else {
        super.stream.beginFrame(218);
        super.stream.endFrame();
        ubb = false;
        return;
      }
    }
    byte byte0 = 52;
    byte byte1 = 44;
    graphics.yf(byte0, byte1, 408, 12, 192);
    int l1 = 0x989898;
    graphics.uf(byte0, byte1 + 12, 408, 17, l1, 160);
    graphics.uf(byte0, byte1 + 29, 8, 170, l1, 160);
    graphics.uf(byte0 + 399, byte1 + 29, 9, 170, l1, 160);
    graphics.uf(byte0, byte1 + 199, 408, 47, l1, 160);
    graphics.drawString("Buying and selling items", byte0 + 1, byte1 + 10, 1,
        0xffffff);
    int j2 = 0xffffff;
    if (super.rq > byte0 + 320 && super.sq >= byte1 && super.rq < byte0 + 408
        && super.sq < byte1 + 12)
      j2 = 0xff0000;
    graphics.yg("Close window", byte0 + 406, byte1 + 10, 1, j2);
    graphics
        .drawString("Shops stock in green", byte0 + 2, byte1 + 24, 1, 65280);
    graphics.drawString("Number you own in blue", byte0 + 135, byte1 + 24, 1,
        65535);
    graphics.drawString("Your money: " + gl(10) + "gp", byte0 + 280,
        byte1 + 24, 1, 0xffff00);
    int k3 = 0xd0d0d0;
    int k4 = 0;
    for (int k5 = 0; k5 < 5; k5++) {
      for (int l5 = 0; l5 < 8; l5++) {
        int j6 = byte0 + 7 + l5 * 49;
        int i7 = byte1 + 28 + k5 * 34;
        if (acb == k4)
          graphics.uf(j6, i7, 49, 34, 0xff0000, 160);
        else
          graphics.uf(j6, i7, 49, 34, k3, 160);
        graphics.qf(j6, i7, 50, 35, 0);
        if (xbb[k4] != -1) {
          graphics.wf(j6, i7, 48, 32, ju + Config.njb[xbb[k4]],
              Config.sjb[xbb[k4]], 0, 0, false);
          graphics.drawString(String.valueOf(ybb[k4]), j6 + 1, i7 + 10, 1,
              65280);
          graphics.yg(String.valueOf(gl(xbb[k4])), j6 + 47, i7 + 10, 1, 65535);
        }
        k4++;
      }

    }

    graphics.rg(byte0 + 5, byte1 + 222, 398, 0);
    if (acb == -1) {
      graphics.ug("Select an object to buy or sell", byte0 + 204, byte1 + 214,
          3, 0xffff00);
      return;
    }
    int i6 = xbb[acb];
    if (i6 != -1) {
      if (ybb[acb] > 0) {
        int k6 = wbb + zbb[acb];
        if (k6 < 10)
          k6 = 10;
        int j7 = (k6 * Config.ojb[i6]) / 100;
        graphics.drawString(
            "Buy a new " + Config.kjb[i6] + " for " + j7 + "gp", byte0 + 2,
            byte1 + 214, 1, 0xffff00);
        int k2 = 0xffffff;
        if (super.rq > byte0 + 298 && super.sq >= byte1 + 204
            && super.rq < byte0 + 408 && super.sq <= byte1 + 215)
          k2 = 0xff0000;
        graphics.yg("Click here to buy", byte0 + 405, byte1 + 214, 3, k2);
      } else {
        graphics.ug("This item is not currently available to buy", byte0 + 204,
            byte1 + 214, 3, 0xffff00);
      }
      if (gl(i6) > 0) {
        int l6 = vbb + zbb[acb];
        if (l6 < 10)
          l6 = 10;
        int k7 = (l6 * Config.ojb[i6]) / 100;
        graphics.yg("Sell your " + Config.kjb[i6] + " for " + k7 + "gp",
            byte0 + 405, byte1 + 239, 1, 0xffff00);
        int l2 = 0xffffff;
        if (super.rq > byte0 + 2 && super.sq >= byte1 + 229
            && super.rq < byte0 + 112 && super.sq <= byte1 + 240)
          l2 = 0xff0000;
        graphics
            .drawString("Click here to sell", byte0 + 2, byte1 + 239, 3, l2);
        return;
      }
      graphics.ug("You do not have any of this item to sell", byte0 + 204,
          byte1 + 239, 3, 0xffff00);
    }
  }

  public void em() {
    byte byte0 = 22;
    byte byte1 = 36;
    graphics.yf(byte0, byte1, 468, 16, 192);
    int i1 = 0x989898;
    graphics.uf(byte0, byte1 + 16, 468, 246, i1, 160);
    graphics.ug("Please confirm your trade with @yel@" + Util.decode37(lbb),
        byte0 + 234, byte1 + 12, 1, 0xffffff);
    graphics.ug("You are about to give:", byte0 + 117, byte1 + 30, 1, 0xffff00);
    for (int j1 = 0; j1 < obb; j1++) {
      String s1 = Config.kjb[pbb[j1]];
      if (Config.pjb[pbb[j1]] == 0)
        s1 = s1 + " (" + qbb[j1] + ")";
      graphics.ug(s1, byte0 + 117, byte1 + 42 + j1 * 12, 1, 0xffffff);
    }

    if (obb == 0)
      graphics.ug("Nothing!", byte0 + 117, byte1 + 42, 1, 0xffffff);
    graphics.ug("In return you will receive:", byte0 + 351, byte1 + 30, 1,
        0xffff00);
    for (int k1 = 0; k1 < rbb; k1++) {
      String s2 = Config.kjb[sbb[k1]];
      if (Config.pjb[sbb[k1]] == 0)
        s2 = s2 + " (" + tbb[k1] + ")";
      graphics.ug(s2, byte0 + 351, byte1 + 42 + k1 * 12, 1, 0xffffff);
    }

    if (rbb == 0)
      graphics.ug("Nothing!", byte0 + 351, byte1 + 42, 1, 0xffffff);
    graphics.ug("Are you sure you want to do this?", byte0 + 234, byte1 + 200,
        4, 65535);
    graphics.ug("There is NO WAY to reverse a trade if you change your mind.",
        byte0 + 234, byte1 + 215, 1, 0xffffff);
    graphics.ug("Remember that not all players are trustworthy", byte0 + 234,
        byte1 + 230, 1, 0xffffff);
    if (!nbb) {
      graphics.xg((byte0 + 118) - 35, byte1 + 238, hu + 25);
      graphics.xg((byte0 + 352) - 35, byte1 + 238, hu + 26);
    } else {
      graphics.ug("Waiting for other player...", byte0 + 234, byte1 + 250, 1,
          0xffff00);
    }
    if (mt == 1) {
      if (super.rq < byte0 || super.sq < byte1 || super.rq > byte0 + 468
          || super.sq > byte1 + 262) {
        mbb = false;
        super.stream.beginFrame(233);
        super.stream.endFrame();
      }
      if (super.rq >= (byte0 + 118) - 35 && super.rq <= byte0 + 118 + 70
          && super.sq >= byte1 + 238 && super.sq <= byte1 + 238 + 21) {
        nbb = true;
        super.stream.beginFrame(202);
        super.stream.endFrame();
      }
      if (super.rq >= (byte0 + 352) - 35 && super.rq <= byte0 + 353 + 70
          && super.sq >= byte1 + 238 && super.sq <= byte1 + 238 + 21) {
        mbb = false;
        super.stream.beginFrame(233);
        super.stream.endFrame();
      }
      mt = 0;
    }
  }

  public void lm() {
    if (mt != 0 && kbb == 0)
      kbb = 1;
    if (kbb > 0) {
      int i1 = super.rq - 22;
      int j1 = super.sq - 36;
      if (i1 >= 0 && j1 >= 0 && i1 < 468 && j1 < 262) {
        if (i1 > 216 && j1 > 30 && i1 < 462 && j1 < 235) {
          int k1 = (i1 - 217) / 49 + ((j1 - 31) / 34) * 5;
          if (k1 >= 0 && k1 < mx) {
            boolean flag = false;
            int l2 = 0;
            int k3 = nx[k1];
            for (int k4 = 0; k4 < bbb; k4++)
              if (cbb[k4] == k3)
                if (Config.pjb[k3] == 0) {
                  for (int i5 = 0; i5 < kbb; i5++) {
                    if (dbb[k4] < ox[k1])
                      dbb[k4]++;
                    flag = true;
                  }

                } else {
                  l2++;
                }

            if (gl(k3) <= l2)
              flag = true;
            if (Config.tjb[k3] == 1) {
              appendMessage("This object cannot be traded with other players",
                  3);
              flag = true;
            }
            if (!flag && bbb < 12) {
              cbb[bbb] = k3;
              dbb[bbb] = 1;
              bbb++;
              flag = true;
            }
            if (flag) {
              super.stream.beginFrame(234);
              super.stream.putInt8(bbb);
              for (int j5 = 0; j5 < bbb; j5++) {
                super.stream.putInt16(cbb[j5]);
                super.stream.putInt32(dbb[j5]);
              }

              super.stream.endFrame();
              hbb = false;
              ibb = false;
            }
          }
        }
        if (i1 > 8 && j1 > 30 && i1 < 205 && j1 < 133) {
          int l1 = (i1 - 9) / 49 + ((j1 - 31) / 34) * 4;
          if (l1 >= 0 && l1 < bbb) {
            int j2 = cbb[l1];
            for (int i3 = 0; i3 < kbb; i3++) {
              if (Config.pjb[j2] == 0 && dbb[l1] > 1) {
                dbb[l1]--;
                continue;
              }
              bbb--;
              jbb = 0;
              for (int l3 = l1; l3 < bbb; l3++) {
                cbb[l3] = cbb[l3 + 1];
                dbb[l3] = dbb[l3 + 1];
              }

              break;
            }

            super.stream.beginFrame(234);
            super.stream.putInt8(bbb);
            for (int i4 = 0; i4 < bbb; i4++) {
              super.stream.putInt16(cbb[i4]);
              super.stream.putInt32(dbb[i4]);
            }

            super.stream.endFrame();
            hbb = false;
            ibb = false;
          }
        }
        if (i1 >= 217 && j1 >= 238 && i1 <= 286 && j1 <= 259) {
          ibb = true;
          super.stream.beginFrame(232);
          super.stream.endFrame();
        }
        if (i1 >= 394 && j1 >= 238 && i1 < 463 && j1 < 259) {
          zab = false;
          super.stream.beginFrame(233);
          super.stream.endFrame();
        }
      } else if (mt != 0) {
        zab = false;
        super.stream.beginFrame(233);
        super.stream.endFrame();
      }
      mt = 0;
      kbb = 0;
    }
    if (!zab)
      return;
    byte byte0 = 22;
    byte byte1 = 36;
    graphics.yf(byte0, byte1, 468, 12, 192);
    int i2 = 0x989898;
    graphics.uf(byte0, byte1 + 12, 468, 18, i2, 160);
    graphics.uf(byte0, byte1 + 30, 8, 248, i2, 160);
    graphics.uf(byte0 + 205, byte1 + 30, 11, 248, i2, 160);
    graphics.uf(byte0 + 462, byte1 + 30, 6, 248, i2, 160);
    graphics.uf(byte0 + 8, byte1 + 133, 197, 22, i2, 160);
    graphics.uf(byte0 + 8, byte1 + 258, 197, 20, i2, 160);
    graphics.uf(byte0 + 216, byte1 + 235, 246, 43, i2, 160);
    int k2 = 0xd0d0d0;
    graphics.uf(byte0 + 8, byte1 + 30, 197, 103, k2, 160);
    graphics.uf(byte0 + 8, byte1 + 155, 197, 103, k2, 160);
    graphics.uf(byte0 + 216, byte1 + 30, 246, 205, k2, 160);
    for (int j3 = 0; j3 < 4; j3++)
      graphics.rg(byte0 + 8, byte1 + 30 + j3 * 34, 197, 0);

    for (int j4 = 0; j4 < 4; j4++)
      graphics.rg(byte0 + 8, byte1 + 155 + j4 * 34, 197, 0);

    for (int l4 = 0; l4 < 7; l4++)
      graphics.rg(byte0 + 216, byte1 + 30 + l4 * 34, 246, 0);

    for (int k5 = 0; k5 < 6; k5++) {
      if (k5 < 5)
        graphics.vg(byte0 + 8 + k5 * 49, byte1 + 30, 103, 0);
      if (k5 < 5)
        graphics.vg(byte0 + 8 + k5 * 49, byte1 + 155, 103, 0);
      graphics.vg(byte0 + 216 + k5 * 49, byte1 + 30, 205, 0);
    }

    graphics.drawString("Trading with: " + abb, byte0 + 1, byte1 + 10, 1,
        0xffffff);
    graphics.drawString("Your Offer", byte0 + 9, byte1 + 27, 4, 0xffffff);
    graphics
        .drawString("Opponent's Offer", byte0 + 9, byte1 + 152, 4, 0xffffff);
    graphics.drawString("Your Inventory", byte0 + 216, byte1 + 27, 4, 0xffffff);
    if (!ibb)
      graphics.xg(byte0 + 217, byte1 + 238, hu + 25);
    graphics.xg(byte0 + 394, byte1 + 238, hu + 26);
    if (hbb) {
      graphics.ug("Other player", byte0 + 341, byte1 + 246, 1, 0xffffff);
      graphics.ug("has accepted", byte0 + 341, byte1 + 256, 1, 0xffffff);
    }
    if (ibb) {
      graphics.ug("Waiting for", byte0 + 217 + 35, byte1 + 246, 1, 0xffffff);
      graphics.ug("other player", byte0 + 217 + 35, byte1 + 256, 1, 0xffffff);
    }
    for (int l5 = 0; l5 < mx; l5++) {
      int i6 = 217 + byte0 + (l5 % 5) * 49;
      int k6 = 31 + byte1 + (l5 / 5) * 34;
      graphics.wf(i6, k6, 48, 32, ju + Config.njb[nx[l5]], Config.sjb[nx[l5]],
          0, 0, false);
      if (Config.pjb[nx[l5]] == 0)
        graphics.drawString(String.valueOf(ox[l5]), i6 + 1, k6 + 10, 1,
            0xffff00);
    }

    for (int j6 = 0; j6 < bbb; j6++) {
      int l6 = 9 + byte0 + (j6 % 4) * 49;
      int j7 = 31 + byte1 + (j6 / 4) * 34;
      graphics.wf(l6, j7, 48, 32, ju + Config.njb[cbb[j6]],
          Config.sjb[cbb[j6]], 0, 0, false);
      if (Config.pjb[cbb[j6]] == 0)
        graphics.drawString(String.valueOf(dbb[j6]), l6 + 1, j7 + 10, 1,
            0xffff00);
      if (super.rq > l6 && super.rq < l6 + 48 && super.sq > j7
          && super.sq < j7 + 32)
        graphics.drawString(Config.kjb[cbb[j6]] + ": @whi@"
            + Config.ljb[cbb[j6]], byte0 + 8, byte1 + 273, 1, 0xffff00);
    }

    for (int i7 = 0; i7 < ebb; i7++) {
      int k7 = 9 + byte0 + (i7 % 4) * 49;
      int l7 = 156 + byte1 + (i7 / 4) * 34;
      graphics.wf(k7, l7, 48, 32, ju + Config.njb[fbb[i7]],
          Config.sjb[fbb[i7]], 0, 0, false);
      if (Config.pjb[fbb[i7]] == 0)
        graphics.drawString(String.valueOf(gbb[i7]), k7 + 1, l7 + 10, 1,
            0xffff00);
      if (super.rq > k7 && super.rq < k7 + 48 && super.sq > l7
          && super.sq < l7 + 32)
        graphics.drawString(Config.kjb[fbb[i7]] + ": @whi@"
            + Config.ljb[fbb[i7]], byte0 + 8, byte1 + 273, 1, 0xffff00);
    }

  }

  public void sm() {
    byte byte0 = 22;
    byte byte1 = 36;
    graphics.yf(byte0, byte1, 468, 16, 192);
    int i1 = 0x989898;
    graphics.uf(byte0, byte1 + 16, 468, 246, i1, 160);
    graphics.ug("Please confirm your duel with @yel@" + Util.decode37(oab),
        byte0 + 234, byte1 + 12, 1, 0xffffff);
    graphics.ug("Your stake:", byte0 + 117, byte1 + 30, 1, 0xffff00);
    for (int j1 = 0; j1 < pab; j1++) {
      String s1 = Config.kjb[qab[j1]];
      if (Config.pjb[qab[j1]] == 0)
        s1 = s1 + " (" + rab[j1] + ")";
      graphics.ug(s1, byte0 + 117, byte1 + 42 + j1 * 12, 1, 0xffffff);
    }

    if (pab == 0)
      graphics.ug("Nothing!", byte0 + 117, byte1 + 42, 1, 0xffffff);
    graphics.ug("Your opponent's stake:", byte0 + 351, byte1 + 30, 1, 0xffff00);
    for (int k1 = 0; k1 < sab; k1++) {
      String s2 = Config.kjb[tab[k1]];
      if (Config.pjb[tab[k1]] == 0)
        s2 = s2 + " (" + uab[k1] + ")";
      graphics.ug(s2, byte0 + 351, byte1 + 42 + k1 * 12, 1, 0xffffff);
    }

    if (sab == 0)
      graphics.ug("Nothing!", byte0 + 351, byte1 + 42, 1, 0xffffff);
    if (vab == 0)
      graphics.ug("You can retreat from this duel", byte0 + 234, byte1 + 180,
          1, 65280);
    else
      graphics.ug("No retreat is possible!", byte0 + 234, byte1 + 180, 1,
          0xff0000);
    if (wab == 0)
      graphics.ug("Magic may be used", byte0 + 234, byte1 + 192, 1, 65280);
    else
      graphics
          .ug("Magic cannot be used", byte0 + 234, byte1 + 192, 1, 0xff0000);
    if (xab == 0)
      graphics.ug("Prayer may be used", byte0 + 234, byte1 + 204, 1, 65280);
    else
      graphics.ug("Prayer cannot be used", byte0 + 234, byte1 + 204, 1,
          0xff0000);
    if (yab == 0)
      graphics.ug("Weapons may be used", byte0 + 234, byte1 + 216, 1, 65280);
    else
      graphics.ug("Weapons cannot be used", byte0 + 234, byte1 + 216, 1,
          0xff0000);
    graphics.ug("If you are sure click 'Accept' to begin the duel",
        byte0 + 234, byte1 + 230, 1, 0xffffff);
    if (!nab) {
      graphics.xg((byte0 + 118) - 35, byte1 + 238, hu + 25);
      graphics.xg((byte0 + 352) - 35, byte1 + 238, hu + 26);
    } else {
      graphics.ug("Waiting for other player...", byte0 + 234, byte1 + 250, 1,
          0xffff00);
    }
    if (mt == 1) {
      if (super.rq < byte0 || super.sq < byte1 || super.rq > byte0 + 468
          || super.sq > byte1 + 262) {
        mab = false;
        super.stream.beginFrame(233);
        super.stream.endFrame();
      }
      if (super.rq >= (byte0 + 118) - 35 && super.rq <= byte0 + 118 + 70
          && super.sq >= byte1 + 238 && super.sq <= byte1 + 238 + 21) {
        nab = true;
        super.stream.beginFrame(198);
        super.stream.endFrame();
      }
      if (super.rq >= (byte0 + 352) - 35 && super.rq <= byte0 + 353 + 70
          && super.sq >= byte1 + 238 && super.sq <= byte1 + 238 + 21) {
        mab = false;
        super.stream.beginFrame(203);
        super.stream.endFrame();
      }
      mt = 0;
    }
  }

  public void bn() {
    if (mt != 0 && kbb == 0)
      kbb = 1;
    if (kbb > 0) {
      int i1 = super.rq - 22;
      int j1 = super.sq - 36;
      if (i1 >= 0 && j1 >= 0 && i1 < 468 && j1 < 262) {
        if (i1 > 216 && j1 > 30 && i1 < 462 && j1 < 235) {
          int k1 = (i1 - 217) / 49 + ((j1 - 31) / 34) * 5;
          if (k1 >= 0 && k1 < mx) {
            boolean flag1 = false;
            int l2 = 0;
            int k3 = nx[k1];
            for (int k4 = 0; k4 < aab; k4++)
              if (bab[k4] == k3)
                if (Config.pjb[k3] == 0) {
                  for (int i5 = 0; i5 < kbb; i5++) {
                    if (cab[k4] < ox[k1])
                      cab[k4]++;
                    flag1 = true;
                  }

                } else {
                  l2++;
                }

            if (gl(k3) <= l2)
              flag1 = true;
            if (Config.tjb[k3] == 1) {
              appendMessage("This object cannot be added to a duel offer", 3);
              flag1 = true;
            }
            if (!flag1 && aab < 8) {
              bab[aab] = k3;
              cab[aab] = 1;
              aab++;
              flag1 = true;
            }
            if (flag1) {
              super.stream.beginFrame(201);
              super.stream.putInt8(aab);
              for (int j5 = 0; j5 < aab; j5++) {
                super.stream.putInt16(bab[j5]);
                super.stream.putInt32(cab[j5]);
              }

              super.stream.endFrame();
              gab = false;
              hab = false;
            }
          }
        }
        if (i1 > 8 && j1 > 30 && i1 < 205 && j1 < 129) {
          int l1 = (i1 - 9) / 49 + ((j1 - 31) / 34) * 4;
          if (l1 >= 0 && l1 < aab) {
            int j2 = bab[l1];
            for (int i3 = 0; i3 < kbb; i3++) {
              if (Config.pjb[j2] == 0 && cab[l1] > 1) {
                cab[l1]--;
                continue;
              }
              aab--;
              jbb = 0;
              for (int l3 = l1; l3 < aab; l3++) {
                bab[l3] = bab[l3 + 1];
                cab[l3] = cab[l3 + 1];
              }

              break;
            }

            super.stream.beginFrame(201);
            super.stream.putInt8(aab);
            for (int i4 = 0; i4 < aab; i4++) {
              super.stream.putInt16(bab[i4]);
              super.stream.putInt32(cab[i4]);
            }

            super.stream.endFrame();
            gab = false;
            hab = false;
          }
        }
        boolean flag = false;
        if (i1 >= 93 && j1 >= 221 && i1 <= 104 && j1 <= 232) {
          iab = !iab;
          flag = true;
        }
        if (i1 >= 93 && j1 >= 240 && i1 <= 104 && j1 <= 251) {
          jab = !jab;
          flag = true;
        }
        if (i1 >= 191 && j1 >= 221 && i1 <= 202 && j1 <= 232) {
          kab = !kab;
          flag = true;
        }
        if (i1 >= 191 && j1 >= 240 && i1 <= 202 && j1 <= 251) {
          lab = !lab;
          flag = true;
        }
        if (flag) {
          super.stream.beginFrame(200);
          super.stream.putInt8(iab ? 1 : 0);
          super.stream.putInt8(jab ? 1 : 0);
          super.stream.putInt8(kab ? 1 : 0);
          super.stream.putInt8(lab ? 1 : 0);
          super.stream.endFrame();
          gab = false;
          hab = false;
        }
        if (i1 >= 217 && j1 >= 238 && i1 <= 286 && j1 <= 259) {
          hab = true;
          super.stream.beginFrame(199);
          super.stream.endFrame();
        }
        if (i1 >= 394 && j1 >= 238 && i1 < 463 && j1 < 259) {
          yz = false;
          super.stream.beginFrame(203);
          super.stream.endFrame();
        }
      } else if (mt != 0) {
        yz = false;
        super.stream.beginFrame(203);
        super.stream.endFrame();
      }
      mt = 0;
      kbb = 0;
    }
    if (!yz)
      return;
    byte byte0 = 22;
    byte byte1 = 36;
    graphics.yf(byte0, byte1, 468, 12, 0xc90b1d);
    int i2 = 0x989898;
    graphics.uf(byte0, byte1 + 12, 468, 18, i2, 160);
    graphics.uf(byte0, byte1 + 30, 8, 248, i2, 160);
    graphics.uf(byte0 + 205, byte1 + 30, 11, 248, i2, 160);
    graphics.uf(byte0 + 462, byte1 + 30, 6, 248, i2, 160);
    graphics.uf(byte0 + 8, byte1 + 99, 197, 24, i2, 160);
    graphics.uf(byte0 + 8, byte1 + 192, 197, 23, i2, 160);
    graphics.uf(byte0 + 8, byte1 + 258, 197, 20, i2, 160);
    graphics.uf(byte0 + 216, byte1 + 235, 246, 43, i2, 160);
    int k2 = 0xd0d0d0;
    graphics.uf(byte0 + 8, byte1 + 30, 197, 69, k2, 160);
    graphics.uf(byte0 + 8, byte1 + 123, 197, 69, k2, 160);
    graphics.uf(byte0 + 8, byte1 + 215, 197, 43, k2, 160);
    graphics.uf(byte0 + 216, byte1 + 30, 246, 205, k2, 160);
    for (int j3 = 0; j3 < 3; j3++)
      graphics.rg(byte0 + 8, byte1 + 30 + j3 * 34, 197, 0);

    for (int j4 = 0; j4 < 3; j4++)
      graphics.rg(byte0 + 8, byte1 + 123 + j4 * 34, 197, 0);

    for (int l4 = 0; l4 < 7; l4++)
      graphics.rg(byte0 + 216, byte1 + 30 + l4 * 34, 246, 0);

    for (int k5 = 0; k5 < 6; k5++) {
      if (k5 < 5)
        graphics.vg(byte0 + 8 + k5 * 49, byte1 + 30, 69, 0);
      if (k5 < 5)
        graphics.vg(byte0 + 8 + k5 * 49, byte1 + 123, 69, 0);
      graphics.vg(byte0 + 216 + k5 * 49, byte1 + 30, 205, 0);
    }

    graphics.rg(byte0 + 8, byte1 + 215, 197, 0);
    graphics.rg(byte0 + 8, byte1 + 257, 197, 0);
    graphics.vg(byte0 + 8, byte1 + 215, 43, 0);
    graphics.vg(byte0 + 204, byte1 + 215, 43, 0);
    graphics.drawString("Preparing to duel with: " + zz, byte0 + 1, byte1 + 10,
        1, 0xffffff);
    graphics.drawString("Your Stake", byte0 + 9, byte1 + 27, 4, 0xffffff);
    graphics
        .drawString("Opponent's Stake", byte0 + 9, byte1 + 120, 4, 0xffffff);
    graphics.drawString("Duel Options", byte0 + 9, byte1 + 212, 4, 0xffffff);
    graphics.drawString("Your Inventory", byte0 + 216, byte1 + 27, 4, 0xffffff);
    graphics.drawString("No retreating", byte0 + 8 + 1, byte1 + 215 + 16, 3,
        0xffff00);
    graphics.drawString("No magic", byte0 + 8 + 1, byte1 + 215 + 35, 3,
        0xffff00);
    graphics.drawString("No prayer", byte0 + 8 + 102, byte1 + 215 + 16, 3,
        0xffff00);
    graphics.drawString("No weapons", byte0 + 8 + 102, byte1 + 215 + 35, 3,
        0xffff00);
    graphics.qf(byte0 + 93, byte1 + 215 + 6, 11, 11, 0xffff00);
    if (iab)
      graphics.yf(byte0 + 95, byte1 + 215 + 8, 7, 7, 0xffff00);
    graphics.qf(byte0 + 93, byte1 + 215 + 25, 11, 11, 0xffff00);
    if (jab)
      graphics.yf(byte0 + 95, byte1 + 215 + 27, 7, 7, 0xffff00);
    graphics.qf(byte0 + 191, byte1 + 215 + 6, 11, 11, 0xffff00);
    if (kab)
      graphics.yf(byte0 + 193, byte1 + 215 + 8, 7, 7, 0xffff00);
    graphics.qf(byte0 + 191, byte1 + 215 + 25, 11, 11, 0xffff00);
    if (lab)
      graphics.yf(byte0 + 193, byte1 + 215 + 27, 7, 7, 0xffff00);
    if (!hab)
      graphics.xg(byte0 + 217, byte1 + 238, hu + 25);
    graphics.xg(byte0 + 394, byte1 + 238, hu + 26);
    if (gab) {
      graphics.ug("Other player", byte0 + 341, byte1 + 246, 1, 0xffffff);
      graphics.ug("has accepted", byte0 + 341, byte1 + 256, 1, 0xffffff);
    }
    if (hab) {
      graphics.ug("Waiting for", byte0 + 217 + 35, byte1 + 246, 1, 0xffffff);
      graphics.ug("other player", byte0 + 217 + 35, byte1 + 256, 1, 0xffffff);
    }
    for (int l5 = 0; l5 < mx; l5++) {
      int i6 = 217 + byte0 + (l5 % 5) * 49;
      int k6 = 31 + byte1 + (l5 / 5) * 34;
      graphics.wf(i6, k6, 48, 32, ju + Config.njb[nx[l5]], Config.sjb[nx[l5]],
          0, 0, false);
      if (Config.pjb[nx[l5]] == 0)
        graphics.drawString(String.valueOf(ox[l5]), i6 + 1, k6 + 10, 1,
            0xffff00);
    }

    for (int j6 = 0; j6 < aab; j6++) {
      int l6 = 9 + byte0 + (j6 % 4) * 49;
      int j7 = 31 + byte1 + (j6 / 4) * 34;
      graphics.wf(l6, j7, 48, 32, ju + Config.njb[bab[j6]],
          Config.sjb[bab[j6]], 0, 0, false);
      if (Config.pjb[bab[j6]] == 0)
        graphics.drawString(String.valueOf(cab[j6]), l6 + 1, j7 + 10, 1,
            0xffff00);
      if (super.rq > l6 && super.rq < l6 + 48 && super.sq > j7
          && super.sq < j7 + 32)
        graphics.drawString(Config.kjb[bab[j6]] + ": @whi@"
            + Config.ljb[bab[j6]], byte0 + 8, byte1 + 273, 1, 0xffff00);
    }

    for (int i7 = 0; i7 < dab; i7++) {
      int k7 = 9 + byte0 + (i7 % 4) * 49;
      int l7 = 124 + byte1 + (i7 / 4) * 34;
      graphics.wf(k7, l7, 48, 32, ju + Config.njb[eab[i7]],
          Config.sjb[eab[i7]], 0, 0, false);
      if (Config.pjb[eab[i7]] == 0)
        graphics.drawString(String.valueOf(fab[i7]), k7 + 1, l7 + 10, 1,
            0xffff00);
      if (super.rq > k7 && super.rq < k7 + 48 && super.sq > l7
          && super.sq < l7 + 32)
        graphics.drawString(Config.kjb[eab[i7]] + ": @whi@"
            + Config.ljb[eab[i7]], byte0 + 8, byte1 + 273, 1, 0xffff00);
    }

  }

  public void tk() {
    if (kx == 0 && super.rq >= ((Graphics2D) (graphics)).yj - 35
        && super.sq >= 3 && super.rq < ((Graphics2D) (graphics)).yj - 3
        && super.sq < 35)
      kx = 1;
    if (kx == 0 && super.rq >= ((Graphics2D) (graphics)).yj - 35 - 33
        && super.sq >= 3 && super.rq < ((Graphics2D) (graphics)).yj - 3 - 33
        && super.sq < 35)
      kx = 2;
    if (kx == 0 && super.rq >= ((Graphics2D) (graphics)).yj - 35 - 66
        && super.sq >= 3 && super.rq < ((Graphics2D) (graphics)).yj - 3 - 66
        && super.sq < 35)
      kx = 3;
    if (kx == 0 && super.rq >= ((Graphics2D) (graphics)).yj - 35 - 99
        && super.sq >= 3 && super.rq < ((Graphics2D) (graphics)).yj - 3 - 99
        && super.sq < 35)
      kx = 4;
    if (kx == 0 && super.rq >= ((Graphics2D) (graphics)).yj - 35 - 132
        && super.sq >= 3 && super.rq < ((Graphics2D) (graphics)).yj - 3 - 132
        && super.sq < 35)
      kx = 5;
    if (kx == 0 && super.rq >= ((Graphics2D) (graphics)).yj - 35 - 165
        && super.sq >= 3 && super.rq < ((Graphics2D) (graphics)).yj - 3 - 165
        && super.sq < 35)
      kx = 6;
    if (kx != 0 && super.rq >= ((Graphics2D) (graphics)).yj - 35
        && super.sq >= 3 && super.rq < ((Graphics2D) (graphics)).yj - 3
        && super.sq < 26)
      kx = 1;
    if (kx != 0 && super.rq >= ((Graphics2D) (graphics)).yj - 35 - 33
        && super.sq >= 3 && super.rq < ((Graphics2D) (graphics)).yj - 3 - 33
        && super.sq < 26)
      kx = 2;
    if (kx != 0 && super.rq >= ((Graphics2D) (graphics)).yj - 35 - 66
        && super.sq >= 3 && super.rq < ((Graphics2D) (graphics)).yj - 3 - 66
        && super.sq < 26)
      kx = 3;
    if (kx != 0 && super.rq >= ((Graphics2D) (graphics)).yj - 35 - 99
        && super.sq >= 3 && super.rq < ((Graphics2D) (graphics)).yj - 3 - 99
        && super.sq < 26)
      kx = 4;
    if (kx != 0 && super.rq >= ((Graphics2D) (graphics)).yj - 35 - 132
        && super.sq >= 3 && super.rq < ((Graphics2D) (graphics)).yj - 3 - 132
        && super.sq < 26)
      kx = 5;
    if (kx != 0 && super.rq >= ((Graphics2D) (graphics)).yj - 35 - 165
        && super.sq >= 3 && super.rq < ((Graphics2D) (graphics)).yj - 3 - 165
        && super.sq < 26)
      kx = 6;
    if (kx == 1
        && (super.rq < ((Graphics2D) (graphics)).yj - 248 || super.sq > 36 + (lx / 5) * 34))
      kx = 0;
    if (kx == 3
        && (super.rq < ((Graphics2D) (graphics)).yj - 199 || super.sq > 294))
      kx = 0;
    if ((kx == 2 || kx == 4 || kx == 5)
        && (super.rq < ((Graphics2D) (graphics)).yj - 199 || super.sq > 240))
      kx = 0;
    if (kx == 6
        && (super.rq < ((Graphics2D) (graphics)).yj - 199 || super.sq > 311))
      kx = 0;
  }

  public void tl(boolean arg0) {
    int i1 = ((Graphics2D) (graphics)).yj - 248;
    graphics.xg(i1, 3, hu + 1);
    for (int j1 = 0; j1 < lx; j1++) {
      int k1 = i1 + (j1 % 5) * 49;
      int i2 = 36 + (j1 / 5) * 34;
      if (j1 < mx && px[j1] == 1)
        graphics.uf(k1, i2, 49, 34, 0xff0000, 128);
      else
        graphics.uf(k1, i2, 49, 34, Graphics2D.kg(181, 181, 181), 128);
      if (j1 < mx) {
        graphics.wf(k1, i2, 48, 32, ju + Config.njb[nx[j1]],
            Config.sjb[nx[j1]], 0, 0, false);
        if (Config.pjb[nx[j1]] == 0)
          graphics.drawString(String.valueOf(ox[j1]), k1 + 1, i2 + 10, 1,
              0xffff00);
      }
    }

    for (int l1 = 1; l1 <= 4; l1++)
      graphics.vg(i1 + l1 * 49, 36, (lx / 5) * 34, 0);

    for (int j2 = 1; j2 <= lx / 5 - 1; j2++)
      graphics.rg(i1, 36 + j2 * 34, 245, 0);

    if (!arg0)
      return;
    i1 = super.rq - (((Graphics2D) (graphics)).yj - 248);
    int k2 = super.sq - 36;
    if (i1 >= 0 && k2 >= 0 && i1 < 248 && k2 < (lx / 5) * 34) {
      int l2 = i1 / 49 + (k2 / 34) * 5;
      if (l2 < mx) {
        int i3 = nx[l2];
        if (fy >= 0) {
          if (Config.fmb[fy] == 3) {
            itemOptions[az] = "Cast " + Config.bmb[fy] + " on";
            cz[az] = "@lre@" + Config.kjb[i3];
            ez[az] = 600;
            hz[az] = l2;
            iz[az] = fy;
            az++;
            return;
          }
        } else {
          if (qx >= 0) {
            itemOptions[az] = "Use " + rx + " with";
            cz[az] = "@lre@" + Config.kjb[i3];
            ez[az] = 610;
            hz[az] = l2;
            iz[az] = qx;
            az++;
            return;
          }
          if (px[l2] == 1) {
            itemOptions[az] = "Remove";
            cz[az] = "@lre@" + Config.kjb[i3];
            ez[az] = 620;
            hz[az] = l2;
            az++;
          } else if (Config.rjb[i3] != 0) {
            if ((Config.rjb[i3] & 0x18) != 0)
              itemOptions[az] = "Wield";
            else
              itemOptions[az] = "Wear";
            cz[az] = "@lre@" + Config.kjb[i3];
            ez[az] = 630;
            hz[az] = l2;
            az++;
          }
          if (!Config.mjb[i3].equals("")) {
            itemOptions[az] = Config.mjb[i3];
            cz[az] = "@lre@" + Config.kjb[i3];
            ez[az] = 640;
            hz[az] = l2;
            az++;
          }
          itemOptions[az] = "Use";
          cz[az] = "@lre@" + Config.kjb[i3];
          ez[az] = 650;
          hz[az] = l2;
          az++;
          itemOptions[az] = "Drop";
          cz[az] = "@lre@" + Config.kjb[i3];
          ez[az] = 660;
          hz[az] = l2;
          az++;
          itemOptions[az] = "Examine";
          cz[az] = "@lre@" + Config.kjb[i3];
          ez[az] = 3600;
          hz[az] = i3;
          az++;
        }
      }
    }
  }

  public void ok(boolean arg0) {
    int i1 = ((Graphics2D) (graphics)).yj - 199;
    char c1 = '\234';
    char c3 = '\230';
    graphics.xg(i1 - 49, 3, hu + 2);
    i1 += 40;
    graphics.yf(i1, 36, c1, c3, 0);
    graphics.sf(i1, 36, i1 + c1, 36 + c3);
    char c5 = '\300';
    int k1 = ((bw.gr - 6040) * 3 * c5) / 2048;
    int i3 = ((bw.hr - 6040) * 3 * c5) / 2048;
    int k4 = Scene.mm[1024 - rv * 4 & 0x3ff];
    int i5 = Scene.mm[(1024 - rv * 4 & 0x3ff) + 1024];
    int k5 = i3 * k4 + k1 * i5 >> 18;
    i3 = i3 * i5 - k1 * k4 >> 18;
    k1 = k5;
    graphics.of((i1 + c1 / 2) - k1, 36 + c3 / 2 + i3, hu - 1, rv + 64 & 0xff,
        c5);
    for (int i7 = 0; i7 < uw; i7++) {
      int l1 = (((ww[i7] * cu + 64) - bw.gr) * 3 * c5) / 2048;
      int j3 = (((xw[i7] * cu + 64) - bw.hr) * 3 * c5) / 2048;
      int l5 = j3 * k4 + l1 * i5 >> 18;
      j3 = j3 * i5 - l1 * k4 >> 18;
      l1 = l5;
      zm(i1 + c1 / 2 + l1, (36 + c3 / 2) - j3, 65535);
    }

    for (int j7 = 0; j7 < ow; j7++) {
      int i2 = (((pw[j7] * cu + 64) - bw.gr) * 3 * c5) / 2048;
      int k3 = (((qw[j7] * cu + 64) - bw.hr) * 3 * c5) / 2048;
      int i6 = k3 * k4 + i2 * i5 >> 18;
      k3 = k3 * i5 - i2 * k4 >> 18;
      i2 = i6;
      zm(i1 + c1 / 2 + i2, (36 + c3 / 2) - k3, 0xff0000);
    }

    for (int k7 = 0; k7 < hw; k7++) {
      Mob l7 = kw[k7];
      int j2 = ((l7.gr - bw.gr) * 3 * c5) / 2048;
      int l3 = ((l7.hr - bw.hr) * 3 * c5) / 2048;
      int j6 = l3 * k4 + j2 * i5 >> 18;
      l3 = l3 * i5 - j2 * k4 >> 18;
      j2 = j6;
      zm(i1 + c1 / 2 + j2, (36 + c3 / 2) - l3, 0xffff00);
    }

    for (int i8 = 0; i8 < vv; i8++) {
      Mob l8 = zv[i8];
      int k2 = ((l8.gr - bw.gr) * 3 * c5) / 2048;
      int i4 = ((l8.hr - bw.hr) * 3 * c5) / 2048;
      int k6 = i4 * k4 + k2 * i5 >> 18;
      i4 = i4 * i5 - k2 * k4 >> 18;
      k2 = k6;
      int k8 = 0xffffff;
      for (int i9 = 0; i9 < super.gd; i9++) {
        if (l8.cr != super.hd[i9] || super.id[i9] != 99)
          continue;
        k8 = 65280;
        break;
      }

      zm(i1 + c1 / 2 + k2, (36 + c3 / 2) - i4, k8);
    }

    graphics.zf(i1 + c1 / 2, 36 + c3 / 2, 2, 0xffffff, 255);
    graphics.of(i1 + 19, 55, hu + 24, rv + 128 & 0xff, 128);
    graphics.sf(0, 0, eu, fu + 12);
    if (!arg0)
      return;
    i1 = super.rq - (((Graphics2D) (graphics)).yj - 199);
    int j8 = super.sq - 36;
    if (i1 >= 40 && j8 >= 0 && i1 < 196 && j8 < 152) {
      char c2 = '\234';
      char c4 = '\230';
      char c6 = '\300';
      int j1 = ((Graphics2D) (graphics)).yj - 199;
      j1 += 40;
      int l2 = ((super.rq - (j1 + c2 / 2)) * 16384) / (3 * c6);
      int j4 = ((super.sq - (36 + c4 / 2)) * 16384) / (3 * c6);
      int l4 = Scene.mm[1024 - rv * 4 & 0x3ff];
      int j5 = Scene.mm[(1024 - rv * 4 & 0x3ff) + 1024];
      int l6 = j4 * l4 + l2 * j5 >> 15;
      j4 = j4 * j5 - l2 * l4 >> 15;
      l2 = l6;
      l2 += bw.gr;
      j4 = bw.hr - j4;
      if (mt == 1)
        cl(cw, dw, l2 / 128, j4 / 128, false);
      mt = 0;
    }
  }

  public void ql(boolean arg0) {
    int i1 = ((Graphics2D) (graphics)).yj - 199;
    int j1 = 36;
    graphics.xg(i1 - 49, 3, hu + 3);
    char c1 = '\304';
    char c2 = '\u0107';
    int l1;
    int k1 = l1 = Graphics2D.kg(160, 160, 160);
    if (my == 0)
      k1 = Graphics2D.kg(220, 220, 220);
    else
      l1 = Graphics2D.kg(220, 220, 220);
    graphics.uf(i1, j1, c1 / 2, 24, k1, 128);
    graphics.uf(i1 + c1 / 2, j1, c1 / 2, 24, l1, 128);
    graphics.uf(i1, j1 + 24, c1, c2 - 24, Graphics2D.kg(220, 220, 220), 128);
    graphics.rg(i1, j1 + 24, c1, 0);
    graphics.vg(i1 + c1 / 2, j1, 24, 0);
    graphics.ug("Stats", i1 + c1 / 4, j1 + 16, 4, 0);
    graphics.ug("Quests", i1 + c1 / 4 + c1 / 2, j1 + 16, 4, 0);
    if (my == 0) {
      int i2 = 72;
      int k2 = -1;
      graphics.drawString("Skills", i1 + 5, i2, 3, 0xffff00);
      i2 += 13;
      for (int l2 = 0; l2 < 9; l2++) {
        int i3 = 0xffffff;
        if (super.rq > i1 + 3 && super.sq >= i2 - 11 && super.sq < i2 + 2
            && super.rq < i1 + 90) {
          i3 = 0xff0000;
          k2 = l2;
        }
        graphics.drawString(zx[l2] + ":@yel@" + ux[l2] + "/" + vx[l2], i1 + 5,
            i2, 1, i3);
        i3 = 0xffffff;
        if (super.rq >= i1 + 90 && super.sq >= i2 - 13 - 11
            && super.sq < (i2 - 13) + 2 && super.rq < i1 + 196) {
          i3 = 0xff0000;
          k2 = l2 + 9;
        }
        graphics.drawString(zx[l2 + 9] + ":@yel@" + ux[l2 + 9] + "/"
            + vx[l2 + 9], (i1 + c1 / 2) - 5, i2 - 13, 1, i3);
        i2 += 13;
      }

      graphics.drawString("Quest Points:@yel@" + yx, (i1 + c1 / 2) - 5,
          i2 - 13, 1, 0xffffff);
      i2 += 8;
      graphics.drawString("Equipment Status", i1 + 5, i2, 3, 0xffff00);
      i2 += 12;
      for (int j3 = 0; j3 < 3; j3++) {
        graphics
            .drawString(by[j3] + ":@yel@" + xx[j3], i1 + 5, i2, 1, 0xffffff);
        if (j3 < 2)
          graphics.drawString(by[j3 + 3] + ":@yel@" + xx[j3 + 3], i1 + c1 / 2
              + 25, i2, 1, 0xffffff);
        i2 += 13;
      }

      i2 += 6;
      graphics.rg(i1, i2 - 15, c1, 0);
      if (k2 != -1) {
        graphics.drawString(ay[k2] + " skill", i1 + 5, i2, 1, 0xffff00);
        i2 += 12;
        int k3 = sx[0];
        for (int i4 = 0; i4 < 98; i4++)
          if (wx[k2] >= sx[i4])
            k3 = sx[i4 + 1];

        graphics.drawString("Total xp: " + wx[k2] / 4, i1 + 5, i2, 1, 0xffffff);
        i2 += 12;
        graphics
            .drawString("Next level at: " + k3 / 4, i1 + 5, i2, 1, 0xffffff);
      } else {
        graphics.drawString("Overall levels", i1 + 5, i2, 1, 0xffff00);
        i2 += 12;
        int l3 = 0;
        for (int j4 = 0; j4 < 18; j4++)
          l3 += vx[j4];

        graphics.drawString("Skill total: " + l3, i1 + 5, i2, 1, 0xffffff);
        i2 += 12;
        graphics.drawString("Combat level: " + bw.zr, i1 + 5, i2, 1, 0xffffff);
        i2 += 12;
      }
    }
    if (my == 1) {
      ky.mc(ly);
      ky.dd(ly, 0, "@whi@Quest-list (green=completed)");
      for (int j2 = 0; j2 < ny; j2++)
        ky.dd(ly, j2 + 1, (py[j2] ? "@gre@" : "@red@") + quests[j2]);

      ky.hc();
    }
    if (!arg0)
      return;
    i1 = super.rq - (((Graphics2D) (graphics)).yj - 199);
    j1 = super.sq - 36;
    if (i1 >= 0 && j1 >= 0 && i1 < c1 && j1 < c2) {
      if (my == 1)
        ky.pd(i1 + (((Graphics2D) (graphics)).yj - 199), j1 + 36, super.uq,
            super.tq);
      if (j1 <= 24 && mt == 1) {
        if (i1 < 98) {
          my = 0;
          return;
        }
        if (i1 > 98)
          my = 1;
      }
    }
  }

  public void ym(boolean arg0) {
    int i1 = ((Graphics2D) (graphics)).yj - 199;
    int j1 = 36;
    graphics.xg(i1 - 49, 3, hu + 4);
    char c1 = '\304';
    char c2 = '\266';
    int l1;
    int k1 = l1 = Graphics2D.kg(160, 160, 160);
    if (ey == 0)
      k1 = Graphics2D.kg(220, 220, 220);
    else
      l1 = Graphics2D.kg(220, 220, 220);
    graphics.uf(i1, j1, c1 / 2, 24, k1, 128);
    graphics.uf(i1 + c1 / 2, j1, c1 / 2, 24, l1, 128);
    graphics.uf(i1, j1 + 24, c1, 90, Graphics2D.kg(220, 220, 220), 128);
    graphics.uf(i1, j1 + 24 + 90, c1, c2 - 90 - 24,
        Graphics2D.kg(160, 160, 160), 128);
    graphics.rg(i1, j1 + 24, c1, 0);
    graphics.vg(i1 + c1 / 2, j1, 24, 0);
    graphics.rg(i1, j1 + 113, c1, 0);
    graphics.ug("Magic", i1 + c1 / 4, j1 + 16, 4, 0);
    graphics.ug("Prayers", i1 + c1 / 4 + c1 / 2, j1 + 16, 4, 0);
    if (ey == 0) {
      cy.mc(dy);
      int i2 = 0;
      for (int i3 = 0; i3 < Config.amb; i3++) {
        String s1 = "@yel@";
        for (int l4 = 0; l4 < Config.emb[i3]; l4++) {
          int k5 = Config.gmb[i3][l4];
          if (nm(k5, Config.hmb[i3][l4]))
            continue;
          s1 = "@whi@";
          break;
        }

        int l5 = ux[6];
        if (Config.dmb[i3] > l5)
          s1 = "@bla@";
        cy.dd(dy, i2++, s1 + "Level " + Config.dmb[i3] + ": " + Config.bmb[i3]);
      }

      cy.hc();
      int i4 = cy.ic(dy);
      if (i4 != -1) {
        graphics.drawString("Level " + Config.dmb[i4] + ": " + Config.bmb[i4],
            i1 + 2, j1 + 124, 1, 0xffff00);
        graphics.drawString(Config.cmb[i4], i1 + 2, j1 + 136, 0, 0xffffff);
        for (int i5 = 0; i5 < Config.emb[i4]; i5++) {
          int i6 = Config.gmb[i4][i5];
          graphics.xg(i1 + 2 + i5 * 44, j1 + 150, ju + Config.njb[i6]);
          int j6 = gl(i6);
          int k6 = Config.hmb[i4][i5];
          String s3 = "@red@";
          if (nm(i6, k6))
            s3 = "@gre@";
          graphics.drawString(s3 + j6 + "/" + k6, i1 + 2 + i5 * 44, j1 + 150,
              1, 0xffffff);
        }

      } else {
        graphics.drawString("Point at a spell for a description", i1 + 2,
            j1 + 124, 1, 0);
      }
    }
    if (ey == 1) {
      cy.mc(dy);
      int j2 = 0;
      for (int j3 = 0; j3 < Config.imb; j3++) {
        String s2 = "@whi@";
        if (Config.lmb[j3] > vx[5])
          s2 = "@bla@";
        if (qy[j3])
          s2 = "@gre@";
        cy.dd(dy, j2++, s2 + "Level " + Config.lmb[j3] + ": " + Config.jmb[j3]);
      }

      cy.hc();
      int j4 = cy.ic(dy);
      if (j4 != -1) {
        graphics.ug("Level " + Config.lmb[j4] + ": " + Config.jmb[j4], i1 + c1
            / 2, j1 + 130, 1, 0xffff00);
        graphics.ug(Config.kmb[j4], i1 + c1 / 2, j1 + 145, 0, 0xffffff);
        graphics.ug("Drain rate: " + Config.mmb[j4], i1 + c1 / 2, j1 + 160, 1,
            0);
      } else {
        graphics.drawString("Point at a prayer for a description", i1 + 2,
            j1 + 124, 1, 0);
      }
    }
    if (!arg0)
      return;
    i1 = super.rq - (((Graphics2D) (graphics)).yj - 199);
    j1 = super.sq - 36;
    if (i1 >= 0 && j1 >= 0 && i1 < 196 && j1 < 182) {
      cy.pd(i1 + (((Graphics2D) (graphics)).yj - 199), j1 + 36, super.uq,
          super.tq);
      if (j1 <= 24 && mt == 1)
        if (i1 < 98 && ey == 1) {
          ey = 0;
          cy.zc(dy);
        } else if (i1 > 98 && ey == 0) {
          ey = 1;
          cy.zc(dy);
        }
      if (mt == 1 && ey == 0) {
        int k2 = cy.ic(dy);
        if (k2 != -1) {
          int k3 = ux[6];
          if (Config.dmb[k2] > k3) {
            appendMessage(
                "Your magic ability is not high enough for this spell", 3);
          } else {
            int k4;
            for (k4 = 0; k4 < Config.emb[k2]; k4++) {
              int j5 = Config.gmb[k2][k4];
              if (nm(j5, Config.hmb[k2][k4]))
                continue;
              appendMessage(
                  "You don't have all the reagents you need for this spell", 3);
              k4 = -1;
              break;
            }

            if (k4 == Config.emb[k2]) {
              fy = k2;
              qx = -1;
            }
          }
        }
      }
      if (mt == 1 && ey == 1) {
        int l2 = cy.ic(dy);
        if (l2 != -1) {
          int l3 = vx[5];
          if (Config.lmb[l2] > l3)
            appendMessage(
                "Your prayer ability is not high enough for this prayer", 3);
          else if (ux[5] == 0)
            appendMessage(
                "You have run out of prayer points. Return to a church to recharge",
                3);
          else if (qy[l2]) {
            super.stream.beginFrame(211);
            super.stream.putInt8(l2);
            super.stream.endFrame();
            qy[l2] = false;
            nk("prayeroff");
          } else {
            super.stream.beginFrame(212);
            super.stream.putInt8(l2);
            super.stream.endFrame();
            qy[l2] = true;
            nk("prayeron");
          }
        }
      }
      mt = 0;
    }
  }

  public void lk(boolean arg0) {
    int i1 = ((Graphics2D) (graphics)).yj - 199;
    int j1 = 36;
    graphics.xg(i1 - 49, 3, hu + 5);
    char c1 = '\304';
    char c2 = '\266';
    int l1;
    int k1 = l1 = Graphics2D.kg(160, 160, 160);
    if (iy == 0)
      k1 = Graphics2D.kg(220, 220, 220);
    else
      l1 = Graphics2D.kg(220, 220, 220);
    graphics.uf(i1, j1, c1 / 2, 24, k1, 128);
    graphics.uf(i1 + c1 / 2, j1, c1 / 2, 24, l1, 128);
    graphics.uf(i1, j1 + 24, c1, c2 - 24, Graphics2D.kg(220, 220, 220), 128);
    graphics.rg(i1, j1 + 24, c1, 0);
    graphics.vg(i1 + c1 / 2, j1, 24, 0);
    graphics.rg(i1, (j1 + c2) - 16, c1, 0);
    graphics.ug("Friends", i1 + c1 / 4, j1 + 16, 4, 0);
    graphics.ug("Ignore", i1 + c1 / 4 + c1 / 2, j1 + 16, 4, 0);
    gy.mc(hy);
    if (iy == 0) {
      for (int i2 = 0; i2 < super.gd; i2++) {
        String s1;
        if (super.id[i2] == 99)
          s1 = "@gre@";
        else if (super.id[i2] > 0)
          s1 = "@yel@";
        else
          s1 = "@red@";
        gy.dd(hy, i2, s1 + Util.decode37(super.hd[i2])
            + "~439~@whi@Remove         WWWWWWWWWW");
      }

    }
    if (iy == 1) {
      for (int j2 = 0; j2 < super.jd; j2++)
        gy.dd(hy, j2, "@yel@" + Util.decode37(super.kd[j2])
            + "~439~@whi@Remove         WWWWWWWWWW");

    }
    gy.hc();
    if (iy == 0) {
      int k2 = gy.ic(hy);
      if (k2 >= 0 && super.rq < 489) {
        if (super.rq > 429)
          graphics.ug("Click to remove " + Util.decode37(super.hd[k2]), i1 + c1
              / 2, j1 + 35, 1, 0xffffff);
        else if (super.id[k2] == 99)
          graphics.ug("Click to message " + Util.decode37(super.hd[k2]), i1
              + c1 / 2, j1 + 35, 1, 0xffffff);
        else if (super.id[k2] > 0)
          graphics.ug(Util.decode37(super.hd[k2]) + " is on world "
              + super.id[k2], i1 + c1 / 2, j1 + 35, 1, 0xffffff);
        else
          graphics.ug(Util.decode37(super.hd[k2]) + " is offline", i1 + c1 / 2,
              j1 + 35, 1, 0xffffff);
      } else {
        graphics.ug("Click a name to send a message", i1 + c1 / 2, j1 + 35, 1,
            0xffffff);
      }
      int k3;
      if (super.rq > i1 && super.rq < i1 + c1 && super.sq > (j1 + c2) - 16
          && super.sq < j1 + c2)
        k3 = 0xffff00;
      else
        k3 = 0xffffff;
      graphics.ug("Click here to add a friend", i1 + c1 / 2, (j1 + c2) - 3, 1,
          k3);
    }
    if (iy == 1) {
      int l2 = gy.ic(hy);
      if (l2 >= 0 && super.rq < 489 && super.rq > 429) {
        if (super.rq > 429)
          graphics.ug("Click to remove " + Util.decode37(super.kd[l2]), i1 + c1
              / 2, j1 + 35, 1, 0xffffff);
      } else {
        graphics.ug("Blocking messages from:", i1 + c1 / 2, j1 + 35, 1,
            0xffffff);
      }
      int l3;
      if (super.rq > i1 && super.rq < i1 + c1 && super.sq > (j1 + c2) - 16
          && super.sq < j1 + c2)
        l3 = 0xffff00;
      else
        l3 = 0xffffff;
      graphics
          .ug("Click here to add a name", i1 + c1 / 2, (j1 + c2) - 3, 1, l3);
    }
    if (!arg0)
      return;
    i1 = super.rq - (((Graphics2D) (graphics)).yj - 199);
    j1 = super.sq - 36;
    if (i1 >= 0 && j1 >= 0 && i1 < 196 && j1 < 182) {
      gy.pd(i1 + (((Graphics2D) (graphics)).yj - 199), j1 + 36, super.uq,
          super.tq);
      if (j1 <= 24 && mt == 1)
        if (i1 < 98 && iy == 1) {
          iy = 0;
          gy.zc(hy);
        } else if (i1 > 98 && iy == 0) {
          iy = 1;
          gy.zc(hy);
        }
      if (mt == 1 && iy == 0) {
        int i3 = gy.ic(hy);
        if (i3 >= 0 && super.rq < 489)
          if (super.rq > 429)
            removeFriend(super.hd[i3]);
          else if (super.id[i3] != 0) {
            rcb = 2;
            jy = super.hd[i3];
            super.ar = "";
            super.br = "";
          }
      }
      if (mt == 1 && iy == 1) {
        int j3 = gy.ic(hy);
        if (j3 >= 0 && super.rq < 489 && super.rq > 429)
          o(super.kd[j3]);
      }
      if (j1 > 166 && mt == 1 && iy == 0) {
        rcb = 1;
        super.yq = "";
        super.zq = "";
      }
      if (j1 > 166 && mt == 1 && iy == 1) {
        rcb = 3;
        super.yq = "";
        super.zq = "";
      }
      mt = 0;
    }
  }

  public void wm(boolean arg0) {
    int i1 = ((Graphics2D) (graphics)).yj - 199;
    int j1 = 36;
    graphics.xg(i1 - 49, 3, hu + 6);
    char c1 = '\304';
    graphics.uf(i1, 36, c1, 65, Graphics2D.kg(181, 181, 181), 160);
    graphics.uf(i1, 101, c1, 65, Graphics2D.kg(201, 201, 201), 160);
    graphics.uf(i1, 166, c1, 95, Graphics2D.kg(181, 181, 181), 160);
    graphics.uf(i1, 261, c1, 40, Graphics2D.kg(201, 201, 201), 160);
    int k1 = i1 + 3;
    int i2 = j1 + 15;
    graphics.drawString("Game options - click to toggle", k1, i2, 1, 0);
    i2 += 15;
    if (sy)
      graphics.drawString("Camera angle mode - @gre@Auto", k1, i2, 1, 0xffffff);
    else
      graphics.drawString("Camera angle mode - @red@Manual", k1, i2, 1,
          0xffffff);
    i2 += 15;
    if (ty)
      graphics.drawString("Mouse buttons - @red@One", k1, i2, 1, 0xffffff);
    else
      graphics.drawString("Mouse buttons - @gre@Two", k1, i2, 1, 0xffffff);
    i2 += 15;
    if (isMember)
      if (uy)
        graphics.drawString("Sound effects - @red@off", k1, i2, 1, 0xffffff);
      else
        graphics.drawString("Sound effects - @gre@on", k1, i2, 1, 0xffffff);
    i2 += 15;
    i2 += 5;
    graphics.drawString("Security settings", k1, i2, 1, 0);
    i2 += 15;
    int k2 = 0xffffff;
    if (super.rq > k1 && super.rq < k1 + c1 && super.sq > i2 - 12
        && super.sq < i2 + 4)
      k2 = 0xffff00;
    graphics.drawString("Change password", k1, i2, 1, k2);
    i2 += 15;
    k2 = 0xffffff;
    if (super.rq > k1 && super.rq < k1 + c1 && super.sq > i2 - 12
        && super.sq < i2 + 4)
      k2 = 0xffff00;
    graphics.drawString("Change recovery questions", k1, i2, 1, k2);
    i2 += 15;
    i2 += 15;
    i2 += 5;
    graphics.drawString("Privacy settings. Will be applied to", i1 + 3, i2, 1,
        0);
    i2 += 15;
    graphics
        .drawString("all people not on your friends list", i1 + 3, i2, 1, 0);
    i2 += 15;
    if (super.ld == 0)
      graphics.drawString("Block chat messages: @red@<off>", i1 + 3, i2, 1,
          0xffffff);
    else
      graphics.drawString("Block chat messages: @gre@<on>", i1 + 3, i2, 1,
          0xffffff);
    i2 += 15;
    if (super.md == 0)
      graphics.drawString("Block private messages: @red@<off>", i1 + 3, i2, 1,
          0xffffff);
    else
      graphics.drawString("Block private messages: @gre@<on>", i1 + 3, i2, 1,
          0xffffff);
    i2 += 15;
    if (super.nd == 0)
      graphics.drawString("Block trade requests: @red@<off>", i1 + 3, i2, 1,
          0xffffff);
    else
      graphics.drawString("Block trade requests: @gre@<on>", i1 + 3, i2, 1,
          0xffffff);
    i2 += 15;
    if (isMember)
      if (super.od == 0)
        graphics.drawString("Block duel requests: @red@<off>", i1 + 3, i2, 1,
            0xffffff);
      else
        graphics.drawString("Block duel requests: @gre@<on>", i1 + 3, i2, 1,
            0xffffff);
    i2 += 15;
    i2 += 5;
    graphics.drawString("Always logout when you finish", k1, i2, 1, 0);
    i2 += 15;
    k2 = 0xffffff;
    if (super.rq > k1 && super.rq < k1 + c1 && super.sq > i2 - 12
        && super.sq < i2 + 4)
      k2 = 0xffff00;
    graphics.drawString("Click here to logout", i1 + 3, i2, 1, k2);
    if (!arg0)
      return;
    i1 = super.rq - (((Graphics2D) (graphics)).yj - 199);
    j1 = super.sq - 36;
    if (i1 >= 0 && j1 >= 0 && i1 < 196 && j1 < 265) {
      int l2 = ((Graphics2D) (graphics)).yj - 199;
      byte byte0 = 36;
      char c2 = '\304';
      int l1 = l2 + 3;
      int j2 = byte0 + 30;
      if (super.rq > l1 && super.rq < l1 + c2 && super.sq > j2 - 12
          && super.sq < j2 + 4 && mt == 1) {
        sy = !sy;
        super.stream.beginFrame(213);
        super.stream.putInt8(0);
        super.stream.putInt8(sy ? 1 : 0);
        super.stream.endFrame();
      }
      j2 += 15;
      if (super.rq > l1 && super.rq < l1 + c2 && super.sq > j2 - 12
          && super.sq < j2 + 4 && mt == 1) {
        ty = !ty;
        super.stream.beginFrame(213);
        super.stream.putInt8(2);
        super.stream.putInt8(ty ? 1 : 0);
        super.stream.endFrame();
      }
      j2 += 15;
      if (isMember && super.rq > l1 && super.rq < l1 + c2 && super.sq > j2 - 12
          && super.sq < j2 + 4 && mt == 1) {
        uy = !uy;
        super.stream.beginFrame(213);
        super.stream.putInt8(3);
        super.stream.putInt8(uy ? 1 : 0);
        super.stream.endFrame();
      }
      j2 += 15;
      j2 += 20;
      if (super.rq > l1 && super.rq < l1 + c2 && super.sq > j2 - 12
          && super.sq < j2 + 4 && mt == 1) {
        scb = 6;
        super.yq = "";
        super.zq = "";
      }
      j2 += 15;
      if (super.rq > l1 && super.rq < l1 + c2 && super.sq > j2 - 12
          && super.sq < j2 + 4 && mt == 1) {
        super.stream.beginFrame(197);
        super.stream.endFrame();
      }
      j2 += 15;
      j2 += 15;
      boolean flag = false;
      j2 += 35;
      if (super.rq > l1 && super.rq < l1 + c2 && super.sq > j2 - 12
          && super.sq < j2 + 4 && mt == 1) {
        super.ld = 1 - super.ld;
        flag = true;
      }
      j2 += 15;
      if (super.rq > l1 && super.rq < l1 + c2 && super.sq > j2 - 12
          && super.sq < j2 + 4 && mt == 1) {
        super.md = 1 - super.md;
        flag = true;
      }
      j2 += 15;
      if (super.rq > l1 && super.rq < l1 + c2 && super.sq > j2 - 12
          && super.sq < j2 + 4 && mt == 1) {
        super.nd = 1 - super.nd;
        flag = true;
      }
      j2 += 15;
      if (isMember && super.rq > l1 && super.rq < l1 + c2 && super.sq > j2 - 12
          && super.sq < j2 + 4 && mt == 1) {
        super.od = 1 - super.od;
        flag = true;
      }
      j2 += 15;
      if (flag)
        h(super.ld, super.md, super.nd, super.od);
      j2 += 20;
      if (super.rq > l1 && super.rq < l1 + c2 && super.sq > j2 - 12
          && super.sq < j2 + 4 && mt == 1)
        logout();
      mt = 0;
    }
  }

  public void dl() {
    int i1 = -1;
    for (int j1 = 0; j1 < uw; j1++)
      bx[j1] = false;

    for (int k1 = 0; k1 < dx; k1++)
      jx[k1] = false;

    int l1 = zt.ri();
    Model ah[] = zt.oh();
    int ai[] = zt.mi();
    for (int i2 = 0; i2 < l1; i2++) {
      if (az > 200)
        break;
      int j2 = ai[i2];
      Model h1 = ah[i2];
      if (h1.vh[j2] <= 65535 || h1.vh[j2] >= 0x30d40 && h1.vh[j2] <= 0x493e0)
        if (h1 == zt.ao) {
          int l2 = h1.vh[j2] % 10000;
          int k3 = h1.vh[j2] / 10000;
          if (k3 == 1) {
            String s1 = "";
            int j4 = 0;
            if (bw.zr > 0 && zv[l2].zr > 0)
              j4 = bw.zr - zv[l2].zr;
            if (j4 < 0)
              s1 = "@or1@";
            if (j4 < -3)
              s1 = "@or2@";
            if (j4 < -6)
              s1 = "@or3@";
            if (j4 < -9)
              s1 = "@red@";
            if (j4 > 0)
              s1 = "@gr1@";
            if (j4 > 3)
              s1 = "@gr2@";
            if (j4 > 6)
              s1 = "@gr3@";
            if (j4 > 9)
              s1 = "@gre@";
            s1 = " " + s1 + "(level-" + zv[l2].zr + ")";
            if (fy >= 0) {
              if (Config.fmb[fy] == 1 || Config.fmb[fy] == 2 && dw < 2203) {
                itemOptions[az] = "Cast " + Config.bmb[fy] + " on";
                cz[az] = "@whi@" + zv[l2].dr;
                ez[az] = 800;
                fz[az] = zv[l2].gr;
                gz[az] = zv[l2].hr;
                hz[az] = zv[l2].er;
                iz[az] = fy;
                az++;
              }
            } else if (qx >= 0) {
              itemOptions[az] = "Use " + rx + " with";
              cz[az] = "@whi@" + zv[l2].dr;
              ez[az] = 810;
              fz[az] = zv[l2].gr;
              gz[az] = zv[l2].hr;
              hz[az] = zv[l2].er;
              iz[az] = qx;
              az++;
            } else {
              if (dw + yu + cv < 2203 && (zv[l2].hr - 64) / cu + yu + cv < 2203) {
                itemOptions[az] = "Attack";
                cz[az] = "@whi@" + zv[l2].dr + s1;
                if (j4 >= 0 && j4 < 5)
                  ez[az] = 805;
                else
                  ez[az] = 2805;
                fz[az] = zv[l2].gr;
                gz[az] = zv[l2].hr;
                hz[az] = zv[l2].er;
                az++;
              } else if (isMember) {
                itemOptions[az] = "Duel with";
                cz[az] = "@whi@" + zv[l2].dr + s1;
                fz[az] = zv[l2].gr;
                gz[az] = zv[l2].hr;
                ez[az] = 2806;
                hz[az] = zv[l2].er;
                az++;
              }
              itemOptions[az] = "Trade with";
              cz[az] = "@whi@" + zv[l2].dr;
              ez[az] = 2810;
              hz[az] = zv[l2].er;
              az++;
              itemOptions[az] = "Follow";
              cz[az] = "@whi@" + zv[l2].dr;
              ez[az] = 2820;
              hz[az] = zv[l2].er;
              az++;
            }
          } else if (k3 == 2) {
            if (fy >= 0) {
              if (Config.fmb[fy] == 3) {
                itemOptions[az] = "Cast " + Config.bmb[fy] + " on";
                cz[az] = "@lre@" + Config.kjb[rw[l2]];
                ez[az] = 200;
                fz[az] = pw[l2];
                gz[az] = qw[l2];
                hz[az] = rw[l2];
                iz[az] = fy;
                az++;
              }
            } else if (qx >= 0) {
              itemOptions[az] = "Use " + rx + " with";
              cz[az] = "@lre@" + Config.kjb[rw[l2]];
              ez[az] = 210;
              fz[az] = pw[l2];
              gz[az] = qw[l2];
              hz[az] = rw[l2];
              iz[az] = qx;
              az++;
            } else {
              itemOptions[az] = "Take";
              cz[az] = "@lre@" + Config.kjb[rw[l2]];
              ez[az] = 220;
              fz[az] = pw[l2];
              gz[az] = qw[l2];
              hz[az] = rw[l2];
              az++;
              itemOptions[az] = "Examine";
              cz[az] = "@lre@" + Config.kjb[rw[l2]];
              ez[az] = 3200;
              hz[az] = rw[l2];
              az++;
            }
          } else if (k3 == 3) {
            String s2 = "";
            int k4 = -1;
            int l4 = kw[l2].ir;
            if (Config.dkb[l4] > 0) {
              int i5 = (Config.zjb[l4] + Config.ckb[l4] + Config.akb[l4] + Config.bkb[l4]) / 4;
              int j5 = (vx[0] + vx[1] + vx[2] + vx[3] + 27) / 4;
              k4 = j5 - i5;
              s2 = "@yel@";
              if (k4 < 0)
                s2 = "@or1@";
              if (k4 < -3)
                s2 = "@or2@";
              if (k4 < -6)
                s2 = "@or3@";
              if (k4 < -9)
                s2 = "@red@";
              if (k4 > 0)
                s2 = "@gr1@";
              if (k4 > 3)
                s2 = "@gr2@";
              if (k4 > 6)
                s2 = "@gr3@";
              if (k4 > 9)
                s2 = "@gre@";
              s2 = " " + s2 + "(level-" + i5 + ")";
            }
            if (fy >= 0) {
              if (Config.fmb[fy] == 2) {
                itemOptions[az] = "Cast " + Config.bmb[fy] + " on";
                cz[az] = "@yel@" + Config.wjb[kw[l2].ir];
                ez[az] = 700;
                fz[az] = kw[l2].gr;
                gz[az] = kw[l2].hr;
                hz[az] = kw[l2].er;
                iz[az] = fy;
                az++;
              }
            } else if (qx >= 0) {
              itemOptions[az] = "Use " + rx + " with";
              cz[az] = "@yel@" + Config.wjb[kw[l2].ir];
              ez[az] = 710;
              fz[az] = kw[l2].gr;
              gz[az] = kw[l2].hr;
              hz[az] = kw[l2].er;
              iz[az] = qx;
              az++;
            } else {
              if (Config.dkb[l4] > 0) {
                itemOptions[az] = "Attack";
                cz[az] = "@yel@" + Config.wjb[kw[l2].ir] + s2;
                if (k4 >= 0)
                  ez[az] = 715;
                else
                  ez[az] = 2715;
                fz[az] = kw[l2].gr;
                gz[az] = kw[l2].hr;
                hz[az] = kw[l2].er;
                az++;
              }
              itemOptions[az] = "Talk-to";
              cz[az] = "@yel@" + Config.wjb[kw[l2].ir];
              ez[az] = 720;
              fz[az] = kw[l2].gr;
              gz[az] = kw[l2].hr;
              hz[az] = kw[l2].er;
              az++;
              if (!Config.yjb[l4].equals("")) {
                itemOptions[az] = Config.yjb[l4];
                cz[az] = "@yel@" + Config.wjb[kw[l2].ir];
                ez[az] = 725;
                fz[az] = kw[l2].gr;
                gz[az] = kw[l2].hr;
                hz[az] = kw[l2].er;
                az++;
              }
              itemOptions[az] = "Examine";
              cz[az] = "@yel@" + Config.wjb[kw[l2].ir];
              ez[az] = 3700;
              hz[az] = kw[l2].ir;
              az++;
            }
          }
        } else if (h1 != null && h1.uh >= 10000) {
          int i3 = h1.uh - 10000;
          int l3 = ix[i3];
          if (!jx[i3]) {
            if (fy >= 0) {
              if (Config.fmb[fy] == 4) {
                itemOptions[az] = "Cast " + Config.bmb[fy] + " on";
                cz[az] = "@cya@" + Config.jlb[l3];
                ez[az] = 300;
                fz[az] = fx[i3];
                gz[az] = gx[i3];
                hz[az] = hx[i3];
                iz[az] = fy;
                az++;
              }
            } else if (qx >= 0) {
              itemOptions[az] = "Use " + rx + " with";
              cz[az] = "@cya@" + Config.jlb[l3];
              ez[az] = 310;
              fz[az] = fx[i3];
              gz[az] = gx[i3];
              hz[az] = hx[i3];
              iz[az] = qx;
              az++;
            } else {
              if (!Config.llb[l3].equalsIgnoreCase("WalkTo")) {
                itemOptions[az] = Config.llb[l3];
                cz[az] = "@cya@" + Config.jlb[l3];
                ez[az] = 320;
                fz[az] = fx[i3];
                gz[az] = gx[i3];
                hz[az] = hx[i3];
                az++;
              }
              if (!Config.mlb[l3].equalsIgnoreCase("Examine")) {
                itemOptions[az] = Config.mlb[l3];
                cz[az] = "@cya@" + Config.jlb[l3];
                ez[az] = 2300;
                fz[az] = fx[i3];
                gz[az] = gx[i3];
                hz[az] = hx[i3];
                az++;
              }
              itemOptions[az] = "Examine";
              cz[az] = "@cya@" + Config.jlb[l3];
              ez[az] = 3300;
              hz[az] = l3;
              az++;
            }
            jx[i3] = true;
          }
        } else if (h1 != null && h1.uh >= 0) {
          int j3 = h1.uh;
          int i4 = yw[j3];
          if (!bx[j3]) {
            if (fy >= 0) {
              if (Config.fmb[fy] == 5) {
                itemOptions[az] = "Cast " + Config.bmb[fy] + " on";
                cz[az] = "@cya@" + Config.zkb[i4];
                ez[az] = 400;
                fz[az] = ww[j3];
                gz[az] = xw[j3];
                hz[az] = zw[j3];
                iz[az] = yw[j3];
                jz[az] = fy;
                az++;
              }
            } else if (qx >= 0) {
              itemOptions[az] = "Use " + rx + " with";
              cz[az] = "@cya@" + Config.zkb[i4];
              ez[az] = 410;
              fz[az] = ww[j3];
              gz[az] = xw[j3];
              hz[az] = zw[j3];
              iz[az] = yw[j3];
              jz[az] = qx;
              az++;
            } else {
              if (!Config.blb[i4].equalsIgnoreCase("WalkTo")) {
                itemOptions[az] = Config.blb[i4];
                cz[az] = "@cya@" + Config.zkb[i4];
                ez[az] = 420;
                fz[az] = ww[j3];
                gz[az] = xw[j3];
                hz[az] = zw[j3];
                iz[az] = yw[j3];
                az++;
              }
              if (!Config.clb[i4].equalsIgnoreCase("Examine")) {
                itemOptions[az] = Config.clb[i4];
                cz[az] = "@cya@" + Config.zkb[i4];
                ez[az] = 2400;
                fz[az] = ww[j3];
                gz[az] = xw[j3];
                hz[az] = zw[j3];
                iz[az] = yw[j3];
                az++;
              }
              itemOptions[az] = "Examine";
              cz[az] = "@cya@" + Config.zkb[i4];
              ez[az] = 3400;
              hz[az] = i4;
              az++;
            }
            bx[j3] = true;
          }
        } else {
          if (j2 >= 0)
            j2 = h1.vh[j2] - 0x30d40;
          if (j2 >= 0)
            i1 = j2;
        }
    }

    if (fy >= 0 && Config.fmb[fy] <= 1) {
      itemOptions[az] = "Cast " + Config.bmb[fy] + " on self";
      cz[az] = "";
      ez[az] = 1000;
      hz[az] = fy;
      az++;
    }
    if (i1 != -1) {
      int k2 = i1;
      if (fy >= 0) {
        if (Config.fmb[fy] == 6) {
          itemOptions[az] = "Cast " + Config.bmb[fy] + " on ground";
          cz[az] = "";
          ez[az] = 900;
          fz[az] = wu.xib[k2];
          gz[az] = wu.yib[k2];
          hz[az] = fy;
          az++;
          return;
        }
      } else if (qx < 0) {
        itemOptions[az] = "Walk here";
        cz[az] = "";
        ez[az] = 920;
        fz[az] = wu.xib[k2];
        gz[az] = wu.yib[k2];
        az++;
      }
    }
  }

  public void pm() {
    if (mt != 0) {
      for (int i1 = 0; i1 < az; i1++) {
        int k1 = wy + 2;
        int i2 = xy + 27 + i1 * 15;
        if (super.rq <= k1 - 2 || super.sq <= i2 - 12 || super.sq >= i2 + 4
            || super.rq >= (k1 - 3) + yy)
          continue;
        bm(kz[i1]);
        break;
      }

      mt = 0;
      vy = false;
      return;
    }
    if (super.rq < wy - 10 || super.sq < xy - 10 || super.rq > wy + yy + 10
        || super.sq > xy + zy + 10) {
      vy = false;
      return;
    }
    graphics.uf(wy, xy, yy, zy, 0xd0d0d0, 160);
    graphics.drawString("Choose option", wy + 2, xy + 12, 1, 65535);
    for (int j1 = 0; j1 < az; j1++) {
      int l1 = wy + 2;
      int j2 = xy + 27 + j1 * 15;
      int k2 = 0xffffff;
      if (super.rq > l1 - 2 && super.sq > j2 - 12 && super.sq < j2 + 4
          && super.rq < (l1 - 3) + yy)
        k2 = 0xffff00;
      graphics
          .drawString(itemOptions[kz[j1]] + " " + cz[kz[j1]], l1, j2, 1, k2);
    }

  }

  public void bl() {
    if (fy >= 0 || qx >= 0) {
      itemOptions[az] = "Cancel";
      cz[az] = "";
      ez[az] = 4000;
      az++;
    }
    for (int i1 = 0; i1 < az; i1++)
      kz[i1] = i1;

    for (boolean flag = false; !flag;) {
      flag = true;
      for (int j1 = 0; j1 < az - 1; j1++) {
        int l1 = kz[j1];
        int j2 = kz[j1 + 1];
        if (ez[l1] > ez[j2]) {
          kz[j1] = j2;
          kz[j1 + 1] = l1;
          flag = false;
        }
      }

    }

    if (az > 20)
      az = 20;
    if (az > 0) {
      int k1 = -1;
      for (int i2 = 0; i2 < az; i2++) {
        if (cz[kz[i2]] == null || cz[kz[i2]].length() <= 0)
          continue;
        k1 = i2;
        break;
      }

      String s1 = null;
      if ((qx >= 0 || fy >= 0) && az == 1)
        s1 = "Choose a target";
      else if ((qx >= 0 || fy >= 0) && az > 1)
        s1 = "@whi@" + itemOptions[kz[0]] + " " + cz[kz[0]];
      else if (k1 != -1)
        s1 = cz[kz[k1]] + ": @whi@" + itemOptions[kz[0]];
      if (az == 2 && s1 != null)
        s1 = s1 + "@whi@ / 1 more option";
      if (az > 2 && s1 != null)
        s1 = s1 + "@whi@ / " + (az - 1) + " more options";
      if (s1 != null)
        graphics.drawString(s1, 6, 14, 1, 0xffff00);
      if (!ty && mt == 1 || ty && mt == 1 && az == 1) {
        bm(kz[0]);
        mt = 0;
        return;
      }
      if (!ty && mt == 2 || ty && mt == 1) {
        zy = (az + 1) * 15;
        yy = graphics.df("Choose option", 1) + 5;
        for (int k2 = 0; k2 < az; k2++) {
          int l2 = graphics.df(itemOptions[k2] + " " + cz[k2], 1) + 5;
          if (l2 > yy)
            yy = l2;
        }

        wy = super.rq - yy / 2;
        xy = super.sq - 7;
        vy = true;
        if (wy < 0)
          wy = 0;
        if (xy < 0)
          xy = 0;
        if (wy + yy > 510)
          wy = 510 - yy;
        if (xy + zy > 315)
          xy = 315 - zy;
        mt = 0;
      }
    }
  }

  public void bm(int arg0) {
    int i1 = fz[arg0];
    int j1 = gz[arg0];
    int k1 = hz[arg0];
    int l1 = iz[arg0];
    int i2 = jz[arg0];
    int j2 = ez[arg0];
    if (j2 == 200) {
      im(cw, dw, i1, j1, true);
      super.stream.beginFrame(224);
      super.stream.putInt16(i1 + bv);
      super.stream.putInt16(j1 + cv);
      super.stream.putInt16(k1);
      super.stream.putInt16(l1);
      super.stream.endFrame();
      fy = -1;
    }
    if (j2 == 210) {
      im(cw, dw, i1, j1, true);
      super.stream.beginFrame(250);
      super.stream.putInt16(i1 + bv);
      super.stream.putInt16(j1 + cv);
      super.stream.putInt16(k1);
      super.stream.putInt16(l1);
      super.stream.endFrame();
      qx = -1;
    }
    if (j2 == 220) {
      im(cw, dw, i1, j1, true);
      super.stream.beginFrame(252);
      super.stream.putInt16(i1 + bv);
      super.stream.putInt16(j1 + cv);
      super.stream.putInt16(k1);
      super.stream.endFrame();
    }
    if (j2 == 3200)
      appendMessage(Config.ljb[k1], 3);
    if (j2 == 300) {
      zk(i1, j1, k1);
      super.stream.beginFrame(223);
      super.stream.putInt16(i1 + bv);
      super.stream.putInt16(j1 + cv);
      super.stream.putInt8(k1);
      super.stream.putInt16(l1);
      super.stream.endFrame();
      fy = -1;
    }
    if (j2 == 310) {
      zk(i1, j1, k1);
      super.stream.beginFrame(239);
      super.stream.putInt16(i1 + bv);
      super.stream.putInt16(j1 + cv);
      super.stream.putInt8(k1);
      super.stream.putInt16(l1);
      super.stream.endFrame();
      qx = -1;
    }
    if (j2 == 320) {
      zk(i1, j1, k1);
      super.stream.beginFrame(238);
      super.stream.putInt16(i1 + bv);
      super.stream.putInt16(j1 + cv);
      super.stream.putInt8(k1);
      super.stream.endFrame();
    }
    if (j2 == 2300) {
      zk(i1, j1, k1);
      super.stream.beginFrame(229);
      super.stream.putInt16(i1 + bv);
      super.stream.putInt16(j1 + cv);
      super.stream.putInt8(k1);
      super.stream.endFrame();
    }
    if (j2 == 3300)
      appendMessage(Config.klb[k1], 3);
    if (j2 == 400) {
      kk(i1, j1, k1, l1);
      super.stream.beginFrame(222);
      super.stream.putInt16(i1 + bv);
      super.stream.putInt16(j1 + cv);
      super.stream.putInt16(i2);
      super.stream.endFrame();
      fy = -1;
    }
    if (j2 == 410) {
      kk(i1, j1, k1, l1);
      super.stream.beginFrame(241);
      super.stream.putInt16(i1 + bv);
      super.stream.putInt16(j1 + cv);
      super.stream.putInt16(i2);
      super.stream.endFrame();
      qx = -1;
    }
    if (j2 == 420) {
      kk(i1, j1, k1, l1);
      super.stream.beginFrame(242);
      super.stream.putInt16(i1 + bv);
      super.stream.putInt16(j1 + cv);
      super.stream.endFrame();
    }
    if (j2 == 2400) {
      kk(i1, j1, k1, l1);
      super.stream.beginFrame(230);
      super.stream.putInt16(i1 + bv);
      super.stream.putInt16(j1 + cv);
      super.stream.endFrame();
    }
    if (j2 == 3400)
      appendMessage(Config.alb[k1], 3);
    if (j2 == 600) {
      super.stream.beginFrame(220);
      super.stream.putInt16(k1);
      super.stream.putInt16(l1);
      super.stream.endFrame();
      fy = -1;
    }
    if (j2 == 610) {
      super.stream.beginFrame(240);
      super.stream.putInt16(k1);
      super.stream.putInt16(l1);
      super.stream.endFrame();
      qx = -1;
    }
    if (j2 == 620) {
      super.stream.beginFrame(248);
      super.stream.putInt16(k1);
      super.stream.endFrame();
    }
    if (j2 == 630) {
      super.stream.beginFrame(249);
      super.stream.putInt16(k1);
      super.stream.endFrame();
    }
    if (j2 == 640) {
      super.stream.beginFrame(246);
      super.stream.putInt16(k1);
      super.stream.endFrame();
    }
    if (j2 == 650) {
      qx = k1;
      kx = 0;
      rx = Config.kjb[nx[qx]];
    }
    if (j2 == 660) {
      super.stream.beginFrame(251);
      super.stream.putInt16(k1);
      super.stream.endFrame();
      qx = -1;
      kx = 0;
      appendMessage("Dropping " + Config.kjb[nx[k1]], 4);
    }
    if (j2 == 3600)
      appendMessage(Config.ljb[k1], 3);
    if (j2 == 700) {
      int k2 = (i1 - 64) / cu;
      int k4 = (j1 - 64) / cu;
      cl(cw, dw, k2, k4, true);
      super.stream.beginFrame(225);
      super.stream.putInt16(k1);
      super.stream.putInt16(l1);
      super.stream.endFrame();
      fy = -1;
    }
    if (j2 == 710) {
      int l2 = (i1 - 64) / cu;
      int l4 = (j1 - 64) / cu;
      cl(cw, dw, l2, l4, true);
      super.stream.beginFrame(243);
      super.stream.putInt16(k1);
      super.stream.putInt16(l1);
      super.stream.endFrame();
      qx = -1;
    }
    if (j2 == 720) {
      int i3 = (i1 - 64) / cu;
      int i5 = (j1 - 64) / cu;
      cl(cw, dw, i3, i5, true);
      super.stream.beginFrame(245);
      super.stream.putInt16(k1);
      super.stream.endFrame();
    }
    if (j2 == 725) {
      int j3 = (i1 - 64) / cu;
      int j5 = (j1 - 64) / cu;
      cl(cw, dw, j3, j5, true);
      super.stream.beginFrame(195);
      super.stream.putInt16(k1);
      super.stream.endFrame();
    }
    if (j2 == 715 || j2 == 2715) {
      int k3 = (i1 - 64) / cu;
      int k5 = (j1 - 64) / cu;
      cl(cw, dw, k3, k5, true);
      super.stream.beginFrame(244);
      super.stream.putInt16(k1);
      super.stream.endFrame();
    }
    if (j2 == 3700)
      appendMessage(Config.xjb[k1], 3);
    if (j2 == 800) {
      int l3 = (i1 - 64) / cu;
      int l5 = (j1 - 64) / cu;
      cl(cw, dw, l3, l5, true);
      super.stream.beginFrame(226);
      super.stream.putInt16(k1);
      super.stream.putInt16(l1);
      super.stream.endFrame();
      fy = -1;
    }
    if (j2 == 810) {
      int i4 = (i1 - 64) / cu;
      int i6 = (j1 - 64) / cu;
      cl(cw, dw, i4, i6, true);
      super.stream.beginFrame(219);
      super.stream.putInt16(k1);
      super.stream.putInt16(l1);
      super.stream.endFrame();
      qx = -1;
    }
    if (j2 == 805 || j2 == 2805) {
      int j4 = (i1 - 64) / cu;
      int j6 = (j1 - 64) / cu;
      cl(cw, dw, j4, j6, true);
      super.stream.beginFrame(228);
      super.stream.putInt16(k1);
      super.stream.endFrame();
    }
    if (j2 == 2806) {
      super.stream.beginFrame(204);
      super.stream.putInt16(k1);
      super.stream.endFrame();
    }
    if (j2 == 2810) {
      super.stream.beginFrame(235);
      super.stream.putInt16(k1);
      super.stream.endFrame();
    }
    if (j2 == 2820) {
      super.stream.beginFrame(214);
      super.stream.putInt16(k1);
      super.stream.endFrame();
    }
    if (j2 == 900) {
      cl(cw, dw, i1, j1, true);
      super.stream.beginFrame(221);
      super.stream.putInt16(i1 + bv);
      super.stream.putInt16(j1 + cv);
      super.stream.putInt16(k1);
      super.stream.endFrame();
      fy = -1;
    }
    if (j2 == 920) {
      cl(cw, dw, i1, j1, false);
      if (tu == -24)
        tu = 24;
    }
    if (j2 == 1000) {
      super.stream.beginFrame(227);
      super.stream.putInt16(k1);
      super.stream.endFrame();
      fy = -1;
    }
    if (j2 == 4000) {
      qx = -1;
      fy = -1;
    }
  }

  public Client() {
    isMember = false;
    et = new BigInteger("18439792161837834709");
    ft = new BigInteger("192956484481579778191558061814292671521");
    invalidHost = false;
    outOfMemory = false;
    jt = true;
    lt = 0xbc614e;
    nt = 8000;
    ot = new int[nt];
    pt = new int[nt];
    rt = new int[8192];
    st = new int[8192];
    ut = 2;
    wt = 2;
    cu = 128;
    eu = 512;
    fu = 334;
    gu = 9;
    nu = 40;
    ru = -1;
    su = -1;
    av = -1;
    dv = -1;
    lv = 550;
    mv = false;
    pv = 1;
    rv = 128;
    tv = 4000;
    uv = 500;
    yv = new Mob[tv];
    zv = new Mob[uv];
    aw = new Mob[uv];
    bw = new Mob();
    ew = -1;
    fw = 2500;
    gw = 500;
    jw = new Mob[fw];
    kw = new Mob[gw];
    lw = new Mob[gw];
    mw = new int[500];
    nw = 500;
    pw = new int[nw];
    qw = new int[nw];
    rw = new int[nw];
    sw = new int[nw];
    tw = 1500;
    vw = new Model[tw];
    ww = new int[tw];
    xw = new int[tw];
    yw = new int[tw];
    zw = new int[tw];
    loadedModels = new Model[200];
    bx = new boolean[tw];
    cx = 500;
    ex = new Model[cx];
    fx = new int[cx];
    gx = new int[cx];
    hx = new int[cx];
    ix = new int[cx];
    jx = new boolean[cx];
    lx = 30;
    nx = new int[35];
    ox = new int[35];
    px = new int[35];
    qx = -1;
    rx = "";
    sx = new int[99];
    ux = new int[18];
    vx = new int[18];
    wx = new int[18];
    xx = new int[5];
    fy = -1;
    ny = 25;
    py = new boolean[ny];
    qy = new boolean[50];
    ry = false;
    sy = true;
    ty = false;
    uy = false;
    vy = false;
    bz = 250;
    cz = new String[bz];
    itemOptions = new String[bz];
    ez = new int[bz];
    fz = new int[bz];
    gz = new int[bz];
    hz = new int[bz];
    iz = new int[bz];
    jz = new int[bz];
    kz = new int[bz];
    vz = 5;
    wz = new String[vz];
    xz = new int[vz];
    yz = false;
    zz = "";
    bab = new int[8];
    cab = new int[8];
    eab = new int[8];
    fab = new int[8];
    gab = false;
    hab = false;
    iab = false;
    jab = false;
    kab = false;
    lab = false;
    mab = false;
    nab = false;
    qab = new int[8];
    rab = new int[8];
    tab = new int[8];
    uab = new int[8];
    zab = false;
    abb = "";
    cbb = new int[14];
    dbb = new int[14];
    fbb = new int[14];
    gbb = new int[14];
    hbb = false;
    ibb = false;
    mbb = false;
    nbb = false;
    pbb = new int[14];
    qbb = new int[14];
    sbb = new int[14];
    tbb = new int[14];
    ubb = false;
    xbb = new int[256];
    ybb = new int[256];
    zbb = new int[256];
    acb = -1;
    bcb = -2;
    ccb = false;
    ecb = new int[256];
    fcb = new int[256];
    hcb = new int[256];
    icb = new int[256];
    jcb = -1;
    kcb = -2;
    lcb = 48;
    ncb = false;
    pcb = new String[5];
    tcb = "";
    ucb = "";
    vcb = false;
    wcb = false;
    cdb = false;
    ddb = "";
    idb = false;
    feb = "";
    geb = "";
    username = "";
    ieb = "";
    zeb = false;
    dfb = -1;
    efb = new int[5];
    ffb = new int[5];
    gfb = new int[5];
    hfb = new int[5];
    jfb = new String[5];
    kfb = false;
    wfb = new int[5];
    xfb = new int[5];
    zfb = new String[50];
    agb = new int[50];
    bgb = new int[50];
    cgb = new int[50];
    dgb = new int[50];
    fgb = new int[50];
    ggb = new int[50];
    hgb = new int[50];
    igb = new int[50];
    kgb = new int[50];
    lgb = new int[50];
    mgb = new int[50];
    qgb = false;
    rgb = false;
    tgb = 1;
    ugb = 2;
    vgb = 2;
    wgb = 8;
    xgb = 14;
    zgb = 1;
    jhb = new int[50];
    khb = new int[50];
    lhb = new int[50];
    mhb = new int[50];
  }

  public boolean isMember;
  public static String cacheDirectory = "";
  public BigInteger et;
  public BigInteger ft;
  int gt;
  boolean invalidHost;
  boolean outOfMemory;
  public boolean jt;
  int kt;
  int lt;
  int mt;
  int nt;
  int ot[];
  int pt[];
  int qt;
  int rt[];
  int st[];
  int tt;
  int ut;
  int vt;
  int wt;
  int xt;
  Graphics yt;
  Scene zt;
  m graphics;
  int bu;
  int cu;
  int du;
  int eu;
  int fu;
  int gu;
  int hu;
  int iu;
  int ju;
  int ku;
  int lu;
  int mu;
  int nu;
  int ou;
  int pu;
  int qu;
  int ru;
  int su;
  int tu;
  int uu;
  int vu;
  World wu;
  int xu;
  int yu;
  int zu;
  int av;
  int bv;
  int cv;
  int dv;
  int ev;
  int fv;
  int gv;
  int hv;
  int iv;
  int jv;
  int kv;
  int lv;
  boolean mv;
  int nv;
  int ov;
  int pv;
  int qv;
  int rv;
  int sv;
  int tv;
  int uv;
  int vv;
  int wv;
  int xv;
  Mob yv[];
  Mob zv[];
  Mob aw[];
  Mob bw;
  int cw;
  int dw;
  int ew;
  int fw;
  int gw;
  int hw;
  int iw;
  Mob jw[];
  Mob kw[];
  Mob lw[];
  int mw[];
  int nw;
  int ow;
  int pw[];
  int qw[];
  int rw[];
  int sw[];
  int tw;
  int uw;
  Model vw[];
  int ww[];
  int xw[];
  int yw[];
  int zw[];
  Model loadedModels[];
  boolean bx[];
  int cx;
  int dx;
  Model ex[];
  int fx[];
  int gx[];
  int hx[];
  int ix[];
  boolean jx[];
  int kx;
  int lx;
  int mx;
  int nx[];
  int ox[];
  int px[];
  int qx;
  String rx;
  int sx[];
  final int tx = 18;
  int ux[];
  int vx[];
  int wx[];
  int xx[];
  int yx;
  String zx[] = { "Attack", "Defense", "Strength", "Hits", "Ranged", "Prayer",
      "Magic", "Cooking", "Woodcut", "Fletching", "Fishing", "Firemaking",
      "Crafting", "Smithing", "Mining", "Herblaw", "Carpentry", "Thieving" };
  String ay[] = { "Attack", "Defense", "Strength", "Hits", "Ranged", "Prayer",
      "Magic", "Cooking", "Woodcutting", "Fletching", "Fishing", "Firemaking",
      "Crafting", "Smithing", "Mining", "Herblaw", "Carpentry", "Thieving" };
  String by[] = { "Armour", "WeaponAim", "WeaponPower", "Magic", "Prayer" };
  Menu cy;
  int dy;
  int ey;
  int fy;
  Menu gy;
  int hy;
  int iy;
  long jy;
  Menu ky;
  int ly;
  int my;
  int ny;
  String quests[] = { "Black knight's fortress", "Cook's assistant",
      "Demon slayer", "Doric's quest", "The restless ghost",
      "Goblin diplomacy", "Ernest the chicken", "Imp catcher",
      "Pirate's treasure", "Prince Ali rescue", "Romeo & Juliet",
      "Sheep shearer", "Shield of Arrav", "The knight's sword",
      "Vampire slayer", "Witch's potion", "Dragon slayer",
      "Witch's house (members)", "Lost city (members)",
      "Hero's quest (members)", "Druidic ritual (members)",
      "Merlin's crystal (members)", "Scorpion catcher (members)",
      "Family crest (members)", "Tribal totem (members)" };
  boolean py[];
  boolean qy[];
  boolean ry;
  boolean sy;
  boolean ty;
  boolean uy;
  boolean vy;
  int wy;
  int xy;
  int yy;
  int zy;
  int az;
  int bz;
  String cz[];
  String itemOptions[];
  int ez[];
  int fz[];
  int gz[];
  int hz[];
  int iz[];
  int jz[];
  int kz[];
  int lz;
  int mz;
  int nz;
  int oz;
  Menu pz;
  int qz;
  int rz;
  int sz;
  int tz;
  int uz;
  int vz;
  String wz[];
  int xz[];
  boolean yz;
  String zz;
  int aab;
  int bab[];
  int cab[];
  int dab;
  int eab[];
  int fab[];
  boolean gab;
  boolean hab;
  boolean iab;
  boolean jab;
  boolean kab;
  boolean lab;
  boolean mab;
  boolean nab;
  long oab;
  int pab;
  int qab[];
  int rab[];
  int sab;
  int tab[];
  int uab[];
  int vab;
  int wab;
  int xab;
  int yab;
  boolean zab;
  String abb;
  int bbb;
  int cbb[];
  int dbb[];
  int ebb;
  int fbb[];
  int gbb[];
  boolean hbb;
  boolean ibb;
  int jbb;
  int kbb;
  long lbb;
  boolean mbb;
  boolean nbb;
  int obb;
  int pbb[];
  int qbb[];
  int rbb;
  int sbb[];
  int tbb[];
  boolean ubb;
  int vbb;
  int wbb;
  int xbb[];
  int ybb[];
  int zbb[];
  int acb;
  int bcb;
  boolean ccb;
  int dcb;
  int ecb[];
  int fcb[];
  int gcb;
  int hcb[];
  int icb[];
  int jcb;
  int kcb;
  int lcb;
  int mcb;
  boolean ncb;
  int ocb;
  String pcb[];
  int qcb;
  int rcb;
  int scb;
  String tcb;
  String ucb;
  boolean vcb;
  boolean wcb;
  int playerIP;
  String lastIP;
  int daysSinceLogin;
  int adb;
  int bdb;
  boolean cdb;
  String ddb;
  int edb;
  int fdb;
  int gdb;
  int hdb;
  boolean idb;
  int jdb;
  Menu kdb;
  int ldb;
  int mdb;
  Menu ndb;
  int odb;
  int pdb;
  int qdb;
  int rdb;
  int sdb;
  int tdb;
  int udb;
  int vdb;
  Menu wdb;
  int xdb;
  int ydb;
  int zdb;
  int aeb;
  int beb;
  int ceb;
  int deb;
  int eeb;
  String feb;
  String geb;
  String username;
  String ieb;
  Menu design;
  int keb;
  int leb;
  int meb;
  int neb;
  int oeb;
  int peb;
  int qeb;
  int reb;
  int seb;
  int teb;
  int ueb;
  int veb;
  int web;
  int xeb;
  int yeb;
  boolean zeb;
  Menu afb;
  int bfb;
  int cfb;
  int dfb;
  int efb[];
  int ffb[];
  int gfb[];
  int hfb[];
  int ifb[] = { 0, 1, 2, 3, 4 };
  String jfb[];
  boolean kfb;
  Menu lfb;
  int mfb;
  int nfb;
  int previousPassword;
  int newPassword;
  int confirmation;
  int rfb;
  int sfb;
  int tfb;
  int ufb;
  int vfb;
  int wfb[];
  int xfb[];
  int yfb;
  String zfb[];
  int agb[];
  int bgb[];
  int cgb[];
  int dgb[];
  int egb;
  int fgb[];
  int ggb[];
  int hgb[];
  int igb[];
  int jgb;
  int kgb[];
  int lgb[];
  int mgb[];
  int ngb;
  int ogb;
  int pgb[][] = { { 11, 2, 9, 7, 1, 6, 10, 0, 5, 8, 3, 4 },
      { 11, 2, 9, 7, 1, 6, 10, 0, 5, 8, 3, 4 },
      { 11, 3, 2, 9, 7, 1, 6, 10, 0, 5, 8, 4 },
      { 3, 4, 2, 9, 7, 1, 6, 10, 8, 11, 0, 5 },
      { 3, 4, 2, 9, 7, 1, 6, 10, 8, 11, 0, 5 },
      { 4, 3, 2, 9, 7, 1, 6, 10, 8, 11, 0, 5 },
      { 11, 4, 2, 9, 7, 1, 6, 10, 0, 5, 8, 3 },
      { 11, 2, 9, 7, 1, 6, 10, 0, 5, 8, 4, 3 } };
  boolean qgb;
  boolean rgb;
  int sgb;
  int tgb;
  int ugb;
  int vgb;
  int wgb;
  int xgb;
  int ygb;
  int zgb;
  public int ahb[] = { 0xff0000, 0xff8000, 0xffe000, 0xa0e000, 57344, 32768,
      41088, 45311, 33023, 12528, 0xe000e0, 0x303030, 0x604000, 0x805000,
      0xffffff };
  public int bhb[] = { 0xffc030, 0xffa040, 0x805030, 0x604020, 0x303030,
      0xff6020, 0xff4000, 0xffffff, 65280, 65535 };
  public int chb[] = { 0xecded0, 0xccb366, 0xb38c40, 0x997326, 0x906020 };
  int dhb[] = { 0, 1, 2, 1 };
  int ehb[] = { 0, 1, 2, 1, 0, 0, 0, 0 };
  int fhb[] = { 0, 0, 0, 0, 0, 1, 2, 1 };
  byte sounds[];
  AudioInputStream audio;
  int ihb;
  int jhb[];
  int khb[];
  int lhb[];
  int mhb[];
  String questions[] = { "Where were you born?",
      "What was your first teacher's name?",
      "What is your father's middle name?", "Who was your first best friend?",
      "What is your favourite vacation spot?",
      "What is your mother's middle name?", "What was your first pet's name?",
      "What was the name of your first school?",
      "What is your mother's maiden name?",
      "Who was your first boyfriend/girlfriend?",
      "What was the first computer game you purchased?",
      "Who is your favourite actor/actress?", "Who is your favourite author?",
      "Who is your favourite musician?",
      "Who is your favourite cartoon character?",
      "What is your favourite book?", "What is your favourite food?",
      "What is your favourite movie?" };
}
