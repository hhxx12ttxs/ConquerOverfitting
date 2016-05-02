package Engine;

import GUI.GUIComponent;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Object model by tomasz on 16.05.2014.
 * Methods' implementation by tomasz, rafal, mariusz
 * Klasa zawierająca algorytm genetyczny 1+1.
 * Przedmiotem ewolucji jest ciag kolek, ktorego cechami sa: wspolrzedne srodka, kolor, promien i przezroczystosc
 * Liczebnosc cech okresla fNumber, liczebnosc kolek noOfGenes
 */
public class ImageRecreator implements Runnable {

    /* licznosc kolek skladajacych sie na pojedynczego osobnika */
    private final int noOfGenes;
    /* sigma jest odchyleniem standardowym rozkladu normalnego cechy
     * kazda cecha ma osobna sigme, poniewaz ma osobny rozklad */
    private double[] sigma = new double[GenerationsCreator.fNumber];
    /* parametr m okreslajacy co ile mutacji nalezy weryfikowac sigme */
    private int m;
    private BufferedImage targetImage;

    private GUIComponent mainFrame;

    public boolean cont = true;

    public ImageRecreator(GUIComponent mainFrame) {
        this.mainFrame = mainFrame;
        noOfGenes = EngineConstants.noOfGenes;
        for ( int i=0; i < GenerationsCreator.fNumber; ++i )
        	sigma[i] = EngineConstants.exampleStartingSigma;
        m = EngineConstants.mAttribute;
    }

    public void recreateImage(BufferedImage targetImage) {
        this.targetImage = targetImage;
    }

    /* metoda zawiera procedure ewolucji 
     * w pojedynczej iteracji mutuje jedno kolo i jedna cecha 
     * wybor kola i cechy jest deterministyczny, kolejne cechy mutuja, 
     * po czym nastepuje przejscie do nastepnego kola; 
     * rodzic i potomek sa porownywani na podstawie sumy modulow roznic skladowych kolorow per piksel 
     * kazda cecha ewoluuje az do spelnienia kryterium minimalnej sigmy
     * gdy wszystkie cechy osiagna kryterium stopu, algorytm konczy dzialanie */
    public void run()
    {
        /* 0. inicjalizacja */
        //Ustawianie parametrów populacji
        Dimension imageSize = new Dimension(targetImage.getWidth(), targetImage.getHeight());
        GenerationsCreator.setDimension(imageSize);
        GenerationsCreator.setNoOfGenes(noOfGenes);

        // sigmy sa osobne dla cech, to i ich liczniki musza byc osobne
        int[] noOfChosenChildren = new int[GenerationsCreator.fNumber];
        int[] noOfChosenParents = new int[GenerationsCreator.fNumber];
        for ( int i=0; i < GenerationsCreator.fNumber; ++i )
        {
            noOfChosenChildren[i] = 0;
            noOfChosenParents[i] = 0;
        }
        // licznik kolek i cech, poniewaz modyfikujemy po jednym per mutacja
        int cCounter = 0;
        int fCounter = 0;
        // do wydruku koncowej oceny dopasowania
        double compatibilityFactor = 0;

        /* 1. Tworzenie osobnika początkowego */
        GenerationsCreator parent = new GenerationsCreator();
        mainFrame.updateCurrentImage(parent.getBufferedImage());

        do {
            /* 2. Generowanie potomka */
            GenerationsCreator child = new GenerationsCreator(parent);
            // mutacja pojedynczej cechy - wybor na podstawie przekazywanych parametrow
            child.mutate(sigma,cCounter,fCounter);

            /* 3. Wybieranie lepszego osobnika */
            /* 3.1 Ocena potomka [i rodzica]
            /* na podstawie oceny ktory lepiej przybliza oryginalny obraz
             * miara jest suma po pikselach sumy modulow roznic miedzy skladowymi koloru (RGB) */
            BufferedImage parentImage = parent.getBufferedImage();
            double parentCompatibilityFactor = ImageComparator.cTI(parentImage, targetImage);
            BufferedImage childImage = child.getBufferedImage();
            double childCompatibilityFactor = ImageComparator.cTI(childImage, targetImage);

            //System.out.println("parentCompatibilityFactor: " + parentCompatibilityFactor);
            //System.out.println("childCompatibilityFactor: " + childCompatibilityFactor);
            //System.out.println(child);

            /* 3.2 porownanie ocen, wybor lepszego z nich i zapis statystyk algorytmu */
            if (childCompatibilityFactor < parentCompatibilityFactor)
            {
                parent = child;
                compatibilityFactor = childCompatibilityFactor;
                mainFrame.updateCurrentImage(childImage);
                noOfChosenChildren[fCounter] += 1;
            }
            else
            {
                noOfChosenParents[fCounter] += 1;
            }

            // 4 i 5. Aktualizacja proporcji wybranych y-ków
            if (noOfChosenChildren[fCounter] + noOfChosenParents[fCounter] == m)
            {
                // nie liczymy na zmiennopozycyjnych, wiec przesuniecie dziesietne
                int phi = noOfChosenChildren[fCounter] * 100 / m;

                if (phi < EngineConstants.sigmaDecisionBorder)
                {
                    sigma[fCounter] *= EngineConstants.c1Attribute;
                }
                else if (phi > EngineConstants.sigmaDecisionBorder)
                {
                    sigma[fCounter] *= EngineConstants.c2Attribute;
                }


                //System.out.println("phi: " + phi + "; sigma: " + sigma + "; noOfChosenChildren: " + noOfChosenChildren);

                noOfChosenChildren[fCounter] = 0;
                noOfChosenParents[fCounter] = 0;
            }

            // wybor kolejnych cech dla danego kola
            // jezeli obsluzono wszystkie cechy, wybor nastepnego kola
            // ignoruj cechy ktore osiagnely wlasne kryterium stopu
            // wyjdz z petli z cecha bez stopu lub globalny stop
            for ( int i = 0; i < GenerationsCreator.fNumber; ++i)
            {
                fCounter = (fCounter + 1) % GenerationsCreator.fNumber;
                if ( fCounter == 0 )
                    cCounter = (cCounter + 1) % noOfGenes;
                //cCounter = random.nextInt(noOfGenes);
                if ( sigma[fCounter] > EngineConstants.sigmaMinimum )
                    break;
            }

            //try
            //{
            //    Thread.sleep(1000);
            //}
            //catch (InterruptedException e)
            //{
            //    e.printStackTrace();
            //}

            // jezeli wybrano ceche ze spelnionym warunkiem stopu, to globalny stop
        } while (sigma[fCounter] > EngineConstants.sigmaMinimum && cont);
        //wydruk oceny wyniku
        System.out.println( "result: " + compatibilityFactor );
        //Powiadomienie o zakończeniu pracy
        mainFrame.algorithmStopped();
    }
}

