package com.example.icntracking;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;

public class UDPServer extends Thread {
    private final static String TAG = MainActivity.class.getSimpleName();

    int serverPort;
    DatagramSocket socket;
    UDPServerRxListener rxListener;

    boolean running;

    public UDPServer(int serverPort, UDPServerRxListener rxListener) {
        super();
        this.serverPort = serverPort;
        this.rxListener = rxListener;
    }

    public void setRunning(boolean running){
        this.running = running;
    }

    public void tx(InetAddress address, int port, byte[] frame) throws IOException {
        DatagramPacket packet = new DatagramPacket(frame, frame.length, address, port);
        socket.send(packet);
    }

    @Override
    public void run() {

        running = true;

        try {
            Log.i(TAG,"Starting UDP Server");
            socket = new DatagramSocket(serverPort);

            while(running){
                byte[] buf = new byte[256 + ZMeshFrameHelper.ZMESH_UDP_PAYLOAD_LEN];

                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);     //this code block the program flow
                // Call callback
                int frameLen = packet.getLength();
                byte[] frame = new byte[frameLen];
                System.arraycopy(packet.getData(), 0, frame, 0, frameLen);
                rxListener.onRx(packet.getAddress(), packet.getPort(), frame);
            }

            Log.e(TAG, "UDP Server ended");

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(socket != null){
                socket.close();
                Log.e(TAG, "socket.close()");
            }
        }
    }

}
