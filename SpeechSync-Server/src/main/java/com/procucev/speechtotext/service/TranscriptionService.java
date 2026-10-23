package com.procucev.speechtotext.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.procucev.speechtotext.entity.Transcription;
import com.procucev.speechtotext.exception.TranscriptionNotFoundException;
import com.procucev.speechtotext.repository.TranscriptionRepository;

@Service
public class TranscriptionService {

    private final TranscriptionRepository repository;

    public TranscriptionService(TranscriptionRepository repository) {
        this.repository = repository;
    }

    public Transcription create(Transcription transcription) {
        transcription.setId(null);
        return repository.save(transcription);
    }

    public List<Transcription> getAll() {
        return repository.findAll();
    }

    public Transcription getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new TranscriptionNotFoundException(id));
    }

    @Transactional
    public Transcription update(Long id, Transcription request) {
        Transcription existing = getById(id);
        existing.setTranscript(request.getTranscript());
        existing.setLanguage(request.getLanguage());
        return repository.save(existing);
    }

    public void delete(Long id) {
        Transcription existing = getById(id);
        repository.delete(existing);
    }
}