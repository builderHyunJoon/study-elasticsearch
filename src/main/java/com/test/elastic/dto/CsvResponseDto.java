package com.test.elastic.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public class CsvResponseDto {
    private String site_id;
    private String sbjt_id_k;
    private String sbjt_name_kskn;
    private String lesson_id_k;
    private String lesson_name_kskn;
    private String original_script;
    private String summarized_script;
    private String _score;

    /**
     * SearchResponseDto의 응답 데이터를 CsvResponseDto 리스트로 변환하는 메서드
     * @param response SearchResponseDto 객체
     * @return CsvResponseDto 리스트
     */
    public static CsvResponseDto fromSearchResponse(SearchResponseDto response) {
        CsvResponseDto csv = new CsvResponseDto();
        if (response == null || response.get_objects() == null) {
            return null;
        }
        SearchResponseDto.Hit hit = response.get_objects().get(0);
            if (hit.get_metadata() != null) {
                csv.setSite_id(hit.get_metadata().getSite_id());
                csv.setSbjt_id_k(hit.get_metadata().getSbjt_id_k());
                csv.setSbjt_name_kskn(hit.get_metadata().getSbjt_name_kskn());
                csv.setLesson_id_k(hit.get_metadata().getLesson_id_k());
                csv.setLesson_name_kskn(hit.get_metadata().getLesson_name_kskn());
                csv.setOriginal_script(hit.get_metadata().getOriginal_script());
                csv.setSummarized_script(hit.get_metadata().getSummarized_script());

            csv.set_score(String.valueOf(hit.get_score()));
        }

            log.info("csv: {}", csv);
        return csv;
    }
}
