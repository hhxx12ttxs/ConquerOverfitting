package es.unileon.ulebank.assets;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import es.unileon.ulebank.account.Account;
import es.unileon.ulebank.assets.command.PaidLoanCommand;
import es.unileon.ulebank.assets.exceptions.LoanException;
import es.unileon.ulebank.assets.financialproducts.InterestRate;
import es.unileon.ulebank.assets.handler.CommandHandler;
import es.unileon.ulebank.assets.history.LoanHistory;
import es.unileon.ulebank.assets.iterator.LoanIterator;
import es.unileon.ulebank.assets.iterator.LoanIteratorDates;
import es.unileon.ulebank.assets.strategy.loan.FrenchMethod;
import es.unileon.ulebank.assets.strategy.loan.ScheduledPayment;
import es.unileon.ulebank.assets.strategy.loan.StrategyLoan;
import es.unileon.ulebank.assets.support.PaymentPeriod;
import es.unileon.ulebank.client.Client;
import es.unileon.ulebank.exceptions.TransactionException;
import es.unileon.ulebank.fees.FeeStrategy;
import es.unileon.ulebank.fees.InvalidFeeException;
import es.unileon.ulebank.fees.LoanFee;
import es.unileon.ulebank.handler.Handler;
import es.unileon.ulebank.history.GenericTransaction;
import es.unileon.ulebank.history.Transaction;
import es.unileon.ulebank.tasklist.Task;
import es.unileon.ulebank.tasklist.TaskList;
import es.unileon.ulebank.time.Time;

// TODO PREGUNTAR A CAMINO COMO ACTUALIZAR DEBT CUANDO PASIVOS REALIZA EL PAGO DE LA CUOTA

public class Loan implements FinancialProduct {
    /**
     * Type of time period used for the effective interest
     */
    private PaymentPeriod paymentPeriod;

    /**
     * Interest applicated to the loan
     */
    private double interest;

    /**
     * Number of fees to resolve the loan
     */
    private int amortizationTime;

    /**
     * Amount of money required for the user
     */
    private double initialCapital;

    /**
     * Amount of money that the user have not payed yet
     */
    private double debt;

    /**
     * Unique identificator for the loan
     */
    private Handler idLoan;

    /**
     * Strategy used for calculate the payments
     */
    private StrategyLoan strategy;

    /**
     * Money that you have already payed
     */
    private double amortized;

    /**
     * Arraylist where you store the fees with all data
     */
    private List<ScheduledPayment> payments;

    /**
     * Commisions that you have in the contract
     */

    /**
     * Commission that you applied if the owner cancel the loan
     */
    private FeeStrategy cancelCommission;
    /**
     * Commission applied when the bank studied the account and other things
     */
    private FeeStrategy studyFee;
    /**
     * Commission applied for open a loan
     */
    private FeeStrategy openningFee;
    /**
     * Commission applied if the owner decides modify the loan contract during
     * the loan
     */
    private FeeStrategy modifyFee;
    /**
     * Commission applied if the owner decides to amortize some part of the loan
     */
    private FeeStrategy amortizedFee;
    /**
     * Commission applicated in the case of the client do not pay the fee in the
     * correct time
     */
    private FeeStrategy delayedPaymentFee;

    /**
     * Account where we must charge the different payments of the loan
     */
    private final Account account;
    /**
     * Client that
     */
    protected static Client client;

    /**
     * List where we store the payments for every loans
     */

    private final LoanHistory loanHistory;

    /*
     * internal index used to have the possibility to change the arraylist of
     * the payments
     */
    private int arrayListIndex;

    /*
     * Fixed fee that you have to pay every month
     */
    private double periodFee;

    /**
     * This is the date that the loan is created
     */
    private Date creatinngDate;
    /**
     * This is the interest of the bank
     */

    private double interestOfBank;

    /**
     * This is the loan description for the client
     */
    private String description;

    private ScheduledPayment nextPayment;

    /**
     * Constructor of the class
     * 
     * @param idLoan
     * @param initialCapital
     * @param interest
     * @param paymentPeriod
     * @param amortizationTime
     * @param account
     * @throws LoanException
     */
    public Loan(Handler idLoan, double initialCapital, double interest,
            PaymentPeriod paymentPeriod, int amortizationTime, Account account,
            Client client, String description) throws LoanException {
        final StringBuilder exceptionMessage = new StringBuilder();

        this.loanHistory = new LoanHistory();
        try {
            this.cancelCommission = new LoanFee(0, false);
            this.studyFee = new LoanFee(0, false);
            this.cancelCommission = new LoanFee(0, false);
            this.modifyFee = new LoanFee(0, false);
            this.openningFee = new LoanFee(0, false);
            this.amortizedFee = new LoanFee(0, false);
            this.delayedPaymentFee = new LoanFee(0, false);
        } catch (final InvalidFeeException e) {
            exceptionMessage.append("Commission is marformed.");
        }

        this.idLoan = idLoan;

        if (initialCapital < 100000000) {
            this.debt = initialCapital;
        } else {
            exceptionMessage
                    .append("The bank can not lend this amount of money");
        }

        if ((interest >= 0) && (interest <= 1)) {
            this.interest = interest;
            this.setInterestOfBank(interest);
        } else {
            exceptionMessage
                    .append("The interest value must be a value between 0 and 1\n");
        }

        this.paymentPeriod = paymentPeriod;
        this.amortizationTime = amortizationTime;
        this.payments = new ArrayList<ScheduledPayment>();
        this.initialCapital = this.debt;
        this.strategy = new FrenchMethod(this);
        this.account = account;
        Loan.client = client;
        this.description = description;
        this.payments = this.strategy.doCalculationOfPayments();
        this.loanHistory.addAllPayments(this.payments);
        this.arrayListIndex = 0;

        if (exceptionMessage.length() > 1) {
            throw new LoanException(exceptionMessage.toString());
        }

        this.creatinngDate = new Date(Time.getInstance().getTime());
        this.debt += this.openningFee.getFee(this.debt);
        this.debt += this.studyFee.getFee(this.debt);

    }

    /**
     * 
     * @param idLoan
     * @param initialCapital
     * @param interestRate
     * @param paymentPeriod
     * @param amortizationTime
     * @param account
     * @throws LoanException
     */
    public Loan(Handler idLoan, double initialCapital,
            InterestRate interestRate, PaymentPeriod paymentPeriod,
            int amortizationTime, Account account, Client client,
            String description) throws LoanException {
        this(idLoan, initialCapital, interestRate.getInterestRate(),
                paymentPeriod, amortizationTime, account, client, description);

    }

    /**
     * This method can forward the actual date
     * 
     * @param date
     * @param paymentPeriod
     * @return The new simulated date
     */
    /* TODO puede ser cambiado a otra clase utils */
    public Date forwardDate(Date date, PaymentPeriod paymentPeriod) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);// reset the parameter

        int month = calendar.get(Calendar.MONTH) + paymentPeriod.getTime();
        int year = calendar.get(Calendar.YEAR);
        final int day = calendar.get(Calendar.DATE);
        if (month >= 12) {
            year++;
        }
        month = month % 12;

        calendar.set(year, month, day);

        return calendar.getTime();
    }

    /**
     * This method returns an ArrayList with all fees of the loan
     * 
     * @return payments The arraylist with fees
     */
    public List<ScheduledPayment> calcPayments() {
        this.payments = this.strategy.doCalculationOfPayments();
        return this.payments;
    }

    /**
     * This method allows to know what is the price that one person must pay if
     * he decide cancel the loan. This amount of money is the total amount of
     * money for cancel the loan
     * 
     * @return double with amount of money to pay
     * @throws LoanException
     */
    public double cancelLoan() throws LoanException {
        final StringBuilder msgException = new StringBuilder();
        double feeCancel = 0;

        feeCancel = this.cancelCommission.getFee(this.debt);

        // We carry out the transaction to discount the money from the account
        // of the customer
        try {
            if (!(this.account.getBalance() < this.debt)) {
                final Transaction transactionCharge = new GenericTransaction(
                        -(feeCancel+this.debt), new Date(Time.getInstance().getTime()),
                        "cancel loan");

                transactionCharge.setEffectiveDate(new Date(Time.getInstance()
                        .getTime()));
                this.account.doTransaction(transactionCharge);
            } else {
                msgException.append("not enough money");
            }
        } catch (final TransactionException transactionException) {
            msgException.append("Transaction error.\n");
            msgException.append(transactionException.getMessage());
        }

        if (msgException.length() > 0) {
            throw new LoanException(msgException.toString());
        }

        // If the exception is not launched the payment is made and will zero
        // debt.
        this.debt = 0;

        return feeCancel;

    }

    /**
     * Method used to paying the fee if payment is not made
     * 
     * @param index
     *            indicates the number of payments to be amortized
     */
    @Deprecated
    public void paid(int index) { // Este metodo se borrara asiq no lo useis
        if ((index >= 0) && (index < this.payments.size())) {
            final ScheduledPayment payment = this.payments.get(index);
            if (!payment.isPaid()) {
                this.debt -= payment.getAmortization();
                payment.setPaid(true);
            }
        }
    }

    /**
     * This method i for paid with a ScheduledPayment
     * 
     * @param payment
     * @throws LoanException
     */
    private void paid(ScheduledPayment payment) throws LoanException {
        final StringBuffer exceptionMessage = new StringBuffer();
        try {
            final Transaction transaction = new GenericTransaction(
                   - payment.getImportOfTerm(), new Date(Time.getInstance()
                            .getTime()), "payment");

            transaction
                    .setEffectiveDate(new Date(Time.getInstance().getTime()));

            this.account.doTransaction(transaction);

        } catch (final TransactionException e) {
            exceptionMessage.append("Transaction error.\n");
        }

        // if the transaction has not errors and was made successfully
        if (exceptionMessage.length() == 0) {
            // we subtract the quantity to amortize of the debt
            this.debt -= payment.getAmortization();
            payment.setPaid(true);
        } else {
            throw new LoanException(
                    "The payment has not been made successfully.");
        }
    }

    /**
     * This method paid the nextsheluedPaymen,udpdate the paids and add in the
     * taskList the task
     * 
     * @throws LoanException
     */

    public void paid() throws LoanException {
        this.paid(this.nextPayment);
        this.setAmortizationTime(this.getAmortizationTime() - 1);
        final TaskList taskList = TaskList.getInstance();
        final Task task = new Task(this.forwardDate(
                this.nextPayment.getExpiration(), this.paymentPeriod),
                new PaidLoanCommand(new CommandHandler(), this));
        taskList.addTask(task);
        this.update();
    }

    /**
     * Method used to pay the payment by an id handler
     * 
     * @param handlerId
     *            is the handler of the payment
     * @throws LoanException
     */
    public void paid(Handler handlerId) throws LoanException {

        // we look for the payment
        boolean found = false;
        ScheduledPayment payment = null;
        for (int i = 0; (i < this.payments.size()) && !found; i++) {
            payment = this.payments.get(i);
            if (payment.getId().compareTo(handlerId) == 0) {
                found = true;
            }
        }

        if ((payment != null) && !payment.isPaid()) {

            this.paid(payment);

        }
    }

    /**
     * Method that applies the delayed interest if some fee has not been payed
     * in time
     */
    public void delayedPayment() {
        final boolean isPaid = this.isNotPaid();
        if (isPaid && (this.debt > 0)) {
            this.debt = this.debt
                    + (this.debt * this.delayedPaymentFee.getFee(this.debt));
        }

    }

    /**
     * Method that is necesary when the interest change
     */
    @Override
    public void update() {
        this.payments = this.strategy.doCalculationOfPayments();
        this.loanHistory.addAllPayments(this.payments);
    }

    /**
     * /** Method that allows to amortize some money before the loan finish
     * 
     * @param quantity
     *            Amount of money that you want to amortize
     * @return (double) amount of money that you have to pay for amortize
     * @throws LoanException
     */

    public double amortize(double quantity) throws LoanException {
        final StringBuffer exceptionMessage = new StringBuffer();
        final double comission = this.amortizedFee.getFee(quantity);

        if (!(quantity <= this.debt)) {
            exceptionMessage
                    .append("The money to amortize is more than the debt!");
        }

        if (exceptionMessage.length() > 0) {
            throw new LoanException(exceptionMessage.toString());
        }

        // We carry out the transaction to discount the money from the account
        // of the customer.
        try {
            if (!(this.account.getBalance() < this.debt)) {
                final Transaction transactionCharge = new GenericTransaction(
                       -( quantity + comission), new Date(Time.getInstance().getTime()),
                        "liquidate a quantity");
                transactionCharge.setEffectiveDate(new Date(Time.getInstance()
                        .getTime()));
                this.account.doTransaction(transactionCharge);
            } else {
                exceptionMessage.append("not enough money");
            }
        } catch (final TransactionException transactionException) {
            exceptionMessage.append("Transaction error.\n");
            exceptionMessage.append(transactionException.getMessage());
        }

        // If the transaction is unsuccessful we launched the exception.
        if (exceptionMessage.length() > 0) {
            throw new LoanException(exceptionMessage.toString());
        }

        // Si la transaccion se realizo con exito descontamos el dinero de la
        // deuda
        this.debt += this.amortizedFee.getFee(this.debt);
        this.debt -= quantity;

        this.setAmortized(this.initialCapital - this.debt);
        this.update();

        return comission;
    }

    public PaymentPeriod getPaymentPeriod() {
        return this.paymentPeriod;
    }

    public void setPaymentPeriod(PaymentPeriod paymentPeriod) {
        this.paymentPeriod = paymentPeriod;
    }

    public double getInterest() {
        return this.interest;
    }

    public void setInterest(double interest) {
        this.interest = interest;
    }

    public int getAmortizationTime() {
        return this.amortizationTime;
    }

    public void setAmortizationTime(int amortizationTime) {
        this.amortizationTime = amortizationTime;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmountOfMoney() {
        return this.initialCapital;
    }

    public void setStrategy(StrategyLoan strategy) {
        this.strategy = strategy;
        this.update();
    }

    @Override
    public Handler getId() {
        return this.idLoan;
    }

    public void setId(Handler idLoan) {
        this.idLoan = idLoan;
    }

    public List<ScheduledPayment> getPayments() {
        return this.payments;
    }

    /**
     * Method that returns true if any month has not been paid.
     * 
     * @return true if it has not paid any month, false if all were paid
     */
    public boolean isNotPaid() {
        boolean isNotPaid = false;

        for (int i = 0; (i < this.payments.size()) && !isNotPaid; i++) {
            final ScheduledPayment payment = this.payments.get(i);
            if (!payment.isPaid()) {
                isNotPaid = true;
            }
        }

        return isNotPaid;
    }

    public LoanIteratorDates iterator(Date startDate, Date endDate) {
        return new LoanIteratorDates(this.payments, startDate, endDate);
    }

    public void setDebt(double debt) {
        this.debt = debt;
        this.update();
    }

    public double getDebt() {
        return this.debt;
    }

    public StrategyLoan getStrategy() {
        return this.strategy;
    }

    public double getAmortized() {
        return this.amortized;
    }

    public void setAmountOfMoney(double amountOfMoney) {
        this.initialCapital = amountOfMoney;
    }

    public LoanIterator iterator() {
        return new LoanIterator(this.payments);
    }

 

    // metodo de pago de cantidades diferentes a la mensual calculada
    public void makeAbnormalPayment(double amount) {
        // excepciones
        // pongo la condicion de que el pago se haga entre los meses indicados?
        if ((amount < this.debt) && (amount > 0)) {
            final ScheduledPayment hesGonnaPay = this.payments
                    .get(this.arrayListIndex);

            double interest = 0;
            double amortized = 0;
            double totalLoan = this.debt;
            double totalCapital = this.debt;

            interest = amount * this.interest;
            amortized = amount - interest;
            if (totalLoan > amount) {
                totalLoan -= amount;
            } else {
                totalLoan = 0;
            }
            totalCapital = this.round(totalLoan, 100);
            amortized = this.round(amortized, 100);
            interest = this.round(interest, 100);
            hesGonnaPay.setAmortization(amortized);
            hesGonnaPay.setInterests(interest);
            hesGonnaPay.setOutstandingCapital(totalCapital);
            // Cambiar
            hesGonnaPay.setPaymentDate(new Date());

            hesGonnaPay.setPaid(true);
            // hesGonnaPay.setOutstandingCapital(outstandingCapital);
            this.debt = this.debt - amount;
            hesGonnaPay.setImportOfTerm(amount);
        }

        // borro todos los elementos en adelante porque hay que recalcular
        final int auxSize = this.payments.size();
        for (int auxInd = this.arrayListIndex + 1; auxInd < auxSize; auxInd++) {
            this.payments.remove(this.payments.get(this.payments.size() - 1));
        }
        // se recalcula todo
        this.strategy.doCalculationOfPayments();
        // actualizo el indice del arrayList
        ++this.arrayListIndex;

    }

    /**
     * Method used to round some numbers for the payments. This method allow us
     * to be more exactly in the calcs
     * 
     * @param num
     * @param factor
     * @return num Number rounded
     */

    public double round(double num, int factor) {
        num = num * factor;
        num = Math.round(num);
        num = num / factor;
        return num;
    }

    public double getPeriodFee() {
        return this.periodFee;
    }

    public void setAmortized(double amortized) {
        this.amortized = amortized;
    }

    public Account getLinkedAccount() {
        return this.account;
    }

    public void setCancelCommission(FeeStrategy commission) {
        this.cancelCommission = commission;
    }

    public void setStudyCommission(FeeStrategy commission) {
        this.studyFee = commission;
        this.debt += this.studyFee.getFee(this.debt);
        this.update();
    }

    public void setOpenningCommission(FeeStrategy commission) {
        this.openningFee = commission;
        this.debt += this.openningFee.getFee(this.debt);
        this.update();
    }

    public void setDelayedPaymentFee(FeeStrategy commission) {
        this.delayedPaymentFee = commission;
        this.debt = this.delayedPaymentFee.getFee(this.debt);
    }

    public void setAmortizedCommission(FeeStrategy commission) {
        this.amortizedFee = commission;
    }

    public void setModifyCommission(FeeStrategy commission) {
        this.modifyFee = commission;
    }

    public double getInterestOfBank() {
        return this.interestOfBank;
    }

    public void setInterestOfBank(double interestOfBank) {
        this.interestOfBank = interestOfBank;
    }

    public Date getCreatinngDate() {
        return this.creatinngDate;
    }

    public void setCreatinngDate(Date creatinngDate) {
        this.creatinngDate = creatinngDate;
    }

    public ScheduledPayment getNextPayment() {
        return this.nextPayment;
    }

    public void setNextPayment(ScheduledPayment nextPayment) {
        this.nextPayment = nextPayment;
    }
    public Account getAccount() {
        return account;
    }

}

