public class Server {
	public int id;
	public String type;
	public int limit;
	public int bootupTime;
	public float hourlyRate;
	public int nCore;
	public int nMemory;
	public int nDisk;

	public Server(int id, String t, int l, int b, float hr, int c, int m, int d){
		this.id = id;
		this.type = t;
		this.limit = l;
		this.bootupTime = b;
		this.hourlyRate = hr;
		this.nCore = c;
		this.nMemory = m;
		this.nDisk = d;
	}
}
