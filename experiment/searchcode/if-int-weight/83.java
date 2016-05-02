package pgu.client.contacts.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import pgu.client.app.utils.JsonHelper;
import pgu.client.contacts.ContactsActivity;
import pgu.client.contacts.ContactsView;
import pgu.client.resources.ResourcesApp;
import pgu.client.resources.ResourcesApp.CssResourceApp;
import pgu.shared.utils.ChartType;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.Popover;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;

public class ContactsViewImpl extends Composite implements ContactsView {

    private static ContactsViewImplUiBinder uiBinder = GWT.create(ContactsViewImplUiBinder.class);

    interface ContactsViewImplUiBinder extends UiBinder<Widget, ContactsViewImpl> {
    }

    @UiField
    CheckBox worldBtn, americasBtn, europeBtn, asiaBtn, oceaniaBtn, africaBtn;
    @UiField
    CheckBox barChartBtn, pieChartBtn;
    @UiField
    HTMLPanel worldMap, americasMap, europeMap, asiaMap, oceaniaMap, africaMap;
    @UiField
    HTMLPanel pieChart, barChart;
    @UiField
    HTMLPanel loadingPanel, chartsPanel;
    @UiField
    TextBox fusionBox;
    @UiField
    Button addFusionBtn;
    @UiField
    HTMLPanel fusionPanel, contactsNamesPanel;
    @UiField
    Button closeContactsNamesPanel;
    @UiField
    HTML contactsNamesHtml;
    @UiField
    Popover infoPop, fusionInfoPop;
    @UiField
    Button infoPopBtn, fusionInfoPopBtn;
    @UiField
    HTML titleContactsCount;

    private final CssResourceApp css;

    private final HashMap<String, HTMLPanel> type2chart = new HashMap<String, HTMLPanel>();
    private final HashMap<String, CheckBox> type2chartBox = new HashMap<String, CheckBox>();

    private final ArrayList<String> fusionUrls = new ArrayList<String>();

    private final JsonHelper             json = new JsonHelper();

    private ContactsActivity presenter;

    private int totalNbOfContacts;

    public ContactsViewImpl(final EventBus eventBus) {
        initWidget(uiBinder.createAndBindUi(this));

        exportMethods();

        css = ResourcesApp.INSTANCE.css();

        pieChart.getElement().setId("pgu_geo_contacts_piechart");
        barChart.getElement().setId("pgu_geo_contacts_barchart");

        worldMap.getElement().setId("pgu_geo_contacts_map_world");
        americasMap.getElement().setId("pgu_geo_contacts_map_americas");
        europeMap.getElement().setId("pgu_geo_contacts_map_europe");
        asiaMap.getElement().setId("pgu_geo_contacts_map_asia");
        oceaniaMap.getElement().setId("pgu_geo_contacts_map_oceania");
        africaMap.getElement().setId("pgu_geo_contacts_map_africa");

        contactsNamesPanel.getElement().setId("pgu_geo_contacts_names_panel");

        type2chart.put(ChartType.AFRICA, africaMap);
        type2chart.put(ChartType.AMERICAS, americasMap);
        type2chart.put(ChartType.ASIA, asiaMap);
        type2chart.put(ChartType.BAR, barChart);
        type2chart.put(ChartType.EUROPE, europeMap);
        type2chart.put(ChartType.OCEANIA, oceaniaMap);
        type2chart.put(ChartType.PIE, pieChart);
        type2chart.put(ChartType.WORLD, worldMap);

        type2chartBox.put(ChartType.AFRICA, africaBtn);
        type2chartBox.put(ChartType.AMERICAS, americasBtn);
        type2chartBox.put(ChartType.ASIA, asiaBtn);
        type2chartBox.put(ChartType.BAR, barChartBtn);
        type2chartBox.put(ChartType.EUROPE, europeBtn);
        type2chartBox.put(ChartType.OCEANIA, oceaniaBtn);
        type2chartBox.put(ChartType.PIE, pieChartBtn);
        type2chartBox.put(ChartType.WORLD, worldBtn);

        //        showLoadingPanel();
        hideAllCharts();

        updateTitleContactsCount(0);

        infoPop.setHeading("Charts");
        infoPop.setText("<p>Clicking on the regions of the geocharts will display your contacts' names.</p>" + //
                "<br/>" + //
                "<p>Only the <b>first 500</b> contacts have been fetched.</p>" + //
                "<br/>" + //
                "<p>Also this information is only available here and <b>not</b> on your public profile.</p>" //
                );

        fusionInfoPop.setHeading("Fusion tables");
        fusionInfoPop.setText("<p>How to create a fusion tables?</p>" + //
                "<p>See this <a href=\"http://support.google.com/fusiontables/answer/184641/\" target=\"blank\">video tutorial</a> from google.</p>" + //
                "<br/>" + //
                "<p>You'll need this <a href=\"javascript:;\" onclick=\"pgu_geo.download_contacts_csv();false;\" >csv file</a>.</p>");

    }

    public native void exportMethods() /*-{
        $wnd.pgu_geo.download_contacts_csv = $entry(@pgu.client.contacts.ui.ContactsViewImpl::downloadContactsCsv());
    }-*/;

    public static void downloadContactsCsv() {

        final StringBuilder csv = new StringBuilder();
        csv.append("Country,Contacts number\r\n");

        for (final Entry<String, Integer> e : country2contactNumber.entrySet()) {
            csv.append(e.getKey());
            csv.append(",");
            csv.append(e.getValue());
            csv.append("\r\n");
        }

        downloadContactsCsvInternal(csv.toString());
    }

    public static native void downloadContactsCsvInternal(String csv) /*-{

        if ($wnd.navigator.appName != 'Microsoft Internet Explorer') {
            $wnd.open('data:text/csv;charset=utf-8,' + $wnd.encodeURI(csv), 'contacts.csv');

        } else {
            var popup = $wnd.open('','csv','');
            popup.document.body.innerHTML = '<pre>' + csv + '</pre>';
        }

    }-*/;

    @UiHandler("infoPopBtn")
    public void clickInfoPop(final ClickEvent e) {
        infoPop.toggle();
    }

    @UiHandler("fusionInfoPopBtn")
    public void clickFusionInfoPop(final ClickEvent e) {
        fusionInfoPop.toggle();
    }

    public void openAndShowContactNames(final String regionCode) {
        final String contactsNames = country2contactNames.get(regionCode);

        if (contactsNames != null) {
            contactsNamesHtml.setHTML(contactsNames);
            showContactsNamesPanel();

        } else {
            hideContactsNamesPanel();
            contactsNamesHtml.setHTML("");

        }
    }

    @UiHandler("closeContactsNamesPanel")
    public void clickCloseContactsNamesPanel(final ClickEvent e) {
        hideContactsNamesPanel();
        contactsNamesHtml.setHTML("");
    }

    private native void showContactsNamesPanel() /*-{
        $wnd.$('#pgu_geo_contacts_names_panel').collapse('show');
    }-*/;

    private native void hideContactsNamesPanel() /*-{
        $wnd.$('#pgu_geo_contacts_names_panel').collapse('hide');
    }-*/;

    @UiHandler("fusionBox")
    public void keydownOnAddFusionTable(final KeyDownEvent event) {

        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {

            event.preventDefault();
            event.stopPropagation();

            addFusionTable();
        }
    }

    @UiHandler("addFusionBtn")
    public void clickAddFusion(final ClickEvent e) {
        addFusionTable();
    }

    //        https://www.google.com/fusiontables/embedviz?viz=MAP&q=select+col0%3E%3E1+from+1dlLBDtnjrqYG7W3eamJX3-1ogV8XdGHXkX2wOaU&h=false&lat=47.74359286233701&lng=6.064453125&z=3&t=1&l=col0%3E%3E1
    //        https://www.google.com/fusiontables/embedviz?viz=GVIZ&t=BAR&containerId=gviz_canvas&q=select+col0%3E%3E0%2C+col1%3E%3E0+from+1dlLBDtnjrqYG7W3eamJX3-1ogV8XdGHXkX2wOaU&qrs=+where+col0%3E%3E0+%3E%3D+&qre=+and+col0%3E%3E0+%3C%3D+&qe=+limit+13&width=500&height=300
    private void addFusionTable() {
        final String url = fusionBox.getText();

        if (!url.startsWith("https://www.google.com/fusiontables/embedviz")) {
            return;
        }

        addFusionPanel(url);
        saveFusionUrls();
    }

    private void addFusionPanel(final String url) {

        if (fusionUrls.contains(url)) {
            return;
        }

        fusionUrls.add(url);

        final Frame frame = new Frame(url);
        frame.addStyleName(css.chartWell());

        final Button closeBtn = new Button();
        closeBtn.setText("x");
        closeBtn.addStyleName("close");
        closeBtn.getElement().getStyle().setMarginTop(11, Unit.PX);

        final HorizontalPanel hp = new HorizontalPanel();
        hp.getElement().getStyle().setFloat(Style.Float.LEFT);
        hp.add(frame);
        hp.add(closeBtn);

        closeBtn.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(final ClickEvent event) {
                hp.removeFromParent();
                fusionUrls.remove(url);
                saveFusionUrls();
            }
        });

        fusionPanel.add(hp);
    }

    private void saveFusionUrls() {

        final JsArrayString jsFusionUrls = JavaScriptObject.createArray().cast();

        for (final String fusionUrl : fusionUrls) {
            jsFusionUrls.push(fusionUrl);
        }

        final String jsonFusionUrls = json.stringify(jsFusionUrls);
        presenter.saveFusionUrls(jsonFusionUrls);
    }

    @UiHandler("worldBtn")
    public void clickWorld(final ClickEvent e) {
        worldMap.setVisible(worldBtn.getValue());
        saveChartsPreferences();
    }

    @UiHandler("americasBtn")
    public void clickAmericas(final ClickEvent e) {
        americasMap.setVisible(americasBtn.getValue());
        saveChartsPreferences();
    }

    @UiHandler("europeBtn")
    public void clickEurope(final ClickEvent e) {
        europeMap.setVisible(europeBtn.getValue());
        saveChartsPreferences();
    }

    @UiHandler("asiaBtn")
    public void clickAsia(final ClickEvent e) {
        asiaMap.setVisible(asiaBtn.getValue());
        saveChartsPreferences();
    }

    @UiHandler("oceaniaBtn")
    public void clickOceania(final ClickEvent e) {
        oceaniaMap.setVisible(oceaniaBtn.getValue());
        saveChartsPreferences();
    }

    @UiHandler("africaBtn")
    public void clickAfrica(final ClickEvent e) {
        africaMap.setVisible(africaBtn.getValue());
        saveChartsPreferences();
    }

    @UiHandler("pieChartBtn")
    public void clickPieChart(final ClickEvent e) {
        pieChart.setVisible(pieChartBtn.getValue());
        saveChartsPreferences();
    }

    @UiHandler("barChartBtn")
    public void clickBarChart(final ClickEvent e) {
        barChart.setVisible(barChartBtn.getValue());
        saveChartsPreferences();
    }

    private static final HashMap<String, Integer> country2contactNumber = new HashMap<String, Integer>();
    private final HashMap<String, String> country2locationNames = new HashMap<String, String>();

    private native void parseFusionUrls(final String json) /*-{
        // [url1, url2]

        if (!json) {
            return;
        }

        var fusion_urls = JSON.parse(json);
        for ( var i = 0, len = fusion_urls.length; i < len; i++) {

            var fusion_url = fusion_urls[i];
            this.@pgu.client.contacts.ui.ContactsViewImpl::addFusionPanel(Ljava/lang/String;)
                 (fusion_url);
        }

    }-*/;

    private void hideAllCharts() {
        for (final HTMLPanel chart : type2chart.values()) {
            chart.setVisible(false);
        }

        for (final CheckBox checkBox : type2chartBox.values()) {
            checkBox.setValue(false);
        }
    }

    private native void parseChartsPreferences(final String json) /*-{
        // ['world','americas']
        var chart_types = [];
        var hasToSaveConfig = false;

        if (!json) {

            chart_types.push('world');
            chart_types.push('pie');

            hasToSaveConfig = true;

        } else {
            chart_types = chart_types.concat(JSON.parse(json));
        }

        for ( var i = 0, len = chart_types.length; i < len; i++) {
            var chart_type = chart_types[i];
            this.@pgu.client.contacts.ui.ContactsViewImpl::displayChartType(Ljava/lang/String;)
                 (chart_type);
        }

        if (hasToSaveConfig) {
            this.@pgu.client.contacts.ui.ContactsViewImpl::saveChartsPreferences()
                 ();
        }

    }-*/;

    private void displayChartType(final String chartType) {
        type2chart.get(chartType).setVisible(true);
        type2chartBox.get(chartType).setValue(true);
    }

    private void saveChartsPreferences() {

        final JsArrayString chartTypes = JavaScriptObject.createArray().cast();
        for (final Entry<String, HTMLPanel> e : type2chart.entrySet()) {

            final String chartType = e.getKey();
            final HTMLPanel chart = e.getValue();

            if (chart.isVisible()) {
                chartTypes.push(chartType);
            }
        }

        final String jsonChartTypes = json.stringify(chartTypes);
        presenter.saveChartsPreferences(jsonChartTypes);
    }

    public void addLocationsNames(final String country, final String htmlLocations) {
        country2locationNames.put(country, htmlLocations);
    }

    public void addContactsNumber(final String country, final int number) {
        country2contactNumber.put(country, number);
    }

    private void buildCharts(final TreeMap<Integer, ArrayList<String>> weight2codes) {

        initDataTable();
        for (final Entry<Integer, ArrayList<String>> e : weight2codes.entrySet()) {
            final Integer weight = e.getKey();
            final ArrayList<String> countryCodes = e.getValue();

            for (final String countryCode : countryCodes) {
                addDataRow(countryCode, weight);
            }
        }
        buildChartsUI();

        final String jsonContactsNumberByCountry = json.stringify(getContactsTable());
        presenter.saveContactsNumberByCountry(jsonContactsNumberByCountry);
    }

    private native JavaScriptObject getContactsTable() /*-{
        return $wnd.pgu_geo.contacts_table;
    }-*/;

    private native void buildChartsUI() /*-{
        $wnd.console.log('build geo chart');

        var
            google = $wnd.google
          , data = $wnd.pgu_geo.contacts_table
          , data_table = google.visualization.arrayToDataTable(data)
        ;

        //
        // geo charts
        // see options @ https://google-developers.appspot.com/chart/interactive/docs/gallery/geochart
        var maps = [
            {id:'pgu_geo_contacts_map_world', options: {height:347, width: 556}} // default width: 556px
           ,{id:'pgu_geo_contacts_map_americas', options: {region:'019', height:347, width: 556}}
           ,{id:'pgu_geo_contacts_map_europe', options: {region:150, height:347, width: 556}}
           ,{id:'pgu_geo_contacts_map_asia', options: {region:142, height:347, width: 556}}
           ,{id:'pgu_geo_contacts_map_oceania', options: {region:'009', height:347, width: 556}}
           ,{id:'pgu_geo_contacts_map_africa', options: {region:'002', height:347, width: 556}}
        ];

        var that = this;

        var click_region_handler = function(e) {
            that.@pgu.client.contacts.ui.ContactsViewImpl::openAndShowContactNames(Ljava/lang/String;)(e.region.toLowerCase());
        };

        for (var i = 0, len = maps.length; i < len; i++) {
            var map = maps[i];

            var geo_chart = new google.visualization.GeoChart($doc.getElementById(map.id));
            geo_chart.draw(data_table, map.options);

            // https://developers.google.com/chart/interactive/docs/events?hl=en
            google.visualization.events.addListener(geo_chart, 'regionClick', click_region_handler);
        }

        //
        // charts
        var click_chart_handler = function(the_chart) {

            var selection = the_chart.getSelection();
            if (selection.length != 1) {
                $wnd.console.log('!! different of one ');
                $wnd.console.log(selection);
                // TODO PGU hide the contacts area
                return;
            }

            var
                item = selection[0]
              , region = data_table.getFormattedValue(item.row, 0) // item.column
            ;

            that.@pgu.client.contacts.ui.ContactsViewImpl::openAndShowContactNames(Ljava/lang/String;)(region.toLowerCase());
        };

        //
        // pie chart
        var
            pie_options = {
                title: $wnd.pgu_geo.contacts._total + ' Contacts by countries'
                , is3D: true
                , width : 556
                , height : 347
            }
          , pie_chart = new google.visualization.PieChart($doc.getElementById('pgu_geo_contacts_piechart'))
        ;

        pie_chart.draw(data_table, pie_options);

        var click_pie_handler = function() {
            return click_chart_handler(pie_chart);
        }

        google.visualization.events.addListener(pie_chart, 'select', click_pie_handler);

        //
        // bar chart
        var
            bar_options = {
                  title: $wnd.pgu_geo.contacts._total + ' Contacts by countries'
                , vAxis: {title: 'Country'}
                , width : 556
                , height : 347
            }
          , bar_chart = new google.visualization.BarChart($doc.getElementById('pgu_geo_contacts_barchart'))
        ;

        bar_chart.draw(data_table, bar_options);

        var click_bar_handler = function() {
            return click_chart_handler(bar_chart);
        }

        google.visualization.events.addListener(bar_chart, 'select', click_bar_handler);

    }-*/;

    private native void addDataRow(final String countryCode, final int weight) /*-{
        $wnd.console.log('addDataRow ' + countryCode + ', ' + weight);

        $wnd.pgu_geo.contacts_table.push([countryCode, weight]);
    }-*/;

    private native void initDataTable() /*-{
        $wnd.console.log('initDataTable ');

        $wnd.pgu_geo.contacts_table = [];
        $wnd.pgu_geo.contacts_table.push(['Country', 'Contacts']);

    }-*/;

    private void logResult(final HashMap<String, Integer> code2weight) {
        final StringBuilder sb = new StringBuilder();
        for (final Entry<String, Integer> e : code2weight.entrySet()) {
            sb.append(e.getKey());
            sb.append(": ");
            sb.append(e.getValue());
            sb.append(",");
        }
        GWT.log(sb.toString());
    }

    private void showLoadingPanel() {
        chartsPanel.setVisible(false);
        loadingPanel.setVisible(true);
    }

    @Override
    public void showChartsPanel() {
        loadingPanel.setVisible(false);
        chartsPanel.setVisible(true);
    }

    private final HashMap<String, String> country2contactNames = new HashMap<String, String>();

    public void addContactsNames(final String country, final String htmlNames) {
        country2contactNames.put(country, htmlNames);
    }

    @Override
    public void setPresenter(final ContactsActivity presenter) {
        this.presenter = presenter;
    }

    private boolean areContactsSetInView = false;

    @Override
    public boolean areContactsSetInView() {
        return areContactsSetInView;
    }

    private String stringify(final JavaScriptObject jso) {
        return json.stringify(jso);
    }

    @Override
    public native String getJsonRawContacts() /*-{
        var contacts = $wnd.pgu_geo.contacts;

        return this.@pgu.client.contacts.ui.ContactsViewImpl::stringify(Lcom/google/gwt/core/client/JavaScriptObject;)
                    (contacts);
    }-*/;

    @Override
    public void showContacts() {

        if (areContactsSetInView) {
            return;
        }

        areContactsSetInView = true;

        presenter.showWaitingIndicator();
        showLoadingPanel();

        // TODO PGU Jan 28, 2013
        // TODO PGU Jan 28, 2013
        // TODO PGU Jan 28, 2013
        // TODO PGU Jan 28, 2013
        country2contactNumber.clear();
        country2locationNames.clear();
        country2contactNames.clear();

        // TODO PGU Nov 6, 2012 use the country's label: country2locationNames
        parseContacts();
        prepareDataForCharts();
        showChartsPanel();

        presenter.hideWaitingIndicator();

        presenter.fetchChartsPreferences();
        presenter.fetchFusionUrls();
    }

    private void updateContactsCount(final int contactsCount) {
        totalNbOfContacts = contactsCount;
        updateTitleContactsCount(contactsCount);
    }

    private void updateTitleContactsCount(final int contactsCount) {
        titleContactsCount.setHTML("<h2>" + contactsCount + " Contacts</h2>");
    }

    private native void parseContacts() /*-{

        var contacts_obj = $wnd.pgu_geo.contacts || {"_total":0};
        var contacts = contacts_obj.values;

        var country2location_names = {};
        var country2contact_count = {};
        var country2contact_names = {};

        this.@pgu.client.contacts.ui.ContactsViewImpl::updateContactsCount(I)
             (contacts_obj._total);

        for (var i = 0; i < contacts_obj._total; i++) {

            var contact = contacts[i];

            var location = contact.location || {};
            var location_name = location.name || "";

            var country = location.country || {};
            var country_code = country.code || "";

            if ("" === country_code) {
                continue;
            }

            if (country2location_names.hasOwnProperty(country_code)) {
                var location_names = country2location_names[country_code];

                if (location_names.indexOf(location_name) === -1) {
                    location_names.push(location_name);
                }

            } else {
                country2location_names[country_code] = [].concat(location_name);
            }

            if (country2contact_count.hasOwnProperty(country_code)) {
                country2contact_count[country_code] = country2contact_count[country_code] + 1;

            } else {
                country2contact_count[country_code] = 1;
            }

            var contact_name = contact.lastName + ' ' + contact.firstName;
            if (country2contact_names.hasOwnProperty(country_code)) {
                country2contact_names[country_code].push(contact_name);

            } else {
                country2contact_names[country_code] = [].concat(contact_name);
            }
        }

        // fill up the maps
        for (var country in country2location_names) {
            if ('__gwt_ObjectId' === country) {
                continue;
            }
            if (country2location_names.hasOwnProperty(country)) {
                var location_names = country2location_names[country];
                location_names.sort();

                var fmt_location_names = location_names.join(', ');

                this.@pgu.client.contacts.ui.ContactsViewImpl::addLocationsNames(Ljava/lang/String;Ljava/lang/String;)
                     (country, fmt_location_names);
            }
        }

        for (var country in country2contact_names) {
            if ('__gwt_ObjectId' === country) {
                continue;
            }
            if (country2contact_names.hasOwnProperty(country)) {
                var contact_names = country2contact_names[country];
                contact_names.sort();

                var fmt_contact_names = contact_names.join(', ');
                this.@pgu.client.contacts.ui.ContactsViewImpl::addContactsNames(Ljava/lang/String;Ljava/lang/String;)
                     (country, fmt_contact_names);
            }
        }

        for (var country in country2contact_count) {
            if ('__gwt_ObjectId' === country) {
                continue;
            }

            if (country2contact_count.hasOwnProperty(country)) {
                var contact_count = country2contact_count[country];

                this.@pgu.client.contacts.ui.ContactsViewImpl::addContactsNumber(Ljava/lang/String;I)
                     (country, contact_count);
            }
        }

        console.log(country2location_names);
        console.log(country2contact_count);
        console.log(country2contact_names);

    }-*/;

    private void prepareDataForCharts() {

        final TreeMap<Integer, ArrayList<String>> weight2codes = new TreeMap<Integer, ArrayList<String>>();
        for (final Entry<String, Integer> e : country2contactNumber.entrySet()) {

            final String countryCode = e.getKey();
            final Integer weight = e.getValue();

            if (weight2codes.containsKey(weight)) {
                weight2codes.get(weight).add(countryCode);

            } else {
                final ArrayList<String> countryCodes = new ArrayList<String>();
                countryCodes.add(countryCode);
                weight2codes.put(weight, countryCodes);
            }
        }

        for (final ArrayList<String> countryCodes : weight2codes.values()) {
            Collections.sort(countryCodes);
        }

        buildCharts(weight2codes);
    }

    @Override
    public void onFetchChartsPreferencesSuccess(final String jsonChartsPreferences) {
        hideAllCharts(); // prepare the charts visibility
        parseChartsPreferences(jsonChartsPreferences);
    }

    @Override
    public void onFetchFusionUrlsSuccess(final String jsonFusionUrls) {
        fusionPanel.clear();
        fusionUrls.clear();
        parseFusionUrls(jsonFusionUrls);
    }

    @Override
    public int getTotalNbOfContacts() {
        return totalNbOfContacts;
    }

}

