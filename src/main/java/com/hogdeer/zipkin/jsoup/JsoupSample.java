package com.hogdeer.zipkin.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JsoupSample {


    public static void main(String[] args) throws IOException {
        //https://www.yangming.com/e-service/Track_Trace/blconnect.aspx?BLADG=YMLUE120481720,YMLUE115221703,YMLUE175253738,YMLUE175253683,&rdolType=BL&type=cargo
        Document doc = Jsoup.connect("https://www.yangming.com/e-service/Track_Trace/blconnect.aspx?BLADG=YMLUE175253016,&rdolType=BL&type=cargo").get();

//        Document doc= Jsoup.connect("https://www.zimchina.com/tools/track-a-shipment?consnumber=ZIMUORF0942278").get();

//        String title = doc.title();
//        System.out.println(title);
//
//
//        getBlNo(doc);
//        getVessel(doc);
//        getContainers(doc);

    }






//    private static String getBlNo(Document doc){
//        Element element  =doc.getElementById("ContentPlaceHolder1_rptBLNo_lblBLNo_0");
//        if (element!=null){
//            String blNo=element.text();
//            //YMLU
//            System.out.println("blNo:"+blNo);
//            return  blNo;
//        }
//        return  null;
//    }
//
//
//    private static String getVessel(Document doc){
//        Element element  =doc.getElementById("ContentPlaceHolder1_rptBLNo_lblVessel_0");
//        if (element!=null){
//            String str=element.text();
//            String[] arry= str.split("-");
//
//            System.out.println("船名:"+arry[0].trim()+" ,航次 "+arry[1].trim());
//            return  str;
//        }
//        return  null;
//    }
//
//
//
//    private static String getContainers(Document doc){
//
//        Element tableElement= doc.getElementById("ContentPlaceHolder1_rptBLNo_gvLatestEvent_0");
//        Element tbodyElement=tableElement.getElementsByTag("tbody").get(0);
//        Elements trList= tbodyElement.getElementsByTag("tr");
//        Map<String,Integer> map=new HashMap<>();
//        for (int i = 0; i < trList.size(); i++) {
//            Element element =  trList.get(i);
//            String hgh= element.getElementsByTag("td").get(0).text();
//            String size=element.getElementById("ContentPlaceHolder1_rptBLNo_gvLatestEvent_0_lblSize_"+i).text();
//            String type=element.getElementById("ContentPlaceHolder1_rptBLNo_gvLatestEvent_0_lblType_"+i).text();
//            String seaNo=element.getElementById("ContentPlaceHolder1_rptBLNo_gvLatestEvent_0_lblSealNo_"+i).text();
//            String moveType=element.getElementById("ContentPlaceHolder1_rptBLNo_gvLatestEvent_0_lblMoveType_"+i).text();
//            String date=element.getElementById("ContentPlaceHolder1_rptBLNo_gvLatestEvent_0_lblDate_"+i).text();
//            String latestEvent=element.getElementById("ContentPlaceHolder1_rptBLNo_gvLatestEvent_0_lblLatestEvent_"+i).text();
//            String place=element.getElementById("ContentPlaceHolder1_rptBLNo_gvLatestEvent_0_lblPlace_"+i).text();
//            String vgm=element.getElementById("ContentPlaceHolder1_rptBLNo_gvLatestEvent_0_lblVGM_"+i).text();
//
//
//            int index= type.indexOf("-");
//            String typestr= size+type.substring(0,index);
//            Integer num=1;
//            if (map.containsKey(typestr)){
//                num= map.get(typestr);
//                num++;
//                map.put(typestr,num);
//            }else{
//                map.put(typestr,num);
//            }
//            System.out.println("货柜号码:"+hgh+" 尺寸："+size+" 柜型："+type+" 封条号码："+seaNo+" 运送方式："+moveType+" 日期时间："+date+" 最新动态："+latestEvent+" 地点："+place +" 体积："+vgm);
//
//        }
//        for (Map.Entry<String,Integer> entry: map.entrySet() ){
//            System.out.println( entry.getValue() +" * "+entry.getKey());
//        }
//
//        return  null;
//    }

}
