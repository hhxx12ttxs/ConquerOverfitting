package struktur;

import java.io.Serializable;
import java.util.ArrayList;

import server.DMXUniversum;

public class Chaser implements Serializable {
	public int sortid;
	boolean play = false;
	Signal[] signals;
	String name;
	int tapcount = 0;
	int lasttap = 0;
	double counter; 		/* position des chasers */
	double speed = 0.5; 	/* geschwindigkeit des chasers */
	String type = "Dim";
	boolean relativ = false; /*	relativer chaser */
	int channelsize = 1; 	 /* Aufl&#x161;sung pro Kanal (1 = 8 bit, 2 = 16 bit, ...) */
	boolean rueckwaerts = false;
	boolean hinher = false;
	boolean invertdelay = false;
	boolean rueckdelay = false;
	
	ArrayList<Chaser> connected = new ArrayList<Chaser>();
	
	public Chaser() {
		sortid = DMXUniversum.chaserList.size();
		counter = 0;
		signals = new Signal[1];
		fuelleSignalarray();
	}
	
	public Chaser(int v) {
		sortid = DMXUniversum.chaserList.size();
		counter = 0;
		signals = new Signal[v];
		fuelleSignalarray();
	}
	
	public Chaser(int v, String n) {
		sortid = DMXUniversum.chaserList.size();
		counter = 0;
		signals = new Signal[v];
		name = n;
		fuelleSignalarray();
	}
	
	public Chaser(int v, String n, String t) {
		sortid = DMXUniversum.chaserList.size();
		counter = 0;
		signals = new Signal[v];
		name = n;
		type = t;
		fuelleSignalarray();
	}
	
	public Chaser(String n) {
		sortid = DMXUniversum.chaserList.size();
		counter = 0;
		name = n;
		signals = new Signal[1];
		fuelleSignalarray();
	}
	
	public Chaser(String n, String t) {
		sortid = DMXUniversum.chaserList.size();
		counter = 0;
		name = n;
		type = t;
		fuelleSignalarray();
	}

	private void fuelleSignalarray() { /* signalarray mit signalen f&#x;llen (sonst gibts ne exception) */
		int maxvalue = 256;
		for(int i = 1 ; i < channelsize ; i++)
			maxvalue = maxvalue * maxvalue;
		maxvalue = maxvalue - 1;
		
		for(int i = 0 ; i < signals.length ; i++)
			signals[i] = new Signal(new StepLinear(0,maxvalue));
	}
	
	public int getID() {
		return DMXUniversum.chaserList.indexOf(this);
	}
	
	public void setName(String n) {
		name = n;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean getRueckwaerts() {
		return rueckwaerts;
	}
	
	public boolean getInvertdelay() {
		return invertdelay;
	}
	
	public void toggleInverdelay() {
		invertdelay = !invertdelay;
	}
	
	public boolean getRueckdelay() {
		return rueckdelay;
	}
	
	public void toggleRueckdelay() {
		rueckdelay = !rueckdelay;
	}
	
	public void toggleRueckwaerts() {
		rueckwaerts = !rueckwaerts;
	}
	
	public boolean getHinHer() {
		return hinher;
	}
	
	public void toggleHinHer() {
		hinher = !hinher;
	}
	
	public Chaser[] getConnected() {
		Chaser[] ret = new Chaser[connected.size()];
		for(int i = 0 ; i < connected.size() ; i++)
			ret[i] = connected.get(i);
		return ret;
	}
	
	public void setType(String t) {
		if(t.equalsIgnoreCase("RGB")) {
			setChannelSize(1);
			setSize(3);
		}
		
		if(t.equalsIgnoreCase("XY")) {
			setChannelSize(1);
			setSize(2);
			relativ = true;
		}
		
		if(t.equalsIgnoreCase("XY16")) {
			setChannelSize(2);
			setSize(2);
			relativ = true;
		}

		if(t.equalsIgnoreCase("Dim")) {
			setChannelSize(1);
			setSize(1);
		}

		type = t;
	}
	
	public void setSize(int s) {
		if(signals.length != s && s > 0) {
			/* chaser aus allen fixtures mit zu vielen kan&#x160;len werfen */
			for(int i = 0 ; i < DMXUniversum.fixtureList.size() ; i++) {
				Fixture f = DMXUniversum.fixtureList.get(i);
				ChannelGroup[] cg = f.getChannels();
				for(int j = 0 ; j < cg.length ; j++)
					if(cg[j].getLength() != s)
						cg[j].removeChaser(this);
			}
			
			type = "";
			signals = new Signal[(s)];
			fuelleSignalarray();
		}
	}
	
	public void setChannelActive(int i, boolean v) {
		signals[i].setActive(v);
	}
	
	public void setChannelSize(int i) {
		channelsize = i;
	}
	
	public void toggleConnection(int id) {
		Chaser c = DMXUniversum.chaserList.get(id);
		if(connected.contains(c))
			removeConnection(id);
		else
			addConnection(id);
	}
	
	public void addConnection(int id) {
		Chaser c = DMXUniversum.chaserList.get(id);
		c.addConnection(this);
		connected.add(c);
	}
	
	private void addConnection(Chaser c) {
		connected.add(c);
	}
	
	public void removeConnection(int id) {
		Chaser c = DMXUniversum.chaserList.get(id);
		c.removeConnection(this);
		connected.remove(c);
	}
	
	private void removeConnection(Chaser c) {
		connected.remove(c);
	}
	
	public void addStep() {
		for(Signal s: signals)
			s.addStep();
	}
	
	public void removeStep() {
		for(Signal s: signals)
			s.removeStep();
	}
	
	public String getType() {
		return type;
	}
	
	public int[][][] getSignal() {
		int signallaenge = 0;
		if(signals[0] != null)
			signallaenge = signals[0].getSize();
		int signalanzahl = signals.length;
			
		int[][][] ret = new int[signalanzahl][signallaenge][0];
		
		for(int i = 0 ; i < signalanzahl ; i++)
			for(int j = 0 ; j < signallaenge ; j++) {
				ret[i][j] = signals[i].getStep(j);
			}
		return ret;
	}
	
	public int getSignalCount() { // anzahl der verschiedenen signale
		return signals.length;
	}
	
	public int getChaserSize() { // anzahl der kan&#x160;le
		return signals.length * channelsize;
	}
	
	public int getRange() {
		return 256 * channelsize;
	}
	
	public void setChaserStep(int sid, int spos, int steppos, int value) {
		signals[sid].setValue(spos, steppos, value);
	}
	
	public void start() {
		startc();
		for(Chaser c : connected)
			c.startc();
	}
	
	private void startc() {
		play = true;
	}
	
	public void stop() {
		stopc();
		for(Chaser c : connected)
			c.stopc();
	}
	
	private void stopc() {
		play = false;
		
		/* chaser flag aus fixtures nehmen */
		for(Fixture fix: DMXUniversum.fixtureList)
			fix.removeChaserFlag(this);
	}
	
	public void startSync() {
		tapSync();
		start();
	}
	
	public void stopSync() {
		tapSync();
		stop();
	}
	
	/* wert aus signal zur&#x;ck geben */
	public boolean isActive() {
		return play;
	}
	
	public boolean isRelativ() {
		return relativ;
	}
	
	public void count() {
		if(hinher) {
			double t0 = (counter % signals[0].getSize());
			double t1 = ((counter + Math.abs(speed)) % signals[0].getSize());
			if(rueckwaerts)
				t1 = ((counter - Math.abs(speed)) % signals[0].getSize());
			if( (!rueckwaerts && t0 > t1) || (rueckwaerts && t0 < t1) )
				if(invertdelay)
					rueckdelay = !rueckdelay;
				else 
					rueckwaerts = !rueckwaerts;
		}
		
		if(rueckwaerts)
			counter = counter - Math.abs(speed);
		else
			counter = counter + Math.abs(speed);
		tapcount++;
	}
	
	/* offset der lampe... */
	public int[] getValue(int offset) { // chaser ausgabe
		int[] ret = new int[signals.length * channelsize];
		
		int rtemp = 0;
		
		for(int i = 0 ; i < signals.length ; i++) {
			rtemp = signals[i].getValue(counter + offset);
			for(int j = channelsize - 1 ; j >= 0 ; j--) {
				ret[(i*channelsize) + j] = rtemp % 256;
				rtemp = rtemp / 256;
			}
		}
		return ret;
	}

	public void setSpeed(double value) {
		speed = value;
	}
	
	public void tapSync() {
		if(speed > 0) {
			if((tapcount - lasttap) > 0 && (tapcount - lasttap) < 400)
				setSpeed(1.0 / (double) (tapcount - lasttap));
			lasttap = tapcount;
		}
	}
	
	public void setSpeedMS(int value) {
		if(value >= 40 || value <= -40)
			setSpeed(1000 / ((float) value * 50));
	}
	
	public int getSpeedMS() {
		return (int) (getSpeedSec() * 1000);
	}
	
	public double getSpeedSec() {
		return 1 / (speed * 50);
	}

	public void remove() {
		for(Chaser c : DMXUniversum.chaserList)
			c.removeConnected(this);

		for(int i = 0 ; i < DMXUniversum.fixtureList.size() ; i++) {
			Fixture f = DMXUniversum.fixtureList.get(i);
			ChannelGroup[] cg = f.getChannels();
			for(int j = 0 ; j < cg.length ; j++)
				cg[j].removeChaser(this);
		}
		DMXUniversum.chaserList.remove(this);
	}
	
	private void removeConnected(Chaser chaser) {
		connected.remove(chaser);
	}
		
}

