import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Transcription {
  id?: number;
  transcript: string;
  language: string;
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({ providedIn: 'root' })
export class TranscriptionService {
  private readonly api =
    'http://localhost:8080/api/transcriptions';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Transcription[]> {
    return this.http.get<Transcription[]>(this.api);
  }

  create(data: Transcription): Observable<Transcription> {
    return this.http.post<Transcription>(this.api, data);
  }

  update(id: number, data: Transcription): Observable<Transcription> {
    return this.http.put<Transcription>(`${this.api}/${id}`, data);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }
}