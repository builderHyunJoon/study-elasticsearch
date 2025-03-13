package com.test.elastic.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
public class UpsertDto {

    private Upsert _upsert;

    /**
     * ScriptDto를 받아 UpsertDto 객체로 변환하는 메서드
     */
    public static UpsertDto from(ScriptDto scriptDto) {
        UpsertDto dto = new UpsertDto();
        dto.set_upsert(Upsert.from(scriptDto));
        return dto;
    }

    @Data
    @NoArgsConstructor
    public static class Upsert {
        private String _id;
        private Metadata metadata;
        private List<String> document;

        public static Upsert from(ScriptDto scriptDto) {
            Upsert upsert = new Upsert();
            // JSON의 _id 필드는 lesson_id_k를 사용
            upsert.set_id(scriptDto.getLesson_id_k());
            upsert.setMetadata(Metadata.from(scriptDto));
            // document는 original_script를 리스트에 담아서 사용
            upsert.setDocument(Collections.singletonList(scriptDto.getOriginal_script()));
            return upsert;
        }
    }

    @Data
    @NoArgsConstructor
    public static class Metadata {
        private String site_id;
        private String sbjt_id_k;
        private String sbjt_name_kskn;
        private String lesson_id_k;
        private String lesson_name_kskn;
        private String original_script;
        private String summarized_script;

        public static Metadata from(ScriptDto scriptDto) {
            Metadata metadata = new Metadata();
            metadata.setSbjt_id_k(scriptDto.getSbjt_id_k());
            metadata.setSite_id(scriptDto.getSite_id());
            metadata.setSbjt_name_kskn(scriptDto.getSbjt_name_kskn());
            metadata.setLesson_id_k(scriptDto.getLesson_id_k());
            metadata.setLesson_name_kskn(scriptDto.getLesson_name_kskn());
            metadata.setOriginal_script(scriptDto.getOriginal_script());
            metadata.setSummarized_script(scriptDto.getSummarized_script());
            return metadata;
        }
    }
}



