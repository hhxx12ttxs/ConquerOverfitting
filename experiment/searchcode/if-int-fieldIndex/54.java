package spikes.klaus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class ContactListCleaner {

	private static final String COD_DE_AREA = "41";
	private static final String OPERADORA = "21";
	
	private static BufferedReader _reader;
	private static FileWriter _writer;

	private static String[] _fieldNames;

	private static String _line;
	private static String[] _lineValues;
	private static String[] _cleanLineValues;
	private static String _cellPhone;
	private static String _homePhone;
	private static String _businessPhone;

	
	public static void main(String[] args) throws Exception {
		File inFile = new File(args[0]);
		_reader = new BufferedReader(new FileReader(inFile));
		_writer = new FileWriter(args[0] + ".clean.txt");
		try {
			tryToCleanContacts();
		} finally {
			_writer.close();
		}		
	}

	
	private static void tryToCleanContacts() throws IOException {
		processHeader();
		while (readNextLine())
			processLine();
	}


	private static void processLine() throws IOException {
		_lineValues = split(_line);
		cleanLineFields();
		writeCleanLine();
	}


	private static void writeCleanLine() throws IOException {
		writeLine(merge(_cleanLineValues));
	}


	private static void writeLine(String line) throws IOException {
		System.out.println(line);
		_writer.write(line + "\n");
	}

	
	private static boolean readNextLine() throws IOException {
		_line = _reader.readLine();
		return _line != null;
	}


	private static void processHeader() throws IOException {
		String header = _reader.readLine();
		writeLine(header);
		_fieldNames = split(header);
	}

	
	private static String merge(String[] fields) {
		String result = fields[0];
		for (int i = 1; i < fields.length; i++)
			result += "\t" + fields[i];
		return result;
	}


	private static String[] split(String line) {
		return line.split("\t");
	}


	private static void cleanLineFields() {
		_cleanLineValues = _lineValues.clone();
		
		standardizeNames();
		standardizePhones();
	}


	private static void printValues() {
		System.out.println("-----");
		for (int i = 0; i < _lineValues.length; i++) {
			String value = _lineValues[i];
			if (value.isEmpty()) continue;
			printFieldIfInteresting(i, value);
		}
	}


	private static void standardizePhones() {
		try {
			getPhoneFields();
		} catch (WeirdPhoneFormat e) {
			printValues();
			return;
		}

		organizePhoneTypes();
		setFormattedPhoneFields();
	}


	private static void organizePhoneTypes() {
		if (isCellNumber(_cellPhone)) return;
		
		if (isCellNumber(_homePhone)) {
			System.out.println("Switching: " + _homePhone + "->" + _cellPhone);
			String tmp = _homePhone;
			_homePhone = _cellPhone;
			_cellPhone = tmp;
			return;
		}

		if (isCellNumber(_businessPhone)) {
			System.out.println("Switching: " + _businessPhone + "->" + _cellPhone);
			String tmp = _businessPhone;
			_businessPhone = _cellPhone;
			_cellPhone = tmp;
		}
	}


	private static boolean isCellNumber(String phone) {
		if (phone.isEmpty()) return false;
		if (phone.length() != 10) throw new IllegalStateException();
		System.out.println(phone.charAt(phone.length() - 8));
		return phone.charAt(phone.length() - 8) > '5';
	}


	private static void getPhoneFields() throws WeirdPhoneFormat {
		_cellPhone = getSignificantDigits("Telefone celular");
		_homePhone = getSignificantDigits("Telefone residencial");
		_businessPhone = getSignificantDigits("Telefone comercial");
	}


	private static String getSignificantDigits(String phoneField) throws WeirdPhoneFormat {
		String result = getDigits(phoneField);
		
		if (result.isEmpty()) return result;
		
		if (result.startsWith("0")) result = result.replaceFirst("0", "");
		if (result.length() == 12) result = removeOperadora(result);
		
		if (result.length() == 10)
			return result;

		if (result.length() == 8)
			return COD_DE_AREA + result;

		throw new WeirdPhoneFormat(result);
	}


	private static String removeOperadora(String result) {
		return result.substring(2);
	}


	private static String getDigits(String phoneField) throws WeirdPhoneFormat {
		String result = "";
		String value = field(phoneField);
		for (int i = 0; i < value.length(); i++) {
			char character = value.charAt(i);
			checkWeird(character);
			if (	Character.isDigit(character))
				result += character;
		}
		return result;
	}


	private static void checkWeird(char character) throws WeirdPhoneFormat {
		if (!("0123456789 +()-".contains("" + character)))
			throw new WeirdPhoneFormat("Weird Character: " + character); 
	}


	private static void setFormattedPhoneFields() {
		setFormattedPhone("Telefone celular", _cellPhone);
		setFormattedPhone("Telefone residencial", _homePhone);
		setFormattedPhone("Telefone comercial", _businessPhone);
	}


	private static void setFormattedPhone(String field, String phone) {
		if (phone.isEmpty()) {
			setField(field, phone);
			return;
		}

		if (phone.length() != 10) throw new IllegalStateException(phone);

		String formatted = "";
		formatted += "0" + OPERADORA; //0XX
		formatted += "(" + phone.substring(0,2) + ")"; //0XX(41)
		formatted += phone.substring(2, 6) + "-" + phone.substring(6); //0XX(41)9999-1234
		setField(field, formatted);
	}


	private static void standardizeNames() {
		setField("Sobrenome", field("Primeiro nome") + field("Sobrenome"));
		setField("Primeiro nome", "");
	}


	private static void printFieldIfInteresting(int fieldIndex, String value) {
		if (isFieldInteresting(_fieldNames[fieldIndex]))
			System.out.println(_fieldNames[fieldIndex] + ": " + value);
	}


	private static boolean isFieldInteresting(String fieldName) {
//		if (fieldName.equals("Primeiro nome")) return false;
//		if (fieldName.equals("Sobrenome")) return false;
//		if (fieldName.equals("Telefone celular")) return false;
//		if (fieldName.equals("Telefone residencial")) return false;
//		if (fieldName.equals("Telefone comercial")) return false;
		if (fieldName.equals("Birthday")) return false;
		if (fieldName.equals("Datas especiais")) return false;
		if (fieldName.equals("Iniciais")) return false;
		if (fieldName.equals("Particular")) return false;
		if (fieldName.equals("Prioridade")) return false;
		if (fieldName.equals("Sensibilidade")) return false;
		if (fieldName.equals("Sexo")) return false;
		return true;
	}


	private static void setField(String fieldName, String value) {
		_cleanLineValues[fieldIndex(fieldName)] = value;
	}


	private static String field(String fieldName) {
		return _cleanLineValues[fieldIndex(fieldName)];
	}


	private static int fieldIndex(String fieldName) {
		for (int i = 0; i < _fieldNames.length; i++)
			if (_fieldNames[i].equals(fieldName))
				return i;
		throw new IllegalArgumentException("Field '" + fieldName + "' not found.");
	}

}


class WeirdPhoneFormat extends Exception {

	public WeirdPhoneFormat(String message) {
		super(message);
	}
	
}

