private double total;
private double saldo;
private boolean estado;
private TbProveedor tbProveedorByCuenta;
CuentaAPagar that = (CuentaAPagar) o;

if (Double.compare(that.total, total) != 0) return false;

