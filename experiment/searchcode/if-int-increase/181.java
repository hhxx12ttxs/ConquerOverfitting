 import emarket.client.AbstractClientAgent;
 import emarket.client.IllegalCancelBidException;
 import emarket.client.IllegalClientBidException;
 import emarket.client.interfaces.IAsset;
 import emarket.client.interfaces.IBid;
 import emarket.client.interfaces.IMarketHistoryBid;
 import emarket.client.interfaces.IMarketRepresentation;
 import java.io.PrintStream;
 import java.util.ArrayList;
 
 public class agent11518709 extends AbstractClientAgent
 {
   private int vouchers = 0;
   private IMarketRepresentation[] mr;
   private IAsset[] assets;
   private boolean IHaveMyTravelTickets = false;
 
   private boolean IHaveMyRoomsBooked = false;
 
   public void runAgentTasks()
   {
		     IMarketRepresentation localIMarketRepresentation = null;
		 
		     this.mr = getNoticeBoardDetails();
		     this.assets = getAssets();
		 
		     String str1 = "flight to melbourne";
		 
		     String str2 = "room";
		
						//Code for buying flight ticket
		 
		     localIMarketRepresentation = getMarket(str1);
		 
		     //if get enough tickets then set the flag to true, and all the rest buying fly tickets code will not be run
		     for (int i = 0; i < this.assets.length; i++) {
		       if (this.assets[i].getGoodName().equals(str1))
		       {
							//System.out.println(this.assets[i].getDesiredMinimumQuantity());
		         if (this.assets[i].getQuantity() != this.assets[i].getDesiredMinimumQuantity()) break;
		         this.IHaveMyTravelTickets = true; break;
		       }
		 
		     }
		     
		     //if not enough tickets are gained, perform buying activity
		     if ((localIMarketRepresentation.isMarketOpen()) && (!this.IHaveMyTravelTickets))
		     {
		    	 //at the last minute to the end of the game, cancel all outstanding bids, and place bids of that number at prcie 600
		    	 // thus ensure I won't pay ticket price for more than 600, or 600 at the end.
					if (getCurrentTime() == 99L) {
				         IBid[] arrayOfIBid = getOutstandingBuyBids(localIMarketRepresentation.getMarketName());
				                  
				         for (int k = 0; k < arrayOfIBid.length; k++) 
								 {
				                   if (localIMarketRepresentation.getAction().equals("Submit and Cancel"))
				                	   try {
				                		   cancelBid(arrayOfIBid[k]);
				                	   }
				                   catch (IllegalCancelBidException illegalClientBidException) {
				                	   illegalClientBidException.printStackTrace();
				                   }
								 }
				         		try
				         		{
				         			submitBid(localIMarketRepresentation.getMarketName(), str1, 601L,arrayOfIBid.length );
				         		} catch (IllegalClientBidException illegalClientBidException) {
				         			illegalClientBidException.printStackTrace();
				         		}
						}
			       
					//every 10 time unit, check whether there is still outstanding bills, if yes, increase the bid price by $10
					else if ((getCurrentTime())%10 ==3) 
					{
				         IBid[] arrayOfIBid = getOutstandingBuyBids(localIMarketRepresentation.getMarketName());
						 long l = 0;
						 if(arrayOfIBid.length>0)
				         l = arrayOfIBid[0].getAmount() + 10L;
				         for (int k = 0; k < arrayOfIBid.length; k++) 
								 {
				           if (localIMarketRepresentation.getAction().equals("Submit and Cancel"))
				             try {
				               cancelBid(arrayOfIBid[k]);
				             }
				             catch (IllegalCancelBidException illegalClientBidException) {
				            	 illegalClientBidException.printStackTrace();
				             }
				         }
				         try
				         {
				           submitBid(localIMarketRepresentation.getMarketName(), str1, l,arrayOfIBid.length );
				         } catch (IllegalClientBidException illegalClientBidException) {
				        	 illegalClientBidException.printStackTrace();
				         }
			       }
					
					//at the starting of the game place 4 buy bids at price $250 and $200 each.
			       else if (getOutstandingBuyBids(localIMarketRepresentation.getMarketName()) == null)
			       {
			         try
			         {
			           submitBid(localIMarketRepresentation.getMarketName(), str1, 250L, 4);
								submitBid(localIMarketRepresentation.getMarketName(), str1, 200L, 4);
			         }
			         catch (IllegalClientBidException illegalClientBidException)
			         {
			        	 illegalClientBidException.printStackTrace();
			         }
			 
			       }
			 
		     }
		 
						//code for buying vouchers for hotel room
		     for (int j = 0; j < this.assets.length; j++) {
		    	 //check if i have got enough vouchers
		       if ((this.assets[j].getGoodName().equals(str2)) && (this.assets[j].getQuantity() >= this.assets[j].getDesiredMinimumQuantity())) {
		         this.IHaveMyRoomsBooked = true;
		       }
		       if (this.assets[j].getGoodName().equals(str2))
		       {
					//System.out.println(this.assets[j].getQuantity());
					this.vouchers = this.assets[j].getQuantity();
		       }
		     }
		    //run activities if not enough vouchers have been gained
		     if (!this.IHaveMyRoomsBooked)
		     {
		       IMarketRepresentation[] localmarket = getBedMarkets();
		 
		       for (int m = 0; m < localmarket.length; m++)
		       {
		    	   //get the hisory bis in this market
		         IMarketHistoryBid[] historybid = getMarketHistoryBidsForMarket(localmarket[m].getMarketName(), getCurrentTime() - 1L);
		         
							int bidnum = historybid.length;
							long lastbid = 30;
		         if (historybid.length != 0)
			
		         {
		        	 //get the last bid price in this sale round
		        	 lastbid = historybid[historybid.length-1].getAmountClearedFor();
		         }
		 
		         localIMarketRepresentation = localmarket[m];
		         
		         int num = localIMarketRepresentation.getQuantityGoodsOffered();
		         if(m<localmarket.length-2)
		         {
		         //at the time 3 units prior to the closing time of the market place buy bids
				 if ((localIMarketRepresentation.isMarketOpen()) && (getCurrentTime() == localIMarketRepresentation.getClosingTime() - 3L)) {
			           
			           try
			           {
			        	   //place buy bids with price $10 higer than the last bid in this market, check the number to ensure no over bids
			             submitBid(localIMarketRepresentation.getMarketName(), str2, lastbid+10, num < 24 - this.vouchers ? num : 24 - this.vouchers);
			           }
			           catch (IllegalClientBidException illegalClientBidException) {
			        	   illegalClientBidException.printStackTrace();
			           }
			 
			         }
		 
		        }
		         else
		         {
		        	 if ((localIMarketRepresentation.isMarketOpen()) && (getCurrentTime() == localIMarketRepresentation.getClosingTime() - 2L)) {
				           
				           try
				           {
				        	   //place buy bids with price $10 higer than the last bid in this market, check the number to ensure no over bids
				             submitBid(localIMarketRepresentation.getMarketName(), str2, lastbid+10, num < 24 - this.vouchers ? num : 24 - this.vouchers);
				           }
				           catch (IllegalClientBidException illegalClientBidException) {
				        	   illegalClientBidException.printStackTrace();
				           }
				 
				         }
					 
					// at the last time unit, check whether the number of bids placed in this market is larger than the number of vouchers on sale 
			         if ((localIMarketRepresentation.isMarketOpen()) && (getCurrentTime() == localIMarketRepresentation.getClosingTime() - 1L)&&bidnum>num) {
			         
			           try
			           {
			        	  //if more than the number of vouchers is placed, than replace bids to ensure I get enough tickets
			             submitBid(localIMarketRepresentation.getMarketName(), str2, 105L, num < 24 - this.vouchers ? num : 24 - this.vouchers);
									submitBid(localIMarketRepresentation.getMarketName(), str2, 105L, num -bidnum);
			           }
			           catch (IllegalClientBidException illegalClientBidException) {
			        	   illegalClientBidException.printStackTrace();
			           }
			 
			         }
		         }
		       }
		 
		     }
		
		 
		     
		     		//Buy entertainment tickets
		     for (int m = 0; m < this.assets.length; m++)
		     {
		       if ((this.assets[m].getGoodName().startsWith("ticket")) && (this.assets[m].getDesiredMinimumQuantity() > 0) && (this.assets[m].getQuantity() < this.assets[m].getDesiredMinimumQuantity()))
		       {
		         String ticketname = this.assets[m].getGoodName();
		 
		         localIMarketRepresentation = getMarket(ticketname);
		 
		         IBid[] bids = getAllSellBidsForMarket(localIMarketRepresentation.getMarketName());
		 //find the cheapest buy bid for the ticket I want to buy 
		         if ((bids != null) && (bids.length > 0))
		         {
		           IBid buybid = null;
		           for (int i1 = 0; i1 < bids.length; i1++) {
		             IBid currentbid = bids[i1];
		             if (currentbid.getGoodName().equals(ticketname)) {
		               if (buybid == null)
		            	   buybid = currentbid;
		               else if (buybid.getAmount() > currentbid.getAmount()) {
		            	   buybid = currentbid;
		               }
		 
		             }
		 
		           }
		 //check whether the cheapest bid's price is lower than $100, in case some agent play a trick, that is submit sell bids at more than $100
		           if ((buybid != null) && (buybid.getAmount() < 100L)) {
		             try {
		               submitBid(localIMarketRepresentation.getMarketName(), ticketname, buybid.getAmount(), this.assets[m].getDesiredMinimumQuantity());
		             } catch (IllegalClientBidException illegalClientBidException) {
		               System.out.println("TestClient1: submit failed: reason=" + illegalClientBidException.getReason());
		             }
		           }
		         }
		 
		       }
		 
		     }
		 
					//Sell entertainment tickets
		     for (int m = 0; m < this.assets.length; m++)
		       if ((this.assets[m].getGoodName().startsWith("ticket")) && (this.assets[m].getDesiredMinimumQuantity() == 0) && (this.assets[m].getQuantity() > 0)) {
		         String goodname = this.assets[m].getGoodName();
		         localIMarketRepresentation = getMarket(goodname);
		 
		        IBid[] bids = getOutstandingSellBids(localIMarketRepresentation.getMarketName(), goodname);
		 //at time 90 submit sell bid at price $90(reduce sell price, so that will be easy to find a buyer)
		         if ((getCurrentTime() == 90L) && (localIMarketRepresentation.isMarketOpen()) && (bids != null))
			           try {
				             submitBid(localIMarketRepresentation.getMarketName(), goodname, -90L, this.assets[m].getQuantity());
			           }
		           catch (IllegalClientBidException illegalClientBidException) {
			             System.out.println("TestClient1: submit2 failed: reason=" + illegalClientBidException.getReason());
		           }
		         //at the beginning submit sell bids at price $99, to match price as higher as possible
		         else if ((bids == null) && (localIMarketRepresentation.isMarketOpen()))
		         {
		           try
		           {
		             submitBid(localIMarketRepresentation.getMarketName(), goodname, -99L, this.assets[m].getQuantity());
		           }
		           catch (IllegalClientBidException illegalClientBidException) {
		             System.out.println("TestClient1: submit2 failed: reason=" + illegalClientBidException.getReason());
		           }
		         }
		 
		       }
		   }
		 
		   private IMarketRepresentation[] getBedMarkets()
		   {
		     ArrayList localArrayList = new ArrayList(10);
		 
		     String str = "room";
		 
		     for (int i = 0; i < this.mr.length; i++) {
		       String[] arrayOfString = this.mr[i].getGoodsAllowedByName();
		       for (int j = 0; j < arrayOfString.length; j++) {
		         if (arrayOfString[j].equals(str)) {
		           localArrayList.add(this.mr[i]);
		         }
		       }
		     }
		 
		     return (IMarketRepresentation[])localArrayList.toArray(new IMarketRepresentation[0]);
		   }
		 
		   private IMarketRepresentation getMarket(String paramString)
		   {
		     IMarketRepresentation localIMarketRepresentation = null;
		 
		     for (int i = 0; i < this.mr.length; 
		       i++) {
		       String[] arrayOfString = this.mr[i].getGoodsAllowedByName();
		       for (int j = 0; j < arrayOfString.length; 
		         j++) {
		         if (arrayOfString[j].equals(paramString)) {
		           localIMarketRepresentation = this.mr[i];
		           break;
		         }
		 
		       }
		 
		       if (localIMarketRepresentation != null)
		       {
		         break;
		       }
		     }
		     return localIMarketRepresentation;
		   }
		 }


