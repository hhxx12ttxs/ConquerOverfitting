package it.mobileos.fattazzo.app;

import android.net.TrafficStats;
import android.widget.TextView;

/**
 * 
 * @author fattazzo - fattazzo82 [at] gmail . com
 * 
 */
public class UpdateTrafficaRunnable implements Runnable {

	private MainActivity mainActivity;

	private long lastRxBytes = 0;
	private long lastTxBytes = 0;
	private long lastRxPackets = 0;
	private long lastTxPackets = 0;

	public UpdateTrafficaRunnable(MainActivity mainActivity) {
		super();
		this.mainActivity = mainActivity;
	}

	private long getRxLiveBytes(long rxTotalBytes) {
		long result = rxTotalBytes - lastRxBytes;
		;
		if (lastRxBytes == 0) {
			result = 0;
		}
		lastRxBytes = rxTotalBytes;

		return result;
	}

	private long getRxLivePackests(long rxTotalPackets) {
		long result = rxTotalPackets - lastRxPackets;
		if (lastRxPackets == 0) {
			result = 0;
		}
		lastRxPackets = rxTotalPackets;

		return result;
	}

	private long getTxLiveBytes(long txTotalBytes) {
		long result = txTotalBytes - lastTxBytes;
		;
		if (lastTxBytes == 0) {
			result = 0;
		}
		lastTxBytes = txTotalBytes;

		return result;
	}

	private long getTxLivePackests(long txTotalPackets) {
		long result = txTotalPackets - lastTxPackets;
		if (lastTxPackets == 0) {
			result = 0;
		}
		lastTxPackets = txTotalPackets;

		return result;
	}

	@Override
	public void run() {
		TextView rxTextView = (TextView) mainActivity.findViewById(R.id.rxTraffic);
		TextView txTextView = (TextView) mainActivity.findViewById(R.id.txTraffic);

		long rxTotalBytes = TrafficStats.getTotalRxBytes();
		long rxTotalPackets = TrafficStats.getTotalRxPackets();
		long txTotalBytes = TrafficStats.getTotalTxBytes();
		long txTotalPackets = TrafficStats.getTotalTxPackets();

		long rxLiveBytes = getRxLiveBytes(rxTotalBytes);
		long rxLivePackets = getRxLivePackests(rxTotalPackets);
		long rxOverallBytes = rxTotalBytes - mainActivity.getStartingRXByte();
		long rxOverallPackets = rxTotalPackets - mainActivity.getStartingRXPackets();

		long txLiveBytes = getTxLiveBytes(txTotalBytes);
		long txLivePackets = getTxLivePackests(txTotalPackets);
		long txOverallBytes = txTotalBytes - mainActivity.getStartingTXByte();
		long txOverallPackets = txTotalPackets - mainActivity.getStartingTXPackets();

		// aggiorno le text view dei dati trasmessi e inviati
		rxTextView.setText(Long.toString(rxOverallBytes) + " / " + Long.toString(rxOverallPackets));
		txTextView.setText(Long.toString(txOverallBytes) + " / " + Long.toString(txOverallPackets));

		// aggiorno il grafico con i nuovi valori
		mainActivity.updateGraph(rxOverallBytes, txOverallBytes, rxOverallPackets, txOverallPackets, rxLiveBytes,
				txLiveBytes, rxLivePackets, txLivePackets);

		// rilancio il monitoraggio tra 1 secondo
		mainActivity.getTrafficHandler().postDelayed(this, 1000);
	}
}

