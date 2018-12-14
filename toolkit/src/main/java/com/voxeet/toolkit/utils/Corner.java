package com.voxeet.toolkit.utils;

import android.graphics.Point;
import android.support.annotation.NonNull;

/**
 * Created by kevinleperf on 20/11/2017.
 */

public class Corner {
    private Point _point;
    private Type _type;

    public Point getPoint() {
        return _point;
    }

    public Type getType() {
        return _type;
    }

    public static enum Type {
        TopLeft,
        TopRight,
        BottomLeft,
        BottomRight
    }

    private Corner() {

    }

    public Corner(Type type, Point point) {
        this();
        _type = type;
        _point = point;
    }

    public boolean isCloser(@NonNull Corner corner, @NonNull Point center) {
        //return if the current point is closer than "corner" (ie distance is lower than)
        return getDistance(_point, center) < getDistance(corner._point, center);
    }

    private double getDistance(Point p1, Point p2) {
        return Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2);
    }
}
