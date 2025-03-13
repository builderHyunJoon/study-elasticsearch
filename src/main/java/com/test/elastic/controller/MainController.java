package com.test.elastic.controller;

import com.opencsv.CSVWriter;
import com.test.elastic.dto.CsvResponseDto;
import com.test.elastic.dto.ScriptDto;
import com.test.elastic.service.MainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;

    @GetMapping("/search-vs")
    public ResponseEntity<?> searchVSdata() throws Exception {
        List<CsvResponseDto> csvResponseDtoList = mainService.searchVSdata();

        StringWriter stringWriter = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(stringWriter);

        // CSV 헤더 작성
        String[] header = {"site_id", "sbjt_id_k", "sbjt_name_kskn", "lesson_id_k", "lesson_name_kskn", "original_script", "summarized_script", "_score"};
        csvWriter.writeNext(header);

        // 데이터 작성
        for (CsvResponseDto csvResponseDto : csvResponseDtoList) {
            String originalScript = csvResponseDto.getOriginal_script();
            if (originalScript != null && originalScript.length() > 30000) {
                originalScript = originalScript.substring(0, 30000);
            }
            String[] data = {
                    csvResponseDto.getSite_id(),
                    csvResponseDto.getSbjt_id_k(),
                    csvResponseDto.getSbjt_name_kskn(),
                    csvResponseDto.getLesson_id_k(),
                    csvResponseDto.getLesson_name_kskn(),
                    originalScript,
                    csvResponseDto.getSummarized_script(),
                    csvResponseDto.get_score()
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

//    @GetMapping("/elasticsearch-to-csv")
//    public ResponseEntity<byte[]> downloadCsv() throws IOException {
//        List<ScriptDto> scriptDtoList = mainService.setLessonInfo();
//
//        StringWriter stringWriter = new StringWriter();
//        CSVWriter csvWriter = new CSVWriter(stringWriter);
//
//            // CSV 헤더 작성
//            String[] header = {"FMY_SITE_DS_CD", "COURSE_ID", "LECT_ID", "ORIGIN_SCRIPT", "SUMRY_CNTN", "PRBM_CAT_ID", "USE_YN", "DELT_YN"};
//            csvWriter.writeNext(header);
//
//            // 데이터 작성
//            for (ScriptDto scriptDto : scriptDtoList) {
//                String[] data = {
//                        scriptDto.getFMY_SITE_DS_CD(),
//                        scriptDto.getCOURSE_ID(),
//                        scriptDto.getLECT_ID(),
//                        scriptDto.getORIGIN_SCRIPT(),
//                        scriptDto.getSUMRY_CNTN(),
//                        scriptDto.getPRBM_CAT_ID(),
//                        "Y",
//                        "N"
//                };
//                csvWriter.writeNext(data);
//            }
//
//            // CSV 파일 생성 및 다운로드
//            byte[] csvBytes = stringWriter.toString().getBytes(Charset.forName("EUC-KR"));
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(new MediaType("text", "csv"));
//            headers.setContentDispositionFormData("attachment", "script_data.csv");
//            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
//
//            return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
//
//    }

}
