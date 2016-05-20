/*
 * Copyright (c) 2010 Mathew Hall, University of Sheffield.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * Neither the name of the University of Sheffield nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package search.fitnessfunctions;

import com.sun.org.apache.bcel.internal.generic.INSTANCEOF;
import gpinterpreter.stack.StackInstruction;
import gpinterpreter.stack.StackInterpreter;
import gpinterpreter.vector.VecInterpreter;
import gpinterpreter.vector.VecInstruction;

import java.util.ArrayList;

import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.IChromosome;
import org.jgap.impl.FixedBinaryGene;
import org.jgap.impl.IntegerGene;

import primitives.cluster.ClusterHead;
import search.genes.*;

public abstract class TreeFitnessFunction extends FitnessFunction {

	/**
	 *
	 */
	private static final long serialVersionUID = -5219003121816689020L;
	private ClusterHead tree;

	public void setTree(ClusterHead tree) {
		this.tree = tree;
	}
	private int evaluations = 0;
	private int fitnessBudget = -1;

	public void setFitnessBudget(int fb) {
		fitnessBudget = fb;
	}

	public boolean budgetHit() {
		return evaluations > fitnessBudget;
	}
	private ClusterHead best = null;

	public ClusterHead getFittest() {
		return best;
	}
	double bestFitness = -1;

	public double getBestFitness() {
		return bestFitness;
	}
	private int evalsToBest = -1;

	public int getEvalsToBest() {
		return evalsToBest;
	}
	
	protected boolean punishesWastage(){
		return false;
	}

	public double scaleWastage(double fitness, int amount){
		return fitness;
	}
	
	public double evaluate(IChromosome a_subject) {

		

		if(a_subject.size() == 0) return 0.0;
		
		Gene first = a_subject.getGene(0);
		
		//if first gene is InstructionGene, then use vector stuff:
		if( first instanceof InstructionGene){
		
			ArrayList<InstructionGene> prog = new ArrayList<InstructionGene>();
			for (int i = 0; i < a_subject.size(); i++) {

				prog.add((InstructionGene) a_subject.getGene(i));
			}
			
			return evaluateInstructionGene(prog);

		}else{
			ArrayList<StackInstructionGene> prog = new ArrayList<StackInstructionGene>();
			
			for(int i= 0; i< a_subject.size(); i++){
				prog.add((StackInstructionGene) a_subject.getGene(i));
			}
			
			return evaluateStackGene(prog);
			
			
		}
		


	}

	/**
	 * Evaluates a program represented as a list of VecInstruction objects.
	 *
	 * @param program the instructions that will be interpreted to produce a
	 * ClusterHead to be evaluated by this fitness function.
	 * @param IGNORED not used, exists to circumvent problems with type erasure.
	 * @return
	 */
	public double evaluateVectInst(ArrayList<VecInstruction> program, boolean IGNORED) {

		VecInterpreter interp = new VecInterpreter(program, tree.deepCopy());
		ClusterHead evaluated = interp.run();
                ClusterHead saved = evaluated.deepCopy();
		evaluations++;
		double fitness = evaluate(evaluated);
		if (!budgetHit()) {
			evalsToBest = evaluations;

			if (fitness >= bestFitness) {
				bestFitness = fitness;
				best = saved;
				evalsToBest = evaluations;
			}
		}
		return fitness;

	}

	public double evaluateInstructionGene(ArrayList<InstructionGene> program) {

		evaluations++;
		VecInterpreter interp = new VecInterpreter(new ArrayList<VecInstruction>(),
				tree.deepCopy());

		boolean isIntegerGene = false;
		boolean isSymbolInstructionGene = false;
		boolean isBitFieldInstructionGene = false;
		boolean isFixedBinaryGene = false;

		Gene g = (Gene) (program.get(0));

		int size = tree.getSize();

		VecInstruction inst = null;
		try {
			IntegerGene ig = (IntegerGene) g;
			inst = IntegerInstructionGene.getInstruction(ig.intValue(),
					size);
			isIntegerGene = true;

		} catch (ClassCastException ex) {
			try {
				SymbolInstructionGene ig = (SymbolInstructionGene) g;
				inst = (VecInstruction) (ig.getAllele());
				isSymbolInstructionGene = true;
			} catch (ClassCastException exc) {
				try {
					BitFieldInstructionGene bfig = (BitFieldInstructionGene) g;
					inst = bfig.getInstruction();
					isBitFieldInstructionGene = true;
				} catch (Exception exce) {
					try {
						FixedBinaryGene fbg = (FixedBinaryGene) g;
						inst = BitFieldInstructionGene.getInstruction(
								BitFieldInstructionGene.asInteger(fbg),
								size);
						isFixedBinaryGene = true;
					} catch (Exception excep) {
						throw new RuntimeException(
								"State error: Chromosome populated with unknown Gene type: "
								+ g.getClass().getName());
					}
				}

			}
		}

		for (int i = 0; i < program.size(); i++) {

			g = (InstructionGene) (program.get(i));
			if (isIntegerGene) {
				IntegerGene ig = (IntegerGene) g;
				inst = IntegerInstructionGene.getInstruction(ig.intValue(),
						size);
			} else if (isSymbolInstructionGene) {
				SymbolInstructionGene ig = (SymbolInstructionGene) g;
				inst = (VecInstruction) (ig.getAllele());

			} else if (isFixedBinaryGene) {
				FixedBinaryGene fbg = (FixedBinaryGene) g;
				inst = BitFieldInstructionGene.getInstruction(
						BitFieldInstructionGene.asInteger(fbg), size);
			} else if (isBitFieldInstructionGene) {
				BitFieldInstructionGene bfig = (BitFieldInstructionGene) g;
				inst = bfig.getInstruction();
			}
			interp.execute(inst);
		}


		ClusterHead evaluated = interp.getTree();
                ClusterHead saved = evaluated.deepCopy();
		double fitness = evaluate(evaluated);

		if (!budgetHit()) {
			evalsToBest = evaluations;

			if (fitness >= bestFitness) {
				bestFitness = fitness;
				best = saved;
				evalsToBest = evaluations;
			}
		}

		return fitness;

	}

	public double evaluateStackGene(ArrayList<StackInstructionGene> program){
		evaluations++;
		
		ArrayList<StackInstruction> p = new ArrayList<StackInstruction>();
		
		for(int i =0; i<program.size(); i++){
			p.add(program.get(i).getInstruction());
		}
		
		
		StackInterpreter interp = new StackInterpreter(p, tree.deepCopy());

		
		ClusterHead evaluated = interp.run();
                ClusterHead saved = evaluated.deepCopy();
		double fitness = evaluate(evaluated);
		if(punishesWastage()){
			fitness=scaleWastage(fitness, interp.getWastage());
		}
		
		if (!budgetHit()) {
			evalsToBest = evaluations;

			if (fitness >= bestFitness) {
				bestFitness = fitness;
				best = saved;
				evalsToBest = evaluations;
			}
		}
		
		return fitness;
		
	}
	
	public abstract double evaluate(ClusterHead result);

	/**
	 * @return true if the fitness function requires clusters to be subsumed
	 * (with EdgeSubsumptionTransformer)
	 *
	 */
	public static Boolean subsumes() {
		return false;
	}

	public int getFitnessEvaluations() {
		return evaluations;
	}
}

