package Project.OpenBook.Controller;

import Project.OpenBook.Dto.Sentence.SentenceCreateDto;
import Project.OpenBook.Dto.Sentence.SentenceUpdateDto;
import Project.OpenBook.Service.SentenceService;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SentenceController {

    private final SentenceService sentenceService;


    @ApiOperation("문장 생성")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201" , description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 토픽 제목 입력")
    })
    @PostMapping("/admin/sentences")
    public ResponseEntity createSentence(@Validated @RequestBody SentenceCreateDto sentenceCreateDto){
        sentenceService.createSentence(sentenceCreateDto);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @ApiOperation("문장 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200" , description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 문장 아이디 입력")
    })
    @PatchMapping("/admin/sentences/{sentenceId}")
    public ResponseEntity updateSentence(@PathVariable Long sentenceId, @Validated @RequestBody SentenceUpdateDto sentenceUpdateDto){
        sentenceService.updateSentence(sentenceId,sentenceUpdateDto);
        return new ResponseEntity(HttpStatus.OK);
    }

    @ApiOperation("문장 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200" , description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 문장 아이디 입력")
    })
    @DeleteMapping("/admin/sentences/{sentenceId}")
    public ResponseEntity createSentence(@PathVariable Long sentenceId){
        sentenceService.deleteSentence(sentenceId);
        return new ResponseEntity(HttpStatus.OK);
    }

}
