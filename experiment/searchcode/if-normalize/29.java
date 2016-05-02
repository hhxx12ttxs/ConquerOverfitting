import org.jbox2d.common.*;
import org.jbox2d.dynamics.joints.*;
import org.jbox2d.collision.AABB;
import java.util.ArrayList;
class LWorm extends LBody{
  
  int WormLength = 9;
  float dF = 0;
  boolean Pull = true;
  
  Vec2 SumForce = new Vec2(0,0);
  
  ArrayList<LWormPart> Parts = new ArrayList<>();
  ArrayList<DistanceJoint> Joints = new ArrayList<>();
  
  LWorm(float X, float Y,luc in_parent){
    super(in_parent);
    
    Vec2 size = new Vec2(1,WormLength * Luc.pixSize - Luc.pixHalfSize);
    Vec2 coord = new Vec2(X,Y);
    Vec2 pointCoordAndSize = Luc.box2d.coordPixelsToWorld(coord.add(size));
    Vec2 pointCoord = Luc.box2d.coordPixelsToWorld(coord);
    Vec2 lowerBound = new Vec2(luc.min(pointCoordAndSize.x,pointCoord.x), luc.min(pointCoordAndSize.y,pointCoord.y));
    Vec2 upperBound = new Vec2(luc.max(pointCoordAndSize.x,pointCoord.x), luc.max(pointCoordAndSize.y,pointCoord.y));    
    lowerBound.addLocal(new Vec2(0.2f,0.2f));
    upperBound.subLocal(new Vec2(0.2f,0.2f));
    
    AABB aabb = new AABB();
    aabb.lowerBound.set(lowerBound);
    aabb.upperBound.set(upperBound);
    Query callback = new Query();
    Luc.box2d.world.queryAABB(callback, aabb);
     //println(callback.Grounds.size());
     assert(callback.Grounds.size()>4);
     float MinY = Luc.screenHeight; float FirstX = 0; float SizeY = 0;
    for (int i=0;i<callback.Grounds.size();i++){
      LGround PushedGround = callback.Grounds.get(i);
      if(PushedGround.getPosition().y < MinY){
        MinY = PushedGround.getPosition().y;
        FirstX = PushedGround.getPosition().x;
        SizeY = PushedGround.getSize().y;
      }
      PushedGround.setPosition(new Vec2(PushedGround.getPosition().x + PushedGround.getSize().x, PushedGround.getPosition().y ));
    } 
    /*
    float MinY = Y;
    float FirstX = X;
    float SizeY = iPixDefaultSize;
    */
    for(int i =0;i< luc.floor(WormLength/2.f)+1;i++){
      LWormPart newPart = new LWormPart((int)FirstX, (int)(MinY + i*2*SizeY),Luc);
      Parts.add(newPart);
      //newPart.arBodiesAffect.add(this);
    }
    
      Luc.All.addBodies(Parts);
      
    for(int i =0;i< (Parts.size() - 1);i++){
      LWormPart Part1 = Parts.get(i);
      LWormPart Part2 = Parts.get(i+1);
    DistanceJointDef djd = new DistanceJointDef();
    // Connection between previous particle and this one
    djd.bodyA = Part1.myBody;
    djd.bodyB = Part2.myBody;
    // Equilibrium length
    djd.length = Luc.box2d.scalarPixelsToWorld(SizeY*2);
    
    // These properties affect how springy the joint is 
    djd.frequencyHz = 50;  // Try a value less than 5 (0 for no elasticity)
    djd.dampingRatio = 2f; // Ranges between 0 and 1
    djd.collideConnected = true;
    
    DistanceJoint dj = (DistanceJoint) Luc.box2d.world.createJoint(djd);
    Part1.BackJoint = dj;
    Joints.add(dj);
    }
    
    for(int i =1;i< Parts.size();i++){
          Parts.get(i).setUnStable();
      //newPart.setUnStable();
    }
    Parts.get(Parts.size()-1).Head = Parts.get(0);
    Parts.get(0).setStable();

    
    
  }
  
  void act(){
    ApplyForce();
    //display();
}
  
    void ApplyForce(){
/*
      float dS = Parts.get(2).getPosition().y - Parts.get(0).getPosition().y ;
      if(dS >= 4*iPixDefaultSize && Pull){
        dF=-0.02;    Pull = false;
      }
      else if (dS <= 2*iPixDefaultSize && !Pull)
      {
        dF=0.02;  Pull = true;      
      }
              for(int i =0;i<Joints.size();i++){
          Joints.get(i).setLength(Joints.get(i).getLength()+ dF); 
        }   
    Parts.get(0).PhBody.applyForceToCenter(new Vec2(0,0.5));
    */ 
        
    Vec2 Head = new Vec2(Parts.get(0).getPosition().add(Parts.get(0).getSize().mul(0.5f)));
    Vec2 FirstBody = new Vec2(Parts.get(1).getPosition().add(Parts.get(1).getSize().mul(0.5f)));
    Vec2 Diff = Head.sub(FirstBody);
    Vec2 normalize = new Vec2(Diff.x/Diff.length(),Diff.y/Diff.length());
    //Vec2 Ahead = FirstBody.add(normalize.mul(parent.iPixDefaultHalfSize));
    //float direction= 0.0f;
    if(Luc.random(0,300)>299 ) {
        float direction = 0f;
        if(Luc.random(-1,1)>0) direction= 1.f; else direction= -1.f;
        normalize = new Vec2(normalize.y*direction,normalize.x*direction);}
    if( normalize.y>0 && Luc.random(0,300)<290)normalize = new Vec2(normalize.x,-normalize.y);
    //direction = 1;
       //Vec2 normalizeNorm = new Vec2(direction*normalize.y,direction*normalize.x);
    Vec2 size = new Vec2(Luc.pixHalfSize,Luc.pixHalfSize);
    Vec2 coord = Head.add(normalize.mul(Luc.pixHalfSize));
    Luc.fill(Luc.color(255,0,0));
        Luc.rect(coord.x, coord.y, size.x, size.y);
    Vec2 pointCoordAndSize = Luc.box2d.coordPixelsToWorld(coord.add(size));
    Vec2 pointCoord = Luc.box2d.coordPixelsToWorld(coord);
    Vec2 lowerBound = new Vec2(luc.min(pointCoordAndSize.x,pointCoord.x), luc.min(pointCoordAndSize.y,pointCoord.y));
    Vec2 upperBound = new Vec2(luc.max(pointCoordAndSize.x,pointCoord.x), luc.max(pointCoordAndSize.y,pointCoord.y));    

    lowerBound.subLocal(new Vec2(0.0f,0.0f));
    upperBound.addLocal(new Vec2(0.0f,0.0f));
    
    AABB aabb = new AABB();
    aabb.lowerBound.set(lowerBound);
    aabb.upperBound.set(upperBound);
    Query callback = new Query();
    Luc.box2d.world.queryAABB(callback, aabb);
     //(callback.Grounds.size());
     //assert(callback.Grounds.size()>4);
    for(LGround oGround:callback.Grounds){
        Luc.All.removeBody(oGround);
    }
           
        
        
  SumForce.set(0,0);
  for(LWormPart oLWormPart: Parts){
      oLWormPart.arBodiesAffect.clear();
      oLWormPart.arBodiesAffect.addAll(arBodiesAffect);
  }
  }
  //for (LBody oLForceBody: arBodiesAffect) {
  //    Vec2 f = oLForceBody.GetForce(pos); 
  //      SumForce.addLocal(f);
  //    }
  //  }
    

Vec2 getForce(Vec2 input)
{
  return SumForce;
}    


}


