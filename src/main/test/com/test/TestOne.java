package com.test;

import com.nettysocket.pratise.NChatServer;
import com.nettysocket.pratise.client.NClient;
import com.nettysocket.pratise.util.NUtil;
import org.junit.Test;

public class TestOne {

    @Test
    public void helloTest() {
        NUtil.logger.info("hello");
        NUtil.logger.debug("this is debug hello");
    }

    public @Test
    void startMySocketServer() {
        NChatServer chatServer = new NChatServer(8090);
        chatServer.start();
    }

    @Test
    public void startChatClient() {
        NClient nClient = new NClient();
        nClient.start();
    }


}
