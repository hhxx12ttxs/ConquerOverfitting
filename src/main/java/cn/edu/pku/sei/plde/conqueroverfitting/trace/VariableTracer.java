package cn.edu.pku.sei.plde.conqueroverfitting.trace;

import com.sun.jdi.*;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import org.apache.commons.lang3.StringUtils;
import org.junit.runner.JUnitCore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yanrunfa on 16/2/19.
 */

public class VariableTracer {
    private VirtualMachine vm;
    public Process process;
    private EventRequestManager eventRequestManager;
    public EventQueue eventQueue;
    private EventSet eventSet;
    private boolean vmExit = false;

    public final String _classpath;
    public final String _testclasspath;
    public String _testclassname;
    public String _varname;


    public VariableTracer(String classpath, String testclasspath){
        _classpath = classpath;
        _testclasspath = testclasspath;
    }

    /**
     *
     * @param testclassname the test class which to be traced
     * @param varname the name of variable which to be traced
     * @return the trace list
     */
    public List<String> trace(String testclassname, String varname){
        _testclassname = testclassname;
        _varname = varname;
        String junitPath = JUnitCore.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        String[] mainArgs = {"org.junit.runner.JUnitCore", _testclassname};
        String[] optionArgs = {"-cp","\""+_classpath+System.getProperty("path.separator")+_testclasspath+System.getProperty("path.separator")+junitPath+"\""};


        LaunchingConnector launchingConnector = Bootstrap.virtualMachineManager().defaultConnector();
        Map<String, Connector.Argument> defaultArguments = launchingConnector.defaultArguments();
        defaultArguments.get("main").setValue(StringUtils.join(mainArgs," "));
        defaultArguments.get("options").setValue(StringUtils.join(optionArgs," "));
        defaultArguments.get("suspend").setValue("true");
        try {
            vm = launchingConnector.launch(defaultArguments);
        } catch (Exception e){
            e.printStackTrace();
        }

        process = vm.process();

        eventRequestManager = vm.eventRequestManager();
        ClassPrepareRequest classPrepareRequest = eventRequestManager.createClassPrepareRequest();
        classPrepareRequest.addClassFilter(_testclassname);
        classPrepareRequest.addCountFilter(1);
        classPrepareRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        classPrepareRequest.enable();

        // Enter event loop
        try {
            eventLoop();
        } catch (Exception e){
            e.printStackTrace();
        }

        process.destroy();
        return new ArrayList<String>();
    };

    private void eventLoop() throws Exception {
        eventQueue = vm.eventQueue();
        while (true) {
            if (vmExit) {
                break;
            }
            eventSet = eventQueue.remove();
            EventIterator eventIterator = eventSet.eventIterator();
            while (eventIterator.hasNext()) {
                Event event = (Event) eventIterator.next();
                execute(event);
            }
        }
    }

    private void execute(Event event) throws Exception {
        if (event instanceof VMStartEvent) {
            System.out.println("VM started");
            eventSet.resume();
        } else if (event instanceof ClassPrepareEvent) {
            ClassPrepareEvent classPrepareEvent = (ClassPrepareEvent) event;
            String mainClassName = classPrepareEvent.referenceType().name();
            if (mainClassName.equals(_testclassname)) {
                System.out.println("Class " + mainClassName
                        + " is already prepared");
            }
            if (true) {
                // Get location
                ReferenceType referenceType = classPrepareEvent.referenceType();
                List locations = referenceType.locationsOfLine(10);
                Location location = (Location) locations.get(0);

                // Create BreakpointEvent
                BreakpointRequest breakpointRequest = eventRequestManager
                        .createBreakpointRequest(location);
                breakpointRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
                breakpointRequest.enable();
            }
            eventSet.resume();
        } else if (event instanceof BreakpointEvent) {
            System.out.println("Reach line 10 of com.ibm.jdi.test.HelloWorld");
            BreakpointEvent breakpointEvent = (BreakpointEvent) event;
            ThreadReference threadReference = breakpointEvent.thread();
            StackFrame stackFrame = threadReference.frame(0);
            LocalVariable localVariable = stackFrame
                    .visibleVariableByName(_varname);
            Value value = stackFrame.getValue(localVariable);
            String str = ((StringReference) value).value();
            System.out.println("The local variable str at line 10 is " + str
                    + " of " + value.type().name());
            eventSet.resume();
        } else if (event instanceof VMDisconnectEvent) {
            vmExit = true;
        } else {
            eventSet.resume();
        }
    }

}
