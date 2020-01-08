package backgroundClasses;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JOptionPane;

public class TankServer {
	private ServerSocket ss;
	private Socket s;
	private PrintWriter pw;
	private BufferedReader rR;
	
	public TankServer(int socketPort) throws IOException {
		ss = new ServerSocket(socketPort);
		JOptionPane.showMessageDialog(null,"Server is open on "+InetAddress.getLocalHost());
		s = ss.accept();
		pw = new PrintWriter(s.getOutputStream());
		rR = new BufferedReader(new InputStreamReader(s.getInputStream()));
	}
	public TankServer(int socketPort, String ip) throws IOException {
		s = new Socket(ip, socketPort);
		pw = new PrintWriter(s.getOutputStream());
		rR = new BufferedReader(new InputStreamReader(s.getInputStream()));
	}
	public void print(double d[]){
		String s = "";
		s+=d[0];
		for(int i = 1; i<d.length; i++){
			s+="/"+d[i];
		}
		pw.println(s);
		pw.flush();
	}
	public double[] get() throws IOException{
		
		String s = rR.readLine();
		
		String[] arr = s.split("/");
	
		
		double[] d = new double[arr.length];
		
		for(int i = 0; i<d.length; i++){
			d[i] = Double.parseDouble(arr[i]);
		}
		
		return d;
	}
	public void end(){
		try {
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			ss.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
