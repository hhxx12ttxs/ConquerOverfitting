/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package runapplication;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.encog.neural.data.NeuralData;
import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogPersistedCollection;

/**
 *
 * @author nima
 */
public class Main {

    private static String sinaurl = "http://hq.sinajs.cn/list=";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException, DocumentException, ParseException, IOException, Exception {

        {

            if (CheckAbility()) {
                return;
            }
            String udir = args[0];
            XmlClass XC = new XmlClass(udir, "savemoney.xml");
            OpenMysqlConnection dataomc = new OpenMysqlConnection(udir);

            ArrayList<String> stockasSina = dataomc.getSinaStockList();

            ArrayList<String> stockasYahoo = dataomc.getYahooStockList();

            for (String sina : stockasSina) {
                update updatec = new update(sinaurl + sina);
                String sinadataline = updatec.getsl();
                if (sinadataline != null && sinadataline.compareToIgnoreCase("\"\";") != 0) {
                    String yahoo = Sina2Yahoo(stockasYahoo, sina);

                    String[] sinadata = sinadataline.split(",");

                    try {
                        if (Double.parseDouble(sinadata[8]) == 0) {
                            continue;
                        }
                        double lastclose = dataomc.SaveMysql(yahoo, sinadata);
                        XC.SaveTrue(yahoo, Double.parseDouble(sinadata[3]), lastclose);
                        XC.Save();
                    } catch (Exception e) {
                    }
                }
            }

            dataomc.Close();



            dataomc = new OpenMysqlConnection(udir);
            XC = new XmlClass(udir, "savemoney.xml");
            Element rootElement = XC.getRootElement();

            Iterator elementIterator = rootElement.elementIterator();

            while (elementIterator.hasNext()) {
                Element item = (Element) elementIterator.next();
                if (item.attribute("updateDate") == null) {
                    continue;
                }
                String udatevaue = item.attribute("updateDate").getValue();
                S2D updatedate = new S2D(udatevaue);
                S2D todaydate = new S2D(updatedate.getTodayChinese());
                if (updatedate.getDate().compareTo(todaydate.getDate()) != 0) {

                    continue;
                }
                String yahooname = item.attributeValue("yahoo");
                String egfile = item.attributeValue("egfile");

                EncogPersistedCollection epc = new EncogPersistedCollection(udir + "network" + File.separatorChar + egfile);
                BasicNetwork notebook = (BasicNetwork) epc.find("notebook");
                NeuralDataPair nds = dataomc.getTodayData(yahooname);
                if (nds == null) {
                    continue;
                }
                double tomorrow = compute(notebook, nds);
                XC.SaveFalse(item, tomorrow);
            }
            dataomc.removeyear();
            XC.Save();
            dataomc.Close();
        }
    }

    private static boolean CheckAbility() throws ParseException {
        update ue = new update("http://hq.sinajs.cn/list=sh000001");

        String sinaline = ue.getsl();
        if (sinaline == null) {
            return true;
        }
        String[] sinadata = sinaline.split(",");

        S2D lastday = new S2D(sinadata[30]);

        S2D today = new S2D(lastday.getTodayChinese());

        if (lastday.getDate().compareTo(today.getDate()) == 0) {
            return false;
        }
        return true;
    }

    private static String Sina2Yahoo(ArrayList<String> stockasYahoo, String sina) {
        String yahoo = null;
        for (String yahooString : stockasYahoo) {

            if (yahooString.indexOf(sina.substring(2)) != -1) {
                yahoo = yahooString;
                break;
            }
        }
        return yahoo;
    }

    private static double compute(BasicNetwork notebook, NeuralDataPair nds) {
        NeuralData compute = notebook.compute(nds.getInput());
        return compute.getData(0);
    }
}

