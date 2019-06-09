package com.nettysocket.pratise;

public class NChatServerMain {

    public static void main(String[] args) {
        NChatServer server=new NChatServer(8090);
        server.start();

    }


}
