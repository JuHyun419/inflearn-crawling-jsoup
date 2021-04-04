package inflearn;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Objects;

public class InflearnCrawling3 {

    public static void main(String[] args) throws IOException {
        Connection conn = Jsoup.connect("https://www.inflearn.com/courses/it-programming?order=seq&page=1");
        Document document = conn.get();

        Elements thumbnailElements = document.getElementsByClass("swiper-lazy");
        Elements titleElements = document.select("div.card-content > div.course_title");
        Elements priceElements = document.getElementsByClass("price");

        Elements instructorElements = document.getElementsByClass("instructor");
        Elements linkElements = document.select("a.course_card_front");

        for (int i = 0; i < 10; i++) {
            System.out.println(i + " thumbnail: " + thumbnailElements.get(i).text());
            System.out.println(i + " title: " + titleElements.get(i).text());
            System.out.println(i + " price: " + priceElements.get(i).text());
            System.out.println(i + " instructor: " + instructorElements.get(i).text());
            System.out.println(i + " link: " + linkElements.get(i).attr("abs:href"));

            Connection innerConn = Jsoup.connect(linkElements.get(i).attr("abs:href"));
            Document innerDocument = innerConn.get();

            Element listener = innerDocument.selectFirst("div.cd-header__info-cover");
            String listenerCount = null;

            if (Objects.isNull(listener)) {
                System.out.println(i + " is Null");
                listenerCount = innerDocument.selectFirst("span > strong").text();
            } else {
                System.out.println(i + " is Not Null");
                listenerCount = innerDocument.select("div.cd-header__info-cover > span > strong").get(1).text();
            }

            System.out.println(i + " listenerCount: " + listenerCount);

            /* 평점 */
            Element ratingElement = innerDocument.selectFirst("div.dashboard-star__num");
            if (Objects.isNull(ratingElement)) {
                System.out.println(i + " rating is Null");
            } else {
                System.out.println(i + " rating: " + ratingElement.text());
            }

            /* 강의 세션 개수 + 강의 시간 */
            String course = innerDocument.selectFirst("span.cd-curriculum__sub-title").text();
            System.out.println(i + " course: " + course);
            System.out.println();

        }
    }
}
