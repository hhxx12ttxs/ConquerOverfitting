package klient;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 *
 * @author Krzysztof Kutt
 */
public class SubscriptionsHandler {
    public static void newSubscription(List<Subscription> subscriptions){
        String xml = "";
        String resourceId = "";
        String metric = "";
        try {
            System.out.println("--- newSubscription ---");

            System.out.print("Podaj resourceId: ");
            resourceId = IO.readString();
            System.out.print("Podaj metric:     ");
            metric = IO.readString();

            //stwórz XMLa
            xml = XMLParser.createNewSubscriptionXML(resourceId, metric);
        } catch (IOException ex) {
            System.out.println("Zle wpisane wartosci!");
        }
        try {
            //wyślij żądanie do serwera
            URL url = new URL(Config.HTTPAddress);
            URLConnection connection = url.openConnection();
            ((HttpURLConnection)connection).setRequestMethod("POST");
            connection.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write(xml);
            out.close();

            int responseCode = ((HttpURLConnection) connection).getResponseCode();
            if (responseCode == 201) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                        connection.getInputStream()));
                String response = in.readLine();
                in.close();

                if (response.startsWith("Location")) {
                    //stwórz nową subskrypcję, nadaj jej resourceId, metric i location i wrzuć do listy
                    Subscription sub = new Subscription();
                    sub.setResourceId(resourceId);
                    sub.setMetric(metric);
                    sub.setLocation(response.replaceFirst("Location: ", ""));
                    subscriptions.add(sub);
                    System.out.println("Dodano subskrypcje!");
                } else {
                    //nie przypisuj nic (i wyświetl komunikat o niepowodzeniu)
                    System.out.println("Blad! Odpowiedz serwera: " + response);
                }
            } else {
                System.out.println("Blad! Serwer zwrócił kod: " + responseCode);
            }

        } catch (MalformedURLException ex) {
            System.out.println("Zly adres!");
        } catch (IOException ex) {
            System.out.println("Problemy z polaczeniem!");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    public static void getSubscription(List<Subscription> subscriptions) {
        System.out.println("--- getSubscription ---");
        int subIndex = selectSubscription(subscriptions);

        Subscription sub = subscriptions.get(subIndex);

        //jeżeli ta subskrypcja ma już host i port, nie rób nic, tylko wypisz komunikat
        if (!(sub.getHost().equals("") || sub.getPort().equals(""))) {
            System.out.println("Ta subskrypcja jest juz pobrana!");
        } else {
            try {
                //wyślij żądanie do serwera
                URL url = new URL(sub.getLocation());
                System.out.println(sub.getLocation());
                URLConnection connection = url.openConnection();
                ((HttpURLConnection) connection).setRequestMethod("GET");

                int responseCode = ((HttpURLConnection) connection).getResponseCode();
                if (responseCode == 200) {

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(
                            connection.getInputStream()));
                    String response = in.readLine();
                    in.close();

                    if (response.startsWith("<subscription id")) {
                        String host = XMLParser.getValueFromXml(new ByteArrayInputStream(response.getBytes()), "host");
                        String port = XMLParser.getValueFromXml(new ByteArrayInputStream(response.getBytes()), "port");

                        sub.setHost(host);
                        sub.setPort(port);

                        System.out.println("Pobrano subskrypcje!");
                    } else {
                        //nie przypisuj nic (i wyświetl komunikat o niepowodzeniu)
                        System.out.println("Blad! Odpowiedz serwera: " + response);
                    }
                } else {
                    System.out.println("Blad! Serwer zwrócił kod: " + responseCode);
                }

            } catch (MalformedURLException ex) {
                System.out.println("Zly adres!");
            } catch (IOException ex) {
                System.out.println("Problemy z polaczeniem!");
                System.out.println(ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
  
    public static void connectHost(List<Subscription> subscriptions){
        System.out.println("--- connectHost ---");
        int subIndex = selectSubscription(subscriptions);
        
        Subscription sub = subscriptions.get(subIndex);
        
        if( sub.getHost().equals("") || sub.getPort().equals("") ){
            System.out.println("Najpierw pobierz subskrypcje, aby miec host i port!");
        } else {
            
            //w osobnym wątku zostanie nawiązane połączenie i będą wyświetlane napływające dane
            //aż do zakończenia subskrypcji
            Runnable sensorReader = new SensorReader(sub.getHost(), sub.getPort(), sub.getResourceId(),
                    sub.getMetric(), sub.getLocation().substring( sub.getLocation().lastIndexOf("/")+1 ) );
            //Thread thread = new Thread(sensorReader);
            //thread.start();
            System.out.println("-- Mozesz zakonczyc polaczenie wpisujac: 'end'");
            sensorReader.run();
            
            //WARN: program będzie działał tak, że będzie wyświetlał te dane na tym samym okienku konsoli
            //na którym będzie menu - jest to trochę mało przyjemne (musisz np wybrać opcję w menu,
            //a tu Ci napływają nowe pomiary), ale tak najprościej to zrobić wg mnie
        }
    }
    
    public static void closeSubscription(List<Subscription> subscriptions) {
        System.out.println("--- closeSubscription ---");
        int subIndex = selectSubscription(subscriptions);

        Subscription sub = subscriptions.get(subIndex);

        try {
            //wyślij żądanie do serwera
            URL url = new URL(sub.getLocation());
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            ((HttpURLConnection) connection).setRequestMethod("DELETE");
            connection.connect();
            System.out.println("DUPA3");

            int responseCode = ((HttpURLConnection) connection).getResponseCode();
            //System.out.println("Serwer zwrócił kod: " + responseCode);
            
        } catch (MalformedURLException ex) {
            System.out.println("Zly adres!");
        } catch (IOException ex) {
            System.out.println("Problemy z polaczeniem!");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }

        //skasuj subskrypcję z listy subskrypcji
        subscriptions.remove(subIndex);
    }
    
    private static int selectSubscription(List<Subscription> subscriptions){
        int subIndex = 0;  //w przypadku wpisania liczby wykraczającej poza zakres
                           //albo w przypadku wpisania czegoś co nie jest liczbą
                           //zwróci indeks pierwszej subskrypcji
        
        try {
            System.out.println("Ktora subskrypcje wybierasz (podaj indeks)?");
            listSubscriptions(subscriptions);
            
            //wczytaj liczbę wpisaną przez użytkownika
            int opt = IO.readInteger();
            
            if( opt >= 0 && opt < subscriptions.size())
                subIndex = opt;
            
        } catch (IOException ex) {
            //nie rób nic; zwróci indeks 0
        }
        
        return subIndex;
    }

    public static void listSubscriptions(List<Subscription> subscriptions) {
        System.out.println("***** SUBSKRYPCJE *****");
        for (int i = 0; i < subscriptions.size(); i++) {
            Subscription sub = subscriptions.get(i);
            System.out.println(i + " -> [" + sub.getResourceId() + ":" + sub.getMetric() + "], " + sub.getLocation()
                    + ", TCP: " + sub.getHost() + ":" + sub.getPort());
        }
    }
}

