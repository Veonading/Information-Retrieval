package com.wyp.utils;

import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;



public class Yahoo_exchange {
	private static Float exchange_rate=(float) 7.5;
	    public float convert(String currencyFrom, String currencyTo) throws IOException {
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpGet httpGet = new HttpGet("http://quote.yahoo.com/d/quotes.csv?s=" + currencyFrom + currencyTo + "=X&f=l1&e=.csv");
	        ResponseHandler<String> responseHandler = new BasicResponseHandler();
	        String responseBody = httpclient.execute(httpGet, responseHandler);
	        httpclient.getConnectionManager().shutdown();
	        return Float.parseFloat(responseBody);
	    }

	    public static void main(String[] args) {
	        Yahoo_exchange ycc = new Yahoo_exchange();
	        try {
	            float current = ycc.convert("USD", "ILS");
	            System.out.println(current);
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	}