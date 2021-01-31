/**
 * 
 */
package uk.ac.cf.milling.utils.webapp;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import uk.ac.cf.milling.objects.CuttingTool;
import uk.ac.cf.milling.objects.Nc;
import uk.ac.cf.milling.utils.data.DataManipulationUtils;
import uk.ac.cf.milling.utils.data.IoUtils;
import uk.ac.cf.milling.utils.db.CarouselUtils;
import uk.ac.cf.milling.utils.db.NcUtils;

/**
 * @author Theocharis Alexopoulos
 *
 */
public class MonitoringUtils {

	/**
	 * Sending the events related to machine monitoring using the provided response.
	 * @param response - the response that the server side event is attached to
	 * @param ncId - the id of the selected numerical control file
	 */
	public static void sendMonitoringEvents(HttpServletResponse response, int ncId){
		//Initiate the process to send data to server
		response.setContentType("text/event-stream");
		response.setCharacterEncoding("UTF-8");
		PrintWriter pw = null;

		Nc nc = NcUtils.getNc(ncId);
		System.out.println("NC id:" + ncId);
		System.out.println("Theor file:" + nc.getAnalysisPath());
		System.out.println("Monit file:" + nc.getMonitoringPath());

//		String[] titlesSim = IoUtils.getCSVTitles(nc.getAnalysisPath());
//		double[][] dataSim = IoUtils.getCSVValues(nc.getAnalysisPath());

		String[] titlesMon = IoUtils.getCSVTitles(nc.getMonitoringPath());
		double[][] dataMon = IoUtils.getCSVValues(nc.getMonitoringPath());

		int dataLength = dataMon.length;
		int timeIndex = DataManipulationUtils.findTitleIndex("t", titlesMon);
		int toolIndex = DataManipulationUtils.findTitleIndex("T", titlesMon);

		double pocketId = 0.0;
		int it = 0;
		long executionTimeStart = System.currentTimeMillis();
		boolean isConnected = true;

		while(it < dataLength && isConnected){ // Every iteration is 1 sample from the machine
			JSONObject sample = new JSONObject();

			// Update sample if cutting tool has changed
			pocketId = updateCuttingToolData(sample, pocketId, dataMon[it][toolIndex]);
			
			// Generate the string to send to the client
			String sampleData = "data:" + addToJson(sample, titlesMon, dataMon[it]) + "\n\n";
			
			// Send the sample data to the client
			isConnected = sendToClient(response, pw, sampleData);
			
			// Wait if needed so client receives data exactly as it would be recorded by the machine monitoring system
			synchroniseWithMonitoringTime(dataMon[it][timeIndex] * 1000, executionTimeStart);
			it++;
		}
		response.setContentType("text/html");
		if (pw != null) {
			pw.close();
		}
	}


	/**
	 * @param response 
	 * @param pw
	 * @param sampleData
	 */
	private static boolean sendToClient(HttpServletResponse response, PrintWriter pw, String sampleData) {
		try {
			pw = response.getWriter();
			pw.print(sampleData);

			response.flushBuffer();
			response.flushBuffer();
		} catch (IOException e) {
			System.err.println("Client aborted the connection");
			pw.close();
			return false;
		}
		return true;

	}


	/**
	 * Pauses execution until sampleTime == currentExecutionTime
	 * 
	 * @param sampleTime
	 * @param executionTimeStart
	 */
	private static void synchroniseWithMonitoringTime(double sampleTime, long executionTimeStart) {
		//Wait if needed to be in sync with the data file.
		try {
			if((sampleTime) > (System.currentTimeMillis() - executionTimeStart)) {
				Thread.sleep((long) (sampleTime - (System.currentTimeMillis() - executionTimeStart)));
			} else {
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}


	/**
	 * @param sample
	 * @param newPocketId 
	 * @param pocketId 
	 */
	@SuppressWarnings("unchecked")
	private static double updateCuttingToolData(JSONObject sample, double pocketId, double newPocketId ) {
		if ( !(pocketId == newPocketId) ){
			CuttingTool tool = CarouselUtils.getCarouselPocketTool(Double.valueOf(newPocketId).intValue());

			//TODO this works only for tools with constant radius
			sample.put("toolRadius", tool.getAxialProfile().get(0).getDistanceFromCentre());
			sample.put("toolHeight", tool.getToolLength());

			System.out.println("Tool changed. New " + tool.toString());
		}
		return newPocketId;
	}


	/**
	 * @param titlesMon
	 * @param ds
	 */
	@SuppressWarnings("unchecked")
	private static JSONObject addToJson(JSONObject json, String[] titlesMon, double[] ds) {
		for (int i = 0; i < titlesMon.length; i++) {
			json.put(titlesMon[i], ds[i]);
		}
		return json;
	}


	/**
	 * @param xCoord
	 * @param yCoord
	 * @param zCoord
	 * @param timeMon
	 * @param timeTh
	 * @return a string containing the parameters formated in JSON so it can be submitted to the client
	 */
	private static String formatResponse(double xCoord, double yCoord, double zCoord, double timeMon, double timeTh){

		String state = "data:{"
				+ "\"xCoord\":" + xCoord + ", "
				+ "\"yCoord\":" + yCoord + ", "
				+ "\"zCoord\":" + zCoord + ", "
				+ "\"thTime\":" + timeTh + ", "
				+ "\"monTime\":" + timeMon + "}\n\n";
		return state;
	}

}
