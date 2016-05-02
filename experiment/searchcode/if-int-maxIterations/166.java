package badwithnames.FractalView;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingWorker;

public class ModelWorker extends SwingWorker<Void, PointDatum> {
  private int               blockx;
  private int               blocky;
  private int               maxIterations;
  private Point[][]         points;
  private Deque<PointDatum> results;
  
  public ModelWorker( int x, int y, Point[][] points, int maxIterations, int width, Deque<PointDatum> results ){
    blockx = x;
    blocky = y;
    this.points = points;
    this.maxIterations = maxIterations;
    this.results = results;
    //System.out.println( "Creating Worker" );
  }
  
  @Override
  protected Void doInBackground() throws Exception {
    //System.out.println( "Worker Processing" );
    LinkedList<PointDatum> processing = new LinkedList<PointDatum>();
    PointDatum p;
    processing.add( new PointDatum( 0, 0, points.length ));
    
    while( !processing.isEmpty() && !isCancelled() ){
      p = processing.remove();
      for( int i = 1; i < maxIterations && points[p.x][p.y].magSq() < 4 ; ++i ){
        points[p.x][p.y].iterate();
      }
      p.iterations = points[p.x][p.y].i;
      //System.out.println( "WORKER[" + String.valueOf( points[p.x][p.y].cRe ) + "," + String.valueOf( points[p.x][p.y].cIm ) + "] - " + String.valueOf( p.iterations ));
      if( p.iterations >= maxIterations )
        p.iterations = 0;
      if( 1 < p.width ){
        for( int i = 0; i < 2; ++i ){
          for( int j = 0; j < 2; ++j ){
            processing.add( new PointDatum( p.x + i*p.width/2, p.y + j*p.width/2, p.width/2 ));
          }
        }
      }
      p.x += blockx;
      p.y += blocky;
      publish( p );
    }
    
    return null;
  }
  
  @Override
  protected void process( List<PointDatum> data ){
    results.addAll( data );
  }
}

