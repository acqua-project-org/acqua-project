package org.inria.acqua.misc;
import it.sauronsoftware.ftp4j.*;
import java.io.*;
import java.util.Hashtable;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

/**
 * @author mjost
 */
public class FTPClientWrapper {
	private static Logger logger = Logger.getLogger(FTPClientWrapper.class.getName()); 
    //private FTPClient client;
    private static final boolean PASSIVE_MODE = true;
    private String server;
    private String user;
    private String pass;

    public FTPClientWrapper(String server, String loginname, String pass){
        this.server = server;
        this.user = loginname;
        this.pass = pass;
    }
    
    /*
    public FTPClientWrapper(String server, String loginname, String pass){
        this.server = server;
        this.user = loginname;
        this.pass = pass;
    }
     * 
     */

    private FTPClient getNewClient() throws Exception{
        FTPClient client  = new FTPClient();

        try{
            client.connect(server);
            client.login(user, pass);
            client.setPassive(PASSIVE_MODE);
        }catch(Exception e){
            this.done(client);
            throw e;
        }
        return client;
    }

    private void done(FTPClient client){
        try{
            //client.logout();
            client.disconnect(true);
            //client.abruptlyCloseCommunication();
        }catch(Exception e){
            logger.warn("Failed while disconnecting from FTP server. Skipping...");
        }
    }


    public void download(String file, ByteArrayOutputStream os) throws Exception{
        FTPClient client = null;
        try{
            client = getNewClient();
            client.download(file, os, 0, null);
            this.done(client);
        }catch(Exception e){
            this.done(client);
            throw e;
        }
    }


    public String downloadAsString(String file) throws Exception{
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        download(file, os);
        return new String(os.toByteArray());

    }



    public String downloadAsStringWithTimeout(String file, int timeout_ms) throws TimeoutException, Exception{
        final String finalfile = file;
        final Hashtable<Runnable, Exception> ht = new Hashtable<Runnable, Exception>();
        final Hashtable<Runnable, String> ret = new Hashtable<Runnable, String>();

        Runnable runnable = new Runnable(){
            public void run(){
                try{
                    String r = downloadAsString(finalfile);
                    ret.put(this, r);
                }catch(Exception e){
                    ht.put(this, e);
                }

            }
        };
        Thread thread = new Thread(runnable);
        thread.setName("ftp-downloader");
        thread.start();

        thread.join(timeout_ms);

        if (thread.isAlive()){ /* Thread is still alive... */
            thread.interrupt();
            throw new TimeoutException();
        }else{
            /* It finished, either because of an Error/Exception or correct execution. */
            if (ht.size()==0){ /* No exception. Everything OK. */
                return ret.get(runnable);
            }else{
                throw ht.get(runnable);
            }
        }
    }




    public FTPFile[] list(String path) throws Exception{
        FTPClient client = null;
        FTPFile[] ret = null;
        try{
            client = getNewClient();
            client.changeDirectory(path);
            ret = client.list();
            this.done(client);
        }catch(Exception e){
            this.done(client);
            throw e;
        }
        return ret;
    }


    public FTPFile[] listWithTimeout(String path, int timeout_ms) throws TimeoutException, Exception{
        final String finalpath = path;
        final Hashtable<Runnable, Exception> ht = new Hashtable<Runnable, Exception>();
        final Hashtable<Runnable, FTPFile[]> ret = new Hashtable<Runnable, FTPFile[]>();

        Runnable runnable = new Runnable(){
            public void run(){
                try{
                    FTPFile[] r = list(finalpath);
                    ret.put(this, r);
                }catch(Exception e){
                    ht.put(this, e);
                }

            }
        };
        Thread thread = new Thread(runnable);
        thread.setName("ftp-lister");
        thread.start();

        thread.join(timeout_ms);

        if (thread.isAlive()){ /* Thread is still alive... */
            thread.interrupt();
            throw new TimeoutException();
        }else{
            /* It finished, either because of an Error/Exception or correct execution. */
            if (ht.size()==0){ /* No exception. Everything OK. */
                return ret.get(runnable);
            }else{
                throw ht.get(runnable);
            }
        }
    }


    public void upload(String path, ByteArrayInputStream is) throws Exception{
        FTPClient client = null;
        try{
            client = getNewClient();
            client.upload(path , is, 0, 0, null);
            this.done(client);
        }catch(Exception e){
            this.done(client);
            throw e;
        }
    }

    private String getOnlyDirectory(String path) throws Exception{
        if (path.charAt(0) != '/' ){
            throw new Exception("Invalid directory '" + path + "', does not start with '/'.");
        }
        
        if (path.charAt(path.length()-1) == '/'){
            return path;
        }else{
            int index = path.lastIndexOf('/');
            return path.substring(0, index+1);
        }
    }

    public void forceCreationOfPath(String path) throws Exception{
        String realpath = getOnlyDirectory(path);

        String[] path_s = realpath.trim().split("/");
        String initial = "/";
        for(String p: path_s){
            if (!p.isEmpty()){
                initial = initial + p + "/";
                try{
                    this.createDirectory(initial);
                }catch(Exception e){
                    logger.warn("Not creating");
                }
            }
        }
    }

    public void forceCreationOfPathWithTimeout(String path, int timeout_ms) throws Exception{
        final String finalpath = path;
        final Hashtable<Runnable, Exception> ht = new Hashtable<Runnable, Exception>();

        Runnable runnable = new Runnable(){
            public void run(){
                try{
                    forceCreationOfPath(finalpath);
                }catch(Exception e){
                    ht.put(this, e);
                }

            }
        };
        Thread thread = new Thread(runnable);
        thread.setName("ftp-creat-path");
        thread.start();

        thread.join(timeout_ms);

        if (thread.isAlive()){ /* Thread is still alive... */
            thread.interrupt();
            
            throw new TimeoutException();
        }else{
            /* It finished, either because of an Error/Exception or correct execution. */
            if (ht.size()==0){ /* No exception. Everything OK. */
                return;
            }else{
                throw ht.get(runnable);
            }
        }
    }

    public void uploadFromString(String path, String content) throws Exception{
        ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes());
        upload(path, is);
    }

    public void uploadFromStringWithTimeout(String path, String content, int timeout_ms) throws TimeoutException, Exception{
        final String finalcontent = content;
        final String finalpath = path;
        final Hashtable<Runnable, Exception> ht = new Hashtable<Runnable, Exception>();

        Runnable runnable = new Runnable(){
            public void run(){
                try{
                    uploadFromString(finalpath, finalcontent);
                }catch(Exception e){
                    ht.put(this, e);
                }

            }
        };
        Thread thread = new Thread(runnable);
        thread.setName("ftp-uploader");
        thread.start();

        thread.join(timeout_ms);
        
        if (thread.isAlive()){ /* Thread is still alive... */
            thread.interrupt();
            throw new TimeoutException();
        }else{
            /* It finished, either because of an Error/Exception or correct execution. */
            if (ht.size()==0){ /* No exception. Everything OK. */
                return;
            }else{
                throw ht.get(runnable);
            }
        }
    }

    public void deleteFile(String filename) throws Exception{
        FTPClient client = null;
        try{
            client = getNewClient();
            client.deleteFile(filename);
            this.done(client);
        }catch(Exception e){
            this.done(client);
            throw e;
        }
    }

    public void createDirectory(String path) throws Exception{
        FTPClient client = null;
        try{
            client = getNewClient();
            client.createDirectory(path);
            this.done(client);
        }catch(Exception e){
            this.done(client);
            throw e;
        }
    }

    public void disconnect(){

    }

    public static void main1(String args[]) throws Exception{
        String URL_FTP_SERVER = "erriapo.pl.sophia.inria.fr";
        //String LOGIN_NAME_FTP_SERVER = "acqua";
        //String LOGIN_PASS_FTP_SERVER = "_FTP17820_Client"; /* i=13365*4/3 */
        String LOGIN_NAME_FTP_SERVER = "test";
        String LOGIN_PASS_FTP_SERVER = "test"; /* i=13365*4/3 */

        FTPClientWrapper client = new FTPClientWrapper(URL_FTP_SERVER, LOGIN_NAME_FTP_SERVER, LOGIN_PASS_FTP_SERVER);

        FTPFile[] res = client.list("/");
        for(FTPFile i:res){
            logger.info("-\t" + i.getName());
        }

        client.disconnect();
    }


    public static void main(String args[]) throws Exception{
        String URL_FTP_SERVER = "erriapo.pl.sophia.inria.fr";
        //String LOGIN_NAME_FTP_SERVER = "acqua";
        //String LOGIN_PASS_FTP_SERVER = "_FTP17820_Client"; /* i=13365*4/3 */
        String LOGIN_NAME_FTP_SERVER = "test";
        String LOGIN_PASS_FTP_SERVER = "test"; /* i=13365*4/3 */


        FTPClientWrapper client = new FTPClientWrapper(URL_FTP_SERVER, LOGIN_NAME_FTP_SERVER, LOGIN_PASS_FTP_SERVER);

        client.uploadFromStringWithTimeout("/test.txt", "Hello", 10*1000);

        client.disconnect();
    }
}
