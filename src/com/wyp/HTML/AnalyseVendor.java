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
import com.wyp.utils.Pair;
/**
 *
 * @author yaohucaizi
 */
public class AnalyseVendor {
	private static double marge = 1.3;

    /**
     * 读取网页全部内容
     */
    public String getHtmlContent(String htmlurl) {
        URL url;
        String temp;
        StringBuffer sb = new StringBuffer();
        try {
            url = new URL(htmlurl);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "gbk"));// 读取网页全部内容
            while ((temp = in.readLine()) != null) {
                //sb.append(temp);
				sb.append(temp).append(System.getProperty("line.separator"));
            }
            in.close();
        } catch (final MalformedURLException me) {
            System.out.println("你输入的URL格式有问题!");
            me.getMessage();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     *
     * @param s
     * @return 获得网页标题
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
     * @return 获得链接
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
     * @return 获得脚本代码
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
     * @return 获得CSS
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
     * @return 去掉标记
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
     * @return 读取Fissler的产品细节,更新cvs文件,给shopify
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
    	//读取产品名称
        String product_name = getProductTitle(doc);
        System.out.println("===========================");
        System.out.println(product_name);

        //读取产品描述in feature-bullets
        String product_desc = getProductDesc(doc);
        
        //读取产品号
        //String product_nr = getProductNumber(doc);
         
    	
        //读取价格
        List<String> prices = getProductPrices(doc);    
        String old_price = prices.get(0);    
        String new_price = prices.get(1);
        //System.out.println(old_price);
        //System.out.println(new_price);
        
        
        //读取图片
        List<String> imgList = getProductImgs(doc);
        
        String SKU = "";
	    //if(variations.size()==0) //no variations
	    	//SKU = product_nr.replaceAll(" ", "");
	    	//System.out.println(SKU);
	    /*else{
	    	SKU = "\"".concat((variations.get(0)).get(0)).concat("\"");
	   	}*/
	    	
        //读取供应商
        String vendor = getProductVendor(SKU);
        
        //更新cvs文件
        try { 
        	
    	    FileWriter writer = new FileWriter("products_export_wmf.csv", true);
    	    //写Handle
    	    //writer.append("\"".concat(product_nr).concat("\""));
    	    writer.append(',');
    	    //写Title
    	    writer.append("\"".concat(product_name).concat("\""));  
    	    writer.append(',');
    	    //写Collection
    	    writer.append("WMF");
    	    writer.append(',');
    	    //写Body (HTML)
    	    //writer.append("\"".concat(product_desc).concat("\""));
    	    writer.append(',');   	    
    	    //写Vendor
    	    writer.append(vendor);
    	    writer.append(',');
    	    //写Type
    	    writer.append(type);
    	    writer.append(',');
    	    //写Tags
    	    writer.append("\"".concat("WMF, ").concat(type).concat("\""));
    	    writer.append(',');
    	    //写Published
    	    writer.append("true");
    	    writer.append(',');
    	    //写Option1 Name
    	    writer.append("Title");
    	    writer.append(',');
    	    //写Option1 Value
    	    writer.append("Default Title");
    	    writer.append(',');   	    
    	    //写Option2 Name
    	    writer.append("");
    	    writer.append(',');
    	    //写Option2 Value
    	    writer.append("");
    	    writer.append(',');
    	    //写Option3 Name
    	    writer.append("");
    	    writer.append(',');
    	    //写Option3 Value
    	    writer.append("");
    	    writer.append(',');
    	    //写Variant SKU
    	    writer.append(SKU);
    	    writer.append(',');
    	    //写Variant Grams
    	    writer.append("1100");
    	    writer.append(',');
    	    //写Variant Inventory Tracker
    	    writer.append("");
    	    writer.append(',');
    	    //写Variant Inventory Qty
    	    writer.append("1");
    	    writer.append(',');
    	    //写Variant Inventory Policy
    	    writer.append("deny");
    	    writer.append(',');
      	    //写Variant Fulfillment Service
    	    writer.append("manual");
    	    writer.append(',');
      	    //写Variant Price
    	    writer.append("\"".concat(new_price).concat("\""));
    	    writer.append(',');
      	    //写Variant Compare at Price
    	    writer.append("\"".concat(old_price).concat("\""));
    	    writer.append(',');
      	    //写Variant Requires Shipping
    	    writer.append("true");
    	    writer.append(',');
      	    //写Variant Taxable
    	    writer.append("false");
    	    writer.append(',');
      	    //写Variant Barcode
    	    writer.append("");
    	    writer.append(',');
      	    //写Image Src
    	    if(imgList.size()>0) writer.append(imgList.get(0));
    	    else writer.append("");
    	    writer.append(',');
      	    //写Image Alt Text 
    	    writer.append("");
    	    writer.append(',');
      	    //写Gift Card
    	    writer.append("false");
    	    writer.append(',');
    	    
    	    //Metafields
      	    //写SEO Title
    	    writer.append("\"".concat(product_name).concat("\""));
    	    writer.append(',');
      	    //写SEO Description  
    	    writer.append("\"".concat(product_name).concat("\""));
    	    writer.append(',');
      	    //写Google Shopping / Google Product Category
    	    writer.append("");
    	    writer.append(',');
      	    //写Google Shopping / Gender
    	    writer.append("");
    	    writer.append(',');
      	    //写Google Shopping / Age Group
    	    writer.append("");
    	    writer.append(',');
      	    //写Google Shopping / MPN
    	    writer.append("");
    	    writer.append(',');
      	    //写Google Shopping / Adwords Grouping
    	    writer.append("");
    	    writer.append(',');
      	    //写Google Shopping / Adwords Labels
    	    writer.append("");
    	    writer.append(',');
      	    //写Google Shopping / Condition
    	    writer.append("new");
    	    writer.append(',');
      	    //写Google Shopping / Custom Product
    	    writer.append("");
    	    writer.append(',');
      	    //写Google Shopping / Custom Label 0
    	    writer.append("");
    	    writer.append(',');
    	    //写Google Shopping / Custom Label 1
    	    writer.append("");
    	    writer.append(',');
    	    //写Google Shopping / Custom Label 2
    	    writer.append("");
    	    writer.append(',');
    	    //写Google Shopping / Custom Label 3
    	    writer.append("");
    	    writer.append('\n');
   	    
    	  //添加其他图片
       	    int i=1;
       	    while(imgList.size() > i){
       	    	//写Handle
       	    	//writer.append("\"".concat(product_nr).concat("\""));
           	    writer.append(',');
           	    //写Title
           	    writer.append("");
           	    writer.append(',');
           	    //写Collection
           	    writer.append("");
           	    writer.append(',');
           	    //写Body (HTML)
           	    writer.append("");
           	    writer.append(',');
           	    
           	    //写Vendor
           	    writer.append("");
           	    writer.append(',');
           	    //写Type
           	    writer.append("");
           	    writer.append(',');
           	    //写Tags
           	    writer.append("");
           	    writer.append(',');
           	    //写Published
           	    writer.append("");
           	    writer.append(',');
           	    //写Option1 Name
           	    writer.append("");
           	    writer.append(',');
           	    //写Option1 Value
           	    writer.append("");
           	    //writer.append("\"".concat(product_name).concat("\""));
           	    writer.append(',');   	    
           	    //写Option2 Name
           	    writer.append("");
           	    writer.append(',');
           	    //写Option2 Value
           	    writer.append("");
           	    writer.append(',');
           	    //写Option3 Name
           	    writer.append("");
           	    writer.append(',');
           	    //写Option3 Value
           	    writer.append("");
           	    writer.append(',');
           	    //写Variant SKU
           	    writer.append("");
           	    writer.append(',');
           	    //写Variant Grams
           	    writer.append("");
           	    writer.append(',');
           	    //写Variant Inventory Tracker
           	    writer.append("");
           	    writer.append(',');
           	    //写Variant Inventory Qty
           	    writer.append("");
           	    writer.append(',');
           	    //写Variant Inventory Policy
           	    writer.append("");
           	    writer.append(',');
             	    //写Variant Fulfillment Service
           	    writer.append("");
           	    writer.append(',');
             	    //写Variant Price
           	    writer.append("");
           	    writer.append(',');
             	    //写Variant Compare at Price
           	    writer.append("");
           	    writer.append(',');
             	    //写Variant Requires Shipping
           	    writer.append("");
           	    writer.append(',');
             	    //写Variant Taxable
           	    writer.append("");
           	    writer.append(',');
             	    //写Variant Barcode
           	    writer.append("");
           	    writer.append(',');
             	    //写Image Src
           	    writer.append(imgList.get(i));
           	    writer.append(',');
             	    //写Image Alt Text 
           	    writer.append("");
           	    writer.append(',');
             	    //写Gift Card
           	    writer.append("");
           	    writer.append(',');
           	    
           	    //Metafields
             	    //写SEO Title
           	    writer.append("");
           	    writer.append(',');
             	    //写SEO Description  
           	    writer.append("");
           	    writer.append(',');
             	    //写Google Shopping / Google Product Category
           	    writer.append("");
           	    writer.append(',');
             	    //写Google Shopping / Gender
           	    writer.append("");
           	    writer.append(',');
             	    //写Google Shopping / Age Group
           	    writer.append("");
           	    writer.append(',');
             	    //写Google Shopping / MPN
           	    writer.append("");
           	    writer.append(',');
             	    //写Google Shopping / Adwords Grouping
           	    writer.append("");
           	    writer.append(',');
             	    //写Google Shopping / Adwords Labels
           	    writer.append("");
           	    writer.append(',');
             	    //写Google Shopping / Condition
           	    writer.append("");
           	    writer.append(',');
             	    //写Google Shopping / Custom Product
           	    writer.append("");
           	    writer.append(',');
             	    //写Google Shopping / Custom Label 0
           	    writer.append("");
           	    writer.append(',');
           	    //写Google Shopping / Custom Label 1
           	    writer.append("");
           	    writer.append(',');
           	    //写Google Shopping / Custom Label 2
           	    writer.append("");
           	    writer.append(',');
           	    //写Google Shopping / Custom Label 3
           	    writer.append("");
           	    writer.append('\n');
          	       i++;
       	    }

    	    
      
    	    writer.flush();
    	    writer.close();
        } catch (FileNotFoundException e) { 
            // File对象的创建过程中的异常捕获
            e.printStackTrace(); 
        } catch (IOException e) { 
            // BufferedWriter在关闭对象捕捉异常
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
        String url = link_es.get(0).select("a").first().attr("href");
        return url;
    }
    
    public static String calProductType(String type, String brand){
    	if(type.contains("14") && type.contains("86")){
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
    		if(brand.contains("RÖMER")){
    			type = "14,86,94";
    		}
    		if(brand.contains("CONCORD")){
    			type = "14,86,143";
    		}
    		if(brand.contains("STM")){
    			type = "14,86,168";
    		}
    		if(brand.contains("CONCORD")){
    			type = "14,86,169";
    		}
    	}
		return type;
    }
    /**
    *
    * @param s
    * @return 读取Fissler的产品细节,更新cvs文件,给shopify
     * @throws Exception 
    *
    */
   public static void getProductDetailsMagento(List<String> product, String type, String vendor) throws Exception{
   	   String url = searProductInAmazon(product);
   	   String product_name = product.get(0);
       System.out.println("===========================");
       System.out.println(product_name);
       
       //读取产品品牌和经销商       
       Integer indexEmpty = product_name.indexOf(' ');
       String manufacturer = vendor;
       String product_brand = product_name.substring(0, indexEmpty);
       //计算产品分类
       type = calProductType(type, product_brand);
       System.out.println("type:" + type);
   		
	   Document doc = Jsoup.connect(url)
   			 .data("query", "Java")
   			  .userAgent("Mozilla")
   			  .cookie("auth", "token")
   			  .timeout(30000)
   			  .post();
       
   		//读取产品技术参数
        List<Pair<String, String>> techDets = getProductTechDet(doc);
        //形成技术参数表
        String short_desc = "";
        if(techDets.size()>0){
     	   for(int i=0; i<techDets.size()-1; i++){
     		   short_desc = short_desc.concat("<li><b>").concat(translate_WMF(techDets.get(i).getFirst())).concat("</b>:&nbsp");
     		   short_desc = short_desc.concat(translate_WMF(techDets.get(i).getSecond())).concat("</li>");
     	   }
        }
        short_desc = "<ul>"+short_desc+"</ul>";
        System.out.println("short_desc: "+short_desc);
        
        //读取产品附加参数
        List<Pair<String, String>> techZusatz = getProductZusatzInfo(doc);
        //读取ASIN
        String asin = getProductASIN(techZusatz);  
        System.out.println("ASIN: "+asin);    
        //读取产品连包装重量
        String weightInclPack = getProductWeightInclPack(techZusatz);
        System.out.println("weight incl. packung: "+weightInclPack);
        
        //读取产品号
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
        String product_color = getProductColor(techDets);
           	
        //读取价格
        String euroOldPrice = product.get(3);
        float old_price = 1000000;
        Integer indexPoint;
        if(euroOldPrice.length()>1){
        	indexPoint = euroOldPrice.indexOf('.');
        	euroOldPrice = euroOldPrice.substring(0, indexPoint+3);
        	old_price = Float.parseFloat(euroOldPrice);
        }
        
        String euroNewPrice = product.get(2);
        float new_price = 1000000;
        if(euroNewPrice.length()>1){
        	indexPoint = euroNewPrice.indexOf('.');
        	euroNewPrice = euroNewPrice.substring(0, indexPoint+3);
        	new_price = Float.parseFloat(euroNewPrice);
        }

 		System.out.println("new price eu: "+new_price);
 		System.out.println("old price eu: "+old_price);
        
        //计算运费,每公斤4欧元
        Float transEuro = Float.parseFloat(weightInclPack)*5;
        //计算总价：货值＋运费
 		
        Float old_price_trans = old_price+transEuro;
        Float new_price_trans = new_price+transEuro;
        //计算人民币价格
        Float rate = getExchangeRate("EUR", "CNY");
        String new_price_rmb;
        if(new_price_trans>0){
     	   new_price_rmb = Float.toString(new_price_trans*rate*(float)marge);
     	   new_price_rmb = new_price_rmb.substring(0, new_price_rmb.indexOf("."));
        }
        else 
     	   new_price_rmb = "";
        String old_price_rmb = Float.toString(old_price_trans*rate);
        old_price_rmb = old_price_rmb.substring(0, old_price_rmb.indexOf("."));
        
        System.out.println("===========================");
 		System.out.println("new price: "+new_price_rmb);
 		System.out.println("old price: "+old_price_rmb); 		

   	    int qty = 5;
   	    if( Float.parseFloat(old_price_rmb.replace(",",".")) > 10000)
   	    	qty = 0;
        
 		boolean productExists = productExists(product_nr);
 		
 		
 		if(productExists){
 			System.out.println("该产品已经在海淘速达网店里！");
 			try { 
 		       	
 		   	    FileWriter writer = new FileWriter("vendor_update.csv", true);
 		   	   
 		   	    //
 		   	    writer.append("\""+manufacturer+"\","+product_nr+"\","+old_price+","+new_price+","+qty+"\n");   
 		   	    writer.flush();
 		   	    writer.close();
 			}catch (FileNotFoundException e) { 
 				// File对象的创建过程中的异常捕获
 				e.printStackTrace(); 
 			} catch (IOException e) { 
 				// BufferedWriter在关闭对象捕捉异常
 				e.printStackTrace(); 
 			}
 		}
 		
 		else{
 			System.out.println("该产品尚未在海淘速达网店里！");
 			//读取产品描述
 			String product_desc = getProductDesc(doc);
 			System.out.println("product_desc: "+product_desc);
       
 			//读取图片
 			List<String> imgList = getProductImgs(doc);
	    	   	
 			//读取供应商
 			//String vendor = getProductVendor(SKU);
       
 			//更新cvs文件
 			try { 
       	
 				FileWriter writer = new FileWriter("vendor_new.csv", true);
   	   
 				//写store, websites, attribute_set, configurable_attributes, type, category_ids
 				writer.append("\"admin\",\"base\",\"Default\",\"\",\"simple\",\""+type+"\",");
 				//写sku,has_options,name,meta_title, meta_description
 				writer.append("\""+product_nr+"\", 0,\""+product_name+"\",\"\",\"\",");
 				//写image,small_image,thumbnail
 				String img_link = "";
 				if(imgList.size()>0) img_link = imgList.get(0);
 				String img = img_link.substring(img_link.lastIndexOf("/")).toLowerCase().replace("%", "_");
 				writer.append("\"+"+img_link+"\",+"+img_link+",+"+img_link+",");   	    
 				//写media_gallery
 				//http://www.amazon.de/Brita-Wasserfilter-Starterpaket-inklusive-Kartuschen/dp/B00MOIUL78/ref=sr_1_1?&s=kitchen&ie=UTF8&qid=1436433484&sr=1-1&keywords=brita+B00YASEP4K
 				
 				for(int i=0; i<imgList.size(); i++){
 					if (i<imgList.size()-1)
 						writer.append("+"+imgList.get(i)+";");
 					else
 						writer.append("+"+imgList.get(i)+",");
 				}   	    
 				//写url_key, url_path,custom_design,page_layout,options_container,image_label
 				writer.append("\""+product_name.replace(" ", "-")+"\",\""+product_name.replace(" ", "-")+".html\""+",\"\",1 column,Product Info Column,\"\",");  	    
 				//写small_image_label,thumbnail_label,country_of_manufacture,msrp_enabled,msrp_display_actual_price_type
 				writer.append("\"\",\"\",\"\",Use config,Use config,");
 				//写gift_message_available,gift_wrapping_available,manufacturer,status,is_recurring
 				writer.append("No,No,"+manufacturer+",Enabled,No,");   	    
 				//写visibility,tax_class_id,color,apparel_type,sleeve_length
 				writer.append("\"Catalog, Search\",Taxable Goods,\""+product_color+"\",Knits,\"\",");
 				//写fit,size,length,gender,description
 				writer.append(",S,,Male,\""+product_desc+"\",");
   	    
 				//写short_description (tech data),meta_keyword,custom_layout_update,special_from_date,special_to_date
 				writer.append("\""+short_desc+"\",,,,,");
 				//写news_from_date,news_to_date,custom_design_from,custom_design_to,price
 				writer.append("2013-03-01 00:00:00,,,,\""+old_price+"\",");
 				//写special_price,weight,msrp,gift_wrapping_price,qty
   	    	
 				writer.append("\""+new_price+"\",\""+product_weight+"\",,,"+qty+",");
 				//写min_qty,use_config_min_qty,is_qty_decimal,backorders,use_config_backorders
 				writer.append("0,1,0,0,1,");
 				//写min_sale_qty,use_config_min_sale_qty,max_sale_qty,use_config_max_sale_qty,is_in_stock
 				if (qty == 0)
 					writer.append("1,1,0,1,0,");
 				else writer.append("1,1,0,1,1,");
 				//写low_stock_date,notify_stock_qty,use_config_notify_stock_qty,manage_stock,use_config_manage_stock
 				writer.append(",,1,0,1,");
 				//写stock_status_changed_auto,use_config_qty_increments,qty_increments,use_config_enable_qty_inc,enable_qty_increments
 				writer.append("0,1,0,1,0,");
 				//写is_decimal_divided,stock_status_changed_automatically,use_config_enable_qty_increments,product_name,store_id
 				writer.append("0,0,1,Lexington Cardigan Sweater,0,");
 				//写product_type_id,product_status_changed,product_changed_websites
 				writer.append("simple,,");
 				writer.append('\n');
   	    
 				//写Handle
 				//writer.append("\"".concat(product_nr).concat("\""));
 				//writer.append(',');
  	        
 				writer.flush();
 				writer.close();
 			} catch (FileNotFoundException e) { 
 				// File对象的创建过程中的异常捕获
 				e.printStackTrace(); 
 			} catch (IOException e) { 
 				// BufferedWriter在关闭对象捕捉异常
 				e.printStackTrace(); 
 			}
 		}
       
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

			//监视是否在更新某个品牌
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
				System.out.println(Manufacturer+"没有在更新");
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
    	//读取供应商
    	String csvFile = "wmf2015-2-23.csv";
    	BufferedReader br = null;
    	String line = "";
    	String cvsSplitBy = ","; 
    	String[] product;
    	String vendor = "";
    	try {     
    		br = new BufferedReader(new FileReader(csvFile));
    		while ((line = br.readLine()) != null) {     
    			// 找产品条目
    			product = line.split(cvsSplitBy);
    			String pn = product[2];//报价单产品号
    			if(SKU.indexOf(pn) >= 0){
        			vendor = product[12]; //products[12]代表供应商
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
    * @return 读取Fissler的产品链接
     * @throws IOException 
    *
    */
    public List<String> getAmazonLinks(String url) throws IOException {
    	List<String> list = new ArrayList<String>();
        Document doc = Jsoup.connect(url)
   			 .data("query", "Java")
			  .userAgent("Mozilla")
			  .cookie("auth", "token")
			  .timeout(30000)
			  .post();
       
        //读取当页链接
        Elements links_body = doc.getElementsByClass("s-result-item");
        //System.out.println("size: "+links_body.size());
        for(int i=0; i<links_body.size();i++){
        	//System.out.println("i: "+i);
        	Element link_body = links_body.get(i);
        	Element link_dic = link_body.getElementsByClass("a-inline-block").first().child(0);
        	String link = link_dic.attr("href");
        	//System.out.println("html: "+link);
        	list.add(link);
        }
        //合并下一页的结果
        if(!(doc.getElementsByClass("pagnRA1").isEmpty())){//no next page

            //System.out.println("here!");
        	return list;    
        }
        else if(!(doc.getElementsByClass("pagnRA").isEmpty())){
            //System.out.println("there!");
        	
        	String link_next = "http://www.amazon.de"+doc.getElementsByClass("pagnRA").first().child(0).attr("href");
        	//System.out.println("next site: "+link_next);
        	List<String> list_next = new ArrayList<String>();
        	list_next = getAmazonLinks(link_next);
        	list.addAll(list_next);
        	return list;
        }
        else return list;
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
	    * @return 读取Fissler的产品细节,更新cvs文件,给shopify
	     * @throws Exception 
	    *
	    */
	   public static void getProductDetailsMagento(String url, String type) throws Exception{
	   		Document doc = Jsoup.connect(url)
	   			 .data("query", "Java")
	   			  .userAgent("Mozilla")
	   			  .cookie("auth", "token")
	   			  .timeout(30000)
	   			  .post();
	
	   		
	   		//读取产品名称
	       String product_name = getProductTitle(doc);
	       System.out.println("===========================");
	       System.out.println(product_name);
	       
	       //读取产品品牌
	       String manufacturer = getProductManu(type);
	       
	   		//读取产品技术参数
	        List<Pair<String, String>> techDets = getProductTechDet(doc);
	        //形成技术参数表
	        String short_desc = "";
	        if(techDets.size()>0){
	     	   for(int i=0; i<techDets.size()-1; i++){
	     		   short_desc = short_desc.concat("<li><b>").concat(translate_WMF(techDets.get(i).getFirst())).concat("</b>:&nbsp");
	     		   short_desc = short_desc.concat(translate_WMF(techDets.get(i).getSecond())).concat("</li>");
	     	   }
	        }
	        short_desc = "<ul>"+short_desc+"</ul>";
	        System.out.println("short_desc: "+short_desc);
	        
	        //读取产品附加参数
	        List<Pair<String, String>> techZusatz = getProductZusatzInfo(doc);
	        //读取ASIN
	        String asin = getProductASIN(techZusatz);  
	        System.out.println("ASIN: "+asin);    
	        //读取产品连包装重量
	        String weightInclPack = getProductWeightInclPack(techZusatz);
	        System.out.println("weight incl. packung: "+weightInclPack);
	        
	        //读取产品号
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
	           	
	        //读取价格
	        List<String> prices = getProductPrices(doc);    
	        String old_price = prices.get(0); 
	        String new_price = prices.get(1); 
	        if(new_price.length()<1) new_price="-100000000";
	        //计算运费,每公斤4欧元
	        Float transEuro = Float.parseFloat(weightInclPack)*5;
	        //计算总价：货值＋运费
	
	 		System.out.println("new price eu: "+new_price);
	 		System.out.println("old price eu: "+old_price);
	 		
	        Float old_price_trans = Float.parseFloat(old_price)+transEuro;
	        Float new_price_trans = Float.parseFloat(new_price)+transEuro;
	        //计算人民币价格
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
	        
	        System.out.println("===========================");
	 		System.out.println("new price: "+new_price);
	 		System.out.println("old price: "+old_price); 		
	
	   	    int qty = 5;
	   	    if( Float.parseFloat(old_price.replace(",",".")) > 10000)
	   	    	qty = 0;
	        
	 		boolean productExists = productExists(product_nr);
	 		
	 		
	 		if(productExists){
	 			System.out.println("该产品已经在海淘速达网店里！");
	 			try { 
	 		       	
	 		   	    FileWriter writer = new FileWriter("amazon_update.csv", true);
	 		   	   
	 		   	    //
	 		   	    writer.append("\""+manufacturer+"\","+product_nr+"\","+old_price+","+new_price+","+qty+"\n");   
	 		   	    writer.flush();
	 		   	    writer.close();
	 			}catch (FileNotFoundException e) { 
	 				// File对象的创建过程中的异常捕获
	 				e.printStackTrace(); 
	 			} catch (IOException e) { 
	 				// BufferedWriter在关闭对象捕捉异常
	 				e.printStackTrace(); 
	 			}
	 		}
	 		
	 		else{
	 			System.out.println("该产品尚未在海淘速达网店里！");
	 			//读取产品描述
	 			String product_desc = getProductDesc(doc);
	 			System.out.println("product_desc: "+product_desc);
	       
	 			//读取图片
	 			List<String> imgList = getProductImgs(doc);
		    	   	
	 			//读取供应商
	 			//String vendor = getProductVendor(SKU);
	       
	 			//更新cvs文件
	 			try { 
	       	
	 				FileWriter writer = new FileWriter("amazon_new.csv", true);
	   	   
	 				//写store, websites, attribute_set, configurable_attributes, type, category_ids
	 				writer.append("\"admin\",\"base\",\"Default\",\"\",\"simple\",\""+type+"\",");
	 				//写sku,has_options,name,meta_title, meta_description
	 				writer.append("\""+product_nr+"\", 0,\""+product_name+"\",\"\",\"\",");
	 				//写image,small_image,thumbnail
	 				String img_link = "";
	 				if(imgList.size()>0) img_link = imgList.get(0);
	 				String img = img_link.substring(img_link.lastIndexOf("/")).toLowerCase().replace("%", "_");
	 				writer.append("\"+"+img_link+"\",+"+img_link+",+"+img_link+",");   	    
	 				//写media_gallery
	 				//http://www.amazon.de/Brita-Wasserfilter-Starterpaket-inklusive-Kartuschen/dp/B00MOIUL78/ref=sr_1_1?&s=kitchen&ie=UTF8&qid=1436433484&sr=1-1&keywords=brita+B00YASEP4K
	 				
	 				for(int i=0; i<imgList.size(); i++){
	 					if (i<imgList.size()-1)
	 						writer.append("+"+imgList.get(i)+";");
	 					else
	 						writer.append("+"+imgList.get(i)+",");
	 				}   	    
	 				//写url_key, url_path,custom_design,page_layout,options_container,image_label
	 				writer.append("\""+product_name.replace(" ", "-")+"\",\""+product_name.replace(" ", "-")+".html\""+",\"\",1 column,Product Info Column,\"\",");  	    
	 				//写small_image_label,thumbnail_label,country_of_manufacture,msrp_enabled,msrp_display_actual_price_type
	 				writer.append("\"\",\"\",\"\",Use config,Use config,");
	 				//写gift_message_available,gift_wrapping_available,manufacturer,status,is_recurring
	 				writer.append("No,No,"+manufacturer+",Enabled,No,");   	    
	 				//写visibility,tax_class_id,material,apparel_type,sleeve_length
	 				writer.append("\"Catalog, Search\",Taxable Goods,\""+product_material+"\",Knits,\"\",");
	 				//写fit,size,length,gender,description
	 				writer.append(",S,,Male,\""+product_desc+"\",");
	   	    
	 				//写short_description (tech data),meta_keyword,custom_layout_update,special_from_date,special_to_date
	 				writer.append("\""+short_desc+"\",,,,,");
	 				//写news_from_date,news_to_date,custom_design_from,custom_design_to,price
	 				writer.append("2013-03-01 00:00:00,,,,\""+old_price+"\",");
	 				//写special_price,weight,msrp,gift_wrapping_price,qty
	   	    	
	 				writer.append("\""+new_price+"\",\""+product_weight+"\",,,"+qty+",");
	 				//写min_qty,use_config_min_qty,is_qty_decimal,backorders,use_config_backorders
	 				writer.append("0,1,0,0,1,");
	 				//写min_sale_qty,use_config_min_sale_qty,max_sale_qty,use_config_max_sale_qty,is_in_stock
	 				if (qty == 0)
	 					writer.append("1,1,0,1,0,");
	 				else writer.append("1,1,0,1,1,");
	 				//写low_stock_date,notify_stock_qty,use_config_notify_stock_qty,manage_stock,use_config_manage_stock
	 				writer.append(",,1,0,1,");
	 				//写stock_status_changed_auto,use_config_qty_increments,qty_increments,use_config_enable_qty_inc,enable_qty_increments
	 				writer.append("0,1,0,1,0,");
	 				//写is_decimal_divided,stock_status_changed_automatically,use_config_enable_qty_increments,product_name,store_id
	 				writer.append("0,0,1,Lexington Cardigan Sweater,0,");
	 				//写product_type_id,product_status_changed,product_changed_websites
	 				writer.append("simple,,");
	 				writer.append('\n');
	   	    
	 				//写Handle
	 				//writer.append("\"".concat(product_nr).concat("\""));
	 				//writer.append(',');
	  	        
	 				writer.flush();
	 				writer.close();
	 			} catch (FileNotFoundException e) { 
	 				// File对象的创建过程中的异常捕获
	 				e.printStackTrace(); 
	 			} catch (IOException e) { 
	 				// BufferedWriter在关闭对象捕捉异常
	 				e.printStackTrace(); 
	 			}
	 		}
	       
	   }

	public static String getProductTitle(Document doc){	
    	Element tit = doc.getElementById("productTitle");	  
    	String title = tit.text();
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
    		return "RÖMER";
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
    		 //else to_remove = BingTrans.execute(to_remove, "de", "zh-CHS");//描述部分
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
    	 return Yahoo_exchange.convert(from, to);
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
    	//System.out.println("prod. nr: " + number);
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
    	to_trans = to_trans.replaceAll("mit Metalldeckel", "带金属盖").replace("Steckdeckel aus Glas", "带玻璃盖").replace("Steckdeckel aus hitzebeständigem Güteglas", "高品质耐热玻璃锅盖");
    	to_trans = to_trans.replaceAll("ist formstabil und unverwüstlich","不变形，不损坏").replaceAll("geschmacksneutral und beständig gegen Speisesäuren","保持食物原味,抵抗酸性食物腐蚀");
    	to_trans = to_trans.replaceAll("Edelstahl Rostfrei 18/10","18/10抗磨不锈钢").replaceAll("für alle Herdarten geeigntet","适用所有炉灶").replaceAll("auch für Induktion","亦适用微波炉").replaceAll("Bewährter Schüttrand für leichtes abgießen","特色锅沿，方便倒出食物");
    	to_trans = to_trans.replaceAll("mit Cool+ Grifftechnologie","锅把儿和锅体的导热隔离技术").replace("reduziert effektiv die Wärmeübertragung vom Topf auf die Griffe","防止把手过热").replace("Kochen für Geniesser","美食家的烹饪器具");
    	to_trans = to_trans.replaceAll("Guetesiegel: ","品质认证").replaceAll("Güetesiegel: ","品质认证").replace("Allherdboden","技术适应所有炉灶").replace("auch Induktion","亦适用微波炉").replaceAll("für alle Herdarten geeignet","适应所有炉灶").replaceAll("Geprüfte Sicherheit","安全认证");
    	to_trans = to_trans.replaceAll("hitzebeständiger Glasdeckel mit Dampföffnung für kontrolliertes Abdampfen","带蒸汽孔的耐热玻璃盖").replaceAll("ansprechendes Design","魅力设计").replaceAll("beständig gegen alle Speisesäuren","抵抗酸性食物腐蚀").replaceAll("da zu passt","跟它相配的是");
    	to_trans = to_trans.replaceAll("Griffe aus Edelstahl","不锈钢锅把").replace("mit Güteglasdeckel", "带高品质玻璃锅盖").replace("die beim Kochen nicht heiß werden", "烹饪时不会过热").replaceAll("hochwertiger Güteglasdeckel bis 180°C mit Silikonring", "带硅胶密封圈高品质玻璃锅盖，受热至180度");
    	to_trans = to_trans.replaceAll("Kochgeschirr Bodenmarke , funktion 4 Signet - Innovation","").replace("Stiftung Warentest", "商品检验基金会").replace("design award", "设计大奖").replace("red dot award", "红点大奖").replace("Design Zentrum", "设计中心").replaceAll("Design Plus Messe Frankfurt", "法兰克福博览会设计大奖").replaceAll("Nominierung für den", "提名");
    	to_trans = to_trans.replaceAll("teilig", "件套").replaceAll(", ohne Einsatz","").replaceAll("innen grau beschichtet","灰色内胆").replaceAll("Keramik-Beschichtung","陶瓷涂层").replaceAll("ohne Einsatz","").replaceAll("Edelstahl Rostfrei", "不锈钢").replaceAll("Töpfe sind stapelbar","锅具可以摞放").replaceAll("Topfset Töpfe","锅具套装").replaceAll("Topfset Töpfe Kochtopfset","锅具套装").replaceAll("für aller Herdarten geeignet","适用于所有炉灶");
    	to_trans = to_trans.replaceAll("mit Schüttrand für zielsicheres Ausgießen","带宽口，方便倒出食物").replaceAll("Cromargan ist geschmacksneutral und hygienisch und spülmaschinenbeständig","Cromargan保持食物原味,卫生,耐机洗").replaceAll("sitzt perfekt auf dem","严密紧扣在").replaceAll("ermöglicht den Garprozess visuell zu überwachen","可以亲见烹饪过程").replaceAll("dank DuPont Select Antihaftbeschichtung gut geeignet für fettarmes Braten","由于DuPont Select不沾技术，特别适合少油烹饪");
    	to_trans = to_trans.replaceAll("Die Artikel dieser Serie können Sie kombinieren","本系列的锅具可以由用户自由配置").replaceAll("Die Kombination aus Servierplatte, Topf und Porzelanschale bietet vielseitige Einsatzmöglichkeiten zum Vorbereiten, Kochen, Warmhalten und Servieren","大餐盘,锅具,瓷碗的组合实现多种的准备，烹制，用餐的功能").replaceAll("Kunststoffgriff mit Flammschutz","防燃塑胶把手").replaceAll("DuPont Select Antihaftbeschichtung","DuPont Select不沾涂层").replaceAll("beste Antihafteigenschaften","极佳不沾效果");
    	to_trans = to_trans.replaceAll("Griff für Pfanne","煎锅锅柄").replaceAll("auf mineralischer Basis","使用以矿物质为基础的材料").replaceAll("Model Jahr","设计年份").replaceAll("Model Name","设计名称").replaceAll("Art des Gurtes","绑带形式").replaceAll("Besone Merkmale","特别之处").replaceAll("Maximale Größe des Kindes","推荐最低身高").replaceAll("Mindestgröße des Kindes","推荐最低身高").replaceAll("Empfohlenes maximales Körpergewicht","推荐最高体重").replaceAll("Empfohlenes minimales Körpergewicht","推荐最低体重").replaceAll("Benötigt Batterien","需要电池").replaceAll("sehr gute Antihaftfähigkeit","不沾效果出色").replaceAll("extrem hitzebeständig","耐超高温").replaceAll("Durit Select Pro Antihaftbeschichtung","Durit Select Pro不沾涂层").replaceAll("dank Durit Select Pro Antihaftbeschichtung gut geeignet für fettarmes Braten","由于Durit Select Pro不沾涂层技术，非常适合低油烹饪").replaceAll("Aluguss mit Durit Protect Plus Antihaft-Versiegelung","Durit Protect Plus技术不沾涂层铸铝").replaceAll("geeignet für alle Herdarten außer","不适用于").replaceAll("Ergonomischer Stilgriff aus Kunststoff","人体工学设计把手").replaceAll("Ergonomischer Stilgriff","人体工学设计把手").replaceAll("Pfanne flach","平底煎锅").replaceAll("Die Porzellanschale passt in den Topf, der Topfdeckel auf die Porzelanschale, die Schale und der Topf auf die Servierplatte. Diese kann als Tablett, Servierteller oder Platzteller verwendet werden. Die Töpfe sind stapelbar","瓷碗合适放在锅里,锅盖盖在碗上,锅碗放在大餐盘上,便可待客；锅具还可以叠置");
    	to_trans = to_trans.replaceAll("Dichtungsring","密封圈").replaceAll("Größe","尺寸").replaceAll("V ca.","尺寸").replace("Material", "材料").replaceAll("Glas","玻璃").replaceAll("poliert", "抛光").replaceAll("mattiert","亚光").replaceAll("matt","亚光").replaceAll("Aluguss","铸铝").replaceAll("Inhalt", "包装内含").replace("stapelbar", "可以摞放");
    	to_trans = to_trans.replaceAll("spülmaschinenfest", "可以机洗").replace("Antihaftversiegelung","不沾涂层").replace("Steckdeckel","锅盖").replace("Deckel", "锅盖").replace("deckel", "锅盖").replaceAll("Auszeichnung", "获奖状况").replace("Hannover", "汉诺威").replace("Kollektion/Serie", "系列").replace("Kollektion", "系列").replaceAll("Antihaftbeschichtung","不沾技术").replaceAll("Keramikbeschichtung","陶瓷涂层");
    	to_trans = to_trans.replaceAll("Bratentopf","炖锅").replaceAll("Stielpfanne","带柄平底锅").replaceAll("Auflaufgericht","烤锅").replaceAll("Bratentöpfen","炖锅").replaceAll("Fleischtopf","汤锅").replaceAll("Fleischtöpfen","汤锅").replaceAll("WMF", "WMF 福腾宝").replaceAll("Schnellkochtopf", "高压锅").replaceAll("Schnelltopf","高压锅").replaceAll("-Set", "套装").replaceAll("Set", "套装").replace("hitzebeständiges ", "耐热");
    	to_trans = to_trans.replaceAll("Durchmesser","直径").replaceAll("Marke","品牌").replace("Farbe", "颜色").replace("Schwarz","黑色").replace("Grau", "灰色").replace("Silber", "银色").replace("hoch","深款").replace("Metall", "金属").replace("Pfannenset","炒锅套装").replace("induktionsgeeignet","微波炉适用").replace("inkl.","包括").replace("Pfannenwender","锅铲").replace("NEU","全新").replace("OVP","原包装");
    	to_trans = to_trans.replaceAll("Salatreiher","沙拉滤水盆").replaceAll("Servierpfanne","餐桌平底锅").replaceAll("Kuechenschuessel","厨房盆子").replaceAll("Passiermuehle","奶酪磨").replaceAll("Griff","锅柄").replaceAll("stiel","把").replaceAll("Kochgeschirrset","锅具套装").replaceAll("Pfanne","煎锅").replaceAll("Kochgeschirr","锅具").replace("Induktion","微波炉").replace("Edelstahl","不锈钢").replace("Aluminium","铝").replace("Haushaltswaren","家居产品").replace("Topf","锅具").replace("-tlg","件套").replace("formstabil","不变形").replace("geschmacksneutral","保留食物原味");
    	to_trans = to_trans.replaceAll("Spülmaschinenfest","可机洗").replaceAll("jeweils","各自").replaceAll("Gewürzmühle","调料研磨器").replaceAll("GRATIS","赠品").replaceAll("Stielkasserolle","带柄锅具").replaceAll("Topfset","锅具套装").replaceAll("Kennzeichnung","认证");
    	to_trans = to_trans.replaceAll("Ausführung","工艺处理").replaceAll("Besonheiten","特色").replaceAll("beschichtet","带涂层").replaceAll("Spritzschutz","防油溅").replaceAll("pflegeleicht","易于护理").replaceAll("backofenfester","可以进烤箱的").replaceAll("Pfannen","炒锅").replaceAll("geblasen","吹制").replaceAll("spülmaschinenbesändig","可以机洗").replaceAll("unverwüstlich","不损坏").replaceAll("Bodenmarke","底部带标识");
    	to_trans = to_trans.replaceAll("Restdrucksicherung","余压安全设施").replaceAll("Schnellpfanne","高压锅").replaceAll("Bestandteile","组成");
    	to_trans = to_trans.replaceAll("und","并且").replace("bis", "至").replace("mit", "带").replace("ohne", "不带").replace("Mit", "带").replace("Nein", "不").replace("der", "").replace("Der", "").replace("die", "").replace("Die", "").replace("das", "").replace("Das", "");
    	to_trans = to_trans.replaceAll("Neigungsvegstellbare","可调角度的").replaceAll("Modellnummer","产品型号").replaceAll("Artikelgewicht","产品重量").replaceAll("Produktabmessungen","产品尺寸").replace("Sitzbreite","座椅宽度");
    	return to_trans;//.replace(" ", "");
    }
    
    
    public static void main(String[] args) throws Exception {
    	try{
    		File file_update = new File("vendor_update.csv");
    		File file_new = new File("vendor_new.csv");
 
    		if(file_new.delete() && file_update.delete()){
    			System.out.println(file_new.getName() + " and "+ file_update.getName() + " is deleted!");
    		}else{
    			System.out.println("Delete operation is failed.");
    		}
 
    		FileWriter writer = new FileWriter("vendor_new.csv", true);
    		writer.append("store,websites,attribute_set,configurable_attributes,type,category_ids,sku,has_options,name,meta_title,meta_description,image,small_image,thumbnail,media_gallery,url_key, url_path,custom_design,page_layout,options_container,image_label,small_image_label,thumbnail_label,country_of_manufacture,msrp_enabled,msrp_display_actual_price_type,gift_message_available,gift_wrapping_available,manufacturer,status,is_recurring,visibility,tax_class_id,material,apparel_type,sleeve_length,fit,size,length,gender,description,short_description,meta_keyword,custom_layout_update,special_from_date,special_to_date,news_from_date,news_to_date,custom_design_from,custom_design_to,price,special_price,weight,msrp,gift_wrapping_price,qty,min_qty,use_config_min_qty,is_qty_decimal,backorders,use_config_backorders,min_sale_qty,use_config_min_sale_qty,max_sale_qty,use_config_max_sale_qty,is_in_stock,low_stock_date,notify_stock_qty,use_config_notify_stock_qty,manage_stock,use_config_manage_stock,stock_status_changed_auto,use_config_qty_increments,qty_increments,use_config_enable_qty_inc,enable_qty_increments,is_decimal_divided,stock_status_changed_automatically,use_config_enable_qty_increments,product_name,store_id,product_type_id,product_status_changed,product_changed_websites");
    		writer.append('\n');  	  
    		writer.flush();
    		writer.close();
   	    
    		
    		writer = new FileWriter("vendor_update.csv", true);
    		writer.append("manufacturer,sku,price,special_price,qty");
    		writer.append('\n');  	  
   	    	writer.flush();
   	   		writer.close();
   	   		
   	   		
    	}catch(Exception e){ 
    		e.printStackTrace(); 
    	}
   	    
        AnalyseVendor t = new AnalyseVendor(); 
        
        //德品国际汽车座椅
        List<ArrayList<String>> p = t.getVendorProductList("Baby Preisliste_Autositz.csv");
        for(int i=0; i<p.size();i++){
        	List<String> pro_link = p.get(i);        	
        	getProductDetailsMagento(pro_link, "14,86", "德品国际");
        }
        
       
        //找出已经不在amazon的产品
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
    }
}