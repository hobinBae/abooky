<template>
  <div ref="container" style="width: 100vw; height: 100vh"></div>
</template>

<script setup lang="ts">
/**
 * Three.js 기반 인트로 3D 뷰
 * - 마우스 패럴랙스: 카메라가 마우스에 따라 부드럽게 미소 이동
 * - OrbitControls 사용자 입력 완전 차단: 드래그/휠/터치로는 절대 카메라가 움직이지 않음
 * - 핫스팟 클릭 시 카메라 이동 및 상위 컴포넌트로 이벤트 emit
 * - HDRI 배경, 조명, 셰이더 예열 포함
 */

import * as THREE from 'three'
import { ref, onMounted, defineEmits, onUnmounted, defineExpose, onActivated, onDeactivated } from 'vue'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { GLTFLoader, type GLTF } from 'three/examples/jsm/loaders/GLTFLoader.js'
import { KTX2Loader } from 'three/examples/jsm/loaders/KTX2Loader.js'
import { MeshoptDecoder } from 'three/examples/jsm/libs/meshopt_decoder.module.js'
import { RGBELoader } from 'three/examples/jsm/loaders/RGBELoader.js'
import { RectAreaLightUniformsLib } from 'three/examples/jsm/lights/RectAreaLightUniformsLib.js'
import { gsap } from 'gsap'

/* ---------------------------------- 이벤트 ---------------------------------- */
const emit = defineEmits(['loaded', 'hotspot', 'background-loaded', 'yard-animation-finished'])

/* ---------------------------------- 프롭스 ---------------------------------- */
const props = defineProps({
  // 마우스 패럴랙스 강도. 값이 클수록 마우스에 더 크게 반응
  parallaxFactor: {
    type: Number,
    default: 1.25
  }
})

/* ----------------------------- 외부에서 호출 가능 ---------------------------- */
defineExpose({ moveToYard, moveCameraTo, loadModel, getHotspotByName })

/* ----------------------------- 전역 상태/객체들 ----------------------------- */
const container = ref<HTMLDivElement | null>(null)

let scene: THREE.Scene
let camera: THREE.PerspectiveCamera
let renderer: THREE.WebGLRenderer
let controls: OrbitControls

const hotspots: THREE.Object3D[] = []
let hoveredHotspot: THREE.Object3D | null = null

// 패럴랙스 기준 위치 및 활성화 플래그
const baseCameraPosition = new THREE.Vector3()
let parallaxEnabled = true

// 애니메이션 루프 제어
let animationFrameId = 0
const isAnimating = ref(false)

// 빈번한 이벤트에서의 할당을 줄이기 위한 재사용 객체
const globalRaycaster = new THREE.Raycaster()
const globalMouse = new THREE.Vector2()

// 입력 차단 가드 리스너들을 해제하기 위해 보관
const blockedEventTypes = ['pointerdown', 'pointermove', 'pointerup', 'wheel', 'touchstart', 'touchmove', 'touchend', 'contextmenu'] as const
let swallowHandler: (e: Event) => void

/* -------------------------------- 라이프사이클 ------------------------------- */
onMounted(() => {
  initScene()
  startAnimation()
})

onActivated(() => {
  startAnimation()
})

onDeactivated(() => {
  stopAnimation()
})

onUnmounted(() => {
  // 전역/DOM 리스너 정리
  window.removeEventListener('resize', onWindowResize)
  container.value?.removeEventListener('click', onCanvasClick)
  container.value?.removeEventListener('mousemove', onMouseMove)

  // 입력 차단 가드 해제
  if (renderer?.domElement && swallowHandler) {
    blockedEventTypes.forEach(t =>
      renderer.domElement.removeEventListener(t, swallowHandler, { capture: true } as any)
    )
  }

  // 렌더 루프 정지
  stopAnimation()

  // 잔여 트윈 제거
  if (camera) {
    gsap.killTweensOf(camera.position)
  }
  if (controls) {
    gsap.killTweensOf((controls as any).target)
  }
})

/* ---------------------------------- 초기화 ---------------------------------- */
function initScene() {
  /* 씬 */
  scene = new THREE.Scene()

  /* 카메라 */
  camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 0.1, 1000)
  camera.position.set(7.3, 25, 30)
  baseCameraPosition.copy(camera.position)

  /* 렌더러 */
  renderer = new THREE.WebGLRenderer({ antialias: true, powerPreference: 'high-performance' })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))            // 과한 해상도 제한
  renderer.setSize(window.innerWidth, window.innerHeight)
  renderer.outputColorSpace = THREE.SRGBColorSpace
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.2

  // DOM 연결 및 이벤트
  container.value?.appendChild(renderer.domElement)
  container.value?.addEventListener('click', onCanvasClick)
  container.value?.addEventListener('mousemove', onMouseMove)
  window.addEventListener('resize', onWindowResize)

  /* OrbitControls */
  controls = new OrbitControls(camera, renderer.domElement)
  controls.target.set(0.3, 35, 25)
  controls.update()

  // 사용자 입력을 통한 모든 조작을 완전 차단
  lockOrbitControls()

  /* 조명, 환경맵, 모델 */
  RectAreaLightUniformsLib.init()
  setupLights()
  loadHDR()
  // loadModel() // 필요 시 외부에서 호출
}

/* ------------------------------ OrbitControls 봉인 ------------------------------ */
/**
 * OrbitControls에 의한 사용자 입력(드래그/휠/터치)로 카메라가 미세하게라도 움직이지 않도록 완전 차단한다.
 * 1) controls.enabled=false 로 내부 입력 처리 루프 비활성화
 * 2) 회전/줌/패닝/댐핑 off
 * 3) mouseButtons/touches 매핑을 null로 비워 입력 바인딩 제거
 * 4) 캡처 단계에서 포인터/휠/터치/컨텍스트메뉴 이벤트를 swallow 하여 OrbitControls가 이벤트를 수신하지 못하게 함
 */
function lockOrbitControls() {
  // 1) 내부 입력 처리 루프 비활성화
  controls.enabled = false

  // 2) 회전/줌/패닝/댐핑 비활성화 (보조)
  controls.enableRotate = false
  controls.enableZoom = false
  controls.enablePan = false
  controls.enableDamping = false

  // 3) 입력 매핑 해제
  ;(controls as any).mouseButtons = { LEFT: null, MIDDLE: null, RIGHT: null }
  ;(controls as any).touches = { ONE: null, TWO: null }

  // 4) 캡처 단계에서 이벤트 삼키기
  swallowHandler = (e: Event) => {
    e.preventDefault()
    e.stopPropagation()
    ;(e as any).stopImmediatePropagation?.()
  }
  blockedEventTypes.forEach(type => {
    renderer.domElement.addEventListener(type, swallowHandler, { capture: true })
  })

  // 주의: 애니메이션 중 controls.enabled = true 로 되돌리는 코드가 있다면 제거할 것.
  // 현재 구조는 GSAP으로 camera.position / controls.target을 직접 트윈하므로 문제 없음.
}

/* ---------------------------------- 조명 ---------------------------------- */
function setupLights() {
  // 주광: 그림자 품질은 성능에 맞춰 조정 가능
  const dirLight = new THREE.DirectionalLight(0xffffff, 1.5)
  dirLight.position.set(30, 30, 10)
  dirLight.castShadow = true
  dirLight.shadow.mapSize.width = 512
  dirLight.shadow.mapSize.height = 512
  dirLight.shadow.camera.near = 1
  dirLight.shadow.camera.far = 100
  dirLight.shadow.camera.left = -100
  dirLight.shadow.camera.right = 100
  dirLight.shadow.camera.top = 100
  dirLight.shadow.camera.bottom = -100
  scene.add(dirLight)

  // 포인트 라이트들: 따뜻한 실내광 느낌
  const point1 = new THREE.PointLight(0xf0ab43, 10, 15)
  point1.position.set(-12, 3, -6.6)
  scene.add(point1)

  const point1a = new THREE.PointLight(0xf0ab43, 10, 15)
  point1a.position.set(-12, 3, 2)
  scene.add(point1a)

  const point4 = new THREE.PointLight(0xf0ab43, 10, 15)
  point4.position.set(8.5, 3, -0.33191)
  scene.add(point4)

  // 필요 시 RectAreaLight를 켜서 포인트 라이트 대비 부드러운 면광원 효과를 추가할 수 있음
  // const areaLight = new THREE.RectAreaLight(0xeba64d, 8, 2.28, 2.28)
  // areaLight.position.set(7.3, 5, 25)
  // areaLight.rotation.set(
  //   THREE.MathUtils.degToRad(91.174),
  //   THREE.MathUtils.degToRad(180.48163),
  //   THREE.MathUtils.degToRad(-0.75941)
  // )
  // scene.add(areaLight)
}

/* --------------------------------- HDR 로드 -------------------------------- */
function loadHDR() {
  new RGBELoader().load('/3D/background.hdr', (texture: THREE.Texture) => {
    texture.mapping = THREE.EquirectangularReflectionMapping
    scene.background = texture
    scene.environment = texture
    emit('background-loaded')
  })
}

/* -------------------------------- 모델 로드 -------------------------------- */
function loadModel() {
  const ktx2Loader = new KTX2Loader()
    .setTranscoderPath('https://cdn.jsdelivr.net/gh/mrdoob/three.js@r165/examples/jsm/libs/basis/')
    .detectSupport(renderer)

  const loader = new GLTFLoader()
  loader.setKTX2Loader(ktx2Loader)
  loader.setMeshoptDecoder(MeshoptDecoder)

  loader.load('/3D/hanok_250806.glb', (gltf: GLTF) => {
    const hanok = gltf.scene
    hanok.scale.set(1.5, 1.5, 1.5)

    // 재질 튜닝 및 그림자 설정
    hanok.traverse((child: THREE.Object3D) => {
      if ((child as THREE.Mesh).isMesh) {
        const mesh = child as THREE.Mesh
        mesh.castShadow = true
        mesh.receiveShadow = true

        const material = mesh.material as THREE.MeshStandardMaterial
        if (material instanceof THREE.MeshStandardMaterial) {
          material.envMap = scene.environment
          material.envMapIntensity = 0.4
        }

        if (mesh.name.includes('Ground')) {
          material.color.multiplyScalar(0.4) // 바닥 톤 다운
          mesh.castShadow = false
        }
        if (mesh.name.includes('Bench')) {
          mesh.receiveShadow = false
        }
      }
    })

    scene.add(hanok)

    // 셰이더 예열: 카메라 이동 시 초기 프레임 드랍 방지
    prewarmShaders(hanok)

    // 핫스팟 구성
    createHotspots()

    // 외부에 로딩 완료 알림
    emit('loaded')
  })
}

/* ------------------------------- 셰이더 예열 ------------------------------- */
function prewarmShaders(model: THREE.Object3D) {
  model.traverse((child: THREE.Object3D) => {
    if ((child as THREE.Mesh).isMesh) child.frustumCulled = false
  })
  renderer.render(scene, camera)
  model.traverse((child: THREE.Object3D) => {
    if ((child as THREE.Mesh).isMesh) child.frustumCulled = true
  })
}

/* -------------------------------- 핫스팟 -------------------------------- */
function createHotspots() {
  const texture = new THREE.TextureLoader().load('/3D/star.png')

  // 내서재
  hotspots.push(
    createHotspot(new THREE.Vector3(-9.5, 2, 1), texture, 'library', () => {
      moveCameraTo(-9.5, 3, 0, -11, 3, 0, () => emit('hotspot', 'library'))
    })
  )

  // 서점
  hotspots.push(
    createHotspot(new THREE.Vector3(-2.5, 2, -6), texture, 'store', () => {
      moveCameraTo(-2, 2, -6.5, -2, 2, -10.5, () => emit('hotspot', 'store'))
    })
  )

  // 책 만들기
  hotspots.push(
    createHotspot(new THREE.Vector3(8.5, 2, 3), texture, 'create', () => {
      const tl = gsap.timeline({
        onStart: () => {
          parallaxEnabled = false
        },
        onUpdate: () => { controls.update() },
        onComplete: () => {
          parallaxEnabled = true
          baseCameraPosition.copy(camera.position)
          emit('hotspot', 'create')
        }
      })
      tl.to(camera.position, { x: 2, y: 3, z: 1, duration: 1 })
      tl.to(controls.target, { x: 10, y: 3, z: 1, duration: 1 }, '<')
      tl.to(camera.position, { x: 8.5, y: 2, z: 1, duration: 3 })
      tl.to(controls.target, { x: 8.5, y: 2, z: -3, duration: 3 }, '<')
    })
  )

  hotspots.forEach(h => scene.add(h))
}

function createHotspot(pos: THREE.Vector3, texture: THREE.Texture, name: string, onClick: () => void) {
  const material = new THREE.SpriteMaterial({ map: texture })
  const sprite = new THREE.Sprite(material)
  sprite.scale.set(1, 1, 1)
  sprite.position.copy(pos)
  sprite.name = name
  ;(sprite as any).userData.onClick = onClick
  return sprite
}

function getHotspotByName(name: string) {
  return hotspots.find(h => h.name === name)
}

/* ----------------------------- 카메라 이동 유틸 ----------------------------- */
function moveCameraTo(
  px: number, py: number, pz: number,
  tx: number, ty: number, tz: number,
  onCompleteCallback?: () => void
) {
  // 기존 카메라 애니메이션을 중지하여 충돌 방지
  gsap.killTweensOf(camera.position)
  gsap.killTweensOf((controls as any).target)

  const tl = gsap.timeline({
    onStart: () => {
      parallaxEnabled = false
    },
    onUpdate: () => { controls.update() },
    onComplete: () => {
      parallaxEnabled = true
      baseCameraPosition.copy(camera.position)
      if (onCompleteCallback) onCompleteCallback()
    }
  })
  tl.to(camera.position, { x: px, y: py, z: pz, duration: 2 })
  tl.to((controls as any).target, { x: tx, y: ty, z: tz, duration: 2 }, '<')
}

function moveToYard() {
  const tl = gsap.timeline({
    onStart: () => {
      parallaxEnabled = false
    },
    onUpdate: () => { controls.update() },
    onComplete: () => {
      parallaxEnabled = true
      baseCameraPosition.copy(camera.position)
      emit('yard-animation-finished')
    }
  })
  tl.to(camera.position, { x: 7.3, y: 2.5, z: 30, duration: 4 })
  tl.to((controls as any).target, { x: 7.3, y: 2.5, z: 0, duration: 4 }, '<')
  tl.to(camera.position, { x: 5.5, y: 2.5, z: 14, duration: 2 })
  tl.to((controls as any).target, { x: 1, y: 3, z: 3.579, duration: 2 }, '<')
}

/* ------------------------------ 렌더 루프/리사이즈 ------------------------------ */
function animate() {
  if (!isAnimating.value) return
  animationFrameId = requestAnimationFrame(animate)
  renderer.render(scene, camera)
}

function startAnimation() {
  if (!isAnimating.value) {
    isAnimating.value = true
    animate()
  }
}

function stopAnimation() {
  if (isAnimating.value) {
    cancelAnimationFrame(animationFrameId)
    isAnimating.value = false
  }
}

function onWindowResize() {
  camera.aspect = window.innerWidth / window.innerHeight
  camera.updateProjectionMatrix()
  renderer.setSize(window.innerWidth, window.innerHeight)
}

/* ------------------------------- 입력/히트테스트 ------------------------------- */
/**
 * 캔버스 클릭 시 핫스팟 교차 판정 후 onClick 실행
 * Raycaster/Vector2는 전역 인스턴스를 재사용하여 성능 및 GC 최소화
 */
function onCanvasClick(event: MouseEvent) {
  if (!container.value) return
  const rect = container.value.getBoundingClientRect()

  globalMouse.set(
    ((event.clientX - rect.left) / rect.width) * 2 - 1,
    -((event.clientY - rect.top) / rect.height) * 2 + 1
  )

  globalRaycaster.setFromCamera(globalMouse, camera)
  const intersects = globalRaycaster.intersectObjects(hotspots, true)

  if (intersects.length > 0) {
    const first = intersects[0].object as any
    if (first.userData && typeof first.userData.onClick === 'function') {
      first.userData.onClick()
    }
  }
}

/**
 * 마우스 이동에 따른 패럴랙스 및 핫스팟 호버 스케일 처리
 * OrbitControls 입력은 차단되어도 Raycaster 기반의 커스텀 상호작용은 그대로 동작
 */
function onMouseMove(event: MouseEvent) {
  if (!container.value) return
  const rect = container.value.getBoundingClientRect()

  globalMouse.set(
    ((event.clientX - rect.left) / rect.width) * 2 - 1,
    -((event.clientY - rect.top) / rect.height) * 2 + 1
  )

  // 패럴랙스: 기준점에서 소폭 이동
  if (parallaxEnabled) {
    const targetX = baseCameraPosition.x + globalMouse.x * props.parallaxFactor
    const targetY = baseCameraPosition.y - globalMouse.y * props.parallaxFactor

    gsap.to(camera.position, {
      x: targetX,
      y: targetY,
      duration: 0.5,
      ease: 'power1.out',
      overwrite: 'auto'
    })
  }

  // 핫스팟 호버 판정
  globalRaycaster.setFromCamera(globalMouse, camera)
  const intersects = globalRaycaster.intersectObjects(hotspots, true)

  if (intersects.length > 0) {
    const first = intersects[0].object
    if (hoveredHotspot !== first) {
      if (hoveredHotspot) gsap.to(hoveredHotspot.scale, { x: 1, y: 1, z: 1, duration: 0.3 })
      hoveredHotspot = first
      gsap.to(hoveredHotspot.scale, { x: 1.2, y: 1.2, z: 1.2, duration: 0.3 })
    }
  } else {
    if (hoveredHotspot) {
      gsap.to(hoveredHotspot.scale, { x: 1, y: 1, z: 1, duration: 0.3 })
      hoveredHotspot = null
    }
  }
}
</script>

<style scoped>
canvas {
  display: block;
}
</style>
