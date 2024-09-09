package com.bucheon.yeoddadae;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.List;

public class SttService extends Service {
    int originalStreamVolume;
    AudioManager amanager;

    private final String TAG = "SttService";
    private SpeechRecognizer speechRecognizer;
    private SttCallback sttCallback;
    private boolean isListeningForWakeUpWord = false;

    public interface SttCallback {
        void onMainCommandReceived(String mainCommand);
        void onUpdateUI(String message);
    }

    public void setSttCallback(SttCallback callback) {
        sttCallback = callback;
    }

    public class SttBinder extends Binder {
        public SttService getService() {
            return SttService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new SttBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        amanager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        originalStreamVolume = amanager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        listenForWakeUpWord();
        return START_STICKY;
    }

    public void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    public void startListeningForWakeUpWord() {
        stopListening();
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        listenForWakeUpWord();
    }

    public void startListeningForMainCommand() {
        stopListening();
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        listenForMainCommand();
    }

    private void listenForWakeUpWord() {
        isListeningForWakeUpWord = true;

        amanager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND);

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR"); // 언어를 한국어로 설정

        speechRecognizer.startListening(intent); // 듣기 시작

        speechRecognizer.setRecognitionListener(new RecognitionListener() { // RecognitionListener 설정
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "호출어듣기 : 준비완료");
                if (sttCallback != null) {
                    sttCallback.onUpdateUI("호출어듣는중");
                }
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "호출어듣기 : 말하기시작");
            }

            @Override
            public void onResults(Bundle results) {
                List<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null && matches.size() > 0) {
                    String wakeUpWord = matches.get(0);

                    Log.d(TAG, "호출어듣기 : 내용 : " + wakeUpWord);
                    
                    if (wakeUpWord.contains("음성 명령")) { // 웨이크업 워드가 감지되었는지 확인
                        Log.d(TAG, "호출어듣기 : 호출어 확인 성공");
                        amanager.setStreamVolume(AudioManager.STREAM_MUSIC, originalStreamVolume, AudioManager.FLAG_PLAY_SOUND);
                        startListeningForMainCommand();
                    }
                    else {
                        Log.d(TAG, "호출어듣기 : 호출어가 아님");
                        if (isListeningForWakeUpWord) { // 계속해서 웨이크업 워드를 듣기 시작. 돌아가기를 처리하기 전에 플래그를 확인
                            listenForWakeUpWord();
                        }
                    }
                }
                else { // 결과가 null 이거나 없음
                    Log.d(TAG, "호출어듣기 : 듣기 결과가 null 이거나 없음");
                    listenForWakeUpWord();
                }
            }

            @Override
            public void onError(int error) {
                switch (error) {
                    case SpeechRecognizer.ERROR_NO_MATCH: // 음성을 인식하지 못했을 때의 처리를 여기에 추가
                        Log.d(TAG, "호출어듣기 : 음성을 인식하지 못했습니다");
                        listenForWakeUpWord();
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: // 음성 입력이 타임아웃 되었을 때
                        Log.d(TAG, "호출어듣기 : 타임아웃");
                        listenForWakeUpWord();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onRmsChanged(float rmsdB) {}
            @Override
            public void onBufferReceived(byte[] buffer) {}
            @Override
            public void onEndOfSpeech() {}
            @Override
            public void onPartialResults(Bundle partialResults) {}
            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void listenForMainCommand() {
        isListeningForWakeUpWord = false;  // 플래그를 false로 설정

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR"); // 언어를 한국어로 설정

        speechRecognizer.startListening(intent); // 듣기 시작

        speechRecognizer.setRecognitionListener(new RecognitionListener() { // RecognitionListener 설정
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "명령어듣기 : 준비완료");
                if (sttCallback != null) {
                    sttCallback.onUpdateUI("메인명령어듣는중");
                }
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "명령어듣기 : 말하기시작");
            }

            @Override
            public void onResults(Bundle results) {
                List<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null && matches.size() > 0) {
                    String mainCommand = matches.get(0);

                    Log.d(TAG, "명령어듣기 : 내용 : " + mainCommand);
                    if (sttCallback != null) {
                        sttCallback.onMainCommandReceived(mainCommand);
                    }
                }
            }

            @Override
            public void onError(int error) {
                switch (error) {
                    case SpeechRecognizer.ERROR_NO_MATCH: // 음성을 인식하지 못했을 때의 처리를 여기에 추가
                        Log.d(TAG, "명령어듣기 : 음성인식실패");
                        if (sttCallback != null) {
                            sttCallback.onUpdateUI("음성인식실패");
                        }
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: // 음성 입력이 타임아웃 되었을 때의 처리를 여기에 추가
                        Log.d(TAG, "명령어듣기 : 타임아웃");
                        if (sttCallback != null) {
                            sttCallback.onUpdateUI("타임아웃");
                        }
                        break;
                    // 다른 오류 코드에 대한 처리를 추가할 수 있습니다.
                    default:
                        // 그 외의 경우에 대한 처리
                        break;
                }
            }

            @Override
            public void onRmsChanged(float rmsdB) {}
            @Override
            public void onBufferReceived(byte[] buffer) {}
            @Override
            public void onEndOfSpeech() {}
            @Override
            public void onPartialResults(Bundle partialResults) {}
            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    @Override
    public void onDestroy() {
        stopListening();
        super.onDestroy();
    }
}