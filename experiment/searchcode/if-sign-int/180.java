/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mvallet.core;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import mvallet.interfaces.ISendTransactionListener;
import mvallet.utils.Utils;

/**
 *
 * @author Miha
 */
public abstract class SpendableWallet<T> extends Wallet<T> {

    private final BigInteger mandatoryFee = BigInteger.valueOf(50000);
    private final BigInteger minimumOutput = BigInteger.valueOf(1000000);
    
    List<ISendTransactionListener> transactionListeners = new ArrayList<>();
    
    /**
     * Get KeyPairs for given addresses. Return null if something goes wrong (decryption fails, etc.)
     * @param addresses
     * @return 
     */
    protected abstract Map<Address, KeyPair> getKeys(List<Address> addresses);

    public void addSendTransactionListener(ISendTransactionListener l) {
        transactionListeners.add(l);
    }
    
    @Deprecated
    public BigInteger getPriority(
            BigInteger amount, 
            Address destinationAddress, 
            Address changeAddress, 
            List<Address> inputAddresses, 
            long currentHeight) {
        ChosenInputs chosenInputs = chooseTxInputs(amount, inputAddresses, BigInteger.ZERO);
        
        if (chosenInputs == null) return null;

        CreatedTransaction ctx = createTransaction(amount, destinationAddress, changeAddress, inputAddresses, BigInteger.ZERO, false);
        if (ctx == null) return null;
        
        String tx = ctx.getRawTx();
        int bytes = tx.length()/2;
        
        BigInteger difficulty = BigInteger.ZERO;
        
        for (TransactionInput input : chosenInputs.getInputs()) {
            difficulty = difficulty.add(
                    input.getValue().multiply(
                        BigInteger.valueOf(currentHeight).subtract(BigInteger.valueOf(input.getHeight()))  
                        )
                    );
        }
        
        difficulty = difficulty.divide(BigInteger.valueOf(bytes));
        
        return difficulty;
    }
    
    public BigInteger getPriority(
            List<TransactionOutput> destinations,
            Address changeAddress, 
            List<Address> inputAddresses, 
            long currentHeight) {
        
        BigInteger amount = BigInteger.ZERO;
        for (TransactionOutput o : destinations) amount = amount.add(o.getAmount());
        
        ChosenInputs chosenInputs = chooseTxInputs(amount, inputAddresses, BigInteger.ZERO);
        
        if (chosenInputs == null) return null;

        CreatedTransaction ctx = createTransaction(destinations, changeAddress, inputAddresses, BigInteger.ZERO, false);
        if (ctx == null) return null;
        
        String tx = ctx.getRawTx();
        int bytes = tx.length()/2;
        
        BigInteger difficulty = BigInteger.ZERO;
        
        for (TransactionInput input : chosenInputs.getInputs()) {
            difficulty = difficulty.add(
                    input.getValue().multiply(
                        BigInteger.valueOf(currentHeight).subtract(BigInteger.valueOf(input.getHeight()))  
                        )
                    );
        }
        
        difficulty = difficulty.divide(BigInteger.valueOf(bytes));
        
        return difficulty;
    }

    @Deprecated
    private CreatedTransaction createTransaction(
            BigInteger amount, 
            Address destinationAddress, 
            Address changeAddress, 
            List<Address> inputAddresses, 
            BigInteger fee, 
            boolean sign) {
        
        // inputs
        ChosenInputs chosenInputs = chooseTxInputs(amount, inputAddresses, fee);
        if (chosenInputs == null) return null;

        // outputs
        List<TransactionOutput> outputs = new ArrayList<>();
        outputs.add(new TransactionOutput(destinationAddress, amount));

        BigInteger change = chosenInputs.getTotal().subtract(chosenInputs.getFee()).subtract(amount);
        if (change.compareTo(BigInteger.ZERO) > 0) {
            outputs.add(new TransactionOutput(changeAddress, change));
        }

        List<TransactionInput> signedInputs;
        if (sign) {
            // keys
            List<Address> addrs = new ArrayList<>();
            for (TransactionInput i : chosenInputs.getInputs()) {
                addrs.add(i.getAddress());
            }
            Map<Address, KeyPair> keys = getKeys(addrs);
            if (keys == null) return null;

            // signed inputs
            signedInputs = signInputs(chosenInputs.getInputs(), keys, outputs);
        }
        else {
            signedInputs = new ArrayList<>();
            for (TransactionInput input : chosenInputs.getInputs()) 
                signedInputs.add(input);
        }
        
        return new CreatedTransaction(
                rawTransaction(signedInputs, outputs),
                signedInputs,
                outputs);
    }
    
    

    private CreatedTransaction createTransaction(
            List<TransactionOutput> destinations,
            Address changeAddress, 
            List<Address> inputAddresses, 
            BigInteger fee, 
            boolean sign) {
        
        BigInteger amount = BigInteger.ZERO;
        for (TransactionOutput o : destinations) amount = amount.add(o.getAmount());
        
        // inputs
        ChosenInputs chosenInputs = chooseTxInputs(amount, inputAddresses, fee);
        if (chosenInputs == null) return null;

        // outputs
        List<TransactionOutput> outputs = new ArrayList<>();
        outputs.addAll(destinations);

        BigInteger change = chosenInputs.getTotal().subtract(chosenInputs.getFee()).subtract(amount);
        if (change.compareTo(BigInteger.ZERO) > 0) {
            outputs.add(new TransactionOutput(changeAddress, change));
        }

        List<TransactionInput> signedInputs;
        if (sign) {
            // keys
            List<Address> addrs = new ArrayList<>();
            for (TransactionInput i : chosenInputs.getInputs()) {
                addrs.add(i.getAddress());
            }
            Map<Address, KeyPair> keys = getKeys(addrs);
            if (keys == null) return null;

            // signed inputs
            signedInputs = signInputs(chosenInputs.getInputs(), keys, outputs);
        }
        else {
            signedInputs = new ArrayList<>();
            for (TransactionInput input : chosenInputs.getInputs()) 
                signedInputs.add(input);
        }
        
        return new CreatedTransaction(
                rawTransaction(signedInputs, outputs),
                signedInputs,
                outputs);
    }

    private List<TransactionInput> signInputs(
            List<TransactionInput> inputs, 
            Map<Address, KeyPair> keys, 
            List<TransactionOutput> outputs) {
        List<TransactionInput> result = new ArrayList<>();

        for (int i = 0; i < inputs.size(); i++) {
            TransactionInput input = inputs.get(i);
            KeyPair keyPair = keys.get(input.getAddress());

            String tx = rawTransaction(inputs, outputs, i);
            byte[] hash = Utils.sha256sha256(Utils.fromHexString(tx));
            byte[] sig = Utils.sign(hash, keyPair.getPrivateKey());

            assert Utils.verify(hash, sig, keyPair.getPublicKey());

            result.add(
                    new TransactionInput(
                    input.getAddress(),
                    input.getValue(),
                    input.getPrevTxHash(),
                    input.getIndex(),
                    input.getHeight(),
                    input.getRawOutputScript(),
                    keyPair.getPublicKey(),
                    sig));
        }

        return result;
    }
    
    private String rawTransaction(
            List<TransactionInput> inputs, 
            List<TransactionOutput> outputs) {
        return rawTransaction(inputs, outputs, -1);
    }

    private String rawTransaction(
            List<TransactionInput> inputs, 
            List<TransactionOutput> outputs, 
            int forSignatureIndex) {
        String s = Utils.intToHex(1, 4);

        s += Utils.intToHex(inputs.size());
        for (int i = 0; i < inputs.size(); i++) {
            TransactionInput input = inputs.get(i);

            s += Utils.reverseHexEncoded(input.getPrevTxHash());
            s += Utils.intToHex(input.getIndex(), 4);

            String script = "";
            if (forSignatureIndex < 0) {
                byte[] inputSig = (input.getSignature() != null) ? input.getSignature() : new byte[72];
                byte[] inputPub = (input.getPublicKey() != null) ? input.getPublicKey() : new byte[65];
                
                byte[] sig = Arrays.copyOf(inputSig, inputSig.length + 1);
                sig[sig.length - 1] = 1;

                script = Utils.intToHex(sig.length);
                script += Utils.toHexString(sig);
                script += Utils.intToHex(inputPub.length);
                script += Utils.toHexString(inputPub);
            } else if (forSignatureIndex == i) {
                script = input.getRawOutputScript();
            }

            s += Utils.intToHex(script.length() / 2);
            s += script;
            s += "ffffffff";
        }

        s += Utils.intToHex(outputs.size());
        for (int j = 0; j < outputs.size(); j++) {
            TransactionOutput output = outputs.get(j);

            s += Utils.intToHex(output.getAmount(), 8);
            String script = "76a9";
            script += "14";
            // use unsafe address conversion as we checked user addresses for validity beforehand
            script += Utils.toHexString(Utils.addressToHash160(output.getAddress()));
            script += "88ac";
            s += Utils.intToHex(script.length() / 2);
            s += script;
        }

        s += Utils.intToHex(0, 4);// s += "\n";
        if (forSignatureIndex >= 0) {
            s += Utils.intToHex(1, 4);
        }
        return s;
    }

    private ChosenInputs chooseTxInputs(
            BigInteger amount, 
            List<Address> addrs, 
            BigInteger fee) {
        BigInteger total = BigInteger.ZERO;
        List<TransactionInput> inputs = new ArrayList<>();
        m:
        for (Address addr : addrs) {
            List<Transaction> txs = super.getTransactions(addr);
            if (txs == null) continue;
            
            for (Transaction tx : txs) {
                if (tx.getRaw_output_script() != null) {
                    BigInteger value = tx.getValue();
                    inputs.add(new TransactionInput(
                            addr,
                            value,
                            tx.getTx_hash(),
                            tx.getIndex(),
                            tx.getHeight(),
                            tx.getRaw_output_script()));
                    total = total.add(value);
                    if (total.compareTo(amount.add(fee)) >= 0) {
                        break m;
                    }
                }
            }
        }

        if (total.compareTo(amount.add(fee)) < 0) {
            for (ISendTransactionListener s : transactionListeners) s.onNotEnoughFunds(amount, fee);
            return null;
        }

        return new ChosenInputs(inputs, total, fee);
    }

    @Deprecated
    protected void sendTx(
            BigInteger amount, 
            Address destinationAddress, 
            Address changeAddress, 
            BigInteger fee) {
        sendTx(amount, destinationAddress, changeAddress, this.addresses(), fee);
    }
    
    protected void sendTx(
            List<TransactionOutput> destinations,
            Address changeAddress, 
            BigInteger fee) {
        sendTx(destinations, changeAddress, this.addresses(), fee);
    }
    
    @Deprecated
    protected void sendTx(
            BigInteger amount, 
            Address destinationAddress, 
            Address changeAddress, 
            List<Address> inputAddresses, 
            BigInteger fee) {
        CreatedTransaction ctx = createTransaction(amount, destinationAddress, changeAddress, inputAddresses, fee, true);
        if (ctx == null) return;
        String tx = ctx.getRawTx();
        sendRawTx(tx);
    }
    
    protected void sendTx(
            List<TransactionOutput> destinations,
            Address changeAddress, 
            List<Address> inputAddresses, 
            BigInteger fee) {
        CreatedTransaction ctx = createTransaction(destinations, changeAddress, inputAddresses, fee, true);
        if (ctx == null) return;
        String tx = ctx.getRawTx();
        sendRawTx(tx);
    }
    
    private void sendRawTx(String tx) {
        if (tx == null) return;
        this.request("blockchain.transaction.broadcast", tx);
    }

    @Deprecated
    public BigInteger recommendedFee(
            BigInteger amount, 
            Address destinationAddress, 
            Address changeAddress, 
            List<Address> inputAddresses) {
        CreatedTransaction tx = createTransaction(amount, destinationAddress, changeAddress, inputAddresses, BigInteger.ZERO, false);
        
        if (tx == null) return null;
        long bytes = tx.getRawTx().length()/2;
        
        if (bytes < 10000) {
            for (TransactionOutput output : tx.getOutputs()) {
                if (output.getAmount().compareTo(minimumOutput) < 0) return this.mandatoryFee;
            }
            
            return BigInteger.ZERO;
        }
        
        return this.mandatoryFee;
    }
    
    public BigInteger recommendedFee(
            List<TransactionOutput> destinations,
            Address changeAddress, 
            List<Address> inputAddresses) {
        CreatedTransaction tx = createTransaction(destinations, changeAddress, inputAddresses, BigInteger.ZERO, false);
        
        if (tx == null) return null;
        long bytes = tx.getRawTx().length()/2;
        
        if (bytes < 10000) {
            for (TransactionOutput output : tx.getOutputs()) {
                if (output.getAmount().compareTo(minimumOutput) < 0) return this.mandatoryFee;
            }
            
            return BigInteger.ZERO;
        }
        
        return this.mandatoryFee;
    }

    @Override
    public void onCallback(Response res) {
        super.onCallback(res);
        
        Logger.getLogger(SpendableWallet.class.getName()).log(Level.INFO, res.toString());
        switch (res.method) {
            case "blockchain.transaction.broadcast": {
                String rawTx = res.params.get(0);
                String hash = res.result.getAsString();
                
                String chash = "";
                try {
                    chash = 
                        Utils.toHexString(
                            Utils.reverse(
                                Utils.sha256sha256(
                                    Utils.fromHexString(rawTx))));
                } catch (Exception e) {
                    
                }
                
                for (int i = 0; i < transactionListeners.size(); i++) {
                    ISendTransactionListener s = (ISendTransactionListener) transactionListeners.get(i);
                    if (hash.equals(chash)) {
                        s.onTransactionSuccess(rawTx, chash);
                    } else {
                        s.onTransactionError(rawTx, chash, hash);
                    }
                }
                
                break;
            }
        }
    }
}

class ChosenInputs {
    private List<TransactionInput> inputs;
    private BigInteger total;
    private BigInteger fee;

    public ChosenInputs(List<TransactionInput> inputs, BigInteger total, BigInteger fee) {
        this.inputs = inputs;
        this.total = total;
        this.fee = fee;
    }

    public List<TransactionInput> getInputs() {
        return inputs;
    }

    public BigInteger getTotal() {
        return total;
    }

    public BigInteger getFee() {
        return fee;
    }
}

class CreatedTransaction {
    private String rawTx;
    private List<TransactionInput> inputs;
    private List<TransactionOutput> outputs;
    
    public CreatedTransaction(String rawTx, List<TransactionInput> inputs, List<TransactionOutput> outputs) {
        this.rawTx = rawTx;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public String getRawTx() {
        return rawTx;
    }

    public List<TransactionInput> getInputs() {
        return inputs;
    }

    public List<TransactionOutput> getOutputs() {
        return outputs;
    }
}
