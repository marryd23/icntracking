package com.example.icntracking;

import java.net.InetAddress;

public interface UDPServerRxListener {
    void onRx(InetAddress address, int port, byte[] frame);
}
