package inflearn;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class InflearnCrawling {

    private static final Logger log = LoggerFactory.getLogger(InflearnCrawling.class);
    private static final int FIRST_PAGE_INDEX = 1;
    private static final int LAST_PAGE_INDEX = 32;
    private static final String PLATFORM = "Inflearn";

    public static void insert(InflearnModel model) {
        final String url = "jdbc:mysql://localhost/study?useSSL=false";
        final String sql = "INSERT INTO Lectures VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (java.sql.Connection conn = DriverManager.getConnection(url, "root", "1234");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            Class.forName("com.mysql.cj.jdbc.Driver");

            pstmt.setString(1, null);
            pstmt.setString(2, model.getImageUrl());
            pstmt.setString(3, model.getTitle());
            pstmt.setInt(4, model.getPrice());
            pstmt.setInt(5, model.getSalePrice());
            pstmt.setFloat(6, model.getRating());
            pstmt.setString(7, model.getInstructor());
            pstmt.setString(8, model.getUrl());
            pstmt.setInt(9, model.getViewCount());
            pstmt.setString(10, model.getPlatform());
            pstmt.setInt(11, model.getSessionCount());
            pstmt.setString(12, model.getCurrency());
            pstmt.setString(13, model.getDescription());
            pstmt.setString(14, model.getSkills());
            pstmt.executeUpdate();
        } catch (ClassNotFoundException e) {
            log.error("Driver loading failure");
            e.printStackTrace();
        } catch (SQLException e) {
            log.error("SQL Exception");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            // ?????? ?????? ?????? ????????? ??????
            for (int i = FIRST_PAGE_INDEX; i <= LAST_PAGE_INDEX; i++) {
                final String inflearnUrl = "https://www.inflearn.com/courses/it-programming?order=seq&page=" + i;
                System.out.println("url: " + inflearnUrl);
                Connection conn = Jsoup.connect(inflearnUrl);
                Document document = conn.get();

                // ????????? ?????? ?????? ?????????
                //   - ????????? ??????, ?????? ??????, ??????(????????????), ??????, ?????????, ?????? ??????, ????????? ???, ?????????, ?????? ?????? ?????? + ??????
                Elements imageUrlElements = document.getElementsByClass("swiper-lazy");
                Elements titleElements = document.select("div.card-content > div.course_title");
                Elements priceElements = document.getElementsByClass("price");
                Elements instructorElements = document.getElementsByClass("instructor");
                Elements linkElements = document.select("a.course_card_front");
                Elements descriptionElements = document.select("p.course_description");
                Elements skillElements = document.select("div.course_skills > span");
                String[] imageUrls = new String[imageUrlElements.size()];

                int setIndex = 0;
                int getIndex = 0;

                for (Element e : imageUrlElements) {
                    imageUrls[setIndex++] = e.attr("abs:src");
                }

                for (int j = 0; j < titleElements.size(); j++) {
                    final String title = titleElements.get(j).text();
                    final String price = priceElements.get(j).text();
                    final String realPrice = getRealPrice(price);
                    final String salePrice = getSalePrice(price);
                    final int realIntPrice = toInt(removeNotNumeric(realPrice));
                    final int saleIntPrice = toInt(removeNotNumeric(salePrice));
                    final String currency = String.valueOf(price.charAt(0));
                    final String instructor = instructorElements.get(j).text();
                    final String url = linkElements.get(j).attr("abs:href");
                    final String description = descriptionElements.get(j).text();
                    final String skills = removeWhiteSpace(skillElements.get(j).text());

                    System.out.println("?????????: " + imageUrls[j]);
                    System.out.println("?????? ??????: " + title);
                    System.out.println("??????: " + realIntPrice);
                    System.out.println("?????? ??????: " + saleIntPrice);
                    System.out.println("??????: " + currency);
                    System.out.println("?????????: " + instructor);
                    System.out.println("?????? ??????: " + url);
                    System.out.println("?????? ??????: " + description);
                    System.out.println("?????? ??????: " + skills);

                    /* ?????? ?????? ?????? */
                    Connection innerConn = Jsoup.connect(url);
                    Document innerDocument = innerConn.get();

                    /* ?????? */
                    Element ratingElement = innerDocument.selectFirst("div.dashboard-star__num");
                    final float rating = Objects.isNull(ratingElement)
                            ? toFloat("0")
                            : toFloat(ratingElement.text());
                    System.out.println("??????: " + rating);

                    /* ????????? ??? */
                    Element listenerElement = innerDocument.selectFirst("div.cd-header__info-cover");
                    final String listener = Objects.isNull(listenerElement)
                            ? innerDocument.selectFirst("span > strong").text()
                            : innerDocument.select("div.cd-header__info-cover > span > strong").get(1).text();
                    System.out.println("????????? ???: " + removeNotNumeric(listener));
                    final int viewCount = Integer.parseInt(removeNotNumeric(listener));
                    System.out.println("?????????: " + PLATFORM);

                    /* ?????? ?????? ?????? */
                    Element sessionElement = innerDocument.selectFirst("span.cd-curriculum__sub-title");
                    final int sessionCount = Objects.isNull(sessionElement)
                            ? 82
                            : toInt(getSessionCount(innerDocument.selectFirst("span.cd-curriculum__sub-title").text()));

                    System.out.println("?????? ?????? ??????: " + sessionCount);
                    System.out.println();

                    InflearnModel inflearnModel = InflearnModel.builder()
                            .imageUrl(imageUrls[getIndex++])
                            .title(title)
                            .price(realIntPrice)
                            .salePrice(saleIntPrice)
                            .rating(rating)
                            .instructor(instructor)
                            .url(url)
                            .viewCount(viewCount)
                            .platform(PLATFORM)
                            .sessionCount(sessionCount)
                            .currency(currency)
                            .description(description)
                            .skills(skills)
                            .build();

                    insert(inflearnModel);
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

    // html ?????? ??????
    private static String stripHtml(final String html) {
        return Jsoup.clean(html, Whitelist.none());
    }

    // ??? ???, ??? ??? ????????? ??????
    private static String removeBracket(final String str) {
        return str.replaceAll("^[(]|[)]$", "");
    }

    private static String getSessionCount(final String course) {
        // ex) course = "??? 21???"
        final String endStr = "???";
        return removeNotNumeric(course.substring(0, course.indexOf(endStr)));
    }

    private static String removeNotNumeric(final String str) {
        return str.replaceAll("\\W", "");
    }

    private static String removeWhiteSpace(final String str) {
        return str.replaceAll("\\s", "");
    }

    private static int toInt(final String str) {
        if (str.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(str);
    }

    private static float toFloat(final String str) {
        return Float.parseFloat(str);
    }

}
