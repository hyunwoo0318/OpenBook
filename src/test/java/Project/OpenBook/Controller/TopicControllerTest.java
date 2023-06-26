package Project.OpenBook.Controller;

import Project.OpenBook.Domain.*;
import Project.OpenBook.Dto.error.ErrorDto;
import Project.OpenBook.Dto.error.ErrorMsgDto;
import Project.OpenBook.Dto.keyword.KeywordDto;
import Project.OpenBook.Dto.topic.TopicDto;
import Project.OpenBook.Repository.category.CategoryRepository;
import Project.OpenBook.Repository.chapter.ChapterRepository;
import Project.OpenBook.Repository.choice.ChoiceRepository;
import Project.OpenBook.Repository.description.DescriptionRepository;
import Project.OpenBook.Repository.dupdate.DupDateRepository;
import Project.OpenBook.Repository.keyword.KeywordRepository;
import Project.OpenBook.Repository.topic.TopicRepository;
import Project.OpenBook.Repository.topickeyword.TopicKeywordRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = { "spring.config.location=classpath:application-test.yml" })
class TopicControllerTest {
    @LocalServerPort
    int port;
    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private ChapterRepository chapterRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired private DupDateRepository dupDateRepository;

    @Autowired
    KeywordRepository keywordRepository;

    @Autowired
    TopicKeywordRepository topicKeywordRepository;

    @Autowired
    ChoiceRepository choiceRepository;

    @Autowired
    DescriptionRepository descriptionRepository;
    @Autowired
    TestRestTemplate restTemplate;

    private final String prefix = "http://localhost:";
    private String suffix;
    private String URL;

    private Topic t1,t2;

    private Chapter ch1;
    private Category c1;

    private Keyword k1,k2,k3;

    private void initConfig() {
        URL = prefix + port + suffix;
        restTemplate = restTemplate.withBasicAuth("admin1", "admin1");
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    private void baseSetting() {
        c1 = new Category("c1");
        categoryRepository.saveAndFlush(c1);

        ch1 = new Chapter("ch1", 1);
        chapterRepository.saveAndFlush(ch1);

        t1 = new Topic("title1", 100, 200, 0, 0, "detail1", ch1, c1);
        t2 = new Topic("title2", 300, 400, 0, 0, "detail2", ch1, c1);
        topicRepository.saveAndFlush(t1);
        topicRepository.saveAndFlush(t2);

        k1 = new Keyword("k1");
        k2 = new Keyword("k2");
        k3 = new Keyword("k3");
        keywordRepository.saveAllAndFlush(Arrays.asList(k1, k2, k3));

        TopicKeyword topicKeyword1 = new TopicKeyword(t1, k1);
        TopicKeyword topicKeyword2 = new TopicKeyword(t1, k2);
        topicKeywordRepository.saveAndFlush(topicKeyword1);
        topicKeywordRepository.saveAndFlush(topicKeyword2);
    }

    private void baseClear() {
        topicKeywordRepository.deleteAllInBatch();
        keywordRepository.deleteAllInBatch();
        dupDateRepository.deleteAllInBatch();
        topicRepository.deleteAllInBatch();
        chapterRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("특정 토픽 상세정보 조회 - GET /topics/{topicTitle}")
    @TestInstance(PER_CLASS)
    public class queryTopic{
        @BeforeAll
        public void init(){
            suffix = "/topics/";
            initConfig();
        }

        @AfterEach
        public void clear(){
            baseClear();
        }

        @BeforeEach
        public void setting() {
            baseSetting();
        }
        @DisplayName("특정 토픽 상세정보 조회 성공")
        @Test
        public void queryTopicSuccess() {
            ResponseEntity<TopicDto> response = restTemplate.getForEntity(URL + "title1", TopicDto.class);

            TopicDto expectResult = new TopicDto(1, "title1", "c1", 100, 200, "detail1");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(expectResult);
        }

        @DisplayName("존재하지 않는 토픽 조회 요청")
        @Test
        public void queryTopicFail() {
            ResponseEntity<ErrorMsgDto> response = restTemplate.getForEntity(URL + "title-1", ErrorMsgDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(new ErrorMsgDto("존재하지 않는 토픽 제목입니다."));
        }
    }


    @Nested
    @DisplayName("특정 토픽의 전체 키워드 조회 - GET /topics/{topicTitle}/keywords")
    @TestInstance(PER_CLASS) //TODO
    public class queryTopicKeyword{
        @BeforeAll
        public void init(){
            suffix = "/topics/";
            initConfig();
        }

        @AfterEach
        public void clear(){
            baseClear();
        }

        @BeforeEach
        public void setting() {
            baseSetting();
        }
        @DisplayName("특정 토픽의 전체 키워드 조회 성공")
        @Test
        public void queryTopicSuccess() {
            ResponseEntity<List<KeywordDto>> response = restTemplate.exchange(URL + "title1/keywords", HttpMethod.GET, null, new ParameterizedTypeReference<List<KeywordDto>>() {
            });

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(Arrays.asList(new KeywordDto("k1"), new KeywordDto("k2")));
        }

        @DisplayName("특정 토픽의 전체 키워드 조회 실패 - 존재하지 않는 토픽 제목 입력하기")
        @Test
        public void queryTopicFail(){
            ResponseEntity<ErrorMsgDto> response = restTemplate.getForEntity(URL + "title-1/keywords", ErrorMsgDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(new ErrorMsgDto("존재하지 않는 토픽 제목입니다."));
        }
    }
    @Nested
    @DisplayName("토픽 추가 - POST /admin/topics")
    @TestInstance(PER_CLASS)
    public class createTopic{
        @BeforeAll
        public void init(){
            suffix = "/admin/topics";
            initConfig();
        }

        @AfterEach
        public void clear(){
            baseClear();
        }

        @BeforeEach
        public void setting() {
            baseSetting();
        }
        @DisplayName("토픽 생성 성공")
        @Test
        public void queryKeywordsSuccess() {
            TopicDto topicDto = new TopicDto(1, "title33", "c1", 19980318, 20230321, "detail2");

            ResponseEntity<Void> response = restTemplate.postForEntity(URL, topicDto, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(topicRepository.findAll().size()).isEqualTo(3);
            assertThat(topicRepository.findTopicByTitle("title2").isPresent()).isTrue();
        }

        @DisplayName("토픽 생성 실패 - DTO validation")
        @Test
        public void createTopicFailWrongDTO(){
            //필수 입력 조건인 chapterNum,title,categoryName 생략
            TopicDto topicDto = new TopicDto();

            ResponseEntity<List<ErrorDto>> response = restTemplate.exchange(URL, HttpMethod.POST, new HttpEntity<>(topicDto), new ParameterizedTypeReference<List<ErrorDto>>() {
            });

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().size()).isEqualTo(3);
        }

        @DisplayName("새로운 토픽 추가 실패 - 제목이 중복되는 경우")
        @Test
        public void createTopicFailDupTitle() {
            TopicDto wrongDto = new TopicDto(1, "title1", "c1", 0, 0, "detail123");
            ResponseEntity<ErrorMsgDto> response = restTemplate.exchange(URL, HttpMethod.POST, new HttpEntity<>(wrongDto), ErrorMsgDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(new ErrorMsgDto("중복된 토픽 제목입니다."));
        }

        @DisplayName("새로운 토픽 추가 실패 - 존재하지 않는 단원 번호를 입력하는 경우")
        @Test
        public void createTopicFailWNotExistChapter() {
            TopicDto wrongDto = new TopicDto(123, "title123", "c1", 0, 0, "detail123");
            ResponseEntity<ErrorMsgDto> response = restTemplate.exchange(URL, HttpMethod.POST, new HttpEntity<>(wrongDto), ErrorMsgDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(new ErrorMsgDto("존재하지 않는 단원 번호입니다."));
        }

        @DisplayName("새로운 토픽 추가 실패 - 존재하지 않는 카테고리 이름을 입력하는 경우")
        @Test
        public void createTopicFailNotExistCategory() {
            TopicDto wrongDto = new TopicDto(1, "title123", "c-1", 0, 0, "detail123");
            ResponseEntity<ErrorMsgDto> response = restTemplate.exchange(URL, HttpMethod.POST, new HttpEntity<>(wrongDto), ErrorMsgDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(new ErrorMsgDto("존재하지 않는 카테고리 제목입니다."));
        }

    }

    @Nested
    @DisplayName("해당 토픽에 키워드 추가 - POST /admin/topics")
    @TestInstance(PER_CLASS)
    public class addKeywordInTopic{
        @BeforeAll
        public void init(){
            suffix = "/admin/topics/";
            initConfig();
        }

        @AfterEach
        public void clear(){
            baseClear();
        }

        @BeforeEach
        public void setting() {
            baseSetting();
        }
        @DisplayName("해당 토픽에 키워드 추가 성공 - 기존에 존재하는 키워드의 경우")
        @Test
        public void addExistedKeywordsSuccess() {
            KeywordDto k3Dto = new KeywordDto("k3");

            ResponseEntity<Void> response = restTemplate.postForEntity(URL + "title1/keywords", k3Dto, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            List<String> keywordList = topicRepository.queryTopicKeywords("title1");
            assertThat(keywordList.size()).isEqualTo(3);
        }

        @DisplayName("해당 토픽에 키워드 추가 성공 - 새로운 키워드를 입력한 경우")
        @Test
        public void addNotExistedKeywordsSuccess() {
            KeywordDto dto = new KeywordDto("k5000");

            ResponseEntity<Void> response = restTemplate.postForEntity(URL + "title1/keywords", dto, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            List<String> keywordList = topicRepository.queryTopicKeywords("title1");
            assertThat(keywordList.size()).isEqualTo(3);

            assertThat(keywordRepository.findByName("k5000").isPresent()).isTrue();
        }

        @DisplayName("해당 토픽에 키워드 추가 실패 - 이미 해당 토픽에서 해당 키워드를 가지고 있는 경우")
        @Test
        public void addKeywordsFail(){
            KeywordDto k1Dto = new KeywordDto("k1");

            ResponseEntity<Void> response = restTemplate.postForEntity(URL + "title1/keywords", k1Dto, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            List<String> keywordList = topicRepository.queryTopicKeywords("title1");
            assertThat(keywordList.size()).isEqualTo(2);
        }



    }

    @Nested
    @DisplayName("토픽 상세정보 수정 - PATCH /admin/topics/{topicTitle}")
    @TestInstance(PER_CLASS)
    public class updateTopic{
        @BeforeAll
        public void init(){
            suffix = "/admin/topics/";
            initConfig();
        }

        @AfterEach
        public void clear(){
            baseClear();
        }

        @BeforeEach
        public void setting() {
            baseSetting();
            Category c2 = new Category("c2");
            categoryRepository.saveAndFlush(c2);
        }

        @DisplayName("기존 상세정보 변경 성공 - PATCH admin/topics")
        @Test
        public void updateTopicSuccess() {
            TopicDto dto = new TopicDto(1, "title3", "c2", -1000, 1000, "detail123");

            ResponseEntity<Void> response = restTemplate.exchange(URL + "title1", HttpMethod.PATCH, new HttpEntity<>(dto), Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(topicRepository.findTopicByTitle("title1").isEmpty()).isTrue();

            TopicDto expectDto = topicRepository.queryTopicDto("title3");
            assertThat(expectDto).usingRecursiveComparison().isEqualTo(dto);
        }

        @DisplayName("기존 상세정보 변경 실패 - DTO Validation")
        @Test
        public void updateTopicFailWrongDto() {
            //필수 입력 조건인 chapterNum,title,categoryName 생략
            TopicDto topicDto = new TopicDto();
            ResponseEntity<List<ErrorDto>> response = restTemplate.exchange(URL + "title1", HttpMethod.PATCH, new HttpEntity<>(topicDto), new ParameterizedTypeReference<List<ErrorDto>>() {
            });

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().size()).isEqualTo(3);
        }

        @DisplayName("기존 토픽 변경 실패 - 중복된 제목을 입력하는 경우")
        @Test
        public void updateTopicFailDupTitle() {
            TopicDto wrongDto = new TopicDto(1, "title2", "c1", 0, 0, "detail123");
            ResponseEntity<ErrorMsgDto> response = restTemplate.exchange(URL+"title1", HttpMethod.PATCH, new HttpEntity<>(wrongDto), ErrorMsgDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(new ErrorMsgDto("중복된 토픽 제목입니다."));
        }

        @DisplayName("기존 토픽 변경 실패 - 존재하지 않는 단원 입력")
        @Test
        public void updateTopicFailNotExistChapter() {
            TopicDto wrongDto = new TopicDto(123, "title123", "c1", 0, 0, "detail123");
            ResponseEntity<ErrorMsgDto> response = restTemplate.exchange(URL+"title1", HttpMethod.PATCH, new HttpEntity<>(wrongDto), ErrorMsgDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(new ErrorMsgDto("존재하지 않는 단원 번호입니다."));
        }

        @DisplayName("기존 토픽 변경 실패 - 존재하지 않는 카테고리 입력 ")
        @Test
        public void updateTopicFailNotExistCategory() {
            TopicDto wrongDto = new TopicDto(1, "title123", "c-1", 0, 0, "detail123");
            ResponseEntity<ErrorMsgDto> response = restTemplate.exchange(URL+"title1", HttpMethod.PATCH, new HttpEntity<>(wrongDto), ErrorMsgDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(new ErrorMsgDto("존재하지 않는 카테고리 제목입니다."));
        }
    }

    @Nested
    @DisplayName("토픽 삭제 - DELETE /admin/topics/{topicTitle}")
    @TestInstance(PER_CLASS)
    public class deleteTopic{
        @BeforeAll
        public void init(){
            suffix = "/admin/topics/";
            initConfig();
        }

        @AfterEach
        public void clear(){
            choiceRepository.deleteAllInBatch();
            descriptionRepository.deleteAllInBatch();
            baseClear();
        }

        @BeforeEach
        public void setting() {
            c1 = new Category("c1");
            categoryRepository.saveAndFlush(c1);

            ch1 = new Chapter("ch1", 1);
            chapterRepository.saveAndFlush(ch1);

            t1 = new Topic("title1", 100, 200, 0, 0, "detail1", ch1, c1);
            t2 = new Topic("title2", 300, 400, 0, 0, "detail2", ch1, c1);
            topicRepository.saveAndFlush(t1);
            topicRepository.saveAndFlush(t2);

            k1 = new Keyword("k1");
            k2 = new Keyword("k2");
            k3 = new Keyword("k3");
            keywordRepository.saveAllAndFlush(Arrays.asList(k1, k2, k3));
        }

        @DisplayName("토픽 삭제 성공")
        @Test
        public void deleteTopicSuccess() {
            ResponseEntity<Void> response = restTemplate.exchange(URL + "title1", HttpMethod.DELETE, null, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(topicRepository.findTopicByTitle("title1").isEmpty()).isTrue();
        }

        @DisplayName("토픽 삭제 실패 - 존재하지 않는 토픽 제목 입력")
        @Test
        public void deleteNotExistTopicFail() {
            ResponseEntity<ErrorMsgDto> response = restTemplate.exchange(URL + "/title3", HttpMethod.DELETE, null, ErrorMsgDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(new ErrorMsgDto("존재하지 않는 토픽 제목입니다."));
            assertThat(topicRepository.findTopicByTitle("title1").isPresent()).isTrue();
        }

        @DisplayName("토픽 삭제 실패 - 키워드가 존재하는 토픽 삭제 시도")
        @Test
        public void deleteTopicHasKeywordFail() {
            TopicKeyword topicKeyword = new TopicKeyword(t1, k1);
            topicKeywordRepository.saveAndFlush(topicKeyword);

            ResponseEntity<ErrorMsgDto> response = restTemplate.exchange(URL + "/title1", HttpMethod.DELETE, null, ErrorMsgDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(new ErrorMsgDto("해당 토픽에 키워드가 존재합니다."));
            assertThat(topicRepository.findTopicByTitle("title1").isPresent()).isTrue();
        }

        @DisplayName("토픽 삭제 실패 - 선지가 존재하는 경우")
        @Test
        public void deleteTopicHasChoiceFail() {
            Choice choice1 = new Choice("ch1", t1);
            choiceRepository.save(choice1);
            ResponseEntity<ErrorMsgDto> response = restTemplate.exchange(URL + "/title1", HttpMethod.DELETE, null, ErrorMsgDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(new ErrorMsgDto("해당 토픽에 선지가 존재합니다."));
            assertThat(topicRepository.findTopicByTitle("title1").isPresent()).isTrue();
        }

        @DisplayName("토픽 삭제 실패 - 보기가 존재하는 경우")
        @Test
        public void deleteTopicHasDescFail() {
            Description desc1 = new Description("desc1", t1);
            descriptionRepository.save(desc1);
            ResponseEntity<ErrorMsgDto> response = restTemplate.exchange(URL + "/title1", HttpMethod.DELETE, null, ErrorMsgDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(new ErrorMsgDto("해당 토픽에 보기가 존재합니다."));
            assertThat(topicRepository.findTopicByTitle("title1").isPresent()).isTrue();
        }

    }

    private TopicDto topicDtoConvertor(Topic topic) {
        return new TopicDto(topic.getChapter().getNumber(), topic.getTitle(), topic.getCategory().getName(),
                topic.getStartDate(), topic.getEndDate(), topic.getDetail());
    }
}