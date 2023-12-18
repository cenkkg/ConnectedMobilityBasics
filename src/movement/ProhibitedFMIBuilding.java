package movement;

import core.Coord;
import core.Settings;
import core.SimClock;
import input.WKTReader;
import movement.map.MapRoute;
import movement.map.SimMap;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Random Waypoint Movement with a prohibited region where nodes may not move
 * into. The polygon is defined by a *closed* (same point as first and
 * last) path, represented as a list of {@code Coord}s.
 *
 * @author teemuk
 */
public class ProhibitedFMIBuilding
        extends MapBasedMovement {
    //==========================================================================//
    // Settings
    //==========================================================================//
    public static final int SIM_TIME = 30000;
    public static final int SCHEDULE_LEN = 10;
    public static final int STABLE_LOCATION_VISIT_TIME = 70;
    public static final String SIM_TIME_SETTING = "endTime";
    public static final String SCHEDULE_SETTING = "schedule";
    public static final String INVERT_SETTING = "rwpInvert";
    public static final boolean INVERT_DEFAULT = false;
    public static final String STABLE_SETTING = "stable";
    public static final boolean STABLE_DEFAULT = false;
    /**
     * FILES
     */
    public static final String ROUTE_FILE_S = "routeFile";
    public static final String CLASSROOMS_FILE_S = "classroomsFiles";
    public static final String STABLE_LOCATIONS_FILE_S = "stableLocationsFiles";
    public static final String LOCATION_FILE_S = "locationFile";
    public static final double STABLE_LOCATION_PROBABILITY = 0.2;
    //==========================================================================//

    //==========================================================================//
    // Instance vars
    //==========================================================================//
    // Constructor vars
    private int simTime;
    private Coord lastWaypoint;
    private int[] schedule;
    private boolean stable;
    private Coord stableLocation;
    private int lectureTime;
    private List<Coord> polygon;

    // Path related vars
    private List<Coord> classrooms;
    private List<Coord> stableLocations;
    private Coord stableLocationTarget;
    private boolean stableLocationGoing;
    private int stableLocationEnterTime;
    private boolean stableLocationEntered;
    /**
     * Inverted, i.e., only allow nodes to move inside the polygon.
     */
    private boolean invert;
    /**
     * sim map for the model
     */
    private SimMap map = null;
    //==========================================================================//

    public ProhibitedFMIBuilding(final Settings settings) {
        super(settings);
        WKTReader reader = new WKTReader();
        this.simTime = settings.getInt(SIM_TIME_SETTING, SIM_TIME);
        this.invert = settings.getBoolean(INVERT_SETTING, INVERT_DEFAULT);
        this.stable = settings.getBoolean(STABLE_SETTING, STABLE_DEFAULT);
        if (this.stable) {
            try {
                String locationFileName = settings.getSetting(LOCATION_FILE_S);
                this.stableLocation = reader.readPoints(new File(locationFileName)).get(0);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return;
        }
        this.schedule = settings.getCsvInts(SCHEDULE_SETTING, SCHEDULE_LEN);
        this.lectureTime = (this.simTime / SCHEDULE_LEN);
        String buildingFileName = settings.getSetting(ROUTE_FILE_S);
        String[] classroomsFilesNames = settings.getCsvSetting(CLASSROOMS_FILE_S);
        String[] stableLocationsFilesNames = settings.getCsvSetting(STABLE_LOCATIONS_FILE_S);
        try {
            this.polygon = reader.readLines(new File(buildingFileName)).get(0);
            this.classrooms = new ArrayList<>();
            for (String s : classroomsFilesNames) {
                this.classrooms.add(reader.readPoints(new File(s)).get(0));
            }
            this.stableLocations = new ArrayList<>();
            for (String s : stableLocationsFilesNames) {
                this.stableLocations.add(reader.readPoints(new File(s)).get(0));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public ProhibitedFMIBuilding(final ProhibitedFMIBuilding other) {
        // Copy constructor will be used when settings up nodes. Only one
        // prototype node instance in a group is created using the Settings
        // passing constructor, the rest are replicated from the prototype.
        super(other);
        // Remember to copy any state defined in this class.
        this.simTime = other.simTime;

        this.invert = other.invert;
        this.stable = other.stable;
        this.stableLocation = other.stableLocation;

        this.schedule = other.schedule;
        this.lectureTime = other.lectureTime;

        this.polygon = other.polygon;
        this.classrooms = other.classrooms;
        this.stableLocations = other.stableLocations;

        this.stableLocationGoing = other.stableLocationGoing;
        this.stableLocationEnterTime = other.stableLocationEnterTime;
        this.stableLocationEntered = other.stableLocationEntered;
    }


    //==========================================================================//
    // Implementation
    //==========================================================================//
    @Override
    public Coord getInitialLocation() {
        if (this.stable) {
            this.lastWaypoint = this.stableLocation;
        } else {
            do {
                this.lastWaypoint = this.randomCoord();
            } while ((this.invert) ?
                    isOutside(polygon, this.lastWaypoint) :
                    isInside(this.polygon, this.lastWaypoint));
        }
        return this.lastWaypoint;
    }

    @Override
    public Path getPath() {
        // Creates a new path from the previous waypoint to a new one.
        if (this.stable) {
            Path p = new Path(0);
            p.addWaypoint(this.lastWaypoint);
            return p;
        } else if (this.stableLocationTarget == this.lastWaypoint && this.stableLocationGoing) {
            this.stableLocationGoing = false;
            this.stableLocationEntered = true;
            this.stableLocationEnterTime = SimClock.getIntTime();
            Path p = new Path(0);
            p.addWaypoint(this.lastWaypoint);
            return p;
        } else if (this.stableLocationEntered) {
            if ((SimClock.getIntTime() - this.stableLocationEnterTime) <= STABLE_LOCATION_VISIT_TIME) {
                Path p = new Path(0);
                p.addWaypoint(this.lastWaypoint);
                return p;
            } else {
                this.stableLocationEntered = false;
            }
        }
        final Path p;
        p = new Path(super.generateSpeed());
        p.addWaypoint(this.lastWaypoint.clone());
        // Add only one point. An arbitrary number of Coords could be added to
        // the path here and the simulator will follow the full path before
        // asking for the next one.
        Coord c;
        if (SimClock.getIntTime() > this.simTime) {
            p.addWaypoint(this.lastWaypoint.clone());
            return p;
        }
        int period = SimClock.getIntTime() / this.lectureTime;
        period = period == 10 ? 9 : period;
        if (schedule[period] >= 0) {
            // System.out.println("GO TO CLASSROOM");
            c = this.getSpecificCordWoutIntersect(classrooms.get(schedule[period]));
        } else {
            Random rand = new Random();
            double rand_val = rand.nextDouble(1);
            if (rand_val <= STABLE_LOCATION_PROBABILITY) {
                // System.out.println("GO TO STABLE LOCATION");
                this.stableLocationTarget = stableLocations.get(rand.nextInt(stableLocations.size()));
                this.stableLocationGoing = true;
                c = this.getSpecificCordWoutIntersect(stableLocations.get(rand.nextInt(stableLocations.size())));
            } else {
                do {
                    c = this.randomCoord();
                } while (pathIntersects(this.polygon, this.lastWaypoint, c));
            }
        }
        p.addWaypoint(c);

        this.lastWaypoint = c;
        return p;
    }

    @Override
    public ProhibitedFMIBuilding replicate() {
        return new ProhibitedFMIBuilding(this);
    }

    private Coord getSpecificCordWoutIntersect(Coord c) {
        double verMov = c.getY() - this.lastWaypoint.getY();
        double horMov = c.getX() - this.lastWaypoint.getX();
        boolean isFirstPass = true;
        boolean isLastPass = false;
        Coord newC = null;
        while (pathIntersects(this.polygon, this.lastWaypoint, c) && !isLastPass) {
            if (isFirstPass) {
                newC = new Coord(this.lastWaypoint.getX() + horMov, this.lastWaypoint.getY());
            } else {
                newC = new Coord(this.lastWaypoint.getX(), this.lastWaypoint.getY() + verMov);
                isLastPass = true;
            }
            isFirstPass = false;
        }
        if(newC == null) {
            final double midX = 3350.0;
            final double midY = 1150.0;
            newC =  new Coord((midX + this.lastWaypoint.getX())/2, (midY + this.lastWaypoint.getY())/2);
        }
        return newC;
    }

    private Coord randomCoord() {
        return new Coord(
                rng.nextDouble() * super.getMaxX(),
                rng.nextDouble() * super.getMaxY());
    }

    private static boolean pathIntersects(
            final List<Coord> polygon,
            final Coord start,
            final Coord end) {
        final int count = countIntersectedEdges(polygon, start, end);
        return (count > 0);
    }

    private static boolean isInside(
            final List<Coord> polygon,
            final Coord point) {
        final int count = countIntersectedEdges(polygon, point,
                new Coord(-10, 0));
        return ((count % 2) != 0);
    }

    private static boolean isOutside(
            final List<Coord> polygon,
            final Coord point) {
        return !isInside(polygon, point);
    }

    private static int countIntersectedEdges(
            final List<Coord> polygon,
            final Coord start,
            final Coord end) {
        int count = 0;
        for (int i = 0; i < polygon.size() - 1; i++) {
            final Coord polyP1 = polygon.get(i);
            final Coord polyP2 = polygon.get(i + 1);

            final Coord intersection = intersection(start, end, polyP1, polyP2);
            if (intersection == null) continue;

            if (isOnSegment(polyP1, polyP2, intersection)
                    && isOnSegment(start, end, intersection)) {
                count++;
            }
        }
        return count;
    }

    private static boolean isOnSegment(
            final Coord L0,
            final Coord L1,
            final Coord point) {
        final double crossProduct
                = (point.getY() - L0.getY()) * (L1.getX() - L0.getX())
                - (point.getX() - L0.getX()) * (L1.getY() - L0.getY());
        if (Math.abs(crossProduct) > 0.0000001) return false;

        final double dotProduct
                = (point.getX() - L0.getX()) * (L1.getX() - L0.getX())
                + (point.getY() - L0.getY()) * (L1.getY() - L0.getY());
        if (dotProduct < 0) return false;

        final double squaredLength
                = (L1.getX() - L0.getX()) * (L1.getX() - L0.getX())
                + (L1.getY() - L0.getY()) * (L1.getY() - L0.getY());
        if (dotProduct > squaredLength) return false;

        return true;
    }

    private static Coord intersection(
            final Coord L0_p0,
            final Coord L0_p1,
            final Coord L1_p0,
            final Coord L1_p1) {
        final double[] p0 = getParams(L0_p0, L0_p1);
        final double[] p1 = getParams(L1_p0, L1_p1);
        final double D = p0[1] * p1[0] - p0[0] * p1[1];
        if (D == 0.0) return null;

        final double x = (p0[2] * p1[1] - p0[1] * p1[2]) / D;
        final double y = (p0[2] * p1[0] - p0[0] * p1[2]) / D;

        return new Coord(x, y);
    }

    private static double[] getParams(
            final Coord c0,
            final Coord c1) {
        final double A = c0.getY() - c1.getY();
        final double B = c0.getX() - c1.getX();
        final double C = c0.getX() * c1.getY() - c0.getY() * c1.getX();
        return new double[]{A, B, C};
    }

}
