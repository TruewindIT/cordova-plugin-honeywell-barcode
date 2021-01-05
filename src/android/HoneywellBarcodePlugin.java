package com.truewind.honeywellbarcodeplugin;

import android.os.Handler;
import android.os.Message;

import org.apache.cordova.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import honeywell.hedc_usb_com.HEDCUsbCom;

/**
 * This class wraps Honeywell's SDK provided functionality 
 * in the form of three listeners: OnConnectionStateListener, OnBarcodeListener and OnImageListener
 * Author: João César (Truewind - www.truewindglobal.com)
 */
public class HoneywellBarcodePlugin extends CordovaPlugin implements HEDCUsbCom.OnConnectionStateListener, HEDCUsbCom.OnBarcodeListener,HEDCUsbCom.OnImageListener {

	HEDCUsbCom m_engine;

    private byte m_manual_mode          =  0;
    private byte m_presentation_mode    =  3;

    protected String barcodeData = "0";
    protected String connectionStatus = "";
    	
    CallbackContext mGlobalCallbackContext;	
	private CordovaWebView mainWebView;

	private static final String INIT = "engineInit";
	private static final String STOP = "engineStop";
	private static final String EVENT_CALLBACK = "honeywell_callback";
	
    private String[] AsciiTab = {
            "NUL", "SOH", "STX", "ETX", "EOT", "ENQ", "ACK", "BEL",	"BS",  "HT",  "LF",  "VT",
            "FF",  "CR",  "SO",  "SI",	"DLE", "DC1", "DC2", "DC3", "DC4", "NAK", "SYN", "ETB",
            "CAN", "EM",  "SUB", "ESC", "FS",  "GS",  "RS",  "US",	"SP",  "DEL",
    };
	
	public String ConvertToString(byte[] data, int length)
    {
        String s = "";
        String s_final = "";
        for (int i = 0; i < length; i++) {
            if ((data[i]>=0)&&(data[i] < 0x20 ))
            {
                s = String.format("<%s>",AsciiTab[data[i]]);
            }
            else if (data[i] >= 0x7F)
            {
                s = String.format("<0x%02X>",data[i]);
            }
            else {
                s = String.format("%c", data[i]&0xFF);
            }
            s_final+=s;
        }

        return s_final;
    }
	
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        
		super.initialize(cordova, webView);
		mainWebView = webView;
        
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        		
		
		boolean success = true;
		
		switch (action)	
		{
			case EVENT_CALLBACK:
				
				mGlobalCallbackContext = callbackContext;					
				
				PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT, "honeywell_callback");
                result.setKeepCallback(true);
                mGlobalCallbackContext.sendPluginResult(result);
				break;
				
			case INIT:				
				
				if(m_engine==null){				
					m_engine = new HEDCUsbCom(cordova.getContext(),this,this,this);
				}else{
					m_engine.SetTriggerMode(m_presentation_mode);
				}
				
				callbackContext.success("engineInit: success");
				break;
			
			case STOP:

				if(m_engine!=null){
						m_engine.SetTriggerMode(m_manual_mode);
						
						callbackContext.success("engineStop: success");
				}else{	
						callbackContext.success("engineStop: engine is not initialized");			   
						success = false;
				}
				break;			
			
			default:
			                
                callbackContext.error( action + " is not a supported function.");
                success = false;
                break;	
		}
		
		return success;
		
    }
    
	
	@Override
    public void OnBarcodeData(final byte[] data, final int length) {        		

		   cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
					
					// get data from sensor
					barcodeData=ConvertToString(data,length);        

					// send to plugin
					PluginResult result = new PluginResult(PluginResult.Status.OK, barcodeData);
					result.setKeepCallback(true);
					mGlobalCallbackContext.sendPluginResult(result);
				}					
		   });	
			
    }

    @Override
    public void OnConnectionStateEvent(HEDCUsbCom.ConnectionState state) {

			cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

					// get connection state
					if (state == HEDCUsbCom.ConnectionState.Connected) {
						
						connectionStatus = "Device connected";   
						
						//setting it in presentation mode
						m_engine.SetTriggerMode(m_presentation_mode);						
						
					} else {
						
						connectionStatus = "Device not connected";
					}       
					
					// send to plugin
					//PluginResult result = new PluginResult(PluginResult.Status.OK, connectionStatus);
					//result.setKeepCallback(true);
					//mGlobalCallbackContext.sendPluginResult(result);
				}					
		   });		   
    }

    @Override
    public void OnImageData(byte[] bytes, int i) {
			
			// not implemented
			
    }    
}
