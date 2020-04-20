package com.hogdeer.zipkin.jsoup;

import org.apache.commons.collections.map.HashedMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.IOException;
import java.util.*;

public class YmlRepoPageProcessor implements PageProcessor {

    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000);

    @Override
    public void process(Page page) {
        try {
//            String url="https://www.pilship.com/--/120.html?search_type=bl&refnumbers=LGB801986100";
            String url="https://www.yangming.com/e-service/Track_Trace/blconnect.aspx?BLADG=I488142522,&rdolType=BL&type=cargo";
            Document doc = getDoc(url);
//        Document doc=  page.getHtml().getDocument();
//        System.out.println(page.getHtml());

            Map<String ,Object> object= new HashedMap();

            getBlNo(doc,object);
            getRoutingSchedule(doc,object);
            getVessel(doc,object);
            getContainers(doc,object);

            for (Map.Entry<String,Object> entry: object.entrySet() ){
                System.out.println( entry.getKey() +": "+entry.getValue());
            }

        }catch (Exception e) {
            e.printStackTrace();
    }


    }

    @Override
    public Site getSite() {
        return site;
    }

    /**
     * 获取BLNo
     * @param doc
     * @return
     */
    private static String getBlNo(Document doc,Map<String ,Object> object){

        Element element  =doc.getElementById("ContentPlaceHolder1_rptBLNo_lblBLNo_0");
        if (element!=null){
            String blNo=element.text();
            //YMLU
//            System.out.println("blNo:"+blNo);
            object.put("blNo",blNo);
            return  blNo;
        }
        return  null;
    }


    /**
     * 获取船名航次
     * @param doc
     * @return
     */
    private static String getVessel(Document doc ,Map<String ,Object> object){
        Element element  =doc.getElementById("ContentPlaceHolder1_rptBLNo_lblVessel_0");
        if (element!=null){
            String str=element.text();
            String[] arry= str.split("-");

//            System.out.println("船名:"+arry[0].trim()+" ,航次 "+arry[1].trim());
            object.put("船名",arry[0].trim());
            object.put("航次",arry[1].trim());
            return  str;
        }
        return  null;
    }

    /**
     * 获取货柜信息
     * @param doc
     * @return
     */
//https://www.yangming.com/e-service/Track_Trace/ctconnect.aspx?rdolType=BL&ctnrno=BEAU4491229&blno=E175253016&movertype=11&lifecycle=2
    private static String getContainers(Document doc ,Map<String ,Object> object){

        Element tableElement= doc.getElementById("ContentPlaceHolder1_rptBLNo_gvLatestEvent_0");
        Element tbodyElement=tableElement.getElementsByTag("tbody").get(0);
        Elements trList= tbodyElement.getElementsByTag("tr");
        Map<String,Integer> map=new HashMap<>();
        Map<String,String> urlMap=new HashMap<>();
        List<String> containerNoList=new ArrayList<>();
        for (int i = 0; i < trList.size(); i++) {
            Element element =  trList.get(i);
            Element td=  element.getElementsByTag("td").get(0);
            Element a=  td.getElementsByTag("a").get(0);
            String href=a.attributes().get("href");

            String hgh= td.text();
            urlMap.put(hgh,"https://www.yangming.com/e-service/Track_Trace/"+href);


            String size=element.getElementById("ContentPlaceHolder1_rptBLNo_gvLatestEvent_0_lblSize_"+i).text();
            String type=element.getElementById("ContentPlaceHolder1_rptBLNo_gvLatestEvent_0_lblType_"+i).text();
//            String seaNo=element.getElementById("ContentPlaceHolder1_rptBLNo_gvLatestEvent_0_lblSealNo_"+i).text();
//            String moveType=element.getElementById("ContentPlaceHolder1_rptBLNo_gvLatestEvent_0_lblMoveType_"+i).text();
//            String date=element.getElementById("ContentPlaceHolder1_rptBLNo_gvLatestEvent_0_lblDate_"+i).text();
//            String latestEvent=element.getElementById("ContentPlaceHolder1_rptBLNo_gvLatestEvent_0_lblLatestEvent_"+i).text();
//            String place=element.getElementById("ContentPlaceHolder1_rptBLNo_gvLatestEvent_0_lblPlace_"+i).text();
//            String vgm=element.getElementById("ContentPlaceHolder1_rptBLNo_gvLatestEvent_0_lblVGM_"+i).text();


            int index= type.indexOf("-");
            String typestr= size+type.substring(0,index);
            Integer num=1;
            if (map.containsKey(typestr)){
                num= map.get(typestr);
                num++;
                map.put(typestr,num);
            }else{
                map.put(typestr,num);
            }
//            System.out.println("货柜号码:"+hgh+" 尺寸："+size+" 柜型："+type+" 封条号码："+seaNo+" 运送方式："+moveType+" 日期时间："+date+" 最新动态："+latestEvent+" 地点："+place +" 体积："+vgm);
            containerNoList.add(hgh);
        }
        object.put("Container No.",containerNoList);
        StringBuffer buffer=new StringBuffer();
        for (Map.Entry<String,Integer> entry: map.entrySet() ){
//            System.out.println( entry.getValue() +" * "+entry.getKey());
            buffer.append(entry.getValue() +"*"+entry.getKey()).append(" ");
        }
        object.put("箱型 箱尺",buffer.toString());

        getContainers(urlMap);


        return  null;
    }


    /**
     * 货柜明细
     * @param map
     */
    private static void getContainers(Map<String,String> map){
        if (map!=null&&map.size()>0){
            System.out.println("货柜明细及最新动态");
            for (Map.Entry<String ,String> entry: map.entrySet() ) {
                String url =  entry.getValue();
                Document doc =getDoc(url);
                Element tableElement= doc.getElementById("ContentPlaceHolder1_gvContainerNo");
                Element tbodyElement=tableElement.getElementsByTag("tbody").get(0);
                Elements trList= tbodyElement.getElementsByTag("tr");
                System.out.println("货柜号码"+entry.getKey());
                for (int j = 0; j < trList.size(); j++) {
                    Element element =  trList.get(j);
                    String dateTime=element.getElementById("ContentPlaceHolder1_gvContainerNo_lblDateTime_"+j).text();
                    String event=element.getElementById("ContentPlaceHolder1_gvContainerNo_lblEvent_"+j).text();
//                    String atFacility=element.getElementById("ContentPlaceHolder1_gvContainerNo_lblAtFacility_"+j).text();
                    //System.out.println("日期/时间:"+dateTime +" Event:"+event+" At Facility:"+atFacility);
                    System.out.println("日期/时间:"+dateTime +" Event:"+event);
                }
                doc=null;
            }
        }
    }


    /**
     *
     * @param doc
     */
    private static void  getRoutingSchedule(Document doc, Map<String ,Object> object){
//        System.out.println("货载运送路径及时间表");
        Elements elements= doc.getElementsByClass("cargo-trackbox3");
        if (elements!=null){
            Element element= elements.get(0);
            Elements ulList= element.getElementsByTag("ul");
//            for (int i = 0; i < ulList.size(); i++) {
//                Element li =  ulList.get(i);
//                String routing= li.getElementById("ContentPlaceHolder1_rptBLNo_rptRoutingSchedule_0_lblRouting_"+i).text();
//                String dateTime=  li.getElementById("ContentPlaceHolder1_rptBLNo_rptRoutingSchedule_0_lblDateTime_"+i).text();
//                System.out.println("routing:"+routing +" dateTime:"+dateTime);
//            }
            Element firstElement=  ulList.first();
            scheduleDate(firstElement,object,"POL ATD","POL ETD");
            Element lastElement=  ulList.last();
            scheduleDate(lastElement,object,"DES ATA","DES ETA");
            if (ulList!=null&&ulList.size()==3){
                Element li =  ulList.get(1);
                scheduleDate(li,object,"POD ATA","POD ETA");
            }
        }
    }



    private static  void scheduleDate(Element element, Map<String ,Object> object,String actual,String estimated){
        String routing= element.getElementsByClass("date").first().text();
        int index = routing.indexOf("(");
        String dateStr =  routing.substring(0,index).trim();
        if (routing.contains("Actual")){
            object.put(actual,dateStr);
        }
        if (routing.contains("Estimated")){
            object.put(estimated,dateStr);
        }
    }




    private  static  Document getDoc(String url){
        try {
            Document doc = Jsoup.connect(url).get();
            return  doc;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }






    public static void main(String[] args) {
        //Spider.create(new YmlRepoPageProcessor()).addUrl("https://www.yangming.com/e-service/Track_Trace/blconnect.aspx?BLADG=YMLUE175253016,&rdolType=BL&type=cargo").thread(1).run();
        new YmlRepoPageProcessor().process(null);
    }
}
