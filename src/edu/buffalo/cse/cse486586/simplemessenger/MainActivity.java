package edu.buffalo.cse.cse486586.simplemessenger;



import android.app.ListActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;



/**
 * Demonstrates the using a list view in transcript mode
 *
 */
public class MainActivity extends ListActivity implements /*OnClickListener,*/ OnKeyListener {

    private EditText tbox;
    
    private String text;
    
    private ArrayAdapter<String> adapter;
    
    private ArrayList<String> messages = new ArrayList<String>();
    
    private ServerSocket serverSocket;
    
   // private Handler handler= new Handler();
    
    public static String incoming=null;
    
    public  int remotePort;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, messages);
        
        setListAdapter(adapter);
        
        tbox = (EditText) findViewById(R.id.userText);

       // tbox.setOnClickListener(this);
        tbox.setOnKeyListener(this);
        
        TelephonyManager tel =
                (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        
        if(Integer.parseInt(portStr)==5554) remotePort=11112;
        else if(Integer.parseInt(portStr)==5556) remotePort=11108;
        //launching server thread in the form of AsyncTask
        new ServerThread().execute();
    }

   /* public void onClick(View v) {
        sendText();
    }
*/
    private void sendText() {
         text = tbox.getText().toString();
         
        //adapter.add("Local_app :"+text);
        new Thread(new Runnable(){
        	public void run(){
        		Log.d("Sender method","Sender method is invoked");
        		try{
        			Socket server= new Socket(InetAddress.getByName("10.0.2.2"),remotePort);
        			PrintWriter pw= new PrintWriter(server.getOutputStream(), true);
        			pw.println(text);
        			pw.close();
        			server.close();
        		}catch(Exception e){
        			Log.d("From sender thread","Unable to connect to the remote server");
        		}
        		
        	}
        }).start();
      //  new Thread(new Sender(text)).start();
        tbox.setText(null);
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    sendText();                   
                    return true;
            }
        }
        return false;
    }
    
    public class ServerThread extends AsyncTask<Void, String, Void>{
    	
    	@Override
		protected Void doInBackground(Void... params) {
			// launching the server thread.....
    		try {
				serverSocket= new ServerSocket(10000);
				while(true){
					Socket client= serverSocket.accept();
					try{
				      BufferedReader br= new BufferedReader(new InputStreamReader(client.getInputStream()));
				   //   String incoming=null;
				      while((incoming=br.readLine())!=null){
				    	  Log.d("ServerActivity", incoming);
				    	  publishProgress(incoming);
				      }
				      
					}catch(IOException e){
						
						client.close();
						continue;
					}
				}
				
			} catch (IOException e) {
				Log.d("Server creation error","some error in creation of server");
				e.printStackTrace();
			}
			return null;
		}
    	
    	 protected void onProgressUpdate(String...progress ) {
    		  adapter.add("Remote App : "+progress[0]);
              }

		
	}// end of server thread/asynctask

    // sender  thread that captures the user input text and sends it to remote app
        public class Sender implements Runnable {

    	String message=null;
    	Sender(String msg){
    		message=msg;
    	}
    	public void run() {
    		Log.d("Sender method","Sender method is invoked");
    		try{
    			Socket server= new Socket(InetAddress.getByName("10.0.2.2"),remotePort);
    			PrintWriter pw= new PrintWriter(server.getOutputStream(), true);
    			pw.println(message);
    			pw.close();
    			server.close();
    		}catch(Exception e){
    			
    		}
    	}
    }

}
