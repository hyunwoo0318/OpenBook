package Project.OpenBook.Controller;

import Project.OpenBook.Domain.*;
import Project.OpenBook.Dto.choice.ChoiceContentIdDto;
import Project.OpenBook.Dto.question.QuestionDto;
import Project.OpenBook.Repository.category.CategoryRepository;
import Project.OpenBook.Repository.chapter.ChapterRepository;
import Project.OpenBook.Repository.choice.ChoiceRepository;
import Project.OpenBook.Repository.description.DescriptionRepository;
import Project.OpenBook.Repository.topic.TopicRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.TestPropertySource;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {  "spring.config.location=classpath:application-test.yml",
                                    "spring.config.location=classpath:prompt-template.yml"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QuestionControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    ChapterRepository chapterRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    TopicRepository topicRepository;

    @Autowired
    ChoiceRepository choiceRepository;

    @Autowired
    DescriptionRepository descriptionRepository;

    private final String prefix = "http://localhost:";

    String URL;


    @BeforeAll
    public void initTestForChoiceController() {
        URL = prefix + port;
        restTemplate = restTemplate.withBasicAuth("admin1", "admin1");
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        init();
    }

    private void init(){

        //카테고리 전체 저장
        Category c1 = new Category("유물");
        Category c2 = new Category("사건");
        Category c3 = new Category("국가");
        Category c4 = new Category("인물");

        categoryRepository.saveAllAndFlush(Arrays.asList(c1, c2, c3, c4));

        //단원 전체 저장
        Chapter ch1 = new Chapter("ch1", 1);
        Chapter ch2 = new Chapter("ch2", 2);
        Chapter ch3 = new Chapter("ch3", 3);

        chapterRepository.saveAllAndFlush(Arrays.asList(ch1, ch2, ch3));

        //topic 전체 생성
        Random rand = new Random();
        List<Topic> topicList = new ArrayList<>();


        for (int i = 1; i <= 1000; i++) {
            int year = rand.nextInt(2000) + 1;
            int month = rand.nextInt(12) + 1; // 1~12 사이의 월을 랜덤으로 생성
            int day = rand.nextInt(26) + 1; // 1부터 최대 일수 사이의 일을 랜덤으로 생성
            int length = rand.nextInt(500);

            Integer startDate = year * 1000 + month * 100 + day;
            Integer endDate = startDate + length;

            Category c  = null;

            if(i <= 10){
                c = c1;
            }else if(i <= 20){
                c = c2;
            } else if (i <= 30) {
                c = c3;
            }else {
                c = c4;
            }

            Topic topic = new Topic("topic" + i, startDate, endDate, 0, 0, "detail" + i, ch1, c);
            topicList.add(topic);
        }

        topicRepository.saveAllAndFlush(topicList);

        //선지, 보기 생성
        for (Topic topic : topicList) {
            for (int i = 1; i <= 5; i++) {
                choiceRepository.save(new Choice("choice" + i + " in " + topic.getTitle(), topic));
                descriptionRepository.save(new Description("description" + i + " in " + topic.getTitle(), topic));
            }
        }
    }

    @DisplayName("type1 question 생성 성공 - GET /admin/temp-question?type=1&category=사건")
    @Test
    public void type1QuestionSuccess(){
        Long type = 1L;
        String categoryName = "사건";
        String prompt = "해당 사건에 대한 설명으로 옳은 것은?";

        List<Category> all = categoryRepository.findAll();


        ResponseEntity<QuestionDto> response = restTemplate.getForEntity(URL + "/admin/temp-question?type=1&category=사건", QuestionDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        QuestionDto questionDto = response.getBody();

        assertThat(questionDto.getType()).isEqualTo(type);
        assertThat(questionDto.getCategoryName()).isEqualTo(categoryName);
        assertThat(questionDto.getPrompt()).isEqualTo(prompt);

        //정답 선지와 보기의 topic이 동일한지 확인
        Long descriptionId = questionDto.getDescription().getId();
        Optional<Description> descriptionOptional = descriptionRepository.findById(descriptionId);
        assertThat(descriptionOptional.isEmpty()).isFalse();
        Long descriptionTopicId = descriptionOptional.get().getTopic().getId();

        Long answerChoiceId = questionDto.getAnswerChoiceId();
        Optional<Choice> choiceOptional = choiceRepository.findById(answerChoiceId);
        assertThat(choiceOptional.isEmpty()).isFalse();
        Long choiceTopicId = choiceOptional.get().getTopic().getId();

        assertThat(descriptionTopicId).isEqualTo(choiceTopicId);

        //총 선지가 5개 들어갔는지 테스트
        List<Long> choiceIdList = questionDto.getChoiceList().stream().map(q -> q.getId()).collect(Collectors.toList());
        List<Choice> choiceList = choiceRepository.findAllById(choiceIdList);
        assertThat(choiceList.size()).isEqualTo(5);

        //선지 5개의 topic이 모두 category가 요구사항과 맞는지 테스트


        //오답 선지들은 보기와 topic이 다른지 테스트
        for (Choice choice : choiceList) {
            Category category = choiceRepository.queryCategoryByChoice(choice.getId());
            assertThat(category.getName()).isEqualTo(categoryName);
            if(choice.getId() == questionDto.getAnswerChoiceId()){
                continue;
            }else{
                assertThat(choice.getTopic().getId()).isNotEqualTo(descriptionTopicId);
            }
        }
    }

    @DisplayName("type2 question 생성 성공 -  GET /admin/temp-question?type=2&category=사건")
    @Test
    public void type2QuestionSuccess() {
        Long type = 2L;
        String categoryName = "사건";
        String prompt = "해당 사건이 발생한 시기에 동아시아에서 볼 수 있는 모습으로 가장 적절한 것은?";

        ResponseEntity<QuestionDto> response = restTemplate.getForEntity(URL + "/admin/temp-question?type=2&category=사건", QuestionDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        QuestionDto questionDto = response.getBody();

        assertThat(questionDto.getType()).isEqualTo(type);
        assertThat(questionDto.getCategoryName()).isEqualTo(categoryName);
        assertThat(questionDto.getPrompt()).isEqualTo(prompt);

        //보기의 topic의 시간대의 사이에 정답 선지의 시간대가 있는지 테스트
        Long descriptionId = questionDto.getDescription().getId();
        Topic descriptionTopic = topicRepository.queryTopicByDescription(descriptionId);
        Topic answerTopic = topicRepository.queryTopicByChoice(questionDto.getAnswerChoiceId());

        assertThat(answerTopic.getEndDate()>=(descriptionTopic.getEndDate())).isTrue();
        assertThat(answerTopic.getStartDate()<=(descriptionTopic.getStartDate())).isTrue();

        //오답 선지들은 해당 범위에 없는지 테스트
        List<ChoiceContentIdDto> choiceList = questionDto.getChoiceList();
        for (ChoiceContentIdDto c : choiceList) {
            if(c.getId() == questionDto.getAnswerChoiceId()) continue;
            else{
                Topic topic = topicRepository.queryTopicByChoice(c.getId());
                assertThat((topic.getEndDate()<(descriptionTopic.getEndDate())) &&
                        (topic.getStartDate()>(descriptionTopic.getStartDate()))).isFalse();
            }
        }
    }

    @DisplayName("type3 question 생성 성공 -  GET /admin/temp-question?type=3&category=사건")
    @Test
    public void type3QuestionSuccess() {
        Long type = 3L;
        String categoryName = "사건";
        String prompt = "해당 사건이 발생한 이후에 동아시아에서 볼 수 있는 모습으로 가장 적절한 것은?";

        ResponseEntity<QuestionDto> response = restTemplate.getForEntity(URL + "/admin/temp-question?type=3&category=사건", QuestionDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        QuestionDto questionDto = response.getBody();

        assertThat(questionDto.getType()).isEqualTo(type);
        assertThat(questionDto.getCategoryName()).isEqualTo(categoryName);
        assertThat(questionDto.getPrompt()).isEqualTo(prompt);

        //보기의 topic의 시간대의 사이에 정답 선지의 시간대가 있는지 테스트
        Long descriptionId = questionDto.getDescription().getId();
        Topic descriptionTopic = topicRepository.queryTopicByDescription(descriptionId);
        Topic answerTopic = topicRepository.queryTopicByChoice(questionDto.getAnswerChoiceId());

        assertThat(answerTopic.getStartDate()>(descriptionTopic.getEndDate())).isTrue();

        //오답 선지들은 해당 범위에 없는지 테스트
        List<ChoiceContentIdDto> choiceList = questionDto.getChoiceList();
        for (ChoiceContentIdDto c : choiceList) {
            if(c.getId() == questionDto.getAnswerChoiceId()) continue;
            else{
                Topic topic = topicRepository.queryTopicByChoice(c.getId());
                assertThat(topic.getStartDate()>(descriptionTopic.getEndDate())).isFalse();
            }
        }
    }

    @DisplayName("type4 question 생성 성공 -  GET /admin/temp-question?type=4&category=사건")
    @Test
    public void type4QuestionSuccess() {
        Long type = 4L;
        String categoryName = "사건";
        String prompt = "해당 사건이 발생한 이전에 동아시아에서 볼 수 있는 모습으로 가장 적절한 것은?";

        ResponseEntity<QuestionDto> response = restTemplate.getForEntity(URL + "/admin/temp-question?type=4&category=사건", QuestionDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        QuestionDto questionDto = response.getBody();

        assertThat(questionDto.getType()).isEqualTo(type);
        assertThat(questionDto.getCategoryName()).isEqualTo(categoryName);
        assertThat(questionDto.getPrompt()).isEqualTo(prompt);

        //보기의 topic의 시간대의 사이에 정답 선지의 시간대가 있는지 테스트
        Long descriptionId = questionDto.getDescription().getId();
        Topic descriptionTopic = topicRepository.queryTopicByDescription(descriptionId);
        Topic answerTopic = topicRepository.queryTopicByChoice(questionDto.getAnswerChoiceId());

        assertThat(answerTopic.getEndDate()<(descriptionTopic.getStartDate())).isTrue();

        //오답 선지들은 해당 범위에 없는지 테스트
        List<ChoiceContentIdDto> choiceList = questionDto.getChoiceList();
        for (ChoiceContentIdDto c : choiceList) {
            if(c.getId() == questionDto.getAnswerChoiceId()) continue;
            else{
                Topic topic = topicRepository.queryTopicByChoice(c.getId());
                assertThat(topic.getEndDate()<(descriptionTopic.getStartDate())).isFalse();
            }
        }
    }

//    @DisplayName("type5 question 생성 성공 -  GET /admin/temp-question?type=5&category=사건")
//    @Test
//    public void type5QuestionSuccess() {
//        Long type = 5L;
//        String categoryName = "사건";
//        String prompt = "해당 사건이 발생한 시기를 연표에서 옳게 고른 것은?";
//
//        ResponseEntity<QuestionDto> response = restTemplate.getForEntity(URL + "/admin/temp-question?type=5&category=사건", QuestionDto.class);
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//
//        QuestionDto questionDto = response.getBody();
//
//        assertThat(questionDto.getType()).isEqualTo(type);
//        assertThat(questionDto.getCategoryName()).isEqualTo(categoryName);
//        assertThat(questionDto.getPrompt()).isEqualTo(prompt);
//    }



}
