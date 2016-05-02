package roborally.program.command.advanced;

import roborally.entity.Robot;
import roborally.program.ExecutionState;
import roborally.program.command.Command;
import roborally.program.condition.Condition;
import be.kuleuven.cs.som.annotate.Basic;
import be.kuleuven.cs.som.annotate.Immutable;

/**
 * A class for representing an advanced command: If.
 * 
 * @author 	Niels De Bock, Michael vincken (Computer Science)
 * @version	1.0
 */
public final class If extends Command{
	/**
	 * Initializes this new if-statement with a given condition, command to execute
	 * if the given condition evaluates to true and a command to execute
	 * if the given condition evaluates to false.
	 * 
	 * @param 	condition
	 * 			The condition to evaluate this if with.
	 * @param 	commandTrue
	 * 			The command to execute if the given condition evaluates to true.
	 * @param 	commandFalse
	 * 			The command to execute if the given condition evaluates to false.
	 * @post	This if's condition is equal to the given condition.
	 * @post	This if's command when true is equal to the given commandTrue.
	 * @post	This if's command when false is equal to the given commandFalse.
	 */
	public If(Condition condition, Command commandTrue, Command commandFalse){
		this.condition = condition;
		this.commandTrue = commandTrue;
		this.commandFalse = commandFalse;
	}

	/**
	 * Gets the condition.
	 * 
	 * @return The condition this if will evaluate.
	 */
	@Basic @Immutable
	public Condition getCondition() {
		return condition;
	}

	/**
	 * Condition to evaluate this if with.
	 */
	private final Condition condition;

	/**
	 * Gets the command to execute if this if's condition evaluates to true.
	 * @return	the command to execute if this if's condition evaluates to true.
	 */
	@Basic @Immutable
	public Command getCommandTrue() {
		return commandTrue;
	}

	/**
	 * A variable holding the command to execute if this if's condition evaluates to true.
	 */
	private final Command commandTrue;

	/**
	 * Gets the command to execute if this if's condition evaluates to false.
	 * @return	the command to execute if this if's condition evaluates to false.
	 */
	@Basic @Immutable
	public Command getCommandFalse() {
		return commandFalse;
	}

	/**
	 * A variable holding the command to execute if this if's condition evaluates to false.
	 */
	private final Command commandFalse;

	/**
	 * Executes this if statement.
	 * @effect	This if's condition gets evaluated if there isn't already a
	 * 			previous evaluation. Otherwise, the previous evaluation is used.
	 * @effect	If this if's condition is evaluated to true. It's command
	 * 			to execute when this if's condition is evaluated to true
	 * 			is executed.
	 * @effect	If this if's condition is evaluated to false. It's command
	 * 			to execute when this if's condition is evaluated to false
	 * 			is executed.
	 * @post	If "command true" is executed or "command false" is executed,
	 * 			then this if is considered executed.
	 */
	@Override
	public void execute(Robot robot, ExecutionState executionState) {
		if((condition.isEvaluated() && condition.getPreviousEvaluation()) || 
				(!condition.isEvaluated() && condition.evaluate(robot))){
			
			commandTrue.execute(robot, executionState);
			
			if(commandTrue.isExecuted())
				this.setExecuted(true);
		}
		else{
			commandFalse.execute(robot, executionState);
			
			if(commandFalse.isExecuted())
				this.setExecuted(true);
		}

	}

	@Override
	public void reset(){
		setExecuted(false);
		commandTrue.reset();
		commandFalse.reset();
	}
	
	@Override
	public String toString(){
		return "If " + getCondition().toString() + "\n"
				+ "{" + "\n"
				+ "\t" + getCommandTrue().toString().replaceAll("\n", "\n\t") + "\n"
				+ "}" + "\n" 
				+ "else" + "\n"
				+ "{" + "\n"
				+ "\t" + getCommandFalse().toString().replaceAll("\n", "\n\t") + "\n"
				+ "}";
	}
}

