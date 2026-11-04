package com.example.public_test.controller;


import com.example.public_controller.PublicController;
import com.example.user_info.sevice.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublicController.class)   // 2.7.x: @MockBean, not @MockitoBean
class PublicControllerSuggestUsernameTest {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    UserService userService;

    // ── POSITIVE ────────────────────────────────────────────

    @Test
    @DisplayName("200 + body from service")
    void returns200AndBody() throws Exception {
        when(userService.suggestUsername("bikash")).thenReturn("bikash123");

        mockMvc.perform(get("/suggest-username").param("name", "bikash"))
                .andExpect(status().isOk())
                .andExpect(content().string("bikash123"));

        verify(userService).suggestUsername("bikash");
        verifyNoMoreInteractions(userService);
    }

    @Test
    @DisplayName("URL-encoded name is decoded before service call")
    void decodesName() throws Exception {
        when(userService.suggestUsername("Bikash Tharu")).thenReturn("Bikash.Tharu");

        mockMvc.perform(get("/suggest-username").param("name", "Bikash Tharu"))
                .andExpect(status().isOk())
                .andExpect(content().string("Bikash.Tharu"));

        verify(userService).suggestUsername("Bikash Tharu");
    }

    @Test
    @DisplayName("extra params ignored")
    void ignoresExtraParams() throws Exception {
        when(userService.suggestUsername("bikash")).thenReturn("bikash");

        mockMvc.perform(get("/suggest-username")
                        .param("name", "bikash")
                        .param("role", "admin"))
                .andExpect(status().isOk());

        verify(userService).suggestUsername("bikash");
    }

    // ── NEGATIVE ────────────────────────────────────────────

    @Test
    @DisplayName("missing 'name' → 400, service not called")
    void missingParamIs400() throws Exception {
        mockMvc.perform(get("/suggest-username"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }


    @Test
    @DisplayName("keyless query '?Bikash Tharu' → 400")
    void keylessQueryIs400() throws Exception {
        mockMvc.perform(get(URI.create("/suggest-username?Bikash%20Tharu")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("wrong method POST → 405")
    void postIs405() throws Exception {
        mockMvc.perform(post("/suggest-username").param("name", "bikash"))
                .andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("empty name forwarded as \"\" (no validation)")
    void emptyParamForwarded() throws Exception {
        when(userService.suggestUsername("")).thenReturn("");

        mockMvc.perform(get("/suggest-username").param("name", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(userService).suggestUsername("");
    }

//        @Test
//        void serviceFailureIs500() throws Exception {
//            when(userService.suggestUsername("bikash"))
//                    .thenThrow(new IllegalStateException("db down"));
//
//           mockMvc.perform(get("/suggest-username").param("name", "bikash"))
//                    .andExpect(content().string("")); // Boot error body is empty in @WebMvcTest without advice
//
//            verify(userService).suggestUsername("bikash");
//        }
//    }
}

