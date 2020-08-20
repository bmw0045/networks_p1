import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Scanner;


/** Server Class
 * This amazing program will blow your mind 
 * 
 * @author Brett Waldon, Alex Mershon, Taylor Kyser 
 * @version 07/28/20
 */
public class Server { 

   public static void main(String args[]) throws Exception {
      
      int[] ports = {10004, 10005, 10006, 10007}; //port numbers for our group 
      int port = ports[0];
       
      String lochost = InetAddress.getLocalHost().getHostAddress().trim();  //get IP (to use for Client) 
      System.out.println("\nConnected to: " + lochost); //print Server IP
      
      DatagramSocket udpServerSocket = new DatagramSocket(port); //UDP socket for sending/receiving to/from client 
       
      byte[] receiveData = new byte[256]; //for sending and receiving data
   
      Scanner readFileIn;  //instance of the Scanner class lets it read in files  
      
      while (true) {
            
         System.out.println("Ready to receive..."); // might note be neccessary, idk 
         
         StringBuilder fileContent = new StringBuilder();   //variable for the file data contents
         DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); //Creates a new datagram
         udpServerSocket.receive(receivePacket);
         System.out.println("Receiving the request packet."); //required: receiving statement 
         
         InetAddress IPAddress = receivePacket.getAddress(); //get host IPAddress
         int portRec = receivePacket.getPort(); //get host port number 
            
         String clientData = new String(receivePacket.getData()); //data from client 
            
            //data form file read in
         readFileIn = new Scanner(clientData); 
            
         //readFileIn.next(); //read in the TestFile
         String fileName = readFileIn.next(); //grabs file name
         //readFileIn.close(); //closes the file 
            
         try {
            readFileIn = new Scanner(new File(fileName));  //host requests the file 
         }
         catch (Exception e) {   //catch if file not found 
            System.out.println(e.getClass());
            udpServerSocket.send(setNullPacket(IPAddress, portRec));
            continue;
         }
         while (readFileIn.hasNext()) {  //loop to get each line of content
            fileContent.append(readFileIn.nextLine());
         }
         System.out.println("File: " + fileContent); //print out received file content  
         readFileIn.close(); //close  file
      
      
       //////////////////////////////////////////////////////////////////////////////////////////// 
       // Header Format (outlined in project description) 
         String headerFormat = "HTTP/1.0 200 Document Follows\r\n" 
                  + "Content-Type: text/plain\r\n"
                  + "Content-Length: " + fileContent.length() + "\r\n"
                  + fileContent.length() + "\r\n"+ "\r\n" + fileContent;
       ////////////////////////////////////////////////////////////////////////////////////////////
         
         
         ArrayList<Packet> PacketList = Packet.Segment(headerFormat.getBytes()); //separates the file into packets
         int packetNumber = 0;
         
         for (Packet packet : PacketList) {  //loops through the packets and sends them to the host
            DatagramPacket sendPacket = packet.getDatagramPacket(IPAddress, portRec); 
            udpServerSocket.send(sendPacket); 
            packetNumber++; 
            System.out.println("Sending Packet " + packetNumber + " of " + PacketList.size()); 
         }
         ///transmits a null packet to indicate the end of file 
         udpServerSocket.send(setNullPacket(IPAddress, portRec));
      }
    
    
   }

   /** setNullPacket
     * Creates null packet to send to client to signify end of transmission
     *
     * @param IPAddress 
     * @return a null packet to client 
     * 
     */
   
   private static DatagramPacket setNullPacket(InetAddress IPAddress, int portRec){
      String nullByte = "\0";
      ArrayList<Packet> nullPacket = Packet.Segment(nullByte.getBytes());   
      DatagramPacket nullDatagram = nullPacket.get(0).getDatagramPacket(IPAddress, portRec);
      System.out.println("Sending Null Packet");
      return nullDatagram;
   }

}