import limelight.util.Box;

public class FittingYOffsetStrategy implements YOffsetStrategy
{
public int calculateYOffset(TextModel model)
{
int yOffset = model.getYOffset();
Box boundingBox = model.getContainer().getBounds();

