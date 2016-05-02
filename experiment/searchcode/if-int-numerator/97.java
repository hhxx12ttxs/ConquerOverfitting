/*
 * Copyright 2010 Brendan Kenny
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package gwt.ns.sample.rationals.client;

import gwt.ns.json.client.Json;
import gwt.ns.webworker.client.IterativeWorkerEntryPoint;

/**
 * This Worker steps through the Calkin-Wilf enumeration of the rationals.
 * Entries are passed back to the Worker's parent context as they are computed.
 * 
 * <p>The sequence is calculated through a naive, iterative version of the
 * hyperbinary recurrence relation. Production systems which need an
 * enumeration of the rational numbers should not rely on this implementation.
 * </p>
 * 
 * @see <a href="http://www.math.upenn.edu/~wilf/website/recounting.pdf">Recounting the Rationals</a>
 */
public class RationalsWorker extends IterativeWorkerEntryPoint {
	// Delay between calls of execute().
	static final int DELAY_MS = 15;
	
	// The index of first entry in rationals sequence to calculate. Higher
	// indices require many more iterations to calculate. Must be non-negative.
	static final int START_INDEX = 10000000;
	
	int denominator;
	int index = START_INDEX;
	int numerator;
	StackInt stack = StackInt.create();
	
	@Override
	public int execute() {
		stack.push(index++);
		numerator = denominator;
		denominator = 0;
		
		// naive computation of index-th denominator in sequence
		// recurrence relation:
		//   h(0) = 1
		//   h(2k + 1) = h(k)
		//   h(2k) = h(k) + h(k - 1)
		while(!stack.isEmpty()) {
			int cur = stack.pop();
			
			if (cur == 0 || cur == 1) {
				// base cases
				denominator++;
				
			} else if (cur % 2 != 0) {
				// odd case
				stack.push((cur - 1) / 2);
				
			} else {
				// even case
				cur /= 2;
				stack.push(cur);
				stack.push(cur - 1);
			}
		}
		
		// entry is complete when numerator is calculated
		if (numerator != 0)
			postRational(numerator, denominator);
		
		return DELAY_MS;
	}

	@Override
	public void onWorkerClose() {
		// nothing to close
	}
	
	@Override
	public void onWorkerLoad() {
		// nothing to initialize
	}
	
	/**
	 * Creates a message containing calculated numerator and denominator and
	 * send to parent context.
	 * 
	 * @param numerator
	 * @param denominator
	 */
	public void postRational(int numerator, int denominator) {
		String data = Json.strigify(RationalNumber.create(numerator, denominator));
		postMessage(data);
	}
}

