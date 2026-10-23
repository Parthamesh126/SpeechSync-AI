package com.procucev.speechtotext.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.procucev.speechtotext.entity.Transcription;
import com.procucev.speechtotext.exception.GlobalExceptionHandler;
import com.procucev.speechtotext.exception.TranscriptionNotFoundException;
import com.procucev.speechtotext.service.TranscriptionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class TranscriptionControllerTest {

    @Mock
    private TranscriptionService service;

    @InjectMocks
    private TranscriptionController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper()
                .findAndRegisterModules();
    }

    @Test
    void createShouldReturn201() throws Exception {
        Transcription request = new Transcription();
        request.setTranscript("Hello नमस्कार");
        request.setLanguage("hi-IN");

        Transcription saved = new Transcription();
        saved.setId(1L);
        saved.setTranscript("Hello नमस्कार");
        saved.setLanguage("hi-IN");

        when(service.create(any(Transcription.class))).thenReturn(saved);

        mockMvc.perform(post("/api/transcriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.transcript").value("Hello नमस्कार"))
                .andExpect(jsonPath("$.language").value("hi-IN"));
    }

    @Test
    void getAllShouldReturn200() throws Exception {
        Transcription item = new Transcription();
        item.setId(1L);
        item.setTranscript("Test transcript");
        item.setLanguage("en-IN");

        when(service.getAll()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/transcriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].transcript").value("Test transcript"));
    }

    @Test
    void getByIdShouldReturn200() throws Exception {
        Transcription item = new Transcription();
        item.setId(1L);
        item.setTranscript("Hello");
        item.setLanguage("en-IN");

        when(service.getById(1L)).thenReturn(item);

        mockMvc.perform(get("/api/transcriptions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transcript").value("Hello"));
    }

    @Test
    void getByIdShouldReturn404WhenMissing() throws Exception {
        when(service.getById(999L))
                .thenThrow(new TranscriptionNotFoundException(999L));

        mockMvc.perform(get("/api/transcriptions/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Transcription not found with id: 999"));
    }

    @Test
    void updateShouldReturn200() throws Exception {
        Transcription request = new Transcription();
        request.setTranscript("Updated text");
        request.setLanguage("mr-IN");

        Transcription updated = new Transcription();
        updated.setId(1L);
        updated.setTranscript("Updated text");
        updated.setLanguage("mr-IN");

        when(service.update(
                org.mockito.ArgumentMatchers.eq(1L),
                any(Transcription.class)
        )).thenReturn(updated);

        mockMvc.perform(put("/api/transcriptions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transcript").value("Updated text"))
                .andExpect(jsonPath("$.language").value("mr-IN"));
    }

    @Test
    void deleteShouldReturn204() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc.perform(delete("/api/transcriptions/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }
}