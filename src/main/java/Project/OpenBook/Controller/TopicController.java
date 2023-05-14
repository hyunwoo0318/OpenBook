package Project.OpenBook.Controller;

import Project.OpenBook.Domain.Topic;
import Project.OpenBook.Dto.error.ErrorDto;
import Project.OpenBook.Dto.topic.TopicDto;
import Project.OpenBook.Dto.topic.TopicTitleListDto;
import Project.OpenBook.Service.ChapterService;
import Project.OpenBook.Service.TopicService;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
public class TopicController {

    private final TopicService topicService;
    private final ChapterService chapterService;

    @ApiOperation(value = "각 토픽에 대한 상세정보 조회")
    @GetMapping("/topics/{topicTitle}")
    public ResponseEntity queryTopics( @PathVariable("topicTitle") String topicTitle) {
        TopicDto topicDto = topicService.queryTopic(topicTitle);
        if (topicDto==null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(topicDto, HttpStatus.OK);
    }


    @ApiOperation("해당 단원의 모든 topic 조회")
    @GetMapping("/chapters/{number}/topics")
    public ResponseEntity queryChapterTopics(@PathVariable("number") int number) {
        List<Topic> topicList = chapterService.getTopicsInChapter(number);
        if (topicList == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        List<String> topicTitleList = topicList.stream().map(t -> t.getTitle()).collect(Collectors.toList());
        TopicTitleListDto dto = new TopicTitleListDto(topicTitleList);
        return new ResponseEntity(dto, HttpStatus.OK);
    }

    @ApiOperation(value = "새로운 상세정보 입력")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "상세정보 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력으로 상세정보 생성 실패"),
    })
    @PostMapping("/admin/topics")
    public ResponseEntity createTopic(@Validated @RequestBody TopicDto topicDto, BindingResult bindingResult) {
        List<ErrorDto> errorDtoList = new ArrayList<>();

        if (bindingResult.hasErrors()) {
           errorDtoList  = bindingResult.getFieldErrors().stream().map(err -> new ErrorDto(err.getField(), err.getDefaultMessage())).collect(Collectors.toList());
           return new ResponseEntity(errorDtoList, HttpStatus.BAD_REQUEST);
        }

        Topic topic = topicService.createTopic(topicDto, errorDtoList);
        if (topic == null) {
            return new ResponseEntity(errorDtoList, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity(HttpStatus.CREATED);
    }

    @ApiOperation(value = "상세정보 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상세정보 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력으로 인해 상세정보 수정 실패"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 상세정보 수정 시도")
    })
    @PatchMapping("/admin/topics/{topicTitle}")
    public ResponseEntity updateTopic(@PathVariable("topicTitle")String topicTitle,@RequestBody TopicDto topicDto, BindingResult bindingResult) {
        System.out.println(topicDto.toString());
        List<ErrorDto> errorDtoList = new ArrayList<>();
        if (bindingResult.hasErrors()) {
            List<ObjectError> allErrors = bindingResult.getAllErrors();
            for (ObjectError allError : allErrors) {
                System.out.println(allError.getObjectName() + " " + allError.getDefaultMessage());
            }
            errorDtoList = bindingResult.getFieldErrors().stream().map(err -> new ErrorDto(err.getField(), err.getDefaultMessage())).collect(Collectors.toList());
            return new ResponseEntity(errorDtoList, HttpStatus.BAD_REQUEST);
        }

        Topic topic = topicService.updateTopic(topicTitle, topicDto, errorDtoList);
        if (topic == null && errorDtoList.isEmpty()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        if (!errorDtoList.isEmpty()) {
            for (ErrorDto errorDto : errorDtoList) {
                System.out.println(errorDto.getField() + " " + errorDto.getMessage() );
            }
            return new ResponseEntity(errorDtoList, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity(HttpStatus.OK);
    }

    @ApiOperation(value = "상세정보 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적인 삭제"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 상세정보 삭제 요청")
    })
    @DeleteMapping("/admin/topics/{topicTitle}")
    public ResponseEntity deleteTopic(@PathVariable("topicTitle") String topicTitle) {
        if(!topicService.deleteTopic(topicTitle)){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(HttpStatus.OK);
    }
}
