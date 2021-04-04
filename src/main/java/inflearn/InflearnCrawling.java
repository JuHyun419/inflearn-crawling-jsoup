package inflearn;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InflearnCrawling {

    private static List<InflearnModel> crawlingList = new ArrayList<>();
    private static final int FIRST_PAGE_INDEX = 1;
    private static final int LAST_PAGE_INDEX = 32;
    private static final String PLATFORM = "인프런";

    public static void main(String[] args) {
        try {
            // 개발 강의 모든 페이징 순회
            for (int i = FIRST_PAGE_INDEX; i <= 1; i++) {
                final String url = "https://www.inflearn.com/courses/it-programming?order=seq&page=" + i;
                Connection conn = Jsoup.connect(url);
                Document document = conn.get();

                // 크롤링 항목 필요 리스트
                //   - 썸네일 링크, 강의 제목, 가격(할인가격), 평점, 강의자, 강의 링크, 수강자 수, 플랫폼, 강의 세션 개수 + 시간
                Elements thumbnailElements = document.getElementsByClass("swiper-lazy");
                Elements titleElements = document.select("div.card-content > div.course_title");
                Elements priceElements = document.getElementsByClass("price");
                Elements instructorElements = document.getElementsByClass("instructor");
                Elements linkElements = document.select("a.course_card_front");
                Elements descriptionElements = document.select("p.course_description");
                String[] thumbnails = new String[thumbnailElements.size()];

                int index = 0;

                for (Element e : thumbnailElements) {
                    thumbnails[index++] = e.attr("abs:src");
                }

                for (int j = 0; j < titleElements.size(); j++) {
                    final String title = titleElements.get(j).text();
                    final String price = priceElements.get(j).text();
                    final String realPrice = getRealPrice(price);
                    final String salePrice = getSalePrice(price);
                    final String instructor = instructorElements.get(j).text();
                    final String link = linkElements.get(j).attr("abs:href");
                    final String courseDescription = descriptionElements.get(j).text();

                    System.out.println("썸네일: " + thumbnails[j]);
                    System.out.println("강의 제목: " + title);
                    System.out.println("가격: " + realPrice);
                    System.out.println("할인 가격: " + salePrice);
                    System.out.println("강의자: " + instructor);
                    System.out.println("강의 링크: " + link);
                    System.out.println("강의 설명: " + courseDescription);

                    /* 강의 링크 내부 */
                    Connection innerConn = Jsoup.connect(link);
                    Document innerDocument = innerConn.get();

                    /* 평점 */
                    Element ratingElement = innerDocument.selectFirst("div.dashboard-star__num");
                    final String rating = Objects.isNull(ratingElement)
                            ? "0"
                            : ratingElement.text();
                    System.out.println("평점: " + rating);

                    /* 수강자 수 */
                    Element listenerElement = innerDocument.selectFirst("div.cd-header__info-cover");
                    final String listener = Objects.isNull(listenerElement)
                            ? innerDocument.selectFirst("span > strong").text()
                            : innerDocument.select("div.cd-header__info-cover > span > strong").get(1).text();
                    System.out.println("수강자 수: " + removeNotNumeric(listener));
                    int listenerCount = Integer.parseInt(removeNotNumeric(listener));
                    System.out.println("플랫폼: " + PLATFORM);

                    /* 강의 세션 개수 */
                    final String course = innerDocument.selectFirst("span.cd-curriculum__sub-title").text();
                    System.out.println("강의 세션 개수: " + getSessionCount(course));
                    int sessionAmount = Integer.parseInt(getSessionCount(course));


                    InflearnModel inflearnModel = InflearnModel.builder()
                            .thumbnail(thumbnails[j])
                            .title(title)
                            .realPrice(realPrice)
                            .salePrice(salePrice)
                            .rating(rating)
                            .instructor(instructor)
                            .link(link)
                            .listenerCount(listenerCount)
                            .platform(PLATFORM)
                            .sessionAmount(sessionAmount)
                            .build();

                    System.out.println(inflearnModel.getThumbnail());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getRealPrice(final String price) {
        final String[] pricesArray = price.split(" ");
        return pricesArray[0];
    }

    private static String getSalePrice(final String price) {
        final String[] pricesArray = price.split(" ");
        return (pricesArray.length == 1) ? price : pricesArray[1];
    }

    // html 태그 제거
    private static String stripHtml(final String html) {
        return Jsoup.clean(html, Whitelist.none());
    }

    // 맨 앞, 맨 뒤 소괄호 제거
    private static String removeBracket(final String str) {
        return str.replaceAll("^[(]|[)]$", "");
    }

    private static String getSessionCount(final String course) {
        return removeNotNumeric(course.substring(0, course.indexOf("개")));
    }

    private static String removeNotNumeric(final String str) {
        return str.replaceAll("\\W", "");
    }

}
