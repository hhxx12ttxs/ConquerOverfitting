
abstract class TraceTrailParticle extends Particle {

public TraceTrailParticle previous;
public TraceTrailParticle next;
this.next = null;

if (this.previous != null) this.previous.next = this;
}
public TraceTrailParticle(double x, double y, double z, TraceTrailParticle prev) {

