package it.imolinfo.jbi4corba.test.servant.testproviderfault;

import it.imolinfo.jbi4corba.test.testproviderfault.EchoComplexArrayException;
import it.imolinfo.jbi4corba.test.testproviderfault.EchoComplexException;
import it.imolinfo.jbi4corba.test.testproviderfault.EchoFaultPOA;
import it.imolinfo.jbi4corba.test.testproviderfault.EchoSimpleException;
import it.imolinfo.jbi4corba.test.testproviderfault.EchoStruct;
import it.imolinfo.jbi4corba.test.testproviderfault.EchoFault;
import it.imolinfo.jbi4corba.test.testproviderfault.EchoFaultHelper;

import java.io.InputStream;
import java.util.Properties;

import java.util.logging.Logger;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class EchoFaultImpl extends EchoFaultPOA {

    /**
     * logger.
     */
    private static Logger log = Logger.getLogger(EchoFaultImpl.class.getName());
    //LogFactory.getLog(EchoImpl.class);
    /**
     * main first argument is the port (host is supposed to be localhost) second argument is
     * daemon=true/false optional, default false.
     * If daemon is true the servan starts as daemon, useful for integration tests
     */
    public static void main(String[] args) throws Exception {
        String propertyFile = args[0];

        boolean daemon = args.length > 1 ? "daemon=true".equals(args[1]) : false;
        startCorbaServant(daemon, propertyFile);

    }

    private static void startOrbd(final String port) {
        Thread orbdThread = new Thread(new Runnable() {

            public void run() {
                log.info("starting orbd on port: " + port);
                com.sun.corba.se.impl.activation.ORBD.main(new String[]{"-ORBInitialPort", port, "-ORBInitialHost", "localhost"});
            }
        });
        orbdThread.setDaemon(true);
        orbdThread.start();
        log.info("orbd launched");
    }

    private static void startCorbaServant(final boolean daemon, final String orbPropertyFile) {
        Thread servantThread = new Thread(new Runnable() {

            public void run() {
                try {
                    // create and initialize the ORB                    
                    log.info("loading orb.properties: " + orbPropertyFile);

                    InputStream is = this.getClass().getResourceAsStream("/" + orbPropertyFile);
                    log.info("input stream: " + is);
                    Properties props = new Properties();
                    props.load(is);

                    log.info("launching orb with properties: " + props);

                    ORB orb = ORB.init((String[]) null, props);

                    // get reference to rootpoa & activate the POAManager
                    POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
                    rootpoa.the_POAManager().activate();

                    // create servant and register it with the ORB
                    EchoFaultImpl helloImpl = new EchoFaultImpl();
                    log.info("EchoFaultImpl ..." + helloImpl);

                    // get object reference from the servant
                    org.omg.CORBA.Object ref = rootpoa.servant_to_reference(helloImpl);
                    EchoFault href = EchoFaultHelper.narrow(ref);

                    if (daemon) {
                        startOrbd(props.getProperty("orbd.port"));
                        Thread.currentThread().sleep(2000);
                    }

                    // get the root naming context
                    // NameService invokes the name service
                    org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");

                    // Use NamingContextExt which is part of the Interoperable
                    // Naming Service (INS) specification.
                    NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

                    // bind the Object Reference in Naming
                    String name = "EchoFault";
                    NameComponent[] path = ncRef.to_name(name);
                    ncRef.rebind(path, href);
                    log.info("EchoImpl - echoref rebindato: " + ncRef);

                    log.info("EchoServer ready and waiting ...");

                    // wait for invocations from clients
                    orb.run();
                } catch (Exception e) {
                    log.severe("ERROR: " + e);
                    e.printStackTrace(System.out);
                }
                log.info("EchoServer Exiting ...");
            }
        });
        servantThread.setDaemon(daemon);
        servantThread.start();
    }
    
    // ==========================================
    //                  The operations in the IDL
    // ==========================================
    public String echo(String arg0) throws EchoSimpleException, EchoComplexException, EchoComplexArrayException {
        System.out.println("message received: " + arg0);
        if ("SIMPLE".equalsIgnoreCase(arg0)) {
            System.out.println("Throwing a EchoSimpleException");
            throw new EchoSimpleException(arg0);
        }
        if ("COMPLEX".equalsIgnoreCase(arg0)) {
            System.out.println("Throwing a EchoComplexException");
            EchoComplexException echoComplexException = new EchoComplexException();

            EchoStruct reason = new EchoStruct();
            reason.fieldBoolean = true;
            reason.fieldChar = 'a';
            reason.fieldLong = 2;
            reason.fieldString = "complexException";

            echoComplexException.reason = reason;

            // ex.reason
            throw echoComplexException;
        }
        if ("COMPLEX2".equalsIgnoreCase(arg0)) {
            System.out.println("Throwing a EchoComplexArrayException");
            EchoComplexArrayException echoComplexArrayException = new EchoComplexArrayException();

            EchoStruct reason1 = new EchoStruct();
            reason1.fieldBoolean = true;
            reason1.fieldChar = 'a';
            reason1.fieldLong = 2;
            reason1.fieldString = "complexException";
            
            EchoStruct reason2 = new EchoStruct();
            reason2.fieldBoolean = false;
            reason2.fieldChar = 'b';
            reason2.fieldLong = 3;
            reason2.fieldString = "complexException2"; 
            
            EchoStruct[] reasons = new EchoStruct[]{reason1, reason2};

            echoComplexArrayException.reasons = reasons;            

            // ex.reason
            throw echoComplexArrayException;
        }        
        if ("RUNTIME".equalsIgnoreCase(arg0)) {
            System.out.println("Throwing a NullPointerException");

            throw new NullPointerException();
        }
        return arg0;
    }
}


