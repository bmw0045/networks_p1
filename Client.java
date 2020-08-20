/** Brett Walton, Alex Merson
    COMP 4320
    Project 1
    Due: 7/23/2020
    
    Java file for Client operations.
    
**/

import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.util.ArrayList;

public class Client {

   public static void main(String[] args) throws Exception {
      final String SERVERIP = ""; //specified somewhere?
      int[] ports = {10004, 10005, 10006, 10007};
      int port = ports[0];
      
      BufferedReader inFromUser = 
         new BufferedReader(new InputStreamReader(System.in));
      
      DatagramSocket clientSocket = new DatagramSocket();
      InetAddress IPAddress = InetAddress.getByName(SERVERIP);
      //Create Packets
      byte[] sendData = new byte[256];
      byte[] getData = new byte[256];
      //gremlin placeholder
      int packetNum = 0;
      boolean isDataFinished = false;
      //String sentence = inFromUser.readLine();
      //sendData = sentence.getBytes();
      
      String TestFile = "TestFile.html"; 
      sendData = TestFile.getBytes(); 
      
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
      clientSocket.send(sendPacket);
      System.out.println("Sending request packet....");
      
      DatagramPacket receivePacket; 
      ArrayList<Packet> receivedPackets = new ArrayList<>();
      
      while (!isDataFinished) { 
         receivePacket = new DatagramPacket(getData, getData.length);
         clientSocket.receive(receivePacket); 
            
         Packet createReceivedPacket = Packet.CreateNewPacket(receivePacket);
         packetNum++;
      
         System.out.println("Packet: " + packetNum);
            //checks to see if the data is done sending         
         if (createReceivedPacket.GETPacketsData()[0] == '\0') {
            isDataFinished = true;
            if(receivedPackets.size() == 0){
               System.out.println("Error File Not Found");
               return;
            }  
         }
         else {
            receivedPackets.add(createReceivedPacket);
         }
      }
      
      byte[] reassemblePackets = Packet.ReassemblePacket(receivedPackets);
      String modifiedPacketData = new String(reassemblePackets);
      System.out.println("Packet Data Received from UDPServer:\n" + modifiedPacketData);
      clientSocket.close();
      
      //display to browser
      int index = modifiedPacketData.lastIndexOf("\r\n", 100);
      modifiedPacketData = modifiedPacketData.substring(index);
   
      
   }
}
