package org.sigmah.client.page.project.logframe;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sigmah.client.EventBus;
import org.sigmah.client.dispatch.Dispatcher;
import org.sigmah.client.i18n.I18N;
import org.sigmah.client.page.project.logframe.FormWindow.FormSubmitListener;
import org.sigmah.client.page.project.logframe.grid.ActionsMenu;
import org.sigmah.client.page.project.logframe.grid.FlexTableView;
import org.sigmah.client.page.project.logframe.grid.FlexTableView.FlexTableViewListener;
import org.sigmah.client.page.project.logframe.grid.GroupActionMenu;
import org.sigmah.client.page.project.logframe.grid.HTMLTableUtils;
import org.sigmah.client.page.project.logframe.grid.IndicatorListWidget;
import org.sigmah.client.page.project.logframe.grid.Row;
import org.sigmah.client.page.project.logframe.grid.RowActionsMenu;
import org.sigmah.client.page.project.logframe.grid.RowsGroup;
import org.sigmah.shared.domain.logframe.LogFrameGroupType;
import org.sigmah.shared.dto.logframe.ExpectedResultDTO;
import org.sigmah.shared.dto.logframe.LogFrameActivityDTO;
import org.sigmah.shared.dto.logframe.LogFrameDTO;
import org.sigmah.shared.dto.logframe.LogFrameGroupDTO;
import org.sigmah.shared.dto.logframe.LogFrameModelDTO;
import org.sigmah.shared.dto.logframe.PrerequisiteDTO;
import org.sigmah.shared.dto.logframe.SpecificObjectiveDTO;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Slider;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 * Represents a log frame grid.
 * 
 * @author tmi
 * 
 */
public class ProjectLogFrameGrid {

    /**
     * Manages log frame events.
     * 
     * @author tmi
     * 
     */

    public static interface LogFrameGridListener {

        /**
         * Method called when the log frame has been edited.
         */
        public void logFrameEdited();
    }

    /**
     * CSS style name for the entire grid.
     */
    private static final String CSS_LOG_FRAME_GRID_STYLE_NAME = "logframe-grid";

    /**
     * CSS style name for the action button which add elements.
     */
    private static final String CSS_ADD_ACTION_STYLE_NAME = CSS_LOG_FRAME_GRID_STYLE_NAME + "-add-action";

    /**
     * CSS style name for the action button which add groups.
     */
    private static final String CSS_ADD_GROUP_ACTION_STYLE_NAME = CSS_LOG_FRAME_GRID_STYLE_NAME + "-add-group-action";

    /**
     * CSS style name for the labels which display codes.
     */
    private static final String CSS_CODE_LABEL_STYLE_NAME = CSS_LOG_FRAME_GRID_STYLE_NAME + "-code-label";

    /**
     * CSS style name for the labels which display codes (active state).
     */
    private static final String CSS_CODE_LABEL_ACTIVE_STYLE_NAME = CSS_CODE_LABEL_STYLE_NAME + "-active";

    /**
     * CSS style name for the menus buttons.
     */
    private static final String CSS_MENU_BUTTON_STYLE_NAME = CSS_LOG_FRAME_GRID_STYLE_NAME + "-menu-button";

    /**
     * CSS style name for the menus buttons (active state).
     */
    private static final String CSS_MENU_BUTTON_ACTIVE_STYLE_NAME = CSS_MENU_BUTTON_STYLE_NAME + "-active";

    /**
     * Listeners.
     */
    private final ArrayList<LogFrameGridListener> listeners;

    /**
     * The current displayed log frame.
     */
    private LogFrameDTO logFrame;

    /**
     * The current displayed log frame model.
     */
    private LogFrameModelDTO logFrameModel;

    /**
     * If the log frame is read only or not.
     */
    private boolean readOnly;

    /**
     * The grid used to manage the log frame.
     */
    public final FlexTable table;

    /**
     * The number of the columns in the log frame grid.
     */
    private int columnsCount = 0;

    /**
     * The form window for adding elements.
     */
    private final FormWindow formWindow;

    /**
     * A view of the flex table in charge of the specific objectives.
     */
    private FlexTableView specificObjectivesView;

    /**
     * A view of the flex table in charge of the expected results.
     */
    private FlexTableView expectedResultsView;

    /**
     * A view of the flex table in charge of the activities.
     */
    private FlexTableView activitiesView;

    /**
     * A view of the flex table in charge of the prerequisites.
     */
    private FlexTableView prerequisitesView;

    private int databaseId;

    private final Dispatcher dispatcher;

    private final EventBus eventBus;

    /**
     * Builds an empty grid.
     */
    public ProjectLogFrameGrid(EventBus eventBus, Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        this.eventBus = eventBus;

        listeners = new ArrayList<LogFrameGridListener>();
        table = new FlexTable();
        formWindow = new FormWindow();
    }

    /**
     * Registers a listener.
     * 
     * @param l
     *            The new listener.
     */
    public void addListener(LogFrameGridListener l) {
        this.listeners.add(l);
    }

    /**
     * Unregisters a listener.
     * 
     * @param l
     *            The old listener.
     */
    public void removeListener(LogFrameGridListener l) {
        this.listeners.remove(l);
    }

    /**
     * Informs the view that the log frame has been edited.
     */
    protected void fireLogFrameEdited() {
        for (final LogFrameGridListener l : listeners) {
            l.logFrameEdited();
        }
    }

    /**
     * Returns the main widget.
     * 
     * @return the main widget.
     */
    public Widget getWidget() {
        return table;
    }

    /**
     * Clears table content.
     */
    protected void resetTable() {
        table.clear(true);
        table.removeAllRows();
    }

    /**
     * Checks if a log frame is currently displayed in the grid. If not, an
     * exception is thrown.
     */
    protected void ensureLogFrame() {

        if (logFrame == null) {
            throw new IllegalStateException(
                    "No log frame currently displayed. Specify a log frame before adding an element.");
        }
    }

    /**
     * Initializes table content.
     */
    protected void initTable() {

        resetTable();
       
        // Table parameters.
        table.setCellPadding(0);
        table.setCellSpacing(0);

        // Columns sizes.
        table.getColumnFormatter().setWidth(0, "100px");
        table.getColumnFormatter().setWidth(1, "50px");
        table.getColumnFormatter().setWidth(2, "50px");
        table.getColumnFormatter().setWidth(3, "22%");
        table.getColumnFormatter().setWidth(4, "18%");
        table.getColumnFormatter().setWidth(5, "18%");
        table.getColumnFormatter().setWidth(6, "22%");

        // Columns headers labels.
        final Label interventionLogicLabel = new Label(I18N.CONSTANTS.logFrameInterventionLogic());
        final Label indicatorsLabel = new Label(I18N.CONSTANTS.indicators());
        final Label meansOfVerificationLabel = new Label(I18N.CONSTANTS.logFrameMeansOfVerification());
        final Label risksAndAssumptionsLabel = new Label(I18N.CONSTANTS.logFrameRisksAndAssumptions());

        table.getFlexCellFormatter().setColSpan(0, 1, 2);
        table.setWidget(0, 2, interventionLogicLabel);
        table.setWidget(0, 3, indicatorsLabel);
        table.setWidget(0, 4, meansOfVerificationLabel);
        table.setWidget(0, 5, risksAndAssumptionsLabel);

        // Rows headers labels (and actions).

        // Specific objectives.
        final Label specificObjectivesLabel = new Label(I18N.CONSTANTS.logFrameSpecificObjectives() + " ("
                + I18N.CONSTANTS.logFrameSpecificObjectivesCode() + ")");

        final Label specificObjectivesButton = new Label(I18N.CONSTANTS.logFrameAddRow());
        specificObjectivesButton.addStyleName(CSS_ADD_ACTION_STYLE_NAME);
        specificObjectivesButton.setTitle(I18N.CONSTANTS.logFrameAddOS());
        specificObjectivesButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent e) {
                addSpecificObjective();
            }
        });

        final Label specificObjectivesGroupsButton = new Label(I18N.CONSTANTS.logFrameAddGroup());
        specificObjectivesGroupsButton.addStyleName(CSS_ADD_GROUP_ACTION_STYLE_NAME);
        specificObjectivesGroupsButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent e) {
                addSpecificObjectivesGroup();
            }
        });

        // Are groups enabled ?
        if (!logFrameModel.getEnableSpecificObjectivesGroups()) {
            specificObjectivesGroupsButton.setVisible(false);
        }

        final Grid specificObjectivesGrid = new Grid(3, 1);
        specificObjectivesGrid.setWidth("100%");
        specificObjectivesGrid.setCellPadding(0);
        specificObjectivesGrid.setCellSpacing(0);
        specificObjectivesGrid.setWidget(0, 0, specificObjectivesLabel);

        if (!readOnly) {
            specificObjectivesGrid.setWidget(1, 0, specificObjectivesButton);
            specificObjectivesGrid.setWidget(2, 0, specificObjectivesGroupsButton);
        }

        // Expected results.
        final Label exceptedResultsLabel = new Label(I18N.CONSTANTS.logFrameExceptedResults() + " ("
                + I18N.CONSTANTS.logFrameExceptedResultsCode() + ")");

        final Label expectedResultsButton = new Label(I18N.CONSTANTS.logFrameAddRow());
        expectedResultsButton.addStyleName(CSS_ADD_ACTION_STYLE_NAME);
        expectedResultsButton.setTitle(I18N.CONSTANTS.logFrameAddER());
        expectedResultsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent e) {
                addExpectedResult();
            }
        });

        final Label expectedResultsGroupsButton = new Label(I18N.CONSTANTS.logFrameAddGroup());
        expectedResultsGroupsButton.addStyleName(CSS_ADD_GROUP_ACTION_STYLE_NAME);
        expectedResultsGroupsButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent e) {
                addExpectedResultsGroup();
            }
        });

        // Are groups enabled ?
        if (!logFrameModel.getEnableExpectedResultsGroups()) {
            expectedResultsGroupsButton.setVisible(false);
        }

        final Grid exceptedResultsGrid = new Grid(3, 1);
        exceptedResultsGrid.setWidth("100%");
        exceptedResultsGrid.setCellPadding(0);
        exceptedResultsGrid.setCellSpacing(0);
        exceptedResultsGrid.setWidget(0, 0, exceptedResultsLabel);

        if (!readOnly) {
            exceptedResultsGrid.setWidget(1, 0, expectedResultsButton);
            exceptedResultsGrid.setWidget(2, 0, expectedResultsGroupsButton);
        }

        // Activities.
        final Label activitiesLabel = new Label(I18N.CONSTANTS.logFrameActivities());

        final Label activitiesButton = new Label(I18N.CONSTANTS.logFrameAddRow());
        activitiesButton.addStyleName(CSS_ADD_ACTION_STYLE_NAME);
        activitiesButton.setTitle(I18N.CONSTANTS.logFrameAddA());
        activitiesButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent e) {
                addActivity();
            }
        });

        final Label activitiesGroupsButton = new Label(I18N.CONSTANTS.logFrameAddGroup());
        activitiesGroupsButton.addStyleName(CSS_ADD_GROUP_ACTION_STYLE_NAME);
        activitiesGroupsButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent e) {
                addActivitiesGroup();
            }
        });

        // Are groups enabled ?
        if (!logFrameModel.getEnableActivitiesGroups()) {
            activitiesGroupsButton.setVisible(false);
        }

        final Grid activitiesGrid = new Grid(3, 1);
        activitiesGrid.setWidth("100%");
        activitiesGrid.setCellPadding(0);
        activitiesGrid.setCellSpacing(0);
        activitiesGrid.setWidget(0, 0, activitiesLabel);

        if (!readOnly) {
            activitiesGrid.setWidget(1, 0, activitiesButton);
            activitiesGrid.setWidget(2, 0, activitiesGroupsButton);
        }

        // Prerequisites.
        final Label prerequisitesLabel = new Label(I18N.CONSTANTS.logFramePrerequisites());

        final Label prerequisitesButton = new Label(I18N.CONSTANTS.logFrameAddRow());
        prerequisitesButton.addStyleName(CSS_ADD_ACTION_STYLE_NAME);
        prerequisitesButton.setTitle(I18N.CONSTANTS.logFrameAddP());
        prerequisitesButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent e) {
                addPrerequisite();
            }
        });

        final Label prerequisitesGroupButton = new Label(I18N.CONSTANTS.logFrameAddGroup());
        prerequisitesGroupButton.addStyleName(CSS_ADD_GROUP_ACTION_STYLE_NAME);
        prerequisitesGroupButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent e) {
                addPrerequisitesGroup();
            }
        });

        // Are groups enabled ?
        if (!logFrameModel.getEnablePrerequisitesGroups()) {
            prerequisitesGroupButton.setVisible(false);
        }

        final Grid prerequisitesGrid = new Grid(3, 1);
        prerequisitesGrid.setWidth("100%");
        prerequisitesGrid.setCellPadding(0);
        prerequisitesGrid.setCellSpacing(0);
        prerequisitesGrid.setWidget(0, 0, prerequisitesLabel);

        if (!readOnly) {
            prerequisitesGrid.setWidget(1, 0, prerequisitesButton);
            prerequisitesGrid.setWidget(2, 0, prerequisitesGroupButton);
        }

        table.setWidget(1, 0, specificObjectivesGrid);
        table.setWidget(2, 0, exceptedResultsGrid);
        table.setWidget(3, 0, activitiesGrid);
        table.setWidget(4, 0, prerequisitesGrid);

        // Header styles.
        HTMLTableUtils.applyHeaderStyles(table, true);

        // Initializes grid views.
        columnsCount = 7;

        specificObjectivesView = new FlexTableView(table, columnsCount, 1);
        expectedResultsView = new FlexTableView(table, columnsCount, 2);
        activitiesView = new FlexTableView(table, columnsCount, 3);
        prerequisitesView = new FlexTableView(table, columnsCount, 4);

        specificObjectivesView.addDependency(expectedResultsView);
        specificObjectivesView.addDependency(activitiesView);
        specificObjectivesView.addDependency(prerequisitesView);

        expectedResultsView.addDependency(activitiesView);
        expectedResultsView.addDependency(prerequisitesView);

        activitiesView.addDependency(prerequisitesView);

        // Views listeners.

        specificObjectivesView.addFlexTableViewListener(new FlexTableViewListener() {

            @Override
            public void rowRemoved(RowsGroup<?> group, Row<?> row) {

                // Checks if the max number of elements is reached.
                final Integer max = logFrameModel.getSpecificObjectivesMax();
                if (max != null && max > 0 && max > specificObjectivesView.getRowsCount()) {
                    specificObjectivesButton.setVisible(true);
                }
            }

            @Override
            public void rowAdded(RowsGroup<?> group, Row<?> row) {

                // Checks if the max number of elements is reached.
                final Integer max = logFrameModel.getSpecificObjectivesMax();
                if (max != null && max > 0 && max <= specificObjectivesView.getRowsCount()) {
                    specificObjectivesButton.setVisible(false);
                }
            }

            @Override
            public void groupAdded(RowsGroup<?> group) {

                // Checks if the max number of groups is reached.
                final Integer max = logFrameModel.getSpecificObjectivesGroupsMax();
                if (max != null && max > 1 && max <= specificObjectivesView.getGroupsCount()) {
                    specificObjectivesGroupsButton.setVisible(false);
                }
            }
        });

        expectedResultsView.addFlexTableViewListener(new FlexTableViewListener() {

            @Override
            public void rowRemoved(RowsGroup<?> group, Row<?> row) {

                // Checks if the max number of elements is reached.
                final Integer max = logFrameModel.getExpectedResultsMax();
                if (max != null && max > 0 && max > expectedResultsView.getRowsCount()) {
                    expectedResultsButton.setVisible(true);
                }
            }

            @Override
            public void rowAdded(RowsGroup<?> group, Row<?> row) {

                // Checks if the max number of elements is reached.
                final Integer max = logFrameModel.getExpectedResultsMax();
                if (max != null && max > 0 && max <= expectedResultsView.getRowsCount()) {
                    expectedResultsButton.setVisible(false);
                }
            }

            @Override
            public void groupAdded(RowsGroup<?> group) {

                // Checks if the max number of groups is reached.
                final Integer max = logFrameModel.getExpectedResultsGroupsMax();
                if (max != null && max > 1 && max <= expectedResultsView.getGroupsCount()) {
                    expectedResultsGroupsButton.setVisible(false);
                }
            }
        });

        activitiesView.addFlexTableViewListener(new FlexTableViewListener() {

            @Override
            public void rowRemoved(RowsGroup<?> group, Row<?> row) {

                // Checks if the max number of elements is reached.
                final Integer max = logFrameModel.getActivitiesMax();
                if (max != null && max > 0 && max > activitiesView.getRowsCount()) {
                    activitiesButton.setVisible(true);
                }
            }

            @Override
            public void rowAdded(RowsGroup<?> group, Row<?> row) {

                // Checks if the max number of elements is reached.
                final Integer max = logFrameModel.getActivitiesMax();
                if (max != null && max > 0 && max <= activitiesView.getRowsCount()) {
                    activitiesButton.setVisible(false);
                }
            }

            @Override
            public void groupAdded(RowsGroup<?> group) {

                // Checks if the max number of groups is reached.
                final Integer max = logFrameModel.getActivitiesGroupsMax();
                if (max != null && max > 1 && max <= activitiesView.getGroupsCount()) {
                    activitiesGroupsButton.setVisible(false);
                }
            }
        });

        prerequisitesView.addFlexTableViewListener(new FlexTableViewListener() {

            @Override
            public void rowRemoved(RowsGroup<?> group, Row<?> row) {

                // Checks if the max number of elements is reached.
                final Integer max = logFrameModel.getPrerequisitesMax();
                if (max != null && max > 0 && max > prerequisitesView.getRowsCount()) {
                    prerequisitesButton.setVisible(true);
                }
            }

            @Override
            public void rowAdded(RowsGroup<?> group, Row<?> row) {

                // Checks if the max number of elements is reached.
                final Integer max = logFrameModel.getPrerequisitesMax();
                if (max != null && max > 0 && max <= prerequisitesView.getRowsCount()) {
                    prerequisitesButton.setVisible(false);
                }
            }

            @Override
            public void groupAdded(RowsGroup<?> group) {

                // Checks if the max number of groups is reached.
                final Integer max = logFrameModel.getPrerequisitesGroupsMax();
                if (max != null && max > 1 && max <= prerequisitesView.getGroupsCount()) {
                    prerequisitesGroupButton.setVisible(false);
                }
            }
        });
    }

    /**
     * Displays the log frame content in the log frame grid (specific
     * objectives, expected results, prerequisites, activities);
     * 
     * @param table
     *            The log frame grid.
     * @param logFrame
     *            The log frame.
     */
    public void displayLogFrame(int databaseId, LogFrameDTO logFrame) {
        displayLogFrame(databaseId, logFrame, true);
    }

    /**
     * Displays the log frame content in the log frame grid (specific
     * objectives, expected results, prerequisites, activities);
     * 
     * @param table
     *            The log frame grid.
     * @param enabled
     *            If the log frame is read only or not.
     * @param logFrame
     *            The log frame.
     */
    public void displayLogFrame(int databaseId, LogFrameDTO logFrame, boolean enabled) {
        this.logFrame = logFrame;
        this.databaseId = databaseId;
        this.logFrameModel = logFrame.getLogFrameModel();
        this.readOnly = !enabled;

        ensureLogFrame();

        resetTable();
        initTable();

        // Displays all the groups (even the empty ones).
        for (final LogFrameGroupDTO group : logFrame.getGroups()) {
            switch (group.getType()) {
            case SPECIFIC_OBJECTIVE:
                if (logFrameModel.getEnableSpecificObjectivesGroups()) {
                    addSpecificObjectivesGroup(group);
                }
                break;
            case EXPECTED_RESULT:
                if (logFrameModel.getEnableExpectedResultsGroups()) {
                    addExpectedResultsGroup(group);
                }
                break;
            case PREREQUISITE:
                if (logFrameModel.getEnablePrerequisitesGroups()) {
                    addPrerequisitesGroup(group);
                }
                break;
            case ACTIVITY:
                if (logFrameModel.getEnableActivitiesGroups()) {
                    addActivitiesGroup(group);
                }
                break;
            }
        }

        // Displays the specific objectives (and recursively the expected
        // results and the activities).
        for (final SpecificObjectiveDTO objective : logFrame.getSpecificObjectives()) {
            addSpecificObjective(objective);
        }

        // Displays the prerequisietes.
        for (final PrerequisiteDTO prerequisite : logFrame.getPrerequisites()) {
            addPrerequisite(prerequisite);
        }
    }

    /**
     * Updates the log frame instance of the grid after an updating.
     * 
     * @param logFrame
     *            The updated log frame.
     */
    public void updateLogFrame(LogFrameDTO logFrame) {

        // For the moment, each save action requires rebuilding the whole log
        // frame. Its needed to update the ids of the new entities in the local
        // maps.
        // TODO optimize this
        displayLogFrame(databaseId, logFrame);

        // this.logFrame = logFrame;
        // this.logFrameModel = logFrame.getLogFrameModelDTO();
    }

    // ------------------------------------------------------------------------
    // - SPECIFIC OBJECTIVES
    // ------------------------------------------------------------------------

    // ------------------------
    // -- GROUPS
    // ------------------------

    /**
     * Creates a new display group for the specific objectives.
     */
    protected void addSpecificObjectivesGroup() {

        ensureLogFrame();

        // Checks if the model allows groups.
        if (!logFrameModel.getEnableSpecificObjectivesGroups()) {
            MessageBox.alert(I18N.CONSTANTS.logFrameUnauthorizedAction(), I18N.CONSTANTS.logFrameGroupsDisabledOS(),
                    null);
            return;
        }

        // Checks if the max number of groups is reached.
        final Integer max = logFrameModel.getSpecificObjectivesGroupsMax();
        if (max != null && max > 1 && max <= specificObjectivesView.getGroupsCount()) {
            MessageBox.alert(I18N.CONSTANTS.logFrameUnauthorizedAction(), I18N.CONSTANTS.logFrameGroupsMaxReachedOS(),
                    null);
            return;
        }

        // Asks for the new group label.
        MessageBox.prompt(I18N.CONSTANTS.logFrameAddGroup(), I18N.CONSTANTS.logFrameAddGroupToOS(), false,
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {

                        // OK.
                        if (Dialog.OK.equals(be.getButtonClicked().getItemId())) {

                            String label = ProjectLogFramePresenter.DEFAULT_GROUP_LABEL;
                            if (be.getValue() != null) {
                                label = be.getValue();
                            }

                            // Creates the new group.
                            final LogFrameGroupDTO group = logFrame.addGroup(label,
                                    LogFrameGroupType.SPECIFIC_OBJECTIVE);

                            // Displays it.
                            addSpecificObjectivesGroup(group);
                        }
                    }
                });
    }

    /**
     * Adds a new display group for the specific objectives.
     * 
     * @param group
     *            The specific objectives groups.
     * @return The just created group.
     */
    private RowsGroup<LogFrameGroupDTO> addSpecificObjectivesGroup(final LogFrameGroupDTO group) {

        ensureLogFrame();

        final RowsGroup<LogFrameGroupDTO> g = new RowsGroup<LogFrameGroupDTO>(group) {

            @Override
            public String getTitle(LogFrameGroupDTO userObject) {

                // Builds the title (prefix + label).
                final StringBuilder sb = new StringBuilder();
                sb.append(I18N.CONSTANTS.logFrameGroup());
                sb.append(" (");
                sb.append(I18N.CONSTANTS.logFrameSpecificObjectivesCode());
                sb.append(") - ");
                sb.append(userObject.getLabel());

                return sb.toString();
            }

            @Override
            public int getId(LogFrameGroupDTO userObject) {
                return userObject.getClientSideId();
            }

            @Override
            public int[] getMergedColumnIndexes(LogFrameGroupDTO userObject) {
                return new int[] { 1 };
            }

            @Override
            public boolean isVisible(LogFrameGroupDTO userObject) {
                return logFrameModel.getEnableSpecificObjectivesGroups();
            }

            @Override
            public Widget getWidget() {

                // Displays the group's label.
                final Label label = new Label(getTitle());

                // Grid.
                final Grid grid = new Grid(1, 2);
                grid.setCellPadding(0);
                grid.setCellSpacing(0);
                grid.setWidget(0, 0, label);
                if (!readOnly) {
                    grid.setWidget(0, 1, buildGroupMenu(specificObjectivesView, this, label));
                }

                return grid;
            }
        };

        // Adds a new group.
        specificObjectivesView.addGroup(g);

        fireLogFrameEdited();

        return g;
    }

    // ------------------------
    // -- ROWS
    // ------------------------

    /**
     * Adds a new specific objective empty row.
     */
    protected void addSpecificObjective() {

        ensureLogFrame();

        // Checks if the max number of elements is reached.
        final Integer max = logFrameModel.getSpecificObjectivesMax();
        if (max != null && max > 0 && max <= specificObjectivesView.getRowsCount()) {
            MessageBox.alert(I18N.CONSTANTS.logFrameUnauthorizedAction(), I18N.CONSTANTS.logFrameMaxReachedOS(), null);
            return;
        }

        // Must select a group.
        if (logFrameModel.getEnableSpecificObjectivesGroups()) {

            // Sets the form window.
            formWindow.clear();
            formWindow.addChoicesList(I18N.CONSTANTS.logFrameGroup(),
                    logFrame.getAllGroupsNotDeleted(LogFrameGroupType.SPECIFIC_OBJECTIVE), false, "label");
            formWindow.addFormSubmitListener(new FormSubmitListener() {

                @Override
                public void formSubmitted(Object... elements) {

                    // Checks that the values are correct.
                    final Object element = elements[0];
                    if (!(element instanceof LogFrameGroupDTO)) {
                        return;
                    }

                    // Retrieves the selected group.
                    final LogFrameGroupDTO group = (LogFrameGroupDTO) element;

                    // Creates and displays a new objective.
                    final SpecificObjectiveDTO objective = logFrame.addSpecificObjective();
                    objective.setGroup(group);
                    addSpecificObjective(objective);
                }
            });

            formWindow.show(I18N.CONSTANTS.logFrameAddOS(), I18N.CONSTANTS.logFrameSelectGroupOS());
        }
        // Groups are disabled, no need to select a group, the default one will
        // be selected.
        else {

            // Retrieves the default group.
            final LogFrameGroupDTO group = logFrame.getDefaultGroup(LogFrameGroupType.SPECIFIC_OBJECTIVE);

            // Creates and displays a new objective.
            final SpecificObjectiveDTO objective = logFrame.addSpecificObjective();
            objective.setGroup(group);
            addSpecificObjective(objective);
        }
    }

    /**
     * Adds a specific objective row.
     * 
     * @param specificObjective
     *            The specific objective. Must not be <code>null</code>.
     */
    private void addSpecificObjective(final SpecificObjectiveDTO specificObjective) {

        // Checks if the objective is correct.
        if (specificObjective == null) {
            throw new NullPointerException("specific objective must not be null");
        }

        // Retrieves the group.
        final LogFrameGroupDTO logFrameGroup = specificObjective.getGroup();

        // Retrieves the equivalent rows group.
        @SuppressWarnings("unchecked")
        RowsGroup<LogFrameGroupDTO> g = (RowsGroup<LogFrameGroupDTO>) specificObjectivesView.getGroup(logFrameGroup
                .getClientSideId());

        // If the rows hasn't been created already, adds it.
        if (g == null) {
            g = addSpecificObjectivesGroup(logFrameGroup);
        }

        // Sets the display label.
        final StringBuilder sb = new StringBuilder();
        sb.append(I18N.CONSTANTS.logFrameSpecificObjectivesCode());
        sb.append(" ");
        sb.append(specificObjective.getFormattedCode());
        specificObjective.setLabel(sb.toString());

        // Sets the position the last if it doesn't exist.
        if (specificObjective.getPosition() == null) {
            specificObjective.setPosition(g.getRowsCount() + 1);
        }

        // Adds the row.
        specificObjectivesView.insertRow(specificObjective.getPosition(), logFrameGroup.getClientSideId(),
                new Row<SpecificObjectiveDTO>(specificObjective) {

                    @Override
                    public boolean isSimilar(int column, SpecificObjectiveDTO userObject, SpecificObjectiveDTO other) {

                        switch (column) {
                        case 1:
                            // Code.
                            return userObject.getCode() == other.getCode();
                        }
                        return false;
                    }

                    @Override
                    public Widget getWidgetAt(int column, final SpecificObjectiveDTO userObject) {

                        switch (column) {
                        case 0:

                            // Parent code.
                            return null;

                        case 1:

                            // Code.
                            final Label codeLabel = new Label();
                            codeLabel.addStyleName(CSS_CODE_LABEL_STYLE_NAME);

                            if (userObject != null) {
                                codeLabel.setText(userObject.getLabel());
                            }

                            // Grid.
                            final Grid grid = new Grid(1, 2);
                            grid.setCellPadding(0);
                            grid.setCellSpacing(0);
                            grid.setWidget(0, 0, codeLabel);

                            if (!readOnly) {
                                grid.setWidget(0, 1, buildSpecificObjectiveMenu(this, codeLabel));
                            }

                            return grid;

                        case 2:

                            // Intervention logic.
                            final TextArea interventionLogicTextBox = new TextArea();
                            interventionLogicTextBox.setWidth("100%");
                            interventionLogicTextBox.setHeight("100%");
                            interventionLogicTextBox.setVisibleLines(3);
                            interventionLogicTextBox.addStyleName("html-textbox");
                            interventionLogicTextBox.setEnabled(!readOnly);

                            if (userObject != null) {
                                interventionLogicTextBox.setText(userObject.getInterventionLogic());
                            }

                            interventionLogicTextBox.addChangeHandler(new ChangeHandler() {
                                @Override
                                public void onChange(ChangeEvent e) {
                                    userObject.setInterventionLogic(interventionLogicTextBox.getText());
                                    fireLogFrameEdited();
                                }
                            });

                            return interventionLogicTextBox;

                        case 4:

                            // Indicators.
                            IndicatorListWidget indicatorListWidget = new IndicatorListWidget(eventBus, dispatcher, databaseId, specificObjective);
							indicatorListWidget.addValueChangeHandler(new ValueChangeHandler<Void>() {
								
								@Override
								public void onValueChange(ValueChangeEvent<Void> event) {
									fireLogFrameEdited();	
								}
							});
                            return indicatorListWidget;                        

                        case 5:

                            // Risks and Assumptions.
                            final TextArea risksAssumptionsTextBox = new TextArea();
                            risksAssumptionsTextBox.setWidth("100%");
                            risksAssumptionsTextBox.setHeight("100%");
                            risksAssumptionsTextBox.setVisibleLines(3);
                            risksAssumptionsTextBox.addStyleName("html-textbox");
                            risksAssumptionsTextBox.setEnabled(!readOnly);

                            if (userObject != null) {
                            	risksAssumptionsTextBox.setText(userObject.getRisksAndAssumptions());
                            }

                            risksAssumptionsTextBox.addChangeHandler(new ChangeHandler() {
                                @Override
                                public void onChange(ChangeEvent e) {
                                    userObject.setRisksAndAssumptions(risksAssumptionsTextBox.getText());
                                    fireLogFrameEdited();
                                }
                            });

                            return risksAssumptionsTextBox;

                        default:
                            return null;
                        }
                    }

                    @Override
                    public int getId(SpecificObjectiveDTO userObject) {
                        return userObject.getClientSideId();
                    }
                });

        fireLogFrameEdited();

        // Adds sub expected results.
        for (final ExpectedResultDTO result : specificObjective.getExpectedResults()) {
            addExpectedResult(result);
        }

    }

    // ------------------------------------------------------------------------
    // - EXPECTED RESULTS
    // ------------------------------------------------------------------------

    // ------------------------
    // -- GROUPS
    // ------------------------

    /**
     * Creates a new display group for the expected results.
     */
    protected void addExpectedResultsGroup() {

        ensureLogFrame();

        // Checks if the model allows groups.
        if (!logFrameModel.getEnableExpectedResultsGroups()) {
            MessageBox.alert(I18N.CONSTANTS.logFrameUnauthorizedAction(), I18N.CONSTANTS.logFrameGroupsDisabledER(),
                    null);
            return;
        }

        // Checks if the max number of groups is reached.
        final Integer max = logFrameModel.getExpectedResultsGroupsMax();
        if (max != null && max > 1 && max <= expectedResultsView.getGroupsCount()) {
            MessageBox.alert(I18N.CONSTANTS.logFrameUnauthorizedAction(), I18N.CONSTANTS.logFrameGroupsMaxReachedER(),
                    null);
            return;
        }

        // Asks for the new group label.
        MessageBox.prompt(I18N.CONSTANTS.logFrameAddGroup(), I18N.CONSTANTS.logFrameAddGroupToER(), false,
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {

                        // OK.
                        if (Dialog.OK.equals(be.getButtonClicked().getItemId())) {

                            String label = ProjectLogFramePresenter.DEFAULT_GROUP_LABEL;
                            if (be.getValue() != null) {
                                label = be.getValue();
                            }

                            // Creates the new group.
                            final LogFrameGroupDTO group = logFrame.addGroup(label, LogFrameGroupType.EXPECTED_RESULT);

                            // Displays it.
                            addExpectedResultsGroup(group);
                        }
                    }
                });
    }

    /**
     * Adds a new display group for the expected results.
     * 
     * @param group
     *            The expected results groups.
     * @return The just created group.
     */
    private RowsGroup<LogFrameGroupDTO> addExpectedResultsGroup(final LogFrameGroupDTO group) {

        ensureLogFrame();

        final RowsGroup<LogFrameGroupDTO> g = new RowsGroup<LogFrameGroupDTO>(group) {

            @Override
            public String getTitle(LogFrameGroupDTO userObject) {

                // Builds the title (prefix + label).
                final StringBuilder sb = new StringBuilder();
                sb.append(I18N.CONSTANTS.logFrameGroup());
                sb.append(" (");
                sb.append(I18N.CONSTANTS.logFrameExceptedResultsCode());
                sb.append(") - ");
                sb.append(userObject.getLabel());

                return sb.toString();
            }

            @Override
            public int getId(LogFrameGroupDTO userObject) {
                return userObject.getClientSideId();
            }

            @Override
            public int[] getMergedColumnIndexes(LogFrameGroupDTO userObject) {
                return new int[] { 0 };
            }

            @Override
            public boolean isVisible(LogFrameGroupDTO userObject) {
                return logFrameModel.getEnableExpectedResultsGroups();
            }

            @Override
            public Widget getWidget() {
                // Displays the group's label.
                final Label label = new Label(getTitle());

                // Grid.
                final Grid grid = new Grid(1, 2);
                grid.setCellPadding(0);
                grid.setCellSpacing(0);
                grid.setWidget(0, 0, label);
                if (!readOnly) {
                    grid.setWidget(0, 1, buildGroupMenu(expectedResultsView, this, label));
                }

                return grid;
            }
        };

        // Adds a new group.
        expectedResultsView.addGroup(g);

        fireLogFrameEdited();

        return g;
    }

    // ------------------------
    // -- ROWS
    // ------------------------

    /**
     * Adds a new expected result empty row.
     */
    protected void addExpectedResult() {

        ensureLogFrame();

        // Checks if the max number of elements is reached.
        final Integer max = logFrameModel.getExpectedResultsMax();
        if (max != null && max > 0 && max <= expectedResultsView.getRowsCount()) {
            MessageBox.alert(I18N.CONSTANTS.logFrameUnauthorizedAction(), I18N.CONSTANTS.logFrameMaxReachedER(), null);
            return;
        }

        final List<SpecificObjectiveDTO> objectives = logFrame.getSpecificObjectives();

        // Checks if there is at least one available specific objective.
        if (objectives.isEmpty()) {
            MessageBox.alert(I18N.CONSTANTS.logFrameNoSpecificObjective(),
                    I18N.CONSTANTS.logFrameNoSpecificObjectiveDetails(), null);
            return;
        }

        // Must select a group.
        if (logFrameModel.getEnableExpectedResultsGroups()) {

            // Sets the form window.
            formWindow.clear();
            formWindow.addChoicesList(I18N.CONSTANTS.logFrameSpecificObjective(), objectives, false, "label");
            formWindow.addChoicesList(I18N.CONSTANTS.logFrameGroup(),
                    logFrame.getAllGroupsNotDeleted(LogFrameGroupType.EXPECTED_RESULT), false, "label");
            formWindow.addFormSubmitListener(new FormSubmitListener() {

                @Override
                public void formSubmitted(Object... elements) {

                    // Checks that the values are correct.
                    final Object element0 = elements[0];
                    if (!(element0 instanceof SpecificObjectiveDTO)) {
                        return;
                    }

                    final Object element1 = elements[1];
                    if (!(element1 instanceof LogFrameGroupDTO)) {
                        return;
                    }

                    // Retrieves the selected OS and group.
                    final SpecificObjectiveDTO specificObjective = (SpecificObjectiveDTO) element0;
                    final LogFrameGroupDTO group = (LogFrameGroupDTO) element1;

                    // Creates and displays a new objective.
                    final ExpectedResultDTO result = specificObjective.addExpectedResult();
                    result.setGroup(group);
                    addExpectedResult(result);
                }
            });

            formWindow.show(I18N.CONSTANTS.logFrameAddER(), I18N.CONSTANTS.logFrameSelectGroupER());
        }
        // Groups are disabled, no need to select a group, the default one will
        // be selected.
        else {

            // Sets the form window.
            formWindow.clear();
            formWindow.addChoicesList(I18N.CONSTANTS.logFrameSpecificObjective(), logFrame.getSpecificObjectives(),
                    false, "label");
            formWindow.addFormSubmitListener(new FormSubmitListener() {

                @Override
                public void formSubmitted(Object... elements) {

                    // Checks that the values are correct.
                    final Object element0 = elements[0];
                    if (!(element0 instanceof SpecificObjectiveDTO)) {
                        return;
                    }

                    // Retrieves the selected OS.
                    final SpecificObjectiveDTO specificObjective = (SpecificObjectiveDTO) element0;

                    // Retrieves the default group.
                    final LogFrameGroupDTO group = logFrame.getDefaultGroup(LogFrameGroupType.EXPECTED_RESULT);

                    // Creates and displays a new objective.
                    final ExpectedResultDTO result = specificObjective.addExpectedResult();
                    result.setGroup(group);
                    addExpectedResult(result);
                }
            });

            formWindow.show(I18N.CONSTANTS.logFrameAddER(), I18N.CONSTANTS.logFrameSelectGroup2ER());
        }
    }

    /**
     * Adds an activity row.
     * 
     * @param result
     *            The expected result. Must not be <code>null</code>.
     */
    private void addExpectedResult(final ExpectedResultDTO result) {

        // Checks if the result is correct.
        if (result == null) {
            throw new NullPointerException("result must not be null");
        }

        // Retrieves the group.
        final LogFrameGroupDTO group = result.getGroup();

        // Retrieves the equivalent rows group.
        @SuppressWarnings("unchecked")
        RowsGroup<LogFrameGroupDTO> g = (RowsGroup<LogFrameGroupDTO>) expectedResultsView.getGroup(group
                .getClientSideId());

        // If the rows hasn't been created already, adds it.
        if (g == null) {
            g = addExpectedResultsGroup(group);
        }

        // Sets the display label.
        final StringBuilder sb = new StringBuilder();
        sb.append(I18N.CONSTANTS.logFrameExceptedResultsCode());
        sb.append(" ");
        sb.append(result.getFormattedCode());
        result.setLabel(sb.toString());

        // Sets the position the last if it doesn't exist.
        if (result.getPosition() == null) {
            result.setPosition(g.getRowsCount() + 1);
        }

        // Adds the row.
        expectedResultsView.insertRow(result.getPosition(), group.getClientSideId(),
                new Row<ExpectedResultDTO>(result) {

                    @Override
                    public boolean isSimilar(int column, ExpectedResultDTO userObject, ExpectedResultDTO other) {

                        switch (column) {
                        case 0:
                            // Parent code.
                            return userObject.getParentSpecificObjective() != null
                                    && other.getParentSpecificObjective() != null
                                    && userObject.getParentSpecificObjective().getCode() == other
                                            .getParentSpecificObjective().getCode();
                        }
                        return false;
                    }

                    @Override
                    public Widget getWidgetAt(int column, final ExpectedResultDTO userObject) {

                        switch (column) {
                        case 0:

                            // Parent code.
                            final Label parentCodeLabel = new Label();
                            parentCodeLabel.addStyleName(CSS_CODE_LABEL_STYLE_NAME);

                            final SpecificObjectiveDTO parent;
                            if (userObject != null && (parent = userObject.getParentSpecificObjective()) != null) {

                                final StringBuilder sb = new StringBuilder();

                                sb.append(I18N.CONSTANTS.logFrameExceptedResultsCode());
                                sb.append(" (");
                                sb.append(I18N.CONSTANTS.logFrameSpecificObjectivesCode());
                                sb.append(" ");
                                sb.append(parent.getFormattedCode());
                                sb.append(")");

                                parentCodeLabel.setText(sb.toString());
                            }

                            return parentCodeLabel;

                        case 1:

                            // Code.
                            final Label codeLabel = new Label();
                            codeLabel.addStyleName(CSS_CODE_LABEL_STYLE_NAME);

                            if (userObject != null) {
                                codeLabel.setText(userObject.getLabel());
                            }

                            // Grid.
                            final Grid grid = new Grid(1, 2);
                            grid.setCellPadding(0);
                            grid.setCellSpacing(0);
                            grid.setWidget(0, 0, codeLabel);
                            if (!readOnly) {
                                grid.setWidget(0, 1, buildExpectedResultMenu(this, codeLabel));
                            }

                            return grid;

                        case 2:

                            // Intervention logic.
                            final TextArea interventionLogicTextBox = new TextArea();
                            interventionLogicTextBox.setWidth("100%");
                            interventionLogicTextBox.setHeight("100%");
                            interventionLogicTextBox.setVisibleLines(3);
                            interventionLogicTextBox.addStyleName("html-textbox");
                            interventionLogicTextBox.setEnabled(!readOnly);

                            if (userObject != null) {
                                interventionLogicTextBox.setText(userObject.getInterventionLogic());
                            }

                            interventionLogicTextBox.addChangeHandler(new ChangeHandler() {
                                @Override
                                public void onChange(ChangeEvent e) {
                                    userObject.setInterventionLogic(interventionLogicTextBox.getText());
                                    fireLogFrameEdited();
                                }
                            });

                            return interventionLogicTextBox;

                        case 4:

                            // Indicators.
                            IndicatorListWidget indicatorListWidget = new IndicatorListWidget(eventBus, dispatcher, databaseId, result);
                            indicatorListWidget.addValueChangeHandler(new ValueChangeHandler<Void>() {
								
								@Override
								public void onValueChange(ValueChangeEvent<Void> event) {
									fireLogFrameEdited();
								}
							});
							return indicatorListWidget;
                       

                        case 5:

                        	 // Risks and Assumptions.
                            final TextArea risksAssumptionsTextBox = new TextArea();
                            risksAssumptionsTextBox.setWidth("100%");
                            risksAssumptionsTextBox.setHeight("100%");
                            risksAssumptionsTextBox.setVisibleLines(3);
                            risksAssumptionsTextBox.addStyleName("html-textbox");
                            risksAssumptionsTextBox.setEnabled(!readOnly);

                            if (userObject != null) {
                            	risksAssumptionsTextBox.setText(userObject.getRisksAndAssumptions());
                            }

                            risksAssumptionsTextBox.addChangeHandler(new ChangeHandler() {
                                @Override
                                public void onChange(ChangeEvent e) {
                                    userObject.setRisksAndAssumptions(risksAssumptionsTextBox.getText());
                                    fireLogFrameEdited();
                                }
                            });

                            return risksAssumptionsTextBox;

                        default:
                            return null;
                        }
                    }

                    @Override
                    public int getId(ExpectedResultDTO userObject) {
                        return userObject.getClientSideId();
                    }
                });

        fireLogFrameEdited();

        // Adds sub activities.
        for (final LogFrameActivityDTO activity : result.getActivities()) {
            addActivity(activity);
        }

    }

    // ------------------------------------------------------------------------
    // - ACTIVITIES
    // ------------------------------------------------------------------------

    // ------------------------
    // -- GROUPS
    // ------------------------

    /**
     * Creates a new display group for the activities.
     */
    protected void addActivitiesGroup() {

        ensureLogFrame();

        // Checks if the model allows groups.
        if (!logFrameModel.getEnableActivitiesGroups()) {
            MessageBox.alert(I18N.CONSTANTS.logFrameUnauthorizedAction(), I18N.CONSTANTS.logFrameGroupsDisabledA(),
                    null);
            return;
        }

        // Checks if the max number of groups is reached.
        final Integer max = logFrameModel.getActivitiesGroupsMax();
        if (max != null && max > 1 && max <= activitiesView.getGroupsCount()) {
            MessageBox.alert(I18N.CONSTANTS.logFrameUnauthorizedAction(), I18N.CONSTANTS.logFrameGroupsMaxReachedA(),
                    null);
            return;
        }

        // Asks for the new group label.
        MessageBox.prompt(I18N.CONSTANTS.logFrameAddGroup(), I18N.CONSTANTS.logFrameAddGroupToA(), false,
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {

                        // OK.
                        if (Dialog.OK.equals(be.getButtonClicked().getItemId())) {

                            String label = ProjectLogFramePresenter.DEFAULT_GROUP_LABEL;
                            if (be.getValue() != null) {
                                label = be.getValue();
                            }

                            // Creates the new group.
                            final LogFrameGroupDTO group = logFrame.addGroup(label, LogFrameGroupType.ACTIVITY);

                            // Displays it.
                            addActivitiesGroup(group);
                        }
                    }
                });
    }

    /**
     * Adds a new display group for the activities.
     * 
     * @param group
     *            The activities group.
     * @return The just created group.
     */
    private RowsGroup<LogFrameGroupDTO> addActivitiesGroup(final LogFrameGroupDTO group) {

        ensureLogFrame();

        final RowsGroup<LogFrameGroupDTO> g = new RowsGroup<LogFrameGroupDTO>(group) {

            @Override
            public String getTitle(LogFrameGroupDTO userObject) {

                // Builds the title (prefix + label).
                final StringBuilder sb = new StringBuilder();
                sb.append(I18N.CONSTANTS.logFrameGroup());
                sb.append(" (");
                sb.append(I18N.CONSTANTS.logFrameActivitiesCode());
                sb.append(") - ");
                sb.append(userObject.getLabel());

                return sb.toString();
            }

            @Override
            public int getId(LogFrameGroupDTO userObject) {
                return userObject.getClientSideId();
            }

            @Override
            public int[] getMergedColumnIndexes(LogFrameGroupDTO userObject) {
                return new int[] { 0 };
            }

            @Override
            public boolean isVisible(LogFrameGroupDTO userObject) {
                return logFrameModel.getEnableActivitiesGroups();
            }

            @Override
            public Widget getWidget() {
                // Displays the group's label.
                final Label label = new Label(getTitle());

                // Grid.
                final Grid grid = new Grid(1, 2);
                grid.setCellPadding(0);
                grid.setCellSpacing(0);
                grid.setWidget(0, 0, label);
                if (!readOnly) {
                    grid.setWidget(0, 1, buildGroupMenu(activitiesView, this, label));
                }

                return grid;
            }
        };

        // Adds a new group.
        activitiesView.addGroup(g);

        fireLogFrameEdited();

        return g;
    }

    // ------------------------
    // -- ROWS
    // ------------------------

    /**
     * Adds a new activity empty row.
     */
    protected void addActivity() {

        ensureLogFrame();

        // Checks if the max number of elements is reached.
        final Integer max = logFrameModel.getActivitiesMax();
        if (max != null && max > 0 && max <= activitiesView.getRowsCount()) {
            MessageBox.alert(I18N.CONSTANTS.logFrameUnauthorizedAction(), I18N.CONSTANTS.logFrameMaxReachedA(), null);
            return;
        }

        final List<ExpectedResultDTO> results = logFrame.getAllExpectedResultsDTO();

        // Checks if there is at least one available result.
        if (results == null || results.isEmpty()) {
            MessageBox.alert(I18N.CONSTANTS.logFrameNoExceptedResults(),
                    I18N.CONSTANTS.logFrameNoExceptedResultsDetails(), null);
            return;
        }

        // Must select a group.
        if (logFrameModel.getEnableActivitiesGroups()) {

            // Sets the form window.
            formWindow.clear();
            formWindow.addChoicesList(I18N.CONSTANTS.logFrameExceptedResult(), results, false, "label");
            formWindow.addChoicesList(I18N.CONSTANTS.logFrameGroup(),
                    logFrame.getAllGroups(LogFrameGroupType.ACTIVITY), false, "label");
            formWindow.addDateField(I18N.CONSTANTS.logFrameActivityStartDate(), true);
            formWindow.addDateField(I18N.CONSTANTS.logFrameActivityEndDate(), true);
            formWindow.addFormSubmitListener(new FormSubmitListener() {

                @Override
                public void formSubmitted(Object... elements) {

                    // Checks that the values are correct.
                    final Object element0 = elements[0];
                    if (!(element0 instanceof ExpectedResultDTO)) {
                        return;
                    }

                    final Object element1 = elements[1];
                    if (!(element1 instanceof LogFrameGroupDTO)) {
                        return;
                    }

                    final Object element2 = elements[2];
                    if (element2 != null && !(element2 instanceof Date)) {
                        return;
                    }

                    final Object element3 = elements[3];
                    if (element3 != null && !(element3 instanceof Date)) {
                        return;
                    }

                    // Retrieves the selected ER and group and activity params.
                    final ExpectedResultDTO expectedResult = (ExpectedResultDTO) element0;
                    final LogFrameGroupDTO group = (LogFrameGroupDTO) element1;
                    final Date startDate = element2 != null ? (Date) element2 : null;
                    final Date endDate = element3 != null ? (Date) element3 : null;

                    // Creates and displays a new activity.
                    final LogFrameActivityDTO activity = expectedResult.addActivity();
                    activity.setGroup(group);
                    activity.setStartDate(startDate);
                    activity.setEndDate(endDate);
                    activity.setAdvancement(0); // advancement set to 0 at the
                                                // begin
                    addActivity(activity);
                }
            });

            formWindow.show(I18N.CONSTANTS.logFrameAddA(), I18N.CONSTANTS.logFrameSelectGroupA());
        }
        // Groups are disabled, no need to select a group, the default one will
        // be selected.
        else {

            // Sets the form window.
            formWindow.clear();
            formWindow.addChoicesList(I18N.CONSTANTS.logFrameExceptedResult(), results, false, "label");
            formWindow.addDateField(I18N.CONSTANTS.logFrameActivityStartDate(), true);
            formWindow.addDateField(I18N.CONSTANTS.logFrameActivityEndDate(), true);
            formWindow.addFormSubmitListener(new FormSubmitListener() {

                @Override
                public void formSubmitted(Object... elements) {

                    // Checks that the values are correct.
                    final Object element0 = elements[0];
                    if (!(element0 instanceof ExpectedResultDTO)) {
                        return;
                    }

                    final Object element1 = elements[1];

                    if (element1 != null && !(element1 instanceof Date)) {
                        return;
                    }

                    final Object element2 = elements[2];
                    if (element2 != null && !(element2 instanceof Date)) {
                        return;
                    }

                    // Retrieves the selected ER and group and activity params.
                    final ExpectedResultDTO expectedResult = (ExpectedResultDTO) element0;
                    final LogFrameGroupDTO group = logFrame.getDefaultGroup(LogFrameGroupType.ACTIVITY);
                    final Date startDate = element1 != null ? (Date) element1 : null;
                    final Date endDate = element2 != null ? (Date) element2 : null;

                    // Creates and displays a new activity.
                    final LogFrameActivityDTO activity = expectedResult.addActivity();
                    activity.setGroup(group);
                    activity.setStartDate(startDate);
                    activity.setEndDate(endDate);
                    activity.setAdvancement(0); // advancement set to 0 at the
                                                // begin
                    addActivity(activity);
                }
            });

            formWindow
