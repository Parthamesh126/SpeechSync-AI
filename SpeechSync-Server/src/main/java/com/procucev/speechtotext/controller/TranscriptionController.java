package com.procucev.speechtotext.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.procucev.speechtotext.entity.Transcription;
import com.procucev.speechtotext.service.TranscriptionService;

@RestController
@RequestMapping("/api/transcriptions")
@CrossOrigin(origins = "http://localhost:4200")
public class TranscriptionController {

    private final TranscriptionService service;

    public TranscriptionController(TranscriptionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transcription create(
            @Valid @RequestBody Transcription transcription) {
        return service.create(transcription);
    }

    @GetMapping
    public List<Transcription> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Transcription getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Transcription update(
            @PathVariable Long id,
            @Valid @RequestBody Transcription transcription) {
        return service.update(id, transcription);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}