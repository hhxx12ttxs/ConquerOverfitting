/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.snips.pml;

/**
 *
 * @author rhindi
 */
public class NormalizationFactors {

    public float[] inputShift;
    public float[] inputDiv;
    public float outputShift = 0;
    public float outputDiv = 0;

    public NormalizationFactors(){

    }

    public NormalizationFactors(DataSet trainingSet){
        this.initializeNormalizationFactors(trainingSet);
    }

    //Get factors from training data. This is done once from the raw data.
    private void initializeNormalizationFactors(DataSet trainingSet) {

        //Set up normalization arrays
        float[] inputMax = new float[trainingSet.inputs[0].features];
        float[] inputMin = new float[trainingSet.inputs[0].features];
        float[] inputAvg = new float[trainingSet.inputs[0].features];
        float[] inputStd = new float[trainingSet.inputs[0].features];

        for(int i=0; i<inputMax.length; i++){
            inputMax[i] = Float.NEGATIVE_INFINITY;
            inputMin[i] = Float.POSITIVE_INFINITY;
        }

        float outputMax = Float.NEGATIVE_INFINITY;
        float outputMin = Float.POSITIVE_INFINITY;
        float outputAvg = 0;
        float outputStd = 0;

        inputDiv = new float[inputMax.length];
        inputShift = new float[inputMax.length];

        //Iterate through the trainingset to find range of values
        for(int i=0; i<trainingSet.inputs.length; i++){

            for(int j=0; j<trainingSet.inputs[i].features; j++){

                float in = trainingSet.inputs[i].values[j];

                if(in < inputMin[j]){
                    inputMin[j] = in;
                }

                if(in > inputMax[j]){
                    inputMax[j] = in;
                }

                inputAvg[j] += in;
                inputStd[j] += in*in;
            }


            float out = trainingSet.outputs[i];

            if(out < outputMin){
                outputMin = out;
            }

            if(out > outputMax){
                outputMax = out;
            }

            outputAvg += out;
            outputStd += out*out;
        }


        //Set output factors
        outputAvg /= (float)trainingSet.elements;
        outputStd = (float)Math.sqrt(outputStd/((float)trainingSet.elements) - outputAvg*outputAvg);

        //only normalize if min < 0 || max > 1 for specific input. Some features such as probabilities are already normalized!
        if(outputMin < 0 || outputMax > 1){
            outputShift = outputMin;
            outputDiv = (outputMax - outputMin);
        }
        else{
            outputShift = 0;
            outputDiv = 1;
        }


        //Set input factors
        for(int i=0; i<inputMax.length; i++){

            inputAvg[i] /= (float)trainingSet.elements;
            inputStd[i]  = (float)Math.sqrt(inputStd[i]/((float)trainingSet.elements) - inputAvg[i]*inputAvg[i]);

            if(inputMin[i] < 0 || inputMax[i] > 1){
                inputShift[i] = inputMin[i];
                inputDiv[i] = (inputMax[i] - inputMin[i]);
                System.out.println("Normalized feature " + i);
            }
            else{
                inputShift[i] = 0;
                inputDiv[i] = 1;
            }
        }
    }

    //Normalize one input according to factors
    public final Input normalizeInput(Input input){

        /*
         * Normalize by taking the min and max values +- 1 std
         * Shift left by (min - std), and divide by (max - min + std)
         *
        */

        Input normalizedInput = new Input(input);

        for(int i=0; i<input.features; i++){

            normalizedInput.values[i] = Math.min(Math.max((input.values[i] - inputShift[i]) / inputDiv[i], 0), 1);

        }

        return normalizedInput;
    }

    //Denormalize the prediction, and convert back to its original value range
    public final Input denormalizeInput(Input normalizedInput){

        Input input = new Input(normalizedInput);

        for(int i=0; i<normalizedInput.features; i++){
            input.values[i] = normalizedInput.values[i] * inputDiv[i] + inputShift[i];
        }

        return input;
    }


    //Normalize one input according to factors
    public final float normalizeOutput(float output){
        return Math.min(Math.max((output - outputShift) / outputDiv, 0), 1);
    }

    //Denormalize the prediction, and convert back to its original value range
    public final float denormalizeOutput(float normalizedOutput){

        return normalizedOutput * outputDiv + outputShift;
    }

    public final float[] denormalizeOutputs(float[] outputs){
        float[] denormed = new float[outputs.length];
        for(int i=0; i<outputs.length; i++){
            denormed[i] = denormalizeOutput(outputs[i]);
        }

        return denormed;
    }

    public final String serialize(){

        StringBuilder sb = new StringBuilder();

        sb.append(inputDiv.length);
        sb.append(Constants.serializationSeparator);

        for(int i=0; i<inputDiv.length; i++){
            sb.append(inputDiv[i]);
            sb.append(Constants.serializationSeparator);
        }

        for(int i=0; i<inputShift.length; i++){
            sb.append(inputShift[i]);
            sb.append(Constants.serializationSeparator);
        }

        sb.append(outputDiv);
        sb.append(Constants.serializationSeparator);

        sb.append(outputShift);
        sb.append(Constants.serializationSeparator);

        return sb.toString();
    }

    public final int deserialize(String[] ss, int startPos){

        inputDiv = new float[Integer.parseInt(ss[startPos++])];
        inputShift = new float[inputDiv.length];

        for(int i=0; i<inputDiv.length; i++){
            inputDiv[i] = Float.parseFloat(ss[startPos++]);
        }

        for(int i=0; i<inputShift.length; i++){
            inputShift[i] = Float.parseFloat(ss[startPos++]);
        }

        outputDiv = Float.parseFloat(ss[startPos++]);
        outputShift = Float.parseFloat(ss[startPos++]);

        return startPos;
    }

}

