public abstract class RESOLVE_BASE_EXT extends RESOLVE_BASE {
public RType replica(RType r) {
RType retVal = null;
if (r instanceof Std_Integer_Realiz.Integer) {
retVal = Std_Boolean_Fac.Replica((Std_Boolean_Realiz.Boolean)r);
} else if (r instanceof Std_Character_Realiz.Character) {

