package machina;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;

import machina.exp.WishExp;
import machina.frame.EvalFrame;

/**
 * Singleton
 */
public class Evaluator implements IObservable<IExp> {
	
	private static Evaluator instance = new Evaluator();
	
	public static Evaluator getInstance() {
		return instance;
	}
	
	private long step = 0L;
	private final Observable<IExp> observable = new Observable<IExp>(null);
	private Queue<Callable<Boolean>> queue = new LinkedList<Callable<Boolean>>();
	private Queue<Callable<Boolean>> dynamicQueue = new LinkedList<Callable<Boolean>>();

	private Evaluator() {}
	
	public void force(EvalFrame frame) {
		IExp result = force(frame.getCtx(), frame.getExp());
		frame.setResult(result);
	}
	
	public IExp force(EvalCtx ctx, IExp exp) {
		
		while (!exp.isNormalForm()) {
			// Step each thing in the queue.  This could be done in parallel.
			Queue<Callable<Boolean>> q2 = new LinkedList<Callable<Boolean>>();
			for (Callable<Boolean> work : queue) {
				try {
					boolean keep = work.call();
					if (keep) q2.add(work);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			
			// Eval our main exp.  This could add stuff to the queue.
			IExp e2 = exp.evalStep(ctx);
			exp = e2;
			
			// Flip queues.
			queue = q2;
			queue.addAll(dynamicQueue);
			dynamicQueue.clear();
			
			// Track how many steps we've taken.
			step++;

			System.out.println("step: " + step + " exp: " + exp + "  q:" + queue);
			observable.notifyObservers(exp);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
		}
		return exp;
	}
	
	public IExp wish(EvalCtx ctx, IExp exp) {
		if (exp instanceof WishExp) {
			WishExp wish = (WishExp)exp;
			
			// If exp is already a wish that hasn't been granted, just return it.
			if (wish.getResult() == null) return exp;
			
			// Otherwise return its result so that the wisher's wish is granted
			// seamlessly and doesn't even know the wish existed.
			return wish.getResult();
		}
		
		// If exp is already normal, just return it.  You are wishing for something
		// you already have.
		if (exp.isNormalForm()) return exp;
		
		WishExp wish = new WishExp(ctx, exp);
		dynamicQueue.add(new EvalWishGranter(ctx, wish));
		return wish;
	}
	
	public long getStep() {
		return step;
	}

	public void registerObserver(IObserver<IExp> observer) {
		observable.registerObserver(observer);
	}
}

