package utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

public final class AppUtils {
  public static ObjectNode okJsonResponse() {
    ObjectNode result = Json.newObject();
    result.put("status", "ok");

    return result;
  }

  public static ObjectNode errorJsonResponse() {
    return errorJsonResponse(null);
  }

  public static ObjectNode errorJsonResponse(String message) {
    ObjectNode result = Json.newObject();
    result.put("status", "error");

    if (!Strings.isNullOrEmpty(message)) {
      result.put("message", message);
    }

    return result;
  }

  public static void setHeaders(Http.Response response) {
    response.setHeader("Access-Control-Allow-Origin", "*");       // Need to add the correct domain in here!!
    response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");   // Only allow POST
    response.setHeader("Access-Control-Max-Age", "300");          // Cache response for 5 minutes
    response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");         // Ensure this header is also allowed!
  }

  /**
   * Arrondi un nombre a <code>numDecimals</code> decimales pres.
   * @param value Valeur a arrondir.
   * @param numDecimals Nombre de decimales.
   * @return Nombre arrondi.
   */
  public static double round(double value, int numDecimals) {
    if (numDecimals < 0) {
      throw new IllegalArgumentException();
    }

    long factor = (long) Math.pow(10, numDecimals);
    value = value * factor;
    long tmp = Math.round(value);

    return (double) tmp / factor;
  }
}

