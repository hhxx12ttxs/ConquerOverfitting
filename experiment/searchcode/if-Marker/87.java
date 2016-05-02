package com.kivancmuslu.www.solstice.pmd.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.Nullable;

import com.kivancmuslu.www.nullness.PrimitiveUtil;
import com.kivancmuslu.www.nullness.StringUtil;
import com.kivancmuslu.www.solstice.logic.messages.api.AnalysisMessage;
import com.kivancmuslu.www.solstice.logic.messages.api.ProcessBasedAnalysisSucceedMessage;
import com.kivancmuslu.www.solstice.pmd.common.SharedOperations;
import com.kivancmuslu.www.solstice.server.api.AnalysisPreferences;
import com.kivancmuslu.www.solstice.server.api.AnalysisServer;
import com.kivancmuslu.www.solstice.server.api.MarkerWrapper;
import com.kivancmuslu.www.util.eclipse.nullness.MarkerUtil;
import com.kivancmuslu.www.util.eclipse.nullness.PathUtil;
import com.kivancmuslu.www.util.eclipse.nullness.ResourceUtil;

@AnalysisPreferences(preferencePageID = ContinuousPMDPreferencePage.PREFERENCE_ID,
                     storedStringPreferenceIDs = {SharedOperations.PMD_INSTALLATION_DIR,
                                                  SharedOperations.COMMA_SEPARATED_RULESETS})//
public class ContinuousPMDServer extends AnalysisServer
{
    public ContinuousPMDServer()
    {
        super(true);
    }

    private static String processAnalysisCompleted(ProcessBasedAnalysisSucceedMessage message)
    {
        String stdOut = message.getStdOut();
        String stdErr = message.getStdErr();
        StringBuilder explanation = new StringBuilder();

        if (stdOut.equals("") && stdErr.equals(""))
            explanation.append("No warnings.");
        else
        {
            explanation.append("PMD standard output:");
            explanation.append(LS);
            if (stdOut.equals(""))
                stdOut = "NO OUTPUT.";
            explanation.append(stdOut);

            explanation.append(LS);

            explanation.append("PMD standard error:");
            explanation.append(LS);
            if (stdErr.equals(""))
                stdErr = "NO ERROR.";
            explanation.append(stdErr);
        }
        return StringUtil.toString(explanation);
    }

    @Override
    protected String convertAnalysisMessageToContent(AnalysisMessage message)
    {
        if (message instanceof ProcessBasedAnalysisSucceedMessage)
            return processAnalysisCompleted((ProcessBasedAnalysisSucceedMessage) message);
        return super.convertAnalysisMessageToContent(message);
    }

    @Override
    protected @Nullable Object readIncomingMessage(ObjectInputStream ois)
        throws ClassNotFoundException, IOException
    {
        return ois.readObject();
    }

    @Override
    protected List<MarkerWrapper> convertAnalysisMessageToMarkers(AnalysisMessage message)
    {
        if (message instanceof ProcessBasedAnalysisSucceedMessage)
            return generateMarkersFromMessage((ProcessBasedAnalysisSucceedMessage) message);
        return super.convertAnalysisMessageToMarkers(message);
    }

    @Override
    protected String getMarkerType()
    {
        return SharedOperations.ANALYSIS_MARKER_ID;
    }

    private List<MarkerWrapper> generateMarkersFromMessage(ProcessBasedAnalysisSucceedMessage message)
    {
        List<MarkerWrapper> markers = new ArrayList<>();
        IProject project;
        try
        {
            project = ResourceUtil.getProject(message.getResource().deserialize());
        }
        catch (CoreException | IOException e)
        {
            logWarning("Cannot deserialized project while generating markers. " + e);
            return markers;
        }

        for (String line: StringUtil.split(message.getStdOut(), LS))
        {
            MarkerWrapper marker = convertLineToMarker(line, project);
            if (marker != null)
                markers.add(marker);
        }
        return markers;
    }

    private @Nullable MarkerWrapper convertLineToMarker(String lineOut, IProject project)
    {
        // Initialize marker attributes
        IPath testLocation = new Path("");
        int line = -1;
        String message = "";

        // Parse the lineOut for filePath, markerLine, and markerMessage
        // e.g., \test\basic\Foo.java:11: This for loop could be simplified to a while loop
        try
        {
            int filePathLastIndex = lineOut.indexOf(':');
            testLocation = PathUtil.append(testLocation,
                                           StringUtil.substring(lineOut, 0, filePathLastIndex));
            int lineLastIndex = lineOut.indexOf(':', filePathLastIndex + 1);
            line = Integer.parseInt(lineOut.substring(filePathLastIndex + 1, lineLastIndex));
            message = StringUtil.trim(StringUtil.substring(lineOut, lineLastIndex + 1));
        }
        catch (IndexOutOfBoundsException e)
        {
            logWarning("Cannot parse information on line: " + lineOut, e);
            return null;
        }
        catch (NumberFormatException e)
        {
            logWarning("Cannot parse marker line information on line: " + lineOut, e);
            return null;
        }

        IFile file = ResourceUtil.getFile(project, testLocation);
        if (!file.exists())
        {
            logWarning(file.getName() + " does not exist. Cannot create markers.");
            return null;
        }

        // Set marker attributes
        HashMap<String, Object> markerMap = new HashMap<>();
        markerMap.put(MarkerUtil.SEVERITY,
                      PrimitiveUtil.valueOf(SharedOperations.ANALYSIS_MARKER_SEVERITY));
        markerMap.put(MarkerUtil.PRIORITY,
                      PrimitiveUtil.valueOf(SharedOperations.ANALYSIS_MARKER_PRIORITY));
        markerMap.put(MarkerUtil.LINE_NUMBER, PrimitiveUtil.valueOf(line));
        markerMap.put(MarkerUtil.MESSAGE, message);
        MarkerWrapper marker = MarkerWrapper.wrap(file, markerMap, getMarkerType());
        return marker;
    }
}

