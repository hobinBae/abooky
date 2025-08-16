<template>
  <div class="book-editor-page">
    <CustomAlert ref="customAlertRef" />

    <input type="file" ref="storyImageInput" @change="handleStoryImageUpload" accept="image/*" style="display: none;">
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

        <div class="left-sidebar-content">

          <div class="story-list-container">
            <div class="story-list-header">
              <h3 class="story-list-title">목차</h3>
              <button @click="addStory" class="btn-add-story" title="이야기 추가"><i class="bi bi-plus-lg"></i></button>
            </div>
            <ul class="story-list">
              <li v-for="(story, index) in paginatedStories" :key="story.id ?? ('tmp-' + index)"
                @click="selectStory((storiesCurrentPage - 1) * storiesPerPage + index)"
                :class="{ active: ((storiesCurrentPage - 1) * storiesPerPage + index) === currentStoryIndex }"
                :title="`${story.title} - 내용: ${story.content?.substring(0, 50) || '비어있음'}...`">
                <span>{{ story.title }}</span>
                <button @click.stop="deleteStory(story, (storiesCurrentPage - 1) * storiesPerPage + index)"
                  class="btn-delete-story">×</button>
              </li>
            </ul>
            <div v-if="totalStoryPages > 1" class="story-list-pagination">
              <button @click="prevStoryPage" :disabled="storiesCurrentPage === 1" class="btn-pagination">&lt;</button>
              <span>{{ storiesCurrentPage }} / {{ totalStoryPages }}</span>
              <button @click="nextStoryPage" :disabled="storiesCurrentPage === totalStoryPages"
                class="btn-pagination">&gt;</button>
            </div>
          </div>

          <div class="story-image-preview-container">
            <div v-if="currentStory?.imageUrl" class="image-preview-box">

              <button @click="removeStoryImage" class="btn-remove-image" title="이미지 삭제">×</button>

              <img :src="currentStory.imageUrl" alt="이야기 이미지 미리보기">
            </div>
            <div v-else class="image-preview-placeholder">
              <i class="bi bi-card-image"></i>
              <span>이야기에 첨부된 이미지가 없습니다.</span>
            </div>
          </div>
        </div>

        <div class="editor-area" v-if="currentStory">
          <div class="editor-main">
            <div class="editor-title-wrapper">
              <span class="editor-title-label">이야기 제목</span>
              <input type="text" v-model="currentStory.title" placeholder="이야기 제목"
                class="story-title-input title-input-highlight">
            </div>
            <!-- 호빈 주석 -->
            <!-- <div class="ai-question-area">
              <p v-if="isInterviewStarted"><i class="bi bi-robot"></i> {{ aiQuestion }}</p>
              <p v-else><i class="bi bi-robot"></i>AI 인터뷰 시작을 누르고 질문을 받아보세요.</p>
            </div> -->
            <div class="ai-question-area">
              <p><i class="bi bi-robot"></i> {{ aiQuestion }}</p>
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
            <button v-if="!isRecording" @click="startRecording" class="btn-sidebar"><i
                class="bi bi-soundwave"></i><span>음성 답변 시작</span></button>
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
            <button @click="triggerImageUpload" class="btn-sidebar"><i class="bi bi-image"></i> <span>이야기 사진
                첨부</span></button>
            <div class="sidebar-action-group">
              <button @click="saveDraft" class="btn-sidebar btn-outline-sidebar">
                <i class="bi bi-cloud-arrow-down"></i> <span>임시 저장 (나가기)</span>
              </button>
              <button @click="moveToPublishingStep" class="btn-sidebar btn-primary-sidebar">
                <i class="bi bi-send-check"></i> <span>발행하기</span>
              </button>
            </div>
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
import CustomAlert from '@/components/common/CustomAlert.vue'; // [추가] CustomAlert 컴포넌트 가져오기

// --- 인터페이스 정의 ---
interface Story { id?: number; title: string; content: string; activeSessionId?: string | null; imageUrl?: string; imageId?: number; }
interface Book { id: string; title: string; summary: string; type: string; authorId: string; isPublished: boolean; stories: Story[]; createdAt: Date; updatedAt: Date; tags?: string[]; completed?: boolean; }
interface ApiEpisode { episodeId: number; title: string; content: string; activeSessionId?: string | null; imageUrl?: string; imageId?: number; }

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

interface EpisodeResponseData {
  episodeId?: number;
  id?: number;
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
// [추가] CustomAlert 컴포넌트의 참조를 저장할 ref 생성
const customAlertRef = ref<InstanceType<typeof CustomAlert> | null>(null);
// --- 컴포넌트 상태 ---
const creationStep = ref<'setup' | 'editing' | 'publishing'>('setup');
const currentBook = ref<Partial<Book & { categoryId: number | null }>>({ title: '', summary: '', type: 'autobiography', stories: [], tags: [], categoryId: null });
const selectedCategoryId = ref<number | null>(null);
const currentStoryIndex = ref(-1);
const aiQuestion = ref('AI 인터뷰 시작을 누르고 질문을 받아보세요.');

// 현재 스토리 상태에 따라 AI 질문 메시지를 업데이트하는 함수
function updateAiQuestionMessage() {
  if (isInterviewStarted.value) {
    // 인터뷰가 진행 중이면 그대로 유지
    return;
  }

  if (currentStory.value?.content?.trim()) {
    // 에피소드에 내용이 있으면 편집 유도 메시지
    aiQuestion.value = '이미 작성된 에피소드입니다. 내용을 수정하거나 새로운 이야기를 추가해보세요.';
  } else {
    // 에피소드가 비어있으면 인터뷰 시작 유도 메시지
    aiQuestion.value = 'AI 인터뷰 시작을 누르고 질문을 받아보세요.';
  }
}
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

const storyImageInput = ref<HTMLInputElement | null>(null);

const isSavingAnswer = ref(false);

// --- [추가] 목차 페이지네이션 상태 ---
const storiesCurrentPage = ref(1);
const storiesPerPage = 5; // 페이지 당 5개의 이야기를 표시
// --- 오디오 녹음 상태 ---
const visualizerCanvas = ref<HTMLCanvasElement | null>(null);
let audioContext: AudioContext | null = null;
let analyser: AnalyserNode | null = null;
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
    customAlertRef.value?.showAlert({
      title: '입력 필요',
      message: '책 제목을 입력해주세요.'
    });
    return;
  }
  if (!selectedCategoryId.value) {
    customAlertRef.value?.showAlert({
      title: '선택 필요',
      message: '장르를 선택해 주세요.'
    });
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
        'Content-Type': 'multipart/form-data',
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
    customAlertRef.value?.showAlert({
      title: '오류 발생',
      message: '책 생성에 실패했습니다.'
    });
  }
}

// 단계 2: 편집
async function startRecording() {
  if (!isInterviewStarted.value || !currentSessionId.value) {
    customAlertRef.value?.showAlert({
      title: '인터뷰 시작 필요',
      message: '먼저 AI 인터뷰 시작을 눌러주세요.'
    });
    return;
  }
  if (isRecording.value) return;

  try {
    mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true });
    isRecording.value = true;
    audioChunks = [];

    // --- 오디오 시각화 설정 (복원된 코드) ---
    if (!audioContext) {
      audioContext = new AudioContext();
    }
    if (audioContext.state === 'suspended') {
      await audioContext.resume();
    }

    if (!audioContext) {
      customAlertRef.value?.showAlert({
        title: '오류 발생',
        message: '오디오 컨텍스트를 생성할 수 없습니다.'
      });
      isRecording.value = false;
      return;
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

      if (audioBlob.size < 1024) { // 너무 짧은 녹음은 보내지 않음
        console.log('녹음된 오디오가 너무 짧아 전송하지 않습니다.');
        customAlertRef.value?.showAlert({
          title: '입력 오류',
          message: '3초 이상 답변해주세요.'
        });
        return;
      }

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
        customAlertRef.value?.showAlert({
          title: '오류 발생',
          message: '음성 답변 처리에 실패했습니다.'
        });
      }
    };

    mediaRecorder.start();

    await nextTick();

    visualize(); // 시각화 함수 다시 호출

  } catch (err) {
    console.error('마이크 접근 오류:', err);
    customAlertRef.value?.showAlert({
      title: '오류 발생',
      message: '마이크에 접근할 수 없습니다. 권한을 확인해주세요.'
    });
    isRecording.value = false;
  }
}

function stopRecording() {
  if (!isRecording.value || !mediaRecorder) return;

  mediaRecorder.stop();

  isRecording.value = false;
  mediaStream?.getTracks().forEach(track => track.stop());
  mediaStream = null;

  // 시각화 애니메이션 중지 (복원된 코드)
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
        activeSessionId: e.activeSessionId,
        imageUrl: e.imageUrl,
        imageId: e.imageId
      })) || [],
      tags: bookData.tags || [],
      categoryId: bookData.categoryId,
      type: bookData.bookType.toLowerCase(),
      completed: bookData.completed,
    };
    tags.value = bookData.tags || [];
    selectedCategoryId.value = bookData.categoryId;
    creationStep.value = 'editing';

    if (currentBook.value.stories && currentBook.value.stories.length > 0) {
      const activeStoryIndex = currentBook.value.stories.findIndex(story =>
        story.activeSessionId && story.activeSessionId.trim() !== ''
      );

      if (activeStoryIndex !== -1) {
        console.log(`진행 중인 세션이 있는 스토리(인덱스: ${activeStoryIndex})를 선택합니다.`);
        await selectStory(activeStoryIndex);
      } else {
        await selectStory(0);
      }
    }
  } catch (error) {
    console.error('책 정보를 불러오는데 실패했습니다:', error);
    customAlertRef.value?.showAlert({
      title: '오류 발생',
      message: '책 정보를 불러오는데 실패했습니다. 이전 페이지로 돌아갑니다.'
    });
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
    customAlertRef.value?.showAlert({
      title: '삭제 오류',
      message: '삭제할 이야기의 정보가 올바르지 않습니다.'
    });
    return;
  }

  try {
    await apiClient.delete(`/api/v1/books/${currentBook.value.id}/episodes/${story.id}`);

    // 1. 배열에서 이야기를 삭제합니다.
    currentBook.value.stories?.splice(index, 1);

    // 2. [추가] 페이지네이션 보정 로직
    // 현재 페이지가 1보다 크고, 삭제 후 현재 페이지에 더 이상 이야기가 없다면
    if (storiesCurrentPage.value > 1 && paginatedStories.value.length === 0) {
      // 이전 페이지로 이동합니다.
      storiesCurrentPage.value--;
    }

    // 3. 선택된 이야기 인덱스를 조정합니다.
    if (currentStoryIndex.value === index) {
      currentStoryIndex.value = -1;
    } else if (currentStoryIndex.value > index) {
      currentStoryIndex.value--;
    }

    customAlertRef.value?.showAlert({
      title: '삭제 완료',
      message: '이야기가 삭제되었습니다.'
    });
  } catch (error) {
    console.error('이야기 삭제 오류:', error);
    customAlertRef.value?.showAlert({
      title: '삭제 오류',
      message: '이야기 삭제에 실패했습니다.'
    });
  }
}

async function fetchEpisodeImages(episodeId: number) {
  if (!currentBook.value?.id) return;
  try {
    const response = await apiClient.get(`/api/v1/books/${currentBook.value.id}/episodes/${episodeId}/images`);
    if (response.data.data && response.data.data.length > 0) {
      const story = currentBook.value.stories?.find(s => s.id === episodeId);
      if (story) {
        story.imageUrl = response.data.data[0].imageUrl;
        story.imageId = response.data.data[0].imageId;
      }
    }
  } catch (error) {
    console.error(`${episodeId}번 이야기의 이미지 정보를 불러오는데 실패했습니다.`, error);
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
    customAlertRef.value?.showAlert({
      title: '추가 오류',
      message: '새로운 이야기를 추가하는데 실패했습니다.'
    });
  }
}


async function selectStory(index: number) {
  // ★ 다른 스토리를 선택하기 전에, 현재 진행 중인 인터뷰 상태를 완전히 정리합니다.
  await resetInterviewState();

  currentStoryIndex.value = index;
  // isContentChanged.value = false; // resetInterviewState에 포함됨

  const story = currentBook.value.stories?.[index];
  if (story && !story.imageUrl) {
    await fetchEpisodeImages(story.id!);
  }

  // Vue의 반응성을 보장하기 위해 강제로 업데이트
  await nextTick();

  // 재연결 로직은 activeSessionId 기반이므로 그대로 유지해도 좋습니다.
  // 다만 이 로직은 현재 구현에서는 사용되지 않을 수 있습니다.
  if (story && story.activeSessionId) {
    console.log(`기존 세션(${story.activeSessionId})에 재연결합니다.`);
    currentSessionId.value = story.activeSessionId;
    isInterviewStarted.value = true;
    await connectToSseStream();
  }

  // 콘솔 로그로 현재 선택된 스토리 확인
  console.log(`스토리 선택됨 - 인덱스: ${index}, 제목: ${story?.title}, 내용 길이: ${story?.content?.length || 0}`);

  // AI 질문 메시지 업데이트
  updateAiQuestionMessage();
}


async function saveStory() {
  if (isInterviewStarted.value === true) {
    customAlertRef.value?.showAlert({
      title: '안내',
      message: 'AI 인터뷰 진행 중에는 "질문 답변완료" 버튼을 사용해주세요. 이 버튼이 답변 저장과 다음 질문 요청을 모두 처리합니다.'
    });
    return;
  }

  if (!currentStory.value?.id || !currentBook.value?.id) {
    customAlertRef.value?.showAlert({
      title: '저장 오류',
      message: '저장할 에피소드 정보가 올바르지 않습니다.'
    });
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
    customAlertRef.value?.showAlert({
      title: '저장 완료',
      message: '에피소드가 성공적으로 저장되었습니다.'
    });
    isContentChanged.value = false;

  } catch (error) {
    console.error('에피소드 저장(수정) 실패:', error);
    customAlertRef.value?.showAlert({
      title: '저장 오류',
      message: '에피소드 저장에 실패했습니다.'
    });
  }
}

// [수정] 상태 초기화 로직을 하나의 함수로 통합하여 재사용성 및 안정성 확보
async function resetInterviewState() {
  console.log("인터뷰 상태를 초기화합니다...");

  if (eventSource) {
    eventSource.close();
    eventSource = null;
  }

  isConnected.value = false;
  isConnecting.value = false;
  isInterviewStarted.value = false;
  isRecording.value = false;
  isContentChanged.value = false;
  currentSessionId.value = null;
  currentAnswerMessageId.value = null;
  firstChunkForThisAnswer = true;

  // Story 객체의 activeSessionId도 초기화
  if (currentStory.value) {
    currentStory.value.activeSessionId = null;
  }

  // AI 질문 메시지 업데이트 (인터뷰 종료 후 상태에 맞게)
  updateAiQuestionMessage();
}



async function startAiInterview() {
  if (!currentBook.value?.id) {
    customAlertRef.value?.showAlert({
      title: '정보 오류',
      message: '책 정보가 올바르지 않습니다.'
    });
    return;
  }
  if (!currentStory.value?.id) {
    customAlertRef.value?.showAlert({
      title: '선택 오류',
      message: '먼저 이야기를 추가/선택해주세요.'
    });
    return;
  }
  await resetInterviewState();

  if (isConnecting.value || isConnected.value || isInterviewStarted.value) {
    console.log('이미 AI 인터뷰가 진행 중이거나 연결 중입니다.');
    return;
  }

  try {
    const res = await apiClient.post(
      `/api/v1/conversation/${currentBook.value.id}/episodes/${currentStory.value.id}/sessions`
    );
    currentSessionId.value = res.data.data.sessionId;

    if (currentStory.value) {
      currentStory.value.activeSessionId = currentSessionId.value;
    }

    isInterviewStarted.value = true;
    isContentChanged.value = false;
    aiQuestion.value = 'AI 인터뷰 세션에 연결 중... 첫 질문을 기다립니다.';
    await connectToSseStream();
  } catch (e) {
    console.error('세션 시작 실패:', e);
    customAlertRef.value?.showAlert({
      title: '세션 오류',
      message: 'AI 인터뷰 세션 시작에 실패했습니다.'
    });
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
  await new Promise(res => setTimeout(res, 300));
}

async function cleanupBeforeLeave() {
  console.log('페이지 이탈 전 상태 정리 시작...');
  await safeCloseEventSource();
  currentSessionId.value = null;
  currentAnswerMessageId.value = null;
  isInterviewStarted.value = false;
  isContentChanged.value = false;
  firstChunkForThisAnswer = true;
  aiQuestion.value = 'AI 인터뷰 시작을 누르고 질문을 받아보세요.';
  console.log('페이지 이탈 전 상태 정리 완료');
}

let firstChunkForThisAnswer = true;

async function connectToSseStream() {
  if (!currentSessionId.value) {
    console.warn('세션 ID가 없어 SSE 연결을 할 수 없습니다.');
    return;
  }
  if (isConnecting.value || isConnected.value) {
    console.log('이미 SSE 연결 중이거나 연결되어 있습니다.');
    return;
  }

  isConnecting.value = true;
  await safeCloseEventSource();

  try {
    const baseURL = apiClient.defaults?.baseURL || '';
    const url = `${baseURL}/api/v1/conversation/${currentBook.value.id}/${currentSessionId.value}/stream`;
    eventSource = new EventSource(url, { withCredentials: true });

    eventSource.onopen = () => {
      console.log('SSE 연결 성공');
      isConnecting.value = false;
      isConnected.value = true;
    };

    // === QUESTION ===
    eventSource.addEventListener('question', (ev: MessageEvent<string>) => {
      const q = safeJson<QuestionEventData>(ev.data);
      if (!q) return;

      const isCompletion = q.questionType === 'CHAPTER_COMPLETE' || q.isLastQuestion === true;
      aiQuestion.value = q.text ?? '';

      if (isCompletion) {
        console.log('챕터/전체 완료 신호 수신. 연결 종료.');
        const storyTitle = currentStory.value?.title || '현재';
        aiQuestion.value = `'${storyTitle}' 이야기가 완성되었습니다. 내용을 확인하고 저장하거나, 왼쪽 목차에서 새 이야기를 만들어 다음 챕터를 시작해주세요.`;

        // 세션 종료 상태로 전환
        isInterviewStarted.value = false;
        isConnected.value = false;

        try { eventSource?.close(); } catch { }
        eventSource = null;
        return;
      }

      // 챕터 완료가 아닌 모든 질문에서는 에디터를 비워서 새로 시작
      if (!isCompletion && currentStory.value) {
        console.log('새 질문 수신 - 에디터 내용 초기화');
        currentStory.value.content = '';
        if (currentBook.value?.stories && currentStoryIndex.value >= 0) {
          const cur = currentBook.value.stories[currentStoryIndex.value];
          currentBook.value.stories.splice(currentStoryIndex.value, 1, { ...cur, content: '' });
        }
      }

      firstChunkForThisAnswer = true;
      isContentChanged.value = false;
    });

    // === PARTIAL TRANSCRIPT ===
    eventSource.addEventListener('partialTranscript', async (ev: MessageEvent<string>) => {
      console.log('🎤 SSE partialTranscript 이벤트 수신:', ev.data);
      const t = safeJson<PartialTranscriptEventData>(ev.data);
      if (!t) {
        console.error('❌ partialTranscript JSON 파싱 실패:', ev.data);
        return;
      }

      if (currentStory.value) {
        if (firstChunkForThisAnswer) {
          currentStory.value.content = '';
          firstChunkForThisAnswer = false;
        }
        currentStory.value.content += (t.text || '') + ' ';
        await nextTick();

        if (currentBook.value?.stories && currentStoryIndex.value >= 0) {
          const cur = currentBook.value.stories[currentStoryIndex.value];
          if (cur) currentBook.value.stories.splice(currentStoryIndex.value, 1, { ...cur });
        }
      }

      if (typeof t.messageId !== 'undefined' && t.messageId !== null) {
        currentAnswerMessageId.value = t.messageId as number;
      }
      isContentChanged.value = true;
    });

    // === EPISODE (완성본 수신) ===
    eventSource.addEventListener('episode', async (ev: MessageEvent<string>) => {
      console.log('생성된 에피소드 데이터를 수신했습니다:', ev.data);

      const e = safeJson<EpisodeResponseData>(ev.data);
      if (!e) {
        console.warn('episode 이벤트 JSON 파싱 실패:', ev.data);
        return;
      }

      const episodeId = e.episodeId ?? e.id;
      const title = e.title ?? '';
      const content = e.content ?? '';
      if (!episodeId) {
        console.warn('에피소드 ID가 없습니다:', e);
        return;
      }

      const storiesArr = currentBook.value?.stories;
      if (!storiesArr || storiesArr.length === 0) {
        console.warn('stories 배열이 비어있습니다.');
        return;
      }

      const idx = storiesArr.findIndex(s => s.id === episodeId);
      if (idx === -1) {
        console.warn('stories에서 해당 에피소드를 찾지 못했습니다:', episodeId);
        return;
      }

      const updated = { ...storiesArr[idx], title, content };
      storiesArr.splice(idx, 1, updated); // 반응성 트리거
      await nextTick();

      console.log('에피소드 업데이트 완료:', updated);

      // AI 질문 메시지 업데이트 (에피소드 생성 후)
      updateAiQuestionMessage();
    });

    // === ERROR ===
    eventSource.onerror = (error) => {
      console.error('SSE 에러:', error);
      isConnecting.value = false;
      isConnected.value = false;
      isInterviewStarted.value = false;
      aiQuestion.value = '인터뷰 서버와 연결이 끊겼습니다. 페이지를 새로고침 해주세요.';
      try { eventSource?.close(); } catch { }
    };

  } catch (error) {
    console.error('SSE 연결 실패:', error);
    isConnecting.value = false;
    isConnected.value = false;
    aiQuestion.value = 'AI 인터뷰 서버 연결에 실패했습니다. 잠시 후 다시 시도해주세요.';
  }
}
function safeJson<T>(data: string): T | null {
  try {
    return JSON.parse(data) as T;
  } catch {
    return null;
  }
}

async function submitAnswerAndGetFollowUp() {
  if (!isInterviewStarted.value || !currentSessionId.value) return;
  if (!currentStory.value?.content?.trim()) {
    customAlertRef.value?.showAlert({
      title: '입력 필요',
      message: '답변을 입력해주세요.'
    });
    return;
  }

  isSavingAnswer.value = true;

  try {
    console.log('답변 저장 및 다음 질문 요청...');

    // 1. 먼저 답변을 저장
    if (currentAnswerMessageId.value) {
      // STT로 받은 답변을 수정한 경우 → 기존 메시지 업데이트
      console.log(`기존 메시지 업데이트: ID=${currentAnswerMessageId.value}`);
      const updateRequest = {
        messageId: currentAnswerMessageId.value,
        content: currentStory.value.content.trim()
      };
      await apiClient.put('/api/v1/conversation/message', updateRequest);
      console.log('기존 답변 업데이트 완료');
    } else {
      // 직접 입력한 답변 → 새 메시지 생성
      console.log('새 답변 메시지 생성');
      const createRequest = {
        sessionId: currentSessionId.value,
        messageType: 'ANSWER',
        content: currentStory.value.content.trim()
      };
      const response = await apiClient.post('/api/v1/conversation/message', createRequest);
      currentAnswerMessageId.value = response.data.messageId;
      console.log('새 답변 메시지 생성 완료:', response.data);
    }

    // 2. 다음 질문 요청
    await apiClient.post(`/api/v1/conversation/${currentBook.value.id}/episodes/${currentStory.value?.id}/next?sessionId=${currentSessionId.value}`);

    isContentChanged.value = false;
    firstChunkForThisAnswer = true;

  } catch (error) {
    console.error('답변 저장 또는 다음 질문 요청 실패:', error);
  } finally {
    isSavingAnswer.value = false;
  }
}

async function skipQuestion() {
  if (!isInterviewStarted.value || !currentSessionId.value || !currentBook.value?.id || !currentStory.value?.id) {
    return;
  }
  try {
    await apiClient.post(
      `/api/v1/conversation/${currentBook.value.id}/episodes/${currentStory.value.id}/skip`,
      null,
      { params: { sessionId: currentSessionId.value } }
    );

    // UI 즉시 정리(서버가 곧 새 'question' 이벤트를 푸시함)
    if (currentStory.value) currentStory.value.content = '';
    isContentChanged.value = false;
    firstChunkForThisAnswer = true;

    customAlertRef.value?.showAlert({
      title: '건너뛰기',
      message: '이 질문을 건너뛰었습니다. 새 질문을 불러오는 중...'
    });
  } catch (e) {
    console.error('질문 건너뛰기 실패:', e);
    customAlertRef.value?.showAlert({ title: '오류', message: '질문 건너뛰기에 실패했습니다.' });
  }
}

async function autoCorrect() {
  if (!currentStory.value || !currentStory.value.content?.trim()) {
    customAlertRef.value?.showAlert({
      title: '교정 오류',
      message: '교정할 내용이 없습니다.'
    });
    return;
  }
  if (!selectedCategoryId.value) {
    customAlertRef.value?.showAlert({
      title: '선택 오류',
      message: 'AI 교정을 위해서는 먼저 카테고리를 선택해야 합니다.'
    });
    return;
  }
  isCorrecting.value = true;
  correctedContent.value = null;
  try {
    const requestBody = {
      textToCorrect: currentStory.value.content,
      bookCategory: selectedCategoryId.value
    };
    const response = await apiClient.post('/api/v1/ai/proofread', requestBody);
    correctedContent.value = response.data.data.correctedText;
  } catch (error) {
    console.error('AI 자동 교정 실패:', error);
    customAlertRef.value?.showAlert({
      title: '교정 오류',
      message: 'AI 자동 교정에 실패했습니다. 잠시 후 다시 시도해주세요.'
    });
  } finally {
    isCorrecting.value = false;
  }
}

function applyCorrection() { if (currentStory.value && correctedContent.value) { currentStory.value.content = correctedContent.value; correctedContent.value = null; } }
function cancelCorrection() { correctedContent.value = null; }

async function saveDraft() {
  if (!currentBook.value?.id) {
    customAlertRef.value?.showAlert({
      title: '정보 오류',
      message: '책 정보가 올바르지 않습니다.'
    });
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
        headers: { 'Content-Type': 'multipart/form-data' },
      });

      customAlertRef.value?.showAlert({
        title: '임시 저장',
        message: '임시 저장되었습니다.'
      });
      isSavedOrPublished.value = true;
      await cleanupBeforeLeave();
      router.push('/continue-writing');
    } catch (error) {
      console.error('임시 저장 오류:', error);
      customAlertRef.value?.showAlert({
        title: '임시 저장 오류',
        message: '임시 저장에 실패했습니다.'
      });
    }
  }
}

function moveToPublishingStep() { creationStep.value = 'publishing'; }

function handleCoverUpload(event: Event) {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files[0]) {
    const file = target.files[0];
    uploadedCoverFile.value = file;
    const reader = new FileReader();
    reader.onload = (e) => {
      selectedCover.value = e.target?.result as string;
    };
    reader.readAsDataURL(file);
    customAlertRef.value?.showAlert({
      title: '표지 첨부',
      message: '표지가 첨부되었습니다.'
    });
  }
}

function addTag() {
  const newTag = tagInput.value.trim();
  if (newTag && !tags.value.includes(newTag) && tags.value.length < 5) {
    if (/\s/.test(newTag)) {
      customAlertRef.value?.showAlert({
        title: '입력 오류',
        message: '태그에는 공백을 포함할 수 없습니다.'
      });
      return;
    }
    tags.value.push(newTag);
    tagInput.value = '';
  } else if (tags.value.length >= 5) {
    customAlertRef.value?.showAlert({
      title: '입력 오류',
      message: '태그는 최대 5개까지 등록할 수 있습니다.'
    });
  }
}

function removeTag(index: number) {
  tags.value.splice(index, 1);
}

async function finalizePublication() {
  if (!currentBook.value.id || !currentBook.value.title) {
    customAlertRef.value?.showAlert({
      title: '정보 오류',
      message: '책 정보가 올바르지 않습니다.'
    });
    return;
  }
  if (!confirm('이 정보로 책을 최종 발행하시겠습니까?')) return;

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

    const bookUpdateData = new FormData();
    bookUpdateData.append('title', currentBook.value.title);
    bookUpdateData.append('summary', currentBook.value.summary || '');
    if (selectedCategoryId.value) {
      bookUpdateData.append('categoryId', String(selectedCategoryId.value));
    }
    tags.value.forEach(tag => bookUpdateData.append('tags', tag));

    if (uploadedCoverFile.value) {
      bookUpdateData.append('file', uploadedCoverFile.value);
    } else {
      bookUpdateData.append('coverImageUrl', selectedCover.value);
    }

    await apiClient.patch(`/api/v1/books/${currentBook.value.id}`, bookUpdateData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });

    await apiClient.patch(`/api/v1/books/${currentBook.value.id}/complete`, { tags: tags.value });

    customAlertRef.value?.showAlert({
      title: '발행 완료',
      message: '책이 성공적으로 발행되었습니다!'
    });
    isSavedOrPublished.value = true;
    await cleanupBeforeLeave();
    router.push(`/book-detail/${currentBook.value.id}`);

  } catch (error) {
    console.error('책 발행 오류:', error);
    customAlertRef.value?.showAlert({
      title: '발행 오류',
      message: '책 발행에 실패했습니다.'
    });
  }
}

async function finalizePublicationAsCopy() {
  if (!currentBook.value.id || !currentBook.value.title) {
    customAlertRef.value?.showAlert({
      title: '정보 오류',
      message: '책 정보가 올바르지 않습니다.'
    });
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
    customAlertRef.value?.showAlert({
      title: '복사 오류',
      message: '복사할 이야기가 하나 이상 있어야 합니다.'
    });
    return;
  }

  const copyRequest = {
    title: `${currentBook.value.title} - 복사본`,
    summary: currentBook.value.summary,
    categoryId: selectedCategoryId.value,
    episodes: episodesToCopy,
    tags: tags.value,
  };

  try {
    const response = await apiClient.post(`/api/v1/books/${currentBook.value.id}/copy`, copyRequest);
    const newBook = response.data.data;

    if (selectedCategoryId.value) {
      const bookData = new FormData();
      bookData.append('title', `${currentBook.value.title} - 복사본`);
      bookData.append('summary', currentBook.value.summary || '');
      bookData.append('categoryId', String(selectedCategoryId.value));

      await apiClient.patch(`/api/v1/books/${newBook.bookId}`, bookData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
    }

    await apiClient.patch(`/api/v1/books/${newBook.bookId}/complete`, { tags: tags.value });

    customAlertRef.value?.showAlert({
      title: '발행 완료',
      message: '책이 복사본으로 성공적으로 발행되었습니다!'
    });
    isSavedOrPublished.value = true;
    await cleanupBeforeLeave();
    router.push(`/book-detail/${newBook.bookId}`);
  } catch (error) {
    console.error('복사본 발행 오류:', error);
    customAlertRef.value?.showAlert({
      title: '발행 오류',
      message: '복사본 발행에 실패했습니다.'
    });
  }
}

function uploadimage() {
  customAlertRef.value?.showAlert({
    title: '업로드 오류',
    message: '이미지 업로드 기능은 아직 구현되지 않았습니다.'
  });
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

const handleBeforeUnload = (event: BeforeUnloadEvent) => {
  if (creationStep.value !== 'setup' && !isSavedOrPublished.value) {
    event.preventDefault();
    event.returnValue = '';
  }
};

onBeforeRouteLeave((to, from, next) => {
  if (creationStep.value !== 'setup' && !isSavedOrPublished.value) {
    const answer = window.confirm(
      '저장하지 않은 변경사항이 있습니다. 정말로 페이지를 떠나시겠습니까? 현재 작업 내용은 모두 삭제됩니다.'
    );
    if (answer) {
      next();
    } else {
      next(false);
    }
  } else {
    next();
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

  // 초기 로딩 시 AI 질문 메시지 업데이트
  setTimeout(() => updateAiQuestionMessage(), 100);
});

onUpdated(() => {
  adjustButtonFontSize();
});

onBeforeUnmount(async () => {
  await resetInterviewState();
  if (connectTimer) {
    clearTimeout(connectTimer);
    connectTimer = null;
  }
  if (currentSessionId.value) {
    const baseURL = apiClient.defaults?.baseURL || '';
    const url = `${baseURL}/api/v1/conversation/stream/${currentSessionId.value}`;
    const headers = { 'Authorization': `Bearer ${authStore.accessToken}` };
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

  if (eventSource) {
    eventSource.close();
    isConnected.value = false;
    isConnecting.value = false;
    console.log('SSE 연결 종료');
  }

  window.removeEventListener('beforeunload', handleBeforeUnload);
});

watch(() => currentStory.value?.content, (newContent) => {
  if (isInterviewStarted.value) {
    isContentChanged.value = newContent !== undefined && newContent.trim().length > 0;
    console.log('Content changed, isContentChanged set to:', isContentChanged.value);
  }
});

watch(() => route.params.bookId, async (newBookId, oldBookId) => {
  if (newBookId && newBookId !== oldBookId) {
    console.log(`Route 변경 감지: ${oldBookId} -> ${newBookId}`);
    await cleanupBeforeLeave();
    if (route.query.start_editing === 'true') {
      await loadBookForEditing(newBookId as string);
    } else {
      loadOrCreateBook(newBookId as string || null);
    }
  }
}, { immediate: false });

// 현재 스토리 변경을 감지하여 로그 출력
watch(() => currentStory.value, (newStory, oldStory) => {
  console.log('currentStory 변경됨:', {
    이전: oldStory ? { id: oldStory.id, title: oldStory.title, contentLength: oldStory.content?.length } : null,
    현재: newStory ? { id: newStory.id, title: newStory.title, contentLength: newStory.content?.length } : null
  });
}, { deep: true });

// 현재 스토리 인덱스 변경을 감지
watch(() => currentStoryIndex.value, (newIndex, oldIndex) => {
  console.log(`currentStoryIndex 변경: ${oldIndex} -> ${newIndex}`);
});

// 현재 스토리의 내용 변경을 감지하여 AI 질문 메시지 업데이트
watch(() => currentStory.value?.content, () => {
  updateAiQuestionMessage();
}, { deep: true });

// --- [추가] 목차 페이지네이션을 위한 계산된 속성 및 함수 ---
const totalStoryPages = computed(() => {
  const totalStories = currentBook.value.stories?.length || 0;
  if (totalStories === 0) return 1;
  return Math.ceil(totalStories / storiesPerPage);
});

const paginatedStories = computed(() => {
  const stories = currentBook.value.stories || [];
  const start = (storiesCurrentPage.value - 1) * storiesPerPage;
  const end = start + storiesPerPage;
  return stories.slice(start, end);
});

function prevStoryPage() {
  if (storiesCurrentPage.value > 1) {
    storiesCurrentPage.value--;
  }
}

function nextStoryPage() {
  if (storiesCurrentPage.value < totalStoryPages.value) {
    storiesCurrentPage.value++;
  }
}

interface Story {
  id?: number;
  title: string;
  content: string;
  activeSessionId?: string | null;
  imageUrl?: string; // [추가] 이야기별 이미지 URL
}

// [추가] 숨겨진 파일 입력창을 클릭하는 함수
function triggerImageUpload() {
  if (!currentStory.value) {
    customAlertRef.value?.showAlert({
      title: '선택 오류',
      message: '먼저 이미지를 추가할 이야기를 선택해주세요.'
    });
    return;
  }
  storyImageInput.value?.click();
}

// [추가] 파일이 선택되었을 때 처리하는 함수
async function handleStoryImageUpload(event: Event) {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files[0] && currentStory.value && currentBook.value?.id) {
    const file = target.files[0];
    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await apiClient.post(
        `/api/v1/books/${currentBook.value.id}/episodes/${currentStory.value.id}/images`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        }
      );

      if (currentStory.value) {
        currentStory.value.imageUrl = response.data.data.imageUrl;
      }

      customAlertRef.value?.showAlert({
        title: '업로드 완료',
        message: `'${file.name}' 이미지가 성공적으로 첨부되었습니다.`
      });
    } catch (error) {
      console.error('이미지 업로드 실패:', error);
      customAlertRef.value?.showAlert({
        title: '업로드 실패',
        message: '이미지 업로드 중 오류가 발생했습니다.'
      });
    }
  }
}
// [추가] 이야기 이미지를 삭제하는 함수
async function removeStoryImage() {
  if (currentStory.value && currentBook.value?.id && currentStory.value.id && currentStory.value.imageId) {
    if (!confirm('정말로 이미지를 삭제하시겠습니까?')) return;

    try {
      await apiClient.delete(
        `/api/v1/books/${currentBook.value.id}/episodes/${currentStory.value.id}/images/${currentStory.value.imageId}`
      );

      currentStory.value.imageUrl = undefined;
      currentStory.value.imageId = undefined;

      customAlertRef.value?.showAlert({
        title: '삭제 완료',
        message: '이미지가 성공적으로 삭제되었습니다.'
      });
    } catch (error) {
      console.error('이미지 삭제 실패:', error);
      customAlertRef.value?.showAlert({
        title: '삭제 실패',
        message: '이미지 삭제 중 오류가 발생했습니다.'
      });
    }
  }
}
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
  /* align-self: start; */
  /* [삭제] 이 줄을 삭제하거나 주석 처리합니다. */
}

.story-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.4rem;
  margin-bottom: 0.4rem;
}

/* --- [추가] 목차 페이지네이션 스타일 --- */
.story-list-pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.8rem;
  padding-top: 0.8rem;
  margin-top: auto;
  /* 이 속성은 버튼을 컨테이너 하단에 붙입니다. */
  border-top: 1px solid var(--border-color);
  flex-shrink: 0;
  /* 컨테이너 크기가 줄어도 작아지지 않음 */
}

.story-list-pagination span {
  font-size: 0.8rem;
  font-weight: 500;
  color: var(--secondary-text-color);
  font-family: 'SCDream4', serif;
}

.btn-pagination {
  background: none;
  color: #555;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-pagination:hover:not(:disabled) {
  border-color: var(--accent-color);
  color: var(--accent-color);
}

.btn-pagination:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.story-list-title {
  font-size: 0.8rem;
  font-weight: 700;
  color: #000000;
  margin: 0;
  font-family: 'SCDream4', serif;
}

/* --- [추가] 이야기 이미지 미리보기 스타일 --- */
.story-image-preview-container {
  width: 90%;
  max-width: 250px;
  /* [추가] 최대 너비를 250px로 제한합니다. */
  margin: 1.5rem auto 0;
}

.image-preview-box,
.image-preview-placeholder {
  width: 100%;
  aspect-ratio: 12 / 10;
  /* 미리보기 박스 비율 (조정 가능) */
  border-radius: 6px;
  background-color: var(--surface-color);
  border: 2px solid #5b673b;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;

  /* 이미지가 박스를 벗어나지 않도록 */
}

.image-preview-placeholder {
  flex-direction: column;
  gap: 0.5rem;
  color: var(--secondary-text-color);
  font-size: 0.7rem;
  border-style: dashed;
}

.image-preview-placeholder i {
  font-size: 2rem;
}

.image-preview-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  /* 이미지가 비율을 유지하며 박스를 꽉 채움 */
}

.image-preview-box,
.image-preview-placeholder {
  /* ... 기존 스타일 ... */
  position: relative;
  /* [추가] 자식 요소의 위치 기준점으로 설정 */
  overflow: hidden;
}

/* --- [추가] 이미지 삭제 버튼 스타일 --- */
.btn-remove-image {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  z-index: 10;

  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: none;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;

  display: flex;
  align-items: center;
  justify-content: center;

  font-size: 1.2rem;
  font-weight: bold;
  line-height: 1;

  cursor: pointer;
  transition: background-color 0.2s ease;
}

.btn-remove-image:hover {
  background-color: rgba(0, 0, 0, 0.8);
}

.left-sidebar-content {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  /* [추가] 자식 요소들을 위아래 양끝으로 분리 */
  /* align-self: start; */
  /* [삭제] 높이를 꽉 채우기 위해 이 속성 제거 */
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
  /* flex-grow: 1; */
  /* [삭제] 목록이 불필요하게 늘어나는 것을 방지 */
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

.sidebar-action-group {
  margin-top: auto;
  /* 그룹 전체를 아래로 밀어냅니다. */
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  /* 그룹 내 버튼들의 간격을 설정합니다. */
  width: 100%;
  /* 버튼들이 부모 너비에 맞게 정렬되도록 합니다. */
  align-items: flex-end;
  /* 버튼들을 오른쪽으로 정렬합니다. */
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

  .sidebar-action-group {
    display: contents;
  }

}
</style>
