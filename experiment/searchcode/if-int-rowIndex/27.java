/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.client.page.project.dashboard;

import java.util.Arrays;
import java.util.Date;

import org.sigmah.client.dispatch.Dispatcher;
import org.sigmah.client.dispatch.remote.Authentication;
import org.sigmah.client.i18n.I18N;
import org.sigmah.client.icon.IconImageBundle;
import org.sigmah.client.page.project.ProjectPresenter;
import org.sigmah.client.page.project.dashboard.funding.FundingIconProvider;
import org.sigmah.client.ui.FlexibleGrid;
import org.sigmah.client.ui.StylableHBoxLayout;
import org.sigmah.client.util.DateUtils;
import org.sigmah.client.util.NumberUtils;
import org.sigmah.shared.domain.profile.GlobalPermissionEnum;
import org.sigmah.shared.dto.ProjectFundingDTO;
import org.sigmah.shared.dto.element.FlexibleElementDTO;
import org.sigmah.shared.dto.element.FlexibleElementType;
import org.sigmah.shared.dto.profile.ProfileUtils;
import org.sigmah.shared.dto.reminder.MonitoredPointDTO;
import org.sigmah.shared.dto.reminder.ReminderDTO;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;

/**
 * 
 * @author Denis Colliot (dcolliot@ideia.fr)
 * @author Rapha??l Calabro (rcalabro@ideia.fr)
 */
public class ProjectDashboardView extends ProjectDashboardPresenter.View {

    private final Authentication authentication;
    private final Dispatcher dispatcher;
    private final ToolBar toolBar;

    private TabPanel tabPanelPhases;
    private LayoutContainer panelProjectModel;
    private LayoutContainer panelSelectedPhase;

    private Button buttonSavePhase;
    private Button buttonActivatePhase;
    private Button buttonPhaseGuide;

    private ContentPanel panelFinancialProjects;
    private ContentPanel panelLocalProjects;
    private Grid<FlexibleElementDTO> gridRequiredElements;

    private FlexibleGrid<ProjectFundingDTO> financialGrid;
    private Button addFinancialProjectButton;
    private Button createFinancialProjectButton;
    private FlexibleGrid<ProjectFundingDTO> localGrid;
    private Button addLocalPartnerProjectButton;
    private Button createLocalPartnerProjectButton;

    private ContentPanel panelReminders;
    private Button addReminderButton;
    private Grid<ReminderDTO> remindersGrid;

    private ContentPanel panelMonitoredPoints;
    private Button addMonitoredPointButton;
    private Grid<MonitoredPointDTO> monitoredPointsGrid;

    private Image editIcon;

    private final ProjectPresenter projectPresenter;

    private ContentPanel requiredElementContentPanel;

    public ProjectDashboardView(Authentication authentication, Dispatcher dispatcher, ProjectPresenter projectPresenter) {

        this.authentication = authentication;
        this.dispatcher = dispatcher;
        this.projectPresenter = projectPresenter;

        final BorderLayout borderLayout = new BorderLayout();
        borderLayout.setContainerStyle("x-border-layout-ct main-background");
        setLayout(borderLayout);

        /* Center panel */
        ListStore<FlexibleElementDTO> storeRequiredElements = new ListStore<FlexibleElementDTO>();

        gridRequiredElements = new Grid<FlexibleElementDTO>(storeRequiredElements, getRequiredElementsColumModel());
        gridRequiredElements.setAutoExpandColumn("label");
        gridRequiredElements.getView().setForceFit(true);

        // Phases tab panel
        tabPanelPhases = new TabPanel();
        tabPanelPhases.setPlain(true);
        tabPanelPhases.setTabScroll(true);
        tabPanelPhases.setAnimScroll(true);

        // Toolbar
        toolBar = new ToolBar();
        toolBar.setAlignment(HorizontalAlignment.LEFT);
        toolBar.setBorders(false);

        buttonSavePhase = new Button(I18N.CONSTANTS.projectSavePhaseButton(), IconImageBundle.ICONS.save());
        buttonActivatePhase = new Button(I18N.CONSTANTS.projectClosePhaseButton(), IconImageBundle.ICONS.activate());
        buttonPhaseGuide = new Button(I18N.CONSTANTS.projectPhaseGuideHeader(), IconImageBundle.ICONS.info());

        buttonActivatePhase.setEnabled(false);
        buttonSavePhase.setEnabled(false);
        buttonPhaseGuide.setEnabled(false);

        toolBar.add(buttonActivatePhase);
        toolBar.add(new SeparatorToolItem());
        toolBar.add(buttonSavePhase);
        toolBar.add(new SeparatorToolItem());
        toolBar.add(buttonPhaseGuide);

        // Tab item main panel
        panelProjectModel = new LayoutContainer(new BorderLayout());
        panelProjectModel.setBorders(false);
        panelProjectModel.addStyleName("project-current-phase-panel");

        panelSelectedPhase = new LayoutContainer(new FitLayout());

        final BorderLayoutData wd = new BorderLayoutData(LayoutRegion.WEST, 250);
        wd.setMargins(new Margins(0, 4, 4, 4));

        requiredElementContentPanel = new ContentPanel(new FitLayout());

        requiredElementContentPanel.add(gridRequiredElements);
        panelProjectModel.add(requiredElementContentPanel, wd);

        final BorderLayoutData cd = new BorderLayoutData(LayoutRegion.CENTER);
        cd.setMargins(new Margins(0, 4, 4, 0));

        final ContentPanel cp2 = new ContentPanel(new FitLayout());
        cp2.setHeading(I18N.CONSTANTS.phaseDetails());
        cp2.setScrollMode(Scroll.AUTOY);

        cp2.setTopComponent(toolBar);
        cp2.add(panelSelectedPhase, new FitData(new Margins(4)));

        panelProjectModel.add(cp2, cd);

        panelReminders = getRemindersPanel();
        panelMonitoredPoints = getMonitoredPointsPanel();

        final VBoxLayout vLayout = new VBoxLayout();
        vLayout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
        final ContentPanel westPanel = new ContentPanel(vLayout);
        westPanel.setHeading(I18N.CONSTANTS.reminders());

        final VBoxLayoutData flex = new VBoxLayoutData(new Margins(0, 0, 5, 0));
        flex.setFlex(1);
        westPanel.add(panelReminders, flex);
        westPanel.add(panelMonitoredPoints, flex);

        /* South panel */

        buildFinancialProjectsPanel();
        buildLocalPartnerProjectsPanel();

        final HBoxLayout layout = new StylableHBoxLayout("join-background");
        layout.setHBoxLayoutAlign(HBoxLayoutAlign.STRETCH);
        final ContentPanel southPanel = new ContentPanel(layout);
        southPanel.setHeading(I18N.CONSTANTS.projectLinkedProjects());

        panelFinancialProjects.setWidth("50%");
        panelLocalProjects.setWidth("50%");
        southPanel.add(panelFinancialProjects, new HBoxLayoutData(0, 2, 0, 0));
        southPanel.add(panelLocalProjects, new HBoxLayoutData(0, 0, 0, 2));

        /* BorderLayoutData */
        final BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH, 140);
        southData.setCollapsible(true);
        southData.setMargins(new Margins(4, 0, 0, 0));

        final BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 250);
        westData.setMargins(new Margins(0, 4, 4, 0));
        westData.setCollapsible(true);

        final BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
        centerData.setMargins(new Margins(0, 0, 4, 4));

        add(westPanel, westData);
        add(tabPanelPhases, centerData);
        add(southPanel, southData);
    }

    /**
     * Generates the {@link ColumnModel} for the required elements grid.
     * 
     * @return the {@link ColumnModel} for the required elements grid.
     */
    private ColumnModel getRequiredElementsColumModel() {

        // Element's label.
        final ColumnConfig labelColumn = new ColumnConfig("label", I18N.CONSTANTS.projectRequiredElementsGridLabel(),
                150);

        // Element's completion.
        final CheckColumnConfig filledInColumn = new CheckColumnConfig("filledIn",
                I18N.CONSTANTS.projectRequiredElementsGridChecked(), 50);
        filledInColumn.setMenuDisabled(false);
        filledInColumn.setSortable(false);
        filledInColumn.setRenderer(new GridCellRenderer<FlexibleElementDTO>() {
            @Override
            public Object render(FlexibleElementDTO model, String property, ColumnData config, int rowIndex,
                    int colIndex, ListStore<FlexibleElementDTO> store, Grid<FlexibleElementDTO> grid) {
                if (model.isFilledIn()) {
                    return IconImageBundle.ICONS.elementCompleted().createImage();
                } else {
                    return IconImageBundle.ICONS.elementUncompleted().createImage();
                }
            }
        });

        // Element's type.
        final ColumnConfig typeColumn = new ColumnConfig("typeOfElement",
                I18N.CONSTANTS.projectRequiredElementsElementType(), 75);
        typeColumn.setRenderer(new GridCellRenderer<FlexibleElementDTO>() {
            @Override
            public Object render(FlexibleElementDTO model, String property, ColumnData config, int rowIndex,
                    int colIndex, ListStore<FlexibleElementDTO> store, Grid<FlexibleElementDTO> grid) {
                return FlexibleElementType.getFlexibleElementTypeName(model);
            }
        });

        return new ColumnModel(Arrays.asList(filledInColumn, labelColumn, typeColumn));
    }

    @Override
    public Button getButtonActivatePhase() {
        return buttonActivatePhase;
    }

    @Override
    public Button getButtonPhaseGuide() {
        return buttonPhaseGuide;
    }

    @Override
    public Button getButtonSavePhase() {
        return buttonSavePhase;
    }

    @Override
    public Grid<FlexibleElementDTO> getGridRequiredElements() {
        return gridRequiredElements;
    }

    @Override
    public ContentPanel getPanelFinancialProjects() {
        return panelFinancialProjects;
    }

    @Override
    public ContentPanel getPanelLocalProjects() {
        return panelLocalProjects;
    }

    @Override
    public LayoutContainer getPanelProjectModel() {
        return panelProjectModel;
    }

    @Override
    public ContentPanel getPanelReminders() {
        return panelReminders;
    }

    @Override
    public Button getAddReminderButton() {
        return addReminderButton;
    }

    @Override
    public Grid<ReminderDTO> getRemindersGrid() {
        return remindersGrid;
    }

    @Override
    public LayoutContainer getPanelSelectedPhase() {
        return panelSelectedPhase;
    }

    @Override
    public ContentPanel getPanelWatchedPoints() {
        return panelMonitoredPoints;
    }

    @Override
    public TabPanel getTabPanelPhases() {
        return tabPanelPhases;
    }

    @Override
    public TabPanel getTabPanelProject() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void flushToolbar() {
        toolBar.removeAll();
        toolBar.removeAllListeners();
    }

    @Override
    public void fillToolbar() {

        flushToolbar();

        if (ProfileUtils.isGranted(authentication, GlobalPermissionEnum.CHANGE_PHASE)) {
            toolBar.add(buttonActivatePhase);
            toolBar.add(new SeparatorToolItem());
        }
        toolBar.add(buttonSavePhase);
        toolBar.add(new SeparatorToolItem());
        toolBar.add(buttonPhaseGuide);
    }

    @Override
    public FlexibleGrid<ProjectFundingDTO> getFinancialProjectGrid() {
        return financialGrid;
    }

    @Override
    public Button getAddFinancialProjectButton() {
        return addFinancialProjectButton;
    }

    @Override
    public Button getCreateFinancialProjectButton() {
        return createFinancialProjectButton;
    }

    @Override
    public FlexibleGrid<ProjectFundingDTO> getLocalPartnerProjectGrid() {
        return localGrid;
    }

    @Override
    public Button getAddLocalPartnerProjectButton() {
        return addLocalPartnerProjectButton;
    }

    @Override
    public Button getCreateLocalPartnerProjectButton() {
        return createLocalPartnerProjectButton;
    }

    @Override
    public Grid<MonitoredPointDTO> getMonitoredPointsGrid() {
        return monitoredPointsGrid;
    }

    @Override
    public Button getAddMonitoredPointButton() {
        return addMonitoredPointButton;
    }

    @Override
    public Image getEditIcon() {
        return editIcon;
    }

    /**
     * Builds the grid to display financial projects.
     */
    private void buildFinancialProjectsPanel() {

        // The grid sorter.
        final StoreSorter<ProjectFundingDTO> storeSorter = new StoreSorter<ProjectFundingDTO>() {

            @Override
            public int compare(Store<ProjectFundingDTO> store, ProjectFundingDTO m1, ProjectFundingDTO m2,
                    String property) {

                if ("name".equals(property)) {
                    return m1.getFunding().getName().compareTo(m2.getFunding().getName());
                } else if ("fullName".equals(property)) {
                    return m1.getFunding().getFullName().compareTo(m2.getFunding().getFullName());
                } else {
                    return super.compare(store, m1, m2, property);
                }
            }
        };

        // Builds the grid.
        final ListStore<ProjectFundingDTO> financialStore = new ListStore<ProjectFundingDTO>();
        financialStore.setStoreSorter(storeSorter);
        financialGrid = new FlexibleGrid<ProjectFundingDTO>(financialStore, null, 2, getFinancialColumnModel());
        financialGrid.setAutoExpandColumn("name");

        // Builds the panel tool bar.
        addFinancialProjectButton = new Button(I18N.CONSTANTS.createProjectTypeFundingSelect(),
                IconImageBundle.ICONS.select());
        addFinancialProjectButton.setTitle(I18N.CONSTANTS.createProjectTypeFundingSelectDetails());

        createFinancialProjectButton = new Button(I18N.CONSTANTS.createProjectTypeFundingCreate(),
                IconImageBundle.ICONS.add());
        createFinancialProjectButton.setTitle(I18N.CONSTANTS.createProjectTypeFundingCreateDetails());

        final Label title = new Label(I18N.CONSTANTS.projectFinancialProjectsHeader());
        title.addStyleName("toolbar-title");

        final ToolBar toolbar = new ToolBar();
        toolbar.add(title);

        if (ProfileUtils.isGranted(authentication, GlobalPermissionEnum.EDIT_PROJECT)) {
            toolbar.add(new SeparatorToolItem());
            toolbar.add(addFinancialProjectButton);
        }

        if (ProfileUtils.isGranted(authentication, GlobalPermissionEnum.CREATE_PROJECT,
                GlobalPermissionEnum.EDIT_PROJECT)) {
            toolbar.add(new SeparatorToolItem());
            toolbar.add(createFinancialProjectButton);
        }

        // Builds the grid panel.
        panelFinancialProjects = new ContentPanel();
        panelFinancialProjects.setHeaderVisible(false);

        panelFinancialProjects.setTopComponent(toolbar);
        panelFinancialProjects.add(financialGrid);
    }

    /**
     * Builds the grid to display local partner projects.
     */
    private void buildLocalPartnerProjectsPanel() {

        // The grid sorter.
        final StoreSorter<ProjectFundingDTO> storeSorter = new StoreSorter<ProjectFundingDTO>() {

            @Override
            public int compare(Store<ProjectFundingDTO> store, ProjectFundingDTO m1, ProjectFundingDTO m2,
                    String property) {

                if ("name".equals(property)) {
                    return m1.getFunding().getName().compareTo(m2.getFunding().getName());
                } else if ("fullName".equals(property)) {
                    return m1.getFunding().getFullName().compareTo(m2.getFunding().getFullName());
                } else {
                    return super.compare(store, m1, m2, property);
                }
            }
        };

        // Builds the grid.
        final ListStore<ProjectFundingDTO> localStore = new ListStore<ProjectFundingDTO>();
        localStore.setStoreSorter(storeSorter);
        localGrid = new FlexibleGrid<ProjectFundingDTO>(new ListStore<ProjectFundingDTO>(), null, 2,
                getLocalPartnerColumnModel());
        localGrid.setAutoExpandColumn("name");

        // Builds the panel tool bar.
        addLocalPartnerProjectButton = new Button(I18N.CONSTANTS.createProjectTypePartnerSelect(),
                IconImageBundle.ICONS.select());
        addLocalPartnerProjectButton.setTitle(I18N.CONSTANTS.createProjectTypePartnerSelectDetails());

        createLocalPartnerProjectButton = new Button(I18N.CONSTANTS.createProjectTypePartnerCreate(),
                IconImageBundle.ICONS.add());
        createLocalPartnerProjectButton.setTitle(I18N.CONSTANTS.createProjectTypePartnerCreateDetails());

        final Label title = new Label(I18N.CONSTANTS.projectLocalPartnerProjectsHeader());
        title.addStyleName("toolbar-title");

        final ToolBar toolbar = new ToolBar();
        toolbar.add(title);

        if (ProfileUtils.isGranted(authentication, GlobalPermissionEnum.EDIT_PROJECT)) {
            toolbar.add(new SeparatorToolItem());
            toolbar.add(addLocalPartnerProjectButton);
        }

        if (ProfileUtils.isGranted(authentication, GlobalPermissionEnum.CREATE_PROJECT,
                GlobalPermissionEnum.EDIT_PROJECT)) {
            toolbar.add(new SeparatorToolItem());
            toolbar.add(createLocalPartnerProjectButton);
        }

        // Builds the grid panel.
        panelLocalProjects = new ContentPanel();
        panelLocalProjects.setHeaderVisible(false);

        panelLocalProjects.setTopComponent(toolbar);
        panelLocalProjects.add(localGrid);
    }

    /**
     * Gets the columns for the funding projects grid.
     * 
     * @return The columns for the funding projects grid.
     */
    private ColumnConfig[] getFinancialColumnModel() {

        // Icon.
        final ColumnConfig iconColumn = new ColumnConfig();
        iconColumn.setId("icon");
        iconColumn.setSortable(false);
        iconColumn.setWidth(15);
        iconColumn.setAlignment(HorizontalAlignment.CENTER);
        iconColumn.setRenderer(new GridCellRenderer<ProjectFundingDTO>() {

            @Override
            public Object render(ProjectFundingDTO model, String property, ColumnData config, int rowIndex,
                    int colIndex, ListStore<ProjectFundingDTO> store, Grid<ProjectFundingDTO> grid) {
                return FundingIconProvider.getProjectTypeIcon(
                        model.getFunding().getProjectModelType(authentication.getOrganizationId())).createImage();
            }
        });

        // Name.
        final ColumnConfig nameColumn = new ColumnConfig();
        nameColumn.setId("name");
        nameColumn.setHeader(I18N.CONSTANTS.projectName());
        nameColumn.setWidth(80);
        nameColumn.setRenderer(new GridCellRenderer<ProjectFundingDTO>() {

            @Override
            public Object render(ProjectFundingDTO model, String property, ColumnData config, int rowIndex,
                    int colIndex, ListStore<ProjectFundingDTO> store, Grid<ProjectFundingDTO> grid) {

                final Hyperlink nameHyperlink = new Hyperlink(model.getFunding().getName(), true,
                        ProjectPresenter.PAGE_ID.toString() + '!' + model.getFunding().getId());
                nameHyperlink.addStyleName("hyperlink");

                return nameHyperlink;
            }
        });

        // Full name.
        final ColumnConfig fullNameColumn = new ColumnConfig();
        fullNameColumn.setId("fullName");
        fullNameColumn.setHeader(I18N.CONSTANTS.projectFullName());
        fullNameColumn.setWidth(130);
        fullNameColumn.setRenderer(new GridCellRenderer<ProjectFundingDTO>() {

            @Override
            public Object render(ProjectFundingDTO model, String property, ColumnData config, int rowIndex,
                    int colIndex, ListStore<ProjectFundingDTO> store, Grid<ProjectFundingDTO> grid) {

                final Label fullNameLabel = new Label(model.getFunding().getFullName());

                return fullNameLabel;
            }
        });

        // Amount.
        final ColumnConfig amountColumn = new ColumnConfig();
        amountColumn.setId("percentage");
        amountColumn.setHeader(I18N.CONSTANTS.projectFinances() + " (" + I18N.CONSTANTS.currencyEuro() + ')');
        amountColumn.setWidth(120);

        // Percentage.
        final ColumnConfig percentageColumn = new ColumnConfig();
        percentageColumn.setId("percentage2");
        percentageColumn.setHeader(I18N.CONSTANTS.createProjectPercentage());
        percentageColumn.setWidth(60);
        percentageColumn.setRenderer(new GridCellRenderer<ProjectFundingDTO>() {

            @Override
            public Object render(ProjectFundingDTO model, String property, ColumnData config, int rowIndex,
                    int colIndex, ListStore<ProjectFundingDTO> store, Grid<ProjectFundingDTO> grid) {

                // The amount of the funding.
                final Double amount = model.getPercentage();

                // The current project budget.
                final Double budget = model.getFunded().getPlannedBudget();

                final Label percentageLabel = new Label(NumberUtils.ratioAsString(amount, budget));
                return percentageLabel;
            }
        });

        // Edit icon.
        final ColumnConfig editButtonColumn = new ColumnConfig();
        editButtonColumn.setId("editButton");
        editButtonColumn.setSortable(false);
        editButtonColumn.setWidth(30);
        editButtonColumn.setAlignment(HorizontalAlignment.LEFT);
        editButtonColumn.setRenderer(new FinancialProjectEditButtonGridCellRender(this, dispatcher, projectPresenter));

        return new ColumnConfig[] { iconColumn, nameColumn, fullNameColumn, amountColumn, percentageColumn,
                editButtonColumn };
    }

    /**
     * Gets the columns for the funded projects grid.
     * 
     * @return The columns for the funded projects grid.
     */
    private ColumnConfig[] getLocalPartnerColumnModel() {

        // Icon.
        final ColumnConfig iconColumn = new ColumnConfig();
        iconColumn.setId("icon");
        iconColumn.setSortable(false);
        iconColumn.setWidth(15);
        iconColumn.setAlignment(HorizontalAlignment.CENTER);
        iconColumn.setRenderer(new GridCellRenderer<ProjectFundingDTO>() {

            @Override
            public Object render(ProjectFundingDTO model, String property, ColumnData config, int rowIndex,
                    int colIndex, ListStore<ProjectFundingDTO> store, Grid<ProjectFundingDTO> grid) {
                return FundingIconProvider.getProjectTypeIcon(
                        model.getFunded().getProjectModelType(authentication.getOrganizationId())).createImage();
            }
        });

        // Name.
        final ColumnConfig nameColumn = new ColumnConfig();
        nameColumn.setId("name");
        nameColumn.setHeader(I18N.CONSTANTS.projectName());
        nameColumn.setWidth(80);
        nameColumn.setRenderer(new GridCellRenderer<ProjectFundingDTO>() {

            @Override
            public Object render(ProjectFundingDTO model, String property, ColumnData config, int rowIndex,
                    int colIndex, ListStore<ProjectFundingDTO> store, Grid<ProjectFundingDTO> grid) {

                final Hyperlink nameHyperlink = new Hyperlink(model.getFunded().getName(), true,
                        ProjectPresenter.PAGE_ID.toString() + '!' + model.getFunded().getId());
                nameHyperlink.addStyleName("hyperlink");

                return nameHyperlink;
            }
        });

        // Full name.
        final ColumnConfig fullNameColumn = new ColumnConfig();
        fullNameColumn.setId("fullName");
        fullNameColumn.setHeader(I18N.CONSTANTS.projectFullName());
        fullNameColumn.setWidth(130);
        fullNameColumn.setRenderer(new GridCellRenderer<ProjectFundingDTO>() {

            @Override
            public Object render(ProjectFundingDTO model, String property, ColumnData config, int rowIndex,
                    int colIndex, ListStore<ProjectFundingDTO> store, Grid<ProjectFundingDTO> grid) {

                final Label fullNameLabel = new Label(model.getFunded().getFullName());

                return fullNameLabel;
            }
        });

        // Amount.
        final ColumnConfig amountColumn = new ColumnConfig();
        amountColumn.setId("percentage");
        amountColumn.setHeader(I18N.CONSTANTS.projectFundedBy() + " (" + I18N.CONSTANTS.currencyEuro() + ')');
        amountColumn.setWidth(120);

        // Percentage.
        final ColumnConfig percentageColumn = new ColumnConfig();
        percentageColumn.setId("percentage2");
        percentageColumn.setHeader(I18N.CONSTANTS.createProjectPercentage());
        percentageColumn.setWidth(60);
        percentageColumn.setRenderer(new GridCellRenderer<ProjectFundingDTO>() {

            @Override
            public Object render(ProjectFundingDTO model, String property, ColumnData config, int rowIndex,
                    int colIndex, ListStore<ProjectFundingDTO> store, Grid<ProjectFundingDTO> grid) {

                // The amount of the funding.
                final Double amount = model.getPercentage();

                // The funded project budget.
                final Double budget = model.getFunded().getPlannedBudget();

                final Label percentageLabel = new Label(NumberUtils.ratioAsString(amount, budget));
                return percentageLabel;
            }
        });

        // Edit icon.
        final ColumnConfig editButtonColumn = new ColumnConfig();
        editButtonColumn.setId("editButton");
        editButtonColumn.setSortable(false);
        editButtonColumn.setWidth(30);
        editButtonColumn.setAlignment(HorizontalAlignment.LEFT);
        editButtonColumn
                .setRenderer(new LocalPartnerProjectEditButtonGridCellRender(this, dispatcher, projectPresenter));

        return new ColumnConfig[] { iconColumn, nameColumn, fullNameColumn, amountColumn, percentageColumn,
                editButtonColumn };
    }

    /**
     * Gets the panel which displays the reminders.
     * 
     * @return The panel which displays the reminders.
     */
    private ContentPanel getRemindersPanel() {

        // Store filters.

        final StoreFilter<ReminderDTO> notCompletedFilter = new StoreFilter<ReminderDTO>() {

            @Override
            public boolean select(Store<ReminderDTO> store, ReminderDTO parent, ReminderDTO item, String property) {
                return !item.isCompleted();
            }
        };

        final StoreFilter<ReminderDTO> completedFilter = new StoreFilter<ReminderDTO>() {

            @Override
            public boolean select(Store<ReminderDTO> store, ReminderDTO parent, ReminderDTO item, String property) {
                return item.isCompleted();
            }
        };

        final StoreFilter<ReminderDTO> exceededFilter = new StoreFilter<ReminderDTO>() {

            @Override
            public boolean select(Store<ReminderDTO> store, ReminderDTO parent, ReminderDTO item, String property) {
                return !item.isCompleted() && DateUtils.DAY_COMPARATOR.compare(new Date(), item.getExpectedDate()) > 0;
            }
        };

        // Store
        final ListStore<ReminderDTO> remindersStore = new ListStore<ReminderDTO>();

        // Grid.
        remindersGrid = new Grid<ReminderDTO>(remindersStore, new ColumnModel(Arrays.asList(getRemindersColumnModel())));
        remindersGrid.getView().setForceFit(true);
        remindersGrid.setBorders(false);

        if (ProfileUtils.isGranted(authentication, GlobalPermissionEnum.EDIT_PROJECT)) {
            remindersGrid.addPlugin((CheckColumnConfig) remindersGrid.getColumnModel().getColumn(0));
        }
        remindersGrid.setAutoExpandColumn("label");

        // Filter menu.

        final FilterSelectionListener<ReminderDTO> filterListener = new FilterSelectionListener<ReminderDTO>(
                remindersStore);

        final MenuItem noFilterItem = new MenuItem(I18N.CONSTANTS.monitoredPointAll());
        noFilterItem.addSelectionListener(new SelectionListener<MenuEvent>() {
            @Override
            public void componentSelected(MenuEvent ce) {
                filterListener.filter(noFilterItem, null);
            }
        });

        final MenuItem completedFilterItem = new MenuItem(I18N.CONSTANTS.monitoredPointCompleted(),
                IconImageBundle.ICONS.closedReminder());
        completedFilterItem.addSelectionListener(new SelectionListener<MenuEvent>() {
            @Override
            public void componentSelected(MenuEvent ce) {
                filterListener.filter(completedFilterItem, completedFilter);
            }
        });

        final MenuItem notCompletedFilterItem = new MenuItem(I18N.CONSTANTS.monitoredPointUncompleted(),
                IconImageBundle.ICONS.openedReminder());
        notCompletedFilterItem.addSelectionListener(new SelectionListener<MenuEvent>() {
            @Override
            public void componentSelected(MenuEvent ce) {
                filterListener.filter(notCompletedFilterItem, notCompletedFilter);
            }
        });

        final MenuItem exceededFilterItem = new MenuItem(I18N.CONSTANTS.monitoredPointExceeded(),
                IconImageBundle.ICONS.overdueReminder());
        exceededFilterItem.addSelectionListener(new SelectionListener<MenuEvent>() {
            @Override
            public void componentSelected(MenuEvent ce) {
                filterListener.filter(exceededFilterItem, exceededFilter);
            }
        });

        final Menu filterMenu = new Menu();
        filterMenu.add(noFilterItem);
        filterMenu.add(new SeparatorMenuItem());
        filterMenu.add(completedFilterItem);
        filterMenu.add(notCompletedFilterItem);
        filterMenu.add(exceededFilterItem);

        // Fires manually the first filter (no filter).
        filterListener.filter(noFilterItem, null);

        // Filter button.
        final Button filterButton = new Button(I18N.CONSTANTS.filter(), IconImageBundle.ICONS.filter());
        filterButton.setMenu(filterMenu);

        addReminderButton = new Button(I18N.CONSTANTS.addItem(), IconImageBundle.ICONS.add());

        // Toolbar.
        final ToolBar toolbar = new ToolBar();
        toolbar.setAlignment(HorizontalAlignment.LEFT);

        if (ProfileUtils.isGranted(authentication, GlobalPermissionEnum.EDIT_PROJECT)) {
            toolbar.add(addReminderButton);
        }
        toolbar.add(filterButton);

        // Panel.
        final ContentPanel panel = new ContentPanel(new FitLayout());
        panel.setHeading(I18N.CONSTANTS.reminderPoints());
        panel.setBorders(false);

        panel.setTopComponent(toolbar);
        panel.add(remindersGrid);

        return panel;
    }

    /**
     * Gets the panel which displays the monitored points.
     * 
     * @return The panel which displays the monitored points.
     */
    private ContentPanel getMonitoredPointsPanel() {

        // Store filters.

        final StoreFilter<MonitoredPointDTO> notCompletedFilter = new StoreFilter<MonitoredPointDTO>() {

            @Override
            public boolean select(Store<MonitoredPointDTO> store, MonitoredPointDTO parent, MonitoredPointDTO item,
                    String property) {
                return !item.isCompleted();
            }
        };

        final StoreFilter<MonitoredPointDTO> completedFilter = new StoreFilter<MonitoredPointDTO>() {

            @Override
            public boolean select(Store<MonitoredPointDTO> store, MonitoredPointDTO parent, MonitoredPointDTO item,
                    String property) {
                return item.isCompleted();
            }
        };

        final StoreFilter<MonitoredPointDTO> exceededFilter = new StoreFilter<MonitoredPointDTO>() {

            @Override
            public boolean select(Store<MonitoredPointDTO> store, MonitoredPointDTO parent, MonitoredPointDTO item,
                    String property) {
                return !item.isCompleted() && DateUtils.DAY_COMPARATOR.compare(new Date(), item.getExpectedDate()) > 0;
            }
        };

        // Store
        final ListStore<MonitoredPointDTO> monitoredPointsStore = new ListStore<MonitoredPointDTO>();

        // Grid.
        monitoredPointsGrid = new Grid<MonitoredPointDTO>(monitoredPointsStore, new ColumnModel(
                Arrays.asList(getMonitoredPointsColumnModel())));
        monitoredPointsGrid.getView().setForceFit(true);
        monitoredPointsGrid.setBorders(false);

        if (ProfileUtils.isGranted(authentication, GlobalPermissionEnum.EDIT_PROJECT)) {
            monitoredPointsGrid.addPlugin((CheckColumnConfig) monitoredPointsGrid.getColumnModel().getColumn(0));
        }
        monitoredPointsGrid.setAutoExpandColumn("label");

        // Filter menu.

        final FilterSelectionListener<MonitoredPointDTO> filterListener = new FilterSelectionListener<MonitoredPointDTO>(
                monitoredPointsStore);

        final MenuItem noFilterItem = new MenuItem(I18N.CONSTANTS.monitoredPointAll());
        noFilterItem.addSelectionListener(new SelectionListener<MenuEvent>() {
            @Override
            public void componentSelected(MenuEvent ce) {
                filterListener.filter(noFilterItem, null);
            }
        });

        final MenuItem completedFilterItem = new MenuItem(I18N.CONSTANTS.monitoredPointCompleted(),
                IconImageBundle.ICONS.closedPoint());
        completedFilterItem.addSelectionListener(new SelectionListener<MenuEvent>() {
            @Override
            public void componentSelected(MenuEvent ce) {
                filterListener.filter(completedFilterItem, completedFilter);
            }
        });

        final MenuItem notCompletedFilterItem = new MenuItem(I18N.CONSTANTS.monitoredPointUncompleted(),
                IconImageBundle.ICONS.openedPoint());
        notCompletedFilterItem.addSelectionListener(new SelectionListener<MenuEvent>() {
            @Override
            public void componentSelected(MenuEvent ce) {
                filterListener.filter(notCompletedFilterItem, notCompletedFilter);
            }
        });

        final MenuItem exceededFilterItem = new MenuItem(I18N.CONSTANTS.monitoredPointExceeded(),
                IconImageBundle.ICONS.overduePoint());
        exceededFilterItem.addSelectionListener(new SelectionListener<MenuEvent>() {
            @Override
            public void componentSelected(MenuEvent ce) {
                filterListener.filter(exceededFilterItem, exceededFilter);
            }
        });

        final Menu filterMenu = new Menu();
        filterMenu.add(noFilterItem);
        filterMenu.add(new SeparatorMenuItem());
        filterMenu.add(completedFilterItem);
        filterMenu.add(notCompletedFilterItem);
        filterMenu.add(exceededFilterItem);

        // Fires manually the first filter (no filter).
        filterListener.filter(noFilterItem, null);

        // Filter button.
        final Button filterButton = new Button(I18N.CONSTANTS.filter(), IconImageBundle.ICONS.filter());
        filterButton.setMenu(filterMenu);

        addMonitoredPointButton = new Button(I18N.CONSTANTS.addItem(), IconImageBundle.ICONS.add());

        // Toolbar.
        final ToolBar toolbar = new ToolBar();
        toolbar.setAlignment(HorizontalAlignment.LEFT);

        if (ProfileUtils.isGranted(authentication, GlobalPermissionEnum.EDIT_PROJECT)) {
            toolbar.add(addMonitoredPointButton);
        }
        toolbar.add(filterButton);

        // Panel.
        final ContentPanel panel = new ContentPanel(new FitLayout());
        panel.setHeading(I18N.CONSTANTS.monitoredPoints());
        panel.setBorders(false);

        panel.setTopComponent(toolbar);
        panel.add(monitoredPointsGrid);

        return panel;
    }

    /**
     * Define a listener to apply a one filter at a time and manage the menu
     * item state.
     * 
     * @author tmi
     * 
     * @param <E>
     */
    private static class FilterSelectionListener<E extends ModelData> {

        private MenuItem currentItem;
        private StoreFilter<E> currentFilter;
        private final Store<E> store;

        private FilterSelectionListener(Store<E> store) {
            this.store = store;
        }

        public void filter(MenuItem item, StoreFilter<E> filter) {
            activate();
            currentItem = item;
            filter(filter);
            desactivate();
        }

        private void activate() {
            if (currentItem != null) {
                currentItem.setEnabled(true);
            }
        }

        private void desactivate() {
            if (currentItem != null) {
                currentItem.setEnabled(false);
            }
        }

        private void filter(StoreFilter<E> filter) {

            if (store == null) {
                return;
            }

            store.removeFilter(currentFilter);

            if (filter != null) {
                store.addFilter(filter);
            }

            store.applyFilters(null);
            currentFilter = filter;
        }
    }

    /**
     * Gets the columns for the monitored points grid.
     * 
     * @return The columns for the monitored points grid.
     */
    private ColumnConfig[] getMonitoredPointsColumnModel() {

        final DateTimeFormat format = DateUtils.DATE_SHORT;
        final Date now = new Date();

        // Completed ?
        final CheckColumnConfig completedColumn = new CheckColumnConfig();
        completedColumn.setId("completed");
        completedColumn.setHeader(I18N.CONSTANTS.monitoredPointClose() + "?");
        completedColumn.setWidth(20);
        completedColumn.setSortable(false);
        final CellEditor checkBoxEditor = new CellEditor(new CheckBox());
        completedColumn.setEditor(checkBoxEditor);

        // Icon
        final ColumnConfig iconColumn = new ColumnConfig();
        iconColumn.setId("icon");
        iconColumn.setHeader("");
        iconColumn.setWidth(16);
        iconColumn.setRenderer(new GridCellRenderer<MonitoredPointDTO>() {

            @Override
            public Object render(MonitoredPointDTO model, String property, ColumnData config, int rowIndex,
                    int colIndex, ListStore<MonitoredPointDTO> store, Grid<MonitoredPointDTO> grid) {

                if (model.isCompleted()) {
                    return IconImageBundle.ICONS.closedPoint().createImage();
                } else if (DateUtils.DAY_COMPARATOR.compare(now, model.getExpectedDate()) > 0) {
                    return IconImageBundle.ICONS.overduePoint().createImage();
                } else {
                    return IconImageBundle.ICONS.openedPoint().createImage();
                }
            }
        });

        // Label.
        final ColumnConfig labelColumn = new ColumnConfig();
        labelColumn.setId("label");
        labelColumn.setHeader(I18N.CONSTANTS.monitoredPointLabel());
        labelColumn.setWidth(60);
        if (ProfileUtils.isGranted(authentication, GlobalPermissionEnum.EDIT_PROJECT)) {
            labelColumn.setRenderer(new MonitoredPointLabelCellRender(this, this.dispatcher));
        }

        // Expected date.
        final ColumnConfig expectedDateColumn = new ColumnConfig();
        expectedDateColumn.setId("expectedDate");
        expectedDateColumn.setHeader(I18N.CONSTANTS.monitoredPointExpectedDate());
        expectedDateColumn.setWidth(60);
        expectedDateColumn.setDateTimeFormat(format);
        expectedDateColumn.setRenderer(new GridCellRenderer<MonitoredPointDTO>() {

            @Override
            public Object render(MonitoredPointDTO model, String property, ColumnData config, int rowIndex,
                    int colIndex, ListStore<MonitoredPointDTO> store, Grid<MonitoredPointDTO> grid) {

                final Label l = new Label(format.format(model.getExpectedDate()));
                if (!model.isCompleted() && DateUtils.DAY_COMPARATOR.compare(now, model.getExpectedDate()) > 0) {
                    l.addStyleName("points-date-exceeded");
                }
                return l;
            }
        });

        // Completion date.
        final ColumnConfig completionDateColumn = new ColumnConfig();
        completionDateColumn.setId("completionDate");
        completionDateColumn.setHeader(I18N.CONSTANTS.monitoredPointCompletionDate());
        completionDateColumn.setWidth(60);
        completionDateColumn.setDateTimeFormat(format);

        return new ColumnConfig[] { completedColumn, iconColumn, labelColumn, expectedDateColumn, completionDateColumn };
    }

    /**
     * Gets the columns for the reminders grid.
     * 
     * @return The columns for the reminders grid.
     */
    private ColumnConfig[] getRemindersColumnModel() {

        final DateTimeFormat format = DateUtils.DATE_SHORT;
        final Date now = new Date();

        // Completed ?
        final CheckColumnConfig completedColumn = new CheckColumnConfig();
        completedColumn.setId("completed");
        completedColumn.setHeader(I18N.CONSTANTS.monitoredPointClose() + "?");
        completedColumn.setWidth(20);
        completedColumn.setSortable(false);
        final CellEditor checkBoxEditor = new CellEditor(new CheckBox());
        completedColumn.setEditor(checkBoxEditor);

        // Icon
        final ColumnConfig iconColumn = new ColumnConfig();
        iconColumn.setId("icon");
        iconColumn.setHeader("");
        iconColumn.setWidth(16);
        iconColumn.setRenderer(new GridCellRenderer<ReminderDTO>() {

            @Override
            public Object render(ReminderDTO model, String property, ColumnData config, int rowIndex, int colIndex,
                    ListStore<ReminderDTO> store, Grid<ReminderDTO> grid) {

                if (model.isCompleted()) {
                    return IconImageBundle.ICONS.closedReminder().createImage();
                } else if (DateUtils.DAY_COMPARATOR.compare(now, model.getExpectedDate()) > 0) {
                    return IconImageBundle.ICONS.overdueReminder().createImage();
                } else {
                    return IconImageBundle.ICONS.openedReminder().createImage();
                }
            }
        });

        // Label.
        final ColumnConfig labelColumn = new ColumnConfig();
        labelColumn.setId("label");
        labelColumn.setHeader(I18N.CONSTANTS.monitoredPointLabel());
        labelColumn.setWidth(60);
        if (ProfileUtils.isGranted(authentication, GlobalPermissionEnum.EDIT_PROJECT)) {
            labelColumn.setRenderer(new ReminderLableCellRenderer(this, this.dispatcher));
        }

        // Expected date.
        final ColumnConfig expectedDateColumn = new ColumnConfig();
        expectedDateColumn.setId("expectedDate");
        expectedDateColumn.setHeader(I18N.CONSTANTS.monitoredPointExpectedDate());
        expectedDateColumn.setWidth(60);
        expectedDateColumn.setDateTimeFormat(format);
        expectedDateColumn.setRenderer(new GridCellRenderer<ReminderDTO>() {

            @Override
            public Object render(ReminderDTO model, String property, ColumnData config, int rowIndex, int colIndex,
                    ListStore<ReminderDTO> store, Grid<ReminderDTO> grid) {

                final Label l = new Label(format.format(model.getExpectedDate()));
                if (!model.isCompleted() && DateUtils.DAY_COMPARATOR.compare(now, model.getExpectedDate()) > 0) {
                    l.addStyleName("points-date-exceeded");
                }
                return l;
            }
        });

        // Completion date.
        final ColumnConfig completionDateColumn = new ColumnConfig();
        completionDateColumn.setId("completionDate");
        completionDateColumn.setHeader(I18N.CONSTANTS.monitoredPointCompletionDate());
        completionDateColumn.setWidth(60);
        completionDateColumn.setDateTimeFormat(format);

        return new ColumnConfig[] { completedColumn, iconColumn, labelColumn, expectedDateColumn, completionDateColumn };
    }

    @Override
    public ContentPanel getRequiredElementContentPanel() {

        return requiredElementContentPanel;
    }

}

