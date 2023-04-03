/**
 * Rick Ammann
 * CSC 133 - Section 03
 * Homework 2 - Simple Object Paint
 */

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.util.LinkedList;

/* Defines method to draw each object
 * Defines boolean dragUpdate which handles object updates on drag
 */
interface ShapeObject{
    void draw(GraphicsContext g);
    boolean dragUpdate();
}

/* Base class for ActionTool that builds tool boxes
 * as well as implement activation/deactivation of tools.
 */
abstract class AbstractTool extends StackPane {
    private Rectangle rectangle;

    public AbstractTool(Color color){
        this.rectangle = new Rectangle();
        rectangle.setWidth(SimplePaintObjects.CELL_W);
        rectangle.setHeight(SimplePaintObjects.CELL_W);
        rectangle.setFill(color);
        rectangle.setStroke(SimplePaintObjects.TOOL_RECT_BG);
        rectangle.setStrokeWidth(5);
        this.getChildren().add(rectangle);
    }

    /* When activated change rectangle outline to current color*/
    public void activate(){
        this.rectangle.setStroke(rectangle.getFill());
    }

    /* When deactivated change color back to white */
    public void deactivate(){
        this.rectangle.setStroke(SimplePaintObjects.TOOL_RECT_BG);
    }
}

class ColorTool extends AbstractTool{
    Color color;
    public ColorTool(Color color) {
        super(color);
        this.color = color;
        this.setOnMousePressed(e -> activate());
    }
}

/* Builds the clear button */
class ActionTool extends AbstractTool{
        public ActionTool(String text) {
        super(SimplePaintObjects.TOOL_RECT_FG);

        Label cmdName = new Label(text);
        cmdName.setTextFill(SimplePaintObjects.TOOL_FG);
        cmdName.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        this.getChildren().add(cmdName);

    }
}


abstract class ShapeTool extends AbstractTool{
    public ShapeTool(Color color) {
        super(color);
    }

    abstract public void draw(
            GraphicsContext g,
            Color color,
            Point2D start,
            Point2D end);

    abstract public ShapeObject getPaintShape();

}

/* Draws strokes from where mouse event starts and ends  */
class LineSegmentShape implements ShapeObject {
    private final Point2D start;
    private final Point2D end;
    private final Color color;
    private final int penWidth;
    public LineSegmentShape(
            Point2D start,
            Point2D end,
            Color color,
            int penWidth) {
        this.penWidth = penWidth;
        this.start = start;
        this.end = end;
        this.color = color;
    }

    @Override
    public void draw(GraphicsContext g) {
        g.setLineWidth(penWidth);
        g.setStroke(color);
        g.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());

    }
    @Override
    public boolean dragUpdate(){
        return true;           // returns true to constantly update pointTool
    }
}

class PointTool extends ShapeTool{
    private LineSegmentShape currentLineSegment;
    private final int penWidth;
    public PointTool(int penWidth){
        super(SimplePaintObjects.TOOL_RECT_FG);
        this.penWidth = penWidth;
        Ellipse toolImage = new Ellipse(penWidth, penWidth);
        toolImage.setStroke(SimplePaintObjects.TOOL_FG);
        toolImage.setFill(SimplePaintObjects.TOOL_FG);

        this.getChildren().add(toolImage);
    }
    @Override
    public void draw(
            GraphicsContext g,
            Color color,
            Point2D start,
            Point2D end){

        currentLineSegment = new LineSegmentShape(start, end, color, penWidth);
        currentLineSegment.draw(g);
    }

    @Override
    public ShapeObject getPaintShape(){
        return currentLineSegment;
    }
}

/* Sets up stroke line with fixed width and does not update on drag */
class LineShape implements ShapeObject{
    private final Point2D start;
    private final Point2D end;
    private final Color color;

    public LineShape(Point2D start, Point2D end, Color color){
        this.start = start;
        this.end = end;
        this.color = color;
    }

    @Override
    public void draw(GraphicsContext g){
        g.setLineWidth(2);
        g.setStroke(color);
        g.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
    }
    @Override
    public boolean dragUpdate(){
        return false;
    }
}

class LineTool extends ShapeTool{
    private LineShape currentLineShape;
    public LineTool(){
        super(SimplePaintObjects.TOOL_RECT_FG);
        Line toolImage = new Line(0,0,45,45);
        toolImage.setStroke(SimplePaintObjects.TOOL_FG);
        toolImage.setFill(SimplePaintObjects.TOOL_FG);

        this.getChildren().add(toolImage);
    }

    @Override
    public void draw(
            GraphicsContext g,
            Color color,
            Point2D start,
            Point2D end){
        currentLineShape = new LineShape(start, end, color);
        currentLineShape.draw(g);
    }
    @Override
    public ShapeObject getPaintShape(){
        return currentLineShape;
    }
}

/* Base class for polygon shapes that sets up the shape
 * size based on the mouse coordinates
 * */
abstract class FilledPolyShape implements ShapeObject{
    private final Color color;
    private double x, y, width, height;

    public FilledPolyShape(Point2D start, Point2D end, Color color){
        this.width = 2 * Math.abs(start.getX() - end.getX());
        if(end.getX() < start.getX()){
            width = 0;
        }
        this.height = 2 * Math.abs(start.getY() - end.getY());
        if(end.getY() < start.getY()){
            height = 0;
        }
        this.x = end.getX() - width;
        this.y = end.getY() - height;

        this.color = color;
    }

    @Override
    abstract public void draw(GraphicsContext g);
    @Override
    abstract public boolean dragUpdate();

    // Getters for PolyShapes
    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }

    public double getWidth(){
        return width;
    }

    public double getHeight(){
        return height;
    }

    public Color getColor(){
        return color;
    }
}

class RectangleShape extends FilledPolyShape{
    public RectangleShape(Point2D start, Point2D end, Color color){
        super(start, end, color);

    }
    @Override
    public void draw(GraphicsContext g){
        g.setFill(getColor());
        g.fillRect(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public boolean dragUpdate(){
        return false;
    }

}

class RectangleTool extends ShapeTool{
    private RectangleShape currentRectangleShape;
    public RectangleTool(){
        super(SimplePaintObjects.TOOL_RECT_FG);
        Rectangle toolImage = new Rectangle(0,0,40,40);
        toolImage.setFill(SimplePaintObjects.TOOL_FG);

        this.getChildren().add(toolImage);
    }
    @Override
    public void draw(
            GraphicsContext g,
            Color color,
            Point2D start,
            Point2D end) {

        currentRectangleShape = new RectangleShape(start, end, color);
        currentRectangleShape.draw(g);

    }
    @Override
    public ShapeObject getPaintShape(){
        return currentRectangleShape;
    }
}

class OvalShape extends FilledPolyShape{
    public OvalShape(Point2D start, Point2D end, Color color){
        super(start, end, color);

    }

    @Override
    public void draw(GraphicsContext g){
        g.setFill(getColor());
        g.fillOval(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public boolean dragUpdate(){
        return false;
    }
}

class OvalTool extends ShapeTool{
    private OvalShape currentOvalShape;
    static final int RADIUS_X = 20, RADIUS_Y = 20;
    public OvalTool(){
        super(SimplePaintObjects.TOOL_RECT_FG);
        Ellipse toolImage = new Ellipse(RADIUS_X, RADIUS_Y);
        toolImage.setFill(SimplePaintObjects.TOOL_FG);

        this.getChildren().add(toolImage);
    }

    @Override
    public void draw(
            GraphicsContext g,
            Color color,
            Point2D start,
            Point2D end){
        currentOvalShape = new OvalShape(start, end, color);
        currentOvalShape.draw(g);
    }
    @Override
    public ShapeObject getPaintShape(){
        return currentOvalShape;
    }
}

class RoundedRectangleShape extends FilledPolyShape{
    static final int ARC_W = 50, ARC_H = 50;
    public RoundedRectangleShape(
            Point2D start,
            Point2D end,
            Color color){
        super(start, end, color);
    }

    @Override
    public void draw(GraphicsContext g){
        g.setFill(getColor());
        g.fillRoundRect(getX(), getY(), getWidth(), getHeight(),
                ARC_W , ARC_H);
    }

    @Override
    public boolean dragUpdate(){
        return false;
    }
}

class RoundedRectangleTool extends ShapeTool {
    static final int RR_W = 40, RR_H = 40;
    private RoundedRectangleShape currentRoundedRectangleShape;
    public RoundedRectangleTool() {
        super(SimplePaintObjects.TOOL_RECT_FG);
        Rectangle toolImage = new Rectangle(RR_W, RR_H);
        toolImage.setArcHeight(15);
        toolImage.setArcWidth(15);
        toolImage.setFill(SimplePaintObjects.TOOL_FG);

        this.getChildren().add(toolImage);
    }

    @Override
    public void draw(
            GraphicsContext g,
            Color color,
            Point2D start,
            Point2D end){
        currentRoundedRectangleShape = new RoundedRectangleShape(
                start,
                end,
                color);
        currentRoundedRectangleShape.draw(g);
    }

    @Override
    public ShapeObject getPaintShape(){
        return currentRoundedRectangleShape;
    }
}

/* Main class - sets up GUI and handles mouse events */
public class SimplePaintObjects extends Application {
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // constants
    //

    static final Color TOOL_RECT_FG = Color.LIGHTCORAL;
    static final Color TOOL_RECT_BG = Color.WHITE;
    static final Color TOOL_FG = Color.LEMONCHIFFON;
    static final int PADDING = 5;
    static final int CELL_W = 60;
    static final int CANVAS_W = 600;

    // Height Scales with CELL_W rectangle size in VBox
    static final int CANVAS_H = (CELL_W * 8) + (PADDING * 8);

    private final Color[] palette = {
            Color.BLACK, Color.RED, Color.GREEN, Color.BLUE,
            Color.CYAN, Color.MAGENTA, Color.YELLOW
    };
    private GraphicsContext g;

    private LinkedList<ShapeObject> shapeObjectLinkedList = new LinkedList();
    private ColorTool currentColorTool;
    private ShapeTool currentShapeTool;

    private double prevX, prevY;
    private double endX, endY;

    private Point2D start, end;

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public static void main(String[] args) {
        launch(args);
    }

    private Node makeColorPane() {
        VBox colorPane = new VBox();
        colorPane.setPadding(new Insets(0, 0, 0, PADDING));

        for (int i = 0; i < 7; i++) {
            colorPane.getChildren().add(
                    addMouseHandlerToColorTool(
                            new ColorTool(palette[i])
                    ));

        }

        colorPane.getChildren().add(addMouseHandlerToActionTool
                (new ActionTool("Clear"), this::myClearAction));


        // Set default selected color to black
        currentColorTool = (ColorTool) colorPane.getChildren().get(0);
        currentColorTool.activate();

        return colorPane;
    }

    private Node makeToolPane() {
        VBox toolPane = new VBox();
        toolPane.setPadding(new Insets(0, 0, 0, PADDING));

        addPointTools(toolPane);
        toolPane.getChildren().add(
                addMouseHandlerToShapeTool(new LineTool()));
        toolPane.getChildren().add(
                addMouseHandlerToShapeTool(new RectangleTool()));
        toolPane.getChildren().add(
                addMouseHandlerToShapeTool(new OvalTool()));
        toolPane.getChildren().add(
                addMouseHandlerToShapeTool(new RoundedRectangleTool()));


        // Set default tool to PointTool
        currentShapeTool = (ShapeTool) toolPane.getChildren().get(0);
        currentShapeTool.activate();

        return toolPane;
    }

    private void addPointTools(VBox toolPane) {
        for (int i = 2; i <= 8; i += 2) {
            toolPane.getChildren().add(addMouseHandlerToShapeTool
                    (new PointTool(i)));
        }
    }

    private Node makeCanvas() {
        Canvas canvas = new Canvas(CANVAS_W, CANVAS_H);
        g = canvas.getGraphicsContext2D();
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, CANVAS_W, CANVAS_H);

        clearCanvas();
        canvas.setOnMousePressed(e -> mousePressed(e));
        canvas.setOnMouseDragged(e -> mouseDragged(e));
        canvas.setOnMouseReleased(e -> mouseReleased(e));

        return canvas;
    }


    private Parent makeRootPane() {
        HBox root = new HBox();
        root.setPadding(new Insets(PADDING, PADDING, PADDING, PADDING));
        root.setSpacing(PADDING);

        root.getChildren().add(makeCanvas());
        root.getChildren().add(makeToolPane());
        root.getChildren().add(makeColorPane());

        return root;
    }

    private void myClearAction(){
        clearCanvas();
        shapeObjectLinkedList.clear();
    }

    private ShapeTool addMouseHandlerToShapeTool(ShapeTool tool) {
        tool.setOnMousePressed((e) -> {
            this.currentShapeTool.deactivate();
            this.currentShapeTool = tool;
            tool.activate();
        });

        return tool;
    }

    private ColorTool addMouseHandlerToColorTool(ColorTool tool) {
        tool.setOnMousePressed((e) -> {
            this.currentColorTool.deactivate();
            this.currentColorTool = tool;
            tool.activate();

        });

        return tool;
    }

    /* Set up button activation and clearAction runnable when clicked */
    private ActionTool addMouseHandlerToActionTool(
            ActionTool tool,
            Runnable myClearAction) {

        tool.setOnMousePressed((e) -> {
            tool.activate();
            myClearAction.run();
        });

        tool.setOnMouseReleased((e) ->{
            tool.deactivate();
        });
        return tool;
    }

    /* Sets coordinates to where mouse was clicked */
    private void mousePressed(MouseEvent e) {
        prevX =  e.getX();
        prevY = e.getY();
    }

    private void mouseDragged(MouseEvent e) {
        clearCanvas();

        endX = e.getX();
        endY = e.getY();
        start = new Point2D(prevX, prevY);
        end = new Point2D(endX, endY);

        // Store shape objects into linked list
        for(int i = 0; i <  shapeObjectLinkedList.size(); i++){
            shapeObjectLinkedList.get(i).draw(g);
        }

        currentShapeTool.draw(g, currentColorTool.color, start, end);

        // PointTool continuously adds ShapeObject while dragging
        if(currentShapeTool.getPaintShape().dragUpdate() == true){
            shapeObjectLinkedList.add(currentShapeTool.getPaintShape());
            prevX = endX;
            prevY = endY;
        }
    }

    /* Adds to list of drawn shapes when mouse released */
    private void mouseReleased(MouseEvent e) {
        shapeObjectLinkedList.add(currentShapeTool.getPaintShape());
    }

    private void clearCanvas() {
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, CANVAS_W, CANVAS_H);
    }

    public void start(Stage primaryStage) {
        primaryStage.setScene(new Scene(makeRootPane()));
        primaryStage.setTitle("Simple Paint Objects");
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}