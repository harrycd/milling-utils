/**
 * 
 */
package uk.ac.cf.milling.utils.simulation;

import java.util.ArrayList;
import java.util.List;

import uk.ac.cf.milling.objects.CuttingToolProfile;
import uk.ac.cf.milling.utils.db.SettingUtils;

/**
 * Methods to generate the cutting tool profile.
 * Implementations for end mill, ball nose mill and chamfer cutter.
 * @author Theocharis Alexopoulos
 *
 */
public class ProfileBuilderUtils {

	/**
	 * @param height - working height of the cutting tool
	 * @return a list of cutting tool profiles for the end mill with the specified characteristics
	 *  
	 */
	public static List<CuttingToolProfile> generateProfileEndMill(int toolId, double height, double radius){
		double elementSize = SettingUtils.getElementSize();
		List<CuttingToolProfile> profiles = new ArrayList<CuttingToolProfile>();

		//Create the profiles at the flat nose of the tool
		int noseRadialElements = (int) (radius/elementSize); //the last element belongs to side profile 
		for (int i = 0; i <= noseRadialElements; i++){
			CuttingToolProfile profile = new CuttingToolProfile();
			profile.setToolId(toolId);
			profile.setDistanceFromCentre(i * elementSize);
			profile.setRadialProfile(true);
			profiles.add(profile);
		}

		//Create the profile along the side of the end mill (axial profile)
		//TODO check which radius is correct. radius or noseRadialElements*elementSize (<=radius)

		//The element at the corner is the last radial and the first axial so its profile is updated
		profiles.get(profiles.size()-1).setAxialProfile(true);

		int sideAxialElements = (int) (1 + height/elementSize);
		for (int i = 1; i < sideAxialElements; i++){
			CuttingToolProfile profile = new CuttingToolProfile();
			profile.setToolId(toolId);
			profile.setDistanceFromNose(i * elementSize);
			profile.setDistanceFromCentre(noseRadialElements*elementSize); 
			profile.setAxialProfile(true);
			profiles.add(profile);
		}
		return profiles;
	}

	public static List<CuttingToolProfile> generateProfileBallNoseMill(int toolId, double toolHeight, double toolRadius, double noseRadius){
		//Check if input data is valid
		if (noseRadius < toolRadius){
			System.out.println("Nose radius cannot be smaller that the tool radius");
			//TODO handle error
			return null;
		}

		List<CuttingToolProfile> profiles = new ArrayList<CuttingToolProfile>();
		double elementSize = SettingUtils.getElementSize();
		int toolAxialElements = (int) (1 + toolHeight/elementSize);
//		int toolRadialElements = (int) (toolRadius/elementSize);

		//Create the nose profile
		/*
		 * https://www.helppost.gr/ypologismos-geometria/kykliko-tmima/
		 * Chord length is c=2*SQRT(r^2 - d^2) so radius is c/2
		 * c = length of chord
		 * r = radius of arc
		 * d = distance of chord from centre of arc
		 */

		int zElement = 0;
		
		double axialDistanceFromArcCentre = noseRadius - zElement*elementSize;
		double radialDistanceFromToolAxialCentre = 0.5 * Math.sqrt(noseRadius*noseRadius - axialDistanceFromArcCentre*axialDistanceFromArcCentre);
		double maxRadialDistanceFromToolAxialCentre = 0; //To smoothen the profile
		while (radialDistanceFromToolAxialCentre < toolRadius){
			CuttingToolProfile profile = new CuttingToolProfile();
			profile.setToolId(toolId);
			profile.setDistanceFromNose(zElement * elementSize);
			profile.setDistanceFromCentre(radialDistanceFromToolAxialCentre);
			profile.setAxialProfile(true);
			profile.setRadialProfile(true);
			profiles.add(profile);
			maxRadialDistanceFromToolAxialCentre = radialDistanceFromToolAxialCentre;

			// Calculation is needed after profile creation otherwise the last loop will create profile with
			// radialDistanceFromToolAxialCentre > toolRadius 
			zElement++;
			axialDistanceFromArcCentre = noseRadius - zElement*elementSize;
			radialDistanceFromToolAxialCentre = 0.5 * Math.sqrt(noseRadius*noseRadius - axialDistanceFromArcCentre*axialDistanceFromArcCentre);
		}
		
		
		//Create the side profile (axial profile)

		for (int i = zElement; i < toolAxialElements; i++){
			CuttingToolProfile profile = new CuttingToolProfile();
			profile.setToolId(toolId);
			profile.setDistanceFromNose(i * elementSize);
			profile.setDistanceFromCentre(maxRadialDistanceFromToolAxialCentre); 
			profile.setAxialProfile(true);
			profiles.add(profile);
		}
		return profiles;
	}
}
