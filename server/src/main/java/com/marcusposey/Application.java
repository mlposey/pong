package com.marcusposey;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Application {
    public static void main(String[] args) throws Exception {
        final int kPort = 8001;

        World world = new World();
        world.beginLoop();

        GameServer s = new GameServer(kPort, world);
        s.start();

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String command = input.readLine();
            if(command.equals("exit")) {
                s.stop(1000);
                world.nuke();
                break;
            }
        }
    }
}
