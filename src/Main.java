import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

/**
 * Created with IntelliJ IDEA.
 * User: michalfrystacky
 * Date: 3/2/13
 * Time: 6:36 PM
 * To change this template use File | Settings | File Templates.
 */

public class Main {

    public Main() {

        try {

            Display.setDisplayMode(new DisplayMode(800, 600));

        } catch (LWJGLException e) {

            e.printStackTrace();

        }

    }


    public static void main(String[] args) {

        new Main();

    }

}
