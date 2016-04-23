package serviciosumafactorial;

public class ServicioSumaFactorialProxy implements serviciosumafactorial.ServicioSumaFactorial {
  private String _endpoint = null;
  private serviciosumafactorial.ServicioSumaFactorial servicioSumaFactorial = null;
  
  public ServicioSumaFactorialProxy() {
    _initServicioSumaFactorialProxy();
  }
  
  public ServicioSumaFactorialProxy(String endpoint) {
    _endpoint = endpoint;
    _initServicioSumaFactorialProxy();
  }
  
  private void _initServicioSumaFactorialProxy() {
    try {
      servicioSumaFactorial = (new serviciosumafactorial.ServicioSumaFactorialServiceLocator()).getServicioSumaFactorial();
      if (servicioSumaFactorial != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)servicioSumaFactorial)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)servicioSumaFactorial)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (servicioSumaFactorial != null)
      ((javax.xml.rpc.Stub)servicioSumaFactorial)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public serviciosumafactorial.ServicioSumaFactorial getServicioSumaFactorial() {
    if (servicioSumaFactorial == null)
      _initServicioSumaFactorialProxy();
    return servicioSumaFactorial;
  }
  
  public int sumar(int a, int b) throws java.rmi.RemoteException{
    if (servicioSumaFactorial == null)
      _initServicioSumaFactorialProxy();
    return servicioSumaFactorial.sumar(a, b);
  }
  
  public int factorial(int a) throws java.rmi.RemoteException{
    if (servicioSumaFactorial == null)
      _initServicioSumaFactorialProxy();
    return servicioSumaFactorial.factorial(a);
  }
  
  
}
