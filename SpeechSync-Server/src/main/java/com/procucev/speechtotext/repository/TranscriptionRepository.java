package com.procucev.speechtotext.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.procucev.speechtotext.entity.Transcription;

public interface TranscriptionRepository extends JpaRepository<Transcription, Long> {
	
}