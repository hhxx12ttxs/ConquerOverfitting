/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crimsonrpg.core.plots;

import com.crimsonrpg.api.plot.Plot;
import com.crimsonrpg.api.CrimsonAPI;
import com.crimsonrpg.api.flag.FlagPtFunds;
import com.crimsonrpg.api.flag.FlagPtLevel;
import com.crimsonrpg.api.flag.FlagPtSize;
import com.crimsonrpg.api.plot.PlotUtils;

/**
 * Collects taxes. Yum.
 * 
 * Let me tell you how it will be; 
 * There's one for you, nineteen for me. 
 * 'Cause Iâm the taxman, 
 * Yeah, Iâm the taxman. 
 * 
 * NOTE: This has tons of comments because Dylan was stupid at the time :D
 */
public class Taxman implements Runnable {
	private CrimsonPlots plugin;

	public Taxman(CrimsonPlots instance) {
		this.plugin = instance;
	}

	public void run() {
		//Make sure our tax setting is set
		if (!plugin.getConfig().isLong("other.lastTax")) {
			plugin.getConfig().set("other.lastTax", System.currentTimeMillis());
		}

		//Get the last tax
		long lastTax = plugin.getConfig().getLong("other.lastTax");

		//Make sure we can tax
		if (!((lastTax + 86400000) < System.currentTimeMillis())) {
			return;
		}

		//Collect taxes
		for (Plot plot : CrimsonAPI.getPlotManager().getList()) {
			int tax = (int) //Integer
					(PlotUtils.getValue(plot) //The plot value
					* plot.getFlag(FlagPtLevel.class).getLevel().getTaxRate() //The tax rate in percent.
					* 0.01); //This is a percentage.

			//Check if we don't have enough money
			if (plot.getFlag(FlagPtFunds.class).getFunds() < tax) {

				//Shrink the plot
				FlagPtLevel levelFlag = plot.getFlag(FlagPtLevel.class);
				plot.getFlag(FlagPtSize.class).shrink(1);

				//Check for downgrading
				if (plot.getSize() < levelFlag.getLevel().getMinSize()) {

					//Downgrade the plot
					boolean downgrade = levelFlag.downgrade();
					if (downgrade) {
						PlotUtils.sendMemberMessage(plot, "Your plot " + plot.getName() + " has been shrunk and downgraded to a " + levelFlag.getLevel().getName() + " due to failure to pay taxes.");
						continue;
					}

					//Send the disband message
					PlotUtils.sendMemberMessage(plot, "Your plot " + plot.getName() + " has been disbanded due to a failure to pay taxes.");
					plot.disband();
					continue;
				}

				//Notify all plot members of their epic fail.
				//TODO: Possibly make a variable that notifies people when they first login that their plot failed to pay tax?
				PlotUtils.sendMemberMessage(plot, "Your plot " + plot.getName() + " has been shrunk due to failure to pay taxes.");

				continue;
			}

			//Collect the tax ('Cause I'm the taxman)
			plot.getFlag(FlagPtFunds.class).subtract(tax);

			//Notify of tax collection
			PlotUtils.sendMemberMessage(plot, "Tax has been collected on your plot " + plot.getName() + ".");
		}

		//Set the lastTax
		plugin.getConfig().set("other.lastTax", System.currentTimeMillis());
	}

}
