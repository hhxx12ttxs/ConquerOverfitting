public class PeakWindow {
private String m_name;
private Double m_mz;
private double m_ltol, m_rtol;

public PeakWindow(String name, String mz, String ltol, String rtol) {
double d_ltol = new Double(ltol).doubleValue();
double d_rtol = new Double(rtol).doubleValue();
m_ltol = d_ltol;
m_rtol = d_rtol;

