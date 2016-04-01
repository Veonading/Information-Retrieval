package com.wyp.HTML;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileNotFoundException; 

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.csvreader.CsvReader;
import com.wyp.utils.DownloadImage;
import com.wyp.utils.Pair;
import com.wyp.utils.Translate;
import com.wyp.utils.UploadImage;
import com.wyp.utils.Yahoo_exchange;
/**
 *
 * @author yaohucaizi
 */
public class AnalyseDM {
	private static double marge = 1.3;
	
	private static Float exchange_rate = (float) 7.5;

    /**
     * 
     */
    public String getHtmlContent(String htmlurl) {
        URL url;
        String temp;
        StringBuffer sb = new StringBuffer();
        try {
            url = new URL(htmlurl);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "gbk"));// 
            while ((temp = in.readLine()) != null) {
                //sb.append(temp);
				sb.append(temp).append(System.getProperty("line.separator"));
            }
            in.close();
        } catch (final MalformedURLException me) {
            System.out.println("URL!");
            me.getMessage();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     *
     * @param s
     * @return 
     */
    public static String getTitle(String s) {
        String regex;
        String title = "";
        List<String> list = new ArrayList<String>();
        regex = "<title>.*?</title>";
        Pattern pa = Pattern.compile(regex, Pattern.CANON_EQ);
        Matcher ma = pa.matcher(s);
        while (ma.find()) {
            list.add(ma.group());
        }
        for (int i = 0; i < list.size(); i++) {
            title = title + list.get(i);
        }
        return outTag(title);
    }

    /**
     *
     * @param s
     * @return 
     */
    public static String getLink(Document doc) {
        String link = "";      
        Elements link_es = doc.getElementsByClass("s-access-detail-page");
        link = link_es.get(0).select("a").first().attr("href");
        return link;
    }

    /**
     *
     * @param s
     * @return 
     */
    public List<String> getScript(String s) {
        String regex;
        List<String> list = new ArrayList<String>();
        regex = "<SCRIPT.*?</SCRIPT>";
        Pattern pa = Pattern.compile(regex, Pattern.DOTALL);
        Matcher ma = pa.matcher(s);
        while (ma.find()) {
            list.add(ma.group());
        }
        return list;
    }
    
    public List<String> getNews(String s) {
        String regex = "<a.*?</a>";
        Pattern pa = Pattern.compile(regex, Pattern.DOTALL);
        Matcher ma = pa.matcher(s);
        List<String> list = new ArrayList<String>();
        while (ma.find()) {
            list.add(ma.group());
        }
        return list;
    }

    /**
     *
     * @param s
     * @return CSS
     */
    public List<String> getCSS(String s) {
        String regex;
        List<String> list = new ArrayList<String>();
        regex = "<style.*?</style>";
        Pattern pa = Pattern.compile(regex, Pattern.DOTALL);
        Matcher ma = pa.matcher(s);
        while (ma.find()) {
            list.add(ma.group());
        }
        return list;
    }

    /**
     *
     * @param s
     * @return 
     */
    public static String outTag(final String s) {
        return s.replaceAll("<.*?>", "");
    }
    public static String outQuote(final String s) {
        if(s.contains("href=")){
        	int index_b = s.indexOf("href=");
        	int index_e = s.lastIndexOf(".html");
        	//System.out.println(s);
        	return s.substring(index_b+6,index_e+5);
        }else if(s.contains("src=")){
            	int index_b = s.indexOf("src=");
            	int index_e = s.lastIndexOf(".jpg");
            	//System.out.println(s);
            	return s.substring(index_b+5,index_e+4);
        }
        else return s.replaceAll("href=\"", "").replaceAll("\"", "");
    }


    /**
     *
     * @param s
     * @return Fissler,cvs,shopify
     * @throws Exception 
     *
     */
    public static void getProductDetails(String url, String type) throws Exception{
    	Document doc = Jsoup.connect(url)
    			 .data("query", "Java")
    			  .userAgent("Mozilla")
    			  .cookie("auth", "token")
    			  .timeout(30000)
    			  .post();
    	//
        String product_name = getProductTitle(doc);
        System.out.println("===========================");
        System.out.println(product_name);

        //in feature-bullets
        String product_desc = getProductDesc(doc);
        
        //
        //String product_nr = getProductNumber(doc);
         
    	
        //
        List<String> prices = getProductPrices(doc);    
        String old_price = prices.get(0);    
        String new_price = prices.get(1);
        //System.out.println(old_price);
        //System.out.println(new_price);
        
        
        //
        List<String> imgList = getProductImgs(doc);
        
        String SKU = "";
	    //if(variations.size()==0) //no variations
	    	//SKU = product_nr.replaceAll(" ", "");
	    	//System.out.println(SKU);
	    /*else{
	    	SKU = "\"".concat((variations.get(0)).get(0)).concat("\"");
	   	}*/
	    	
        //
        String vendor = getProductVendor(SKU);
        
        //cvs
        try { 
        	
    	    FileWriter writer = new FileWriter("products_export_wmf.csv", true);
    	    //Handle
    	    //writer.append("\"".concat(product_nr).concat("\""));
    	    writer.append(',');
    	    //Title
    	    writer.append("\"".concat(product_name).concat("\""));  
    	    writer.append(',');
    	    //Collection
    	    writer.append("WMF");
    	    writer.append(',');
    	    //Body (HTML)
    	    //writer.append("\"".concat(product_desc).concat("\""));
    	    writer.append(',');   	    
    	    //Vendor
    	    writer.append(vendor);
    	    writer.append(',');
    	    //Type
    	    writer.append(type);
    	    writer.append(',');
    	    //Tags
    	    writer.append("\"".concat("WMF, ").concat(type).concat("\""));
    	    writer.append(',');
    	    //Published
    	    writer.append("true");
    	    writer.append(',');
    	    //Option1 Name
    	    writer.append("Title");
    	    writer.append(',');
    	    //Option1 Value
    	    writer.append("Default Title");
    	    writer.append(',');   	    
    	    //Option2 Name
    	    writer.append("");
    	    writer.append(',');
    	    //Option2 Value
    	    writer.append("");
    	    writer.append(',');
    	    //Option3 Name
    	    writer.append("");
    	    writer.append(',');
    	    //Option3 Value
    	    writer.append("");
    	    writer.append(',');
    	    //Variant SKU
    	    writer.append(SKU);
    	    writer.append(',');
    	    //Variant Grams
    	    writer.append("1100");
    	    writer.append(',');
    	    //Variant Inventory Tracker
    	    writer.append("");
    	    writer.append(',');
    	    //Variant Inventory Qty
    	    writer.append("1");
    	    writer.append(',');
    	    //Variant Inventory Policy
    	    writer.append("deny");
    	    writer.append(',');
      	    //Variant Fulfillment Service
    	    writer.append("manual");
    	    writer.append(',');
      	    //Variant Price
    	    writer.append("\"".concat(new_price).concat("\""));
    	    writer.append(',');
      	    //Variant Compare at Price
    	    writer.append("\"".concat(old_price).concat("\""));
    	    writer.append(',');
      	    //Variant Requires Shipping
    	    writer.append("true");
    	    writer.append(',');
      	    //Variant Taxable
    	    writer.append("false");
    	    writer.append(',');
      	    //Variant Barcode
    	    writer.append("");
    	    writer.append(',');
      	    //Image Src
    	    if(imgList.size()>0) writer.append(imgList.get(0));
    	    else writer.append("");
    	    writer.append(',');
      	    //Image Alt Text 
    	    writer.append("");
    	    writer.append(',');
      	    //Gift Card
    	    writer.append("false");
    	    writer.append(',');
    	    
    	    //Metafields
      	    //SEO Title
    	    writer.append("\"".concat(product_name).concat("\""));
    	    writer.append(',');
      	    //SEO Description  
    	    writer.append("\"".concat(product_name).concat("\""));
    	    writer.append(',');
      	    //Google Shopping / Google Product Category
    	    writer.append("");
    	    writer.append(',');
      	    //Google Shopping / Gender
    	    writer.append("");
    	    writer.append(',');
      	    //Google Shopping / Age Group
    	    writer.append("");
    	    writer.append(',');
      	    //Google Shopping / MPN
    	    writer.append("");
    	    writer.append(',');
      	    //Google Shopping / Adwords Grouping
    	    writer.append("");
    	    writer.append(',');
      	    //Google Shopping / Adwords Labels
    	    writer.append("");
    	    writer.append(',');
      	    //Google Shopping / Condition
    	    writer.append("new");
    	    writer.append(',');
      	    //Google Shopping / Custom Product
    	    writer.append("");
    	    writer.append(',');
      	    //Google Shopping / Custom Label 0
    	    writer.append("");
    	    writer.append(',');
    	    //Google Shopping / Custom Label 1
    	    writer.append("");
    	    writer.append(',');
    	    //Google Shopping / Custom Label 2
    	    writer.append("");
    	    writer.append(',');
    	    //Google Shopping / Custom Label 3
    	    writer.append("");
    	    writer.append('\n');
   	    
    	  //
       	    int i=1;
       	    while(imgList.size() > i){
       	    	//Handle
       	    	//writer.append("\"".concat(product_nr).concat("\""));
           	    writer.append(',');
           	    //Title
           	    writer.append("");
           	    writer.append(',');
           	    //Collection
           	    writer.append("");
           	    writer.append(',');
           	    //Body (HTML)
           	    writer.append("");
           	    writer.append(',');
           	    
           	    //Vendor
           	    writer.append("");
           	    writer.append(',');
           	    //Type
           	    writer.append("");
           	    writer.append(',');
           	    //Tags
           	    writer.append("");
           	    writer.append(',');
           	    //Published
           	    writer.append("");
           	    writer.append(',');
           	    //Option1 Name
           	    writer.append("");
           	    writer.append(',');
           	    //Option1 Value
           	    writer.append("");
           	    //writer.append("\"".concat(product_name).concat("\""));
           	    writer.append(',');   	    
           	    //Option2 Name
           	    writer.append("");
           	    writer.append(',');
           	    //Option2 Value
           	    writer.append("");
           	    writer.append(',');
           	    //Option3 Name
           	    writer.append("");
           	    writer.append(',');
           	    //Option3 Value
           	    writer.append("");
           	    writer.append(',');
           	    //Variant SKU
           	    writer.append("");
           	    writer.append(',');
           	    //Variant Grams
           	    writer.append("");
           	    writer.append(',');
           	    //Variant Inventory Tracker
           	    writer.append("");
           	    writer.append(',');
           	    //Variant Inventory Qty
           	    writer.append("");
           	    writer.append(',');
           	    //Variant Inventory Policy
           	    writer.append("");
           	    writer.append(',');
             	    //Variant Fulfillment Service
           	    writer.append("");
           	    writer.append(',');
             	    //Variant Price
           	    writer.append("");
           	    writer.append(',');
             	    //Variant Compare at Price
           	    writer.append("");
           	    writer.append(',');
             	    //Variant Requires Shipping
           	    writer.append("");
           	    writer.append(',');
             	    //Variant Taxable
           	    writer.append("");
           	    writer.append(',');
             	    //Variant Barcode
           	    writer.append("");
           	    writer.append(',');
             	    //Image Src
           	    writer.append(imgList.get(i));
           	    writer.append(',');
             	    //Image Alt Text 
           	    writer.append("");
           	    writer.append(',');
             	    //Gift Card
           	    writer.append("");
           	    writer.append(',');
           	    
           	    //Metafields
             	    //SEO Title
           	    writer.append("");
           	    writer.append(',');
             	    //SEO Description  
           	    writer.append("");
           	    writer.append(',');
             	    //Google Shopping / Google Product Category
           	    writer.append("");
           	    writer.append(',');
             	    //Google Shopping / Gender
           	    writer.append("");
           	    writer.append(',');
             	    //Google Shopping / Age Group
           	    writer.append("");
           	    writer.append(',');
             	    //Google Shopping / MPN
           	    writer.append("");
           	    writer.append(',');
             	    //Google Shopping / Adwords Grouping
           	    writer.append("");
           	    writer.append(',');
             	    //Google Shopping / Adwords Labels
           	    writer.append("");
           	    writer.append(',');
             	    //Google Shopping / Condition
           	    writer.append("");
           	    writer.append(',');
             	    //Google Shopping / Custom Product
           	    writer.append("");
           	    writer.append(',');
             	    //Google Shopping / Custom Label 0
           	    writer.append("");
           	    writer.append(',');
           	    //Google Shopping / Custom Label 1
           	    writer.append("");
           	    writer.append(',');
           	    //Google Shopping / Custom Label 2
           	    writer.append("");
           	    writer.append(',');
           	    //Google Shopping / Custom Label 3
           	    writer.append("");
           	    writer.append('\n');
          	       i++;
       	    }

    	    
      
    	    writer.flush();
    	    writer.close();
        } catch (FileNotFoundException e) { 
            // File
            e.printStackTrace(); 
        } catch (IOException e) { 
            // BufferedWriter
            e.printStackTrace(); 
        }
        
    }
    public static String searProductInAmazon(List<String> product) throws IOException{
    	String searchString = product.get(0).replace(' ', '+') + "+" + product.get(1).replace(' ', '+');
    	String searchUrl = "http://www.amazon.de/s/ref=nb_sb_noss?__mk_de_DE=%C3%85M%C3%85%C5%BD%C3%95%C3%91&url=search-alias%3Daps&field-keywords=" + searchString;
    	
    	Document doc = Jsoup.connect(searchUrl)
      			 .data("query", "Java")
      			  .userAgent("Mozilla")
      			  .cookie("auth", "token")
      			  .timeout(30000)
      			  .post();
    	    
        Elements link_es = doc.getElementsByClass("s-access-detail-page");
        
        if(link_es.size()>0)
        	return link_es.get(0).select("a").first().attr("href");
        else
        	return "";
    }
    
    public static String calProductType(String type, String brand){
    	if(type.contains("14") && type.contains("86")){//AUTOSITZ
    		if(brand.contains("CYBEX")){
    			type = "14,86,87";
    		}
    		if(brand.contains("BRITAX")){
    			type = "14,86,90";
    		}
    		if(brand.contains("MAXI-COSI")){
    			type = "14,86,91";
    		}
    		if(brand.contains("OSANN")){
    			type = "14,86,92";
    		}
    		if(brand.contains("RECARO")){
    			type = "14,86,93";
    		}
    		if(brand.contains("RMER")){
    			type = "14,86,94";
    		}
    		if(brand.contains("CONCORD")){
    			type = "14,86,143";
    		}
    		if(brand.contains("STM")){
    			type = "14,86,168";
    		}
    	}else if(type.contains("14") && type.contains("170")){//trolly
    		if(brand.contains("BABY-PLUS")){
    			type = "14,170,171";
    		}else if(brand.contains("CHICCO")){
    			type = "14,170,172";
    		}else if(brand.contains("HARTAN")){
    			type = "14,170,173";
    		}else if(brand.contains("GESSLEIN")){
    			type = "14,170,174";
    		}else if(brand.contains("PEG")){
    			type = "14,170,175";
    		}else if(brand.contains("MAXI-COSI")){
    			type = "14,170,176";
    		}else if(brand.contains("QUINNY")){
    			type = "14,170,177";
    		}else if(brand.contains("BABYJOGGER")){
    			type = "14,170,178";
    		}
    	}else if(type.contains("179")){//trolly
    		if((brand.contains("WMF")) && (brand.contains("Kaffee"))){ //WMF 咖啡机
    			type = "179,190,182";
    		}else if(brand.contains("WMF") && brand.contains("Wasserk")){ //WMF 咖啡机
    			type = "179,191,192";
    		}else if(brand.contains("WMF") && brand.contains("Standm")){ //WMF 咖啡机
    			type = "179,193,194";
    		}else if(brand.contains("WMF") && brand.contains("Toaster")){ //WMF 咖啡机
    			type = "179,195,196";
    		}else if(brand.contains("WMF") && brand.contains("Handmixer")){ //WMF 咖啡机
    			type = "179,197,198";
    		}else if(brand.contains("WMF") && brand.contains("Stabmixer")){ //WMF 咖啡机
    			type = "179,184,199";
    		}else if(brand.contains("De'Longhi")){ //WMF 咖啡机
    			type = "179,190,181";
    		}else if(brand.contains("BRAUN") && brand.contains("Rasie")){ //WMF 咖啡机
    			type = "179,183,180";
    		}else if(brand.contains("BRAUN") && brand.contains("Stabmixer")){ //WMF 咖啡机
    			type = "179,184,185";
    		}else if(brand.contains("BRAUN") && brand.contains("Zahnb")){ //WMF 咖啡机
    			type = "179,186,187";
    		}else if(brand.contains("BRAUN") && brand.contains("Epilie")){ //WMF 咖啡机
    			type = "179,188,189";
    		}
    	}
		return type;
    }
    /**
    *
    * @param s
    * @return Fissler,cvs,shopify
     * @throws Exception 
    *
    */
   public static boolean getProductDetailsMagento(List<String> product, String type, String vendor) throws Exception{
   	   String url = searProductInAmazon(product);
   	   String product_name = product.get(0);
       System.out.println("===========================");
       System.out.println(product_name);
       
       //       
       Integer indexEmpty = product_name.indexOf(' ');
       String manufacturer = vendor;
       String product_brand = product_name.substring(0, indexEmpty);
       //
       type = calProductType(type, product_name);
       
       //this product is not found in amazon
       if(url.length()<=0)
    	   return false;
   		
	   Document doc = Jsoup.connect(url)
   			 .data("query", "Java")
   			  .userAgent("Mozilla")
   			  .cookie("auth", "token")
   			  .timeout(30000)
   			  .post();
       
   		//
        List<Pair<String, String>> techDets = getProductTechDet(doc);
        //
        String short_desc = "";
        Translate translator = new Translate();
        if(techDets.size()>0){
     	   for(int i=0; i<techDets.size()-1; i++){
     		   //根据产品门类调用不同翻译器
     		   short_desc = short_desc.concat("<li><b>").
     				   concat(translator.translate(techDets.get(i).getFirst(),type)).
     				   concat("</b>:&nbsp");
     		   short_desc = short_desc.concat(translator.translate(techDets.get(i).getSecond(),type)).
     				   concat("</li>");
     	   }
        }
        short_desc = "<ul>"+short_desc+"</ul>";
        //System.out.println("short_desc: "+short_desc);
        
        //
        List<Pair<String, String>> techZusatz = getProductZusatzInfo(doc);
        //ASIN
        String asin = getProductASIN(techZusatz);  
        //System.out.println("ASIN: "+asin);    
        //
        String weightInclPack = getProductWeightInclPack(techZusatz);
        System.out.println("weight incl. packung: "+weightInclPack);
        
        //
        //String product_nr = getProductNumber(techDets); 
        //if(product_nr.length()<1){
     	   String product_nr = asin;
     	   if(product_nr.length()<1){
     		   product_nr = product_name.replace(" ", "-");
     	   }
        //}  
        //System.out.println("prod. nr.: "+product_nr);    
     	   
        String product_weight = getProductWeight(techDets);
        String product_material = getProductMaterial(techDets);
        String product_color = getProductColor(techDets);
           	
        //
        String euroOldPrice = product.get(3);
        float old_price = 1000000;
        Integer indexPoint;
        if(euroOldPrice.length()>1){
        	indexPoint = euroOldPrice.indexOf('.');
        	euroOldPrice = euroOldPrice.substring(0, indexPoint+3).replace(",", "");
        	old_price = Float.parseFloat(euroOldPrice);
        }
        
        String euroNewPrice = product.get(2);
        float new_price = 1000000;
        if(euroNewPrice.length()>1){
        	indexPoint = euroNewPrice.indexOf('.');
        	euroNewPrice = euroNewPrice.substring(0, indexPoint+3).replace(",", "");
        	new_price = Float.parseFloat(euroNewPrice);
        }
        
        //计算运输成本
        Float transEuro = Float.parseFloat(weightInclPack)*5;
        //
 		
        Float old_price_trans = old_price+transEuro;
        Float new_price_trans = new_price+transEuro;
        //
        Float rate = getExchangeRate("EUR", "CNY");
        String new_price_rmb;
        if(new_price_trans < 1000000){
     	   new_price_rmb = Float.toString(new_price_trans*rate*(float)marge);
     	   new_price_rmb = new_price_rmb.substring(0, new_price_rmb.indexOf("."));
        }
        else 
     	   new_price_rmb = "";
        
        String old_price_rmb;
        if(old_price_trans < 1000000){
        	old_price_rmb = Float.toString(old_price_trans*rate);
        	old_price_rmb = old_price_rmb.substring(0, old_price_rmb.indexOf("."));
        }else
        	old_price_rmb = "";
        
        
 		//System.out.println("new price: "+new_price_rmb);
 		//System.out.println("old price: "+old_price_rmb); 		

   	    int qty = 5;
   	    //if( Float.parseFloat(old_price_rmb.replace(",",".")) > 10000)
   	    if(old_price_rmb.length()<1)
   	    	qty = 0;
        
 		boolean productExists = productExists(product_nr);
 		
 		
 		if(productExists){
 			System.out.println("该产品已经在海淘速达店里");
 			try { 
 		       	
 		   	    FileWriter writer = new FileWriter("vendor_update.csv", true);
 		   	   
 		   	    //
 		   	    writer.append("\""+manufacturer+"\","+product_nr+"\","+old_price_rmb+","+new_price_rmb+","+qty+"\n");   
 		   	    writer.flush();
 		   	    writer.close();
 			}catch (FileNotFoundException e) { 
 				// File
 				e.printStackTrace(); 
 			} catch (IOException e) { 
 				// BufferedWriter
 				e.printStackTrace(); 
 			}
 		}
 		
 		else{
 			System.out.println("该产品尚未在店里");
 			
 			String product_desc = getProductDesc(doc);
 			//System.out.println("product_desc: "+product_desc);
       
 			
 			List<String> imgList = getProductImgs(doc);
 			//download, upload all images
 			for(int i=0; i<imgList.size(); i++){
 				new DownloadImage(imgList.get(i), asin+"_"+i+".jpg");
 			}
	    	   	
 			//
 			//String vendor = getProductVendor(SKU);
       
 			//cvs
 			try { 
       	
 				FileWriter writer = new FileWriter("vendor_new.csv", true);
   	   
 				//store, websites, attribute_set, configurable_attributes, type, category_ids
 				writer.append("\"admin\",\"base\",\"Default\",\"\",\"simple\",\""+type+"\",");
 				//sku,has_options,name,meta_title, meta_description
 				writer.append("\""+product_nr+"\", 0,\""+product_name+"\",\"\",\"\",");
 				
 				//image,small_image,thumbnail
 				/*String img_link = "";
 				if(imgList.size()>0) img_link = imgList.get(0);
 				String img = img_link.substring(img_link.lastIndexOf("/")).toLowerCase().replace("%", "_");
 				writer.append("\"+"+img_link+"\",+"+img_link+",+"+img_link+",");   */
 				writer.append("/"+asin+"_0.jpg,/"+asin+"_0.jpg,/"+asin+"_0.jpg,");
 				
 				//media_gallery
 				/*
 				for(int i=0; i<imgList.size(); i++){
 					if (i<imgList.size()-1)
 						writer.append("+"+imgList.get(i)+";");
 					else
 						writer.append("+"+imgList.get(i)+",");
 				}   */
 				for(int i=0; i<imgList.size(); i++){
 					if (i<imgList.size()-1)
 						writer.append("/"+asin+"_"+i+".jpg;");
 					else
 						writer.append("/"+asin+"_"+i+".jpg,");
 				}
 				
 				//url_key, url_path,custom_design,page_layout,options_container,image_label
 				writer.append("\""+product_name.replace(" ", "-")+"\",\""+product_name.replace(" ", "-")+".html\""+",\"\",1 column,Product Info Column,\"\",");  	    
 				//small_image_label,thumbnail_label,country_of_manufacture,msrp_enabled,msrp_display_actual_price_type
 				writer.append("\"\",\"\",\"\",Use config,Use config,");
 				//gift_message_available,gift_wrapping_available,manufacturer,status,is_recurring
 				writer.append("No,No,"+manufacturer+",Enabled,No,");   	    
 				//visibility,tax_class_id,color,apparel_type,sleeve_length
 				writer.append("\"Catalog, Search\",Taxable Goods,\""+product_color+"\",Knits,\"\",");
 				//fit,size,length,gender,description
 				writer.append(",S,,Male,\""+product_desc+"\",");
   	    
 				//short_description (tech data),meta_keyword,custom_layout_update,special_from_date,special_to_date
 				writer.append("\""+short_desc+"\",,,,,");
 				//news_from_date,news_to_date,custom_design_from,custom_design_to,price
 				writer.append("2013-03-01 00:00:00,,,,\""+old_price_rmb+"\",");
 				//special_price,weight,msrp,gift_wrapping_price,qty
   	    	
 				writer.append("\""+new_price_rmb+"\",\""+product_weight+"\",,,"+qty+",");
 				//min_qty,use_config_min_qty,is_qty_decimal,backorders,use_config_backorders
 				writer.append("0,1,0,0,1,");
 				//min_sale_qty,use_config_min_sale_qty,max_sale_qty,use_config_max_sale_qty,is_in_stock
 				if (qty == 0)
 					writer.append("1,1,0,1,0,");
 				else writer.append("1,1,0,1,1,");
 				//low_stock_date,notify_stock_qty,use_config_notify_stock_qty,manage_stock,use_config_manage_stock
 				writer.append(",,1,0,1,");
 				//stock_status_changed_auto,use_config_qty_increments,qty_increments,use_config_enable_qty_inc,enable_qty_increments
 				writer.append("0,1,0,1,0,");
 				//is_decimal_divided,stock_status_changed_automatically,use_config_enable_qty_increments,product_name,store_id
 				writer.append("0,0,1,Lexington Cardigan Sweater,0,");
 				//product_type_id,product_status_changed,product_changed_websites
 				writer.append("simple,,");
 				writer.append('\n');
   	    
 				//Handle
 				//writer.append("\"".concat(product_nr).concat("\""));
 				//writer.append(',');
  	        
 				writer.flush();
 				writer.close();
 			} catch (FileNotFoundException e) { 
 				// File
 				e.printStackTrace(); 
 			} catch (IOException e) { 
 				// BufferedWriter
 				e.printStackTrace(); 
 			}
 		}
       return true;
   }

   public static boolean productExists(String SKU){	  
	   boolean exists = false;
	   try {
			
			CsvReader products = new CsvReader("amazon_old.csv");
		
			products.readHeaders();

			while (products.readRecord())
			{
				String SKU_old = products.get("sku");
				if(SKU_old != "" && (SKU.contains(SKU_old) || SKU_old.contains(SKU))){
					System.out.println("SKU: "+SKU+"; SKU_old: "+SKU_old);
					return true;
				}					
			}
	
			
			products.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	      
		return exists;	
   }

   public static boolean productActuel(String SKU, String Manufacturer){	  
	   boolean actuel = false;
	   
	   System.out.println("SKU: "+SKU+"; Manufacturer: "+Manufacturer);
			   
	   try {
			
			CsvReader products = new CsvReader("amazon_update.csv");
		
			products.readHeaders();

			//
			boolean Manu_exists = false;
			while (products.readRecord())
			{
				String SKU_actuel = products.get("sku");
				String Manufacturer_actuel = products.get("manufacturer");
				if(Manufacturer != "" && (Manufacturer.contains(Manufacturer_actuel) || Manufacturer_actuel.contains(Manufacturer))){	
					Manu_exists = true;
					if((SKU.contains(SKU_actuel) || SKU_actuel.contains(SKU))){
						System.out.println("SKU: "+SKU+"; SKU_actuel: "+SKU_actuel);
						return true;
					}
				}
			}			
			products.close();
			if(Manu_exists == false){
				System.out.println(Manufacturer+"");
			}
				actuel = true;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	      
		return actuel;	
   }

   
    public static String getProductVendor(String SKU){	   	
    	//
    	String csvFile = "wmf2015-2-23.csv";
    	BufferedReader br = null;
    	String line = "";
    	String cvsSplitBy = ","; 
    	String[] product;
    	String vendor = "";
    	try {     
    		br = new BufferedReader(new FileReader(csvFile));
    		while ((line = br.readLine()) != null) {     
    			// 
    			product = line.split(cvsSplitBy);
    			String pn = product[2];//
    			if(SKU.indexOf(pn) >= 0){
        			vendor = product[12]; //products[12]
        			return vendor;
    			}
    		}         		
     
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
    		e.printStackTrace();
    	} finally {
    		if (br != null) {
    			try {
    				br.close();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    	}
		return vendor;
    }
    
    /**
    *
    * @param s
    * @return Fissler
     * @throws IOException 
    *
    */
    public List<ArrayList<String>> getDMLinks(String url) throws IOException {
    	List<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
    	String preffix = "https://www.dm.de";
    	
    	//number of pages
    	int pageNr=1;
    	
    	
    	String url_this = url;
    			
        Document doc = Jsoup.connect(url)
   			 .data("query", "Java")
			  .userAgent("Mozilla")
			  .cookie("auth", "token")
			  .timeout(30000)
			  .post();
       
        //how many pages there are
        Elements pageIndexes = doc.getElementsByClass("paging-items");
        if(pageIndexes.size()>0){
        	Element pageIndex = pageIndexes.first();
        	pageNr = pageIndex.childNodeSize();
        }
        
        for(int page=1; page<=pageNr; page++){
        	url_this = url+"?cp="+page;
            doc = Jsoup.connect(url_this)
       			 .data("query", "Java")
    			  .userAgent("Mozilla")
    			  .cookie("auth", "token")
    			  .timeout(30000)
    			  .post();
        	Elements links_body = doc.getElementsByClass("product-tile-description");
        	//System.out.println("size: "+links_body.size());
        	for(int i=0; i<links_body.size();i++){
        		Element link_body = links_body.get(i);
        		Element link_dic = link_body.getElementsByTag("strong").first();
        		String link = link_dic.attr("href");
        		link = preffix.concat(link);
        		list.add(link);
        	}
        }
        //
        
        return list;
    }
    
    public List<ArrayList<String>> getVendorProductList(String csvFile) throws IOException{
    	List<ArrayList<String>> productList = new ArrayList<ArrayList<String>>();
    	try {
 			
 			CsvReader products = new CsvReader(csvFile);		
 			products.readHeaders();

 			while (products.readRecord())
 			{
 				ArrayList<String> product = new ArrayList<String>();
 				String artikel = products.get("Artikel");
 				product.add(artikel);
 				String color = products.get("DE Beschreibung");
 				product.add(color);
 				String einkaufP = products.get("EK");
 				product.add(einkaufP);
 				String uvp = products.get("UVP");
 				product.add(uvp);
 				String verkaufP = products.get("VK (netto)");
 				product.add(verkaufP);
 				productList.add(product);
 			}			
 			products.close();
 			
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}	
    	
    	return productList;
    }
    
    public List<String> getProductLinks(String url) throws IOException {
        List<String> list = new ArrayList<String>();
        Document doc = Jsoup.connect(url)
   			 .data("query", "Java")
			  .userAgent("Mozilla")
			  .cookie("auth", "token")
			  .timeout(30000)
			  .post();
        Elements link_es = doc.getElementsByClass("product-name");
    	for(Element link_e : link_es){
    		String link = link_e.select("a").first().attr("href");
    		list.add(link);
    	}
    	//more sites
    	if(doc.getElementsByClass("i-next").isEmpty()){
    		System.out.println("no further sites!");
    		return list;
    	}else{
    		String next = doc.getElementsByClass("i-next").first().attr("href");
    		System.out.println(next);
    		List<String> list_tmp = new ArrayList<String>();
    		list_tmp = getProductLinks(next);
    		list.addAll(list_tmp);
    		return list;
    	}
    }
    
    /**
	    *
	    * @param s
	    * @return Fissler,cvs,shopify
	     * @throws Exception 
	    *
	    */
	   public static void getProductDetailsMagento(String url, String type) throws Exception{
		   System.out.println("url: "+url);
		   
		   Document doc = Jsoup.connect(url)
	   				.referrer(url)
	   			 .data("query", "Java")
	   			  .userAgent("Mozilla")
	   			  .cookie("auth", "token")
	   			  .timeout(30000)
	   			  .post();
	
	
	   		
	   		//
	       String product_name = getProductTitle(doc);
	       System.out.println("===========================");
	       System.out.println(product_name);
	       
	       //
	       String manufacturer = getProductManu(type);
	       
	   		//
	        List<Pair<String, String>> techDets = getProductTechDet(doc);
	        //
	        String short_desc = "";
	        if(techDets.size()>0){
	     	   for(int i=0; i<techDets.size()-1; i++){
	     		   short_desc = short_desc.concat("<li><b>").concat(translate_WMF(techDets.get(i).getFirst())).concat("</b>:&nbsp");
	     		   short_desc = short_desc.concat(translate_WMF(techDets.get(i).getSecond())).concat("</li>");
	     	   }
	        }
	        short_desc = "<ul>"+short_desc+"</ul>";
	        System.out.println("short_desc: "+short_desc);
	        
	        //
	        List<Pair<String, String>> techZusatz = getProductZusatzInfo(doc);
	        //ASIN
	        String asin = getProductASIN(techZusatz);  
	        System.out.println("ASIN: "+asin);    
	        //
	        String weightInclPack = getProductWeightInclPack(techZusatz);
	        System.out.println("weight incl. packung: "+weightInclPack);
	        
	        //
	        String product_brand = getProductBrand(techDets);
	        //String product_nr = getProductNumber(techDets); 
	        //if(product_nr.length()<1){
	     	   String product_nr = asin;
	     	   if(product_nr.length()<1){
	     		   product_nr = product_name.replace(" ", "-");
	     	   }
	        //}  
	        System.out.println("prod. nr.: "+product_nr);    
	     	   
	        String product_weight = getProductWeight(techDets);
	        String product_material = getProductMaterial(techDets);
	           	
	        //
	        List<String> prices = getProductPrices(doc);    
	        String old_price = prices.get(0); 
	        String new_price = prices.get(1); 
	        if(new_price.length()<1) new_price="-100000000";
	        //,4
	        Float transEuro = Float.parseFloat(weightInclPack)*5;
	        //
	
	 		System.out.println("new price eu: "+new_price);
	 		System.out.println("old price eu: "+old_price);
	 		
	        Float old_price_trans = Float.parseFloat(old_price)+transEuro;
	        Float new_price_trans = Float.parseFloat(new_price)+transEuro;
	        //
	        Float rate = getExchangeRate("EUR", "CNY");
	        double g=1.2;
	        Float marge = (float)g;
	        String new_price_rmb;
	        if(new_price_trans>0){
	     	   new_price_rmb = Float.toString(new_price_trans*rate*marge);
	     	   new_price = new_price_rmb.substring(0, new_price_rmb.indexOf("."));
	        }
	        else 
	     	   new_price = "";
	        String old_price_rmb = Float.toString(old_price_trans*rate*marge);
	        old_price = old_price_rmb.substring(0, old_price_rmb.indexOf("."));
	        
	        //System.out.println("===========================");
	 		System.out.println("new price: "+new_price);
	 		System.out.println("old price: "+old_price); 		
	
	   	    int qty = 5;
	   	    if( Float.parseFloat(old_price.replace(",",".")) > 10000)
	   	    	qty = 0;
	        
	 		boolean productExists = productExists(product_nr);
	 		
	 		
	 		if(productExists){
	 			System.out.println("");
	 			try { 
	 		       	
	 		   	    FileWriter writer = new FileWriter("amazon_update.csv", true);
	 		   	   
	 		   	    //
	 		   	    writer.append("\""+manufacturer+"\","+product_nr+"\","+old_price+","+new_price+","+qty+"\n");   
	 		   	    writer.flush();
	 		   	    writer.close();
	 			}catch (FileNotFoundException e) { 
	 				// File
	 				e.printStackTrace(); 
	 			} catch (IOException e) { 
	 				// BufferedWriter
	 				e.printStackTrace(); 
	 			}
	 		}
	 		
	 		else{
	 			System.out.println("");
	 			//
	 			String product_desc = getProductDesc(doc);
	 			System.out.println("product_desc: "+product_desc);
	       
	 			//
	 			List<String> imgList = getProductImgs(doc);
		    	   	
	 			//
	 			//String vendor = getProductVendor(SKU);
	       
	 			//cvs
	 			try { 
	       	
	 				FileWriter writer = new FileWriter("amazon_new.csv", true);
	   	   
	 				//store, websites, attribute_set, configurable_attributes, type, category_ids
	 				writer.append("\"admin\",\"base\",\"Default\",\"\",\"simple\",\""+type+"\",");
	 				//sku,has_options,name,meta_title, meta_description
	 				writer.append("\""+product_nr+"\", 0,\""+product_name+"\",\"\",\"\",");
	 				//image,small_image,thumbnail
	 				String img_link = "";
	 				if(imgList.size()>0) img_link = imgList.get(0);
	 				String img = img_link.substring(img_link.lastIndexOf("/")).toLowerCase().replace("%", "_");
	 				writer.append("\"+"+img_link+"\",+"+img_link+",+"+img_link+",");   	    
	 				//media_gallery
	 				//http://www.amazon.de/Brita-Wasserfilter-Starterpaket-inklusive-Kartuschen/dp/B00MOIUL78/ref=sr_1_1?&s=kitchen&ie=UTF8&qid=1436433484&sr=1-1&keywords=brita+B00YASEP4K
	 				
	 				for(int i=0; i<imgList.size(); i++){
	 					if (i<imgList.size()-1)
	 						writer.append("+"+imgList.get(i)+";");
	 					else
	 						writer.append("+"+imgList.get(i)+",");
	 				}   	    
	 				//url_key, url_path,custom_design,page_layout,options_container,image_label
	 				writer.append("\""+product_name.replace(" ", "-")+"\",\""+product_name.replace(" ", "-")+".html\""+",\"\",1 column,Product Info Column,\"\",");  	    
	 				//small_image_label,thumbnail_label,country_of_manufacture,msrp_enabled,msrp_display_actual_price_type
	 				writer.append("\"\",\"\",\"\",Use config,Use config,");
	 				//gift_message_available,gift_wrapping_available,manufacturer,status,is_recurring
	 				writer.append("No,No,"+manufacturer+",Enabled,No,");   	    
	 				//visibility,tax_class_id,material,apparel_type,sleeve_length
	 				writer.append("\"Catalog, Search\",Taxable Goods,\""+product_material+"\",Knits,\"\",");
	 				//fit,size,length,gender,description
	 				writer.append(",S,,Male,\""+product_desc+"\",");
	   	    
	 				//short_description (tech data),meta_keyword,custom_layout_update,special_from_date,special_to_date
	 				writer.append("\""+short_desc+"\",,,,,");
	 				//news_from_date,news_to_date,custom_design_from,custom_design_to,price
	 				writer.append("2013-03-01 00:00:00,,,,\""+old_price+"\",");
	 				//special_price,weight,msrp,gift_wrapping_price,qty
	   	    	
	 				writer.append("\""+new_price+"\",\""+product_weight+"\",,,"+qty+",");
	 				//min_qty,use_config_min_qty,is_qty_decimal,backorders,use_config_backorders
	 				writer.append("0,1,0,0,1,");
	 				//min_sale_qty,use_config_min_sale_qty,max_sale_qty,use_config_max_sale_qty,is_in_stock
	 				if (qty == 0)
	 					writer.append("1,1,0,1,0,");
	 				else writer.append("1,1,0,1,1,");
	 				//low_stock_date,notify_stock_qty,use_config_notify_stock_qty,manage_stock,use_config_manage_stock
	 				writer.append(",,1,0,1,");
	 				//stock_status_changed_auto,use_config_qty_increments,qty_increments,use_config_enable_qty_inc,enable_qty_increments
	 				writer.append("0,1,0,1,0,");
	 				//is_decimal_divided,stock_status_changed_automatically,use_config_enable_qty_increments,product_name,store_id
	 				writer.append("0,0,1,Lexington Cardigan Sweater,0,");
	 				//product_type_id,product_status_changed,product_changed_websites
	 				writer.append("simple,,");
	 				writer.append('\n');
	   	    
	 				//Handle
	 				//writer.append("\"".concat(product_nr).concat("\""));
	 				//writer.append(',');
	  	        
	 				writer.flush();
	 				writer.close();
	 			} catch (FileNotFoundException e) { 
	 				// File
	 				e.printStackTrace(); 
	 			} catch (IOException e) { 
	 				// BufferedWriter
	 				e.printStackTrace(); 
	 			}
	 		}
	       
	   }

	public static String getProductTitle(Document doc){	
		Elements spans = doc.select("span[itemprop=name]");
		String title = spans.first().ownText();
		System.out.println("name: "+title);
        return title;    
    }
	
    public static String getProductManu(String type){
    	if(type.contains("141")){
    		return "Doppelherz";
    	}else if(type.contains(",62,") || type.contains(",108,")){
    		return "WMF";
    	}else if(type.contains(",109,")){
    		return "Zwillinge";
    	}else if(type.contains(",65,")){
    		return "LE CREUSET";
    	}else if(type.contains(",67,")){
    		return "FISSLER";
    	}else if(type.contains(",107,")){
    		return "Victorinox";
    	}else if(type.contains(",87")){
    		return "CYBEX";
    	}else if(type.contains(",88")){
    		return "Cybex Silver";
    	}else if(type.contains(",89")){
    		return "Cybex Gold";
    	}else if(type.contains(",90")){
    		return "Britax";
    	}else if(type.contains(",91")){
    		return "Maxi-Cosi";
    	}else if(type.contains(",92")){
    		return "OSANN";
    	}else if(type.contains(",93")){
    		return "Recaro";
    	}else if(type.contains(",94")){
    		return "RMER";
    	}else if(type.contains(",143")){
    		return "Concord";
    	}
    	return "";
    }
    
    public static String getProductDesc(Document doc) throws Exception{
    	Element desc_e = doc.getElementById("feature-bullets");
    	String desc = desc_e.html();
    	//remove return symbol
    	int zu=0, off=0;
    	while(1>0){
    		 zu = desc.indexOf(">", off);
    		 if(zu > desc.length()-3) break;
    		 off = desc.indexOf("<", zu);
    		 String to_remove="";
    		 if((off-zu)>1)
    			 to_remove = desc.substring(zu+1,off);
    		 if(to_remove.length()<6)
    			 to_remove = "";
    		 //else to_remove = BingTrans.execute(to_remove, "de", "zh-CHS");//
    	     desc = (desc.substring(0, zu+1))+to_remove+desc.substring(off, desc.length());
    	     String desc_tmp = desc.replace("\"", "'");
    	     desc = desc_tmp;
    	     off = zu+1;
    	}
    	desc.replace("", "");
        desc = translate_WMF(desc);
    	/*GoogleTrans googleTrans = new GoogleTrans();
    	desc = desc.replace(" ", "%20");
		// String transStr = tHTTPRequest.sendGoogleTrans(srcStr, "zh-CN", "en");
		String desc_cn = googleTrans.sendGoogleTrans(desc, "de", "zh-CN");*/
    	//System.out.println("===========================");
    	//System.out.println(desc);
    	return desc;
    }
    
    public static Float getExchangeRate(String from, String to) throws IOException{
    	Yahoo_exchange ycc = new Yahoo_exchange();
    	float current=10000;
        try {
        	current = exchange_rate;
            //current = ycc.convert("USD", "ILS");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return current;
    }
    
    public static List<String> getProductPrices(Document doc) throws IOException{ 

    	//Float rate = getExchangeRate("EUR", "CNY");
    	List<String> prices = new ArrayList<String>();
    	if(doc.getElementById("price")==null){
    		prices.add("100000");
    		prices.add("100000");
    		return prices;
    	}
    		
    	Element price_e = doc.getElementById("price").child(0);   //price tabel 
    	String old_price, new_price;
    	//System.out.println(price_e.child(0).child(1).html());
    	if(! (price_e.child(0).child(0).getElementsByClass("a-text-strike").isEmpty())){
    		System.out.println("child: "+price_e.child(0).child(0).child(1).text().replaceAll(".",""));
    		old_price = price_e.child(0).child(0).child(1).text().replaceAll("\\.","").replaceAll(",",".").replaceAll("EUR ",""); 
    		//old_price = Float.toString(Float.parseFloat(old_price)*rate);
    		old_price = old_price.substring(0,old_price.indexOf("."));
    		//System.out.println("old price: "+old_price);
    		//old_price = price_e.getElementsByClass("old-price").first().text();  
    		new_price = price_e.child(0).child(1).child(1).child(0).text().replaceAll("\\.","").replaceAll(",",".").replaceAll("EUR ","");  
    		//new_price = Float.toString(Float.parseFloat(new_price)*rate);
    		new_price = new_price.substring(0,new_price.indexOf("."));
    		
    		prices.add(old_price);
    		prices.add(new_price);
    	}else{
    		old_price = price_e.child(0).child(0).child(1).child(0).text(); 
    		old_price = old_price.substring(4).replaceAll("\\.","").replaceAll(",",".").replaceAll("EUR ","");
    		if(old_price.contains("-")){
    			old_price = "10000000";
    		}
    		else{
    			//new_price = Float.toString(Float.parseFloat(new_price)*rate);
    			//new_price = new_price.substring(0,new_price.indexOf("."));
    		}
    		prices.add(old_price);
    		prices.add("");
    	}
    	
    	
    	return prices;
    }
 

    public static List<String> getProductImgs(Document doc) {
    	String element_general = doc.data();
    	int start = element_general.indexOf("'colorImages'");
    	int end = element_general.indexOf("'colorToAsin'");
    	String img_body = element_general.substring(start, end);
    	
    	List<String> imgs = new ArrayList<String>();
    			
    	int loc_begin_o = img_body.indexOf("hiRes", 12);
    	int loc_begin = img_body.indexOf("http:", loc_begin_o);
    	if(loc_begin - loc_begin_o > 10){
    		loc_begin = img_body.indexOf("large", loc_begin_o);
    		loc_begin = img_body.indexOf("http:", loc_begin);
    	}
    	int loc_end = 0;
    	while (loc_begin > loc_end){
    		loc_end = img_body.indexOf(".jpg", loc_begin+12);
    		String img = img_body.substring(loc_begin, loc_end+4);
    		imgs.add(img);
    		loc_begin_o = img_body.indexOf("hiRes", loc_end+12);
        	loc_begin = img_body.indexOf("http:", loc_begin_o);
        	if(loc_begin - loc_begin_o > 10){
        		loc_begin = img_body.indexOf("large", loc_begin_o);
        		loc_begin = img_body.indexOf("http:", loc_begin);
        	}
    	}
    	
    	//System.out.println(imgs);
    	Elements img_es = doc.getElementsByClass("activelb");
    	for(Element img_e : img_es)	{
    		String img_l = img_e.attr("href");
    		imgs.add(img_l);
    	}
         
         return imgs;
    }
    
    //get product details
    public static List<Pair<String, String>> getProductTechDet(Document doc){
    	List<Pair<String, String>> techDets = new ArrayList<Pair<String,String>>();
    	if(doc.getElementsByClass("pdTab").size() < 1)
    		return techDets;
    	Element det_e = doc.getElementsByClass("pdTab").first().child(0);
    	Elements det_list = det_e.child(0).children();
    	for(int i=0; i<det_list.size(); i++){
    		Element det = det_list.get(i);
    		String title = det.child(0).text();
    		String data = det.child(1).text();  		
    		Pair<String, String> pair = new Pair<String, String>(title, data);
    		techDets.add(pair);
    	}
    	return techDets;
    }
    
    //get product details
    public static List<Pair<String, String>> getProductZusatzInfo(Document doc){
    	List<Pair<String, String>> techDets = new ArrayList<Pair<String,String>>();
    	if(doc.getElementsByClass("pdTab").size() < 1)
    		return techDets;
    	Element det_e = doc.getElementsByClass("pdTab").get(1).child(0);
    	Elements det_list = det_e.child(0).children();
    	for(int i=0; i<det_list.size(); i++){
    		Element det = det_list.get(i);
    		String title = det.child(0).text();
    		String data = det.child(1).text();  		
    		Pair<String, String> pair = new Pair<String, String>(title, data);
    		techDets.add(pair);
    	}
    	return techDets;
    }
    
    public static String getProductNumber(List<Pair<String, String>> techDets){
    	String number="";
    	for(int i=0; i<techDets.size();i++){
    		Pair<String,String> pair = techDets.get(i);
    		if(pair.getFirst().contains("Modellnummer")){
    			number = pair.getSecond();
    			break;
    		}
    	}
    	//if(number.length()<1) number = "000000";
    	return number;
    }
    
    public static String getProductASIN(List<Pair<String, String>> zusatzInfo){
    	String asin="";
    	for(int i=0; i<zusatzInfo.size();i++){
    		Pair<String,String> pair = zusatzInfo.get(i);
    		if(pair.getFirst().contains("ASIN")){
    			asin = pair.getSecond();
    			break;
    		}
    	}
    	//if(number.length()<1) number = "000000";
    	//System.out.println("prod. ASIN: " + asin);
    	return asin;
    } 
    public static String getProductWeightInclPack(List<Pair<String, String>> zusatzInfo){
    	String weightInclPack = "0.20";

    	for(int i=0; i<zusatzInfo.size();i++){
    		Pair<String,String> pair = zusatzInfo.get(i);
    		if(pair.getFirst().contains("Produktgewicht")){
    			String gewicht = pair.getSecond();
    			if(gewicht.contains("Kg"))
    				weightInclPack = gewicht.replace(" Kg", "").replace(",", ".");
    			else
    				weightInclPack = Float.toString(Float.parseFloat(gewicht.replace(" g", ""))/1000);
    			break;
    		}    		
    	}
    
    	return weightInclPack;
    }
    public static String getProductWeight(List<Pair<String, String>> techDets){
    	String weight="";
    	for(int i=0; i<techDets.size();i++){
    		Pair<String,String> pair = techDets.get(i);
    		if(pair.getFirst().contains("Artikelgewicht")){
    			weight = pair.getSecond().replace(" Kg", "").replace(" g", "");
    			break;
    		}
    	}
    	//System.out.println("prod. weight: " + weight);
    	return weight;
    }
    
    public static String getProductBrand(List<Pair<String, String>> techDets){
    	String brand = "";
    	for(int i=0; i<techDets.size();i++){
    		Pair<String,String> pair = techDets.get(i);
    		if(pair.getFirst().contains("Marke")){
    			brand = pair.getSecond();
    			break;
    		}
    	}
    	return brand;
    }
    

    public static String getProductMaterial(List<Pair<String, String>> techDets){
    	String material = "";
    	for(int i=0; i<techDets.size();i++){
    		Pair<String,String> pair = techDets.get(i);
    		if(pair.getFirst().contains("Material")){
    			material = pair.getSecond();
    			break;
    		}
    	}
    	return material;
    }
    

    public static String getProductColor(List<Pair<String, String>> techDets){
    	String color = "";
    	for(int i=0; i<techDets.size();i++){
    		Pair<String,String> pair = techDets.get(i);
    		if(pair.getFirst().contains("Farbe")){
    			color = pair.getSecond();
    			break;
    		}
    	}
    	return color;
    }

    public static String translate_WMF(String to_trans){
    	to_trans = to_trans.replaceAll("<span class='a-list-item'>","<span style='font-size:15px'>");
    	to_trans = to_trans.replaceAll("mit Metalldeckel", "").replace("Steckdeckel aus Glas", "").replace("Steckdeckel aus hitzebestndigem Gteglas", "");
    	to_trans = to_trans.replaceAll("ist formstabil und unverwstlich","").replaceAll("geschmacksneutral und bestndig gegen Speisesuren",",");
    	to_trans = to_trans.replaceAll("Edelstahl Rostfrei 18/10","18/10").replaceAll("fr alle Herdarten geeigntet","").replaceAll("auch fr Induktion","").replaceAll("Bewhrter Schttrand fr leichtes abgieen","");
    	to_trans = to_trans.replaceAll("mit Cool+ Grifftechnologie","").replace("reduziert effektiv die Wrmebertragung vom Topf auf die Griffe","").replace("Kochen fr Geniesser","");
    	to_trans = to_trans.replaceAll("Guetesiegel: ","").replaceAll("Getesiegel: ","").replace("Allherdboden","").replace("auch Induktion","").replaceAll("fr alle Herdarten geeignet","").replaceAll("Geprfte Sicherheit","");
    	to_trans = to_trans.replaceAll("hitzebestndiger Glasdeckel mit Dampfffnung fr kontrolliertes Abdampfen","").replaceAll("ansprechendes Design","").replaceAll("bestndig gegen alle Speisesuren","").replaceAll("da zu passt","");
    	to_trans = to_trans.replaceAll("Griffe aus Edelstahl","").replace("mit Gteglasdeckel", "").replace("die beim Kochen nicht hei werden", "").replaceAll("hochwertiger Gteglasdeckel bis 180C mit Silikonring", "180");
    	to_trans = to_trans.replaceAll("Kochgeschirr Bodenmarke , funktion 4 Signet - Innovation","").replace("Stiftung Warentest", "").replace("design award", "").replace("red dot award", "").replace("Design Zentrum", "").replaceAll("Design Plus Messe Frankfurt", "").replaceAll("Nominierung fr den", "");
    	to_trans = to_trans.replaceAll("teilig", "").replaceAll(", ohne Einsatz","").replaceAll("innen grau beschichtet","").replaceAll("Keramik-Beschichtung","").replaceAll("ohne Einsatz","").replaceAll("Edelstahl Rostfrei", "").replaceAll("Tpfe sind stapelbar","").replaceAll("Topfset Tpfe","").replaceAll("Topfset Tpfe Kochtopfset","").replaceAll("fr aller Herdarten geeignet","");
    	to_trans = to_trans.replaceAll("mit Schttrand fr zielsicheres Ausgieen","").replaceAll("Cromargan ist geschmacksneutral und hygienisch und splmaschinenbestndig","Cromargan,,").replaceAll("sitzt perfekt auf dem","").replaceAll("ermglicht den Garprozess visuell zu berwachen","").replaceAll("dank DuPont Select Antihaftbeschichtung gut geeignet fr fettarmes Braten","DuPont Select");
    	to_trans = to_trans.replaceAll("Die Artikel dieser Serie knnen Sie kombinieren","").replaceAll("Die Kombination aus Servierplatte, Topf und Porzelanschale bietet vielseitige Einsatzmglichkeiten zum Vorbereiten, Kochen, Warmhalten und Servieren",",,").replaceAll("Kunststoffgriff mit Flammschutz","").replaceAll("DuPont Select Antihaftbeschichtung","DuPont Select").replaceAll("beste Antihafteigenschaften","");
    	to_trans = to_trans.replaceAll("Griff fr Pfanne","").replaceAll("auf mineralischer Basis","").replaceAll("Model Jahr","").replaceAll("Model Name","").replaceAll("Art des Gurtes","").replaceAll("Besone Merkmale","").replaceAll("Maximale Gre des Kindes","").replaceAll("Mindestgre des Kindes","").replaceAll("Empfohlenes maximales Krpergewicht","").replaceAll("Empfohlenes minimales Krpergewicht","").replaceAll("Bentigt Batterien","").replaceAll("sehr gute Antihaftfhigkeit","").replaceAll("extrem hitzebestndig","").replaceAll("Durit Select Pro Antihaftbeschichtung","Durit Select Pro").replaceAll("dank Durit Select Pro Antihaftbeschichtung gut geeignet fr fettarmes Braten","Durit Select Pro").replaceAll("Aluguss mit Durit Protect Plus Antihaft-Versiegelung","Durit Protect Plus").replaceAll("geeignet fr alle Herdarten auer","").replaceAll("Ergonomischer Stilgriff aus Kunststoff","").replaceAll("Ergonomischer Stilgriff","").replaceAll("Pfanne flach","").replaceAll("Die Porzellanschale passt in den Topf, der Topfdeckel auf die Porzelanschale, die Schale und der Topf auf die Servierplatte. Diese kann als Tablett, Servierteller oder Platzteller verwendet werden. Die Tpfe sind stapelbar",",,,");
    	to_trans = to_trans.replaceAll("Dichtungsring","").replaceAll("Gre","").replaceAll("V ca.","").replace("Material", "").replaceAll("Glas","").replaceAll("poliert", "").replaceAll("mattiert","").replaceAll("matt","").replaceAll("Aluguss","").replaceAll("Inhalt", "").replace("stapelbar", "");
    	to_trans = to_trans.replaceAll("splmaschinenfest", "").replace("Antihaftversiegelung","").replace("Steckdeckel","").replace("Deckel", "").replace("deckel", "").replaceAll("Auszeichnung", "").replace("Hannover", "").replace("Kollektion/Serie", "").replace("Kollektion", "").replaceAll("Antihaftbeschichtung","").replaceAll("Keramikbeschichtung","");
    	to_trans = to_trans.replaceAll("Bratentopf","").replaceAll("Stielpfanne","").replaceAll("Auflaufgericht","").replaceAll("Bratentpfen","").replaceAll("Fleischtopf","").replaceAll("Fleischtpfen","").replaceAll("WMF", "WMF ").replaceAll("Schnellkochtopf", "").replaceAll("Schnelltopf","").replaceAll("-Set", "").replaceAll("Set", "").replace("hitzebestndiges ", "");
    	to_trans = to_trans.replaceAll("Durchmesser","").replaceAll("Marke","").replace("Farbe", "").replace("Schwarz","").replace("Grau", "").replace("Silber", "").replace("hoch","").replace("Metall", "").replace("Pfannenset","").replace("induktionsgeeignet","").replace("inkl.","").replace("Pfannenwender","").replace("NEU","").replace("OVP","");
    	to_trans = to_trans.replaceAll("Salatreiher","").replaceAll("Servierpfanne","").replaceAll("Kuechenschuessel","").replaceAll("Passiermuehle","").replaceAll("Griff","").replaceAll("stiel","").replaceAll("Kochgeschirrset","").replaceAll("Pfanne","").replaceAll("Kochgeschirr","").replace("Induktion","").replace("Edelstahl","").replace("Aluminium","").replace("Haushaltswaren","").replace("Topf","").replace("-tlg","").replace("formstabil","").replace("geschmacksneutral","");
    	to_trans = to_trans.replaceAll("Splmaschinenfest","").replaceAll("jeweils","").replaceAll("Gewrzmhle","").replaceAll("GRATIS","").replaceAll("Stielkasserolle","").replaceAll("Topfset","").replaceAll("Kennzeichnung","");
    	to_trans = to_trans.replaceAll("Ausfhrung","").replaceAll("Besonheiten","").replaceAll("beschichtet","").replaceAll("Spritzschutz","").replaceAll("pflegeleicht","").replaceAll("backofenfester","").replaceAll("Pfannen","").replaceAll("geblasen","").replaceAll("splmaschinenbesndig","").replaceAll("unverwstlich","").replaceAll("Bodenmarke","");
    	to_trans = to_trans.replaceAll("Restdrucksicherung","").replaceAll("Schnellpfanne","").replaceAll("Bestandteile","");
    	to_trans = to_trans.replaceAll("und","").replace("bis", "").replace("mit", "").replace("ohne", "").replace("Mit", "").replace("Nein", "").replace("der", "").replace("Der", "").replace("die", "").replace("Die", "").replace("das", "").replace("Das", "");
    	to_trans = to_trans.replaceAll("Neigungsvegstellbare","").replaceAll("Modellnummer","").replaceAll("Artikelgewicht","").replaceAll("Produktabmessungen","").replace("Sitzbreite","");
    	return to_trans;//.replace(" ", "");
    }
    
    
    public static void main(String[] args) throws Exception {
    	try{
    		File file_update = new File("DM_update.csv");
    		File file_new = new File("DM_new.csv");
 
    		if(file_new.delete() && file_update.delete()){
    			System.out.println(file_new.getName() + " and "+ file_update.getName() + " is deleted!");
    		}else{
    			System.out.println("Delete operation is failed.");
    		}
 
    		FileWriter writer = new FileWriter("DM_new.csv", true);
    		writer.append("store,websites,attribute_set,configurable_attributes,type,category_ids,sku,has_options,name,meta_title,meta_description,image,small_image,thumbnail,media_gallery,url_key, url_path,custom_design,page_layout,options_container,image_label,small_image_label,thumbnail_label,country_of_manufacture,msrp_enabled,msrp_display_actual_price_type,gift_message_available,gift_wrapping_available,manufacturer,status,is_recurring,visibility,tax_class_id,material,apparel_type,sleeve_length,fit,size,length,gender,description,short_description,meta_keyword,custom_layout_update,special_from_date,special_to_date,news_from_date,news_to_date,custom_design_from,custom_design_to,price,special_price,weight,msrp,gift_wrapping_price,qty,min_qty,use_config_min_qty,is_qty_decimal,backorders,use_config_backorders,min_sale_qty,use_config_min_sale_qty,max_sale_qty,use_config_max_sale_qty,is_in_stock,low_stock_date,notify_stock_qty,use_config_notify_stock_qty,manage_stock,use_config_manage_stock,stock_status_changed_auto,use_config_qty_increments,qty_increments,use_config_enable_qty_inc,enable_qty_increments,is_decimal_divided,stock_status_changed_automatically,use_config_enable_qty_increments,product_name,store_id,product_type_id,product_status_changed,product_changed_websites");
    		writer.append('\n');  	  
    		writer.flush();
    		writer.close();
   	    
    		
    		writer = new FileWriter("DM_update.csv", true);
    		writer.append("manufacturer,sku,price,special_price,qty");
    		writer.append('\n');  	  
   	    	writer.flush();
   	   		writer.close();
   	   		

   	   		
    	}catch(Exception e){ 
    		e.printStackTrace(); 
    	}
   	    
        AnalyseDM t = new AnalyseDM(); 
        
        //doppelt herz
        List<ArrayList<String>> p = t.getDMLinks("https://www.dm.de/marken/doppelherz");
        /*for(int i=0; i<p.size();i++){
        	String pro_link = p.get(i); 
        	System.out.println(pro_link);
        	getProductDetailsMagento(pro_link, "6,18, 141");
        }*/
        
       
        //amazon
 	   	try {
 			
 			CsvReader products = new CsvReader("amazon_old.csv");		
 			products.readHeaders();

 			while (products.readRecord())
 			{
 				String SKU_old = products.get("sku");
 				String Manufacturer = products.get("manufacturer");
 				if(SKU_old != ""){
 					if(! productActuel(SKU_old, Manufacturer)){
 						System.out.println("product outdated");
 						FileWriter writer = new FileWriter("amazon_update.csv", true);
 			    		//manufacturer,sku,price,special_price,qty
 						writer.append("\""+Manufacturer+"\","+SKU_old+"\"10000, 10000, 0");
 						writer.append('\n');  	  
 						writer.flush();
 		   	   			writer.close();
 					}
 				}
 			}			
 			products.close();
 			
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}	

	   		//upload images
	   		File folder = new File("/Users/weiding/Desktop/product_images/");
	   		File[] listOfFiles = folder.listFiles();

	   		for (File file : listOfFiles) {
	   			if (file.isFile()) {
	   				new UploadImage(file.getName());
	   			}
	   		}
    }
}