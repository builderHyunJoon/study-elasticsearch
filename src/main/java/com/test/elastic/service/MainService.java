package com.test.elastic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.elastic.dto.HscLessonDto;
import com.test.elastic.dto.JhsLessonDto;
import com.test.elastic.dto.ScriptDto;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainService {

    private final RestHighLevelClient client;

    public List<JhsLessonDto> getAllJhsDocuments() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // 1. SearchRequest 생성
        SearchRequest searchRequest = new SearchRequest("v1-jhs_lesson_concept-20250114");
        searchRequest.source(new SearchSourceBuilder().query(QueryBuilders.matchAllQuery()));

        // 2. Elasticsearch 서버로 요청
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        // 3. 응답을 JhsLessonDocument 리스트로 변환
        List<JhsLessonDto> documents = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            JhsLessonDto document = objectMapper.readValue(hit.getSourceAsString(), JhsLessonDto.class);
            documents.add(document);
        }

        return documents;
    }



    public List<HscLessonDto> getAllHscLessons() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // 1. SearchRequest 생성
        SearchRequest searchRequest = new SearchRequest("hsc_summarized_script_250115_v1");
        searchRequest.source(new SearchSourceBuilder().query(QueryBuilders.matchAllQuery()));

        // 2. Elasticsearch 서버로 요청
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        // 3. 응답을 HscLessonDocument 리스트로 변환
        List<HscLessonDto> documents = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            HscLessonDto document = objectMapper.readValue(hit.getSourceAsString(), HscLessonDto.class);
            documents.add(document);
        }

        return documents;
    }

    public ScriptDto toDto(JhsLessonDto jhsDoc) {
        String fmySiteDsCd = null;
        String prbmCatId = null;
        if (StringUtils.isNotEmpty(jhsDoc.getCategory().get(0).get("cate_cd_4_l"))) {
            prbmCatId = jhsDoc.getCategory().get(0).get("cate_cd_4_l");
        }
        if (StringUtils.isNotEmpty(jhsDoc.getCategory().get(0).get("fmy_site_ds_cd_k"))) {
            fmySiteDsCd = jhsDoc.getCategory().get(0).get("fmy_site_ds_cd_k");
        }

        return ScriptDto.builder()
                .FMY_SITE_DS_CD(fmySiteDsCd) // category의 첫 번째 요소
                .COURSE_ID(jhsDoc.getSbjt_id_k())
                .LECT_ID(jhsDoc.getLesson_id_k())
                .LECT_ITEM_ID(jhsDoc.getIndex_no_l())
                .PRBM_CAT_ID(prbmCatId) // PRBM_CAT_ID 값이 없으므로 null 설정. 필요에 따라 다른 값으로 설정
                .build();
    }

}
