package sesion01_holaslick;



import java.util.ArrayList;
import org.newdawn.slick.*;

public class Controladoru {
	
	private ArrayList<SpriteMovil> alfabetou;
	
	public Controladoru(){
		alfabetou = new ArrayList<SpriteMovil>();
		
		
	}
	
	public void add(float posicionY) throws SlickException {
		
		SpriteMovil u = new SpriteMovil("beto/u.png", new Punto(11300, posicionY),new Punto (-250,0));
		alfabetou.add(u);
	}
	
	
	public void draw(){

	          for(int u = 0; u < alfabetou.size(); u++ ){
				
				alfabetou.get(u).draw();
			}
		 
		}

	public void update(int delta){
		
			for (int u = 0; u < alfabetou.size(); u++){
				alfabetou.get(u).update(delta);
			}
			
			
		}
	public void delete(){
	
				for(int u = 0; u < alfabetou.size(); u++){
					if (alfabetou.get(u).getPosicion().getX() < 0){
						alfabetou.remove(u);
						Juego.restarVida();
					}
				}
				
			}
			
		}
