import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class contains the methods used for packets including 
 * - creating a packet 
 * - getting packet data
 * - get/set the packet header 
 * - get/set the packet segment number 
 * - segmentation 
 * - re-assembly
 * and the check value junction
 * 
 * @author Taylor Kyser, Alex Mershon, and Brett Walton
 * @date 7/27/2020
 */

class Packet {

    /////Package Header/////
    //Constants
    private static final String HEADER_CHECKSUM = "CheckSum";
    private static final String HEADER_SEGEMENT_NUM = "SegmentNumber";

    private static final int HEADER_LINES = 4;  
    private static int PACKET_SIZE = 256;  
    private static final int PACKET_DATA_SIZE = PACKET_SIZE - HEADER_LINES; 
    private byte[] DataPackage;
    
    private Map<String, String> HeaderOfPacket;

    //Constructor
    private Packet() {
        
        DataPackage = new byte[PACKET_SIZE];

        HeaderOfPacket = new HashMap<>();
    }

    public enum HEADER_ELEMENTS {
        SEGMENT_NUMBER,
        CHECKSUM
    }
     
    static byte[] ReassemblePacket(ArrayList<Packet> PacketList) {
        int ttlSize = 0;
        
        for (Packet thisPacketList : PacketList) ttlSize += thisPacketList.getPacketsDataSize();
        
        byte[] packetReturn = new byte[ttlSize];
        int countReturn = 0;
        for (int i = 0; i < PacketList.size(); i++) {
            
            for (Packet LocatePacket : PacketList) {
            	
                String segmentNum = LocatePacket.getHeaderVal(HEADER_ELEMENTS.SEGMENT_NUMBER);
                
                if (Integer.parseInt(segmentNum) == i) {
                    for (int k = 0; k < LocatePacket.getPacketsDataSize(); k++)
                        packetReturn[countReturn + k] = LocatePacket.GETPacketsData(k);
                    countReturn += LocatePacket.getPacketsDataSize();
                    break;
                }
            }
        }
        
        return packetReturn;
    }

    //Segment is called by the UDPServer to break the packets into segments
    static ArrayList<Packet> Segment(byte[] packetBytes) {
    	
        ArrayList<Packet> packetReturn = new ArrayList<>();
        
        int fileSize = packetBytes.length;
        
        if (fileSize == 0) {
            throw new IllegalArgumentException("File Empty");
        }
        int bytesCount = 0;
        int segmentNum = 0;
        
        while (bytesCount < fileSize) {
            Packet nxtPacket = new Packet();
            byte[] nxtPacketsData = new byte[PACKET_DATA_SIZE];
            //read in amount of data size 256 (total) - 4 (header) = 252 (data)
            int readInDataSize = PACKET_DATA_SIZE;             
            if (fileSize - bytesCount < PACKET_DATA_SIZE) {
                readInDataSize = fileSize - bytesCount;
            }
            
            int j = bytesCount;
            for (int i = 0; i < readInDataSize; i++) {
                nxtPacketsData[i] = packetBytes[j];
                j++;
            }

            nxtPacket.setPacketsData(nxtPacketsData);
              
            nxtPacket.setHeaderValue(HEADER_ELEMENTS.SEGMENT_NUMBER, segmentNum + "");

            //Checksum errors
            String CheckSumPacket = String.valueOf(Packet.CheckSum(nxtPacketsData));
            nxtPacket.setHeaderValue(HEADER_ELEMENTS.CHECKSUM, CheckSumPacket);
            packetReturn.add(nxtPacket);

            segmentNum++;

            bytesCount = bytesCount + readInDataSize;
        }

        return packetReturn;
    }

    //Creates a new packet
    static Packet CreateNewPacket(DatagramPacket packet) {
        Packet newPacket = new Packet();
        ByteBuffer bufferByte = ByteBuffer.wrap(packet.getData()); 
        newPacket.setHeaderValue(HEADER_ELEMENTS.SEGMENT_NUMBER, bufferByte.getShort() + ""); 
        newPacket.setHeaderValue(HEADER_ELEMENTS.CHECKSUM, bufferByte.getShort() + ""); 
        byte[] PacketsData = packet.getData(); 
        byte[] remains = new byte[PacketsData.length - bufferByte.position()]; 
        
        System.arraycopy(PacketsData, bufferByte.position(), remains, 0, remains.length); 
        newPacket.setPacketsData(remains); 
        return newPacket; 
    }

    //////////PACKAGE HEADER METHODS//////////

    
    static short CheckSum(byte[] packetBytes) {
        long value = 0;
        int packetBytesLength = packetBytes.length;
        int count = 0;
        while (packetBytesLength > 1) {  
            value += ((packetBytes[count]) << 8 & 0xFF00) | ((packetBytes[count + 1]) & 0x00FF);
            
            if ((value & 0xFFFF0000) > 0) {
                value = ((value & 0xFFFF) + 1);
            }
            
            count += 2;
            packetBytesLength -= 2;
        }

        if (packetBytesLength > 0) {
            value += (packetBytes[count] << 8 & 0xFF00);
            if ((value & 0xFFFF0000) > 0) { 
                value = ((value & 0xFFFF) + 1);
            }
        }
        
        return (short) (~value & 0xFFFF);
    }


    //Get Header Element Values
    String getHeaderVal(HEADER_ELEMENTS HeaderElements) {
        switch (HeaderElements) {
            case SEGMENT_NUMBER:
                return HeaderOfPacket.get(HEADER_SEGEMENT_NUM);
            case CHECKSUM:
                return HeaderOfPacket.get(HEADER_CHECKSUM);
            default:
                throw new IllegalArgumentException("Something is broken... bad broken");
        }
    }


    ////////////PACKAGE DATA METHODS////////////
    //SET header element pairs
    private void setHeaderValue(HEADER_ELEMENTS HeaderElements, String HeaderValue) {
        switch (HeaderElements) {
            case SEGMENT_NUMBER:
                HeaderOfPacket.put(HEADER_SEGEMENT_NUM, HeaderValue);
                break;
            case CHECKSUM:
                HeaderOfPacket.put(HEADER_CHECKSUM, HeaderValue);
                break;
            default:
                throw new IllegalArgumentException("Something is broken... bad broken");
        }
    }

    //Get at packet index
    private byte GETPacketsData(int index) {
        if (index >= 0 && index < DataPackage.length)
            return DataPackage[index];
        throw new IndexOutOfBoundsException(
                "GET PACKET DATA INDEX OUT OF BOUNDS EXCEPTION: index = " + index);
    }
      
    byte[] GETPacketsData() {
        return DataPackage;
    }

    int getPacketsDataSize() {
        return DataPackage.length;
    }


    //Takes an array of bytes to be set as the data segment.
    //If the Packet contains data already, the data is overwritten.
 
    private void setPacketsData(byte[] tooSet) throws IllegalArgumentException {
        int argueSize = tooSet.length;
        if (argueSize > 0) {
            DataPackage = new byte[argueSize];
            System.arraycopy(tooSet, 0, DataPackage, 0, DataPackage.length);
        } else
            throw new IllegalArgumentException(
                    "ILLEGAL ARGUEMENT EXCEPTION-SET PACKET DATA: tooSet.length = " + tooSet.length);
    }

    //returns packet as a datagram packet
    DatagramPacket getDatagramPacket(InetAddress i, int port) {
        byte[] setData = ByteBuffer.allocate(256)
                .putShort(Short.parseShort(HeaderOfPacket.get(HEADER_SEGEMENT_NUM)))
                .putShort(Short.parseShort(HeaderOfPacket.get(HEADER_CHECKSUM)))
                .put(DataPackage)
                .array();

        return new DatagramPacket(setData, setData.length, i, port);
    }

    
}