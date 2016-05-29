* @author jbao
*/
public class CompFluidDynamics {

// u_t + c*u_x = 0
public static double[][] LinearConvection1D(FunctionModel fx) {
// centered difference in space, forward difference in time
// u_i(n+1) - u_i(n) / dt + c/2/dx ( u_i+1(n) - u_i-1(n)) = 0
int numSteps = 100;

