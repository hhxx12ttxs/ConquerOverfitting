/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package updatetree;

import java.sql.*;
import java.util.*;
import org.dom4j.*;
import org.encog.*;
import org.encog.neural.data.NeuralDataSet;
import org.encog.normalize.DataNormalization;
import org.encog.normalize.input.*;
import org.encog.normalize.output.OutputFieldDirect;
import org.encog.normalize.output.multiplicative.MultiplicativeGroup;
import org.encog.normalize.output.multiplicative.OutputFieldMultiplicative;
import org.encog.normalize.target.NormalizationStorageNeuralDataSet;

/**
 *
 * @author nima
 */
public class OpenMysqlConnection {

    private Connection connection = null;
    private Statement statement = null;

    public OpenMysqlConnection(String udir) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException, DocumentException, Exception {
        XmlConfigureation xml = new XmlConfigureation(udir, "configuration.xml");
        String url = "jdbc:mysql://" + xml.getConfigurationValue("host") + ":" + xml.getConfigurationValue("port") + "/stockdabase";
        Object newInstance = Class.forName("com.mysql.jdbc.Driver").newInstance();

        connection = DriverManager.getConnection(url, xml.getConfigurationValue("name"), xml.getConfigurationValue("password"));
        statement = connection.createStatement();

    }

    ArrayList<String> getSinaStockList() throws SQLException {
        ArrayList<String> arrayList = new ArrayList<String>();
        ResultSet executeQuery = statement.executeQuery("select sina from stock");
        while (executeQuery.next()) {
            arrayList.add(executeQuery.getString("sina"));
        }
        return arrayList;
    }

    ArrayList<String> getYahooStockList() throws SQLException {
        ArrayList<String> arrayList = new ArrayList<String>();
        ResultSet executeQuery = statement.executeQuery("select yahoo from stock");
        while (executeQuery.next()) {
            arrayList.add(executeQuery.getString("yahoo"));
        }
        return arrayList;
    }

    void SaveMysql(String[] split) throws SQLException {
        ArrayList<String> array = new ArrayList<String>();
        array.add(split[30]);
        array.add(split[1]);
        array.add(split[4]);
        array.add(split[5]);
        array.add(split[3]);
        array.add(split[8]);
        array.add(split[3]);
        String result = arrayToString(array);
    }

    NeuralDataSet getTodayData(String yahooname) throws SQLException {
        ResultSet executeQuery = statement.executeQuery("SELECT * FROM marryagedb  where stockid like \"" + yahooname + "\"  and volume !=0 order by  stockdate ;  ");
        ArrayList<double[]> daydate = new ArrayList<double[]>();

        while (executeQuery.next()) {
            double[] row = new double[6];
            row[0] = executeQuery.getDouble("open");
            row[1] = executeQuery.getDouble("high");
            row[2] = executeQuery.getDouble("low");
            row[3] = executeQuery.getDouble("close");
            row[4] = executeQuery.getDouble("volume");
            row[5] = executeQuery.getDouble("adj");
            daydate.add(row);
        }
        if(daydate.size() < 100)
        {
            return  null;
        }
        index id = new index(daydate);
        ArrayList<double[]> gda = id.getComputedData();
        double[][] gcomputedata = new double[gda.size()][gda.get(0).length];
        for (int i = 0; i < gda.size(); i++) {
            double[] ds = gda.get(i);
            for (int j = 0; j < ds.length; j++) {
                double d = ds[j];
                gcomputedata[i][j] = d;
            }
        }
        return NormalizedArrayListDate(gcomputedata, gda.get(0).length);


    }

    private String arrayToString(ArrayList<String> array) {
        String tmp = "";
        for (int i = 0; i < array.size(); i++) {
            String string = array.get(i);
            tmp += string;
            if ((i + 1) != array.size()) {
                tmp += ",";
            }
        }
        return tmp;
    }

    private NeuralDataSet NormalizedArrayListDate(double[][] gcomputedata, int length) {
        DataNormalization dn = new DataNormalization();
        dn.setReport(new NullStatusReportable());
        NormalizationStorageNeuralDataSet nn = new NormalizationStorageNeuralDataSet(length - 1, 1);
        dn.setTarget(nn);
        InputField ifd[] = new InputField[length];
        for (int i = 0; i < length; i++) {
            dn.addInputField(ifd[i] = new InputFieldArray2D(true, gcomputedata, i));
        }
        MultiplicativeGroup group = new MultiplicativeGroup();
        int count = 1;
        for (InputField inputField : ifd) {
            if (count == length) {
                OutputFieldDirect od = new OutputFieldDirect(inputField);
                od.setIdeal(true);
                dn.addOutputField(od);
            } else {
                dn.addOutputField(new OutputFieldMultiplicative(group, inputField));
            }

        }
        dn.process();

        return nn.getDataset();
    }
}

