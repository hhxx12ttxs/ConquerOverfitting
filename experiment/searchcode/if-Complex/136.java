        // only show button if complex child object
        this.btnGotoCategory.setVisible(obj instanceof ComplexContentObjectImplementation);
import chamiloda.gui.implementations.RepositoryUI;
import chamiloda.domain.contentobjects.ComplexContentObjectImplementation;
import chamiloda.domain.contentobjects.ContentObjectImplementation;
        txtLastModified.setEditable(false);
        }
        else if (obj instanceof ComplexContentObjectImplementation)
        {
    private ContentObjectImplementation viewedObject = null;
    private ComplexContentObjectImplementation viewedComplexObject = null;

