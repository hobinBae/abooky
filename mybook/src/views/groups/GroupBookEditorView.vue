<template>
  <div class="groupbook-editor-page">
    <CustomAlert ref="customAlertRef" />

    <input type="file" ref="episodeImageInput" @change="handleEpisodeImageUpload" accept="image/*" style="display: none;">

    <!-- 발행 섹션 -->
    <section v-if="creationStep === 'publishing'" class="publish-section">
      <div class="publish-header">
        <h2 class="section-title">그룹책 발행하기</h2>
      </div>
      <p class="section-subtitle">마지막으로 그룹책의 정보를 확인하고 발행해주세요.</p>
      <div class="publish-form">
        <div class="form-group">
          <label for="final-group-book-title">제목 최종 수정</label>
          <input id="final-group-book-title" type="text" v-model="currentGroupBook.title"
            class="form-control title-input-highlight" placeholder="그룹책 제목을 입력해주세요">
        </div>
        <div class="form-group">
          <label for="final-group-book-summary">그룹책 소개</label>
          <textarea id="final-group-book-summary" v-model="currentGroupBook.summary"
            class="form-control" rows="5" placeholder="그룹책 소개를 입력해주세요"></textarea>
        </div>
        <div class="form-group">
          <label for="group-book-tags">태그</label>
          <div class="tag-container">
            <div class="tag-list">
              <span v-for="(tag, index) in groupBookTags" :key="index" class="tag-item">
                {{ tag }}
                <button @click="removeTag(index)" class="btn-remove-tag">×</button>
              </span>
            </div>
            <input id="group-book-tags" type="text" v-model="tagInput" @keyup.enter.prevent="addTag"
              placeholder="태그 입력 후 Enter" class="form-control" :disabled="groupBookTags.length >= 5">
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
          <button @click="closePublishModal" class="btn btn-primary btn-lg">
            <i class="bi bi-arrow-left"></i> 뒤로가기
          </button>
          <button @click="confirmPublish" class="btn btn-primary btn-lg">
            그룹책 발행하기 <i class="bi bi-check-circle"></i>
          </button>
        </div>
      </div>
    </section>

    <section v-if="creationStep === 'setup'" class="setup-section">
      <h2 class="section-title">새로운 그룹책 만들기</h2>
      <p class="section-subtitle">그룹의 추억을 함께 기록하기 위한 기본 정보를 입력해주세요.</p>

      <div class="setup-form">
        <div class="form-group">
          <label for="book-title">책 제목</label>
          <input id="book-title" type="text" v-model="currentGroupBook.title" placeholder="그룹의 추억을 담을 제목을 지어주세요."
            class="form-control">
        </div>
        <div class="form-group">
          <label for="book-summary">책 소개</label>
          <textarea id="book-summary" v-model="currentGroupBook.summary" placeholder="이 그룹책에 어떤 추억들을 기록할지 간단히 소개해보세요."
            class="form-control" rows="4"></textarea>
        </div>
        <div class="form-group">
          <label>그룹 타입 선택</label>
          <div class="type-selection">
            <button v-for="groupType in groupTypes" :key="groupType.id" @click="currentGroupBook.groupType = groupType.id"
              :class="{ active: currentGroupBook.groupType === groupType.id }">
              <i :class="groupType.icon"></i>
              <span>{{ groupType.name }}</span>
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
        <span class="editor-title-label"> 책 제목 </span>
        <input type="text" v-model="currentGroupBook.title" class="book-title-input title-input-highlight">
      </div>

      <div class="workspace-main">
        <div class="left-sidebar-content">
          <div class="story-list-container">
            <div class="story-list-header">
              <h3 class="story-list-title">목차</h3>
              <button @click="addEpisode" class="btn-add-episode" title="이야기 추가"><i class="bi bi-plus-lg"></i></button>
            </div>
            <ul class="story-list">
              <li v-for="(episode, index) in paginatedEpisodes" :key="episode.id ?? ('tmp-' + index)"
                @click="selectEpisode((episodesCurrentPage - 1) * episodesPerPage + index)"
                :class="{ active: ((episodesCurrentPage - 1) * episodesPerPage + index) === currentEpisodeIndex }">
                <span>{{ episode.title }}</span>
                <!-- <span class="template-badge" :class="episode.template?.toLowerCase()">{{ getTemplateKoreanName(episode.template) }}</span> -->
                <button @click.stop="deleteEpisode(episode, (episodesCurrentPage - 1) * episodesPerPage + index)"
                  class="btn-delete-episode">×</button>
              </li>
            </ul>
            <div v-if="totalEpisodePages > 1" class="story-list-pagination">
              <button @click="prevEpisodePage" :disabled="episodesCurrentPage === 1" class="btn-pagination">&lt;</button>
              <span>{{ episodesCurrentPage }} / {{ totalEpisodePages }}</span>
              <button @click="nextEpisodePage" :disabled="episodesCurrentPage === totalEpisodePages"
                class="btn-pagination">&gt;</button>
            </div>
          </div>

          <div class="story-image-preview-container">
            <div v-if="currentEpisode?.imageUrl" class="image-preview-box">
              <button @click="removeEpisodeImage" class="btn-remove-image" title="사진 삭제">×</button>
              <img :src="currentEpisode.imageUrl" alt="이야기 사진 미리보기">
            </div>
            <div v-else class="image-preview-placeholder">
              <i class="bi bi-card-image"></i>
              <span>이야기에 첨부된 사진이 없습니다.</span>
            </div>
          </div>
        </div>

        <div class="editor-area" v-if="currentEpisode">
          <div class="editor-main">
            <div class="editor-title-wrapper">
              <span class="editor-title-label">이야기 제목</span>
              <input type="text" v-model="currentEpisode.title" placeholder="이야기 제목"
                class="story-title-input title-input-highlight">
            </div>
            <div class="ai-question-area">
              <p v-if="isInterviewStarted"><i class="bi bi-robot"></i> {{ aiQuestion }}</p>
              <p v-else><i class="bi bi-robot"></i>AI 인터뷰 시작을 누르고 질문을 받아보세요.</p>
            </div>
            <div class="story-content-wrapper">
              <textarea v-model="currentEpisode.content" class="story-content-editor"
                placeholder="이곳에 답변을 적어보세요. AI가 질문에 따라 내용을 정리해드립니다." maxlength="5000"></textarea>
              <div class="char-counter">
                {{ currentEpisode.content.length }} / 5000
              </div>
            </div>
            <div v-if="isTemplateCompleted" class="correction-panel">
              <div class="correction-actions">
                <i class="bi bi-check-circle-fill text-success"></i>
                <h4>" {{ getTemplateKoreanName(currentTemplate) }} " 주제 완료!</h4>
              </div>
              <p class="correction-actions">이 주제의 모든 질문이 완료되었습니다. 이야기를 저장하고 다음 단계로 진행해보세요.</p>
              <div v-if="showNextEpisodeGuide" class="next-episode-guide">
                <p class="guide-text">다음 주제로 진행할 준비가 되었습니다!</p>
                <button @click="createNextEpisode" class="btn btn-primary">
                  <i class="bi bi-plus-circle"></i> 새 이야기 생성
                </button>
              </div>
            </div>
          </div>
          <div class="editor-sidebar">
            <button @click="startAiInterview" class="btn-sidebar">
              <i class="bi bi-mic"></i>
              <span>AI 인터뷰 시작</span>
            </button>
            <button @click="submitAnswer" :disabled="!isInterviewStarted || !isAnswerReady" class="btn-sidebar">
              <i class="bi bi-check-circle"></i>
              <span>답변 제출</span>
            </button>
            <button @click="requestNextQuestion" :disabled="!isInterviewStarted" class="btn-sidebar">
              <i class="bi bi-skip-end-circle"></i>
              <span>다음 질문 요청</span>
            </button>
            <button @click="saveEpisode" class="btn-sidebar">
              <i class="bi bi-save"></i>
              <span>이야기 저장</span>
            </button>
            <button @click="triggerImageUpload" class="btn-sidebar">
              <i class="bi bi-image"></i>
              <span>사진 첨부</span>
            </button>
            <div class="sidebar-action-group">
              <button @click="endSession" class="btn-sidebar btn-primary-sidebar">
                <i class="bi bi-door-open"></i>
                <span> 나가기 </span>
              </button>
              <button @click="publishGroupBook" class="btn-sidebar btn-primary-sidebar">
                <i class="bi bi-send"></i>
                <span>발행하기</span>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onBeforeUnmount } from 'vue';
import { useRouter, useRoute, onBeforeRouteLeave } from 'vue-router';
import CustomAlert from '@/components/common/CustomAlert.vue';
import { groupBookService } from '@/services/groupBookService';

// --- 인터페이스 정의 ---
interface Episode {
  id?: number;
  title: string;
  content: string;
  template?: string;
  imageUrl?: string;
  imageId?: number;
}

interface GroupBook {
  id?: number;
  groupId: number;
  title: string;
  summary: string;
  groupType: string;
  episodes: Episode[];
  createdAt?: Date;
  updatedAt?: Date;
}

interface QuestionEventData {
  text: string;
  currentChapter?: string;
  currentStage?: string;
  isLastQuestion?: boolean;
  isTemplateCompleted?: boolean;
}

interface ApiError {
  response?: {
    data?: {
      message?: string;
      details?: string[];
    };
    status?: number;
  };
  message?: string;
}

// --- 정적 데이터 ---
const groupTypes = [
  { id: 'FAMILY', name: '가족', icon: 'bi bi-house-heart' },
  { id: 'FRIENDS', name: '친구들', icon: 'bi bi-people' },
  { id: 'COUPLE', name: '커플', icon: 'bi bi-heart' },
  { id: 'TEAM', name: '팀', icon: 'bi bi-briefcase' },
  { id: 'OTHER', name: '기타', icon: 'bi bi-collection' }
];

const coverOptions = ['https://ssafytrip.s3.ap-northeast-2.amazonaws.com/book/default_1.jpg', 'https://ssafytrip.s3.ap-northeast-2.amazonaws.com/book/default_2.jpg', 'https://ssafytrip.s3.ap-northeast-2.amazonaws.com/book/default_3.jpg', 'https://ssafytrip.s3.ap-northeast-2.amazonaws.com/book/default_4.jpg', 'https://ssafytrip.s3.ap-northeast-2.amazonaws.com/book/default_5.jpg',];


// --- 라우터 및 상태 ---
const router = useRouter();
const route = useRoute();
const customAlertRef = ref<InstanceType<typeof CustomAlert> | null>(null);

// --- 컴포넌트 상태 ---
const creationStep = ref<'setup' | 'editing' | 'publishing'>('setup');
const currentGroupBook = ref<Partial<GroupBook>>({
  title: '',
  summary: '',
  groupType: 'FAMILY',
  episodes: []
});

const currentEpisodeIndex = ref(-1);
const aiQuestion = ref('AI 인터뷰 시작을 누르고 질문을 받아보세요.');
const isInterviewStarted = ref(false);
const isSavedOrPublished = ref(false);
const currentTemplate = ref('INTRO');

// 템플릿 완료 관련 상태
const isTemplateCompleted = ref(false);
const allAnswersText = ref('');
const showNextEpisodeGuide = ref(false);

// 임시 답변 저장
const tempAnswers = ref<string[]>([]);

// SSE 관련 상태
const currentSessionId = ref<string | null>(null);
let eventSource: EventSource | null = null;
const isConnecting = ref(false);
const isConnected = ref(false);

const selectedCover = ref(coverOptions[0]);
const uploadedCoverFile = ref<File | null>(null);
const episodeImageInput = ref<HTMLInputElement | null>(null);

// --- 발행 관련 상태 ---
const groupBookTags = ref<string[]>([]);
const tagInput = ref('');

// --- 페이지네이션 상태 ---
const episodesCurrentPage = ref(1);
const episodesPerPage = 5;

// --- 계산된 속성 ---
const currentEpisode = computed(() => {
  if (currentGroupBook.value.episodes && currentEpisodeIndex.value > -1 && currentGroupBook.value.episodes[currentEpisodeIndex.value]) {
    return currentGroupBook.value.episodes[currentEpisodeIndex.value];
  }
  return null;
});

const totalEpisodePages = computed(() => {
  const totalEpisodes = currentGroupBook.value.episodes?.length || 0;
  if (totalEpisodes === 0) return 1;
  return Math.ceil(totalEpisodes / episodesPerPage);
});

const paginatedEpisodes = computed(() => {
  const episodes = currentGroupBook.value.episodes || [];
  const start = (episodesCurrentPage.value - 1) * episodesPerPage;
  const end = start + episodesPerPage;
  return episodes.slice(start, end);
});

const isAnswerReady = computed(() => {
  return (currentEpisode.value?.content ?? '').trim().length > 0;
});

// --- 함수 ---
function getTemplateKoreanName(template?: string): string {
  const templateNames: Record<string, string> = {
    'INTRO': '소개',
    'STORY': '이야기',
    'REFLECTION': '회상',
    'FUTURE': '미래'
  };
  return templateNames[template || 'INTRO'] || '소개';
}

async function moveToEditingStep() {
  if (!currentGroupBook.value.title) {
    customAlertRef.value?.showAlert({
      title: '입력 필요',
      message: '그룹북 제목을 입력해주세요.'
    });
    return;
  }

  const groupId = parseInt((route.query.groupId || route.params.groupId) as string);
  if (!groupId || isNaN(groupId)) {
    customAlertRef.value?.showAlert({
      title: '오류 발생',
      message: '그룹 정보를 찾을 수 없습니다.'
    });
    return;
  }

  if (!currentGroupBook.value.groupType) {
    customAlertRef.value?.showAlert({ title: '입력 필요', message: '그룹 타입을 선택해주세요.' });
    return;
  }

  const groupBookData = {
    title: currentGroupBook.value.title?.trim() || '',
    summary: currentGroupBook.value.summary?.trim() || '',
    groupType: currentGroupBook.value.groupType
  };

  try {
    const response = await groupBookService.createGroupBook(groupId, groupBookData);
    if (response) {
      currentGroupBook.value.id = response.groupBookId;
      currentGroupBook.value.groupId = groupId;
      currentGroupBook.value.episodes = (response.episodes || []).map(e => ({
        id: e.groupEpisodeId,
        groupEpisodeId: e.groupEpisodeId,
        title: e.title,
        content: e.editedContent || '',
        template: e.template,
        imageUrl: e.imageUrl,
        imageId: e.imageId
      }));
      creationStep.value = 'editing';
    }
  } catch (error) {
    const err = error as ApiError;
    console.error('그룹북 생성 오류:', error);
    console.error('에러 데이터:', err.response?.data);
    console.error('전송한 데이터:', groupBookData);
    console.error('validation details:', err.response?.data?.details);

    const errorMessage = err.response?.data?.details?.[0] || err.response?.data?.message || '그룹북 생성에 실패했습니다.';
    customAlertRef.value?.showAlert({
      title: '오류 발생',
      message: errorMessage
    });
  }
}

async function loadGroupBookForEditing(groupId: number, groupBookId: number) {
  try {
    const groupBookData = await groupBookService.getGroupBook(groupId, groupBookId);
    if (groupBookData) {
      currentGroupBook.value = {
        id: groupBookData.groupBookId,
        groupId: groupId,
        title: groupBookData.title,
        summary: groupBookData.summary,
        groupType: groupBookData.groupType,
        episodes: groupBookData.episodes?.map(e => ({
          id: e.groupEpisodeId,
          groupEpisodeId: e.groupEpisodeId,
          title: e.title,
          content: e.editedContent || '',
          template: e.template,
          imageUrl: e.imageUrl,
          imageId: e.imageId
        })) || []
      };
      creationStep.value = 'editing';

      if (currentGroupBook.value.episodes && currentGroupBook.value.episodes.length > 0) {
        await selectEpisode(0);
      }
    }
  } catch (error) {
    console.error('그룹북 정보를 불러오는데 실패했습니다:', error);
    customAlertRef.value?.showAlert({
      title: '오류 발생',
      message: '그룹북 정보를 불러오는데 실패했습니다.'
    });
    router.back();
  }
}

async function addEpisode() {
  if (!currentGroupBook.value?.id || !currentGroupBook.value?.groupId) return;

  try {
    const episodeData = {
      title: `${(currentGroupBook.value.episodes?.length || 0) + 1}번째 이야기`,
      summary: '',
      orderNo: (currentGroupBook.value.episodes?.length || 0) + 1
    };

    const response = await groupBookService.createEpisode(
      currentGroupBook.value.groupId,
      currentGroupBook.value.id,
      episodeData
    );

    if (response) {
      console.log('에피소드 생성 응답:', response);
      console.log('응답 구조:', Object.keys(response));
      console.log('groupEpisodeId:', response.groupEpisodeId);
      console.log('id 필드:', response.id);

      const newEpisode: Episode = {
        id: response.id,
        title: response.title,
        content: '',
        template: response.template
      };

      currentGroupBook.value.episodes = [...(currentGroupBook.value.episodes || []), newEpisode];
      currentEpisodeIndex.value = (currentGroupBook.value.episodes?.length || 1) - 1;

      console.log('현재 에피소드 인덱스:', currentEpisodeIndex.value);
      console.log('현재 에피소드:', currentEpisode.value);
    }
  } catch (error) {
    console.error('에피소드 추가 오류:', error);
    customAlertRef.value?.showAlert({
      title: '추가 오류',
      message: '새로운 에피소드를 추가하는데 실패했습니다.'
    });
  }
}

async function selectEpisode(index: number) {
  // 기존 SSE 연결 정리
  await cleanupSseConnection();

  currentEpisodeIndex.value = index;
  const episode = currentGroupBook.value.episodes?.[index];

  if (episode && !episode.imageUrl && episode.id) {
    await fetchEpisodeImages(episode.id);
  }

  // 상태 초기화
  currentSessionId.value = null;
  isInterviewStarted.value = false;
  aiQuestion.value = 'AI 인터뷰 시작을 누르고 질문을 받아보세요.';
  currentTemplate.value = episode?.template || 'INTRO';

  // 임시 답변 초기화
  tempAnswers.value = [];
}

async function fetchEpisodeImages(episodeId: number) {
  if (!currentGroupBook.value?.id || !currentGroupBook.value?.groupId) return;

  try {
    const images = await groupBookService.getEpisodeImages(
      currentGroupBook.value.groupId,
      currentGroupBook.value.id,
      episodeId
    );

    if (images && images.length > 0) {
      const episode = currentGroupBook.value.episodes?.find(e => e.id === episodeId);
      if (episode) {
        episode.imageUrl = images[0].imageUrl;
        episode.imageId = images[0].imageId;
      }
    }
  } catch (error) {
    console.error(`에피소드 이미지 정보를 불러오는데 실패했습니다.`, error);
  }
}

async function deleteEpisode(episode: Episode, index: number) {
  if (!confirm(`'${episode.title}' 에피소드를 삭제하시겠습니까?`)) return;
  if (!currentGroupBook.value?.groupId || !currentGroupBook.value?.id || !episode.id) return;

  try {
    await groupBookService.deleteEpisode(
      currentGroupBook.value.groupId,
      currentGroupBook.value.id,
      episode.id
    );

    currentGroupBook.value.episodes?.splice(index, 1);

    if (episodesCurrentPage.value > 1 && paginatedEpisodes.value.length === 0) {
      episodesCurrentPage.value--;
    }

    if (currentEpisodeIndex.value === index) {
      currentEpisodeIndex.value = -1;
    } else if (currentEpisodeIndex.value > index) {
      currentEpisodeIndex.value--;
    }

    customAlertRef.value?.showAlert({
      title: '삭제 완료',
      message: '에피소드가 삭제되었습니다.'
    });
  } catch (error) {
    console.error('에피소드 삭제 오류:', error);
    customAlertRef.value?.showAlert({
      title: '삭제 오류',
      message: '에피소드 삭제에 실패했습니다.'
    });
  }
}

async function startAiInterview() {
  console.log('AI 인터뷰 시작 시도');
  console.log('currentGroupBook:', currentGroupBook.value);
  console.log('currentEpisode:', currentEpisode.value);
  console.log('검증 조건:', {
    groupId: currentGroupBook.value?.groupId,
    bookId: currentGroupBook.value?.id,
    episodeId: currentEpisode.value?.id
  });

  if (!currentGroupBook.value?.groupId || !currentGroupBook.value?.id || !currentEpisode.value?.id) {
    customAlertRef.value?.showAlert({
      title: '정보 오류',
      message: '그룹북 또는 에피소드 정보가 올바르지 않습니다.'
    });
    return;
  }

  if (isConnecting.value || isConnected.value || isInterviewStarted.value) {
    console.log('이미 AI 인터뷰가 진행 중이거나 연결 중입니다.');
    return;
  }

  try {
    const sessionId = await groupBookService.startConversation(
      currentGroupBook.value.groupId,
      currentGroupBook.value.id,
      currentEpisode.value.id
    );

    currentSessionId.value = sessionId;
    isInterviewStarted.value = true;
    aiQuestion.value = 'AI 인터뷰 세션에 연결 중... 첫 질문을 기다립니다.';

    await connectToSseStream();
  } catch (error) {
    console.error('세션 시작 실패:', error);
    customAlertRef.value?.showAlert({
      title: '세션 오류',
      message: 'AI 인터뷰 세션 시작에 실패했습니다.'
    });
    isInterviewStarted.value = false;
    currentSessionId.value = null;
  }
}

async function connectToSseStream() {
  if (!currentSessionId.value || !currentGroupBook.value?.groupId ||
      !currentGroupBook.value?.id || !currentEpisode.value?.id) {
    console.warn('세션 정보가 없어 SSE 연결을 할 수 없습니다.');
    return;
  }

  if (isConnecting.value || isConnected.value) {
    console.log('이미 SSE 연결 중이거나 연결되어 있습니다.');
    return;
  }

  isConnecting.value = true;

  try {
    const sseEmitter = await groupBookService.establishConversationStream(
      currentGroupBook.value.groupId,
      currentGroupBook.value.id,
      currentEpisode.value.id,
      currentSessionId.value
    );

    eventSource = sseEmitter;

    eventSource.onopen = () => {
      console.log('SSE 연결 성공');
      isConnecting.value = false;
      isConnected.value = true;
    };

    eventSource.addEventListener('connected', (event: MessageEvent) => {
      console.log('SSE 연결 확인:', event.data);
    });

    eventSource.addEventListener('question', (event: MessageEvent) => {
      try {
        const questionData = JSON.parse(event.data) as QuestionEventData;
        aiQuestion.value = questionData.text || '';

        if (questionData.currentChapter) {
          currentTemplate.value = questionData.currentChapter;
        }

        if (questionData.isLastQuestion) {
          isInterviewStarted.value = false;
        }

        // 템플릿 완료 처리
        if (questionData.isTemplateCompleted) {
          handleTemplateCompleted();
        }
      } catch (e) {
        console.error('Error parsing question data:', e);
        aiQuestion.value = event.data;
      }
    });

    eventSource.addEventListener('answer-saved', () => {
      customAlertRef.value?.showAlert({
        title: '저장 완료',
        message: '답변이 AI에 의해 교정되어 저장되었습니다.'
      });

      if (currentEpisode.value) {
        currentEpisode.value.content = '';
      }
    });

    eventSource.onerror = async (error) => {
      console.error('SSE 에러:', error);
      aiQuestion.value = '인터뷰 서버와 연결이 끊겼습니다.';
      await cleanupSseConnection();
    };

  } catch (error) {
    console.error('SSE 연결 실패:', error);
    aiQuestion.value = 'AI 인터뷰 서버 연결에 실패했습니다.';
    await cleanupSseConnection();
  }
}

async function submitAnswer() {
  if (!isInterviewStarted.value || !currentEpisode.value?.content.trim()) {
    customAlertRef.value?.showAlert({
      title: '답변 오류',
      message: '답변 내용을 입력해주세요.'
    });
    return;
  }

  // 현재 답변을 임시 답변 목록에 추가
  tempAnswers.value.push(currentEpisode.value.content.trim());

  // 입력창 초기화 (새로운 답변 입력을 위해)
  currentEpisode.value.content = '';

  customAlertRef.value?.showAlert({
    title: '답변 임시 저장',
    message: '답변이 임시 저장되었습니다. 다음 질문을 진행하세요.'
  });

  console.log('임시 저장된 답변들:', tempAnswers.value);
}

async function requestNextQuestion() {
  if (!isInterviewStarted.value || !currentSessionId.value) return;
  if (!currentGroupBook.value?.groupId || !currentGroupBook.value?.id || !currentEpisode.value?.id) return;

  try {
    await groupBookService.getNextQuestion(
      currentGroupBook.value.groupId,
      currentGroupBook.value.id,
      currentEpisode.value.id,
      currentSessionId.value
    );
  } catch (error) {
    console.error('다음 질문 요청 실패:', error);
    customAlertRef.value?.showAlert({
      title: '요청 오류',
      message: '다음 질문을 가져오는데 실패했습니다.'
    });
  }
}

async function saveEpisode() {
  if (!currentEpisode.value?.id || !currentGroupBook.value?.groupId || !currentGroupBook.value?.id) {
    console.error('저장을 위한 필수 정보가 없습니다:', {
      episodeId: currentEpisode.value?.id,
      groupId: currentGroupBook.value?.groupId,
      bookId: currentGroupBook.value?.id
    });
    customAlertRef.value?.showAlert({
      title: '저장 오류',
      message: '에피소드 정보가 올바르지 않습니다.'
    });
    return;
  }

  console.log('에피소드 저장 시도:', {
    groupId: currentGroupBook.value.groupId,
    bookId: currentGroupBook.value.id,
    episodeId: currentEpisode.value.id,
    title: currentEpisode.value.title,
    contentLength: currentEpisode.value.content?.length || 0,
    tempAnswersCount: tempAnswers.value.length,
    hasSession: !!currentSessionId.value
  });

  try {
    // 모든 답변 내용을 수집 (임시 답변 + 현재 입력 내용)
    const allContent = [];
    if (tempAnswers.value.length > 0) {
      allContent.push(...tempAnswers.value);
    }
    if (currentEpisode.value.content?.trim()) {
      allContent.push(currentEpisode.value.content.trim());
    }

    const finalContent = allContent.join('\n\n');
    console.log('최종 저장할 내용:', finalContent);

    // 에피소드 내용 업데이트 (임시 답변들을 모두 포함)
    console.log('에피소드 내용 업데이트 중...');

    const updateData = {
      title: currentEpisode.value.title,
      editedContent: finalContent
    };

    console.log('업데이트 데이터:', updateData);

    const updatedEpisode = await groupBookService.updateEpisode(
      currentGroupBook.value.groupId,
      currentGroupBook.value.id,
      currentEpisode.value.id,
      updateData
    );

    console.log('에피소드 업데이트 응답:', updatedEpisode);

    // 저장 성공 후 임시 답변들 초기화
    tempAnswers.value = [];
    console.log('임시 답변 목록 초기화 완료');

    customAlertRef.value?.showAlert({
      title: '저장 완료',
      message: '에피소드가 저장되었습니다.'
    });

    // 템플릿이 완료된 상태라면 다음 에피소드 가이드 표시
    if (isTemplateCompleted.value) {
      showNextEpisodeGuide.value = true;
    }
  } catch (error) {
    const err = error as ApiError;
    console.error('에피소드 저장 실패:', error);
    console.error('에러 상세:', {
      message: err.message,
      response: err.response?.data,
      status: err.response?.status
    });

    const errorMessage = err.response?.data?.message || err.message || '에피소드 저장에 실패했습니다.';
    customAlertRef.value?.showAlert({
      title: '저장 오류',
      message: errorMessage
    });
  }
}

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

async function publishGroupBook() {
  if (!currentGroupBook.value?.id || !currentGroupBook.value?.groupId) {
    customAlertRef.value?.showAlert({
      title: '오류',
      message: '그룹북 정보를 찾을 수 없습니다.'
    });
    return;
  }

  // 현재 에피소드 저장
  if (currentEpisode.value?.id) {
    try {
      await saveEpisode();
    } catch (error) {
      console.error('에피소드 저장 실패:', error);
    }
  }

  // 발행 단계로 이동
  creationStep.value = 'publishing';
}

function closePublishModal() {
  creationStep.value = 'editing';
  tagInput.value = '';
}

function addTag() {
  const newTag = tagInput.value.trim();
  if (newTag && !groupBookTags.value.includes(newTag) && groupBookTags.value.length < 5) {
    groupBookTags.value.push(newTag);
    tagInput.value = '';
  } else if (groupBookTags.value.length >= 5) {
    customAlertRef.value?.showAlert({
      title: '태그 제한',
      message: '태그는 최대 5개까지 추가할 수 있습니다.'
    });
  }
}

function removeTag(index: number) {
  groupBookTags.value.splice(index, 1);
}

async function confirmPublish() {
  if (!currentGroupBook.value?.id || !currentGroupBook.value?.groupId) {
    return;
  }

  // 제목 유효성 검사
  if (!currentGroupBook.value.title?.trim()) {
    customAlertRef.value?.showAlert({
      title: '입력 필요',
      message: '그룹북 제목을 입력해주세요.'
    });
    return;
  }

  try {
    // 그룹북 기본 정보 업데이트 (표지 이미지 포함)
    try {
      const formData = new FormData();
      formData.append('title', currentGroupBook.value.title);
      formData.append('summary', currentGroupBook.value.summary || '');

      // 표지 이미지가 있는 경우에만 추가
      if (uploadedCoverFile.value) {
        formData.append('file', uploadedCoverFile.value);
      } else if (selectedCover.value) {
        formData.append('coverImageUrl', selectedCover.value);
      }

      console.log('그룹북 업데이트 요청:', {
        title: currentGroupBook.value.title,
        summary: currentGroupBook.value.summary,
        hasFile: !!uploadedCoverFile.value
      });

      await groupBookService.updateGroupBook(
        currentGroupBook.value.groupId,
        currentGroupBook.value.id,
        formData
      );

      console.log('그룹북 정보 업데이트 완료');
    } catch (updateError) {
      console.error('그룹북 정보 업데이트 실패:', updateError);
      customAlertRef.value?.showAlert({
        title: '업데이트 실패',
        message: '그룹북 정보 업데이트 중 오류가 발생했습니다.'
      });
      return;
    }

    // 그룹북 발행 API 호출
    console.log('그룹북 발행 시도...', {
      groupId: currentGroupBook.value.groupId,
      bookId: currentGroupBook.value.id,
      tags: groupBookTags.value
    });

    await groupBookService.completeGroupBook(
      currentGroupBook.value.groupId,
      currentGroupBook.value.id,
      { tags: groupBookTags.value }
    );

    customAlertRef.value?.showAlert({
      title: '발행 완료',
      message: '그룹북이 성공적으로 발행되었습니다!'
    });

    isSavedOrPublished.value = true;
    closePublishModal();
    await cleanupBeforeLeave();

    // 그룹책 상세 페이지로 이동
    router.push(`/group-book-detail/${currentGroupBook.value.groupId}/${currentGroupBook.value.id}`);
  } catch (error) {
    console.error('그룹북 발행 실패:', error);
    customAlertRef.value?.showAlert({
      title: '발행 실패',
      message: '그룹북 발행 중 오류가 발생했습니다.'
    });
  }
}

async function endSession() {
  if (confirm('세션을 종료하고 그룹으로 돌아가시겠습니까?')) {
    isSavedOrPublished.value = true;
    await cleanupBeforeLeave();
    router.push(`/group/${currentGroupBook.value?.groupId}`);
  }
}

// SSE 연결 정리 함수
async function cleanupSseConnection() {
  console.log('SSE 연결 정리 시작...');

  // 1. 백엔드에 SSE 스트림 종료 요청
  if (currentSessionId.value && currentGroupBook.value?.groupId &&
      currentGroupBook.value?.id && currentEpisode.value?.id) {
    try {
      await groupBookService.closeSseStream(
        currentGroupBook.value.groupId,
        currentGroupBook.value.id,
        currentEpisode.value.id,
        currentSessionId.value
      );
      console.log('백엔드 SSE 스트림 종료 성공');
    } catch (e) {
      console.error('백엔드 SSE 연결 종료 실패:', e);
    }
  }

  // 2. 프론트엔드 EventSource 정리
  if (eventSource) {
    try {
      eventSource.close();
      console.log('EventSource 정리 완료');
    } catch (e) {
      console.error('EventSource 정리 실패:', e);
    }
    eventSource = null;
  }

  // 3. 상태 초기화
  isConnected.value = false;
  isConnecting.value = false;
  currentSessionId.value = null;
  isInterviewStarted.value = false;

  console.log('SSE 연결 정리 완료');
}

async function cleanupBeforeLeave() {
  console.log('페이지 이탈 전 상태 정리 시작...');

  // SSE 정리
  await cleanupSseConnection();

  // 기타 상태 정리
  aiQuestion.value = 'AI 인터뷰 시작을 누르고 질문을 받아보세요.';

  console.log('페이지 이탈 전 상태 정리 완료');
}

// 페이지네이션 함수들
function prevEpisodePage() {
  if (episodesCurrentPage.value > 1) {
    episodesCurrentPage.value--;
  }
}

function nextEpisodePage() {
  if (episodesCurrentPage.value < totalEpisodePages.value) {
    episodesCurrentPage.value++;
  }
}

// 이미지 관련 함수들
function triggerImageUpload() {
  if (!currentEpisode.value) {
    customAlertRef.value?.showAlert({
      title: '선택 오류',
      message: '먼저 이미지를 추가할 에피소드를 선택해주세요.'
    });
    return;
  }
  episodeImageInput.value?.click();
}

async function handleEpisodeImageUpload(event: Event) {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files[0] && currentEpisode.value &&
      currentGroupBook.value?.groupId && currentGroupBook.value?.id) {
    const file = target.files[0];

    console.log('에피소드 이미지 업로드 시작:', {
      fileName: file.name,
      fileSize: file.size,
      fileType: file.type,
      episodeId: currentEpisode.value.id,
      groupId: currentGroupBook.value.groupId,
      bookId: currentGroupBook.value.id
    });

    try {
      const response = await groupBookService.uploadEpisodeImage(
        currentGroupBook.value.groupId,
        currentGroupBook.value.id,
        currentEpisode.value.id!,
        file
      );

      console.log('에피소드 이미지 업로드 응답:', response);

      if (currentEpisode.value && response) {
        currentEpisode.value.imageUrl = response.imageUrl;
        currentEpisode.value.imageId = response.imageId;

        // 그룹북의 에피소드 목록에서도 해당 에피소드의 이미지 정보 업데이트
        if (currentGroupBook.value?.episodes) {
          const episodeIndex = currentGroupBook.value.episodes.findIndex(e => e.id === currentEpisode.value?.id);
          if (episodeIndex !== -1) {
            currentGroupBook.value.episodes[episodeIndex].imageUrl = response.imageUrl;
            currentGroupBook.value.episodes[episodeIndex].imageId = response.imageId;
          }
        }

        console.log('에피소드 이미지 상태 업데이트 완료:', {
          imageUrl: response.imageUrl,
          imageId: response.imageId
        });
      }

      customAlertRef.value?.showAlert({
        title: '업로드 완료',
        message: '이미지가 성공적으로 첨부되었습니다.'
      });

      // 파일 입력 초기화 (같은 파일을 다시 업로드할 수 있도록)
      if (episodeImageInput.value) {
        episodeImageInput.value.value = '';
      }
    } catch (error) {
      const err = error as ApiError;
      console.error('이미지 업로드 실패:', error);
      console.error('에러 상세:', {
        message: err.message,
        response: err.response?.data,
        status: err.response?.status
      });
      customAlertRef.value?.showAlert({
        title: '업로드 실패',
        message: `이미지 업로드 중 오류가 발생했습니다: ${err.response?.data?.message || err.message}`
      });
    }
  } else {
    console.warn('에피소드 이미지 업로드 조건 불충족:', {
      hasFile: !!(target.files && target.files[0]),
      hasCurrentEpisode: !!currentEpisode.value,
      hasCurrentEpisodeId: !!currentEpisode.value?.id,
      hasGroupId: !!currentGroupBook.value?.groupId,
      hasBookId: !!currentGroupBook.value?.id
    });
  }
}

async function removeEpisodeImage() {
  if (currentEpisode.value?.imageId && currentGroupBook.value?.groupId &&
      currentGroupBook.value?.id && currentEpisode.value?.id) {
    if (!confirm('정말로 이미지를 삭제하시겠습니까?')) return;

    try {
      await groupBookService.deleteEpisodeImage(
        currentGroupBook.value.groupId,
        currentGroupBook.value.id,
        currentEpisode.value.id,
        currentEpisode.value.imageId
      );

      currentEpisode.value.imageUrl = undefined;
      currentEpisode.value.imageId = undefined;

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



// 템플릿 완료 처리 함수들
function handleTemplateCompleted() {
  console.log('템플릿 완료 감지:', currentTemplate.value);

  // 1. 템플릿 완료 상태로 변경
  isTemplateCompleted.value = true;
  isInterviewStarted.value = false;

  // 2. 지금까지의 답변들을 텍스트 영역에 표시
  displayAllAnswers();

  // 3. 새 에피소드 생성 가이드 표시
  showNextEpisodeGuide.value = true;
}

function displayAllAnswers() {
  // 현재 에피소드의 모든 답변을 합쳐서 표시
  // 실제로는 서버에서 받아온 편집된 내용을 사용해야 하지만,
  // 우선 현재 입력된 내용을 사용
  if (currentEpisode.value?.content) {
    allAnswersText.value = currentEpisode.value.content;
  }

  // 텍스트 영역에 요약된 답변 표시
  if (currentEpisode.value) {
    currentEpisode.value.content = allAnswersText.value;
  }
}

async function createNextEpisode() {
  if (!currentGroupBook.value?.groupId || !currentGroupBook.value?.id) {
    customAlertRef.value?.showAlert({
      title: '오류',
      message: '그룹북 정보를 찾을 수 없습니다.'
    });
    return;
  }

  // 새 에피소드 생성 전에 현재 에피소드 자동 저장
  if (currentEpisode.value?.id) {
    try {
      console.log('새 에피소드 생성 전 현재 에피소드 자동 저장...');
      await saveEpisode();
      console.log('현재 에피소드 자동 저장 완료');
    } catch (error) {
      console.error('현재 에피소드 자동 저장 실패:', error);
      // 저장 실패해도 다음 에피소드 생성은 계속 진행
    }
  }

  try {
    const nextEpisode = await groupBookService.createNextTemplateEpisode(
      currentGroupBook.value.groupId,
      currentGroupBook.value.id,
      currentTemplate.value
    );

    if (nextEpisode) {
      console.log('다음 에피소드 생성 응답:', nextEpisode);
      console.log('다음 에피소드 응답 구조:', Object.keys(nextEpisode));
      console.log('groupEpisodeId:', nextEpisode.groupEpisodeId);
      console.log('id 필드:', nextEpisode.groupEpisodeId);

      // 새 에피소드를 목록에 추가
      currentGroupBook.value.episodes = currentGroupBook.value.episodes || [];
      currentGroupBook.value.episodes.push({
        id: nextEpisode.groupEpisodeId,
        title: nextEpisode.title,
        content: '',
        template: nextEpisode.template,
        imageUrl: nextEpisode.imageUrl,
        imageId: nextEpisode.imageId
      });

      // 새 에피소드로 전환
      currentEpisodeIndex.value = currentGroupBook.value.episodes.length - 1;

      // 상태 초기화
      isTemplateCompleted.value = false;
      showNextEpisodeGuide.value = false;
      allAnswersText.value = '';

      // AI 인터뷰 상태 초기화
      isInterviewStarted.value = false;
      isConnecting.value = false;
      isConnected.value = false;
      currentSessionId.value = null;
      aiQuestion.value = 'AI 인터뷰 시작을 누르고 질문을 받아보세요.';

      // 임시 답변 초기화
      tempAnswers.value = [];

      // 기존 SSE 연결 정리
      await cleanupSseConnection();

      // 새 템플릿으로 업데이트
      currentTemplate.value = nextEpisode.template || 'STORY';

      customAlertRef.value?.showAlert({
        title: '새 에피소드 생성',
        message: `${getTemplateKoreanName(currentTemplate.value)} 템플릿의 새 에피소드가 생성되었습니다.`
      });
    }
  } catch (error) {
    console.error('다음 에피소드 생성 실패:', error);
    customAlertRef.value?.showAlert({
      title: '생성 실패',
      message: '다음 에피소드 생성 중 오류가 발생했습니다.'
    });
  }
}

// 라이프사이클 훅
onMounted(() => {
  const groupId = parseInt(route.params.groupId as string);
  const bookId = route.params.bookId ? parseInt(route.params.bookId as string) : null;

  if (route.query.start_editing === 'true' && bookId) {
    loadGroupBookForEditing(groupId, bookId);
  } else if (bookId) {
    loadGroupBookForEditing(groupId, bookId);
  }
  else {
    // 새 그룹북 생성 모드
    creationStep.value = 'setup';
    currentGroupBook.value.groupId = groupId;
  }
});

onBeforeRouteLeave((to, from, next) => {
  if (creationStep.value !== 'setup' && !isSavedOrPublished.value) {
    const answer = window.confirm(
      '저장하지 않은 변경사항이 있습니다. 정말로 페이지를 떠나시겠습니까?'
    );
    if (answer) {
      cleanupBeforeLeave().then(() => next());
    } else {
      next(false);
    }
  } else {
    next();
  }
});

onBeforeUnmount(() => {
  cleanupBeforeLeave();
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
