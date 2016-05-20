package au.edu.swin.swinbank.ui.web.internetbank;

import au.edu.swin.swinbank.bll.transaction.TransactionManagerRemote;
import au.edu.swin.swinbank.common.transaction.TransactionTO;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

/**
 *
 * @author allan
 */
public class TransactionHistoryManagedBean
{
    @EJB
    private TransactionManagerRemote transactionManager;

    private List<TransactionTO> transactions;
    private ArrayList fromMonths;
    private ArrayList fromYears;
    private ArrayList toMonths;
    private ArrayList toYears;
    private int fromMonth;
    private int fromYear;
    private int toMonth;
    private int toYear;

    private Date fromDate;
    private Date toDate;

    /** Creates a new instance of TransactionHistoryJSFBean */
    public TransactionHistoryManagedBean()
    {
        Calendar cal = Calendar.getInstance();

        cal.set(1990, Calendar.JANUARY, 1);
        fromDate = cal.getTime();

        fromMonth = 0;
        fromYear = 1990;

        toDate = new Date();
        cal.setTime(toDate);

        toMonth = cal.get(Calendar.MONTH);
        toYear = cal.get(Calendar.YEAR);
    }

    /**
     * @return the transactions
     */
    public List<TransactionTO> getTransactions()
    {
        //TODO: Unhardcode Account ID in favour of value retrieved from session
        return transactionManager.getTransactionsForAccountBetweenDates(4L, fromDate, toDate);
    }

    /**
     * @param transactions the transactions to set
     */
    public void setTransactions(List<TransactionTO> transactions)
    {
        this.transactions = transactions;
    }

    public String changePeriod()
    {
        Calendar cal = Calendar.getInstance();
        cal.set(fromYear, fromMonth, 1);
        Date newFromDate = cal.getTime();

        //Add one to the month, as we want to get transactions falling
        //within the to month
        //If the to month is December, set the cut-off to be 0 (January) and
        //turn to the next year
        cal.set(toMonth < 11 ? toYear : toYear + 1, toMonth < 11 ? toMonth + 1 : 0, 1);
        Date newToDate = cal.getTime();

        boolean validRange = true;

        //TODO: Also add validation for future dates, or update selects
        if(newFromDate.after(newToDate))
        {
            validRange = false;
            FacesMessage message = new FacesMessage("Invalid 'From Date' Specified", "('From Date' must occur before the specified 'To Date')");
            FacesContext.getCurrentInstance().addMessage("1", message);
        }
        else if(newToDate.before(newFromDate))
        {
            validRange = false;
            FacesMessage message = new FacesMessage("Invalid 'To Date' Specified", "('To Date' must occur after the specified 'From Date')");
            FacesContext.getCurrentInstance().addMessage("1", message);
        }
        else
        {
            fromDate = newFromDate;
            toDate = newToDate;
        }

        if(validRange)
            transactions = getTransactions();

        return "changed";
    }

    public ArrayList<SelectItem> getFromMonths()
    {
        fromMonths = new ArrayList<SelectItem>();

        fromMonths.add(new SelectItem(0, "January"));
        fromMonths.add(new SelectItem(1, "February"));
        fromMonths.add(new SelectItem(2, "March"));
        fromMonths.add(new SelectItem(3, "April"));
        fromMonths.add(new SelectItem(4, "May"));
        fromMonths.add(new SelectItem(5, "June"));
        fromMonths.add(new SelectItem(6, "July"));
        fromMonths.add(new SelectItem(7, "August"));
        fromMonths.add(new SelectItem(8, "September"));
        fromMonths.add(new SelectItem(9, "October"));
        fromMonths.add(new SelectItem(10, "November"));
        fromMonths.add(new SelectItem(11, "December"));

        return fromMonths;
    }

    public ArrayList<SelectItem> getFromYears()
    {
        fromYears = new ArrayList<SelectItem>();

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        int year = cal.get(Calendar.YEAR) + 1;

        for (int i = year; i >= 1990; i--)
            fromYears.add(new SelectItem(i, Integer.toString(i)));

        return fromYears;
    }

    public ArrayList<SelectItem> getToMonths()
    {
        toMonths = new ArrayList<SelectItem>();

        toMonths.add(new SelectItem(0, "January"));
        toMonths.add(new SelectItem(1, "February"));
        toMonths.add(new SelectItem(2, "March"));
        toMonths.add(new SelectItem(3, "April"));
        toMonths.add(new SelectItem(4, "May"));
        toMonths.add(new SelectItem(5, "June"));
        toMonths.add(new SelectItem(6, "July"));
        toMonths.add(new SelectItem(7, "August"));
        toMonths.add(new SelectItem(8, "September"));
        toMonths.add(new SelectItem(9, "October"));
        toMonths.add(new SelectItem(10, "November"));
        toMonths.add(new SelectItem(11, "December"));

        return toMonths;
    }

    public ArrayList<SelectItem> getToYears()
    {
        toYears = new ArrayList<SelectItem>();

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        int year = cal.get(Calendar.YEAR) + 1;

        for (int i = year; i >= 1990; i--)
            toYears.add(new SelectItem(i, Integer.toString(i)));

        return toYears;
    }

    public int getFromMonth()
    {
        return fromMonth;
    }

    public void setFromMonth(int afterDateMonth)
    {
        this.fromMonth = afterDateMonth;
    }

    public int getFromYear()
    {
        return fromYear;
    }

    public void setFromYear(int afterDateYear)
    {
        this.fromYear = afterDateYear;
    }

    public int getToMonth()
    {
        return toMonth;
    }

    public void setToMonth(int beforeDateMonth)
    {
        this.toMonth = beforeDateMonth;
    }

    public int getToYear()
    {
        return toYear;
    }

    public void setToYear(int beforeDateYear)
    {
        this.toYear = beforeDateYear;
    }
}
