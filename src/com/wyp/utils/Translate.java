package com.wyp.utils;

public class Translate {
	public Translate(){}
	
	public String translate(String toTrans, String type){
		if(type.contains("14,86,")){
			return translate(toTrans, AUTOSIT);
		}
		return "";
	}
	
	public String translate(String toTrans, String[][] pairs){
		toTrans = toTrans.replaceAll("<span class='a-list-item'>","<span style='font-size:15px'>");
    	for(int i=0; i<pairs.length; i++){
    		toTrans = toTrans.replace(pairs[i][1], pairs[i][0]);
    	}
		return toTrans;
	}
	
	private String[][] ATTR = {
			{"Modellnummer","产品号"},
			{"Artikelgewicht","产品重量"},
			{"Produktabmessungen","产品尺寸"},
			{"Model Jahr","上市年份"},
			{"Model Name","产品名称"},
			{"Farbe","颜色"},
			{"Style","款式"},
			{"Material","材质"},
			{"Empfohlenes maximales Körpergewicht","适用儿童最大公斤数"},
			{"Besondere Merkmale","特别提醒"},
			{"Benötigt Batterien","是否需要电池"},
			{"Batterien","电池"},
			{"Bestandteile","成分"},
			{"Länge der Sitz-/Liegefläche","座／躺椅长度"},
			{"FAA-Approved for use on aircraft","美国航空管理局机舱批准"},
			{"Sitzbreite","座椅宽度"}
	};
	
	private String[][] COLOR = {
			{"black raven","黑色"},
			{"grau","灰"},
			
	};
	
	private String[][] MATERIAL = {
			{"Polyester","聚酯"},
			{"Mischgewebe","混纺"},
			{"Kunststoff","人工材料"},
			
	};
	
	private String[][]  AUTOSIT = {
			{"Nein","不"},
			{"Ja","是"},
			{"FAA-Approved for use on aircraft","美国航空管理局机舱批准"},
	};
}
