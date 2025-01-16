package com.test.elastic.controller;

import com.opencsv.CSVWriter;
import com.test.elastic.dto.OriginLessonDto;
import com.test.elastic.dto.ScriptDto;
import com.test.elastic.dto.SummarizedLessonDto;
import com.test.elastic.service.MainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;

    @GetMapping("/elasticsearch-to-csv")
    public ResponseEntity<byte[]> downloadCsv() throws IOException {
        List<ScriptDto> scriptDtoList = mainService.setLessonInfo();

        StringWriter stringWriter = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(stringWriter);

            // CSV 헤더 작성
            String[] header = {"FMY_SITE_DS_CD", "COURSE_ID", "LECT_ID", "ORIGIN_SCRIPT", "SUMRY_CNTN", "PRBM_CAT_ID", "USE_YN", "DELT_YN"};
            csvWriter.writeNext(header);

            // 데이터 작성
            for (ScriptDto scriptDto : scriptDtoList) {
                String[] data = {
                        scriptDto.getFMY_SITE_DS_CD(),
                        scriptDto.getCOURSE_ID(),
                        scriptDto.getLECT_ID(),
                        scriptDto.getORIGIN_SCRIPT(),
                        scriptDto.getSUMRY_CNTN(),
                        scriptDto.getPRBM_CAT_ID(),
                        "Y",
                        "N"
                };
                csvWriter.writeNext(data);
            }

            // CSV 파일 생성 및 다운로드
            byte[] csvBytes = stringWriter.toString().getBytes(Charset.forName("EUC-KR"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("text", "csv"));
            headers.setContentDispositionFormData("attachment", "script_data.csv");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);

    }

}
