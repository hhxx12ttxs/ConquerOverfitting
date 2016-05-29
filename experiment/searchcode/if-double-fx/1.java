@SuppressWarnings(&quot;unchecked&quot;) public class FxObject<T extends IEasable<T>> extends Fx<T> {
public FxObject(IEasable<T> step, double time) {
super((T)step,time);
super((T)step,time,method);
}

public T state(Fx<?> fx, double d, Ease method) {
if (fx == null) fx = this;
return step.ease(((Fx<T>)fx).step,d,method);
}
}

