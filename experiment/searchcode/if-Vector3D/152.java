package plugins.adufour.activemeshes;

import icy.file.xls.XlsManager;
import icy.image.IcyBufferedImage;
import icy.image.colormap.FireColorMap;
import icy.main.Icy;
import icy.painter.Painter;
import icy.sequence.Sequence;
import icy.swimmingPool.SwimmingObject;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;
import icy.type.DataType;
import icy.type.value.DoubleValue;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import javax.swing.JOptionPane;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3d;

import plugins.adufour.activemeshes.energy.ChanVeseMumfordShahTerm;
import plugins.adufour.activemeshes.energy.CouplingTerm;
import plugins.adufour.activemeshes.energy.CurvatureRegularizationTerm;
import plugins.adufour.activemeshes.energy.GradientTerm;
import plugins.adufour.activemeshes.energy.Model;
import plugins.adufour.activemeshes.mesh.Mesh;
import plugins.adufour.activemeshes.mesh.MeshException;
import plugins.adufour.activemeshes.mesh.MeshSplittingException;
import plugins.adufour.activemeshes.mesh.Vertex;
import plugins.adufour.activemeshes.painters.VTKMeshPainter;
import plugins.adufour.activemeshes.producers.Icosahedron;
import plugins.adufour.activemeshes.producers.MarchingTetrahedra;
import plugins.adufour.activemeshes.util.SlidingWindowConvergence;
import plugins.adufour.activemeshes.util.SlidingWindowConvergence.Operation;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.connectedcomponents.ConnectedComponent;
import plugins.adufour.connectedcomponents.ConnectedComponentDescriptor;
import plugins.adufour.connectedcomponents.ConnectedComponents;
import plugins.adufour.ezplug.EzButton;
import plugins.adufour.ezplug.EzException;
import plugins.adufour.ezplug.EzGroup;
import plugins.adufour.ezplug.EzPlug;
import plugins.adufour.ezplug.EzStoppable;
import plugins.adufour.ezplug.EzVar;
import plugins.adufour.ezplug.EzVarBoolean;
import plugins.adufour.ezplug.EzVarDouble;
import plugins.adufour.ezplug.EzVarFile;
import plugins.adufour.ezplug.EzVarInteger;
import plugins.adufour.ezplug.EzVarListener;
import plugins.adufour.ezplug.EzVarSequence;
import plugins.adufour.filtering.ConvolutionException;
import plugins.adufour.hierarchicalkmeans.HKMeans;
import plugins.adufour.vars.lang.Var;
import plugins.adufour.vars.lang.VarInteger;
import plugins.adufour.vars.lang.VarSequence;
import plugins.fab.trackmanager.TrackGroup;
import plugins.fab.trackmanager.TrackManager;
import plugins.fab.trackmanager.TrackPool;
import plugins.fab.trackmanager.TrackSegment;
import plugins.nchenouard.spot.Detection;

public class ActiveMeshes extends EzPlug implements EzStoppable, Block
{
    private final EzVarSequence           input                    = new EzVarSequence("Input");
    
    private final EzVarSequence           seq_init                 = new EzVarSequence("Binary mask");
    
    private final EzVarBoolean            init_balls               = new EzVarBoolean("Replace meshes by balls", false);
    private final EzVarDouble             init_ballsSize           = new EzVarDouble("Ball size", 2, 2, 10, 1);
    
    private final EzVarBoolean            regul_flag               = new EzVarBoolean("Enable", true);
    private final EzVarBoolean            regul_resIndep           = new EzVarBoolean("Resolution independance", false);
    private final EzVarDouble             regul_weight             = new EzVarDouble("Regul. weight", 0.01, 0, 10, 0.01);
    
    private final EzVarBoolean            cvms_flag                = new EzVarBoolean("Enable", false);
    private final EzVarBoolean            cvms_stabilizeData       = new EzVarBoolean("Stabilize intensity", false);
    private final EzVarBoolean            cvms_local               = new EzVarBoolean("Localize means", true);
    private final EzVarDouble             cvms_weight              = new EzVarDouble("Region weight", 1, 0, 10, 0.1);
    private final EzVarDouble             cvms_sens                = new EzVarDouble("Sensitivity", 1.0, 1, 20, 0.1);
    
    private final EzVarBoolean            grad_flag                = new EzVarBoolean("Enable", false);
    private final EzVarSequence           grad_data                = new EzVarSequence("Gradient data");
    private final EzVarDouble             grad_weight              = new EzVarDouble("Gradient weight", 0.5, -10, 10, 0.1);
    
    private final EzVarInteger            mesh_resolution          = new EzVarInteger("Resolution", 10, 1, 100, 1);
    private final EzVarBoolean            reSample_flag            = new EzVarBoolean("Re-Sample", true);
    private final EzVarDouble             timeStep                 = new EzVarDouble("Time step", 0.01, 0, 1, 0.01);
    
    private final EzVarDouble             conv_epsilon             = new EzVarDouble("Epsilon", 0.001, 0.00001, 1, 0.00001);
    private final EzVarInteger            conv_winSize             = new EzVarInteger("Window size", 100, 10, 10000, 10);
    
    private final EzVarBoolean            useVTK                   = new EzVarBoolean("Use VTK", true);
    private final EzVarInteger            refreshRate              = new EzVarInteger("Refresh rate (it.)", 10, 1, 100, 1);
    
    private final EzVarBoolean            exportXLS_flag           = new EzVarBoolean("Export meshes to Excel", false);
    private final EzVarFile               exportXLS_file           = new EzVarFile("Excel output folder", null);
    
    private final EzVarBoolean            exportSwimmingPool       = new EzVarBoolean("Export to swimming pool", false);
    
    private final VarSequence             outputSequence           = new VarSequence("Labeled output", null);
    
    private final Var<TrackGroup>         outputTracks             = new Var<TrackGroup>("Tracks", TrackGroup.class, null);
    
    private final EzVarBoolean            tracking_flag            = new EzVarBoolean("Enable", false);
    private final EzVarBoolean            tracking_watchIncoming   = new EzVarBoolean("Watch incoming objects", false);
    private final EzVarInteger            tracking_incomingMinSize = new EzVarInteger("min. object size (voxels)", 0, Integer.MAX_VALUE, 1);
    private final EzVarInteger            tracking_incomingMaxSize = new EzVarInteger("max. object size (voxels)", 1000000, 1, Integer.MAX_VALUE, 1);
    
    private EzButton                      buttonNextFrame;
    private boolean                       stopAndMoveToNextFrame;
    
    private CurvatureRegularizationTerm   regul;
    
    private ChanVeseMumfordShahTerm       cvms;
    
    private GradientTerm                  grad;
    
    private CouplingTerm                  coupling;
    
    private final VTKMeshPainter          vtkPainter               = new VTKMeshPainter();
    
    private boolean                       stop;
    
    private final Runnable                repainter                = new Runnable()
                                                                   {
                                                                       @Override
                                                                       public void run()
                                                                       {
                                                                           input.getValue().painterChanged(vtkPainter);
                                                                       }
                                                                   };
    
    private final TrackPool               trackPool                = new TrackPool();
    private TrackGroup                    trackGroup;
    
    private ExecutorService               mainService;
    private ExecutorService               meshService;
    
    private final ArrayList<Future<Mesh>> meshTasks                = new ArrayList<Future<Mesh>>();
    
    @Override
    public void initialize()
    {
        addEzComponent(new EzGroup("Input", input, seq_init, init_balls, init_ballsSize));
        input.setToolTipText("The 3D volume or 3D+time sequence to segment");
        seq_init.setToolTipText("A binary volume used to initialize the meshes on the first frame");
        init_balls.addVisibilityTriggerTo(init_ballsSize, true);
        init_balls.setToolTipText("Objects detected in the binary volume will be replaced by unit balls instead of being triangulated");
        init_ballsSize.setToolTipText("Ball radius unit is the mesh resolution");
        
        addEzComponent(new EzGroup("Regularization", regul_flag, regul_weight, regul_resIndep));
        regul_flag.addVisibilityTriggerTo(regul_weight, true);
        regul_flag.addVisibilityTriggerTo(regul_resIndep, true);
        
        addEzComponent(new EzGroup("Region-based information", cvms_flag, cvms_weight, cvms_local, cvms_stabilizeData)); // cvms_sens
        cvms_local.setToolTipText("Check to enable local means re-computation (more robust against non-constant backgrounds)");
        cvms_stabilizeData.setToolTipText("Check to compute the 99.999% min/max intensity values (avoids spurious intensity peaks)");
        cvms_flag.addVisibilityTriggerTo(cvms_local, true);
        cvms_flag.addVisibilityTriggerTo(cvms_weight, true);
        //cvms_flag.addVisibilityTriggerTo(cvms_sens, true);
        
        addEzComponent(new EzGroup("Gradient-based information", grad_flag, grad_weight, grad_data));
        grad_flag.addVisibilityTriggerTo(grad_data, true);
        grad_flag.addVisibilityTriggerTo(grad_weight, true);
        
        input.addVarChangeListener(new EzVarListener<Sequence>()
        {
            @Override
            public void variableChanged(EzVar<Sequence> source, Sequence newValue)
            {
                grad_data.setValue(newValue);
            }
        });
        
        addEzComponent(new EzGroup("Mesh properties", mesh_resolution, reSample_flag));
        
        addEzComponent(new EzGroup("Evolution", timeStep, conv_winSize, conv_epsilon, useVTK, refreshRate));
        
        useVTK.addVisibilityTriggerTo(refreshRate, true);
        
        addEzComponent(new EzGroup("Export", exportXLS_flag, exportXLS_file, exportSwimmingPool));
        exportXLS_flag.addVisibilityTriggerTo(exportXLS_file, true);
        
        buttonNextFrame = new EzButton("Next frame", new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                stopAndMoveToNextFrame = true;
            }
        });
        addEzComponent(new EzGroup("Tracking", tracking_flag, tracking_watchIncoming, tracking_incomingMinSize, tracking_incomingMaxSize, buttonNextFrame));
        
        tracking_flag.addVisibilityTriggerTo(tracking_watchIncoming, true);
        tracking_watchIncoming.addVisibilityTriggerTo(tracking_incomingMinSize, true);
        tracking_watchIncoming.addVisibilityTriggerTo(tracking_incomingMaxSize, true);
        
        setTimeDisplay(true);
    }
    
    @Override
    public void execute()
    {
        Sequence inputSequence = input.getValue(true);
        Sequence initSequence = seq_init.getValue(true);
        
        if (inputSequence == initSequence && !isHeadLess())
        {
            int answer = JOptionPane.showConfirmDialog(getUI().getFrame(), "Input and initialization masks are identical. Proceed ?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.NO_OPTION) return;
        }
        
        mainService = Executors.newFixedThreadPool(SystemUtil.getAvailableProcessors());
        meshService = Executors.newFixedThreadPool(SystemUtil.getAvailableProcessors());
        
        trackGroup = new TrackGroup(inputSequence);
        trackGroup.setDescription("Active Meshes");
        
        // add the VTK painter if not present
        if (!input.getValue(true).getPainters().contains(vtkPainter)) input.getValue().addPainter(vtkPainter);
        
        vtkPainter.reset();
        
        int startT = inputSequence.getFirstViewer() == null ? 0 : inputSequence.getFirstViewer().getT();
        
        int endT = tracking_flag.getValue() ? inputSequence.getSizeT() : startT + 1;
        
        Sequence currentTimePoint = new Sequence();
        currentTimePoint.setPixelSizeX(inputSequence.getPixelSizeX());
        currentTimePoint.setPixelSizeY(inputSequence.getPixelSizeY());
        currentTimePoint.setPixelSizeZ(inputSequence.getPixelSizeZ());
        
        for (int t = startT; t < endT; t++)
        {
            if (inputSequence.getFirstViewer() != null) inputSequence.getFirstViewer().setT(t);
            
            stopAndMoveToNextFrame = false;
            stop = false;
            
            for (int z = 0; z < inputSequence.getSizeZ(); z++)
                currentTimePoint.setImage(0, z, inputSequence.getImage(t, z, 0));
            
            ArrayList<Mesh> meshes = new ArrayList<Mesh>();
            
            if (t == startT)
            {
                if (regul_flag.getValue()) regul = new CurvatureRegularizationTerm(meshService, regul_weight.getVariable(), regul_resIndep.getValue());
                
                if (cvms_flag.getValue()) cvms = new ChanVeseMumfordShahTerm(meshService, cvms_weight.getVariable());//, cvms_sens.getVariable());
                
                if (grad_flag.getValue()) grad = new GradientTerm(meshService, grad_weight.getVariable());
                
                coupling = new CouplingTerm(meshService);
                
                initializeMeshes(t, initSequence, meshes, useVTK.getValue());
                
                if (stop)
                {
                    mainService.shutdown();
                    return;
                }
            }
            else
            {
                // copy meshes from the previous frame
                for (TrackSegment segment : trackGroup.getTrackSegmentList())
                {
                    Detection previousDetection = segment.getDetectionAtTime(t - 1);
                    
                    if (previousDetection == null) continue;
                    
                    Mesh previousMesh = (Mesh) previousDetection;
                    
                    Mesh newMesh = previousMesh.copyContour();
                    
                    // restore the old detection coordinates in voxels
                    previousDetection.setX(previousDetection.getX() / inputSequence.getPixelSizeX());
                    previousDetection.setY(previousDetection.getY() / inputSequence.getPixelSizeY());
                    previousDetection.setZ(previousDetection.getZ() / inputSequence.getPixelSizeZ());
                    
                    newMesh.setT(t);
                    segment.addDetection(newMesh);
                    meshes.add(newMesh);
                    vtkPainter.addMesh(newMesh);
                }
                
            }
            
            if (cvms_flag.getValue()) cvms.setImageData(currentTimePoint, 0, 0, false, cvms_stabilizeData.getValue());
            if (grad_flag.getValue()) grad.setImageData(grad_data.getValue(), t, 0, false, false);
            
            try
            {
                ArrayList<Mesh> allMeshes = new ArrayList<Mesh>(meshes);
                
                deformMeshes(meshes);
                
                // find new objects
                if (tracking_flag.getValue() && tracking_watchIncoming.getValue())
                {
                    System.out.print("looking for new objects...");
                    
                    // compute the minimum and maximum object sizes
                    int minSize = tracking_incomingMinSize.getValue();
                    int maxSize = tracking_incomingMaxSize.getValue();
                    
                    int nbNewObjects = findEnteringObjects(allMeshes, currentTimePoint, minSize, maxSize, (short) 5);
                    
                    System.out.println("done (found " + nbNewObjects + ")");
                    
                    deformMeshes(allMeshes);
                }
            }
            catch (Exception e)
            {
                stopExecution();
                e.printStackTrace();
            }
            
            if (stop) break;
        }
        
        // trackPool is only used to iterate over all detections
        
        final SwimmingObject tracks = new SwimmingObject(trackGroup);
        
        trackPool.clearTracks();
        trackPool.addResult(tracks);
        // trackPool.getTrackGroupList().add(trackGroup);
        
        outputTracks.setValue(trackGroup);
        
        if (exportSwimmingPool.getValue())
        {
            ThreadUtil.invokeLater(new Runnable()
            {
                public void run()
                {
                    Icy.getMainInterface().getSwimmingPool().add(tracks);
                    new TrackManager();
                }
            });
        }
        if (exportXLS_flag.getValue())
        {
            exportMeshesToXLS(exportXLS_file.getValue(true));
        }
        
        Sequence output = rasterizeContours(input.getValue());
        if (output.getColorModel() != null)
        {
            output.getColorModel().setColormap(0, new FireColorMap());
            outputSequence.setValue(output);
            if (getUI() != null) addSequence(outputSequence.getValue());
        }
        
        mainService.shutdown();
        meshService.shutdown();
    }
    
    private void initializeMeshes(final int t, Sequence binary, ArrayList<Mesh> meshes, final boolean useVTK)
    {
        if (getUI() != null) getUI().setProgressBarMessage("Creating meshes...");
        
        Sequence inputSeq = input.getValue();
        final Point3d imageResolution = new Point3d(inputSeq.getPixelSizeX(), inputSeq.getPixelSizeY(), inputSeq.getPixelSizeZ());
        
        trackGroup.getTrackSegmentList().clear();
        
        final List<ConnectedComponent> ccs = ConnectedComponents.extractConnectedComponents(binary, true, 10, 100000000, null).get(0);
        
        if (ccs.size() == 0)
        {
            System.out.println("no object");
            throw new EzException("No object found", true);
        }
        
        ExecutorService service = Executors.newFixedThreadPool(SystemUtil.getAvailableProcessors());
        
        ArrayList<Future<Mesh>> initTasks = new ArrayList<Future<Mesh>>(ccs.size());
        
        final DoubleValue done = new DoubleValue(0);
        
        for (final ConnectedComponent cc : ccs)
        {
            initTasks.add(service.submit(new Callable<Mesh>()
            {
                public Mesh call()
                {
                    Mesh mesh;
                    
                    if (init_balls.getValue())
                    {
                        double meshResolution = mesh_resolution.getValue() * imageResolution.x;
                        Vector3d translation = new Vector3d(cc.getX() * imageResolution.x, cc.getY() * imageResolution.x, cc.getZ() * imageResolution.z);
                        mesh = new Icosahedron(init_ballsSize.getValue() * meshResolution, meshResolution, useVTK).getMesh(translation);
                    }
                    else
                    {
                        Point3i minBB = new Point3i();
                        new ConnectedComponentDescriptor().computeBoundingBox(cc, minBB, new Point3i());
                        Vector3d translation = new Vector3d(imageResolution.x * minBB.x, imageResolution.y * minBB.y, imageResolution.z * minBB.z);
                        mesh = new MarchingTetrahedra(cc, mesh_resolution.getValue(), imageResolution, useVTK).getMesh(translation);
                    }
                    
                    mesh.setT(t);
                    
                    try
                    {
                        mesh.topology.reSample(10);
                        return mesh;
                    }
                    catch (MeshSplittingException e)
                    {
                        System.err.println(e.getMessage());
                        mesh.clean();
                        return null;
                    }
                    catch (MeshException e)
                    {
                        System.err.println(e.getMessage());
                        mesh.clean();
                        return null;
                    }
                    finally
                    {
                        synchronized (done)
                        {
                            done.setValue(done.getValue() + 1);
                            if (getUI() != null) getUI().setProgressBarValue(done.getValue() / ccs.size());
                        }
                    }
                }
            }));
        }
        
        for (Future<Mesh> meshTask : initTasks)
        {
            try
            {
                Mesh mesh = meshTask.get();
                if (mesh != null)
                {
                    meshes.add(mesh);
                    vtkPainter.addMesh(mesh);
                    TrackSegment segment = new TrackSegment();
                    segment.addDetection(mesh);
                    trackGroup.addTrackSegment(segment);
                }
            }
            catch (InterruptedException e)
            {
            }
            catch (ExecutionException e)
            {
            }
            catch (RejectedExecutionException e)
            {
                if (!stop) System.err.println("Active Meshes: cannot schedule new initialization tasks.");
            }
        }
        
        initTasks.clear();
        service.shutdown();
        if (getUI() != null) getUI().setProgressBarValue(0.0);
        input.getValue().painterChanged(vtkPainter);
    }
    
    private void initializeModels(ArrayList<Mesh> meshes)
    {
        if (regul_flag.getValue()) regul.unregisterMeshes();
        
        if (cvms_flag.getValue()) cvms.unregisterMeshes();
        
        if (grad_flag.getValue()) grad.unregisterMeshes();
        
        coupling.unregisterMeshes();
        
        for (Mesh mesh : meshes)
            initializeModels(mesh);
    }
    
    private void initializeModels(Mesh mesh)
    {
        if (regul_flag.getValue()) regul.registerMesh(mesh);
        
        if (cvms_flag.getValue()) cvms.registerMesh(mesh);
        
        if (grad_flag.getValue()) grad.registerMesh(mesh);
        
        coupling.registerMesh(mesh);
        
        mesh.setConvergence(new SlidingWindowConvergence(conv_winSize.getValue(), Operation.VAR_COEFF));
    }
    
    private int findEnteringObjects(ArrayList<Mesh> meshes, Sequence sequence, int minSize, int maxSize, short nbClasses)
    {
        final Point3d scale = new Point3d(sequence.getPixelSizeX(), sequence.getPixelSizeY(), sequence.getPixelSizeZ());
        
        // Find objects in the current image that are not already segmented
        // if the distance between a new object and an existing contour is too small, discard it
        
        HKMeans hk = new HKMeans();
        List<ConnectedComponent> objects;
        try
        {
            objects = hk.hKMeans(sequence, 3.0, nbClasses, minSize, maxSize, 10.0, null);
        }
        catch (ConvolutionException e1)
        {
            e1.printStackTrace();
            throw new RuntimeException(e1.getMessage(), e1.getCause());
        }
        
        ExecutorService service = Executors.newFixedThreadPool(SystemUtil.getAvailableProcessors());
        
        ArrayList<Future<Mesh>> initTasks = new ArrayList<Future<Mesh>>(objects.size());
        
        int nbNewObjects = 0;
        
        withTheNextObject: for (final ConnectedComponent cc : objects)
        {
            // check if the object is already segmented using a distance measure
            Point3d ccc = new Point3d(cc.getX() * scale.x, cc.getY() * scale.y, cc.getZ() * scale.z);
            
            for (Mesh mesh : meshes)
                try
                {
                    if (mesh.isInside(ccc) > 0) continue withTheNextObject;
                }
                catch (NullPointerException npe)
                {
                    continue withTheNextObject;
                }
            
            nbNewObjects++;
            
            initTasks.add(service.submit(new Callable<Mesh>()
            {
                public Mesh call()
                {
                    Mesh mesh;
                    
                    if (init_balls.getValue())
                    {
                        double meshResolution = mesh_resolution.getValue() * scale.x;
                        Vector3d translation = new Vector3d(cc.getX() * scale.x, cc.getY() * scale.x, cc.getZ() * scale.z);
                        mesh = new Icosahedron(init_ballsSize.getValue() * meshResolution, meshResolution, useVTK.getValue()).getMesh(translation);
                    }
                    else
                    {
                        Point3i minBB = new Point3i();
                        new ConnectedComponentDescriptor().computeBoundingBox(cc, minBB, new Point3i());
                        Vector3d translation = new Vector3d(scale.x * minBB.x, scale.y * minBB.y, scale.z * minBB.z);
                        mesh = new MarchingTetrahedra(cc, mesh_resolution.getValue(), scale, useVTK.getValue()).getMesh(translation);
                    }
                    
                    try
                    {
                        mesh.topology.reSample(10);
                        return mesh;
                    }
                    catch (MeshSplittingException e)
                    {
                        System.err.println(e.getMessage());
                        mesh.clean();
                        return null;
                    }
                    catch (MeshException e)
                    {
                        System.err.println(e.getMessage());
                        mesh.clean();
                        return null;
                    }
                }
            }));
        }
        
        for (Future<Mesh> meshTask : initTasks)
        {
            try
            {
                Mesh mesh = meshTask.get();
                if (mesh != null)
                {
                    mesh.setT(meshes.get(0).getT());
                    meshes.add(mesh);
                    vtkPainter.addMesh(mesh);
                    TrackSegment segment = new TrackSegment();
                    segment.addDetection(mesh);
                    trackGroup.addTrackSegment(segment);
                    // System.out.println(mesh.getResolution());
                }
            }
            catch (InterruptedException e)
            {
            }
            catch (ExecutionException e)
            {
            }
            catch (RejectedExecutionException e)
            {
                if (!stop) System.err.println("Active Meshes: cannot schedule new initialization tasks.");
            }
        }
        
        service.shutdown();
        
        return nbNewObjects;
    }
    
    private void deformMeshes(ArrayList<Mesh> allMeshes)
    {
        if (getUI() != null) getUI().setProgressBarMessage("Initializing energy models...");
        
        initializeModels(allMeshes);
        
        if (getUI() != null) getUI().setProgressBarMessage("Evolving...");
        
        Sequence inputSeq = input.getValue();
        
        double voxelVolume = inputSeq.getPixelSizeX() * inputSeq.getPixelSizeY() * inputSeq.getPixelSizeZ();
        Point3d minBounds = new Point3d(0, 0, 0);
        Point3d maxBounds = new Point3d(inputSeq.getSizeX() * inputSeq.getPixelSizeX(), inputSeq.getSizeY() * inputSeq.getPixelSizeY(), inputSeq.getSizeZ() * inputSeq.getPixelSizeZ());
        
        int it = 0;
        VarInteger nbConverged = new VarInteger("Number of converged contours", 0);
        
        ArrayList<Mesh> activeMeshes = new ArrayList<Mesh>(allMeshes.size());
        
        mainLoop: do
        {
            nbConverged.setValue(0);
            
            if (cvms_flag.getValue() && it % 25 == 0)
            {
                boolean ok = false;
                do
                {
                    try
                    {
                        cvms.updateMeans(cvms_local.getValue());
                        ok = true;
                    }
                    catch (MeshException e)
                    {
                        ok = false;
                        allMeshes.remove(e.mesh);
                        
                        vtkPainter.removeMesh(e.mesh);
                        
                        e.mesh.clean();
                        
                        ArrayList<TrackSegment> segments = trackGroup.getTrackSegmentList();
                        for (int j = 0; j < segments.size(); j++)
                        {
                            TrackSegment segment = segments.get(j);
                            
                            if (segment.containsDetection(e.mesh))
                            {
                                segment.removeDetection(e.mesh);
                                if (segment.getDetectionList().size() == 0) segments.remove(j--);
                                break;
                            }
                        }
                    }
                } while (!ok);
            }
            
            // 1) Extract active meshes from the list of meshes
            
            activeMeshes.clear();
            
            for (int i = 0; i < allMeshes.size(); i++)
            {
                Mesh mesh = allMeshes.get(i);
                
                SlidingWindowConvergence window = mesh.getConvergence();
                
                if (window.checkConvergence(conv_epsilon.getValue()))
                {
                    nbConverged.setValue(nbConverged.getValue() + 1);
                    
                    if (nbConverged.getValue() == allMeshes.size())
                    {
                        break mainLoop; // all meshes have converged
                    }
                    
                    continue;
                }
                
                // remove the active mesh from the list (will be re-inserted at the end of the loop)
                activeMeshes.add(allMeshes.remove(i--));
            }
            
            // 2) Compute all mesh forces independently
            
            meshTasks.clear();
            meshTasks.ensureCapacity(activeMeshes.size());
            
            for (final Mesh mesh : activeMeshes)
            {
                meshTasks.add(mainService.submit(new Callable<Mesh>()
                {
                    @Override
                    public Mesh call()
                    {
                        mesh.updateNormals();
                        mesh.computeForces();
                        return mesh;
                    }
                }));
            }
            
            // 3) Apply forces
            try
            {
                for (Future<Mesh> meshTask : meshTasks)
                {
                    Mesh mesh = meshTask.get();
                    Model.flushDeformations_OLD(mesh, timeStep.getValue(), minBounds, maxBounds);
                    mesh.getConvergence().add(mesh.getDimension(1));
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            catch (ExecutionException e)
            {
                e.printStackTrace();
            }
            
            // 4) reSample meshes
            
            if (reSample_flag.getValue())
            {
                double minVolume = 10 * voxelVolume;
                
                reSampleMeshes(activeMeshes, minVolume);
            }
            
            // 5) re-insert active meshes in the global list for convergence checking
            
            allMeshes.addAll(activeMeshes);
            
            if (getUI() != null) getUI().setProgressBarValue((double) nbConverged.getValue() / (double) allMeshes.size());
            
            if (it % refreshRate.getValue() == 0)
            {
                ThreadUtil.invokeLater(repainter);
            }
            
            it++;
            
        } while (!stopAndMoveToNextFrame && !stop && allMeshes.size() > 0);
    }
    
    private void reSampleMeshes(ArrayList<Mesh> meshes, final double minVolume)
    {
        ArrayList<Future<Mesh[]>> reSamplingTasks = new ArrayList<Future<Mesh[]>>(meshes.size());
        
        int i = 0;
        
        while (meshes.size() > 0)
        {
            final Mesh mesh = meshes.remove(0);
            
            final int meshID = i++;
            
            reSamplingTasks.add(mainService.submit(new Callable<Mesh[]>()
            {
                public Mesh[] call()
                {
                    try
                    {
                        if (mesh.getDimension(2) < minVolume) throw new MeshException(mesh, "Mesh is vanishing");
                        
                        mesh.topology.reSample(minVolume);
                        
                        return new Mesh[] { mesh };
                    }
                    catch (MeshSplittingException e)
                    {
                        System.out.println("Mesh #" + meshID + " is " + (e.children.length == 0 ? "vanishing" : "splitting"));
                        // meshes.remove(i--);
                        
                        vtkPainter.removeMesh(mesh);
                        
                        mesh.clean();
                        
                        // Track update
                        TrackSegment motherSegment = null;
                        ArrayList<TrackSegment> segments = trackGroup.getTrackSegmentList();
                        for (int j = 0; j < segments.size(); j++)
                        {
                            TrackSegment segment = segments.get(j);
                            
                            if (segment.containsDetection(mesh))
                            {
                                segment.removeDetection(mesh);
                                
                                if (segment.getDetectionList().size() == 0)
                                {
                                    segments.remove(j--);
                                }
                                else
                                {
                                    motherSegment = segment;
                                }
                                break;
                            }
                        }
                        
                        if (e.children.length > 0)
                        {
                            for (Mesh newMesh : e.children)
                            {
                                initializeModels(newMesh);
                                vtkPainter.addMesh(newMesh);
                                // meshes.add(newMesh);
                                
                                // Track update
                                {
                                    TrackSegment childSegment = new TrackSegment();
                                    childSegment.addDetection(newMesh);
                                    trackGroup.addTrackSegment(childSegment);
                                    if (motherSegment != null && motherSegment.getDetectionList().size() > 0)
                                    {
                                        // not sure this line really works anymore
                                        // trackPool.createLink(motherSegment,
                                        // childSegment);
                                        
                                        // is this line supposed to replace the previous ?
                                        motherSegment.addNext(childSegment);
                                    }
                                }
                            }
                        }
                        
                        return e.children;
                    }
                    catch (MeshException e)
                    {
                        System.out.println("Mesh #" + meshID + " is vanishing");
                        // meshes.remove(i--);
                        
                        vtkPainter.removeMesh(mesh);
                        
                        mesh.clean();
                        
                        ArrayList<TrackSegment> segments = trackGroup.getTrackSegmentList();
                        for (int j = 0; j < segments.size(); j++)
                        {
                            TrackSegment segment = segments.get(j);
                            
                            if (segment.containsDetection(mesh))
                            {
                                segment.removeDetection(mesh);
                                if (segment.getDetectionList().size() == 0) segments.remove(j--);
                                break;
                            }
                        }
                        
                        return new Mesh[] {};
                    }
                    
                }
            }));
        }
        
        try
        {
            for (Future<Mesh[]> reSamplingTask : reSamplingTasks)
            {
                Mesh[] newMeshes = reSamplingTask.get();
                for (Mesh newMesh : newMeshes)
                    meshes.add(newMesh);
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }
    }
    
    private void exportMeshesToXLS(File file)
    {
        ArrayList<Detection> detections = trackPool.getAllDetection();
        
        if (detections.size() == 0) return;
        
        XlsManager xlsManager;
        try
        {
            xlsManager = new XlsManager(file);
            
            int cpt = 1;
            for (Detection det : trackPool.getAllDetection())
            {
                Mesh c3d = (Mesh) det;
                
                xlsManager.createNewPage("Object " + (cpt++));
                xlsManager.setLabel(0, 0, "X");
                xlsManager.setLabel(1, 0, "Y");
                xlsManager.setLabel(2, 0, "Z");
                xlsManager.setLabel(3, 0, "NX");
                xlsManager.setLabel(4, 0, "NY");
                xlsManager.setLabel(5, 0, "NZ");
                
                int n = c3d.vertices.size();
                
                for (int i = 0, row = 1; i < n; i++)
                {
                    Vertex v = c3d.vertices.get(i);
                    
                    if (v == null) continue;
                    
                    xlsManager.setNumber(0, row, v.position.x);
                    xlsManager.setNumber(1, row, v.position.y);
                    xlsManager.setNumber(2, row, v.position.z);
                    
                    xlsManager.setNumber(3, row, v.normal.x);
                    xlsManager.setNumber(4, row, v.normal.y);
                    xlsManager.setNumber(5, row, v.normal.z);
                    
                    row++;
                }
            }
            
            xlsManager.SaveAndClose();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    private Sequence rasterizeContours(Sequence s)
    {
        final Sequence sBin = new Sequence();
        sBin.setName(s.getName() + "_bin");
        sBin.setPixelSizeX(s.getPixelSizeX());
        sBin.setPixelSizeY(s.getPixelSizeY());
        sBin.setPixelSizeZ(s.getPixelSizeZ());
        
        // ExecutorService service = Executors.newCachedThreadPool();
        
        ArrayList<Detection> detections = trackPool.getAllDetection();
        
        // retrieve the maximum t (no need to create empty stuff if the process was interrupted)
        
        int maxT = -1;
        for (Detection det : detections)
            if (det.getT() > maxT) maxT = det.getT();
        
        for (int t = 0; t <= maxT; t++)
        {
            for (int z = 0; z < s.getSizeZ(); z++)
                sBin.setImage(t, z, new IcyBufferedImage(s.getSizeX(), s.getSizeY(), 1, DataType.USHORT));
            
            short cpt = 1;
            
            for (final Detection det : detections)
                if (det.getT() == t && det instanceof Mesh)
                {
                    Mesh mesh = (Mesh) det;
                    if (mesh.getDimension(0) == 0) continue; // empty mesh
                    mesh.rasterize(sBin, cpt++, mainService);
                }
        }
        
        return sBin;
    }
    
    @Override
    public void stopExecution()
    {
        stop = true;
    }
    
    @Override
    public void clean()
    {
        if (input.getValue() == null) return;
        
        for (Painter painter : input.getValue().getPainters())
            if (painter instanceof VTKMeshPainter) input.getValue().removePainter(painter);
    }
    
    @Override
    public void declareInput(VarList inputMap)
    {
        inputMap.add(input.getVariable());
        inputMap.add(seq_init.getVariable());
        inputMap.add(init_balls.getVariable());
        inputMap.add(init_ballsSize.getVariable());
        inputMap.add("Regul.", regul_flag.getVariable());
        inputMap.add(regul_weight.getVariable());
        regul_resIndep.setValue(true);
        inputMap.add("Region", cvms_flag.getVariable());
        inputMap.add(cvms_local.getVariable());
        inputMap.add(cvms_weight.getVariable());
        inputMap.add(cvms_sens.getVariable());
        inputMap.add(cvms_stabilizeData.getVariable());
        inputMap.add("Gradient", grad_flag.getVariable());
        inputMap.add(grad_weight.getVariable());
        inputMap.add(mesh_resolution.getVariable());
        inputMap.add(reSample_flag.getVariable());
        inputMap.add(timeStep.getVariable());
        inputMap.add(conv_epsilon.getVariable());
        inputMap.add(conv_winSize.getVariable());
        inputMap.add(exportXLS_flag.getVariable());
        inputMap.add(exportXLS_file.getVariable());
        inputMap.add(useVTK.getVariable());
        inputMap.add(tracking_flag.getVariable());
        inputMap.add("Track incoming", tracking_watchIncoming.getVariable());
        inputMap.add("Incoming min size", tracking_incomingMinSize.getVariable());
        inputMap.add("Incoming max size", tracking_incomingMaxSize.getVariable());
        inputMap.add(exportSwimmingPool.getVariable());
    }
    
    @Override
    public void declareOutput(VarList outputMap)
    {
        outputMap.add(outputSequence);
        outputMap.add(outputTracks);
    }
}

