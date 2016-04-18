package application;

public class LogOffHoursSalesProcessor extends AuctionProcessor {

	public LogOffHoursSalesProcessor(Auction auction) {
		super(auction);
	}

	public void process() {
		
		Hours offHours = OffHoursFactory.getInstance();
		
		if (offHours.isOffHours()) {
			AuctionLogger logger = AuctionLogger.getInstance();
			logger.log("/tmp/offHours", "Auction ".concat(auction.getItemName()).concat(" sold during off hours"));
		}
	}
}

