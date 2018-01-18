package sdk.voxeet.com.toolkit.utils;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import sdk.voxeet.com.toolkit.views.uitookit.sdk.VoxeetView;
import voxeet.com.sdk.utils.ScreenHelper;

/**
 * Created by kevinleperf on 15/01/2018.
 */

public class CornerHelper {

    private CornerHelper() {

    }

    private static Point getCenterPosition(VoxeetView view) {
        Point point = new Point();
        point.x = (int) (view.getX() + view.getWidth() / 2);
        point.y = (int) (view.getY() + view.getHeight() / 2);

        return point;
    }

    private static Corner getClosestCorner(VoxeetView view, WindowManager windowManager, Context context) {
        Point center = getCenterPosition(view);
        Display display = windowManager.getDefaultDisplay();
        int statusHeight = ScreenHelper.getStatusBarHeight(context);

        int top = statusHeight;
        int left = 0;
        int bottom = display.getHeight();
        int right = display.getWidth();

        Point topLeft = new Point(left, top);
        Point topRight = new Point(right, top);
        Point bottomLeft = new Point(left, bottom);
        Point bottomRight = new Point(right, bottom);

        Corner doubleTopLeft = new Corner(Corner.Type.TopLeft, topLeft);
        Corner doubleTopRight = new Corner(Corner.Type.TopRight, topRight);
        Corner doubleBottomLeft = new Corner(Corner.Type.BottomLeft, bottomLeft);
        Corner doubleBottomRight = new Corner(Corner.Type.BottomRight, bottomRight);

        Corner max = doubleTopLeft;

        if (doubleTopRight.isCloser(max, center)) max = doubleTopRight;
        if (doubleBottomLeft.isCloser(max, center)) max = doubleBottomLeft;
        if (doubleBottomRight.isCloser(max, center)) max = doubleBottomRight;

        return max;
    }

    private static Point getFinalPositionForCorner(VoxeetView view, WindowManager windowManager, Context context, Corner corner) {
        Display display = windowManager.getDefaultDisplay();
        int statusHeight = ScreenHelper.getStatusBarHeight(context);

        switch (corner.getType()) {
            case TopRight:
                return new Point(display.getWidth() - view.getWidth(), statusHeight);
            case BottomLeft:
                return new Point(0, display.getHeight() - view.getHeight());
            case BottomRight:
                return new Point(display.getWidth() - view.getWidth(), display.getHeight() - view.getHeight());
            case TopLeft:
            default:
                return new Point(0, statusHeight);
        }
    }

    public static void sendToCorner(VoxeetView view, WindowManager windowManager, Context context) {
        Corner corner = CornerHelper.getClosestCorner(view, windowManager, context);
        Point closest_corner = CornerHelper.getFinalPositionForCorner(view, windowManager, context, corner);
        view.animate().x(closest_corner.x).y(closest_corner.y).setDuration(200).start();
    }
}
