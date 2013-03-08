/************************************************************************************************************
 * Copyright (c) 2013 Michal Frystacky, Geoffrey Hart                                                       *
 *                                                                                                          *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software            *
 * and associated documentation files (the "Software"), to deal in the Software without restriction,        *
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,                *
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is            *
 * furnished to do so, subject to the following conditions:                                                 *
 *                                                                                                          *
 * The above copyright notice and this permission notice shall be included in all copies or substantial     *
 *  portions of the Software.                                                                               *
 *                                                                                                          *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING            *
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND               *
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,             *
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,           *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                  *
 ************************************************************************************************************/

/**
 * Created with IntelliJ IDEA.
 * User: michalfrystacky
 * Date: 3/2/13
 * Time: 7:44 PM
 */


import static org.lwjgl.opengl.Display.create;
import static org.lwjgl.opengl.Display.destroy;
import static org.lwjgl.opengl.Display.isCloseRequested;
import static org.lwjgl.opengl.Display.setDisplayMode;
import static org.lwjgl.opengl.Display.update;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.DisplayMode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class Tree {
    private static LinkedList<Point> resources = new LinkedList<Point>();
    private static LinkedList<Point> existingTreeParts;
    private static LinkedList<Line> renderedTree;
    private static int resourceCount = 1000000;
    private static int treeTrunkSegments = 50; //Size of the tree trunk
    private static double atomicTreeUnit = 0.01; //This effects the smoothness and detail of generating a tree
    private static double killDistance = atomicTreeUnit * 20;
    private static double effectRadius = 2 * killDistance;

    public Tree () {

    }

    private static class Point {
        double x, y, z;

        public Point(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public void display() {
            glBegin(GL_POINTS);
            glVertex3d(x, y, z);
            glEnd();
        }
    }

    private static class Line {
        Point p1;
        Point p2;

        public Line(Point p1, Point p2) {
            this.p1 = p1;
            this.p2 = p2;
        }

        public void display() {
            glBegin(GL_LINES);
            glVertex3d(p1.x, p1.y, p1.z);
            glVertex3d(p2.x, p2.y, p2.z);
            glEnd();
        }

    }

    private static class TreeComponenet {
        TreeComponenet parent;
        LinkedList<TreeComponenet> children;
        double thickness;
        Point self;

    }

    static int width = 1024;
    static int height = 1024;
    static boolean fullscreen = false;

    public static void main(String[] args) {
        //Boiler plate lwjgl code
        try {
            setDisplayMode(new DisplayMode(width, height));
            create();
            //
            try {
                Thread.sleep(50);  // sleep to use less system resources
            } catch (InterruptedException ex) {
            }
        } catch (LWJGLException e) {
            e.printStackTrace();
        }
        /*********************************************************/
        existingTreeParts = new LinkedList<Point>();
        renderedTree = new LinkedList<Line>();

        //create tree trunk
        Point trunkStart = new Point(0, 0, 0);
        existingTreeParts.add(trunkStart);
        Point startPoint = trunkStart;
        for (int i = 0; i < treeTrunkSegments; ++i) {
            Point endPoint = new Point(startPoint.x, startPoint.y + atomicTreeUnit, startPoint.z);
            Line t = new Line(startPoint, endPoint);
            renderedTree.add(t);
            startPoint = endPoint;
            existingTreeParts.add(startPoint);
        }
        //double yScaler =
        for (int i = 0; i < resourceCount; ++i) {
            //generate a random point
            double x = (Math.random() * 2) - 1;
            double y = (Math.random() * 2);
            double z = (Math.random() * 2) - 1;
            Point t = new Point(x, y, z);
            double dis = Math.sqrt(Math.pow(x, 2) + Math.pow(y - 1.25, 2) + Math.pow(z, 2)); //TODO: clean
            if (dis < 0.75) {
                resources.add(t);
            }
        }

        glTranslated(0, -1, 0);
        //Shift our coordinates down with respect to the y axis to make creating the tree more nature
        // Animated reason bellow:
        //                                   _
        //                                 /   \
        //                                |     |
        //                                 \   /
        //                                   V
        //                                   |
        //                                   |(Mathematical tree coords representation is more natural)
        //                                   |
        //---------------------------------------------------------------------------- <- Shifted y Axis
        while (!isCloseRequested()) {
            glRotated(1, 0, 1, 0);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            //test our points
            glColor3d(1, 0, 0);
            glPointSize(5.5f);


            Iterator<Point> displayIterator = resources.iterator();
            while (displayIterator.hasNext()) {
                Point p = displayIterator.next();
                p.display();
            }

            //Growth Step
            HashMap<Point, LinkedList<Point>> resourceAssociations = new HashMap<Point, LinkedList<Point>>();
            Iterator<Point> resourceIterator = resources.iterator();
            //find the closest point

            while (resourceIterator.hasNext()) {
                Point res = resourceIterator.next();
                //find the closest tree point to associate with
                Point closestTreePoint = null;
                double closestDistance = Double.MAX_VALUE;
                for (Point treePart : existingTreeParts) {
                    double xDelta = Math.pow(res.x - treePart.x, 2);
                    double yDelta = Math.pow(res.y - treePart.y, 2);
                    double zDelta = Math.pow(res.z - treePart.z, 2);
                    double distance = xDelta + yDelta + zDelta;
                    if (distance < closestDistance) {
                        closestTreePoint = treePart;
                        closestDistance = distance;
                    }
                }
                closestDistance = Math.sqrt(closestDistance);
                if (closestDistance < effectRadius) {
                    if (closestDistance < killDistance) {
                        resourceIterator.remove();
                    } else {
                        if (!resourceAssociations.containsKey(closestTreePoint)) {
                            resourceAssociations.put(closestTreePoint, new LinkedList<Point>());
                        }
                        resourceAssociations.get(closestTreePoint).add(res);
                    }
                }
            }
            //generate all the vectors, normalize them and combine them
            Iterator<Point> curTreePoint = existingTreeParts.iterator();
            LinkedList<Point> newTreeParts = new LinkedList<Point>();
            while (curTreePoint.hasNext()) {
                Point treePoint = curTreePoint.next();
                if (resourceAssociations.containsKey(treePoint)) {
                    //we are creating a new branch
                    Point attractionVector = new Point(0, 0, 0);
                    for (Point resources : resourceAssociations.get(treePoint)) {
                        Point tempVector = new Point(0, 0, 0);
                        tempVector.x = resources.x - treePoint.x;
                        tempVector.y = resources.y - treePoint.y;
                        tempVector.z = resources.z - treePoint.z;
                        //normalize
                        double mag = Math.sqrt(Math.pow(tempVector.x, 2) + Math.pow(tempVector.y, 2) + Math.pow(tempVector.z, 2));
                        tempVector.x /= mag;
                        tempVector.y /= mag;
                        tempVector.z /= mag;
                        //sum to attraction vector
                        attractionVector.x += tempVector.x;
                        attractionVector.y += tempVector.y;
                        attractionVector.z += tempVector.z;
                    }
                    double mag = Math.sqrt(Math.pow(attractionVector.x, 2) + Math.pow(attractionVector.y, 2) + Math.pow(attractionVector.z, 2));
                    attractionVector.x /= mag;
                    attractionVector.y /= mag;
                    attractionVector.z /= mag;
                    //generate a new tree point
                    Point treeGrowth = new Point(treePoint.x + (attractionVector.x * atomicTreeUnit), treePoint.y + (attractionVector.y * atomicTreeUnit), treePoint.z + (attractionVector.z * atomicTreeUnit));
                    newTreeParts.add(treeGrowth);
                    Line newTreeBranch = new Line(treePoint, treeGrowth);
                    renderedTree.add(newTreeBranch);
                } else {
                    //this is an optimisation that prevents more dynamic trees using dynamic resource allocation
                    curTreePoint.remove();
                }
            }
            existingTreeParts.addAll(newTreeParts);
            //check for overlap between resource and tree points


            glColor3d(0, 1, 0);
            glPointSize(2.5f);
            for (Line l : renderedTree) {
                l.display();
            }

            update();
        }
        destroy();
    }
}