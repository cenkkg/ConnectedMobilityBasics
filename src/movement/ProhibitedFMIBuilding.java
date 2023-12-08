package movement;

import core.Coord;
import core.Settings;
import core.SettingsError;
import core.SimError;
import input.WKTMapReader;
import input.WKTReader;
import movement.map.DijkstraPathFinder;
import movement.map.MapNode;
import movement.map.MapRoute;
import movement.map.SimMap;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
    /** {@code true} to confine nodes inside the polygon */
    public static final String INVERT_SETTING = "rwpInvert";
    public static final boolean INVERT_DEFAULT = false;
    /** Per node group setting used for selecting a route file ({@value}) */
    public static final String ROUTE_FILE_S = "routeFile";
    /**
     * Per node group setting used for selecting a route's type ({@value}).
     * Integer value from {@link MapRoute} class.
     */
    /** map based movement model's settings namespace ({@value})*/
    public static final String MAP_BASE_MOVEMENT_NS = "MapBasedMovement";
    /** number of map files -setting id ({@value})*/
    public static final String NROF_FILES_S = "nrofMapFiles";
    /** map file -setting id ({@value})*/
    public static final String FILE_S = "mapFile";

    /**
     * Per node group setting for selecting map node types that are OK for
     * this node group to traverse trough. Value must be a comma separated list
     * of integers in range of [1,31]. Values reference to map file indexes
     * (see {@link #FILE_S}). If setting is not defined, all map nodes are
     * considered OK.
     */
    public static final String MAP_SELECT_S = "okMaps";
    /** the indexes of the OK map files or null if all maps are OK */
    private int [] okMapNodeTypes;

    /** how many map files are read */
    private int nrofMapFilesRead = 0;
    /** map cache -- in case last mm read the same map, use it without loading*/
    private static SimMap cachedMap = null;
    /** names of the previously cached map's files (for hit comparison) */
    private static List<String> cachedMapFiles = null;


    //==========================================================================//


    //==========================================================================//
    // Instance vars
    //==========================================================================//
    /** Prototype's reference to all routes read for the group */
    private List<Coord> polygon;
    private Coord lastWaypoint;
    /** Inverted, i.e., only allow nodes to move inside the polygon. */
    private final boolean invert;
    /** sim map for the model */
    private SimMap map = null;
    //==========================================================================//



    //==========================================================================//
    // Implementation
    //==========================================================================//
    @Override
    public Path getPath() {
        // Creates a new path from the previous waypoint to a new one.
        final Path p;
        p = new Path( super.generateSpeed() );
        p.addWaypoint( this.lastWaypoint.clone() );
        // Add only one point. An arbitrary number of Coords could be added to
        // the path here and the simulator will follow the full path before
        // asking for the next one.
        Coord c;
        do {
            c = this.randomCoord();
        } while ( pathIntersects( this.polygon, this.lastWaypoint, c ) );
        p.addWaypoint( c );

        this.lastWaypoint = c;
        return p;
    }

    @Override
    public Coord getInitialLocation() {
        do {
            this.lastWaypoint = this.randomCoord();
        } while ( ( this.invert ) ?
                isOutside( polygon, this.lastWaypoint ) :
                isInside( this.polygon, this.lastWaypoint ) );
        System.out.println(this.lastWaypoint);
        return this.lastWaypoint;
    }

    @Override
    public ProhibitedFMIBuilding replicate() {
        return new ProhibitedFMIBuilding( this );
    }

    private Coord randomCoord() {
        return new Coord(
                rng.nextDouble() * super.getMaxX(),
                rng.nextDouble() * super.getMaxY() );
    }
    //==========================================================================//


    //==========================================================================//
    // API
    //==========================================================================//
    public ProhibitedFMIBuilding( final Settings settings ) {
        super( settings );
        this.invert = settings.getBoolean( INVERT_SETTING, INVERT_DEFAULT );
        String fileName = settings.getSetting(ROUTE_FILE_S);
        WKTReader reader = new WKTReader();
        try {
            polygon = reader.readLines(new File(fileName)).get(0);
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println(polygon);
    }

    public ProhibitedFMIBuilding( final ProhibitedFMIBuilding other ) {
        // Copy constructor will be used when settings up nodes. Only one
        // prototype node instance in a group is created using the Settings
        // passing constructor, the rest are replicated from the prototype.
        super( other );
        // Remember to copy any state defined in this class.
        this.invert = other.invert;
        this.polygon = other.polygon;
    }
    //==========================================================================//


    //==========================================================================//
    // Private - geometry
    //==========================================================================//
    private static boolean pathIntersects(
            final List <Coord> polygon,
            final Coord start,
            final Coord end ) {
        final int count = countIntersectedEdges( polygon, start, end );
        return ( count > 0 );
    }

    private static boolean isInside(
            final List <Coord> polygon,
            final Coord point ) {
        final int count = countIntersectedEdges( polygon, point,
                new Coord( -10,0 ) );
        return ( ( count % 2 ) != 0 );
    }

    private static boolean isOutside(
            final List <Coord> polygon,
            final Coord point ) {
        return !isInside( polygon, point );
    }

    private static int countIntersectedEdges(
            final List <Coord> polygon,
            final Coord start,
            final Coord end ) {
        int count = 0;
        for ( int i = 0; i < polygon.size() - 1; i++ ) {
            final Coord polyP1 = polygon.get( i );
            final Coord polyP2 = polygon.get( i + 1 );

            final Coord intersection = intersection( start, end, polyP1, polyP2 );
            if ( intersection == null ) continue;

            if ( isOnSegment( polyP1, polyP2, intersection )
                    && isOnSegment( start, end, intersection ) ) {
                count++;
            }
        }
        return count;
    }

    private static boolean isOnSegment(
            final Coord L0,
            final Coord L1,
            final Coord point ) {
        final double crossProduct
                = ( point.getY() - L0.getY() ) * ( L1.getX() - L0.getX() )
                - ( point.getX() - L0.getX() ) * ( L1.getY() - L0.getY() );
        if ( Math.abs( crossProduct ) > 0.0000001 ) return false;

        final double dotProduct
                = ( point.getX() - L0.getX() ) * ( L1.getX() - L0.getX() )
                + ( point.getY() - L0.getY() ) * ( L1.getY() - L0.getY() );
        if ( dotProduct < 0 ) return false;

        final double squaredLength
                = ( L1.getX() - L0.getX() ) * ( L1.getX() - L0.getX() )
                + (L1.getY() - L0.getY() ) * (L1.getY() - L0.getY() );
        if ( dotProduct > squaredLength ) return false;

        return true;
    }

    private static Coord intersection(
            final Coord L0_p0,
            final Coord L0_p1,
            final Coord L1_p0,
            final Coord L1_p1 ) {
        final double[] p0 = getParams( L0_p0, L0_p1 );
        final double[] p1 = getParams( L1_p0, L1_p1 );
        final double D = p0[ 1 ] * p1[ 0 ] - p0[ 0 ] * p1[ 1 ];
        if ( D == 0.0 ) return null;

        final double x = ( p0[ 2 ] * p1[ 1 ] - p0[ 1 ] * p1[ 2 ] ) / D;
        final double y = ( p0[ 2 ] * p1[ 0 ] - p0[ 0 ] * p1[ 2 ] ) / D;

        return new Coord( x, y );
    }

    private static double[] getParams(
            final Coord c0,
            final Coord c1 ) {
        final double A = c0.getY() - c1.getY();
        final double B = c0.getX() - c1.getX();
        final double C = c0.getX() * c1.getY() - c0.getY() * c1.getX();
        return new double[] { A, B, C };
    }
}
