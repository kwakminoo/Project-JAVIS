# IRIS: 인공지능 비서 앱 (Android + GPT)

![IRIS UI](https://github.com/your-username/iris/assets/preview.png) <!-- 필요 시 이미지 링크 수정 -->

> **아이리스(IRIS)**는 스마트폰에서 실행되는 AI 비서 앱입니다.  
> 음성 또는 텍스트로 대화하며, 실시간으로 정보를 분석하고 GPT-3.5 기반의 자연스러운 응답을 제공합니다.  
> 🎙️ Iron Man의 J.A.R.V.I.S. + SAO의 HUD를 결합한 미래형 인터페이스를 목표로 개발 중입니다.

---

## 🚀 주요 기능

- 🎤 **음성 인식 기반 대화 (Speech to Text)**
- 💬 **GPT-3.5 기반 자연어 응답**
- 🔊 **TTS (Text-to-Speech) 음성 출력**
- 📊 **실시간 파형 시각화 (Waveform Visualizer)**
- 🌐 **API 통신 및 JSON 응답 처리**
- 🧠 **사용자 입력에 따라 GPT와 대화 흐름 유지**
- 📱 **Android Compose 기반 UI**

---

## 🛠️ 기술 스택

| 분야 | 기술 |
|------|------|
| 언어 | Kotlin |
| UI | Jetpack Compose |
| AI | OpenAI GPT-3.5 (Chat Completion API) |
| 음성 | SpeechRecognizer, TextToSpeech (TTS) |
| HTTP | OkHttp, Retrofit (이전), → 현재 OkHttp 직접 사용 |
| UI 애니메이션 | Compose Canvas, LaunchedEffect, Coroutine |
| 기타 | Android Permissions, JSON 파싱 |

---

## 🎯 향후 추가 예정 기능

- [x] 심전도 스타일 파형 시각화  
- [ ] 📁 **HUD형 대화창 UI 구현** (바탕화면 위 투명한 창)
- [ ] 🖱️ **드래그로 확장되는 인터페이스**
- [ ] 🖼️ **텍스트 응답 외 이미지·영상 등 시각 정보 표시**
- [ ] 📚 **사용자 대화 기록 저장 및 재학습 기능**
- [ ] 🔧 **전문 GPT 모듈 연동 (주식, 코딩, 법률 등)**
- [ ] 💡 **키워드 감지 및 알림 기능 (예: "오늘 날씨")**
- [ ] 🔒 **유료 플랜 연동 (OpenAI API 비용 대응)**

---

## 📸 UI 시연 미리보기

(이미지 또는 GIF 추가 예정)

---

## 📦 프로젝트 구조 (간단 정리)

iris/
├── .gitignore                 # Git에서 무시할 파일 목록
├── build.gradle.kts          # 프로젝트 전체 빌드 설정
├── settings.gradle.kts       # 프로젝트 포함 모듈 설정
├── README.md                 # 깃허브 설명 문서
│
├── app/
│   ├── build.gradle.kts      # 앱 모듈 빌드 설정
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/
│   │   │   │   └── com/example/iris/
│   │   │   │       ├── MainActivity.kt            # 앱 메인 로직 + Compose UI
│   │   │   │       ├── ui/
│   │   │   │       │   └── WaveformVisualizer.kt  # 심전도형 파형 애니메이션 컴포넌트
│   │   │   │       └── api/
│   │   │   │           └── (예정: 전문 GPT 연결, 서버 통신 등)
│   │   │   └── res/
│   │   │       ├── layout/                        # 필요 시 XML 레이아웃
│   │   │       ├── values/
│   │   │       │   └── strings.xml                # 앱 문자열
│   │   │       └── drawable/                      # 아이콘, 이미지 등
│   │   └── test/                                  # 테스트 코드
│   │
│   └── proguard-rules.pro        # 난독화 제외 규칙
│
└── gradle/
    └── wrapper/                  # Gradle 실행기 관련



