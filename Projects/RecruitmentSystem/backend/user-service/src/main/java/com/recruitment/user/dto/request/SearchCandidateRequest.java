package com.recruitment.user.dto.request;
import com.recruitment.user.entity.LanguageLevel;
import com.recruitment.user.entity.WorkArrangement;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter; import lombok.Setter;
import java.util.List;
@Getter @Setter public class SearchCandidateRequest {
    @Size(max = 200) private String keyword;
    @Size(max = 20) private List<String> skills;
    @Size(max = 20) private List<String> locations;
    @Min(0) private Integer minimumExperienceMonths;
    private List<String> languageCodes;
    private LanguageLevel minimumLanguageLevel;
    private List<WorkArrangement> workArrangements;
    @Min(0) private Integer page = 0;
    @Min(1) @Max(100) private Integer size = 20;
    private String sort;
}
