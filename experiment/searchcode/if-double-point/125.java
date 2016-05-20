package cgeo.geocaching.geopoint;

import cgeo.geocaching.ICoordinates;

import java.util.Locale;
import java.util.Set;



public class Viewport {

    public final Geopoint center;
    public final Geopoint bottomLeft;
    public final Geopoint topRight;

    public Viewport(final ICoordinates point1, final ICoordinates point2) {
        final Geopoint gp1 = point1.getCoords();
        final Geopoint gp2 = point2.getCoords();
        this.bottomLeft = new Geopoint(Math.min(gp1.getLatitude(), gp2.getLatitude()),
                Math.min(gp1.getLongitude(), gp2.getLongitude()));
        this.topRight = new Geopoint(Math.max(gp1.getLatitude(), gp2.getLatitude()),
                Math.max(gp1.getLongitude(), gp2.getLongitude()));
        this.center = new Geopoint((gp1.getLatitude() + gp2.getLatitude()) / 2,
                (gp1.getLongitude() + gp2.getLongitude()) / 2);
    }

    public Viewport(final ICoordinates center, final double latSpan, final double lonSpan) {
        this.center = center.getCoords();
        final double centerLat = this.center.getLatitude();
        final double centerLon = this.center.getLongitude();
        final double latHalfSpan = Math.abs(latSpan) / 2;
        final double lonHalfSpan = Math.abs(lonSpan) / 2;
        bottomLeft = new Geopoint(centerLat - latHalfSpan, centerLon - lonHalfSpan);
        topRight = new Geopoint(centerLat + latHalfSpan, centerLon + lonHalfSpan);
    }

    public double getLatitudeMin() {
        return bottomLeft.getLatitude();
    }

    public double getLatitudeMax() {
        return topRight.getLatitude();
    }

    public double getLongitudeMin() {
        return bottomLeft.getLongitude();
    }

    public double getLongitudeMax() {
        return topRight.getLongitude();
    }

    public Geopoint getCenter() {
        return center;
    }

    public double getLatitudeSpan() {
        return getLatitudeMax() - getLatitudeMin();
    }

    public double getLongitudeSpan() {
        return getLongitudeMax() - getLongitudeMin();
    }

    /**
     * Check whether a point is contained in this viewport.
     *
     * @param point
     *            the coordinates to check
     * @return true if the point is contained in this viewport, false otherwise or if the point contains no coordinates
     */
    public boolean contains(final ICoordinates point) {
        final Geopoint coords = point.getCoords();
        return coords != null
                && coords.getLongitudeE6() >= bottomLeft.getLongitudeE6()
                && coords.getLongitudeE6() <= topRight.getLongitudeE6()
                && coords.getLatitudeE6() >= bottomLeft.getLatitudeE6()
                && coords.getLatitudeE6() <= topRight.getLatitudeE6();
    }

    @Override
    public String toString() {
        return "(" + bottomLeft.toString() + "," + topRight.toString() + ")";
    }

    /**
     * Check whether another viewport is fully included into the current one.
     *
     * @param vp
     *            the other viewport
     * @return true if the vp is fully included into this one, false otherwise
     */
    public boolean includes(final Viewport vp) {
        return contains(vp.bottomLeft) && contains(vp.topRight);
    }

    /**
     * Return the "where" part of the string appropriate for a SQL query.
     *
     * @param dbTable
     *            the database table to use as prefix, or null if no prefix is required
     * @return the string without the "where" keyword
     */
    public String sqlWhere(final String dbTable) {
        final String prefix = dbTable == null ? "" : (dbTable + ".");
        return String.format((Locale) null,
                "%slatitude >= %s and %slatitude <= %s and %slongitude >= %s and %slongitude <= %s",
                prefix, getLatitudeMin(), prefix, getLatitudeMax(), prefix, getLongitudeMin(), prefix, getLongitudeMax());
    }

    /**
     * Return a widened or shrunk viewport.
     *
     * @param factor
     *            multiplicative factor for the latitude and longitude span (> 1 to widen, < 1 to shrink)
     * @return a widened or shrunk viewport
     */
    public Viewport resize(final double factor) {
        return new Viewport(getCenter(), getLatitudeSpan() * factor, getLongitudeSpan() * factor);
    }

    /**
     * Return a viewport that contains the current viewport as well as another point.
     *
     * @param point
     *            the point we want in the viewport
     * @return either the same or an expanded viewport
     */
    public Viewport expand(final ICoordinates point) {
        if (contains(point)) {
            return this;
        }

        final Geopoint coords = point.getCoords();
        final double latitude = coords.getLatitude();
        final double longitude = coords.getLongitude();
        final double latMin = Math.min(getLatitudeMin(), latitude);
        final double latMax = Math.max(getLatitudeMax(), latitude);
        final double lonMin = Math.min(getLongitudeMin(), longitude);
        final double lonMax = Math.max(getLongitudeMax(), longitude);
        return new Viewport(new Geopoint(latMin, lonMin), new Geopoint(latMax, lonMax));
    }

    /**
     * Return the smallest viewport containing all the given points.
     *
     * @param points
     *            a set of points. Point with null coordinates (or null themselves) will be ignored
     * @return the smallest viewport containing the non-null coordinates, or null if no coordinates are non-null
     */
    static public Viewport containing(final Set<? extends ICoordinates> points) {
        Viewport viewport = null;
        for (final ICoordinates point : points) {
            if (point != null && point.getCoords() != null) {
                if (viewport == null) {
                    viewport = new Viewport(point, point);
                } else {
                    viewport = viewport.expand(point);
                }
            }
        }
        return viewport;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Viewport)) {
            return false;
        }
        final Viewport vp = (Viewport) other;
        return bottomLeft.equals(vp.bottomLeft) && topRight.equals(vp.topRight);
    }

    @Override
    public int hashCode() {
        return bottomLeft.hashCode() ^ topRight.hashCode();
    }

}

