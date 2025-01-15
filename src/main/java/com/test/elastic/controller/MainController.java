package com.test.elastic.controller;

import com.test.elastic.dto.JhsLessonDto;
import com.test.elastic.service.MainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;

    @PostMapping("elasticsearch")
    public void elasticsearch() throws Exception {
        List<JhsLessonDto>  jhsLessonDocumentList=  mainService.getAllJhsDocuments();

        log.info("lessonDtoList 데이터: {}", jhsLessonDocumentList);
//        List<LessonDto> lessonDtoList = new ArrayList<>();
//        for(JhsLessonDocument jhsLessonDocument : jhsLessonDocumentList) {
//            LessonDto lessonDto = jhsLessonDocument.toDto(jhsLessonDocument);
//            lessonDtoList.add(lessonDto);
//        }
//        log.info("lessonDtoList 데이터: {}", lessonDtoList);
    }
}
