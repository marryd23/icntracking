package com.example.icntracking;

import java.net.InetAddress;

public interface UDPServerTxListener {
    void onTx(InetAddress address, int port, byte[] frame);
}
