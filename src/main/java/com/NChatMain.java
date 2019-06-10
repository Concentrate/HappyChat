package com;

import com.nettysocket.pratise.NChatServerMain;
import com.nettysocket.pratise.client.NClientMain;

import java.util.Scanner;

/**
 * Created by liudeyu on 2019/6/10.
 */
public class NChatMain {

    public static void main(String[] argv) {
        System.out.println("input the type of start，1：server,2:client,others:exit");
        int type = 0;
        Scanner scanner = new Scanner(System.in);
        type = scanner.nextInt();
        switch (type) {
            case 1:
                NChatServerMain.main(null);
                break;
            case 2:
                NClientMain.main(null);
                break;
            default:
                break;
        }
        System.out.println("exit now");
    }
}
