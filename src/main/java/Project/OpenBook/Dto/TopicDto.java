package Project.OpenBook.Dto;

import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopicDto {

    @Min(value = 0, message = "단원 번호를 입력해주세요.")
    private int chapter;

    @NotBlank(message = "상세정보 제목을 입력해주세요.")
    private String title;

    @NotBlank(message = "카테고리를 입력해주세요")
    private String category;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

   /* @NotBlank(message = "설명을 입력해주세요.")*/
    private String detail;

}