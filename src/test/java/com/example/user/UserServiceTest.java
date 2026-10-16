package com.example.user;

import com.example.user_info.dto.UserRequestDto;
import com.example.user_info.dto.UserResponseDto;
import com.example.user_info.exception.UserAlreadyExistsException;
import com.example.user_info.exception.UserNotExist;
import com.example.user_info.exception.UserNotSaveException;
import com.example.user_info.model.User;
import com.example.user_info.repo.UserRepository;
import com.example.user_info.sevice.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository repo;
    @InjectMocks
    private UserService userService;
    private UserRequestDto request;

    @BeforeEach
    void setUp() {
        request = new UserRequestDto();
        request.setName("Aakash Chaudhary");
        request.setEmail("aakashchaudhary@gmail.com");
        request.setPhone("9766819737");
        request.setPassword("Aakash@12345");
    }

    @Test
    void saveUser_whenEmailIsNew_returnsSavedUser() {
        when(repo.existsByEmail("aakashchaudhary@gmail.com")).thenReturn(false);
        when(repo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        UserResponseDto userResponseDto = userService.saveUser(request);
        assertNotNull(userResponseDto);
        assertEquals("aakashchaudhary@gmail.com", userResponseDto.getEmail());
        assertEquals("Aakash Chaudhary", userResponseDto.getName());
    }

    @Test
    void saveUser_whenEmailExists_throwsAndDoesNotSave() {

        when(repo.existsByEmail(anyString())).thenReturn(true);
        UserAlreadyExistsException ex = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.saveUser(request));

        assertEquals("Email already registered", ex.getMessage());
        //To verify never call the method call when before is thrown
        verify(repo, never()).save(any());
    }

    @Test
        //Existing email is saved on db or not
    void saveUser_whenEmailHasSpacesAndUpperCase_normalizesEmail() {
        request.setEmail("aakashchaudhary@gmail.com");
        when(repo.existsByEmail(anyString())).thenReturn(false);
        when(repo.save(any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        userService.saveUser(request);

        verify(repo).existsByEmail("aakashchaudhary@gmail.com");
    }


    @Test
        //Data is not saved on db
    void saveUser_whenExistsCheckFails_propagatesException() {
        DataAccessException dbError = new DataAccessResourceFailureException("DB down");

        when(repo.existsByEmail(anyString())).thenReturn(false);
        when(repo.save(any(User.class))).thenThrow(dbError);


        UserNotSaveException ex = assertThrows(UserNotSaveException.class,
                () -> userService.saveUser(request));

        assertEquals("Fail to save user", ex.getMessage());
        assertSame(dbError, ex.getCause());
        verify(repo).save(any(User.class));

    }

    @Test
    void getUser_returnsAllUsersAsResponseDtos() {
        User user1 = new User
                (1L, "Ramu", "ramu@gmail.com", "ramu12345", "9766819737",
                        LocalDateTime.now(),new ArrayList<>() );
        User user2 = new User
                (2L, "Shyam", "ram@gmail.com", "9800000000", "hashedPwd2",
                        LocalDateTime.now(),new ArrayList<>() );
        when(repo.findAll()).thenReturn(List.of(user1, user2));
        List<UserResponseDto> user = userService.getUser();
        assertEquals(2, user.size());
        assertEquals("Ramu", user.get(0).getName());
        assertEquals("ramu@gmail.com", user.get(0).getEmail());
        assertEquals("Shyam", user.get(1).getName());
        assertEquals("ram@gmail.com", user.get(1).getEmail());
        verify(repo).findAll();
    }

    @Test
        /* Drop user from mock db (Positive) */
    void drop_UserFromDb() {
        when(repo.existsById(1L)).thenReturn(true);
        userService.dropUser(1L);
        verify(repo).deleteById(1L);
    }

    @Test
        /*Drop user from mock db (Negative) */
    void drop_userFromDb_ExceptionThrow() {
        when(repo.existsById(1L)).thenReturn(false);
        UserNotExist userNotExist =
                assertThrows(UserNotExist.class, () -> userService.dropUser(1L));
        assertEquals("User is not exist in Database1", userNotExist.getMessage());
        verify(repo, never()).deleteById(any());
    }

    @Test
        /* Update user form mock db (Negative)*/
    void update_userFromDb_ExceptionDrow() {
        when(repo.findById(1L)).thenReturn(Optional.empty());
        UserNotExist userNotExist =
                assertThrows(UserNotExist.class, () -> userService.updateUser(1L, request));
        assertEquals("User not found with id 1", userNotExist.getMessage());
        verify(repo, never()).saveAndFlush(any());

    }

//    @Test
//        /*Updating the password*/
//    void updateUser_whenPasswordProvided_savesEncodedPassword() {
//        User existing = existingUser();
//        UserRequestDto request = new UserRequestDto();
//        request.setPassword("NewPass@123");
//
//        when(repo.findById(1L)).thenReturn(Optional.of(existing));
//        when(repo.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
//
//        userService.updateUser(1L, request);
//
//        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
//        verify(repo).saveAndFlush(captor.capture());
//        assertNotEquals("NewPass@123", captor.getValue().getPassword());
//    }

    @Test
    void updateUser_whenNameIsBlank_keepsOldName() {
        User existing = existingUser();
        UserRequestDto request = new UserRequestDto();
        request.setName("   ");

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.saveAndFlush(any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UserResponseDto response = userService.updateUser(1L, request);

        assertEquals("Ramu", response.getName());
    }

    @Test
    void updateUser_whenAllFieldsNull_keepsOldValues() {
        User existing = existingUser();

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.saveAndFlush(any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UserResponseDto response = userService.updateUser(1L, new UserRequestDto());

        assertEquals("Ramu", response.getName());
        assertEquals("ramu@gmail.com", response.getEmail());
    }

    @Test
    void updateUser_whenAllFieldsProvided_updatesAllFields() {

        User existing = existingUser();
        UserRequestDto request = new UserRequestDto();
        request.setName("New Name");
        request.setEmail("new@gmail.com");
        request.setPhone("9800000000");
        request.setPassword("NewPass@123");

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.saveAndFlush(any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0));


        UserResponseDto response = userService.updateUser(1L, request);


        assertEquals("New Name", response.getName());
        assertEquals("new@gmail.com", response.getEmail());
    }

    private User existingUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Ramu");
        user.setEmail("ramu@gmail.com");
        user.setPassword("oldHashedPwd");
        user.setPhoneNumber("9766819737");
        return user;
    }
}

