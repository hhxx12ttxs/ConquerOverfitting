* You should have received a copy of the GNU General Lesser Public
* License along with this program.  If not, see
* <http://www.gnu.org/licenses/lgpl-2.1.html>.
private double HG(double gam, double b, double lnb, double gam0, double b0, double lnb0)
{
return (((((gam0 - gam) * SpecialFunctions.digamma(gam) - SpecialFunctions.gammaln(gam0)) + SpecialFunctions.gammaln(gam)) - (gam0 * b) / b0) + gam + gam0 * Math.log(b) + gam0 * Math.log(gam0)) - gam0 * lnb0 - gam0 * Math.log(gam);

