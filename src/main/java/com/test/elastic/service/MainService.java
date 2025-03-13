package com.test.elastic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.elastic.dto.*;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainService {

    private final RestHighLevelClient prdClient;
    private final RestHighLevelClient devClient;

    public List<CsvResponseDto> searchVSdata() throws Exception {
        List<CsvResponseDto> csvResponseDtoList = new ArrayList<>();
        List<LessonDto> summarizedLessonDtoList = getAllSummarizedLesson();
        for(LessonDto lessonDto : summarizedLessonDtoList) {
        List<String> textList = new ArrayList<>();
        textList.add(lessonDto.getSummarized_script());
            SearchRequestDto.Filter filter = new SearchRequestDto.Filter();
            SearchRequestDto.Filter.Must must = new SearchRequestDto.Filter.Must();
            SearchRequestDto.Filter.Must.Match match = new SearchRequestDto.Filter.Must.Match();
            List<SearchRequestDto.Filter.Must> mustList = new ArrayList<>();
            match.setValue(lessonDto.getLesson_id_k());
            must.setKey("lesson_id_k");
            must.setMatch(match);
            mustList.add(must);
            filter.setMust(mustList);
            SearchRequestDto searchRequestDto = SearchRequestDto.builder()
                    .text(textList)
                    .size(5)
                    .filter(filter)
                    .build();

            String url = "http://172.20.70.52:59200/summarized_script/_query";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<SearchRequestDto> requestEntity = new HttpEntity<>(searchRequestDto, headers);
            try {
                ResponseEntity<SearchResponseDto> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, SearchResponseDto.class);
                SearchResponseDto searchResponseDto = response.getBody();
                CsvResponseDto csvResponseDto = CsvResponseDto.fromSearchResponse(searchResponseDto);
                csvResponseDtoList.add(csvResponseDto);
            } catch (Exception e) {
                log.info("Exception: {}", e.getMessage());
            }
        }
        return csvResponseDtoList;
    }

    public void putVSdata() throws Exception {
        List<LessonDto> originJhsLessonDtoList = getAllOriginJhsLesson();
        List<LessonDto> originHscLessonDtoList = getAllOriginHscLesson();
        List<LessonDto> summarizedLessonDtoList = getAllSummarizedLesson();

        // 각 리스트를 lesson_id_k를 키로 하는 맵으로 변환
        Map<String, LessonDto> jhsLessonMap = originJhsLessonDtoList.stream()
                .collect(Collectors.toMap(LessonDto::getLesson_id_k, lesson -> lesson, (existing, replacement) -> existing));

        Map<String, LessonDto> hscLessonMap = originHscLessonDtoList.stream()
                .collect(Collectors.toMap(LessonDto::getLesson_id_k, lesson -> lesson, (existing, replacement) -> existing));

        Map<String, LessonDto> summarizedLessonMap = summarizedLessonDtoList.stream()
                .collect(Collectors.toMap(LessonDto::getLesson_id_k, lesson -> lesson, (existing, replacement) -> existing));

        // 모든 키를 합친 Set 생성
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(jhsLessonMap.keySet());
        allKeys.addAll(hscLessonMap.keySet());
        allKeys.addAll(summarizedLessonMap.keySet());

        // 결과 리스트 생성
        List<LessonDto> mergedList = new ArrayList<>();

        // 각 키에 대해 LessonDto 병합
        for (String key : allKeys) {
            LessonDto mergedLesson = new LessonDto();
            mergedLesson.setLesson_id_k(key);

            LessonDto jhsLesson = jhsLessonMap.get(key);
            LessonDto hscLesson = hscLessonMap.get(key);
            LessonDto summarizedLesson = summarizedLessonMap.get(key);

            if (jhsLesson != null) {
                mergedLesson.setOriginal_script(jhsLesson.getOriginal_script());
            } else if (hscLesson != null) {
                mergedLesson.setOriginal_script(hscLesson.getOriginal_script());
            }

            if (summarizedLesson != null) {
                mergedLesson.setSummarized_script(summarizedLesson.getSummarized_script());
            }

            mergedList.add(mergedLesson);
        }
        log.info("mergedList.size: {}", mergedList.size());
        List<UpsertDto> upsertDtoList = new ArrayList<>();
        for(LessonDto lessonDto : mergedList) {
            List<OriginLessonDto> originLessonDtoList  =  getAllOriginJhsLessons(lessonDto.getLesson_id_k());
            if(originLessonDtoList.isEmpty()) {
                continue;
            }
            ScriptDto scriptDto = OriginLessonToScript(lessonDto, originLessonDtoList.get(0));
            UpsertDto upsertDto = UpsertDto.from(scriptDto);
            upsertDtoList.add(upsertDto);
        }

        log.info("upsertDtoList.size: {}", upsertDtoList.size());

        for(UpsertDto upsertDto : upsertDtoList) {
            String url = "http://172.20.70.52:59200/summarized_script/" + upsertDto.get_upsert().get_id();
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<UpsertDto.Upsert> requestEntity = new HttpEntity<>(upsertDto.get_upsert(), headers);

            try {
                ResponseEntity<UpsertDto.Upsert> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, UpsertDto.Upsert.class);
                log.info("객체 색인 성공");
            } catch (Exception e) {
                log.info("Exception: {}", e.getMessage());
            }


        }

    }



    public List<LessonDto> getAllOriginJhsLesson() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<LessonDto> documents = new ArrayList<>();

        // SearchRequest 생성 및 스크롤 활성화
        SearchRequest searchRequest = new SearchRequest("jhs_original_script_250312_v1");
        searchRequest.scroll(TimeValue.timeValueMinutes(1L));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.size(1000); // 한 번에 가져올 문서 수
        searchRequest.source(searchSourceBuilder);

        // 첫 번째 스크롤 요청
        SearchResponse searchResponse = devClient.search(searchRequest, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHit[] searchHits = searchResponse.getHits().getHits();

        // 문서 처리
        while (searchHits != null && searchHits.length > 0) {
            for (SearchHit hit : searchHits) {
                LessonDto document = objectMapper.readValue(hit.getSourceAsString(), LessonDto.class);
                documents.add(document);
            }

            // 다음 스크롤 요청
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(TimeValue.timeValueMinutes(1L));
            searchResponse = devClient.scroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = searchResponse.getScrollId();
            searchHits = searchResponse.getHits().getHits();
        }

        // 스크롤 컨텍스트 삭제
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        devClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);

        return documents;
    }





    public List<LessonDto> getAllOriginHscLesson() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<LessonDto> documents = new ArrayList<>();

        // SearchRequest 생성 및 스크롤 활성화
        SearchRequest searchRequest = new SearchRequest("hsc_original_script_250312_v1");
        searchRequest.scroll(TimeValue.timeValueMinutes(1L));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.size(1000); // 한 번에 가져올 문서 수
        searchRequest.source(searchSourceBuilder);

        // 첫 번째 스크롤 요청
        SearchResponse searchResponse = devClient.search(searchRequest, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHit[] searchHits = searchResponse.getHits().getHits();

        // 문서 처리
        while (searchHits != null && searchHits.length > 0) {
            for (SearchHit hit : searchHits) {
                LessonDto document = objectMapper.readValue(hit.getSourceAsString(), LessonDto.class);
                documents.add(document);
            }

            // 다음 스크롤 요청
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(TimeValue.timeValueMinutes(1L));
            searchResponse = devClient.scroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = searchResponse.getScrollId();
            searchHits = searchResponse.getHits().getHits();
        }

        // 스크롤 컨텍스트 삭제
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        devClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);

        return documents;
    }


    public List<LessonDto> getAllSummarizedLesson() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<LessonDto> documents = new ArrayList<>();

        // SearchRequest 생성 및 스크롤 활성화
        SearchRequest searchRequest = new SearchRequest("summarized_script_250312_v1");
        searchRequest.scroll(TimeValue.timeValueMinutes(1L));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.size(1000); // 한 번에 가져올 문서 수
        searchRequest.source(searchSourceBuilder);

        // 첫 번째 스크롤 요청
        SearchResponse searchResponse = devClient.search(searchRequest, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHit[] searchHits = searchResponse.getHits().getHits();

        // 문서 처리
        while (searchHits != null && searchHits.length > 0) {
            for (SearchHit hit : searchHits) {
                LessonDto document = objectMapper.readValue(hit.getSourceAsString(), LessonDto.class);
                documents.add(document);
            }

            // 다음 스크롤 요청
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(TimeValue.timeValueMinutes(1L));
            searchResponse = devClient.scroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = searchResponse.getScrollId();
            searchHits = searchResponse.getHits().getHits();
        }

        // 스크롤 컨텍스트 삭제
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        devClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);

        return documents;
    }

    public List<OriginLessonDto> getAllOriginJhsLessons(String lesson_id_k) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // 1. SearchRequest 생성
        SearchRequest searchRequest = new SearchRequest("v1-jhs_lesson_concept-20250204", "v1-hsc_lesson_concept-20250204");
        searchRequest.source(new SearchSourceBuilder().query(QueryBuilders.termQuery("lesson_id_k", lesson_id_k)).size(1000));

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



    public ScriptDto OriginLessonToScript(LessonDto lessonDto, OriginLessonDto originLessonDto) {
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
                .site_id(fmySiteDsCd)
                .sbjt_id_k(originLessonDto.getSbjt_id_k())
                .sbjt_name_kskn(originLessonDto.getSbjt_name_kskn())
                .lesson_id_k(lessonDto.getLesson_id_k())
                .lesson_name_kskn(originLessonDto.getLesson_name_kskn())
                .original_script(lessonDto.getOriginal_script())
                .summarized_script(lessonDto.getSummarized_script())
                .build();
    }





}
