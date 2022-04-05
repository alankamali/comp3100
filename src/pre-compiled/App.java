import java.util.*;
import java.io.*;
import java.net.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class App {

	// for some reason the lines below throw and exception error so initialising in the app constructor with error handling 
		//Socket s = new Socket("127.0.0.1", 50000);
		//BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream()));
		//DataOutputStream dout = new DataOutputStream(s.getOutputStream());
	
	Socket s = null;
	BufferedReader din = null;
	DataOutputStream dout = null;

	//global variables 
	Server[] serArr = new Server[1];
	String msg = "";
	String largestServerType = "";
	int nServers = 0;
	int maxCores = 0;
	int serverCounter = 0;
	boolean stop = false;
	
	public App(String add, int port){
		try {
			s = new Socket(add, port);
			din = new BufferedReader(new InputStreamReader(s.getInputStream()));
			dout = new DataOutputStream(s.getOutputStream());
		}catch (Exception e){
			System.out.println("Im gonna break this computer:");
			e.printStackTrace();
		}
	}

	public void run() {

		//start of handshake protocol
		//lines 4-7 pseudocode from iLearn
		send("HELO");
		msg = receive();

		send("AUTH " + System.getProperty("user.name"));
		msg = receive();

		//parse the xml
		//to identify largestServerType and nServers;
		parseXML();

		send("REDY");
		msg = receive();

		if (!msg.equals("NONE")) {
			while (!stop){
				if (msg.equals("OK")) {
					send("REDY");
					msg = receive();
				}
				//maybe this is where client2 isnt working??? not cheking to see if msgs is none after the second redy??
				if (msg.equals("NONE")) {
					stop = true;
					break;
				}
				//schedule jobs in LLR fashion using server counter and nserver
				String[] jobRes = msg.split(" ");
				//JOBN FORMAT:
				//	JOBN submitTime jobID estRuntime core memory disk

				String jobCMD = jobRes[0];

				int jobID = Integer.parseInt(jobRes[2]);
				//if server sends JOBN, then SCHD
				if (jobCMD.equals("JOBN")) {
					send("SCHD " + jobID + " " + largestServerType+ " "  + serverCounter);
					msg = receive();
					serverCounter++;
					//if server limit is reached, reset to 0
					if (serverCounter >= nServers) {
						serverCounter = 0;
					}

				}
				//if Job Complete, then send ready and read new msg
				if (jobCMD.equals("JCPL")){
					send("REDY");
					msg = receive();
				}
			}
		//after "NONE", quit the program
		quit();}
	}


	//sending msgs to server
	public void send(String s){
		try {
			dout.write((s + "\n").getBytes());
			dout.flush();
		} catch (IOException e){
			System.out.println("ERROR: " + e);
		}
	}

	//receiving msgs from server
	public String receive(){
		try {
			msg = din.readLine();
		} catch (IOException e) {
			System.out.println("ERROR: " + e);
		}
		return msg;
	}

	public void quit() {
		try {
			send("QUIT");
			msg = receive();
			if (msg == "QUIT") {
				System.out.println("I hate chest infections");
				din.close();
				dout.close();
				s.close();
			}
		} catch (IOException e) {
			System.out.println("ERROR: " + e);
		}
	}

	public void parseXML(){

		try {
			File dsSysXML = new File("ds-system.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dB = dbFactory.newDocumentBuilder();
			Document doc = dB.parse(dsSysXML);

			//SO said to do this, apparently this makes all the same line a single node??
			//stackoverflow.com/questions/13786607/normalization-in-dom-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			NodeList servers = doc.getElementsByTagName("server");
			//server Array to put the nodes in
			serArr = new Server[servers.getLength()];

			for (int i = 0; i < servers.getLength(); i++){
				Element server = (Element) servers.item(i);
				String t = server.getAttribute("type");
				int l = Integer.parseInt(server.getAttribute("limit"));
				int b = Integer.parseInt(server.getAttribute("bootupTime"));
				float r = Float.parseFloat(server.getAttribute("hourlyRate"));
				int c = Integer.parseInt(server.getAttribute("cores"));
				int m = Integer.parseInt(server.getAttribute("memory"));
				int d = Integer.parseInt(server.getAttribute("disk"));

				Server temp = new Server(i, t, l, b, r, c, m, d);
				serArr[i] = temp;

				//might as well find biggest server and its limit while we looping
				if (c > maxCores){
					maxCores = c;
					nServers = l;
					largestServerType = t;
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	public static void main(String args[]){
		App app = new App("127.0.0.1", 50000);
		app.run();
	}
}
