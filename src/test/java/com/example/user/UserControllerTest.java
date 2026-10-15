package com.example.user;

import com.example.user_info.controller.UserController;
import com.example.user_info.dto.UserRequestDto;
import com.example.user_info.dto.UserResponseDto;

import com.example.user_info.sevice.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@Slf4j
@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
        //Getting response of user in positive
    void getAllUser_shouldReturn200_withUserList() {
        List<UserResponseDto> userResponseDtos = new ArrayList<>(
                Arrays.asList(new UserResponseDto
                                (1L, "Ram", "ram@gmail.com",
                                        "9766819737", LocalDateTime.now()),
                        new UserResponseDto(2L, "Shyam", "shyamu@gmail.com",
                                "9876543210", LocalDateTime.now())));

        when(userService.getUser()).thenReturn(userResponseDtos);
//Status code , other like length of userResponseDto ,message etc.
        try {
            mockMvc.perform(get("/all_user"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                            .value("Users fetched successfully"))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].name").value("Ram"))
                    .andExpect(jsonPath("$.data[1].email").value("shyamu@gmail.com"));
            verify(userService, times(1)).getUser();
        } catch (Exception e) {
            log.error("Testing is error in controller ", e);
        }
    }

    @Test
    void getAllUser_shouldReturn200_withEmptyList_whenNoUsers() throws Exception {
        when(userService.getUser()).thenReturn(List.of());

        mockMvc.perform(get("/all_user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void deleteUser_shouldReturnVoid() throws Exception {
        // Service ko dropUser() le kehi return garanx (void), tara chahi call bhayo ki bhayena check garne
        doNothing().when(userService).dropUser(1L);
        mockMvc.perform(delete("/1"))
                .andExpect(status().isNoContent());
        verify(userService, times(1)).dropUser(1L);
    }

    @Test
    void deleteUser_shouldReturn400_whenIdIsNotNumber() throws Exception {
        mockMvc.perform(delete("/abc"))
                .andExpect(status().isBadRequest());

        // Invalid id bhayo bhane service samma pugnu hunna
        verify(userService, never()).dropUser(any());
    }

    @Test
    void createUser_shouldReturn201_withCreatedUser() throws Exception {
        UserResponseDto userResponseDto = new UserResponseDto(1L, "Dinesh Thapa",
                "dinesh@gmail.com", "9766819737", LocalDateTime.now());
        when(userService.saveUser(any(UserRequestDto.class))).thenReturn(userResponseDto);
        mockMvc.perform(post("/create-newUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("" +
                                "{\"name\":\"Dinesh Thapa\",\"email\":\"dinesh@gmail.com\",\"phone\":\"9766819737\",\"password\":\"Password@12345\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data.userId").value(1L))
                .andExpect(jsonPath("$.data.name").value("Dinesh Thapa"));
    }

    @Test
    void createUser_shouldReturn400_whenEmailIsInvalid() throws Exception {
        mockMvc.perform(post("/create-newUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("" +
                                "{\"name\":\"Dinesh Thapa\",\"email\":\"not-email\",\"phone\":\"9766819737\",\"password\":\"Password@12345\"}"))
                .andExpect(status().isBadRequest());

        // Validation fail bhayo bhane service samma pugnu hunna
        verify(userService, never()).saveUser(any());
    }

    @Test
    void updateUser_shouldReturn200_withUpdatedUser() throws Exception {
        UserResponseDto updatedUser = new UserResponseDto(1L, "Dinesh Updated",
                "dinesh@gmail.com", "9766819737", LocalDateTime.now());
        when(userService.updateUser(eq(1L), any(UserRequestDto.class))).thenReturn(updatedUser);
        mockMvc.perform(put("/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("" +
                                "{\"name\":\"Dinesh Updated\",\"email\":\"dinesh@gmail.com\",\"phone\":\"9766819737\",\"password\":\"Password@12345\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User updated successfully"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.name").value("Dinesh Updated"));

        verify(userService, times(1)).updateUser(eq(1L), any(UserRequestDto.class));
    }

//    @Test
//    void updateUser_shouldReturn404_whenUserNotFound() throws Exception {
//        when(userService.updateUser(eq(99L), any(UserRequestDto.class)))
//                .thenThrow(new UserNotExist("User not found with id: 99"));
//
//        mockMvc.perform(put("/99")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"name\":\"Dinesh Updated\",\"email\":\"dinesh@gmail.com\",\"phone\":\"9766819737\",\"password\":\"Password@12345\"}"))
//                .andDo(print())
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.message").value("User not found with id: 99"));
//    }
}


