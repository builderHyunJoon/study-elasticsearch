package com.test.elastic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.elastic.dto.OriginLessonDto;
import com.test.elastic.dto.ScriptDto;
import com.test.elastic.dto.SummarizedLessonDto;
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

    private final RestHighLevelClient prdClient;
    private final RestHighLevelClient devClient;

    public List<ScriptDto> setLessonInfo() throws IOException {
        List<ScriptDto> scriptDtoList = new ArrayList<>();

        List<SummarizedLessonDto> summarizedJhsLessonDtoList = getAllSummarizedJhsLesson();
        for(SummarizedLessonDto summarizedJhsLessonDto : summarizedJhsLessonDtoList) {
            List<OriginLessonDto> originJhsLessonDtoList = getAllOriginJhsLessons(summarizedJhsLessonDto.getLesson_id_k());
            ScriptDto scriptDto = OriginLessonToScript(summarizedJhsLessonDto, originJhsLessonDtoList.get(0));
            log.info("jhs scriptDto :{}", scriptDto);
            scriptDtoList.add(scriptDto);
        }

        List<SummarizedLessonDto> summarizedHscLessonDtoList = getAllSummarizedHscLesson();
        for(SummarizedLessonDto summarizedHscLessonDto : summarizedHscLessonDtoList) {
            List<OriginLessonDto> originHscLessonDtoList = getAllOriginHscLessons(summarizedHscLessonDto.getLesson_id_k());
            ScriptDto scriptDto = OriginLessonToScript(summarizedHscLessonDto, originHscLessonDtoList.get(0));
            log.info("hsc scriptDto :{}", scriptDto);
            scriptDtoList.add(scriptDto);
        }
        return scriptDtoList;
    }

    public List<SummarizedLessonDto> getAllSummarizedJhsLesson() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // 1. SearchRequest 생성
        SearchRequest searchRequest = new SearchRequest("jhs_summarized_script_250115_v1");
        searchRequest.source(new SearchSourceBuilder().query(QueryBuilders.matchAllQuery()).size(5000));

        // 2. Elasticsearch 서버로 요청
        SearchResponse response = devClient.search(searchRequest, RequestOptions.DEFAULT);

        // 3. 응답을 JhsLessonDocument 리스트로 변환
        List<SummarizedLessonDto> documents = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            SummarizedLessonDto document = objectMapper.readValue(hit.getSourceAsString(), SummarizedLessonDto.class);
            documents.add(document);
        }

        return documents;
    }



    public List<OriginLessonDto> getAllOriginJhsLessons(String lesson_id_k) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // 1. SearchRequest 생성
        SearchRequest searchRequest = new SearchRequest("v1-jhs_lesson_concept-20250114");
        searchRequest.source(new SearchSourceBuilder().query(QueryBuilders.termQuery("lesson_id_k", lesson_id_k)).size(5000));

        // 2. Elasticsearch 서버로 요청
        SearchResponse response = prdClient.search(searchRequest, RequestOptions.DEFAULT);

        // 3. 응답을 HscLessonDocument 리스트로 변환
        List<OriginLessonDto> documents = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            OriginLessonDto document = objectMapper.readValue(hit.getSourceAsString(), OriginLessonDto.class);
            documents.add(document);
        }

        return documents;
    }


    public List<SummarizedLessonDto> getAllSummarizedHscLesson() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // 1. SearchRequest 생성
        SearchRequest searchRequest = new SearchRequest("hsc_summarized_script_250115_v1");
        searchRequest.source(new SearchSourceBuilder().query(QueryBuilders.matchAllQuery()).size(5000));

        // 2. Elasticsearch 서버로 요청
        SearchResponse response = devClient.search(searchRequest, RequestOptions.DEFAULT);

        // 3. 응답을 JhsLessonDocument 리스트로 변환
        List<SummarizedLessonDto> documents = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            SummarizedLessonDto document = objectMapper.readValue(hit.getSourceAsString(), SummarizedLessonDto.class);
            documents.add(document);
        }

        return documents;
    }



    public List<OriginLessonDto> getAllOriginHscLessons(String lesson_id_k) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // 1. SearchRequest 생성
        SearchRequest searchRequest = new SearchRequest("v1-hsc_lesson_concept-20250114");
        searchRequest.source(new SearchSourceBuilder().query(QueryBuilders.termQuery("lesson_id_k", lesson_id_k)).size(5000));

        // 2. Elasticsearch 서버로 요청
        SearchResponse response = prdClient.search(searchRequest, RequestOptions.DEFAULT);

        // 3. 응답을 HscLessonDocument 리스트로 변환
        List<OriginLessonDto> documents = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            OriginLessonDto document = objectMapper.readValue(hit.getSourceAsString(), OriginLessonDto.class);
            documents.add(document);
        }

        return documents;
    }

    public ScriptDto OriginLessonToScript(SummarizedLessonDto summarizedLessonDto, OriginLessonDto originLessonDto) {
        String fmySiteDsCd = null;
        String prbmCatId = null;

        if (StringUtils.isNotEmpty(originLessonDto.getCategory().get(0).get("fmy_site_ds_cd_k"))) {
            fmySiteDsCd = originLessonDto.getCategory().get(0).get("fmy_site_ds_cd_k");
        }

        if(StringUtils.isNotEmpty(fmySiteDsCd) && fmySiteDsCd.equals("JHS")) {
            if (StringUtils.isNotEmpty(originLessonDto.getCategory().get(0).get("cate_cd_4_l"))) {
                prbmCatId = originLessonDto.getCategory().get(0).get("cate_cd_4_l");
            }
        } else if(StringUtils.isNotEmpty(fmySiteDsCd) && fmySiteDsCd.equals("HSC")) {
            if (StringUtils.isNotEmpty(originLessonDto.getCategory().get(0).get("cate_cd_5_l"))) {
                prbmCatId = originLessonDto.getCategory().get(0).get("cate_cd_5_l");
            }
        }

        return ScriptDto.builder()
                .FMY_SITE_DS_CD(fmySiteDsCd)
                .COURSE_ID(originLessonDto.getSbjt_id_k())
                .LECT_ID(summarizedLessonDto.getLesson_id_k())
                .SUMRY_CNTN(summarizedLessonDto.getSummarized_script())
                .PRBM_CAT_ID(prbmCatId)
                .build();
    }

}
