package com.example;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Random;

/**
 * copy lorena.t_eo_t1 from '/tmp/t_eo_t1.txt'
 * 
 * CREATE TABLE lorena.t_eo_t1
(
  intid numeric(20,0) NOT NULL,
  strnam character varying(255) NOT NULL,
  intint numeric(20,0),
  datchanged timestamp without time zone NOT NULL,
  strchanged character varying(30) NOT NULL,
  datcreated timestamp without time zone NOT NULL,
  strcreated character varying(30) NOT NULL,
  intversion numeric(9,0) NOT NULL,
  CONSTRAINT pk_t_eo_t1 PRIMARY KEY (intid )
)
WITH (
  OIDS=FALSE
);
ALTER TABLE lorena.t_eo_t1
  OWNER TO nuclos;
 */
public class CreateTestTable {
	
	private static final String FILE = "/home/tpasch2/t_eo_t1.txt";
	
	private static final String ENCODING = "UTF-8";
	
	private static final int LINES = 200000;
	
	//
	
	private long id;
	
	private Random random = new Random();
	
	public static void main(String[] args) throws Exception {
		final CreateTestTable dut = new CreateTestTable();
		final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(FILE), ENCODING));
		try {
			for (int i = 0; i < LINES; ++i) {
				dut.addLine(out);
			}
		}
		finally {
			out.close();
		}
	}
	
	public CreateTestTable() {
	}
	
	public void addLine(Writer out) throws IOException {
		out.append(Long.toString(++id)).append("\t");
		out.append(word()).append("\t");
		out.append(number()).append("\t");
		out.append("2012-11-30 10:17:14.784").append("\t");
		out.append("nuclos").append("\t");
		out.append("2012-11-30 10:17:14.784").append("\t");
		out.append("nuclos").append("\t");
		out.append("0").append("\n");
	}
	
	public String word() {
		final StringBuilder result = new StringBuilder();
		// Null allowed
		// final int len = random.nextInt(20) - 1;
		// Null NOT allowed
		final int len = random.nextInt(20);
		if (len < 0) {
			result.append("\\N");
		}
		else {
			for (int i = 0; i < len; ++i) {
				final char c = (char) (random.nextInt(95) + ' ');
				result.append(c);
			}
		}
		int index;
		while ((index = result.indexOf("\\")) >= 0) {
			result.delete(index, index + 1);
		}
		return result.toString();
	}
	
	public String number() {
		return Integer.toString(random.nextInt());
	}

}

