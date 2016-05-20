
package atm.ui;

import atm.model.Atm;
import atm.model.Account;
import atm.model.Payment;
import atm.model.Transaction;

public class Console {

    private static java.util.Scanner input;
    private static java.io.PrintStream out;
    private static java.io.PrintStream err;

    private static Atm atm;
    private static Account account;

    public static void run(double startingFunds) {
        try { // Permitir acentuação na linha de comandos
            input = new java.util.Scanner(System.in);
            out = new java.io.PrintStream(System.out, true, "UTF-8");
            err = new java.io.PrintStream(System.err, true, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            out = System.out;
            err = System.out;
        }

        try { // Iniciar a aplicação
            atm = new Atm(startingFunds);
            login();
        } catch (RuntimeException e) {
            printErrorMessage(
                "Erro do Sistema. Dirija-se ao multibanco mais próximo.\n"
              + "Diagnóstico: " + e.getMessage()
            );
            System.exit(1);
        }
    }

    /** Autentica o utilizador, fornecido um pin de acesso */
    private static void login() {
        String pin = askString("PIN: ");

        account = atm.getAccountWithPin(pin);
        printLineBreak();

        if (account == null) {
            printErrorMessage("Pin inválido!");
            login();
        }

        userMenu();
    }

    /** Menu de entrada ao utilizador */
    private static void userMenu() {

        printMenu(null,
            "1. Levantamentos",
            "2. Consulta de saldo de conta",
            "3. Consulta de movimentos de conta",
            "4. Pagamento de serviços",
            "5. Depósitos"
        );

        try {
            switch (getOption()) {
                case 1:
                    withdrawMenu();
                    break;

                case 2:
                    printHeader("Saldo de conta");
                    printStatusMessage(
                        "Conta número: " + account.getNumber() + "\n" +
                        "Saldo Actual: " + currency(account.getBalance())
                    );
                    break;

                case 3:
                    printHeader("Movimentos de conta");
                    printStatusMessage(
                        "Saldo Actual: " + currency(account.getBalance())
                    );
                    for (Transaction t : account.getLatestTransactions(10)) {
                        out.println(t);
                    }
                    break;

                case 4:
                    servicesPayment();
                    break;

                case 5:
                    printHeader("Depósito");
                    double dep = askDouble("Montante: ");
                    atm.deposit(dep, account);
                    printStatusMessage("Obrigado pelo seu depósito.");
                    break;

                default:
                    printErrorMessage("Opção inválida");
            }
        } catch (IllegalArgumentException e) {
            printErrorMessage(e.getMessage());
        }

        printLineBreak();
        login();
    }

    /** Menu dos levantamentos */
    public static void withdrawMenu() {

        printMenu("Levantamento",
            "1. 20       2. 50",
            "3. 100      4. 150",
            "5. 200      6. Outros valores"
        );

        try {
            switch (getOption()) {
                case 1: atm.withdraw(20, account); break;
                case 2: atm.withdraw(50, account); break;
                case 3: atm.withdraw(100, account); break;
                case 4: atm.withdraw(150, account); break;
                case 5: atm.withdraw(200, account); break;
                case 6: withdrawOther(); break;
                default:
                    printErrorMessage("Opção inválida");
            }
        } catch (IllegalArgumentException e) {
            printErrorMessage(e.getMessage());
        }

        printLineBreak();
    }

    /** Levantamento de outras importâncias */
    public static void withdrawOther() {
        try{
            int amount = askInt("Montante: ");
            atm.withdraw(amount, account);

        } catch (IllegalArgumentException e) {
            printErrorMessage(e.getMessage());
            withdrawOther();
        }
    }

    /** Menu do pagamento de serviços */
    public static void servicesPayment() {

        printMenu("Pagamento de Serviços",
            "1. Conta de Electricidade",
            "2. Conta da Água",
            "3. Carregamento Telemóvel"
        );

        switch (getOption()) {
            case 1: atm.payElectricityBill(getPayment(), account); break;
            case 2: atm.payWaterBill(getPayment(), account);       break;
            case 3: atm.payPhoneBill(getPhonePayment(), account);  break;
            default:
                printErrorMessage("Opção inválida");
                servicesPayment();
        }

        printStatusMessage("Pagamento efectuado com sucesso");
        printLineBreak();
    }

    /** Retorna um objecto de pagamento de serviço */
    public static Payment getPayment() {
        try {
            String entity    = askString("Entidade: ");
            String reference = askString("Referência: ");
            double amount    = askDouble("Montante: ");

            printLineBreak();
            return new Payment(entity, reference, amount);

        } catch (IllegalArgumentException e) {
            printErrorMessage(e.getMessage());
            return getPayment();
        }
    }

    /** Retorna um objecto de pagamento de serviço, para um telemóvel */
    public static Payment getPhonePayment() {
        try {
            String phone  = askString("Telemóvel: ");
            String entity = atm.getPhoneEntity(phone);
            double amount = getPhonePaymentAmount();

            printLineBreak();
            return new Payment(entity, phone, amount);

        } catch (IllegalArgumentException e) {
            printErrorMessage(e.getMessage());
            return getPhonePayment();
        }
    }

    /** Menu com quantias de carregamento do telemóvel */
    public static double getPhonePaymentAmount() {
        printMenu("Montante",
            "1. 5 euros",
            "2. 10 euros",
            "3. 20 euros"
        );
        switch (getOption()) {
            case 1: return 5;
            case 2: return 15;
            case 3: return 20;
            default:
                printErrorMessage("Opção inválida");
                return getPhonePaymentAmount();
        }
    }

    /* Métodos de ajuda */

    private static String currency(double amount) {
        return String.format("%.2f euros", amount);
    }

    private static int getOption() {
        int option = askInt("\n> ");
        printLineBreak();
        return option;
    }

    private static int askInt(String label) {
        out.print(label);
        try {
            int value = input.nextInt();
            clearInput();
            return value;

        } catch (java.util.InputMismatchException e) {
            printErrorMessage("Número inteiro inválido. Tente de novo.");
            clearInput();
            return askInt(label);
        }
    }

    private static double askDouble(String label) {
        out.print(label);
        try {
            double value = input.nextDouble();
            clearInput();
            return value;

        } catch (java.util.InputMismatchException e) {
            printErrorMessage("Número decimal inválido. Tente de novo.");
            clearInput();
            return askDouble(label);
        }
    }

    private static String askString(String label) {
        out.print(label);
        return input.nextLine();
    }

    private static void clearInput() {
        if (input.hasNextLine()) {
            input.nextLine();
        }
    }

    /* 
     * Métodos de abstracção do output para tornar o código mais legível e
     * facilitar a manutenção ao remover uma dependência ao método de output,
     * assim como manter a interface consistente.
     */

    private static void printHeader(String header) {
        out.println("\n\n| "+header+" |\n");
    }

    private static void printMenu(String header, String ... entries) {
        if (header != null) {
            printHeader(header);
        }
        for (int i = 0; i < entries.length; i++) {
            out.println(entries[i]);
        }
    }

    private static void printStatusMessage(String msg) {
        out.println(msg);
        out.println();
    }

    private static void printErrorMessage(String msg) {
        err.println(msg);
        err.println();
    }

    private static void printLineBreak() {
        out.println();
    }
}
