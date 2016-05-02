import java.awt.Point;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Vector;

class Game implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8523937259393959679L;

	int reihenfolge = ICheckClient.PLAYER1;
	boolean[][] field;
	ICheckClient player1,player2;
	Vector<Point> spielsteine_player1 = new Vector<Point>();
	Vector<Point> spielsteine_player2 = new Vector<Point>();


	public Game(ICheckClient player1,ICheckClient player2){
		this.player1 = player1;
		this.player2 = player2;
		field = new boolean[8][8];
		initField();
		initSpielsteine(1);
		initSpielsteine(2);
		try {

			player1.setSpielsteine(this.spielsteine_player1,this.spielsteine_player2);//spieler 2
			player2.setSpielsteine(this.spielsteine_player1,this.spielsteine_player2); //spieler 1
			player1.setSpieler(1);
			player2.setSpieler(2);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void initField(){

		for (int y =0;y<field.length;y++){
			if(y <3 || y > 4 ){
				for(int x=0;x<field[y].length;x++){
					if (y%2 == 0){
						if(x%2 !=0 ){
							field[y][x] = true;
						}
					}else{
						if(x%2 ==0 ){
							field[y][x] = true;
						}
					}
				}
			}
		}
	}

	private void initSpielsteine(int spieler){
		if(spieler == 1){
			for (int y = 5; y< field.length;y++){
				for (int x =0;x<field[y].length;x++){
					if (field[y][x])spielsteine_player1.add(new Point(x,y));
				}

			}


		}else if (spieler == 2){

			for (int y = 0; y< 3;y++){
				for (int x =0;x<field[y].length;x++){
					if (field[y][x])spielsteine_player2.add(new Point(x,y));
				}

			}

		}
	}

	public void pushUpdate() throws RemoteException{
		player1.setSpielsteine(this.spielsteine_player1,this.spielsteine_player2);//spieler 2
		player2.setSpielsteine(this.spielsteine_player1,this.spielsteine_player2); //spieler 1
		//player1.setSpieler(1);
		//player2.setSpieler(2);

	}

	public boolean isNextPlayer(int i){
		if(reihenfolge == i)
			return true;
		return false;


	}
	private boolean isDiagonal(int player,Point start, Point end){

		switch(player){
		case ICheckClient.PLAYER1:
			if (start.x < end.x){
				if (end.y == (start.y - (end.x - start.x)))
					return true;

			}
			else if(start.x > end.x){
				if (end.y == (start.y - (start.x - end.x)))
					return true;
			}
			return false;

		case ICheckClient.PLAYER2:
			if (start.x < end.x){
				if (end.y == (start.y +(end.x - start.x)))
					return true;

			}
			else if(start.x > end.x){
				if (end.y == (start.y + (start.x - end.x)))
					return true;
			}
			return false;
		}
		return false;
	}

	public int getJumpLenght(int player,Point start ,Point end){
		switch(player){
		case 1: return start.y - end.y;

		case 2: return end.y - start.y;
		}
		return player;

	}

	private Point getPointBetween(int player,Point start, Point end){

		Point pb = new Point();

		switch(player){
		case 1:
			if(end.x > start.x){
				pb.move(start.x+1, start.y-1);

			}else if(end.x < start.x){
				pb.move(start.x-1, start.y-1);
			}
			break;
		case 2:
			if(end.x > start.x){
				pb.move(start.x+1, start.y+1);

			}else if(end.x < start.x){
				pb.move(start.x-1, start.y+1);
			}
			break;
		}

		return pb;

	}

	public boolean execCommand(Command cmd) throws RemoteException {
		// TODO Auto-generated method stub
		/*
		 *-- Uberpruefen ob cmd.endpunkt nicht einem Spielstein des anderen entspricht. Wenn nein Fehler
		 *			Wenn ja ob der zug diagonal nach vorneist.
		 */

		switch(cmd.getClient().getStatus()){
		case ICheckClient.PLAYER1:
			if(!spielsteine_player2.contains(cmd.get_point_end()) && !spielsteine_player1.contains(cmd.get_point_end())){
				if (isDiagonal(1,cmd.get_point_start(),cmd.get_point_end())){
					switch(getJumpLenght(1, cmd.get_point_start(), cmd.get_point_end())){
					case 1: 
						spielsteine_player1.get(spielsteine_player1.indexOf(cmd.get_point_start())).setLocation(cmd.get_point_end()); //Startpunkt auf Endpunkt setzen.
						return true;
					case 2: 
						Point tmp = getPointBetween(1, cmd.get_point_start(), cmd.get_point_end());
						if(spielsteine_player2.contains(tmp)){
							spielsteine_player1.get(spielsteine_player1.indexOf(cmd.get_point_start())).setLocation(cmd.get_point_end()); //Startpunkt auf Endpunkt setzen.
							spielsteine_player2.remove(tmp);
							return true;
						}

					}

					return false;
				}
			}
			break;
		case ICheckClient.PLAYER2:
			if(!spielsteine_player1.contains(cmd.get_point_end())&& !spielsteine_player2.contains(cmd.get_point_end())){
				if (isDiagonal(2,cmd.get_point_start(),cmd.get_point_end())){
					switch(getJumpLenght(2, cmd.get_point_start(), cmd.get_point_end())){
					case 1: 
						spielsteine_player2.get(spielsteine_player2.indexOf(cmd.get_point_start())).setLocation(cmd.get_point_end()); //Startpunkt auf Endpunkt setzen.
						return true;
					case 2: 
						Point tmp = getPointBetween(2, cmd.get_point_start(), cmd.get_point_end());
						if(spielsteine_player1.contains(tmp)){
							spielsteine_player2.get(spielsteine_player2.indexOf(cmd.get_point_start())).setLocation(cmd.get_point_end()); //Startpunkt auf Endpunkt setzen.
							spielsteine_player1.remove(tmp);
							return true;
						}

					}

					return false;
				}
			}
			break;
		}
		return false;
	}

	private boolean isInBounds(Command cmd){
		if (cmd.get_point_end().x >=0 && cmd.get_point_end().x < 8){
			if (cmd.get_point_end().y >= 0 && cmd.get_point_end().y < 8){
				return true;
			}
			return false;
		}
		if (cmd.get_point_end().y >=0 && cmd.get_point_end().y < 8){
			if (cmd.get_point_end().x >= 0 && cmd.get_point_end().x < 8){
				return true;
			}
			return false;
		}
		return false;

	}

	public boolean possibleJump(Command cmd) throws RemoteException {
		// TODO Auto-generated method stub
		Command tmp = new Command(cmd.getClient(),cmd.get_point_end(),new Point());
		switch(cmd.getClient().getStatus()){
		case 1:
			tmp.get_point_end().move(tmp.get_point_start().x-2, tmp.get_point_start().y-2);
			if(spielsteine_player2.contains(getPointBetween(1, tmp.get_point_start(), tmp.get_point_end()))){
				if (!spielsteine_player1.contains(tmp.get_point_end()) && !spielsteine_player2.contains(tmp.get_point_end())){

					if (isInBounds(tmp)){
						System.out.println(tmp);
						return true;
					}
				}


			}
			else {
				tmp.get_point_end().move(tmp.get_point_start().x+2, tmp.get_point_start().y-2);
				if(spielsteine_player2.contains(getPointBetween(1, tmp.get_point_start(), tmp.get_point_end()))){
					if (!spielsteine_player1.contains(tmp.get_point_end()) && !spielsteine_player2.contains(tmp.get_point_end())){

						if (isInBounds(tmp)){
							System.out.println(tmp);
							return true;
						}
					}


				}
			}
			break;
		case 2:
			tmp.get_point_end().move(tmp.get_point_start().x-2, tmp.get_point_start().y+2);
			if(spielsteine_player1.contains(getPointBetween(2, tmp.get_point_start(), tmp.get_point_end()))){
				if (!spielsteine_player1.contains(tmp.get_point_end()) && !spielsteine_player2.contains(tmp.get_point_end())){

					if (isInBounds(tmp)){
						System.out.println(tmp);
						return true;
					}
				}


			}
			else {
				tmp.get_point_end().move(tmp.get_point_start().x+2, tmp.get_point_start().y+2);
				if(spielsteine_player1.contains(getPointBetween(2, tmp.get_point_start(), tmp.get_point_end()))){
					if (!spielsteine_player1.contains(tmp.get_point_end()) && !spielsteine_player2.contains(tmp.get_point_end())){

						if (isInBounds(tmp)){
							System.out.println(tmp);
							return true;
						}
					}


				}
			}
			break;
		}
		return false;
	}

	public void setNextPlayer() {
		// TODO Auto-generated method stub
		try{
			if (reihenfolge ==1) {
				reihenfolge = ICheckClient.PLAYER2;
				player1.receiveReihenfolge("warten auf Player2");
				player2.receiveReihenfolge("du bist an der Reihe.");
			}else if (reihenfolge== 2){
				reihenfolge =ICheckClient.PLAYER1;
				player2.receiveReihenfolge("warten auf Player1");
				player1.receiveReihenfolge("du bist an der Reihe.");
			}
		}catch(RemoteException e){
			e.printStackTrace();
		}
		//System.out.println("reiheinfolge: " +reihenfolge);


	}

	public boolean isFinished(){
		if(spielsteine_player1.size() <2 || spielsteine_player2.size()<2 )
			return true;
		return false;
	}
}

