/**
 * 
 */
package uk.ac.cf.milling.utils.data;

import static com.willwinder.universalgcodesender.gcode.util.Code.G20;
import static com.willwinder.universalgcodesender.gcode.util.Code.G21;
import static com.willwinder.universalgcodesender.gcode.util.Code.G90;
import static com.willwinder.universalgcodesender.gcode.util.Code.G90_1;
import static com.willwinder.universalgcodesender.gcode.util.Code.G91;
import static com.willwinder.universalgcodesender.gcode.util.Code.G91_1;
import static com.willwinder.universalgcodesender.gcode.util.Code.ModalGroup.Motion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Iterables;
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.GcodeParser.GcodeMeta;
import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.processors.CommentProcessor;
import com.willwinder.universalgcodesender.gcode.processors.Stats;
import com.willwinder.universalgcodesender.gcode.processors.WhitespaceProcessor;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.gcode.util.Plane;
import com.willwinder.universalgcodesender.gcode.util.PlaneFormatter;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.GUIBackend;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.types.PointSegment;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader.NotGcodeStreamFile;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import com.willwinder.universalgcodesender.visualizer.LineSegment;

import uk.ac.cf.milling.utils.db.SettingUtils;

/**
 * Adaptor methods to connect UGS library to the simulator's input module
 * @author Theocharis Alexopoulos
 *
 */
public class GCodeAnalyserUGS {
	private GcodeState state;

	/**
	 * Translates a G-Code file to the format used in CSV data files
	 * @param gcodeFilePath - filepath of the file containing the G-Code
	 * @return a List<string> containing the lines of the generated CSV file
	 */
	public List<String[]> parseGCode(String gcodeFilePath) {
		File gcodeFile = new File(gcodeFilePath);
		state = new GcodeState();
		
		GUIBackend backend = new GUIBackend();
		Settings settings = SettingsFactory.loadSettings();
		try {
			backend.applySettings(settings);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		GUIHelpers.openGcodeFile(gcodeFile, backend);
		
		File preprocessedGcodeFile = backend.getProcessedGcodeFile();
		
		GcodeStreamReader gcodeStreamReader;
		List<LineSegment> lines = new ArrayList<LineSegment>();
		double arcSegmentLength = 0.3;

		try {
			gcodeStreamReader = new GcodeStreamReader(preprocessedGcodeFile);

			lines.clear();
			GcodeParser gp = getParser(arcSegmentLength);

			// Save the state
			//TODO set machine tool home position from database config.
			Units units = Units.MM;
			Position start = new Position(0.0, 0.0, 1000.0, units);

			while (gcodeStreamReader.getNumRowsRemaining() > 0) {
				GcodeCommand commandObject = gcodeStreamReader.getNextCommand();
				List<String> commands = gp.preprocessCommand(commandObject.getCommandString(), state);
				for (String command : commands) {
					List<GcodeMeta> points = addCommand(command, commandObject.getCommandNumber());
					for (GcodeMeta meta : points) {
						if (meta.point != null) {
							addLinesFromPointSegment(start, meta.point, arcSegmentLength, lines);
							start.set(meta.point.point());
						}
					}
				}
			}


//			System.out.println("List size: " + gcodeLineList.size());
		} catch (NotGcodeStreamFile | IOException | GcodeParserException e) {
			e.printStackTrace();
		}

		List<String[]> entries = new ArrayList<String[]>();
		String[] title = {"t","X","Y","Z","T","SS","FR"};
		entries.add(title);
		
		double x = 0.0, y = 0.0, z = 0.0;
		double xDistance = 0.0, yDistance = 0.0, zDistance = 0.0;
		double distance = 0.0, time = 0.0, segmentTime = 0.0;
		double elemSize = SettingUtils.getElementSize();
		double maxDist = 0;
		int segments = 0;
		
		for (LineSegment line:lines) {
			maxDist = 0;
			segments = 1; //initialise to one segment from start point to end
			
			double[] points = line.getPoints();
			xDistance = Math.abs(points[3] - points[0]);
			yDistance = Math.abs(points[4] - points[1]);
			zDistance = Math.abs(points[5] - points[2]);
			
			//TODO break down the path to segments
			//The two points may be very far apart so the path has to be broken down to segments
			// check in which axis the tool has moved the most
			
			if (xDistance > elemSize) maxDist = xDistance;
			if (yDistance > elemSize && yDistance > xDistance) maxDist = yDistance;
			if (zDistance > elemSize && zDistance > yDistance && zDistance > xDistance) maxDist = zDistance;
			if (maxDist > 0) segments = (int) Math.ceil(maxDist / elemSize);
			
			
			//calculate time = distance/feedRate 
			distance = Math.sqrt( xDistance*xDistance + yDistance*yDistance + zDistance*zDistance);
			segmentTime = 60 * distance/line.getFeedRate()/segments;
			
			for (int i = 0; i < segments; i++) {
				time += segmentTime;
				x = points[0] + i * (points[3] - points[0]) / segments;
				y = points[1] + i * (points[4] - points[1]) / segments;
				z = points[2] + i * (points[5] - points[2]) / segments;
				String[] csvLine = new String[7];
				csvLine[0] = String.valueOf(time);
				csvLine[1] = String.valueOf(x);
				csvLine[2] = String.valueOf(y);
				csvLine[3] = String.valueOf(z);
				csvLine[4] = String.valueOf(line.getToolhead());;
				csvLine[5] = String.valueOf(line.getSpindleSpeed());
				csvLine[6] = String.valueOf(line.getFeedRate());
				entries.add(csvLine);
			}
			
			
		}
		
		return entries;

	}
	
	private static GcodeParser getParser(double arcSegmentLength) {
        GcodeParser gp = new GcodeParser();
        gp.addCommandProcessor(new CommentProcessor());
        gp.addCommandProcessor(new WhitespaceProcessor());
        //gp.addCommandProcessor(new ArcExpander(true, arcSegmentLength, 4));
        return gp;
    }
	
	/**
     * Turns a point segment into one or more LineSegment. Arcs are expanded.
     * Keeps track of the minimum and maximum x/y/z locations.
     */
	static int lineCounter = 0;
    private List<LineSegment> addLinesFromPointSegment(final Position start, final PointSegment endSegment, double arcSegmentLength, List<LineSegment> ret) {
        // For a line segment list ALL arcs must be converted to lines.
        double minArcLength = 0;
        LineSegment ls;
        endSegment.convertToMetric();
        
        Position end = new Position(endSegment.point());

        // start is null for the first iteration.
        if (start != null) {
            // Expand arc for graphics.
            if (endSegment.isArc()) {
                List<Position> points =
                    GcodePreprocessorUtils.generatePointsAlongArcBDring(
                        start, end, endSegment.center(), endSegment.isClockwise(),
                        endSegment.getRadius(), minArcLength, arcSegmentLength, new PlaneFormatter(endSegment.getPlaneState()));
                // Create line segments from points.
                if (points != null) {
                    Position startPoint = start;
//                    Point3d point = null;
                    for (Position nextPoint : points) {
                        ls = new LineSegment(startPoint, nextPoint, endSegment.getLineNumber());
                        ls.setToolHead(state.tool);
                        ls.setFeedRate(state.feedRate);
                        ls.setSpindleSpeed(state.spindleSpeed);
                        ls.setIsArc(endSegment.isArc());
                        ls.setIsFastTraverse(endSegment.isFastTraverse());
                        ls.setIsZMovement(endSegment.isZMovement());
//                        this.testExtremes(nextPoint);
                        ret.add(ls);
                        lineCounter++;
                        startPoint = nextPoint;
                    }
                }
            // Line
            } else {
                ls = new LineSegment(start, end, endSegment.getLineNumber());
                ls.setToolHead(state.tool);
                ls.setFeedRate(state.feedRate);
                ls.setSpindleSpeed(state.spindleSpeed);
                ls.setIsArc(endSegment.isArc());
                ls.setIsFastTraverse(endSegment.isFastTraverse());
                ls.setIsZMovement(endSegment.isZMovement());
//                this.testExtremes(end);
                ret.add(ls);
                lineCounter++;
            }
        }
        
        if (lineCounter >= 21610) {
        	System.out.println(ret.get(ret.size() - 1));
        	lineCounter = -100000;
        }
        
        return ret;
    }


    /**
     * Add a command to be processed with a line number.
     * @throws GcodeParserException If the command is too long throw an exception
     */
    private List<GcodeMeta> addCommand(String command, int line) throws GcodeParserException {
    	Stats statsProcessor = new Stats();
    	
    	
    	// The below command updates min max 
        //statsProcessor.processCommand(command, state);
        
    	List<GcodeMeta> results = new ArrayList<>();
        // Add command get meta doesn't update the state, so we need to do that
        // manually.
        //List<String> processedCommands = this.preprocessCommand(command);
        Collection<GcodeMeta> metaObjects = processCommand(command, line, state, true);
        if (metaObjects != null) {
            for (GcodeMeta c : metaObjects) {
                if(c.point != null) {
                    results.add(c);
                }
                if (c.state != null) {
                    state = c.state;
                    // Process stats.
                    statsProcessor.processCommand(command, state);
                }
            }
        }
        
        return results;
    }
    
    
    /**
     * Process commend given an initial state. This method will not modify its
     * input parameters.
     * 
     * @param includeNonMotionStates Create gcode meta responses even if there is no motion, for example "F100" will not
     * return a GcodeMeta entry unless this flag is set to true.
     */
    public static List<GcodeMeta> processCommand(String command, int line, final GcodeState inputState,
            boolean includeNonMotionStates)
            throws GcodeParserException {
        List<String> args = GcodePreprocessorUtils.splitCommand(command);
        if (args.isEmpty()) return null;

        // Initialize with original state
        GcodeState state = inputState.copy();

        state.commandNumber = line;
        
        // handle M codes.
        //codes = GcodePreprocessorUtils.parseCodes(args, 'M');
        //handleMCode(for each codes);

        List<String> fCodes = GcodePreprocessorUtils.parseCodes(args, 'F');
        if (!fCodes.isEmpty()) {
            try {
                state.feedRate = Double.parseDouble(Iterables.getOnlyElement(fCodes));
            } catch (IllegalArgumentException e) {
                throw new GcodeParserException("Multiple F-codes on one line.");
            }
        }

        List<String> sCodes = GcodePreprocessorUtils.parseCodes(args, 'S');
        if (!sCodes.isEmpty()) {
            try {
                state.spindleSpeed = Double.parseDouble(Iterables.getOnlyElement(sCodes));
            } catch (IllegalArgumentException e) {
                throw new GcodeParserException("Multiple S-codes on one line.");
            }
        }
        
        List<String> tCodes = GcodePreprocessorUtils.parseCodes(args, 'T');
        if (!tCodes.isEmpty()) {
        	try {
        		state.tool = Integer.parseInt(Iterables.getOnlyElement(tCodes));
        	} catch (IllegalArgumentException e) {
        		throw new GcodeParserException("Multiple S-codes on one line.");
        	}
        }
        
        // Gather G codes.
        Set<Code> gCodes = GcodePreprocessorUtils.getGCodes(args);
        
        boolean hasAxisWords = GcodePreprocessorUtils.hasAxisWords(args);

        // Error to mix group 1 (Motion) and certain group 0 (NonModal) codes (G10, G28, G30, G92)
        Collection<Code> motionCodes = gCodes.stream()
                .filter(c -> c.consumesMotion())
                .collect(Collectors.toList());

        // 1 motion code per line.
        if (motionCodes.size() > 1) {
            throw new GcodeParserException(Localization.getString("parser.gcode.multiple-axis-commands")
                    + ": " + StringUtils.join(motionCodes, ", "));
        }

        // If there are axis words and nothing to use them, add the currentMotionMode.
        if (hasAxisWords && motionCodes.isEmpty() && state.currentMotionMode != null) {
            gCodes.add(state.currentMotionMode);
        }

        // Apply each code to the state.
        List<GcodeMeta> results = new ArrayList<>();
        for (Code i : gCodes) {
                GcodeMeta meta = handleGCode(i, args, line, state, hasAxisWords);
                meta.command = command;
                // Commands like 'G21' don't return a point segment.
                if (meta.point != null) {
                    meta.point.setSpeed(state.feedRate);
                }
                results.add(meta);
        }

        // Return updated state / command.
        if (results.isEmpty() && includeNonMotionStates) {
          GcodeMeta meta = new GcodeMeta();
          meta.state = state;
          meta.command = command;
          meta.code = state.currentMotionMode;
          return Collections.singletonList(meta);
        }
        
        return results;
    }
    
    /**
     * Branch parser to handle specific gcode command.
     * 
     * A copy of the state object should go in the resulting GcodeMeta object.
     */
    private static GcodeMeta handleGCode(final Code code, List<String> args, int line, GcodeState state, boolean hasAxisWords)
            throws GcodeParserException {
        GcodeMeta meta = new GcodeMeta();

        meta.code = code;

        Position nextPoint = null;

        // If it is a movement code make sure it has some coordinates.
        if (code.consumesMotion()) {
            nextPoint = GcodePreprocessorUtils.updatePointWithCommand(args, state.currentPoint, state.inAbsoluteMode);

            if (nextPoint == null) {
                if (!code.motionOptional()) {
                    throw new GcodeParserException(
                            Localization.getString("parser.gcode.missing-axis-commands") + ": " + code);
                }
            }
        }

        if (nextPoint == null && meta.point != null) {
            nextPoint = meta.point.point();
        }

        switch (code) {
            case G0:
                meta.point = addLinearPointSegment(nextPoint, true, line, state);
                break;
            case G1:
                meta.point = addLinearPointSegment(nextPoint, false, line, state);
                break;

            // Arc command.
            case G2:
                meta.point = addArcPointSegment(nextPoint, true, args, line, state);
                break;
            case G3:
                meta.point = addArcPointSegment(nextPoint, false, args, line, state);
                break;

            case G17:
            case G18:
            case G19:
            case G17_1:
            case G18_1:
            case G19_1:
                state.plane = Plane.lookup(code);
                break;

            //inch
            case G20:
                state.isMetric = false;
                state.units = G20;
                state.currentPoint = state.currentPoint.getPositionIn(UnitUtils.Units.INCH);
                break;
            //mm
            case G21:
                state.isMetric = true;
                state.units = G21;
                state.currentPoint = state.currentPoint.getPositionIn(UnitUtils.Units.MM);
                break;

            // Probe: http://linuxcnc.org/docs/html/gcode/g-code.html#gcode:g38
            case G38_2: // probe toward workpiece, stop on contact, signal error if failure
            case G38_3: // probe toward workpiece, stop on contact
            case G38_4: // probe away from workpiece, stop on loss of contact, signal error if failure
            case G38_5: // probe away from workpiece, stop on loss of contact
                meta.point = addProbePointSegment(nextPoint, true, line, state);
                break;

            // These are not used in the visualizer.
            case G54:
            case G55:
            case G56:
            case G57:
            case G58:
            case G59:
            case G59_1:
            case G59_2:
            case G59_3:
                state.offset = code;
                break;

            case G90:
                state.inAbsoluteMode = true;
                state.distanceMode = G90;
                break;
            case G91:
                state.inAbsoluteMode = false;
                state.distanceMode = G91;
                break;

            case G90_1:
                state.inAbsoluteIJKMode = true;
                state.arcDistanceMode = G90_1;
                break;
            case G91_1:
                state.inAbsoluteIJKMode = false;
                state.arcDistanceMode = G91_1;
                break;

            case G93:
            case G94:
            case G95:
                state.feedMode = code;
                break;
            default:
                break;
        }
        if (code.getType() == Motion) {
            state.currentMotionMode = code;
        }
        meta.state = state.copy();
        return meta;
    }
    
    
    private static PointSegment addProbePointSegment(Position nextPoint, boolean fastTraverse, int line, GcodeState state) {
        PointSegment ps = addLinearPointSegment(nextPoint, fastTraverse, line, state);
        ps.setIsProbe(true);
        return ps;
    }

    /**
     * Create a PointSegment representing the linear command.
     */
    private static PointSegment addLinearPointSegment(Position nextPoint, boolean fastTraverse, int line, GcodeState state) {
        if (nextPoint == null) {
            return null;
        }

        PointSegment ps = new PointSegment(nextPoint, line);

        boolean zOnly = false;

        // Check for z-only
        if ((state.currentPoint.x == nextPoint.x) &&
                (state.currentPoint.y == nextPoint.y) &&
                (state.currentPoint.z != nextPoint.z)) {
            zOnly = true;
        }

        ps.setIsMetric(state.isMetric);
        ps.setIsZMovement(zOnly);
        ps.setIsFastTraverse(fastTraverse);

        // Save off the endpoint.
        state.currentPoint = nextPoint;
        return ps;
    }

    /**
     * Create a PointSegment representing the arc command.
     */
    private static PointSegment addArcPointSegment(Position nextPoint, boolean clockwise, List<String> args, int line, GcodeState state) {
        if (nextPoint == null) {
            return null;
        }

        PointSegment ps = new PointSegment(nextPoint, line);

        Position center =
                GcodePreprocessorUtils.updateCenterWithCommand(
                        args, state.currentPoint, nextPoint, state.inAbsoluteIJKMode, clockwise, new PlaneFormatter(state.plane));

        double radius = GcodePreprocessorUtils.parseCoord(args, 'R');

        // Calculate radius if necessary.
        if (Double.isNaN(radius)) {
            radius = Math.sqrt(
                    Math.pow(state.currentPoint.x - center.x, 2.0)
                            + Math.pow(state.currentPoint.y - center.y, 2.0));
        }

        ps.setIsMetric(state.isMetric);
        ps.setArcCenter(center);
        ps.setIsArc(true);
        ps.setRadius(radius);
        ps.setIsClockwise(clockwise);
        ps.setPlaneState(state.plane);

        // Save off the endpoint.
        state.currentPoint = nextPoint;
        return ps;
    }

}
