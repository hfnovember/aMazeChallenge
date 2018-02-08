package org.inspirecenter.amazechallenge.controller;

import org.inspirecenter.amazechallenge.algorithms.MazeSolver;
import org.inspirecenter.amazechallenge.algorithms.PlayerMove;
import org.inspirecenter.amazechallenge.model.*;

import java.net.NoRouteToHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import static org.inspirecenter.amazechallenge.model.Grid.SHAPE_ONLY_LEFT_SIDE;
import static org.inspirecenter.amazechallenge.model.Grid.SHAPE_ONLY_LOWER_SIDE;
import static org.inspirecenter.amazechallenge.model.Grid.SHAPE_ONLY_RIGHT_SIDE;
import static org.inspirecenter.amazechallenge.model.Grid.SHAPE_ONLY_UPPER_SIDE;

/**
 * @author Nearchos
 *         Created: 11-Dec-17
 */

public class RuntimeController {

    private static HashMap<String, Integer> doubleTurnsMap = new HashMap<>();
    private static HashMap<String, Integer> lostTurnsMap = new HashMap<>();

    public static void makeMove(final Challenge challenge, final Game game, final Map<String,MazeSolver> playerEmailToMazeSolvers) {
        final Grid grid = challenge.getGrid();

        // then apply next move to active players
        final List<String> playersToMove = game.getActivePlayers();
        for (final String playerEmail : playersToMove) {
            final PlayerPositionAndDirection playerPositionAndDirection = game.getPlayerPositionAndDirection(playerEmail);
            final Player player = game.getPlayer(playerEmail);
            final MazeSolver mazeSolver = playerEmailToMazeSolvers.get(playerEmail);

            if (playerHasLostTurns(playerEmail)) {
                decreasePlayerLostTurnsRemaining(playerEmail);
            } else {
                final PlayerMove nextPlayerMove = mazeSolver == null ? PlayerMove.NO_MOVE : mazeSolver.getNextMove(game);
                applyPlayerMove(grid, game, playerEmail, playerPositionAndDirection, nextPlayerMove);
            }

            // check if a second move is needed
            if (playerHasDoubleTurns(playerEmail)) {
                final PlayerMove nextPlayerMove = mazeSolver == null ? PlayerMove.NO_MOVE : mazeSolver.getNextMove(game);
                applyPlayerMove(grid, game, playerEmail, game.getPlayerPositionAndDirection(playerEmail), nextPlayerMove);
                decreasePlayerDoubleTurnsRemaining(playerEmail);
            }

            //Check the player's health:
            if (player.getHealth().isAtMin()) {
                game.resetPlayer(playerEmail);
                game.setPlayerPositionAndDirection(playerEmail, new PlayerPositionAndDirection(grid.getStartingPosition(), Direction.NORTH));
                game.resetPlayer(playerEmail);
                resetTurnEffects();
            }

            generateItems(game, challenge, grid);
            handlePickableState(game);
        }
    }

    private static void applyPlayerMove(final Grid grid, final Game game, final String playerEmail, final PlayerPositionAndDirection playerPositionAndDirection, final PlayerMove playerMove) {
        Direction direction = playerPositionAndDirection.getDirection();
        Position position = playerPositionAndDirection.getPosition();
        AudioEventListener audioEventListener = game.getAudioEventListener();

        switch (playerMove) {
            case TURN_CLOCKWISE:
                direction = playerPositionAndDirection.getDirection().turnClockwise();
                break;
            case TURN_COUNTERCLOCKWISE:
                direction = direction.turnCounterClockwise();
                break;
            case MOVE_FORWARD:
                if (canMoveForward(grid, playerPositionAndDirection.getPosition(), playerPositionAndDirection.getDirection())) {
                    position = movePlayerForward(playerPositionAndDirection);
                    // handle pickableItems and rewards (i.e. add/substract health etc.)
                    for(int i = 0; i < game.getPickableItems().size(); i++) {
                        PickableItem pickableItem = game.getPickableItems().get(i);
                        if(pickableItem.getPosition().equals(position)) {

                            //Change stats:

                            if (pickableItem.getPickableType() == PickableType.BOMB) {
                                if (pickableItem.getState() == 1 || pickableItem.getState() == 2)
                                    game.getPlayer(playerEmail).getHealth().changeBy(pickableItem.getPickableType().getHealthChange());
                            }
                            else game.getPlayer(playerEmail).getHealth().changeBy(pickableItem.getPickableType().getHealthChange());

                            game.getPlayer(playerEmail).changePointsBy(pickableItem.getPickableType().getPointsChange());

                            //Apply effects:
                            if (pickableItem.getPickableType() == PickableType.SPEEDHACK)
                                addPlayerDoubleTurns(playerEmail, PickableType.SPEEDHACK_TURNS_AMOUNT);
                            else if (pickableItem.getPickableType() == PickableType.TRAP)
                                addPlayerLostTurns(playerEmail, PickableType.TRAP_TURNS_AMOUNT);

                            // if audio event listener set, notify with event
                            if(audioEventListener != null) audioEventListener.onAudioEvent(pickableItem);
                            if (pickableItem.getPickableType() != PickableType.BOMB) game.removePickupItem(i);
                        }
                    }
                }
                break;
            case NO_MOVE:
                // Log.d("grid-challenge", "move: " + playerMove);
                break;
            default:
                throw new RuntimeException("Invalid PlayerMove: " + playerMove);
        }
        final PlayerPositionAndDirection updatedPlayerPositionAndDirection = new PlayerPositionAndDirection(position, direction);
        game.setPlayerPositionAndDirection(playerEmail, updatedPlayerPositionAndDirection);
    }

    public static boolean hasSomeoneReachedTheTargetPosition(final Game game, final Grid grid) {
        final Position targetPosition = grid.getTargetPosition();
        boolean someoneHasReachedTheTargetPosition = false;
        for (final String playerEmail : game.getActivePlayers()) {
            final PlayerPositionAndDirection playerPositionAndDirection = game.getPlayerPositionAndDirection(playerEmail);
            if (targetPosition.equals(playerPositionAndDirection.getPosition())) {
                someoneHasReachedTheTargetPosition = true;
                resetTurnEffects();
            }
        }
        return someoneHasReachedTheTargetPosition;
    }

    /**
     * Checks if the given player (specified by its {@link Position} and {@link Direction} can move forward in the
     * given {@link Grid}.
     *
     * @param grid the grid in which the {@link Player} operates
     * @param position the {@link Position} of the {@link Player}
     * @param direction the {@link Direction} of the {@link Player}
     * @return true iff the player at the given {@link Position} and {@link Direction} can move forward
     */
    public static boolean canMoveForward(final Grid grid, final Position position, final Direction direction) {
        return !RuntimeController.hasWall(grid, position, direction);
    }

    /**
     * Checks if the given player (specified by its {@link Position} and {@link Direction} can move forward in the
     * given {@link Grid}.
     *
     * @param grid the grid in which the {@link Player} operates
     * @param position the {@link Position} of the {@link Player}
     * @param direction the {@link Direction} of the {@link Player}
     * @return true iff the player at the given {@link Position} and {@link Direction} can move forward
     */
    public static boolean canMoveBackward(final Grid grid, final Position position, final Direction direction) {
        final Direction oppositeDirection = direction.opposite();
        return !hasWall(grid, position, oppositeDirection);
    }

    /**
     * Checks if the {@link Player} can move 'left' in the given {@link Grid}, relative to its {@link Position} and
     * {@link Direction}.
     *
     * @param grid the grid in which the {@link Player} operates
     * @param position the {@link Position} of the {@link Player}
     * @param direction the {@link Direction} of the {@link Player}
     * @return true iff the {@link Player} can move 'left', relative to {@link Position} and {@link Direction}
     */
    public static boolean canMoveLeft(final Grid grid, final Position position, final Direction direction) {
        final Direction leftDirection = direction.turnCounterClockwise();
        return !hasWall(grid, position, leftDirection);
    }

    /**
     * Checks if the {@link Player} can move 'right' in the given {@link Grid}, relative to its {@link Position} and
     * {@link Direction}.
     *
     * @param grid the grid in which the {@link Player} operates
     * @param position the {@link Position} of the {@link Player}
     * @param direction the {@link Direction} of the {@link Player}
     * @return true iff the {@link Player} can move 'right', relative to {@link Position} and {@link Direction}
     */
    public static boolean canMoveRight(final Grid grid, final Position position, final Direction direction) {
        final Direction rightDirection = direction.turnClockwise();
        return !hasWall(grid, position, rightDirection);
    }

    public static PickableType.Bias look(final Game game, final Grid grid, final Position position, final Direction direction) {

        switch (direction) {
            case NORTH:
                if (position.getRow() - 1 > 0) {
                    for (PickableItem i : game.getPickableItems()) {
                        if (i.getPosition().getRow() == position.getRow()-1 && i.getPosition().getCol() == position.getCol())
                            return i.getPickableType().getBias();
                    }
                }
                break;
            case SOUTH:
                if (position.getRow() + 1 < grid.getHeight()) {
                    for (PickableItem i : game.getPickableItems()) {
                        if (i.getPosition().getRow() == position.getRow()+1 && i.getPosition().getCol() == position.getCol())
                            return i.getPickableType().getBias();
                    }
                }
                break;
            case EAST:
                if (position.getCol() + 1 < grid.getWidth()) {
                    for (PickableItem i : game.getPickableItems()) {
                        if (i.getPosition().getCol() == position.getCol()+1 && i.getPosition().getRow() == position.getRow())
                            return i.getPickableType().getBias();
                    }
                }
                break;
            case WEST:
                if (position.getCol() - 1 > 0) {
                    for (PickableItem i : game.getPickableItems()) {
                        if (i.getPosition().getCol() == position.getCol()-1 && i.getPosition().getRow() == position.getRow())
                            return i.getPickableType().getBias();
                    }
                }
                break;
        }
        return PickableType.Bias.NONE;
    }

    public static Direction compass(final Position targetPosition, final Position playerPosition) {
        Direction direction = Direction.NORTH;
        int rowDifference = playerPosition.getRow() - targetPosition.getRow();
        int colDifference = playerPosition.getCol() - targetPosition.getCol();

        /*
            NOTE:

                Positive rowDifference => Exit is toward NORTH.
                Negative rowDifference => Exit is toward SOUTH.

                Positive colDifference => Exit is toward WEST.
                Negative rowDifference => Exit is toward EAST.
         */

        Direction predominantEastWestDirection = Direction.EAST;
        Direction predominantNorthSouthDirection = Direction.NORTH;

        if (rowDifference >= 0) predominantNorthSouthDirection = Direction.NORTH;
        else predominantNorthSouthDirection = Direction.SOUTH;

        if (colDifference >= 0) predominantEastWestDirection = Direction.WEST;
        else predominantEastWestDirection = Direction.EAST;

        if (Math.max(Math.abs(rowDifference), Math.abs(colDifference)) == Math.abs(rowDifference)) {
            return predominantNorthSouthDirection;
        }
        else
            return predominantEastWestDirection;

    }

    public static int getGridCell(final Grid grid, final int row, final int col) throws IndexOutOfBoundsException {
        if(col < 0 || col > grid.getWidth()) throw new IndexOutOfBoundsException("col not in bounds [0, " + grid.getWidth() + ")");
        if(row < 0 || row > grid.getHeight()) throw new IndexOutOfBoundsException("row not in bounds [0, " + grid.getHeight() + ")");
        final char c = grid.getData().charAt(row * grid.getWidth() + col);
        return Integer.parseInt(Character.toString(c), 16);
    }

    private static boolean hasWall(final Grid grid, final Position position, final Direction direction) {
        final int shape = getGridCell(grid, position.getRow(), position.getCol());
        switch (direction) {
            case NORTH:
                return (shape & SHAPE_ONLY_UPPER_SIDE) != 0;
            case SOUTH:
                return (shape & SHAPE_ONLY_LOWER_SIDE) != 0;
            case WEST:
                return (shape & SHAPE_ONLY_LEFT_SIDE) != 0;
            case EAST:
                return (shape & SHAPE_ONLY_RIGHT_SIDE) != 0;
            default:
                throw new RuntimeException("Invalid direction: " + direction);
        }
    }

    private static Position movePlayerForward(final PlayerPositionAndDirection playerPositionAndDirection) {
        switch (playerPositionAndDirection.getDirection()) {
            case NORTH:
                return playerPositionAndDirection.getPosition().moveNorth();
            case SOUTH:
                return playerPositionAndDirection.getPosition().moveSouth();
            case WEST:
                return playerPositionAndDirection.getPosition().moveWest();
            case EAST:
                return playerPositionAndDirection.getPosition().moveEast();
            default: throw new RuntimeException("Invalid direction: " + playerPositionAndDirection.getDirection());
        }
    }

    public static boolean allPlayersHaveLost(final Game game) {
        return game.getActivePlayers().isEmpty();
    }

    private static void generateItems(Game game, Challenge challenge, Grid grid) {

        //Generate rewards:
        if (game.getNumOfBiasType(PickableType.Bias.REWARD) < challenge.getMax_rewards()) {
            final Random random = new Random();
            int row = random.nextInt(grid.getHeight());
            int col = random.nextInt(grid.getWidth());
            final Position position = new Position(row, col);

            PickableType type = PickableType.getRandomReward();

            boolean exists = false;
            for (PickableItem i : game.getPickableItems()) {
                if (i.getPosition().equals(position)) {
                    exists = true;
                    break;
                }
            }

            if (!exists && !grid.getTargetPosition().equals(position) && !grid.getStartingPosition().equals(position))
                game.addPickableItem(new PickableItem(position, type));

        }

        //Generate penalties:
        if (game.getNumOfBiasType(PickableType.Bias.PENALTY) < challenge.getMax_penalties()) {
            final Random random = new Random();
            int row = random.nextInt(grid.getHeight());
            int col = random.nextInt(grid.getWidth());
            final Position position = new Position(row, col);

            PickableType type = PickableType.getRandomPenalty();

            boolean exists = false;
            for (PickableItem i : game.getPickableItems()) {
                if (i.getPosition().equals(position)) {
                    exists = true;
                    break;
                }
            }

            if (!exists && !grid.getTargetPosition().equals(position) && !grid.getStartingPosition().equals(position))
                game.addPickableItem(new PickableItem(position, type));

        }
    }

    public static void handlePickableState(Game game) {
        for (int i = 0; i < game.getPickableItems().size(); i++) {
            game.getPickableItems().get(i).reduceState();
            if (game.getPickableItems().get(i).getState() <= 0) game.removePickupItem(i);
        }
    }

    public static int getPlayerDoubleTurnsRemaining(String playerEmail) {
        if (!doubleTurnsMap.containsKey(playerEmail)) return 0;
        return doubleTurnsMap.get(playerEmail);
    }

    public static int getPlayerLostTurnsRemaining(String playerEmail) {
        if (!lostTurnsMap.containsKey(playerEmail)) return 0;
        return lostTurnsMap.get(playerEmail);
    }

    public static boolean playerHasDoubleTurns(String playerEmail) {
        return doubleTurnsMap.containsKey(playerEmail);
    }

    public static boolean playerHasLostTurns(String playerEmail) {
        return lostTurnsMap.containsKey(playerEmail);
    }

    public static void decreasePlayerDoubleTurnsRemaining(String playerEmail) {
        if (playerHasDoubleTurns(playerEmail)) {
            Integer turnsLeft = doubleTurnsMap.get(playerEmail);
            doubleTurnsMap.remove(playerEmail);
            turnsLeft--;
            if (turnsLeft > 0) doubleTurnsMap.put(playerEmail, turnsLeft);
        }
    }

    public static void decreasePlayerLostTurnsRemaining(String playerEmail) {
        if (playerHasLostTurns(playerEmail)) {
            Integer turnsLeft = lostTurnsMap.get(playerEmail);
            lostTurnsMap.remove(playerEmail);
            turnsLeft--;
            if (turnsLeft > 0) lostTurnsMap.put(playerEmail, turnsLeft);
        }
    }

    public static void addPlayerDoubleTurns(String playerEmail, Integer doubleTurnsAmount) {
        doubleTurnsMap.put(playerEmail, doubleTurnsAmount);
    }

    public static void addPlayerLostTurns(String playerEmail, Integer lostTurnsAmount) {
        lostTurnsMap.put(playerEmail, lostTurnsAmount);
    }

    public static void resetTurnEffects() {
        lostTurnsMap = new HashMap<>();
        doubleTurnsMap = new HashMap<>();
    }

}