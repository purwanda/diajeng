/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diajeng;

/**
 *
 * @author kanjeng
 */
import java.io.*;
import java.text.*;
import java.util.*;

public class logging {
    public static String logFile = "msglog.log";
    private final static DateFormat df = new SimpleDateFormat ("yyyy.MM.dd  hh:mm:ss ");

    private logging() { }
    
    public static void setLogFilename(String filename) {
        logFile = filename;               
    }
    
    public static void write(String msg) {
        write(logFile, msg);
    }
    
    public static void write(Exception e) {
        write(logFile, stack2string(e));
    }

    public static void write(String file, String msg) {
        try {
            Date now = new Date();
            String currentTime = logging.df.format(now); 
            FileWriter aWriter = new FileWriter(file, true);
            aWriter.write(currentTime + " " + msg 
                    + System.getProperty("line.separator"));
            System.out.println(currentTime + " " + msg);
            aWriter.flush();
            aWriter.close();
        }
        catch (Exception e) {
            System.out.println(stack2string(e));
        }
    }
    
    private static String stack2string(Exception e) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return "------\r\n" + sw.toString() + "------\r\n";
        }
        catch(Exception e2) {
            return "bad stack2string";
        }
    }    
}
