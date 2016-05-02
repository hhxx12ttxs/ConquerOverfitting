/**
 * @author Zhiqiang Ren
 * date: Feb. 4th. 2012
 *
 * @author Jeffrey Crowell
 * date: feb 14 2012
 *
 */
package aipackage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * 
 * Neural Net DataStructure for supervised learning
 * 
 * @author Jeff Crowell
 *
 */
public class NeuralNet
{
    /*
     * layers: array of the number of nodes in each layer (input and output are also layers)
     *
     * all indices start from 0
     */
    public NeuralNet(int [] layers) throws RuntimeException
    {
        if (layers.length < 2)
        {
            throw new RuntimeException("The NeuralNet must have at least two layers.");
        }
        m_layers = new ArrayList<List<Node>>(layers.length);

        for (int i = 0; i < layers.length; ++i)
        {
            List<Node> layer = new ArrayList<Node>(layers[i]);
            for (int k = 0; k < layers[i]; ++k)
            {
                layer.add(new Node(i, k, false));
            }
            m_layers.add(layer);
        }
    }

    /*
     * fully connect all the nodes on each layer
     */
    public void connectAll()
    {
        Random generator = new Random();
        Iterator<List<Node>> iter = m_layers.iterator();

        List<Node> pre_layer = iter.next();
        while (iter.hasNext())
        {
            List<Node> cur_layer = iter.next();

            for (int i = 0; i < pre_layer.size(); ++i)
            {
                for (int j = 0; j < cur_layer.size(); ++j)
                {
                    addConnection(pre_layer, i, cur_layer, j, generator.nextDouble());
                }
            }
            for (Node node: cur_layer)
            {
                addThreshold(node, generator.nextDouble());
            }
            pre_layer = cur_layer;
        }
    }

    public void connectTest()
    {
        addConnection(0, 0, 1, 0, 0.2);
        addConnection(0, 1, 1, 0, 0.3);
        addConnection(0, 2, 1, 0, 0.4);
        addConnection(0, 3, 1, 1, 0.6);
        addConnection(0, 4, 1, 1, 0.7);
        addConnection(0, 5, 1, 1, 0.8);

        addConnection(1, 0, 2, 0, 1.0);
        addConnection(1, 1, 2, 0, 1.1);

        addThreshold(1, 0, 0.1);
        addThreshold(1, 1, 0.5);
        addThreshold(2, 0, 0.9);
    }

    void addConnection(int from_layer, int from_pos, int to_layer, int to_pos, double weight)
    {
        List<Node> layer_f = m_layers.get(from_layer);
        List<Node> layer_t = m_layers.get(to_layer);
        addConnection(layer_f, from_pos, layer_t, to_pos, weight);
    }

    void addConnection(List<Node> layer_f, int from_pos, List<Node> layer_t, int to_pos, double weight)
    {
        Node from_node = layer_f.get(from_pos);
        Node to_node = layer_t.get(to_pos);
        addConnection(from_node, to_node, weight);
    }

    void addConnection(Node from_node, Node to_node, double weight)
    {
        Connection con = new Connection(from_node, to_node, weight);
        from_node.addOutputConnection(con);
        to_node.addInputConnection(con);
    }

    // add a threshold to certain node
    void addThreshold(int layer, int pos, double weight)
    {
        List<Node> layer_i = m_layers.get(layer);
        Node node = layer_i.get(pos);
        addThreshold(node, weight);
    }

    void addThreshold(Node node, double weight)
    {
        Node thrd = new Node(node.getLayer(), node.getPos(), true);
        thrd.setOutput(-1);

        Connection con = new Connection(thrd, node, weight);

        thrd.addOutputConnection(con);
        node.addInputConnection(con);
    }

    // r: rate parameter
    public void train(double [][] inputvs, double [][] outputvs, double r) throws RuntimeException
    {
        //train on one of the data sets at a time.
        //we have many pairs of in/out data, so execute training for each of them
        for(int ii = 0; ii< inputvs.length; ii++)
        {
            trainDS(inputvs[ii], outputvs[ii], r);
        }

    }

    private void setInput(double[] input)
    {
        //set the input for the first layer to be the input array data set
        for(int ii = 0; ii<this.m_layers.get(0).size(); ii++)
        {
            this.m_layers.get(0).get(ii).setInput(input[ii]);
            this.m_layers.get(0).get(ii).setOutput(input[ii]*this.m_layers.get(0).get(ii).f(input[ii]));
        }
    }

    private double getError(double oi, double oj, double r, double Bj)
    {
        //calculate the change needed in the weighting
        return r*oj*oi*(1-oj)*Bj;
    }

    private void trainDS(double[] input, double[] output, double r)
    {
        //so, ok, let's do some backprop on _one_ input and output data set!

        //target, the value we train against
        double target;
        //actual, the actual value with the given weights;
        double actual;
        //diff, the difference in the error between actual and target
        setInput(input); //set up the input array
        GetOutput(); //propagate it through the NN
        int outputLayer = this.m_layers.size()-1; //the index of the output layer
        for(int ii = this.m_layers.size()-1; ii >= 0; ii--) // go through every layer
        {
            for(int jj = 0; jj<(this.m_layers.get(ii)).size(); jj++) //and every node in this layer
            {
                //first check of the Output layer
                if(ii == outputLayer) //ok, we're on the output layer, compute \Beta_{z} for it
                {
                    target = output[jj];
                    actual = this.m_layers.get(ii).get(jj).getOutput();
                    this.m_layers.get(ii).get(jj).setBeta(target-actual);
                }
                else
                {
                    List<Connection> outputConnections = this.m_layers.get(ii).get(jj).getAllOutputConnections();
                    double Bj=0;
                    for(Connection cn: outputConnections)
                    {
                        //get the BJs
                        double wjk = cn.getWeight();
                        double ok = cn.getToNode().getOutput();
                        double Bk = cn.getToNode().getBeta();

                        Bj += (wjk * ok * (1-ok) * Bk);
                    }
                    //now set the beta for the current j
                    this.m_layers.get(ii).get(jj).setBeta(Bj);
                }
            }
        }

        //now, re-weight everything, based on outputs learning rate and the \Beta_{j}
        for(int ii = 0; ii< this.m_layers.size(); ii++)
        {
            for(int jj = 0; jj<this.m_layers.get(ii).size(); jj++)
            {
                List<Connection> outputConnections = this.m_layers.get(ii).get(jj).getAllOutputConnections();
                for(Connection cn: outputConnections)
                {
                    //now compute the weight changes
                    double deltaw;
                    double oi = cn.getFromNode().getOutput();
                    double oj = cn.getToNode().getOutput();
                    double Bj = cn.getToNode().getBeta();
                    deltaw = getError(oi, oj, r, Bj);
                    cn.setWeight(cn.getWeight() + deltaw);
                }
            }
        }
    }

    private double[] GetOutput()
    {
        //propagates the current input through
        int end = this.m_layers.size() - 1;
        int size = this.m_layers.get(end).size();
        double[] outputs = new double[size]; // the output from the neural net
        for(int ii = 1; ii<this.m_layers.size(); ii++) //through every layer
        {
            for(int jj = 0; jj<this.m_layers.get(ii).size(); jj++) //through every node in the layer
            {
                //and set its value back to zero
                //this.m_layers.get(ii).get(jj).setInput(0.0);
                List<Connection> LConn = this.m_layers.get(ii).get(jj).getInputConnection();
                double inputValue = 0;
                for (Connection conn: LConn)
                {
                    Node from = conn.getFromNode();
                    double fromval = from.getOutput();
                    double weight = conn.getWeight();
                    inputValue += (fromval*weight);
                }
                //the output is the weighted sum times the sigmoidal function
                double oput = this.m_layers.get(ii).get(jj).f(inputValue);
                //and set the output
                this.m_layers.get(ii).get(jj).setOutput(oput);
            }
        }
        //copy to the output array and return
        for(int ii = 0; ii< this.m_layers.get(end).size(); ii++)
        {
            outputs[ii] = this.m_layers.get(end).get(ii).getOutput();
        }

        return outputs;
    }

    
    // This method shall change the input and output of each node.
    public double [] evaluate(double [] inputv) throws RuntimeException
    {
        setInput(inputv); //set the input array
        double [] output = GetOutput(); //propagate it through
        return output;
    }
    
    public double error(double [][] inputvs, double [][] outputvs) throws RuntimeException
    {
        if (inputvs.length != outputvs.length)
        {
            throw new RuntimeException("inputvs and outputvs are not of the same length");
        }

        double error = 0;

        for (int i = 0; i < inputvs.length; ++i)
        {
            if (outputvs[i].length != m_layers.get(m_layers.size() - 1).size())
            {
                throw new RuntimeException("incompatible outputs");
            }
            double [] results = evaluate(inputvs[i]);
            for (int j = 0; j < results.length; ++j)
            {
                error += (results[j] - outputvs[i][j]) * (results[j] - outputvs[i][j]);
            }
        }

        error /= inputvs.length;
        error = Math.pow(error, 0.5);

        return error;
    }


    public double errorrate(double [][]inputvs3, double [][]outputvs3)
    {
        double accu = 0;
        for (int i = 0; i < inputvs3.length; ++i)
        {
            double [] inputs3 = inputvs3[i];
            double [] results = evaluate(inputs3);
            double target = outputvs3[i][outputvs3[i].length - 1];
            double ret = results[results.length - 1];
//            System.out.println("target is " + target + ", ret is " + ret);

            if (1.1 - target > 0.5)    // false
            {
                if (ret > 0.5)    // decide to be true
                {
                    ++accu;
                }
            }
            else      // true
            {
                if (ret < 0.5)    // decide to be false
                {
                    ++accu;
                }
            }
        }

        double rate = accu / inputvs3.length;
        System.out.println("error rate is " + accu + "/" + inputvs3.length + " = " + rate);
        return rate;
    }
    
    public int findMax(double[] dataset)
    {
    	int max = 0;
    	for(int ii = 0; ii< dataset.length; ii++)
    	{
    		if(dataset[ii]>dataset[max])
    		{
    			max=ii;
    		}
    	}
    	return max;
    }
    
    //this calculates the error rate of a multi-output system
    //takes into consideration the errors in each of the outputs in the output array
    public double errorRate(double [][]inputvs3, double [][]outputvs3)
    {
    	double accuracy = 0;
    	for(int ii = 0; ii < inputvs3.length; ii++)
    	{
    		double[] input = inputvs3[ii]; //given input
    		double[] eval = evaluate(input); //estimated output by net
    		double[] truth = outputvs3[ii]; //"ground truth"
    		boolean isGood = true;
    		int idx = findMax(eval);
    		double[] thresh = new double[eval.length];
    		for(int ll = 0; ll<thresh.length;ll++)
    		{
    			thresh[ll] = 0.0;
    		}
    		thresh[idx] = 1.0;
    		for(int jj = 0; jj < truth.length; jj++)
    		{
    		
    			// CHECK IF WE ARE CORRECT
    			if( Math.abs(thresh[jj] - truth[jj]) > 0.1)
    			{
    				//If not break;
    				isGood = false;
    			}
    		}
    		
    		if(isGood == true)
    		{
    			accuracy++;
    		}
    	}
        double rate = accuracy / (double) inputvs3.length;
        System.out.println("error rate is " + accuracy + "/" + inputvs3.length + " = " + rate);
        
        // A HIGH ACCURACY IS BETTER THAN A LOW ACCURACY BITCHES
        // so we return 1-rate;
        return 1 - rate;
    }


    private List<List<Node> > m_layers;

}

