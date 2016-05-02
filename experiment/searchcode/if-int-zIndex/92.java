package nl.chrish.towerdefense.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris_000 on 25-4-2014.
 */
public class Renderer {
    private static Renderer instance;
    private OrthographicCamera camera;
    private Rectangle glViewport;
    private SpriteBatch batch;
    private List<Layer> layers;

    protected Renderer() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        System.out.println("Window width: " + w + " Window height: " + h);

        camera = new OrthographicCamera(w / 2, h / 2);

        glViewport = new Rectangle(0, 0, w, h);

        batch = new SpriteBatch();
        layers = new ArrayList<Layer>();

        StaticShapeRenderer.init(camera, batch);
    }

    public static Renderer getInstance() {
        if (instance == null) {
            instance = new Renderer();
        }

        return instance;
    }

    public void initStageCamera(Stage stage) {
        camera = (OrthographicCamera)stage.getCamera();
        camera.position.set(this.glViewport.getWidth() / 2, this.glViewport.getHeight() / 2, 0);
    }

    public void clear() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    public void render() {
        Gdx.gl.glViewport((int) glViewport.x, (int) glViewport.y,
                (int) glViewport.width, (int) glViewport.height);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for(Layer l : layers) {
            for (Renderable r : l.getRenderables()) {
                r.draw(batch);
            }
        }
        batch.end();

        camera.update();
    }

    public OrthographicCamera getCamera() {
        return this.camera;
    }

    public void addRenderable(Renderable renderable) {
        this.addRenderable(renderable, 0);
    }

    public void addRenderable(Renderable renderable, int zindex) {
        while(zindex > this.layers.size() - 1) {
            this.layers.add(new Layer());
        }
        this.layers.get(zindex).addRenderable(renderable);
    }

    public void removeRenderable(Renderable renderable) {
        // Expensive! Specify layer if possible!
        for(Layer l : layers) {
            l.removeRenderable(renderable);
        }
    }

    public void removeRenderable(Renderable renderable, int zindex) {
        this.layers.get(zindex).removeRenderable(renderable);
    }
}

