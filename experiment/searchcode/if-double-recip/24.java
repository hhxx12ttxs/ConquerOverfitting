package redick;

import redick.analysis.*;
import redick.node.*;

import java.util.*;


class Translation extends DepthFirstAdapter {

    private Map<PExpr,JValue> results = new HashMap<PExpr, JValue>();
    private Map<PDyad,JDyadVerb> dyadVerbs = new HashMap<PDyad,JDyadVerb>();
    private Map<PMonad,JMonadVerb> monadVerbs = new HashMap<PMonad,JMonadVerb>();
    
	private Environment env;

	Translation () {
		env = new Environment();
        
        // hardcoding x as 12.0 in the initial environment
		env.put("x", new JDouble(12.0));
	}

	public void outARepl(ARepl node) {
		System.out.println(results.get(node.getExpr()));
	}
    
	public void outAVariableExpr(AVariableExpr node) {
		String name = node.getIdentifier().getText();

        // return the value of the variable
		results.put(node, env.get(name));
	}
    
	public void outAMinusDyad(AMinusDyad node) { dyadVerbs.put(node, JDyadVerb.MINUS); }
	public void outAPlusDyad(APlusDyad node) { dyadVerbs.put(node, JDyadVerb.PLUS);	}
	public void outATimesDyad(ATimesDyad node) {	dyadVerbs.put(node, JDyadVerb.TIMES);	}
	public void outADivisionDyad(ADivisionDyad node) {	dyadVerbs.put(node, JDyadVerb.DIVISION);	}
	public void outARecipMonad(ARecipMonad node) {	monadVerbs.put(node, JMonadVerb.RECIP);	}
    
	public void outAMonadExpr(AMonadExpr node) {
        switch(monadVerbs.get(node.getMonad())) {
        case RECIP:
            {
                JValue value = results.get(node.getArg());
                if(!(value instanceof JDouble))
                    throw new RuntimeException("invalid argument");
            
                double number = ((JDouble) value).getValue();
            
                results.put(node, new JDouble(1.0/number));
            }
            break;
        default:
            throw new RuntimeException("unhandled case");
        }
	}
    
    public void outADyadExpr(ADyadExpr node) {
        switch(dyadVerbs.get(node.getDyad())) {
        case PLUS:
            {
                JValue leftValue = results.get(node.getLeftArg());
                if(!(leftValue instanceof JDouble))
                    throw new RuntimeException("invalid argument");
                
                JValue rightValue = results.get(node.getRightArg());
                if(!(rightValue instanceof JDouble))
                    throw new RuntimeException("invalid argument");
            
                double leftNumber = ((JDouble) leftValue).getValue();
                double rightNumber = ((JDouble) rightValue).getValue();
            
                results.put(node, new JDouble(leftNumber + rightNumber));
            }
            break;
        default:
            throw new RuntimeException("unhandled case");
        }
    }

    @Override
    public void outANumberExpr(
            ANumberExpr node) {
        results.put(node, new JDouble(Double.parseDouble(node.getNumber().getText())));
    }

    public void inAStringExpr(AStringExpr node) {
		System.out.println( "SL: " + node.getStringLiteral() );
		}

}
