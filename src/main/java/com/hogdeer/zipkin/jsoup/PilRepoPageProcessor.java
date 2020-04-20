package com.hogdeer.zipkin.jsoup;

//import cn.hutool.http.Header;
//import cn.hutool.http.HttpRequest;
//import cn.hutool.http.HttpResponse;
import org.apache.commons.collections.map.HashedMap;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.IOException;
import java.util.Map;

public class PilRepoPageProcessor implements PageProcessor {

    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000);

    @Override
    public void process(Page page) {
        try {

//            String url=" https://www.pilship.com/shared/ajax/?fn=get_tracktrace_bl&ref_num=TXSV01251100&_="+System.currentTimeMillis();
//            Document doc = getDoc(url);
            Map<String ,Object> object= new HashedMap();


            System.out.println(page.getJson());




//            getBlNo(doc,object);
//            getRoutingSchedule(doc,object);
//            getVessel(doc,object);
//            getContainers(doc,object);

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





    private  static  Document getDoc(String url){
        try {
            Document doc = Jsoup.connect(url).get();
            return  doc;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }






    public static void main(String[] args) throws IOException {
        //https://www.pilship.com/
        //https://www.pilship.com/--/120.html?search_type=bl&refnumbers=TXSV01251100
        String url="https://www.pilship.com/shared/ajax/?fn=get_tracktrace_bl&ref_num=TXSV01251100&_="+System.currentTimeMillis();
        //HttpResponse res = HttpRequest.get(url).cookie("_ga=GA1.2.897891344.1583977223; _gid=GA1.2.113942298.1583977223; BIGipServerpool_pilship443=!aVOOxJB9F6PCAelGIrddrJxRjz9vjNzEn1s+H2M3Q6CosHRslnDp+eMshXmZkeqMHpGjCPVP4STZaNI=; front_www_pilship_com=o2c1ggcis146a1v6trhsj0pqs6; TS01a292b3_77=0899109c5fab2800d32ef41f66416ee65614f83625f5841c4f22de094a4e896678f57d97f9dc6ffc96ad93247b1e271508ae61b6f1824000125238e3f480335b28209391c1d7ac76c7abaad0296d1b4457a5386224acae8774995207869c3ac0f6c4059ba3dc11283a7f57e5cd64fce79954158f377e008e; TSPD_101=0899109c5fab28004bdbde77083eb8576140f11d844d16bb6b6c2b3f06d8eb41dd890d5da3503713a489f912ac0e4cc6:0899109c5fab28004bdbde77083eb8576140f11d844d16bb6b6c2b3f06d8eb41dd890d5da3503713a489f912ac0e4cc608493227a5063800752558ccea7b6b8957c19bf9e71ca6584df2ec947419944f0cf68fb4287482bf8e05bb0ad8527e3b918b35ed9279c958434d9a9baafb92f1; TS01a292b3=01d0ae8dcea4c1b05929bd899201f912bbf240c554826423d35316309f1abc37a9b848facd206e9f1a120decb3fba7d40de6a07107ff31112d41d6d6a47358975ef372bed8fae09895e0e4a5fac79f722c9b64ba4c").execute();

        //Console.log(res.getStatus());
        //Console.log(res.header(Header.SET_COOKIE));
        //Console.log(res.header(Header.COOKIE));
       // Console.log( res.body());


        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://www.baidu.com");
        HttpResponse httpResponse = httpclient.execute(httpGet);

        Header[] headerArray = httpResponse.getAllHeaders();
        for(Header header : headerArray)
        {
            System.out.println("--Header-----------------------------------------");
            System.out.println("----Key: " + header.getName());
            System.out.println("----RawValue: " + header.getValue());
            HeaderElement[] headerElementArray = header.getElements();
            for(HeaderElement headerElement : headerElementArray)
            {
                System.out.print("------Value: " + headerElement.getName());
                if(null != headerElement.getValue())
                {
                    System.out.println("  <-|->  " + headerElement.getValue());
                }
                else
                {
                    System.out.println();
                }
                NameValuePair[] nameValuePairArray = headerElement.getParameters();
                for(NameValuePair nameValuePair : nameValuePairArray)
                {
                    System.out.println("------Parameter: " + nameValuePair.getName() + "  <-|->  " + nameValuePair.getValue());
                }
            }
        }


    }
}
