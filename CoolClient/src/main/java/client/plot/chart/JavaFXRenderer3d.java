package client.plot.chart;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAutoDrawable;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.jzy3d.plot3d.rendering.view.AWTImageRenderer3d;
import org.jzy3d.plot3d.rendering.view.View;

import java.awt.image.BufferedImage;

@SuppressWarnings("restriction")
/* Disable JavaFX access restriction warnings */
public class JavaFXRenderer3d extends AWTImageRenderer3d {
    public JavaFXRenderer3d(View view, boolean traceGL, boolean debugGL) {
        super(view, traceGL, debugGL);
    }

    @Override
    public void display(GLAutoDrawable canvas) {
        GL gl = canvas.getGL();

        if (view != null) {
            view.clear(gl);
            view.render(gl, glu);

            // Convert as JavaFX Image and notify all listeners
            Image image = makeScreenshotAsJavaFXImage(gl);
            fireDisplay(image);

            if (doScreenshotAtNextDisplay) {
                makeScreenshotAsJavaFXImage(gl);
                doScreenshotAtNextDisplay = false;
            }
        }
    }

    private Image makeScreenshotAsJavaFXImage(GL gl) {
        BufferedImage i = makeScreenshotAsBufferedImage(gl);
        return SwingFXUtils.toFXImage(i, null);
    }
}