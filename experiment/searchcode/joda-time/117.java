package pt.up.fe.Messages;

import pt.up.fe.Communication.ClientSocket;
import pt.up.fe.Logic.SetecApp;

public class SetTimeoutMessage{	//used by caretaker only

	private int hours;
	private int minutes;
	private int idSensor;
	private int idSenior;
	private boolean defaultTimeout;
	private ClientSocket cs=null;
	private SetecApp mAppState;
	
	public SetTimeoutMessage(int idSenior, int idSensor)
	{
		super();
		this.idSenior = idSenior;
		this.idSensor = idSensor;
		defaultTimeout=true;
	}
	
	public SetTimeoutMessage(int idSenior, int idSensor, int hours, int minutes)
	{
		super();
		this.idSenior = idSenior;
		this.idSensor = idSensor;
		this.hours = hours;
		this.minutes = minutes;
		defaultTimeout=false;
	}
	
	public void sendMessage(SetecApp appState) {
		mAppState = appState;
		String ans=null;
		String[] ansParts=null;
		cs = new ClientSocket();
		cs.start();
		while(cs.isSocketAlive()==0)
			;
		if(cs.isSocketAlive()==1)
		{
			if(defaultTimeout) {
				//cs.send("11#C#" + Caretaker.getId() + "#" +  idSensor + "#" + 
					//	idSenior + "#."); //caretaker
				cs.send("11#C#" + mAppState.getCurrentuserId() + "#" +  idSensor + "#" + 
						idSenior + "#."); //caretaker
			}
			else {
				cs.send("11#C#" + mAppState.getCurrentuserId() + "#" +  idSensor + "#" +
						idSenior + "#" + hours + "#" + minutes + "#."); //caretaker

				//cs.send("11#C#" + Caretaker.getId() + "#" +  idSensor + "#" +
					//	idSenior + "#" + hours + "#" + minutes + "#."); //caretaker
			}
			while((ans = cs.read())==null)
			{
			}
			cs.close();
			
			ansParts = ans.split("#");
			
			if(ansParts[3].equals("1"))
			{
				//timeout alterado com sucesso
			}
			else if(ansParts[3].equals("0"))
			{
				//timeout no alterado
			}
			//TODO logic
		}
		else if(cs.isSocketAlive()==0)
		{
			//TODO not conected
		}
		else
		{
			//TODO timeout
		}
	}

}

