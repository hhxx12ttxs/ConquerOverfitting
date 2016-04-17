package me.Zion_Plays.Methoden;

import java.util.Calendar;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Time_methoden {
	
	@SuppressWarnings("deprecation")
	public static void settime(Player p){
		
		Calendar c = Calendar.getInstance();
		
		if(c.getTime().getHours() == 0){
			
			p.getWorld().setTime(0);
			return;
			
			
		}
		if(c.getTime().getHours() == 1){
			
			p.getWorld().setTime(19000);
			return;
			
			
		}
		if(c.getTime().getHours() == 2){
			
			p.getWorld().setTime(20000);
			return;
			
			
		}
		if(c.getTime().getHours() == 3){
			
			p.getWorld().setTime(21000);
			return;
			
			
		}
		if(c.getTime().getHours() == 4){
			
			p.getWorld().setTime(22000);
			return;
			
			
		}
		if(c.getTime().getHours() == 5){
			
			p.getWorld().setTime(23000);
			return;
			
			
		}
		if(c.getTime().getHours() == 6){
			
			p.getWorld().setTime(25000);
			return;
			
			
		}
		if(c.getTime().getHours() == 7){
			
			p.getWorld().setTime(1000);
			return;
			
			
		}
		if(c.getTime().getHours() == 8){
			
			p.getWorld().setTime(2000);
			return;
			
			
		}
		if(c.getTime().getHours() == 9){
			
			p.getWorld().setTime(3000);
			return;
			
			
		}
		if(c.getTime().getHours() == 10){
			
			p.getWorld().setTime(4000);
			return;
			
			
		}
		if(c.getTime().getHours() == 11){
			
			p.getWorld().setTime(5000);
			
			return;
			
		}
		if(c.getTime().getHours() == 12){
			
			p.getWorld().setTime(6000);
			return;
			
			
		}
		if(c.getTime().getHours() == 13){
			
			p.getWorld().setTime(7000);
			
			return;
			
		}
		if(c.getTime().getHours() == 14){
			
			p.getWorld().setTime(8000);
			
			return;
			
		}
		if(c.getTime().getHours() == 15){
			
			p.getWorld().setTime(9000);
			
			return;
			
		}
		if(c.getTime().getHours() == 16){
			
			p.getWorld().setTime(10000);
			
			return;
			
		}
		if(c.getTime().getHours() == 17){
			
			p.getWorld().setTime(11000);
			
			return;
			
		}
		if(c.getTime().getHours() == 18){
			
			p.getWorld().setTime(12000);
			
			return;
			
		}
		if(c.getTime().getHours() == 19){
			
			p.getWorld().setTime(13000);
			
			
			return;
		}
		if(c.getTime().getHours() == 20){
			
			p.getWorld().setTime(14000);
			
			return;
			
		}
		if(c.getTime().getHours() == 21){
			
			p.getWorld().setTime(15000);
			
			return;
			
		}
		if(c.getTime().getHours() == 22){
			
			p.getWorld().setTime(16000);
			
			return;
			
		}
		if(c.getTime().getHours() == 23){
			
			p.getWorld().setTime(17000);
			
			return;
			
		}
		if(c.getTime().getHours() == 24){
			
			p.getWorld().setTime(18000);
			
			return;
			
		}
		
	}

}

