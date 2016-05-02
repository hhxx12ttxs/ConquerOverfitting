package netproj.hosts;

import java.util.Random;

import netproj.skeleton.Packet;

public class ProbFlowSender extends Host {

	public ProbFlowSender(int inputBuffSize, int outputBuffSize, int address) {
		super(inputBuffSize, outputBuffSize, address);
		// TODO Auto-generated constructor stub
	}

	public void sendAbsGaussian(final int dest_ip, final int ttl, final double variance, final int size) {
		final Random rnd = new Random();
		final Host container = (Host) this; 
		new Thread() {
			public void run(){
				while (true) {
					long wait_time = (long) Math.abs(rnd.nextGaussian() * variance);
					Packet p = new Packet(container.getAddress(), dest_ip, size, ttl);
					try {
						sleep(wait_time);
					} catch (InterruptedException e){
						// shrug
					}
					container.sendPacket(p);
				}
			}
		}.start();
	}
	
	public void sendPoisson(final int dest_ip, final int ttl, final double lambda, final int size) {
		final Host container = (Host) this; 
		new Thread() {
			public void run(){
				while (true) {
					long wait_time = (long) getPoisson(lambda);
					Packet p = new Packet(container.getAddress(), dest_ip, size, ttl);
					try {
						sleep(wait_time);
					} catch (InterruptedException e){
						// shrug
					}
					container.sendPacket(p);
				}
			}
		}.start();
	}
	
	public static int getPoisson(double lambda) {
		double L = Math.exp(-lambda);
		double p = 1.0;
		int k = 0;

		do {
		  k++;
		  p *= Math.random();
		} while (p > L);
		return k - 1;
	}

	@Override
	public void processPacket(Packet packet) {
		// drop it like its hot
	}
	@Override
	public void handleCommand(String command) {
		if (command.startsWith("gsend ")) {
			int address = Integer.decode(command.split(" ")[1]);
			int ttl = Integer.decode(command.split(" ")[2]);
			double variance = Double.parseDouble(command.split(" ")[3]);
			int size = Integer.decode(command.split(" ")[4]);
			this.sendAbsGaussian(address, ttl, variance, size);
			System.out.println("Gaussian flow started.");
		} if (command.startsWith("psend ")) {
			int address = Integer.decode(command.split(" ")[1]);
			int ttl = Integer.decode(command.split(" ")[2]);
			double lambda = Double.parseDouble(command.split(" ")[3]);
			int size = Integer.decode(command.split(" ")[4]);
			this.sendPoisson(address, ttl, lambda, size);
			System.out.println("Poisson flow started.");
		} else {
			super.handleCommand(command);
		}
	}

}

