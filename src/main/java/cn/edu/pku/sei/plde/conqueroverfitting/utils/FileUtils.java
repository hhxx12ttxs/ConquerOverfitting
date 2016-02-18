package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class FileUtils {
	public static void writeFile(String fileName, String content) {
		try {
			File outputFile = new File(fileName);
			if (!outputFile.getParentFile().exists())
				outputFile.getParentFile().mkdirs();
			if (outputFile.exists())
				outputFile.delete();
			outputFile.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			bw.write(content + "\r\n");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String ReadFile(String filePath) {
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
		return sb.toString();
	}

	public static ArrayList<String> getJavaFilesInProj(String projectPath) {
		File file = new File(projectPath);
		ArrayList<String> filesPath = new ArrayList<String>();
		LinkedList<File> fileList = new LinkedList<File>();
		fileList.add(file);
		while (!fileList.isEmpty()) {
			File firstFile = fileList.removeFirst();
			File[] subFiles = firstFile.listFiles();
			for (File subFile : subFiles) {
				if (subFile.isDirectory()) {
					fileList.add(subFile);
				} else if (subFile.getName().endsWith(".java")) {
					filesPath.add(subFile.getAbsolutePath());
				}
			}
		}
		return filesPath;
	}
}
