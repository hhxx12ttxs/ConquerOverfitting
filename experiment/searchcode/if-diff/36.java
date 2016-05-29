if (value > top) {
double diff = value - bottom;
if ((m_direction == null) || !m_direction) { // up started
m_diff = diff;
m_diffMax = diff;
} else {
if (diff > m_diff) {

