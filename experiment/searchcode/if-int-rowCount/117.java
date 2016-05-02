import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Drew Gotbaum on 1/6/14.
 * Spreadsheet formatter for Lee & Associates LLC.
 * Takes in a spreadsheet and reformats it to a class/market-based standard.
 */
public class ClassReformat {
    private static Market mkt;
    private static Workbook wb;
    private static CellStyle subMarketStyle;
    private static Workbook inputWB;
    private static Workbook lastQuarterWB;
    private static CellStyle percentStyle;
    private static CellStyle bigNum;

    //private static DecimalFormat decimalFormat = new DecimalFormat("#.#");
    //private static String inputFile = "Downtown and Midtown Export.xlsx";
    public static void infoBox(String infoMessage, String location)
    {
        JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + location, JOptionPane.INFORMATION_MESSAGE);
    }
    private static Sheet createMarketSheet( String marketName){
        String name = WorkbookUtil.createSafeSheetName(marketName);
        Sheet s = wb.createSheet(name);
        CreationHelper creationHelper = wb.getCreationHelper();

        //Initializes font and style for header row
        CellStyle headerStyle = wb.createCellStyle();
        headerStyle.setWrapText(true);
        headerStyle.setAlignment(CellStyle.ALIGN_FILL);
        Font font = wb.createFont();
        font.setFontHeightInPoints((short)14);
        font.setFontName("Calibri New");
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        headerStyle.setFont(font);

        //Initializes Cell Style for Submarkets used in inputMarket()
        subMarketStyle = wb.createCellStyle();
        subMarketStyle.setWrapText(true);
        Font font1 = wb.createFont();
        font1.setFontHeightInPoints((short)12);
        font1.setFontName("Cambria New");
        font1.setBoldweight(Font.BOLDWEIGHT_BOLD);
        subMarketStyle.setFont(font1);
        //Populates the header row.
        Row headers = s.createRow(0);
        for (int i = 0; i< Market.HEADER.length; i++) {
            Cell c = headers.createCell(i);
            c.setCellValue(Market.HEADER[i]);
            c.setCellStyle(headerStyle);
        }
        return s;
    }
    private static boolean isRowUC(Row R, int index) {
        Cell C = R.getCell(index,Row.RETURN_BLANK_AS_NULL);
        if (C != null) {
            if (!C.getStringCellValue().contains("UC")) {
                return false;
            }
            else return true;
        }
        else return false;
    }
    //Searches the input file for the index of the given header.
    //Returns -1 if not found.
    public static int getHeaderIndex(Workbook W, String headerName) {
        Sheet s = W.getSheetAt(0);
        Row r = s.getRow(0);
        for (Cell c : r) {
            if (c.getRichStringCellValue().getString().contains(headerName))
                return c.getColumnIndex();
        }
        infoBox((headerName +" not found in spreadsheet.\n Check for typos or acronyms."),"Error Locating Header");
        throw new IllegalArgumentException(headerName + " not found. Check for typos or acronyms.");
    }
    private static void inputMarket() {
        for (String key : mkt.MARKETS.keySet()) {
            int rowIndex = 1;
            Sheet market = createMarketSheet(key);
            for (String subKey : mkt.MARKETS.get(key).keySet()) {
                Row temp = market.createRow(rowIndex++);
                Cell cl  = temp.createCell(0);
                cl.setCellStyle(subMarketStyle);
                cl.setCellValue(subKey);
                Row aRow = market.createRow(rowIndex++);
                Cell A  = aRow.createCell(0);
                A.setCellValue("Class A");
                Row bRow = market.createRow(rowIndex++);
                Cell B  = bRow.createCell(0);
                B.setCellValue("Class B");
                Row cRow = market.createRow(rowIndex++);
                Cell C  = cRow.createCell(0);
                C.setCellValue("Class C");
                Row totalRow = market.createRow(rowIndex++);
                Cell totalCell  = totalRow.createCell(0);
                totalCell.setCellValue("Total:");
            }
            Row total = market.createRow(rowIndex++);
            Cell totalCell  = total.createCell(0);
            totalCell.setCellValue("TOTALS");
            Row aRow = market.createRow(rowIndex++);
            Cell A  = aRow.createCell(0);
            A.setCellValue("Class A");
            Row bRow = market.createRow(rowIndex++);
            Cell B  = bRow.createCell(0);
            B.setCellValue("Class B");
            Row cRow = market.createRow(rowIndex++);
            Cell C  = cRow.createCell(0);
            C.setCellValue("Class C");
        }
        int rowIndex = 0;

        Sheet Manhattan = createMarketSheet("Manhattan");
        Row first = Manhattan.getRow(rowIndex++);
        first.getCell(0).setCellValue("Manhattan");
        Row aRow = Manhattan.createRow(rowIndex++);
        Cell A  = aRow.createCell(0);
        A.setCellValue("Class A");
        Row bRow = Manhattan.createRow(rowIndex++);
        Cell B  = bRow.createCell(0);
        B.setCellValue("Class B");
        Row cRow = Manhattan.createRow(rowIndex++);
        Cell C  = cRow.createCell(0);
        C.setCellValue("Class C");
        Row totalRow = Manhattan.createRow(rowIndex++);
        Cell totalCell  = totalRow.createCell(0);
        totalCell.setCellValue("Total:");



    }
//    private static double toDouble (double D) {
//        return Double.parseDouble(decimalFormat.format(D));
//    }
    private static void weightedRent(String rent, String space, String outputColumn) {
        DataFormat cf = wb.createDataFormat();
        CellStyle currencyCellStyle = wb.createCellStyle();
        currencyCellStyle.setDataFormat(cf.getFormat("$#,##0.00"));
        int rentIndex = getHeaderIndex(inputWB,rent);
        int outputIndex = getHeaderIndex(wb, outputColumn);
        int DASindex = getHeaderIndex(inputWB, space);
        HashMap<String, double[]> manhattan = new HashMap<String, double[]>();
        manhattan.put("A",new double[2] );
        manhattan.put("B", new double[2]);
        manhattan.put("C",new double[2]);
        for (String key : mkt.MARKETS.keySet()) {
            int rowcount = 1;
            double marketRent = 0.0;
            double marketDAS = 0.0;
            Sheet market = wb.getSheet(key);
            HashMap<String, double[]> totalMarket = new HashMap<String, double[]>();
            totalMarket.put("A",new double[2] );
            totalMarket.put("B", new double[2]);
            totalMarket.put("C",new double[2]);
            for (String submarket : mkt.MARKETS.get(key).keySet()) {
                double submarketRent = 0.0;
                double subMarketDAS = 0.0;
                rowcount++;
                for (String Class : mkt.MARKETS.get(key).get(submarket).keySet()) {
                    double classRent = 0.0;
                    double DASCount = 0.0;
                    for (Row property : mkt.MARKETS.get(key).get(submarket).get(Class) ) {
                        Cell c = property.getCell(rentIndex,Row.RETURN_BLANK_AS_NULL);
                        if (c!=null && c.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                            double value = c.getNumericCellValue();
                            //Row DAS = market.getRow(rowcount);
                            Cell DASCell = property.getCell(DASindex);
                            double mult = DASCell.getNumericCellValue();
                            if (mult != 0.0 ) {
                                DASCount+=mult;
                                subMarketDAS+=mult;
                                marketDAS+=mult;
                                double[] DASandRent = manhattan.get(Class);
                                DASandRent[0]+=mult;
                                DASandRent[1]+=(value*mult);
                                manhattan.put(Class,DASandRent);
                                double[] DASandRentMarket = totalMarket.get(Class);
                                DASandRentMarket[0]+=mult;
                                DASandRentMarket[1]+=(value*mult);
                                totalMarket.put(Class,DASandRentMarket);
                                classRent+=(value*mult);
                                submarketRent+=(value*mult);
                                marketRent+=(value*mult);
                            }
                        }
                    }
                    Row output = market.getRow(rowcount++);
                    Cell currentCell = output.createCell(outputIndex);
                    if (DASCount != 0) {
                        double wtdRent = classRent/DASCount;
                        currentCell.setCellValue(wtdRent);
                        currentCell.setCellStyle(currencyCellStyle);
                    }
                    else currentCell.setCellValue(0);

                }
                Row outputFinal = market.getRow(rowcount++);
                Cell currentCellFinal = outputFinal.createCell(outputIndex);
                if (subMarketDAS != 0) {
                    double wtdRent = submarketRent/subMarketDAS;
                    currentCellFinal.setCellValue(wtdRent);
                    currentCellFinal.setCellStyle(currencyCellStyle);
                }

            }
            Row marketOutput = market.getRow(rowcount++);
            Cell marketCell = marketOutput.createCell(outputIndex);
            if (marketDAS != 0) {
                double wtdRent = marketRent/marketDAS;
                marketCell.setCellValue(wtdRent);
                marketCell.setCellStyle(currencyCellStyle);
            }
            int I = getHeaderIndex(wb,outputColumn);
            for (String K : totalMarket.keySet()){
                double[] num = totalMarket.get(K);
                if (num[0] != 0) {
                    Cell C =  market.getRow(rowcount++).createCell(I);
                    C.setCellValue(num[1]/num[0]);
                    C.setCellStyle(currencyCellStyle);
                }
            }
        }
        Sheet man = wb.getSheet("Manhattan");
        int index = getHeaderIndex(wb,outputColumn);
        double finalDAS = 0;
        double finalRent = 0;
        int rowCount = 1;
        for (String key : manhattan.keySet()){
            double[] nums = manhattan.get(key);
            finalDAS+=nums[0];
            finalRent+=nums[1];
            if (nums[0] != 0) {
                double manRent = nums[1]/nums[0];
                Cell C  = man.getRow(rowCount++).createCell(index);
                C.setCellValue(manRent);
                C.setCellStyle(currencyCellStyle);
            }
        }
        if (finalDAS != 0) {
            Cell C =man.getRow(rowCount).createCell(index);
            C.setCellValue(finalRent/finalDAS);
            C.setCellStyle(currencyCellStyle);
        }
    }
    private static void weightedOverallRent() {
        DataFormat cf = wb.createDataFormat();
        CellStyle currencyCellStyle = wb.createCellStyle();
        currencyCellStyle.setDataFormat(cf.getFormat("$#,##0.00"));
        int subRentIndex = getHeaderIndex(inputWB,"Avg Rent-Sublet");
        int dirRentIndex = getHeaderIndex(inputWB,"Avg Rent-Direct");
        int SASIndex = getHeaderIndex(inputWB,"Sublet Available Space");
        int outputIndex = getHeaderIndex(wb, "Weighted Overall Average");
        int DASindex = getHeaderIndex(inputWB, "Direct Available Space");
        HashMap<String, double[]> manhattan = new HashMap<String, double[]>();
        manhattan.put("A",new double[2] );
        manhattan.put("B", new double[2]);
        manhattan.put("C",new double[2]);
        for (String key : mkt.MARKETS.keySet()) {
            int rowcount = 1;
            double marketRent = 0;
            double marketDAS = 0.0;
            Sheet market = wb.getSheet(key);
            HashMap<String, double[]> totalMarket = new HashMap<String, double[]>();
            totalMarket.put("A",new double[2] );
            totalMarket.put("B", new double[2]);
            totalMarket.put("C",new double[2]);
            for (String submarket : mkt.MARKETS.get(key).keySet()) {
                double submarketRent = 0;
                double subMarketDAS = 0;
                rowcount++;
                for (String Class : mkt.MARKETS.get(key).get(submarket).keySet()) {
                    double classRent = 0.0;
                    double DASCount = 0.0;
                    for (Row property : mkt.MARKETS.get(key).get(submarket).get(Class) ) {
                        Cell dirCell = property.getCell(dirRentIndex,Row.RETURN_BLANK_AS_NULL);
                        Cell subCell = property.getCell(subRentIndex,Row.RETURN_BLANK_AS_NULL);
                        //Direct Available Rent
                        if (dirCell!=null && dirCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                            double value = dirCell.getNumericCellValue();
                            //Row DAS = market.getRow(rowcount);
                            Cell DASCell = property.getCell(DASindex);
                            double mult = DASCell.getNumericCellValue();
                            if (mult != 0) {
                                double[] DASandRent = manhattan.get(Class);
                                DASandRent[0]+=mult;
                                DASandRent[1]+=(value*mult);
                                manhattan.put(Class,DASandRent);
                                double[] DASandRentMarket = totalMarket.get(Class);
                                DASandRentMarket[0]+=mult;
                                DASandRentMarket[1]+=(value*mult);
                                totalMarket.put(Class,DASandRentMarket);
                                DASCount+=mult;
                                subMarketDAS+=mult;
                                marketDAS+=mult;
                                classRent+=(value*mult);
                                submarketRent+=(value*mult);
                                marketRent+=(value*mult);
                            }
                        }
                        //Sublet available rent
                        if (subCell!=null && subCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                            double value = subCell.getNumericCellValue();
                            //Row SAS = market.getRow(rowcount);
                            Cell SASCell = property.getCell(SASIndex);
                            double mult = SASCell.getNumericCellValue();
                            if (mult != 0) {
                                DASCount+=mult;
                                subMarketDAS+=mult;
                                marketDAS+=mult;
                                classRent+=(value*mult);
                                submarketRent+=(value*mult);
                                marketRent+=(value*mult);
                            }
                        }
                    }
                    Row output = market.getRow(rowcount++);
                    Cell currentCell = output.createCell(outputIndex);
                    if (DASCount != 0) {
                        double wtdRent = classRent/DASCount;
                        currentCell.setCellValue(wtdRent);
                        currentCell.setCellStyle(currencyCellStyle);
                    }
                    else currentCell.setCellValue(0);

                }
                Row outputFinal = market.getRow(rowcount++);
                Cell currentCellFinal = outputFinal.createCell(outputIndex);
                if (subMarketDAS != 0) {
                    double wtdRent = submarketRent/subMarketDAS;
                    currentCellFinal.setCellValue(wtdRent);
                    currentCellFinal.setCellStyle(currencyCellStyle);
                }

            }
            Row marketOutput = market.getRow(rowcount++);
            Cell marketCell = marketOutput.createCell(outputIndex);
            if (marketDAS != 0) {
                double wtdRent = marketRent/marketDAS;
                marketCell.setCellValue(wtdRent);
                marketCell.setCellStyle(currencyCellStyle);
            }
            int I = getHeaderIndex(wb,"Weighted Overall Average");
            for (String K : totalMarket.keySet()){
                double[] num = totalMarket.get(K);
                if (num[0] != 0) {
                    Row R = market.getRow(rowcount++);
                    Cell C = R.createCell(I);
                    C.setCellValue(num[1] / num[0]);
                    C.setCellStyle(currencyCellStyle);
                }
            }
        }
        Sheet man = wb.getSheet("Manhattan");
        int index = getHeaderIndex(wb,"Weighted Overall Average");
        double finalDAS = 0;
        double finalRent = 0;
        int rowCount = 1;
        for (String key : manhattan.keySet()){
            double[] nums = manhattan.get(key);
            finalDAS+=nums[0];
            finalRent+=nums[1];
            double manRent = nums[1]/nums[0];
            Row R = man.getRow(rowCount++);
            Cell C = R.createCell(index);
            C.setCellValue(manRent);
            C.setCellStyle(currencyCellStyle);


        }
        Row R = man.getRow(rowCount);
        Cell C = R.createCell(index);
        C.setCellValue(finalRent / finalDAS);
        C.setCellStyle(currencyCellStyle);
    }
    private static void cleanUp() {
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            Sheet s = wb.getSheetAt(i);
            for (int k = 0; k < Market.HEADER.length; k++)
                s.autoSizeColumn(k);
        }
        for (String m : mkt.MARKETS.keySet()) {
            Sheet sheet = wb.getSheet(m);
            Row r = sheet.getRow(0);
            Cell c = r.getCell(0);
            c.setCellValue("Submarkets and their Respective Classes");
        }
    }
    private static void manhattanMapper(HashMap<String,Integer> manhattan,String outputColumn) {
        Sheet man = wb.getSheet("Manhattan");
        int index = getHeaderIndex(wb,outputColumn);
        int sum = 0;
        int rowCount = 1;
        for (String key : manhattan.keySet()){
            int num = manhattan.get(key);
            man.getRow(rowCount++).createCell(index).setCellValue(num);
            sum += num;

        }
        man.getRow(rowCount).createCell(index).setCellValue(sum);
    }
    private static void constructedSquareFeet(){
        int inputIndex = getHeaderIndex(inputWB,"Rentable Building Area");
        int outputIndex = getHeaderIndex(wb, "Under Construction (SF)");
        int UCIndex = getHeaderIndex(inputWB, "Type (my data)");
        HashMap<String, Integer> manhattan = new HashMap<String, Integer>();
        manhattan.put("A",0);
        manhattan.put("B",0);
        manhattan.put("C",0);
        for (String key : mkt.MARKETS.keySet()) {
            HashMap<String, Integer> marketTotals = new HashMap<String, Integer>();
            marketTotals.put("A",0);
            marketTotals.put("B",0);
            marketTotals.put("C",0);
            int rowcount = 1;
            int marketTotal = 0;
            Sheet market = wb.getSheet(key);
            for (String submarket : mkt.MARKETS.get(key).keySet()) {
                int submarketTotal = 0;
                rowcount++;
                for (String Class : mkt.MARKETS.get(key).get(submarket).keySet()) {
                    int classTotal = 0;
                    for (Row property : mkt.MARKETS.get(key).get(submarket).get(Class) ) {
                        Cell c = property.getCell(inputIndex,Row.RETURN_BLANK_AS_NULL);
                        if (c!=null && isRowUC(property, UCIndex)) {
                            int value = (int)c.getNumericCellValue();
                            int manSum = manhattan.get(Class);
                            manSum+= value;
                            manhattan.put(Class,manSum);

                            int marketSum = marketTotals.get(Class);
                            marketSum+=value;
                            marketTotals.put(Class,marketSum);


                            classTotal+=value;
                            submarketTotal+=value;
                            marketTotal+=value;
                        }
                    }
                    Row output = market.getRow(rowcount++);
                    Cell currentCell = output.createCell(outputIndex);
                    currentCell.setCellValue(classTotal);
                    currentCell.setCellStyle(bigNum);
                }
                Row outputFinal = market.getRow(rowcount++);
                Cell currentCellFinal = outputFinal.createCell(outputIndex);
                currentCellFinal.setCellValue(submarketTotal);
                currentCellFinal.setCellStyle(bigNum);
            }
            Row marketOutput = market.getRow(rowcount++);
            Cell marketCell = marketOutput.createCell(outputIndex);
            marketCell.setCellValue(marketTotal);
            marketCell.setCellStyle(bigNum);
            for (String K : manhattan.keySet()){
                int num = marketTotals.get(K);
                Cell C = market.getRow(rowcount++).createCell(outputIndex);
                C.setCellValue(num);
                C.setCellStyle(bigNum);
            }
        }
        Sheet man = wb.getSheet("Manhattan");
        int sum = 0;
        int rowCount = 1;
        for (String key : manhattan.keySet()){
            int num = manhattan.get(key);
            Cell C = man.getRow(rowCount++).createCell(outputIndex);
            C.setCellValue(num);
            C.setCellStyle(bigNum);
            sum += num;

        }
        Cell C = man.getRow(rowCount).createCell(outputIndex);
        C.setCellValue(sum);
        C.setCellStyle(bigNum);

    }
    private static void isUnderConstruction() {
        int inputIndex = getHeaderIndex(inputWB,"Type (my data)");
        int outputIndex = getHeaderIndex(wb, "Under Construction");
        HashMap<String, Integer> manhattan = new HashMap<String, Integer>();
        manhattan.put("A",0);
        manhattan.put("B",0);
        manhattan.put("C",0);
        for (String key: mkt.MARKETS.keySet()) {
            HashMap<String, Integer> marketTotals = new HashMap<String, Integer>();
            marketTotals.put("A",0);
            marketTotals.put("B",0);
            marketTotals.put("C",0);
            int rowcount = 1;
            int total = 0;
            Sheet market = wb.getSheet(key);
            for (String submarket : mkt.MARKETS.get(key).keySet()) {
                int subtotal = 0;
                rowcount++;
                for (String Class : mkt.MARKETS.get(key).get(submarket).keySet()) {
                    int conCount = 0;
                    for (Row property : mkt.MARKETS.get(key).get(submarket).get(Class) ) {
                        Cell c = property.getCell(inputIndex,Row.RETURN_BLANK_AS_NULL);
                        if (c!=null) {
                            String value = c.getStringCellValue();
                            if (value.contains("UC")) {
                                int manSum = manhattan.get(Class);
                                manSum++;
                                manhattan.put(Class,manSum);
                                int marketSum = marketTotals.get(Class);
                                marketSum++;
                                marketTotals.put(Class, marketSum);
                                conCount++;
                                subtotal++;
                                total++;
                            }

                        }
                    }
                    Row output = market.getRow(rowcount++);
                    Cell currentCell = output.createCell(outputIndex);
                    currentCell.setCellValue(conCount);

                }
                Row outputFinal = market.getRow(rowcount++);
                Cell currentCellFinal = outputFinal.createCell(outputIndex);
                currentCellFinal.setCellValue(subtotal);

            }
            Row marketOutput = market.getRow(rowcount++);
            Cell marketCell = marketOutput.createCell(outputIndex);
            marketCell.setCellValue(total);
            int I = getHeaderIndex(wb,"Under Construction");
            for (String K : marketTotals.keySet()){
                int num = marketTotals.get(K);
                market.getRow(rowcount++).createCell(I).setCellValue(num);
            }

        }
        manhattanMapper(manhattan, "Under Construction");
    }
    private static void numBuildings(){
        HashMap<String, Integer> manhattan = new HashMap<String, Integer>();
        manhattan.put("A",0);
        manhattan.put("B",0);
        manhattan.put("C",0);
        int UCIndex = getHeaderIndex(inputWB, "Type (my data)");
        for (String m : mkt.MARKETS.keySet()){
            HashMap<String, Integer> marketTotals = new HashMap<String, Integer>();
            marketTotals.put("A",0);
            marketTotals.put("B",0);
            marketTotals.put("C",0);

            int total = 0;
            int rowCount = 1;
            Sheet sheet = wb.getSheet(m);
            for (String s : mkt.MARKETS.get(m).keySet()){

                int subTotal = 0;
                rowCount++;
                for (String c : mkt.MARKETS.get(m).get(s).keySet()) {
                    int num = 0;
                    for (Row r : mkt.MARKETS.get(m).get(s).get(c)) {
                        if (!isRowUC(r, UCIndex))
                            num++;
                    }
                    subTotal+=num;
                    total+=num;
                    int sum = manhattan.get(c);
                    int marketSum = marketTotals.get(c);
                    marketSum+=num;
                    sum+= num;
                    marketTotals.put(c,marketSum);
                    manhattan.put(c,sum);
                    Row currentRow = sheet.getRow(rowCount++);
                    Cell currentCell = currentRow.createCell(1);
                    currentCell.setCellValue(num);
                    currentCell.setCellStyle(bigNum);
                }
                Row subRow = sheet.getRow(rowCount++);
                Cell subCell = subRow.createCell(1);
                subCell.setCellValue(subTotal);
                subCell.setCellStyle(bigNum);
            }
            Row finalRow = sheet.createRow(rowCount++);
            Cell nameCell = finalRow.createCell(0);
            nameCell.setCellValue(m + " Totals:");
            nameCell.setCellStyle(subMarketStyle);
            Cell finalCell = finalRow.createCell(1);
            finalCell.setCellValue(total);
            finalCell.setCellStyle(bigNum);
            int I = getHeaderIndex(wb,"Number of Buildings");
            for (String key : marketTotals.keySet()){
                int num = marketTotals.get(key);
                sheet.getRow(rowCount++).createCell(I).setCellValue(num);
            }



        }
        manhattanMapper(manhattan,"Number of Buildings");

    }
    private static void processColumn(String inputColumn, String outputColumn) {
        int inputIndex = getHeaderIndex(inputWB,inputColumn);
        int outputIndex = getHeaderIndex(wb, outputColumn);
        int UCIndex = getHeaderIndex(inputWB, "Type (my data)");
        HashMap<String, Integer> manhattan = new HashMap<String, Integer>();
        manhattan.put("A",0);
        manhattan.put("B",0);
        manhattan.put("C",0);
        for (String key : mkt.MARKETS.keySet()) {
            HashMap<String, Integer> marketTotals = new HashMap<String, Integer>();
            marketTotals.put("A",0);
            marketTotals.put("B",0);
            marketTotals.put("C",0);
            int rowcount = 1;
            int marketTotal = 0;
            Sheet market = wb.getSheet(key);
            for (String submarket : mkt.MARKETS.get(key).keySet()) {
                int submarketTotal = 0;
                rowcount++;
                for (String Class : mkt.MARKETS.get(key).get(submarket).keySet()) {
                    int classTotal = 0;
                    for (Row property : mkt.MARKETS.get(key).get(submarket).get(Class) ) {
                        Cell c = property.getCell(inputIndex,Row.RETURN_BLANK_AS_NULL);

                        if (c!=null && !isRowUC(property, UCIndex)) {
                            int value = (int)c.getNumericCellValue();
                            int manSum = manhattan.get(Class);
                            manSum+= value;
                            manhattan.put(Class,manSum);

                            int marketSum = marketTotals.get(Class);
                            marketSum+=value;
                            marketTotals.put(Class,marketSum);


                            classTotal+=value;
                            submarketTotal+=value;
                            marketTotal+=value;
                        }
                    }
                    Row output = market.getRow(rowcount++);
                    Cell currentCell = output.createCell(outputIndex);
                    currentCell.setCellValue(classTotal);
                    currentCell.setCellStyle(bigNum);
                }
                Row outputFinal = market.getRow(rowcount++);
                Cell currentCellFinal = outputFinal.createCell(outputIndex);
                currentCellFinal.setCellValue(submarketTotal);
                currentCellFinal.setCellStyle(bigNum);
            }
            Row marketOutput = market.getRow(rowcount++);
            Cell marketCell = marketOutput.createCell(outputIndex);
            marketCell.setCellValue(marketTotal);
            marketCell.setCellStyle(bigNum);
            int I = getHeaderIndex(wb,outputColumn);
            for (String K : manhattan.keySet()){
                int num = marketTotals.get(K);
                Cell C = market.getRow(rowcount++).createCell(I);
                C.setCellValue(num);
                C.setCellStyle(bigNum);
            }
        }
        Sheet man = wb.getSheet("Manhattan");
        int index = getHeaderIndex(wb,outputColumn);
        int sum = 0;
        int rowCount = 1;
        for (String key : manhattan.keySet()){
            int num = manhattan.get(key);
            Cell C = man.getRow(rowCount++).createCell(index);
            C.setCellValue(num);
            C.setCellStyle(bigNum);
            sum += num;

        }
        Cell C = man.getRow(rowCount).createCell(index);
        C.setCellValue(sum);
        C.setCellStyle(bigNum);
    }
    private static void calculateAvailability(String numerator, String denominator, String outputHeader){
        int denomIndex = getHeaderIndex(wb,denominator);
        int numIndex = getHeaderIndex(wb,numerator);
        int availIndex = getHeaderIndex(wb,outputHeader);
        for (int key = 0; key < wb.getNumberOfSheets(); key++) {
            Sheet market = wb.getSheetAt(key);
            for (int i = 1; i <= market.getLastRowNum(); i++) {
                Row r = market.getRow(i);
                Cell DASCell = r.getCell(numIndex, Row.RETURN_BLANK_AS_NULL);
                Cell invCell = r.getCell(denomIndex, Row.CREATE_NULL_AS_BLANK);
                if (DASCell != null && invCell != null) {
                    double DAS = DASCell.getNumericCellValue();
                    double inv = invCell.getNumericCellValue();
                    if (!(DAS == 0 && inv == 0)) {
                        double directAvailability = (DAS / inv);
                        Cell c = r.createCell(availIndex);
                        c.setCellValue(directAvailability);
                        c.setCellStyle(percentStyle);
                    }
                    else {
                        Cell c = r.createCell(availIndex);
                        c.setCellValue(0);
                    }

                }
            }
        }
    }
    private static void calculateTotalSum(String column1, String column2, String outputHeader){
        int denomIndex = getHeaderIndex(wb,column1);
        int numIndex = getHeaderIndex(wb,column2);
        int availIndex = getHeaderIndex(wb,outputHeader);
        for (int key = 0; key < wb.getNumberOfSheets(); key++) {
            Sheet market = wb.getSheetAt(key);
            for (int i = 1; i <= market.getLastRowNum(); i++) {
                Row r = market.getRow(i);
                Cell DASCell = r.getCell(numIndex, Row.RETURN_BLANK_AS_NULL);
                Cell invCell = r.getCell(denomIndex, Row.CREATE_NULL_AS_BLANK);
                if (DASCell != null && invCell != null) {
                    double DAS = DASCell.getNumericCellValue();
                    double inv = invCell.getNumericCellValue();
                    if (!(DAS == 0 && inv == 0)) {
                        int total = (int)(DAS + inv);
                        Cell c = r.createCell(availIndex);
                        c.setCellValue(total);
                        c.setCellStyle(bigNum);
                    }
                    else {
                        Cell c = r.createCell(availIndex);
                        c.setCellValue(0);
                    }

                }
            }
        }
    }
    private static void occupiedSpace(String inv, String totVacant,String outputHead) {
        int invIndex = getHeaderIndex(wb,inv);
        int totVacIndex = getHeaderIndex(wb,totVacant);
        int availIndex = getHeaderIndex(wb,outputHead);
        for (int key = 0; key < wb.getNumberOfSheets(); key++) {
            Sheet market = wb.getSheetAt(key);
            for (int i = 1; i <= market.getLastRowNum(); i++) {
                Row r = market.getRow(i);
                Cell DASCell = r.getCell(totVacIndex, Row.RETURN_BLANK_AS_NULL);
                Cell invCell = r.getCell(invIndex, Row.CREATE_NULL_AS_BLANK);
                if (DASCell != null && invCell != null) {
                    double Inventory = invCell.getNumericCellValue();
                    double TotalVacant = DASCell.getNumericCellValue();
                    int total = (int)(Inventory - TotalVacant);
                    Cell c = r.createCell(availIndex);
                    c.setCellValue(total);
                    c.setCellStyle(bigNum);


                }
            }
        }
    }
    private static void calculateNetAbsorption() {
        int lastQuarterOSIndex = getHeaderIndex(lastQuarterWB,"Occupied Space");
        int currentOSIndex = getHeaderIndex(wb,"Occupied Space");
        int netIndex = getHeaderIndex(wb,"Net Absorption");
        for (int key = 0; key<wb.getNumberOfSheets();key++) {
            Sheet market = wb.getSheetAt(key);
            for (int i = 1; i <= market.getLastRowNum(); i++) {
                Row r = market.getRow(i);
                Cell currentOS = r.getCell(currentOSIndex, Row.RETURN_BLANK_AS_NULL);
                Cell lastOS = r.getCell(lastQuarterOSIndex, Row.CREATE_NULL_AS_BLANK);
                if (currentOS != null && lastOS != null) {
                    double OS = currentOS.getNumericCellValue();
                    double prevOS = lastOS.getNumericCellValue();
                    if (!(OS == 0 && prevOS == 0)) {
                        int netAbsorption = (int)(OS-prevOS);
                        //System.out.println(OS + " - "+prevOS+" = "+netAbsorption);
                        Cell c = r.createCell(netIndex);
                        c.setCellValue(netAbsorption);
                        c.setCellStyle(bigNum);
                    }
                    else {
                        Cell c = r.createCell(netIndex);
                        c.setCellValue(0);
                    }

                }
            }
        }
    }

    private static void populate() {
        inputMarket();
        numBuildings();
        processColumn("Rentable Building Area", "Inventory");//Calculates Inventory
        processColumn("Direct Available Space", "Direct Available Space");//direct available space
        processColumn("Sublet Available Space", "Sublet Available Space");//sublet available space
        calculateAvailability("Direct Available Space","Inventory","Direct Availability");//Calculates direct availability
        calculateAvailability("Sublet Available Space", "Inventory","Sublet Availability");//calculates sublet availability
        processColumn("Total Available Space","Total Available Space");
        calculateAvailability("Total Available Space","Inventory","Total Availability");
        processColumn("Direct Vacant Space", "Direct Vacant Space");
        processColumn("Sublet Vacant Space", "Sublet Vacant Space");
        calculateAvailability("Direct Vacant Space","Inventory","Direct Vacancy");
        calculateAvailability("Sublet Vacant Space","Inventory","Sublet Vacancy");
        calculateTotalSum("Direct Vacant Space","Sublet Vacant Space","Total Vacant Space");
        calculateAvailability("Total Vacant Space","Inventory","Total Vacancy");
        occupiedSpace("Inventory","Total Vacant Space","Occupied Space");
        isUnderConstruction();
        weightedRent("Avg Rent-Direct","Direct Available Space","Weighted Direct Average");
        weightedRent("Avg Rent-Sublet","Sublet Available Space","Weighted Sublease Average");
        weightedOverallRent();
        constructedSquareFeet();

    }
    public static void main(String [] args) throws IOException, InvalidFormatException {


        FileNameExtensionFilter filter = new FileNameExtensionFilter("XLSX and XLS Spreadsheets", "xls", "xlsx");
        JFileChooser Chooser = new JFileChooser();
        Chooser.setFileFilter(filter);
        int reply = JOptionPane.showConfirmDialog(null, "Would you like to calculate Net Absorption?"
                , "Net Absorption Tool", JOptionPane.YES_NO_OPTION);
        String lastQuarterPath = null;
        boolean absorption = true;
        System.out.println(reply);
        if (reply == 0) {

            if(Chooser.showDialog(null, "Load")== JFileChooser.APPROVE_OPTION) {
                lastQuarterPath = Chooser.getSelectedFile().getPath();
            }
            System.out.println("Yes");
        }
        else absorption = false;

        //System.out.println(absorption);
        //JFileChooser chooser = new JFileChooser();
        //chooser.setFileFilter(filter);
        String inputPath = null;
        int returnVal = Chooser.showOpenDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            inputPath = Chooser.getSelectedFile().getPath();
        }
        String outputFile = JOptionPane.showInputDialog("Enter desired output name: ");
        wb = new XSSFWorkbook();
         inputWB = WorkbookFactory.create(new File("test.xlsx"));

        mkt = new Market(inputWB);
        //Create a percent style for availability cells.
        percentStyle = wb.createCellStyle();
        percentStyle.setDataFormat(wb.createDataFormat().getFormat("0.0%"));
        DataFormat cf = wb.createDataFormat();
        bigNum = wb.createCellStyle();
        bigNum.setDataFormat(cf.getFormat("#,##0"));

        populate();
        if (absorption) {
            lastQuarterWB = WorkbookFactory.create(new File(lastQuarterPath));
            calculateNetAbsorption();
        }

        cleanUp();
        FileOutputStream fileOut = new FileOutputStream(outputFile+".xlsx");
        wb.write(fileOut);
        fileOut.close();


    }
}

