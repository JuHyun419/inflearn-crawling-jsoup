package inflearn;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InflearnCrawling2 {

    private static final String URL = "https://www.inflearn.com/courses";
    private static List<InflearnModel> crawlingList = new ArrayList<>();

    public static void main(String[] args) {
        Connection conn = Jsoup.connect(URL);

        try {
            Document document = conn.get();

            String thumbnail = document.getElementsByClass("swiper-lazy").toString();

            // class명 = rating 에서 span태그만 가져오기
            Elements rating = document.select(".card-content .rating span.review_cnt");

            for (Element e : rating) {
                if (!e.hasText()) {
                    System.out.println("aa");
                } else {
                    System.out.println(e.text());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
