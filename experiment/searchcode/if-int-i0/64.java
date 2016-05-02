package pl.kamilmielnik.fluid.model;

import org.apache.log4j.Logger;

import pl.kamilmielnik.fluid.configuration.derivable.SimulationConfiguration;

public class ProperSimulation extends Simulation {

	private Logger logger = Logger.getLogger(ProperSimulation.class);

	protected float[][] d;
	protected float[][] d_prev;
	protected float dt;

	public ProperSimulation(SimulationConfiguration configuration) {
		super(configuration);
		this.d = getArray();
		this.d_prev = getArray();
		this.dt = configuration.getTimeInterval();
	}

	protected float[][] getArray() {
		float[][] array = new float[configuration.getGridWidth() + 2][];
		for (int i = 0; i < configuration.getGridWidth() + 2; ++i) {
			array[i] = new float[configuration.getGridHeight() + 2];
			for (int j = 0; j < configuration.getGridHeight() + 2; ++j) {
				array[i][j] = 0.5f;
			}
		}
		return array;
	}

	private void addSource(float[][] destination, float[][] source) {
		for (int i = 0; i < configuration.getGridWidth() + 2; ++i) {
			for (int j = 0; j < configuration.getGridHeight() + 2; ++j) {
				destination[i][j] += dt * source[i][j];
			}
		}
	}

	protected void diffuse(int id, float[][] x, float[][] x_prev) {
		int relaxationSteps = configuration.getRelaxationSteps();
		int width = configuration.getGridWidth();
		int height = configuration.getGridHeight();
		int tmp = (int) dt * width * height;

		for (int relaxation = 0; relaxation < relaxationSteps; ++relaxation) {
			for (int i = 1; i <= width; ++i)
				for (int j = 1; j <= height; ++j) {
					x[i][j] = (x_prev[i][j] + tmp * (x[i - 1][j] + x[i + 1][j] + x[i][j - 1] + x[i][j + 1])) / (1 + 4 * tmp);
				}
			protectBoundaries(id, x);
		}
	}

	protected void advect(int id, float[][] d, float[][] d_prev) {
		int width = configuration.getGridWidth();
		int height = configuration.getGridHeight();
		int i0, j0, i1, j1;
		float x, y, s0, t0, s1, t1;

		for (int i = 1; i <= width; ++i)
			for (int j = 1; j <= height; ++j) {
				x = i;
				y = j;
				if (x < 0.5f) x = 0.5f;
				if (x > width + 0.5f) x = (float) (width + 0.5f);
				i0 = (int) x;
				i1 = i0 + 1;
				if (y < 0.5f) y = 0.5f;
				if (y > height + 0.5f) y = (float) (height + 0.5f);
				j0 = (int) y;
				j1 = j0 + 1;
				s1 = x - i0;
				s0 = 1 - s1;
				t1 = y - j0;
				t0 = 1 - t1;
				d[i][j] = s0 * (t0 * d_prev[i0][j0] + t1 * d_prev[i0][j1]) + s1 * (t0 * d_prev[i1][j0] + t1 * d_prev[i1][j1]);
			}
		protectBoundaries(id, d);
	}

	protected void densityStep(float[][] x, float[][] x_prev) {
		addSource(x, x_prev);
		diffuse(0, x_prev, x);
		advect(0, x, x_prev);
	}

	protected void protectBoundaries(int id, float[][] x) {
		int width = configuration.getGridWidth();
		int height = configuration.getGridHeight();

		for (int i = 1; i <= height; ++i) {
			x[0][i] = id == 1 ? -x[1][i] : x[1][i];
			x[width + 1][i] = id == 1 ? -x[width][i] : x[width][i];
		}
		for (int i = 1; i <= width; ++i) {
			x[i][0] = id == 2 ? -x[i][1] : x[i][1];
			x[i][height + 1] = id == 2 ? -x[i][height] : x[i][height];
		}
		x[0][0] = 0.5f * (x[1][0] + x[0][1]);
		x[0][height + 1] = 0.5f * (x[1][height + 1] + x[0][height]);
		x[width + 1][0] = 0.5f * (x[width][0] + x[width + 1][1]);
		x[width + 1][height + 1] = 0.5f * (x[width][height + 1] + x[width + 1][height]);
	}

	public void addDensity(int x, int y) {
		float density = configuration.getDensity();
		if (x < 1 || x > configuration.getGridWidth() || y < 1 || y > configuration.getGridHeight()) return;
		d_prev[x][y] += d_prev[x][y] + density < 1.0f ? density : 1.0f - d_prev[x][y];
		d_prev[x - 1][y] += d_prev[x - 1][y] + density / 2 < 1.0f ? density / 2 : 1.0f - d_prev[x - 1][y];
		d_prev[x + 1][y] += d_prev[x + 1][y] + density / 2 < 1.0f ? density / 2 : 1.0f - d_prev[x + 1][y];
		d_prev[x][y - 1] += d_prev[x][y - 1] + density / 2 < 1.0f ? density / 2 : 1.0f - d_prev[x][y - 1];
		d_prev[x][y + 1] += d_prev[x][y + 1] + density / 2 < 1.0f ? density / 2 : 1.0f - d_prev[x][y + 1];
	}

	public void removeDensity(int x, int y) {
		float density = configuration.getDensity();
		if (x < 1 || x > configuration.getGridWidth() || y < 1 || y > configuration.getGridHeight()) return;
		d_prev[x][y] -= d_prev[x][y] - density > 0 ? density : d_prev[x][y];
		d_prev[x - 1][y] -= d_prev[x - 1][y] - density / 2 > 0 ? density / 2 : d_prev[x - 1][y];
		d_prev[x + 1][y] -= d_prev[x + 1][y] - density / 2 > 0 ? density / 2 : d_prev[x + 1][y];
		d_prev[x][y - 1] -= d_prev[x][y - 1] - density / 2 > 0 ? density / 2 : d_prev[x][y - 1];
		d_prev[x][y + 1] -= d_prev[x][y + 1] - density / 2 > 0 ? density / 2 : d_prev[x][y + 1];
	}

	public void run() {
		long lastLoopTime = System.currentTimeMillis();
		while (true) {
			long delta = System.currentTimeMillis() - lastLoopTime;
			lastLoopTime = System.currentTimeMillis();
			densityStep(d, d_prev);
			requestRepaint();
			dt = delta < 0.1f ? 0.1f : delta;
			logger.debug("Simulation step done in: " + delta);
		}
	}

	@Override
	public float[][] getDensity() {
		return d;
	}

}

