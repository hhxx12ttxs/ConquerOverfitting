import jagex.util;
import jagex.client.sound;
import jagex.client.connection;
import jagex.client.networkedgame;
import jagex.client.menu;
import jagex.client.model;
import jagex.client.graphics;
import jagex.client.camera;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;

@SuppressWarnings("serial")
public class mudclient extends networkedgame {

	public static void main(String[] args) {
		mudclient client = new mudclient();
		client.live = false;
		if(args.length > 0 && args[0].equals("member"))
			client.member = true;
		cachedir = "release/";
		client.mkwin(client.width, client.height + 11, "Runescape by Andrew Gower", false);
		client.qq = 10;
	}

	public void load() {
		if (live) {
			String host = getDocumentBase().getHost().toLowerCase();
			if (!host.endsWith("jagex.com") && !host.endsWith("jagex.co.uk") && !host.endsWith("runescape.com") && !host.endsWith("runescape.co.uk") && !host.endsWith("runescape.net") && !host.endsWith("runescape.org") && !host.endsWith("penguin") && !host.endsWith("puffin")) {
				invalidhost = true;
				return;
			}
		}
		setrsakeys(exponent, modulus);

		int totalxp = 0;
		for (int level = 0; level < 99; level++) {
			int nextlevel = level + 1;
			int xp = (int)((double) nextlevel + 300D * Math.pow(2D, (double) nextlevel / 7D));
			totalxp += xp;
			experience[level] = totalxp & 0xffffffc;
		}

		try {
			String memstr = getParameter("member");
			int memint = Integer.parseInt(memstr);
			if(memint == 1)
				member = true;
		} catch(Exception e) { }

		if (live) {
			port = 43594;
		}

		mouseoffset = 0;
		networkedgame.packetread = 1000;
		networkedgame.clientver = version.MINOR_VERSION;

		try {
			String poffstr = getParameter("poff");
			int offset = Integer.parseInt(poffstr);
			port += offset;
			System.out.println("Offset: " + offset);
		} catch(Exception e) { }

		loadConfig();
		hu = 2000;
		iu = hu + 100;
		ju = iu + 50;
		ku = ju + 300;
		lu = 2510;
		mu = lu + 10;
		gfx = getGraphics();
		setRefreshRate(50);
		gamegfx = new gamegraphics(width, height + 12, 2700, this);
		gamegfx.client = this;
		gamegfx.setrend(0, 0, width, height + 12);
		menu.ig = false;
		menu.jg = iu;
		cy = new menu(gamegfx, 5);
		int l1 = ((graphics) (gamegfx)).width - 199;
		byte byte0 = 36;
		dy = cy.qc(l1, byte0 + 24, 196, 90, 1, 500, true);
		gy = new menu(gamegfx, 5);
		hy = gy.qc(l1, byte0 + 40, 196, 126, 1, 500, true);
		ky = new menu(gamegfx, 5);
		ly = ky.qc(l1, byte0 + 24, 196, 226, 1, 500, true);
		loadMedia();
		km();
		camera = new camera(gamegfx, 15000, 15000, 1000);
		camera.ei(width / 2, height / 2, width / 2, height / 2, width, gu);
		camera.im = 2400;
		camera.jm = 2400;
		camera.km = 1;
		camera.lm = 2300;
		camera.di(-50, -10, -50);
		landscape = new world(camera, gamegfx);
		landscape.bib = hu;
		loadTextures();
		loadModels();
		loadLandscape();
		if (member)
			loadSounds();
		showpercent(100, "Starting game...");
		kl();
		uk();
		ul();
		el();
		qk();
		onpaint();
		vl();
	}

	public void loadConfig() {
		byte arc[] = null;
		try {
			arc = loadfile(cachedir + "config" + version.CONFIG_VERSION + ".jag", "Configuration", 10);
		} catch(IOException e) {
			System.out.println("Load error:" + e);
		}
		cache.load(arc, member);
	}

	public void loadMedia() {
		byte arc[] = null;
		try {
			arc = loadfile(cachedir + "media" + version.MEDIA_VERSION + ".jag", "2d graphics", 20);
		} catch(IOException e) {
			System.out.println("Load error:" + e);
		}
		byte abyte1[] = util.getf("index.dat", 0, arc);
		gamegfx.og(hu, util.getf("inv1.dat", 0, arc), abyte1, 1);
		gamegfx.og(hu + 1, util.getf("inv2.dat", 0, arc), abyte1, 6);
		gamegfx.og(hu + 9, util.getf("bubble.dat", 0, arc), abyte1, 1);
		gamegfx.og(hu + 10, util.getf("runescape.dat", 0, arc), abyte1, 1);
		gamegfx.og(hu + 11, util.getf("splat.dat", 0, arc), abyte1, 3);
		gamegfx.og(hu + 14, util.getf("icon.dat", 0, arc), abyte1, 8);
		gamegfx.og(hu + 22, util.getf("hbar.dat", 0, arc), abyte1, 1);
		gamegfx.og(hu + 23, util.getf("hbar2.dat", 0, arc), abyte1, 1);
		gamegfx.og(hu + 24, util.getf("compass.dat", 0, arc), abyte1, 1);
		gamegfx.og(hu + 25, util.getf("buttons.dat", 0, arc), abyte1, 2);
		gamegfx.og(iu, util.getf("scrollbar.dat", 0, arc), abyte1, 2);
		gamegfx.og(iu + 2, util.getf("corners.dat", 0, arc), abyte1, 4);
		gamegfx.og(iu + 6, util.getf("arrows.dat", 0, arc), abyte1, 2);
		gamegfx.og(ku, util.getf("projectile.dat", 0, arc), abyte1, cache.zlb);
		int i1 = cache.jjb;
		for(int j1 = 1; i1 > 0; j1++) {
			int k1 = i1;
			i1 -= 30;
			if (k1 > 30)
				k1 = 30;
			gamegfx.og(ju + (j1 - 1) * 30, util.getf("objects" + j1 + ".dat", 0, arc), abyte1, k1);
		}

		gamegfx.zg(hu);
		gamegfx.zg(hu + 9);
		for(int l1 = 11; l1 <= 26; l1++)
			gamegfx.zg(hu + l1);

		for(int i2 = 0; i2 < cache.zlb; i2++)
			gamegfx.zg(ku + i2);

		for(int j2 = 0; j2 < cache.jjb; j2++)
			gamegfx.zg(ju + j2);

	}

	public void km()
	{
		byte abyte0[] = null;
		byte abyte1[] = null;
		try
		{
			abyte0 = loadfile(cachedir + "entity" + version.ENTITY_VERSION + ".jag", "people and monsters", 30);
		}
		catch(IOException ioexception)
		{
			System.out.println("Load error:" + ioexception);
		}
		abyte1 = util.getf("index.dat", 0, abyte0);
		byte abyte2[] = null;
		byte abyte3[] = null;
		if(member)
		{
			try
			{
				abyte2 = loadfile(cachedir + "entity" + version.ENTITY_VERSION + ".mem", "member graphics", 45);
			}
			catch(IOException ioexception1)
			{
				System.out.println("Load error:" + ioexception1);
			}
			abyte3 = util.getf("index.dat", 0, abyte2);
		}
		int i1 = 0;
		ngb = 0;
		ogb = ngb;
		label0:
			for(int j1 = 0; j1 < cache.rkb; j1++)
			{
				String s1 = cache.skb[j1];
				for(int k1 = 0; k1 < j1; k1++)
				{
					if(!cache.skb[k1].equalsIgnoreCase(s1))
						continue;
					cache.xkb[j1] = cache.xkb[k1];
					continue label0;
				}

				byte abyte7[] = util.getf(s1 + ".dat", 0, abyte0);
				byte abyte4[] = abyte1;
				if(abyte7 == null && member)
				{
					abyte7 = util.getf(s1 + ".dat", 0, abyte2);
					abyte4 = abyte3;
				}
				if(abyte7 != null)
				{
					gamegfx.og(ogb, abyte7, abyte4, 15);
					i1 += 15;
					if(cache.vkb[j1] == 1)
					{
						byte abyte8[] = util.getf(s1 + "a.dat", 0, abyte0);
						byte abyte5[] = abyte1;
						if(abyte8 == null && member)
						{
							abyte8 = util.getf(s1 + "a.dat", 0, abyte2);
							abyte5 = abyte3;
						}
						gamegfx.og(ogb + 15, abyte8, abyte5, 3);
						i1 += 3;
					}
					if(cache.wkb[j1] == 1)
					{
						byte abyte9[] = util.getf(s1 + "f.dat", 0, abyte0);
						byte abyte6[] = abyte1;
						if(abyte9 == null && member)
						{
							abyte9 = util.getf(s1 + "f.dat", 0, abyte2);
							abyte6 = abyte3;
						}
						gamegfx.og(ogb + 18, abyte9, abyte6, 9);
						i1 += 9;
					}
					if(cache.ukb[j1] != 0)
					{
						for(int l1 = ogb; l1 < ogb + 27; l1++)
							gamegfx.zg(l1);

					}
				}
				cache.xkb[j1] = ogb;
				ogb += 27;
			}

		System.out.println("Loaded: " + i1 + " frames of animation");
	}

	public void loadTextures() {
		byte[] textarc = null;
		try {
			textarc = loadfile(cachedir + "textures" + version.TEXTURE_VERSION + ".jag", "Textures", 50);
		} catch(IOException ioexception) {
			System.out.println("Load error:" + ioexception);
		}
		byte[] indexes = util.getf("index.dat", 0, textarc);
		camera.inittextures(cache.texturecount, 7, 11);

		for (int i1 = 0; i1 < cache.texturecount; i1++) {
			String s1 = cache.pkb[i1];
			byte abyte2[] = util.getf(s1 + ".dat", 0, textarc);
			gamegfx.og(lu, abyte2, indexes, 1);
			gamegfx.drawquad(0, 0, 128, 128, 0xff00ff);
			gamegfx.xg(0, 0, lu);
			int j1 = ((graphics) (gamegfx)).pk[lu];
			String s2 = cache.qkb[i1];
			if(s2 != null && s2.length() > 0)
			{
				byte abyte3[] = util.getf(s2 + ".dat", 0, textarc);
				gamegfx.og(lu, abyte3, indexes, 1);
				gamegfx.xg(0, 0, lu);
			}
			gamegfx.rf(mu + i1, 0, 0, j1, j1);
			int k1 = j1 * j1;
			for(int l1 = 0; l1 < k1; l1++)
				if(((graphics) (gamegfx)).ik[mu + i1][l1] == 65280)
					((graphics) (gamegfx)).ik[mu + i1][l1] = 0xff00ff;

			gamegfx.fg(mu + i1);
			camera.vh(i1, gamegfx.jk[mu + i1], gamegfx.kk[mu + i1], j1 / 64 - 1);
		}

	}

	public void loadModels() {
		cache.pushmodel("torcha2");
		cache.pushmodel("torcha3");
		cache.pushmodel("torcha4");
		cache.pushmodel("skulltorcha2");
		cache.pushmodel("skulltorcha3");
		cache.pushmodel("skulltorcha4");
		cache.pushmodel("firea2");
		cache.pushmodel("firea3");
		cache.pushmodel("fireplacea2");
		cache.pushmodel("fireplacea3");
		if (gappmode()) {
			byte[] modelarc = null;
			try {
				modelarc = loadfile("models" + version.MODEL_VERSION + ".jag", "3d models", 60);
			} catch(IOException e) {
				System.out.println("Load error:" + e);
			}
			for (int i = 0; i < cache.modelcount; i++) {
				int offset = util.goffset(cache.modelnames[i] + ".ob3", modelarc);
				if (offset != 0)
					gamemodels[i] = new model(modelarc, offset, true);
				else
					gamemodels[i] = new model(1, 1);
				if (cache.modelnames[i].equals("giantcrystal"))
					gamemodels[i].transparent = true;
			}

			return;
		}
		showpercent(70, "Loading 3d models");
		for (int i = 0; i < cache.modelcount; i++) {
			gamemodels[i] = new model("../gamedata/models/" + cache.modelnames[i] + ".ob2");
			if (cache.modelnames[i].equals("giantcrystal"))
				gamemodels[i].transparent = true;
		}
	}

	public void loadLandscape() {
		try {
			landscape.maps = loadfile(cachedir + "maps" + version.LANDSCAPE_VERSION + ".jag", "map", 70);
			if (member)
				landscape.memmaps = loadfile(cachedir + "maps" + version.LANDSCAPE_VERSION + ".mem", "members map", 75);
			landscape.lands = loadfile(cachedir + "land" + version.LANDSCAPE_VERSION + ".jag", "landscape", 80);
			if (member)
				landscape.memlands = loadfile(cachedir + "land" + version.LANDSCAPE_VERSION + ".mem", "members landscape", 85);
		} catch(IOException ioexception) {
			System.out.println("Load error:" + ioexception);
		}
	}

	public void loadSounds() {
		try {
			soundsarc = loadfile(cachedir + "sounds" + version.SOUNDS_VERSION + ".mem", "Sound effects", 90);
			soundstream = new sound();
		} catch(Throwable throwable) {
			System.out.println("Unable to init sounds:" + throwable);
		}
	}

	public void kl() {
		pz = new menu(gamegfx, 10);
		qz = pz.dc(5, 269, 502, 56, 1, 20, true);
		rz = pz.ec(7, 324, 498, 14, 1, 80, false, true);
		sz = pz.dc(5, 269, 502, 56, 1, 20, true);
		tz = pz.dc(5, 269, 502, 56, 1, 20, true);
		pz.nc(rz);
	}

	public void tick()
	{
		if(invalidhost)
			return;
		if(it)
			return;
		try
		{
			kt++;
			if(du == 0)
			{
				lasttimeout = 0;
				il();
			}
			if(du == 1)
			{
				lasttimeout++;
				qm();
			}
			lastclick = 0;
			wq = 0;
			xt++;
			if(xt > 500)
			{
				xt = 0;
				int i1 = (int)(Math.random() * 4D);
				if((i1 & 1) == 1)
					tt += ut;
				if((i1 & 2) == 2)
					vt += wt;
			}
			if(tt < -50)
				ut = 2;
			if(tt > 50)
				ut = -2;
			if(vt < -50)
				wt = 2;
			if(vt > 50)
				wt = -2;
			if(lz > 0)
				lz--;
			if(mz > 0)
				mz--;
			if(nz > 0)
				nz--;
			if(oz > 0)
			{
				oz--;
				return;
			}
		} catch(OutOfMemoryError _ex) {
			fm();
			it = true;
		}
	}

	public void render()
	{
		if(invalidhost)
		{
			Graphics g1 = getGraphics();
			g1.setColor(Color.black);
			g1.fillRect(0, 0, 512, 356);
			g1.setFont(new Font("Helvetica", 1, 20));
			g1.setColor(Color.white);
			g1.drawString("Error - unable to load game!", 50, 50);
			g1.drawString("To play RuneScape make sure you play from", 50, 100);
			g1.drawString("http://www.runescape.com", 50, 150);
			setRefreshRate(1);
			return;
		}
		if(it)
		{
			Graphics g2 = getGraphics();
			g2.setColor(Color.black);
			g2.fillRect(0, 0, 512, 356);
			g2.setFont(new Font("Helvetica", 1, 20));
			g2.setColor(Color.white);
			g2.drawString("Error - out of memory!", 50, 50);
			g2.drawString("Close ALL unnecessary programs", 50, 100);
			g2.drawString("and windows before loading the game", 50, 150);
			g2.drawString("RuneScape needs about 48meg of spare RAM", 50, 200);
			setRefreshRate(1);
			return;
		}
		try
		{
			if(du == 0)
			{
				gamegfx.al = false;
				rm();
			}
			if(du == 1)
			{
				gamegfx.al = true;
				yk();
				return;
			}
		}
		catch(OutOfMemoryError _ex)
		{
			fm();
			it = true;
		}
	}

	public void onkill()
	{
		sendlogout();
		fm();
		if(soundstream != null)
			soundstream.stop();
	}

	public void fm()
	{
		try
		{
			if(gamegfx != null)
			{
				gamegfx.jg();
				gamegfx.pixels = null;
				gamegfx = null;
			}
			if(camera != null)
			{
				camera.si();
				camera = null;
			}
			gamemodels = null;
			vw = null;
			ex = null;
			newplayers = null;
			players = null;
			jw = null;
			kw = null;
			ourplayer = null;
			if(landscape != null)
			{
				landscape.djb = null;
				landscape.ejb = null;
				landscape.fjb = null;
				landscape.gjb = null;
				landscape = null;
			}
			System.gc();
			return;
		}
		catch(Exception _ex)
		{
			return;
		}
	}

	public void onkey(int i1)
	{
		if(du == 0)
		{
			if(jdb == 0)
				kdb.od(i1);
			if(jdb == 1)
				ndb.od(i1);
			if(jdb == 2)
				wdb.od(i1);
			if(jdb == 3)
				lfb.od(i1);
		}
		if(du == 1)
		{
			if(qgb)
			{
				jeb.od(i1);
				return;
			}
			if(zeb)
			{
				if(dfb == -1)
					afb.od(i1);
				return;
			}
			if(scb == 0 && rcb == 0)
				pz.od(i1);
			if(scb == 3 || scb == 4 || scb == 5)
				scb = 0;
		}
	}

	public void onclick(int i1, int j1, int k1)
	{
		rt[qt] = j1;
		st[qt] = k1;
		qt = qt + 1 & 0x1fff;
		for(int l1 = 10; l1 < 4000; l1++)
		{
			int i2 = qt - l1 & 0x1fff;
			if(rt[i2] == j1 && st[i2] == k1)
			{
				boolean flag = false;
				for(int j2 = 1; j2 < l1; j2++)
				{
					int k2 = qt - j2 & 0x1fff;
					int l2 = i2 - j2 & 0x1fff;
					if(rt[l2] != j1 || st[l2] != k1)
						flag = true;
					if(rt[k2] != rt[l2] || st[k2] != st[l2])
						break;
					if(j2 == l1 - 1 && flag && fdb == 0 && edb == 0)
					{
						reqlogout();
						return;
					}
				}

			}
		}

	}

	public void qk()
	{
		du = 0;
		jdb = 0;
		heb = "";
		ieb = "";
		feb = "Please enter a username:";
		geb = "*" + heb + "*";
		pcount = 0;
		hw = 0;
	}

	public void wl()
	{
		inputmessage = "";
		enteredmessage = "";
	}

	public void reqlogout()
	{
		if(du == 0)
			return;
		if(fdb > 450)
		{
			ik("@cya@You can't logout during combat!", 3);
			return;
		}
		if(fdb > 0)
		{
			ik("@cya@You can't logout for 10 seconds after combat", 3);
			return;
		} else
		{
			stream.create(6);
			stream.fmtdata();
			edb = 1000;
			return;
		}
	}

	public void playsound(String sound) {
		if(soundstream == null)
			return;
		if (!soundfx) {
			soundstream.set(soundsarc, util.goffset(sound + ".pcm", soundsarc), util.glen(sound + ".pcm", soundsarc));
		}
	}

	public void ul()
	{
		afb = new menu(gamegfx, 100);
		int i1 = 8;
		bfb = afb.jd(256, i1, "@yel@Please provide 5 security questions in case you lose your password", 1, true);
		i1 += 22;
		afb.jd(256, i1, "If you ever lose your password, you will need these to prove you own your account.", 1, true);
		i1 += 13;
		afb.jd(256, i1, "Your answers are encrypted and are ONLY used for password recovery purposes.", 1, true);
		i1 += 22;
		afb.jd(256, i1, "@ora@IMPORTANT:@whi@ To recover your password you must give the EXACT same answers you", 1, true);
		i1 += 13;
		afb.jd(256, i1, "give here. If you think you might forget an answer, or someone else could guess the", 1, true);
		i1 += 13;
		afb.jd(256, i1, "answer, then press the 'different question' button to get a better question.", 1, true);
		i1 += 35;
		for(int j1 = 0; j1 < 5; j1++)
		{
			afb.ad(170, i1, 310, 30);
			jfb[j1] = "~:" + ifb[j1];
			efb[j1] = afb.jd(170, i1 - 7, (j1 + 1) + ": " + recovquestions[ifb[j1]], 1, true);
			ffb[j1] = afb.yc(170, i1 + 7, 310, 30, 1, 80, false, true);
			afb.ad(370, i1, 80, 30);
			afb.jd(370, i1 - 7, "Different", 1, true);
			afb.jd(370, i1 + 7, "Question", 1, true);
			gfb[j1] = afb.md(370, i1, 80, 30);
			afb.ad(455, i1, 80, 30);
			afb.jd(455, i1 - 7, "Enter own", 1, true);
			afb.jd(455, i1 + 7, "Question", 1, true);
			hfb[j1] = afb.md(455, i1, 80, 30);
			i1 += 35;
		}

		afb.nc(ffb[0]);
		i1 += 10;
		afb.ad(256, i1, 250, 30);
		afb.jd(256, i1, "Click here when finished", 4, true);
		cfb = afb.md(256, i1, 250, 30);
	}

	public void rk() {
		if (dfb != -1) {
			if (enteredmessage.length() > 0) {
				jfb[dfb] = enteredmessage;
				afb.kd(efb[dfb], (dfb + 1) + ": " + jfb[dfb]);
				afb.kd(ffb[dfb], "");
				dfb = -1;
			}
			return;
		}
		afb.pd(mousex, mousey, lastclick, mouseclick);
		for (int i1 = 0; i1 < 5; i1++) {
			if (afb.rd(gfb[i1])) {
				for (boolean flag = false; !flag;) {
					ifb[i1] = (ifb[i1] + 1) % recovquestions.length;
					flag = true;
					for (int k1 = 0; k1 < 5; k1++) {
						if (k1 != i1 && ifb[k1] == ifb[i1])
							flag = false;
					}
				}

				jfb[i1] = "~:" + ifb[i1];
				afb.kd(efb[i1], (i1 + 1) + ": " + recovquestions[ifb[i1]]);
				afb.kd(ffb[i1], "");
			}
		}

		for (int j1 = 0; j1 < 5; j1++) {
			if (afb.rd(hfb[j1])) {
				dfb = j1;
				inputmessage = "";
				enteredmessage = "";
			}
		}

		if (afb.rd(cfb)) {
			for (int l1 = 0; l1 < 5; l1++) {
				String s1 = afb.pc(ffb[l1]);
				if (s1 == null || s1.length() < 3) {
					afb.kd(bfb, "@yel@Please provide a longer answer to question: " + (l1 + 1));
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

			stream.create(208);
			for (int i = 0; i < 5; i++) {
				String s3 = jfb[i];
				if (s3 == null || s3.length() == 0)
					s3 = String.valueOf(i + 1);
				if (s3.length() > 50)
					s3 = s3.substring(0, 50);
				stream.p1(s3.length());
				stream.pjstr(s3);
				stream.prsa8(util.encodeb47(afb.pc(ffb[i])), sessionid, exponent, modulus);
			}
			stream.fmtdata();

			for (int l2 = 0; l2 < 5; l2++) {
				ifb[l2] = l2;
				jfb[l2] = "~:" + ifb[l2];
				afb.kd(ffb[l2], "");
				afb.kd(efb[l2], (l2 + 1) + ": " + recovquestions[ifb[l2]]);
			}
			gamegfx.clear();
			zeb = false;
		}
	}

	public void fk() {
		gamegfx.interlace = false;
		gamegfx.clear();
		afb.hc();
		if (dfb != -1) {
			int i1 = 150;
			gamegfx.drawquad(26, i1, 460, 60, 0);
			gamegfx.drawquadout(26, i1, 460, 60, 0xffffff);
			i1 += 22;
			gamegfx.ug("Please enter your question", 256, i1, 4, 0xffffff);
			i1 += 25;
			gamegfx.ug(inputmessage + "*", 256, i1, 4, 0xffffff);
		}
		gamegfx.xg(0, height, hu + 22);
		gamegfx.drawimg(gfx, 0, 0);
	}

	public void el()
	{
		lfb = new menu(gamegfx, 100);
		int i1 = 10;
		mfb = lfb.jd(256, i1, "@yel@To prove this is your account please provide the answers to", 1, true);
		i1 += 15;
		nfb = lfb.jd(256, i1, "@yel@your security questions. You will then be able to reset your password", 1, true);
		i1 += 35;
		for(int j1 = 0; j1 < 5; j1++)
		{
			lfb.ad(256, i1, 410, 30);
			wfb[j1] = lfb.jd(256, i1 - 7, (j1 + 1) + ": question?", 1, true);
			xfb[j1] = lfb.yc(256, i1 + 7, 310, 30, 1, 80, true, true);
			i1 += 35;
		}

		lfb.nc(xfb[0]);
		lfb.ad(256, i1, 410, 30);
		lfb.jd(256, i1 - 7, "If you know it, enter a previous password used on this account", 1, true);
		ofb = lfb.yc(256, i1 + 7, 310, 30, 1, 80, true, true);
		i1 += 35;
		lfb.ad(151, i1, 200, 30);
		lfb.jd(151, i1 - 7, "Choose a NEW password", 1, true);
		pfb = lfb.yc(146, i1 + 7, 200, 30, 1, 80, true, true);
		lfb.ad(361, i1, 200, 30);
		lfb.jd(361, i1 - 7, "Confirm new password", 1, true);
		qfb = lfb.yc(366, i1 + 7, 200, 30, 1, 80, true, true);
		i1 += 35;
		lfb.ad(201, i1, 100, 30);
		lfb.jd(201, i1, "Submit", 4, true);
		rfb = lfb.md(201, i1, 100, 30);
		lfb.ad(311, i1, 100, 30);
		lfb.jd(311, i1, "Cancel", 4, true);
		sfb = lfb.md(311, i1, 100, 30);
	}

	public void fl(boolean flag)
	{
		jeb = new menu(gamegfx, 100);
		jeb.jd(256, 10, "Design Your Character", 4, true);
		int i1 = 140;
		int j1 = 34;
		if(flag)
		{
			i1 += 116;
			j1 -= 10;
		} else
		{
			jeb.ad(i1, j1, 200, 25);
			jeb.jd(i1, j1, "Appearance", 4, false);
			j1 += 15;
		}
		jeb.jd(i1 - 55, j1 + 110, "Front", 3, true);
		jeb.jd(i1, j1 + 110, "Side", 3, true);
		jeb.jd(i1 + 55, j1 + 110, "Back", 3, true);
		byte byte0 = 54;
		j1 += 145;
		jeb.jc(i1 - byte0, j1, 53, 41);
		jeb.jd(i1 - byte0, j1 - 8, "Head", 1, true);
		jeb.jd(i1 - byte0, j1 + 8, "Type", 1, true);
		jeb.lc(i1 - byte0 - 40, j1, menu.jg + 7);
		keb = jeb.md(i1 - byte0 - 40, j1, 20, 20);
		jeb.lc((i1 - byte0) + 40, j1, menu.jg + 6);
		leb = jeb.md((i1 - byte0) + 40, j1, 20, 20);
		jeb.jc(i1 + byte0, j1, 53, 41);
		jeb.jd(i1 + byte0, j1 - 8, "Hair", 1, true);
		jeb.jd(i1 + byte0, j1 + 8, "Color", 1, true);
		jeb.lc((i1 + byte0) - 40, j1, menu.jg + 7);
		meb = jeb.md((i1 + byte0) - 40, j1, 20, 20);
		jeb.lc(i1 + byte0 + 40, j1, menu.jg + 6);
		neb = jeb.md(i1 + byte0 + 40, j1, 20, 20);
		j1 += 50;
		jeb.jc(i1 - byte0, j1, 53, 41);
		jeb.jd(i1 - byte0, j1, "Gender", 1, true);
		jeb.lc(i1 - byte0 - 40, j1, menu.jg + 7);
		oeb = jeb.md(i1 - byte0 - 40, j1, 20, 20);
		jeb.lc((i1 - byte0) + 40, j1, menu.jg + 6);
		peb = jeb.md((i1 - byte0) + 40, j1, 20, 20);
		jeb.jc(i1 + byte0, j1, 53, 41);
		jeb.jd(i1 + byte0, j1 - 8, "Top", 1, true);
		jeb.jd(i1 + byte0, j1 + 8, "Color", 1, true);
		jeb.lc((i1 + byte0) - 40, j1, menu.jg + 7);
		qeb = jeb.md((i1 + byte0) - 40, j1, 20, 20);
		jeb.lc(i1 + byte0 + 40, j1, menu.jg + 6);
		reb = jeb.md(i1 + byte0 + 40, j1, 20, 20);
		j1 += 50;
		jeb.jc(i1 - byte0, j1, 53, 41);
		jeb.jd(i1 - byte0, j1 - 8, "Skin", 1, true);
		jeb.jd(i1 - byte0, j1 + 8, "Color", 1, true);
		jeb.lc(i1 - byte0 - 40, j1, menu.jg + 7);
		seb = jeb.md(i1 - byte0 - 40, j1, 20, 20);
		jeb.lc((i1 - byte0) + 40, j1, menu.jg + 6);
		teb = jeb.md((i1 - byte0) + 40, j1, 20, 20);
		jeb.jc(i1 + byte0, j1, 53, 41);
		jeb.jd(i1 + byte0, j1 - 8, "Bottom", 1, true);
		jeb.jd(i1 + byte0, j1 + 8, "Color", 1, true);
		jeb.lc((i1 + byte0) - 40, j1, menu.jg + 7);
		ueb = jeb.md((i1 + byte0) - 40, j1, 20, 20);
		jeb.lc(i1 + byte0 + 40, j1, menu.jg + 6);
		veb = jeb.md(i1 + byte0 + 40, j1, 20, 20);
		if(!flag)
		{
			i1 = 372;
			j1 = 35;
			jeb.ad(i1, j1, 200, 25);
			jeb.jd(i1, j1, "Character Type", 4, false);
			j1 += 22;
			jeb.jd(i1, j1, "Each character type has different starting", 0, true);
			j1 += 13;
			jeb.jd(i1, j1, "bonuses. But the choice you make here", 0, true);
			j1 += 13;
			jeb.jd(i1, j1, "isn't permanent, and will change depending", 0, true);
			j1 += 13;
			jeb.jd(i1, j1, "on how you play the game.", 0, true);
			j1 += 73;
			jeb.jc(i1, j1, 215, 125);
			String as[] = {
					"Adventurer", "Warrior", "Wizard", "Ranger", "Miner"
			};
			xeb = jeb.cc(i1, j1 + 2, as, 3, true);
		}
		j1 += 82;
		if(flag)
			j1 -= 35;
		jeb.ad(i1, j1, 200, 30);
		if(!flag)
			jeb.jd(i1, j1, "Start Game", 4, false);
		else
			jeb.jd(i1, j1, "Accept", 4, false);
		web = jeb.md(i1, j1, 200, 30);
	}

	public void jk()
	{
		gamegfx.interlace = false;
		gamegfx.clear();
		jeb.hc();
		int i1 = 140;
		int j1 = 50;
		if(rgb)
		{
			i1 += 116;
			j1 -= 25;
		}
		gamegfx.mg(i1 - 32 - 55, j1, 64, 102, cache.xkb[ugb], ahb[xgb]);
		gamegfx.wf(i1 - 32 - 55, j1, 64, 102, cache.xkb[tgb], ahb[wgb], chb[ygb], 0, false);
		gamegfx.wf(i1 - 32 - 55, j1, 64, 102, cache.xkb[sgb], bhb[vgb], chb[ygb], 0, false);
		gamegfx.mg(i1 - 32, j1, 64, 102, cache.xkb[ugb] + 6, ahb[xgb]);
		gamegfx.wf(i1 - 32, j1, 64, 102, cache.xkb[tgb] + 6, ahb[wgb], chb[ygb], 0, false);
		gamegfx.wf(i1 - 32, j1, 64, 102, cache.xkb[sgb] + 6, bhb[vgb], chb[ygb], 0, false);
		gamegfx.mg((i1 - 32) + 55, j1, 64, 102, cache.xkb[ugb] + 12, ahb[xgb]);
		gamegfx.wf((i1 - 32) + 55, j1, 64, 102, cache.xkb[tgb] + 12, ahb[wgb], chb[ygb], 0, false);
		gamegfx.wf((i1 - 32) + 55, j1, 64, 102, cache.xkb[sgb] + 12, bhb[vgb], chb[ygb], 0, false);
		gamegfx.xg(0, height, hu + 22);
		gamegfx.drawimg(gfx, 0, 0);
	}

	public void vk()
	{
		jeb.pd(mousex, mousey, lastclick, mouseclick);
		if(jeb.rd(keb))
			do
				sgb = ((sgb - 1) + cache.rkb) % cache.rkb;
			while((cache.ukb[sgb] & 3) != 1 || (cache.ukb[sgb] & 4 * zgb) == 0);
		if(jeb.rd(leb))
			do
				sgb = (sgb + 1) % cache.rkb;
			while((cache.ukb[sgb] & 3) != 1 || (cache.ukb[sgb] & 4 * zgb) == 0);
		if(jeb.rd(meb))
			vgb = ((vgb - 1) + bhb.length) % bhb.length;
		if(jeb.rd(neb))
			vgb = (vgb + 1) % bhb.length;
		if(jeb.rd(oeb) || jeb.rd(peb))
		{
			for(zgb = 3 - zgb; (cache.ukb[sgb] & 3) != 1 || (cache.ukb[sgb] & 4 * zgb) == 0; sgb = (sgb + 1) % cache.rkb);
			for(; (cache.ukb[tgb] & 3) != 2 || (cache.ukb[tgb] & 4 * zgb) == 0; tgb = (tgb + 1) % cache.rkb);
		}
		if(jeb.rd(qeb))
			wgb = ((wgb - 1) + ahb.length) % ahb.length;
		if(jeb.rd(reb))
			wgb = (wgb + 1) % ahb.length;
		if(jeb.rd(seb))
			ygb = ((ygb - 1) + chb.length) % chb.length;
		if(jeb.rd(teb))
			ygb = (ygb + 1) % chb.length;
		if(jeb.rd(ueb))
			xgb = ((xgb - 1) + ahb.length) % ahb.length;
		if(jeb.rd(veb))
			xgb = (xgb + 1) % ahb.length;
		if(jeb.rd(web))
		{
			stream.create(236);
			stream.p1(zgb);
			stream.p1(sgb);
			stream.p1(tgb);
			stream.p1(ugb);
			stream.p1(vgb);
			stream.p1(wgb);
			stream.p1(xgb);
			stream.p1(ygb);
			stream.p1(jeb.tc(xeb));
			stream.fmtdata();
			gamegfx.clear();
			qgb = false;
		}
	}

	public void uk()
	{
		kdb = new menu(gamegfx, 50);
		int i1 = 40;
		if(!member)
		{
			kdb.jd(256, 200 + i1, "Click on an option", 5, true);
			kdb.ad(156, 240 + i1, 120, 35);
			kdb.ad(356, 240 + i1, 120, 35);
			kdb.jd(156, 240 + i1, "New User", 5, false);
			kdb.jd(356, 240 + i1, "Existing User", 5, false);
			ldb = kdb.md(156, 240 + i1, 120, 35);
			mdb = kdb.md(356, 240 + i1, 120, 35);
		} else {
			kdb.jd(256, 200 + i1, "Welcome to RuneScape", 4, true);
			kdb.jd(256, 215 + i1, "You need a member account to use this server", 4, true);
			kdb.ad(256, 250 + i1, 200, 35);
			kdb.jd(256, 250 + i1, "Click here to login", 5, false);
			mdb = kdb.md(256, 250 + i1, 200, 35);
		}
		ndb = new menu(gamegfx, 50);
		i1 = 70;
		odb = ndb.jd(256, i1 + 8, "To create an account please enter all the requested details", 4, true);
		i1 += 25;
		ndb.ad(256, i1 + 17, 250, 34);
		ndb.jd(256, i1 + 8, "Choose a Username", 4, false);
		sdb = ndb.yc(256, i1 + 25, 200, 40, 4, 12, false, false);
		ndb.nc(sdb);
		i1 += 40;
		ndb.ad(141, i1 + 17, 220, 34);
		ndb.jd(141, i1 + 8, "Choose a Password", 4, false);
		tdb = ndb.yc(141, i1 + 25, 220, 40, 4, 20, true, false);
		ndb.ad(371, i1 + 17, 220, 34);
		ndb.jd(371, i1 + 8, "Confirm Password", 4, false);
		udb = ndb.yc(371, i1 + 25, 220, 40, 4, 20, true, false);
		i1 += 40;
		i1 += 20;
		vdb = ndb.sc(60, i1, 14);
		ndb.kc(75, i1, "I have read and agree to the terms+conditions listed at:", 4, true);
		i1 += 15;
		ndb.jd(256, i1, "http://www.runescape.com/runeterms.html", 4, true);
		i1 += 20;
		ndb.ad(156, i1 + 17, 150, 34);
		ndb.jd(156, i1 + 17, "Submit", 5, false);
		rdb = ndb.md(156, i1 + 17, 150, 34);
		ndb.ad(356, i1 + 17, 150, 34);
		ndb.jd(356, i1 + 17, "Cancel", 5, false);
		qdb = ndb.md(356, i1 + 17, 150, 34);
		wdb = new menu(gamegfx, 50);
		i1 = 230;
		xdb = wdb.jd(256, i1 - 10, "Please enter your username and password", 4, true);
		i1 += 28;
		wdb.ad(140, i1, 200, 40);
		wdb.jd(140, i1 - 10, "Username:", 4, false);
		ydb = wdb.yc(140, i1 + 10, 200, 40, 4, 12, false, false);
		i1 += 47;
		wdb.ad(190, i1, 200, 40);
		wdb.jd(190, i1 - 10, "Password:", 4, false);
		zdb = wdb.yc(190, i1 + 10, 200, 40, 4, 20, true, false);
		i1 -= 55;
		wdb.ad(410, i1, 120, 25);
		wdb.jd(410, i1, "Ok", 4, false);
		aeb = wdb.md(410, i1, 120, 25);
		i1 += 30;
		wdb.ad(410, i1, 120, 25);
		wdb.jd(410, i1, "Cancel", 4, false);
		beb = wdb.md(410, i1, 120, 25);
		i1 += 30;
		wdb.ad(410, i1, 160, 25);
		wdb.jd(410, i1, "I've lost my password", 4, false);
		ceb = wdb.md(410, i1, 160, 25);
		wdb.nc(ydb);
	}

	public void rm()
	{
		vcb = false;
		gamegfx.interlace = false;
		gamegfx.clear();
		if(jdb == 0 || jdb == 2)
		{
			int i1 = (kt * 2) % 3072;
			if(i1 < 1024)
			{
				gamegfx.xg(0, 10, 2500);
				if(i1 > 768)
					gamegfx.qg(0, 10, 2501, i1 - 768);
			} else
				if(i1 < 2048)
				{
					gamegfx.xg(0, 10, 2501);
					if(i1 > 1792)
						gamegfx.qg(0, 10, hu + 10, i1 - 1792);
				} else
				{
					gamegfx.xg(0, 10, hu + 10);
					if(i1 > 2816)
						gamegfx.qg(0, 10, 2500, i1 - 2816);
				}
		}
		if(jdb == 0)
			kdb.hc();
		if(jdb == 1)
			ndb.hc();
		if(jdb == 2)
			wdb.hc();
		if(jdb == 3)
			lfb.hc();
		gamegfx.xg(0, height, hu + 22);
		gamegfx.drawimg(gfx, 0, 0);
	}

	public void vl()
	{
		int i1 = 0;
		byte byte0 = 50;
		byte byte1 = 50;
		landscape.xo(byte0 * 48 + 23, byte1 * 48 + 23, i1);
		landscape.qo(gamemodels);
		char c1 = '\u2600';
		char c2 = '\u1900';
		char c3 = '\u044C';
		char c4 = '\u0378';
		camera.im = 4100;
		camera.jm = 4100;
		camera.km = 1;
		camera.lm = 4000;
		camera.ai(c1, -landscape.oo(c1, c2), c2, 912, c4, 0, c3 * 2);
		camera.wi();
		gamegfx.fadepixels();
		gamegfx.fadepixels();
		gamegfx.drawquad(0, 0, 512, 6, 0);
		for(int j1 = 6; j1 >= 1; j1--)
			gamegfx.sg(0, j1, 0, j1, 512, 8);

		gamegfx.drawquad(0, 194, 512, 20, 0);
		for(int k1 = 6; k1 >= 1; k1--)
			gamegfx.sg(0, k1, 0, 194 - k1, 512, 8);

		gamegfx.xg(15, 15, hu + 10);
		gamegfx.rf(2500, 0, 0, 512, 200);
		gamegfx.fg(2500);
		c1 = '\u2400';
		c2 = '\u2400';
		c3 = '\u044C';
		c4 = '\u0378';
		camera.im = 4100;
		camera.jm = 4100;
		camera.km = 1;
		camera.lm = 4000;
		camera.ai(c1, -landscape.oo(c1, c2), c2, 912, c4, 0, c3 * 2);
		camera.wi();
		gamegfx.fadepixels();
		gamegfx.fadepixels();
		gamegfx.drawquad(0, 0, 512, 6, 0);
		for(int l1 = 6; l1 >= 1; l1--)
			gamegfx.sg(0, l1, 0, l1, 512, 8);

		gamegfx.drawquad(0, 194, 512, 20, 0);
		for(int i2 = 6; i2 >= 1; i2--)
			gamegfx.sg(0, i2, 0, 194 - i2, 512, 8);

		gamegfx.xg(15, 15, hu + 10);
		gamegfx.rf(2501, 0, 0, 512, 200);
		gamegfx.fg(2501);
		for(int j2 = 0; j2 < 64; j2++)
		{
			camera.zh(landscape.fjb[0][j2]);
			camera.zh(landscape.ejb[1][j2]);
			camera.zh(landscape.fjb[1][j2]);
			camera.zh(landscape.ejb[2][j2]);
			camera.zh(landscape.fjb[2][j2]);
		}

		c1 = '\u2B80';
		c2 = '\u2880';
		c3 = '\u01F4';
		c4 = '\u0178';
		camera.im = 4100;
		camera.jm = 4100;
		camera.km = 1;
		camera.lm = 4000;
		camera.ai(c1, -landscape.oo(c1, c2), c2, 912, c4, 0, c3 * 2);
		camera.wi();
		gamegfx.fadepixels();
		gamegfx.fadepixels();
		gamegfx.drawquad(0, 0, 512, 6, 0);
		for(int k2 = 6; k2 >= 1; k2--)
			gamegfx.sg(0, k2, 0, k2, 512, 8);

		gamegfx.drawquad(0, 194, 512, 20, 0);
		for(int l2 = 6; l2 >= 1; l2--)
			gamegfx.sg(0, l2, 0, 194, 512, 8);

		gamegfx.xg(15, 15, hu + 10);
		gamegfx.rf(hu + 10, 0, 0, 512, 200);
		gamegfx.fg(hu + 10);
	}

	public void il()
	{
		if(socktimeout > 0)
			socktimeout--;
		if(jdb == 0)
		{
			kdb.pd(mousex, mousey, lastclick, mouseclick);
			if(kdb.rd(ldb))
			{
				jdb = 1;
				ndb.kd(sdb, "");
				ndb.kd(tdb, "");
				ndb.kd(udb, "");
				ndb.nc(sdb);
				ndb.vc(vdb, 0);
				ndb.kd(odb, "To create an account please enter all the requested details");
			}
			if(kdb.rd(mdb))
			{
				jdb = 2;
				wdb.kd(xdb, "Please enter your username and password");
				wdb.kd(ydb, "");
				wdb.kd(zdb, "");
				wdb.nc(ydb);
				return;
			}
		} else
			if(jdb == 1)
			{
				ndb.pd(mousex, mousey, lastclick, mouseclick);
				if(ndb.rd(sdb))
					ndb.nc(tdb);
				if(ndb.rd(tdb))
					ndb.nc(udb);
				if(ndb.rd(udb))
					ndb.nc(sdb);
				if(ndb.rd(qdb))
					jdb = 0;
				if(ndb.rd(rdb))
				{
					if(ndb.pc(sdb) == null || ndb.pc(sdb).length() == 0 || ndb.pc(tdb) == null || ndb.pc(tdb).length() == 0)
					{
						ndb.kd(odb, "@yel@Please fill in ALL requested information to continue!");
						return;
					}
					if(!ndb.pc(tdb).equalsIgnoreCase(ndb.pc(udb)))
					{
						ndb.kd(odb, "@yel@The two passwords entered are not the same as each other!");
						return;
					}
					if(ndb.pc(tdb).length() < 5)
					{
						ndb.kd(odb, "@yel@Your password must be at least 5 letters long");
						return;
					}
					if(ndb.tc(vdb) == 0)
					{
						ndb.kd(odb, "@yel@You must agree to the terms+conditions to continue");
						return;
					} else
					{
						ndb.kd(odb, "Please wait... Creating new account");
						rm();
						clearprof();
						String s1 = ndb.pc(sdb);
						String s3 = ndb.pc(tdb);
						newplayer(s1, s3);
						return;
					}
				}
			} else
				if(jdb == 2)
				{
					wdb.pd(mousex, mousey, lastclick, mouseclick);
					if(wdb.rd(beb))
						jdb = 0;
					if(wdb.rd(ydb))
						wdb.nc(zdb);
					if(wdb.rd(zdb) || wdb.rd(aeb))
					{
						heb = wdb.pc(ydb);
						ieb = wdb.pc(zdb);
						login(heb, ieb, false);
					}
					if(wdb.rd(ceb))
					{
						heb = wdb.pc(ydb);
						heb = util.fmtstr(heb, 20);
						if(heb.trim().length() == 0)
						{
							setloginstatus("You must enter your username to recover your password", "");
							return;
						}
						setloginstatus(networkedgame.responses[6], networkedgame.responses[7]);
						try
						{
							stream = new connection(this, host, port);
							stream.maxcnt = networkedgame.packetread;
							stream.g4();
							stream.create(4);
							stream.p8(util.encodeb37(heb));
							stream.flush();
							stream.g2();
							int i1 = stream.read();
							System.out.println("Getpq response: " + i1);
							if(i1 == 0)
							{
								setloginstatus("Sorry, the recovery questions for this user have not been set", "");
								return;
							}
							for(int j1 = 0; j1 < 5; j1++)
							{
								int k1 = stream.read();
								byte abyte0[] = new byte[5000];
								stream.read(k1, abyte0);
								String s6 = new String(abyte0, 0, k1);
								if(s6.startsWith("~:"))
								{
									s6 = s6.substring(2);
									int j2 = 0;
									try
									{
										j2 = Integer.parseInt(s6);
									}
									catch(Exception _ex) { }
									s6 = recovquestions[j2];
								}
								lfb.kd(wfb[j1], s6);
							}

							if(kfb)
							{
								setloginstatus("Sorry, you have already attempted 1 recovery, try again later", "");
								return;
							}
							jdb = 3;
							lfb.kd(mfb, "@yel@To prove this is your account please provide the answers to");
							lfb.kd(nfb, "@yel@your security questions. You will then be able to reset your password");
							for(int l1 = 0; l1 < 5; l1++)
								lfb.kd(xfb[l1], "");

							lfb.kd(ofb, "");
							lfb.kd(pfb, "");
							lfb.kd(qfb, "");
							return;
						}
						catch(Exception _ex)
						{
							setloginstatus(networkedgame.responses[12], networkedgame.responses[13]);
						}
						return;
					}
				} else
					if(jdb == 3)
					{
						lfb.pd(mousex, mousey, lastclick, mouseclick);
						if(lfb.rd(rfb))
						{
							String s2 = lfb.pc(pfb);
							String s4 = lfb.pc(qfb);
							if(!s2.equalsIgnoreCase(s4))
							{
								setloginstatus("@yel@The two new passwords entered are not the same as each other!", "");
								return;
							}
							if(s2.length() < 5)
							{
								setloginstatus("@yel@Your new password must be at least 5 letters long", "");
								return;
							}
							setloginstatus(networkedgame.responses[6], networkedgame.responses[7]);
							try
							{
								stream = new connection(this, host, port);
								stream.maxcnt = networkedgame.packetread;
								int i2 = stream.g4();
								String s5 = util.fmtstr(lfb.pc(ofb), 20);
								String s7 = util.fmtstr(lfb.pc(pfb), 20);
								stream.create(8);
								stream.p8(util.encodeb37(heb));
								stream.p4(getseed());
								stream.prsastr(s5 + s7, i2, exponent, modulus);
								for(int k2 = 0; k2 < 5; k2++)
									stream.prsa8(util.encodeb47(lfb.pc(xfb[k2])), i2, exponent, modulus);

								stream.flush();
								stream.read();
								int l2 = stream.read();
								System.out.println("Recover response: " + l2);
								if(l2 == 0)
								{
									jdb = 2;
									setloginstatus("Sorry, recovery failed. You may try again in 1 hour", "");
									kfb = true;
									return;
								}
								if(l2 == 1)
								{
									jdb = 2;
									setloginstatus("Your pass has been reset. You may now use the new pass to login", "");
									return;
								} else
								{
									jdb = 2;
									setloginstatus("Recovery failed! Attempts exceeded?", "");
									return;
								}
							}
							catch(Exception _ex)
							{
								setloginstatus(networkedgame.responses[12], networkedgame.responses[13]);
							}
						}
						if(lfb.rd(sfb))
							jdb = 0;
					}
	}

	public void setloginstatus(String s1, String s2)
	{
		if(jdb == 1)
			ndb.kd(odb, s1 + " " + s2);
		if(jdb == 2)
			wdb.kd(xdb, s1 + " " + s2);
		if(jdb == 3)
		{
			lfb.kd(mfb, s1);
			lfb.kd(nfb, s2);
		}
		geb = s2;
		rm();
		clearprof();
	}

	public void logoutrefused()
	{
		edb = 0;
		ik("@cya@Sorry, you can't logout at the moment", 3);
	}

	public void connectionlost()
	{
		if(edb != 0)
		{
			resetstates();
			return;
		} else
		{
			//connectionlost();
			return;
		}
	}

	public void resetstates()
	{
		jdb = 0;
		du = 0;
		edb = 0;
	}

	public void onlogin()
	{
		qcb = 0;
		edb = 0;
		jdb = 0;
		du = 1;
		wl();
		gamegfx.clear();
		gamegfx.drawimg(gfx, 0, 0);
		for(int i1 = 0; i1 < uw; i1++)
		{
			camera.zh(vw[i1]);
			landscape.fp(ww[i1], xw[i1], yw[i1]);
		}

		for(int j1 = 0; j1 < dx; j1++)
		{
			camera.zh(ex[j1]);
			landscape.fo(fx[j1], gx[j1], hx[j1], ix[j1]);
		}

		uw = 0;
		dx = 0;
		ow = 0;
		pcount = 0;
		for(int k1 = 0; k1 < tv; k1++)
			newplayers[k1] = null;

		for(int l1 = 0; l1 < uv; l1++)
			players[l1] = null;

		hw = 0;
		for(int i2 = 0; i2 < fw; i2++)
			jw[i2] = null;

		for(int j2 = 0; j2 < gw; j2++)
			kw[j2] = null;

		for(int k2 = 0; k2 < 50; k2++)
			qy[k2] = false;

		mt = 0;
		lastclick = 0;
		mouseclick = 0;
		ubb = false;
		ccb = false;
	}

	public void relog()
	{
		String user = ndb.pc(sdb);
		String pass = ndb.pc(tdb);
		jdb = 2;
		wdb.kd(xdb, "Please enter your username and password");
		wdb.kd(ydb, user);
		wdb.kd(zdb, pass);
		rm();
		clearprof();
		login(user, pass, false);
	}

	public void qm()
	{
		pingnread();
		if(edb > 0)
			edb--;
		if(lasttimeout > 4500 && fdb == 0 && edb == 0)
		{
			lasttimeout -= 500;
			reqlogout();
			return;
		}
		if(ourplayer.kr == 8 || ourplayer.kr == 9)
			fdb = 500;
		if(fdb > 0)
			fdb--;
		if(qgb)
		{
			vk();
			return;
		}
		if(zeb)
		{
			rk();
			return;
		}
		for(int i1 = 0; i1 < pcount; i1++)
		{
			livingentity l1 = players[i1];
			int k1 = (l1.curwayp + 1) % 10;
			if(l1.endwaypspr != k1)
			{
				int j2 = -1;
				int k4 = l1.endwaypspr;
				int j6;
				if(k4 < k1)
					j6 = k1 - k4;
				else
					j6 = (10 + k1) - k4;
				int j7 = 4;
				if(j6 > 2)
					j7 = (j6 - 1) * 4;
				if(l1.xwayp[k4] - l1.x > regionarea * 3 || l1.ywayp[k4] - l1.y > regionarea * 3 || l1.xwayp[k4] - l1.x < -regionarea * 3 || l1.ywayp[k4] - l1.y < -regionarea * 3 || j6 > 8)
				{
					l1.x = l1.xwayp[k4];
					l1.y = l1.ywayp[k4];
				} else
				{
					if(l1.x < l1.xwayp[k4])
					{
						l1.x += j7;
						l1.jr++;
						j2 = 2;
					} else
						if(l1.x > l1.xwayp[k4])
						{
							l1.x -= j7;
							l1.jr++;
							j2 = 6;
						}
					if(l1.x - l1.xwayp[k4] < j7 && l1.x - l1.xwayp[k4] > -j7)
						l1.x = l1.xwayp[k4];
					if(l1.y < l1.ywayp[k4])
					{
						l1.y += j7;
						l1.jr++;
						if(j2 == -1)
							j2 = 4;
						else
							if(j2 == 2)
								j2 = 3;
							else
								j2 = 5;
					} else
						if(l1.y > l1.ywayp[k4])
						{
							l1.y -= j7;
							l1.jr++;
							if(j2 == -1)
								j2 = 0;
							else
								if(j2 == 2)
									j2 = 1;
								else
									j2 = 7;
						}
					if(l1.y - l1.ywayp[k4] < j7 && l1.y - l1.ywayp[k4] > -j7)
						l1.y = l1.ywayp[k4];
				}
				if(j2 != -1)
					l1.kr = j2;
				if(l1.x == l1.xwayp[k4] && l1.y == l1.ywayp[k4])
					l1.endwaypspr = (k4 + 1) % 10;
			} else
			{
				l1.kr = l1.nextspr;
			}
			if(l1.sr > 0)
				l1.sr--;
			if(l1.ur > 0)
				l1.ur--;
			if(l1.yr > 0)
				l1.yr--;
			if(gdb > 0)
			{
				gdb--;
				if(gdb == 0)
					ik("You have been granted another life. Be more careful this time!", 3);
				if(gdb == 0)
					ik("You retain your skills. Your objects land where you died", 3);
			}
		}

		for(int j1 = 0; j1 < hw; j1++)
		{
			livingentity l2 = kw[j1];
			int k2 = (l2.curwayp + 1) % 10;
			if(l2.endwaypspr != k2)
			{
				int l4 = -1;
				int k6 = l2.endwaypspr;
				int k7;
				if(k6 < k2)
					k7 = k2 - k6;
				else
					k7 = (10 + k2) - k6;
				int l7 = 4;
				if(k7 > 2)
					l7 = (k7 - 1) * 4;
				if(l2.xwayp[k6] - l2.x > regionarea * 3 || l2.ywayp[k6] - l2.y > regionarea * 3 || l2.xwayp[k6] - l2.x < -regionarea * 3 || l2.ywayp[k6] - l2.y < -regionarea * 3 || k7 > 8)
				{
					l2.x = l2.xwayp[k6];
					l2.y = l2.ywayp[k6];
				} else
				{
					if(l2.x < l2.xwayp[k6])
					{
						l2.x += l7;
						l2.jr++;
						l4 = 2;
					} else
						if(l2.x > l2.xwayp[k6])
						{
							l2.x -= l7;
							l2.jr++;
							l4 = 6;
						}
					if(l2.x - l2.xwayp[k6] < l7 && l2.x - l2.xwayp[k6] > -l7)
						l2.x = l2.xwayp[k6];
					if(l2.y < l2.ywayp[k6])
					{
						l2.y += l7;
						l2.jr++;
						if(l4 == -1)
							l4 = 4;
						else
							if(l4 == 2)
								l4 = 3;
							else
								l4 = 5;
					} else
						if(l2.y > l2.ywayp[k6])
						{
							l2.y -= l7;
							l2.jr++;
							if(l4 == -1)
								l4 = 0;
							else
								if(l4 == 2)
									l4 = 1;
								else
									l4 = 7;
						}
					if(l2.y - l2.ywayp[k6] < l7 && l2.y - l2.ywayp[k6] > -l7)
						l2.y = l2.ywayp[k6];
				}
				if(l4 != -1)
					l2.kr = l4;
				if(l2.x == l2.xwayp[k6] && l2.y == l2.ywayp[k6])
					l2.endwaypspr = (k6 + 1) % 10;
			} else
			{
				l2.kr = l2.nextspr;
				if(l2.ir == 43)
					l2.jr++;
			}
			if(l2.sr > 0)
				l2.sr--;
			if(l2.ur > 0)
				l2.ur--;
			if(l2.yr > 0)
				l2.yr--;
		}

		for(int i2 = 0; i2 < pcount; i2++)
		{
			livingentity l3 = players[i2];
			if(l3.hs > 0)
				l3.hs--;
		}

		if(ry)
		{
			if(ourx - ourplayer.x < -500 || ourx - ourplayer.x > 500 || oury - ourplayer.y < -500 || oury - ourplayer.y > 500)
			{
				ourx = ourplayer.x;
				oury = ourplayer.y;
			}
		} else
		{
			if(ourx - ourplayer.x < -500 || ourx - ourplayer.x > 500 || oury - ourplayer.y < -500 || oury - ourplayer.y > 500)
			{
				ourx = ourplayer.x;
				oury = ourplayer.y;
			}
			if(ourx != ourplayer.x)
				ourx += (ourplayer.x - ourx) / (16 + (lv - 500) / 15);
			if(oury != ourplayer.y)
				oury += (ourplayer.y - oury) / (16 + (lv - 500) / 15);
			if(cameraauto)
			{
				int i3 = pv * 32;
				int i5 = i3 - rv;
				byte byte0 = 1;
				if(i5 != 0)
				{
					qv++;
					if(i5 > 128)
					{
						byte0 = -1;
						i5 = 256 - i5;
					} else
						if(i5 > 0)
							byte0 = 1;
						else
							if(i5 < -128)
							{
								byte0 = 1;
								i5 = 256 + i5;
							} else
								if(i5 < 0)
								{
									byte0 = -1;
									i5 = -i5;
								}
					rv += ((qv * i5 + 255) / 256) * byte0;
					rv &= 0xff;
				} else
				{
					qv = 0;
				}
			}
		}
		if(mousey > height - 4)
		{
			if(mousex > 15 && mousex < 96 && lastclick == 1)
				uz = 0;
			if(mousex > 110 && mousex < 194 && lastclick == 1)
			{
				uz = 1;
				pz.bf[qz] = 0xf423f;
			}
			if(mousex > 215 && mousex < 295 && lastclick == 1)
			{
				uz = 2;
				pz.bf[sz] = 0xf423f;
			}
			if(mousex > 315 && mousex < 395 && lastclick == 1)
			{
				uz = 3;
				pz.bf[tz] = 0xf423f;
			}
			lastclick = 0;
			mouseclick = 0;
		}
		pz.pd(mousex, mousey, lastclick, mouseclick);
		if(uz > 0 && mousex >= 494 && mousey >= height - 66)
			lastclick = 0;
		if(pz.rd(rz))
		{
			String s1 = pz.pc(rz);
			pz.kd(rz, "");
			if(s1.startsWith("::"))
			{
				if(s1.equalsIgnoreCase("::lostcon") && !live)
					stream.close();
				else
					if(s1.equalsIgnoreCase("::closecon") && !live)
						sendlogout();
					else
						sendcmd(s1.substring(2));
			} else
				if(s1.startsWith("reportabuse "))
				{
					s1 = s1.substring(12);
					long l5 = util.encodeb37(s1);
					stream.create(10);
					stream.p8(l5);
					stream.fmtdata();
				} else
				{
					int j5 = util.strlen(s1);
					sendchat(util.lastcm, j5);
					s1 = util.nn(util.lastcm, 0, j5, true);
					ourplayer.sr = 150;
					ourplayer.rr = s1;
					ik(ourplayer.dr + ": " + s1, 2);
				}
		}
		if(uz == 0)
		{
			for(int j3 = 0; j3 < vz; j3++)
				if(xz[j3] > 0)
					xz[j3]--;

		}
		if(gdb != 0)
			lastclick = 0;
		if(zab || yz)
		{
			if(mouseclick != 0)
				jbb++;
			else
				jbb = 0;
			if(jbb > 300)
				kbb += 50;
			else
				if(jbb > 150)
					kbb += 5;
				else
					if(jbb > 50)
						kbb++;
					else
						if(jbb > 20 && (jbb & 5) == 0)
							kbb++;
		} else
		{
			jbb = 0;
			kbb = 0;
		}
		if(lastclick == 1)
			mt = 1;
		else
			if(lastclick == 2)
				mt = 2;
		camera.wh(mousex, mousey);
		lastclick = 0;
		if(cameraauto)
		{
			if(qv == 0 || ry)
			{
				if(leftkey)
				{
					pv = pv + 1 & 7;
					leftkey = false;
					if(!mv)
					{
						if((pv & 1) == 0)
							pv = pv + 1 & 7;
						for(int k3 = 0; k3 < 8; k3++)
						{
							if(dm(pv))
								break;
							pv = pv + 1 & 7;
						}

					}
				}
				if(rightkey)
				{
					pv = pv + 7 & 7;
					rightkey = false;
					if(!mv)
					{
						if((pv & 1) == 0)
							pv = pv + 7 & 7;
						for(int i4 = 0; i4 < 8; i4++)
						{
							if(dm(pv))
								break;
							pv = pv + 7 & 7;
						}

					}
				}
			}
		} else
			if(leftkey)
				rv = rv + 2 & 0xff;
			else
				if(rightkey)
					rv = rv - 2 & 0xff;
		if(mv && lv > 550)
			lv -= 4;
		else
			if(!mv && lv < 750)
				lv += 4;
		if(tu > 0)
			tu--;
		else
			if(tu < 0)
				tu++;
		camera.gi(17);
		ou++;
		if(ou > 5)
		{
			ou = 0;
			pu = pu + 1 & 3;
			qu = (qu + 1) % 3;
		}
		for(int j4 = 0; j4 < uw; j4++)
		{
			int k5 = ww[j4];
			int l6 = xw[j4];
			if(k5 >= 0 && l6 >= 0 && k5 < 96 && l6 < 96 && yw[j4] == 74)
				vw[j4].ve(1, 0, 0);
		}

		for(int i6 = 0; i6 < ihb; i6++)
		{
			lhb[i6]++;
			if(lhb[i6] > 50)
			{
				ihb--;
				for(int i7 = i6; i7 < ihb; i7++)
				{
					jhb[i7] = jhb[i7 + 1];
					khb[i7] = khb[i7 + 1];
					lhb[i7] = lhb[i7 + 1];
					mhb[i7] = mhb[i7 + 1];
				}

			}
		}

	}

	public void ik(String s1, int i1)
	{
		if(i1 == 2 || i1 == 4 || i1 == 6)
		{
			for(; s1.length() > 5 && s1.charAt(0) == '@' && s1.charAt(4) == '@'; s1 = s1.substring(5));
			int j1 = s1.indexOf(":");
			if(j1 != -1)
			{
				String s2 = s1.substring(0, j1);
				long l1 = util.encodeb37(s2);
				for(int i2 = 0; i2 < ignorecnt; i2++)
					if(ignores[i2] == l1)
						return;

			}
		}
		if(i1 == 2)
			s1 = "@yel@" + s1;
		if(i1 == 3 || i1 == 4)
			s1 = "@whi@" + s1;
		if(i1 == 6)
			s1 = "@cya@" + s1;
		if(uz != 0)
		{
			if(i1 == 4 || i1 == 3)
				lz = 200;
			if(i1 == 2 && uz != 1)
				mz = 200;
			if(i1 == 5 && uz != 2)
				nz = 200;
			if(i1 == 6 && uz != 3)
				oz = 200;
			if(i1 == 3 && uz != 0)
				uz = 0;
			if(i1 == 6 && uz != 3 && uz != 0)
				uz = 0;
		}
		for(int k1 = vz - 1; k1 > 0; k1--)
		{
			wz[k1] = wz[k1 - 1];
			xz[k1] = xz[k1 - 1];
		}

		wz[0] = s1;
		xz[0] = 300;
		if(i1 == 2)
			if(pz.bf[qz] == pz.cf[qz] - 4)
				pz.gc(qz, s1, true);
			else
				pz.gc(qz, s1, false);
		if(i1 == 5)
			if(pz.bf[sz] == pz.cf[sz] - 4)
				pz.gc(sz, s1, true);
			else
				pz.gc(sz, s1, false);
		if(i1 == 6)
		{
			if(pz.bf[tz] == pz.cf[tz] - 4)
			{
				pz.gc(tz, s1, true);
				return;
			}
			pz.gc(tz, s1, false);
		}
	}

	public void showmsg(String s1)
	{
		if(s1.startsWith("@bor@"))
		{
			ik(s1, 4);
			return;
		}
		if(s1.startsWith("@que@"))
		{
			ik("@whi@" + s1, 5);
			return;
		}
		if(s1.startsWith("@pri@"))
		{
			ik(s1, 6);
			return;
		} else
		{
			ik(s1, 3);
			return;
		}
	}

	public livingentity mkplayer(int i1, int j1, int k1, int l1)
	{
		if(newplayers[i1] == null)
		{
			newplayers[i1] = new livingentity();
			newplayers[i1].serverindex = i1;
			newplayers[i1].serverid = 0;
		}
		livingentity l2 = newplayers[i1];
		boolean flag = false;
		for(int i2 = 0; i2 < lastpcount; i2++)
		{
			if(lastplayers[i2].serverindex != i1)
				continue;
			flag = true;
			break;
		}

		if(flag)
		{
			l2.nextspr = l1;
			int j2 = l2.curwayp;
			if(j1 != l2.xwayp[j2] || k1 != l2.ywayp[j2])
			{
				l2.curwayp = j2 = (j2 + 1) % 10;
				l2.xwayp[j2] = j1;
				l2.ywayp[j2] = k1;
			}
		} else
		{
			l2.serverindex = i1;
			l2.endwaypspr = 0;
			l2.curwayp = 0;
			l2.xwayp[0] = l2.x = j1;
			l2.ywayp[0] = l2.y = k1;
			l2.nextspr = l2.kr = l1;
			l2.jr = 0;
		}
		players[pcount++] = l2;
		return l2;
	}

	public livingentity om(int i1, int j1, int k1, int l1, int i2)
	{
		if(jw[i1] == null)
		{
			jw[i1] = new livingentity();
			jw[i1].serverindex = i1;
		}
		livingentity l2 = jw[i1];
		boolean flag = false;
		for(int j2 = 0; j2 < iw; j2++)
		{
			if(lw[j2].serverindex != i1)
				continue;
			flag = true;
			break;
		}

		if(flag)
		{
			l2.ir = i2;
			l2.nextspr = l1;
			int k2 = l2.curwayp;
			if(j1 != l2.xwayp[k2] || k1 != l2.ywayp[k2])
			{
				l2.curwayp = k2 = (k2 + 1) % 10;
				l2.xwayp[k2] = j1;
				l2.ywayp[k2] = k1;
			}
		} else
		{
			l2.serverindex = i1;
			l2.endwaypspr = 0;
			l2.curwayp = 0;
			l2.xwayp[0] = l2.x = j1;
			l2.ywayp[0] = l2.y = k1;
			l2.ir = i2;
			l2.nextspr = l2.kr = l1;
			l2.jr = 0;
		}
		kw[hw++] = l2;
		return l2;
	}

	public void onpacket(int id, int len, byte[] buf) {
		try {
			System.out.println("got packet id: " + id);
			if (id == 255) {
				System.out.println("got position packet!");
				lastpcount = pcount;
				for (int i = 0; i < lastpcount; i++) {
					lastplayers[i] = players[i];
				}

				int offset = 8;
				regionx = util.gbits(buf, offset, 10);
				offset += 10;
				regiony = util.gbits(buf, offset, 12);
				System.out.println("x: " + regionx + ", y: " + regiony);
				offset += 12;
				int sprite = util.gbits(buf, offset, 4);
				offset += 4;
				boolean regionloaded = loadregion(regionx, regiony);
				regionx -= localx;
				regiony -= localy;
				int enterx = regionx * regionarea + 64;
				int entery = regiony * regionarea + 64;
				if (regionloaded) {
					ourplayer.curwayp = 0;
					ourplayer.endwaypspr = 0;
					ourplayer.x = ourplayer.xwayp[0] = enterx;
					ourplayer.y = ourplayer.ywayp[0] = entery;
				}
				pcount = 0;
				ourplayer = mkplayer(playerindex, enterx, entery, sprite);
				int knowncount = util.gbits(buf, offset, 8);
				offset += 8;
				for (int i = 0; i < knowncount; i++) {
					livingentity player = lastplayers[i + 1];
					int atpos = util.gbits(buf, offset, 1);
					offset++;
					if (atpos != 0) {
						int waypleft = util.gbits(buf, offset, 1);
						offset++;
						if (waypleft == 0) {
							int nextspr = util.gbits(buf, offset, 3);
							offset += 3;
							int curwayp = player.curwayp;
							int nextx = player.xwayp[curwayp];
							int nexty = player.ywayp[curwayp];
							if (nextspr == 2 || nextspr == 1 || nextspr == 3)
								nextx += regionarea;
							if (nextspr == 6 || nextspr == 5 || nextspr == 7)
								nextx -= regionarea;
							if (nextspr == 4 || nextspr == 3 || nextspr == 5)
								nexty += regionarea;
							if (nextspr == 0 || nextspr == 1 || nextspr == 7)
								nexty -= regionarea;
							player.nextspr = nextspr;
							player.curwayp = curwayp = (curwayp + 1) % 10;
							player.xwayp[curwayp] = nextx;
							player.ywayp[curwayp] = nexty;
						} else {
							int nextspr = util.gbits(buf, offset, 4);
							if ((nextspr & 0xc) == 12) {
								offset += 2;
								continue;
							}
							player.nextspr = util.gbits(buf, offset, 4);
							offset += 4;
						}
					}
					players[pcount++] = player;
				}

				int newcount = 0;
				while(offset + 24 < len * 8) {
					int index = util.gbits(buf, offset, 11);
					offset += 11;
					int localx = util.gbits(buf, offset, 5);
					offset += 5;
					if (localx > 15) {
						localx -= 32;
					}

					int localy = util.gbits(buf, offset, 5);
					offset += 5;
					if (localy > 15) {
						localy -= 32;
					}
					int spr = util.gbits(buf, offset, 4);
					offset += 4;
					int addindex = util.gbits(buf, offset, 1);
					offset++;
					int x = (regionx + localx) * regionarea + 64;
					int y = (regiony + localy) * regionarea + 64;
					mkplayer(index, x, y, spr);
					if (addindex == 0) {
						newpids[newcount++] = index;
					}
				}
				if(newcount > 0) {
					stream.create(254);
					stream.p2(newcount);
					for(int i = 0; i < newcount; i++) {
						livingentity player = newplayers[newpids[i]];
						stream.p2(player.serverindex);
						stream.p2(player.serverid);
					}
					stream.fmtdata();
					newcount = 0;
					return;
				}
			} else {
				if (id == 254) {
					for (int l1 = 1; l1 < len;)
						if (util.g1(buf[l1]) == 255) {
							int i9 = 0;
							int k15 = regionx + buf[l1 + 1] >> 3;
					int j20 = regiony + buf[l1 + 2] >> 3;
		l1 += 3;
		for (int k24 = 0; k24 < ow; k24++) {
			int j28 = (pw[k24] >> 3) - k15;
			int j31 = (qw[k24] >> 3) - j20;
			if (j28 != 0 || j31 != 0) {
				if (k24 != i9) {
					pw[i9] = pw[k24];
					qw[i9] = qw[k24];
					rw[i9] = rw[k24];
					sw[i9] = sw[k24];
				}
				i9++;
			}
		}
		ow = i9;
						} else {
							int j9 = util.g2(buf, l1);
							l1 += 2;
							int l15 = regionx + buf[l1++];
							int k20 = regiony + buf[l1++];
							if((j9 & 0x8000) == 0) {
								pw[ow] = l15;
								qw[ow] = k20;
								rw[ow] = j9;
								sw[ow] = 0;
								for(int l24 = 0; l24 < uw; l24++)
								{
									if(ww[l24] != l15 || xw[l24] != k20)
										continue;
									sw[ow] = cache.hlb[yw[l24]];
									break;
								}

								ow++;
							} else {
								j9 &= 0x7fff;
								int i25 = 0;
								for (int k28 = 0; k28 < ow; k28++) {
									if (pw[k28] != l15 || qw[k28] != k20 || rw[k28] != j9) {
										if (k28 != i25) {
											pw[i25] = pw[k28];
											qw[i25] = qw[k28];
											rw[i25] = rw[k28];
											sw[i25] = sw[k28];
										}
										i25++;
									} else {
										j9 = -123;
									}
								}
								ow = i25;
							}
						}
					return;
				}
				if(id == 253) {
					for(int i2 = 1; i2 < len;)
						if(util.g1(buf[i2]) == 255)
						{
							int k9 = 0;
							int i16 = regionx + buf[i2 + 1] >> 3;
					int l20 = regiony + buf[i2 + 2] >> 3;
								i2 += 3;
								for(int j25 = 0; j25 < uw; j25++)
								{
									int l28 = (ww[j25] >> 3) - i16;
									int k31 = (xw[j25] >> 3) - l20;
									if(l28 != 0 || k31 != 0)
									{
										if(j25 != k9)
										{
											vw[k9] = vw[j25];
											vw[k9].uh = k9;
											ww[k9] = ww[j25];
											xw[k9] = xw[j25];
											yw[k9] = yw[j25];
											zw[k9] = zw[j25];
										}
										k9++;
									} else
									{
										camera.zh(vw[j25]);
										landscape.fp(ww[j25], xw[j25], yw[j25]);
									}
								}

								uw = k9;
						} else
						{
							int l9 = util.g2(buf, i2);
							i2 += 2;
							int j16 = regionx + buf[i2++];
							int i21 = regiony + buf[i2++];
							int k25 = 0;
							for(int i29 = 0; i29 < uw; i29++)
								if(ww[i29] != j16 || xw[i29] != i21)
								{
									if(i29 != k25)
									{
										vw[k25] = vw[i29];
										vw[k25].uh = k25;
										ww[k25] = ww[i29];
										xw[k25] = xw[i29];
										yw[k25] = yw[i29];
										zw[k25] = zw[i29];
									}
									k25++;
								} else
								{
									camera.zh(vw[i29]);
									landscape.fp(ww[i29], xw[i29], yw[i29]);
								}

							uw = k25;
							if(l9 != 60000)
							{
								int l31 = landscape.io(j16, i21);
								int i36;
								int j39;
								if(l31 == 0 || l31 == 4)
								{
									i36 = cache.elb[l9];
									j39 = cache.flb[l9];
								} else
								{
									j39 = cache.elb[l9];
									i36 = cache.flb[l9];
								}
								int k42 = ((j16 + j16 + i36) * regionarea) / 2;
								int j44 = ((i21 + i21 + j39) * regionarea) / 2;
								int i46 = cache.dlb[l9];
								model h2 = gamemodels[i46].qe();
								camera.uh(h2);
								h2.uh = uw;
								h2.ve(0, l31 * 32, 0);
								h2.zd(k42, -landscape.oo(k42, j44), j44);
								h2.se(true, 48, 48, -50, -10, -50);
								landscape.vo(j16, i21, l9);
								if(l9 == 74)
									h2.zd(0, -480, 0);
								ww[uw] = j16;
								xw[uw] = i21;
								yw[uw] = l9;
								zw[uw] = l31;
								vw[uw++] = h2;
							}
						}

					return;
				}
				if(id == 252) {
					int j2 = 1;
					mx = buf[j2++] & 0xff;
					for(int i10 = 0; i10 < mx; i10++)
					{
						int k16 = util.g2(buf, j2);
						j2 += 2;
						nx[i10] = k16 & 0x7fff;
						px[i10] = k16 / 32768;
						if(cache.pjb[k16 & 0x7fff] == 0)
						{
							ox[i10] = util.getssmart(buf, j2);
							if(ox[i10] >= 128)
								j2 += 4;
							else
								j2++;
						} else
						{
							ox[i10] = 1;
						}
					}

					return;
				}
				if(id == 250) {
					int k2 = util.g2(buf, 1);
					int j10 = 3;
					for(int l16 = 0; l16 < k2; l16++)
					{
						int j21 = util.g2(buf, j10);
						j10 += 2;
						livingentity l25 = newplayers[j21];
						byte byte6 = buf[j10];
						j10++;
						if(byte6 == 0)
						{
							int i32 = util.g2(buf, j10);
							j10 += 2;
							if(l25 != null)
							{
								l25.ur = 150;
								l25.tr = i32;
							}
						} else
							if(byte6 == 1)
							{
								byte byte7 = buf[j10];
								j10++;
								if(l25 != null)
								{
									String s3 = util.nn(buf, j10, byte7, true);
									boolean flag3 = false;
									for(int l42 = 0; l42 < ignorecnt; l42++)
										if(ignores[l42] == l25.cr)
											flag3 = true;

									if(!flag3)
									{
										l25.sr = 150;
										l25.rr = s3;
										ik(l25.dr + ": " + l25.rr, 2);
									}
								}
								j10 += byte7;
							} else
								if(byte6 == 2)
								{
									int j32 = util.g1(buf[j10]);
									j10++;
									int j36 = util.g1(buf[j10]);
									j10++;
									int k39 = util.g1(buf[j10]);
									j10++;
									if(l25 != null)
									{
										l25.vr = j32;
										l25.wr = j36;
										l25.xr = k39;
										l25.yr = 200;
										if(l25 == ourplayer)
										{
											ux[3] = j36;
											vx[3] = k39;
											wcb = false;
											cdb = false;
										}
									}
								} else
									if(byte6 == 3)
									{
										int k32 = util.g2(buf, j10);
										j10 += 2;
										int k36 = util.g2(buf, j10);
										j10 += 2;
										if(l25 != null)
										{
											l25.es = k32;
											l25.gs = k36;
											l25.fs = -1;
											l25.hs = nu;
										}
									} else
										if(byte6 == 4)
										{
											int l32 = util.g2(buf, j10);
											j10 += 2;
											int l36 = util.g2(buf, j10);
											j10 += 2;
											if(l25 != null)
											{
												l25.es = l32;
												l25.fs = l36;
												l25.gs = -1;
												l25.hs = nu;
											}
										} else
											if(byte6 == 5)
											{
												if(l25 != null)
												{
													l25.serverid = util.g2(buf, j10);
													j10 += 2;
													l25.cr = util.g8(buf, j10);
													j10 += 8;
													l25.dr = util.decodeb37(l25.cr);
													int i33 = util.g1(buf[j10]);
													j10++;
													for(int i37 = 0; i37 < i33; i37++)
													{
														l25.qr[i37] = util.g1(buf[j10]);
														j10++;
													}

													for(int i40 = i33; i40 < 12; i40++)
														l25.qr[i40] = 0;

													l25.as = buf[j10++] & 0xff;
													l25.bs = buf[j10++] & 0xff;
													l25.cs = buf[j10++] & 0xff;
													l25.ds = buf[j10++] & 0xff;
													l25.zr = buf[j10++] & 0xff;
													l25.ks = buf[j10++] & 0xff;
												} else
												{
													j10 += 14;
													int j33 = util.g1(buf[j10]);
													j10 += j33 + 1;
												}
											} else
												if(byte6 == 6)
												{
													byte byte8 = buf[j10];
													j10++;
													if(l25 != null)
													{
														String s4 = util.nn(buf, j10, byte8, false);
														l25.sr = 150;
														l25.rr = s4;
														if(l25 == ourplayer)
															ik(l25.dr + ": " + l25.rr, 5);
													}
													j10 += byte8;
												}
					}

					return;
				}
				if(id == 249) {
					for(int l2 = 1; l2 < len;)
						if(util.g1(buf[l2]) == 255)
						{
							int k10 = 0;
							int i17 = regionx + buf[l2 + 1] >> 3;
					int k21 = regiony + buf[l2 + 2] >> 3;
													l2 += 3;
													for(int i26 = 0; i26 < dx; i26++)
													{
														int j29 = (fx[i26] >> 3) - i17;
														int k33 = (gx[i26] >> 3) - k21;
														if(j29 != 0 || k33 != 0)
														{
															if(i26 != k10)
															{
																ex[k10] = ex[i26];
																ex[k10].uh = k10 + 10000;
																fx[k10] = fx[i26];
																gx[k10] = gx[i26];
																hx[k10] = hx[i26];
																ix[k10] = ix[i26];
															}
															k10++;
														} else
														{
															camera.zh(ex[i26]);
															landscape.fo(fx[i26], gx[i26], hx[i26], ix[i26]);
														}
													}

													dx = k10;
						} else
						{
							int l10 = util.g2(buf, l2);
							l2 += 2;
							int j17 = regionx + buf[l2++];
							int l21 = regiony + buf[l2++];
							byte byte5 = buf[l2++];
							int k29 = 0;
							for(int l33 = 0; l33 < dx; l33++)
								if(fx[l33] != j17 || gx[l33] != l21 || hx[l33] != byte5)
								{
									if(l33 != k29)
									{
										ex[k29] = ex[l33];
										ex[k29].uh = k29 + 10000;
										fx[k29] = fx[l33];
										gx[k29] = gx[l33];
										hx[k29] = hx[l33];
										ix[k29] = ix[l33];
									}
									k29++;
								} else
								{
									camera.zh(ex[l33]);
									landscape.fo(fx[l33], gx[l33], hx[l33], ix[l33]);
								}

							dx = k29;
							if(l10 != 65535)
							{
								landscape.hp(j17, l21, byte5, l10);
								model h1 = cm(j17, l21, byte5, l10, dx);
								ex[dx] = h1;
								fx[dx] = j17;
								gx[dx] = l21;
								ix[dx] = l10;
								hx[dx++] = byte5;
							}
						}

					return;
				}
				if(id == 248) {
					iw = hw;
					hw = 0;
					for(int i3 = 0; i3 < iw; i3++)
						lw[i3] = kw[i3];

					int i11 = 8;
					int k17 = util.gbits(buf, i11, 8);
					i11 += 8;
					for(int i22 = 0; i22 < k17; i22++)
					{
						livingentity l26 = lw[i22];
						int l29 = util.gbits(buf, i11, 1);
						i11++;
						if(l29 != 0)
						{
							int i34 = util.gbits(buf, i11, 1);
							i11++;
							if(i34 == 0)
							{
								int j37 = util.gbits(buf, i11, 3);
								i11 += 3;
								int j40 = l26.curwayp;
								int i43 = l26.xwayp[j40];
								int k44 = l26.ywayp[j40];
								if(j37 == 2 || j37 == 1 || j37 == 3)
									i43 += regionarea;
								if(j37 == 6 || j37 == 5 || j37 == 7)
									i43 -= regionarea;
								if(j37 == 4 || j37 == 3 || j37 == 5)
									k44 += regionarea;
								if(j37 == 0 || j37 == 1 || j37 == 7)
									k44 -= regionarea;
								l26.nextspr = j37;
								l26.curwayp = j40 = (j40 + 1) % 10;
								l26.xwayp[j40] = i43;
								l26.ywayp[j40] = k44;
							} else
							{
								int k37 = util.gbits(buf, i11, 4);
								if((k37 & 0xc) == 12)
								{
									i11 += 2;
									continue;
								}
								l26.nextspr = util.gbits(buf, i11, 4);
								i11 += 4;
							}
						}
						kw[hw++] = l26;
					}

					while(i11 + 31 < len * 8) 
					{
						int j26 = util.gbits(buf, i11, 11);
						i11 += 11;
						int i30 = util.gbits(buf, i11, 5);
						i11 += 5;
						if(i30 > 15)
							i30 -= 32;
						int j34 = util.gbits(buf, i11, 5);
						i11 += 5;
						if(j34 > 15)
							j34 -= 32;
						int l37 = util.gbits(buf, i11, 4);
						i11 += 4;
						int k40 = (regionx + i30) * regionarea + 64;
						int j43 = (regiony + j34) * regionarea + 64;
						int i45 = util.gbits(buf, i11, 9);
						i11 += 9;
						if(i45 >= cache.vjb)
							i45 = 24;
						om(j26, k40, j43, l37, i45);
					}
					return;
				}
				if(id == 247) {
					int j3 = util.g2(buf, 1);
					int j11 = 3;
					for(int l17 = 0; l17 < j3; l17++)
					{
						int j22 = util.g2(buf, j11);
						j11 += 2;
						livingentity l27 = jw[j22];
						int j30 = util.g1(buf[j11]);
						j11++;
						if(j30 == 1)
						{
							int k34 = util.g2(buf, j11);
							j11 += 2;
							byte byte9 = buf[j11];
							j11++;
							if(l27 != null)
							{
								String s5 = util.nn(buf, j11, byte9, false);
								l27.sr = 150;
								l27.rr = s5;
								if(k34 == ourplayer.serverindex)
									ik("@yel@" + cache.wjb[l27.ir] + ": " + l27.rr, 5);
							}
							j11 += byte9;
						} else
							if(j30 == 2)
							{
								int l34 = util.g1(buf[j11]);
								j11++;
								int i38 = util.g1(buf[j11]);
								j11++;
								int l40 = util.g1(buf[j11]);
								j11++;
								if(l27 != null)
								{
									l27.vr = l34;
									l27.wr = i38;
									l27.xr = l40;
									l27.yr = 200;
								}
							}
					}

					return;
				}
				if(id == 246) {
					ncb = true;
					int k3 = util.g1(buf[1]);
					ocb = k3;
					int k11 = 2;
					for(int i18 = 0; i18 < k3; i18++)
					{
						int k22 = util.g1(buf[k11]);
						k11++;
						pcb[i18] = new String(buf, k11, k22);
						k11 += k22;
					}

					return;
				}
				if (id == 245) {
					ncb = false;
					return;
				}
				if (id == 244) {
					// world info
					loadregion = true;
					playerindex = util.g2(buf, 1);
					worldw = util.g2(buf, 3);
					worldh = util.g2(buf, 5);
					playerheight = util.g2(buf, 7);
					heightmod = util.g2(buf, 9);
					worldh -= playerheight * heightmod;
					return;
				}
				if(id == 243)
				{
					int l3 = 1;
					for(int l11 = 0; l11 < 18; l11++)
						ux[l11] = util.g1(buf[l3++]);

					for(int j18 = 0; j18 < 18; j18++)
						vx[j18] = util.g1(buf[l3++]);

					for(int l22 = 0; l22 < 18; l22++)
					{
						wx[l22] = util.g4(buf, l3);
						l3 += 4;
					}

					yx = util.g1(buf[l3++]);
					return;
				}
				if(id == 242)
				{
					for(int i4 = 0; i4 < 5; i4++)
						xx[i4] = util.g1(buf[1 + i4]);

					return;
				}
				if(id == 241)
				{
					gdb = 250;
					return;
				}
				if(id == 240)
				{
					int j4 = (len - 1) / 4;
					for(int i12 = 0; i12 < j4; i12++)
					{
						int k18 = regionx + util.gsmart(buf, 1 + i12 * 4) >> 3;
					int i23 = regiony + util.gsmart(buf, 3 + i12 * 4) >> 3;
					int k26 = 0;
					for(int k30 = 0; k30 < ow; k30++)
					{
						int i35 = (pw[k30] >> 3) - k18;
						int j38 = (qw[k30] >> 3) - i23;
						if(i35 != 0 || j38 != 0)
						{
							if(k30 != k26)
							{
								pw[k26] = pw[k30];
								qw[k26] = qw[k30];
								rw[k26] = rw[k30];
								sw[k26] = sw[k30];
							}
							k26++;
						}
					}

					ow = k26;
					k26 = 0;
					for(int j35 = 0; j35 < uw; j35++)
					{
						int k38 = (ww[j35] >> 3) - k18;
						int i41 = (xw[j35] >> 3) - i23;
						if(k38 != 0 || i41 != 0)
						{
							if(j35 != k26)
							{
								vw[k26] = vw[j35];
								vw[k26].uh = k26;
								ww[k26] = ww[j35];
								xw[k26] = xw[j35];
								yw[k26] = yw[j35];
								zw[k26] = zw[j35];
							}
							k26++;
						} else
						{
							camera.zh(vw[j35]);
							landscape.fp(ww[j35], xw[j35], yw[j35]);
						}
					}

					uw = k26;
					k26 = 0;
					for(int l38 = 0; l38 < dx; l38++)
					{
						int j41 = (fx[l38] >> 3) - k18;
						int k43 = (gx[l38] >> 3) - i23;
						if(j41 != 0 || k43 != 0)
						{
							if(l38 != k26)
							{
								ex[k26] = ex[l38];
								ex[k26].uh = k26 + 10000;
								fx[k26] = fx[l38];
								gx[k26] = gx[l38];
								hx[k26] = hx[l38];
								ix[k26] = ix[l38];
							}
							k26++;
						} else
						{
							camera.zh(ex[l38]);
							landscape.fo(fx[l38], gx[l38], hx[l38], ix[l38]);
						}
					}

					dx = k26;
					}

					return;
				}
				if(id == 239)
				{
					qgb = true;
					rgb = false;
					fl(false);
					return;
				}
				if(id == 238)
				{
					int k4 = util.g2(buf, 1);
					if(newplayers[k4] != null)
						abb = newplayers[k4].dr;
					zab = true;
					hbb = false;
					ibb = false;
					bbb = 0;
					ebb = 0;
					return;
				}
				if(id == 237)
				{
					zab = false;
					mbb = false;
					return;
				}
				if(id == 236)
				{
					ebb = buf[1] & 0xff;
					int l4 = 2;
					for(int j12 = 0; j12 < ebb; j12++)
					{
						fbb[j12] = util.g2(buf, l4);
						l4 += 2;
						gbb[j12] = util.g4(buf, l4);
						l4 += 4;
					}

					hbb = false;
					ibb = false;
					return;
				}
				if(id == 235)
				{
					byte byte0 = buf[1];
					if(byte0 == 1)
					{
						hbb = true;
						return;
					} else
					{
						hbb = false;
						return;
					}
				}
				if(id == 234)
				{
					ubb = true;
					int i5 = 1;
					int k12 = buf[i5++] & 0xff;
					byte byte4 = buf[i5++];
					vbb = buf[i5++] & 0xff;
					wbb = buf[i5++] & 0xff;
					for(int j23 = 0; j23 < 40; j23++)
						xbb[j23] = -1;

					for(int i27 = 0; i27 < k12; i27++)
					{
						xbb[i27] = util.g2(buf, i5);
						i5 += 2;
						ybb[i27] = util.g2(buf, i5);
						i5 += 2;
						zbb[i27] = buf[i5++];
					}

					if(byte4 == 1)
					{
						int l30 = 39;
						for(int k35 = 0; k35 < mx; k35++)
						{
							if(l30 < k12)
								break;
							boolean flag2 = false;
							for(int k41 = 0; k41 < 40; k41++)
							{
								if(xbb[k41] != nx[k35])
									continue;
								flag2 = true;
								break;
							}

							if(nx[k35] == 10)
								flag2 = true;
							if(!flag2)
							{
								xbb[l30] = nx[k35] & 0x7fff;
								ybb[l30] = 0;
								zbb[l30] = 0;
								l30--;
							}
						}

					}
					if(acb >= 0 && acb < 40 && xbb[acb] != bcb)
					{
						acb = -1;
						bcb = -2;
						return;
					}
				} else
				{
					if(id == 233)
					{
						ubb = false;
						return;
					}
					if(id == 229)
					{
						byte byte1 = buf[1];
						if(byte1 == 1)
						{
							ibb = true;
							return;
						} else
						{
							ibb = false;
							return;
						}
					}
					if(id == 228)
					{
						cameraauto = util.g1(buf[1]) == 1;
						mousebtns = util.g1(buf[2]) == 1;
						soundfx = util.g1(buf[3]) == 1;
						return;
					}
					if(id == 227)
					{
						for(int j5 = 0; j5 < len - 1; j5++)
						{
							boolean flag = buf[j5 + 1] == 1;
							if(!qy[j5] && flag)
								playsound("prayeron");
							if(qy[j5] && !flag)
								playsound("prayeroff");
							qy[j5] = flag;
						}

						return;
					}
					if(id == 226)
					{
						for(int k5 = 0; k5 < ny; k5++)
							py[k5] = buf[k5 + 1] == 1;

						return;
					}
					if(id == 224)
					{
						zeb = true;
						for(int l5 = 0; l5 < 5; l5++)
						{
							ifb[l5] = l5;
							jfb[l5] = "~:" + ifb[l5];
							afb.kd(ffb[l5], "");
							afb.kd(efb[l5], (l5 + 1) + ": " + recovquestions[ifb[l5]]);
						}

						return;
					}
					if(id == 222)
					{
						ccb = true;
						int i6 = 1;
						dcb = buf[i6++] & 0xff;
						lcb = buf[i6++] & 0xff;
						for(int l12 = 0; l12 < dcb; l12++)
						{
							ecb[l12] = util.g2(buf, i6);
							i6 += 2;
							fcb[l12] = util.getssmart(buf, i6);
							if(fcb[l12] >= 128)
								i6 += 4;
							else
								i6++;
						}

						ol();
						return;
					}
					if(id == 221)
					{
						ccb = false;
						return;
					}
					if(id == 220)
					{
						int j6 = buf[1] & 0xff;
						wx[j6] = util.g4(buf, 2);
						return;
					}
					if(id == 219)
					{
						int k6 = util.g2(buf, 1);
						if(newplayers[k6] != null)
							zz = newplayers[k6].dr;
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
					if(id == 218)
					{
						yz = false;
						mab = false;
						return;
					}
					if(id == 217)
					{
						mbb = true;
						nbb = false;
						zab = false;
						int l6 = 1;
						lbb = util.g8(buf, l6);
						l6 += 8;
						rbb = buf[l6++] & 0xff;
						for(int i13 = 0; i13 < rbb; i13++)
						{
							sbb[i13] = util.g2(buf, l6);
							l6 += 2;
							tbb[i13] = util.g4(buf, l6);
							l6 += 4;
						}

						obb = buf[l6++] & 0xff;
						for(int l18 = 0; l18 < obb; l18++)
						{
							pbb[l18] = util.g2(buf, l6);
							l6 += 2;
							qbb[l18] = util.g4(buf, l6);
							l6 += 4;
						}

						return;
					}
					if(id == 216)
					{
						dab = buf[1] & 0xff;
						int i7 = 2;
						for(int j13 = 0; j13 < dab; j13++)
						{
							eab[j13] = util.g2(buf, i7);
							i7 += 2;
							fab[j13] = util.g4(buf, i7);
							i7 += 4;
						}

						gab = false;
						hab = false;
						return;
					}
					if(id == 215)
					{
						if(buf[1] == 1)
							iab = true;
						else
							iab = false;
						if(buf[2] == 1)
							jab = true;
						else
							jab = false;
						if(buf[3] == 1)
							kab = true;
						else
							kab = false;
						if(buf[4] == 1)
							lab = true;
						else
							lab = false;
						gab = false;
						hab = false;
						return;
					}
					if(id == 214)
					{
						int j7 = 1;
						int k13 = buf[j7++] & 0xff;
						int i19 = util.g2(buf, j7);
						j7 += 2;
						int k23 = util.getssmart(buf, j7);
						if(k23 >= 128)
							j7 += 4;
						else
							j7++;
						if(k23 == 0)
						{
							dcb--;
							for(int j27 = k13; j27 < dcb; j27++)
							{
								ecb[j27] = ecb[j27 + 1];
								fcb[j27] = fcb[j27 + 1];
							}

						} else
						{
							ecb[k13] = i19;
							fcb[k13] = k23;
							if(k13 >= dcb)
								dcb = k13 + 1;
						}
						ol();
						return;
					}
					if(id == 213)
					{
						int k7 = 1;
						int l13 = 1;
						int j19 = buf[k7++] & 0xff;
						int l23 = util.g2(buf, k7);
						k7 += 2;
						if(cache.pjb[l23 & 0x7fff] == 0)
						{
							l13 = util.getssmart(buf, k7);
							if(l13 >= 128)
								k7 += 4;
							else
								k7++;
						}
						nx[j19] = l23 & 0x7fff;
						px[j19] = l23 / 32768;
						ox[j19] = l13;
						if(j19 >= mx)
						{
							mx = j19 + 1;
							return;
						}
					} else
					{
						if(id == 212)
						{
							int l7 = buf[1] & 0xff;
							mx--;
							for(int i14 = l7; i14 < mx; i14++)
							{
								nx[i14] = nx[i14 + 1];
								ox[i14] = ox[i14 + 1];
								px[i14] = px[i14 + 1];
							}

							return;
						}
						if(id == 211)
						{
							int i8 = 1;
							int j14 = buf[i8++] & 0xff;
							ux[j14] = util.g1(buf[i8++]);
							vx[j14] = util.g1(buf[i8++]);
							wx[j14] = util.g4(buf, i8);
							i8 += 4;
							return;
						}
						if(id == 210)
						{
							byte byte2 = buf[1];
							if(byte2 == 1)
							{
								gab = true;
								return;
							} else
							{
								gab = false;
								return;
							}
						}
						if(id == 209)
						{
							byte byte3 = buf[1];
							if(byte3 == 1)
							{
								hab = true;
								return;
							} else
							{
								hab = false;
								return;
							}
						}
						if(id == 208)
						{
							mab = true;
							nab = false;
							yz = false;
							int j8 = 1;
							oab = util.g8(buf, j8);
							j8 += 8;
							sab = buf[j8++] & 0xff;
							for(int k14 = 0; k14 < sab; k14++)
							{
								tab[k14] = util.g2(buf, j8);
								j8 += 2;
								uab[k14] = util.g4(buf, j8);
								j8 += 4;
							}

							pab = buf[j8++] & 0xff;
							for(int k19 = 0; k19 < pab; k19++)
							{
								qab[k19] = util.g2(buf, j8);
								j8 += 2;
								rab[k19] = util.g4(buf, j8);
								j8 += 4;
							}

							vab = buf[j8++] & 0xff;
							wab = buf[j8++] & 0xff;
							xab = buf[j8++] & 0xff;
							yab = buf[j8++] & 0xff;
							return;
						}
						if(id == 207)
						{
							String s1 = new String(buf, 1, len - 1);
							playsound(s1);
							return;
						}
						if(id == 206)
						{
							if(ihb < 50)
							{
								int k8 = buf[1] & 0xff;
								int l14 = buf[2] + regionx;
								int l19 = buf[3] + regiony;
								mhb[ihb] = k8;
								lhb[ihb] = 0;
								jhb[ihb] = l14;
								khb[ihb] = l19;
								ihb++;
								return;
							}
						} else
							if(id == 205)
							{
								if(!vcb)
								{
									zcb = util.g4(buf, 1);
									adb = util.g4(buf, 5);
									xcb = util.g4(buf, 9);
									bdb = (int)(Math.random() * 6D);
									wcb = true;
									vcb = true;
									ycb = null;
									return;
								}
							} else
							{
								if(id == 204)
								{
									ddb = new String(buf, 1, len - 1);
									cdb = true;
									return;
								}
								if(id == 203)
								{
									qgb = true;
									rgb = true;
									fl(true);
								}
							}
					}
				}
			}
			return;
		}
		catch(RuntimeException runtimeexception)
		{
			if(gt < 3)
			{
				stream.create(17);
				stream.pjstr(runtimeexception.toString());
				stream.fmtdata();
				stream.create(17);
				stream.pjstr("p-type:" + id + " p-size:" + len);
				stream.fmtdata();
				stream.create(17);
				stream.pjstr("rx:" + regionx + " ry:" + regiony + " num3l:" + uw);
				stream.fmtdata();
				String s2 = "";
				for(int i20 = 0; i20 < 80 && i20 < len; i20++)
					s2 = s2 + buf[i20] + " ";

				stream.create(17);
				stream.pjstr(s2);
				stream.fmtdata();
				gt++;
			}
		}
	}

	public void ol()
	{
		gcb = dcb;
		for(int i1 = 0; i1 < dcb; i1++)
		{
			hcb[i1] = ecb[i1];
			icb[i1] = fcb[i1];
		}

		for(int j1 = 0; j1 < mx; j1++)
		{
			if(gcb >= lcb)
				break;
			int k1 = nx[j1];
			boolean flag = false;
			for(int l1 = 0; l1 < gcb; l1++)
			{
				if(hcb[l1] != k1)
					continue;
				flag = true;
				break;
			}

			if(!flag)
			{
				hcb[gcb] = k1;
				icb[gcb] = 0;
				gcb++;
			}
		}

	}

	public boolean dm(int i1)
	{
		int j1 = ourplayer.x / 128;
		int k1 = ourplayer.y / 128;
		for(int l1 = 2; l1 >= 1; l1--)
		{
			if(i1 == 1 && ((landscape.ajb[j1][k1 - l1] & 0x80) == 128 || (landscape.ajb[j1 - l1][k1] & 0x80) == 128 || (landscape.ajb[j1 - l1][k1 - l1] & 0x80) == 128))
				return false;
			if(i1 == 3 && ((landscape.ajb[j1][k1 + l1] & 0x80) == 128 || (landscape.ajb[j1 - l1][k1] & 0x80) == 128 || (landscape.ajb[j1 - l1][k1 + l1] & 0x80) == 128))
				return false;
			if(i1 == 5 && ((landscape.ajb[j1][k1 + l1] & 0x80) == 128 || (landscape.ajb[j1 + l1][k1] & 0x80) == 128 || (landscape.ajb[j1 + l1][k1 + l1] & 0x80) == 128))
				return false;
			if(i1 == 7 && ((landscape.ajb[j1][k1 - l1] & 0x80) == 128 || (landscape.ajb[j1 + l1][k1] & 0x80) == 128 || (landscape.ajb[j1 + l1][k1 - l1] & 0x80) == 128))
				return false;
			if(i1 == 0 && (landscape.ajb[j1][k1 - l1] & 0x80) == 128)
				return false;
			if(i1 == 2 && (landscape.ajb[j1 - l1][k1] & 0x80) == 128)
				return false;
			if(i1 == 4 && (landscape.ajb[j1][k1 + l1] & 0x80) == 128)
				return false;
			if(i1 == 6 && (landscape.ajb[j1 + l1][k1] & 0x80) == 128)
				return false;
		}

		return true;
	}

	public void ll()
	{
		if((pv & 1) == 1 && dm(pv))
			return;
		if((pv & 1) == 0 && dm(pv))
		{
			if(dm(pv + 1 & 7))
			{
				pv = pv + 1 & 7;
				return;
			}
			if(dm(pv + 7 & 7))
				pv = pv + 7 & 7;
			return;
		}
		int ai[] = {
				1, -1, 2, -2, 3, -3, 4
		};
		for(int i1 = 0; i1 < 7; i1++)
		{
			if(!dm(pv + ai[i1] + 8 & 7))
				continue;
			pv = pv + ai[i1] + 8 & 7;
			break;
		}

		if((pv & 1) == 0 && dm(pv))
		{
			if(dm(pv + 1 & 7))
			{
				pv = pv + 1 & 7;
				return;
			}
			if(dm(pv + 7 & 7))
				pv = pv + 7 & 7;
			return;
		} else
		{
			return;
		}
	}

	public void yk()
	{
		if(gdb != 0)
		{
			gamegfx.fadepixels();
			gamegfx.ug("Oh dear! You are dead...", width / 2, height / 2, 7, 0xff0000);
			al();
			gamegfx.drawimg(gfx, 0, 0);
			return;
		}
		if(qgb)
		{
			jk();
			return;
		}
		if(zeb)
		{
			fk();
			return;
		}
		if(!landscape.cjb)
			return;
		for(int i1 = 0; i1 < 64; i1++)
		{
			camera.zh(landscape.fjb[av][i1]);
			if(av == 0)
			{
				camera.zh(landscape.ejb[1][i1]);
				camera.zh(landscape.fjb[1][i1]);
				camera.zh(landscape.ejb[2][i1]);
				camera.zh(landscape.fjb[2][i1]);
			}
			mv = true;
			if(av == 0 && (landscape.ajb[ourplayer.x / 128][ourplayer.y / 128] & 0x80) == 0)
			{
				camera.uh(landscape.fjb[av][i1]);
				if(av == 0)
				{
					camera.uh(landscape.ejb[1][i1]);
					camera.uh(landscape.fjb[1][i1]);
					camera.uh(landscape.ejb[2][i1]);
					camera.uh(landscape.fjb[2][i1]);
				}
				mv = false;
			}
		}

		if(pu != ru)
		{
			ru = pu;
			for(int j1 = 0; j1 < uw; j1++)
			{
				if(yw[j1] == 51)
				{
					int i2 = ww[j1];
					int j3 = xw[j1];
					int j5 = i2 - ourplayer.x / 128;
					int i7 = j3 - ourplayer.y / 128;
					byte byte0 = 7;
					if(i2 >= 0 && j3 >= 0 && i2 < 96 && j3 < 96 && j5 > -byte0 && j5 < byte0 && i7 > -byte0 && i7 < byte0)
					{
						camera.zh(vw[j1]);
						String s1 = "torcha" + (pu + 1);
						int i14 = cache.pushmodel(s1);
						model h1 = gamemodels[i14].qe();
						camera.uh(h1);
						h1.se(true, 48, 48, -50, -10, -50);
						h1.yd(vw[j1]);
						h1.uh = j1;
						vw[j1] = h1;
					}
				}
				if(yw[j1] == 143)
				{
					int j2 = ww[j1];
					int k3 = xw[j1];
					int k5 = j2 - ourplayer.x / 128;
					int j7 = k3 - ourplayer.y / 128;
					byte byte1 = 7;
					if(j2 >= 0 && k3 >= 0 && j2 < 96 && k3 < 96 && k5 > -byte1 && k5 < byte1 && j7 > -byte1 && j7 < byte1)
					{
						camera.zh(vw[j1]);
						String s2 = "skulltorcha" + (pu + 1);
						int j14 = cache.pushmodel(s2);
						model h2 = gamemodels[j14].qe();
						camera.uh(h2);
						h2.se(true, 48, 48, -50, -10, -50);
						h2.yd(vw[j1]);
						h2.uh = j1;
						vw[j1] = h2;
					}
				}
			}

		}
		if(qu != su)
		{
			su = qu;
			for(int k1 = 0; k1 < uw; k1++)
			{
				if(yw[k1] == 97)
				{
					int k2 = ww[k1];
					int i4 = xw[k1];
					int l5 = k2 - ourplayer.x / 128;
					int k7 = i4 - ourplayer.y / 128;
					byte byte2 = 9;
					if(k2 >= 0 && i4 >= 0 && k2 < 96 && i4 < 96 && l5 > -byte2 && l5 < byte2 && k7 > -byte2 && k7 < byte2)
					{
						camera.zh(vw[k1]);
						String s3 = "firea" + (qu + 1);
						int k14 = cache.pushmodel(s3);
						model h3 = gamemodels[k14].qe();
						camera.uh(h3);
						h3.se(true, 48, 48, -50, -10, -50);
						h3.yd(vw[k1]);
						h3.uh = k1;
						vw[k1] = h3;
					}
				}
				if(yw[k1] == 274)
				{
					int l2 = ww[k1];
					int j4 = xw[k1];
					int i6 = l2 - ourplayer.x / 128;
					int i8 = j4 - ourplayer.y / 128;
					byte byte3 = 9;
					if(l2 >= 0 && j4 >= 0 && l2 < 96 && j4 < 96 && i6 > -byte3 && i6 < byte3 && i8 > -byte3 && i8 < byte3)
					{
						camera.zh(vw[k1]);
						String s4 = "fireplacea" + (qu + 1);
						int l14 = cache.pushmodel(s4);
						model h4 = gamemodels[l14].qe();
						camera.uh(h4);
						h4.se(true, 48, 48, -50, -10, -50);
						h4.yd(vw[k1]);
						h4.uh = k1;
						vw[k1] = h4;
					}
				}
			}

		}
		camera.ki(xv);
		xv = 0;
		for(int l1 = 0; l1 < pcount; l1++)
		{
			livingentity l3 = players[l1];
			if(l3.cs != 255)
			{
				int k4 = l3.x;
				int j6 = l3.y;
				int j8 = -landscape.oo(k4, j6);
				int k9 = camera.lh(5000 + l1, k4, j8, j6, 145, 220, l1 + 10000);
				xv++;
				if(l3 == ourplayer)
					camera.mh(k9);
				if(l3.kr == 8)
					camera.ni(k9, -30);
				if(l3.kr == 9)
					camera.ni(k9, 30);
			}
		}

		for(int i3 = 0; i3 < pcount; i3++)
		{
			livingentity l4 = players[i3];
			if(l4.hs > 0)
			{
				livingentity l6 = null;
				if(l4.gs != -1)
					l6 = jw[l4.gs];
				else
					if(l4.fs != -1)
						l6 = newplayers[l4.fs];
				if(l6 != null)
				{
					int k8 = l4.x;
					int l9 = l4.y;
					int j12 = -landscape.oo(k8, l9) - 110;
					int i15 = l6.x;
					int l15 = l6.y;
					int i16 = -landscape.oo(i15, l15) - cache.kkb[l6.ir] / 2;
					int j16 = (k8 * l4.hs + i15 * (nu - l4.hs)) / nu;
					int k16 = (j12 * l4.hs + i16 * (nu - l4.hs)) / nu;
					int l16 = (l9 * l4.hs + l15 * (nu - l4.hs)) / nu;
					camera.lh(ku + l4.es, j16, k16, l16, 32, 32, 0);
					xv++;
				}
			}
		}

		for(int i5 = 0; i5 < hw; i5++)
		{
			livingentity l7 = kw[i5];
			int l8 = l7.x;
			int i10 = l7.y;
			int k12 = -landscape.oo(l8, i10);
			int j15 = camera.lh(20000 + i5, l8, k12, i10, cache.jkb[l7.ir], cache.kkb[l7.ir], i5 + 30000);
			xv++;
			if(l7.kr == 8)
				camera.ni(j15, -30);
			if(l7.kr == 9)
				camera.ni(j15, 30);
		}

		for(int k6 = 0; k6 < ow; k6++)
		{
			int i9 = pw[k6] * regionarea + 64;
			int j10 = qw[k6] * regionarea + 64;
			camera.lh(40000 + rw[k6], i9, -landscape.oo(i9, j10) - sw[k6], j10, 96, 64, k6 + 20000);
			xv++;
		}

		for(int j9 = 0; j9 < ihb; j9++)
		{
			int k10 = jhb[j9] * regionarea + 64;
			int l12 = khb[j9] * regionarea + 64;
			int k15 = mhb[j9];
			if(k15 == 0)
			{
				camera.lh(50000 + j9, k10, -landscape.oo(k10, l12), l12, 128, 256, j9 + 50000);
				xv++;
			}
			if(k15 == 1)
			{
				camera.lh(50000 + j9, k10, -landscape.oo(k10, l12), l12, 128, 64, j9 + 50000);
				xv++;
			}
		}

		gamegfx.interlace = false;
		gamegfx.clear();
		gamegfx.interlace = f1key;
		if(av == 3)
		{
			int l10 = 40 + (int)(Math.random() * 3D);
			int i13 = 40 + (int)(Math.random() * 7D);
			camera.yi(l10, i13, -50, -10, -50);
		}
		egb = 0;
		yfb = 0;
		jgb = 0;
		if(ry)
		{
			if(cameraauto && !mv)
			{
				int i11 = pv;
				ll();
				if(pv != i11)
				{
					ourx = ourplayer.x;
					oury = ourplayer.y;
				}
			}
			camera.im = 3000;
			camera.jm = 3000;
			camera.km = 1;
			camera.lm = 2800;
			rv = pv * 32;
			int j11 = ourx + tt;
			int j13 = oury + vt;
			camera.ai(j11, -landscape.oo(j11, j13), j13, 912, rv * 4, 0, 2000);
		} else
		{
			if(cameraauto && !mv)
				ll();
			if(!f1key)
			{
				camera.im = 2400;
				camera.jm = 2400;
				camera.km = 1;
				camera.lm = 2300;
			} else
			{
				camera.im = 2200;
				camera.jm = 2200;
				camera.km = 1;
				camera.lm = 2100;
			}
			int k11 = ourx + tt;
			int k13 = oury + vt;
			camera.ai(k11, -landscape.oo(k11, k13), k13, 912, rv * 4, 0, lv * 2);
		}
		camera.wi();
		pl();
		if(tu > 0)
			gamegfx.xg(uu - 8, vu - 8, hu + 14 + (24 - tu) / 6);
		if(tu < 0)
			gamegfx.xg(uu - 8, vu - 8, hu + 18 + (24 + tu) / 6);
		if(!loadregion)
		{
			int l11 = 2203 - (regiony + worldh + localy);
			if(regionx + worldw + localx >= 2640)
				l11 = -50;
			if(l11 > 0)
			{
				int l13 = 1 + l11 / 6;
				gamegfx.xg(453, height - 56, hu + 13);
				gamegfx.ug("Wilderness", 465, height - 20, 1, 0xffff00);
				gamegfx.ug("Level: " + l13, 465, height - 7, 1, 0xffff00);
				if(hdb == 0)
					hdb = 2;
			}
			if(hdb == 0 && l11 > -10 && l11 <= 0)
				hdb = 1;
		}
		if(uz == 0)
		{
			for(int i12 = 0; i12 < vz; i12++)
				if(xz[i12] > 0)
				{
					String s5 = wz[i12];
					gamegfx.drawstring(s5, 7, height - 18 - i12 * 12, 1, 0xffff00);
				}

		}
		pz.qd(qz);
		pz.qd(sz);
		pz.qd(tz);
		if(uz == 1)
			pz.ed(qz);
		else
			if(uz == 2)
				pz.ed(sz);
			else
				if(uz == 3)
					pz.ed(tz);
		menu.ng = 2;
		pz.hc();
		menu.ng = 0;
		gamegfx.qg(((graphics) (gamegfx)).width - 3 - 197, 3, hu, 128);
		gk();
		gamegfx.al = false;
		al();
		gamegfx.drawimg(gfx, 0, 0);
	}

	public void al()
	{
		gamegfx.xg(0, height - 4, hu + 23);
		int i1 = graphics.rgbhash(200, 200, 255);
		if(uz == 0)
			i1 = graphics.rgbhash(255, 200, 50);
		if(lz % 30 > 15)
			i1 = graphics.rgbhash(255, 50, 50);
		gamegfx.ug("All messages", 54, height + 6, 0, i1);
		i1 = graphics.rgbhash(200, 200, 255);
		if(uz == 1)
			i1 = graphics.rgbhash(255, 200, 50);
		if(mz % 30 > 15)
			i1 = graphics.rgbhash(255, 50, 50);
		gamegfx.ug("Chat history", 155, height + 6, 0, i1);
		i1 = graphics.rgbhash(200, 200, 255);
		if(uz == 2)
			i1 = graphics.rgbhash(255, 200, 50);
		if(nz % 30 > 15)
			i1 = graphics.rgbhash(255, 50, 50);
		gamegfx.ug("Quest history", 255, height + 6, 0, i1);
		i1 = graphics.rgbhash(200, 200, 255);
		if(uz == 3)
			i1 = graphics.rgbhash(255, 200, 50);
		if(oz % 30 > 15)
			i1 = graphics.rgbhash(255, 50, 50);
		gamegfx.ug("Private history", 355, height + 6, 0, i1);
	}

	public void yl(int i1, int j1, int k1, int l1, int i2, int j2, int k2)
	{
		int l2 = mhb[i2];
		int i3 = lhb[i2];
		if(l2 == 0)
		{
			int j3 = 255 + i3 * 5 * 256;
			gamegfx.drawcircle(i1 + k1 / 2, j1 + l1 / 2, 20 + i3 * 2, j3, 255 - i3 * 5);
		}
		if(l2 == 1)
		{
			int k3 = 0xff0000 + i3 * 5 * 256;
			gamegfx.drawcircle(i1 + k1 / 2, j1 + l1 / 2, 10 + i3, k3, 255 - i3 * 5);
		}
	}

	public void vm(int i1, int j1, int k1, int l1, int i2, int j2, int k2)
	{
		int l2 = cache.njb[i2] + ju;
		int i3 = cache.sjb[i2];
		gamegfx.wf(i1, j1, k1, l1, l2, i3, 0, 0, false);
	}

	public void an(int i1, int j1, int k1, int l1, int i2, int j2, int k2)
	{
		livingentity l2 = kw[i2];
		int i3 = l2.kr + (rv + 16) / 32 & 7;
		boolean flag = false;
		int j3 = i3;
		if(j3 == 5)
		{
			j3 = 3;
			flag = true;
		} else
			if(j3 == 6)
			{
				j3 = 2;
				flag = true;
			} else
				if(j3 == 7)
				{
					j3 = 1;
					flag = true;
				}
		int k3 = j3 * 3 + dhb[(l2.jr / cache.lkb[l2.ir]) % 4];
		if(l2.kr == 8)
		{
			j3 = 5;
			i3 = 2;
			flag = false;
			i1 -= (cache.nkb[l2.ir] * k2) / 100;
			k3 = j3 * 3 + ehb[(kt / (cache.mkb[l2.ir] - 1)) % 8];
		} else
			if(l2.kr == 9)
			{
				j3 = 5;
				i3 = 2;
				flag = true;
				i1 += (cache.nkb[l2.ir] * k2) / 100;
				k3 = j3 * 3 + fhb[(kt / cache.mkb[l2.ir]) % 8];
			}
		for(int l3 = 0; l3 < 12; l3++)
		{
			int i4 = pgb[i3][l3];
			int l4 = cache.ekb[l2.ir][i4];
			if(l4 >= 0)
			{
				int j5 = 0;
				int k5 = 0;
				int l5 = k3;
				if(flag && j3 >= 1 && j3 <= 3 && cache.wkb[l4] == 1)
					l5 += 15;
				if(j3 != 5 || cache.vkb[l4] == 1)
				{
					int i6 = l5 + cache.xkb[l4];
					j5 = (j5 * k1) / ((graphics) (gamegfx)).pk[i6];
					k5 = (k5 * l1) / ((graphics) (gamegfx)).qk[i6];
					int j6 = (k1 * ((graphics) (gamegfx)).pk[i6]) / ((graphics) (gamegfx)).pk[cache.xkb[l4]];
					j5 -= (j6 - k1) / 2;
					int k6 = cache.tkb[l4];
					int l6 = 0;
					if(k6 == 1)
					{
						k6 = cache.fkb[l2.ir];
						l6 = cache.ikb[l2.ir];
					} else
						if(k6 == 2)
						{
							k6 = cache.gkb[l2.ir];
							l6 = cache.ikb[l2.ir];
						} else
							if(k6 == 3)
							{
								k6 = cache.hkb[l2.ir];
								l6 = cache.ikb[l2.ir];
							}
					gamegfx.wf(i1 + j5, j1 + k5, j6, l1, i6, k6, l6, j2, flag);
				}
			}
		}

		if(l2.sr > 0)
		{
			cgb[yfb] = gamegfx.textwidth(l2.rr, 1) / 2;
			if(cgb[yfb] > 150)
				cgb[yfb] = 150;
			dgb[yfb] = (gamegfx.textwidth(l2.rr, 1) / 300) * gamegfx.textheight(1);
			agb[yfb] = i1 + k1 / 2;
			bgb[yfb] = j1;
			zfb[yfb++] = l2.rr;
		}
		if(l2.kr == 8 || l2.kr == 9 || l2.yr != 0)
		{
			if(l2.yr > 0)
			{
				int j4 = i1;
				if(l2.kr == 8)
					j4 -= (20 * k2) / 100;
				else
					if(l2.kr == 9)
						j4 += (20 * k2) / 100;
				int i5 = (l2.wr * 30) / l2.xr;
				kgb[jgb] = j4 + k1 / 2;
				lgb[jgb] = j1;
				mgb[jgb++] = i5;
			}
			if(l2.yr > 150)
			{
				int k4 = i1;
				if(l2.kr == 8)
					k4 -= (10 * k2) / 100;
				else
					if(l2.kr == 9)
						k4 += (10 * k2) / 100;
				gamegfx.xg((k4 + k1 / 2) - 12, (j1 + l1 / 2) - 12, hu + 12);
				gamegfx.ug(String.valueOf(l2.vr), (k4 + k1 / 2) - 1, j1 + l1 / 2 + 5, 3, 0xffffff);
			}
		}
	}

	public void ml(int i1, int j1, int k1, int l1, int i2, int j2, int k2)
	{
		livingentity l2 = players[i2];
		if(l2.cs == 255)
			return;
		int i3 = l2.kr + (rv + 16) / 32 & 7;
		boolean flag = false;
		int j3 = i3;
		if(j3 == 5)
		{
			j3 = 3;
			flag = true;
		} else
			if(j3 == 6)
			{
				j3 = 2;
				flag = true;
			} else
				if(j3 == 7)
				{
					j3 = 1;
					flag = true;
				}
		int k3 = j3 * 3 + dhb[(l2.jr / 6) % 4];
		if(l2.kr == 8)
		{
			j3 = 5;
			i3 = 2;
			flag = false;
			i1 -= (5 * k2) / 100;
			k3 = j3 * 3 + ehb[(kt / 5) % 8];
		} else
			if(l2.kr == 9)
			{
				j3 = 5;
				i3 = 2;
				flag = true;
				i1 += (5 * k2) / 100;
				k3 = j3 * 3 + fhb[(kt / 6) % 8];
			}
		for(int l3 = 0; l3 < 12; l3++)
		{
			int i4 = pgb[i3][l3];
			int i5 = l2.qr[i4] - 1;
			if(i5 >= 0)
			{
				int l5 = 0;
				int j6 = 0;
				int k6 = k3;
				if(flag && j3 >= 1 && j3 <= 3)
					if(cache.wkb[i5] == 1)
						k6 += 15;
					else
						if(i4 == 4 && j3 == 1)
						{
							l5 = -22;
							j6 = -3;
							k6 = j3 * 3 + dhb[(2 + l2.jr / 6) % 4];
						} else
							if(i4 == 4 && j3 == 2)
							{
								l5 = 0;
								j6 = -8;
								k6 = j3 * 3 + dhb[(2 + l2.jr / 6) % 4];
							} else
								if(i4 == 4 && j3 == 3)
								{
									l5 = 26;
									j6 = -5;
									k6 = j3 * 3 + dhb[(2 + l2.jr / 6) % 4];
								} else
									if(i4 == 3 && j3 == 1)
									{
										l5 = 22;
										j6 = 3;
										k6 = j3 * 3 + dhb[(2 + l2.jr / 6) % 4];
									} else
										if(i4 == 3 && j3 == 2)
										{
											l5 = 0;
											j6 = 8;
											k6 = j3 * 3 + dhb[(2 + l2.jr / 6) % 4];
										} else
											if(i4 == 3 && j3 == 3)
											{
												l5 = -26;
												j6 = 5;
												k6 = j3 * 3 + dhb[(2 + l2.jr / 6) % 4];
											}
				if(j3 != 5 || cache.vkb[i5] == 1)
				{
					int l6 = k6 + cache.xkb[i5];
					l5 = (l5 * k1) / ((graphics) (gamegfx)).pk[l6];
					j6 = (j6 * l1) / ((graphics) (gamegfx)).qk[l6];
					int i7 = (k1 * ((graphics) (gamegfx)).pk[l6]) / ((graphics) (gamegfx)).pk[cache.xkb[i5]];
					l5 -= (i7 - k1) / 2;
					int j7 = cache.tkb[i5];
					int k7 = chb[l2.ds];
					if(j7 == 1)
						j7 = bhb[l2.as];
					else
						if(j7 == 2)
							j7 = ahb[l2.bs];
						else
							if(j7 == 3)
								j7 = ahb[l2.cs];
					gamegfx.wf(i1 + l5, j1 + j6, i7, l1, l6, j7, k7, j2, flag);
				}
			}
		}

		if(l2.sr > 0)
		{
			cgb[yfb] = gamegfx.textwidth(l2.rr, 1) / 2;
			if(cgb[yfb] > 150)
				cgb[yfb] = 150;
			dgb[yfb] = (gamegfx.textwidth(l2.rr, 1) / 300) * gamegfx.textheight(1);
			agb[yfb] = i1 + k1 / 2;
			bgb[yfb] = j1;
			zfb[yfb++] = l2.rr;
		}
		if(l2.ur > 0)
		{
			fgb[egb] = i1 + k1 / 2;
			ggb[egb] = j1;
			hgb[egb] = k2;
			igb[egb++] = l2.tr;
		}
		if(l2.kr == 8 || l2.kr == 9 || l2.yr != 0)
		{
			if(l2.yr > 0)
			{
				int j4 = i1;
				if(l2.kr == 8)
					j4 -= (20 * k2) / 100;
				else
					if(l2.kr == 9)
						j4 += (20 * k2) / 100;
				int j5 = (l2.wr * 30) / l2.xr;
				kgb[jgb] = j4 + k1 / 2;
				lgb[jgb] = j1;
				mgb[jgb++] = j5;
			}
			if(l2.yr > 150)
			{
				int k4 = i1;
				if(l2.kr == 8)
					k4 -= (10 * k2) / 100;
				else
					if(l2.kr == 9)
						k4 += (10 * k2) / 100;
				gamegfx.xg((k4 + k1 / 2) - 12, (j1 + l1 / 2) - 12, hu + 11);
				gamegfx.ug(String.valueOf(l2.vr), (k4 + k1 / 2) - 1, j1 + l1 / 2 + 5, 3, 0xffffff);
			}
		}
		if(l2.ks == 1 && l2.ur == 0)
		{
			int l4 = j2 + i1 + k1 / 2;
			if(l2.kr == 8)
				l4 -= (20 * k2) / 100;
			else
				if(l2.kr == 9)
					l4 += (20 * k2) / 100;
			int k5 = (16 * k2) / 100;
			int i6 = (16 * k2) / 100;
			gamegfx.nf(l4 - k5 / 2, j1 - i6 / 2 - (10 * k2) / 100, k5, i6, hu + 13);
		}
	}

	public void pl()
	{
		for(int i1 = 0; i1 < yfb; i1++)
		{
			int j1 = gamegfx.textheight(1);
			int l1 = agb[i1];
			int k2 = bgb[i1];
			int j3 = cgb[i1];
			int i4 = dgb[i1];
			boolean flag = true;
			while(flag) 
			{
				flag = false;
				for(int i5 = 0; i5 < i1; i5++)
					if(k2 + i4 > bgb[i5] - j1 && k2 - j1 < bgb[i5] + dgb[i5] && l1 - j3 < agb[i5] + cgb[i5] && l1 + j3 > agb[i5] - cgb[i5] && bgb[i5] - j1 - i4 < k2)
					{
						k2 = bgb[i5] - j1 - i4;
						flag = true;
					}

			}
			bgb[i1] = k2;
			gamegfx.centerpara(zfb[i1], l1, k2, 1, 0xffff00, 300);
		}

		for(int k1 = 0; k1 < egb; k1++)
		{
			int i2 = fgb[k1];
			int l2 = ggb[k1];
			int k3 = hgb[k1];
			int j4 = igb[k1];
			int l4 = (39 * k3) / 100;
			int j5 = (27 * k3) / 100;
			int k5 = l2 - j5;
			gamegfx.pg(i2 - l4 / 2, k5, l4, j5, hu + 9, 85);
			int l5 = (36 * k3) / 100;
			int i6 = (24 * k3) / 100;
			gamegfx.wf(i2 - l5 / 2, (k5 + j5 / 2) - i6 / 2, l5, i6, cache.njb[j4] + ju, cache.sjb[j4], 0, 0, false);
		}

		for(int j2 = 0; j2 < jgb; j2++)
		{
			int i3 = kgb[j2];
			int l3 = lgb[j2];
			int k4 = mgb[j2];
			gamegfx.drawtransquad(i3 - 15, l3 - 3, k4, 5, 65280, 192);
			gamegfx.drawtransquad((i3 - 15) + k4, l3 - 3, 30 - k4, 5, 0xff0000, 192);
		}

	}

	public int gl(int i1)
	{
		int j1 = 0;
		for(int k1 = 0; k1 < mx; k1++)
			if(nx[k1] == i1)
				if(cache.pjb[i1] == 1)
					j1++;
				else
					j1 += ox[k1];

		return j1;
	}

	public boolean nm(int i1, int j1)
	{
		if(i1 == 31 && (xm(197) || xm(615) || xm(682)))
			return true;
		if(i1 == 32 && (xm(102) || xm(616) || xm(683)))
			return true;
		if(i1 == 33 && (xm(101) || xm(617) || xm(684)))
			return true;
		if(i1 == 34 && (xm(103) || xm(618) || xm(685)))
			return true;
		return gl(i1) >= j1;
	}

	public boolean xm(int i1)
	{
		for(int j1 = 0; j1 < mx; j1++)
			if(nx[j1] == i1 && px[j1] == 1)
				return true;

		return false;
	}

	public void zm(int i1, int j1, int k1)
	{
		gamegfx.setpixel(i1, j1, k1);
		gamegfx.setpixel(i1 - 1, j1, k1);
		gamegfx.setpixel(i1 + 1, j1, k1);
		gamegfx.setpixel(i1, j1 - 1, k1);
		gamegfx.setpixel(i1, j1 + 1, k1);
	}

	public void cl(int i1, int j1, int k1, int l1, boolean flag)
	{
		xl(i1, j1, k1, l1, k1, l1, false, flag);
	}

	public void im(int i1, int j1, int k1, int l1, boolean flag)
	{
		if(xl(i1, j1, k1, l1, k1, l1, false, flag))
		{
			return;
		} else
		{
			xl(i1, j1, k1, l1, k1, l1, true, flag);
			return;
		}
	}

	public void kk(int i1, int j1, int k1, int l1)
	{
		int i2;
		int j2;
		if(k1 == 0 || k1 == 4)
		{
			i2 = cache.elb[l1];
			j2 = cache.flb[l1];
		} else
		{
			j2 = cache.elb[l1];
			i2 = cache.flb[l1];
		}
		if(cache.glb[l1] == 2 || cache.glb[l1] == 3)
		{
			if(k1 == 0)
			{
				i1--;
				i2++;
			}
			if(k1 == 2)
				j2++;
			if(k1 == 4)
				i2++;
			if(k1 == 6)
			{
				j1--;
				j2++;
			}
			xl(regionx, regiony, i1, j1, (i1 + i2) - 1, (j1 + j2) - 1, false, true);
			return;
		} else
		{
			xl(regionx, regiony, i1, j1, (i1 + i2) - 1, (j1 + j2) - 1, true, true);
			return;
		}
	}

	public void zk(int i1, int j1, int k1)
	{
		if(k1 == 0)
		{
			xl(regionx, regiony, i1, j1 - 1, i1, j1, false, true);
			return;
		}
		if(k1 == 1)
		{
			xl(regionx, regiony, i1 - 1, j1, i1, j1, false, true);
			return;
		} else
		{
			xl(regionx, regiony, i1, j1, i1, j1, true, true);
			return;
		}
	}

	public boolean xl(int i1, int j1, int k1, int l1, int i2, int j2, boolean flag, 
			boolean flag1)
	{
		int k2 = landscape.ho(i1, j1, k1, l1, i2, j2, ot, pt, flag);
		if(k2 == -1)
			return false;
		k2--;
		i1 = ot[k2];
		j1 = pt[k2];
		k2--;
		if(flag1)
			stream.create(215);
		else
			stream.create(194);
		stream.p2(i1 + localx);
		stream.p2(j1 + localy);
		for(int l2 = k2; l2 >= 0 && l2 > k2 - 25; l2--)
		{
			stream.p1(ot[l2] - i1);
			stream.p1(pt[l2] - j1);
		}

		stream.fmtdata();
		tu = -24;
		uu = mousex;
		vu = mousey;
		return true;
	}

	public boolean loadregion(int i1, int j1)
	{
		if(gdb != 0)
		{
			landscape.cjb = false;
			return false;
		}
		loadregion = false;
		i1 += worldw;
		j1 += worldh;
		if(av == playerheight && i1 > ev && i1 < gv && j1 > fv && j1 < hv)
		{
			landscape.cjb = true;
			return false;
		}
		gamegfx.ug("Loading... Please wait", 256, 192, 1, 0xffffff);
		al();
		gamegfx.drawimg(gfx, 0, 0);
		int k1 = localx;
		int l1 = localy;
		int i2 = (i1 + 24) / 48;
		int j2 = (j1 + 24) / 48;
		av = playerheight;
		localx = i2 * 48 - 48;
		localy = j2 * 48 - 48;
		ev = i2 * 48 - 32;
		fv = j2 * 48 - 32;
		gv = i2 * 48 + 32;
		hv = j2 * 48 + 32;
		landscape.xo(i1, j1, av);
		localx -= worldw;
		localy -= worldh;
		int k2 = localx - k1;
		int l2 = localy - l1;
		for(int i = 0; i < uw; i++)
		{
			ww[i] -= k2;
			xw[i] -= l2;
			int j3 = ww[i];
			int l3 = xw[i];
			int k4 = yw[i];
			model obj = vw[i];
			try
			{
				int i6 = zw[i];
				int i7;
				int k7;
				if(i6 == 0 || i6 == 4)
				{
					i7 = cache.elb[k4];
					k7 = cache.flb[k4];
				} else
				{
					k7 = cache.elb[k4];
					i7 = cache.flb[k4];
				}
				int l7 = ((j3 + j3 + i7) * regionarea) / 2;
				int i8 = ((l3 + l3 + k7) * regionarea) / 2;
				if(j3 >= 0 && l3 >= 0 && j3 < 96 && l3 < 96)
				{
					camera.uh(obj);
					obj.ge(l7, -landscape.oo(l7, i8), i8);
					landscape.vo(j3, l3, k4);
					if(k4 == 74)
						obj.zd(0, -480, 0);
				}
			}
			catch(RuntimeException runtimeexception)
			{
				System.out.println("Loc Error: " + runtimeexception.getMessage());
				System.out.println("i:" + i + " obj:" + obj);
				runtimeexception.printStackTrace();
			}
		}

		for(int k3 = 0; k3 < dx; k3++)
		{
			fx[k3] -= k2;
			gx[k3] -= l2;
			int i4 = fx[k3];
			int l4 = gx[k3];
			int j5 = ix[k3];
			int j6 = hx[k3];
			try
			{
				landscape.hp(i4, l4, j6, j5);
				model h2 = cm(i4, l4, j6, j5, k3);
				ex[k3] = h2;
			}
			catch(RuntimeException runtimeexception1)
			{
				System.out.println("Bound Error: " + runtimeexception1.getMessage());
				runtimeexception1.printStackTrace();
			}
		}

		for(int j4 = 0; j4 < ow; j4++)
		{
			pw[j4] -= k2;
			qw[j4] -= l2;
		}

		for(int i5 = 0; i5 < pcount; i5++)
		{
			livingentity l5 = players[i5];
			l5.x -= k2 * regionarea;
			l5.y -= l2 * regionarea;
			for(int k6 = 0; k6 <= l5.curwayp; k6++)
			{
				l5.xwayp[k6] -= k2 * regionarea;
				l5.ywayp[k6] -= l2 * regionarea;
			}

		}

		for(int k5 = 0; k5 < hw; k5++)
		{
			livingentity l6 = kw[k5];
			l6.x -= k2 * regionarea;
			l6.y -= l2 * regionarea;
			for(int j7 = 0; j7 <= l6.curwayp; j7++)
			{
				l6.xwayp[j7] -= k2 * regionarea;
				l6.ywayp[j7] -= l2 * regionarea;
			}

		}

		landscape.cjb = true;
		return true;
	}

	public model cm(int i1, int j1, int k1, int l1, int i2)
	{
		int j2 = i1;
		int k2 = j1;
		int l2 = i1;
		int i3 = j1;
		int j3 = cache.olb[l1];
		int k3 = cache.plb[l1];
		int l3 = cache.nlb[l1];
		model h1 = new model(4, 1);
		if(k1 == 0)
			l2 = i1 + 1;
		if(k1 == 1)
			i3 = j1 + 1;
		if(k1 == 2)
		{
			j2 = i1 + 1;
			i3 = j1 + 1;
		}
		if(k1 == 3)
		{
			l2 = i1 + 1;
			i3 = j1 + 1;
		}
		j2 *= regionarea;
		k2 *= regionarea;
		l2 *= regionarea;
		i3 *= regionarea;
		int i4 = h1.oe(j2, -landscape.oo(j2, k2), k2);
		int j4 = h1.oe(j2, -landscape.oo(j2, k2) - l3, k2);
		int k4 = h1.oe(l2, -landscape.oo(l2, i3) - l3, i3);
		int l4 = h1.oe(l2, -landscape.oo(l2, i3), i3);
		int ai[] = {
				i4, j4, k4, l4
		};
		h1.ne(4, ai, j3, k3);
		h1.se(false, 60, 24, -50, -10, -50);
		if(i1 >= 0 && j1 >= 0 && i1 < 96 && j1 < 96)
			camera.uh(h1);
		h1.uh = i2 + 10000;
		return h1;
	}

	public void gk()
	{
		if(edb != 0)
			am();
		else
			if(wcb)
				mk();
			else
				if(cdb)
					jm();
				else
					if(hdb == 1)
						um();
					else
						if(ccb && fdb == 0)
							nl();
						else
							if(ubb && fdb == 0)
								rl();
							else
								if(mbb)
									em();
								else
									if(zab)
										lm();
									else
										if(mab)
											sm();
										else
											if(yz)
												bn();
											else
												if(scb != 0)
													sl();
												else
													if(rcb != 0)
													{
														hk();
													} else
													{
														if(ncb)
															hl();
														if (ourplayer.kr == 8 || ourplayer.kr == 9)
															xk();
														tk();
														boolean flag = !ncb && !vy;
														if(flag)
															az = 0;
														if(kx == 0 && flag)
															dl();
														if(kx == 1)
															tl(flag);
														if(kx == 2)
															ok(flag);
														if(kx == 3)
															ql(flag);
														if(kx == 4)
															ym(flag);
														if(kx == 5)
															lk(flag);
														if(kx == 6)
															wm(flag);
														if(!vy && !ncb)
															bl();
														if(vy && !ncb)
															pm();
													}
		mt = 0;
	}

	public void hl()
	{
		if(mt != 0)
		{
			for(int i1 = 0; i1 < ocb; i1++)
			{
				if(mousex >= gamegfx.textwidth(pcb[i1], 1) || mousey <= i1 * 12 || mousey >= 12 + i1 * 12)
					continue;
				stream.create(237);
				stream.p1(i1);
				stream.fmtdata();
				break;
			}

			mt = 0;
			ncb = false;
			return;
		}
		for(int j1 = 0; j1 < ocb; j1++)
		{
			int k1 = 65535;
			if(mousex < gamegfx.textwidth(pcb[j1], 1) && mousey > j1 * 12 && mousey < 12 + j1 * 12)
				k1 = 0xff0000;
			gamegfx.drawstring(pcb[j1], 6, 12 + j1 * 12, 1, k1);
		}

	}

	public void xk()
	{
		byte byte0 = 7;
		byte byte1 = 15;
		char c1 = '\257';
		if(mt != 0)
		{
			for(int i1 = 0; i1 < 5; i1++)
			{
				if(i1 <= 0 || mousex <= byte0 || mousex >= byte0 + c1 || mousey <= byte1 + i1 * 20 || mousey >= byte1 + i1 * 20 + 20)
					continue;
				qcb = i1 - 1;
				mt = 0;
				stream.create(231);
				stream.p1(qcb);
				stream.fmtdata();
				break;
			}

		}
		for(int j1 = 0; j1 < 5; j1++)
		{
			if(j1 == qcb + 1)
				gamegfx.drawtransquad(byte0, byte1 + j1 * 20, c1, 20, graphics.rgbhash(255, 0, 0), 128);
			else
				gamegfx.drawtransquad(byte0, byte1 + j1 * 20, c1, 20, graphics.rgbhash(190, 190, 190), 128);
			gamegfx.drawhorline(byte0, byte1 + j1 * 20, c1, 0);
			gamegfx.drawhorline(byte0, byte1 + j1 * 20 + 20, c1, 0);
		}

		gamegfx.ug("Select combat style", byte0 + c1 / 2, byte1 + 16, 3, 0xffffff);
		gamegfx.ug("Controlled (+1 of each)", byte0 + c1 / 2, byte1 + 36, 3, 0);
		gamegfx.ug("Aggressive (+3 strength)", byte0 + c1 / 2, byte1 + 56, 3, 0);
		gamegfx.ug("Accurate   (+3 attack)", byte0 + c1 / 2, byte1 + 76, 3, 0);
		gamegfx.ug("Defensive  (+3 defense)", byte0 + c1 / 2, byte1 + 96, 3, 0);
	}

	public void mk()
	{
		char c1 = '\264';
		int i1 = 167 - c1 / 2;
		gamegfx.drawquad(56, 167 - c1 / 2, 400, c1, 0);
		gamegfx.drawquadout(56, 167 - c1 / 2, 400, c1, 0xffffff);
		i1 += 20;
		gamegfx.ug("Welcome to RuneScape " + heb, 256, i1, 4, 0xffff00);
		i1 += 30;
		gamegfx.ug("You last logged in " + zcb / 1440 + " days, " + (zcb / 60) % 24 + " hours ago", 256, i1, 1, 0xffffff);
		i1 += 15;
		if(ycb == null)
		{
			ycb = util.gip(xcb);
			try
			{
				ycb = InetAddress.getByName(ycb).getHostName();
			}
			catch(Exception exception)
			{
				String s1 = exception.getMessage();
				int l1 = s1.indexOf("cannot connect to");
				if(l1 != -1)
					ycb = s1.substring(l1 + 18);
			}
		}
		gamegfx.ug("from: " + ycb, 256, i1, 1, 0xffffff);
		i1 += 15;
		i1 += 15;
		if(adb != 0)
		{
			int j1 = 1 + adb / 1440;
			if(j1 > 14)
				j1 = 14;
			String s2;
			if(j1 == 14)
				s2 = "Earlier today";
			else
				if(j1 == 13)
					s2 = "Yesterday";
				else
					s2 = (14 - j1) + " days ago";
			gamegfx.ug(s2 + " you requested new recovery questions", 256, i1, 1, 0xff8000);
			i1 += 15;
			gamegfx.ug("If you do not remember making this request then", 256, i1, 1, 0xff8000);
			i1 += 15;
			gamegfx.ug("cancel it and change your password immediately!", 256, i1, 1, 0xff8000);
			i1 += 15;
			i1 += 15;
			int i2 = 0xffffff;
			if(mousey > i1 - 12 && mousey <= i1 && mousex > 106 && mousex < 406)
				i2 = 0xff0000;
			gamegfx.ug("No that wasn't me - Cancel the request!", 256, i1, 1, i2);
			if(i2 == 0xff0000 && mt == 1)
			{
				stream.create(196);
				stream.fmtdata();
				wcb = false;
			}
			i1 += 15;
			i2 = 0xffffff;
			if(mousey > i1 - 12 && mousey <= i1 && mousex > 106 && mousex < 406)
				i2 = 0xff0000;
			gamegfx.ug("That's ok, activate the new questions in " + j1 + " days time", 256, i1, 1, i2);
			if(i2 == 0xff0000 && mt == 1)
				wcb = false;
		} else
		{
			i1 += 7;
			gamegfx.ug("Security tip of the day", 256, i1, 1, 0xff0000);
			i1 += 15;
			if(bdb == 0)
			{
				gamegfx.ug("Don't tell ANYONE your password or recovery questions!", 256, i1, 1, 0xffffff);
				i1 += 15;
				gamegfx.ug("Not even people claiming to be Jagex staff.", 256, i1, 1, 0xffffff);
				i1 += 15;
			}
			if(bdb == 1)
			{
				gamegfx.ug("Never enter your password or recovery questions into ANY", 256, i1, 1, 0xffffff);
				i1 += 15;
				gamegfx.ug("website other than this one - Not even if it looks similar.", 256, i1, 1, 0xffffff);
				i1 += 15;
			}
			if(bdb == 2)
			{
				gamegfx.ug("Don't use RuneScape cheats, helpers, or automaters.", 256, i1, 1, 0xffffff);
				i1 += 15;
				gamegfx.ug("These programs WILL steal your password.", 256, i1, 1, 0xffffff);
				i1 += 15;
			}
			if(bdb == 3)
			{
				gamegfx.ug("Watch out for fake emails, and fake staff. Real staff", 256, i1, 1, 0xffffff);
				i1 += 15;
				gamegfx.ug("will NEVER ask you for your password or recovery questions!", 256, i1, 1, 0xffffff);
				i1 += 15;
			}
			if(bdb == 4)
			{
				gamegfx.ug("Use a password your friends won't guess. Do NOT use your name!", 256, i1, 1, 0xffffff);
				i1 += 15;
				gamegfx.ug("Choose a unique password which you haven't used anywhere else", 256, i1, 1, 0xffffff);
				i1 += 15;
			}
			if(bdb == 5)
			{
				gamegfx.ug("If possible only play runescape from your own computer", 256, i1, 1, 0xffffff);
				i1 += 15;
				gamegfx.ug("Other machines could have been tampered with to steal your pass", 256, i1, 1, 0xffffff);
				i1 += 15;
			}
			i1 += 22;
			int k1 = 0xffffff;
			if(mousey > i1 - 12 && mousey <= i1 && mousex > 106 && mousex < 406)
				k1 = 0xff0000;
			gamegfx.ug("Click here to close window", 256, i1, 1, k1);
			if(mt == 1)
			{
				if(k1 == 0xff0000)
					wcb = false;
				if((mousex < 86 || mousex > 426) && (mousey < 167 - c1 / 2 || mousey > 167 + c1 / 2))
					wcb = false;
			}
		}
		mt = 0;
	}

	public void jm()
	{
		char c1 = '\u0190';
		byte byte0 = 100;
		gamegfx.drawquad(256 - c1 / 2, 167 - byte0 / 2, c1, byte0, 0);
		gamegfx.drawquadout(256 - c1 / 2, 167 - byte0 / 2, c1, byte0, 0xffffff);
		gamegfx.centerpara(ddb, 256, 137, 1, 0xffffff, c1 - 40);
		int i1 = 157 + byte0 / 2;
		int j1 = 0xffffff;
		if(mousey > i1 - 12 && mousey <= i1 && mousex > 106 && mousex < 406)
			j1 = 0xff0000;
		gamegfx.ug("Click here to close window", 256, i1, 1, j1);
		if(mt == 1)
		{
			if(j1 == 0xff0000)
				cdb = false;
			if((mousex < 256 - c1 / 2 || mousex > 256 + c1 / 2) && (mousey < 167 - byte0 / 2 || mousey > 167 + byte0 / 2))
				cdb = false;
		}
		mt = 0;
	}

	public void am()
	{
		gamegfx.drawquad(126, 137, 260, 60, 0);
		gamegfx.drawquadout(126, 137, 260, 60, 0xffffff);
		gamegfx.ug("Logging out...", 256, 173, 5, 0xffffff);
	}

	public void um()
	{
		int i1 = 97;
		gamegfx.drawquad(86, 77, 340, 180, 0);
		gamegfx.drawquadout(86, 77, 340, 180, 0xffffff);
		gamegfx.ug("Warning! Proceed with caution", 256, i1, 4, 0xff0000);
		i1 += 26;
		gamegfx.ug("If you go much further north you will enter the", 256, i1, 1, 0xffffff);
		i1 += 13;
		gamegfx.ug("wilderness. This a very dangerous area where", 256, i1, 1, 0xffffff);
		i1 += 13;
		gamegfx.ug("other players can attack you!", 256, i1, 1, 0xffffff);
		i1 += 22;
		gamegfx.ug("The further north you go the more dangerous it", 256, i1, 1, 0xffffff);
		i1 += 13;
		gamegfx.ug("becomes, but the more treasure you will find.", 256, i1, 1, 0xffffff);
		i1 += 22;
		gamegfx.ug("In the wilderness an indicator at the bottom-right", 256, i1, 1, 0xffffff);
		i1 += 13;
		gamegfx.ug("of the screen will show the current level of danger", 256, i1, 1, 0xffffff);
		i1 += 22;
		int j1 = 0xffffff;
		if(mousey > i1 - 12 && mousey <= i1 && mousex > 181 && mousex < 331)
			j1 = 0xff0000;
		gamegfx.ug("Click here to close window", 256, i1, 1, j1);
		if(mt != 0)
		{
			if(mousey > i1 - 12 && mousey <= i1 && mousex > 181 && mousex < 331)
				hdb = 2;
			if(mousex < 86 || mousex > 426 || mousey < 77 || mousey > 257)
				hdb = 2;
			mt = 0;
		}
	}

	public void sl()
	{
		if(mt != 0)
		{
			mt = 0;
			if(mousex < 106 || mousey < 150 || mousex > 406 || mousey > 210)
			{
				scb = 0;
				return;
			}
		}
		int i1 = 150;
		gamegfx.drawquad(106, i1, 300, 60, 0);
		gamegfx.drawquadout(106, i1, 300, 60, 0xffffff);
		i1 += 22;
		if(scb == 6)
		{
			gamegfx.ug("Please enter your current password", 256, i1, 4, 0xffffff);
			i1 += 25;
			String s1 = "*";
			for(int j1 = 0; j1 < inputtext.length(); j1++)
				s1 = "X" + s1;

			gamegfx.ug(s1, 256, i1, 4, 0xffffff);
			if(enteredtext.length() > 0)
			{
				tcb = enteredtext;
				inputtext = "";
				enteredtext = "";
				scb = 1;
				return;
			}
		} else
			if(scb == 1)
			{
				gamegfx.ug("Please enter your new password", 256, i1, 4, 0xffffff);
				i1 += 25;
				String s2 = "*";
				for(int k1 = 0; k1 < inputtext.length(); k1++)
					s2 = "X" + s2;

				gamegfx.ug(s2, 256, i1, 4, 0xffffff);
				if(enteredtext.length() > 0)
				{
					ucb = enteredtext;
					inputtext = "";
					enteredtext = "";
					if(ucb.length() >= 5)
					{
						scb = 2;
						return;
					} else
					{
						scb = 5;
						return;
					}
				}
			} else
				if(scb == 2)
				{
					gamegfx.ug("Enter password again to confirm", 256, i1, 4, 0xffffff);
					i1 += 25;
					String s3 = "*";
					for(int l1 = 0; l1 < inputtext.length(); l1++)
						s3 = "X" + s3;

					gamegfx.ug(s3, 256, i1, 4, 0xffffff);
					if(enteredtext.length() > 0)
						if(enteredtext.equalsIgnoreCase(ucb))
						{
							scb = 4;
							sendrecov(tcb, ucb);
							return;
						} else
						{
							scb = 3;
							return;
						}
				} else
				{
					if(scb == 3)
					{
						gamegfx.ug("Passwords do not match!", 256, i1, 4, 0xffffff);
						i1 += 25;
						gamegfx.ug("Press any key to close", 256, i1, 4, 0xffffff);
						return;
					}
					if(scb == 4)
					{
						gamegfx.ug("Ok, your request has been sent", 256, i1, 4, 0xffffff);
						i1 += 25;
						gamegfx.ug("Press any key to close", 256, i1, 4, 0xffffff);
						return;
					}
					if(scb == 5)
					{
						gamegfx.ug("Password must be at", 256, i1, 4, 0xffffff);
						i1 += 25;
						gamegfx.ug("least 5 letters long", 256, i1, 4, 0xffffff);
					}
				}
	}

	public void hk()
	{
		if(mt != 0)
		{
			mt = 0;
			if(rcb == 1 && (mousex < 106 || mousey < 145 || mousex > 406 || mousey > 215))
			{
				rcb = 0;
				return;
			}
			if(rcb == 2 && (mousex < 6 || mousey < 145 || mousex > 506 || mousey > 215))
			{
				rcb = 0;
				return;
			}
			if(rcb == 3 && (mousex < 106 || mousey < 145 || mousex > 406 || mousey > 215))
			{
				rcb = 0;
				return;
			}
			if(mousex > 236 && mousex < 276 && mousey > 193 && mousey < 213)
			{
				rcb = 0;
				return;
			}
		}
		int i1 = 145;
		if(rcb == 1)
		{
			gamegfx.drawquad(106, i1, 300, 70, 0);
			gamegfx.drawquadout(106, i1, 300, 70, 0xffffff);
			i1 += 20;
			gamegfx.ug("Enter name to add to friends list", 256, i1, 4, 0xffffff);
			i1 += 20;
			gamegfx.ug(inputtext + "*", 256, i1, 4, 0xffffff);
			if(enteredtext.length() > 0)
			{
				String s1 = enteredtext.trim();
				inputtext = "";
				enteredtext = "";
				rcb = 0;
				if(s1.length() > 0 && util.encodeb37(s1) != ourplayer.cr)
					addfriend(s1);
			}
		}
		if(rcb == 2)
		{
			gamegfx.drawquad(6, i1, 500, 70, 0);
			gamegfx.drawquadout(6, i1, 500, 70, 0xffffff);
			i1 += 20;
			gamegfx.ug("Enter message to send to " + util.decodeb37(jy), 256, i1, 4, 0xffffff);
			i1 += 20;
			gamegfx.ug(inputmessage + "*", 256, i1, 4, 0xffffff);
			if(enteredmessage.length() > 0)
			{
				String s2 = enteredmessage;
				inputmessage = "";
				enteredmessage = "";
				rcb = 0;
				int k1 = util.strlen(s2);
				sendpm(jy, util.lastcm, k1);
				s2 = util.nn(util.lastcm, 0, k1, true);
				showmsg("@pri@You tell " + util.decodeb37(jy) + ": " + s2);
			}
		}
		if(rcb == 3)
		{
			gamegfx.drawquad(106, i1, 300, 70, 0);
			gamegfx.drawquadout(106, i1, 300, 70, 0xffffff);
			i1 += 20;
			gamegfx.ug("Enter name to add to ignore list", 256, i1, 4, 0xffffff);
			i1 += 20;
			gamegfx.ug(inputtext + "*", 256, i1, 4, 0xffffff);
			if(enteredtext.length() > 0)
			{
				String s3 = enteredtext.trim();
				inputtext = "";
				enteredtext = "";
				rcb = 0;
				if(s3.length() > 0 && util.encodeb37(s3) != ourplayer.cr)
					addignore(s3);
			}
		}
		int j1 = 0xffffff;
		if(mousex > 236 && mousex < 276 && mousey > 193 && mousey < 213)
			j1 = 0xffff00;
		gamegfx.ug("Cancel", 256, 208, 1, j1);
	}

	public void nl()
	{
		char c1 = '\u0198';
		char c2 = '\u014E';
		if(mcb > 0 && gcb <= 48)
			mcb = 0;
		if(mcb > 1 && gcb <= 96)
			mcb = 1;
		if(jcb >= gcb || jcb < 0)
			jcb = -1;
		if(jcb != -1 && hcb[jcb] != kcb)
		{
			jcb = -1;
			kcb = -2;
		}
		if(mt != 0)
		{
			mt = 0;
			int i1 = mousex - (256 - c1 / 2);
			int k1 = mousey - (170 - c2 / 2);
			if(i1 >= 0 && k1 >= 12 && i1 < 408 && k1 < 280)
			{
				int i2 = mcb * 48;
				for(int l2 = 0; l2 < 6; l2++)
				{
					for(int l6 = 0; l6 < 8; l6++)
					{
						int k7 = 7 + l6 * 49;
						int j8 = 28 + l2 * 34;
						if(i1 > k7 && i1 < k7 + 49 && k1 > j8 && k1 < j8 + 34 && i2 < gcb && hcb[i2] != -1)
						{
							kcb = hcb[i2];
							jcb = i2;
						}
						i2++;
					}

				}

				i1 = 256 - c1 / 2;
				k1 = 170 - c2 / 2;
				int i7;
				if(jcb < 0)
					i7 = -1;
				else
					i7 = hcb[jcb];
				if(i7 != -1)
				{
					int j2 = icb[jcb];
					if(cache.pjb[i7] == 1 && j2 > 1)
						j2 = 1;
					if(j2 >= 1 && mousex >= i1 + 220 && mousey >= k1 + 238 && mousex < i1 + 250 && mousey <= k1 + 249)
					{
						stream.create(206);
						stream.p2(i7);
						stream.p2(1);
						stream.fmtdata();
					}
					if(j2 >= 5 && mousex >= i1 + 250 && mousey >= k1 + 238 && mousex < i1 + 280 && mousey <= k1 + 249)
					{
						stream.create(206);
						stream.p2(i7);
						stream.p2(5);
						stream.fmtdata();
					}
					if(j2 >= 25 && mousex >= i1 + 280 && mousey >= k1 + 238 && mousex < i1 + 305 && mousey <= k1 + 249)
					{
						stream.create(206);
						stream.p2(i7);
						stream.p2(25);
						stream.fmtdata();
					}
					if(j2 >= 100 && mousex >= i1 + 305 && mousey >= k1 + 238 && mousex < i1 + 335 && mousey <= k1 + 249)
					{
						stream.create(206);
						stream.p2(i7);
						stream.p2(100);
						stream.fmtdata();
					}
					if(j2 >= 500 && mousex >= i1 + 335 && mousey >= k1 + 238 && mousex < i1 + 368 && mousey <= k1 + 249)
					{
						stream.create(206);
						stream.p2(i7);
						stream.p2(500);
						stream.fmtdata();
					}
					if(j2 >= 2500 && mousex >= i1 + 370 && mousey >= k1 + 238 && mousex < i1 + 400 && mousey <= k1 + 249)
					{
						stream.create(206);
						stream.p2(i7);
						stream.p2(2500);
						stream.fmtdata();
					}
					if(gl(i7) >= 1 && mousex >= i1 + 220 && mousey >= k1 + 263 && mousex < i1 + 250 && mousey <= k1 + 274)
					{
						stream.create(205);
						stream.p2(i7);
						stream.p2(1);
						stream.fmtdata();
					}
					if(gl(i7) >= 5 && mousex >= i1 + 250 && mousey >= k1 + 263 && mousex < i1 + 280 && mousey <= k1 + 274)
					{
						stream.create(205);
						stream.p2(i7);
						stream.p2(5);
						stream.fmtdata();
					}
					if(gl(i7) >= 25 && mousex >= i1 + 280 && mousey >= k1 + 263 && mousex < i1 + 305 && mousey <= k1 + 274)
					{
						stream.create(205);
						stream.p2(i7);
						stream.p2(25);
						stream.fmtdata();
					}
					if(gl(i7) >= 100 && mousex >= i1 + 305 && mousey >= k1 + 263 && mousex < i1 + 335 && mousey <= k1 + 274)
					{
						stream.create(205);
						stream.p2(i7);
						stream.p2(100);
						stream.fmtdata();
					}
					if(gl(i7) >= 500 && mousex >= i1 + 335 && mousey >= k1 + 263 && mousex < i1 + 368 && mousey <= k1 + 274)
					{
						stream.create(205);
						stream.p2(i7);
						stream.p2(500);
						stream.fmtdata();
					}
					if(gl(i7) >= 2500 && mousex >= i1 + 370 && mousey >= k1 + 263 && mousex < i1 + 400 && mousey <= k1 + 274)
					{
						stream.create(205);
						stream.p2(i7);
						stream.p2(2500);
						stream.fmtdata();
					}
				}
			} else
				if(gcb > 48 && i1 >= 70 && i1 <= 140 && k1 <= 12)
					mcb = 0;
				else
					if(gcb > 48 && i1 >= 140 && i1 <= 210 && k1 <= 12)
						mcb = 1;
					else
						if(gcb > 96 && i1 >= 210 && i1 <= 280 && k1 <= 12)
						{
							mcb = 2;
						} else
						{
							stream.create(207);
							stream.fmtdata();
							ccb = false;
							return;
						}
		}
		int j1 = 256 - c1 / 2;
		int l1 = 170 - c2 / 2;
		gamegfx.drawquad(j1, l1, 408, 12, 192);
		int k2 = 0x989898;
		gamegfx.drawtransquad(j1, l1 + 12, 408, 17, k2, 160);
		gamegfx.drawtransquad(j1, l1 + 29, 8, 204, k2, 160);
		gamegfx.drawtransquad(j1 + 399, l1 + 29, 9, 204, k2, 160);
		gamegfx.drawtransquad(j1, l1 + 233, 408, 47, k2, 160);
		gamegfx.drawstring("Bank", j1 + 1, l1 + 10, 1, 0xffffff);
		if(gcb > 48)
		{
			int i3 = 0xffffff;
			if(mcb == 0)
				i3 = 0xff0000;
			else
				if(mousex > (j1 + 120) - 50 && mousey >= l1 && mousex < (j1 + 190) - 50 && mousey < l1 + 12)
					i3 = 0xffff00;
			gamegfx.drawstring("<page 1>", (j1 + 120) - 40, l1 + 10, 1, i3);
			i3 = 0xffffff;
			if(mcb == 1)
				i3 = 0xff0000;
			else
				if(mousex > (j1 + 190) - 50 && mousey >= l1 && mousex < (j1 + 260) - 50 && mousey < l1 + 12)
					i3 = 0xffff00;
			gamegfx.drawstring("<page 2>", (j1 + 190) - 40, l1 + 10, 1, i3);
		}
		if(gcb > 96)
		{
			int j3 = 0xffffff;
			if(mcb == 2)
				j3 = 0xff0000;
			else
				if(mousex > (j1 + 260) - 50 && mousey >= l1 && mousex < (j1 + 330) - 50 && mousey < l1 + 12)
					j3 = 0xffff00;
			gamegfx.drawstring("<page 3>", (j1 + 260) - 40, l1 + 10, 1, j3);
		}
		int k3 = 0xffffff;
		if(mousex > j1 + 320 && mousey >= l1 && mousex < j1 + 408 && mousey < l1 + 12)
			k3 = 0xff0000;
		gamegfx.yg("Close window", j1 + 406, l1 + 10, 1, k3);
		gamegfx.drawstring("Number in bank in green", j1 + 7, l1 + 24, 1, 65280);
		gamegfx.drawstring("Number held in blue", j1 + 289, l1 + 24, 1, 65535);
		int j7 = 0xd0d0d0;
		int l7 = mcb * 48;
		for(int k8 = 0; k8 < 6; k8++)
		{
			for(int l8 = 0; l8 < 8; l8++)
			{
				int j9 = j1 + 7 + l8 * 49;
				int k9 = l1 + 28 + k8 * 34;
				if(jcb == l7)
					gamegfx.drawtransquad(j9, k9, 49, 34, 0xff0000, 160);
				else
					gamegfx.drawtransquad(j9, k9, 49, 34, j7, 160);
				gamegfx.drawquadout(j9, k9, 50, 35, 0);
				if(l7 < gcb && hcb[l7] != -1)
				{
					gamegfx.wf(j9, k9, 48, 32, ju + cache.njb[hcb[l7]], cache.sjb[hcb[l7]], 0, 0, false);
					gamegfx.drawstring(String.valueOf(icb[l7]), j9 + 1, k9 + 10, 1, 65280);
					gamegfx.yg(String.valueOf(gl(hcb[l7])), j9 + 47, k9 + 29, 1, 65535);
				}
				l7++;
			}

		}

		gamegfx.drawhorline(j1 + 5, l1 + 256, 398, 0);
		if(jcb == -1)
		{
			gamegfx.ug("Select an object to withdraw or deposit", j1 + 204, l1 + 248, 3, 0xffff00);
			return;
		}
		int i9;
		if(jcb < 0)
			i9 = -1;
		else
			i9 = hcb[jcb];
		if(i9 != -1)
		{
			int i8 = icb[jcb];
			if(cache.pjb[i9] == 1 && i8 > 1)
				i8 = 1;
			if(i8 > 0)
			{
				gamegfx.drawstring("Withdraw " + cache.itemnames[i9], j1 + 2, l1 + 248, 1, 0xffffff);
				int l3 = 0xffffff;
				if(mousex >= j1 + 220 && mousey >= l1 + 238 && mousex < j1 + 250 && mousey <= l1 + 249)
					l3 = 0xff0000;
				gamegfx.drawstring("One", j1 + 222, l1 + 248, 1, l3);
				if(i8 >= 5)
				{
					int i4 = 0xffffff;
					if(mousex >= j1 + 250 && mousey >= l1 + 238 && mousex < j1 + 280 && mousey <= l1 + 249)
						i4 = 0xff0000;
					gamegfx.drawstring("Five", j1 + 252, l1 + 248, 1, i4);
				}
				if(i8 >= 25)
				{
					int j4 = 0xffffff;
					if(mousex >= j1 + 280 && mousey >= l1 + 238 && mousex < j1 + 305 && mousey <= l1 + 249)
						j4 = 0xff0000;
					gamegfx.drawstring("25", j1 + 282, l1 + 248, 1, j4);
				}
				if(i8 >= 100)
				{
					int k4 = 0xffffff;
					if(mousex >= j1 + 305 && mousey >= l1 + 238 && mousex < j1 + 335 && mousey <= l1 + 249)
						k4 = 0xff0000;
					gamegfx.drawstring("100", j1 + 307, l1 + 248, 1, k4);
				}
				if(i8 >= 500)
				{
					int l4 = 0xffffff;
					if(mousex >= j1 + 335 && mousey >= l1 + 238 && mousex < j1 + 368 && mousey <= l1 + 249)
						l4 = 0xff0000;
					gamegfx.drawstring("500", j1 + 337, l1 + 248, 1, l4);
				}
				if(i8 >= 2500)
				{
					int i5 = 0xffffff;
					if(mousex >= j1 + 370 && mousey >= l1 + 238 && mousex < j1 + 400 && mousey <= l1 + 249)
						i5 = 0xff0000;
					gamegfx.drawstring("2500", j1 + 370, l1 + 248, 1, i5);
				}
			}
			if(gl(i9) > 0)
			{
				gamegfx.drawstring("Deposit " + cache.itemnames[i9], j1 + 2, l1 + 273, 1, 0xffffff);
				int j5 = 0xffffff;
				if(mousex >= j1 + 220 && mousey >= l1 + 263 && mousex < j1 + 250 && mousey <= l1 + 274)
					j5 = 0xff0000;
				gamegfx.drawstring("One", j1 + 222, l1 + 273, 1, j5);
				if(gl(i9) >= 5)
				{
					int k5 = 0xffffff;
					if(mousex >= j1 + 250 && mousey >= l1 + 263 && mousex < j1 + 280 && mousey <= l1 + 274)
						k5 = 0xff0000;
					gamegfx.drawstring("Five", j1 + 252, l1 + 273, 1, k5);
				}
				if(gl(i9) >= 25)
				{
					int l5 = 0xffffff;
					if(mousex >= j1 + 280 && mousey >= l1 + 263 && mousex < j1 + 305 && mousey <= l1 + 274)
						l5 = 0xff0000;
					gamegfx.drawstring("25", j1 + 282, l1 + 273, 1, l5);
				}
				if(gl(i9) >= 100)
				{
					int i6 = 0xffffff;
					if(mousex >= j1 + 305 && mousey >= l1 + 263 && mousex < j1 + 335 && mousey <= l1 + 274)
						i6 = 0xff0000;
					gamegfx.drawstring("100", j1 + 307, l1 + 273, 1, i6);
				}
				if(gl(i9) >= 500)
				{
					int j6 = 0xffffff;
					if(mousex >= j1 + 335 && mousey >= l1 + 263 && mousex < j1 + 368 && mousey <= l1 + 274)
						j6 = 0xff0000;
					gamegfx.drawstring("500", j1 + 337, l1 + 273, 1, j6);
				}
				if(gl(i9) >= 2500)
				{
					int k6 = 0xffffff;
					if(mousex >= j1 + 370 && mousey >= l1 + 263 && mousex < j1 + 400 && mousey <= l1 + 274)
						k6 = 0xff0000;
					gamegfx.drawstring("2500", j1 + 370, l1 + 273, 1, k6);
				}
			}
		}
	}

	public void rl()
	{
		if(mt != 0)
		{
			mt = 0;
			int i1 = mousex - 52;
			int j1 = mousey - 44;
			if(i1 >= 0 && j1 >= 12 && i1 < 408 && j1 < 246)
			{
				int k1 = 0;
				for(int i2 = 0; i2 < 5; i2++)
				{
					for(int i3 = 0; i3 < 8; i3++)
					{
						int l3 = 7 + i3 * 49;
						int l4 = 28 + i2 * 34;
						if(i1 > l3 && i1 < l3 + 49 && j1 > l4 && j1 < l4 + 34 && xbb[k1] != -1)
						{
							acb = k1;
							bcb = xbb[k1];
						}
						k1++;
					}

				}

				if(acb >= 0)
				{
					int j3 = xbb[acb];
					if(j3 != -1)
					{
						if(ybb[acb] > 0 && i1 > 298 && j1 >= 204 && i1 < 408 && j1 <= 215)
						{
							int i4 = wbb + zbb[acb];
							if(i4 < 10)
								i4 = 10;
							int i5 = (i4 * cache.itembaseprice[j3]) / 100;
							stream.create(217);
							stream.p2(xbb[acb]);
							stream.p4(i5);
							stream.fmtdata();
						}
						if(gl(j3) > 0 && i1 > 2 && j1 >= 229 && i1 < 112 && j1 <= 240)
						{
							int j4 = vbb + zbb[acb];
							if(j4 < 10)
								j4 = 10;
							int j5 = (j4 * cache.itembaseprice[j3]) / 100;
							stream.create(216);
							stream.p2(xbb[acb]);
							stream.p4(j5);
							stream.fmtdata();
						}
					}
				}
			} else
			{
				stream.create(218);
				stream.fmtdata();
				ubb = false;
				return;
			}
		}
		byte byte0 = 52;
		byte byte1 = 44;
		gamegfx.drawquad(byte0, byte1, 408, 12, 192);
		int l1 = 0x989898;
		gamegfx.drawtransquad(byte0, byte1 + 12, 408, 17, l1, 160);
		gamegfx.drawtransquad(byte0, byte1 + 29, 8, 170, l1, 160);
		gamegfx.drawtransquad(byte0 + 399, byte1 + 29, 9, 170, l1, 160);
		gamegfx.drawtransquad(byte0, byte1 + 199, 408, 47, l1, 160);
		gamegfx.drawstring("Buying and selling items", byte0 + 1, byte1 + 10, 1, 0xffffff);
		int j2 = 0xffffff;
		if(mousex > byte0 + 320 && mousey >= byte1 && mousex < byte0 + 408 && mousey < byte1 + 12)
			j2 = 0xff0000;
		gamegfx.yg("Close window", byte0 + 406, byte1 + 10, 1, j2);
		gamegfx.drawstring("Shops stock in green", byte0 + 2, byte1 + 24, 1, 65280);
		gamegfx.drawstring("Number you own in blue", byte0 + 135, byte1 + 24, 1, 65535);
		gamegfx.drawstring("Your money: " + gl(10) + "gp", byte0 + 280, byte1 + 24, 1, 0xffff00);
		int k3 = 0xd0d0d0;
		int k4 = 0;
		for(int k5 = 0; k5 < 5; k5++)
		{
			for(int l5 = 0; l5 < 8; l5++)
			{
				int j6 = byte0 + 7 + l5 * 49;
				int i7 = byte1 + 28 + k5 * 34;
				if(acb == k4)
					gamegfx.drawtransquad(j6, i7, 49, 34, 0xff0000, 160);
				else
					gamegfx.drawtransquad(j6, i7, 49, 34, k3, 160);
				gamegfx.drawquadout(j6, i7, 50, 35, 0);
				if(xbb[k4] != -1)
				{
					gamegfx.wf(j6, i7, 48, 32, ju + cache.njb[xbb[k4]], cache.sjb[xbb[k4]], 0, 0, false);
					gamegfx.drawstring(String.valueOf(ybb[k4]), j6 + 1, i7 + 10, 1, 65280);
					gamegfx.yg(String.valueOf(gl(xbb[k4])), j6 + 47, i7 + 10, 1, 65535);
				}
				k4++;
			}

		}

		gamegfx.drawhorline(byte0 + 5, byte1 + 222, 398, 0);
		if(acb == -1)
		{
			gamegfx.ug("Select an object to buy or sell", byte0 + 204, byte1 + 214, 3, 0xffff00);
			return;
		}
		int i6 = xbb[acb];
		if(i6 != -1)
		{
			if(ybb[acb] > 0)
			{
				int k6 = wbb + zbb[acb];
				if(k6 < 10)
					k6 = 10;
				int j7 = (k6 * cache.itembaseprice[i6]) / 100;
				gamegfx.drawstring("Buy a new " + cache.itemnames[i6] + " for " + j7 + "gp", byte0 + 2, byte1 + 214, 1, 0xffff00);
				int k2 = 0xffffff;
				if(mousex > byte0 + 298 && mousey >= byte1 + 204 && mousex < byte0 + 408 && mousey <= byte1 + 215)
					k2 = 0xff0000;
				gamegfx.yg("Click here to buy", byte0 + 405, byte1 + 214, 3, k2);
			} else
			{
				gamegfx.ug("This item is not currently available to buy", byte0 + 204, byte1 + 214, 3, 0xffff00);
			}
			if(gl(i6) > 0)
			{
				int l6 = vbb + zbb[acb];
				if(l6 < 10)
					l6 = 10;
				int k7 = (l6 * cache.itembaseprice[i6]) / 100;
				gamegfx.yg("Sell your " + cache.itemnames[i6] + " for " + k7 + "gp", byte0 + 405, byte1 + 239, 1, 0xffff00);
				int l2 = 0xffffff;
				if(mousex > byte0 + 2 && mousey >= byte1 + 229 && mousex < byte0 + 112 && mousey <= byte1 + 240)
					l2 = 0xff0000;
				gamegfx.drawstring("Click here to sell", byte0 + 2, byte1 + 239, 3, l2);
				return;
			}
			gamegfx.ug("You do not have any of this item to sell", byte0 + 204, byte1 + 239, 3, 0xffff00);
		}
	}

	public void em()
	{
		byte byte0 = 22;
		byte byte1 = 36;
		gamegfx.drawquad(byte0, byte1, 468, 16, 192);
		int i1 = 0x989898;
		gamegfx.drawtransquad(byte0, byte1 + 16, 468, 246, i1, 160);
		gamegfx.ug("Please confirm your trade with @yel@" + util.decodeb37(lbb), byte0 + 234, byte1 + 12, 1, 0xffffff);
		gamegfx.ug("You are about to give:", byte0 + 117, byte1 + 30, 1, 0xffff00);
		for(int j1 = 0; j1 < obb; j1++)
		{
			String s1 = cache.itemnames[pbb[j1]];
			if(cache.pjb[pbb[j1]] == 0)
				s1 = s1 + " (" + qbb[j1] + ")";
			gamegfx.ug(s1, byte0 + 117, byte1 + 42 + j1 * 12, 1, 0xffffff);
		}

		if(obb == 0)
			gamegfx.ug("Nothing!", byte0 + 117, byte1 + 42, 1, 0xffffff);
		gamegfx.ug("In return you will receive:", byte0 + 351, byte1 + 30, 1, 0xffff00);
		for(int k1 = 0; k1 < rbb; k1++)
		{
			String s2 = cache.itemnames[sbb[k1]];
			if(cache.pjb[sbb[k1]] == 0)
				s2 = s2 + " (" + tbb[k1] + ")";
			gamegfx.ug(s2, byte0 + 351, byte1 + 42 + k1 * 12, 1, 0xffffff);
		}

		if(rbb == 0)
			gamegfx.ug("Nothing!", byte0 + 351, byte1 + 42, 1, 0xffffff);
		gamegfx.ug("Are you sure you want to do this?", byte0 + 234, byte1 + 200, 4, 65535);
		gamegfx.ug("There is NO WAY to reverse a trade if you change your mind.", byte0 + 234, byte1 + 215, 1, 0xffffff);
		gamegfx.ug("Remember that not all players are trustworthy", byte0 + 234, byte1 + 230, 1, 0xffffff);
		if(!nbb)
		{
			gamegfx.xg((byte0 + 118) - 35, byte1 + 238, hu + 25);
			gamegfx.xg((byte0 + 352) - 35, byte1 + 238, hu + 26);
		} else
		{
			gamegfx.ug("Waiting for other player...", byte0 + 234, byte1 + 250, 1, 0xffff00);
		}
		if(mt == 1)
		{
			if(mousex < byte0 || mousey < byte1 || mousex > byte0 + 468 || mousey > byte1 + 262)
			{
				mbb = false;
				stream.create(233);
				stream.fmtdata();
			}
			if(mousex >= (byte0 + 118) - 35 && mousex <= byte0 + 118 + 70 && mousey >= byte1 + 238 && mousey <= byte1 + 238 + 21)
			{
				nbb = true;
				stream.create(202);
				stream.fmtdata();
			}
			if(mousex >= (byte0 + 352) - 35 && mousex <= byte0 + 353 + 70 && mousey >= byte1 + 238 && mousey <= byte1 + 238 + 21)
			{
				mbb = false;
				stream.create(233);
				stream.fmtdata();
			}
			mt = 0;
		}
	}

	public void lm()
	{
		if(mt != 0 && kbb == 0)
			kbb = 1;
		if(kbb > 0)
		{
			int i1 = mousex - 22;
			int j1 = mousey - 36;
			if(i1 >= 0 && j1 >= 0 && i1 < 468 && j1 < 262)
			{
				if(i1 > 216 && j1 > 30 && i1 < 462 && j1 < 235)
				{
					int k1 = (i1 - 217) / 49 + ((j1 - 31) / 34) * 5;
					if(k1 >= 0 && k1 < mx)
					{
						boolean flag = false;
						int l2 = 0;
						int k3 = nx[k1];
						for(int k4 = 0; k4 < bbb; k4++)
							if(cbb[k4] == k3)
								if(cache.pjb[k3] == 0)
								{
									for(int i5 = 0; i5 < kbb; i5++)
									{
										if(dbb[k4] < ox[k1])
											dbb[k4]++;
										flag = true;
									}

								} else
								{
									l2++;
								}

						if(gl(k3) <= l2)
							flag = true;
						if(cache.tjb[k3] == 1)
						{
							ik("This object cannot be traded with other players", 3);
							flag = true;
						}
						if(!flag && bbb < 12)
						{
							cbb[bbb] = k3;
							dbb[bbb] = 1;
							bbb++;
							flag = true;
						}
						if(flag)
						{
							stream.create(234);
							stream.p1(bbb);
							for(int j5 = 0; j5 < bbb; j5++)
							{
								stream.p2(cbb[j5]);
								stream.p4(dbb[j5]);
							}

							stream.fmtdata();
							hbb = false;
							ibb = false;
						}
					}
				}
				if(i1 > 8 && j1 > 30 && i1 < 205 && j1 < 133)
				{
					int l1 = (i1 - 9) / 49 + ((j1 - 31) / 34) * 4;
					if(l1 >= 0 && l1 < bbb)
					{
						int j2 = cbb[l1];
						for(int i3 = 0; i3 < kbb; i3++)
						{
							if(cache.pjb[j2] == 0 && dbb[l1] > 1)
							{
								dbb[l1]--;
								continue;
							}
							bbb--;
							jbb = 0;
							for(int l3 = l1; l3 < bbb; l3++)
							{
								cbb[l3] = cbb[l3 + 1];
								dbb[l3] = dbb[l3 + 1];
							}

							break;
						}

						stream.create(234);
						stream.p1(bbb);
						for(int i4 = 0; i4 < bbb; i4++)
						{
							stream.p2(cbb[i4]);
							stream.p4(dbb[i4]);
						}

						stream.fmtdata();
						hbb = false;
						ibb = false;
					}
				}
				if(i1 >= 217 && j1 >= 238 && i1 <= 286 && j1 <= 259)
				{
					ibb = true;
					stream.create(232);
					stream.fmtdata();
				}
				if(i1 >= 394 && j1 >= 238 && i1 < 463 && j1 < 259)
				{
					zab = false;
					stream.create(233);
					stream.fmtdata();
				}
			} else
				if(mt != 0)
				{
					zab = false;
					stream.create(233);
					stream.fmtdata();
				}
			mt = 0;
			kbb = 0;
		}
		if(!zab)
			return;
		byte byte0 = 22;
		byte byte1 = 36;
		gamegfx.drawquad(byte0, byte1, 468, 12, 192);
		int i2 = 0x989898;
		gamegfx.drawtransquad(byte0, byte1 + 12, 468, 18, i2, 160);
		gamegfx.drawtransquad(byte0, byte1 + 30, 8, 248, i2, 160);
		gamegfx.drawtransquad(byte0 + 205, byte1 + 30, 11, 248, i2, 160);
		gamegfx.drawtransquad(byte0 + 462, byte1 + 30, 6, 248, i2, 160);
		gamegfx.drawtransquad(byte0 + 8, byte1 + 133, 197, 22, i2, 160);
		gamegfx.drawtransquad(byte0 + 8, byte1 + 258, 197, 20, i2, 160);
		gamegfx.drawtransquad(byte0 + 216, byte1 + 235, 246, 43, i2, 160);
		int k2 = 0xd0d0d0;
		gamegfx.drawtransquad(byte0 + 8, byte1 + 30, 197, 103, k2, 160);
		gamegfx.drawtransquad(byte0 + 8, byte1 + 155, 197, 103, k2, 160);
		gamegfx.drawtransquad(byte0 + 216, byte1 + 30, 246, 205, k2, 160);
		for(int j3 = 0; j3 < 4; j3++)
			gamegfx.drawhorline(byte0 + 8, byte1 + 30 + j3 * 34, 197, 0);

		for(int j4 = 0; j4 < 4; j4++)
			gamegfx.drawhorline(byte0 + 8, byte1 + 155 + j4 * 34, 197, 0);

		for(int l4 = 0; l4 < 7; l4++)
			gamegfx.drawhorline(byte0 + 216, byte1 + 30 + l4 * 34, 246, 0);

		for(int k5 = 0; k5 < 6; k5++)
		{
			if(k5 < 5)
				gamegfx.drawvertline(byte0 + 8 + k5 * 49, byte1 + 30, 103, 0);
			if(k5 < 5)
				gamegfx.drawvertline(byte0 + 8 + k5 * 49, byte1 + 155, 103, 0);
			gamegfx.drawvertline(byte0 + 216 + k5 * 49, byte1 + 30, 205, 0);
		}

		gamegfx.drawstring("Trading with: " + abb, byte0 + 1, byte1 + 10, 1, 0xffffff);
		gamegfx.drawstring("Your Offer", byte0 + 9, byte1 + 27, 4, 0xffffff);
		gamegfx.drawstring("Opponent's Offer", byte0 + 9, byte1 + 152, 4, 0xffffff);
		gamegfx.drawstring("Your Inventory", byte0 + 216, byte1 + 27, 4, 0xffffff);
		if(!ibb)
			gamegfx.xg(byte0 + 217, byte1 + 238, hu + 25);
		gamegfx.xg(byte0 + 394, byte1 + 238, hu + 26);
		if(hbb)
		{
			gamegfx.ug("Other player", byte0 + 341, byte1 + 246, 1, 0xffffff);
			gamegfx.ug("has accepted", byte0 + 341, byte1 + 256, 1, 0xffffff);
		}
		if(ibb)
		{
			gamegfx.ug("Waiting for", byte0 + 217 + 35, byte1 + 246, 1, 0xffffff);
			gamegfx.ug("other player", byte0 + 217 + 35, byte1 + 256, 1, 0xffffff);
		}
		for(int l5 = 0; l5 < mx; l5++)
		{
			int i6 = 217 + byte0 + (l5 % 5) * 49;
			int k6 = 31 + byte1 + (l5 / 5) * 34;
			gamegfx.wf(i6, k6, 48, 32, ju + cache.njb[nx[l5]], cache.sjb[nx[l5]], 0, 0, false);
			if(cache.pjb[nx[l5]] == 0)
				gamegfx.drawstring(String.valueOf(ox[l5]), i6 + 1, k6 + 10, 1, 0xffff00);
		}

		for(int j6 = 0; j6 < bbb; j6++)
		{
			int l6 = 9 + byte0 + (j6 % 4) * 49;
			int j7 = 31 + byte1 + (j6 / 4) * 34;
			gamegfx.wf(l6, j7, 48, 32, ju + cache.njb[cbb[j6]], cache.sjb[cbb[j6]], 0, 0, false);
			if(cache.pjb[cbb[j6]] == 0)
				gamegfx.drawstring(String.valueOf(dbb[j6]), l6 + 1, j7 + 10, 1, 0xffff00);
			if(mousex > l6 && mousex < l6 + 48 && mousey > j7 && mousey < j7 + 32)
				gamegfx.drawstring(cache.itemnames[cbb[j6]] + ": @whi@" + cache.itemexamines[cbb[j6]], byte0 + 8, byte1 + 273, 1, 0xffff00);
		}

		for(int i7 = 0; i7 < ebb; i7++)
		{
			int k7 = 9 + byte0 + (i7 % 4) * 49;
			int l7 = 156 + byte1 + (i7 / 4) * 34;
			gamegfx.wf(k7, l7, 48, 32, ju + cache.njb[fbb[i7]], cache.sjb[fbb[i7]], 0, 0, false);
			if(cache.pjb[fbb[i7]] == 0)
				gamegfx.drawstring(String.valueOf(gbb[i7]), k7 + 1, l7 + 10, 1, 0xffff00);
			if(mousex > k7 && mousex < k7 + 48 && mousey > l7 && mousey < l7 + 32)
				gamegfx.drawstring(cache.itemnames[fbb[i7]] + ": @whi@" + cache.itemexamines[fbb[i7]], byte0 + 8, byte1 + 273, 1, 0xffff00);
		}

	}

	public void sm()
	{
		byte byte0 = 22;
		byte byte1 = 36;
		gamegfx.drawquad(byte0, byte1, 468, 16, 192);
		int i1 = 0x989898;
		gamegfx.drawtransquad(byte0, byte1 + 16, 468, 246, i1, 160);
		gamegfx.ug("Please confirm your duel with @yel@" + util.decodeb37(oab), byte0 + 234, byte1 + 12, 1, 0xffffff);
		gamegfx.ug("Your stake:", byte0 + 117, byte1 + 30, 1, 0xffff00);
		for(int j1 = 0; j1 < pab; j1++)
		{
			String s1 = cache.itemnames[qab[j1]];
			if(cache.pjb[qab[j1]] == 0)
				s1 = s1 + " (" + rab[j1] + ")";
			gamegfx.ug(s1, byte0 + 117, byte1 + 42 + j1 * 12, 1, 0xffffff);
		}

		if(pab == 0)
			gamegfx.ug("Nothing!", byte0 + 117, byte1 + 42, 1, 0xffffff);
		gamegfx.ug("Your opponent's stake:", byte0 + 351, byte1 + 30, 1, 0xffff00);
		for(int k1 = 0; k1 < sab; k1++)
		{
			String s2 = cache.itemnames[tab[k1]];
			if(cache.pjb[tab[k1]] == 0)
				s2 = s2 + " (" + uab[k1] + ")";
			gamegfx.ug(s2, byte0 + 351, byte1 + 42 + k1 * 12, 1, 0xffffff);
		}

		if(sab == 0)
			gamegfx.ug("Nothing!", byte0 + 351, byte1 + 42, 1, 0xffffff);
		if(vab == 0)
			gamegfx.ug("You can retreat from this duel", byte0 + 234, byte1 + 180, 1, 65280);
		else
			gamegfx.ug("No retreat is possible!", byte0 + 234, byte1 + 180, 1, 0xff0000);
		if(wab == 0)
			gamegfx.ug("Magic may be used", byte0 + 234, byte1 + 192, 1, 65280);
		else
			gamegfx.ug("Magic cannot be used", byte0 + 234, byte1 + 192, 1, 0xff0000);
		if(xab == 0)
			gamegfx.ug("Prayer may be used", byte0 + 234, byte1 + 204, 1, 65280);
		else
			gamegfx.ug("Prayer cannot be used", byte0 + 234, byte1 + 204, 1, 0xff0000);
		if(yab == 0)
			gamegfx.ug("Weapons may be used", byte0 + 234, byte1 + 216, 1, 65280);
		else
			gamegfx.ug("Weapons cannot be used", byte0 + 234, byte1 + 216, 1, 0xff0000);
		gamegfx.ug("If you are sure click 'Accept' to begin the duel", byte0 + 234, byte1 + 230, 1, 0xffffff);
		if(!nab)
		{
			gamegfx.xg((byte0 + 118) - 35, byte1 + 238, hu + 25);
			gamegfx.xg((byte0 + 352) - 35, byte1 + 238, hu + 26);
		} else
		{
			gamegfx.ug("Waiting for other player...", byte0 + 234, byte1 + 250, 1, 0xffff00);
		}
		if(mt == 1)
		{
			if(mousex < byte0 || mousey < byte1 || mousex > byte0 + 468 || mousey > byte1 + 262)
			{
				mab = false;
				stream.create(233);
				stream.fmtdata();
			}
			if(mousex >= (byte0 + 118) - 35 && mousex <= byte0 + 118 + 70 && mousey >= byte1 + 238 && mousey <= byte1 + 238 + 21)
			{
				nab = true;
				stream.create(198);
				stream.fmtdata();
			}
			if(mousex >= (byte0 + 352) - 35 && mousex <= byte0 + 353 + 70 && mousey >= byte1 + 238 && mousey <= byte1 + 238 + 21)
			{
				mab = false;
				stream.create(203);
				stream.fmtdata();
			}
			mt = 0;
		}
	}

	public void bn()
	{
		if(mt != 0 && kbb == 0)
			kbb = 1;
		if(kbb > 0)
		{
			int i1 = mousex - 22;
			int j1 = mousey - 36;
			if(i1 >= 0 && j1 >= 0 && i1 < 468 && j1 < 262)
			{
				if(i1 > 216 && j1 > 30 && i1 < 462 && j1 < 235)
				{
					int k1 = (i1 - 217) / 49 + ((j1 - 31) / 34) * 5;
					if(k1 >= 0 && k1 < mx)
					{
						boolean flag1 = false;
						int l2 = 0;
						int k3 = nx[k1];
						for(int k4 = 0; k4 < aab; k4++)
							if(bab[k4] == k3)
								if(cache.pjb[k3] == 0)
								{
									for(int i5 = 0; i5 < kbb; i5++)
									{
										if(cab[k4] < ox[k1])
											cab[k4]++;
										flag1 = true;
									}

								} else
								{
									l2++;
								}

						if(gl(k3) <= l2)
							flag1 = true;
						if(cache.tjb[k3] == 1)
						{
							ik("This object cannot be added to a duel offer", 3);
							flag1 = true;
						}
						if(!flag1 && aab < 8)
						{
							bab[aab] = k3;
							cab[aab] = 1;
							aab++;
							flag1 = true;
						}
						if(flag1)
						{
							stream.create(201);
							stream.p1(aab);
							for(int j5 = 0; j5 < aab; j5++)
							{
								stream.p2(bab[j5]);
								stream.p4(cab[j5]);
							}

							stream.fmtdata();
							gab = false;
							hab = false;
						}
					}
				}
				if(i1 > 8 && j1 > 30 && i1 < 205 && j1 < 129)
				{
					int l1 = (i1 - 9) / 49 + ((j1 - 31) / 34) * 4;
					if(l1 >= 0 && l1 < aab)
					{
						int j2 = bab[l1];
						for(int i3 = 0; i3 < kbb; i3++)
						{
							if(cache.pjb[j2] == 0 && cab[l1] > 1)
							{
								cab[l1]--;
								continue;
							}
							aab--;
							jbb = 0;
							for(int l3 = l1; l3 < aab; l3++)
							{
								bab[l3] = bab[l3 + 1];
								cab[l3] = cab[l3 + 1];
							}

							break;
						}

						stream.create(201);
						stream.p1(aab);
						for(int i4 = 0; i4 < aab; i4++)
						{
							stream.p2(bab[i4]);
							stream.p4(cab[i4]);
						}

						stream.fmtdata();
						gab = false;
						hab = false;
					}
				}
				boolean flag = false;
				if(i1 >= 93 && j1 >= 221 && i1 <= 104 && j1 <= 232)
				{
					iab = !iab;
					flag = true;
				}
				if(i1 >= 93 && j1 >= 240 && i1 <= 104 && j1 <= 251)
				{
					jab = !jab;
					flag = true;
				}
				if(i1 >= 191 && j1 >= 221 && i1 <= 202 && j1 <= 232)
				{
					kab = !kab;
					flag = true;
				}
				if(i1 >= 191 && j1 >= 240 && i1 <= 202 && j1 <= 251)
				{
					lab = !lab;
					flag = true;
				}
				if(flag)
				{
					stream.create(200);
					stream.p1(iab ? 1 : 0);
					stream.p1(jab ? 1 : 0);
					stream.p1(kab ? 1 : 0);
					stream.p1(lab ? 1 : 0);
					stream.fmtdata();
					gab = false;
					hab = false;
				}
				if(i1 >= 217 && j1 >= 238 && i1 <= 286 && j1 <= 259)
				{
					hab = true;
					stream.create(199);
					stream.fmtdata();
				}
				if(i1 >= 394 && j1 >= 238 && i1 < 463 && j1 < 259)
				{
					yz = false;
					stream.create(203);
					stream.fmtdata();
				}
			} else
				if(mt != 0)
				{
					yz = false;
					stream.create(203);
					stream.fmtdata();
				}
			mt = 0;
			kbb = 0;
		}
		if(!yz)
			return;
		byte byte0 = 22;
		byte byte1 = 36;
		gamegfx.drawquad(byte0, byte1, 468, 12, 0xc90b1d);
		int i2 = 0x989898;
		gamegfx.drawtransquad(byte0, byte1 + 12, 468, 18, i2, 160);
		gamegfx.drawtransquad(byte0, byte1 + 30, 8, 248, i2, 160);
		gamegfx.drawtransquad(byte0 + 205, byte1 + 30, 11, 248, i2, 160);
		gamegfx.drawtransquad(byte0 + 462, byte1 + 30, 6, 248, i2, 160);
		gamegfx.drawtransquad(byte0 + 8, byte1 + 99, 197, 24, i2, 160);
		gamegfx.drawtransquad(byte0 + 8, byte1 + 192, 197, 23, i2, 160);
		gamegfx.drawtransquad(byte0 + 8, byte1 + 258, 197, 20, i2, 160);
		gamegfx.drawtransquad(byte0 + 216, byte1 + 235, 246, 43, i2, 160);
		int k2 = 0xd0d0d0;
		gamegfx.drawtransquad(byte0 + 8, byte1 + 30, 197, 69, k2, 160);
		gamegfx.drawtransquad(byte0 + 8, byte1 + 123, 197, 69, k2, 160);
		gamegfx.drawtransquad(byte0 + 8, byte1 + 215, 197, 43, k2, 160);
		gamegfx.drawtransquad(byte0 + 216, byte1 + 30, 246, 205, k2, 160);
		for(int j3 = 0; j3 < 3; j3++)
			gamegfx.drawhorline(byte0 + 8, byte1 + 30 + j3 * 34, 197, 0);

		for(int j4 = 0; j4 < 3; j4++)
			gamegfx.drawhorline(byte0 + 8, byte1 + 123 + j4 * 34, 197, 0);

		for(int l4 = 0; l4 < 7; l4++)
			gamegfx.drawhorline(byte0 + 216, byte1 + 30 + l4 * 34, 246, 0);

		for(int k5 = 0; k5 < 6; k5++)
		{
			if(k5 < 5)
				gamegfx.drawvertline(byte0 + 8 + k5 * 49, byte1 + 30, 69, 0);
			if(k5 < 5)
				gamegfx.drawvertline(byte0 + 8 + k5 * 49, byte1 + 123, 69, 0);
			gamegfx.drawvertline(byte0 + 216 + k5 * 49, byte1 + 30, 205, 0);
		}

		gamegfx.drawhorline(byte0 + 8, byte1 + 215, 197, 0);
		gamegfx.drawhorline(byte0 + 8, byte1 + 257, 197, 0);
		gamegfx.drawvertline(byte0 + 8, byte1 + 215, 43, 0);
		gamegfx.drawvertline(byte0 + 204, byte1 + 215, 43, 0);
		gamegfx.drawstring("Preparing to duel with: " + zz, byte0 + 1, byte1 + 10, 1, 0xffffff);
		gamegfx.drawstring("Your Stake", byte0 + 9, byte1 + 27, 4, 0xffffff);
		gamegfx.drawstring("Opponent's Stake", byte0 + 9, byte1 + 120, 4, 0xffffff);
		gamegfx.drawstring("Duel Options", byte0 + 9, byte1 + 212, 4, 0xffffff);
		gamegfx.drawstring("Your Inventory", byte0 + 216, byte1 + 27, 4, 0xffffff);
		gamegfx.drawstring("No retreating", byte0 + 8 + 1, byte1 + 215 + 16, 3, 0xffff00);
		gamegfx.drawstring("No magic", byte0 + 8 + 1, byte1 + 215 + 35, 3, 0xffff00);
		gamegfx.drawstring("No prayer", byte0 + 8 + 102, byte1 + 215 + 16, 3, 0xffff00);
		gamegfx.drawstring("No weapons", byte0 + 8 + 102, byte1 + 215 + 35, 3, 0xffff00);
		gamegfx.drawquadout(byte0 + 93, byte1 + 215 + 6, 11, 11, 0xffff00);
		if(iab)
			gamegfx.drawquad(byte0 + 95, byte1 + 215 + 8, 7, 7, 0xffff00);
		gamegfx.drawquadout(byte0 + 93, byte1 + 215 + 25, 11, 11, 0xffff00);
		if(jab)
			gamegfx.drawquad(byte0 + 95, byte1 + 215 + 27, 7, 7, 0xffff00);
		gamegfx.drawquadout(byte0 + 191, byte1 + 215 + 6, 11, 11, 0xffff00);
		if(kab)
			gamegfx.drawquad(byte0 + 193, byte1 + 215 + 8, 7, 7, 0xffff00);
		gamegfx.drawquadout(byte0 + 191, byte1 + 215 + 25, 11, 11, 0xffff00);
		if(lab)
			gamegfx.drawquad(byte0 + 193, byte1 + 215 + 27, 7, 7, 0xffff00);
		if(!hab)
			gamegfx.xg(byte0 + 217, byte1 + 238, hu + 25);
		gamegfx.xg(byte0 + 394, byte1 + 238, hu + 26);
		if(gab)
		{
			gamegfx.ug("Other player", byte0 + 341, byte1 + 246, 1, 0xffffff);
			gamegfx.ug("has accepted", byte0 + 341, byte1 + 256, 1, 0xffffff);
		}
		if(hab)
		{
			gamegfx.ug("Waiting for", byte0 + 217 + 35, byte1 + 246, 1, 0xffffff);
			gamegfx.ug("other player", byte0 + 217 + 35, byte1 + 256, 1, 0xffffff);
		}
		for(int l5 = 0; l5 < mx; l5++)
		{
			int i6 = 217 + byte0 + (l5 % 5) * 49;
			int k6 = 31 + byte1 + (l5 / 5) * 34;
			gamegfx.wf(i6, k6, 48, 32, ju + cache.njb[nx[l5]], cache.sjb[nx[l5]], 0, 0, false);
			if(cache.pjb[nx[l5]] == 0)
				gamegfx.drawstring(String.valueOf(ox[l5]), i6 + 1, k6 + 10, 1, 0xffff00);
		}

		for(int j6 = 0; j6 < aab; j6++)
		{
			int l6 = 9 + byte0 + (j6 % 4) * 49;
			int j7 = 31 + byte1 + (j6 / 4) * 34;
			gamegfx.wf(l6, j7, 48, 32, ju + cache.njb[bab[j6]], cache.sjb[bab[j6]], 0, 0, false);
			if(cache.pjb[bab[j6]] == 0)
				gamegfx.drawstring(String.valueOf(cab[j6]), l6 + 1, j7 + 10, 1, 0xffff00);
			if(mousex > l6 && mousex < l6 + 48 && mousey > j7 && mousey < j7 + 32)
				gamegfx.drawstring(cache.itemnames[bab[j6]] + ": @whi@" + cache.itemexamines[bab[j6]], byte0 + 8, byte1 + 273, 1, 0xffff00);
		}

		for(int i7 = 0; i7 < dab; i7++)
		{
			int k7 = 9 + byte0 + (i7 % 4) * 49;
			int l7 = 124 + byte1 + (i7 / 4) * 34;
			gamegfx.wf(k7, l7, 48, 32, ju + cache.njb[eab[i7]], cache.sjb[eab[i7]], 0, 0, false);
			if(cache.pjb[eab[i7]] == 0)
				gamegfx.drawstring(String.valueOf(fab[i7]), k7 + 1, l7 + 10, 1, 0xffff00);
			if(mousex > k7 && mousex < k7 + 48 && mousey > l7 && mousey < l7 + 32)
				gamegfx.drawstring(cache.itemnames[eab[i7]] + ": @whi@" + cache.itemexamines[eab[i7]], byte0 + 8, byte1 + 273, 1, 0xffff00);
		}

	}

	public void tk()
	{
		if(kx == 0 && mousex >= ((graphics) (gamegfx)).width - 35 && mousey >= 3 && mousex < ((graphics) (gamegfx)).width - 3 && mousey < 35)
			kx = 1;
		if(kx == 0 && mousex >= ((graphics) (gamegfx)).width - 35 - 33 && mousey >= 3 && mousex < ((graphics) (gamegfx)).width - 3 - 33 && mousey < 35)
			kx = 2;
		if(kx == 0 && mousex >= ((graphics) (gamegfx)).width - 35 - 66 && mousey >= 3 && mousex < ((graphics) (gamegfx)).width - 3 - 66 && mousey < 35)
			kx = 3;
		if(kx == 0 && mousex >= ((graphics) (gamegfx)).width - 35 - 99 && mousey >= 3 && mousex < ((graphics) (gamegfx)).width - 3 - 99 && mousey < 35)
			kx = 4;
		if(kx == 0 && mousex >= ((graphics) (gamegfx)).width - 35 - 132 && mousey >= 3 && mousex < ((graphics) (gamegfx)).width - 3 - 132 && mousey < 35)
			kx = 5;
		if(kx == 0 && mousex >= ((graphics) (gamegfx)).width - 35 - 165 && mousey >= 3 && mousex < ((graphics) (gamegfx)).width - 3 - 165 && mousey < 35)
			kx = 6;
		if(kx != 0 && mousex >= ((graphics) (gamegfx)).width - 35 && mousey >= 3 && mousex < ((graphics) (gamegfx)).width - 3 && mousey < 26)
			kx = 1;
		if(kx != 0 && mousex >= ((graphics) (gamegfx)).width - 35 - 33 && mousey >= 3 && mousex < ((graphics) (gamegfx)).width - 3 - 33 && mousey < 26)
			kx = 2;
		if(kx != 0 && mousex >= ((graphics) (gamegfx)).width - 35 - 66 && mousey >= 3 && mousex < ((graphics) (gamegfx)).width - 3 - 66 && mousey < 26)
			kx = 3;
		if(kx != 0 && mousex >= ((graphics) (gamegfx)).width - 35 - 99 && mousey >= 3 && mousex < ((graphics) (gamegfx)).width - 3 - 99 && mousey < 26)
			kx = 4;
		if(kx != 0 && mousex >= ((graphics) (gamegfx)).width - 35 - 132 && mousey >= 3 && mousex < ((graphics) (gamegfx)).width - 3 - 132 && mousey < 26)
			kx = 5;
		if(kx != 0 && mousex >= ((graphics) (gamegfx)).width - 35 - 165 && mousey >= 3 && mousex < ((graphics) (gamegfx)).width - 3 - 165 && mousey < 26)
			kx = 6;
		if(kx == 1 && (mousex < ((graphics) (gamegfx)).width - 248 || mousey > 36 + (lx / 5) * 34))
			kx = 0;
		if(kx == 3 && (mousex < ((graphics) (gamegfx)).width - 199 || mousey > 294))
			kx = 0;
		if((kx == 2 || kx == 4 || kx == 5) && (mousex < ((graphics) (gamegfx)).width - 199 || mousey > 240))
			kx = 0;
		if(kx == 6 && (mousex < ((graphics) (gamegfx)).width - 199 || mousey > 311))
			kx = 0;
	}

	public void tl(boolean flag)
	{
		int i1 = ((graphics) (gamegfx)).width - 248;
		gamegfx.xg(i1, 3, hu + 1);
		for(int j1 = 0; j1 < lx; j1++)
		{
			int k1 = i1 + (j1 % 5) * 49;
			int i2 = 36 + (j1 / 5) * 34;
			if(j1 < mx && px[j1] == 1)
				gamegfx.drawtransquad(k1, i2, 49, 34, 0xff0000, 128);
			else
				gamegfx.drawtransquad(k1, i2, 49, 34, graphics.rgbhash(181, 181, 181), 128);
			if(j1 < mx)
			{
				gamegfx.wf(k1, i2, 48, 32, ju + cache.njb[nx[j1]], cache.sjb[nx[j1]], 0, 0, false);
				if(cache.pjb[nx[j1]] == 0)
					gamegfx.drawstring(String.valueOf(ox[j1]), k1 + 1, i2 + 10, 1, 0xffff00);
			}
		}

		for(int l1 = 1; l1 <= 4; l1++)
			gamegfx.drawvertline(i1 + l1 * 49, 36, (lx / 5) * 34, 0);

		for(int j2 = 1; j2 <= lx / 5 - 1; j2++)
			gamegfx.drawhorline(i1, 36 + j2 * 34, 245, 0);

		if(!flag)
			return;
		i1 = mousex - (((graphics) (gamegfx)).width - 248);
		int k2 = mousey - 36;
		if(i1 >= 0 && k2 >= 0 && i1 < 248 && k2 < (lx / 5) * 34)
		{
			int l2 = i1 / 49 + (k2 / 34) * 5;
			if(l2 < mx)
			{
				int i3 = nx[l2];
				if(fy >= 0)
				{
					if(cache.fmb[fy] == 3)
					{
						dz[az] = "Cast " + cache.bmb[fy] + " on";
						cz[az] = "@lre@" + cache.itemnames[i3];
						ez[az] = 600;
						hz[az] = l2;
						iz[az] = fy;
						az++;
						return;
					}
				} else
				{
					if(qx >= 0)
					{
						dz[az] = "Use " + rx + " with";
						cz[az] = "@lre@" + cache.itemnames[i3];
						ez[az] = 610;
						hz[az] = l2;
						iz[az] = qx;
						az++;
						return;
					}
					if(px[l2] == 1)
					{
						dz[az] = "Remove";
						cz[az] = "@lre@" + cache.itemnames[i3];
						ez[az] = 620;
						hz[az] = l2;
						az++;
					} else
						if(cache.rjb[i3] != 0)
						{
							if((cache.rjb[i3] & 0x18) != 0)
								dz[az] = "Wield";
							else
								dz[az] = "Wear";
							cz[az] = "@lre@" + cache.itemnames[i3];
							ez[az] = 630;
							hz[az] = l2;
							az++;
						}
					if(!cache.itemcommand[i3].equals(""))
					{
						dz[az] = cache.itemcommand[i3];
						cz[az] = "@lre@" + cache.itemnames[i3];
						ez[az] = 640;
						hz[az] = l2;
						az++;
					}
					dz[az] = "Use";
					cz[az] = "@lre@" + cache.itemnames[i3];
					ez[az] = 650;
					hz[az] = l2;
					az++;
					dz[az] = "Drop";
					cz[az] = "@lre@" + cache.itemnames[i3];
					ez[az] = 660;
					hz[az] = l2;
					az++;
					dz[az] = "Examine";
					cz[az] = "@lre@" + cache.itemnames[i3];
					ez[az] = 3600;
					hz[az] = i3;
					az++;
				}
			}
		}
	}

	public void ok(boolean flag)
	{
		int i1 = ((graphics) (gamegfx)).width - 199;
		char c1 = '\234';
		char c3 = '\230';
		gamegfx.xg(i1 - 49, 3, hu + 2);
		i1 += 40;
		gamegfx.drawquad(i1, 36, c1, c3, 0);
		gamegfx.setrend(i1, 36, i1 + c1, 36 + c3);
		char c5 = '\300';
		int k1 = ((ourplayer.x - 6040) * 3 * c5) / 2048;
		int i3 = ((ourplayer.y - 6040) * 3 * c5) / 2048;
		int k4 = camera.mm[1024 - rv * 4 & 0x3ff];
		int i5 = camera.mm[(1024 - rv * 4 & 0x3ff) + 1024];
		int k5 = i3 * k4 + k1 * i5 >> 18;
		i3 = i3 * i5 - k1 * k4 >> 18;
			k1 = k5;
			gamegfx.of((i1 + c1 / 2) - k1, 36 + c3 / 2 + i3, hu - 1, rv + 64 & 0xff, c5);
			for(int i7 = 0; i7 < uw; i7++)
			{
				int l1 = (((ww[i7] * regionarea + 64) - ourplayer.x) * 3 * c5) / 2048;
				int j3 = (((xw[i7] * regionarea + 64) - ourplayer.y) * 3 * c5) / 2048;
				int l5 = j3 * k4 + l1 * i5 >> 18;
			j3 = j3 * i5 - l1 * k4 >> 18;
		l1 = l5;
		zm(i1 + c1 / 2 + l1, (36 + c3 / 2) - j3, 65535);
			}

			for(int j7 = 0; j7 < ow; j7++)
			{
				int i2 = (((pw[j7] * regionarea + 64) - ourplayer.x) * 3 * c5) / 2048;
				int k3 = (((qw[j7] * regionarea + 64) - ourplayer.y) * 3 * c5) / 2048;
				int i6 = k3 * k4 + i2 * i5 >> 18;
			k3 = k3 * i5 - i2 * k4 >> 18;
			i2 = i6;
			zm(i1 + c1 / 2 + i2, (36 + c3 / 2) - k3, 0xff0000);
			}

			for(int k7 = 0; k7 < hw; k7++)
			{
				livingentity l7 = kw[k7];
				int j2 = ((l7.x - ourplayer.x) * 3 * c5) / 2048;
				int l3 = ((l7.y - ourplayer.y) * 3 * c5) / 2048;
				int j6 = l3 * k4 + j2 * i5 >> 18;
			l3 = l3 * i5 - j2 * k4 >> 18;
		j2 = j6;
		zm(i1 + c1 / 2 + j2, (36 + c3 / 2) - l3, 0xffff00);
			}

			for(int i8 = 0; i8 < pcount; i8++)
			{
				livingentity l8 = players[i8];
				int k2 = ((l8.x - ourplayer.x) * 3 * c5) / 2048;
				int i4 = ((l8.y - ourplayer.y) * 3 * c5) / 2048;
				int k6 = i4 * k4 + k2 * i5 >> 18;
			i4 = i4 * i5 - k2 * k4 >> 18;
				k2 = k6;
				int k8 = 0xffffff;
				for(int i9 = 0; i9 < friendcnt; i9++)
				{
					if(l8.cr != friends[i9] || friendw[i9] != 99)
						continue;
					k8 = 65280;
					break;
				}

				zm(i1 + c1 / 2 + k2, (36 + c3 / 2) - i4, k8);
			}

			gamegfx.drawcircle(i1 + c1 / 2, 36 + c3 / 2, 2, 0xffffff, 255);
			gamegfx.of(i1 + 19, 55, hu + 24, rv + 128 & 0xff, 128);
			gamegfx.setrend(0, 0, width, height + 12);
			if(!flag)
				return;
			i1 = mousex - (((graphics) (gamegfx)).width - 199);
			int j8 = mousey - 36;
			if(i1 >= 40 && j8 >= 0 && i1 < 196 && j8 < 152)
			{
				char c2 = '\234';
				char c4 = '\230';
				char c6 = '\300';
				int j1 = ((graphics) (gamegfx)).width - 199;
				j1 += 40;
				int l2 = ((mousex - (j1 + c2 / 2)) * 16384) / (3 * c6);
				int j4 = ((mousey - (36 + c4 / 2)) * 16384) / (3 * c6);
				int l4 = camera.mm[1024 - rv * 4 & 0x3ff];
				int j5 = camera.mm[(1024 - rv * 4 & 0x3ff) + 1024];
				int l6 = j4 * l4 + l2 * j5 >> 15;
			j4 = j4 * j5 - l2 * l4 >> 15;
						l2 = l6;
						l2 += ourplayer.x;
						j4 = ourplayer.y - j4;
						if(mt == 1)
							cl(regionx, regiony, l2 / 128, j4 / 128, false);
						mt = 0;
			}
	}

	public void ql(boolean flag)
	{
		int i1 = ((graphics) (gamegfx)).width - 199;
		int j1 = 36;
		gamegfx.xg(i1 - 49, 3, hu + 3);
		char c1 = '\304';
		char c2 = '\u0107';
		int l1;
		int k1 = l1 = graphics.rgbhash(160, 160, 160);
		if(my == 0)
			k1 = graphics.rgbhash(220, 220, 220);
		else
			l1 = graphics.rgbhash(220, 220, 220);
		gamegfx.drawtransquad(i1, j1, c1 / 2, 24, k1, 128);
		gamegfx.drawtransquad(i1 + c1 / 2, j1, c1 / 2, 24, l1, 128);
		gamegfx.drawtransquad(i1, j1 + 24, c1, c2 - 24, graphics.rgbhash(220, 220, 220), 128);
		gamegfx.drawhorline(i1, j1 + 24, c1, 0);
		gamegfx.drawvertline(i1 + c1 / 2, j1, 24, 0);
		gamegfx.ug("Stats", i1 + c1 / 4, j1 + 16, 4, 0);
		gamegfx.ug("Quests", i1 + c1 / 4 + c1 / 2, j1 + 16, 4, 0);
		if(my == 0)
		{
			int i2 = 72;
			int k2 = -1;
			gamegfx.drawstring("Skills", i1 + 5, i2, 3, 0xffff00);
			i2 += 13;
			for(int l2 = 0; l2 < 9; l2++)
			{
				int i3 = 0xffffff;
				if(mousex > i1 + 3 && mousey >= i2 - 11 && mousey < i2 + 2 && mousex < i1 + 90)
				{
					i3 = 0xff0000;
					k2 = l2;
				}
				gamegfx.drawstring(skillnames[l2] + ":@yel@" + ux[l2] + "/" + vx[l2], i1 + 5, i2, 1, i3);
				i3 = 0xffffff;
				if(mousex >= i1 + 90 && mousey >= i2 - 13 - 11 && mousey < (i2 - 13) + 2 && mousex < i1 + 196)
				{
					i3 = 0xff0000;
					k2 = l2 + 9;
				}
				gamegfx.drawstring(skillnames[l2 + 9] + ":@yel@" + ux[l2 + 9] + "/" + vx[l2 + 9], (i1 + c1 / 2) - 5, i2 - 13, 1, i3);
				i2 += 13;
			}

			gamegfx.drawstring("Quest Points:@yel@" + yx, (i1 + c1 / 2) - 5, i2 - 13, 1, 0xffffff);
			i2 += 8;
			gamegfx.drawstring("Equipment Status", i1 + 5, i2, 3, 0xffff00);
			i2 += 12;
			for(int j3 = 0; j3 < 3; j3++)
			{
				gamegfx.drawstring(by[j3] + ":@yel@" + xx[j3], i1 + 5, i2, 1, 0xffffff);
				if(j3 < 2)
					gamegfx.drawstring(by[j3 + 3] + ":@yel@" + xx[j3 + 3], i1 + c1 / 2 + 25, i2, 1, 0xffffff);
				i2 += 13;
			}

			i2 += 6;
			gamegfx.drawhorline(i1, i2 - 15, c1, 0);
			if(k2 != -1)
			{
				gamegfx.drawstring(ay[k2] + " skill", i1 + 5, i2, 1, 0xffff00);
				i2 += 12;
				int k3 = experience[0];
				for(int i4 = 0; i4 < 98; i4++)
					if(wx[k2] >= experience[i4])
						k3 = experience[i4 + 1];

				gamegfx.drawstring("Total xp: " + wx[k2] / 4, i1 + 5, i2, 1, 0xffffff);
				i2 += 12;
				gamegfx.drawstring("Next level at: " + k3 / 4, i1 + 5, i2, 1, 0xffffff);
			} else
			{
				gamegfx.drawstring("Overall levels", i1 + 5, i2, 1, 0xffff00);
				i2 += 12;
				int l3 = 0;
				for(int j4 = 0; j4 < 18; j4++)
					l3 += vx[j4];

				gamegfx.drawstring("Skill total: " + l3, i1 + 5, i2, 1, 0xffffff);
				i2 += 12;
				gamegfx.drawstring("Combat level: " + ourplayer.zr, i1 + 5, i2, 1, 0xffffff);
				i2 += 12;
			}
		}
		if(my == 1)
		{
			ky.mc(ly);
			ky.dd(ly, 0, "@whi@Quest-list (green=completed)");
			for(int j2 = 0; j2 < ny; j2++)
				ky.dd(ly, j2 + 1, (py[j2] ? "@gre@" : "@red@") + oy[j2]);

			ky.hc();
		}
		if(!flag)
			return;
		i1 = mousex - (((graphics) (gamegfx)).width - 199);
		j1 = mousey - 36;
		if(i1 >= 0 && j1 >= 0 && i1 < c1 && j1 < c2)
		{
			if(my == 1)
				ky.pd(i1 + (((graphics) (gamegfx)).width - 199), j1 + 36, lastclick, mouseclick);
			if(j1 <= 24 && mt == 1)
			{
				if(i1 < 98)
				{
					my = 0;
					return;
				}
				if(i1 > 98)
					my = 1;
			}
		}
	}

	public void ym(boolean flag)
	{
		int i1 = ((graphics) (gamegfx)).width - 199;
		int j1 = 36;
		gamegfx.xg(i1 - 49, 3, hu + 4);
		char c1 = '\304';
		char c2 = '\266';
		int l1;
		int k1 = l1 = graphics.rgbhash(160, 160, 160);
		if(ey == 0)
			k1 = graphics.rgbhash(220, 220, 220);
		else
			l1 = graphics.rgbhash(220, 220, 220);
		gamegfx.drawtransquad(i1, j1, c1 / 2, 24, k1, 128);
		gamegfx.drawtransquad(i1 + c1 / 2, j1, c1 / 2, 24, l1, 128);
		gamegfx.drawtransquad(i1, j1 + 24, c1, 90, graphics.rgbhash(220, 220, 220), 128);
		gamegfx.drawtransquad(i1, j1 + 24 + 90, c1, c2 - 90 - 24, graphics.rgbhash(160, 160, 160), 128);
		gamegfx.drawhorline(i1, j1 + 24, c1, 0);
		gamegfx.drawvertline(i1 + c1 / 2, j1, 24, 0);
		gamegfx.drawhorline(i1, j1 + 113, c1, 0);
		gamegfx.ug("Magic", i1 + c1 / 4, j1 + 16, 4, 0);
		gamegfx.ug("Prayers", i1 + c1 / 4 + c1 / 2, j1 + 16, 4, 0);
		if(ey == 0)
		{
			cy.mc(dy);
			int i2 = 0;
			for(int i3 = 0; i3 < cache.amb; i3++)
			{
				String s1 = "@yel@";
				for(int l4 = 0; l4 < cache.emb[i3]; l4++)
				{
					int k5 = cache.gmb[i3][l4];
					if(nm(k5, cache.hmb[i3][l4]))
						continue;
					s1 = "@whi@";
					break;
				}

				int l5 = ux[6];
				if(cache.dmb[i3] > l5)
					s1 = "@bla@";
				cy.dd(dy, i2++, s1 + "Level " + cache.dmb[i3] + ": " + cache.bmb[i3]);
			}

			cy.hc();
			int i4 = cy.ic(dy);
			if(i4 != -1)
			{
				gamegfx.drawstring("Level " + cache.dmb[i4] + ": " + cache.bmb[i4], i1 + 2, j1 + 124, 1, 0xffff00);
				gamegfx.drawstring(cache.cmb[i4], i1 + 2, j1 + 136, 0, 0xffffff);
				for(int i5 = 0; i5 < cache.emb[i4]; i5++)
				{
					int i6 = cache.gmb[i4][i5];
					gamegfx.xg(i1 + 2 + i5 * 44, j1 + 150, ju + cache.njb[i6]);
					int j6 = gl(i6);
					int k6 = cache.hmb[i4][i5];
					String s3 = "@red@";
					if(nm(i6, k6))
						s3 = "@gre@";
					gamegfx.drawstring(s3 + j6 + "/" + k6, i1 + 2 + i5 * 44, j1 + 150, 1, 0xffffff);
				}

			} else
			{
				gamegfx.drawstring("Point at a spell for a description", i1 + 2, j1 + 124, 1, 0);
			}
		}
		if(ey == 1)
		{
			cy.mc(dy);
			int j2 = 0;
			for(int j3 = 0; j3 < cache.imb; j3++)
			{
				String s2 = "@whi@";
				if(cache.lmb[j3] > vx[5])
					s2 = "@bla@";
				if(qy[j3])
					s2 = "@gre@";
				cy.dd(dy, j2++, s2 + "Level " + cache.lmb[j3] + ": " + cache.jmb[j3]);
			}

			cy.hc();
			int j4 = cy.ic(dy);
			if(j4 != -1)
			{
				gamegfx.ug("Level " + cache.lmb[j4] + ": " + cache.jmb[j4], i1 + c1 / 2, j1 + 130, 1, 0xffff00);
				gamegfx.ug(cache.kmb[j4], i1 + c1 / 2, j1 + 145, 0, 0xffffff);
				gamegfx.ug("Drain rate: " + cache.mmb[j4], i1 + c1 / 2, j1 + 160, 1, 0);
			} else
			{
				gamegfx.drawstring("Point at a prayer for a description", i1 + 2, j1 + 124, 1, 0);
			}
		}
		if(!flag)
			return;
		i1 = mousex - (((graphics) (gamegfx)).width - 199);
		j1 = mousey - 36;
		if(i1 >= 0 && j1 >= 0 && i1 < 196 && j1 < 182)
		{
			cy.pd(i1 + (((graphics) (gamegfx)).width - 199), j1 + 36, lastclick, mouseclick);
			if(j1 <= 24 && mt == 1)
				if(i1 < 98 && ey == 1)
				{
					ey = 0;
					cy.zc(dy);
				} else
					if(i1 > 98 && ey == 0)
					{
						ey = 1;
						cy.zc(dy);
					}
			if(mt == 1 && ey == 0)
			{
				int k2 = cy.ic(dy);
				if(k2 != -1)
				{
					int k3 = ux[6];
					if(cache.dmb[k2] > k3)
					{
						ik("Your magic ability is not high enough for this spell", 3);
					} else
					{
						int k4;
						for(k4 = 0; k4 < cache.emb[k2]; k4++)
						{
							int j5 = cache.gmb[k2][k4];
							if(nm(j5, cache.hmb[k2][k4]))
								continue;
							ik("You don't have all the reagents you need for this spell", 3);
							k4 = -1;
							break;
						}

						if(k4 == cache.emb[k2])
						{
							fy = k2;
							qx = -1;
						}
					}
				}
			}
			if(mt == 1 && ey == 1)
			{
				int l2 = cy.ic(dy);
				if(l2 != -1)
				{
					int l3 = vx[5];
					if(cache.lmb[l2] > l3)
						ik("Your prayer ability is not high enough for this prayer", 3);
					else
						if(ux[5] == 0)
							ik("You have run out of prayer points. Return to a church to recharge", 3);
						else
							if(qy[l2])
							{
								stream.create(211);
								stream.p1(l2);
								stream.fmtdata();
								qy[l2] = false;
								playsound("prayeroff");
							} else
							{
								stream.create(212);
								stream.p1(l2);
								stream.fmtdata();
								qy[l2] = true;
								playsound("prayeron");
							}
				}
			}
			mt = 0;
		}
	}

	public void lk(boolean flag)
	{
		int i1 = ((graphics) (gamegfx)).width - 199;
		int j1 = 36;
		gamegfx.xg(i1 - 49, 3, hu + 5);
		char c1 = '\304';
		char c2 = '\266';
		int l1;
		int k1 = l1 = graphics.rgbhash(160, 160, 160);
		if(iy == 0)
			k1 = graphics.rgbhash(220, 220, 220);
		else
			l1 = graphics.rgbhash(220, 220, 220);
		gamegfx.drawtransquad(i1, j1, c1 / 2, 24, k1, 128);
		gamegfx.drawtransquad(i1 + c1 / 2, j1, c1 / 2, 24, l1, 128);
		gamegfx.drawtransquad(i1, j1 + 24, c1, c2 - 24, graphics.rgbhash(220, 220, 220), 128);
		gamegfx.drawhorline(i1, j1 + 24, c1, 0);
		gamegfx.drawvertline(i1 + c1 / 2, j1, 24, 0);
		gamegfx.drawhorline(i1, (j1 + c2) - 16, c1, 0);
		gamegfx.ug("Friends", i1 + c1 / 4, j1 + 16, 4, 0);
		gamegfx.ug("Ignore", i1 + c1 / 4 + c1 / 2, j1 + 16, 4, 0);
		gy.mc(hy);
		if(iy == 0)
		{
			for(int i2 = 0; i2 < friendcnt; i2++)
			{
				String s1;
				if(friendw[i2] == 99)
					s1 = "@gre@";
				else
					if(friendw[i2] > 0)
						s1 = "@yel@";
					else
						s1 = "@red@";
				gy.dd(hy, i2, s1 + util.decodeb37(friends[i2]) + "~439~@whi@Remove         WWWWWWWWWW");
			}

		}
		if(iy == 1)
		{
			for(int j2 = 0; j2 < ignorecnt; j2++)
				gy.dd(hy, j2, "@yel@" + util.decodeb37(ignores[j2]) + "~439~@whi@Remove         WWWWWWWWWW");

		}
		gy.hc();
		if(iy == 0)
		{
			int k2 = gy.ic(hy);
			if(k2 >= 0 && mousex < 489)
			{
				if(mousex > 429)
					gamegfx.ug("Click to remove " + util.decodeb37(friends[k2]), i1 + c1 / 2, j1 + 35, 1, 0xffffff);
				else
					if(friendw[k2] == 99)
						gamegfx.ug("Click to message " + util.decodeb37(friends[k2]), i1 + c1 / 2, j1 + 35, 1, 0xffffff);
					else
						if(friendw[k2] > 0)
							gamegfx.ug(util.decodeb37(friends[k2]) + " is on world " + friendw[k2], i1 + c1 / 2, j1 + 35, 1, 0xffffff);
						else
							gamegfx.ug(util.decodeb37(friends[k2]) + " is offline", i1 + c1 / 2, j1 + 35, 1, 0xffffff);
			} else
			{
				gamegfx.ug("Click a name to send a message", i1 + c1 / 2, j1 + 35, 1, 0xffffff);
			}
			int k3;
			if(mousex > i1 && mousex < i1 + c1 && mousey > (j1 + c2) - 16 && mousey < j1 + c2)
				k3 = 0xffff00;
			else
				k3 = 0xffffff;
			gamegfx.ug("Click here to add a friend", i1 + c1 / 2, (j1 + c2) - 3, 1, k3);
		}
		if(iy == 1)
		{
			int l2 = gy.ic(hy);
			if(l2 >= 0 && mousex < 489 && mousex > 429)
			{
				if(mousex > 429)
					gamegfx.ug("Click to remove " + util.decodeb37(ignores[l2]), i1 + c1 / 2, j1 + 35, 1, 0xffffff);
			} else
			{
				gamegfx.ug("Blocking messages from:", i1 + c1 / 2, j1 + 35, 1, 0xffffff);
			}
			int l3;
			if(mousex > i1 && mousex < i1 + c1 && mousey > (j1 + c2) - 16 && mousey < j1 + c2)
				l3 = 0xffff00;
			else
				l3 = 0xffffff;
			gamegfx.ug("Click here to add a name", i1 + c1 / 2, (j1 + c2) - 3, 1, l3);
		}
		if(!flag)
			return;
		i1 = mousex - (((graphics) (gamegfx)).width - 199);
		j1 = mousey - 36;
		if(i1 >= 0 && j1 >= 0 && i1 < 196 && j1 < 182)
		{
			gy.pd(i1 + (((graphics) (gamegfx)).width - 199), j1 + 36, lastclick, mouseclick);
			if(j1 <= 24 && mt == 1)
				if(i1 < 98 && iy == 1)
				{
					iy = 0;
					gy.zc(hy);
				} else
					if(i1 > 98 && iy == 0)
					{
						iy = 1;
						gy.zc(hy);
					}
			if(mt == 1 && iy == 0)
			{
				int i3 = gy.ic(hy);
				if(i3 >= 0 && mousex < 489)
					if(mousex > 429)
						removefriend(friends[i3]);
					else
						if(friendw[i3] != 0)
						{
							rcb = 2;
							jy = friends[i3];
							inputmessage = "";
							enteredmessage = "";
						}
			}
			if(mt == 1 && iy == 1)
			{
				int j3 = gy.ic(hy);
				if(j3 >= 0 && mousex < 489 && mousex > 429)
					removeignore(ignores[j3]);
			}
			if(j1 > 166 && mt == 1 && iy == 0)
			{
				rcb = 1;
				inputtext = "";
				enteredtext = "";
			}
			if(j1 > 166 && mt == 1 && iy == 1)
			{
				rcb = 3;
				inputtext = "";
				enteredtext = "";
			}
			mt = 0;
		}
	}

	public void wm(boolean flag)
	{
		int x = ((graphics) (gamegfx)).width - 199;
		int j1 = 36;
		gamegfx.xg(x - 49, 3, hu + 6);
		char c1 = '\304';
		gamegfx.drawtransquad(x, 36, c1, 65, graphics.rgbhash(181, 181, 181), 160);
		gamegfx.drawtransquad(x, 101, c1, 65, graphics.rgbhash(201, 201, 201), 160);
		gamegfx.drawtransquad(x, 166, c1, 95, graphics.rgbhash(181, 181, 181), 160);
		gamegfx.drawtransquad(x, 261, c1, 40, graphics.rgbhash(201, 201, 201), 160);
		int k1 = x + 3;
		int y = j1 + 15;
		gamegfx.drawstring("Game options - click to toggle", k1, y, 1, 0);
		y += 15;
		if (cameraauto) {
			gamegfx.drawstring("Camera angle mode - @gre@Auto", k1, y, 1, 0xffffff);
		} else {
			gamegfx.drawstring("Camera angle mode - @red@Manual", k1, y, 1, 0xffffff);
		}
		y += 15;
		if (mousebtns) {
			gamegfx.drawstring("Mouse buttons - @red@One", k1, y, 1, 0xffffff);
		} else {
			gamegfx.drawstring("Mouse buttons - @gre@Two", k1, y, 1, 0xffffff);
		}
		y += 15;
		if (member) {
			if (soundfx) {
				gamegfx.drawstring("Sound effects - @red@off", k1, y, 1, 0xffffff);
			} else {
				gamegfx.drawstring("Sound effects - @gre@on", k1, y, 1, 0xffffff);
			}
		}
		y += 15;
		y += 5;
		gamegfx.drawstring("Security settings", k1, y, 1, 0);
		y += 15;
		int color = 0xffffff;
		if(mousex > k1 && mousex < k1 + c1 && mousey > y - 12 && mousey < y + 4)
			color = 0xffff00;
		gamegfx.drawstring("Change password", k1, y, 1, color);
		y += 15;
		color = 0xffffff;
		if(mousex > k1 && mousex < k1 + c1 && mousey > y - 12 && mousey < y + 4)
			color = 0xffff00;
		gamegfx.drawstring("Change recovery questions", k1, y, 1, color);
		y += 15;
		y += 15;
		y += 5;
		gamegfx.drawstring("Privacy settings. Will be applied to", x + 3, y, 1, 0);
		y += 15;
		gamegfx.drawstring("all people not on your friends list", x + 3, y, 1, 0);
		y += 15;
		if (blockchat == 0) {
			gamegfx.drawstring("Block chat messages: @red@<off>", x + 3, y, 1, 0xffffff);
		} else {
			gamegfx.drawstring("Block chat messages: @gre@<on>", x + 3, y, 1, 0xffffff);
		}
		y += 15;
		if (blockpriv == 0) {
			gamegfx.drawstring("Block private messages: @red@<off>", x + 3, y, 1, 0xffffff);
		} else {
			gamegfx.drawstring("Block private messages: @gre@<on>", x + 3, y, 1, 0xffffff);
		}
		y += 15;
		if (blocktrade == 0) {
			gamegfx.drawstring("Block trade requests: @red@<off>", x + 3, y, 1, 0xffffff);
		} else {
			gamegfx.drawstring("Block trade requests: @gre@<on>", x + 3, y, 1, 0xffffff);
		}
		y += 15;
		if(member) {
			if (blockduel == 0) {
				gamegfx.drawstring("Block duel requests: @red@<off>", x + 3, y, 1, 0xffffff);
			} else {
				gamegfx.drawstring("Block duel requests: @gre@<on>", x + 3, y, 1, 0xffffff);
			}
		}
		y += 15;
		y += 5;
		gamegfx.drawstring("Always logout when you finish", k1, y, 1, 0);
		y += 15;
		color = 0xffffff;
		if(mousex > k1 && mousex < k1 + c1 && mousey > y - 12 && mousey < y + 4)
			color = 0xffff00;
		gamegfx.drawstring("Click here to logout", x + 3, y, 1, color);
		if(!flag)
			return;
		x = mousex - (((graphics) (gamegfx)).width - 199);
		j1 = mousey - 36;
		if(x >= 0 && j1 >= 0 && x < 196 && j1 < 265)
		{
			int l2 = ((graphics) (gamegfx)).width - 199;
			byte byte0 = 36;
			char c2 = '\304';
			int l1 = l2 + 3;
			int j2 = byte0 + 30;
			if(mousex > l1 && mousex < l1 + c2 && mousey > j2 - 12 && mousey < j2 + 4 && mt == 1)
			{
				cameraauto = !cameraauto;
				stream.create(213);
				stream.p1(0);
				stream.p1(cameraauto ? 1 : 0);
				stream.fmtdata();
			}
			j2 += 15;
			if(mousex > l1 && mousex < l1 + c2 && mousey > j2 - 12 && mousey < j2 + 4 && mt == 1)
			{
				mousebtns = !mousebtns;
				stream.create(213);
				stream.p1(2);
				stream.p1(mousebtns ? 1 : 0);
				stream.fmtdata();
			}
			j2 += 15;
			if(member && mousex > l1 && mousex < l1 + c2 && mousey > j2 - 12 && mousey < j2 + 4 && mt == 1)
			{
				soundfx = !soundfx;
				stream.create(213);
				stream.p1(3);
				stream.p1(soundfx ? 1 : 0);
				stream.fmtdata();
			}
			j2 += 15;
			j2 += 20;
			if(mousex > l1 && mousex < l1 + c2 && mousey > j2 - 12 && mousey < j2 + 4 && mt == 1)
			{
				scb = 6;
				inputtext = "";
				enteredtext = "";
			}
			j2 += 15;
			if(mousex > l1 && mousex < l1 + c2 && mousey > j2 - 12 && mousey < j2 + 4 && mt == 1)
			{
				stream.create(197);
				stream.fmtdata();
			}
			j2 += 15;
			j2 += 15;
			boolean updated = false;
			j2 += 35;
			if(mousex > l1 && mousex < l1 + c2 && mousey > j2 - 12 && mousey < j2 + 4 && mt == 1)
			{
				blockchat = 1 - blockchat;
				updated = true;
			}
			j2 += 15;
			if(mousex > l1 && mousex < l1 + c2 && mousey > j2 - 12 && mousey < j2 + 4 && mt == 1)
			{
				blockpriv = 1 - blockpriv;
				updated = true;
			}
			j2 += 15;
			if(mousex > l1 && mousex < l1 + c2 && mousey > j2 - 12 && mousey < j2 + 4 && mt == 1)
			{
				blocktrade = 1 - blocktrade;
				updated = true;
			}
			j2 += 15;
			if(member && mousex > l1 && mousex < l1 + c2 && mousey > j2 - 12 && mousey < j2 + 4 && mt == 1)
			{
				blockduel = 1 - blockduel;
				updated = true;
			}
			j2 += 15;
			if (updated) {
				updatesettings(blockchat, blockpriv, blocktrade, blockduel);
			}
			j2 += 20;
			if (mousex > l1 && mousex < l1 + c2 && mousey > j2 - 12 && mousey < j2 + 4 && mt == 1)
				reqlogout();
			mt = 0;
		}
	}

	public void dl()
	{
		int i1 = -1;
		for(int j1 = 0; j1 < uw; j1++)
			bx[j1] = false;

		for(int k1 = 0; k1 < dx; k1++)
			jx[k1] = false;

		int l1 = camera.ri();
		model ah[] = camera.oh();
		int ai[] = camera.mi();
		for(int i2 = 0; i2 < l1; i2++)
		{
			if(az > 200)
				break;
			int j2 = ai[i2];
			model h1 = ah[i2];
			if(h1.vh[j2] <= 65535 || h1.vh[j2] >= 0x30d40 && h1.vh[j2] <= 0x493e0)
				if(h1 == camera.ao)
				{
					int l2 = h1.vh[j2] % 10000;
					int k3 = h1.vh[j2] / 10000;
					if(k3 == 1)
					{
						String s1 = "";
						int j4 = 0;
						if(ourplayer.zr > 0 && players[l2].zr > 0)
							j4 = ourplayer.zr - players[l2].zr;
						if(j4 < 0)
							s1 = "@or1@";
						if(j4 < -3)
							s1 = "@or2@";
						if(j4 < -6)
							s1 = "@or3@";
						if(j4 < -9)
							s1 = "@red@";
						if(j4 > 0)
							s1 = "@gr1@";
						if(j4 > 3)
							s1 = "@gr2@";
						if(j4 > 6)
							s1 = "@gr3@";
						if(j4 > 9)
							s1 = "@gre@";
						s1 = " " + s1 + "(level-" + players[l2].zr + ")";
						if(fy >= 0)
						{
							if(cache.fmb[fy] == 1 || cache.fmb[fy] == 2 && regiony < 2203)
							{
								dz[az] = "Cast " + cache.bmb[fy] + " on";
								cz[az] = "@whi@" + players[l2].dr;
								ez[az] = 800;
								fz[az] = players[l2].x;
								gz[az] = players[l2].y;
								hz[az] = players[l2].serverindex;
								iz[az] = fy;
								az++;
							}
						} else
							if(qx >= 0)
							{
								dz[az] = "Use " + rx + " with";
								cz[az] = "@whi@" + players[l2].dr;
								ez[az] = 810;
								fz[az] = players[l2].x;
								gz[az] = players[l2].y;
								hz[az] = players[l2].serverindex;
								iz[az] = qx;
								az++;
							} else
							{
								if(regiony + worldh + localy < 2203 && (players[l2].y - 64) / regionarea + worldh + localy < 2203)
								{
									dz[az] = "Attack";
									cz[az] = "@whi@" + players[l2].dr + s1;
									if(j4 >= 0 && j4 < 5)
										ez[az] = 805;
									else
										ez[az] = 2805;
									fz[az] = players[l2].x;
									gz[az] = players[l2].y;
									hz[az] = players[l2].serverindex;
									az++;
								} else
									if(member)
									{
										dz[az] = "Duel with";
										cz[az] = "@whi@" + players[l2].dr + s1;
										fz[az] = players[l2].x;
										gz[az] = players[l2].y;
										ez[az] = 2806;
										hz[az] = players[l2].serverindex;
										az++;
									}
								dz[az] = "Trade with";
								cz[az] = "@whi@" + players[l2].dr;
								ez[az] = 2810;
								hz[az] = players[l2].serverindex;
								az++;
								dz[az] = "Follow";
								cz[az] = "@whi@" + players[l2].dr;
								ez[az] = 2820;
								hz[az] = players[l2].serverindex;
								az++;
							}
					} else
						if(k3 == 2)
						{
							if(fy >= 0)
							{
								if(cache.fmb[fy] == 3)
								{
									dz[az] = "Cast " + cache.bmb[fy] + " on";
									cz[az] = "@lre@" + cache.itemnames[rw[l2]];
									ez[az] = 200;
									fz[az] = pw[l2];
									gz[az] = qw[l2];
									hz[az] = rw[l2];
									iz[az] = fy;
									az++;
								}
							} else
								if(qx >= 0)
								{
									dz[az] = "Use " + rx + " with";
									cz[az] = "@lre@" + cache.itemnames[rw[l2]];
									ez[az] = 210;
									fz[az] = pw[l2];
									gz[az] = qw[l2];
									hz[az] = rw[l2];
									iz[az] = qx;
									az++;
								} else
								{
									dz[az] = "Take";
									cz[az] = "@lre@" + cache.itemnames[rw[l2]];
									ez[az] = 220;
									fz[az] = pw[l2];
									gz[az] = qw[l2];
									hz[az] = rw[l2];
									az++;
									dz[az] = "Examine";
									cz[az] = "@lre@" + cache.itemnames[rw[l2]];
									ez[az] = 3200;
									hz[az] = rw[l2];
									az++;
								}
						} else
							if(k3 == 3)
							{
								String s2 = "";
								int k4 = -1;
								int l4 = kw[l2].ir;
								if(cache.dkb[l4] > 0)
								{
									int i5 = (cache.zjb[l4] + cache.ckb[l4] + cache.akb[l4] + cache.bkb[l4]) / 4;
									int j5 = (vx[0] + vx[1] + vx[2] + vx[3] + 27) / 4;
									k4 = j5 - i5;
									s2 = "@yel@";
									if(k4 < 0)
										s2 = "@or1@";
									if(k4 < -3)
										s2 = "@or2@";
									if(k4 < -6)
										s2 = "@or3@";
									if(k4 < -9)
										s2 = "@red@";
									if(k4 > 0)
										s2 = "@gr1@";
									if(k4 > 3)
										s2 = "@gr2@";
									if(k4 > 6)
										s2 = "@gr3@";
									if(k4 > 9)
										s2 = "@gre@";
									s2 = " " + s2 + "(level-" + i5 + ")";
								}
								if(fy >= 0)
								{
									if(cache.fmb[fy] == 2)
									{
										dz[az] = "Cast " + cache.bmb[fy] + " on";
										cz[az] = "@yel@" + cache.wjb[kw[l2].ir];
										ez[az] = 700;
										fz[az] = kw[l2].x;
										gz[az] = kw[l2].y;
										hz[az] = kw[l2].serverindex;
										iz[az] = fy;
										az++;
									}
								} else
									if(qx >= 0)
									{
										dz[az] = "Use " + rx + " with";
										cz[az] = "@yel@" + cache.wjb[kw[l2].ir];
										ez[az] = 710;
										fz[az] = kw[l2].x;
										gz[az] = kw[l2].y;
										hz[az] = kw[l2].serverindex;
										iz[az] = qx;
										az++;
									} else
									{
										if(cache.dkb[l4] > 0)
										{
											dz[az] = "Attack";
											cz[az] = "@yel@" + cache.wjb[kw[l2].ir] + s2;
											if(k4 >= 0)
												ez[az] = 715;
											else
												ez[az] = 2715;
											fz[az] = kw[l2].x;
											gz[az] = kw[l2].y;
											hz[az] = kw[l2].serverindex;
											az++;
										}
										dz[az] = "Talk-to";
										cz[az] = "@yel@" + cache.wjb[kw[l2].ir];
										ez[az] = 720;
										fz[az] = kw[l2].x;
										gz[az] = kw[l2].y;
										hz[az] = kw[l2].serverindex;
										az++;
										if(!cache.yjb[l4].equals(""))
										{
											dz[az] = cache.yjb[l4];
											cz[az] = "@yel@" + cache.wjb[kw[l2].ir];
											ez[az] = 725;
											fz[az] = kw[l2].x;
											gz[az] = kw[l2].y;
											hz[az] = kw[l2].serverindex;
											az++;
										}
										dz[az] = "Examine";
										cz[az] = "@yel@" + cache.wjb[kw[l2].ir];
										ez[az] = 3700;
										hz[az] = kw[l2].ir;
										az++;
									}
							}
				} else
					if(h1 != null && h1.uh >= 10000)
					{
						int i3 = h1.uh - 10000;
						int l3 = ix[i3];
						if(!jx[i3])
						{
							if(fy >= 0)
							{
								if(cache.fmb[fy] == 4)
								{
									dz[az] = "Cast " + cache.bmb[fy] + " on";
									cz[az] = "@cya@" + cache.jlb[l3];
									ez[az] = 300;
									fz[az] = fx[i3];
									gz[az] = gx[i3];
									hz[az] = hx[i3];
									iz[az] = fy;
									az++;
								}
							} else
								if(qx >= 0)
								{
									dz[az] = "Use " + rx + " with";
									cz[az] = "@cya@" + cache.jlb[l3];
									ez[az] = 310;
									fz[az] = fx[i3];
									gz[az] = gx[i3];
									hz[az] = hx[i3];
									iz[az] = qx;
									az++;
								} else
								{
									if(!cache.llb[l3].equalsIgnoreCase("WalkTo"))
									{
										dz[az] = cache.llb[l3];
										cz[az] = "@cya@" + cache.jlb[l3];
										ez[az] = 320;
										fz[az] = fx[i3];
										gz[az] = gx[i3];
										hz[az] = hx[i3];
										az++;
									}
									if(!cache.mlb[l3].equalsIgnoreCase("Examine"))
									{
										dz[az] = cache.mlb[l3];
										cz[az] = "@cya@" + cache.jlb[l3];
										ez[az] = 2300;
										fz[az] = fx[i3];
										gz[az] = gx[i3];
										hz[az] = hx[i3];
										az++;
									}
									dz[az] = "Examine";
									cz[az] = "@cya@" + cache.jlb[l3];
									ez[az] = 3300;
									hz[az] = l3;
									az++;
								}
							jx[i3] = true;
						}
					} else
						if(h1 != null && h1.uh >= 0)
						{
							int j3 = h1.uh;
							int i4 = yw[j3];
							if(!bx[j3])
							{
								if(fy >= 0)
								{
									if(cache.fmb[fy] == 5)
									{
										dz[az] = "Cast " + cache.bmb[fy] + " on";
										cz[az] = "@cya@" + cache.zkb[i4];
										ez[az] = 400;
										fz[az] = ww[j3];
										gz[az] = xw[j3];
										hz[az] = zw[j3];
										iz[az] = yw[j3];
										jz[az] = fy;
										az++;
									}
								} else
									if(qx >= 0)
									{
										dz[az] = "Use " + rx + " with";
										cz[az] = "@cya@" + cache.zkb[i4];
										ez[az] = 410;
										fz[az] = ww[j3];
										gz[az] = xw[j3];
										hz[az] = zw[j3];
										iz[az] = yw[j3];
										jz[az] = qx;
										az++;
									} else
									{
										if(!cache.blb[i4].equalsIgnoreCase("WalkTo"))
										{
											dz[az] = cache.blb[i4];
											cz[az] = "@cya@" + cache.zkb[i4];
											ez[az] = 420;
											fz[az] = ww[j3];
											gz[az] = xw[j3];
											hz[az] = zw[j3];
											iz[az] = yw[j3];
											az++;
										}
										if(!cache.clb[i4].equalsIgnoreCase("Examine"))
										{
											dz[az] = cache.clb[i4];
											cz[az] = "@cya@" + cache.zkb[i4];
											ez[az] = 2400;
											fz[az] = ww[j3];
											gz[az] = xw[j3];
											hz[az] = zw[j3];
											iz[az] = yw[j3];
											az++;
										}
										dz[az] = "Examine";
										cz[az] = "@cya@" + cache.zkb[i4];
										ez[az] = 3400;
										hz[az] = i4;
										az++;
									}
								bx[j3] = true;
							}
						} else
						{
							if(j2 >= 0)
								j2 = h1.vh[j2] - 0x30d40;
							if(j2 >= 0)
								i1 = j2;
						}
		}

		if(fy >= 0 && cache.fmb[fy] <= 1)
		{
			dz[az] = "Cast " + cache.bmb[fy] + " on self";
			cz[az] = "";
			ez[az] = 1000;
			hz[az] = fy;
			az++;
		}
		if(i1 != -1)
		{
			int k2 = i1;
			if(fy >= 0)
			{
				if(cache.fmb[fy] == 6)
				{
					dz[az] = "Cast " + cache.bmb[fy] + " on ground";
					cz[az] = "";
					ez[az] = 900;
					fz[az] = landscape.xib[k2];
					gz[az] = landscape.yib[k2];
					hz[az] = fy;
					az++;
					return;
				}
			} else
				if(qx < 0)
				{
					dz[az] = "Walk here";
					cz[az] = "";
					ez[az] = 920;
					fz[az] = landscape.xib[k2];
					gz[az] = landscape.yib[k2];
					az++;
				}
		}
	}

	public void pm()
	{
		if(mt != 0)
		{
			for(int i1 = 0; i1 < az; i1++)
			{
				int k1 = wy + 2;
				int i2 = xy + 27 + i1 * 15;
				if(mousex <= k1 - 2 || mousey <= i2 - 12 || mousey >= i2 + 4 || mousex >= (k1 - 3) + yy)
					continue;
				bm(kz[i1]);
				break;
			}

			mt = 0;
			vy = false;
			return;
		}
		if(mousex < wy - 10 || mousey < xy - 10 || mousex > wy + yy + 10 || mousey > xy + zy + 10)
		{
			vy = false;
			return;
		}
		gamegfx.drawtransquad(wy, xy, yy, zy, 0xd0d0d0, 160);
		gamegfx.drawstring("Choose option", wy + 2, xy + 12, 1, 65535);
		for(int j1 = 0; j1 < az; j1++)
		{
			int l1 = wy + 2;
			int j2 = xy + 27 + j1 * 15;
			int k2 = 0xffffff;
			if(mousex > l1 - 2 && mousey > j2 - 12 && mousey < j2 + 4 && mousex < (l1 - 3) + yy)
				k2 = 0xffff00;
			gamegfx.drawstring(dz[kz[j1]] + " " + cz[kz[j1]], l1, j2, 1, k2);
		}

	}

	public void bl()
	{
		if(fy >= 0 || qx >= 0)
		{
			dz[az] = "Cancel";
			cz[az] = "";
			ez[az] = 4000;
			az++;
		}
		for(int i1 = 0; i1 < az; i1++)
			kz[i1] = i1;

		for(boolean flag = false; !flag;)
		{
			flag = true;
			for(int j1 = 0; j1 < az - 1; j1++)
			{
				int l1 = kz[j1];
				int j2 = kz[j1 + 1];
				if(ez[l1] > ez[j2])
				{
					kz[j1] = j2;
					kz[j1 + 1] = l1;
					flag = false;
				}
			}

		}

		if(az > 20)
			az = 20;
		if(az > 0)
		{
			int k1 = -1;
			for(int i2 = 0; i2 < az; i2++)
			{
				if(cz[kz[i2]] == null || cz[kz[i2]].length() <= 0)
					continue;
				k1 = i2;
				break;
			}

			String s1 = null;
			if((qx >= 0 || fy >= 0) && az == 1)
				s1 = "Choose a target";
			else
				if((qx >= 0 || fy >= 0) && az > 1)
					s1 = "@whi@" + dz[kz[0]] + " " + cz[kz[0]];
				else
					if(k1 != -1)
						s1 = cz[kz[k1]] + ": @whi@" + dz[kz[0]];
			if(az == 2 && s1 != null)
				s1 = s1 + "@whi@ / 1 more option";
			if(az > 2 && s1 != null)
				s1 = s1 + "@whi@ / " + (az - 1) + " more options";
			if(s1 != null)
				gamegfx.drawstring(s1, 6, 14, 1, 0xffff00);
			if(!mousebtns && mt == 1 || mousebtns && mt == 1 && az == 1)
			{
				bm(kz[0]);
				mt = 0;
				return;
			}
			if(!mousebtns && mt == 2 || mousebtns && mt == 1)
			{
				zy = (az + 1) * 15;
				yy = gamegfx.textwidth("Choose option", 1) + 5;
				for(int k2 = 0; k2 < az; k2++)
				{
					int l2 = gamegfx.textwidth(dz[k2] + " " + cz[k2], 1) + 5;
					if(l2 > yy)
						yy = l2;
				}

				wy = mousex - yy / 2;
				xy = mousey - 7;
				vy = true;
				if(wy < 0)
					wy = 0;
				if(xy < 0)
					xy = 0;
				if(wy + yy > 510)
					wy = 510 - yy;
				if(xy + zy > 315)
					xy = 315 - zy;
				mt = 0;
			}
		}
	}

	public void bm(int i1)
	{
		int j1 = fz[i1];
		int k1 = gz[i1];
		int l1 = hz[i1];
		int i2 = iz[i1];
		int j2 = jz[i1];
		int k2 = ez[i1];
		if(k2 == 200)
		{
			im(regionx, regiony, j1, k1, true);
			stream.create(224);
			stream.p2(j1 + localx);
			stream.p2(k1 + localy);
			stream.p2(l1);
			stream.p2(i2);
			stream.fmtdata();
			fy = -1;
		}
		if(k2 == 210)
		{
			im(regionx, regiony, j1, k1, true);
			stream.create(250);
			stream.p2(j1 + localx);
			stream.p2(k1 + localy);
			stream.p2(l1);
			stream.p2(i2);
			stream.fmtdata();
			qx = -1;
		}
		if(k2 == 220)
		{
			im(regionx, regiony, j1, k1, true);
			stream.create(252);
			stream.p2(j1 + localx);
			stream.p2(k1 + localy);
			stream.p2(l1);
			stream.fmtdata();
		}
		if(k2 == 3200)
			ik(cache.itemexamines[l1], 3);
		if(k2 == 300)
		{
			zk(j1, k1, l1);
			stream.create(223);
			stream.p2(j1 + localx);
			stream.p2(k1 + localy);
			stream.p1(l1);
			stream.p2(i2);
			stream.fmtdata();
			fy = -1;
		}
		if(k2 == 310)
		{
			zk(j1, k1, l1);
			stream.create(239);
			stream.p2(j1 + localx);
			stream.p2(k1 + localy);
			stream.p1(l1);
			stream.p2(i2);
			stream.fmtdata();
			qx = -1;
		}
		if(k2 == 320)
		{
			zk(j1, k1, l1);
			stream.create(238);
			stream.p2(j1 + localx);
			stream.p2(k1 + localy);
			stream.p1(l1);
			stream.fmtdata();
		}
		if(k2 == 2300)
		{
			zk(j1, k1, l1);
			stream.create(229);
			stream.p2(j1 + localx);
			stream.p2(k1 + localy);
			stream.p1(l1);
			stream.fmtdata();
		}
		if(k2 == 3300)
			ik(cache.klb[l1], 3);
		if(k2 == 400)
		{
			kk(j1, k1, l1, i2);
			stream.create(222);
			stream.p2(j1 + localx);
			stream.p2(k1 + localy);
			stream.p2(j2);
			stream.fmtdata();
			fy = -1;
		}
		if(k2 == 410)
		{
			kk(j1, k1, l1, i2);
			stream.create(241);
			stream.p2(j1 + localx);
			stream.p2(k1 + localy);
			stream.p2(j2);
			stream.fmtdata();
			qx = -1;
		}
		if(k2 == 420)
		{
			kk(j1, k1, l1, i2);
			stream.create(242);
			stream.p2(j1 + localx);
			stream.p2(k1 + localy);
			stream.fmtdata();
		}
		if(k2 == 2400)
		{
			kk(j1, k1, l1, i2);
			stream.create(230);
			stream.p2(j1 + localx);
			stream.p2(k1 + localy);
			stream.fmtdata();
		}
		if(k2 == 3400)
			ik(cache.alb[l1], 3);
		if(k2 == 600)
		{
			stream.create(220);
			stream.p2(l1);
			stream.p2(i2);
			stream.fmtdata();
			fy = -1;
		}
		if(k2 == 610)
		{
			stream.create(240);
			stream.p2(l1);
			stream.p2(i2);
			stream.fmtdata();
			qx = -1;
		}
		if(k2 == 620)
		{
			stream.create(248);
			stream.p2(l1);
			stream.fmtdata();
		}
		if(k2 == 630)
		{
			stream.create(249);
			stream.p2(l1);
			stream.fmtdata();
		}
		if(k2 == 640)
		{
			stream.create(246);
			stream.p2(l1);
			stream.fmtdata();
		}
		if(k2 == 650)
		{
			qx = l1;
			kx = 0;
			rx = cache.itemnames[nx[qx]];
		}
		if(k2 == 660)
		{
			stream.create(251);
			stream.p2(l1);
			stream.fmtdata();
			qx = -1;
			kx = 0;
			ik("Dropping " + cache.itemnames[nx[l1]], 4);
		}
		if(k2 == 3600)
			ik(cache.itemexamines[l1], 3);
		if(k2 == 700)
		{
			int l2 = (j1 - 64) / regionarea;
			int l4 = (k1 - 64) / regionarea;
			cl(regionx, regiony, l2, l4, true);
			stream.create(225);
			stream.p2(l1);
			stream.p2(i2);
			stream.fmtdata();
			fy = -1;
		}
		if(k2 == 710)
		{
			int i3 = (j1 - 64) / regionarea;
			int i5 = (k1 - 64) / regionarea;
			cl(regionx, regiony, i3, i5, true);
			stream.create(243);
			stream.p2(l1);
			stream.p2(i2);
			stream.fmtdata();
			qx = -1;
		}
		if(k2 == 720)
		{
			int j3 = (j1 - 64) / regionarea;
			int j5 = (k1 - 64) / regionarea;
			cl(regionx, regiony, j3, j5, true);
			stream.create(245);
			stream.p2(l1);
			stream.fmtdata();
		}
		if(k2 == 725)
		{
			int k3 = (j1 - 64) / regionarea;
			int k5 = (k1 - 64) / regionarea;
			cl(regionx, regiony, k3, k5, true);
			stream.create(195);
			stream.p2(l1);
			stream.fmtdata();
		}
		if(k2 == 715 || k2 == 2715)
		{
			int l3 = (j1 - 64) / regionarea;
			int l5 = (k1 - 64) / regionarea;
			cl(regionx, regiony, l3, l5, true);
			stream.create(244);
			stream.p2(l1);
			stream.fmtdata();
		}
		if(k2 == 3700)
			ik(cache.xjb[l1], 3);
		if(k2 == 800)
		{
			int i4 = (j1 - 64) / regionarea;
			int i6 = (k1 - 64) / regionarea;
			cl(regionx, regiony, i4, i6, true);
			stream.create(226);
			stream.p2(l1);
			stream.p2(i2);
			stream.fmtdata();
			fy = -1;
		}
		if(k2 == 810)
		{
			int j4 = (j1 - 64) / regionarea;
			int j6 = (k1 - 64) / regionarea;
			cl(regionx, regiony, j4, j6, true);
			stream.create(219);
			stream.p2(l1);
			stream.p2(i2);
			stream.fmtdata();
			qx = -1;
		}
		if(k2 == 805 || k2 == 2805)
		{
			int k4 = (j1 - 64) / regionarea;
			int k6 = (k1 - 64) / regionarea;
			cl(regionx, regiony, k4, k6, true);
			stream.create(228);
			stream.p2(l1);
			stream.fmtdata();
		}
		if(k2 == 2806)
		{
			stream.create(204);
			stream.p2(l1);
			stream.fmtdata();
		}
		if(k2 == 2810)
		{
			stream.create(235);
			stream.p2(l1);
			stream.fmtdata();
		}
		if(k2 == 2820)
		{
			stream.create(214);
			stream.p2(l1);
			stream.fmtdata();
		}
		if(k2 == 900)
		{
			cl(regionx, regiony, j1, k1, true);
			stream.create(221);
			stream.p2(j1 + localx);
			stream.p2(k1 + localy);
			stream.p2(l1);
			stream.fmtdata();
			fy = -1;
		}
		if(k2 == 920)
		{
			cl(regionx, regiony, j1, k1, false);
			if(tu == -24)
				tu = 24;
		}
		if(k2 == 1000)
		{
			stream.create(227);
			stream.p2(l1);
			stream.fmtdata();
			fy = -1;
		}
		if(k2 == 4000)
		{
			qx = -1;
			fy = -1;
		}
	}

	public mudclient()
	{
		member = false;
		exponent = new BigInteger("109774655680521489849841286173837658286681104472611153213811630753140883393493");
		modulus = new BigInteger("64649913839432406085419973565023480135601663298031923145462865211488091291617");
		invalidhost = false;
		it = false;
		live = true;
		lt = 0xbc614e;
		nt = 8000;
		ot = new int[nt];
		pt = new int[nt];
		rt = new int[8192];
		st = new int[8192];
		ut = 2;
		wt = 2;
		regionarea = 128;
		width = 512;
		height = 334;
		gu = 9;
		nu = 40;
		ru = -1;
		su = -1;
		av = -1;
		playerheight = -1;
		lv = 550;
		mv = false;
		pv = 1;
		rv = 128;
		tv = 4000;
		uv = 500;
		newplayers = new livingentity[tv];
		players = new livingentity[uv];
		lastplayers = new livingentity[uv];
		ourplayer = new livingentity();
		playerindex = -1;
		fw = 2500;
		gw = 500;
		jw = new livingentity[fw];
		kw = new livingentity[gw];
		lw = new livingentity[gw];
		newpids = new int[500];
		nw = 500;
		pw = new int[nw];
		qw = new int[nw];
		rw = new int[nw];
		sw = new int[nw];
		tw = 1500;
		vw = new model[tw];
		ww = new int[tw];
		xw = new int[tw];
		yw = new int[tw];
		zw = new int[tw];
		gamemodels = new model[200];
		bx = new boolean[tw];
		cx = 500;
		ex = new model[cx];
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
		experience = new int[99];
		ux = new int[18];
		vx = new int[18];
		wx = new int[18];
		xx = new int[5];
		fy = -1;
		ny = 27;
		py = new boolean[ny];
		qy = new boolean[50];
		ry = false;
		cameraauto = true;
		mousebtns = false;
		soundfx = false;
		vy = false;
		bz = 250;
		cz = new String[bz];
		dz = new String[bz];
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
		loadregion = false;
		feb = "";
		geb = "";
		heb = "";
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

	public boolean member;
	public static String cachedir = "";
	public BigInteger exponent;
	public BigInteger modulus;
	int gt;
	boolean invalidhost;
	boolean it;
	public boolean live;
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
	Graphics gfx;
	camera camera;
	gamegraphics gamegfx;
	int bu;
	int regionarea;
	int du;
	int width;
	int height;
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
	world landscape;
	int worldw;
	int worldh;
	int heightmod;
	int av;
	int localx;
	int localy;
	int playerheight;
	int ev;
	int fv;
	int gv;
	int hv;
	int iv;
	int jv;
	int kv;
	int lv;
	boolean mv;
	int ourx;
	int oury;
	int pv;
	int qv;
	int rv;
	int sv;
	int tv;
	int uv;
	int pcount;
	int lastpcount;
	int xv;
	livingentity newplayers[];
	livingentity players[];
	livingentity lastplayers[];
	livingentity ourplayer;
	int regionx;
	int regiony;
	int playerindex;
	int fw;
	int gw;
	int hw;
	int iw;
	livingentity jw[];
	livingentity kw[];
	livingentity lw[];
	int newpids[];
	int nw;
	int ow;
	int pw[];
	int qw[];
	int rw[];
	int sw[];
	int tw;
	int uw;
	model vw[];
	int ww[];
	int xw[];
	int yw[];
	int zw[];
	model gamemodels[];
	boolean bx[];
	int cx;
	int dx;
	model ex[];
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
	int experience[];
	final int tx = 18;
	int ux[];
	int vx[];
	int wx[];
	int xx[];
	int yx;
	String skillnames[] = {
			"Attack", "Defense", "Strength", "Hits", "Ranged", "Prayer", "Magic", "Cooking", "Woodcut", "Fletching", 
			"Fishing", "Firemaking", "Crafting", "Smithing", "Mining", "Herblaw", "Carpentry", "Thieving"
	};
	String ay[] = {
			"Attack", "Defense", "Strength", "Hits", "Ranged", "Prayer", "Magic", "Cooking", "Woodcutting", "Fletching", 
			"Fishing", "Firemaking", "Crafting", "Smithing", "Mining", "Herblaw", "Carpentry", "Thieving"
	};
	String by[] = {
			"Armour", "WeaponAim", "WeaponPower", "Magic", "Prayer"
	};
	menu cy;
	int dy;
	int ey;
	int fy;
	menu gy;
	int hy;
	int iy;
	long jy;
	menu ky;
	int ly;
	int my;
	int ny;
	String oy[] = {
			"Black knight's fortress", "Cook's assistant", "Demon slayer", "Doric's quest", "The restless ghost", "Goblin diplomacy", "Ernest the chicken", "Imp catcher", "Pirate's treasure", "Prince Ali rescue", 
			"Romeo & Juliet", "Sheep shearer", "Shield of Arrav", "The knight's sword", "Vampire slayer", "Witch's potion", "Dragon slayer", "Witch's house (members)", "Lost city (members)", "Hero's quest (members)", 
			"Druidic ritual (members)", "Merlin's crystal (members)", "Scorpion catcher (members)", "Family crest (members)", "Tribal totem (members)", "Fishing contest (members)", "Monk's friend (members)"
	};
	boolean py[];
	boolean qy[];
	boolean ry;
	boolean cameraauto;
	boolean mousebtns;
	boolean soundfx;
	boolean vy;
	int wy;
	int xy;
	int yy;
	int zy;
	int az;
	int bz;
	String cz[];
	String dz[];
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
	menu pz;
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
	int xcb;
	String ycb;
	int zcb;
	int adb;
	int bdb;
	boolean cdb;
	String ddb;
	int edb;
	int fdb;
	int gdb;
	int hdb;
	boolean loadregion;
	int jdb;
	menu kdb;
	int ldb;
	int mdb;
	menu ndb;
	int odb;
	int pdb;
	int qdb;
	int rdb;
	int sdb;
	int tdb;
	int udb;
	int vdb;
	menu wdb;
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
	String heb;
	String ieb;
	menu jeb;
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
	menu afb;
	int bfb;
	int cfb;
	int dfb;
	int efb[];
	int ffb[];
	int gfb[];
	int hfb[];
	int ifb[] = {
			0, 1, 2, 3, 4
	};
	String jfb[];
	boolean kfb;
	menu lfb;
	int mfb;
	int nfb;
	int ofb;
	int pfb;
	int qfb;
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
	int pgb[][] = {
			{
				11, 2, 9, 7, 1, 6, 10, 0, 5, 8, 
				3, 4
			}, {
				11, 2, 9, 7, 1, 6, 10, 0, 5, 8, 
				3, 4
			}, {
				11, 3, 2, 9, 7, 1, 6, 10, 0, 5, 
				8, 4
			}, {
				3, 4, 2, 9, 7, 1, 6, 10, 8, 11, 
				0, 5
			}, {
				3, 4, 2, 9, 7, 1, 6, 10, 8, 11, 
				0, 5
			}, {
				4, 3, 2, 9, 7, 1, 6, 10, 8, 11, 
				0, 5
			}, {
				11, 4, 2, 9, 7, 1, 6, 10, 0, 5, 
				8, 3
			}, {
				11, 2, 9, 7, 1, 6, 10, 0, 5, 8, 
				4, 3
			}
	};
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
	public int ahb[] = {
			0xff0000, 0xff8000, 0xffe000, 0xa0e000, 57344, 32768, 41088, 45311, 33023, 12528, 
			0xe000e0, 0x303030, 0x604000, 0x805000, 0xffffff
	};
	public int bhb[] = {
			0xffc030, 0xffa040, 0x805030, 0x604020, 0x303030, 0xff6020, 0xff4000, 0xffffff, 65280, 65535
	};
	public int chb[] = {
			0xecded0, 0xccb366, 0xb38c40, 0x997326, 0x906020
	};
	int dhb[] = {
			0, 1, 2, 1
	};
	int ehb[] = {
			0, 1, 2, 1, 0, 0, 0, 0
	};
	int fhb[] = {
			0, 0, 0, 0, 0, 1, 2, 1
	};
	byte soundsarc[];
	sound soundstream;
	int ihb;
	int jhb[];
	int khb[];
	int lhb[];
	int mhb[];

	String recovquestions[] = {
			"Where were you born?", "What was your first teacher's name?", "What is your father's middle name?", "Who was your first best friend?", "What is your favourite vacation spot?", "What is your mother's middle name?", "What was your first pet's name?", "What was the name of your first school?", "What is your mother's maiden name?", "Who was your first boyfriend/girlfriend?", 
			"What was the first computer game you purchased?", "Who is your favourite actor/actress?", "Who is your favourite author?", "Who is your favourite musician?", "Who is your favourite cartoon character?", "What is your favourite book?", "What is your favourite food?", "What is your favourite movie?"
	};

}
