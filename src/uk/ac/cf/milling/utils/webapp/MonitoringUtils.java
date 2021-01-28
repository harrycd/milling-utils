/**
 * 
 */
package uk.ac.cf.milling.utils.webapp;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import uk.ac.cf.milling.objects.CuttingTool;
import uk.ac.cf.milling.objects.KPIs;
import uk.ac.cf.milling.objects.Nc;
import uk.ac.cf.milling.utils.data.DataManipulationUtils;
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
	@SuppressWarnings("unchecked")
	public static void sendMonitoringEvents(HttpServletResponse response, int ncId){
		//Initiate the process to send data to server
		response.setContentType("text/event-stream");
		response.setCharacterEncoding("UTF-8");
		PrintWriter pw = null;
		
		Nc nc = NcUtils.getNc(ncId);
		System.out.println("NC id:" + ncId);
		System.out.println("Theor file:" + nc.getAnalysisPath());
		System.out.println("Monit file:" + nc.getMonitoringPath());
		KPIs kpiTh = DataManipulationUtils.parseDataFile(nc.getAnalysisPath());
		KPIs kpiMon = DataManipulationUtils.parseDataFile(nc.getMonitoringPath());
		double[] timeMon = kpiMon.getTimePoints();
		double[] timeTh = kpiTh.getTimePoints();
		double[] xCoord = kpiMon.getToolX();
		double[] yCoord = kpiMon.getToolY();
		double[] zCoord = kpiMon.getToolZ();
		String[] pocketIds = kpiMon.getCarouselPocketId();
		String pocketId = "";
		int kpiThLength = xCoord.length;
		
		int it = 0;
		while(it < kpiThLength){
			JSONObject json = new JSONObject();
			if (!pocketId.equals(pocketIds[it])){
				CuttingTool tool = CarouselUtils.getCarouselPocketTool(Double.valueOf(pocketIds[it]).intValue());
				//TODO this works only for tools with constant radius
				pocketId = pocketIds[it];
				json.put("toolRadius", tool.getAxialProfile().get(0).getDistanceFromCentre());
				json.put("toolHeight", tool.getToolLength());
				System.out.println("Tool changed. Diameter:" + tool.getAxialProfile().get(0).getDistanceFromCentre() + " Height:" + tool.getToolLength());
			}
			json.put("xCoord", xCoord[it]);
			json.put("yCoord", yCoord[it]);
			json.put("zCoord", zCoord[it]);
			json.put("thTime", timeTh[it]);
			json.put("monTime", timeMon[it]);
			
			
			String state = "data:" + json.toJSONString() + "\n\n";
//			System.out.println(state);
//			state = formatResponse(xCoord[it], yCoord[it], zCoord[it], timeMon[it], timeTh[it]);
//			System.out.println(state);
			
			try {
				pw = response.getWriter();
				pw.print(state);
				
				response.flushBuffer();
				response.flushBuffer();
//				System.out.println(it + " of " + kpiThLength);
				Thread.sleep(100);
			} catch (IOException | InterruptedException e) {
				System.out.println("Exception of type: " + e.getClass().getCanonicalName());
				pw.close();
				break;
			}
			it++;
		}
		pw.close();
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
