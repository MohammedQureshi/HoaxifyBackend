package com.hoaxify.hoaxify;

import com.hoaxify.hoaxify.error.ApiError;
import com.hoaxify.hoaxify.shared.GenericResponse;
import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserControllerTest {

    public static final String API_1_0_USERS = "/api/1.0/users";
    @Autowired
    TestRestTemplate testRestTemplate;
    @Autowired
    UserRepository userRepository;

    @Before
    public void cleanup(){
        userRepository.deleteAll();
    }

    @Test
    public void postUser_whenUserIsValid_receiveOk(){
        User user = createValidUser();
        ResponseEntity<Object> response = postSignUp(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void postUser_whenUserIsValid_userSavedToDatabase(){
        User user = createValidUser();
        postSignUp(user, Object.class);
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    public void postUser_whenUserIsValid_receiveSuccessMessage(){
        User user = createValidUser();
        ResponseEntity<GenericResponse> response = postSignUp(user, GenericResponse.class);
        assertThat(response.getBody().getMessage()).isNotNull();
    }

    @Test
    public void postUser_whenUserIsValid_passwordIshasheedInDatabase(){
        User user = createValidUser();
        postSignUp(user, Object.class);
        List<User> users = userRepository.findAll();
        User inDB = users.get(0);
        assertThat(inDB.getPassword()).isNotEqualTo(user.getPassword());
    }

    @Test
    public void postUser_whenUserHasNullUsername_recieveBadRequest(){
        User user = createValidUser();
        user.setUsername(null);
        ResponseEntity<Object> response =  postSignUp(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasNullDisplayName_recieveBadRequest(){
        User user = createValidUser();
        user.setDisplayName(null);
        ResponseEntity<Object> response =  postSignUp(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasNullPassword_recieveBadRequest(){
        User user = createValidUser();
        user.setPassword(null);
        ResponseEntity<Object> response =  postSignUp(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasUsernameWithLessThenRequired_recieveBadRequest(){
        User user = createValidUser();
        user.setUsername("abc");
        ResponseEntity<Object> response =  postSignUp(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasDisplayNameWithLessThenRequired_recieveBadRequest(){
        User user = createValidUser();
        user.setDisplayName("abc");
        ResponseEntity<Object> response =  postSignUp(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasPasswordWithLessThenRequired_recieveBadRequest(){
        User user = createValidUser();
        user.setPassword("P4sswd");
        ResponseEntity<Object> response =  postSignUp(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasUsernameExceedsTheLengthLimit_recieveBadRequest(){
        User user = createValidUser();
        String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
        user.setUsername(valueOf256Chars);
        ResponseEntity<Object> response =  postSignUp(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasDisplayNameExceedsTheLengthLimit_recieveBadRequest(){
        User user = createValidUser();
        String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
        user.setDisplayName(valueOf256Chars);
        ResponseEntity<Object> response =  postSignUp(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasPasswordExceedsTheLengthLimit_recieveBadRequest(){
        User user = createValidUser();
        String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
        user.setPassword(valueOf256Chars + "A1");
        ResponseEntity<Object> response =  postSignUp(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasPasswordExceedsWithAllLowercase_recieveBadRequest(){
        User user = createValidUser();
        user.setPassword("alllowercase");
        ResponseEntity<Object> response =  postSignUp(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasPasswordExceedsWithAllUppercase_recieveBadRequest(){
        User user = createValidUser();
        user.setPassword("UPPERCASE");
        ResponseEntity<Object> response =  postSignUp(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserHasPasswordExceedsWithAllNumber_recieveBadRequest(){
        User user = createValidUser();
        user.setPassword("123456789");
        ResponseEntity<Object> response =  postSignUp(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenUserIsInvalid_recieveApiError(){
        User user = new User();
        ResponseEntity<ApiError> response = postSignUp(user, ApiError.class);
        assertThat(response.getBody().getUrl()).isEqualTo(API_1_0_USERS);
    }

    @Test
    public void postUser_whenUserIsInvalid_recieveApiErrorWithValidationError(){
        User user = new User();
        ResponseEntity<ApiError> response = postSignUp(user, ApiError.class);
        assertThat(response.getBody().getValidationErrors().size()).isEqualTo(3);
    }

    @Test
    public void postUser_whenUserHasNullUsername_receiveMessageOfNullErrorForUsername(){
        User user = createValidUser();
        user.setUsername(null);
        ResponseEntity<ApiError> response = postSignUp(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("username")).isEqualTo("Username can not be null");
    }

    @Test
    public void postUser_whenUserHasNullPassword_receiveGenericMessageOfNullError(){
        User user = createValidUser();
        user.setPassword(null);
        ResponseEntity<ApiError> response = postSignUp(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("password")).isEqualTo("Can not be null");
    }

    @Test
    public void postUser_whenUserHasInvalidLengthUsername_receiveGenetricMessageOfSizeError(){
        User user = createValidUser();
        user.setUsername("abc");
        ResponseEntity<ApiError> response = postSignUp(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("username")).isEqualTo("It must have minimum 4 and maximum 255 characters");
    }

    @Test
    public void postUser_whenUserHasInvalidPasswordPattern_receiveMessageOfPasswordPatternError(){
        User user = createValidUser();
        user.setPassword("alllowercase");
        ResponseEntity<ApiError> response = postSignUp(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("password")).isEqualTo("Password must have at least one uppercase, one lowercase letter");
    }

    @Test
    public void postUser_whenAnotherUserHasSameUsername_receiveBadRequest(){
        userRepository.save(createValidUser());
        User user = createValidUser();
        ResponseEntity<Object> response = postSignUp(user, Object.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void postUser_whenAnotherUserHasSameUsername_receiveMessageOfDuplicateUsername(){
        userRepository.save(createValidUser());
        User user = createValidUser();
        ResponseEntity<ApiError> response = postSignUp(user, ApiError.class);
        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertThat(validationErrors.get("username")).isEqualTo("This name is in use");
    }

    public <T> ResponseEntity<T> postSignUp(Object request, Class<T> response){
        return testRestTemplate.postForEntity(API_1_0_USERS, request, response);
    }

    private User createValidUser() {
        User user = new User();
        user.setUsername("test-user");
        user.setDisplayName("test-display");
        user.setPassword("P4ssword");
        return user;
    }
}
