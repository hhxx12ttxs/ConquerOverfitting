package cn.edu.pku.sei.plde.conqueroverfitting.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReadFile {
	private String content;

	public ReadFile(String filePath) {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		content = sb.toString();
	}

	public String getContent() {
		return content;
	}
}
