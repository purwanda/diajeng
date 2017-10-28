/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diajeng;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kanjeng
 */
public class diajeng_bkp {
        public static boolean exit;
    public static Connection con1;
    
    public static void main0(String[] args)
    {        
        loadconfig();
        Diajeng menu = new Diajeng();
//        menu.runMenu();
    }    
    
    public void runMenu()
    {
        printHeader();
        while(!exit)
        {
            printMenu();
            int choice = getInput();
            performAction(choice);
        }
    }
    
    private void printHeader()
    {
        System.out.println("+----------------------------+");
        System.out.println("|       Selamat datang       |");        
        System.out.println("|    di aplikasi diajeng     |");                
        System.out.println("+----------------------------+");        
    }
    
    private void printMenu()
    {
        System.out.println("\nSilakan pilih menu berikut : ");
        System.out.println("1> Buka port 2000");
        System.out.println("2> Load config file");
        System.out.println("3> Cek koneksi ke DB");
        System.out.println("0> Keluar\n");
    }
    
    private int getInput()
    {
        Scanner kb = new Scanner(System.in);
        int choice = -1;
        while (choice < 0 || choice > 3)
        {
            try
            {
                System.out.printf("\n masukkan pilihanmu :");
                choice = Integer.parseInt(kb.nextLine());
            }
            catch(NumberFormatException e)
            {
                System.out.println("Pilihan salah. Silakan pilih kembali");
            }
        }
        return choice;
    }
    
    private void performAction(int choice)
    {
        switch(choice)
        {
            case 0 :
                try
                {
                    Server_soket.buka_port=0;
                    if(Server_soket.serverSocket != null)
                    Server_soket.serverSocket.close();
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
                loadconfig();
                break;
            case 3 :                  
                try {
                cekkoneksiDB();
                } catch (SQLException ex) {
                Logger.getLogger(Diajeng.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                break;
            default :
                logging.write("Error lain");
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
		System.out.println("dbip : "+ prop.getProperty("dbip"));
                System.out.println("dbport : "+prop.getProperty("dbport"));
                System.out.println("dbsid : "+prop.getProperty("dbsid"));
		System.out.println("dbuser : "+prop.getProperty("dbuser"));		
                System.out.println("log : "+prop.getProperty("log"));
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
    
    private static void cekkoneksiDB() throws SQLException
    {
        try {
            Connection con;
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con=DriverManager.getConnection("jdbc:oracle:thin:@"+Server_soket.dbip+":"+Server_soket.dbport+":"+Server_soket.dbsid,Server_soket.dbuser,Server_soket.dbpassword);            
            System.out.println("Koneksi ke DB BERHASIL");
            con.close();
        }         
        catch (ClassNotFoundException ex) {
            System.out.println("Koneksi ke DB GAGAL. Cek oracle.jdbc.driver.OracleDriver");
            Logger.getLogger(Diajeng.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(SQLException sqlE){System.out.println("Koneksi ke DB GAGAL");};
    }
    
}
