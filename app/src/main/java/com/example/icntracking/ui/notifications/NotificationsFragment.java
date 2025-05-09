package com.example.icntracking.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.icntracking.MainActivity;
import com.example.icntracking.NetworkKey;
import com.example.icntracking.UDPServer;
import com.example.icntracking.UDPServerRxListener;
import com.example.icntracking.ZMeshFrameHelper;
import com.example.icntracking.ZMeshUtils;
import com.example.icntracking.databinding.FragmentNotificationsBinding;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class NotificationsFragment extends Fragment {
    private final static String TAG = MainActivity.class.getSimpleName();

    private FragmentNotificationsBinding binding;
    static final int ZMESH_UDP_PORT = 22080;
    UDPServer udpServerThread = null;
    Thread interestSubscribeThread;
    int interestSubscribeIntervalMs = 60000;


    // Global config
    String contentStoreHostName = "2.106.185.92";
    // String contentStoreHostName = "192.168.1.248";
    int contentStoreHostPort = 42208;
    int nwkKeyId = 0;
    // String networkKey = "28c32b54abdf503609ea5028440418cd";
    long netId = ZMeshFrameHelper.ZMESH_NETID_LOCALNET; //65552;
    NetworkKey nwkKey = new NetworkKey("Key0", "28c32b54abdf503609ea5028440418cd", NetworkKey.MAC_METHOD_AES128CMAC);
    byte[] contentName = ZMeshUtils.hexToBytes("d7c2aed630b6");
    String contentNameEncKey = "6d60b57fc5e0d6326f2d1cc618ccc562";
    String contentNameEncIv = "01f13097b340a79e75";


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        binding.zmeshOutputTextView.setMovementMethod(new ScrollingMovementMethod());


        // Setup UDP Server Switch listener
        binding.udpServerSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TextView zmeshOutputTextView = binding.zmeshOutputTextView;
                if (binding.udpServerSwitch.isChecked() && udpServerThread == null) {
                    udpServerThread = new UDPServer(ZMESH_UDP_PORT, udpServerRxListener);
                    udpServerThread.start();
                    String zmOutput = "Z-Mesh UDP server running\n";
                    zmeshOutputTextView.append(zmOutput);
                } else {
                    String zmOutput = "Stopping UDP server...\n";
                    zmeshOutputTextView.append(zmOutput);
                    stopUDPServer();
                }
            }
        });
        // Setup InterestSubscribe Switch listener
        binding.interestSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TextView zmeshOutputTextView = binding.zmeshOutputTextView;
                Log.i(TAG, "interestSubscribe clicked: " + binding.interestSubscribe.isChecked());
                if (binding.interestSubscribe.isChecked()) {
                    // Start interest sending thread
                    interestSubscribeThread = new Thread(new Runnable() {
                        @Override public void run() {
                            int iterationTime = 0;
                            try {
                                while(binding.interestSubscribe.isChecked()) {
                                    if (iterationTime >= interestSubscribeIntervalMs) {
                                        sendInterest(true);
                                        iterationTime = 0;
                                    }
                                    iterationTime += 1000;
                                    Thread.sleep(1000);
                                }
                            } catch (IOException | InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            Log.i(TAG, "interestSubscribeThread finished!");
                        }
                    });
                    interestSubscribeThread.start();
                    String zmOutput = "Interest subscribe sent!\n";
                    zmeshOutputTextView.append(zmOutput);
                }
            }
        });

        // Setup button listeners
        View.OnClickListener btnListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Interest buttons
                    if (v.getId() == binding.sendInterestBtn.getId()) {
                        Log.i(TAG, "sendInterestBtn clicked");
                        // Send Interest
                        sendInterest(false);
                    } else if (v.getId() == binding.clearOutputBtn.getId()) {
                        binding.zmeshOutputTextView.setText("");
                        Log.i(TAG, "clearOutputBtn clicked");
                    } else {
                        throw new IllegalStateException("Unexpected value: " + v.getId());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        Button sendInterestBtn = (Button) binding.sendInterestBtn;
        sendInterestBtn.setOnClickListener(btnListener);
        Button clearOutputBtn = (Button) binding.clearOutputBtn;
        clearOutputBtn.setOnClickListener(btnListener);

        return root;
    }

    /**
     * @brief Fundtion for controlling (debug) output
     * @param msg
     */
    void zmeshOutput(int prio, String msg) {
        Log.println(prio, TAG, msg);
        // Write to view
        if (binding.zmeshDebug.isChecked() || prio == Log.INFO) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.zmeshOutputTextView.append(msg + "\n");
                }
            });
        }
    }

    void stopUDPServer() {
        if(udpServerThread != null) {
            Log.i(TAG, "Stopping UDP Server...");
            udpServerThread.setRunning(false);
            udpServerThread = null;
        }
    }

    @Override
    public void onDestroyView() {
        stopUDPServer();
        super.onDestroyView();
        binding = null;
    }

    // Button listener

    // Start Z-Mesh UDP server
    UDPServerRxListener udpServerRxListener = new UDPServerRxListener() {
        @Override
        public void onRx(InetAddress address, int port, byte[] frame) {
            String msg = "Got frame ("+frame.length+"): " + ZMeshUtils.bytesToHex(frame);
            zmeshOutput(Log.DEBUG, "Got frame ("+frame.length+"): " + ZMeshUtils.bytesToHex(frame));

            // Check MAC
            byte[] cmacFrame = ZMeshFrameHelper.getMac(frame);
            byte[] cmacGenerated = ZMeshFrameHelper.verifyZMeshCmac(frame, ZMeshUtils.hexToBytes(nwkKey.getKey()));
            if (!Arrays.equals(cmacFrame, cmacGenerated)) {
                String g = ZMeshUtils.bytesToHex(cmacGenerated);
                String f = ZMeshUtils.bytesToHex(cmacFrame);
                zmeshOutput(Log.ERROR, "Frame CMAC invalid: Frame: " + f + ", generated: " + g);
                return;
            }
            // Dig out common stuff
            byte[] name = ZMeshFrameHelper.getName(frame);
            int fseq = ZMeshFrameHelper.getFseq(frame);
            byte[] payload = ZMeshFrameHelper.getPayload(frame);

            if (name == null || name.length == 0 || fseq < 0) {
                zmeshOutput(Log.WARN, "Name (" + ZMeshUtils.bytesToHex(name) + ") or fseq (" + fseq + ") error");
                return;
            }

            // Determine request type and forward to handler
            int packetType = ZMeshFrameHelper.getFctrlPacketType(frame);
            zmeshOutput(Log.DEBUG, "PacketType: " + packetType + " from: " + netId +"/"+ ZMeshUtils.bytesToHex(name) + ", fseq: " + fseq);
            switch (packetType) {
                case ZMeshFrameHelper.PACKET_TYPE_INTEREST:
                    /* Send it to the content store */
                    handleInterest(netId, name, fseq, frame, payload);
                    break;

                case ZMeshFrameHelper.PACKET_TYPE_CONTENT:
                    /* Send it to the Content Store */
                    handleContent(netId, name, fseq, frame);
                    break;

                case ZMeshFrameHelper.PACKET_TYPE_INTEREST_RETURN:

                    zmeshOutput(Log.ERROR, "PACKET_TYPE_INTEREST_RETURN not implemented!");
                    break;

                case ZMeshFrameHelper.PACKET_TYPE_CONTENT_ANNOUNCEMENT:
                    zmeshOutput(Log.ERROR, "PACKET_TYPE_CONTENT_ANNOUNCEMENT not implemented!");
                    break;

                default:
                    zmeshOutput(Log.WARN, "Unknown packet type: " + packetType);
                    break;
            }
        }
    };

    private void sendInterest(boolean subscribe) throws IOException {
        zmeshOutput(Log.INFO, "Sending interest (subscribe="+subscribe+")");
        // Create an interest Z-Mesh frame
        long timeNow = ZMeshUtils.getEpocTimeMs();
        short interestExpireSecs = 2;
        int fseq = ZMeshFrameHelper.INTEREST_FSEQ_LAST;
        if (subscribe) {
            interestExpireSecs = 60;
            fseq = ZMeshFrameHelper.INTEREST_FSEQ_SUBSCRIBE;
        }
        byte[] interestPayloadByteArr = ZMeshFrameHelper.generateInterestPayload(timeNow, interestExpireSecs);
        byte[] zmeshFrame = ZMeshFrameHelper.makeFrame(ZMeshFrameHelper.ZMESH_TTL_MAX, netId, contentName, ZMeshFrameHelper.PACKET_TYPE_INTEREST, fseq, nwkKeyId, nwkKey, interestPayloadByteArr);
        InetAddress forwarderAddress = InetAddress.getByName(contentStoreHostName);
        udpServerThread.tx(forwarderAddress, contentStoreHostPort, zmeshFrame);
    }

    private void handleContent(long netId, byte[] name, int fseq, byte[] frame) {
        byte[] contentPayload = ZMeshFrameHelper.getPayload(frame);
        byte[] payloadPLainText = ZMeshUtils.decryptBytes(contentPayload, contentPayload.length, fseq, contentNameEncKey, contentNameEncIv);

        String message;
        if (payloadPLainText.length == ZMeshFrameHelper.ZMESH_TIMESTAMP_LEN + 4) {
            ByteBuffer byteBuf = ByteBuffer.wrap(payloadPLainText);
            long timestamp = ZMeshUtils.getPayloadTimestamp(byteBuf);
            float value = byteBuf.getFloat();
            zmeshOutput(Log.INFO, "Received Content: " + netId+"/"+ZMeshUtils.bytesToHex(name) + " FSEQ="+fseq + " ts="+timestamp+" value="+value);
        } else {
            zmeshOutput(Log.INFO, "Received Content: " + netId+"/"+ZMeshUtils.bytesToHex(name) + " FSEQ="+fseq + " frame ("+frame.length+"): "+ZMeshUtils.bytesToHex(frame));
        }
    }

    private void handleInterest(long netId, byte[] name, int fseq, byte[] frame, byte[] payload) {
        zmeshOutput(Log.DEBUG, "Received Interest: " + netId+"/"+ZMeshUtils.bytesToHex(name) + " FSEQ="+fseq + " frame ("+frame.length+"): "+ZMeshUtils.bytesToHex(frame));
        // byte[] interstPayload = ZMeshFrameHelper.getPayload(frame);
    }

}