package gun.simulation;

import java.awt.geom.Point2D;

import gun.model.ArtilleryModel;

public class SimThread extends Thread{
  private ArtilleryModel model;
  private double max_x;
  private double max_y;
  private double dt = 0.05;
  private Point2D.Double goal;
  private double r;
  private SimulationFinishedCallback callback;
  
  public SimThread(ArtilleryModel model, double r, double max_x, double max_y){
    this.model = model;
    this.max_x = max_x;
    this.max_y = max_y;
    this.r  = r;
  }
  
  public void start(SimulationFinishedCallback callback){
    this.callback = callback;
    start();
  }
  
  public void run(){
    ShellIntegrator si = new ShellIntegrator(model, model.getGunAngle());
    Point2D.Double goal = model.getTankPosition();
    Point2D.Double p = si.getShellPosition();
    while(p.distance(goal) > r && p.x < max_x && p.x > 0 && p.y < max_y && p.y > 0){
      si.advance(dt);
      p = si.getShellPosition();
      model.lock();
      model.setShellPosition(si.getShellPosition());
      model.unlock();
      try {
        Thread.sleep((int)(300.0 * dt));
      } catch (InterruptedException e) {
        break;
      }
    }
    if(callback != null){
      callback.simulationFinished();
    }  
  }
}

