<template>
  <div class="book-editor-page">
    <section v-if="creationStep === 'setup'" class="setup-section">
      <h2 class="section-title">새로운 책 만들기</h2>
      <p class="section-subtitle">당신의 이야기를 시작하기 위한 기본 정보를 입력해주세요.</p>

      <div class="setup-form">
        <div class="form-group">
          <label for="book-title">책 제목</label>
          <input id="book-title" type="text" v-model="currentBook.title" placeholder="매력적인 책 제목을 지어주세요."
            class="form-control">
        </div>
        <div class="form-group">
          <label for="book-summary">줄거리 / 책 소개</label>
          <textarea id="book-summary" v-model="currentBook.summary" placeholder="독자들의 흥미를 유발할만한 간단한 소개글을 작성해보세요."
            class="form-control" rows="4"></textarea>
        </div>
        <div class="form-group">
          <label>책 종류 선택</label>
          <div class="type-selection">
            <button v-for="bookType in bookTypes" :key="bookType.id" @click="currentBook.type = bookType.id"
              :class="{ active: currentBook.type === bookType.id }">
              <i :class="bookType.icon"></i>
              <span>{{ bookType.name }}</span>
            </button>
          </div>
        </div>
        <div class="form-group">
          <label>장르 선택</label>
          <div class="genre-toggle">
            <button v-for="category in categories" :key="category.id" @click="selectCategory(category.id)"
              :class="{ active: selectedCategoryId === category.id }">
              {{ category.name }}
            </button>
          </div>
        </div>
        <div class="form-actions">
          <button @click="moveToEditingStep" class="btn btn-primary">
            시작하기 <i class="bi bi-arrow-right"></i>
          </button>
        </div>
      </div>
    </section>

    <section v-else-if="creationStep === 'editing'" class="workspace-section">
      <div class="workspace-header">
        <span class="editor-title-label">책 제목 </span>
        <input type="text" v-model="currentBook.title" class="book-title-input title-input-highlight">
      </div>

      <div class="workspace-main">
        <div class="story-list-container">
          <div class="story-list-header">
            <h3 class="story-list-title">목차</h3>
            <button @click="addStory" class="btn-add-story" title="이야기 추가"><i class="bi bi-plus-lg"></i></button>
          </div>
          <ul class="story-list">
            <li v-for="(story, index) in currentBook.stories" :key="story.id ?? ('tmp-' + index)"
              @click="selectStory(index)" :class="{ active: index === currentStoryIndex }">
              <span>{{ story.title }}</span>
              <button @click.stop="deleteStory(story, index)" class="btn-delete-story">×</button>
            </li>
          </ul>
        </div>

        <div class="editor-area" v-if="currentStory">
          <div class="editor-main">
            <div class="editor-title-wrapper">
              <span class="editor-title-label">이야기 제목</span>
              <input type="text" v-model="currentStory.title" placeholder="이야기 제목"
                class="story-title-input title-input-highlight">
            </div>
            <div class="ai-question-area">
              <p v-if="isInterviewStarted"><i class="bi bi-robot"></i> {{ aiQuestion }}</p>
              <p v-else><i class="bi bi-robot"></i>AI 인터뷰 시작을 누르고 질문을 받아보세요.</p>
            </div>
            <div class="story-content-wrapper">
              <textarea v-model="currentStory.content" class="story-content-editor"
                placeholder="이곳에 이야기를 적거나 음성 녹음 시작을 누르고 말해 보세요." maxlength="5000"></textarea>
              <div class="char-counter">
                {{ currentStory.content.length }} / 5000
              </div>
            </div>

            <div v-if="isRecording" class="audio-visualizer-container">
              <canvas ref="visualizerCanvas"></canvas>
            </div>

            <div v-if="correctedContent" class="correction-panel">
              <h4>AI 교정 제안</h4>
              <p>{{ correctedContent }}</p>
              <div class="correction-actions">
                <button @click="applyCorrection" class="btn btn-primary">편집 내용으로 교체</button>
                <button @click="cancelCorrection" class="btn btn-primary">교정 취소</button>
              </div>
            </div>
          </div>
          <div class="editor-sidebar" :ref="el => { sidebarButtons = (el as any)?.children }">
            <button @click="startAiInterview" class="btn-sidebar"><i class="bi bi-mic"></i> <span>AI 인터뷰
                시작</span></button>

            <button v-if="!isRecording" @click="startRecording" class="btn-sidebar"><i class="bi bi-soundwave"></i>
              <span>음성 답변 시작</span></button>
            <button v-else @click="stopRecording" class="btn-sidebar btn-recording"><i
                class="bi bi-stop-circle-fill"></i> <span>음성 답변 완료</span></button>

            <button @click="submitAnswerAndGetFollowUp" :disabled="!isInterviewStarted || !isContentChanged"
              class="btn-sidebar"><i class="bi bi-check-circle"></i> <span>질문 답변완료</span></button>
            <button @click="skipQuestion" :disabled="!isInterviewStarted" class="btn-sidebar"><i
                class="bi bi-skip-end-circle"></i> <span>질문 건너뛰기</span></button>
            <button @click="autoCorrect" class="btn-sidebar"><i class="bi bi-magic"></i> <span>AI 자동 교정</span></button>
            <button @click="saveStory" class="btn-sidebar"><i class="bi bi-save"></i> <span>이야기 저장</span></button>
            <button @click="saveStory" class="btn-sidebar"><i class="bi bi-universal-access"></i> <span>배호빈
                버튼</span></button>
            <button @click="uploadimage" class="btn-sidebar"><i class="bi bi-image"></i> <span>이야기 사진 첨부</span></button>
            <button @click="saveDraft" class="btn-sidebar btn-outline-sidebar">
              <i class="bi bi-cloud-arrow-down"></i> <span>임시 저장 (나가기)</span>
            </button>
            <button @click="moveToPublishingStep" class="btn-sidebar btn-primary-sidebar">
              <i class="bi bi-send-check"></i> <span>발행하기</span>
            </button>
          </div>
        </div>
        <div v-else class="no-story-message">
          <i class="bi bi-journal-plus"></i>
          <p>왼쪽에서 이야기를 선택하거나<br>새 이야기를 추가해주세요.</p>
        </div>
      </div>
    </section>

    <section v-else-if="creationStep === 'publishing'" class="publish-section">
      <div class="publish-header">
        <h2 class="section-title">책 발행하기</h2>
      </div>
      <p class="section-subtitle">마지막으로 책의 정보를 확인하고, 멋진 표지를 선택해주세요.</p>

      <div class="publish-form">
        <div class="form-group">
          <label for="final-book-title">제목 최종 수정</label>
          <input id="final-book-title" type="text" v-model="currentBook.title"
            class="form-control title-input-highlight">
        </div>
        <div class="form-group">
          <label for="final-book-summary">줄거리 / 책 소개</label>
          <textarea id="final-book-summary" v-model="currentBook.summary" class="form-control" rows="5"></textarea>
        </div>
        <div class="form-group">
          <label>카테고리 선택</label>
          <div class="genre-toggle">
            <button v-for="category in categories" :key="category.id" @click="selectCategory(category.id)"
              :class="{ active: selectedCategoryId === category.id }">
              {{ category.name }}
            </button>
          </div>
        </div>
        <div class="form-group">
          <label for="book-tags">태그</label>
          <div class="tag-container">
            <div class="tag-list">
              <span v-for="(tag, index) in tags" :key="index" class="tag-item">
                {{ tag }}
                <button @click="removeTag(index)" class="btn-remove-tag">×</button>
              </span>
            </div>
            <input id="book-tags" type="text" v-model="tagInput" @keydown.enter.prevent="addTag"
              placeholder="태그 입력 후 Enter" class="form-control" :disabled="tags.length >= 5">
          </div>
        </div>
        <div class="form-group">
          <label>표지 이미지 선택</label>
          <div class="cover-selection">
            <div v-for="(cover, index) in coverOptions" :key="index" class="cover-option"
              :class="{ active: selectedCover === cover }" @click="selectedCover = cover">
              <img :src="cover" alt="Book Cover">
            </div>
          </div>
        </div>
        <div class="form-group">
          <label for="cover-upload">또는, 직접 표지 첨부</label>
          <input id="cover-upload" type="file" @change="handleCoverUpload" class="form-control">
        </div>
        <div class="form-actions">
          <button @click="creationStep = 'editing'" class="btn btn-primary btn-lg">
            <i class="bi bi-arrow-left"></i> 뒤로가기
          </button>
          <button @click="finalizePublicationAsCopy" class="btn btn-primary btn-lg">
            복사본으로 발행 <i class="bi bi-files"></i>
          </button>
          <button @click="finalizePublication" class="btn btn-primary btn-lg">
            책 발행하기 <i class="bi bi-check-circle"></i>
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch, nextTick, onBeforeUnmount, onUpdated } from 'vue';
import { useRouter, useRoute, onBeforeRouteLeave } from 'vue-router';
import apiClient from '@/api'; // API 클라이언트 임포트
import { useAuthStore } from '@/stores/auth';

// --- 인터페이스 정의 ---
interface Story { id?: number; title: string; content: string; activeSessionId?: string | null; }
interface Book { id: string; title: string; summary: string; type: string; authorId: string; isPublished: boolean; stories: Story[]; createdAt: Date; updatedAt: Date; tags?: string[]; completed?: boolean; }
interface ApiEpisode { episodeId: number; title: string; content: string; activeSessionId?: string | null; }

type QuestionType = 'MAIN' | 'FOLLOWUP' | 'CHAPTER_COMPLETE' | string;


interface QuestionEventData {
  text: string;
  questionType?: QuestionType;
  isLastQuestion?: boolean;
}

interface PartialTranscriptEventData {
  messageId: number;     // 서버 계약에 맞게 number/string 여부 확인하세요
  text: string;
}

interface EpisodeEventData {
  episodeId: number;
  title: string;
  content: string;
}

// --- 정적 데이터 ---
const bookTypes = [{ id: 'autobiography', name: '자서전', icon: 'bi bi-file-person' }, { id: 'diary', name: '일기장', icon: 'bi bi-journal-bookmark' }, { id: 'freeform', name: '자유', icon: 'bi bi-brush' },];
const categories = [
  { id: 1, name: '자서전' }, { id: 2, name: '일기' }, { id: 3, name: '소설/시' },
  { id: 4, name: '에세이' }, { id: 5, name: '자기계발' }, { id: 6, name: '역사' },
  { id: 7, name: '경제/경영' }, { id: 8, name: '사회/정치' }, { id: 9, name: '청소년' },
  { id: 10, name: '어린이/동화' }, { id: 11, name: '문화/예술' }, { id: 12, name: '종교' },
  { id: 13, name: '여행' }, { id: 14, name: '스포츠' }
];
const coverOptions = ['https://ssafytrip.s3.ap-northeast-2.amazonaws.com/book/default_1.jpg', 'https://ssafytrip.s3.ap-northeast-2.amazonaws.com/book/default_2.jpg', 'https://ssafytrip.s3.ap-northeast-2.amazonaws.com/book/default_3.jpg', 'https://ssafytrip.s3.ap-northeast-2.amazonaws.com/book/default_4.jpg', 'https://ssafytrip.s3.ap-northeast-2.amazonaws.com/book/default_5.jpg',];

// --- 라우터 및 라우트 ---
const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
let connectTimer: number | null = null;

// --- 컴포넌트 상태 ---
const creationStep = ref<'setup' | 'editing' | 'publishing'>('setup');
const currentBook = ref<Partial<Book & { categoryId: number | null }>>({ title: '', summary: '', type: 'autobiography', stories: [], tags: [], categoryId: null });
const selectedCategoryId = ref<number | null>(null);
const currentStoryIndex = ref(-1);
const aiQuestion = ref('AI 인터뷰 시작을 누르고 질문을 받아보세요.');
const isInterviewStarted = ref(false);
const isRecording = ref(false);
const isContentChanged = ref(false);
const correctedContent = ref<string | null>(null);
const tagInput = ref(''); // 현재 입력 중인 태그
const tags = ref<string[]>([]); // 등록된 태그 목록
const isSavedOrPublished = ref(false);
const episodeJustApplied = ref(false);
//상태 추가
const currentSessionId = ref<string | null>(null);
//메시지 아이디 저장
const currentAnswerMessageId = ref<number | null>(null);
// SSE EventSource 객체를 저장할 변수
let eventSource: EventSource | null = null;
// SSE 연결 상태 추적
const isConnecting = ref(false);
const isConnected = ref(false);

const selectedCover = ref(coverOptions[0]);
const uploadedCoverFile = ref<File | null>(null);
const sidebarButtons = ref<HTMLButtonElement[]>([]);

const isCorrecting = ref(false);

// --- 오디오 녹음 상태 ---
const visualizerCanvas = ref<HTMLCanvasElement | null>(null);
const audioContext: AudioContext | null = null;
const analyser: AnalyserNode | null = null;
let animationFrameId: number | null = null;
let mediaStream: MediaStream | null = null;

let audioChunks: Blob[] = [];
let mediaRecorder: MediaRecorder | null = null;

// --- 계산된 속성 ---
const currentStory = computed(() => {
  if (currentBook.value.stories && currentStoryIndex.value > -1 && currentBook.value.stories[currentStoryIndex.value]) {
    return currentBook.value.stories[currentStoryIndex.value];
  }
  return null;
});


// --- 함수 ---

// 단계 1: 설정
function selectCategory(categoryId: number) {
  selectedCategoryId.value = categoryId;
  currentBook.value.categoryId = categoryId;
}

async function moveToEditingStep() {
  if (!currentBook.value.title) {
    alert('책 제목을 입력해주세요.');
    return;
  }
  if (!selectedCategoryId.value) {
    alert('카테고리를 선택해주세요.');
    return;
  }

  const bookData = new FormData();
  bookData.append('title', currentBook.value.title);
  if (currentBook.value.summary) bookData.append('summary', currentBook.value.summary);

  let bookTypeValue = 'AUTO'; // 기본값
  if (currentBook.value.type === 'diary') {
    bookTypeValue = 'DIARY';
  } else if (currentBook.value.type === 'freeform') {
    bookTypeValue = 'FREE_FORM';
  }
  bookData.append('bookType', bookTypeValue);

  bookData.append('categoryId', String(selectedCategoryId.value));

  try {
    const response = await apiClient.post('/api/v1/books', bookData, {
      headers: {
        'Content-Type': undefined
      },
    });
    const newBook = response.data.data;
    currentBook.value.id = newBook.bookId;
    currentBook.value.title = newBook.title;
    currentBook.value.summary = newBook.summary;
    currentBook.value.stories = newBook.episodes || [];

    creationStep.value = 'editing';
    if (currentBook.value.stories?.length === 0) {
      addStory();
    }
  } catch (error) {
    console.error('책 생성 오류:', error);
    alert('책 생성에 실패했습니다.');
  }
}

// 단계 2: 편집
async function startRecording() {
  if (isRecording.value) return;

  try {
    mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true });
    isRecording.value = true;
    audioChunks = [];

    // --- 오디오 시각화 설정 ---
    if (!audioContext) {
      audioContext = new AudioContext();
    }
    // 브라우저 정책에 따라 정지된 오디오 컨텍스트를 재개합니다.
    if (audioContext.state === 'suspended') {
      await audioContext.resume();
    }
    
    analyser = audioContext.createAnalyser();
    const source = audioContext.createMediaStreamSource(mediaStream);
    source.connect(analyser);
    analyser.fftSize = 256;
    // --- 오디오 시각화 설정 끝 ---

    mediaRecorder = new MediaRecorder(mediaStream);

    mediaRecorder.ondataavailable = (event) => {
      if (event.data.size > 0) {
        audioChunks.push(event.data);
      }
    };

    mediaRecorder.onstop = async () => {
      const audioBlob = new Blob(audioChunks, { type: 'audio/webm;codecs=opus' });
      const formData = new FormData();
      formData.append('sessionId', currentSessionId.value!);
      formData.append('chunkIndex', String(0));
      formData.append('audio', audioBlob, 'audio.webm');
      try {
        console.log('음성 답변 서버로 전송 시작...');
        await apiClient.post('/api/v1/stt/chunk', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
        console.log('음성 답변 전송 성공.');
        isContentChanged.value = true;
      } catch (error) {
        console.error('음성 답변 전송 실패:', error);
        alert('음성 답변 처리에 실패했습니다.');
      }
    };

    mediaRecorder.start();
    
    // isRecording이 true로 설정된 후 DOM이 업데이트될 때까지 기다립니다.
    await nextTick(); 
    
    // 시각화를 시작합니다.
    visualize();

  } catch (err) {
    console.error('마이크 접근 오류:', err);
    alert('마이크에 접근할 수 없습니다. 권한을 확인해주세요.');
    isRecording.value = false;
  }
}

function stopRecording() {
  if (!isRecording.value || !mediaRecorder) return;

  mediaRecorder.stop();

  isRecording.value = false;
  mediaStream?.getTracks().forEach(track => track.stop());
  mediaStream = null;

  // 시각화 애니메이션 중지
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId);
    animationFrameId = null;
  }
}


function visualize() {
  if (!analyser || !visualizerCanvas.value) return;
  const canvas = visualizerCanvas.value;
  const canvasCtx = canvas.getContext('2d');
  const bufferLength = analyser.frequencyBinCount;
  const dataArray = new Uint8Array(bufferLength);

  const draw = () => {
    if (!analyser || !canvasCtx || !isRecording.value) return;
    animationFrameId = requestAnimationFrame(draw);
    analyser.getByteFrequencyData(dataArray);

    let sum = 0;
    for (const value of dataArray) {
      sum += value;
    }
    const avg = sum / bufferLength;

    canvasCtx.clearRect(0, 0, canvas.width, canvas.height);
    const barWidth = Math.min(canvas.width, (avg / 100) * canvas.width);
    canvasCtx.fillStyle = '#8B4513';
    canvasCtx.fillRect(0, 0, barWidth, canvas.height);
  };
  draw();
}

async function loadBookForEditing(bookId: string) {
  try {
    const response = await apiClient.get(`/api/v1/books/${bookId}`, {
      headers: {
        'Cache-Control': 'no-cache',
        'Pragma': 'no-cache',
        'Expires': '0',
      },
    });
    const bookData = response.data.data;
    currentBook.value = {
      id: bookData.bookId,
      title: bookData.title,
      summary: bookData.summary,
      stories: bookData.episodes?.map((e: ApiEpisode) => ({
        id: e.episodeId,
        title: e.title,
        content: e.content,
        activeSessionId: e.activeSessionId // ★★★ activeSessionId를 함께 매핑 ★★★
      })) || [],
      tags: bookData.tags || [],
      categoryId: bookData.categoryId,
      type: bookData.bookType.toLowerCase(),
      completed: bookData.completed,
    };
    tags.value = bookData.tags || []; // [수정] 불러온 태그를 상태에 할당
    selectedCategoryId.value = bookData.categoryId;
    creationStep.value = 'editing';

    if (currentBook.value.stories && currentBook.value.stories.length > 0) {
      // activeSessionId가 있는 스토리를 우선적으로 찾아서 선택
      const activeStoryIndex = currentBook.value.stories.findIndex(story =>
        story.activeSessionId && story.activeSessionId.trim() !== ''
      );

      if (activeStoryIndex !== -1) {
        // 진행 중인 세션이 있는 스토리를 선택하고 이어쓰기 모드로 진입
        console.log(`진행 중인 세션이 있는 스토리(인덱스: ${activeStoryIndex})를 선택합니다.`);
        await selectStory(activeStoryIndex);
      } else {
        // 진행 중인 세션이 없으면 첫 번째 스토리 선택
        await selectStory(0);
      }
    }
  } catch (error) {
    console.error('책 정보를 불러오는데 실패했습니다:', error);
    alert('책 정보를 불러오는데 실패했습니다. 이전 페이지로 돌아갑니다.');
    router.back();
  }
}

function loadOrCreateBook(bookId: string | null) {
  if (bookId) {
    loadBookForEditing(bookId);
  } else {
    creationStep.value = 'setup';
  }
}

async function deleteStory(story: Story, index: number) {
  if (!confirm(`'${story.title}' 이야기를 삭제하시겠습니까?`)) return;
  if (!currentBook.value?.id || !story.id) {
    alert('삭제할 이야기의 정보가 올바르지 않습니다.');
    return;
  }

  try {
    await apiClient.delete(`/api/v1/books/${currentBook.value.id}/episodes/${story.id}`);
    currentBook.value.stories?.splice(index, 1);

    if (currentStoryIndex.value === index) {
      currentStoryIndex.value = -1;
    } else if (currentStoryIndex.value > index) {
      currentStoryIndex.value--;
    }
    alert('이야기가 삭제되었습니다.');
  } catch (error) {
    console.error('이야기 삭제 오류:', error);
    alert('이야기 삭제에 실패했습니다.');
  }
}


async function addStory() {
  if (!currentBook.value?.id) return;

  try {
    const response = await apiClient.post(`/api/v1/books/${currentBook.value.id}/episodes`);
    const newEpisode = response.data.data;
    const newStory: Story = {
      id: newEpisode.episodeId,
      title: newEpisode.title || `${(currentBook.value.stories?.length || 0) + 1}번째 이야기`,
      content: newEpisode.content || ''
    };
    currentBook.value.stories = [...(currentBook.value.stories || []), newStory];
    currentStoryIndex.value = (currentBook.value.stories?.length || 1) - 1;
  } catch (error) {
    console.error('이야기 추가 오류:', error);
    alert('새로운 이야기를 추가하는데 실패했습니다.');
  }
}


async function selectStory(index: number) {
  console.log(`🎯 selectStory 호출: index=${index}`);
  console.log('📚 현재 stories:', currentBook.value.stories?.map(s => ({ id: s.id, title: s.title })));

  if (eventSource && currentSessionId.value) {
    console.log(`다른 스토리 선택으로 SSE 연결(${currentSessionId.value})을 종료합니다.`);
    try {
      // 이 경우는 페이지 이동이 아니므로 apiClient 사용 가능
      await apiClient.delete(`/api/v1/conversation/stream/${currentSessionId.value}`);
    } catch (e) {
      console.error('SSE 연결 종료 API 호출 실패', e);
    }
    eventSource.close();
    isConnected.value = false;
    isConnecting.value = false;
  }

  currentStoryIndex.value = index;
  isContentChanged.value = false;

  // 선택된 스토리를 가져옴
  const story = currentBook.value.stories?.[index];

  console.log('✅ 선택된 스토리:', {
    id: story?.id,
    title: story?.title,
    hasActiveSession: !!story?.activeSessionId,
    contentLength: story?.content?.length || 0
  });

  // 기존 연결이 있다면 먼저 정리
  if (eventSource) {
    eventSource.close();
    isConnected.value = false;
    isConnecting.value = false;
    // 연결 정리를 위한 짧은 대기
    await new Promise(resolve => setTimeout(resolve, 100));
  }

  // ★★★ 여기가 대화 이어하기의 핵심 로직 ★★★
  if (story && story.activeSessionId) {
    // [재연결 시나리오] 선택한 스토리에 진행 중인 세션이 있다면,
    console.log(`기존 세션(${story.activeSessionId})에 재연결합니다.`);
    currentSessionId.value = story.activeSessionId; // '열쇠'를 현재 세션 ID로 설정
    isInterviewStarted.value = true; // 인터뷰 모드로 즉시 전환

    // 상태 설정 후 짧은 지연을 두고 연결
    await new Promise(resolve => setTimeout(resolve, 200));
    await connectToSseStream(); // 해당 세션 ID로 SSE 스트림에 재연결
  } else {
    // [새 시작 시나리오] 진행 중인 세션이 없다면, 모든 관련 상태를 초기화
    currentSessionId.value = null;
    isInterviewStarted.value = false;
    aiQuestion.value = 'AI 인터뷰 시작을 누르고 질문을 받아보세요.';
    currentAnswerMessageId.value = null;
    console.log('🆕 새 시작 모드로 상태 초기화 완료');
  }

  // selectStory 완료 후 최종 상태 확인
  await nextTick();
  console.log('🎯 selectStory 완료 후 최종 상태:', {
    currentStoryIndex: currentStoryIndex.value,
    currentStoryExists: !!currentStory.value,
    currentStoryId: currentStory.value?.id,
    isInterviewStarted: isInterviewStarted.value,
    currentSessionId: currentSessionId.value
  });
}

async function saveStory() {

  if (isInterviewStarted.value === true) {
    // [시나리오 1] 인터뷰 진행 중 -> "메시지 수정"
    // 사용자가 STT 결과를 수정한 내용을 저장하는 경우

    if (!currentAnswerMessageId.value) {
      alert('수정할 답변 정보가 없습니다. 답변이 완료된 후 다시 시도해주세요.');
      return;
    }

    console.log(`메시지 수정 요청: ID=${currentAnswerMessageId.value}`);

    try {
      const updateRequest = {
        messageId: currentAnswerMessageId.value,
        content: currentStory.value?.content.trim() || ''
      };
      await apiClient.put('/api/v1/conversation/message', updateRequest);
      alert('수정된 답변이 저장되었습니다.');

    } catch (error) {
      console.error('메시지 수정 실패:', error);
      alert('답변 저장에 실패했습니다.');
    }

  } else {
    // [시나리오 2] 인터뷰 종료 후 -> "에피소드 수정"
    // 사용자가 목차에서 이전 에피소드를 불러와 제목이나 내용을 수정하는 경우

    if (!currentStory.value?.id || !currentBook.value?.id) {
      alert('저장할 에피소드 정보가 올바르지 않습니다.');
      return;
    }

    console.log(`에피소드 수정 요청: ID=${currentStory.value.id}`);

    try {
      const episodeUpdateRequest = {
        title: currentStory.value.title,
        content: currentStory.value.content
      };
      await apiClient.patch(
        `/api/v1/books/${currentBook.value.id}/episodes/${currentStory.value.id}`,
        episodeUpdateRequest
      );
      alert('에피소드가 성공적으로 저장되었습니다.');
      isContentChanged.value = false;

    } catch (error) {
      console.error('에피소드 저장(수정) 실패:', error);
      alert('에피소드 저장에 실패했습니다.');
    }
  }
}


// 수정함
async function startAiInterview() {
  if (!currentBook.value?.id) {
    alert('책 정보가 올바르지 않습니다.');
    return;
  }
  if (!currentStory.value?.id) {
    alert('먼저 이야기를 추가/선택해주세요.');
    return;
  }

  // 이미 연결 중이거나 연결되어 있다면 중복 시작 방지
  if (isConnecting.value || isConnected.value || isInterviewStarted.value) {
    console.log('이미 AI 인터뷰가 진행 중이거나 연결 중입니다.');
    return;
  }

  try {
    const res = await apiClient.post(
      `/api/v1/conversation/${currentBook.value.id}/episodes/${currentStory.value.id}/sessions`
    );
    currentSessionId.value = res.data.data.sessionId;

    // 현재 스토리에 activeSessionId 저장 (이어쓰기를 위함)
    if (currentStory.value) {
      currentStory.value.activeSessionId = currentSessionId.value;
    }

    isInterviewStarted.value = true;
    isContentChanged.value = false;

    // (선택) 백엔드가 첫 질문을 즉시 생성/반환하지 않는다면 안내 문구 유지
    aiQuestion.value = 'AI 인터뷰 세션에 연결 중... 첫 질문을 기다립니다.';

    // 발급받은 sessionId로 SSE 스트림에 "연결"
    await connectToSseStream();
  } catch (e) {
    console.error('세션 시작 실패:', e);
    alert('AI 인터뷰 세션 시작에 실패했습니다.');
    // 실패 시 상태 초기화
    isInterviewStarted.value = false;
    currentSessionId.value = null;
    if (currentStory.value) {
      currentStory.value.activeSessionId = null;
    }
  }
}

async function safeCloseEventSource() {
  if (connectTimer) { clearTimeout(connectTimer); connectTimer = null; }
  if (!eventSource) return;
  try { eventSource.close(); } catch { }
  eventSource = null;
  isConnected.value = false;
  isConnecting.value = false;
  // 끊고 300ms는 재연결 금지
  await new Promise(res => setTimeout(res, 300));
}

// 페이지 이탈 전 완전한 정리 함수
async function cleanupBeforeLeave() {
  console.log('페이지 이탈 전 상태 정리 시작...');

  // SSE 연결 완전 정리
  await safeCloseEventSource();

  // 모든 상태 초기화
  currentSessionId.value = null;
  currentAnswerMessageId.value = null;
  isInterviewStarted.value = false;
  isContentChanged.value = false;
  firstChunkForThisAnswer = true;
  aiQuestion.value = 'AI 인터뷰 시작을 누르고 질문을 받아보세요.';

  console.log('페이지 이탈 전 상태 정리 완료');
}

let firstChunkForThisAnswer = true;

// ★ 추가: SSE 연결 및 이벤트 리스너 설정 함수
async function connectToSseStream() {
  if (!currentSessionId.value) {
    console.warn('세션 ID가 없어 SSE 연결을 할 수 없습니다.');
    return;
  }

  // 이미 연결 중이거나 연결되어 있다면 중복 연결 방지
  if (isConnecting.value || isConnected.value) {
    console.log('이미 SSE 연결 중이거나 연결되어 있습니다.');
    return;
  }

  isConnecting.value = true;

  await safeCloseEventSource();


  // 기존 연결이 있다면 종료하고 잠시 대기
  if (eventSource) {
    eventSource.close();
    isConnected.value = false;
    await new Promise(resolve => setTimeout(resolve, 300));
  }

  try {
    const baseURL = apiClient.defaults?.baseURL || '';
    const token = authStore.accessToken;
    if (!token) {
      alert('인증 토큰이 없어 인터뷰를 시작할 수 없습니다. 다시 로그인해주세요.');
      return;
    }
    const url = `${baseURL}/api/v1/conversation/${currentBook.value.id}/${currentSessionId.value}/stream?token=${token}`;
    eventSource = new EventSource(url, { withCredentials: true });

    eventSource.onopen = () => {
      console.log('SSE 연결 성공');
      isConnecting.value = false;
      isConnected.value = true;
    };

    eventSource.addEventListener('question', (ev: MessageEvent<string>) => {
      const q = safeJson<QuestionEventData>(ev.data);
      if (!q) return; // 파싱 실패 시 무시

      aiQuestion.value = q.text ?? '';

      // 인터뷰 종료 신호
      if (q.questionType === 'CHAPTER_COMPLETE' || q.isLastQuestion) {
        isInterviewStarted.value = false;
        isContentChanged.value = false;
        if (currentStory.value) currentStory.value.activeSessionId = null;
        return;
      }

      // 직후 episode 반영이면 초기화 금지
      if (episodeJustApplied.value) {
        episodeJustApplied.value = false;
        return;
      }

      // 다음 답변으로 넘어가는 일반 케이스
      if (q.questionType === 'MAIN' || q.questionType === 'FOLLOWUP' || !q.questionType) {
        // 기존 내용 즉시 삭제 대신, 다음 partialTranscript의 첫 청크에서 초기화
        firstChunkForThisAnswer = true;
        isContentChanged.value = false;
      }
    });


    function safeJson<T>(data: string): T | null {
      try {
        return JSON.parse(data) as T;
      } catch {
        return null;
      }
    }
    // 2.'partialTranscript' 이벤트 리스너
    eventSource.addEventListener('partialTranscript', async (ev: MessageEvent<string>) => {
      console.log('🎤 SSE partialTranscript 이벤트 수신:', ev.data);
      const t = safeJson<PartialTranscriptEventData>(ev.data);
      if (!t) {
        console.error('❌ partialTranscript JSON 파싱 실패:', ev.data);
        return;
      }

      console.log('✅ 파싱된 transcript 데이터:', t);
      console.log('📝 현재 currentStory 상태:', {
        exists: !!currentStory.value,
        id: currentStory.value?.id,
        title: currentStory.value?.title,
        contentLength: currentStory.value?.content?.length || 0
      });
      console.log('📌 현재 currentStoryIndex:', currentStoryIndex.value);
      console.log('🔄 firstChunkForThisAnswer 상태:', firstChunkForThisAnswer);
      console.log('📋 전체 stories 개수:', currentBook.value?.stories?.length || 0);

      // 스토리 선택이 올바른지 검증
      if (currentStoryIndex.value >= 0 && currentBook.value?.stories) {
        const selectedStory = currentBook.value.stories[currentStoryIndex.value];
        console.log('🎯 선택된 스토리:', {
          id: selectedStory?.id,
          title: selectedStory?.title,
          isSameAsCurrentStory: selectedStory === currentStory.value
        });
      }

      if (currentStory.value) {
        console.log('✅ currentStory가 존재함, content 업데이트 시도');

        // 다음 답변의 첫 청크에서만 초기화
        if (firstChunkForThisAnswer) {
          console.log('🆕 첫 청크로 content 초기화');
          currentStory.value.content = '';
          firstChunkForThisAnswer = false;
        }

        const beforeContent = currentStory.value.content;
        const addText = (t.text || '') + ' ';
        currentStory.value.content += addText;
        const afterContent = currentStory.value.content;

        console.log('📝 content 업데이트 완료:', {
          before: `"${beforeContent}"`,
          added: `"${addText}"`,
          after: `"${afterContent}"`
        });

        // Vue 반응성 강제 업데이트
        await nextTick();
        console.log('🔄 nextTick 완료, 최종 content:', currentStory.value.content);

        // 반응성 트리거를 위해 스토리 배열을 강제 업데이트
        if (currentBook.value?.stories && currentStoryIndex.value >= 0) {
          const currentStoryRef = currentBook.value.stories[currentStoryIndex.value];
          if (currentStoryRef) {
            // 배열 요소를 새 객체로 교체하여 반응성 보장
            currentBook.value.stories.splice(currentStoryIndex.value, 1, { ...currentStoryRef });
            console.log('🔄 스토리 배열 반응성 강제 업데이트 완료');
          }
        }

      } else {
        console.error('❌ currentStory.value가 null 또는 undefined입니다!');
        console.log('📚 전체 book stories:', currentBook.value?.stories?.map(s => ({
          id: s.id,
          title: s.title,
          contentLength: s.content?.length || 0
        })));

        // 안전장치: currentStoryIndex가 유효하지 않은 경우 첫 번째 스토리 선택
        if (currentBook.value?.stories && currentBook.value.stories.length > 0) {
          if (currentStoryIndex.value < 0 || currentStoryIndex.value >= currentBook.value.stories.length) {
            console.log('🔧 currentStoryIndex가 유효하지 않음, 첫 번째 스토리로 설정');
            currentStoryIndex.value = 0;
            await nextTick(); // 상태 업데이트 대기
          }

          // 직접 스토리에 접근해서 업데이트 시도
          if (currentBook.value.stories[currentStoryIndex.value]) {
            console.log('🔧 직접 스토리 접근으로 content 업데이트 시도');
            const targetStory = currentBook.value.stories[currentStoryIndex.value];

            if (firstChunkForThisAnswer) {
              targetStory.content = '';
              firstChunkForThisAnswer = false;
            }

            const addText = (t.text || '') + ' ';
            targetStory.content += addText;
            console.log('🔧 직접 업데이트 완료:', targetStory.content);
          }
        }
      }

      // 최신 messageId 갱신
      if (typeof t.messageId !== 'undefined' && t.messageId !== null) {
        currentAnswerMessageId.value = t.messageId as number;
        console.log('🆔 messageId 업데이트:', t.messageId);
      }

      // 답변 내용이 존재함을 표시
      isContentChanged.value = true;
      console.log('✅ isContentChanged를 true로 설정');
    });


    eventSource.addEventListener('episode', async (ev: MessageEvent<string>) => {
      episodeJustApplied.value = true;

      const e = safeJson<EpisodeEventData>(ev.data);
      if (!e || !currentBook.value?.stories) return;

      const i = currentBook.value.stories.findIndex(s => s.id === e.episodeId);

      if (i > -1) {
        // 반응성 보장: 새 객체로 교체
        const updated = { ...currentBook.value.stories[i], title: e.title, content: e.content };
        currentBook.value.stories.splice(i, 1, updated);
        await nextTick();
        if (currentStoryIndex.value === -1) currentStoryIndex.value = i;
      } else {
        const newStory = { id: e.episodeId, title: e.title, content: e.content };
        currentBook.value.stories.push(newStory);
        currentStoryIndex.value = currentBook.value.stories.length - 1;
      }
    });

    eventSource.onerror = (error) => {
      console.error('SSE 에러:', error);
      isConnecting.value = false;
      isConnected.value = false;
      aiQuestion.value = '인터뷰 서버와 연결이 끊겼습니다. 페이지를 새로고침 해주세요.';
      eventSource?.close();
    };

  } catch (error) {
    console.error('SSE 연결 실패:', error);
    isConnecting.value = false;
    isConnected.value = false;
    aiQuestion.value = 'AI 인터뷰 서버 연결에 실패했습니다. 잠시 후 다시 시도해주세요.';
  }
}


// 질문 답변 완료 버튼 클릭 시
async function submitAnswerAndGetFollowUp() {
  if (!isInterviewStarted.value || !currentSessionId.value) return;

  try {
    console.log('다음 질문 요청...');
    // "다음 질문"을 요청하는 API 호출
    await apiClient.post(`/api/v1/conversation/${currentBook.value.id}/episodes/${currentStory.value?.id}/next?sessionId=${currentSessionId.value}`);

    // 즉시 화면의 답변 내용을 지우고 상태 초기화
    if (currentStory.value) {
      currentStory.value.content = '';
    }
    isContentChanged.value = false;
    firstChunkForThisAnswer = true;

    // 다음 질문은 SSE의 'question' 이벤트 리스너가 받아서 자동으로 화면에 표시합니다.
  } catch (error) {
    console.error('다음 질문 요청 실패:', error);
    alert('다음 질문을 가져오는데 실패했습니다.');
  }
}

function skipQuestion() { aiQuestion.value = '질문을 건너뛰었습니다. 새로운 질문: 학창시절, 가장 좋아했던 과목과 그 이유는 무엇인가요?'; alert('질문을 건너뛰었습니다.'); isContentChanged.value = false; }
async function autoCorrect() {
  if (!currentStory.value || !currentStory.value.content?.trim()) {
    alert('교정할 내용이 없습니다.');
    return;
  }

  console.log(selectedCategoryId.value);
  // ★★★ 카테고리 선택 유효성 검사 추가 ★★★
  if (!selectedCategoryId.value) {
    alert('AI 교정을 위해서는 먼저 카테고리를 선택해야 합니다.');
    return;
  }

  isCorrecting.value = true;
  correctedContent.value = null;

  try {
    const requestBody = {
      textToCorrect: currentStory.value.content,
      bookCategory: selectedCategoryId.value// ★★★ bookType -> categoryId 로 변경 ★★★
    };

    const response = await apiClient.post('/api/v1/ai/proofread', requestBody);
    correctedContent.value = response.data.data.correctedText;

  } catch (error) {
    console.error('AI 자동 교정 실패:', error);
    alert('AI 자동 교정에 실패했습니다. 잠시 후 다시 시도해주세요.');
  } finally {
    isCorrecting.value = false;
  }
}

function applyCorrection() { if (currentStory.value && correctedContent.value) { currentStory.value.content = correctedContent.value; correctedContent.value = null; } }
function cancelCorrection() { correctedContent.value = null; }

async function saveDraft() {
  if (!currentBook.value?.id) {
    alert('책 정보가 올바르지 않습니다.');
    return;
  }
  if (confirm('작업을 임시 저장하고 목록으로 돌아가시겠습니까?')) {
    try {
      const savePromises = currentBook.value.stories?.map(story => {
        if (story.id) {
          return apiClient.patch(`/api/v1/books/${currentBook.value.id}/episodes/${story.id}`, {
            title: story.title,
            content: story.content,
          });
        }
        return Promise.resolve();
      }) || [];
      await Promise.all(savePromises);

      const bookData = new FormData();
      bookData.append('title', currentBook.value.title || '');
      bookData.append('summary', currentBook.value.summary || '');
      if (selectedCategoryId.value) {
        bookData.append('categoryId', String(selectedCategoryId.value));
      }

      await apiClient.patch(`/api/v1/books/${currentBook.value.id}`, bookData, {
        headers: { 'Content-Type': undefined },
      });

      alert('임시 저장되었습니다.');
      isSavedOrPublished.value = true;

      // 나가기 전에 모든 연결과 상태를 완전히 정리
      await cleanupBeforeLeave();

      router.push('/continue-writing');
    } catch (error) {
      console.error('임시 저장 오류:', error);
      alert('임시 저장에 실패했습니다.');
    }
  }
}

function moveToPublishingStep() { creationStep.value = 'publishing'; }

// handleCoverUpload 함수 수정
function handleCoverUpload(event: Event) {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files[0]) {
    const file = target.files[0];
    uploadedCoverFile.value = file; // ★★★ 파일 객체를 ref에 저장

    const reader = new FileReader();
    reader.onload = (e) => {
      // 미리보기 이미지를 업데이트
      selectedCover.value = e.target?.result as string;
    };
    reader.readAsDataURL(file);
    alert('표지가 첨부되었습니다.');
  }
}

// --- 태그 관리 함수 ---
function addTag() {
  const newTag = tagInput.value.trim();
  if (newTag && !tags.value.includes(newTag) && tags.value.length < 5) {
    // 공백 포함 여부 확인
    if (/\s/.test(newTag)) {
      alert('태그에는 공백을 포함할 수 없습니다.');
      return;
    }
    tags.value.push(newTag);
    tagInput.value = ''; // 입력 필드 초기화
  } else if (tags.value.length >= 5) {
    alert('태그는 최대 5개까지 등록할 수 있습니다.');
  }
}

function removeTag(index: number) {
  tags.value.splice(index, 1);
}

async function finalizePublication() {
  if (!currentBook.value.id || !currentBook.value.title) {
    alert('책 정보가 올바르지 않습니다.');
    return;
  }
  if (!confirm('이 정보로 책을 최종 발행하시겠습니까?')) return;

  try {
    // 1. (선택사항) 에피소드 내용을 최종 저장합니다.
    //    '임시 저장' 등에서 이미 저장이 되었다면 생략 가능하지만, 안전을 위해 수행하는 것이 좋습니다.
    const savePromises = currentBook.value.stories?.map(story => {
      if (story.id) {
        return apiClient.patch(`/api/v1/books/${currentBook.value.id}/episodes/${story.id}`, {
          title: story.title,
          content: story.content,
        });
      }
      return Promise.resolve();
    }) || [];
    await Promise.all(savePromises);

    // 2. 책 정보 수정을 위한 FormData 준비
    const bookUpdateData = new FormData();
    bookUpdateData.append('title', currentBook.value.title);
    bookUpdateData.append('summary', currentBook.value.summary || '');
    if (selectedCategoryId.value) {
      bookUpdateData.append('categoryId', String(selectedCategoryId.value));
    }
    // 모든 태그를 FormData에 추가
    tags.value.forEach(tag => bookUpdateData.append('tags', tag));

    // 3. 표지 이미지 정보 추가
    if (uploadedCoverFile.value) {
      // 사용자가 직접 파일을 업로드한 경우
      bookUpdateData.append('file', uploadedCoverFile.value);
    } else {
      // 기본 이미지를 선택한 경우, 해당 URL을 전송
      bookUpdateData.append('coverImageUrl', selectedCover.value);
    }

    // 4. 책 정보(제목, 줄거리, 카테고리, 태그, 표지) 일괄 업데이트
    await apiClient.patch(`/api/v1/books/${currentBook.value.id}`, bookUpdateData, {
      headers: { 'Content-Type': undefined },
    });

    // 5. 책을 '완성' 상태로 변경
    // 이 API는 이제 상태 변경 역할만 하거나, 태그가 없는 경우를 위해 호출할 수 있습니다.
    // 백엔드 수정이 잘 되었다면 태그는 위에서 이미 업데이트됩니다.
    await apiClient.patch(`/api/v1/books/${currentBook.value.id}/complete`, { tags: tags.value });

    alert('책이 성공적으로 발행되었습니다!');
    isSavedOrPublished.value = true;

    // 발행 완료 후 상태 정리
    await cleanupBeforeLeave();

    router.push(`/book-detail/${currentBook.value.id}`);

  } catch (error) {
    console.error('책 발행 오류:', error);
    alert('책 발행에 실패했습니다.');
  }
}

async function finalizePublicationAsCopy() {
  if (!currentBook.value.id || !currentBook.value.title) {
    alert('책 정보가 올바르지 않습니다.');
    return;
  }

  if (!confirm('복사본으로 저장하시겠습니까? 현재 내용은 별개의 책으로 발행됩니다.')) return;

  const episodesToCopy = currentBook.value.stories?.map(story => ({
    episodeId: story.id,
    title: story.title,
    content: story.content,
    delete: false
  })) || [];

  if (episodesToCopy.length === 0) {
    alert('복사할 이야기가 하나 이상 있어야 합니다.');
    return;
  }

  const copyRequest = {
    title: `${currentBook.value.title} - 복사본`,
    summary: currentBook.value.summary,
    categoryId: selectedCategoryId.value,
    episodes: episodesToCopy,
    tags: tags.value, // 태그는 copy 요청에 포함
  };

  try {
    // 1. 책 복사 API 호출
    const response = await apiClient.post(`/api/v1/books/${currentBook.value.id}/copy`, copyRequest);
    const newBook = response.data.data;

    // 2. 복사된 책의 카테고리 업데이트
    if (selectedCategoryId.value) {
      const bookData = new FormData();
      bookData.append('title', `${currentBook.value.title} - 복사본`);
      bookData.append('summary', currentBook.value.summary || '');
      bookData.append('categoryId', String(selectedCategoryId.value));

      await apiClient.patch(`/api/v1/books/${newBook.bookId}`, bookData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
    }

    // 3. 복사된 책을 complete 상태로 만들기
    await apiClient.patch(`/api/v1/books/${newBook.bookId}/complete`, { tags: tags.value });

    alert('책이 복사본으로 성공적으로 발행되었습니다!');
    isSavedOrPublished.value = true;

    // 복사본 발행 완료 후 상태 정리
    await cleanupBeforeLeave();

    router.push(`/book-detail/${newBook.bookId}`);
  } catch (error) {
    console.error('복사본 발행 오류:', error);
    alert('복사본 발행에 실패했습니다.');
  }
}

function uploadimage() {
  alert('이미지 업로드 기능은 아직 구현되지 않았습니다.');
}

const adjustButtonFontSize = () => {
  nextTick(() => {
    if (sidebarButtons.value) {
      Array.from(sidebarButtons.value).forEach(button => {
        if (button.scrollHeight > button.clientHeight) {
          button.classList.add('font-small');
        } else {
          button.classList.remove('font-small');
        }
      });
    }
  });
};

// --- 생명주기 훅 ---

// 페이지 이탈 방지 (브라우저 새로고침/닫기)
const handleBeforeUnload = (event: BeforeUnloadEvent) => {
  if (creationStep.value !== 'setup' && !isSavedOrPublished.value) {
    event.preventDefault();
    event.returnValue = ''; // 대부분의 브라우저에서 사용자 정의 메시지를 무시하고 기본 메시지를 표시
  }
};

// 페이지 이탈 방지 (Vue Router를 통한 내부 이동)
onBeforeRouteLeave((to, from, next) => {
  if (creationStep.value !== 'setup' && !isSavedOrPublished.value) {
    const answer = window.confirm(
      '저장하지 않은 변경사항이 있습니다. 정말로 페이지를 떠나시겠습니까? 현재 작업 내용은 모두 삭제됩니다.'
    );
    if (answer) {
      next(); // 사용자가 이탈을 확인하면 onBeforeUnmount가 호출됨
    } else {
      next(false); // 이동 차단
    }
  } else {
    next(); // 저장되었거나 변경사항이 없으면 이동
  }
});

onMounted(() => {
  const bookId = route.params.bookId as string | undefined;
  if (route.query.start_editing === 'true' && bookId) {
    loadBookForEditing(bookId);
  } else {
    loadOrCreateBook(bookId || null);
  }
  window.addEventListener('beforeunload', handleBeforeUnload);
  adjustButtonFontSize();
});

onUpdated(() => {
  adjustButtonFontSize();
});

onBeforeUnmount(() => {
  // 타이머 정리
  if (connectTimer) {
    clearTimeout(connectTimer);
    connectTimer = null;
  }
  if (currentSessionId.value) {
    const baseURL = apiClient.defaults?.baseURL || '';
    const url = `${baseURL}/api/v1/conversation/stream/${currentSessionId.value}`;
    const headers = { 'Authorization': `Bearer ${authStore.accessToken}` };

    // 페이지를 닫아도 요청이 취소되지 않도록 fetch + keepalive 사용
    // navigator.sendBeacon(url, new Blob([JSON.stringify({})], { type: 'application/json' })) 도 좋은 대안입니다.
    try {
      fetch(url, {
        method: 'DELETE',
        headers,
        keepalive: true,
      });
      console.log(`SSE 연결 종료 요청 전송: ${currentSessionId.value}`);
    } catch (e) {
      console.error('SSE 연결 종료 요청 전송 실패', e);
    }
  }

  // SSE 연결 정리
  if (eventSource) {
    eventSource.close();
    isConnected.value = false;
    isConnecting.value = false;
    console.log('SSE 연결 종료');
  }



  window.removeEventListener('beforeunload', handleBeforeUnload);

  if (creationStep.value !== 'setup' && !isSavedOrPublished.value && currentBook.value.id) {
    const bookId = currentBook.value.id;
    const headers = {
      'Authorization': `Bearer ${authStore.accessToken}`,
    };

    // try {
    //   // 1. 모든 에피소드에 대한 삭제 요청을 보냅니다.
    //   currentBook.value.stories?.forEach(story => {
    //     if (story.id) {
    //       const baseURL = apiClient.defaults?.baseURL || '';
    //       const episodeUrl = `${baseURL}/api/v1/books/${bookId}/episodes/${story.id}`;
    //       fetch(episodeUrl, {
    //         method: 'DELETE',
    //         headers,
    //         keepalive: true,
    //       });
    //       console.log(`에피소드(ID: ${story.id}) 삭제 요청을 전송했습니다.`);
    //     }
    //   });

    //   // 2. 책 삭제 요청을 보냅니다.
    //   const baseURL = apiClient.defaults?.baseURL || '';
    //   const bookUrl = `${baseURL}/api/v1/books/${bookId}`;
    //   fetch(bookUrl, {
    //     method: 'DELETE',
    //     headers,
    //     keepalive: true,
    //   });
    //   console.log(`책(ID: ${bookId}) 삭제 요청을 전송했습니다.`);

    // } catch (e) {
    //   console.error("페이지 이탈 중 삭제 요청 전송 실패:", e);
    // }
  }
});

watch(() => currentStory.value?.content, (newContent) => {
  if (isInterviewStarted.value) {
    isContentChanged.value = newContent !== undefined && newContent.trim().length > 0;
    console.log('Content changed, isContentChanged set to:', isContentChanged.value);
  }
});

// route 변경 감지하여 컴포넌트 재사용 시에도 올바르게 초기화
watch(() => route.params.bookId, async (newBookId, oldBookId) => {
  if (newBookId && newBookId !== oldBookId) {
    console.log(`Route 변경 감지: ${oldBookId} -> ${newBookId}`);

    // 기존 연결 정리
    await cleanupBeforeLeave();

    // 새로운 책 로드
    if (route.query.start_editing === 'true') {
      await loadBookForEditing(newBookId as string);
    } else {
      loadOrCreateBook(newBookId as string || null);
    }
  }
}, { immediate: false });
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+KR:wght@400;600;700&family=Pretendard:wght@400;500;700&display=swap');
@import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css");

:root {
  --background-color: #F5F5DC;
  --surface-color: #FFFFFF;
  --primary-text-color: #3D2C20;
  --secondary-text-color: #6c757d;
  --accent-color: #8B4513;
  --border-color: #EAE0D5;
  --paper-color: #FDFDF5;
}

.book-editor-page {
  padding: 0.8rem 3.2rem 4rem 3.2rem;
  background-color: var(--background-color);
  color: var(--primary-text-color);
  min-height: calc(100vh - 56px);
  font-family: 'SCDream4', sans-serif;
}

.section-title {
  font-family: 'EBSHunminjeongeumSaeronL', sans-serif;
  font-size: 2.4rem;
  font-weight: 700;
  text-align: center;
  margin-bottom: 0.4rem;
}

.section-subtitle {
  text-align: center;
  font-size: 0.9rem;
  color: #5b673b;
  margin-bottom: 2.4rem;
}

/* --- General Button Styles --- */
.btn {
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
  font-size: 0.8rem;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  padding: 0.5rem 1rem;
}

@keyframes fill-animation {
  0% {
    transform-origin: top;
    transform: scaleY(0);
  }

  50% {
    transform-origin: top;
    transform: scaleY(1);
  }

  50.1% {
    transform-origin: bottom;
    transform: scaleY(1);
  }

  100% {
    transform-origin: bottom;
    transform: scaleY(0);
  }
}

.btn.btn-primary {
  position: relative;
  overflow: hidden;
  z-index: 1;
  display: inline-block;
  border: 2px solid #5b673b !important;
  border-radius: 16px !important;
  margin-left: 0.8rem !important;
  margin-right: 0.8rem !important;
  padding: 0.4rem 1rem !important;
  font-size: 0.8rem !important;
  white-space: nowrap;
  font-family: 'SCDream5', sans-serif;
  transition: color 0.5s ease;
  background-color: transparent;
  color: #000000;
}

.btn.btn-primary::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(185, 174, 122, 0.4);
  transform: scaleY(0);
  z-index: -1;
  animation: fill-animation 3s infinite ease-in-out;
}

.btn-primary:hover {
  color: white !important;
  border-color: #dee2e6 !important;
  background-color: transparent;
}

.btn-outline {
  background-color: transparent;
  border: 1px solid var(--accent-color);
  color: var(--accent-color);
}

.btn-outline:hover {
  background-color: #fff8f0;
}

.btn-lg {
  padding: 0.6rem 1.4rem;
  font-size: 0.9rem;
}

/* --- Title Input Styling --- */
/* 세부사항 입력 책제목 */
.title-input-highlight {
  background-color: transparent;
  border: none;
  border-bottom: 2px solid #c1af9b;
  border-radius: 0;
  padding: 0.4rem 0.15rem;
  font-family: 'ChosunCentennial', serif;
  font-size: 1.4rem;
  font-weight: 600;
  color: var(--primary-text-color);
  transition: border-color 0.3s ease;
  box-shadow: none;
}

.title-input-highlight:focus {
  outline: none;
  border-color: var(--accent-color);
}

.story-title-input.title-input-highlight {
  font-size: 1rem;
}

/* --- Setup / Publish Section --- */
.setup-section,
.publish-section {
  max-width: 640px;
  margin: 0 auto;
  background: var(--surface-color);
  padding: 2rem 2.4rem;
  border-radius: 20px;
  border: 2px solid #657143;
  box-shadow: 0 3px 16px rgba(0, 0, 0, 0.08);
}

.form-group {
  margin-bottom: 1.6rem;
}

.form-group label {
  display: block;
  font-weight: 600;
  margin-bottom: 0.6rem;
  font-size: 0.8rem;
}

.form-control {
  width: 100%;
  padding: 0.6rem 0.8rem;
  border: 1px solid #ccc;
  border-radius: 5px;
  font-size: 0.8rem;
  transition: border-color 0.2s;
  background-color: #fff;
}

.form-control:focus {
  outline: none;
  border-color: var(--accent-color);
}

textarea.form-control {
  resize: vertical;
}

.type-selection {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 1.2rem;
}

.type-selection button {
  background: var(--surface-color);
  border-radius: 24px;
  padding: 0.8rem;
  border: 2px solid #657143;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03);
  cursor: pointer;
  text-align: center;
  transition: color 0.4s ease, box-shadow 0.3s, transform 0.3s ease;
  position: relative;
  overflow: hidden;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.6rem;
  color: var(--primary-text-color);
}

.type-selection button::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(138, 154, 91, 0.4);
  transform-origin: top;
  transform: scaleY(0);
  transition: transform 0.5s ease-in-out;
  z-index: -1;
}

.type-selection button:hover::before,
.type-selection button.active::before {
  transform-origin: bottom;
  transform: scaleY(1);
}

.type-selection button:hover,
.type-selection button.active {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  color: white;
  border-color: #657143;
}

.type-selection button i {
  font-size: 2rem;
  color: var(--accent-color);
  margin-bottom: 0.4rem;
  transition: color 0.4s ease;
}

.type-selection button span {
  font-family: 'EBSHunminjeongeumSaeronL', serif;
  font-size: 1.2rem;
  font-weight: 600;
  transition: color 0.4s ease;
}

.type-selection button:hover i,
.type-selection button:hover span,
.type-selection button.active i,
.type-selection button.active span {
  color: white;
}

.genre-toggle {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
}

.genre-toggle button {
  background: rgba(138, 154, 91, 0.2);
  border: 1px solid transparent;
  border-radius: 16px;
  padding: 0.2rem 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
}

.genre-toggle button:hover {
  background: #a8b87f;
}

.genre-toggle button.active {
  background-color: #6F7D48;
  color: white;

}

.form-actions {
  text-align: center;
  margin-top: 2.4rem;
  display: flex;
  justify-content: center;
  gap: 0.8rem;
}

/* --- Workspace Section --- */
.workspace-section {
  position: relative;
}

.workspace-header {
  display: flex;
  align-items: center;
  margin: 0rem 1.6rem 0.8rem 0.8rem;
  gap: 0.8rem;
}

.book-title-input {
  flex-grow: 1;
}

.workspace-main {
  display: grid;
  grid-template-columns: 224px 1fr;
  gap: 1.6rem;
  height: calc(100vh - 176px);
}

.story-list-container {
  background: var(--surface-color);
  border-radius: 6px;
  border: 1px solid var(--border-color);
  padding: 0.8rem;
  display: flex;
  flex-direction: column;
  font-family: 'Noto Serif KR', serif;
}

.story-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.4rem;
  margin-bottom: 0.4rem;
}

.story-list-title {
  font-size: 0.8rem;
  font-weight: 700;
  color: #000000;
  margin: 0;
  font-family: 'SCDream4', serif;
}

.btn-add-story {
  background: none;
  border: 1px dashed var(--border-color);
  color: var(--secondary-text-color);
  border-radius: 50%;
  cursor: pointer;
  width: 26px;
  height: 26px;
  display: grid;
  place-items: center;
  transition: all 0.2s;

}

.btn-add-story:hover {
  border-color: var(--accent-color);
  color: var(--accent-color);
  transform: rotate(90deg);
}

.story-list {
  list-style: none;
  padding: 0;
  margin: 0;
  overflow-y: auto;
  font-family: 'SCDream4', serif;
  flex-grow: 1;
}

.story-list li {
  padding: 0.6rem 0.8rem;
  border-radius: 0;
  cursor: pointer;
  color: #555;
  transition: background-color 0.2s, color 0.2s;
  border-left: 2px solid transparent;
  border-bottom: 1px solid #EAE0D5;
}

.story-list li:last-child {
  border-bottom: none;
}

.story-list li:hover {
  background-color: #f8ffea56;
}

.story-list li.active {
  background-color: #f1fade56;
  color: var(--primary-text-color);
  border-radius: 4px;
}

.story-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-delete-story {
  background: none;
  border: none;
  color: #adb5bd;
  font-size: 1rem;
  cursor: pointer;
  padding: 0 0.4rem;
  visibility: hidden;
  opacity: 0;
  transition: all 0.2s;
}

.story-list li:hover .btn-delete-story {
  visibility: visible;
  opacity: 1;
}

.btn-delete-story:hover {
  color: #000000;
}

.editor-area {
  display: grid;
  grid-template-columns: 1fr 192px;
  gap: 1.2rem;
  background: var(--paper-color);
  border-radius: 6px;
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.editor-main {
  padding: 0.8rem;
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.story-title-input {
  flex-grow: 1;
}

.ai-question-area {
  background: #fafafa;
  padding: 1.2rem;
  border-radius: 5px;
  color: #000000;
  font-size: 20px;
  border: 1px solid var(--border-color);
}

.ai-question-area p i {
  margin-right: 0.4rem;
}

.story-content-wrapper {
  position: relative;
  flex-grow: 1;
}

.story-content-editor {
  flex-grow: 1;
  width: 100%;
  height: 100%;
  padding: 0.8rem;
  padding-bottom: 1.6rem;
  resize: none;
  border: 1px solid var(--border-color);
  border-radius: 5px;
  background: rgba(138, 154, 91, 0.02);
  outline: none;
  font-family: 'MaruBuri-Light', serif;
  font-size: 20px;
  line-height: 1.5;
}

.char-counter {
  position: absolute;
  bottom: 24px;
  right: 24px;
  font-size: 0.7rem;
  color: #888888c5;
}

.editor-sidebar {
  background: var(--surface-color);
  padding: 0.8rem 2rem;
  border-left: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  align-items: flex-end;
}

.btn-sidebar {
  width: 39px;
  height: 39px;
  margin: 0;
  padding: 0;
  border-radius: 44px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  transition: all 0.4s ease-in-out;
  font-weight: 500;
  background-color: #fff;
  border: 2px solid #664c39;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  font-size: 12px;
}

.btn-sidebar span {
  visibility: hidden;
  opacity: 0;
  width: 0;
  transition: visibility 0s 0.2s, opacity 0.2s ease, width 0.3s ease;
}

.btn-sidebar:hover {
  width: 150px;
  border-radius: 44px;
  justify-content: flex-start;
  padding: 0 0.7rem;
  gap: 0.55rem;
  border-color: var(--accent-color);
  background-color: #f6f8f2;
}

.btn-sidebar:hover span {
  visibility: visible;
  opacity: 1;
  width: auto;
  transition: visibility 0s, opacity 0.2s ease 0.2s, width 0.3s ease 0.1s;
}

.btn-sidebar i {
  font-size: 1rem;
  flex-shrink: 0;
}

.btn-sidebar.font-small {
  font-size: 0.65rem;
}

.btn-sidebar:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-sidebar.btn-recording {
  background-color: #ffdddd;
  border-color: #ff8a8a;
}

.sidebar-divider {
  margin: 1.2rem 0;
  border: none;
  border-top: 1px solid var(--border-color);
}

.audio-visualizer-container {
  margin-top: 0.8rem;
  height: 6px;
  background: #EAE0D5;
  border-radius: 3px;
  overflow: hidden;
}

.audio-visualizer-container canvas {
  width: 100%;
  height: 100%;
}

.correction-panel {
  border: 1px solid #b19366;
  background: #fff7f0;
  padding: 0.8rem;
  border-radius: 10px;
}

.correction-panel h4 {
  margin: 0 0 0.4rem 0;
}

.correction-actions {
  display: flex;
  gap: 0.4rem;
  margin-top: 0.4rem;
}

/* --- Publish Section Specifics --- */
.publish-section .form-control {
  border: 1px solid #ccc;
}

.publish-section .title-input-highlight {
  background-color: #fff;
  border: 1px solid #ccc;
  font-family: 'Pretendard', sans-serif;
  font-weight: 400;
}

.publish-header {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  margin-bottom: 0.4rem;
}

.publish-header .section-title {
  margin-bottom: 0;
}

.cover-selection {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 0.8rem;
}

.cover-option {
  border: 2px solid transparent;
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.2s;
  overflow: hidden;
}

.cover-option img {
  width: 100%;
  height: auto;
  display: block;
}

.cover-option:hover {
  border-color: #ccc;
}

.cover-option.active {
  border-color: var(--accent-color);
  transform: scale(1.05);
}

.no-story-message {
  text-align: center;
  margin: auto;
  color: #b0a89f;
}

.no-story-message i {
  font-size: 2.4rem;
  margin-bottom: 0.8rem;
}

.editor-title-wrapper {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  padding-bottom: 0.8rem;
}

.editor-title-label {
  font-family: 'ChosunCentennial', serif;
  font-weight: 600;
  font-size: 1.2rem;
  white-space: nowrap;
  color: #414141;
}

/* --- Tag Input Styles --- */
.tag-container {
  border-radius: 5px;
  padding: 0.4rem;
  padding-bottom: 0;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
  margin-bottom: 0.4rem;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  background-color: #a8b87f;
  color: #000000;
  border-radius: 13px;
  padding: 0.2rem 0.6rem;
  font-size: 0.7rem;
  font-weight: 500;
}

.btn-remove-tag {
  background: none;
  border: none;
  color: #000000;
  margin-left: 0.4rem;
  cursor: pointer;
  font-size: 1rem;
  line-height: 1;
  padding: 0;
}

.btn-remove-tag:hover {
  color: #343a40;
}

.tag-container .form-control {
  border: 1px solid #ccc;
  box-shadow: none;
  padding-left: 0.6rem;
  margin-top: 0.4rem;
}

.tag-container .form-control:focus {
  box-shadow: none;
}

/* 발행하기 & 임시저장 버튼 항상 확장 스타일 */
.btn-primary-sidebar,
.btn-outline-sidebar {
  width: 150px;
  border-radius: 44px;
  justify-content: flex-start;
  padding: 0 0.7rem;
  gap: 0.55rem;
  background-color: #f6f8f2;
}

/* 위 버튼들의 텍스트(span)를 항상 보이게 처리 */
.btn-primary-sidebar span,
.btn-outline-sidebar span {
  visibility: visible;
  opacity: 1;
  width: auto;
}

/* 호버 시에도 너비가 변경되지 않도록 고정 */
.btn-primary-sidebar:hover,
.btn-outline-sidebar:hover {
  scale: 1.05;
}

/* --- 반응형 레이아웃을 위한 미디어 쿼리 --- */
@media (max-width: 1400px) {

  /* 전체 작업 공간을 세로로 쌓기 */
  .workspace-main {
    grid-template-columns: 1fr;
    height: auto;
    gap: 1.2rem;
  }

  /* 에디터 영역을 세로로 쌓기 */
  .editor-area {
    grid-template-columns: 1fr;
  }

  /* 사이드바를 가로 버튼 그룹으로 변경 */
  .editor-sidebar {
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: center;
    gap: 0.6rem;
    border-left: none;
    border-top: 1px solid var(--border-color);
    padding: 1.2rem 0.8rem;
  }

  .btn-sidebar:hover {
    width: auto;
    min-width: 128px;
    height: 35px;
    border-radius: 26px;
    justify-content: flex-start;
    padding: 0 0.8rem;
    transform: none;
    font-size: 0.8rem;
  }


}
</style>
