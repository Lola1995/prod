package com.crycetruly.r2d2.model;

/**
 * Created by Elia on 1/6/2018.
 */

public class Point {
    private Double points, fitnessPoint;

    public Point() {
    }

    public Point(Double points, Double fitnessPoint) {
        this.points = points;
        this.fitnessPoint = fitnessPoint;
    }

    public Double getFitnessPoint() {
        return fitnessPoint;
    }

    public void setFitnessPoint(Double fitnessPoint) {
        this.fitnessPoint = fitnessPoint;
    }

    public Double getPoints() {
        return points;
    }

    public void setPoints(Double points) {
        this.points = points;
    }
}
