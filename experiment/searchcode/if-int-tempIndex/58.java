package nl.tudelft.lime.edit.policy;

import nl.tudelft.lime.model.LimeSubpart;
import nl.tudelft.lime.model.commands.CreateCommand;
import nl.tudelft.lime.model.commands.ReorderPartCommand;
import nl.tudelft.lime.model.component.LimeDiagram;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editpolicies.TreeContainerEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;

import java.util.List;


/**
 * Displays the outline tree in the outline view
 * @author mazaninfardi
 *
 */
public class LimeTreeContainerEditPolicy extends TreeContainerEditPolicy {
    protected Command createCreateCommand(LimeSubpart child, Rectangle r,
        int index, String label) {
        CreateCommand cmd = new CreateCommand();
        Rectangle rect;

        if (r == null) {
            rect = new Rectangle();
            rect.setSize(new Dimension(-1, -1));
        } else {
            rect = r;
        }

        cmd.setLocation(rect);
        cmd.setParent((LimeDiagram) getHost().getModel());
        cmd.setChild(child);
        cmd.setLabel(label);

        if (index >= 0) {
            cmd.setIndex(index);
        }

        return cmd;
    }

    @SuppressWarnings("unchecked")
    protected Command getAddCommand(ChangeBoundsRequest request) {
        CompoundCommand command = new CompoundCommand();
        command.setDebugLabel("Add in LimeTreeContainerEditPolicy"); //$NON-NLS-1$

        List<EditPart> editparts = (List<EditPart>) request.getEditParts();
        int index = findIndexOfTreeItemAt(request.getLocation());

        for (int i = 0; i < editparts.size(); i++) {
            EditPart child = (EditPart) editparts.get(i);

            if (isAncestor(child, getHost())) {
                command.add(UnexecutableCommand.INSTANCE);
            } else {
                LimeSubpart childModel = (LimeSubpart) child.getModel();
                command.add(createCreateCommand(childModel,
                        new Rectangle(new org.eclipse.draw2d.geometry.Point(),
                            childModel.getSize()), index,
                        "Reparent Lime Subpart")); //$NON-NLS-1$
            }
        }

        return command;
    }

    protected Command getCreateCommand(CreateRequest request) {
        LimeSubpart child = (LimeSubpart) request.getNewObject();
        int index = findIndexOfTreeItemAt(request.getLocation());

        return createCreateCommand(child, null, index, "Create LimeSubpart"); //$NON-NLS-1$
    }

    @SuppressWarnings("unchecked")
    protected Command getMoveChildrenCommand(ChangeBoundsRequest request) {
        CompoundCommand command = new CompoundCommand();
        List<EditPart> editparts = (List<EditPart>) request.getEditParts();
        List children = getHost().getChildren();
        int newIndex = findIndexOfTreeItemAt(request.getLocation());

        for (int i = 0; i < editparts.size(); i++) {
            EditPart child = editparts.get(i);
            int tempIndex = newIndex;
            int oldIndex = children.indexOf(child);

            if ((oldIndex == tempIndex) || ((oldIndex + 1) == tempIndex)) {
                command.add(UnexecutableCommand.INSTANCE);

                return command;
            } else if (oldIndex <= tempIndex) {
                tempIndex--;
            }

            command.add(new ReorderPartCommand((LimeSubpart) child.getModel(),
                    (LimeDiagram) getHost().getModel(), tempIndex));
        }

        return command;
    }

    protected boolean isAncestor(EditPart source, EditPart target) {
        if (source == target) {
            return true;
        }

        if (target.getParent() != null) {
            return isAncestor(source, target.getParent());
        }

        return false;
    }
}

