import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;


class myClient {
	public static void main(String args[]) throws Exception{
		Socket s = new Socket("127.0.0.1", 50000);
		BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream()));
		DataOutputStream dout = new DataOutputStream(s.getOutputStream());
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		//array of servers to keep
		Server[] serArr = new Server[1];
		String inHandShake="";
		String userName = System.getProperty("user.name");
		//Start of Handshake protocol
		dout.write("HELO\n".getBytes());
		dout.flush();
		System.out.println("Client: HELO");
		inHandShake = din.readLine()+"\n";
		System.out.println("Server: " + inHandShake);
		
		dout.write(("AUTH " + userName + "\n").getBytes());
		dout.flush();
		System.out.println("Client: AUTH");
		inHandShake = din.readLine()+"\n";
		System.out.println("Server: " + inHandShake);
		

		//parsing through xml file and finding largest server
		File sysXML = new File("ds-system.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dB = dbFactory.newDocumentBuilder();
		Document doc = dB.parse(sysXML);

		doc.getDocumentElement().normalize();
		NodeList servers = doc.getElementsByTagName("server");
		serArr = new Server[servers.getLength()];
		int nServers = 0;
		String largestServerType = "";
		int maxCores = 0;
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
			
			//if (serArr[i].nCore > maxCores){
			//	maxCores = serArr[i].nCore;
			//	nServers = serArr[i].limit;
			//	largestServerType = serArr[i].type;
			//}
			
			//start 
			if (c > maxCores) {
				maxCores = c;
				nServers = l;
				largestServerType = t;
			}
			//finito
		}


		//from pseudo code from line 15 - 21
		dout.write("REDY\n".getBytes());
		dout.flush();
		System.out.println("Client: REDY");
		//recieve after first REDY
		inHandShake = din.readLine();
		//for debugging
		System.out.println("Server: " + inHandShake);
		//end of debug
		int serverCounter = 0;

		//TO DO
		//need to handle when inHandShake is JCPL fml 
		while (!inHandShake.equals("NONE")){
			if (inHandShake.equals("OK")) {
				dout.write("REDY\n".getBytes());
				dout.flush();
				System.out.println("Client: REDY");
				inHandShake = din.readLine();
			}

			//testing again LOL
			System.out.println("Server: " + inHandShake);
			
			//schedule job if readLine() is JOBN
			String[] jobRes = inHandShake.split(" ");
			int jobID = Integer.parseInt(jobRes[2]);
			if (jobRes[0].equals("JOBN")){
				dout.write(("SCHD " + jobID + " " + largestServerType+ " " + serverCounter + "\n").getBytes());
				dout.flush();
				System.out.println("Job has been SCHD with jobID: " + jobID);
				serverCounter++;
			}
			if (serverCounter >= nServers){
				//resetting counter to 0 for LLR
				System.out.println("serverCounter: " + serverCounter + "nServers: " +nServers);
				System.out.println("max # of servers is used, setting back to 0");
				serverCounter = 0;
			}
			inHandShake = din.readLine();
			System.out.println("Server: " + inHandShake);
		}
		//dout.write("QUIT\n".getBytes());
		//inHandShake = din.readLine();
		//if (inHandShake.equals("QUIT")){
		//	dout.close();
		//	din.close();
		//	s.close();
		//}
		dout.close();
		din.close();
		s.close();
	}
}
