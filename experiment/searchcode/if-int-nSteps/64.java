private final int nDimensions;
private final double[] m_start, m_end, m_step;
private int nSteps;
nSteps = Integer.MIN_VALUE;
for (int i = 0; i < nDimensions; ++i) {
if (m_step[i] == 0) {

