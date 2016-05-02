<<<<<<< HEAD
package javapns.test;

import java.util.*;

import javapns.*;
import javapns.communication.exceptions.*;
import javapns.devices.*;
import javapns.devices.implementations.basic.*;
import javapns.json.*;
import javapns.notification.*;
import javapns.notification.transmission.*;


/**
 * A command-line test facility for the Push Notification Service.
 * <p>Example:  <code>java -cp "[required libraries]" javapns.test.NotificationTest keystore.p12 mypass 2ed202ac08ea9033665e853a3dc8bc4c5e78f7a6cf8d55910df230567037dcc4</code></p>
 * 
 * <p>By default, this test uses the sandbox service.  To switch, add "production" as a fourth parameter:</p>
 * <p>Example:  <code>java -cp "[required libraries]" javapns.test.NotificationTest keystore.p12 mypass 2ed202ac08ea9033665e853a3dc8bc4c5e78f7a6cf8d55910df230567037dcc4 production</code></p>
 * 
 * <p>Also by default, this test pushes a simple alert.  To send a complex payload, add "complex" as a fifth parameter:</p>
 * <p>Example:  <code>java -cp "[required libraries]" javapns.test.NotificationTest keystore.p12 mypass 2ed202ac08ea9033665e853a3dc8bc4c5e78f7a6cf8d55910df230567037dcc4 production complex</code></p>
 * 
 * <p>To send a simple payload to a large number of fake devices, add "threads" as a fifth parameter, the number of fake devices to construct, and the number of threads to use:</p>
 * <p>Example:  <code>java -cp "[required libraries]" javapns.test.NotificationTest keystore.p12 mypass 2ed202ac08ea9033665e853a3dc8bc4c5e78f7a6cf8d55910df230567037dcc4 sandbox threads 1000 5</code></p>
 * 
 * @author Sylvain Pedneault
 */
public class NotificationTest extends TestFoundation {

	/**
	 * Execute this class from the command line to run tests.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		/* Verify that the test is being invoked  */
		if (!verifyCorrectUsage(NotificationTest.class, args, "keystore-path", "keystore-password", "device-token", "[production|sandbox]", "[complex|simple|threads]", "[#devices]", "[#threads]")) return;

		/* Initialize Log4j to print logs to console */
		configureBasicLogging();

		/* Push an alert */
		try {
			pushTest(args);
		} catch (CommunicationException e) {
			e.printStackTrace();
		} catch (KeystoreException e) {
			e.printStackTrace();
		}
	}


	private NotificationTest() {
	}


	/**
	 * Push a test notification to a device, given command-line parameters.
	 * 
	 * @param args
	 * @throws KeystoreException 
	 * @throws CommunicationException 
	 */
	private static void pushTest(String[] args) throws CommunicationException, KeystoreException {
		String keystore = args[0];
		String password = args[1];
		String token = args[2];
		boolean production = args.length >= 4 ? args[3].equalsIgnoreCase("production") : false;
		boolean simulation = args.length >= 4 ? args[3].equalsIgnoreCase("simulation") : false;
		boolean complex = args.length >= 5 ? args[4].equalsIgnoreCase("complex") : false;
		boolean threads = args.length >= 5 ? args[4].equalsIgnoreCase("threads") : false;
		int threadDevices = args.length >= 6 ? Integer.parseInt(args[5]) : 100;
		int threadThreads = args.length >= 7 ? Integer.parseInt(args[6]) : 10;
		boolean simple = !complex && !threads;

		verifyKeystore(keystore, password, production);

		if (simple) {

			/* Push a test alert */
			List<PushedNotification> notifications = Push.test(keystore, password, production, token);
			printPushedNotifications(notifications);

		} else if (complex) {

			/* Push a more complex payload */
			List<PushedNotification> notifications = Push.payload(createComplexPayload(), keystore, password, production, token);
			printPushedNotifications(notifications);

		} else if (threads) {

			/* Push a Hello World! alert repetitively using NotificationThreads */
			pushSimplePayloadUsingThreads(keystore, password, production, token, simulation, threadDevices, threadThreads);

		}
	}


	/**
	 * Create a complex payload for test purposes.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Payload createComplexPayload() {
		PushNotificationPayload complexPayload = PushNotificationPayload.complex();
		try {
			// You can use addBody to add simple message, but we'll use
			// a more complex alert message so let's comment it
			complexPayload.addCustomAlertBody("My alert message");
			complexPayload.addCustomAlertActionLocKey("Open App");
			complexPayload.addCustomAlertLocKey("javapns rocks %@ %@%@");
			ArrayList parameters = new ArrayList();
			parameters.add("Test1");
			parameters.add("Test");
			parameters.add(2);
			complexPayload.addCustomAlertLocArgs(parameters);
			complexPayload.addBadge(45);
			complexPayload.addSound("default");
			complexPayload.addCustomDictionary("acme", "foo");
			complexPayload.addCustomDictionary("acme2", 42);
			ArrayList values = new ArrayList();
			values.add("value1");
			values.add(2);
			complexPayload.addCustomDictionary("acme3", values);
		} catch (JSONException e) {
			System.out.println("Error creating complex payload:");
			e.printStackTrace();
		}
		return complexPayload;
	}


	protected static void pushSimplePayloadUsingThreads(String keystore, String password, boolean production, String token, boolean simulation, int devices, int threads) {
		try {

			System.out.println("Creating PushNotificationManager and AppleNotificationServer");
			AppleNotificationServer server = new AppleNotificationServerBasicImpl(keystore, password, production);
			System.out.println("Creating payload (simulation mode)");
			//			Payload payload = PushNotificationPayload.alert("Hello World!");
			Payload payload = PushNotificationPayload.test();

			System.out.println("Generating " + devices + " fake devices");
			List<Device> deviceList = new ArrayList<Device>(devices);
			for (int i = 0; i < devices; i++) {
				String tokenToUse = token;
				if (tokenToUse == null || tokenToUse.length() != 64) {
					tokenToUse = "123456789012345678901234567890123456789012345678901234567" + (1000000 + i);
				}
				deviceList.add(new BasicDevice(tokenToUse));
			}

			System.out.println("Creating " + threads + " notification threads");
			NotificationThreads work = new NotificationThreads(server, simulation ? payload.asSimulationOnly() : payload, deviceList, threads);
			//work.setMaxNotificationsPerConnection(10000);
			System.out.println("Linking notification work debugging listener");
			work.setListener(DEBUGGING_PROGRESS_LISTENER);

			System.out.println("Starting all threads...");
			long timestamp1 = System.currentTimeMillis();
			work.start();
			System.out.println("All threads started, waiting for them...");
			work.waitForAllThreads();
			long timestamp2 = System.currentTimeMillis();
			System.out.println("All threads finished in " + (timestamp2 - timestamp1) + " milliseconds");

			printPushedNotifications(work.getPushedNotifications(true));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * A NotificationProgressListener you can use to debug NotificationThreads.
	 */
	public static final NotificationProgressListener DEBUGGING_PROGRESS_LISTENER = new NotificationProgressListener() {

		public void eventThreadStarted(NotificationThread notificationThread) {
			System.out.println("   [EVENT]: thread #" + notificationThread.getThreadNumber() + " started with " + notificationThread.getDevices().size() + " devices beginning at message id #" + notificationThread.getFirstMessageIdentifier());
		}


		public void eventThreadFinished(NotificationThread thread) {
			System.out.println("   [EVENT]: thread #" + thread.getThreadNumber() + " finished: pushed messages #" + thread.getFirstMessageIdentifier() + " to " + thread.getLastMessageIdentifier() + " toward " + thread.getDevices().size() + " devices");
		}


		public void eventConnectionRestarted(NotificationThread thread) {
			System.out.println("   [EVENT]: connection restarted in thread #" + thread.getThreadNumber() + " because it reached " + thread.getMaxNotificationsPerConnection() + " notifications per connection");
		}


		public void eventAllThreadsStarted(NotificationThreads notificationThreads) {
			System.out.println("   [EVENT]: all threads started: " + notificationThreads.getThreads().size());
		}


		public void eventAllThreadsFinished(NotificationThreads notificationThreads) {
			System.out.println("   [EVENT]: all threads finished: " + notificationThreads.getThreads().size());
		}


		public void eventCriticalException(NotificationThread notificationThread, Exception exception) {
			System.out.println("   [EVENT]: critical exception occurred: " + exception);
		}
	};


	/**
	 * Print to the console a comprehensive report of all pushed notifications and results.
	 * @param notifications a raw list of pushed notifications
	 */
	public static void printPushedNotifications(List<PushedNotification> notifications) {
		List<PushedNotification> failedNotifications = PushedNotification.findFailedNotifications(notifications);
		List<PushedNotification> successfulNotifications = PushedNotification.findSuccessfulNotifications(notifications);
		int failed = failedNotifications.size();
		int successful = successfulNotifications.size();

		if (successful > 0 && failed == 0) {
			printPushedNotifications("All notifications pushed successfully (" + successfulNotifications.size() + "):", successfulNotifications);
		} else if (successful == 0 && failed > 0) {
			printPushedNotifications("All notifications failed (" + failedNotifications.size() + "):", failedNotifications);
		} else if (successful == 0 && failed == 0) {
			System.out.println("No notifications could be sent, probably because of a critical error");
		} else {
			printPushedNotifications("Some notifications failed (" + failedNotifications.size() + "):", failedNotifications);
			printPushedNotifications("Others succeeded (" + successfulNotifications.size() + "):", successfulNotifications);
		}
	}


	/**
	 * Print to the console a list of pushed notifications.
	 * @param description a title for this list of notifications
	 * @param notifications a list of pushed notifications to print
	 */
	public static void printPushedNotifications(String description, List<PushedNotification> notifications) {
		System.out.println(description);
		for (PushedNotification notification : notifications) {
			try {
				System.out.println("  " + notification.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
=======
package Task003;

/**
 * Created by Kamil on 25.02.16.
 */
public class ComplexNumber {

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    private double x;
    private double y;

    public ComplexNumber add(ComplexNumber complexNumber) {
        ComplexNumber add = new ComplexNumber();
        add.x = this.x + complexNumber.getX();
        add.y = this.y + complexNumber.getY();
        return add;
    }

    public void add2(ComplexNumber complexNumber) {
        this.x += complexNumber.getX();
        this.y += complexNumber.getY();
    }

    public ComplexNumber sub(ComplexNumber complexNumber) {
        ComplexNumber sub = new ComplexNumber();
        sub.x = this.x - complexNumber.getX();
        sub.y = this.y - complexNumber.getY();
        return sub;
    }

    public void sub2(ComplexNumber complexNumber) {
        this.x -= complexNumber.getX();
        this.y -= complexNumber.getY();
    }

    public ComplexNumber multNumber(double i) {
        ComplexNumber multNumber = new ComplexNumber(this.x * i, this.y * i);
        return multNumber;
    }

    public void multNumber2(double i) {
        this.x *= i;
        this.y *= i;
    }

    public ComplexNumber mult(ComplexNumber complexNumber) {
        ComplexNumber mult = new ComplexNumber();
        mult.x = this.x * complexNumber.getX() - this.y * complexNumber.getY();
        mult.y = this.x * complexNumber.getY() + this.y * complexNumber.getX();
        return mult;
    }

    public void mult2(ComplexNumber complexNumber) {
        this.x = this.x * complexNumber.getX() - this.y * complexNumber.getY();
        this.y = this.x * complexNumber.getY() + this.y * complexNumber.getX();
    }

    public ComplexNumber() {
        this(0, 0);
    }

    public ComplexNumber div(ComplexNumber complexNumber) {
        double a = complexNumber.getX() * complexNumber.getX() + complexNumber.getY() * complexNumber.getY();
        ComplexNumber div = new ComplexNumber();
        div.x = 1.0 * (this.x * complexNumber.getX() + this.y * complexNumber.getY()) / a;
        div.y = 1.0 * (-this.x * complexNumber.getY() + this.y * complexNumber.getX()) / a;
        return div;

    }

    public void div2(ComplexNumber complexNumber) {
        double a = complexNumber.getX() * complexNumber.getX() + complexNumber.getY() * complexNumber.getY();
        this.x = 1.0 * (this.x * complexNumber.getX() + this.y * complexNumber.getY()) / a;
        this.y = 1.0 * (-this.x * complexNumber.getY() + this.y * complexNumber.getX()) / a;

    }

    public double lenght() {
        double d = Math.sqrt(this.x * this.x + this.y * this.y);
        return d;
    }

    public String toString() {
        if (this.y >= 0) {
            return (this.x + " + " + this.y + "i");
        } else {
            return (this.x + " - " + (-1) * this.y + "i");
        }
    }

    public double arg() {
        double arg = Math.atan(1.0 * this.y / this.x);
        return arg;
    }

    public ComplexNumber pow(double pow) {
        ComplexNumber poow = new ComplexNumber();
        poow.x = Math.pow(this.x, pow) * Math.cos(pow * this.arg());
        poow.y = Math.pow(this.x, pow) * Math.sin(pow * this.arg());
        return poow;
    }

    public boolean equals(ComplexNumber complexNumber) {
        return (this.x == complexNumber.getX() && this.y == complexNumber.getY());
    }

    public ComplexNumber(double a, double b) {
        this.x = a;
        this.y = b;
    }
>>>>>>> 76aa07461566a5976980e6696204781271955163

}

