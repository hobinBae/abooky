<template>
  <div class="book-editor-page">
    <section v-if="creationStep === 'setup'" class="setup-section">
      <h2 class="section-title">새로운 책 만들기</h2>
      <p class="section-subtitle">당신의 이야기를 시작하기 위한 기본 정보를 입력해주세요.</p>

      <div class="setup-form">
        <div class="form-group">
          <label for="book-title">책 제목</label>
          <input id="book-title" type="text" v-model="currentBook.title" placeholder="매력적인 책 제목을 지어주세요"
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
          <label>카테고리 선택</label>
          <div class="genre-toggle">
            <button v-for="category in categories" :key="category.id" @click="selectCategory(category.id)"
              :class="{ active: selectedCategoryId === category.id }">
              {{ category.name }}
            </button>
          </div>
        </div>
        <div class="form-actions">
          <button @click="moveToEditingStep" class="btn btn-primary btn-lg">
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
            <li v-for="(story, index) in currentBook.stories" :key="index"
              @click="selectStory(index)"
              :class="{ active: index === currentStoryIndex }">
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
                placeholder="이곳에 이야기를 적거나 음성 녹음 시작을 누르고 말해 보세요." maxlength="1000"></textarea>
              <div class="char-counter">
                {{ currentStory.content.length }} / 1000
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
                <button @click="cancelCorrection" class="btn btn-outline">교정 취소</button>
              </div>
            </div>
          </div>
          <div class="editor-sidebar">
            <button @click="startAiInterview" class="btn-sidebar"><i class="bi bi-mic"></i> AI 인터뷰 시작</button>

            <button v-if="!isRecording" @click="startRecording" class="btn-sidebar"><i class="bi bi-soundwave"></i> 음성
              답변 시작</button>
            <button v-else @click="stopRecording" class="btn-sidebar btn-recording"><i
                class="bi bi-stop-circle-fill"></i> 음성 답변 완료</button>

            <button @click="submitAnswerAndGetFollowUp" :disabled="!isInterviewStarted || !isContentChanged" class="btn-sidebar"><i
                class="bi bi-check-circle"></i> 질문 답변완료</button>
            <button @click="skipQuestion" :disabled="!isInterviewStarted" class="btn-sidebar"><i
                class="bi bi-skip-end-circle"></i> 질문 건너뛰기</button>
            <button @click="autoCorrect" class="btn-sidebar"><i class="bi bi-magic"></i> AI 자동 교정</button>
            <button @click="saveStory" class="btn-sidebar"><i class="bi bi-save"></i> 이야기 저장</button>

            <hr class="sidebar-divider">

            <button @click="saveDraft" class="btn-sidebar btn-outline-sidebar">
              <i class="bi bi-cloud-arrow-down"></i> 임시 저장 (나가기)
            </button>
            <button @click="moveToPublishingStep" class="btn-sidebar btn-primary-sidebar">
              <i class="bi bi-send-check"></i> 발행하기
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
        <button @click="creationStep = 'editing'" class="btn-back">
          <i class="bi bi-arrow-left"></i> 뒤로가기
        </button>
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
            <input id="book-tags" type="text" v-model="tagInput"
                   @keydown.enter.prevent="addTag"
                   placeholder="태그 입력 후 Enter"
                   class="form-control"
                   :disabled="tags.length >= 5">
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
          <button @click="finalizePublicationAsCopy" class="btn btn-outline btn-lg">
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
import { ref, onMounted, computed, watch, nextTick, onBeforeUnmount } from 'vue';
import { useRouter, useRoute, onBeforeRouteLeave } from 'vue-router';
import apiClient from '@/api'; // API 클라이언트 임포트
import { useAuthStore } from '@/stores/auth';

// --- 인터페이스 정의 ---
interface Story { id?: number; title: string; content: string; }
interface Book { id: string; title: string; summary: string; type: string; authorId: string; isPublished: boolean; stories: Story[]; createdAt: Date; updatedAt: Date; tags?: string[]; }

// --- 정적 데이터 ---
const bookTypes = [{ id: 'autobiography', name: '자서전', icon: 'bi bi-person-badge' }, { id: 'diary', name: '일기장', icon: 'bi bi-journal-bookmark' }, { id: 'freeform', name: '자유', icon: 'bi bi-pen' },];
const categories = [
  { id: 1, name: '자서전' }, { id: 2, name: '일기' }, { id: 3, name: '소설/시' },
  { id: 4, name: '에세이' }, { id: 5, name: '자기계발' }, { id: 6, name: '역사' },
  { id: 7, name: '경제/경영' }, { id: 8, name: '사회/정치' }, { id: 9, name: '청소년' },
  { id: 10, name: '어린이/동화' }, { id: 11, name: '문화/예술' }, { id: 12, name: '종교' },
  { id: 13, name: '여행' }, { id: 14, name: '스포츠' }
];
const coverOptions = ['https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=400', 'https://images.unsplash.com/photo-1532012197267-da84d127e765?q=80&w=400', 'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?q=80&w=400', 'https://images.unsplash.com/photo-1589998059171-988d887df646?q=80&w=400', 'https://images.unsplash.com/photo-1512820790803-83ca734da794?q=80&w=400',];

// --- 라우터 및 라우트 ---
const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

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
const selectedCover = ref(coverOptions[0]);
const tagInput = ref(''); // 현재 입력 중인 태그
const tags = ref<string[]>([]); // 등록된 태그 목록
const isSavedOrPublished = ref(false);

// --- 오디오 녹음 상태 ---
const visualizerCanvas = ref<HTMLCanvasElement | null>(null);
let audioContext: AudioContext | null = null;
let analyser: AnalyserNode | null = null;
let animationFrameId: number | null = null;
let mediaStream: MediaStream | null = null;

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
  if(currentBook.value.summary) bookData.append('summary', currentBook.value.summary);

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
    alert('책 생성에 실패했습니다.');
  }
}

// 단계 2: 편집
async function startRecording() {
  if (isRecording.value) return;
  isRecording.value = true;
  await nextTick();

  if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
    alert('음성 녹음이 지원되지 않는 브라우저입니다.');
    isRecording.value = false;
    return;
  }

  try {
    mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true });
    audioContext = new AudioContext();
    analyser = audioContext.createAnalyser();
    const source = audioContext.createMediaStreamSource(mediaStream);
    source.connect(analyser);
    analyser.fftSize = 256;
    visualize();
  } catch (err) {
    console.error('마이크 접근 오류:', err);
    alert('마이크에 접근할 수 없습니다. 권한을 확인해주세요.');
    isRecording.value = false;
  }
}

function stopRecording() {
  if (!isRecording.value) return;
  isRecording.value = false;

  mediaStream?.getTracks().forEach(track => track.stop());
  mediaStream = null;

  if (audioContext) {
    audioContext.close();
    audioContext = null;
  }
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId);
    animationFrameId = null;
  }
  isContentChanged.value = true;
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

function loadOrCreateBook(bookId: string | null) {
  if (bookId) {
    // TODO: API를 통해 기존 책 데이터를 불러오는 로직 추가
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

function selectStory(index: number) {
  currentStoryIndex.value = index;
  isContentChanged.value = false;
}

async function saveStory() {
  if (!currentStory.value?.id || !currentBook.value?.id) {
    alert('저장할 이야기가 없거나 책 정보가 올바르지 않습니다.');
    return;
  }

  try {
    const episodeData = {
      title: currentStory.value.title,
      content: currentStory.value.content,
    };
    await apiClient.patch(`/api/v1/books/${currentBook.value.id}/episodes/${currentStory.value.id}`, episodeData);
    alert('이야기가 저장되었습니다.');
    isContentChanged.value = false;
  } catch (error) {
    console.error('이야기 저장 오류:', error);
    alert('이야기 저장에 실패했습니다.');
  }
}

function startAiInterview() {
  isInterviewStarted.value = true;
  isContentChanged.value = false;
  aiQuestion.value = '당신의 어린 시절, 가장 기억에 남는 장소는 어디인가요? 그곳에서의 특별한 경험을 이야기해주세요.';
}

function submitAnswerAndGetFollowUp() {
  if (currentStory.value && currentStory.value.content.trim() !== '') {
    aiQuestion.value = `AI가 당신의 답변에 대한 꼬리 질문을 생성했습니다: ${currentStory.value.content.substring(0, 20)}...에 대해 더 자세히 이야기해주세요.`;
    alert('질문 답변이 완료되었고, 꼬리 질문을 받았습니다.');
    isContentChanged.value = false;
  } else {
    alert('답변 내용을 입력하거나 음성 녹음을 완료해주세요.');
  }
}

function skipQuestion() { aiQuestion.value = '질문을 건너뛰었습니다. 새로운 질문: 학창시절, 가장 좋아했던 과목과 그 이유는 무엇인가요?'; alert('질문을 건너뛰었습니다.'); isContentChanged.value = false; }
function autoCorrect() { if (currentStory.value) { correctedContent.value = `(AI 교정됨) ${currentStory.value.content} - 문법과 문체가 ${currentBook.value.type} 스타일에 맞게 수정되었습니다.`; } }
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
        headers: { 'Content-Type': 'multipart/form-data' },
      });

      alert('임시 저장되었습니다.');
      isSavedOrPublished.value = true;
      router.push('/continue-writing');
    } catch (error) {
      console.error('임시 저장 오류:', error);
      alert('임시 저장에 실패했습니다.');
    }
  }
}

function moveToPublishingStep() { creationStep.value = 'publishing'; }

// 단계 3: 발행
function handleCoverUpload(event: Event) {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files[0]) {
    const reader = new FileReader();
    reader.onload = (e) => { selectedCover.value = e.target?.result as string; };
    reader.readAsDataURL(target.files[0]);
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
    const bookData = new FormData();
    bookData.append('title', currentBook.value.title);
    bookData.append('summary', currentBook.value.summary || '');
    if (selectedCategoryId.value) {
      bookData.append('categoryId', String(selectedCategoryId.value));
    }
    await apiClient.patch(`/api/v1/books/${currentBook.value.id}`, bookData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });

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

    // 상태 배열에 저장된 태그를 사용
    await apiClient.patch(`/api/v1/books/${currentBook.value.id}/complete`, { tags: tags.value });

    alert('책이 성공적으로 발행되었습니다!');
    isSavedOrPublished.value = true;
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
  };

  try {
    const response = await apiClient.post(`/api/v1/books/${currentBook.value.id}/copy`, copyRequest);
    const newBook = response.data.data;
    alert('책이 복사본으로 성공적으로 발행되었습니다!');
    isSavedOrPublished.value = true;
    router.push(`/book-detail/${newBook.bookId}`);
  } catch (error) {
    console.error('복사본 발행 오류:', error);
    alert('복사본 발행에 실패했습니다.');
  }
}


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
  loadOrCreateBook(bookId || null);
  window.addEventListener('beforeunload', handleBeforeUnload);
});

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload);

  if (creationStep.value !== 'setup' && !isSavedOrPublished.value && currentBook.value.id) {
    const bookId = currentBook.value.id;
    const headers = {
      'Authorization': `Bearer ${authStore.accessToken}`,
    };

    try {
      // 1. 모든 에피소드에 대한 삭제 요청을 보냅니다.
      currentBook.value.stories?.forEach(story => {
        if (story.id) {
          const episodeUrl = `${apiClient.defaults.baseURL}/api/v1/books/${bookId}/episodes/${story.id}`;
          fetch(episodeUrl, {
            method: 'DELETE',
            headers,
            keepalive: true,
          });
          console.log(`에피소드(ID: ${story.id}) 삭제 요청을 전송했습니다.`);
        }
      });

      // 2. 책 삭제 요청을 보냅니다.
      const bookUrl = `${apiClient.defaults.baseURL}/api/v1/books/${bookId}`;
      fetch(bookUrl, {
        method: 'DELETE',
        headers,
        keepalive: true,
      });
      console.log(`책(ID: ${bookId}) 삭제 요청을 전송했습니다.`);

    } catch (e) {
      console.error("페이지 이탈 중 삭제 요청 전송 실패:", e);
    }
  }
});

watch(() => currentStory.value?.content, (newContent) => {
  if (isInterviewStarted.value) {
    isContentChanged.value = newContent !== undefined && newContent.trim().length > 0;
    console.log('Content changed, isContentChanged set to:', isContentChanged.value);
  }
});
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
  padding: 60px 5rem 5rem;
  background-color: var(--background-color);
  color: var(--primary-text-color);
  min-height: calc(100vh - 56px);
  font-family: 'Pretendard', sans-serif;
}

.section-title {
  font-family: 'Pretendard', sans-serif;
  font-size: 2.5rem;
  font-weight: 700;
  text-align: center;
  margin-bottom: 0.5rem;
}

.section-subtitle {
  text-align: center;
  font-size: 1.1rem;
  color: #5C4033;
  margin-bottom: 3rem;
}

/* --- General Button Styles --- */
.btn {
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
  font-size: 0.95rem;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.6rem 1.2rem;
}

.btn.btn-primary {
  background-color: #8B4513;
  border: 1px solid #8B4513;
  color: #FFFFFF;
}

.btn-primary:hover {
  background-color: #6a341a;
  border-color: #6a341a;
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
  padding: 0.8rem 1.8rem;
  font-size: 1.1rem;
}

/* --- Title Input Styling --- */
.title-input-highlight {
  background-color: transparent;
  border: none;
  border-bottom: 2px solid #EAE0D5;
  border-radius: 0;
  padding: 0.5rem 0.2rem;
  font-family: 'Noto Serif KR', serif;
  font-size: 1.75rem;
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
  font-size: 1.3rem;
  border-bottom-width: 1px;
}

/* --- Setup / Publish Section --- */
.setup-section,
.publish-section {
  max-width: 800px;
  margin: 0 auto;
  background: var(--surface-color);
  padding: 2.5rem 3rem;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.form-group {
  margin-bottom: 2rem;
}

.form-group label {
  display: block;
  font-weight: 600;
  margin-bottom: 0.75rem;
  font-size: 1rem;
}

.form-control {
  width: 100%;
  padding: 0.8rem 1rem;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 1rem;
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
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
}

.type-selection button {
  background: #f9f9f9;
  border: 2px solid var(--border-color);
  border-radius: 8px;
  padding: 1.5rem 1rem;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
}

.type-selection button i {
  font-size: 2rem;
  color: var(--secondary-text-color);
}

.type-selection button span {
  font-weight: 600;
}

.type-selection button:hover {
  border-color: var(--accent-color);
  background: #fff;
}

.type-selection button.active {
  border-color: var(--accent-color);
  background: #fff8f0;
  color: var(--accent-color);
}

.type-selection button.active i {
  color: var(--accent-color);
}

.genre-toggle {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.genre-toggle button {
  background: #f1f1f1;
  border: 1px solid transparent;
  border-radius: 20px;
  padding: 0.5rem 1rem;
  cursor: pointer;
  transition: all 0.2s;
}

.genre-toggle button:hover {
  background: #e0e0e0;
}

.genre-toggle button.active {
  background-color: #6c757d; /* Darker Gray */
  color: white;

}

.form-actions {
  text-align: center;
  margin-top: 3rem;
  display: flex; /* [추가] 버튼을 옆으로 나열하기 위해 flex 사용 */
  justify-content: center; /* [추가] 중앙 정렬 */
  gap: 1rem; /* [추가] 버튼 사이 간격 */
}

/* --- Workspace Section --- */
.workspace-section {
  position: relative;
}

.workspace-header {
  display: flex;
  align-items: center;
  margin: 1rem 2rem 1rem 1rem;
  gap: 1rem;
}

.book-title-input {
  flex-grow: 1;
}

.workspace-main {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 2rem;
  height: calc(100vh - 220px);
}

.story-list-container {
  background: var(--surface-color);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  padding: 1rem;
  display: flex;
  flex-direction: column;
  font-family: 'Noto Serif KR', serif;
}

.story-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem;
  margin-bottom: 0.5rem;
  border-bottom: 1px solid var(--border-color);
}

.story-list-title {
  font-size: 1rem;
  font-weight: 700;
  color: #5C4033;
  margin: 0;
}

.btn-add-story {
  background: none;
  border: 1px dashed var(--border-color);
  color: var(--secondary-text-color);
  border-radius: 50%;
  cursor: pointer;
  width: 32px;
  height: 32px;
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
  flex-grow: 1;
}

.story-list li {
  padding: 0.8rem 1rem;
  border-radius: 0;
  cursor: pointer;
  font-weight: 500;
  color: #555;
  transition: background-color 0.2s, color 0.2s;
  border-left: 3px solid transparent;
  border-bottom: 1px solid #EAE0D5;
}

.story-list li:last-child {
  border-bottom: none;
}

.story-list li:hover {
  background-color: #f8f6f2;
}

.story-list li.active {
  background-color: #f8f6f2;
  color: var(--primary-text-color);
  font-weight: 700;
  border-left: 3px solid var(--accent-color);
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
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0 0.5rem;
  visibility: hidden;
  opacity: 0;
  transition: all 0.2s;
}

.story-list li:hover .btn-delete-story {
  visibility: visible;
  opacity: 1;
}

.btn-delete-story:hover {
  color: #dc3545;
}

.editor-area {
  display: grid;
  grid-template-columns: 1fr 240px;
  gap: 1.5rem;
  background: var(--paper-color);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.editor-main {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.story-title-input {
  flex-grow: 1;
}

.ai-question-area {
  background: #fafafa;
  padding: 1rem;
  border-radius: 6px;
  /* font-style: italic; */
  color: #000000;
  border: 1px solid var(--border-color);
}

.ai-question-area p i {
  margin-right: 0.5rem;
}

.story-content-wrapper {
  position: relative;
  flex-grow: 1;
}

.story-content-editor {
  flex-grow: 1;
  width: 100%;
  height: 100%;
  padding: 1rem;
  padding-bottom: 2rem;
  /* Make space for counter */
  resize: none;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background: white;
  outline: none;
  font-family: 'Noto Serif KR', serif;
  font-size: 1.1rem;
  line-height: 1.9;
}

.char-counter {
  position: absolute;
  bottom: 10px;
  right: 10px;
  font-size: 0.85rem;
  color: #888;
}

.editor-sidebar {
  background: var(--surface-color);
  padding: 1.5rem 1rem;
  border-left: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.btn-sidebar {
  width: 90%;
  margin: 0 auto;
  text-align: left;
  padding: 0.75rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  transition: all 0.2s;
  font-weight: 500;
  background-color: #fff;
  border: 1px solid #ddd;
  color: #333;
}

.btn-sidebar:hover {
  border-color: var(--accent-color);
  background-color: #f8f6f2;
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
  margin: 3.5rem 0;
  border: none;
  border-top: 1px solid var(--border-color);
}

.audio-visualizer-container {
  margin-top: 1rem;
  height: 8px;
  background: #EAE0D5;
  border-radius: 4px;
  overflow: hidden;
}

.audio-visualizer-container canvas {
  width: 100%;
  height: 100%;
}

.correction-panel {
  border: 1px solid #b8e0b8;
  background: #f0fff0;
  padding: 1rem;
  border-radius: 6px;
}

.correction-panel h4 {
  margin: 0 0 0.5rem 0;
}

.correction-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

/* --- Publish Section Specifics --- */
.publish-section .form-control {
  border: 1px solid #ccc;
}

.publish-section .title-input-highlight {
  background-color: #fff;
  border: 1px solid #ccc;
  font-family: 'Pretendard', sans-serif;
  font-weight: 400; /* Normal weight */
}

.publish-header {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  margin-bottom: 0.5rem;
}

.publish-header .section-title {
  margin-bottom: 0;
}

.btn-back {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: 1px solid var(--border-color);
  color: var(--secondary-text-color);
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.btn-back:hover {
  background-color: #f8f9fa;
}

.cover-selection {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 1rem;
}

.cover-option {
  border: 3px solid transparent;
  border-radius: 6px;
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
  font-size: 3rem;
  margin-bottom: 1rem;
}

.editor-title-wrapper {
  display: flex;
  align-items: center;
  gap: 1rem;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 1rem;
}

.editor-title-label {
  font-family: 'Noto Serif KR', serif;
  font-weight: 600;
  font-size: 1.5rem;
  white-space: nowrap;
  color: #414141;
}

/* --- Tag Input Styles --- */
.tag-container {
  /* border: 1px solid #ccc; */ /* 외곽선 제거 */
  border-radius: 6px;
  padding: 0.5rem;
  padding-bottom: 0; /* 아래쪽 패딩 제거 */
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  background-color: #e9ecef;
  color: #495057;
  border-radius: 16px;
  padding: 0.3rem 0.8rem;
  font-size: 0.9rem;
  font-weight: 500;
}

.btn-remove-tag {
  background: none;
  border: none;
  color: #868e96;
  margin-left: 0.5rem;
  cursor: pointer;
  font-size: 1.2rem;
  line-height: 1;
  padding: 0;
}

.btn-remove-tag:hover {
  color: #343a40;
}

.tag-container .form-control {
  border: 1px solid #ccc; /* 입력란에만 외곽선 추가 */
  box-shadow: none;
  padding-left: 0.8rem; /* 패딩 조정 */
  margin-top: 0.5rem; /* 위쪽 태그 목록과의 간격 */
}

.tag-container .form-control:focus {
  box-shadow: none;
}
</style>
