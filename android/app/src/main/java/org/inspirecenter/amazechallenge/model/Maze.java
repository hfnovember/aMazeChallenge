package org.inspirecenter.amazechallenge.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Scanner;

/**
 * @author Nearchos
 *         Created: 14-Aug-17
 */
public class Maze implements Serializable { // consider renaming the class to Grid to accommodate more general grids (instead of just mazes)

    public static final int SHAPE_ONLY_UPPER_SIDE = 0x1; // -
    public static final int SHAPE_ONLY_LOWER_SIDE = 0x2; // _
    public static final int SHAPE_ONLY_LEFT_SIDE  = 0x4; // |
    public static final int SHAPE_ONLY_RIGHT_SIDE = 0x8; //  |

    private final int gridSize;
    private final int[][] grid;
    private Position startingPosition;
    private Position targetPosition;

    public Maze(final InputStream inputStream) {
        final Scanner scanner = new Scanner(inputStream);
        gridSize = scanner.nextInt();
        grid = new int[gridSize][gridSize];
        scanner.nextLine(); // reads the rest of the first line (containing the gridSize)
        for (int row = 0; row < gridSize; row++) {
            final String line = scanner.nextLine();
            for (int col = 0; col < gridSize; col++) {
                grid[row][col] = Integer.parseInt(Character.toString(line.charAt(col)), 16);
            }
        }

        final int startingPositionRow = scanner.nextInt();
        final int startingPositionCol = scanner.nextInt();
        startingPosition = new Position(startingPositionRow, startingPositionCol);
        final int targetPositionRow = scanner.nextInt();
        final int targetPositionCol = scanner.nextInt();
        targetPosition = new Position(targetPositionRow, targetPositionCol); // top right
    }

    public Maze(final int gridSize, final int[][] grid, final Position startingPosition, final Position targetPosition) {
        this.gridSize = gridSize;
        this.grid = grid;
        this.startingPosition = startingPosition;
        this.targetPosition = targetPosition;
    }

    int getGridSize() {
        return gridSize;
    }

    int get(final int row, final int col) {
        return grid[row][col];
    }

    public Position getStartingPosition() {
        return startingPosition;
    }

    public Position getTargetPosition() {
        return targetPosition;
    }

    boolean hasWall(final Position position, final Direction direction) {
        final int shape = get(position.getRow(), position.getCol());
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

    @Override
    public String toString() {
        return "Maze " + getGridSize() + "x" + getGridSize();
    }

    public static Maze parseJSON(final JSONObject jsonObject) throws JSONException {
        final int gridSize = jsonObject.getInt("size");
        final int[][] grid = new int[gridSize][gridSize];
        final Scanner scanner = new Scanner(jsonObject.getString("data"));
        for (int row = 0; row < gridSize; row++) {
            final String line = scanner.nextLine();
            for (int col = 0; col < gridSize; col++) {
                grid[row][col] = Integer.parseInt(Character.toString(line.charAt(col)), 16);
            }
        }
        final Position startingPosition = Position.parseJSON(jsonObject.getJSONObject("startingPosition"));
        final Position targetPosition = Position.parseJSON(jsonObject.getJSONObject("targetPosition"));
        return new Maze(gridSize, grid, startingPosition, targetPosition);
    }
}