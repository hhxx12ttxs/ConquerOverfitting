* @since 1.0
*/
public class PreciseDurationField extends BaseDurationField {

private static final long serialVersionUID = -8346152187724495365L;
public PreciseDurationField(DurationFieldType type, long unitMillis) {
super(type);
iUnitMillis = unitMillis;

