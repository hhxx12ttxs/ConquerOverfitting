/**
 * Copyright (c) 2009, 5AM Solutions, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * - Neither the name of the author nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.fiveamsolutions.tissuelocator.test.selenium;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.fiveamsolutions.tissuelocator.test.ClientProperties;

/**
 * abstract base class with helper methods for testing list pages.
 * @author ddasgupta
 */
public abstract class AbstractListTest extends AbstractTissueLocatorSeleniumTest {
    private static final String MAC_OS_X = "mac os x";
    private static final String OS_NAME = "os.name";

    /**
     * Default page size.
     */
    protected static final int DEFAULT_PAGE_SIZE = 20;
    private static final int HUNDRED_NUM = 100;
    private static final int FIFTY_NUM = 50;
    private static final int TEN_NUM = 10;
    private static final int TWENTY_NUM = 20;
    /**
     * Default delay.
     */
    protected static final int DELAY = 500;
    private static final String TEN = "10";
    private static final String TWENTY = "20";
    private static final String FIFTY = "50";
    private static final String HUNDRED = "100";

    /**
     * tests column sorting.
     * @param colCount the number of columns to sort
     * @param tableId the id of the table
     * @param includeTime include time in the date format used to test date columns
     * @param dateCols the indexes of the date columns
     * @throws ParseException on error
     */
    protected void sorting(int colCount, String tableId, boolean includeTime, int... dateCols) throws ParseException {
        sorting(colCount, tableId, includeTime, false, new int[0], dateCols);
    }

    /**
     * tests column sorting.
     * @param colCount the number of columns to sort
     * @param tableId the id of the table
     * @param includeTime include time in the date format used to test date columns
     * @param skipCols the columns to skip
     * @param dateCols the indexes of the date columns
     * @throws ParseException on error
     */
    protected void sorting(int colCount, String tableId, boolean includeTime, int[] skipCols, int... dateCols)
            throws ParseException {
        sorting(colCount, tableId, includeTime, false, skipCols, dateCols);
    }

    /**
     * tests column sorting.
     * @param colCount the number of columns to sort
     * @param tableId the id of the table
     * @param includeTime include time in the date format used to test date columns
     * @param treatNumbersAsStrings whether to treat columns with numbers them as strings
     * @param dateCols the indexes of the date columns
     * @throws ParseException on error
     */
    protected void sorting(int colCount, String tableId, boolean includeTime, boolean treatNumbersAsStrings,
            int... dateCols) throws ParseException {
        sorting(colCount, tableId, includeTime, treatNumbersAsStrings, new int[0], dateCols);
    }

    /**
     * tests column sorting.
     * @param colCount the number of columns to sort
     * @param tableId the id of the table
     * @param includeTime include time in the date format used to test date columns
     * @param treatNumbersAsStrings whether to treat columns with numbers them as strings
     * @param skipCols the columns to skip
     * @param dateCols the indexes of the date columns
     * @throws ParseException on error
     */
    protected void sorting(int colCount, String tableId, boolean includeTime, boolean treatNumbersAsStrings,
            int[] skipCols, int... dateCols) throws ParseException {
        //sort each column in ascending and descending order
        int previousAscCount = -1;
        int previousDescCount = -1;
        for (int colIndex = 1; colIndex <= colCount; colIndex++) {
            boolean isSkip = false;
            for (int skipCol : skipCols) {
                isSkip |= skipCol == colIndex;
            }
            if (isSkip) {
                continue;
            }

            boolean isDate = false;
            for (int dateCol : dateCols) {
                isDate |= dateCol == colIndex;
            }
            int ascCount = testColumnSort(colIndex, true, isDate, includeTime, treatNumbersAsStrings, tableId);
            int descCount = testColumnSort(colIndex, false, isDate, includeTime, treatNumbersAsStrings, tableId);
            assertEquals(ascCount, descCount);
            if (previousAscCount > 0) {
                assertEquals(previousAscCount, ascCount);
            }
            if (previousDescCount > 0) {
                assertEquals(previousDescCount, descCount);
            }
            previousAscCount = ascCount;
            previousDescCount = descCount;
        }
    }

    private int testColumnSort(int colIndex, boolean ascending, boolean isDateColumn,
            boolean includeTime, boolean treatNumbersAsStrings, String tableId) throws ParseException {
        if (colIndex != 1 || !ascending) {
            clickAndWait("xpath=//table[@id='" + tableId + "']/thead/tr/th[" + colIndex + "]/a");
        }
        int rowIndex = 1;
        String prevValue = null;
        while (true) {
            if (!selenium.isElementPresent("xpath=//table[@id='" + tableId + "']/tbody/tr[" + rowIndex + "]")) {
                break;
            }
            String cellXPath = "xpath=//table[@id='" + tableId + "']/tbody/tr[" + rowIndex + "]/td[" + colIndex + "]";
            if (selenium.isElementPresent(cellXPath)) {
                String curValue = selenium.getTable(tableId + "." + rowIndex + "." + (colIndex - 1));
                if (prevValue != null) {
                    int compare = 0;
                    if (!isDateColumn || StringUtils.isEmpty(prevValue) || StringUtils.isEmpty(curValue)) {
                        if (!getAsNumber(curValue).equals(Double.NaN) && !getAsNumber(prevValue).equals(Double.NaN)
                                && !treatNumbersAsStrings) {
                            compare = getAsNumber(prevValue).compareTo(getAsNumber(curValue));
                        } else {
                            String osName = System.getProperty(OS_NAME).toLowerCase();
                            if (osName.startsWith(MAC_OS_X)) {
                                compare = prevValue.compareTo(curValue);
                            } else {
                                compare = prevValue.toLowerCase().compareTo(curValue.toLowerCase());
                            }
                        }
                    } else if (isDateColumn) {
                        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                        if (includeTime) {
                             df = new SimpleDateFormat("MMMMM dd, yyyy hh:mm aaa");
                        }
                        Date prevDate = df.parse(prevValue);
                        Date curDate = df.parse(curValue);
                        compare = prevDate.compareTo(curDate);
                    }
                    if (ascending) {
                        assertTrue(String.format("%s is prev, %s is cur", prevValue, curValue), compare <= 0);
                    } else {
                        assertTrue(String.format("%s is prev, %s is cur", prevValue, curValue),
                                compare >= 0 || StringUtils.isBlank(prevValue));
                    }
                }
                prevValue = curValue;
            }
            rowIndex++;
        }
        return rowIndex;
    }

    private Double getAsNumber(String s) {
        String numString = s;
        if (s.endsWith(" mg")) {
            numString = s.substring(0, s.length() - " mg".length());
        }
        numString = numString.replace(",", "");
        try {
            return Double.parseDouble(numString);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    /**
     * tests paging.  Assumes that there are at least 2 pages of data.
     * @param tableId the id of the table
     * @param columnIndex the index of the column used to verify paging
     */
    protected void paging(String tableId, int columnIndex) {
        String cellId = tableId + "." + (columnIndex - 1) + ".1";
        //test paging
        clickAndWait("xpath=//table[@id='" + tableId + "']/thead/tr/th[" + columnIndex + "]/a");
        String prevValue = selenium.getTable(cellId);
        clickAndWait("link=2");
        String curValue = selenium.getTable(cellId);
        assertTrue(prevValue.compareTo(curValue) <= 0);
        prevValue = selenium.getTable(cellId);
        clickAndWait("link=1");
        curValue = selenium.getTable(cellId);
        assertTrue(prevValue.compareTo(curValue) >= 0);
        prevValue = selenium.getTable(cellId);
        clickAndWait("link=Next >>");
        curValue = selenium.getTable(cellId);
        assertTrue(prevValue.compareTo(curValue) <= 0);
        prevValue = selenium.getTable(cellId);
        clickAndWait("link=<< Prev");
        curValue = selenium.getTable(cellId);
        assertTrue(prevValue.compareTo(curValue) >= 0);
        prevValue = selenium.getTable(cellId);
        clickAndWait("link=Last >|");
        curValue = selenium.getTable(cellId);
        assertTrue(prevValue.compareTo(curValue) <= 0);
        prevValue = curValue;
        clickAndWait("link=|< First");
        curValue = selenium.getTable(cellId);
        assertTrue(prevValue.compareTo(curValue) >= 0);
    }
    /**
     * tests paging.  Assumes that there are at least 2 pages of data.
     * @param tableId the id of the table
     */
    protected void paging(String tableId) {
        paging(tableId, 2);
    }

    /**
     * tests page size control.
     * @param resultCount the number of results, assumed to be between 50 and 100.
     */
    protected void pageSize(int resultCount) {
        NumberFormat format = NumberFormat.getInstance();
        String formattedResultCount = format.format(resultCount);
        assertEquals(TWENTY, selenium.getSelectedLabel("pageSize"));
        assertTrue(selenium.isTextPresent("1-20 of " + formattedResultCount + " Results"));
        String pageString = format.format((int) Math.ceil((double) resultCount / (double) TWENTY_NUM));
        assertTrue(selenium.isTextPresent("1 of " + pageString + " Pages"));
        selenium.select("pageSize", "label=" + TEN);
        waitForPageToLoad();
        assertEquals(TEN, selenium.getSelectedLabel("pageSize"));
        assertTrue(selenium.isTextPresent("1-10 of " + formattedResultCount + " Results"));
        pageString = format.format((int) Math.ceil((double) resultCount / (double) TEN_NUM));
        assertTrue(selenium.isTextPresent("1 of " + pageString + " Pages"));
        selenium.select("pageSize", "label=" + TWENTY);
        waitForPageToLoad();
        assertEquals(TWENTY, selenium.getSelectedLabel("pageSize"));
        assertTrue(selenium.isTextPresent("1-20 of " + formattedResultCount + " Results"));
        pageString = format.format((int) Math.ceil((double) resultCount / (double) TWENTY_NUM));
        assertTrue(selenium.isTextPresent("1 of " + pageString + " Pages"));
        selenium.select("pageSize", "label=" + FIFTY);
        waitForPageToLoad();
        assertEquals(FIFTY, selenium.getSelectedLabel("pageSize"));
        assertTrue(selenium.isTextPresent("1-50 of " + formattedResultCount + " Results"));
        pageString = format.format((int) Math.ceil((double) resultCount / (double) FIFTY_NUM));
        assertTrue(selenium.isTextPresent("1 of " + pageString + " Pages"));
        selenium.select("pageSize", "label=" + HUNDRED);
        waitForPageToLoad();
        assertEquals(HUNDRED, selenium.getSelectedLabel("pageSize"));
        if (resultCount > HUNDRED_NUM) {
            assertTrue(selenium.isTextPresent("1-100 of " + formattedResultCount + " Results"));
            pageString = format.format((int) Math.ceil((double) resultCount / (double) HUNDRED_NUM));
            assertTrue(selenium.isTextPresent("1 of " + pageString + " Pages"));
        } else {
            assertTrue(selenium.isTextPresent("1-" + resultCount + " of " + formattedResultCount + " Results"));
            assertTrue(selenium.isTextPresent("1 of 1 Page"));
        }
        if (selenium.isElementPresent("link=Sign Out")) {
        clickAndWait("link=Sign Out");
    }
    }

    /**
     * Test the institute restriction.
     * @param tableId the id of the table
     * @param objectName the name of the object in the menu and the page title
     * @param fieldId the id of the institution field on the edit page
     * @param institutionColumnIndex the index of the institution column.
     * @param buttonColumnIndex the index of the button column.
     * @param firstColumnEdit whether the first column includes a link to the edit page
     * @param showOnlyTestResults whether to filter the results to only the test results
     */
    protected void institutionRestriction(String tableId, String objectName, String fieldId,
            int institutionColumnIndex, int buttonColumnIndex, boolean firstColumnEdit,
            boolean showOnlyTestResults) {
        String instName = ClientProperties.getDefaultConsortiumMemberName();
        String linkXpath = "xpath=//table[@id='%s']/tbody/tr[%d]/td[%d]/a";
        login(ClientProperties.getInstitutionalAdminEmail(), PASSWORD);
        mouseOverAndPause("link=Administration");
        clickAndWait("link=" + objectName + " Administration");
        assertTrue(selenium.isTextPresent(objectName + " Administration"));
        int rowIndex = 0;
        if (showOnlyTestResults) {
            showOnlyTestResults();
        }
        for (int i = 1; i <= DEFAULT_PAGE_SIZE; i++) {
            boolean isPch = selenium.getTable(tableId + "." + i + "." + institutionColumnIndex).contains(instName);
            assertEquals(isPch || !firstColumnEdit,
                    selenium.isElementPresent(String.format(linkXpath, tableId, i, 1)));
            assertEquals(isPch, selenium.isElementPresent(String.format(linkXpath, tableId, i, buttonColumnIndex)));
            if (isPch) {
                rowIndex = i;
            }
        }
        if (StringUtils.isNotBlank(fieldId)) {
            clickAndWait(String.format(linkXpath, tableId, rowIndex, buttonColumnIndex));
            assertFalse(selenium.isElementPresent(fieldId));
            assertTrue(selenium.isTextPresent(instName));
        }
        clickAndWait("link=Sign Out");

        loginAsAdmin();
        mouseOverAndPause("link=Administration");
        clickAndWait("link=" + objectName + " Administration");
        assertTrue(selenium.isTextPresent(objectName + " Administration"));
        for (int i = 1; i <= DEFAULT_PAGE_SIZE; i++) {
            assertTrue(selenium.isElementPresent(String.format(linkXpath, tableId, i, 1)));
            assertTrue(selenium.isElementPresent(String.format(linkXpath, tableId, i, buttonColumnIndex)));
        }
        if (StringUtils.isNotBlank(fieldId)) {
            int colIndex = firstColumnEdit ? 1 : buttonColumnIndex;
            clickAndWait(String.format(linkXpath, tableId, 1, colIndex));
            assertTrue(selenium.isElementPresent(fieldId));
        }
        clickAndWait("link=Sign Out");
    }

    /**
     * Verifies that the table export link is present.
     */
    protected void verifyExportPresent() {
        assertTrue(selenium.isElementPresent("link=Export All"));
    }
}

