import org.eclipse.jface.viewers.Viewer;
import com.ose.pmd.dump.AbstractBlock;

public class BlockContentProvider implements IStructuredContentProvider
{
private AbstractBlock[] blocks;

public void inputChanged(Viewer viewer, Object oldInput, Object newInput)

