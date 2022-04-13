package edu.neu.coe.csye6225.webapp.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CmdRunner {
    public static String run(String cmd) throws Exception{
        System.out.println(String.format("Running command: %s", cmd));

        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        String s;
        String result = "";
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
            result = result.concat(s);
        }

        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
            throw new Exception(s);
        }

        return result;
    }
}
