import com.google.gson.*;
import org.apache.log4j.Logger;
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
import java.util.LinkedHashSet;
import java.util.Map;

public class MultiCrawWeb implements Runnable {
    private Logger logger=Logger.getLogger(MultiCrawWeb.class);
    String ip;
    int port;
    int ThreadID;
    static int ThreadNum=10;
    static int MovieNum=50;

    public MultiCrawWeb(int threadID) throws IOException {
        ThreadID = threadID;
        updateProxy();
    }

    static LinkedHashSet<MovieMetaEntity> urlset;
    static MovieMetaEntity[] urllist;
    static Map<String,String> cookies;
    static {
        try {
            cookies = getCookies();
            urlset = new LinkedHashSet<>();
            int i=0;
            while (i<MovieNum/20) {
                Document doc = Jsoup.connect("https://movie.douban.com/j/new_search_subjects?sort=T&range=0,10&tags=&start="+20*i)
                        .userAgent("Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)")
                        .cookies(cookies)
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
                        .get();
                //            Document doc=Jsoup.parse(new File("/home/ldx/JavaProject/web/src/main/resources/list"),"utf-8");
                String t = doc.text();
                JsonArray jsonElements = new JsonParser().parse(t).getAsJsonObject().get("data").getAsJsonArray();
                Gson gson = new Gson();
                for (JsonElement object : jsonElements) {
                    urlset.add(gson.fromJson(object, MovieMetaEntity.class));
                }
                i++;
                System.out.print(',');
            }
            System.out.println();
            urllist=urlset.toArray(new MovieMetaEntity[]{});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map getCookies() throws IOException {
        return Jsoup.connect("https://movie.douban.com/subject/24773958/?from=showing")
                .userAgent("Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)").timeout(5000).execute().cookies();
    }

    public Document getDoc(String url) throws IOException {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
        int error404c=0;
        URL aurl = new URL(url);
        while (true) {
            HttpsURLConnection urlcon = (HttpsURLConnection) aurl.openConnection(proxy);
            InputStream is=null;
            BufferedReader buffer=null;
            urlcon.setConnectTimeout(1000);
            urlcon.setConnectTimeout(1000);
            try {
                urlcon.connect();
                is = urlcon.getInputStream();
                buffer = new BufferedReader(new InputStreamReader(is));
                StringBuffer bs = new StringBuffer();
                String l;
                while ((l = buffer.readLine()) != null) {
                    bs.append(l);
                }
                is.close();
                buffer.close();
                return Jsoup.parse(bs.toString());
            } catch (Exception e) {
                error404c++;
                if (is!=null){
                    is.close();
                }
                if (buffer!=null){
                    buffer.close();
                }
                if (error404c>1){
                    return null;
                }
                updateProxy();
            }
        }

    }
    public void crawReview(String[] URLs,String name) throws IOException {
        if (name.equals(""))
            return;
        Document doc=null;
        File movie = new File("/home/ldx/JavaProject/web/src/main/resources/movie/" + name);
        BufferedWriter bf = new BufferedWriter(new FileWriter(movie));
        try {
            logger.debug("craw "+name+" review");
            for (String url : URLs) {
                doc = getDoc(url);
                if (doc == null) {
                    continue;
                }
                logger.debug("Review-Thread"+ThreadID+" "+name+url);
                Element comm = doc.getElementById("comments");
                Elements comms = comm.getElementsByClass("comment-text");
                for (Element e : comms) {
                    bf.write(e.text() + "\n");
                }
            }
            bf.flush();
            bf.close();

        }catch (Exception e){
            e.printStackTrace();
            logger.info("crawReviewError"+name);
            logger.debug(doc);
            bf.flush();
            bf.close();
        }
    }

    public void crawMovie(String baseURL,String name) throws IOException {
        int start=0;
        Document doc=null;
        String url=baseURL+"reviews"+"?start="+start;
        LinkedHashSet<Element> set=new LinkedHashSet<>();
        try {
            for (int i = 0; i < 5; i++) {
                doc = getDoc(url);
                if (doc == null) {
                    continue;
                }
                Elements reviewList = doc.getElementsByTag("h2");
                set.addAll(reviewList);
                start += 20;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            logger.info("crawMovieError");
            logger.debug(doc);
        }

        crawReview(set.stream().map((e) -> e.getElementsByTag("a").attr("href")).toArray(String[]::new),name);
    }

    public void updateProxy() throws IOException {
        Jsoup.connect("http://localhost:8080/api/delete?proxy="+ip+":"+port).ignoreContentType(true).ignoreHttpErrors(true).get();
        ProxyEntity proxyEntity=IPPool.getProxy();
        ip=proxyEntity.getIp();
        port=proxyEntity.getPort();

    }

    @Override
    public void run() {
        int len=urlset.size();
        for (int i = ThreadID; i < len; i+=ThreadNum) {
            logger.info("Thread"+ThreadID+":"+urllist[i].title+"---crawStart"+i);
            try {
                crawMovie(urllist[i].getUrl(),urllist[i].title);
                logger.info("Thread"+ThreadID+":"+urllist[i].title+"---crawOver"+i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        logger.info("------Thread"+ThreadID+" task successfully over---------");
    }
    public static void main(String[] args) throws IOException {

        Thread[] threads=new Thread[ThreadNum];
        for (int i = 0; i < ThreadNum; i++) {
            threads[i]=new Thread(new MultiCrawWeb(i));
        }
        for (int i = 0; i <ThreadNum; i++) {
            threads[i].start();
        }
    }
}
