import com.google.gwt.user.client.rpc.IsSerializable;

public abstract class PojoCField<T>  implements IsSerializable {

T value;


String fieldName;

public PojoCField() {
}

public PojoCField(String fieldName) {
this.fieldName = fieldName;

