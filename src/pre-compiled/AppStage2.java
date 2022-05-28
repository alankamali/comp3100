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


	//for stage 2 testing below might have to delete later
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

		assignFirstSCHD();
		newAlgo();

		quit();
	}

	/**
	Implenet new algorithm such that it performs better than baseline algorithms in general

	I have no idea what im doing yet so this is just testing stage

	what I want to do:
		1) as jobs come in, give to the first server which can take it.
			- if server has waiting jobs, go to next server from getscapable
		2) as soon as limit is reached switch to second biggest server, keep going till all servers are exhausted
			* if i have an arraylist of servers from biggest to small, then i can iterate through them?
		3) if all servers have running jobs, put next job on the biggest server with shortest estWaitingTime


	 */
	//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX START OF TESTING XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	
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

				//JOBN FORMAT:
				//	JOBN submitTime jobID estRuntime core memory disk

				if (msgRes[0].equals("JOBN")) {
					send("GETS Capable " + msgRes[4] + " " + msgRes[5] + " " + msgRes[6]);

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
					ArrayList<String[]> whichServerToSendTo = new ArrayList<String[]>();

					for (String s : serversAvailFromGetsCapable){
						String[] temp = s.split(" ");
						whichServerToSendTo.add(temp);
					}


					System.out.println("msgRes: " + java.util.Arrays.toString(msgRes));


					send("OK");
					msg = receive(); //this should be "."
					/**
					for servers S in whichServer
						if server is booting
							add to servers to run lstj on
						if none booting
							schd to first avail 
					 */

					//LSTJ response: 	
					//jobID jobState submitTime startTime estRunTime core memory disk
					// 0       1         2         3         4        5     6     7
					
					//Sort the servers in ascending order of cores
					// Collections.sort(whichServerToSendTo, new Comparator<String[]>(){
					// 	public int compare (String[] s1, String[] s2){
					// 		return s1[4].compareTo(s2[4]);
					// 	}
					// });


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




					for (String[] s : whichServerToSendTo){
						System.out.println("whichServSend2: " + java.util.Arrays.toString(s));
					}

					Boolean capableServerFound = false;
					if (!capableServerFound){
						for (String[] server : whichServerToSendTo){
							System.out.println(Integer.parseInt(server[4]) + " this is the number of cores of server[4");
							System.out.println(Integer.parseInt(msgRes[4]) + " this is the number of cores of msgRes[4");
							if ((Integer.parseInt(server[4]) >= Integer.parseInt(msgRes[4])) && Integer.parseInt(server[5]) >= Integer.parseInt(msgRes[5]) && Integer.parseInt(server[6]) >= Integer.parseInt(msgRes[6]) && Integer.parseInt(server[7]) < 2){
								send("SCHD " + msgRes[2] + " " + server[0] + " " + server[1]);
								capableServerFound = true;

								break;
							} 
						}

					} 
					if (!capableServerFound){
						System.out.println("entered else if for LSTJ ");
						send("LSTJ " + whichServerToSendTo.get(0)[0] + " " + Integer.parseInt(whichServerToSendTo.get(0)[1]));
						msg = receive();
						String[] lstjRes = msg.split(" ");
						ArrayList<String[]> lstjResponseArr = new ArrayList<String[]>();

						send("OK");
						// msg = receive(); //should be .

						for (int i = 0; i < Integer.parseInt(lstjRes[1]); i++){

							msg = receive();
							String[] temp = msg.split(" ");
							lstjResponseArr.add(temp);
							System.out.println(java.util.Arrays.toString(temp) + "temp msg");
						}
						send("OK");
						msg = receive(); // should be .

						//would be n ice to loop thru and find the most efficient server (ie, least amount of remainign time left) to send to
						send("SCHD " + msgRes[2] + " " + whichServerToSendTo.get(0)[0] + " " + Integer.parseInt(whichServerToSendTo.get(0)[1]));
					}

				// example, if:
				// c: Gets Capable x y z
				// s: serverType serverID state curStartTime core memory disk #wJobs #rJobs
				//        0         1        2       3         4    5      6    7       8

				//	JOBN submitTime jobID estRuntime core memory disk
				//    0      1         2    3         4      5    6

				
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


	//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX  END OF TESTING  XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

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
