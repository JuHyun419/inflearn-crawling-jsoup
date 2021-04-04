package inflearn;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InflearnModel {

    private String thumbnail;       // 썸네일
    private String title;           // 강의 제목
    private String realPrice;       // 정가
    private String salePrice;       // 할인 가격
    private String rating;          // 평점
    private String instructor;      // 강의자
    private String link;            // 강의 페이지 링크
    private int listenerCount;      // 수강자 수
    private String platform;  // 플랫폼 이름
    private int sessionAmount;     // 강의 세션 개수

}
