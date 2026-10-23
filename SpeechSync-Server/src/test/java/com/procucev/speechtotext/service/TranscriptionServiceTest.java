package com.procucev.speechtotext.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.procucev.speechtotext.entity.Transcription;
import com.procucev.speechtotext.exception.TranscriptionNotFoundException;
import com.procucev.speechtotext.repository.TranscriptionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TranscriptionServiceTest {

    @Mock
    private TranscriptionRepository repository;

    @InjectMocks
    private TranscriptionService service;

    private Transcription transcription;

    @BeforeEach
    void setUp() {
        transcription = new Transcription();
        transcription.setId(1L);
        transcription.setTranscript("Hello, नमस्कार!");
        transcription.setLanguage("hi-IN");
    }

    @Test
    void createShouldSaveTranscription() {
        when(repository.save(any(Transcription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transcription result = service.create(transcription);

        assertNotNull(result);
        assertEquals("Hello, नमस्कार!", result.getTranscript());
        assertEquals("hi-IN", result.getLanguage());
        assertEquals(null, result.getId());
        verify(repository).save(transcription);
    }

    @Test
    void getAllShouldReturnTranscriptions() {
        when(repository.findAll()).thenReturn(List.of(transcription));

        List<Transcription> result = service.getAll();

        assertEquals(1, result.size());
        assertEquals("Hello, नमस्कार!", result.get(0).getTranscript());
        verify(repository).findAll();
    }

    @Test
    void getByIdShouldReturnTranscription() {
        when(repository.findById(1L)).thenReturn(Optional.of(transcription));

        Transcription result = service.getById(1L);

        assertEquals(1L, result.getId());
        assertEquals("hi-IN", result.getLanguage());
    }

    @Test
    void getByIdShouldThrowWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                TranscriptionNotFoundException.class,
                () -> service.getById(99L)
        );
    }

    @Test
    void updateShouldModifyExistingTranscription() {
        Transcription request = new Transcription();
        request.setTranscript("Updated transcript");
        request.setLanguage("en-IN");

        when(repository.findById(1L)).thenReturn(Optional.of(transcription));
        when(repository.save(any(Transcription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transcription result = service.update(1L, request);

        assertEquals("Updated transcript", result.getTranscript());
        assertEquals("en-IN", result.getLanguage());
        verify(repository).save(transcription);
    }

    @Test
    void deleteShouldRemoveExistingTranscription() {
        when(repository.findById(1L)).thenReturn(Optional.of(transcription));

        service.delete(1L);

        verify(repository).delete(transcription);
    }

    @Test
    void deleteShouldNotDeleteWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                TranscriptionNotFoundException.class,
                () -> service.delete(99L)
        );

        verify(repository, never()).delete(any(Transcription.class));
    }
}