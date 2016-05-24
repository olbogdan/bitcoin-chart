/*
 * Copyright 2016 Flinbor Bogdanov Oleksandr
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package in.flinbor.bitcoin.view.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import in.flinbor.bitcoin.R;

/**
 * Linear chart with horizontal scrolling
 */
public class LineChartView extends View {

    public static final int HORIZONTAL_GRID_LINES = 10;

    private int xAxisHeight;
    private int xAxisWidth;
    private int graphPadding;
    private int dotRadius;
    private int tagPadding;

    private List<Item> dotList;

    //X-Axis - bottom line
    private Paint xAxisPaint = new Paint();
    //dots
    private Paint dotPaint   = new Paint();
    //lines
    private Paint linePaint  = new Paint();
    //grid lines
    private Paint gridPaint  = new Paint();
    //tags
    private Rect  tagRect    = new Rect();
    private Paint tagPaint   = new Paint();
    private int   height;
    private int   width;


    public LineChartView(Context context) {
        this(context, null);
    }

    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources r = getResources();

        tagPadding   = r.getDimensionPixelSize(R.dimen.line_chart_tag_padding);
        graphPadding = r.getDimensionPixelSize(R.dimen.line_chart_graph_padding);
        dotRadius    = r.getDimensionPixelSize(R.dimen.line_chart_dot_radius);

        tagPaint.setAntiAlias(true);
        tagPaint.setColor(Color.WHITE);

        tagPaint.setTextSize(r.getDimension(R.dimen.font_small));
        tagPaint.setStrokeWidth(r.getDimension(R.dimen.line_chart_tag_stroke));
        tagPaint.setTextAlign(Paint.Align.CENTER);


        xAxisPaint.setAntiAlias(true);
        xAxisPaint.setTextSize(r.getDimension(R.dimen.font_small));
        xAxisPaint.setTextAlign(Paint.Align.CENTER);
        xAxisPaint.setStyle(Paint.Style.FILL);
        xAxisPaint.setColor(r.getColor(R.color.blue_dark));

        dotPaint.setAntiAlias(true);
        dotPaint.setColor(r.getColor(R.color.green_dark));

        linePaint.setAntiAlias(true);
        linePaint.setColor(r.getColor(R.color.green));
        linePaint.setStrokeWidth(r.getDimension(R.dimen.line_chart_lines_stroke));

        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(r.getDimension(R.dimen.line_chart_grid_paint_stroke));
        gridPaint.setPathEffect(new DashPathEffect(new float[]{r.getDimension(R.dimen.line_chart_grid_dash_line),
                r.getDimension(R.dimen.line_chart_grid_dash_space)}, 0));
        gridPaint.setColor(r.getColor(R.color.gray));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //calculate length of bottom line - multiply count of values on the x-axis with size of one value
        width = MeasureSpec.getSize(widthMeasureSpec);
        if (dotList != null) {
            int lengthOfXAxis = xAxisWidth * (dotList.size() - 1);
            int leftAndRightGraphPadding = graphPadding * 2;
            width = lengthOfXAxis + leftAndRightGraphPadding;
        }
        height = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(width, height);
        refreshView();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (dotList != null) {
            drawBackgroundGrid(canvas);
            drawConnectLines(canvas);
            drawDots(canvas);
            drawTags(canvas);
        }
    }

    /**
     * Init view with data set
     *
     * @param yValues     The Integer List for showing yValues.size() must < xAxisValues.size()
     * @param xAxisValues The String List with names of X-Axis points
     */
    public void setDataList(List<Float> yValues, List<String> xAxisValues) {
        if (yValues.size() > xAxisValues.size()) {
            throw new RuntimeException("error in Line Chart: yValues.size() > xAxisValues.size()");
        }
        if (yValues.size() == 0) {
            return;
        }
        setXAxisSize(xAxisValues);
        createDotList(yValues, xAxisValues);
        refreshView();
        requestLayout();
    }

    /**
     * Calculate sizes of X-Axis line (it is X bottom line with x values)
     *
     * @param xAxisValues string values for set to X-Axis
     */
    private void setXAxisSize(List<String> xAxisValues) {
        String longestStr = "";
        int countLines = 1;
        for (String s : xAxisValues) {

            //find longest string
            String[] splitString = s.split("\n");
            for (String spl : splitString) {
                longestStr = spl.length() > longestStr.length() ? spl : longestStr;
            }

            //find max count of lines
            countLines = splitString.length > countLines ? splitString.length : countLines;
        }
        //create Rect with longest string
        Rect r = new Rect();
        xAxisPaint.getTextBounds(longestStr, 0, longestStr.length(), r);

        //height of one text Rect Multiply count of lines
        xAxisHeight = r.height() * countLines;
        xAxisWidth = r.width() + (int) xAxisPaint.measureText(longestStr, 0, 1);
    }

    /**
     * Create Dot list and set values from yValues and xAxisValues lists to it
     *
     * @param yValues Y values to create Dot list with
     * @param xAxisValues X values to create Dot list with
     */
    private void createDotList(List<Float> yValues, List<String> xAxisValues) {
        dotList = new ArrayList<>();
        for (int i = 0; i < yValues.size(); i++) {
            if (yValues.get(i) == 0) {
//                throw new RuntimeException("error in Line Chart: 0 value not supported");
                Log.e("LineView", "error in Line Chart: 0 value not supported");
            }

            Item item = new Item();
            item.yValue = Math.round(yValues.get(i));
            item.xAxisTextValue = xAxisValues.get(i);
            dotList.add(item);
        }
    }

    /**
     * Calculate coordinate of X and Y, add it to dotList invalidate redrawing
     */
    private void refreshView() {
        if (height == 0 || dotList == null || dotList.isEmpty()) {
            return;
        }

        int maxYValue = Integer.MAX_VALUE;
        int minYValue = Integer.MAX_VALUE;
        for (int i = 0; i < (dotList.size()); i++) {
            Item item = dotList.get(i);
            item.x = (graphPadding + xAxisWidth * i);

            if (maxYValue == Integer.MAX_VALUE) {
                maxYValue = item.yValue;
            } else {
                maxYValue = item.yValue > maxYValue ? item.yValue : maxYValue;
            }
            if (minYValue == Integer.MAX_VALUE) {
                minYValue = item.yValue;
            } else {
                minYValue = item.yValue < minYValue ? item.yValue : minYValue;
            }
        }

        int valueRange;
        if (maxYValue == minYValue) {
            //set max value to prevent division by zero, result line will be drawn at Y = 0
            valueRange = maxYValue;
        } else {
            valueRange = maxYValue - minYValue;
        }
        //calculate available height for graph
        float heightCanvasForGraph = height - xAxisHeight - graphPadding * 2.0f;
        //calculate densities multiplier = Available space / diapason values to show
        float multiplier = heightCanvasForGraph / valueRange;

        for (int i = 0; i < dotList.size(); i++) {
            Item item = dotList.get(i);
            //calculate coordinate: Y(coordinate) = Y(value) - Ymin(value) * densities multiplier
            float normalY = ((item.yValue - minYValue) * multiplier);
            //invert Y coordinate for reversed coordinate system add bottom padding
            float revertedY = heightCanvasForGraph - normalY + graphPadding;

            item.y = Math.round(revertedY);
        }
        postInvalidate();
    }


    /**
     * Draw Tags above all points
     *
     * @param canvas The canvas to draw on
     */
    private void drawTags(@NonNull Canvas canvas) {
        if (dotList != null && !dotList.isEmpty()) {
            for (Item dot : dotList) {
                drawTag(canvas, String.valueOf(dot.yValue), dot.getPoint());
            }
        }
    }

    /**
     * Draw Bubble with text above the point
     *
     * @param canvas The canvas to draw on
     * @param point  The Point consists of the x y coordinates from left bottom to right top
     */
    @SuppressWarnings("ConstantConditions")
    private void drawTag(@NonNull Canvas canvas, @NonNull String tagText, @NonNull Point point) {
        int x = point.x;

        //raise the Tag's position over a circle dot
        int y = point.y - dotRadius * 2;

        //allocated Rectangle by size of text
        tagPaint.getTextBounds(tagText, 0, tagText.length(), tagRect);

        //get Drawable image for Tag background
        NinePatchDrawable tagDrawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tagDrawable = (NinePatchDrawable) getResources().getDrawable(R.drawable.bubble, null);
        } else {
            //noinspection deprecation
            tagDrawable = (NinePatchDrawable) getResources().getDrawable(R.drawable.bubble);
        }

        //set position of drawable, extra stretch for a better look
        tagDrawable.setBounds(x - tagRect.width() / 2 - tagPadding * 2,
                y - tagRect.height() - tagPadding * 2,
                x + tagRect.width() / 2 + tagPadding * 2,
                y + tagPadding * 2);
        tagDrawable.draw(canvas);
        canvas.drawText(tagText, x, y, tagPaint);
    }


    /**
     * Draw Dots over the points
     *
     * @param canvas The canvas to draw on
     */
    private void drawDots(@NonNull Canvas canvas) {
        if (dotList != null && !dotList.isEmpty()) {
            for (Item dot : dotList) {
                canvas.drawCircle(dot.x, dot.y, dotRadius, dotPaint);
            }
        }
    }

    /**
     * Draw connect the points of the chart using the lines
     *
     * @param canvas The canvas to draw on
     */
    private void drawConnectLines(@NonNull Canvas canvas) {
        if (dotList != null && !dotList.isEmpty()) {
            for (int i = 0; i < (dotList.size() - 1); i++) {
                canvas.drawLine(dotList.get(i).x,
                        dotList.get(i).y,
                        dotList.get(i + 1).x,
                        dotList.get(i + 1).y,
                        linePaint);
            }
        }
    }

    /**
     * Draw background grid lines and Axis values
     *
     * @param canvas The canvas to draw on
     */
    private void drawBackgroundGrid(@NonNull Canvas canvas) {
        //draw vertical grid
        for (int i = 0; i < dotList.size(); i++) {
            canvas.drawLine(dotList.get(i).x, 0, dotList.get(i).x,
                    getHeight() - xAxisHeight * 2, gridPaint);
        }

        //draw horizontal grid
        int maxY = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (Item val : dotList) {
            if (maxY == Integer.MAX_VALUE) {
                maxY = val.y;
            } else {
                maxY = val.y > maxY ? val.y : maxY;
            }
            if (minY == Integer.MAX_VALUE) {
                minY = val.y;
            } else {
                minY = val.y < minY ? val.y : minY;
            }
        }
        int gridStep = (maxY - minY) / HORIZONTAL_GRID_LINES;
        for (int i = 0; i < HORIZONTAL_GRID_LINES + 1; i++) {
            int y = gridStep * i + minY;
            canvas.drawLine(0, y, width, y, gridPaint);
        }

        //draw X-Axis values text
        for (int i = 0; i < dotList.size(); i++) {
            int y = getHeight() - xAxisHeight - getResources().getDimensionPixelSize(R.dimen.spacing_small);//bottom margin
            for (String line : dotList.get(i).xAxisTextValue.split("\n")) {
                int x = graphPadding + xAxisWidth * i;
                canvas.drawText(line, x, y, xAxisPaint);
                y += xAxisPaint.descent() - xAxisPaint.ascent();
            }
        }
    }

    /**
     * the point in the chart
     */
    private class Item {
        private int x; //position on canvas
        private int y; //position on canvas
        private int yValue; //value of item
        private String xAxisTextValue; //text on X-Axis

        Point getPoint() {
            return new Point(x, y);
        }
    }

}
