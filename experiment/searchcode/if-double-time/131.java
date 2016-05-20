package cheapcoffee;
import java.util.Iterator;
import java.util.Random;

public class CheapCoffee
{
	final double ARRIVALRATE = 0.2;
	final double SERVICERATE = 0.25;
	
	private double time = 0;
	private double breakTime = 0;
	private double serviceTime = 0;
	
	private int rejectedCustomers = 0;
	private int totalCustomers = 0;
	private double totalQueueTimeSinceStart = 0;
	private double percentRejected = 0;
	private double averageQueueTime = 0;
	
	private FutureEventList fel = new FutureEventList();
	private Iterator<Event> felIt = fel.listIterator();
	
	private Queue queue = new Queue(Main.maxQueueSize);
	private Customer customer;
	
	private Arrival arrival;
	private Departure departure;
	private Service service = new Service();
	private Event currentEvent;

	private Random rnd = new Random();

	public CheapCoffee(long seed, int simulationLength) {
//		System.out.println("");
		this.breakTime = simulationLength;
		rnd.setSeed(seed);
		run();
	}

	private void run() {
		time = 0;
		arrival = new Arrival(calculateInterArrivalTime()); // räkna ut när första kommer
		fel.insertSorted(arrival); // sätt in första eventet i future event list
		do{
			currentEvent = fel.getFirst();
			time = currentEvent.getTime();
			
			eventHandler(currentEvent); // behandla nästkommande event

			fel.removeFirst(); // ta fram nästa
			if((time > breakTime) && (fel.size() <= 0)) // kolla om vi ska bryta
				break; 
		}while (felIt.hasNext()); // kör till tom
	}

	private void eventHandler(Event ev){
		// DET KOMMER EN NY KUND
		if(ev instanceof Arrival){
//			System.out.println("##################### Arrival ###################");
//			System.out.println("");
			
			// SKAPA KUNDEN
			totalCustomers += 1;
			customer = new Customer(totalCustomers,time);
//			System.out.println("  Customer " + customer.getCustomerId() + " arrives at time: " + customer.getArrivalTime());
			
			// OM DET ÄR LEDIG i KASSAN
			if(!service.isBusy()){
//				System.out.println("  Place Customer " + customer.getCustomerId() + " in service");
				
				service.setCurrentCustomer(customer); // Kunden befinner sig i kassan
				service.setBusy(); // Kassan blir upptagen
				
				serviceTime = calculateServiceTime(); // Beräkna hur lång tid kunden kommer vara i kassan
				customer.setServiceTime(serviceTime); // Kunden får en servicetid
				departure = new Departure(time + serviceTime); // Schemalägg när kunden är klar
				customer.setDepartureTime(departure.getTime()); // Kunden får en tid då den är färdig
				
//				System.out.println("  Customer " + customer.getCustomerId() + " will depart at: " + departure.getTime());
				
				fel.insertSorted(departure); // Lägg in event för när kunden ska gå
			
			// OM DET STÅR FOLK I KÖN
			} else {
//				System.out.println("  Customer "+ service.getCurrentCustomer().getCustomerId() +" is now beeing served.");
				
				// OM DET FINNS PLATS I KÖN
				if(!queue.isQueueFull()){
					queue.addLast(customer); // Ställ kunden sist i kön
					customer.setStartQueueTime(time); // Börja räkna på hur länge kunden kommer stå i kön
//					System.out.println("  Place customer in queue");
				
				// OM KÖN ÄR FULL
				} else {
					rejectedCustomers += 1; // Räkna ivägskickade kunder
				}
			}
			
			// BERÄKNA NÄR NÄSTA KUND KOMMER
			arrival = new Arrival(time + calculateInterArrivalTime());
			if(breakTime >= arrival.getTime()) // Om uträknad tid överskrider stängningstid, neka kunden inträde
				fel.insertSorted(arrival);
		
		// KUNDEN ÄR KLAR OCH LÄMNAR
		} else if(ev instanceof Departure){
//			System.out.println("#################### Departure ##################");
//			System.out.println("");
			
//			System.out.println("  Customer " + service.getCurrentCustomer().getCustomerId() + " departs at time: " + service.getCurrentCustomer().getDepartureTime());
			
			// OM KÖN ÄR TOM
			if(queue.isQueueEmpty()){
				service.setIdle(); // Sätt kassan ledig
//				System.out.println("  Set idle");
			
			// OM DET FINNS FOLK I KÖN
			} else {
				customer = queue.takeFirst(); // Tag första kunden i kön
				customer.setEndQueueTime(time); // Avsluta kundens kötid
//				System.out.println("  Take next customer in queue");
				
//				System.out.println("  Customer " + customer.getCustomerId() + " queuing time: " + customer.getQueueTime());
				totalQueueTimeSinceStart += customer.getQueueTime(); // Lägg till kundens kötid till totalen
				
				service.setCurrentCustomer(customer); // Uppdatera kunden i kassan
//				System.out.println("  Customer "+ service.getCurrentCustomer().getCustomerId() +" is now beeing served.");
				
				serviceTime = calculateServiceTime(); // Beräkna hur länge kunden befinner sig i kassan
				customer.setServiceTime(serviceTime); // Kunden får en servicetid
				
				departure = new Departure(time + serviceTime); // Schemalägg när kunden är klar
				customer.setDepartureTime(departure.getTime()); // Kunden får en tid då den är färdig
				
//				System.out.println("  Customer " + customer.getCustomerId() + " will depart at: " + departure.getTime());
				
				fel.insertSorted(departure);
				
			}
			
		}
		percentRejected = (double) rejectedCustomers/totalCustomers;
		averageQueueTime =  (double) totalQueueTimeSinceStart/totalCustomers;
		
//		System.out.println("  Rejected customers: " + rejectedCustomers);
//		System.out.println("  Total customers: " + totalCustomers);
//		System.out.println("  Total customers in queue: " + totalCustomersInQueueSinceStart);
//		System.out.println("  Total queue time: " + totalQueueTimeSinceStart);
//		System.out.println("  Average queue time: " + averageQueueTime);
//		System.out.println("  Percent rejected: " + String.format("%.5g", 100*percentRejected) + "%");
//		System.out.println("  " + queue.toString(service.getCurrentCustomer()));
//		System.out.println("");
	}

	public double calculateInterArrivalTime() {
		double rndSeed = rnd.nextDouble();
		double iat = -Math.log(rndSeed)/ARRIVALRATE;
		return iat;
	}
	
	public double calculateServiceTime() {
		double rndSeed = rnd.nextDouble();
		double ist = -Math.log(rndSeed)/SERVICERATE;
		return ist;
	}
	
	public int getTotalCustomers() {
		return totalCustomers;
	}

	public double getPercentRejected() {
		return percentRejected;
	}

	public double getAverageQueueTime() {
		return averageQueueTime;
	}
}

