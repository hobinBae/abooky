<template>
  <!-- 전체 인트로 페이지를 감싸는 컨테이너 -->
  <div class="intro-container">
    <!-- Three.js 씬을 렌더링하는 자식 컴포넌트 -->
    <!-- ref를 통해 스크립트에서 컴포넌트의 함수를 호출할 수 있음 -->
    <!-- @loaded, @hotspot은 자식 컴포넌트에서 발생하는 이벤트를 수신하는 리스너 -->
    <ThreeScene ref="threeSceneRef" @loaded="onSceneLoaded" @hotspot="onHotspotActivated" />

    <!-- 3D 모델 로딩이 완료되면 나타나는 '들어가기' 버튼 -->
    <button v-if="showEnterButton" class="enter-button" @click="enterYard">
      들어가기
    </button>

    <!-- 핫스팟으로 이동했을 때 나타나는 버튼 그룹 -->
    <div v-if="activeHotspot" class="hotspot-actions">
      <!-- '내서재 가기', '서점 가기' 등 동적으로 라벨이 바뀌는 액션 버튼 -->
      <button class="action-button" @click="goToPage">
        {{ getButtonLabel(activeHotspot) }}
      </button>
      <!-- 마당으로 돌아가는 버튼 -->
      <button class="return-button" @click="returnToYard">
        <img src="/3D/back_icon.png" alt="돌아가기" />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import ThreeScene from '@/components/ThreeScene.vue'

// 부모 컴포넌트(App.vue)로 이벤트를 보내기 위한 emit 함수 정의
const emit = defineEmits(['update-navbar'])
// 페이지 이동을 위한 Vue Router 인스턴스
const router = useRouter()

// ThreeScene 컴포넌트의 인스턴스를 참조하기 위한 ref
const threeSceneRef = ref<InstanceType<typeof ThreeScene> | null>(null)
// '들어가기' 버튼의 표시 여부를 제어하는 상태
const showEnterButton = ref(false)
// 현재 활성화된 핫스팟의 이름을 저장하는 상태 ('library', 'store', 'create' 또는 null)
const activeHotspot = ref<string | null>(null)

/**
 * ThreeScene 컴포넌트에서 3D 모델 로딩이 완료되었을 때 호출되는 핸들러.
 */
const onSceneLoaded = () => {
  // '들어가기' 버튼을 표시함
  showEnterButton.value = true
}

/**
 * '들어가기' 버튼 클릭 시 호출되는 핸들러.
 * 마당으로 카메라를 이동시키고 네비게이션 바를 표시하도록 부모에 알림.
 */
const enterYard = () => {
  if (threeSceneRef.value) {
    // ThreeScene 컴포넌트의 moveToYard 함수를 호출하여 카메라 애니메이션 시작
    threeSceneRef.value.moveToYard()
    // '들어가기' 버튼 숨김
    showEnterButton.value = false
    // App.vue에 'update-navbar' 이벤트를 보내 네비게이션 바를 표시하도록 요청
    emit('update-navbar', true)
  }
}

/**
 * ThreeScene에서 핫스팟에 도달했을 때 호출되는 핸들러.
 * @param hotspotName - 도달한 핫스팟의 이름 ('library', 'store', 'create')
 */
const onHotspotActivated = (hotspotName: string) => {
  // 활성화된 핫스팟의 이름을 상태에 저장하여 관련 UI를 표시
  activeHotspot.value = hotspotName
}

/**
 * '돌아가기' 버튼 클릭 시 호출되는 핸들러.
 * 카메라를 마당으로 되돌리고 핫스팟 관련 UI를 숨김.
 */
const returnToYard = () => {
  if (threeSceneRef.value?.moveCameraTo) {
    // ThreeScene 컴포넌트의 moveCameraTo 함수를 특정 좌표로 호출
    threeSceneRef.value.moveCameraTo(5.5, 2.5, 14, 1, 3, 3.579)
    // 활성화된 핫스팟 상태를 null로 초기화하여 버튼들 숨김
    activeHotspot.value = null
  }
}

/**
 * 활성화된 핫스팟 이름에 따라 버튼에 표시될 텍스트를 반환.
 * @param hotspot - 현재 활성화된 핫스팟의 이름
 * @returns 버튼 라벨 문자열
 */
const getButtonLabel = (hotspot: string) => {
  switch (hotspot) {
    case 'library':
      return '내서재 가기'
    case 'store':
      return '서점 가기'
    case 'create':
      return '책 만들러 가기'
    default:
      return ''
  }
}

/**
 * '페이지 가기' 버튼 클릭 시 호출되는 핸들러.
 * 활성화된 핫스팟에 해당하는 페이지로 라우팅.
 */
const goToPage = () => {
  if (!activeHotspot.value) return
  // 핫스팟 이름과 실제 경로를 매핑하는 객체
  const pathMap: { [key: string]: string } = {
    library: '/my-library',
    store: '/bookstore',
    create: '/create-book'
  }
  // 해당 경로로 페이지 이동
  router.push(pathMap[activeHotspot.value])
}
</script>

<style scoped>
/* 인트로 페이지 전체 컨테이너 스타일 */
.intro-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden; /* 캔버스가 넘치는 것을 방지 */
}

/* '들어가기' 버튼 스타일 */
.enter-button {
  position: absolute;
  bottom: 10%;
  left: 50%;
  transform: translateX(-50%);
  padding: 16px 32px;
  font-size: 1.2rem;
  border-radius: 8px;
  border: none;
  background: #2d5c2f;
  color: #fff;
  cursor: pointer;
  z-index: 100; /* 3D 캔버스 위에 표시되도록 z-index 설정 */
  transition: opacity 0.5s;
}

/* 핫스팟 관련 버튼들을 감싸는 컨테이너 */
.hotspot-actions {
  position: absolute;
  bottom: 10%;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px; /* 버튼 사이의 간격 */
  z-index: 100;
}

/* '페이지 가기' 버튼 스타일 */
.action-button {
  padding: 12px 24px;
  font-size: 1rem;
  border-radius: 8px;
  border: none;
  background: rgba(45, 92, 47, 0.8); /* 반투명 배경 */
  color: #fff;
  cursor: pointer;
}

/* '돌아가기' 버튼 스타일 */
.return-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
}

.return-button img {
  width: 60px;
  height: 60px;
  display: block;
}
</style>
