/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import static java.lang.Math.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Function;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import static javafx.geometry.Pos.CENTER_LEFT;
import static javafx.geometry.Pos.CENTER_RIGHT;
import javafx.scene.Node;
import javafx.scene.control.*;
import static javafx.scene.control.ContentDisplay.CENTER;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.util.Callback;
import javafx.util.Duration;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import org.jaudiotagger.tag.images.Artwork;
import util.File.FileUtil;
import util.dev.Log;

/** 
 * Provides static utility methods for various purposes. 
 * 
 * @author _Plutonium
 */
public interface Util {
    
    /**
     * Method equivalent to object's equal method, but if both objects are null
     * they are considered equal as well.
     * Equivalent to {@code (o1==null && o2==null) || (o1!=null && o1.equals(o2));}
     */
    public static boolean nullEqual(Object o1, Object o2) {
        return (o1==null && o2==null) || (o1!=null && o1.equals(o2));
    }
    
/********************************** DEBUG *************************************/
    
    /**
     * Simple black background with no insets or radius. use for layout debugging
     * Equivalent to {@code new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY));}
     */
    public static Background SIMPLE_BGR = new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY));
    
    /**
     * Simple black border with no radius
     * Equivalent to {@code new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));}
     */
    public static Border BORDER_SIMPLE = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));
    
/********************************** STRING ************************************/
    
    /**
     * Prints out the value of Duration - string representation of the duration
     * in the format h:m:s - 00:00:00. If any of the h,m,s values is single digit,
     * decade digit '0' is still written to retain the correct format.
     * If hours = 0, they are left out.
     * Example:
     * 01:00:06
     *    04:45
     *    00:34
     * @param duration
     * @return 
     */
    public static String formatDuration(Duration duration) {
        Objects.requireNonNull(duration);
        
        double sec_total = duration.toMillis()/1000;
        int seconds = (int) sec_total %60;
        int minutes = (int)((sec_total-seconds) /60) %60;
        int hours   = (int)(sec_total-seconds-60*minutes) /3600;
        
        if(hours>99)
            return String.format("%d:%02d:%02d", hours, minutes, seconds); 
        else if (hours>0)
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);            
        else if(minutes>0)
            return String.format("%02d:%02d", minutes, seconds);
        else
            return String.format("00:%02d",seconds);            
    }
    
    /**
     * Prints out the value of Duration - string representation of the duration
     * in the format h:m:s - 00:00:00. If any of the h,m,s values is single digit,
     * decade digit '0' is written to retain the correct format only IF 
     * include_zeros = true.
     * If hours = 0, they are left out.
     * Example:
     *  1:00:06
     *     4:45
     *       34
     * @param duration
     * @param include_zeros
     * @return 
     */
    public static String formatDuration(Duration duration, boolean include_zeros) {
        Objects.requireNonNull(duration);
        if (include_zeros) return formatDuration(duration);
        
        double sec_total = duration.toMillis()/1000;
        int seconds = (int) sec_total %60;
        int minutes = (int)((sec_total-seconds) /60) %60;
        int hours   = (int)(sec_total-seconds-60*minutes) /3600;
        
        if(hours>99)
            return String.format("%3d:%2d:%2d", hours, minutes, seconds);            
        else if(hours>0)
            return String.format("%2d:%2d:%2d", hours, minutes, seconds);       
        else if (minutes>0)
            return String.format("%2d:%2d", minutes, seconds);
        else
            return String.format("%2d",seconds);
    }
    
    /**
     * Convenience method to clean String objects.
     * Assigns string empty value if it should be empty according to shouldBeEmpty()
     * method. Use as a convenient filter for suspicious String objects.
     * More formally returns:
     * (shouldBeEmpty(str)) ? "" : str;
     * @see shouldBeEmpty(String str)
     * @param str String to emptify.
     * @return "" if String should be empty, otherwise does nothing..
     */
    public static String emptifyString(String str) {
        return (shouldBeEmpty(str)) ? "" : str;
    }
    
    /**
     * Broader check for emptiness of String object.
     * Checks for: 
        - null
        - "null", "isNULL" and other combinations
        - ""
        - whitespaceOnly.
     * 
     * @param str String to check.
     * @return true if any of the above is met.
     */
    public static boolean shouldBeEmpty(String str) {
        return str == null || str.equalsIgnoreCase("null") || str.trim().isEmpty();       
    }
    
    /**
     * Checks and formats String so it can be safely used for naming a File.
     * Replaces all forbidden characters and " " with "_".
     * @param str
     * @return 
     */
    public static String filenamizeString(String str) {
        String out = str;
               out = out.replace(" ", "_");
               out = out.replace("/", "_");
               out = out.replace("\\", "_");
               out = out.replace(":", "_");
               out = out.replace("*", "_");
               out = out.replace("?", "_");
               out = out.replace("<", "_");
               out = out.replace(">", "_");
               out = out.replace("|", "_");
        return out;
    }
    
    /** 
     * Converts first letter of the string to upper case.
     */
    public static String capitalize(String s) {
        return s.isEmpty() ? "" : s.substring(0, 1).toUpperCase() + s.substring(1);
    }
    
    /** 
     * Converts first letter of the string to upper case and all others into
     * lower case.
     */
    public static String capitalizeStrong(String s) {
        return s.isEmpty() ? "" : s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
    
    /** 
     * Converts enum constant to 'human readable' string. 
     * first letter upper case, 
     * other letters lower case, 
     * '_' into ' '
     */
    public static String enumToHuman(Enum e) {
        return capitalizeStrong(e.name().replace('_', ' '));
    }
    
    /**
     * Convenience method. Equivalent to: loadImage(file, size, size);
     * @param file
     * @param size
     * @return 
     */
    public static Image loadImage(File file, double size) {
        return loadImage(file, size, size);
    }
    
    /**
     * Loads image file. with requested size.
     * <p>
     * Loads File object into Image object of desired size
     * (aspect ratio remains unaffected) to reduce memory consumption.
     * For example it is possible to use {@link Screen} class to find out
     * screen properties to dynamically set optimal resolution or limit it even
     * further for small thumbnails, where intended size is known.
     * 
     * @param file file to load.
     * @param size to resize image's width to. Use 0 to use original image size.
     * The size will be clipped to original if it is greater.
     * @return loaded image or null if file null or not a valid image source.
     */
    public static Image loadImage(File file, double width, double height) {
        if (file == null) return null;
        if (width == 0 && height == 0)
            return new Image(file.toURI().toString());
        else {
            // find out real image file resolution
            Dimension d = getImageDim(file);
            int w = d==null ? Integer.MAX_VALUE : d.width;
            int h = d==null ? Integer.MAX_VALUE : d.height;
            
            // lets not get over real size (Image unfortunately does that if we dont stop it)
            int fin_width = Math.min((int)width,w);
            int fin_height = Math.min((int)height,h);
            return new Image(file.toURI().toString(), fin_width, fin_height, true, true);
        }
    }
    
    // thx: http://stackoverflow.com/questions/672916/how-to-get-image-height-and-width-using-java
    public static Dimension getImageDim(File f) {
        Dimension result = null;
        String suffix = FileUtil.getSuffix(f.toURI());
        Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
        if (iter.hasNext()) {
            ImageReader reader = iter.next();
            try {
                ImageInputStream stream = new FileImageInputStream(f);
                reader.setInput(stream);
                int width = reader.getWidth(reader.getMinIndex());
                int height = reader.getHeight(reader.getMinIndex());
                result = new Dimension(width, height);
            } catch (IOException e) {
                Log.warn("Problem finding out image size" + e.getMessage());
            } finally {
                reader.dispose();
            }
        } else
            throw new RuntimeException("No reader found for given file: " + f.getPath());

        return result;
    }
    
    /**
     * Compares two Artwork objects.
     * Artwork's equals() method doesnt return true properly. Use this method
     * instead.
     * <p>
     * Method is deprecated as Artwork should not be used anyway. The method
     * works well though.
     * @param art1
     * @param art2
     * @return 
     */
    @Deprecated
    public static boolean equals(Artwork art1, Artwork art2) {
        if (art1 == null && art2 == null) { return true; }
        if (art1 == null || art2 == null) { return false; }
        return Arrays.equals(art1.getBinaryData(), art2.getBinaryData());
    }

    
    
    /**
     * Logarithm
     * @param base of the log
     * @param i number to calculate log for
     * @return base specified logarithm of the number
     */
    static int log(int base, int i) {
        short p = 0;
        while(pow(base, p) <= i)
            p++;
        return p;
    }
    
    /**
     * @param number
     * @return number of digits of a number
     */
    public static int digits(int number) {
        int x = number;
        int cifres = 0;
        while (x > 0) {
            x /= 10;
            cifres++;
        }
        return cifres;
    }
    
    /**
     * Creates zeropadded string - string of a number with '0' added in to
     * maintain consistency in number of length.
     * @param n number to turn onto zeropadded string
     * @param max number to zeropad to
     * @param ch character to use. Notable characters are: ' ' or '0'
     * @return 
     */
    public static String zeroPad(int n, int max, char ch) {
        int diff = digits(max) - digits(n);
        String out = "";
        for (int i=1; i<=diff; i++)
            out += ch;
        return out + String.valueOf(n);
    }
    
    /**
     * @return highest possible number of the same decadic length as specified
     * number in absolute value.
     * Examples:  9 for 1-10, 99 for 10-99, 999 for nubmers 100-999, etc...
     */
    public static int decMin1(int n) {
        // normally we would do the below
        // return n==0 ? 0 : (int) (pow(10, 1+digits(n))-1);
        // but why not make this perform faster
        if(n==0) return n;
        n = abs(n);
        if(n<10) return 9;
        else if(n<100) return 99;
        else if(n<1000) return 999;
        else if(n<10000) return 9999;
        else if(n<100000) return 99999;
        else if(n<1000000) return 999999;
        else return (int) (pow(10, 1+digits(n))-1);
    }
    
    /**
     * Returns file itself if exists or its existing parent recursively. If
     * null or no parent exists, returns application location.
     */
    public static File getExistingParent(File f, File defaultFile) {
        if (f==null) return defaultFile;
        if (f.exists()) return f;
        else return getExistingParent(f.getParentFile(), defaultFile);
    }
    
    /** @return {@code max(min,min(i,max))} */
    public static int clip(int min, int i, int max) {
        return max(min,min(i,max));
    }
    /** @return {@code max(min,min(i,max))} */
    public static long clip(long min, long i, long max) {
        return max(min,min(i,max));
    }
    /** @return {@code max(min,min(i,max))} */
    public static float clip(float min, float i, float max) {
        return max(min,min(i,max));
    }
    /** @return {@code max(min,min(i,max))} */
    public static double clip(double min, double i, double max) {
        return max(min,min(i,max));
    }
    
/******************************** GRAPHICS ************************************/
    
    /** Sets anchors for given node within its parent AnchorPane. */
    public static void setAnchors(Node n, double a) {
        AnchorPane.setTopAnchor(n, a);
        AnchorPane.setRightAnchor(n, a);
        AnchorPane.setBottomAnchor(n, a);
        AnchorPane.setLeftAnchor(n, a);
    }
    
    /** Sets anchors for given node within its parent AnchorPane. */
    public static void setAnchors(Node n, double top, double right, double bottom, double left) {
        AnchorPane.setTopAnchor(n, top);
        AnchorPane.setRightAnchor(n, right);
        AnchorPane.setBottomAnchor(n, bottom);
        AnchorPane.setLeftAnchor(n, left);
    }
    
    /**
     * Returns copy of the selected items of the table. Because the original list
     * is observable, changes would show up if it was used as a parameter. Often,
     * we need a 'snapshot' of the selected items list at the moment and we dont
     * want that snapshot to mutate.
     * @param <T> type of element in the list
     * @param t
     * @return 
     */
    public static<T> List<T> copySelectedItems(TableView<T> t) {
        return new ArrayList(t.getSelectionModel().getSelectedItems());
    }
    
    /**
     * Creates column that indexes rows from 1 and is right aligned. The column 
     * is of type Void - table data type agnostic.
     * @param name name of the column. For example "#"
     * @return the column
     */
    public static<T> TableColumn<T,Void> createIndexColumn(String name) {
        TableColumn<T,Void> indexColumn = new TableColumn(name);
        indexColumn.setSortable(false);
        indexColumn.setCellFactory( column -> 
            new TableCell<T,Void>(){
                {
                    // we want to align the index to the right, not left
                    setAlignment(CENTER_RIGHT);
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) setText(null);
                    else setText(String.valueOf(getIndex()+1)+ ".");
                }
            }
        );
        return indexColumn;
    }
    
    /**
     * Creates default cell factory, which sets cell text to provided text when 
     * cells text equals "". This is to differentiate between empty cell and nonempty
     * cell with 'empty' value.
     * For example: '<empty cell>'
     * @param empty_value
     * @return 
     */
    public static Callback<TableColumn<?,?>, TableCell<?,?>> EMPTY_TEXT_DEFAULT_CELL_FACTORY(String empty_value) { 
       return new Callback<TableColumn<?,?>, TableCell<?,?>>() {
            @Override public TableCell<?,?> call(TableColumn<?,?> param) {
                return new TableCell<Object,Object>() {
                    @Override protected void updateItem(Object item, boolean empty) {
                        if (item == getItem()) return;

                        super.updateItem(item, empty);

                        if (item == null) {
                            super.setText(null);
                            super.setGraphic(null);
                        } else if ("".equals(item)) {
                            super.setText(empty_value);
                            super.setGraphic(null);
                        } else if (item instanceof Node) {
                            super.setText(null);
                            super.setGraphic((Node)item);
                        } else {
                            super.setText(item.toString());
                            super.setGraphic(null);
                        }
                    }
                };
            }
        };
    }
    
    /**
     * Same as {@link #DEFAULT_ALIGNED_CELL_FACTORY(javafx.geometry.Pos)}, but
     * the alignment is inferred from the type of element in the cell (not table
     * or column, because we are aligning cell content) in the following way: 
     * String content is aligned to CENTER_LEFT and the rest CENTER_RIGHT.
     * <p>
     * The factory will need to be cast if it its generic types are declared.
     * 
     * @param type for cell content.
     */
    public static<T,O> Callback<TableColumn<T,O>,TableCell<T,O>> DEFAULT_ALIGNED_CELL_FACTORY(Class<O> type, String no_val_text) {
        Pos al = type.equals(String.class) ? CENTER_LEFT : CENTER_RIGHT;
        return DEFAULT_ALIGNED_CELL_FACTORY(al, no_val_text);
    }
    
    /**
     * Returns {@link TableColumn.DEFAULT_CELL_FACTORY} (the default factory used
     * when no factory is specified), aligning the cell content to specified value.
     * <p>
     * The factory will need to be cast if it its generic types are declared.
     * @param a cell alignment
     * @return 
     */
    public static<T,O> Callback<TableColumn<T,O>,TableCell<T,O>> DEFAULT_ALIGNED_CELL_FACTORY(Pos a, String no_val_text) {
        return column -> {
            TableCell c = EMPTY_TEXT_DEFAULT_CELL_FACTORY(no_val_text).call(column);
                      c.setAlignment(a);
            return c;
        };
    }
    
    /**
     * Convenience method to make it easier to select given rows of the
     * TableView via its SelectionModel.
     * This methods provides alternative to TableViewSelectionModel.selectIndices()
     * that requires array parameter. This method makes the appropriate conversions
     * and selects the items using List parameter
     * <p>
     * After the method is invoked only the provided rows will be selected - it
     * clears any previously selected rows.
     * @param selectedIndexes
     * @param selectionModel
     */
    public static void selectRows(List<Integer> selectedIndexes, TableViewSelectionModel<?> selectionModel) {
        selectionModel.clearSelection();
        int[] newSelected = new int[selectedIndexes.size()];
        for (int i = 0; i < selectedIndexes.size(); i++) {
            newSelected[i] = selectedIndexes.get(i);
        }
        if (newSelected.length != 0) {
            selectionModel.selectIndices(newSelected[0], newSelected);
        }
    }
    
    public static final EventHandler<MouseEvent> consumeOnSecondaryButton = e-> {
        if (e.getButton()==MouseButton.SECONDARY) e.consume();
    };
    
    public static MenuItem createmenuItem(String text, EventHandler<ActionEvent> actionHandler) {
        MenuItem i = new MenuItem(text);
                 i.setOnAction(actionHandler);
        return i;
    }
    
    public static Label createIcon(AwesomeIcon icon, int size, String tooltip, EventHandler<MouseEvent> onClick) {
        Label i = AwesomeDude.createIconLabel(icon,"",String.valueOf(size)+"px",String.valueOf(GUI.GUI.font.getValue().getSize()),CENTER);
              i.setOnMouseClicked(onClick);
        if(tooltip!=null && !tooltip.isEmpty()) i.setTooltip(new Tooltip(tooltip));
        return i;
    }
    
/***************************** REFLECTION *************************************/
    
    /**
     * Returns all declared fields of the class including inherited ones.
     * Equivalent to union of declared fields of the class and all its
     * superclasses.
     */
    public static List<Field> getAllFields(Class clazz) {
       List<Field> fields = new ArrayList();
       // get all fields of the class (but not inherited fields)
       fields.addAll(Arrays.asList(clazz.getDeclaredFields()));

       Class superClazz = clazz.getSuperclass();
       // get super class' fields recursively
       if(superClazz != null) fields.addAll(getAllFields(superClazz));

       return fields;
    }
    
    /**
     * Returns all declared methods of the class including inherited ones.
     * Equivalent to union of declared fields of the class and all its
     * superclasses.
     */
    public static List<Method> getAllMethods(Class clazz) {
       List<Method> methods = new ArrayList();
       // get all fields of the class (but not inherited fields)
       methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));

       Class superClazz = clazz.getSuperclass();
       // get super class' fields recursively
       if(superClazz != null) methods.addAll(getAllMethods(superClazz));

       return methods;
   }
    
    public static Field getField(Class clazz, String name) throws NoSuchFieldException {
       // get all fields of the class (but not inherited fields)
       Field f = null;
        try {
            f = clazz.getDeclaredField(name);
        } catch (NoSuchFieldException | SecurityException ex) {
            // ignore
        }
       
       if (f!=null) return f;
       
       Class superClazz = clazz.getSuperclass();
       // get super class' fields recursively
       if (superClazz != null) return getField(superClazz, name);
       else throw new NoSuchFieldException();
    }
    
    /**
     * Converts primitive to wrappers, otherwise does nothing. 
     * @param c
     * @return Object class of given class or class itself if not primitive.
     */
    public static Class unPrimitivize(Class c) {
        if(c.isPrimitive()) {
            if(c.equals(boolean.class)) return Boolean.class;
            if(c.equals(int.class)) return Integer.class;
            if(c.equals(float.class)) return Float.class;
            if(c.equals(double.class)) return Double.class;
            if(c.equals(long.class)) return Long.class;
            if(c.equals(byte.class)) return Byte.class;
            if(c.equals(short.class)) return Short.class;
            if(c.equals(char.class)) return Character.class;
        }
        return c;
    }
    
    /**
     * Returns i-th generic parameter of the field starting from 0. 
     * For example {@code Integer for List<Integer>}
     * @param f
     * @return 
     */
    public static Class getGenericType(Field f, int i) {
            ParameterizedType pType = (ParameterizedType) f.getGenericType();
            Class<?> genericType = (Class<?>) pType.getActualTypeArguments()[i];
            return genericType;
    }
    
    /**
     * Returns i-th generic parameter of the class starting from 0.
     * For example Integer for {@code IntegerList extends List<Integer>}
     * <p>
     * Will NOT work on variables, using getClass() method on them.
     * 
     * @param c
     * @param i
     * @return 
     */
    public static Class getGenericClass(Class c, int i) {
        return (Class) ((ParameterizedType) c.getGenericSuperclass()).getActualTypeArguments()[i];
    }
    
    /**
     * Same as {@link #getGenericClass(java.lang.Class, int)} but for interfaces.
     * Returns p-th generic parameter of the i-th interface of c class starting from 0.
     * 
     * @param c
     * @param i
     * @param p
     * @return 
     */
    public static Class getGenericInterface(Class c, int i, int p) {
        return (Class) ((ParameterizedType) c.getGenericInterfaces()[i]).getActualTypeArguments()[p];
    }
    
    /**
     * Renames declared enum sonstant using the mapper function on the enum
     * constant string.
     * <p>
     * This method effectively overrides both enum's toString() and valueOf() 
     * methods. It allows using arbitrary string values for enum constants,
     * but in toString/valueOf cpliant way.
     * <p>
     * Use in enum constructor. For example:
     * <p>
     * <pre>
     * {@code
     *  class MyEnum {
     *      A,
     *      B;
     * 
     *      public MuEnum() {
     *          mapEnumConstant(MyEnum.class, this, String::toLowerCase);
     *      }
     *  }
     * }
     * </pre>
     * <p>
     * @param <E>
     * @param c class
     * @param e enum constant/instance
     * @param mapper function to apply on the constant
     */
    public static<E extends Enum>void mapEnumConstant(E e, Function<E, String> mapper) {
        try {
            Field f = e.getClass().getSuperclass().getDeclaredFields()[0];
            f.setAccessible(true);
            f.set(e, mapper.apply(e));
            f.setAccessible(false);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Returns enum constants of an enum class in declared order. Works for 
     * enums with class method
     * bodies (for which Enum.getEnumConstants) does not work.
     * @param <T>
     * @param c
     * @return 
     */
    public static <T> T[] getEnumConstants(Class c) {
        // handle enums
        if(c.isEnum()) return (T[]) c.getEnumConstants();

        // handle enum with class method bodies (they are not recognized as enums)
        else {
            Class ec = c.getEnclosingClass();
            if(ec!=null && ec.isEnum()) 
                return (T[]) ec.getEnumConstants();
            else 
                throw new IllegalArgumentException("Class " + c + " is not an Enum.");
        }
    }

}

