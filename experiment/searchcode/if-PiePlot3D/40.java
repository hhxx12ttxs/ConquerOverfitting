/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zcu.kiv.eegdatabase.logic.controller.history;

import cz.zcu.kiv.eegdatabase.data.dao.AuthorizationManager;
import cz.zcu.kiv.eegdatabase.data.dao.PersonDao;
import cz.zcu.kiv.eegdatabase.data.dao.SimpleHistoryDao;
import cz.zcu.kiv.eegdatabase.data.pojo.History;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.util.Rotation;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Creates pie graph TOP download files by graph type(DAILY, WEEKLY, MONTHLY)
 *
 * @author pbruha
 */
public class GraphController extends AbstractController {

    public GraphController() {
    }

    private Log log = LogFactory.getLog(getClass());
    private SimpleHistoryDao<History, Integer> historyDao;
    private AuthorizationManager auth;
    private PersonDao personDao;

    protected ModelAndView handleRequestInternal(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        log.debug("Processing creating pie graph");
        String graphType = "";

        List<DownloadStatistic> topDownloadedFilesList = null;
        boolean isGroupAdmin;
        long countFile = 0;

        graphType = request.getParameter("graphType");
        int groupId = Integer.parseInt(request.getParameter("groupId"));
        isGroupAdmin = auth.userIsGroupAdmin();
        response.setContentType("image/png");

        topDownloadedFilesList = historyDao.getTopDownloadHistory(ChoiceHistory.valueOf(graphType), isGroupAdmin, groupId);


        DefaultPieDataset dataset = new DefaultPieDataset();
        if (groupId != -1) {
            for (int i = 0; i < topDownloadedFilesList.size(); i++) {
                dataset.setValue(topDownloadedFilesList.get(i).getFileName(), new Long(topDownloadedFilesList.get(i).getCount()));
                countFile = countFile + topDownloadedFilesList.get(i).getCount();
            }

            if (historyDao.getCountOfFilesHistory(ChoiceHistory.valueOf(graphType), isGroupAdmin, groupId) > countFile) {
                dataset.setValue("Other", historyDao.getCountOfFilesHistory(ChoiceHistory.valueOf(graphType), isGroupAdmin, groupId) - countFile);
            }
        }
        JFreeChart chart = ChartFactory.createPieChart3D(
                "Daily downloads", // chart title
                dataset, // data
                true, // include legend
                true,
                false);

        PiePlot3D plot = (PiePlot3D) chart.getPlot();
        plot.setStartAngle(290);
        plot.setDirection(Rotation.CLOCKWISE);
        plot.setForegroundAlpha(0.5f);
        plot.setNoDataMessage("No data to display");

        ChartUtilities.writeChartAsPNG(response.getOutputStream(), chart, 600, 400);
        response.getOutputStream().close();
        return null;
    }

    public SimpleHistoryDao<History, Integer> getHistoryDao() {
        return historyDao;
    }

    public void setHistoryDao(SimpleHistoryDao<History, Integer> historyDao) {
        this.historyDao = historyDao;
    }

    public AuthorizationManager getAuth() {
        return auth;
    }

    public void setAuth(AuthorizationManager auth) {
        this.auth = auth;
    }

    public PersonDao getPersonDao() {
        return personDao;
    }

    public void setPersonDao(PersonDao personDao) {
        this.personDao = personDao;
    }
}

