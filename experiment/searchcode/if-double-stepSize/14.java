package uconnocalypse.engine;

import java.util.ArrayList;
import java.util.List;
import uconnocalypse.data.EntitySpecies;
import uconnocalypse.data.PhysicalType;

public final class CollisionEngine {

    private final EntityStore entityStore;
    private final double stepTime; // seconds
    private final BoxTree<Entity> staticBoxTree = new BoxTree<>();
    private final BoxTree<Entity> dynamicBoxTree = new BoxTree<>();
    private final BoxTree<Entity> monsterBoxTree = new BoxTree<>();
    private final BoxTree<Entity> playerBoxTree = new BoxTree<>();

    public CollisionEngine(EntityStore entityStore, long tickTime) {
        this.entityStore = entityStore;
        this.stepTime = 0.000000001 * tickTime;
        entityStore.addWatcher(new EntityWatcher() {
            @Override
            protected void added(Entity entity) {
                EntitySpecies species = entity.getSpecies();
                int physicalType = species.getPhysicalType();
                if (species.isStatic() && !PhysicalType.isPlayer(physicalType)
                        && !PhysicalType.isMonster(physicalType)) {
                    staticBoxTree.insert(entity);
                }
            }

            @Override
            protected void removed(Entity entity) {
                EntitySpecies species = entity.getSpecies();
                int physicalType = species.getPhysicalType();
                if (species.isStatic() && !PhysicalType.isPlayer(physicalType)
                        && !PhysicalType.isMonster(physicalType)) {
                    staticBoxTree.remove(entity);
                }
            }
        });
    }

    public void checkCollisions() {
        dynamicBoxTree.clear();
        monsterBoxTree.clear();
        playerBoxTree.clear();
        for (Entity entity : entityStore) {
            int physType = entity.getSpecies().getPhysicalType();
            if (PhysicalType.isMonster(physType)) {
                monsterBoxTree.insert(entity);
            } else if (PhysicalType.isPlayer(physType)) {
                playerBoxTree.insert(entity);
            } else if (!entity.getSpecies().isStatic() &&
                    !PhysicalType.isMiniscule(physType)) {
                dynamicBoxTree.insert(entity);
            }
        }

        // Perform collision checks
        for (Entity entity : entityStore) {
            int physType = entity.getSpecies().getPhysicalType();
            if (PhysicalType.isMonster(physType)) {
                // collide with static, dynamic
                checkCollisionBroad(entity, staticBoxTree);
                checkCollisionBroad(entity, dynamicBoxTree);
            } else if (PhysicalType.isPlayer(physType)) {
                // collide with static, dynamic, monster
                checkCollisionBroad(entity, staticBoxTree);
                checkCollisionBroad(entity, dynamicBoxTree);
                checkCollisionBroad(entity, monsterBoxTree);
            } else if (PhysicalType.isMiniscule(physType)) {
                // collide with static, dynamic, monster, player
                checkCollisionBroad(entity, staticBoxTree);
                checkCollisionBroad(entity, dynamicBoxTree);
                checkCollisionBroad(entity, monsterBoxTree);
                checkCollisionBroad(entity, playerBoxTree);
            } else if (!entity.getSpecies().isStatic()) {
                // collide with static, dynamic (self)
                checkCollisionBroad(entity, staticBoxTree);
                checkCollisionBroad(entity, dynamicBoxTree, true);
            }
        }
    }

    private void checkCollisionBroad(Entity entity, BoxTree<Entity> boxTree) {
        checkCollisionBroad(entity, boxTree, false);
    }

    private static void stepEntity(Entity entity, double stepSize) {
        entity.setXPos(entity.getXPos() + stepSize * entity.getXVel());
        entity.setYPos(entity.getYPos() + stepSize * entity.getYVel());
    }

    private void moveToBoundary(Entity a, Entity b) {
        // The entities collided, and need to be moved.
        // Run 5 iterations of a binary search along their trajectories.
        double stepSize = stepTime / 2;
        stepEntity(a, -stepSize);
        stepEntity(b, -stepSize);
        for (int i = 0; i < 5; i++) {
            stepSize /= 2;
            if (checkCollisionNarrow(a.getAbsoluteGeometry(), b.getAbsoluteGeometry())) {
                // step back again
                stepEntity(a, -stepSize);
                stepEntity(b, -stepSize);
            } else {
                // step forward
                stepEntity(a, stepSize);
                stepEntity(b, stepSize);
            }
        }
        // ensure they definitely aren't still colliding
        if (checkCollisionNarrow(a.getAbsoluteGeometry(), b.getAbsoluteGeometry())) {
            // step back again
            stepEntity(a, -stepSize);
            stepEntity(b, -stepSize);
        }
        // For now (temporary), set velocities to 0.
        a.setXVel(0);
        a.setYVel(0);
        b.setXVel(0);
        b.setYVel(0);
    }
    
    public List<Entity> getCollidingEntities(PhysicalGeometry hitArea) {
        List<Entity> collidingEntities = new ArrayList<>();
        for (Entity possibleCollidee : staticBoxTree.intersect(hitArea)) {
            if (checkCollisionNarrow(hitArea, possibleCollidee.getAbsoluteGeometry())) {
                // For now, don't check for "solid".
                collidingEntities.add(possibleCollidee);
            }
        }
        return collidingEntities;
    }

    private void checkCollisionBroad(Entity entity, BoxTree<Entity> boxTree,
            boolean selfCollision) {
        for (Entity possibleCollidee : boxTree.intersect(entity)) {
            if (checkCollisionNarrow(entity.getAbsoluteGeometry(),
                    possibleCollidee.getAbsoluteGeometry())) {
                if (PhysicalType.isSolid(entity.getSpecies().getPhysicalType())
                        && PhysicalType.isSolid(possibleCollidee.getSpecies().getPhysicalType())) {
                    moveToBoundary(entity, possibleCollidee);
                    entityStore.notifyUpdated(entity);
                    entityStore.notifyUpdated(possibleCollidee);
                }

                // Notify entity watchers
                entityStore.notifyCollided(entity, possibleCollidee);
                if (!selfCollision) {
                    entityStore.notifyCollided(possibleCollidee, entity);
                }
            }
        }
    }

    private boolean checkCollisionNarrow(PhysicalGeometry collider, PhysicalGeometry collidee) {
        // There are three cases:
        //  * circle-circle
        //  * polygon-polygon
        //  * polygon-circle
        boolean circularCollider = collider.isCircular();
        boolean circularCollidee = collidee.isCircular();
        if (circularCollider && circularCollidee) {
            return checkCollisionCircleCircle(collider, collidee);
        } else if (circularCollider && !circularCollidee) {
            // note: reverse collision
            return checkCollisionPolygonCircle(collidee, collider);
        } else if (!circularCollider && circularCollidee) {
            return checkCollisionPolygonCircle(collider, collidee);
        } else {
            return checkCollisionPolygonPolygon(collider, collidee);
        }
    }

    private static double calcB(Point p1, Point p2) {
        return (p2.x - p1.x) / (p1.y - p2.y);
    }

    private static double calcC(Point p1, double b) {
        if (b > 10000 || b < -10000) {
            return -p1.y;
        } else {
            return -(p1.x + b * p1.y);
        }
    }

    private static double calcDist(double b, double c, Point p0) {
        if (b > 10000 || b < -10000) {
            return p0.y + c;
        } else {
            return (p0.x + b * p0.y + c) / Math.sqrt(1 + b * b);
        }
    }

    private static boolean checkIntersect(double b, double c, Iterable<Point> pointsA, Iterable<Point> pointsB) {
        double aMin = Double.POSITIVE_INFINITY;
        double aMax = Double.NEGATIVE_INFINITY;
        double bMin = Double.POSITIVE_INFINITY;
        double bMax = Double.NEGATIVE_INFINITY;
        for (Point pa : pointsA) {
            double dist = calcDist(b, c, pa);
            aMin = Math.min(aMin, dist);
            aMax = Math.max(aMax, dist);
        }
        for (Point pb : pointsB) {
            double dist = calcDist(b, c, pb);
            bMin = Math.min(bMin, dist);
            bMax = Math.max(bMax, dist);
        }
        return (aMin < bMax) && (aMax > bMin);
    }

    private static boolean checkCollisionPolygonPolygon(PhysicalGeometry a, PhysicalGeometry b) {
        List<Point> pointsA = a.getPoints();
        List<Point> pointsB = b.getPoints();
        List<Double> bValuesA = new ArrayList<>();
        List<Double> bValuesB = new ArrayList<>();
        List<Double> cValuesA = new ArrayList<>();
        List<Double> cValuesB = new ArrayList<>();
        // populate b and c values
        bValuesA.add(calcB(pointsA.get(0), pointsA.get(pointsA.size() - 1)));
        cValuesA.add(calcC(pointsA.get(0), bValuesA.get(0)));
        for (int i = 0; i < pointsA.size() - 1; i++) {
            bValuesA.add(calcB(pointsA.get(i), pointsA.get(i + 1)));
            cValuesA.add(calcC(pointsA.get(i + 1), bValuesA.get(bValuesA.size() - 1)));
        }
        bValuesB.add(calcB(pointsB.get(0), pointsB.get(pointsB.size() - 1)));
        cValuesB.add(calcC(pointsB.get(0), bValuesB.get(0)));
        for (int i = 0; i < pointsB.size() - 1; i++) {
            bValuesB.add(calcB(pointsB.get(i), pointsB.get(i + 1)));
            cValuesB.add(calcC(pointsB.get(i + 1), bValuesB.get(bValuesB.size() - 1)));
        }
        // For each pair of b and c values, we need to calculate the maximum and
        // minimum dimension for each polygon, and determine their intersection
        // region.
        boolean collision = true;
        for (int i = 0; i < bValuesA.size(); i++) {
            collision &= checkIntersect(bValuesA.get(i), cValuesA.get(i), pointsA, pointsB);
        }
        for (int i = 0; i < bValuesB.size(); i++) {
            collision &= checkIntersect(bValuesB.get(i), cValuesB.get(i), pointsA, pointsB);
        }
        return collision;
    }

    private static boolean checkCollisionPolygonCircle(PhysicalGeometry polygon, PhysicalGeometry circle) {
        List<Point> points = polygon.getPoints();
        Point center = circle.getCenter();
        double radius = circle.getRadius();
        for (int i = 0; i < points.size(); i++) {
            Point disp = center.subtract(points.get(i));
            if (Math.sqrt(disp.x * disp.x - disp.y * disp.y) < radius) {
                return true;
            }
        }
        for (int i = 0; i < points.size(); i++) {
            double b;
            if (i == points.size() - 1) {
                b = calcB(points.get(i), points.get(0));
            } else {
                b = calcB(points.get(i), points.get(i + 1));
            }
            double c = calcC(points.get(i), b);
            double dist = Math.abs(calcDist(b, c, center));
            if (dist < radius) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkCollisionCircleCircle(PhysicalGeometry a, PhysicalGeometry b) {
        Point centerA = a.getCenter();
        Point centerB = b.getCenter();
        double combinedRadius = a.getRadius() + b.getRadius();
        Point disp = centerA.subtract(centerB);
        if (Math.sqrt(disp.x * disp.x + disp.y * disp.y) < combinedRadius) {
            return true;
        } else {
            return false;
        }
    }
}

