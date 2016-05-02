/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Server;

/**
 *
 * @author hung
 */

import java.rmi.*;
import java.rmi.server.*;
import Biometric.Image;
import java.rmi.registry.LocateRegistry;


public class Server extends UnicastRemoteObject implements Validation{
    Image img;

    public Server() throws RemoteException {
        super();
        // Create an Image Object that Performs the Eye image processing.
        img = new Image();
        img.init();
        img.loadDatabase();

        System.out.println("There are " + img.getNumEyes() + " eyes in database.");
    }

    @Override
    public String Validate(int[] eyeSignature) throws RemoteException{
        System.out.println("Validating eye...");
        // Create a scoring array for the comparisons.
        double[] score = new double[img.getNumEyes()];
        // Perform scoring use "fuzzy" Hamming distance.
        score = img.Score(eyeSignature);
        // Report scores and find the greatest.
        int whichOne = -1; double greatest = 0.0;
        for (int i = 0; i < img.getNumEyes(); i++) {
            System.out.println("   score[" + i +"] = " + score[i]);
            if (score[i] > greatest) {
                greatest = score[i];
                whichOne = i;
            }
        }
        System.out.println("eye[" + whichOne + "] was the best match.");

        // If the greatest value is more than 95% confidence, then admit.
        if (greatest >= 0.95) {
            System.out.println("Match is >= 95% confidence - admitted.");

            return "Hello User " + (whichOne + 1);
            //return true;
        } else {
            System.out.println("Match is < 95% confidence - denied.");
            //return false;
        }
        return "Access Denied";
    }

    public static void main(String[] args) {
       
        try {
            Server server = new Server();
            LocateRegistry.createRegistry(9999);
            Naming.rebind("rmi://localhost:9999/ebs", server);
            //Naming.rebind("Validator", server);
            System.out.println("Server is bound to registry.");
        } catch (Exception e) {
            System.err.println("Server error:");
            e.printStackTrace();
        }
    }
}

