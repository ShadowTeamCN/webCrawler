import com.google.gson.Gson;

import com.google.gson.reflect.TypeToken;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IPPool {


    public static ProxyEntity getProxy() throws IOException {
        Document doc= Jsoup.connect("http://localhost:8080/api/get").ignoreContentType(true).ignoreHttpErrors(true).get();
        return new Gson().fromJson(doc.text(),ProxyEntity.class);
    }

    public static List<ProxyEntity> getProxyList(int size) throws IOException {
        Document doc= Jsoup.connect("http://localhost:8080/api/get_list?num="+size).ignoreContentType(true).ignoreHttpErrors(true).get();
        ProxyEntityList proxyEntityList=new ProxyEntityList();
        proxyEntityList=new Gson().fromJson(doc.text(),ProxyEntityList.class);
        ArrayList<ProxyEntity> arrayList=new ArrayList<>();
        for (ProxyEntityList.Data data:proxyEntityList.data){
            ProxyEntity ape=new ProxyEntity();
            ape.data.ip=data.ip;
            ape.data.port=data.port;
            arrayList.add(ape);
        }
        return arrayList;
    }
    public static void main(String[]args) throws IOException {
        System.out.println(getProxyList(10));
    }

}
