package org.inspirecenter.amazechallenge.model;

import org.inspirecenter.amazechallenge.algorithms.MazeSolver;

import java.io.Serializable;

import static org.inspirecenter.amazechallenge.model.Direction.NORTH;

/**
 * @author Nearchos Paspallis
 * 17/08/2017.
 */
public class Player implements Serializable {

    private String name;
    private Position position;
    private Direction direction;
    private ShapeColor color;
    private Shape shape;
    private Class<MazeSolver> mazeSolverClass;

    public Player(final String name, final ShapeColor color, final Shape shape, final Class<MazeSolver> mazeSolverClass) {
        this.name = name;
        this.position = new Position(0, 0);
        this.direction = NORTH;
        this.color = color;
        this.shape = shape;
        this.mazeSolverClass = mazeSolverClass;
    }

    public String getName() {
        return name;
    }

    void init(final Position position, final Direction direction) {
        this.position = position;
        this.direction = direction;
    }

    public Position getPosition() {
        return position;
    }

    public Direction getDirection() {
        return direction;
    }

    public void turnClockwise() {
        this.direction = direction.turnClockwise();
    }

    public void turnCounterClockwise() {
        this.direction = direction.turnCounterClockwise();
    }

    void moveForward() {
        switch (direction) {
            case NORTH:
                position = position.moveNorth();
                break;
            case SOUTH:
                position = position.moveSouth();
                break;
            case WEST:
                position = position.moveWest();
                break;
            case EAST:
                position = position.moveEast();
                break;
            default: throw new RuntimeException("Invalid direction: " + direction);
        }
    }

    public ShapeColor getColor() {
        return color;
    }

    public Shape getShape() {
        return shape;
    }

    public Class<MazeSolver> getMazeSolverClass() {
        return mazeSolverClass;
    }

    @Override public String toString() {
        return name;
    }
}