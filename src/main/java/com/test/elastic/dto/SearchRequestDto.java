package com.test.elastic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequestDto {
    private List<String> text;
    private int size;
    private Filter filter;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Filter {
        private List<Must> must;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Must {
            private String key;
            private Match match;

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Match {
                private String value;
            }
        }
    }
}
