package cn.edu.pku.sei.plde.conqueroverfitting.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReadFile {
	private String source;

	public ReadFile(String filePath) {
		try {
			FileReader fr = new FileReader(filePath);
			BufferedReader br = new BufferedReader(fr);

			String line = "";
			line = br.readLine();
			while (line != null) {
				line = line.replaceAll("&nbsp;", " ");
				line = line.replaceAll("\t", " ");
				line = line.replaceAll("\\s{2,100}", " ");
				source = source + line + "\r\n";
				line = br.readLine();
			}
			br.close();
			fr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getSource() {
		return source;
	}
}
