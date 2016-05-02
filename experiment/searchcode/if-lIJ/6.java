package monitor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 *
 * @author Krzysztof Kutt
 */
public class HTTPServer {
    static HashMap<Sensor, Monitor> register;  //który sensor jest przy którym monitorze
    static HashMap<Integer, Subscription> subscriptions;  //numer subskrypcji i jej dane
    static int subscriptionNumber;
    
    public static void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(Config.HTTP_SERVER_PORT), 0);
        
        HTTPServer.register = new HashMap<Sensor, Monitor>();
        HTTPServer.subscriptions = new HashMap<Integer, Subscription>();
        subscriptionNumber = 0;
        
        //lista obslugiwanych zasobów:
        server.createContext(Config.HTTP_SUBSCRIPTIONS_PATH, new SubscriptionsHandler());
        
        server.setExecutor(null); //default executor
        server.start();
    }
    
    static class SubscriptionsHandler implements HttpHandler {
        
        @Override
        public void handle(HttpExchange t) throws IOException {
            String method = t.getRequestMethod();
            String path = t.getRequestURI().getPath();
            
            /***** nowa subskrypcja *****/
            if( method.equalsIgnoreCase("POST") && path.equalsIgnoreCase(Config.HTTP_SUBSCRIPTIONS_PATH) ){
                
                //wyszukaj taki zasób w rejestrze:
                InputStream is = t.getRequestBody();
                Sensor sensor = XMLParser.getSensorFromXml(is);
                if( register.containsKey(sensor) ){
                    //monitor, w którym jest ta subskrypcja
                    Monitor monitor = register.get(sensor);
                    
                    //kolejny numer dla subskrypcji
                    Integer i = new Integer(subscriptionNumber);
                    subscriptionNumber++;
                    
                    subscriptions.put(i, new Subscription(sensor, monitor));
        
                    //wyślij odpowiedź do klienta
                    String response = "Location: http://127.0.0.1:8000" + Config.HTTP_SUBSCRIPTIONS_PATH + "/" + i;
                    //DO TESTOWANIA:
                    System.out.println(response);
                    
                    t.sendResponseHeaders(201, response.length());
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    
                } else {
                    //brak podanego zasobu
                    String response = "Sensor [ " + sensor.getResourceId() + ", " + sensor.getMetric() + " ] not exists";
                    //DO TESTOWANIA:
                    System.out.println(response);
                    
                    t.sendResponseHeaders(404, response.length());
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }

            
            /***** pobierz subskrypcję *****/
            } else if( method.equalsIgnoreCase("GET") && !path.replace(Config.HTTP_SUBSCRIPTIONS_PATH,"").isEmpty() ){
                int subscriptionId = Integer.parseInt(path.replace(Config.HTTP_SUBSCRIPTIONS_PATH+"/", ""));
                
                if( subscriptions.containsKey(new Integer(subscriptionId)) ){
                    Subscription sub = subscriptions.get(new Integer(subscriptionId));
                    
                    //wyślij odpowiedź do klienta
                    String response = XMLParser.createSubscriptionInfoXml(subscriptionId, "127.0.0.1", sub.getMonitor().getClientPortNumber());
                    //DO TESTOWANIA:
                    System.out.println(response);
                    
                    t.sendResponseHeaders(200, response.length());
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    
                } else {
                    //brak podanej subskrypcji
                    String response = "Subscription " + subscriptionId + " not exists";
                    //DO TESTOWANIA:
                    System.out.println(response);
                    
                    t.sendResponseHeaders(404, response.length());
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }

            
            /***** usuń subskrypcję *****/
            } else if( method.equalsIgnoreCase("DELETE") && !path.replace(Config.HTTP_SUBSCRIPTIONS_PATH,"").isEmpty() ){
                int subscriptionId = Integer.parseInt(path.replace(Config.HTTP_SUBSCRIPTIONS_PATH+"/", ""));
                
                if( subscriptions.containsKey(new Integer(subscriptionId)) ){
                
                    System.out.println("-- DEL: subId: " + subscriptionId);
                
                    Subscription sub = subscriptions.get(new Integer(subscriptionId));
                
                    //usuwanie subskrypcji z monitora
                    sub.getMonitor().removeSubscription(sub.getChannel());
                
                    System.out.println("DUPA4");
                    //usuwanie subskrypcji z serwera
                    subscriptions.remove(new Integer(subscriptionId));
                    
                    //odpowiedź nie jest wysyłana do klienta
                    System.out.println("INFO: Subskrypcja " + subscriptionId + " skasowana!");
                    
                } else {
                    //brak podanej subskrypcji
                    String response = "Subscription " + subscriptionId + " not exists";
                    //DO TESTOWANIA:
                    System.out.println(response);
                    
                    t.sendResponseHeaders(404, response.length());
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
                
                
            /***** wszystkie pozostałe zapytania *****/
            } else {
                //błędne zapytanie
                String response = "What are you doing?";
                //DO TESTOWANIA:
                System.out.println(response);
                
                t.sendResponseHeaders(400, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
                
            }
            
        }
        
    }

}

