package com.test.elastic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponseDto {
    private int took;
    private int status;
    private String _collection;
    private Object _vector;
    private List<Hit> _objects;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Hit {
        private String _id;
        private double _score;
        private Metadata _metadata;
        private Object _vector;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadata {
        private String _id;
        private String lesson_id_k;
        private String lesson_name_kskn;
        private String original_script;
        private String sbjt_id_k;
        private String sbjt_name_kskn;
        private String site_id;
        private String summarized_script;
    }
}
