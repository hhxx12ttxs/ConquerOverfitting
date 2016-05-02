<<<<<<< HEAD
package nodebox.graphics;

import nodebox.node.Node;
import nodebox.node.Parameter;
import nodebox.node.ProcessingContext;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public abstract class AbstractGraphicsContext implements GraphicsContext {


    // TODO: Support output mode
    protected Color.Mode colorMode;
    protected float colorRange;
    protected Color fillColor;
    protected Color strokeColor;
    protected float strokeWidth;
    protected Path path;
    protected boolean autoClosePath;
    protected boolean pathClosed;
    protected Transform.Mode transformMode;
    protected Transform transform = new Transform();
    protected ArrayList<Transform> transformStack;
    protected String fontName;
    protected float fontSize;
    protected float lineHeight;
    protected Text.Align align;
    protected GraphicsContext.RectMode rectMode = GraphicsContext.RectMode.CORNER;
    protected GraphicsContext.EllipseMode ellipseMode = GraphicsContext.EllipseMode.CORNER;

    public void resetContext() {
        colorMode = Color.Mode.RGB;
        colorRange = 1f;
        fillColor = new Color();
        strokeColor = null;
        strokeWidth = 1f;
        path = null;
        autoClosePath = true;
        transformMode = Transform.Mode.CENTER;
        transform = new Transform();
        transformStack = new ArrayList<Transform>();
        fontName = "Helvetica";
        fontSize = 24;
        lineHeight = 1.2f;
        align = Text.Align.LEFT;
    }

    //// Primitives ////

    // TODO: Support rect modes.


    public RectMode rectmode() {
        return rectMode;
    }

    public RectMode rectmode(RectMode m) {
        return rectMode = m;
    }

    public RectMode rectmode(String m) {
        try {
            RectMode newMode = RectMode.valueOf(m.toUpperCase(Locale.US));
            return rectMode = newMode;
        } catch (IllegalArgumentException e) {
            throw new NodeBoxError("rectmode: available types for rectmode() are CORNER, CENTER, CORNERS and RADIUS\\n\"");
        }
    }

    public RectMode rectmode(int m) {
        try {
            RectMode newMode = RectMode.values()[m];
            return rectMode = newMode;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NodeBoxError("rectmode: available types for rectmode() are CORNER, CENTER, CORNERS and RADIUS\\n\"");
        }
    }

    private Path createPath() {
        Path p = new Path();
        p.setTransformDelegate(new ContextTransformDelegate(this));
        return p;
    }

    public Path Path() {
        return createPath();
    }

    public Path BezierPath() {
        return createPath();
    }

    public Path rect(Rect r) {
        return rect(r.getX(), r.getY(), r.getWidth(), r.getHeight(), true);
    }

    public Path rect(Rect r, boolean draw) {
        return rect(r.getX(), r.getY(), r.getWidth(), r.getHeight(), draw);
    }

    public Path rect(float x, float y, float width, float height) {
        return rect(x, y, width, height, true);
    }

    public Path rect(float x, float y, float width, float height, boolean draw) {
        Path p = createPath();
        switch (rectMode) {
            case CENTER:
                p.rect(x, y, width, height);
                break;
            case CORNER:
                p.cornerRect(x, y, width, height);
                break;
        }
        inheritFromContext(p);
        if (draw)
            addPath(p);
        return p;
    }

    public Path rect(Rect r, float roundness) {
        return rect(r.getX(), r.getY(), r.getWidth(), r.getHeight(), roundness, roundness, true);
    }

    public Path rect(Rect r, float roundness, boolean draw) {
        return rect(r.getX(), r.getY(), r.getWidth(), r.getHeight(), roundness, roundness, draw);
    }

    public Path rect(float x, float y, float width, float height, float roundness) {
        return rect(x, y, width, height, roundness, roundness, true);
    }

    public Path rect(float x, float y, float width, float height, float roundness, boolean draw) {
        return rect(x, y, width, height, roundness, roundness, draw);
    }

    public Path rect(float x, float y, float width, float height, float rx, float ry) {
        return rect(x, y, width, height, rx, ry, true);
    }

    public Path rect(float x, float y, float width, float height, float rx, float ry, boolean draw) {
        Path p = createPath();
        switch (rectMode) {
            case CENTER:
                p.rect(x, y, width, height, rx, ry);
                break;
            case CORNER:
                p.cornerRect(x, y, width, height, rx, ry);
                break;
        }
        inheritFromContext(p);
        if (draw)
            addPath(p);
        return p;
    }

    public EllipseMode ellipsemode() {
        return ellipseMode;
    }

    public EllipseMode ellipsemode(EllipseMode m) {
        return ellipseMode = m;
    }

    public EllipseMode ellipsemode(String m) {
        try {
            EllipseMode newMode = EllipseMode.valueOf(m.toUpperCase(Locale.US));
            return ellipseMode = newMode;
        } catch (IllegalArgumentException e) {
            throw new NodeBoxError("ellipsemode: available types for ellipsemode() are CORNER, CENTER, CORNERS and RADIUS\\n\"");
        }
    }

    public EllipseMode ellipsemode(int m) {
        try {
            EllipseMode newMode = EllipseMode.values()[m];
            return ellipseMode = newMode;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NodeBoxError("ellipsemode: available types for ellipsemode() are CORNER, CENTER, CORNERS and RADIUS\\n\"");
        }
    }

    public Path oval(float x, float y, float width, float height) {
        // TODO: Deprecation warning
        return ellipse(x, y, width, height, true);
    }

    public Path oval(float x, float y, float width, float height, boolean draw) {
        // TODO: Deprecation warning
        return ellipse(x, y, width, height, draw);
    }

    public Path ellipse(float x, float y, float width, float height) {
        return ellipse(x, y, width, height, true);
    }

    public Path ellipse(float x, float y, float width, float height, boolean draw) {
        Path p = createPath();
        switch (ellipseMode) {
            case CENTER:
                p.ellipse(x, y, width, height);
                break;
            case CORNER:
                p.cornerEllipse(x, y, width, height);
                break;
        }
        inheritFromContext(p);
        if (draw)
            addPath(p);
        return p;
    }

    public Path line(float x1, float y1, float x2, float y2) {
        return line(x1, y1, x2, y2, true);
    }

    public Path line(float x1, float y1, float x2, float y2, boolean draw) {
        Path p = createPath();
        p.line(x1, y1, x2, y2);
        inheritFromContext(p);
        if (draw)
            addPath(p);
        return p;
    }

    public Path star(float cx, float cy) {
        return star(cx, cy, 20, 100, 50, true);
    }

    public Path star(float cx, float cy, int points) {
        return star(cx, cy, points, 100, 50, true);
    }

    public Path star(float cx, float cy, int points, float outer) {
        return star(cx, cy, points, outer, 50, true);
    }

    public Path star(float cx, float cy, int points, float outer, float inner) {
        return star(cx, cy, points, outer, inner, true);
    }

    public Path star(float cx, float cy, int points, float outer, float inner, boolean draw) {
        float PI = (float) Math.PI;
        Path p = createPath();
        p.moveto(cx, cy + outer);
        for (int i = 1; i < points * 2; i++) {
            float angle = i * PI / points;
            float x = (float) Math.sin(angle);
            float y = (float) Math.cos(angle);
            float radius = i % 2 == 0 ? outer : inner;
            x += cx + radius * x;
            y += cy + radius * y;
            p.lineto(x, y);
        }
        p.close();
        inheritFromContext(p);
        if (draw)
            addPath(p);
        return p;
    }

    public Path arrow(float x, float y) {
        return arrow(x, y, 100, ArrowType.NORMAL, true);
    }

    public Path arrow(float x, float y, ArrowType type) {
        return arrow(x, y, 100, type, true);
    }

    public Path arrow(float x, float y, String type) {
        return arrow(x, y, 100, type, true);
    }

    public Path arrow(float x, float y, int type) {
        return arrow(x, y, 100, type, true);
    }

    public Path arrow(float x, float y, float width) {
        return arrow(x, y, width, NORMAL, true);
    }

    public Path arrow(float x, float y, float width, boolean draw) {
        return arrow(x, y, width, NORMAL, draw);
    }

    public Path arrow(float x, float y, float width, ArrowType type) {
        return arrow(x, y, width, type, true);
    }

    public Path arrow(float x, float y, float width, String type) {
        return arrow(x, y, width, type, true);
    }

    public Path arrow(float x, float y, float width, int type) {
        return arrow(x, y, width, type, true);
    }

    public Path arrow(float x, float y, float width, String type, boolean draw) {
        try {
            ArrowType arrowType = ArrowType.valueOf(type.toUpperCase(Locale.US));
            return arrow(x, y, width, arrowType, draw);
        } catch (IllegalArgumentException e) {
            throw new NodeBoxError("arrow: available types for arrow() are NORMAL and FORTYFIVE\\n\"");
        }
    }

    public Path arrow(float x, float y, float width, int type, boolean draw) {
        try {
            ArrowType arrowType = ArrowType.values()[type];
            return arrow(x, y, width, arrowType, draw);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NodeBoxError("arrow: available types for arrow() are NORMAL and FORTYFIVE\\n\"");
        }
    }

    public Path arrow(float x, float y, float width, ArrowType type, boolean draw) {
        if (type == ArrowType.NORMAL)
            return arrowNormal(x, y, width, draw);
        else
            return arrowFortyFive(x, y, width, draw);
    }

    private Path arrowNormal(float x, float y, float width, boolean draw) {
        float head = width * .4f;
        float tail = width * .2f;

        Path p = createPath();
        p.moveto(x, y);
        p.lineto(x - head, y + head);
        p.lineto(x - head, y + tail);
        p.lineto(x - width, y + tail);
        p.lineto(x - width, y - tail);
        p.lineto(x - head, y - tail);
        p.lineto(x - head, y - head);
        p.lineto(x, y);
        p.close();
        inheritFromContext(p);
        if (draw)
            addPath(p);
        return p;
    }

    private Path arrowFortyFive(float x, float y, float width, boolean draw) {
        float head = .3f;
        float tail = 1 + head;

        Path p = createPath();
        p.moveto(x, y);
        p.lineto(x, y + width * (1 - head));
        p.lineto(x - width * head, y + width);
        p.lineto(x - width * head, y + width * tail * .4f);
        p.lineto(x - width * tail * .6f, y + width);
        p.lineto(x - width, y + width * tail * .6f);
        p.lineto(x - width * tail * .4f, y + width * head);
        p.lineto(x - width, y + width * head);
        p.lineto(x - width * (1 - head), y);
        p.lineto(x, y);
        p.close();
        inheritFromContext(p);
        if (draw)
            addPath(p);
        return p;
    }

    //// Path commands ////

    public void beginpath() {
        path = createPath();
        pathClosed = false;
    }

    public void beginpath(float x, float y) {
        beginpath();
        moveto(x, y);
    }

    public void moveto(float x, float y) {
        if (path == null)
            throw new NodeBoxError("No current path. Use beginpath() first.");
        path.moveto(x, y);
    }

    public void lineto(float x, float y) {
        if (path == null)
            throw new NodeBoxError("No current path. Use beginpath() first.");
        path.lineto(x, y);
    }

    public void curveto(float x1, float y1, float x2, float y2, float x3, float y3) {
        if (path == null)
            throw new NodeBoxError("No current path. Use beginPath() first.");
        path.curveto(x1, y1, x2, y2, x3, y3);
    }

    public void closepath() {
        if (path == null)
            throw new NodeBoxError("No current path. Use beginpath() first.");
        if (!pathClosed) {
            path.close();
            pathClosed = true;
        }
    }

    public Path endpath() {
        return endpath(true);
    }

    public Path endpath(boolean draw) {
        if (path == null)
            throw new NodeBoxError("No current path. Use beginpath() first.");
        if (autoClosePath)
            closepath();
        Path p = path;
        inheritFromContext(p);
        if (draw)
            addPath(p);
        // Initialize a new path
        path = null;
        pathClosed = false;
        return p;
    }

    public void drawpath(Path path) {
        inheritFromContext(path);
        addPath(path);
    }

    public void drawpath(Iterable<Point> points) {
        Path path = createPath();
        for (Point pt : points) {
            path.addPoint(pt);
        }
        inheritFromContext(path);
        addPath(path);
    }

    public boolean autoclosepath() {
        return autoClosePath;
    }

    public boolean autoclosepath(boolean c) {
        return autoClosePath = c;
    }

    public Path findpath(List<Point> points) {
        return findpath(points, 1);
    }

    public Path findpath(List<Point> points, float curvature) {
        Path path = Path.findPath(points, curvature);
        inheritFromContext(path);
        addPath(path);
        return path;
    }

    //// Clipping ////

    // TODO: implement clipping

    public void beginclip(Path p) {
        throw new RuntimeException("beginclip is not implemented yet.");
    }

    public void endclip() {
        throw new RuntimeException("endclip is not implemented yet.");
    }


    //// Transformation commands ////


    public Transform.Mode transform() {
        return transformMode;
    }

    public Transform.Mode transform(Transform.Mode mode) {
        return transformMode = mode;
    }

    public Transform.Mode transform(String mode) {
        try {
            Transform.Mode newMode = Transform.Mode.valueOf(mode.toUpperCase(Locale.US));
            return transformMode = newMode;
        } catch (IllegalArgumentException e) {
            throw new NodeBoxError("transform: available types for transform() are CORNER and CENTER\\n\"");
        }
    }

    public Transform.Mode transform(int mode) {
        try {
            Transform.Mode newMode = Transform.Mode.values()[mode];
            return transformMode = newMode;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NodeBoxError("transform: available types for transform() are CORNER and CENTER\\n\"");
        }
    }

    public void push() {
        transformStack.add(0, transform.clone());
    }

    public void pop() {
        if (transformStack.isEmpty())
            throw new NodeBoxError("Pop: too many pops!");
        transform = transformStack.get(0);
        transformStack.remove(0);
    }

    public void reset() {
        transformStack.clear();
        transform = new Transform();
    }

    public void translate(float tx, float ty) {
        transform.translate(tx, ty);
    }

    public void rotate(float r) {
        transform.rotate(r);
    }

    public void scale(float scale) {
        transform.scale(scale);
    }

    public void scale(float sx, float sy) {
        transform.scale(sx, sy);
    }

    public void skew(float skew) {
        transform.skew(skew);
    }

    public void skew(float kx, float ky) {
        transform.skew(kx, ky);
    }

    //// Color commands ////

    public String outputmode() {
        throw new RuntimeException("outputmode is not implemented yet.");
    }

    public String outputmode(String mode) {
        throw new RuntimeException("outputmode is not implemented yet.");
    }

    public Color.Mode colormode() {
        return colorMode;
    }

    public Color.Mode colormode(Color.Mode mode) {
        return colormode(mode, null);
    }

    public Color.Mode colormode(Color.Mode mode, Float range) {
        if (range != null) colorRange = range;
        return colorMode = mode;
    }

    public Color.Mode colormode(String mode) {
        return colormode(mode, null);
    }

    public Color.Mode colormode(String mode, Float range) {
        try {
            Color.Mode newMode = Color.Mode.valueOf(mode.toUpperCase(Locale.US));
            return colormode(newMode, range);
        } catch (IllegalArgumentException e) {
            throw new NodeBoxError("colormode: available types for colormode() are RGB, HSB and CMYK\\n\"");
        }
    }

    public Color.Mode colormode(int mode) {
        return colormode(mode, null);
    }

    public Color.Mode colormode(int mode, Float range) {
        try {
            Color.Mode newMode = Color.Mode.values()[mode];
            return colormode(newMode, range);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NodeBoxError("colormode: available types for colormode() are RGB, HSB and CMYK\\n\"");
        }
    }

    public float colorrange() {
        return colorRange;
    }

    public float colorrange(float range) {
        return colorRange = range;
    }

    /**
     * Create an empty (black) color object.
     *
     * @return the new color.
     */
    public Color color() {
        return new Color();
    }

    /**
     * Create a new color with the given grayscale value.
     *
     * @param x the gray component.
     * @return the new color.
     */
    public Color color(float x) {
        float nx = normalize(x);
        return new Color(nx, nx, nx);
    }

    /**
     * Create a new color with the given grayscale and alpha value.
     *
     * @param x the grayscale value.
     * @param y the alpha value.
     * @return the new color.
     */
    public Color color(float x, float y) {
        float nx = normalize(x);
        return new Color(nx, nx, nx, normalize(y));
    }

    /**
     * Create a new color with the the given R/G/B or H/S/B value.
     *
     * @param x the red or hue component.
     * @param y the green or saturation component.
     * @param z the blue or brightness component.
     * @return the new color.
     */
    public Color color(float x, float y, float z) {
        return new Color(normalize(x), normalize(y), normalize(z), colormode());
    }

    /**
     * Create a new color with the the given R/G/B/A or H/S/B/A value.
     *
     * @param x the red or hue component.
     * @param y the green or saturation component.
     * @param z the blue or brightness component.
     * @param a the alpha component.
     * @return the new color.
     */
    public Color color(float x, float y, float z, float a) {
        return new Color(normalize(x), normalize(y), normalize(z), normalize(a), colormode());
    }

    /**
     * Create a new color with the the given color.
     * <p/>
     * The color object is cloned; you can change the original afterwards.
     * If the color object is null, the new color is turned off (same as nocolor).
     *
     * @param c the color object.
     * @return the new color.
     */
    public Color color(Color c) {
        return c == null ? new Color(0, 0, 0, 0) : c.clone();
    }

    /**
     * Get the current fill color.
     *
     * @return the current fill color.
     */
    public Color fill() {
        return fillColor;
    }

    /**
     * Set the current fill color to given grayscale value.
     *
     * @param x the gray component.
     * @return the current fill color.
     */
    public Color fill(float x) {
        float nx = normalize(x);
        return fillColor = new Color(nx, nx, nx);
    }

    /**
     * Set the current fill color to given grayscale and alpha value.
     *
     * @param x the grayscale value.
     * @param y the alpha value.
     * @return the current fill color.
     */
    public Color fill(float x, float y) {
        float nx = normalize(x);
        return fillColor = new Color(nx, nx, nx, normalize(y));
    }

    /**
     * Set the current fill color to the given R/G/B or H/S/B value.
     *
     * @param x the red or hue component.
     * @param y the green or saturation component.
     * @param z the blue or brightness component.
     * @return the current fill color.
     */
    public Color fill(float x, float y, float z) {
        return fillColor = new Color(normalize(x), normalize(y), normalize(z), colormode());
    }

    /**
     * Set the current fill color to the given R/G/B/A or H/S/B/A value.
     *
     * @param x the red or hue component.
     * @param y the green or saturation component.
     * @param z the blue or brightness component.
     * @param a the alpha component.
     * @return the current fill color.
     */
    public Color fill(float x, float y, float z, float a) {
        return fillColor = new Color(normalize(x), normalize(y), normalize(z), normalize(a), colormode());
    }

    /**
     * Set the current fill color to the given color.
     * <p/>
     * The color object is cloned; you can change the original afterwards.
     * If the color object is null, the current fill color is turned off (same as nofill).
     *
     * @param c the color object.
     * @return the current fill color.
     */
    public Color fill(Color c) {
        return fillColor = c == null ? null : c.clone();
    }

    public void nofill() {
        fillColor = null;
    }

    /**
     * Get the current stroke color.
     *
     * @return the current stroke color.
     */
    public Color stroke() {
        return strokeColor;
    }

    /**
     * Set the current stroke color to given grayscale value.
     *
     * @param x the gray component.
     * @return the current stroke color.
     */
    public Color stroke(float x) {
        float nx = normalize(x);
        return strokeColor = new Color(nx, nx, nx);
    }

    /**
     * Set the current stroke color to given grayscale and alpha value.
     *
     * @param x the grayscale value.
     * @param y the alpha value.
     * @return the current stroke color.
     */
    public Color stroke(float x, float y) {
        float nx = normalize(x);
        return strokeColor = new Color(nx, nx, nx, normalize(y));
    }

    /**
     * Set the current stroke color to the given R/G/B or H/S/B value.
     *
     * @param x the red or hue component.
     * @param y the green or saturation component.
     * @param z the blue or brightness component.
     * @return the current stroke color.
     */
    public Color stroke(float x, float y, float z) {
        return strokeColor = new Color(normalize(x), normalize(y), normalize(z), colormode());
    }

    /**
     * Set the current stroke color to the given R/G/B/A or H/S/B/A value.
     *
     * @param x the red or hue component.
     * @param y the green or saturation component.
     * @param z the blue or brightness component.
     * @param a the alpha component.
     * @return the current stroke color.
     */
    public Color stroke(float x, float y, float z, float a) {
        return strokeColor = new Color(normalize(x), normalize(y), normalize(z), normalize(a), colormode());
    }

    /**
     * Set the current stroke color to the given color.
     * <p/>
     * The color object is cloned; you can change the original afterwards.
     * If the color object is null, the current stroke color is turned off (same as nostroke).
     *
     * @param c the color object.
     * @return the current stroke color.
     */
    public Color stroke(Color c) {
        return strokeColor = c == null ? null : c.clone();
    }

    public void nostroke() {
        strokeColor = null;
    }

    public float strokewidth() {
        return strokeWidth;
    }

    public float strokewidth(float w) {
        return strokeWidth = w;
    }

    //// Image commands ////

    public Image image(String path, float x, float y) {
        throw new RuntimeException("'image' is not applicable to this type of GraphicsContext.");
    }

    public Image image(String path, float x, float y, Float width) {
        throw new RuntimeException("'image' is not applicable to this type of GraphicsContext.");
    }

    public Image image(String path, float x, float y, Float width, Float height) {
        throw new RuntimeException("'image' is not applicable to this type of GraphicsContext.");
    }

    public Image image(String path, float x, float y, Float width, Float height, float alpha) {
        throw new RuntimeException("'image' is not applicable to this type of GraphicsContext.");
    }

    public Image image(String path, float x, float y, Float width, Float height, boolean draw) {
        throw new RuntimeException("'image' is not applicable to this type of GraphicsContext.");
    }

    public Image image(String path, float x, float y, Float width, Float height, float alpha, boolean draw) {
        throw new RuntimeException("'image' is not applicable to this type of GraphicsContext.");
    }

    public Image image(Image img, float x, float y, Float width, Float height, float alpha, boolean draw) {
        throw new RuntimeException("'image' is not applicable to this type of GraphicsContext.");
    }

    public Image image(BufferedImage img, float x, float y, Float width, Float height, float alpha, boolean draw) {
        throw new RuntimeException("'image' is not applicable to this type of GraphicsContext.");
    }

    public Size imagesize(String path) {
        throw new RuntimeException("'imagesize' is not applicable to this type of GrqphicsContext.");
    }

    public Size imagesize(Image img) {
        throw new RuntimeException("'imagesize' is not applicable to this type of GrqphicsContext.");
    }

    public Size imagesize(BufferedImage img) {
        throw new RuntimeException("'imagesize' is not applicable to this type of GrqphicsContext.");
    }

    //// Font commands ////

    public String font() {
        return fontName;
    }

    public String font(String fontName) {
        if (!Text.fontExists(fontName))
            throw new NodeBoxError("Font '" + fontName + "' does not exist.");
        return this.fontName = fontName;
    }

    public String font(String fontName, float fontSize) {
        font(fontName);
        fontsize(fontSize);
        return fontName;
    }

    public float fontsize() {
        return fontSize;
    }

    public float fontsize(float s) {
        return fontSize = s;
    }

    public float lineheight() {
        return lineHeight;
    }

    public float lineheight(float lineHeight) {
        return this.lineHeight = lineHeight;
    }

    public Text.Align align() {
        return align;
    }

    public Text.Align align(Text.Align align) {
        return this.align = align;
    }

    public Text.Align align(String align) {
        try {
            Text.Align newAlign = Text.Align.valueOf(align.toUpperCase(Locale.US));
            return this.align = newAlign;
        } catch (IllegalArgumentException e) {
            throw new NodeBoxError("align: available types for align() are LEFT, RIGHT, CENTER and JUSTIFY\\n\"");
        }
    }

    public Text.Align align(int align) {
        try {
            Text.Align newAlign = Text.Align.values()[align];
            return this.align = newAlign;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NodeBoxError("align: available types for align() are LEFT, RIGHT, CENTER and JUSTIFY\\n\"");
        }
    }

    public Text text(String text, float x, float y) {
        return text(text, x, y, 0, 0, true);
    }

    public Text text(String text, float x, float y, float width) {
        return text(text, x, y, width, 0, true);
    }

    public Text text(String text, float x, float y, float width, float height) {
        return text(text, x, y, width, height, true);
    }

    public Text text(String text, float x, float y, float width, float height, boolean draw) {
        Text t = new Text(text, x, y, width, height);
        t.setTransformDelegate(new ContextTransformDelegate(this));
        inheritFromContext(t);
        if (draw)
            addText(t);
        return t;
    }

    public Path textpath(String text, float x, float y) {
        return textpath(text, x, y, 0, 0);
    }

    public Path textpath(String text, float x, float y, float width) {
        return textpath(text, x, y, width, 0);
    }

    public Path textpath(String text, float x, float y, float width, float height) {
        Text t = new Text(text, x, y, width, height);
        inheritFontAttributesFromContext(t);
        Path path = t.getPath();
        path.setTransformDelegate(new ContextTransformDelegate(this));
        inheritFromContext(path);
        return path;
    }

    public Rect textmetrics(String text) {
        return textmetrics(text, 0, 0);
    }

    public Rect textmetrics(String text, float width) {
        return textmetrics(text, width, 0);
    }

    public Rect textmetrics(String text, float width, float height) {
        Text t = new Text(text, 0, 0, width, height);
        inheritFromContext(t);
        return t.getMetrics();
    }

    public float textwidth(String text) {
        return textmetrics(text, 0, 0).getWidth();
    }

    public float textwidth(String text, float width) {
        return textmetrics(text, width).getWidth();
    }

    public float textheight(String text) {
        return textmetrics(text, 0, 0).getHeight();
    }

    public float textheight(String text, float width) {
        return textmetrics(text, width).getHeight();
    }

    //// Utility methods ////

    public void var(String name, VarType type) {
        var(name, type, null, null, null);
    }

    public void var(String name, String type) {
        var(name, type, null, null, null);
    }

    public void var(String name, int type) {
        var(name, type, null, null, null);
    }

    public void var(String name, VarType type, Object value) {
        var(name, type, value, null, null);
    }

    public void var(String name, String type, Object value) {
        var(name, type, value, null, null);
    }

    public void var(String name, int type, Object value) {
        var(name, type, value, null, null);
    }

    public void var(String name, String type, Object value, Float min, Float max) {
        try {
            var(name, VarType.valueOf(type.toUpperCase(Locale.US)), value, min, max);
        } catch (IllegalArgumentException e) {
            throw new NodeBoxError("var: available types for var() are NUMBER, TEXT, BOOLEAN and FONT \\n\"");
        }
    }

    public void var(String name, int type, Object value, Float min, Float max) {
        try {
            var(name, VarType.values()[type], value, min, max);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NodeBoxError("var: available types for var() are NUMBER, TEXT, BOOLEAN and FONT \\n\"");
        }
    }

    public void var(String name, VarType type, Object value, Float min, Float max) {
        Node node = ProcessingContext.getCurrentContext().getNode();
        if (node == null) return;
        Parameter p = node.getParameter(name);
        if (p != null) {
            if (p.getType() != type.type) {
                p.setType(type.type);
            }
            if (p.getWidget() != type.widget) {
                p.setWidget(type.widget);
            }
            if (p.getMinimumValue() != null && !p.getMinimumValue().equals(min)) {
                p.setMinimumValue(min);
            }
            if (p.getMaximumValue() != null && !p.getMaximumValue().equals(max)) {
                p.setMaximumValue(max);
            }
        } else {
            p = node.addParameter(name, type.type);
            p.setWidget(type.widget);
            if (value != null) {
                p.setValue(value);
            }
            if (min != null || max != null) {
                p.setBoundingMethod(Parameter.BoundingMethod.HARD);
                p.setMinimumValue(min);
                p.setMaximumValue(max);
            }
            node.updateDependencies(ProcessingContext.getCurrentContext());
        }
    }

    public Parameter findVar(String name) {
        Node node = ProcessingContext.getCurrentContext().getNode();
        if (node == null) return null;
        return node.getParameter(name);
    }

    protected float normalize(float v) {
        // Bring the color into the 0-1 scale for the current colorrange
        if (colorRange == 1f) return v;
        return v / colorRange;

    }

    public double random() {
        return Math.random();
    }

    public long random(int max) {
        return Math.round(Math.random() * max);
    }

    public long random(int min, int max) {
        return Math.round(min + (Math.random() * (max - min)));
    }

    public double random(double max) {
        return Math.random() * max;
    }

    public double random(double min, double max) {
        return min + (Math.random() * (max - min));
    }

    public Object choice(List objects) {
        if (objects == null || objects.isEmpty()) return null;
        return objects.get((int) random(objects.size() - 1));
    }

    public Iterator<Point> grid(int columns, int rows) {
        return grid(columns, rows, 1, 1);
    }

    public Iterator<Point> grid(float columns, float rows) {
        return grid(Math.round(columns), Math.round(rows), 1, 1);
    }

    public Iterator<Point> grid(float columns, float rows, double columnSize, double rowSize) {
        return grid(Math.round(columns), Math.round(rows), columnSize, rowSize);
    }

    public Iterator<Point> grid(final int columns, final int rows, final double columnSize, final double rowSize) {
        return new Iterator<Point>() {
            int x = 0;
            int y = 0;

            public boolean hasNext() {
                return y < rows;
            }

            public Point next() {
                Point pt = new Point((float) (x * columnSize), (float) (y * rowSize));
                x++;
                if (x >= columns) {
                    x = 0;
                    y++;
                }
                return pt;
            }

            public void remove() {
            }
        };
    }


    //// Context drawing /////

    public void draw(Grob g) {
        if (g instanceof Path) {
            addPath((Path) g);
        } else if (g instanceof Geometry) {
            for (Path path : ((Geometry) g).getPaths()) {
                addPath(path);
            }
        } else if (g instanceof Contour) {
            addPath(((Contour) g).toPath());
        } else if (g instanceof Text) {
            addText((Text) g);
        } else {
            throw new IllegalArgumentException("Don't know how to add a " + g + " to the current context.");
        }
    }

    protected abstract void addPath(Path p);

    protected abstract void addText(Text t);

    protected void inheritFromContext(Path p) {
        p.setFillColor(fillColor == null ? null : fillColor.clone());
        p.setStrokeColor(strokeColor == null ? null : strokeColor.clone());
        p.setStrokeWidth(strokeWidth);
        TransformDelegate d = p.getTransformDelegate();
        d.transform(p, transform, true);
    }

    protected void inheritFromContext(Text t) {
        t.setFillColor(fillColor == null ? null : fillColor.clone());
        inheritFontAttributesFromContext(t);
        // todo: check if this is sufficient.
        TransformDelegate d = t.getTransformDelegate();
        d.transform(t, transform, true);
/*        Rect r = t.getBounds();
        float dx = r.getX() + r.getWidth() / 2;
        float dy = r.getY() + r.getHeight() / 2;
        if (transformMode == Transform.Mode.CENTER) {
            Transform trans = new Transform();
            trans.translate(dx, dy);
            trans.append(transform);
            trans.translate(-dx, -dy);
            t.setTransform(trans);
        } else {
            t.setTransform(transform);
        } */
    }

    private void inheritFontAttributesFromContext(Text t) {
        t.setFontName(fontName);
        t.setFontSize(fontSize);
        t.setLineHeight(lineHeight);
        t.setAlign(align);
    }

=======
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.mahout.math;

import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.function.DoubleDoubleFunction;
import org.apache.mahout.math.function.DoubleFunction;

import java.util.Iterator;

/** Implementations of generic capabilities like sum of elements and dot products */
public abstract class AbstractVector implements Vector {

  private int size;
  protected double lengthSquared = -1.0;

  protected AbstractVector(int size) {
    this.size = size;
  }

  @Override
  public double aggregate(DoubleDoubleFunction aggregator, DoubleFunction map) {
    if (size < 1) {
      throw new IllegalArgumentException("Cannot aggregate empty vector");
    }
    double result = map.apply(getQuick(0));
    for (int i = 1; i < size; i++) {
      result = aggregator.apply(result, map.apply(getQuick(i)));
    }
    return result;
  }

  @Override
  public double aggregate(Vector other, DoubleDoubleFunction aggregator, DoubleDoubleFunction combiner) {
    if (size < 1) {
      throw new IllegalArgumentException("Cannot aggregate empty vector");
    }
    double result = combiner.apply(getQuick(0), other.getQuick(0));
    for (int i = 1; i < size; i++) {
      result = aggregator.apply(result, combiner.apply(getQuick(i), other.getQuick(i)));
    }
    return result;
  }

  /**
   * Subclasses must override to return an appropriately sparse or dense result
   *
   * @param rows    the row cardinality
   * @param columns the column cardinality
   * @return a Matrix
   */
  protected abstract Matrix matrixLike(int rows, int columns);

  @Override
  public Vector viewPart(int offset, int length) {
    if (offset < 0) {
      throw new IndexException(offset, size);
    }
    if (offset + length > size) {
      throw new IndexException(offset + length, size);
    }
    return new VectorView(this, offset, length);
  }

  @Override
  public Vector clone() {
    try {
      AbstractVector r = (AbstractVector) super.clone();
      r.size = size;
      r.lengthSquared = lengthSquared;
      return r;
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException("Can't happen");
    }
  }

  @Override
  public Vector divide(double x) {
    if (x == 1.0) {
      return like().assign(this);
    }
    Vector result = like().assign(this);
    Iterator<Element> iter = result.iterateNonZero();
    while (iter.hasNext()) {
      Element element = iter.next();
      element.set(element.get() / x);
    }
    return result;
  }

  @Override
  public double dot(Vector x) {
    if (size != x.size()) {
      throw new CardinalityException(size, x.size());
    }
    if (this == x) {
      return dotSelf();
    }
    double result = 0.0;
    Iterator<Element> iter = iterateNonZero();
    while (iter.hasNext()) {
      Element element = iter.next();
      result += element.get() * x.getQuick(element.index());
    }
    return result;
  }
  
  public double dotSelf() {
    double result = 0.0;
    Iterator<Element> iter = iterateNonZero();
    while (iter.hasNext()) {
      double value = iter.next().get();
      result += value * value;
    }
    return result;
  }

  @Override
  public double get(int index) {
    if (index < 0 || index >= size) {
      throw new IndexException(index, size);
    }
    return getQuick(index);
  }

  @Override
  public Element getElement(int index) {
    return new LocalElement(index);
  }

  @Override
  public Vector minus(Vector that) {
    if (size != that.size()) {
      throw new CardinalityException(size, that.size());
    }

    // TODO: check the numNonDefault elements to further optimize
    Vector result = like().assign(this);
    Iterator<Element> iter = that.iterateNonZero();
    while (iter.hasNext()) {
      Element thatElement = iter.next();
      int index = thatElement.index();
      result.setQuick(index, this.getQuick(index) - thatElement.get());
    }
    return result;
  }

  @Override
  public Vector normalize() {
    return divide(Math.sqrt(dotSelf()));
  }

  @Override
  public Vector normalize(double power) {
    return divide(norm(power));
  }
  
  @Override
  public Vector logNormalize() {
    return logNormalize(2.0, Math.sqrt(dotSelf()));
  }
  
  @Override
  public Vector logNormalize(double power) {
    return logNormalize(power, norm(power));
  }
  
  public Vector logNormalize(double power, double normLength) {   
    // we can special case certain powers
    if (Double.isInfinite(power) || power <= 1.0) {
      throw new IllegalArgumentException("Power must be > 1 and < infinity");
    } else {
      double denominator = normLength * Math.log(power);
      Vector result = like().assign(this);
      Iterator<Element> iter = result.iterateNonZero();
      while (iter.hasNext()) {
        Element element = iter.next();
        element.set(Math.log(1 + element.get()) / denominator);
      }
      return result;
    }
  }

  @Override
  public double norm(double power) {
    if (power < 0.0) {
      throw new IllegalArgumentException("Power must be >= 0");
    }
    // we can special case certain powers
    if (Double.isInfinite(power)) {
      double val = 0.0;
      Iterator<Element> iter = this.iterateNonZero();
      while (iter.hasNext()) {
        val = Math.max(val, Math.abs(iter.next().get()));
      }
      return val;
    } else if (power == 2.0) {
      return Math.sqrt(dotSelf());
    } else if (power == 1.0) {
      double val = 0.0;
      Iterator<Element> iter = this.iterateNonZero();
      while (iter.hasNext()) {
        val += Math.abs(iter.next().get());
      }
      return val;
    } else if (power == 0.0) {
      // this is the number of non-zero elements
      double val = 0.0;
      Iterator<Element> iter = this.iterateNonZero();
      while (iter.hasNext()) {
        val += iter.next().get() == 0 ? 0 : 1;
      }
      return val;
    } else {
      double val = 0.0;
      Iterator<Element> iter = this.iterateNonZero();
      while (iter.hasNext()) {
        Element element = iter.next();
        val += Math.pow(element.get(), power);
      }
      return Math.pow(val, 1.0 / power);
    }
  }

  @Override
  public double getLengthSquared() {
    if (lengthSquared >= 0.0) {
      return lengthSquared;
    }
    return lengthSquared = dotSelf();
  }

  @Override
  public double getDistanceSquared(Vector v) {
    if (size != v.size()) {
      throw new CardinalityException(size, v.size());
    }
    // if this and v has a cached lengthSquared, dot product is quickest way to compute this.
    if (lengthSquared >= 0 && v instanceof AbstractVector && ((AbstractVector)v).lengthSquared >= 0) {
      return lengthSquared + v.getLengthSquared() - 2 * this.dot(v);
    }
    Vector randomlyAccessed;
    Iterator<Element> it;
    double d = 0.0;
    if (lengthSquared >= 0.0) {
      it = v.iterateNonZero();
      randomlyAccessed = this;
      d += lengthSquared;
    } else { // TODO: could be further optimized, figure out which one is smaller, etc
      it = iterateNonZero();
      randomlyAccessed = v;
      d += v.getLengthSquared();
    }
    while (it.hasNext()) {
      Element e = it.next();
      double value = e.get();
      d += value * (value - 2.0 * randomlyAccessed.getQuick(e.index()));
    }
    //assert d > -1.0e-9; // round-off errors should never be too far off!
    return Math.abs(d);
  }

  @Override
  public double maxValue() {
    double result = Double.NEGATIVE_INFINITY;
    int nonZeroElements = 0;
    Iterator<Element> iter = this.iterateNonZero();
    while (iter.hasNext()) {
      nonZeroElements++;
      Element element = iter.next();
      result = Math.max(result, element.get());
    }
    if (nonZeroElements < size) {
      return Math.max(result, 0.0);
    }
    return result;
  }
  
  @Override
  public int maxValueIndex() {
    int result = -1;
    double max = Double.NEGATIVE_INFINITY;
    int nonZeroElements = 0;
    Iterator<Element> iter = this.iterateNonZero();
    while (iter.hasNext()) {
      nonZeroElements++;
      Element element = iter.next();
      double tmp = element.get();
      if (tmp > max) {
        max = tmp;
        result = element.index();
      }
    }
    // if the maxElement is negative and the vector is sparse then any
    // unfilled element(0.0) could be the maxValue hence we need to
    // find one of those elements
    if (nonZeroElements < size && max < 0.0) {
      for (Element element : this) {
        if (element.get() == 0.0) {
          return element.index();
        }
      }
    }
    return result;
  }

  @Override
  public double minValue() {
    double result = Double.POSITIVE_INFINITY;
    int nonZeroElements = 0;
    Iterator<Element> iter = this.iterateNonZero();
    while (iter.hasNext()) {
      nonZeroElements++;
      Element element = iter.next();
      result = Math.min(result, element.get());
    }
    if (nonZeroElements < size) {
      return Math.min(result, 0.0);
    }
    return result;
  }

  @Override
  public int minValueIndex() {
    int result = -1;
    double min = Double.POSITIVE_INFINITY;
    int nonZeroElements = 0;
    Iterator<Element> iter = this.iterateNonZero();
    while (iter.hasNext()) {
      nonZeroElements++;
      Element element = iter.next();
      double tmp = element.get();
      if (tmp < min) {
        min = tmp;
        result = element.index();
      }
    }
    // if the maxElement is positive and the vector is sparse then any
    // unfilled element(0.0) could be the maxValue hence we need to
    // find one of those elements
    if (nonZeroElements < size && min > 0.0) {
      for (Element element : this) {
        if (element.get() == 0.0) {
          return element.index();
        }
      }
    }
    return result;
  }

  @Override
  public Vector plus(double x) {
    Vector result = like().assign(this);
    if (x == 0.0) {
      return result;
    }
    int size = result.size();
    for (int i = 0; i < size; i++) {
      result.setQuick(i, getQuick(i) + x);
    }
    return result;
  }

  @Override
  public Vector plus(Vector x) {
    if (size != x.size()) {
      throw new CardinalityException(size, x.size());
    }

    // prefer to have this be the denser than x
    if (!isDense() && (x.isDense() || x.getNumNondefaultElements() > this.getNumNondefaultElements())) {
      return x.plus(this);
    }

    Vector result = like().assign(this);
    Iterator<Element> iter = x.iterateNonZero();
    while (iter.hasNext()) {
      Element e = iter.next();
      int index = e.index();
      result.setQuick(index, this.getQuick(index) + e.get());
    }
    return result;
  }

  @Override
  public void addTo(Vector v) {
    Iterator<Element> it = iterateNonZero();
    while (it.hasNext()) {
      Element e = it.next();
      int index = e.index();
      v.setQuick(index, v.getQuick(index) + e.get());
    }
  }

  @Override
  public void set(int index, double value) {
    if (index < 0 || index >= size) {
      throw new IndexException(index, size);
    }
    setQuick(index, value);
  }

  @Override
  public Vector times(double x) {
    if (x == 0.0) {
      return like();
    }

    Vector result = like().assign(this);
    if (x == 1.0) {
      return result;
    }
    
    Iterator<Element> iter = result.iterateNonZero();
    while (iter.hasNext()) {
      Element element = iter.next();
      element.set(element.get() * x);
    }

    return result;
  }

  @Override
  public Vector times(Vector x) {
    if (size != x.size()) {
      throw new CardinalityException(size, x.size());
    }

    Vector to = this;
    Vector from = x;
    // Clone and edit to the sparse one; if both are sparse, edit the more sparse one (more zeroes)
    if (isDense() || (!x.isDense() && getNumNondefaultElements() > x.getNumNondefaultElements())) {
      to = x;
      from = this;
    }

    Vector result = to.like().assign(to);
    Iterator<Element> iter = result.iterateNonZero();
    while (iter.hasNext()) {
      Element element = iter.next();
      element.set(element.get() * from.getQuick(element.index()));
    }

    return result;
  }

  @Override
  public double zSum() {
    double result = 0.0;
    Iterator<Element> iter = iterateNonZero();
    while (iter.hasNext()) {
      result += iter.next().get();
    }

    return result;
  }

  @Override
  public Vector assign(double value) {
    for (int i = 0; i < size; i++) {
      setQuick(i, value);
    }
    return this;
  }

  @Override
  public Vector assign(double[] values) {
    if (size != values.length) {
      throw new CardinalityException(size, values.length);
    }
    for (int i = 0; i < size; i++) {
      setQuick(i, values[i]);
    }
    return this;
  }

  @Override
  public Vector assign(Vector other) {
    if (size != other.size()) {
      throw new CardinalityException(size, other.size());
    }
    for (int i = 0; i < size; i++) {
      setQuick(i, other.getQuick(i));
    }
    return this;
  }

  @Override
  public Vector assign(DoubleDoubleFunction f, double y) {
    Iterator<Element> it = f.apply(0, y) == 0 ? iterateNonZero() : iterator();
    while (it.hasNext()) {
      Element e = it.next();
      e.set(f.apply(e.get(), y));
    }
    return this;
  }

  @Override
  public Vector assign(DoubleFunction function) {
    Iterator<Element> it = function.apply(0) == 0 ? iterateNonZero() : iterator();
    while (it.hasNext()) {
      Element e = it.next();
      e.set(function.apply(e.get()));
    }
    return this;
  }

  @Override
  public Vector assign(Vector other, DoubleDoubleFunction function) {
    if (size != other.size()) {
      throw new CardinalityException(size, other.size());
    }
    for (int i = 0; i < size; i++) {
      setQuick(i, function.apply(getQuick(i), other.getQuick(i)));
    }
    return this;
  }

  @Override
  public Matrix cross(Vector other) {
    Matrix result = matrixLike(size, other.size());
    for (int row = 0; row < size; row++) {
      result.assignRow(row, other.times(getQuick(row)));
    }
    return result;
  }

  @Override
  public final int size() {
    return size;  
  }

  @Override
  public String asFormatString() {
    return toString();
  }

  @Override
  public int hashCode() {
    int result = size;
    Iterator<Element> iter = iterateNonZero();
    while (iter.hasNext()) {
      Element ele = iter.next();
      result += ele.index() * RandomUtils.hashDouble(ele.get());
    }
    return result;
  }

  /**
   * Determines whether this {@link Vector} represents the same logical vector as another
   * object. Two {@link Vector}s are equal (regardless of implementation) if the value at
   * each index is the same, and the cardinalities are the same.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Vector)) {
      return false;
    }
    Vector that = (Vector) o;
    if (size != that.size()) {
      return false;
    }
    for (int index = 0; index < size; index++) {
      if (getQuick(index) != that.getQuick(index)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append('{');
    for (int index = 0; index < size; index++) {
      double value = getQuick(index);
      if (value != 0.0) {
        result.append(index);
        result.append(':');
        result.append(value);
        result.append(',');
      }
    }
    if (result.length() > 1) {
      result.setCharAt(result.length() - 1, '}');
    }
    return result.toString();
  }


  protected final class LocalElement implements Element {
    int index;

    LocalElement(int index) {
      this.index = index;
    }

    @Override
    public double get() {
      return getQuick(index);
    }

    @Override
    public int index() {
      return index;
    }

    @Override
    public void set(double value) {
      setQuick(index, value);
    }
  }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

