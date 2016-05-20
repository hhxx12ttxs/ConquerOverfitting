package com.sitesolved.slimbuddy.registration;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.log4j.Logger;
import org.jdom.Element;
import com.sitesolved.siteorder.dto.*;
import com.sitesolved.siteorder.utils.ProtxResult;
import com.sitesolved.siteorder.utils.ProtxSettings;
import com.sitesolved.util.URLReader;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import com.sitesolved.businessdelegates.WebsiteDelegate;
import com.sitesolved.datastorageobjects.ModuleConstants;
import com.sitesolved.exception.ProtxException;
import com.sitesolved.exception.SiteSolvedException;

public class SlimBuddyProtxUtils {

	static Logger log = Logger.getLogger(SlimBuddyProtxUtils.class);

	public SlimBuddyProtxUtils(String vendorName, String encryptionPassword) {
		this.vendorName = vendorName;
		this.encryptionPassword = encryptionPassword;
	}

	private String vendorName;

	private String encryptionPassword;

	// ** Your server's IP address or dns name and web app directory. Fully
	// qualified
	// ** Examples :
	// String myServer="https://www.newco.com/jsp-form-kit/",
	// String myServer="192.168.0.1/jsp-form-kit/",
	// String myServer="http://localhost/jsp-form-kit/"

	String myServer = "http://localhost/jsp-form-kit/";

	// *********************************************************************************
	// The protx site to send information to **

	// ** Simulator site **
	String vspsite = "https://ukvpstest.protx.com/VSPSimulator/VSPFormGateway.asp";

	// ** Test site **
	// String vspsite="https://ukvpstest.protx.com/vps2form/submit.asp";

	// ** Live site - ONLY uncomment when going live **
	// String vspsite="https://ukvps.protx.com/vps2form/submit.asp";

	/*
	 * The SimpleXor encryption algorithm This simple function and the Base64
	 * will deter script kiddies and prevent the "View Source" type tampering It
	 * won't stop a motivated, capable hacker, but the most they could do is
	 * change the amount field to something else, so provided the vendor checks
	 * the reports and compares amounts, there is no harm done. It's more secure
	 * than the other PSPs who don't both encrypting their forms at all.
	 */

	public static byte[] simpleXor(byte[] in, String key) {
		// Initialise result
		byte[] result = new byte[in.length];

		// Step through string a character at a time
		for (int i = 0; i < in.length; i++) {
			// Get ASCII code from string, get ASCII code from key (loop through
			// with MOD), XOR the two, get the character from the result
			// % is MOD (modulus), ^ is XOR
			result[i] = (byte) (in[i] ^ key.charAt(i % key.length()));
		}
		return result;
	}

	public static String getFirstToken(String detail) {
		java.util.Map tokens = getToken(detail);
		if (tokens == null)
			return null;
		return (String) tokens.values().iterator().next();
	}

	/*
	 * Retrieve fields from returned details. This method is still able to
	 * retrieve fields, even if the & or = symbol is part of a field's value
	 */
	public static java.util.Map getToken(String detail) {

		// List of possible tokens
		String[] tokens = new String[] { "Status", "StatusDetail",
				"VendorTxCode", "VPSTxId", "TxAuthNo", "Amount", "AVSCV2",
				"AddressResult", "PostCodeResult", "CV2Result", "GiftAid",
				"3DSecureStatus", "CAVV" };

		// Initialise arrays
		java.util.SortedMap positions = new java.util.TreeMap();
		java.util.Map result = new java.util.HashMap();

		// Get the next token in the sequence
		for (int i = tokens.length - 1; i >= 0; i--) {
			// Find the position in the string
			int start = detail.indexOf(tokens[i]);
			// If it's present
			if (start != -1) {
				// Record position and token name
				positions.put(new Integer(start), tokens[i]);
			}
		}

		// Iterate through the result array, getting the token values
		java.util.Iterator i = positions.keySet().iterator();
		if (i.hasNext()) {
			Integer start = (Integer) i.next();
			Integer current = start;
			String token;
			while (i.hasNext()) {
				token = (String) positions.get(start);
				current = (Integer) i.next();
				result.put(token, detail.substring(start.intValue()
						+ token.length() + 1, current.intValue() - 1));
				start = current;
			}
			// get the final value
			token = (String) positions.get(current);
			result.put(token, detail.substring(current.intValue()
					+ token.length() + 1));
		}
		return result;
	}

	// workaround for intolerant java request object
	public static String getCrypt(javax.servlet.http.HttpServletRequest request) {
		if (request.getParameter("crypt") != null) {
			return request.getParameter("crypt");
		} else {
			String query = request.getQueryString();
			int start = query.indexOf("crypt=");
			if (start == -1) {
				return null;
			}
			int end = query.indexOf("&", start);
			if (end == -1) {
				return query.substring(start + 6);
			}
			return query.substring(start + 6, end);
		}
	}

	// base64 functionality
	static String map = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

	static char[] encodeMap = new char[64];

	static char[] decodeMap = new char[128];
	static {
		for (int idx = 0; idx < map.length(); idx++) {
			encodeMap[idx] = map.charAt(idx);
			decodeMap[map.charAt(idx)] = (char) idx;
		}
		decodeMap[' '] = 62; // to handle url decode converting + to space
	}

	public static String base64Encode(byte[] in) {
		int iLen = in.length;
		int oDataLen = (iLen * 4 + 2) / 3; // output length without padding
		int oLen = ((iLen + 2) / 3) * 4; // output length including padding
		char[] out = new char[oLen];
		int ip = 0;
		int op = 0;
		while (ip < iLen) {
			int i0 = in[ip++] & 0xff;
			int i1 = ip < iLen ? in[ip++] & 0xff : 0;
			int i2 = ip < iLen ? in[ip++] & 0xff : 0;
			int o0 = i0 >>> 2;
			int o1 = ((i0 & 3) << 4) | (i1 >>> 4);
			int o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
			int o3 = i2 & 0x3F;
			out[op++] = encodeMap[o0];
			out[op++] = encodeMap[o1];
			out[op] = op < oDataLen ? encodeMap[o2] : '=';
			op++;
			out[op] = op < oDataLen ? encodeMap[o3] : '=';
			op++;
		}
		return new String(out);
	}

	public static byte[] base64Decode(String in) {
		int iLen = in.length();
		while (iLen > 0 && in.charAt(iLen - 1) == '=')
			iLen--;
		int oLen = (iLen * 3) / 4;
		byte[] out = new byte[oLen];
		int ip = 0;
		int op = 0;
		while (ip < iLen) {
			int i0 = in.charAt(ip++);
			int i1 = in.charAt(ip++);
			int i2 = ip < iLen ? in.charAt(ip++) : 'A';
			int i3 = ip < iLen ? in.charAt(ip++) : 'A';
			int b0 = decodeMap[i0];
			int b1 = decodeMap[i1];
			int b2 = decodeMap[i2];
			int b3 = decodeMap[i3];
			int o0 = (b0 << 2) | (b1 >>> 4);
			int o1 = ((b1 & 0xf) << 4) | (b2 >>> 2);
			int o2 = ((b2 & 3) << 6) | b3;
			out[op++] = (byte) o0;
			if (op < oLen)
				out[op++] = (byte) o1;
			if (op < oLen)
				out[op++] = (byte) o2;
		}
		return out;
	}

	public static String getPaymentSubmissionForm(
			com.sitesolved.siteorder.dto.SiteOrderDto orderDto,

			String encryptionPassword,

			String buttonImage, String vendorName, String vspSite,
			String returnUrl, String cancelUrl, String currency,
			String description,

			String vendorEmail, String emailMessage) {

		try {

			// org.jdom.output.XMLOutputter xx = new
			// org.jdom.output.XMLOutputter();
			//
			// Element orderXml =
			// com.sitesolved.siteserver.web.siteshop.EpdqUtils.getOrderXml(basketXml);
			// if (orderXml == null) throw new ProtexException("No basket");
			// if (orderXml.getChild("totals") == null) throw new
			// ProtexException("Basket has no totals\n" );
			// if (orderXml.getChild("totals").getChildText("grosstotal") ==
			// null) throw new ProtexException("Basket has no gross total");
			//
			double total = orderDto.getTotalIncVat();
			// convert basket contents to text
			StringBuffer bc = new StringBuffer();
			int lineCounter = 0;

			// first append the number of order lines (add 1 for delivery)
			int lines = orderDto.getOrderLines().size() + 1;
			bc.append(Integer.toString(lines));
			bc.append(":");

			// set up a decimal formatter for currencies
			java.text.DecimalFormat df = new java.text.DecimalFormat();
			df.setMaximumFractionDigits(2);
			df.setMinimumFractionDigits(2);
			df.setDecimalSeparatorAlwaysShown(true);
			df.setGroupingUsed(false);

			// now append the contents of each basket line in the required Protx
			// style
			for (Iterator lineIterator = orderDto.getOrderLines().iterator(); lineIterator
					.hasNext();) {
				OrderLineDto line = (OrderLineDto) lineIterator.next();
				lineCounter++;

				// get the product name for this item
				String productName = "";
				productName = (String) line.getProductDto().getDetails().get(
						"product_name");

				// fall back to stock code as name if there is not product name
				// available (there should be)
				if (productName == null)
					productName = line.getProductDto().getStockCode();

				// append the stock code
				bc.append(productName);
				bc.append(" (" + line.getProductDto().getStockCode() + ")");

				// append the line quanitity
				bc.append(":");
				bc.append(line.getQuantity());

				// append line costs
				bc.append(":");

				double unitCostExVat = line.getUnitCostExVat();
				double unitCostVat = line.getGoodsTotalVAT()
						/ line.getQuantity();
				double unitCostIncVat = line.getGoodsTotalIncVat()
						/ line.getQuantity();
				bc.append(df.format(unitCostExVat));
				bc.append(":");
				bc.append(df.format(unitCostVat));
				bc.append(":");
				bc.append(df.format(unitCostIncVat));
				bc.append(":");
				bc.append(df.format(line.getTotalIncVat()));
				bc.append(":");

			}

			// append shipping cost
			bc.append(" Delivery:---:---:---:---:");
			bc.append(df.format(orderDto.getTotalDeliveryIncVat()));

			// we now have the whole basket as a String
			String shoppingBasket = bc.toString();

			// now set the protx specific variables

			String vendorTxCode = orderDto.getInvoiceNumber();
			String amount = df.format(orderDto.getTotalIncVat());
			// String customerName = orderDto.getInvoiceAddress().getName();
			// if (customerName == null) customerName = "";

			String customerEmail = orderDto.getUserEmailAddress();

			// get delivery address in Protx format
			StringBuffer deliveryAddress = new StringBuffer();
			if (orderDto.getDeliveryAddress().getAddressLine1() != null) {
				deliveryAddress.append(orderDto.getDeliveryAddress()
						.getAddressLine1());
				deliveryAddress.append("\n");
			}
			if (orderDto.getDeliveryAddress().getAddressLine2() != null) {
				deliveryAddress.append(orderDto.getDeliveryAddress()
						.getAddressLine2());
				deliveryAddress.append("\n");
			}
			if (orderDto.getDeliveryAddress().getAddressLine3() != null) {
				deliveryAddress.append(orderDto.getDeliveryAddress()
						.getAddressLine3());
				deliveryAddress.append("\n");
			}
			if (orderDto.getDeliveryAddress().getTown() != null) {
				deliveryAddress.append(orderDto.getDeliveryAddress().getTown());
				deliveryAddress.append("\n");
			}
			if (orderDto.getDeliveryAddress().getCity() != null) {
				deliveryAddress.append(orderDto.getDeliveryAddress().getCity());
				deliveryAddress.append("\n");
			}
			if (orderDto.getDeliveryAddress().getCounty() != null) {
				deliveryAddress.append(orderDto.getDeliveryAddress()
						.getCounty());
				deliveryAddress.append("\n");
			}
			String deliveryPostCode = orderDto.getDeliveryAddress()
					.getPostCode();

			// get billing address in Protx Format
			StringBuffer invoiceAddress = new StringBuffer();
			if (orderDto.getInvoiceAddress().getAddressLine1() != null) {
				invoiceAddress.append(orderDto.getInvoiceAddress()
						.getAddressLine1());
				invoiceAddress.append("\n");
			}
			if (orderDto.getInvoiceAddress().getAddressLine2() != null) {
				invoiceAddress.append(orderDto.getInvoiceAddress()
						.getAddressLine2());
				invoiceAddress.append("\n");
			}
			if (orderDto.getInvoiceAddress().getAddressLine3() != null) {
				invoiceAddress.append(orderDto.getInvoiceAddress()
						.getAddressLine3());
				invoiceAddress.append("\n");
			}
			if (orderDto.getInvoiceAddress().getTown() != null) {
				invoiceAddress.append(orderDto.getInvoiceAddress().getTown());
				invoiceAddress.append("\n");
			}
			if (orderDto.getInvoiceAddress().getCity() != null) {
				invoiceAddress.append(orderDto.getInvoiceAddress().getCity());
				invoiceAddress.append("\n");
			}
			if (orderDto.getInvoiceAddress().getCounty() != null) {
				invoiceAddress.append(orderDto.getInvoiceAddress().getCounty());
				invoiceAddress.append("\n");
			}
			String invoicePostCode = orderDto.getInvoiceAddress().getPostCode();

			String contactNumber = orderDto.getInvoiceAddress().getTelephone();
			String contactFax = null;
			String allowGiftAid = null;
			String applyAVSCV2 = null;
			String apply3DSecure = null;

			String customerName = null;
			if (orderDto.getInvoiceAddress().getForename() != null
					&& orderDto.getInvoiceAddress().getSurname() != null) {
				customerName = orderDto.getInvoiceAddress().getForename() + " "
						+ orderDto.getInvoiceAddress().getSurname();
			}
			if (orderDto.getInvoiceAddress().getTitle() != null
					&& !orderDto.getInvoiceAddress().getTitle().equals("null")) {
				customerName = orderDto.getInvoiceAddress().getTitle() + " "
						+ customerName;
			}

			// build the Protx Crypt String
			String stuff = "VendorTxCode=" + vendorTxCode + "&" + "Amount="
					+ amount + "&" + "Currency=" + currency + "&"
					+ "Description=" + description + "&" + "SuccessURL="
					+ returnUrl + "&" + "FailureURL=" + cancelUrl + "&";

			if (customerEmail != null) {
				stuff += "CustomerEmail=" + customerEmail + "&";
			}
			if (vendorEmail != null) {
				stuff += "VendorEmail=" + vendorEmail + "&";
			}
			if (customerName != null) {
				stuff += "CustomerName=" + customerName + "&";
			}
			if (deliveryAddress != null) {
				stuff += "DeliveryAddress=" + deliveryAddress.toString() + "&";
			}
			if (deliveryPostCode != null) {
				stuff += "DeliveryPostCode=" + deliveryPostCode + "&";
			}
			if (invoiceAddress != null) {
				stuff += "BillingAddress=" + invoiceAddress.toString() + "&";
			}
			if (invoiceAddress != null) {
				stuff += "BillingPostCode=" + invoicePostCode + "&";
			}
			if (contactNumber != null) {
				stuff += "ContactNumber=" + contactNumber + "&";
			}
			if (contactFax != null) {
				stuff += "ContactFax=" + contactFax + "&";
			}
			if (allowGiftAid != null) {
				stuff += "AllowGiftAid=" + allowGiftAid + "&";
			}
			if (applyAVSCV2 != null) {
				stuff += "ApplyAVSCV2=" + applyAVSCV2 + "&";
			}
			if (apply3DSecure != null) {
				stuff += "Apply3DSecure=" + apply3DSecure + "&";
			}
			stuff += "Basket=";
			stuff += shoppingBasket;

			stuff += "&EmailMessage=" + emailMessage;

			// log.debug("Pre-encryption: " + stuff.toString());

			// Encrypt the String
			String crypt = base64Encode(simpleXor(stuff.getBytes(),
					encryptionPassword));

			// log.debug("post encryption: " + crypt);
			// build the form
			StringBuffer s = new StringBuffer();
			s.append("<form action=\"");
			s.append(vspSite);
			s.append("\" method=\"post\">\n");

			s.append("<input type=\"hidden\" name=\"VPSProtocol\" value=\"");
			s.append("2.22");
			s.append("\" />\n");

			s.append("<input type=\"hidden\" name=\"TxType\" value=\"");
			s.append("PAYMENT");
			s.append("\" />\n");

			s.append("<input type=\"hidden\" name=\"Vendor\" value=\"");
			s.append(vendorName);
			s.append("\" />\n");

			s.append("<input type=\"hidden\" name=\"Crypt\" value=\"");
			s.append(crypt);
			s.append("\" />\n");

			s.append("<input type=\"image\" src=\"");
			s.append(buttonImage);
			s.append("\" />\n</form>\n");

			return s.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
			if (ex instanceof ProtxException)
				return "<div class=\"error\">Error building payment form : Protx message = "
						+ ex.getMessage() + "</div>";
			return "<div class=\"error\">Error building payment form (see log file)</div>";
		}
	}

	public static String getDirectPaymentSubmissionForm(
			com.sitesolved.siteorder.dto.SiteOrderDto orderDto,

			String encryptionPassword,

			String buttonImage, String vendorName, String vspSite,
			String returnUrl, String cancelUrl, String currency,
			String description,

			String vendorEmail, String emailMessage, String urlOfPaymentPage,
			String urlOfConfirmationPage, HashMap dynamicContent) {

		try {
			StringBuffer s = new StringBuffer();
			Set k = dynamicContent.keySet();
			log.debug("available params");
			for (Iterator iter = k.iterator(); iter.hasNext();) {
				String element = (String) iter.next();
				log.debug(element);
			}

			if (dynamicContent.get("3d_secure") != null) {
				s.append((String) dynamicContent.get("3d_secure"));
			} else {

				// build the form

				s
						.append("<form class=\"protx_direct_form\" method=\"post\" action=\"/siteorderactions.do");
				s.append("\" method=\"post\">\n");

				s
						.append("<input type=\"hidden\" name=\"requested_action\" value=\"");
				s.append("authenticate_protx_direct_payment");
				s.append("\" />\n");

				s.append("<input type=\"hidden\" name=\"errorpage\" value=\"");
				s.append(urlOfPaymentPage);
				s.append("\" />\n");

				s
						.append("<input type=\"hidden\" name=\"successpage\" value=\"");
				s.append(urlOfConfirmationPage);
				s.append("\" />\n");

				if (dynamicContent.containsKey("error_card_number")) {
					s.append("<div class=\"error\">");
					s
							.append("<label for=\"card_number\">Card Number</label> <input type=\"text\" name=\"card_number\"  /><br />\n");
					s.append("</div>");
				} else {
					s
							.append("<label for=\"card_number\">Card Number</label> <input type=\"text\" name=\"card_number\"  /><br />\n");
				}

				if (dynamicContent.containsKey("error_name_on_card")) {
					s.append("<div class=\"error\">");
					s
							.append("<label for=\"name_on_card\">Full Name</label> <input type=\"text\" name=\"name_on_card\"  /><br />\n");
					s.append("</div>");
				} else {
					s
							.append("<label for=\"name_on_card\">Full Name</label> <input type=\"text\" name=\"name_on_card\"  /><br />\n");
				}

				s.append("<label for=\"startMonth\">Start Date</label>");
				s.append(getMonths("start_month"));
				s.append(getYears("start_year", 10, 0));
				s.append("<br />\n");

				if (dynamicContent.containsKey("error_expiry")) {
					s.append("<div class=\"error\">");

					s.append("<label for=\"expiryDate\">Expiry Date</label>");
					s.append(getMonths("exp_month"));
					s.append(getYears("exp_year", 0, 20));
					s.append("</div>");
					s.append("<br />\n");
				} else {
					s.append("<label for=\"expiryDate\">Expiry Date</label>");
					s.append(getMonths("exp_month"));
					s.append(getYears("exp_year", 0, 20));
					s.append("<br />\n");
				}
				s
						.append("<label for=\"issue_number\">Issue Number</label> <input class=\"issue\" type=\"text\" name=\"issue_number\"  /><br />\n");

				if (dynamicContent.containsKey("error_cvv_number")) {
					s.append("<div class=\"error\">");
					s
							.append("<label for=\"cvv_number\">CVV</label> <input class=\"cvv\" type=\"text\" name=\"cvv_number\"  /><br />\n");
					s.append("</div>");
				} else {
					s
							.append("<label for=\"cvv_number\">CVV</label> <input class=\"cvv\" type=\"text\" name=\"cvv_number\"  /><br />\n");
				}

				if (dynamicContent.containsKey("error_card_type")) {
					s.append("<div class=\"error\">");
					s.append("<label for=\"card_type\">Card Type</label>");
					s.append("<select name=\"card_type\">");
					s.append("<option value=\"VISA\">Visa</option>");
					s.append("<option value=\"MC\">MasterCard</option>");
					s.append("<option value=\"DELTA\">Delta</option>");
					s.append("<option value=\"MAESTRO\">Maestro</option>");
					s.append("<option value=\"UKE\">Visa Electron</option>");
					s.append("<option value=\"SOLO\">Solo</option>");
					s.append("<option value=\"JCB\">JCB</option>");

					s.append("</select><br />\n");
					s.append("</div>");
				} else {
					s.append("<label for=\"card_type\">Card Type</label>");
					s.append("<select name=\"card_type\">");
					s.append("<option value=\"VISA\">Visa</option>");
					s.append("<option value=\"MC\">MasterCard</option>");
					s.append("<option value=\"DELTA\">Delta</option>");
					s.append("<option value=\"MAESTRO\">Maestro</option>");
					s.append("<option value=\"UKE\">Visa Electron</option>");
					s.append("<option value=\"SOLO\">Solo</option>");
					s.append("<option value=\"JCB\">JCB</option>");

					s.append("</select><br />\n");
				}

				s.append("<input class=\"paynowbutton\" type=\"image\" src=\"");
				s.append(buttonImage);
				s.append("\" />\n</form>\n");
			}
			return s.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
			if (ex instanceof ProtxException)
				return "<div class=\"error\">Error building payment form : Protx message = "
						+ ex.getMessage() + "</div>";
			return "<div class=\"error\">Error building payment form (see log file)</div>";
		}
	}

	
	public static ProtxResult processProtxDirectPaymentRequest(
			Registration registration, 
			PaymentCardDto paymentCardDto, Properties protxParams) throws SiteSolvedException {
		StringBuffer post = new StringBuffer();
		NameValuePair[] arParams = null;
		try {

			String testUrl = "https://ukvpstest.protx.com/vspgateway/service/vspdirect-register.vsp";
			String liveUrl = "https://live.sagepay.com/gateway/service/vspdirect-register.vsp";
			String simulationUrl = "https://ukvpstest.protx.com/VSPSimulator/VSPDirectGateway.asp";

			log.debug("processing direct auth request");

			
			String strProtocol = (String) protxParams
			.getProperty("protx_vps_protocol");
	String strVendorName = (String) protxParams
			.getProperty("protx_vendorname");
	String description = (String) protxParams
			.getProperty("protx_description");
	String strCurrency = (String) protxParams.get("protx_currency");

	String mode = (String) protxParams.getProperty("protx_mode");
	String url = "";
	if (mode.equals("live"))
		url = liveUrl;
	if (mode.equals("test"))
		url = testUrl;
	if (mode.equals("simulation"))
		url = simulationUrl;

	log.debug("mode is " + mode);
	log.debug("server url is " + url);
	log.debug("payment card dto " + paymentCardDto);

	post.append(url);


			ArrayList params = new ArrayList();
			NameValuePair protocol = new NameValuePair("VPSProtocol",
					strProtocol);
			params.add(protocol);

			NameValuePair vendor = new NameValuePair("Vendor", strVendorName);
			params.add(vendor);

			NameValuePair txType = new NameValuePair("TxType", "PAYMENT");
			params.add(txType);

			NameValuePair vendorTx = new NameValuePair("VendorTxCode", registration.getId());
			params.add(vendorTx);

			DecimalFormat nf = new DecimalFormat("0.00");
			nf.setMinimumFractionDigits(2);
			nf.setMaximumFractionDigits(2);
			NameValuePair amount = new NameValuePair("Amount", nf
					.format(registration.getInitialPayment()));
			params.add(amount);

			NameValuePair nvdescription = new NameValuePair("Description",
					description);
			params.add(nvdescription);

			NameValuePair nvcurrency = new NameValuePair("Currency",
					strCurrency);
			params.add(nvcurrency);

			NameValuePair cardHolder = new NameValuePair("CardHolder",
					paymentCardDto.getNameOnCard());
			params.add(cardHolder);

			NameValuePair cardNumber = new NameValuePair("CardNumber",
					paymentCardDto.getCardNumber());
			params.add(cardNumber);

			SimpleDateFormat dateFormat = new SimpleDateFormat("MMyy");
			NameValuePair startDate = new NameValuePair("StartDate",
					paymentCardDto.getStartDate(dateFormat));
			params.add(startDate);

			NameValuePair expDate = new NameValuePair("ExpiryDate",
					paymentCardDto.getExpiryDate(dateFormat));
			params.add(expDate);

			if (paymentCardDto.getIssueNumber() != null) {
				NameValuePair issueNumber = new NameValuePair("IssueNumber",
						paymentCardDto.getIssueNumber());
				params.add(issueNumber);
			}

			NameValuePair cv2 = new NameValuePair("CV2", paymentCardDto
					.getCvvNumber());
			params.add(cv2);

			NameValuePair cardType = new NameValuePair("CardType",
					paymentCardDto.getCardType());
			params.add(cardType);

			NameValuePair invoiceAddress = new NameValuePair("BillingAddress",
					registration.getProtxAddress());
			params.add(invoiceAddress);

			NameValuePair invoicePostcode = new NameValuePair(
					"BillingPostCode", registration.getPostcode());
			params.add(invoicePostcode);

			
			NameValuePair customerName = new NameValuePair("CustomerName",
					registration.getForename() + " " + registration.getSurname());
			params.add(customerName);

			NameValuePair customerEmail = new NameValuePair("CustomerEmail",
					registration.getEmail());
			params.add(customerEmail);

			arParams = new NameValuePair[params.size()];
			int counter = 0;
			for (Iterator iter = params.iterator(); iter.hasNext();) {
				NameValuePair p = (NameValuePair) iter.next();
				// log.debug("sending " + p.getName() + " = " + p.getValue());
				arParams[counter] = p;
				counter++;

			}

		} catch (Exception ex) {
			log.error("error getting parameter from order", ex);
			throw new SiteSolvedException("Missing parameter");
		}
		log.debug("sending auth request to protx: " + post.toString());
		String result = URLReader.postToUrl(post.toString(), arParams);
		log.debug("received result: " + result);

		ProtxResult protxResult = new ProtxResult(result);
		return protxResult;

	}

	public static ProtxResult process3DSecureCallback(SiteOrderDto orderDto,
			String websitePk, ProtxResult protxResult)
			throws SiteSolvedException {
		StringBuffer post = new StringBuffer();
		NameValuePair[] arParams = null;
		try {

			// String testUrl =
			// "https://ukvpstest.protx.com/VPSDirectAuth/Callback3D.asp";
			String testUrl = "https://ukvpstest.protx.com/vspgateway/service/direct3dcallback.vsp";
			// String liveUrl =
			// "https://ukvps.protx.com/VPSDirectAuth/Callback3D.asp";
			String liveUrl = "https://live.sagepay.com/gateway/service/direct3dcallback.vsp";

			String simulationUrl = "https://ukvpstest.protx.com/VSPSimulator/VSPDirectCallback.asp";

			log.debug("processing 3d secure callback request");

			WebsiteDelegate wd = new WebsiteDelegate();
			HashMap siteOrderParams = wd.listConfigurationParameters(websitePk,
					ModuleConstants.MODULE_SITEORDER);

			String mode = (String) siteOrderParams.get("protx_mode");
			String url = "";
			if (mode.equals("live"))
				url = liveUrl;
			if (mode.equals("test"))
				url = testUrl;
			if (mode.equals("simulation"))
				url = simulationUrl;

			log.debug("mode is " + mode);
			log.debug("server url is " + url);

			post.append(url);

			ArrayList params = new ArrayList();

			NameValuePair MD = new NameValuePair("MD", protxResult.getMD());
			params.add(MD);

			NameValuePair PARes = new NameValuePair("PaRes", protxResult
					.getPARes());
			params.add(PARes);

			arParams = new NameValuePair[params.size()];
			int counter = 0;
			for (Iterator iter = params.iterator(); iter.hasNext();) {
				NameValuePair p = (NameValuePair) iter.next();
				arParams[counter] = p;
				counter++;

			}

		} catch (Exception ex) {
			log.error("error getting parameter from order", ex);
			throw new SiteSolvedException("Missing parameter");
		}
		log.debug("sending auth request to protx: " + post.toString());
		String result = URLReader.postToUrl(post.toString(), arParams);
		log.debug("received result: " + result);

		protxResult = new ProtxResult(result);
		return protxResult;

	}

	private static String addressToProtxString(OrderAddressDto a) {
		StringBuffer address = new StringBuffer();
		ArrayList addressElements = new ArrayList();
		if (a.getAddressLine1() != null
				&& a.getAddressLine1().trim().length() > 0) {
			addressElements.add(a.getAddressLine1());
		}
		if (a.getAddressLine2() != null
				&& a.getAddressLine2().trim().length() > 0) {
			addressElements.add(a.getAddressLine2());
		}
		if (a.getAddressLine3() != null
				&& a.getAddressLine3().trim().length() > 0) {
			addressElements.add(a.getAddressLine3());
		}
		if (a.getTown() != null && a.getTown().trim().length() > 0) {
			addressElements.add(a.getTown());
		}
		if (a.getCity() != null && a.getCity().trim().length() > 0) {
			addressElements.add(a.getCity());
		}
		if (a.getCounty() != null && a.getCounty().trim().length() > 0) {
			addressElements.add(a.getCounty());
		}

		for (Iterator iter = addressElements.iterator(); iter.hasNext();) {
			String el = (String) iter.next();
			address.append(el);
			if (iter.hasNext())
				address.append(",");

		}

		return address.toString();
	}

	public static String getMonths(String fieldName) {
		StringBuffer s = new StringBuffer();
		s.append("<select name=\"");
		s.append(fieldName);
		s.append("\">");
		s.append("<option value=\"01\">01 (January)</option>");
		s.append("<option value=\"02\">02 (February)</option>");
		s.append("<option value=\"03\">03 (March)</option>");
		s.append("<option value=\"04\">04 (April)</option>");
		s.append("<option value=\"05\">05 (May)</option>");
		s.append("<option value=\"06\">06 (June)</option>");
		s.append("<option value=\"07\">07 (July)</option>");
		s.append("<option value=\"08\">08 (August)</option>");
		s.append("<option value=\"09\">09 (September)</option>");
		s.append("<option value=\"10\">10 (October)</option>");
		s.append("<option value=\"11\">11 (November)</option>");
		s.append("<option value=\"12\">12 (December)</option>");
		s.append("</select>");
		return s.toString();
	}

	public static String getYears(String fieldName, int yearsBefore,
			int yearsAfter) {
		StringBuffer s = new StringBuffer();
		Date now = new Date();
		GregorianCalendar calNow = new GregorianCalendar();
		calNow.setTime(now);
		int year = calNow.get(Calendar.YEAR);
		int fromYear = year - yearsBefore;
		int toYear = year + yearsAfter;
		s.append("<select name=\"");
		s.append(fieldName);
		s.append("\">");
		for (int i = fromYear; i <= toYear; i++) {
			s.append("<option value=\"");
			s.append(i);
			s.append("\">");
			s.append(i);
			s.append("</option>\n");
		}
		s.append("</select>");
		return s.toString();
	}

	public static void post3DSecureResult(String md, String PARes) {

	}

	public static ProtxResult processProtxDirectRepeatPaymentRequest(
			ProtxSettings protxSettings, SiteOrderDto originalOrder,
			SiteOrderDto repeatOrder) throws SiteSolvedException {
		StringBuffer post = new StringBuffer();
		NameValuePair[] arParams = null;
		try {

			String testUrl = "https://ukvpstest.protx.com/vspgateway/service/repeat.vsp";
			String liveUrl = "https://ukvps.protx.com/vspgateway/service/repeat.vsp";
			String simulationUrl = "https://ukvpstest.protx.com/VSPSimulator/repeat.vsp";

			log.debug("processing direct auth repeat order request");

			String url = "";
			if (protxSettings.getMode().equals("live"))
				url = liveUrl;
			if (protxSettings.getMode().equals("test"))
				url = testUrl;
			if (protxSettings.getMode().equals("simulation"))
				url = simulationUrl;

			log.debug("mode is " + protxSettings.getMode());
			log.debug("server url is " + url);

			post.append(url);

			ArrayList params = new ArrayList();
			NameValuePair protocol = new NameValuePair("VPSProtocol",
					protxSettings.getProtocol());
			params.add(protocol);

			NameValuePair vendor = new NameValuePair("Vendor", protxSettings
					.getVendorName());
			params.add(vendor);

			NameValuePair txType = new NameValuePair("TxType", "REPEAT");
			params.add(txType);

			NameValuePair vendorTx = new NameValuePair("VendorTxCode",
					repeatOrder.getInvoiceNumber());
			params.add(vendorTx);

			DecimalFormat nf = new DecimalFormat("0.00");
			nf.setMinimumFractionDigits(2);
			nf.setMaximumFractionDigits(2);
			NameValuePair amount = new NameValuePair("Amount", nf
					.format(repeatOrder.getTotalIncVat()));
			params.add(amount);

			NameValuePair nvcurrency = new NameValuePair("Currency",
					protxSettings.getCurrency());
			params.add(nvcurrency);

			NameValuePair nvdescription = new NameValuePair("Description",
					protxSettings.getDescription());
			params.add(nvdescription);

			String originalOrderVPSTxId = (String) originalOrder.getDetails()
					.get("VPSTxId");
			String originalOrderInvoiceNumber = (String) originalOrder
					.getInvoiceNumber();
			String originalOrderSecurityKey = (String) originalOrder
					.getDetails().get("SecurityKey");
			String originalOrderTxAuthNo = (String) originalOrder.getDetails()
					.get("TxAuthNo");

			NameValuePair relatedTx = new NameValuePair("RelatedVPSTxId",
					originalOrderVPSTxId);
			params.add(relatedTx);

			NameValuePair relatedVendorTxCode = new NameValuePair(
					"RelatedVendorTxCode", originalOrder.getInvoiceNumber());
			params.add(relatedVendorTxCode);

			NameValuePair relatedSecurityKey = new NameValuePair(
					"RelatedSecurityKey", originalOrderSecurityKey);
			params.add(relatedSecurityKey);

			NameValuePair relatedTxAuthNo = new NameValuePair(
					"RelatedTxAuthNo", originalOrderTxAuthNo);
			params.add(relatedTxAuthNo);

			arParams = new NameValuePair[params.size()];
			int counter = 0;
			for (Iterator iter = params.iterator(); iter.hasNext();) {
				NameValuePair p = (NameValuePair) iter.next();
				// log.debug("sending " + p.getName() + " = " + p.getValue());
				arParams[counter] = p;
				counter++;

			}

		} catch (Exception ex) {
			log.error("error getting parameter from order", ex);
			throw new SiteSolvedException("Missing parameter");
		}
		log.debug("sending auth request to protx: " + post.toString());
		String result = URLReader.postToUrl(post.toString(), arParams);

		log.debug("received result: " + result);

		ProtxResult protxResult = new ProtxResult(result);
		return protxResult;

	}

	public static SiteOrderDto addResultData(SiteOrderDto order,
			ProtxResult result) {
		if (order.getDetails() == null)
			order.setDetails(new HashMap());
		order.getDetails().put("Status", result.getStatus());
		order.getDetails().put("StatusDetail", result.getStatusDetail());
		order.getDetails().put("VPSTxId", result.getVPSTxId());
		order.getDetails().put("TxAuthNo", result.getTxAuthNo());
		order.getDetails().put("SecurityKey", result.getSecurityKey());
		return order;
	}

	public static ProtxResult process3DSecureCallback(ProtxResult protxResult, String mode) throws SiteSolvedException {
		StringBuffer post = new StringBuffer();
		NameValuePair[] arParams = null;
		try {

			// String testUrl =
			// "https://ukvpstest.protx.com/VPSDirectAuth/Callback3D.asp";
			String testUrl = "https://ukvpstest.protx.com/vspgateway/service/direct3dcallback.vsp";
			// String liveUrl =
			// "https://ukvps.protx.com/VPSDirectAuth/Callback3D.asp";
			String liveUrl = "https://live.sagepay.com/gateway/service/direct3dcallback.vsp";

			String simulationUrl = "https://ukvpstest.protx.com/VSPSimulator/VSPDirectCallback.asp";

			log.debug("processing 3d secure callback request");

			WebsiteDelegate wd = new WebsiteDelegate();
			
			String url = "";
			if (mode.equals("live"))
				url = liveUrl;
			if (mode.equals("test"))
				url = testUrl;
			if (mode.equals("simulation"))
				url = simulationUrl;

			log.debug("mode is " + mode);
			log.debug("server url is " + url);

			post.append(url);

			ArrayList params = new ArrayList();
 
			NameValuePair MD = new NameValuePair("MD", protxResult.getMD());
			params.add(MD);

			NameValuePair PARes = new NameValuePair("PaRes", protxResult
					.getPARes());
			params.add(PARes);

			arParams = new NameValuePair[params.size()];
			int counter = 0;
			for (Iterator iter = params.iterator(); iter.hasNext();) {
				NameValuePair p = (NameValuePair) iter.next();
				arParams[counter] = p;
				counter++;

			}

		} catch (Exception ex) {
			log.error("error getting parameter from order", ex);
			throw new SiteSolvedException("Missing parameter");
		}
		log.debug("sending auth request to protx: " + post.toString());
		String result = URLReader.postToUrl(post.toString(), arParams);
		log.debug("received result: " + result);

		protxResult = new ProtxResult(result);
		return protxResult;

	}

}

