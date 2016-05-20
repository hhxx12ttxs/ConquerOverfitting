package uk.ac.lkl.migen.system.ai.reasoning;

import java.util.*;

import uk.ac.lkl.migen.system.ai.analysis.*;
import uk.ac.lkl.migen.system.ai.feedback.generator.FeedbackGenerator;
import uk.ac.lkl.migen.system.ai.feedback.strategy.*;
import uk.ac.lkl.migen.system.ai.reasoning.request.*;
import uk.ac.lkl.migen.system.ai.reasoning.trigger.ReasoningTrigger;
import uk.ac.lkl.migen.system.ai.um.*;
import uk.ac.lkl.migen.system.expresser.model.shape.block.BlockShape;
import uk.ac.lkl.migen.system.expresser.ui.uievent.GoalAreaCheckBoxToggledEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.UIEvent;
import uk.ac.lkl.migen.system.task.ConstructionExpressionTask;
import uk.ac.lkl.migen.system.task.goal.Goal;

/**
 * This class implements the task-specific reasoning 
 * for the Intro task.
 * 
 * @author sergut, mavrikis
 *
 */
public class IntroRulesReasoner extends ConstructionExpressionSingleRulesReasoner {
    public IntroRulesReasoner(
	    FeedbackGenerator fbGenerator,
	    ShortTermLearnerModel um,
	    Map<String, Detector> detectors,
	    Map<String, Evaluator> evaluators,
	    Map<String, Verifier> verifiers, 
	    ReasoningTrigger trigger,
	    ConstructionExpressionTask task) {
	super(ReasonerType.INTRODUCTION,fbGenerator,um,detectors,evaluators,verifiers,trigger,task);

	//setGoalAccomplishment(Goal.CONSTRUCT_PATTERN, true);
	um.incrementFeedbackInterventionCount(FeedbackStrategyType.LACK_OF_COLOR_GENERALITY);
	um.incrementFeedbackInterventionCount(FeedbackStrategyType.LACK_OF_COLOR_GENERALITY);
	um.incrementFeedbackInterventionCount(FeedbackStrategyType.LACK_OF_COLOR_GENERALITY);

    }

    /**
     * Evaluates the rules and (maybe) fires a feedback strategy.
     * 
     */
    protected void evaluateRules() {
	// TODO: local rules should probably have preference. Need to think.
	// Anyway, for now super class evaluate rules are empty
	// Additionally, note that a future prioritisation mechanism may rend
	// this consideration obsolete by taking all fb strategies together.
	super.evaluateRules();
	this.evaluateRules(null);
    }

    /**
     * Address an explicit request for feedback (e.g. pressing the 
     * 'HELP' button. 
     * 
     * @param request the request for feedback
     */
    @Override
    public void addressFeedbackRequest(FeedbackRequest request) {
	System.out.println("Feedback request is " + request);

	if ((request == FeedbackRequest.MAKE_PATTERN)) {
	    if (maxGlobalSimilarity() < 0.7) {
		//TODO: check if goal CONSTRUCT_PATTERN is accomplished
		generateFeedbackStrategy(new GoalNotAccomplishedFeedbackStrategy(Goal.CONSTRUCT_PATTERN),request);
		return;
	    } else {
		generateFeedbackStrategy(new PositiveTowardsGoalFeedbackStrategy(Goal.CONSTRUCT_PATTERN),request);
		return;
	    }
	    //TODO: consider how to improve this for the particular task
	    //&& (constructionEvaluator.evaluate() > 0.7)

	}
	//if nothing else call the other rules 
	this.evaluateRules(request);
    }	

    @Override
    public void addressUIEventFeedbackRequest(UIEvent<?> event) {
	if (event instanceof GoalAreaCheckBoxToggledEvent) {
	    GoalAreaCheckBoxToggledEvent gaEvent = (GoalAreaCheckBoxToggledEvent)event;
	    Goal goal = getTask().getGoalsList().get(gaEvent.getCheckBoxIndex());
	    System.out.println("Goal clicked:" + goal.getDescription());
	    //fire explicit feedback strategy referrring to the goal
	    //the feedback layer is responsible to look at um 
	    //and decide what to say 
	    //TODO: goals however need to be unaccomplished (hurray change again!) 
	    generateFeedbackStrategy(new GoalNotAccomplishedFeedbackStrategy(goal), null);
	}
    }



    /**
     * Evaluates the rules and (maybe) fires a feedback strategy.
     * 
     * Can receive a feedback request as a parameter. This happens 
     * when the student explicitly asks for feebdack (e.g. pressing
     * the HELP button). 
     * 
     * When the rules are evaluated without any explicit feedback
     * request, this parameter should be null.
     * 
     * @param request the feedback request
     */
    private void evaluateRules(FeedbackRequest request) {
	System.out.println("Evaluate rules of IntroRulesReasoner was called!");
	System.out.println("Feedback request is " + request);

	evaluateGoalAccomplishmentGoals();
	evaluateFeedbackRules(request);
    }   

    /*
     * GOAL ACCOMPLISHMENT RULES 
     */
    private void evaluateGoalAccomplishmentGoals() {


	/* expression is found (i.e. General (Any) Model is coloured) */
	if (
		//goal is not accomplished before
		(!isGoalAccomplished(Goal.UNMESSABLE_PATTERN_LINKING_TUTORIAL))
		&& constructionLooksLikeSolution()
		//and is coloured generally
		&& !existsNonGeneralColouredShapes()
	) {	    
	    System.out.println("*****OEOEO goal accmploshies******");
	    setGoalAccomplishment(Goal.UNMESSABLE_PATTERN_LINKING_TUTORIAL, true);
	    return;
	}

    }

    /*
     * FEEDBACK STRATEGY RULES	
     */
    private void evaluateFeedbackRules(FeedbackRequest request) {

	/* if My Model is not coloured */
	if (
		constructionLooksLikeSolution()
	) {    
	    //System.out.println("check if there is an uncoloured pattern");
	    BlockShape uncolouredPattern = detectIncorrectColouredShapes(); 
	    //but some part of the pattern is not coloured 
	    if (Detector.NULL_SHAPE_DETECTED != uncolouredPattern) {
		System.out.println("and there is an uncoloured pattern");
		//provide help on colouring the pattern 
		boolean explicit = (request != null) && (request == FeedbackRequest.COLOR_STUDENT_WORLD);
		generateFeedbackStrategy(new IncorrectColorPatternFeedbackStrategy(uncolouredPattern), request);
		return;
	    }
	}


	/* if expression is not found */ 
	if (!isGoalAccomplished(Goal.UNMESSABLE_PATTERN_LINKING_TUTORIAL)) {
	    System.out.println("if UNMESSABLE PATTERN has not been accomplished");
	    if (
		    //and the pattern looks ok
		    constructionLooksLikeSolution()
		    && existsNonGeneralColouredShapes()
	    ) {    
		System.out.println("if student has constructed pattern");
		System.out.println("if there exists a pattern that is not general");

		BlockShape messablePattern = detectNonGeneralColouredShape();
		//but some part of the pattern is not coloured generally
		if (Detector.NULL_SHAPE_DETECTED != messablePattern) {
		    System.out.println("and there is a messable pattern");

		    //provide help on lack of color generality 
		    //check if this is the help requested 
		    boolean explicit = (request != null) && (request == FeedbackRequest.EXPRESSION);
		    //TODO: or ask for generally colouring the pattern

		    //if we have given LACK_OF_COLOR_GENERALITY
		    if ((getFeedbackInterventionCount(FeedbackStrategyType.COLOR_MESS_UP) > 0) 
		      && (getFeedbackInterventionCount(FeedbackStrategyType.REFLECT_ON_CORRECT_MULTIPLICATION) == 0))
		    {
			//we should provide prompt to student 
			//to reflect on their action
			generateFeedbackStrategy(new ReflectOnCorrectMultiplicationFeedbackStrategy(messablePattern), request);
			//TODO: have play with increment 0 etc manipulate things here 
			return;
		    }
		    
		    if (getFeedbackInterventionCount(FeedbackStrategyType.LACK_OF_COLOR_GENERALITY) < 3) {
			generateFeedbackStrategy(new LackOfColorGeneralityFeedbackStrategy(messablePattern), request);
			return;
		    } else if (getFeedbackInterventionCount(FeedbackStrategyType.COLOR_MESS_UP) < 3) {
			generateFeedbackStrategy(new ColorMessUpFeedbackStrategy(messablePattern), request);
			return;
		    } 
		    //else if (getFeedbackInterventionCount(FeedbackStrategyType.LOCK_DOWN) < 3) {
		    //	fbGenerator.generateFeedbackOn(new MessUpFeedbackStrategy(messablePattern), explicit);
		    //	return;
		    //}
		} else {
		    // TODO: what to do if student does not find expression, but pattern is not messable?
		}
	    }
	}

	/*
	 * RULES BECAUSE OF OTHER STATE OF THE SYSTEM 
	 *
	 * i.e. if we reach here it means that all goals have 
	 * been reqached at some point or other and have not been 
	 * retracted. Perhaps we can give some suggestion given the state
	 * of the microworld, (especially if explicit help is requested)
	 * with the assumption that students have done this before 
	 * we have some leverage to provide support 
	 */

    }   


    @SuppressWarnings("unused")
    private boolean foundGeneralExpression() {
	Verifier verifier = getVerifier("CorrectGeneralAllocationVerifier");
	if (verifier.getValue())
	    return true;
	else
	    return false;
    }

    @SuppressWarnings("unused")
    private boolean existsIncorrectColouredShapes() {
	Detector detector = getDetector("IncorrectAllocationDetector");
	if (detector.getValue() == Detector.NULL_SHAPE_DETECTED)
	    return false;
	else
	    return true;	
    }

    private BlockShape detectIncorrectColouredShapes() {
	Detector detector = getDetector("IncorrectAllocationDetector");
	return detector.getValue();
    }

    private boolean existsNonGeneralColouredShapes() {
	Detector detector = getDetector("NonGeneralColouringDetector");
	if (detector.getValue() == Detector.NULL_SHAPE_DETECTED)
	    return false;
	else
	    return true;	
    }

    private BlockShape detectNonGeneralColouredShape() {
	Detector detector = getDetector("NonGeneralColouringDetector");
	return detector.getValue();
    }

    private double maxGlobalSimilarity() {
	Evaluator evaluator = getEvaluator("ConstructionEvaluator");
	return evaluator.getValue();
    }

    private boolean constructionLooksLikeSolution() {
	Verifier verifier = getVerifier("ColorBlindApparentSolutionVerifier");
	return verifier.getValue();
    }

    @SuppressWarnings("unused")
    private boolean isGeneralWorldAnimated() {
	Verifier verifier = getVerifier("AnimatingButtonVerifier");
	return verifier.getValue();	
    }

    @Override
    protected void initialiseEvidence() {
	// TODO Auto-generated method stub
	
    }

}

