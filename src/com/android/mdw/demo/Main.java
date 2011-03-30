package com.android.mdw.demo;

import java.util.HashMap;
import java.util.LinkedList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class Main extends Activity {
	/**
	 * constantes necesarias a utilizar al guardar la información 
	 * en un HashMap, por el momento son necesarias para el parser
	 * de XML
	 * 
	 */
	static final String DATA_TITLE = "T";
	static final String DATA_LINK  = "L";
	
	/**
	 * Utilizaremos un SimpleAdapter que toma un listado de mapas para llenar su información, la 
	 * implementación se realizará con un LinkedList y un HashMap. Para este ejemplo utilizaremos un
	 * campo estático no final, es decir, una variable de clase, al realizar sus aplicaciones NO 
	 * deberían hacerlo, lo correcto es utilizar clases de aplicación, pero esto lo veremos hasta
	 * la siguiente guía. Además, más adelante es buena idea utilizar otro almacenamiento no volátil
	 * como una base de datos de SQLite.
	 */
	static LinkedList<HashMap<String, String>> data;

	/**
	 * Guardamos la dirección del feed como otra variable de clase para poder modificarla sin
	 * complicaciones más adelante.
	 */
	static String feedUrl = "http://www.maestrosdelweb.com/index.xml";	
	
	/**
	 * para el diálogo de progreso es necesaria una variable global porque iniciamos el diálogo en
	 * una función y lo ocultamos en otra
	 */
	private ProgressDialog progressDialog;
	
	/**
	 * Android nos presenta la restricciones que no podemos alterar los elementos de interfaz 
	 * gráfica en un hilo de ejecución que no sea el principal por lo que es necesario utilizar 
	 * un manejador(Handler) para enviar un mensaje de un hilo a otro cuando la carga de datos
	 * haya terminado.
	 */	
	private final Handler progressHandler = new Handler() {
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
			if (msg.obj != null) {
				data = (LinkedList<HashMap<String, String>>)msg.obj;
				setData(data);					
			}
			progressDialog.dismiss();
	    }
	};
	
    /** Este método es llamado cuando la actividad es creada */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        /** 
         * Le ponemos nombre a la ventana 
         */
        setTitle("Lector de feed Maestros del Web");
        
        Button btn = (Button) findViewById(R.id.btnLoad);
        
        /**
         * personalizamos el evento del click del botón de carga  
         */
        btn.setOnClickListener(new OnClickListener() {			
    		@Override
    		public void onClick(View v) {
    			ListView lv = (ListView) findViewById(R.id.lstData);
    			/**
    			 * Si el ListView ya contiene datos (es diferente de null) vamos
    			 * a mostrar un diálogo preguntando al usuario si está seguro de
    			 * realizar la carga de nuevo
    			 */
    			if (lv.getAdapter() != null) {
    				AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
    				builder.setMessage("ya ha cargado datos, ¿Está seguro de hacerlo de nuevo?")
    				       .setCancelable(false)
    				       .setPositiveButton("Si", new DialogInterface.OnClickListener() {
    				           public void onClick(DialogInterface dialog, int id) {
    				        	   loadData();
    				           }
    				       })
    				       .setNegativeButton("No", new DialogInterface.OnClickListener() {
    				           public void onClick(DialogInterface dialog, int id) {
    				                dialog.cancel();					            
    				           }
    				       })
    				       .create()
    				       .show();
    				
    			/**
    			 * Si el ListView no contiene datos (es null) cargamos con loadData()
    			 */    				
    			} else {
    				loadData();
    			}
    		}
    	});
        
        ListView lv = (ListView) findViewById(R.id.lstData);
        /**
         * Cuando el usuario haga click en algún elemento de la lista, lo llevaremos al 
         * enlace del elemento a través del navegador.
         */
        lv.setOnItemClickListener(new OnItemClickListener() {

    		@Override
    		public void onItemClick(AdapterView<?> av, View v, int position,
    				long id) {
		        /**
		         * Obtenemos el elemento sobre el que se presionó
		         */
    			HashMap<String, String> entry = data.get(position);

		        /**
		         * Preparamos el intent ACTION_VIEW y luego iniciamos la actividad (navegador en este caso)
		         */
    			Intent browserAction = new Intent(Intent.ACTION_VIEW, 
    					Uri.parse(entry.get(DATA_LINK)));
    			startActivity(browserAction);				
    		}
    	});        
    }
    
    /** 
     * Función auxiliar que recibe una lista de mapas, y utilizando esta data crea un adaptador
     * para poblar al ListView del diseño
     * */
    private void setData(LinkedList<HashMap<String, String>> data){
    	SimpleAdapter sAdapter = new SimpleAdapter(getApplicationContext(), data, 
    			android.R.layout.two_line_list_item, 
    			new String[] { DATA_TITLE, DATA_LINK }, 
    			new int[] { android.R.id.text1, android.R.id.text2 });
    	ListView lv = (ListView) findViewById(R.id.lstData);
    	lv.setAdapter(sAdapter);
    }   
    
    /**
     * Función auxiliar que inicia la carga de datos, muestra al usuario un diálogo de que
     * se están cargando los datos y levanta un thread para lograr la carga.
     */
    private void loadData() {
    	progressDialog = ProgressDialog.show(
    			Main.this,
    			"", 
    			"Por favor espere mientras se cargan los datos...", 
    			true);
    	
    	new Thread(new Runnable(){
    		@Override
    		public void run() {
    			XMLParser parser = new XMLParser(feedUrl); 
                Message msg = progressHandler.obtainMessage();
                msg.obj = parser.parse();
    			progressHandler.sendMessage(msg);
    		}}).start();
    }    
}