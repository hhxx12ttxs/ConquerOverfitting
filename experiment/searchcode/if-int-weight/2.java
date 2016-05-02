/**
 * 
 */
package com.autonavi.lbscloud.apollo.irouter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * 权重分配算法
 * 
 * @author pei.lv
 * @date 2014-4-4
 * 
 */
public class Weight {

	private Random random = new Random();

	public static List<Server> servers;

	private int factor;

	public void init() {
		this.servers = new ArrayList<Server>();
		servers.add(new Server("127.0.0.1", 30));
		servers.add(new Server("127.0.0.2", 50));
		servers.add(new Server("127.0.0.3", 1000));
		servers.add(new Server("127.0.0.4", 100));

		// 初始化
		this.factor = 0;//整个集群的静态处理能力
		for (Iterator<Server> iter = servers.iterator(); iter.hasNext();) {
			Server server = iter.next();
//			server.offset = factor;//1 0； 2 30；3 80；4 1080
			factor += server.weight;
			
			System.out.println(server.toString());
		}
		System.out.println(factor);
	}

	public static void main(String[] args) {
		Weight w = new Weight();
		w.init();
		for (int i = 0; i < 100; i++) {
			Server server = w.choose();
			server.inc();
//			System.out.println(server.ip);
		}
		
		for(Server config: servers){
			System.out.println(config.ip +":" + config.i);
		}

	}
	
	public Server choose(){
//		return this.servers.get(new Random().nextInt(this.servers.size()));
		 Server selected = null;
		    int rv = random.nextInt(factor);
		    System.out.println(rv);
		    for (int i = servers.size() - 1; i >= 0; i--) {
		        Server server = servers.get(i);
//		        if (rv >= server.offset) {
//		            selected = server;
//		            break;
//		        }
		    }
		    return selected;
	}
	
}

//class Server {
//	public String ip;
//	public int weight;
//	public int offset;
//	
//	public int i;
//
//	public Server(String ip, int weight) {
//		this.ip = ip;
//		this.weight = weight;
//	}
//	
//	public void inc(){
//		this.i++;
//	}
//
//	@Override
//	public String toString() {
//		return "Server [ip=" + ip + ", weight=" + weight + ", offset=" + offset
//				+ "]";
//	}
//
//	
//	
//	
//}

