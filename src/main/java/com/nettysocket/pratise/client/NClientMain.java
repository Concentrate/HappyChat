package com.nettysocket.pratise.client;

import com.nettysocket.pratise.util.NConstants;

public class NClientMain {
    public static void main(String[] args) {
        NClient nClient=new NClient();
        nClient.start(NConstants.WEBSOCKET_URL);
    }
}
