import java.util.Random;

@Component
public class RandomFactory {

private long seed = Long.MIN_VALUE;
public Random getRandom() {
Random random;

if (seed == Long.MIN_VALUE) {
random = new Random(System.currentTimeMillis());

