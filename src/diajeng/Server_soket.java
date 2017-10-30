package diajeng;

import org.apache.commons.codec.binary.Base64;
import static diajeng.Server_soket.dbip;
import static diajeng.Server_soket.pan;
import static diajeng.Server_soket.tanggal;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.sql.*;  
import org.json.simple.JSONObject;
import org.json.simple.*; 
import java.sql.ResultSet; 
//import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Server_soket extends Thread {

//    private static ServerSocket serverSocket;
    public static ServerSocket serverSocket;    
    private static final int PORT = 2000;  
    public static int buka_port=0;
    public static String tanggal,pan,nomor,dbip,dbsid,dbuser,dbpassword;
    public static int dbport;
    
    public void run() {
        try 
        {
            serverSocket = new ServerSocket(PORT);            
            logging.write("Server berjalan, menunggu client...");
            buka_port=1;
        } catch (IOException ioEx) {
            logging.write("Tdak dapat mensetup port");
//            System.out.println("\nTidak dapat mensetup port!");
            System.exit(1);
        }
        do 
        {
            try
            {
                //Menunggu koneksi dari client...
                Socket client = serverSocket.accept();
                logging.write("\nClient baru diterima.\n");
 
                //Buat thread untuk menangani komunikasi dengan client ini
                //lewatkan socket yang relevan ke contructor dari  thread ini
                ClientHandler handler = new ClientHandler(client);
                handler.start();    //menjalankan thread yang telah dibuat
            }
            catch(IOException ioEx)
            {
                logging.write("Gagal buat thread handler client");
            }
        } while (buka_port==1);
    }
    
}

class ClientHandler extends Thread 
{
 
    private Socket client;
    DataInputStream dis = null;
    DataOutputStream dos = null;
 
    public ClientHandler(Socket socket) 
    {
        //Set up referensi ke socket yang beraosiasi...
        client = socket;
        try 
        {
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());
        } catch (IOException ioEx) {
            //ioEx.printStackTrace();
        }
    }
    
    public static String decrypt(String key, String initVector, String encrypted) 
    {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(DatatypeConverter.parseBase64Binary(encrypted‌​));

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
 
        public static String encrypt(String key, String initVector, String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.encodeBase64String(encrypted);
                    
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
        
    public void run() 
    {
        String received="";
        String responserver="";
 
        do 
        {
            try
            {
                String res = dis.readUTF();
                System.out.println("req yang diterima : "+res);
                received = decrypt("Bar12345Bar12345","RandomInitVector",res);
                System.out.println("hasil dekrip req yang diterima : "+received);
                try 
                {
                    Object obj = new JSONParser().parse(received);
                    JSONObject jo = (JSONObject) obj;
                    String produk = (String) jo.get("produk");
                    System.out.println("produk = "+produk);
                    switch (produk) {
                        case "atmb":
                            tanggal = (String) jo.get("tanggal");
                            pan = (String) jo.get("pan");
                            responserver = prosesDataatmb(tanggal,pan);
                            break;
                        case "IsatPre":
                            String tgl = (String) jo.get("tanggal");
                            String nomor = (String) jo.get("nomor");
                            responserver = prosesDataisatpre(tgl,nomor);
                            break;
                        case "IsatPost":
                            tanggal = (String) jo.get("tanggal");
                            nomor = (String) jo.get("nomor");
                            responserver = prosesDataisatpost(tanggal,nomor);
                            break;
                        case "XLPre":
                            tanggal = (String) jo.get("tanggal");
                            nomor = (String) jo.get("nomor");
                            responserver = prosesDataxlpre(tanggal,nomor);
                            break;
                        case "XLPost":
                            tanggal = (String) jo.get("tanggal");
                            nomor = (String) jo.get("nomor");
                            responserver = prosesdatapayment(tanggal,nomor,"122");
                            break;
                        case "SmartfrenPre":
                            tanggal = (String) jo.get("tanggal");
                            nomor = (String) jo.get("nomor");
                            responserver = prosesDatasmartfrenpre(tanggal,nomor);
                            break;                                        
                        case "SmartfrenPost":
                            tanggal = (String) jo.get("tanggal");
                            nomor = (String) jo.get("nomor");
                            responserver = prosesDatasmartfrenpost(tanggal,nomor);
                            break;                                                
                        case "AxisPre":
                            tanggal = (String) jo.get("tanggal");
                            nomor = (String) jo.get("nomor");
                            responserver = prosesdatapayment(tanggal,nomor,"110","1141");
                            break;
                        case "MNC":
                            tanggal = (String) jo.get("tanggal");
                            nomor = (String) jo.get("nomor");
                            responserver = prosesdatapayment(tanggal,nomor,"800");
                            break;
                        case "TselPre":
                            tanggal = (String) jo.get("tanggal");
                            nomor = (String) jo.get("nomor");
                            responserver = prosesDatatselpre(tanggal,nomor);
                            break;                            
                        case "TselPost":
                            tanggal = (String) jo.get("tanggal");
                            nomor = (String) jo.get("nomor");
                            responserver = prosesdatapayment(tanggal,nomor,"132");
                            break;
                        case "EsiaPre":
                            tanggal = (String) jo.get("tanggal");
                            nomor = (String) jo.get("nomor");
                            responserver = prosesdatapayment(tanggal,nomor,"181");
                            break;
                        case "EsiaPost":
                            tanggal = (String) jo.get("tanggal");
                            nomor = (String) jo.get("nomor");
                            responserver = prosesdatapayment(tanggal,nomor,"182");
                            break;
                        case "PLN":
                            tanggal = (String) jo.get("tanggal");
                            nomor = (String) jo.get("nomor");
                            responserver = prosesdatapayment(tanggal,nomor,"212","2101");
                            break;                            
                        case "PGN":
                            tanggal = (String) jo.get("tanggal");
                            nomor = (String) jo.get("nomor");
                            responserver = prosesdatapayment(tanggal,nomor,"212","2102");
                            break;
                        case "PBB":
                            tanggal = (String) jo.get("tanggal");
                            nomor = (String) jo.get("nomor");
                            responserver = prosesdatapayment(tanggal,nomor,"231");
                            break;
                        case "Citi":
                            tanggal = (String) jo.get("tanggal");
                            nomor = (String) jo.get("nomor");
                            responserver = prosesDataciti(tanggal,nomor);
                            break;
                        case "Adira":
                            tanggal = (String) jo.get("tanggal");
                            nomor = (String) jo.get("nomor");
                            responserver = prosesDataadira(tanggal,nomor);
                            break;
                        case "FIF":
                            tanggal = (String) jo.get("tanggal");
                            nomor = (String) jo.get("nomor");
                            responserver = prosesDatafif(tanggal,nomor);
                            break;            
                        case "summit":
                            tanggal = (String) jo.get("tanggal");
                            nomor = (String) jo.get("nomor");
                            responserver = prosesdatapayment(tanggal,nomor,"414","4101");
                            break;                            
                        case "baf":
                            tanggal = (String) jo.get("tanggal");
                            nomor = (String) jo.get("nomor");
                            responserver = prosesdatapayment(tanggal,nomor,"414","4102");
                            break;
                        case "ui":
                            tanggal = (String) jo.get("tanggal");
                            nomor = (String) jo.get("nomor");
                            responserver = prosesdatapayment(tanggal,nomor,"612");
                            break;
                        case "takaful":
                            tanggal = (String) jo.get("tanggal");
                            nomor = (String) jo.get("nomor");
                            responserver = prosesDatatakaful(tanggal,nomor);
                            break;
                        case "pru":
                            tanggal = (String) jo.get("tanggal");
                            nomor = (String) jo.get("nomor");
                            responserver = prosesDatapru(tanggal,nomor);
                            break;
                        case "garuda":
                            tanggal = (String) jo.get("tanggal");
                            nomor = (String) jo.get("nomor");
                            responserver = prosesdatapayment(tanggal,nomor,"788");
                            break;
                        default: responserver = prosesDataatmb("0101","12345678");
                             break;
                    }
                    
                } catch (ParseException ex) 
                {
                    Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            catch(IOException ioEx)
            {
                received = "QUIT";
            } catch (Exception ex) {
                System.out.println("ga bisa didekrip");
                logging.write("ga bisa didekrip");
                received = "QUIT";
            }
            
            if (received != "QUIT"){
                logging.write(received);
            }
            
            //Mengirim respon ke client
            try
            {
                System.out.println("Respon asli : "+responserver);
                dos.writeUTF(encrypt("Bar12345Bar12345","RandomInitVector",responserver));
                System.out.println("Respon yang udah dienkrip : "+encrypt("Bar12345Bar12345","RandomInitVector",responserver));
            }
            catch(IOException ioEx)
            {
                received = "QUIT";
            }                        
 
            //Ulangi sampai client mengirimkan pesan 'QUIT'...
        } while (!received.equals("QUIT"));
         
        try 
        {
            if (client != null) {
                logging.write("Menutup koneksi...");
                client.close();
            }
        } catch (IOException ioEx) {
            logging.write("Penutupan koneksi gagal!");
        }
    }
    
    private String prosesDataatmb(String tgl,String pan)
    {
        Connection con;
        String responserver="";
        try
        {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con=DriverManager.getConnection("jdbc:oracle:thin:@"+Server_soket.dbip+":"+Server_soket.dbport+":"+Server_soket.dbsid,Server_soket.dbuser,Server_soket.dbpassword);
            logging.write("Koneksi ke DB berhasil");
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_100,de_125,de_127 from atmbiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_002='"+pan+"' order by timestamp,msg_dir");
            JSONObject json = Convertor.convertToJSON(rs);
            responserver=json.toString();
            con.close();
            logging.write(responserver);
        }
        catch(Exception e)
        {
            logging.write(e);
        }
        return responserver;
    }
    
    private String prosesDataisatpre(String tgl,String nomor)
    {
        Connection con;
        String responserver="";
        try
        {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con=DriverManager.getConnection("jdbc:oracle:thin:@"+Server_soket.dbip+":"+Server_soket.dbport+":"+Server_soket.dbsid,Server_soket.dbuser,Server_soket.dbpassword);            
            logging.write("Koneksi ke DB berhasil");
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='141' and de_048 like '%"+nomor+"%' "
                    + "union all select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='151' and de_048 like '%"+nomor+"%' "
                    + "union all select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='161' and de_048 like '%"+nomor+"%' "
                    + "union all select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='110' and de_048 like '1157%' and de_048 like '%"+nomor+"%' "
                    + "union all select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='110' and de_048 like '1159%' and de_048 like '%"+nomor+"%' "
                    + "union all select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='110' and de_048 like '1161%' and de_048 like '%"+nomor+"%' "        
                    + "order by timestamp,msg_dir");
            JSONObject json = Convertor.convertToJSON(rs);
            responserver=json.toString();
            con.close();
            logging.write(responserver);
        }
        catch(Exception e)
        {
            logging.write(e);
        }
        return responserver;
    }

    private String prosesDatatakaful(String tgl,String nomor)
    {
        Connection con;
        String responserver="";
        try
        {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con=DriverManager.getConnection("jdbc:oracle:thin:@"+Server_soket.dbip+":"+Server_soket.dbport+":"+Server_soket.dbsid,Server_soket.dbuser,Server_soket.dbpassword);            
            logging.write("Koneksi ke DB berhasil");
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='712' and de_048 like '%"+nomor+"%' "
                    + "union all select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='722' and de_048 like '%"+nomor+"%' "
                    + "union all select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='732' and de_048 like '%"+nomor+"%' "
                    + "union all select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='742' and de_048 like '%"+nomor+"%' "
                    + "order by timestamp,msg_dir");
            JSONObject json = Convertor.convertToJSON(rs);
            responserver=json.toString();
            con.close();
            logging.write(responserver);
        }
        catch(Exception e)
        {
            logging.write(e);
        }
        return responserver;
    }    

    private String prosesDatapru(String tgl,String nomor)
    {
        Connection con;
        String responserver="";
        try
        {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con=DriverManager.getConnection("jdbc:oracle:thin:@"+Server_soket.dbip+":"+Server_soket.dbport+":"+Server_soket.dbsid,Server_soket.dbuser,Server_soket.dbpassword);            
            logging.write("Koneksi ke DB berhasil");
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='751' and de_048 like '%"+nomor+"%' "
                    + "union all select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='752' and de_048 like '%"+nomor+"%' "
                    + "order by timestamp,msg_dir");
            JSONObject json = Convertor.convertToJSON(rs);
            responserver=json.toString();
            con.close();
            logging.write(responserver);
        }
        catch(Exception e)
        {
            logging.write(e);
        }
        return responserver;
    }    
    
    private String prosesDatafif(String tgl,String nomor)
    {
        Connection con;
        String responserver="";
        try
        {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con=DriverManager.getConnection("jdbc:oracle:thin:@"+Server_soket.dbip+":"+Server_soket.dbport+":"+Server_soket.dbsid,Server_soket.dbuser,Server_soket.dbpassword);
            logging.write("Koneksi ke DB berhasil");
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='412' and de_048 like '%"+nomor+"%' "
                    + "union all select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='414' and de_048 like '4112%' and de_048 like '%"+nomor+"%' "
                    + "union all select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='414' and de_048 like '4113%' and de_048 like '%"+nomor+"%' "
                    + "order by timestamp,msg_dir");
            JSONObject json = Convertor.convertToJSON(rs);
            responserver=json.toString();
            con.close();
            logging.write(responserver);
        }
        catch(Exception e)
        {
            logging.write(e);
        }
        return responserver;
    }    
    
   private String prosesDataadira(String tgl,String nomor)
    {
        Connection con;
        String responserver="";
        try
        {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con=DriverManager.getConnection("jdbc:oracle:thin:@"+Server_soket.dbip+":"+Server_soket.dbport+":"+Server_soket.dbsid,Server_soket.dbuser,Server_soket.dbpassword);
            logging.write("Koneksi ke DB berhasil");
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='414' and de_048 like '0007' and de_048 like '%"+nomor+"%' "
                    + "union all select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='414' and de_048 like '4122%' and de_048 like '%"+nomor+"%' "
                    + "order by timestamp,msg_dir");
            JSONObject json = Convertor.convertToJSON(rs);
            responserver=json.toString();
            con.close();
            logging.write(responserver);
        }
        catch(Exception e)
        {
            logging.write(e);
        }
        return responserver;
    }    
    
    private String prosesDataciti(String tgl,String nomor)
    {
        Connection con;
        String responserver="";
        try
        {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con=DriverManager.getConnection("jdbc:oracle:thin:@"+Server_soket.dbip+":"+Server_soket.dbport+":"+Server_soket.dbsid,Server_soket.dbuser,Server_soket.dbpassword);
            logging.write("Koneksi ke DB berhasil");
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='311' and de_048 like '%"+nomor+"%' "
                    + "union all select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='312' and de_048 like '%"+nomor+"%' "
                    + "order by timestamp,msg_dir");
            JSONObject json = Convertor.convertToJSON(rs);
            responserver=json.toString();
            con.close();
            logging.write(responserver);
        }
        catch(Exception e)
        {
            logging.write(e);
        }
        return responserver;
    }
    
    private String prosesDatasmartfrenpre(String tgl,String nomor)
    {
        Connection con;
        String responserver="";
        try
        {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con=DriverManager.getConnection("jdbc:oracle:thin:@"+Server_soket.dbip+":"+Server_soket.dbport+":"+Server_soket.dbsid,Server_soket.dbuser,Server_soket.dbpassword);
            logging.write("Koneksi ke DB berhasil");
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='110' and de_048 like '1101%' and de_048 like '%"+nomor+"%' "
                    + "union all select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='110' and de_048 like '1191%' and de_048 like '%"+nomor+"%' "
                    + "union all select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='110' and de_048 like '1193%' and de_048 like '%"+nomor+"%' "
                    + "order by timestamp,msg_dir");
            JSONObject json = Convertor.convertToJSON(rs);
            responserver=json.toString();
            con.close();
            logging.write(responserver);
        }
        catch(Exception e)
        {
            logging.write(e);
        }
        return responserver;
    }    

    private String prosesDatasmartfrenpost(String tgl,String nomor)
    {
        Connection con;
        String responserver="";
        try
        {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con=DriverManager.getConnection("jdbc:oracle:thin:@"+Server_soket.dbip+":"+Server_soket.dbport+":"+Server_soket.dbsid,Server_soket.dbuser,Server_soket.dbpassword);
            logging.write("Koneksi ke DB berhasil");
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='100' and de_048 like '1002%' and de_048 like '%"+nomor+"%' "
                    + "union all select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='100' and de_048 like '1192%' and de_048 like '%"+nomor+"%' "
                    + "union all select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='100' and de_048 like '1194%' and de_048 like '%"+nomor+"%' "
                    + "order by timestamp,msg_dir");
            JSONObject json = Convertor.convertToJSON(rs);
            responserver=json.toString();
            con.close();
            logging.write(responserver);
        }
        catch(Exception e)
        {
            logging.write(e);
        }
        return responserver;
    } 
    
    private String prosesDataisatpost(String tgl,String nomor)
    {
        Connection con;
        String responserver="";
        try
        {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con=DriverManager.getConnection("jdbc:oracle:thin:@"+Server_soket.dbip+":"+Server_soket.dbport+":"+Server_soket.dbsid,Server_soket.dbuser,Server_soket.dbpassword);
            logging.write("Koneksi ke DB berhasil");
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='142' and de_048 like '%"+nomor+"%' "
                    + "union all select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='152' and de_048 like '%"+nomor+"%' "
                    + "union all select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='162' and de_048 like '%"+nomor+"%' "
                    + "union all select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='100' and de_048 like '1158%' and de_048 like '%"+nomor+"%' "
                    + "order by timestamp,msg_dir");
            JSONObject json = Convertor.convertToJSON(rs);
            responserver=json.toString();
            con.close();
            logging.write(responserver);
        }
        catch(Exception e)
        {
            logging.write(e);
        }
        return responserver;
    }    

    private String prosesDatatselpre(String tgl,String nomor)
    {
        Connection con;
        String responserver="";
        try
        {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con=DriverManager.getConnection("jdbc:oracle:thin:@"+Server_soket.dbip+":"+Server_soket.dbport+":"+Server_soket.dbsid,Server_soket.dbuser,Server_soket.dbpassword);
            logging.write("Koneksi ke DB berhasil");
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='131' and de_048 like '%"+nomor+"%' "
                    + "union all select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='133' and de_048 like '%"+nomor+"%' "
                    + "order by timestamp,msg_dir");
            JSONObject json = Convertor.convertToJSON(rs);
            responserver=json.toString();
            con.close();
            logging.write(responserver);
        }
        catch(Exception e)
        {
            logging.write(e);
        }
        return responserver;
    }        
    
    private String prosesDataxlpre(String tgl,String nomor)
    {
        Connection con;
        String responserver="";
        try
        {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con=DriverManager.getConnection("jdbc:oracle:thin:@"+Server_soket.dbip+":"+Server_soket.dbport+":"+Server_soket.dbsid,Server_soket.dbuser,Server_soket.dbpassword);
            logging.write("Koneksi ke DB berhasil");
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='121' and de_048 like '%"+nomor+"%' "
                    + "union all select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='110' and de_048 like '1123%' and de_048 like '%"+nomor+"%' "
                    + "order by timestamp,msg_dir");
            JSONObject json = Convertor.convertToJSON(rs);
            responserver=json.toString();
            con.close();
            logging.write(responserver);
        }
        catch(Exception e)
        {
            logging.write(e);
        }
        return responserver;
    }    
    
    private String prosesdatapayment(String tgl,String nomor, String de_63)
    {
        Connection con;
        String responserver="";
        try
        {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con=DriverManager.getConnection("jdbc:oracle:thin:@"+Server_soket.dbip+":"+Server_soket.dbport+":"+Server_soket.dbsid,Server_soket.dbuser,Server_soket.dbpassword);
            logging.write("Koneksi ke DB berhasil");
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='"+de_63+"' and de_048 like '%"+nomor+"%' order by timestamp,msg_dir");
            JSONObject json = Convertor.convertToJSON(rs);
            responserver=json.toString();
            con.close();
            logging.write(responserver);
        }
        catch(Exception e)
        {
            logging.write(e);
        }
        return responserver;
    }

    private String prosesdatapayment(String tgl,String nomor, String de_63, String de_48)
    {
        Connection con;
        String responserver="";
        try
        {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con=DriverManager.getConnection("jdbc:oracle:thin:@"+Server_soket.dbip+":"+Server_soket.dbport+":"+Server_soket.dbsid,Server_soket.dbuser,Server_soket.dbpassword);
            logging.write("Koneksi ke DB berhasil");
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("select substr(file_name,3,4) mmdd,timestamp,mti,msg_dir,proc,de_002,de_003,de_004,de_011,de_032,de_037,de_039,de_041,de_063,de_048 from soppiaf_parser where substr(file_name,1,6)='IA"+tgl+"' and de_063='"+de_63+"' and de_048 like '"+de_48+"%' and de_048 like '%"+nomor+"%' order by timestamp,msg_dir");
            JSONObject json = Convertor.convertToJSON(rs);
            responserver=json.toString();
            con.close();
            logging.write(responserver);
        }
        catch(Exception e)
        {
            logging.write(e);
        }
        return responserver;
    }
    
}

