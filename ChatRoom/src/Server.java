import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class Server implements Runnable {
    private ArrayList<ConnectionHandler> connectionsList;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;
    
    
    
    public Server() {
    	connectionsList = new ArrayList<>();
    	done = false;
    }
    
	@Override
	public void run() {
		try {
		    server = new ServerSocket(9999);
		    pool = Executors.newCachedThreadPool();
			while(!done) {
			Socket client = server.accept();
			ConnectionHandler handler = new ConnectionHandler(client);
			connectionsList.add(handler);
			pool.execute(handler);
			}
		} catch (IOException e) {
		   shutDown();
			  
		  }
		}

	
	public void broadcast(String message) {
		for(ConnectionHandler ch : connectionsList) {
			if(ch != null) {
				ch.sendMessage(message);
			}
		}
	}
	
	public void shutDown() {
		try {
		done = true;
		if(!server.isClosed()) {
		server.close();
		}
		for(ConnectionHandler ch : connectionsList) {
			ch.shutdown();
		}
		} catch (IOException e) {
		   e.printStackTrace();
			}
		
	}
	
	public class ConnectionHandler implements Runnable {
		
		private Socket client;
		private BufferedReader in;
		private PrintWriter out;
		private String nickname;

		public ConnectionHandler(Socket client) {
			this.client = client;
		}
		
		
		@Override
		public void run() {
			try {
				out = new PrintWriter(client.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				out.println("===*****Instruction*****===");
				out.println("=== /nick 'yournick' to change your nick ===");
				out.println("=== /q to quit ===");
				out.println("Enter your nickname: ");
				nickname = in.readLine();
				System.out.println(nickname + " connected!");
				broadcast(nickname + " joined the chat" );
				String message;
				while((message = in.readLine()) != null) {
					if(message.startsWith("/nick")) {
						String[] messageSplit = message.split(" ", 2);
						if(messageSplit.length == 2) {
							broadcast(nickname + " renamed to: " + messageSplit[1]);
							System.out.println(nickname + " renamed to: " + messageSplit[1]);
							nickname = messageSplit[1];
							out.println("Nickname changed succesfully! " + nickname);
							
						}
						else {
							out.println("Could not change the nickname");
						}
					}
				else if(!message.startsWith("/nick")) {
						broadcast(nickname + ": " + message);					
					}			
				}
			}catch(Exception e) {
				shutdown();
			}
		}
		public void sendMessage(String message) {
			out.println(message);
		}
		public void shutdown() {
			try {
				in.close();
				out.close();
				if(!client.isClosed()) {
				client.close();
				
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
				
			}
		}
	 public static void main(String[] args) {
		 Server server = new Server();
		 server.run();
	 }
	}
	
	



