import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class crawWeb  {
    static String bid;
    static Map<String,String> cookies;
    static int end=100;

    static {
        try {
            cookies = getCookies();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map getCookies() throws IOException {
        return Jsoup.connect("https://movie.douban.com/subject/24773958/?from=showing")
                .userAgent("Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)").timeout(5000).execute().cookies();
    }

    public static void crawReview(String[] URLs,String name) throws IOException {
        if (name.equals(""))
            return;
        Document doc;
        int error404c=0;
        File movie=new File("/home/ldx/JavaProject/web/src/main/resources/movie/"+name);
        BufferedWriter bf=new BufferedWriter(new FileWriter(movie));
        Outer: for (String url:URLs) {
            System.out.println(url);
            while (true) {
                try {
                    doc = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)")
                            .cookies(cookies).timeout(1000).get();
                    break;
                } catch (IOException e) {
                    System.out.println(e);
                    cookies=getCookies();
                    error404c++;
                }
                if (error404c>1){
                    error404c=0;
                    continue Outer;
                }
            }

            Element comm=doc.getElementById("comments");
            Elements comms=comm.getElementsByClass("comment-text");
            for (Element e:comms) {
                bf.write(e.text() + "\n");
            }
        }
        bf.flush();
        bf.close();
//        Document doc=Jsoup.parse(new File("/home/ldx/JavaProject/web/src/main/resources/review"),"utf-8");
//        Element name=doc.getElementsByClass("main-hd").first().getElementsByTag("a").get(1);
//        System.out.println(name.text());
    }

    public static void crawMovie(String baseURL) throws IOException {
        int start=0;
        Document doc;
        String name="";
        ArrayList<Element> arrayList=new ArrayList<>();
        for (int i = 0; i <5 ; i++) {
            doc=Jsoup.connect(baseURL+"/reviews"+"?start="+start)
                    .userAgent("Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)")
                    .cookies(cookies).get();
            Elements reviewList=doc.getElementsByTag("h2");
            arrayList.addAll(reviewList);
            start+=20;
            if (i==0){
                name=doc.getElementsByClass("sidebar-info-wrapper").get(0).getElementsByTag("a").get(0).text().substring(2);
            }
        }

        crawReview(arrayList.stream().map((e) -> e.getElementsByTag("a").attr("href")).toArray(String[]::new),name);
//        System.out.println(arrayList);
    }
    public static ArrayList<String> getMovieList() throws IOException {
        List<String> list = new ArrayList<>();
        Document doc=Jsoup.connect("https://movie.douban.com/subject/24773958/reviews")
                .userAgent("Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)")
                .timeout(5000)
                .cookies(cookies).get();
        return null;
    }


    public static Document getDoc(String url,String ip,int port) throws IOException {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
        URL aurl = new URL(url);
        HttpsURLConnection urlcon = (HttpsURLConnection) aurl.openConnection(proxy);
        urlcon.connect();
        InputStream is = urlcon.getInputStream();
        BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
        StringBuffer bs = new StringBuffer();
        String l = null;
        while ((l = buffer.readLine()) != null) {
            bs.append(l);
        }
        System.out.println(bs.toString());
        Document doc = Jsoup.parse(bs.toString());
        return doc;
    }

    public static void main(String[] args) throws IOException {

//        String cookies=getCookies();
//        Document doc=Jsoup.connect("https://movie.douban.com/subject/24773958/reviews")
//                .userAgent("Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)")
//                .cookies(cookies).get();

        Document doc=Jsoup.parse(new File("/home/ldx/JavaProject/web/src/main/resources/reviews"),"utf-8");
//        Elements elements=doc.getElementsByClass("count");
//        System.out.println(elements);

        crawMovie("https://movie.douban.com/subject/24773958");
//        crawReview(new String[]{});

    }

}
