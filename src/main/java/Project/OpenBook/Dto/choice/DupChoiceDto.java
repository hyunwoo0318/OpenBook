package Project.OpenBook.Dto.choice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DupChoiceDto {

    private String content;

    private Long id;

    private Boolean isSelected;
}
