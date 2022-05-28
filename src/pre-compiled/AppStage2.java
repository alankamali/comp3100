import java.util.*;
import java.io.*;
import java.net.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class AppStage2 {

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
	boolean stop = false;
	ArrayList<Server> serrArrList = new ArrayList<Server>();

	
	public AppStage2(String add, int port){
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
		parseXML();

		send("REDY");
		msg = receive();

		//just assign the first job to the first capable server then run algorithm
		assignFirstSCHD();
		newAlgo();

		quit();
	}

	/**
	Implenet new algorithm such that it performs better than baseline algorithms in general

	what I want to do:
		1) as jobs come in, give to the first server which can take it.
		2) if server has waiting jobs > 2, go to next server which is capable
	 */
	
	public void newAlgo () {
		if (!msg.equals("NONE")) {
			while (!stop){
				if (msg.equals("OK")) {
					send("REDY");
					msg = receive();
				}

				if (msg.equals("NONE")) {
					stop = true;
					break;
				}

				//Take the new server msg and split into sections
				String[] msgRes = msg.split(" ");

				if (msgRes[0].equals("JOBN")) {
					send("GETS Capable " + msgRes[4] + " " + msgRes[5] + " " + msgRes[6]);

					msg = receive(); //send back eg: DATA 5 123 (5 lines of data at 123 length each)
					
					//Store the msgs that come after DATA 
					String[] getsCapableRes = msg.split(" ");
					int numberOfDataLinesFromGetsCapable = Integer.parseInt(getsCapableRes[1]);
					ArrayList<String> serversAvailFromGetsCapable = new ArrayList<String>();
					
					send("OK");
					
					for (int i = 0; i < numberOfDataLinesFromGetsCapable; i++){
						msg = receive();
						serversAvailFromGetsCapable.add(msg);
					}

					//split each line of the msgs from serversAvailFromGetsCapable
					ArrayList<String[]> whichServerToSendTo = new ArrayList<String[]>();
					for (String s : serversAvailFromGetsCapable){
						String[] temp = s.split(" ");
						whichServerToSendTo.add(temp);
					}

					send("OK");
					msg = receive(); //this should be "."
					
					//Sort the servers in ascending order of cores, this will also improve utilisation since servers with the most "readilyavail cores" havent been booted up yet.
					//NB: learnt how to sort from this link https://stackoverflow.com/questions/4699807/sort-arraylist-of-array-in-java
					Collections.sort(whichServerToSendTo, new Comparator<String[]>(){
						public int compare (String[] s1, String[] s2){
							int s11 = Integer.parseInt(s1[4]);
							int s22 = Integer.parseInt(s2[4]);
							if (s11 >= s22){
								return s22;
							}else {
								return s11;
							}
						}
					});

					/**
						Loop through the servers which are capable to run the job
						 	find one that has enough resources to run the job now
							also has less than 2 waiting jobs
						if not found
							find out what jobs are running on the server 
							schd job to server which is capable of running it and wait

							(what I should be doing is find out the server with least amount of estRunTime remaining and schd to them)

					 */
					Boolean capableServerFound = false;
					if (!capableServerFound){
						for (String[] server : whichServerToSendTo){
							if ((Integer.parseInt(server[4]) >= Integer.parseInt(msgRes[4])) && Integer.parseInt(server[5]) >= Integer.parseInt(msgRes[5]) && Integer.parseInt(server[6]) >= Integer.parseInt(msgRes[6]) && Integer.parseInt(server[7]) < 2){
								send("SCHD " + msgRes[2] + " " + server[0] + " " + server[1]);
								capableServerFound = true;

								break;
							} 
						}

					} 
					/* 
						LSTJ each server which is capable
						add up their est remaining times of jobs
						find the one with the lowest est remaining time
						schd job to that server 
					*/

					if (!capableServerFound){
						int lowestEstRemainingTime = 9999999;
						String[] serverToSendTo = whichServerToSendTo.get(0);
						for (String[] server: whichServerToSendTo) {
							int estRemainingTime = 0;
							send("LSTJ " + server[0] + " " + Integer.parseInt(server[1]));
							msg = receive();

							send("OK");

							String[] lstjRes = msg.split(" ");
							ArrayList<String[]> lstjResponseArr = new ArrayList<String[]>();
							
							for (int i = 0; i < Integer.parseInt(lstjRes[1]); i++){
								msg = receive();
								String[] temp = msg.split(" ");
								lstjResponseArr.add(temp);
								estRemainingTime += Integer.parseInt(temp[4]);
								if (estRemainingTime < lowestEstRemainingTime && server[2].equals("active")){   //should i also check how many running jobs there are???
									lowestEstRemainingTime = estRemainingTime;
									serverToSendTo = server;
								}
							}
							send("OK");
							msg = receive(); // should be .

						}
						send("SCHD " + msgRes[2] + " " + serverToSendTo[0] + " " + Integer.parseInt(serverToSendTo[1]));
					}
				
				//if Job Complete, then send ready and read new msg
				}
				if (msgRes[0].equals("JCPL")){
					send("REDY");
				}
				msg = receive();
			}
		}
	}

	public void assignFirstSCHD() {
		String[] msgArray = msg.split(" ");
		if (msgArray[0].equals("JOBN")){
			send("GETS Capable " + msgArray[4] + " " + msgArray[5] + " " + msgArray[6]);
			msg = receive(); //send back eg: DATA 5 123 (5 lines of data at 123 length each)
			//after recieving DATA X i need to loop X times and record the data 
			String[] getsCapableRes = msg.split(" ");
			int numberOfDataLinesFromGetsCapable = Integer.parseInt(getsCapableRes[1]);
			ArrayList<String> serversAvailFromGetsCapable = new ArrayList<String>();
			send("OK");

			for (int i = 0; i < numberOfDataLinesFromGetsCapable; i++){
				msg = receive();
				serversAvailFromGetsCapable.add(msg);
			}
			send("OK");
			msg = receive();
			String[] whichServerToSendTo = serversAvailFromGetsCapable.get(0).split(" ");
			send("SCHD " + msgArray[2] + " " + whichServerToSendTo[0] + " " + whichServerToSendTo[1]);
			msg = receive();
		}
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
				serrArrList.add(temp);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	public static void main(String args[]){
		AppStage2 app = new AppStage2("127.0.0.1", 50000);
		app.run();
	}
}
