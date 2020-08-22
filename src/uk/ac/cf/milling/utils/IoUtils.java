/**
 * 
 */
package uk.ac.cf.milling.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Theocharis Alexopoulos
 *
 */
public class IoUtils {
	/**
	 * @param pathToFile - The path to the .csv file to read from
	 * @param titleLines - Number of lines containing title/header
	 * @return A list of String arrays containing the data of the .csv file
	 */
	public static List<String[]> readCSVFile(String pathToFile, int titleLines){
		List<String[]> entries = new ArrayList<String[]>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(pathToFile));
			String line;

			// Skip title lines
			for (int i = 0; i < titleLines; i++){
				br.readLine();
			}

			while ((line = br.readLine()) != null) {
				String[] entry = line.split(",");
				entries.add(entry);
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return entries;
	}

	/**
	 * @param filePath - Path of CSV file to read
	 * @param startLine - File line to begin reading from (First line index is 1)
	 * @param endLine - File line to stop reading at
	 * @return A list of String arrays containing the specified lines data 
	 */
	public static List<String[]> readCSVFile(String filePath, int startLine, int endLine){
		List<String[]> entries = new ArrayList<String[]>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line;
			int lineNumber = 1;

			// Skip lines until start line
			while (lineNumber < startLine && br.readLine() != null) {
				lineNumber++;
			}

			while ((line = br.readLine()) != null && endLine >= lineNumber++) {
				String[] entry = line.split(",");
				entries.add(entry);
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return entries;
	}

	/**
	 * @param - filePath - path of csv file to read
	 * @return a String array containing the titles
	 */
	public static String[] getCSVTitles(String filePath){

		List<String[]> titles = readCSVFile(filePath, 1, 1);

		return titles.get(0);

	}

	public static String[] getCommonCSVTitles(File[] files) {
		List<String[]> titlesList = new ArrayList<String[]>();
		for (File file : files) {
			titlesList.add( getCSVTitles(file) );
		}

		//Get the first array of titles and find common with the rest
		List<String> commonTitles = new ArrayList<String>(Arrays.asList(titlesList.get(0))) ;
		for (int i = 1; i < titlesList.size(); i++) {
			List<String> titles = new ArrayList<String>(Arrays.asList(titlesList.get(i)));
			commonTitles.retainAll(titles);
		}

		return commonTitles.toArray(new String[0]);

	}

	/**
	 * @param file - csv file to read
	 * @return a String array containing the titles
	 */
	public static String[] getCSVTitles(File file){
		return getCSVTitles(file.getAbsolutePath());
	}

	/**
	 * @param filePath - file path of the CSV file to read
	 * @return a double[rows][columns] that contains values of csv. 
	 * The lines that contain text (ex. titles) are omitted.
	 */
	public static double[][] getCSVValues(String filePath){
		List<double[]> values = new ArrayList<double[]>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line;

			//read all lines

			while ((line = br.readLine()) != null) {
				if (line.matches("[\\d+[,\\.]]+[\\n|\\r]*")) {//is number separated by , or .
					String[] lineData = line.split(",");
					double[] lineValues = new double[lineData.length];
					int it=0;
					for (String data : lineData) {
						lineValues[it++] = Double.parseDouble(data);
					}
					values.add(lineValues);
				}
			}


			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return values.toArray(new double[][] {});

		//		int rowCount = values.size();
		//		int columnCount = values.get(0).length;
		//		double[][] formattedValues = new double[columnCount][rowCount];
		//		for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
		//			for (int columnIndex = 0; columnIndex < columnCount; columnIndex++)
		//				formattedValues[columnIndex][rowIndex] = values.get(rowIndex)[columnIndex];
		//		}
		//
		//		return formattedValues;
	}

	public static double[][] getCSVValues(File file) {
		return getCSVValues(file.getAbsolutePath());
	}
	
	/**
	 * @param analysisFilePath - the file-path of the file containing the GCode simulation analysis
	 * @return a List<String> containing the analysis blocks of the analysis file
	 */
	public static List<String> readAnalysisFile(String analysisFilePath){
		try {
			return Files.readAllLines(Paths.get(analysisFilePath), Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<String>();
	}

	/**
	 * Adds a column to the end of the specified csv file
	 * @param filePath - The path of the csv file that the column will be added
	 * @param column - String[] containing the new column data. Each cell is a separate line in the file
	 */
	public static void addColumnToCSVFile(String filePath, String[] column){
		try {
			// Read the CSV file and append the new column at the end of each line
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String[] newFile = new String[column.length];
			int i = 0;

			for (String line = br.readLine(); line != null; line = br.readLine(), i++){
				newFile[i] = line + "," + column[i] + "\n"; 
			}
			br.close();

			System.out.println("File lines: " + i + " Column lines: " + column.length);

			//write the new file
			BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
			for (int it = 0; it < i; it++){
				bw.write(newFile[it]);
			}
			bw.close();




		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds a column to the end of the specified CSV file
	 * @param filePath - The path of the CSV file that the column will be added
	 * @param title - Title of the newly inserted column
	 * @param data - The data of the new column. Every cell of the array is a new line
	 */
	public static void addColumnToCSVFile(String filePath, String title, double[] data){
		int fileLines = data.length + 1;
		String[] column = new String[fileLines];

		column[0] = title;
		for (int i = 1; i < fileLines; i++){
			column[i] = String.valueOf(data[i-1]);
		}

		addColumnToCSVFile(filePath, column);
	}

	/**
	 * Adds a column to the end of the specified CSV file
	 * @param filePath - The path of the CSV file that the column will be added
	 * @param title - Title of the newly inserted column
	 * @param data - The data of the new column. Every cell of the array is a new line
	 */
	public static void addColumnToCSVFile(String filePath, String title, long[] data){
		int fileLines = data.length + 1;
		String[] column = new String[fileLines];

		column[0] = title;
		for (int i = 1; i < fileLines; i++){
			column[i] = String.valueOf(data[i-1]);
		}

		addColumnToCSVFile(filePath, column);
	}



	/**
	 * @param pathToFile - path of the .csv file to write to
	 * @param entries - data to write to the file. Every array 
	 * represents one line of the csv file and each of its elements 
	 * will automatically be separated by comma 
	 */
	public static void writeCSVFile(String pathToFile, List<String[]> entries){
		BufferedWriter bw = null;
		FileWriter fw = null;

		try {
			fw = new FileWriter(pathToFile);
			bw = new BufferedWriter(fw);

			for (String[] entry:entries){
				String line = concatenateAttributes(entry)+"\n";
				bw.write(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null)	bw.close();
				if (fw != null)	fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param pathToFile - The path of the .csv file to write to
	 * @param data - a 2D table containing the data to write to the specified file
	 */
	public static void writeCSVFile(String pathToFile, double[][] data){
		BufferedWriter bw = null;
		FileWriter fw = null;

		try {
			fw = new FileWriter(pathToFile);
			bw = new BufferedWriter(fw);

			int columns = data.length;
			int rows = data[0].length;

			for (int row = 0; row < rows; row++){

				String line = "";

				for (int column = 0; column<columns; column++){

					line += data[column][row] + ",";

				}
				line = line.substring(0, line.length() - 1) + "\n";
				bw.write(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null)	bw.close();
				if (fw != null)	fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param pathToFile - The path of the .csv file to write to
	 * @param data - a 2D table containing the data to write to the specified file
	 */
	public static void writeCSVFile(String pathToFile, int[][] data){
		BufferedWriter bw = null;
		FileWriter fw = null;

		try {
			fw = new FileWriter(pathToFile);
			bw = new BufferedWriter(fw);

			int columns = data.length;
			int rows = data[0].length;

			for (int row = 0; row < rows; row++){

				String line = "";

				for (int column = 0; column<columns; column++){

					line += data[column][row] + ",";

				}
				line = line.substring(0, line.length() - 1) + "\n";
				bw.write(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null)	bw.close();
				if (fw != null)	fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}



	/**
	 * Writes to the specified output file deleting previous contents
	 * @param outputFilePath - the filepath of the output file
	 * @param kpis - the KPIs calculated from GCode simulation
	 */
	public static void writeFile(String filePath, String fileData){
		// Write output to file for post processing
		File output = new File(filePath);
		try {
			output.createNewFile();
			Files.write(output.toPath(), fileData.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	/**
	 * @param entry - The values of the attributes to concatenate separated by comma
	 * @return the produced string of comma separated attributes
	 */
	private static String concatenateAttributes(String[] entry) {
		String line = "";
		for (String attribute:entry){
			line += attribute + ",";
		}
		line = line.substring(0, line.length()-1);
		return line;
	}

	/**
	 * @param folderPath - Folder path containing the files to look for
	 * @param fileExtention - Extension of the files to look for
	 * @return
	 */
	public static List<String> getFileNames(String folderPath, String fileExtention){
		List<String> fileNames = new ArrayList<String>();
		final File folder = new File(folderPath);
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) continue;
			String fileName = fileEntry.getName();

			int beginIndex = fileName.length() - fileExtention.length();
			int endIndex = fileName.length();
			if (fileName.substring(beginIndex, endIndex).equals(fileExtention)){
				fileNames.add(fileName);
			}
		}
		return fileNames;
	}

	/**
	 * @param filePath - path to the file to check if exists
	 * @return true if file exists, false if not or if it is a directory
	 */
	public static boolean checkFileExists(String filePath) {
		File file = new File(filePath);
		return file.exists();
	}

}
