package com.test.elastic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScriptDto {
    String FMY_SITE_DS_CD;
    Long COURSE_ID;
    String LECT_ID;
    Long LECT_ITEM_ID;
    String PRBM_CAT_ID;
}
