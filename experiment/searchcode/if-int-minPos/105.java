package nl.chrish.towerdefense.tilegridsystem;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by chris_000 on 26-4-2014.
 */
public abstract class TileGridInputListener implements InputProcessor {
    private float currentZoomLevel = 1.0f;
    private final float minZoomLevel = 0.125f;
    private float zoomStep = 0.0375f;
    protected MouseCoordinates mc = new MouseCoordinates();
    protected OrthographicCamera camera;
    protected TileGrid tileGrid;
    protected CameraBounds cameraBounds;

    private class CameraBounds {
        public Vector2 minPos = new Vector2();
        public Vector2 maxPos = new Vector2();
    }

    @SuppressWarnings("unused")
    protected TileGridInputListener() {

    }

    public TileGridInputListener(OrthographicCamera _camera, TileGrid _tileGrid) {
        this.camera = _camera;
        this.tileGrid = _tileGrid;

        this.setCameraBounds();
        this.centerCamera();

        // Make sure the the maps fits on the screen, and there are no black spaces on either side.
        while(this.isCameraOutOfBounds()) {
            this.setZoomLevel(currentZoomLevel - zoomStep);
        }
    }

    @Override
    public boolean keyDown(int keycode) {

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        getLastMouseCoordinates(pointer);

        mc.initialX = screenX;
        mc.initialY = screenY;

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(Math.abs((mc.lastX * mc.lastY) - (mc.initialX * mc.initialY)) > 10) {
            return false;
        }

        Vector3 currentCoordinates = new Vector3(screenX, screenY, 0);

        camera.unproject(currentCoordinates);

        Tile tile = this.tileGrid.getTileAtCoordinates((int)mc.worldCoordinates.x, (int)mc.worldCoordinates.y);

        if(tile == this.tileGrid.getTileAtCoordinates((int)currentCoordinates.x, (int)currentCoordinates.y)) {
            tileGrid.activateTile(tile);
        }

        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if(pointer != 0) {
            return false;
        }

        mc.diffX = (int)((mc.lastX - screenX) * camera.zoom);
        mc.diffY = (int)((screenY - mc.lastY) * camera.zoom);

        moveCamera(mc.diffX, mc.diffY);

        getLastMouseCoordinates(pointer);
        return false;
    }

    private void moveCamera(float diffX, float diffY) {
        camera.position.set(
                Math.min(this.cameraBounds.maxPos.x, Math.max(camera.position.x + diffX, this.cameraBounds.minPos.x)),
                Math.min(this.cameraBounds.maxPos.y, Math.max(camera.position.y + diffY, this.cameraBounds.minPos.y)),
                0
        );

        camera.update();
    }

    protected void getLastMouseCoordinates(int pointer) {
        mc.lastX = Gdx.input.getX(pointer);
        mc.lastY = Gdx.input.getY(pointer);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        mc.worldCoordinates.x = screenX;
        mc.worldCoordinates.y = screenY;

        camera.unproject(mc.worldCoordinates);

        Tile t = tileGrid.getTileAtCoordinates(mc.worldCoordinates.x, mc.worldCoordinates.y);
        if(t != null) {
            tileGrid.highlightTile(t);
        }
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        float newZoomLevel = currentZoomLevel;
        float oldZoomLevel = currentZoomLevel;

        if(amount > 0) {
            newZoomLevel += zoomStep;
        } else {
            newZoomLevel -= zoomStep;
        }

        setZoomLevel(newZoomLevel);
        correctZoomLevel(oldZoomLevel);

        return true;
    }

    protected void setZoomLevel(float newZoomLevel) {
        camera.zoom = newZoomLevel;
        camera.update();
        currentZoomLevel = newZoomLevel;
        setCameraBounds();
    }

    protected void correctZoomLevel(float oldZoomLevel) {
        // TODO: if not possible from the side, first try centering the view and zooming out
        if (isCameraOutOfBounds() ) {
            setZoomLevel(oldZoomLevel);
        }
    }

    protected boolean isCameraOutOfBounds() {
        return (
                this.camera.position.x > this.cameraBounds.maxPos.x
                        || this.camera.position.x < this.cameraBounds.minPos.x
                        || this.camera.position.y > this.cameraBounds.maxPos.y
                        || this.camera.position.y < this.cameraBounds.minPos.y
                        || this.camera.zoom < this.minZoomLevel
        );
    }

    protected void setCameraBounds() {
        this.cameraBounds = new CameraBounds();

        this.cameraBounds.minPos.x = camera.zoom * (camera.viewportWidth / 2);
        this.cameraBounds.maxPos.x = this.tileGrid.getGridWidth() - this.cameraBounds.minPos.x;
        this.cameraBounds.minPos.y = camera.zoom * (camera.viewportHeight / 2);
        this.cameraBounds.maxPos.y = this.tileGrid.getGridHeight() - this.cameraBounds.minPos.y;
    }

    protected void centerCamera() {
        this.camera.position.x = tileGrid.getGridWidth() / 2;
        this.camera.position.y = tileGrid.getGridHeight() / 2;
    }
}

