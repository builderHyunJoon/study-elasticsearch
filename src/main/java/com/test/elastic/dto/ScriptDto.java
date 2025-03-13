package com.test.elastic.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScriptDto {
    String site_id;
    String sbjt_id_k;
    String sbjt_name_kskn;
    String lesson_id_k;
    String lesson_name_kskn;
    String summarized_script;
    String original_script;


//    String FMY_SITE_DS_CD;
//    String COURSE_ID;
//    String LECT_ID;
//    String LECT_ITEM_ID;
//    String ORIGIN_SCRIPT;
//    String SUMRY_CNTN;
//    String PRBM_CAT_ID;
}
