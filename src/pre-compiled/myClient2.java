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
		//Start of Handshake protocol
		dout.write("HELO\n".getBytes());
		dout.flush();
		System.out.println("Sent HELO");
		inHandShake = din.readLine()+"\n";
		System.out.println("Server says: " + inHandShake);
		
		dout.write("AUTH alik\n".getBytes());
		dout.flush();
		System.out.println("Sent AUTH");
		inHandShake = din.readLine()+"\n";
		System.out.println("Server says: " + inHandShake);
		

		//TO DO:
		//do a loop to find the largest server + how many?
		//read xml file ???
		File sysXML = new File("ds-system.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dB = dbFactory.newDocumentBuilder();
		Document doc = dB.parse(sysXML);

		doc.getDocumentElement().normalize();
		NodeList servers = doc.getElementsByTagName("server");
		System.out.println("Number of servers on NodeList is: " + servers.getLength());
		serArr = new Server[servers.getLength()];
		int nServers = 0;
		for (int i = 0; i < servers.getLength(); i++){
			int maxCores = 0;
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
			System.out.println("Core count is: " + serArr[i].nCore);
			System.out.println("Limit count is: " + nServers);
			if (serArr[i].nCore > maxCores){
				maxCores = serArr[i].nCore;
				nServers = serArr[i].limit;
			}
			
		}


		//from pseudo code from line 15 - 21
		dout.write("OK\n".getBytes());
		dout.flush();
		System.out.println("Client sent: OK");
		while (din.readLine() != "NONE"){
			dout.write("REDY\n".getBytes());
			dout.flush();
			System.out.println("Client says: REDY");
			inHandShake = din.readLine() + "\n";
			System.out.println("Server says: " + inHandShake);
		}

		String str="", str2="";
		while (!str.equals("quit")) {
			str = br.readLine()+"\n";
			dout.write(str.getBytes());
			dout.flush();
			str2 = din.readLine();
			System.out.println("Server says: "+ str2);
		}
		dout.close();
		s.close();
	}
}
