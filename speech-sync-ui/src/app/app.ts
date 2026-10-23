
import { CommonModule, isPlatformBrowser } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  Inject,
  NgZone,
  OnDestroy,
  OnInit,
  PLATFORM_ID
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Transcription, TranscriptionService } from './services/transcription';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class AppComponent implements OnInit, OnDestroy {
  transcript = '';
  language = 'hi-IN';
  history: Transcription[] = [];
  editingId: number | null = null;
  listening = false;
  speaking = false;
  error = '';

  private recognition: any;
  private browser = false;
  private finalTranscript = '';

  geminiListening = false;

  private geminiSocket?: WebSocket;
  private audioStream?: MediaStream;
  private audioContext?: AudioContext;
  private audioSource?: MediaStreamAudioSourceNode;
  private audioProcessor?: ScriptProcessorNode;
  private geminiFinalTranscript = '';

  constructor(
    private api: TranscriptionService,
    private zone: NgZone,
    private cdr: ChangeDetectorRef,
    @Inject(PLATFORM_ID) private platformId: object
  ) {}

  ngOnInit(): void {
    this.browser = isPlatformBrowser(this.platformId);

    if (!this.browser) return;

    this.loadHistory();

    const SpeechRecognition =
      (window as any).SpeechRecognition ||
      (window as any).webkitSpeechRecognition;

    if (!SpeechRecognition) {
      this.error =
        'Speech recognition is not supported in this browser. Try Chrome or Edge.';
      return;
    }

    this.recognition = new SpeechRecognition();
    this.recognition.lang = this.language;
    this.recognition.continuous = true;
    this.recognition.interimResults = true;

    this.recognition.onresult = (event: any) => {
      this.zone.run(() => {
        let interimText = '';

        for (let i = event.resultIndex; i < event.results.length; i++) {
          const result = event.results[i];
          const text = result[0]?.transcript ?? '';

          if (result.isFinal) {
            this.finalTranscript += text.trim() + ' ';
          } else {
            interimText += text;
          }
        }

        this.transcript = this.finalTranscript + interimText;
      });
    };

    this.recognition.onerror = (event: any) => {
      this.zone.run(() => {
        this.error = `Speech recognition error: ${event.error}`;
        this.listening = false;
        this.cdr.detectChanges();
      });
    };

    this.recognition.onend = () => {
      this.zone.run(() => {
        this.listening = false;
        this.cdr.detectChanges();
      });
    };
  }

  startListening(): void {
    if (!this.recognition || this.listening) return;

    this.stopSpeaking();
    this.error = '';
    this.recognition.lang = this.language;
    this.finalTranscript = this.transcript.trim()
      ? this.transcript.trim() + ' '
      : '';

    try {
      this.recognition.start();
      this.listening = true;
    } catch {
      this.error = 'Could not start microphone. Please try again.';
    }
  }

  stopListening(): void {
    if (this.recognition && this.listening) {
      this.recognition.stop();
      this.listening = false;
    }
  }

  // Text-to-Speech: read the current transcript aloud.
  speakTranscript(): void {
    if (!this.browser) return;

    const text = this.transcript.trim();

    if (!text) {
      this.error = 'Enter or record some text before playing it.';
      return;
    }

    if (!('speechSynthesis' in window)) {
      this.error = 'Text-to-speech is not supported in this browser.';
      return;
    }

    this.error = '';
    window.speechSynthesis.cancel();

    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = this.language;
    utterance.rate = 1;
    utterance.pitch = 1;

    utterance.onstart = () => {
      this.zone.run(() => {
        this.speaking = true;
        this.cdr.detectChanges();
      });
    };

    utterance.onend = () => {
      this.zone.run(() => {
        this.speaking = false;
        this.cdr.detectChanges();
      });
    };

    utterance.onerror = (event: SpeechSynthesisErrorEvent) => {
      this.zone.run(() => {
        this.speaking = false;
        if (event.error !== 'canceled' && event.error !== 'interrupted') {
          this.error = 'Unable to play speech in this browser.';
        }
        this.cdr.detectChanges();
      });
    };

    window.speechSynthesis.speak(utterance);
  }

  stopSpeaking(): void {
    if (!this.browser || !('speechSynthesis' in window)) return;

    window.speechSynthesis.cancel();
    this.speaking = false;
  }

  loadHistory(): void {
    this.api.getAll().subscribe({
      next: (data) => {
        this.history = [...data];
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load history:', err);
        this.error = 'Unable to load history.';
        this.cdr.detectChanges();
      }
    });
  }

  saveTranscript(): void {
    const text = this.transcript.trim();

    if (!text) {
      this.error = 'Please record or enter some text first.';
      this.cdr.detectChanges();
      return;
    }

    const data: Transcription = {
      transcript: text,
      language: this.language
    };

    const request = this.editingId !== null
      ? this.api.update(this.editingId, data)
      : this.api.create(data);

    request.subscribe({
      next: () => {
        this.transcript = '';
        this.finalTranscript = '';
        this.editingId = null;
        this.error = '';
        this.loadHistory();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Save failed:', err);
        this.error = 'Unable to save transcription.';
        this.cdr.detectChanges();
      }
    });
  }

  edit(item: Transcription): void {
    this.stopSpeaking();
    this.transcript = item.transcript;
    this.finalTranscript = item.transcript + ' ';
    this.language = item.language || 'hi-IN';
    this.editingId = item.id ?? null;

    if (this.browser) {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  delete(item: Transcription): void {
    if (
      item.id == null ||
      !this.browser ||
      !window.confirm('Delete this transcription permanently?')
    ) return;

    this.api.delete(item.id).subscribe({
      next: () => {
        this.history = this.history.filter(
          record => record.id !== item.id
        );
        this.error = '';
        this.cdr.detectChanges();
        this.loadHistory();
      },
      error: (err) => {
        console.error('Delete failed:', err);
        this.error = 'Unable to delete transcription.';
        this.cdr.detectChanges();
      }
    });
  }

  clearTranscript(): void {
    this.stopSpeaking();
    this.transcript = '';
    this.finalTranscript = '';
    this.editingId = null;
    this.error = '';
  }

  ngOnDestroy(): void {
    if (this.recognition) {
      this.recognition.onresult = null;
      this.recognition.onerror = null;
      this.recognition.onend = null;
      this.recognition.abort();
      this.stopGeminiListening();
    }

    this.stopSpeaking();
  }

  
  async startGeminiListening(): Promise<void> {
    if (this.geminiListening) return;

    this.stopListening();
    this.stopSpeaking();
    this.error = '';
    this.geminiFinalTranscript = this.transcript.trim()
      ? this.transcript.trim() + ' '
      : '';

    if (!navigator.mediaDevices?.getUserMedia) {
      this.error = 'Microphone access requires localhost or HTTPS.';
      return;
    }

    try {
      // Get microphone permission.
      this.audioStream = await navigator.mediaDevices.getUserMedia({
        audio: {
          channelCount: 1,
          echoCancellation: true,
          noiseSuppression: true,
          autoGainControl: true
        }
      });

      // Ask our Spring Boot backend for a short-lived token.
      const response = await fetch('https://speechsync-api.onrender.com/api/gemini/token', {
        method: 'POST'
      });

      if (!response.ok) {
        throw new Error(`Token request failed (${response.status})`);
      }

      const tokenData = await response.json();
      const token = tokenData.token as string;

      if (!token) {
        throw new Error('The backend did not return a Gemini token.');
      }

      const socketUrl =
        'wss://generativelanguage.googleapis.com/ws/' +
        'google.ai.generativelanguage.v1beta.GenerativeService.' +
        'BidiGenerateContentConstrained?access_token=' +
        encodeURIComponent(token);

      this.geminiSocket = new WebSocket(socketUrl);

      this.geminiSocket.onopen = () => {
        this.geminiSocket?.send(JSON.stringify({
          setup: {
            model: 'models/gemini-3.5-transcribe-live',
            generationConfig: {
              responseModalities: ['TEXT']
            },
            inputAudioTranscription: {}
          }
        }));
      };

      
      this.geminiSocket.onmessage = async (event: MessageEvent) => {
        try {
          let raw: string;

          if (typeof event.data === 'string') {
            raw = event.data;
          } else if (event.data instanceof Blob) {
            raw = await event.data.text();
          } else if (event.data instanceof ArrayBuffer) {
            raw = new TextDecoder().decode(event.data);
          } else {
            console.warn('Unsupported Gemini message format:', event.data);
            return;
          }

          const message = JSON.parse(raw);

          this.zone.run(() => {
            if (message.setupComplete) {
              this.geminiListening = true;
              this.startGeminiAudioCapture();
              this.cdr.detectChanges();
              return;
            }

            const content = message.serverContent;
            if (!content) {
              console.log('Gemini message:', message);
              return;
            }

            const finalText = content.inputTranscription?.text ?? '';
            const interimText =
              content.interimInputTranscription?.text ?? '';

            if (finalText) {
              this.geminiFinalTranscript += finalText;
              this.transcript = this.geminiFinalTranscript;
            } else if (interimText) {
              this.transcript = this.geminiFinalTranscript + interimText;
            }

            this.cdr.detectChanges();
          });
        } catch (error) {
          console.error('Failed to process Gemini WebSocket message:', error);
        }
      };

      this.geminiSocket.onerror = () => {
        this.zone.run(() => {
          this.error = 'Gemini WebSocket failed. Check the API key, model access and browser console.';
          this.stopGeminiListening();
          this.cdr.detectChanges();
        });
      };

      this.geminiSocket.onclose = (event: CloseEvent) => {
        this.zone.run(() => {
          this.stopGeminiAudioCapture();
          this.geminiListening = false;

          if (event.code !== 1000 && !this.error) {
            this.error = `Gemini connection closed (${event.code}). Try recording again.`;
          }

          this.cdr.detectChanges();
        });
      };
    } catch (error) {
      console.error('Gemini startup failed:', error);
      this.error = error instanceof Error
        ? error.message
        : 'Unable to start Gemini transcription.';

      this.stopGeminiListening();
    }
  }

  private startGeminiAudioCapture(): void {
    if (!this.audioStream || !this.geminiSocket) return;

    this.audioContext = new AudioContext();
    this.audioSource =
      this.audioContext.createMediaStreamSource(this.audioStream);

    // 4096 frames gives reasonably small audio chunks.
    this.audioProcessor =
      this.audioContext.createScriptProcessor(4096, 1, 1);

    const inputSampleRate = this.audioContext.sampleRate;

    this.audioProcessor.onaudioprocess = (event: AudioProcessingEvent) => {
      if (this.geminiSocket?.readyState !== WebSocket.OPEN) return;

      const input = event.inputBuffer.getChannelData(0);
      const pcm = this.resampleTo16k(input, inputSampleRate);

      const bytes = new Uint8Array(pcm.buffer);
      let binary = '';

      // Avoid passing a large array to String.fromCharCode at once.
      for (let i = 0; i < bytes.length; i += 8192) {
        binary += String.fromCharCode(
          ...bytes.subarray(i, Math.min(i + 8192, bytes.length))
        );
      }

      this.geminiSocket.send(JSON.stringify({
        realtimeInput: {
          audio: {
            data: btoa(binary),
            mimeType: 'audio/pcm;rate=16000'
          }
        }
      }));
    };

    this.audioSource.connect(this.audioProcessor);

    // Required for ScriptProcessorNode callbacks in many browsers.
    this.audioProcessor.connect(this.audioContext.destination);
  }

  private resampleTo16k(
    input: Float32Array,
    inputSampleRate: number
  ): Int16Array {
    const targetRate = 16000;

    if (inputSampleRate === targetRate) {
      return this.floatToPcm16(input);
    }

    const outputLength = Math.max(
      1,
      Math.round(input.length * targetRate / inputSampleRate)
    );

    const output = new Float32Array(outputLength);
    const ratio = inputSampleRate / targetRate;

    for (let i = 0; i < outputLength; i++) {
      const position = i * ratio;
      const left = Math.floor(position);
      const right = Math.min(left + 1, input.length - 1);
      const fraction = position - left;

      output[i] =
        input[left] * (1 - fraction) + input[right] * fraction;
    }

    return this.floatToPcm16(output);
  }

  private floatToPcm16(input: Float32Array): Int16Array {
    const output = new Int16Array(input.length);

    for (let i = 0; i < input.length; i++) {
      const sample = Math.max(-1, Math.min(1, input[i]));
      output[i] = sample < 0
        ? sample * 32768
        : sample * 32767;
    }

    return output;
  }

  stopGeminiListening(): void {
    this.stopGeminiAudioCapture();

    if (
      this.geminiSocket &&
      this.geminiSocket.readyState === WebSocket.OPEN
    ) {
      this.geminiSocket.send(JSON.stringify({
        realtimeInput: {
          audioStreamEnd: true
        }
      }));

      this.geminiSocket.close(1000, 'Recording stopped');
    } else if (this.geminiSocket) {
      this.geminiSocket.close();
    }

    this.geminiSocket = undefined;
    this.geminiListening = false;
  }

  private stopGeminiAudioCapture(): void {
    if (this.audioProcessor) {
      this.audioProcessor.onaudioprocess = null;
      this.audioProcessor.disconnect();
      this.audioProcessor = undefined;
    }

    this.audioSource?.disconnect();
    this.audioSource = undefined;

    this.audioStream?.getTracks().forEach(track => track.stop());
    this.audioStream = undefined;

    if (this.audioContext && this.audioContext.state !== 'closed') {
      void this.audioContext.close();
    }
    this.audioContext = undefined;
  }

}