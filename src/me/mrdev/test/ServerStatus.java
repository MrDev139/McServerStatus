package me.mrdev.test;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerStatus {

    private String hostname;
    private int port;
    private boolean ping;
    private boolean debug;
    private long latency = -1; //will return this if .ping(false)

    ServerStatus(Builder builder) {
        this.hostname = builder.hostname;
        this.port = builder.port;
        this.ping = builder.ping;
        this.debug = builder.debug;
    }

    public long getPing() {
        return latency;
    }

    public String getStatus() {
        try (Socket client = new Socket(hostname, port)) {
            client.setSoTimeout(1000);
            if(debug) {
                System.out.println("Client connected to the server, fetching status...");
                System.out.println("sending Handshake(0x00) packet");
            }
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            DataInputStream in = new DataInputStream(client.getInputStream());

            /*create a buffer byte array with byteoutputstream
             * UTF-8 string prefixed with its size in bytes as a VarInt.
             * at the end we do a length-data packet model(we get packet size from buffer, then actual packet)
             * thats it for 0x00 handshake packet
             * */

            ByteArrayOutputStream packet = new ByteArrayOutputStream();
            DataOutputStream packetOut = new DataOutputStream(packet);
            packetOut.writeByte(0x00); //handshake id
            writeVar(47, packetOut); //47 --> 1.8.x
            writeVar(hostname.length(), packetOut);
            packetOut.writeBytes(hostname); //ig default is normal utf-8?
            packetOut.writeShort(port);
            writeVar(1, packetOut);

            writeVar(packet.size(), out);
            out.write(packet.toByteArray());

            if(debug)System.out.println("Sending Status(0x00) packet");

            writeVar(1, out); //size
            out.writeByte(0x00); //status packet

            int size = readVar(in);
            int ID = in.readByte();

            if(ID != 0x00) {
                System.out.println("Received invalid packet");
                if(debug)System.out.println("Received non 0x00 packet ID: " + ID);
                return null;
            }
            if(debug)System.out.println("Received RESPONSE(0x00) packet :");
            int strLen = readVar(in);
            byte[] str = new byte[strLen];
            in.readFully(str);
            String response = new String(str);

            if(!ping) {
                return response;
            }

            if(debug)System.out.println("Sending a PING(0x01) packet");
            packet.reset();
            packetOut.writeByte(0x01); //PING request
            packetOut.writeLong(69); //69 cuz why not lol, the server will send it back
            writeVar(packet.size(), out);
            out.write(packet.toByteArray());
            long time = System.currentTimeMillis(); //we start counting

            size = readVar(in); //we recycle variable around here
            ID = in.readByte();

            if(ID != 0x01) {
                System.out.println("Received Invalid packet");
                if(debug)System.out.println("Received non 0x01 packet ID: " + ID);
                return null;
            }

            if(in.readLong() != 69) { // cuz it sends back 69 so i hardcode it lul
                System.out.println("What the fuck, Cosmic rays are REAL");
                return null;
            }

            if(debug)System.out.println("received PONG(0x01) packet");
            latency = System.currentTimeMillis() - time;
            return response;


        }catch (Exception e) {
            System.out.println("Couldn't connect to the server");
        }
        return null;
    }

    private void writeVar(int a, DataOutputStream os) throws IOException {
        while((a & 0xFFFFFF80) != 0) { //while int doesn't fit into 7 bits value(all the Fs is filler for int-32 bits)
            os.writeByte((byte) ((a & 0x7F) | 0x80)); //AND 01111111, gets 7 bits, OR 1000000 to MSB = 1
            a >>>= 7; // we shift 7 bits and continue
        }
        os.write((byte) (a)); //last byte
    }

    private int readVar(DataInputStream is) throws IOException {
        byte current = is.readByte();
        int tmp = 0;
        int counter = 0;
        while((current & 0x80) == 0X80) { //if MSB = 1, continue
            tmp |= (current & 0x7F) << counter;
            counter += 7;
            current = is.readByte();
        }
        tmp |= current << counter;
        return tmp;
    }

    public static class Builder {
        private String hostname;
        private int port = 25565; //default port
        private boolean ping = true; //it makes sense cuz in a server list u get ping too right?
        private boolean debug;

        public Builder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder ping(boolean ping) {
            this.ping = ping;
            return this;
        }

        public Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public ServerStatus build() {
            return new ServerStatus(this);
        }

    }
}
