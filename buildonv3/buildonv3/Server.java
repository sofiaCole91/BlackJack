import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * The server that can be run both as a console application or a GUI
 */
public class Server {
	// a unique ID for each connection
	private static int uniqueId;
	// an ArrayList to keep the list of the Client
	private ArrayList<ClientThread> al;

	// to display time
	private SimpleDateFormat sdf;
	// the port number to listen for connection
	private int port;
	// the boolean that will be turned of to stop the server
	private boolean keepGoing;
	swingBlackJack5 superswing;
	String x;







	/*
	 *  server constructor that receive the port to listen to for connection as parameter
	 *  in console
	 */




	public Server(int port) {
		// the port
		this.port = port;
		// to display hh:mm:ss
		sdf = new SimpleDateFormat("HH:mm:ss");
		// ArrayList for the Client list
		al = new ArrayList<ClientThread>();



		}



	public void ServerGameJoinLogic(){
	
	
	}






	public void start() {
		keepGoing = true;
		superswing = new swingBlackJack5();
		/* create socket server and wait for connection requests */
		try
		{
			// the socket used by the server
			ServerSocket serverSocket = new ServerSocket(port);

			// infinite loop to wait for connections
			while(keepGoing)
			{
				// format message saying we are waiting
				InetAddress address = InetAddress.getLocalHost();

				//display(address);


				//debug
				/*
				System.out.println("CURRENT Host ADDRESS: " + address.getHostAddress());
				System.out.println("CURRENT IP ADDRESS: " + address.getAddress());
				System.out.println("CURRENT IP ADDRESS: " + address.getByAddress(address2));
				System.out.println("CURRENT HostName: " + address.getHostName());
				System.out.println("CURRENT Canonical Host Name: " + address.getCanonicalHostName());
				System.out.println("CURRENT hashcode: " + address.hashCode());
				System.out.println("CURRENT : " + serverSocket.getInetAddress());
				*/

				System.out.println("");
				byte[] address2 =address.getAddress();
				display("Server waiting for Clients on port " + port + ".");







				Socket socket = serverSocket.accept();  	// accept connection
				superswing.numberOfPlayers++;
				superswing.resetGame();
				superswing.setup();

				String x=(superswing.GameState);
				broadcast("New Player joined the game, redealing: "+ x);



				System.out.println("Number of players connected: " + superswing.numberOfPlayers);
				// if I was asked to stop
				if(!keepGoing)
					break;
				ClientThread t = new ClientThread(socket);  // make a thread of it
				al.add(t);									// save it in the ArrayList
				t.start();
			}
			// I was asked to stop
			try {
				serverSocket.close();
				for(int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {

					tc.sInput.close();
					tc.sOutput.close();
					tc.socket.close();
					}
					catch(IOException ioE) {
						// not much I can do
					}
				}
			}
			catch(Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		// something went bad
		catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}
    /*
     * For the GUI to stop the server
     */
	protected void stop() {
		keepGoing = false;
		// connect to myself as Client to exit statement
		// Socket socket = serverSocket.accept();
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {
			// nothing I can really do
		}
	}
	/*
	 * Display an event (not a message) to the console or the GUI
	 */


	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;
			System.out.println(time);

	}

	private synchronized void broadcastSpecific(String message, String Username) {

		String messageLf = "C " + message + " EN";
		for(int i = al.size(); --i >= 0;) {
			ClientThread ct = al.get(i);
			if (ct.username == Username){
				if(!ct.writeMsg(messageLf)) {
					al.remove(i);
					superswing.numberOfPlayers--;
					superswing.resetGame();
					superswing.setup();
					x=(superswing.GameState);
					broadcast("Player left the game, redealing: "+ x);
					display("Disconnected Client " + ct.username + " removed from list.");
				}
			}
		}
	}
	/*
	 *  to broadcast a message to all Clients Obselete?
	 */
	private synchronized void broadcast(String message) {
		// add HH:mm:ss and \n to the message
		String time = sdf.format(new Date());
		String messageLf = message + "\n";
			//System.out.print(messageLf);//display messages locally on server.
		// we loop in reverse order in case we would have to remove a Client
		// because it has disconnected
		for(int i = al.size(); --i >= 0;) {
			ClientThread ct = al.get(i);



			// try to write to the Client if it fails remove it from the list
			if(!ct.writeMsg(messageLf)) { //writeMsg Writes to all clients
				al.remove(i);
				superswing.numberOfPlayers--;
				superswing.resetGame();
				superswing.setup();
				x=(superswing.GameState);
				broadcast("Player left the game, redealing: "+ x);
				display("Disconnected Client " + ct.username + " removed from list.");
			}
		}
	}

	private void ChangeServerValue(String arrayToBeChanged, int target, int value) {



	}


	// for a client who logoff using the LOGOUT message
	synchronized void remove(int id) {
		// scan the array list until we found the Id
		for(int i = 0; i < al.size(); ++i) {
			ClientThread ct = al.get(i);
			// found it
			if(ct.id == id) {
				al.remove(i);
				return;
			}
		}
	}

	/*
	 *  To run as a console application just open a console window and:
	 * > java Server
	 * > java Server portNumber
	 * If the port number is not specified 1500 is used
	 */







	public static void main(String[] args) {



		// start server on port 1500 unless a PortNumber is specified
		int portNumber = 1500;
		switch(args.length) {
			case 1:
				try {
					portNumber = Integer.parseInt(args[0]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
					System.out.println("Usage is: > java Server [portNumber]");
					return;
				}
			case 0:
				break;
			default:
				System.out.println("Usage is: > java Server [portNumber]");
				return;

		}
		// create a server object and start it
		Server server = new Server(portNumber);
		server.start();
	}

	/** One instance of this thread will run for each client */
	class ClientThread extends Thread {



		// the socket where to listen/talk
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		// my unique id (easier for deconnection)
		int id;
		// the Username of the Client
		String username;
		// the only type of message a will receive
		ChatMessage cm;
		// the date I connect
		String date;

		// Constructor
		ClientThread(Socket socket) {
			// a unique id
			id = ++uniqueId;
			this.socket = socket;
			/* Creating both Data Stream */
			System.out.println("Thread trying to create Object Input/Output Streams");
			try
			{
				// create output first
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				// read the username
				username = (String) sInput.readObject();
				display(username + " just connected.");
			}
			catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			// have to catch ClassNotFoundException
			// but I read a String, I am sure it will work
			catch (ClassNotFoundException e) {
			}
            date = new Date().toString() + "\n";
		}

		// what will run forever
		public void run() {










			// to loop until LOGOUT
			boolean keepGoing = true;
			while(keepGoing) {
				// read a String (which is an object)
				try {
					cm = (ChatMessage) sInput.readObject();
				}
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				// the messaage part of the ChatMessage

				String message = cm.getMessage();
				String arrayname = cm.getArrayname();
				int value = cm.getValue();
				int target = cm.getTarget();




				// Switch on the type of message receive
				switch(cm.getType()) {

				case ChatMessage.MESSAGE:
					broadcast(username + ": " + message);
					break;

				case ChatMessage.SETUSERNAME:
					String oldusername = username;
					username = message.substring(12);
					display(oldusername + " changed username to: "+ username );
					break;


				case ChatMessage.LOGOUT:
					superswing.numberOfPlayers--;
					superswing.resetGame();
					superswing.setup();
					x=(superswing.GameState);
					broadcast("Player left the game, redealing: "+ x);
					display(username + " disconnected with a LOGOUT message.");
					keepGoing = false;
					break;

				case ChatMessage.DRAW:

					x=superswing.ShowHand(id-1);

					broadcastSpecific(x,username);

					break;

				case ChatMessage.RETRIEVE:

					//broadcast(username);
					//System.out.println(id +" requested a refresh");

					superswing.game();
					x=(superswing.GameState);
					broadcast("\n" + x);
					String GameStateString =getState(username);
					

					break;



				case ChatMessage.CHANGE:


					superswing.event(id,value);

					x=(superswing.GameState);
					broadcast("\n" + x);


					break;
					
				case ChatMessage.LEAVE:

					System.out.println(id +" left");
					superswing.numberOfPlayers--;
					superswing.resetGame();
					superswing.setup();
					x=(superswing.GameState);
					broadcast("Player left the game, redealing: "+ x);
					display("Disconnected Client " + id + " removed from list.");


					break;


				case ChatMessage.WHOISIN:

					writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
					// scan al the users connected
					for(int i = 0; i < al.size(); ++i) {
						ClientThread ct = al.get(i);
						writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
					}
					break;
				}
			}

			// remove myself from the arrayList containing the list of the
			// connected Clients
			remove(id);
			close();
		}

		// try to close everything
		private void close() {
			// try to close the connection
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		/*
		 * Write a String to the Client output stream
		 */
		private String getState(String gameID) {

			 return superswing.GameState;
		}



		private boolean writeMsg(String msg) {

			// if Client is still connected send the message to it
			if(!socket.isConnected()) {
				close();
				return false;
			}
			// write the message to the stream
			try {
				sOutput.writeObject(msg);
			}
			// if an error occurs, do not abort just inform the user
			catch(IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
	}
}


