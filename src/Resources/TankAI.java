package Resources;
import java.awt.geom.Line2D;
import java.util.Random;
import java.awt.*;

public class TankAI {
    private final Tank controlledTank;
    private final TankClient gameClient;
    private static final Random random = new Random();

    private static final int DANGER_HEALTH = 30; // Health threshold to seek cover
    private static final int COVER_CHECK_INTERVAL = 500; // Milliseconds
    private long lastCoverCheck = 0;

    // AI behavior constants
    private static final int TARGETING_RANGE = 300;  // Maximum distance to start pursuing player
    private static final int SHOOTING_RANGE = 200;   // Maximum distance to start shooting
    private static final int MIN_DISTANCE = 100;     // Minimum distance to keep from player
    private static final double DECISION_INTERVAL = 1.0; // Seconds between AI decisions
    private long lastDecisionTime = 0;

    /**
     * Constructor for tank AI controller
     * @param tank The tank this AI controls
     * @param gameClient Reference to main game client
     */
    public TankAI(Tank tank, TankClient gameClient) {
        this.controlledTank = tank;
        this.gameClient = gameClient;
    }

    /**
     * Updates AI behavior
     * Should be called each game frame
     */
    public void update() {
        // Only update AI decision-making at intervals
        if (System.currentTimeMillis() - lastDecisionTime < DECISION_INTERVAL * 1000) {
            return;
        }

        lastDecisionTime = System.currentTimeMillis();
        Tank playerTank = gameClient.getPlayerTank();

        if (!playerTank.isAlive() || !controlledTank.isAlive()) {
            return;
        }

        // Calculate distance and direction to player
        double distance = calculateDistance(playerTank);

        // Update targeting
        updateTargeting(playerTank);

        // Decide movement strategy based on distance
        decideMoveStrategy(distance, playerTank);

        // Decide whether to shoot
        decideToShoot(distance);
    }



    /**
     * Updates tank's barrel direction to face player
     */
    private void updateTargeting(Tank playerTank) {
        int dx = playerTank.getPositionX() - controlledTank.getPositionX();
        int dy = playerTank.getPositionY() - controlledTank.getPositionY();

        // Calculate angle to player
        double angle = Math.atan2(dy, dx);

        // Convert angle to direction
        Tank.Direction newDirection = angleToDirection(angle);
        controlledTank.setBarrelDirection(newDirection);
    }

    /**
     * Decides movement strategy based on distance to player
     */
    private void decideMoveStrategy(double distance, Tank playerTank) {
        if (distance > TARGETING_RANGE) {
            // Too far - move towards player
            moveTowardsPlayer(playerTank);
        } else if (distance < MIN_DISTANCE) {
            // Too close - move away
            moveAwayFromPlayer(playerTank);
        } else {
            // Good distance - strafe or take cover
            tacticalMovement();
        }
    }

    /**
     * Moves tank towards player
     */
    private void moveTowardsPlayer(Tank playerTank) {
        int dx = playerTank.getPositionX() - controlledTank.getPositionX();
        int dy = playerTank.getPositionY() - controlledTank.getPositionY();
        controlledTank.setMovementDirection(angleToDirection(Math.atan2(dy, dx)));
    }

    /**
     * Moves tank away from player
     */
    private void moveAwayFromPlayer(Tank playerTank) {
        int dx = controlledTank.getPositionX() - playerTank.getPositionX();
        int dy = controlledTank.getPositionY() - playerTank.getPositionY();
        controlledTank.setMovementDirection(angleToDirection(Math.atan2(dy, dx)));
    }

    /**
     * Enhanced tactical movement with better cover seeking
     */
    private void tacticalMovement() {
        // Check health status
        if (controlledTank.getHealthPoints() < DANGER_HEALTH) {
            // Low health - prioritize finding cover
            if (System.currentTimeMillis() - lastCoverCheck > COVER_CHECK_INTERVAL) {
                seekSafestCover();
                lastCoverCheck = System.currentTimeMillis();
            }
            return;
        }

        // Normal tactical movement
        if (random.nextInt(100) < 30) {
            performStrafingMovement();
        } else if (random.nextInt(100) < 20) {
            seekTacticalAdvantage();
        }
    }

    /**
     * Seeks and moves towards the safest cover position
     */
    private void seekSafestCover() {
        Point bestPosition = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        // Iterate through all walls to find best cover position
        for (Wall wall : gameClient.getWalls()) {
            Point[] coverPositions = wall.getCoverPositions();

            // Evaluate each possible cover position
            for (Point pos : coverPositions) {
                double score = evaluateCoverPosition(pos, wall);

                // Update if better position found
                if (score > bestScore && isPositionAccessible(pos)) {
                    bestScore = score;
                    bestPosition = pos;
                }
            }
        }

        // Move to the best position if found
        if (bestPosition != null) {
            moveTowardsPosition(bestPosition.x, bestPosition.y);
        } else {
            // If no good cover found, implement fallback behavior
            performEvasiveManeuver();
        }
    }

    /**
     * Checks if a position is accessible
     */
    private boolean isPositionAccessible(Point position) {
        // Check if position is within game bounds
        if (position.x < 0 || position.x > TankClient.GAME_WIDTH ||
                position.y < 0 || position.y > TankClient.GAME_HEIGHT) {
            return false;
        }

        // Check if position is blocked by other tanks
        Rectangle positionBounds = new Rectangle(
                position.x - Tank.TANK_WIDTH/2,
                position.y - Tank.TANK_HEIGHT/2,
                Tank.TANK_WIDTH,
                Tank.TANK_HEIGHT
        );

        for (Tank tank : gameClient.getEnemyTanks()) {
            if (tank != controlledTank && tank.getCollisionBounds().intersects(positionBounds)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Performs evasive maneuver when no good cover is found
     */
    private void performEvasiveManeuver() {
        Tank playerTank = gameClient.getPlayerTank();

        // Move in zigzag pattern away from player
        double angle = Math.atan2(
                controlledTank.getPositionY() - playerTank.getPositionY(),
                controlledTank.getPositionX() - playerTank.getPositionX()
        );

        // Add randomization to movement
        angle += (Math.random() - 0.5) * Math.PI/4;

        // Set new movement direction
        controlledTank.setMovementDirection(angleToDirection(angle));
    }

    /**
     * Evaluates how good a cover position is
     */
    private double evaluateCoverPosition(Point position, Wall wall) {
        Tank playerTank = gameClient.getPlayerTank();
        double distanceToPlayer = DistanceCalculator.calculate(position.x, position.y,
                playerTank.getPositionX(), playerTank.getPositionY());

        Point wallCenter = new Point(
                wall.getPositionX() + wall.getWidth()/2,
                wall.getPositionY() + wall.getHeight()/2);
        double distanceToWall = DistanceCalculator.calculate(
                position.x, position.y,
                wallCenter.x, wallCenter.y);

        // Factors to consider:
        // 1. Is position behind wall relative to player?
        // 2. Is distance to player appropriate?
        // 3. Is position reachable?

        double score = 0;
        if (wall.isPointBehindWall(position.x, position.y,
                playerTank.getPositionX(), playerTank.getPositionY())) {
            score += 100;
        }

        // Prefer positions at medium range
        score -= Math.abs(distanceToPlayer - SHOOTING_RANGE);

        // Prefer positions close to but not too close to wall
        if (distanceToWall < 50) {
            score += 50;
        }

        return score;
    }

    /**
     * Utility class for distance calculations
     */
    private static class DistanceCalculator {
        /**
         * Calculate distance between two points
         */
        public static double calculate(int x1, int y1, int x2, int y2) {
            int dx = x2 - x1;
            int dy = y2 - y1;
            return Math.sqrt(dx * dx + dy * dy);
        }

        /**
         * Calculate distance between tank and target point
         */
        public static double calculate(Tank tank, int targetX, int targetY) {
            return calculate(tank.getPositionX(), tank.getPositionY(), targetX, targetY);
        }

        /**
         * Calculate distance between two tanks
         */
        public static double calculate(Tank tank1, Tank tank2) {
            return calculate(tank1.getPositionX(), tank1.getPositionY(),
                    tank2.getPositionX(), tank2.getPositionY());
        }

        /**
         * Calculate distance between tank and wall
         */
        public static double calculate(Tank tank, Wall wall) {
            return calculate(tank.getPositionX(), tank.getPositionY(),
                    wall.getPositionX(), wall.getPositionY());
        }
    }

    private double calculateDistance(Tank playerTank) {
        return DistanceCalculator.calculate(controlledTank, playerTank);
    }

    /**
     * Finds and moves towards nearest cover
     */
    private void moveToNearestCover() {
        Wall nearestWall = findNearestWall();
        if (nearestWall != null) {
            Point coverPosition = findCoverPosition(nearestWall);
            moveTowardsPosition(coverPosition.x, coverPosition.y);
        }
    }

    /**
     * Performs strafing movement pattern
     */
    private void performStrafingMovement() {
        Tank playerTank = gameClient.getPlayerTank();
        double distance = calculateDistance(playerTank);

        // Strafe perpendicular to player direction
        double angle = Math.atan2(
                playerTank.getPositionY() - controlledTank.getPositionY(),
                playerTank.getPositionX() - controlledTank.getPositionX()
        );

        // Add 90 or -90 degrees for perpendicular movement
        angle += (random.nextBoolean() ? Math.PI/2 : -Math.PI/2);

        controlledTank.setMovementDirection(angleToDirection(angle));
    }

    /**
     * Seeks tactical advantage position
     */
    private void seekTacticalAdvantage() {
        Tank playerTank = gameClient.getPlayerTank();
        Wall[] walls = gameClient.getWalls();

        // Find position that gives good firing angle but keeps some cover
        Point bestPosition = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Wall wall : walls) {
            Point[] positions = wall.getCoverPositions();
            for (Point pos : positions) {
                double score = evaluateTacticalPosition(pos, wall, playerTank);
                if (score > bestScore) {
                    bestScore = score;
                    bestPosition = pos;
                }
            }
        }

        if (bestPosition != null) {
            moveTowardsPosition(bestPosition.x, bestPosition.y);
        }
    }

    /**
     * Evaluates position for tactical advantage
     */
    private double evaluateTacticalPosition(Point position, Wall wall, Tank playerTank) {
        double score = 0;

        // Check if position provides partial cover
        if (hasPartialCover(position, wall, playerTank)) {
            score += 50;
        }

        // Check if position provides good firing angle
        if (hasGoodFiringAngle(position, playerTank)) {
            score += 30;
        }

        // Consider distance to player
        double distance = DistanceCalculator.calculate(
                position.x, position.y,
                playerTank.getPositionX(),
                playerTank.getPositionY()
        );

        if (distance > MIN_DISTANCE && distance < SHOOTING_RANGE) {
            score += 20;
        }

        return score;
    }

    private boolean hasPartialCover(Point position, Wall wall, Tank playerTank) {
        // Calculate if wall provides some cover but still allows firing
        Line2D sightLine = new Line2D.Double(
                playerTank.getPositionX(), playerTank.getPositionY(),
                position.x, position.y
        );

        Rectangle wallBounds = wall.getCollisionBounds();
        return !sightLine.intersects(wallBounds) &&
                position.distance(wall.getPositionX(), wall.getPositionY()) < 100;
    }

    private boolean hasGoodFiringAngle(Point position, Tank playerTank) {
        // Check if position provides clear line of sight to player
        Line2D sightLine = new Line2D.Double(
                position.x, position.y,
                playerTank.getPositionX(), playerTank.getPositionY()
        );

        for (Wall wall : gameClient.getWalls()) {
            if (sightLine.intersects(wall.getCollisionBounds())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds the nearest wall to the tank's current position
     * @return Nearest wall or null if no walls exist
     */
    private Wall findNearestWall() {
        Wall[] walls = gameClient.getWalls();
        Wall nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Wall wall : walls) {
            double distance = DistanceCalculator.calculate(controlledTank, wall);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = wall;
            }
        }
        return nearest;
    }



    /**
     * Moves tank towards a specific position
     * @param targetX X coordinate to move towards
     * @param targetY Y coordinate to move towards
     */
    private void moveTowardsPosition(int targetX, int targetY) {
        int dx = targetX - controlledTank.getPositionX();
        int dy = targetY - controlledTank.getPositionY();
        controlledTank.setMovementDirection(angleToDirection(Math.atan2(dy, dx)));
    }

    /**
     * Finds the best cover position near a wall
     * @param wall Wall to use as cover
     * @return Point representing the best cover position
     */
    private Point findCoverPosition(Wall wall) {
        // Calculate a position behind the wall relative to the player
        Tank playerTank = gameClient.getPlayerTank();
        int dx = wall.getPositionX() - playerTank.getPositionX();
        int dy = wall.getPositionY() - playerTank.getPositionY();

        // Normalize the direction
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance == 0) return new Point(wall.getPositionX(), wall.getPositionY());

        dx = (int)(dx / distance * MIN_DISTANCE);
        dy = (int)(dy / distance * MIN_DISTANCE);

        return new Point(
                wall.getPositionX() + dx,
                wall.getPositionY() + dy
        );
    }

    /**
     * Decides whether to shoot based on conditions
     */
    private void decideToShoot(double distance) {
        if (distance <= SHOOTING_RANGE) {
            // Check if we have clear line of sight
            if (hasLineOfSight(gameClient.getPlayerTank())) {
                // Higher chance to shoot when closer
                int shootChance = (int)(70 * (SHOOTING_RANGE - distance) / SHOOTING_RANGE);
                if (random.nextInt(100) < shootChance) {
                    controlledTank.fireMissile();
                }
            }
        }
    }

    /**
     * Checks if there's clear line of sight to target
     */
    private boolean hasLineOfSight(Tank target) {
        // Implement ray casting to check for obstacles between tank and target
        Line2D sightLine = new Line2D.Double(
                controlledTank.getPositionX(),
                controlledTank.getPositionY(),
                target.getPositionX(),
                target.getPositionY()
        );

        // Check if line intersects any walls
        for (Wall wall : gameClient.getWalls()) {
            if (sightLine.intersects(wall.getCollisionBounds())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Converts angle to tank direction
     */
    private Tank.Direction angleToDirection(double angle) {
        // Convert angle to 8-way direction
        double normalized = Math.toDegrees(angle);
        if (normalized < 0) normalized += 360;

        if (normalized >= 337.5 || normalized < 22.5) return Tank.Direction.R;
        if (normalized >= 22.5 && normalized < 67.5) return Tank.Direction.RD;
        if (normalized >= 67.5 && normalized < 112.5) return Tank.Direction.D;
        if (normalized >= 112.5 && normalized < 157.5) return Tank.Direction.LD;
        if (normalized >= 157.5 && normalized < 202.5) return Tank.Direction.L;
        if (normalized >= 202.5 && normalized < 247.5) return Tank.Direction.LU;
        if (normalized >= 247.5 && normalized < 292.5) return Tank.Direction.U;
        return Tank.Direction.RU;
    }
}
