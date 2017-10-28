package diajeng;

import static diajeng.Server_soket.buka_port;
import java.util.Scanner;
import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Diajeng {

    public static boolean exit,adminlogin;
    public static Connection con1;
    public static ServerSocket serverSocketadmin;
    private static final int portadmin = 2001;
    private static Socket clientadmin;
    
    public static void main(String[] args)
    {                
        loadconfig();
        try 
        {
            serverSocketadmin = new ServerSocket(portadmin);            
            logging.write("port admin terbuka...");
            buka_port=1;
        } catch (IOException ioEx) {
            logging.write("Tdak dapat mensetup port admin");
            System.exit(1);
        }
        exit=false;adminlogin=false;
        while(!exit) 
        {
            try
            {
                if(!adminlogin) clientadmin = serverSocketadmin.accept();
                adminlogin=true;
                PrintWriter out = new PrintWriter(clientadmin.getOutputStream());
                out.println("\n+----------------------------+");
                out.println("\n|       Selamat datang       |");        
                out.println("\n|    di aplikasi diajeng     |");                
                out.println("\n+----------------------------+");        
                out.flush();
                out.println("\nSilakan pilih menu berikut : ");
                out.println("\n1> Buka port 2000");
                out.println("\n2> Load config file");
                out.println("\n3> Cek koneksi ke DB");
                out.println("\n4> Keluar dari menu admin");
                out.println("\n0> Matikan aplikasi\n");
                out.flush();
                Scanner in = new Scanner(clientadmin.getInputStream());
                int choice = Integer.parseInt(in.nextLine());
                performAction(choice,out);
            }
            catch(IOException ioEx)
            {
                logging.write("Gagal buat thread handler client");
            }
        }
    }    

    private static void performAction(int choice,PrintWriter out)
    {
        PrintWriter out1;
        out1=out;
        switch(choice)
        {
            case 0 :
                try
                {
                    out1.close();
                    clientadmin.close();
                    Server_soket.buka_port=0;
                    if(Server_soket.serverSocket != null)
                    Server_soket.serverSocket.close();
                    if(serverSocketadmin != null) serverSocketadmin.close();
                }
                catch(IOException ioEx)
                {
                    System.out.println("socket tidak berhasil diclose");
                }
                catch(Exception e)
                {
                    logging.write(e);
                }                
                exit = true;
                logging.write("Aplikasi ditutup");
                break;
            case 1 :
                Server_soket soket = new Server_soket();
                soket.start(); 
                break;
            case 2 :  
                loadconfig(out1);                
                break;
            case 3 :                  
                cekkoneksiDB(out1);
                break;
            case 4 :                
                try
                {
                    out1.close();
                    clientadmin.close();
                    adminlogin=false;
                }
                catch(IOException ioEx)
                {
                    System.out.println("socket tidak berhasil diclose");
                }
                catch(Exception e)
                {
                    logging.write("socket tidak berhasil diclose");
                    logging.write(e);
                }                
                break;                
            default :
                logging.write("Error lain");
        }      
    }
    
    private static void loadconfig(PrintWriter out)
    {
        Properties prop = new Properties();
        InputStream input = null;

	try {

		input = new FileInputStream("config.properties");

		// load a properties file
		prop.load(input);

		// get the property value and print it out
                logging.setLogFilename(prop.getProperty("log"));
                Server_soket.dbip=prop.getProperty("dbip");
                Server_soket.dbport=Integer.parseInt(prop.getProperty("dbport"));
                Server_soket.dbsid=prop.getProperty("dbsid");
                Server_soket.dbuser=prop.getProperty("dbuser");
                Server_soket.dbpassword=prop.getProperty("dbpassword");
		out.println("dbip : "+ prop.getProperty("dbip"));
                out.println("dbport : "+prop.getProperty("dbport"));
                out.println("dbsid : "+prop.getProperty("dbsid"));
		out.println("dbuser : "+prop.getProperty("dbuser"));		
                out.println("log : "+prop.getProperty("log"));
                out.flush();
	} catch (IOException ex) {
		ex.printStackTrace();
                out.println("Load config gagal");
	} finally {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
        
    }
    
    private static void loadconfig()
    {
        Properties prop = new Properties();
        InputStream input = null;

	try {

		input = new FileInputStream("config.properties");

		// load a properties file
		prop.load(input);

		// get the property value and print it out
                logging.setLogFilename(prop.getProperty("log"));
                Server_soket.dbip=prop.getProperty("dbip");
                Server_soket.dbport=Integer.parseInt(prop.getProperty("dbport"));
                Server_soket.dbsid=prop.getProperty("dbsid");
                Server_soket.dbuser=prop.getProperty("dbuser");
                Server_soket.dbpassword=prop.getProperty("dbpassword");
	} catch (IOException ex) {
		ex.printStackTrace();
	} finally {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
        
    }
        
    private static void cekkoneksiDB(PrintWriter out) 
    {
        PrintWriter out1;
        out1=out;
        try {
            Connection con;
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con=DriverManager.getConnection("jdbc:oracle:thin:@"+Server_soket.dbip+":"+Server_soket.dbport+":"+Server_soket.dbsid,Server_soket.dbuser,Server_soket.dbpassword);            
            out1.println("Koneksi ke DB BERHASIL");
            out1.flush();
//            System.out.println("Koneksi ke DB BERHASIL");
            con.close();
        }         
        catch (ClassNotFoundException ex) {
            out1.println("Koneksi ke DB GAGAL.Cek oracle.jdbc.driver.OracleDriver");
            out1.flush();
//            System.out.println("Koneksi ke DB GAGAL. Cek oracle.jdbc.driver.OracleDriver");
            Logger.getLogger(Diajeng.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(SQLException sqlE){
            out1.println("Koneksi ke DB GAGAL");
            out1.flush();
            System.out.println("Koneksi ke DB GAGAL");
        };
    }

}
