<template>
  <div ref="container" style="width: 100vw; height: 100vh"></div>
</template>

<script setup lang="ts">
/**
 * Three.js 기반 인트로 3D 뷰
 * - 마우스 패럴랙스: 시선(Yaw/Pitch)만 회전 → 사람이 고개를 돌려 보는 느낌
 * - OrbitControls 사용자 입력 완전 차단
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
// 강도 조절 포인트:
// - parallaxAngle: 마우스 -1~1 → 최대 회전각(라디안 계수). 0.015~0.035 권장.
const props = defineProps({
  parallaxFactor: { type: Number, default: 0.3 }, // (호환용, 현재 사용하지 않음)
  parallaxAngle:  { type: Number, default: 0.06 }, // ✅ XY 회전 강도
  parallaxFactorZ:{ type: Number, default: 0.012 } // (호환용, 현재 사용하지 않음)
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

// 패럴랙스 기준 (카메라/타겟)
const baseCameraPosition = new THREE.Vector3()
const baseTargetPosition = new THREE.Vector3()
let parallaxEnabled = false

// 애니메이션 루프 제어
let animationFrameId = 0
const isAnimating = ref(false)

// 재사용 객체
const globalRaycaster = new THREE.Raycaster()
const globalMouse = new THREE.Vector2()

// 입력 차단 가드
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
  window.removeEventListener('resize', onWindowResize)
  container.value?.removeEventListener('click', onCanvasClick)
  container.value?.removeEventListener('mousemove', onMouseMove)

  if (renderer?.domElement && swallowHandler) {
    blockedEventTypes.forEach(t =>
      renderer.domElement.removeEventListener(t, swallowHandler, { capture: true } as any)
    )
  }

  stopAnimation()

  if (camera) gsap.killTweensOf(camera.position)
  if (controls) gsap.killTweensOf((controls as any).target)
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
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.setSize(window.innerWidth, window.innerHeight)
  renderer.outputColorSpace = THREE.SRGBColorSpace
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.2

  // DOM & 이벤트
  container.value?.appendChild(renderer.domElement)
  container.value?.addEventListener('click', onCanvasClick)
  container.value?.addEventListener('mousemove', onMouseMove)
  window.addEventListener('resize', onWindowResize)

  /* OrbitControls */
  controls = new OrbitControls(camera, renderer.domElement)
  controls.target.set(0.3, 35, 25)
  controls.update()
  baseTargetPosition.copy(controls.target) // ✅ 시선 기준 저장

  // 사용자 입력 완전 차단
  lockOrbitControls()

  /* 조명, 환경맵, 모델 */
  RectAreaLightUniformsLib.init()
  setupLights()
  loadHDR()

  addPhoto()
  // loadModel() // 필요 시 외부에서
}

/* ------------------------------ OrbitControls 봉인 ------------------------------ */
function lockOrbitControls() {
  controls.enabled = false
  controls.enableRotate = false
  controls.enableZoom = false
  controls.enablePan = false
  controls.enableDamping = false

  ;(controls as any).mouseButtons = { LEFT: null, MIDDLE: null, RIGHT: null }
  ;(controls as any).touches = { ONE: null, TWO: null }

  swallowHandler = (e: Event) => {
    e.preventDefault()
    e.stopPropagation()
    ;(e as any).stopImmediatePropagation?.()
  }
  blockedEventTypes.forEach(type => {
    renderer.domElement.addEventListener(type, swallowHandler, { capture: true })
  })
}

/* ---------------------------------- 조명 ---------------------------------- */
function setupLights() {
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

  const point1 = new THREE.PointLight(0xf0ab43, 10, 15)
  point1.position.set(-12, 3, -6.6)
  scene.add(point1)

  const point1a = new THREE.PointLight(0xf0ab43, 10, 15)
  point1a.position.set(-12, 3, 2)
  scene.add(point1a)

  const point2 = new THREE.PointLight(0xf0ab43, 30, 30)
  point2.position.set(7.3, 5, 25)
  scene.add(point2)

  const point3 = new THREE.PointLight(0xf0ab43, 8, 100)
  point3.position.set(-8, 2.5, -4.3)
  scene.add(point3)

  const point4 = new THREE.PointLight(0xf0ab43, 8, 100)
  point4.position.set(-5, 2.5, -4.3)
  scene.add(point4)

  const point5 = new THREE.PointLight(0xf0ab43, 5, 30)
  point5.position.set(1.55, 2.25, -4.3)
  scene.add(point5)

  const point6 = new THREE.PointLight(0xf0ab43, 5, 30)
  point6.position.set(4.8, 2.25, -4.3)
  scene.add(point6)

  const point7 = new THREE.PointLight(0xf0ab43, 10, 15)
  point7.position.set(8.5, 3, -0.33191)
  scene.add(point7)
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

/* ----------------------------- 사진 한 장 추가 ----------------------------- */
function addPhoto() {
  const BASE_WIDTH = 8
  const texture = new THREE.TextureLoader().load('/3D/bookstore.png', onLoad, undefined, onError)
  if ('colorSpace' in texture) { ;(texture as any).colorSpace = THREE.SRGBColorSpace }

  const geometry = new THREE.PlaneGeometry(BASE_WIDTH, BASE_WIDTH)
  const material = new THREE.MeshBasicMaterial({ map: texture, side: THREE.FrontSide })
  const mesh = new THREE.Mesh(geometry, material)
  mesh.position.set(-1.3, 2.04, -13)
  mesh.rotation.y = 0
  scene.add(mesh)

  function onLoad(tex: THREE.Texture) {
    const img = (tex.image as HTMLImageElement | { width: number; height: number }) || null
    if (img && img.width && img.height) {
      const aspectH = img.height / img.width
      const newHeight = BASE_WIDTH * aspectH
      mesh.geometry.dispose()
      mesh.geometry = new THREE.PlaneGeometry(BASE_WIDTH, newHeight)
    }
    if (renderer) tex.anisotropy = renderer.capabilities.getMaxAnisotropy()
  }
  function onError(err: unknown) { console.error('텍스처 로드 실패: /3D/bookstore.png', err) }
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
          material.color.multiplyScalar(0.4)
          mesh.castShadow = false
        }
        if (mesh.name.includes('Bench')) {
          mesh.receiveShadow = false
        }
      }
    })

    scene.add(hanok)
    prewarmShaders(hanok)
    createHotspots()
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
      moveCameraTo(-1.5, 2, -7, -1.5, 2, -10.5, () => emit('hotspot', 'store'))
    })
  )

  // 책 만들기
  hotspots.push(
    createHotspot(new THREE.Vector3(8.5, 2, 3), texture, 'create', () => {
      const tl = gsap.timeline({
        onStart: () => { parallaxEnabled = false },
        onUpdate: () => { controls.update() },
        onComplete: () => {
          parallaxEnabled = true
          baseCameraPosition.copy(camera.position)
          baseTargetPosition.copy(controls.target) // ✅ 시선 기준 갱신
          emit('hotspot', 'create')
        }
      })
      tl.to(camera.position, { x: 2, y: 2, z: 1, duration: 2 })
      tl.to(controls.target, { x: 10, y: 3, z: 1, duration: 2 }, '<')
      tl.to(camera.position, { x: 8.5, y: 2, z: 1, duration: 2 })
      tl.to(controls.target, { x: 8.5, y: 2, z: -3, duration: 2 }, '<')
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
  sprite.userData.onClick = onClick
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
  const tl = gsap.timeline({
    onStart: () => { parallaxEnabled = false },
    onUpdate: () => { controls.update() },
    onComplete: () => {
      parallaxEnabled = true
      baseCameraPosition.copy(camera.position)
      baseTargetPosition.copy(controls.target) // ✅ 시선 기준 갱신
      if (onCompleteCallback) onCompleteCallback()
    }
  })
  tl.to(camera.position, { x: px, y: py, z: pz, duration: 2 })
  tl.to(controls.target, { x: tx, y: ty, z: tz, duration: 2 }, '<')
}

function moveToYard() {
  const camCurveA = new THREE.CatmullRomCurve3(
    [ new THREE.Vector3(camera.position.x, camera.position.y, camera.position.z),
      new THREE.Vector3(7.3, 2.5, 30.0) ],
    false, 'catmullrom', 0.2
  )

  const camCurveB = new THREE.CatmullRomCurve3(
    [ new THREE.Vector3(7.3, 2.5, 30.0),
      new THREE.Vector3(7.3, 2.5, 16.0),
      new THREE.Vector3(5.5, 2.5, 14.0) ],
    false, 'catmullrom', 0.2
  )

  const targetCurveA = new THREE.CatmullRomCurve3(
    [ new THREE.Vector3(controls.target.x, controls.target.y, controls.target.z),
      new THREE.Vector3(7.3, 2.5, 16.0) ],
    false, 'catmullrom', 0.2
  )

  const targetCurveB = new THREE.CatmullRomCurve3(
    [ new THREE.Vector3(7.3, 2.5, 18.0),
      new THREE.Vector3(7.3, 2.5, 14.0),
      new THREE.Vector3(1.0, 2.5, 3.579) ],
    false, 'catmullrom', 0.2
  )

  const state: any = { phase: 'A', tA: 0, tB: 0, _prevDamping: controls.enableDamping }

  const updateAlongCurve = () => {
    if (state.phase === 'A') {
      const camPos = camCurveA.getPointAt(state.tA)
      const tgtPos = targetCurveA.getPointAt(state.tA)
      camera.position.copy(camPos)
      controls.target.copy(tgtPos)
    } else {
      const camPos = camCurveB.getPointAt(state.tB)
      const tgtPos = targetCurveB.getPointAt(state.tB)
      camera.position.copy(camPos)
      controls.target.copy(tgtPos)
    }
    controls.update()
  }

  const tl = gsap.timeline({
    onStart: () => { parallaxEnabled = false; controls.enableDamping = false },
    onUpdate: updateAlongCurve,
    onComplete: () => {
      parallaxEnabled = true
      controls.enableDamping = state._prevDamping ?? true
      baseCameraPosition.copy(camera.position)
      baseTargetPosition.copy(controls.target) // ✅ 시선 기준 갱신
      emit('yard-animation-finished')
    }
  })

  tl.to(state, { tA: 1, duration: 2, ease: 'none' })
  tl.add(() => { state.phase = 'B' })
  tl.to(state, { tB: 1, duration: 2, ease: 'none' })
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
    const first = intersects[0].object
    if (first.userData && typeof first.userData.onClick === 'function') {
      ;(first.userData.onClick as () => void)()
    }
  }
}

/**
 * 마우스 이동: 시선 회전 패럴랙스(Yaw/Pitch)
 */
function onMouseMove(event: MouseEvent) {
  if (!container.value) return
  const rect = container.value.getBoundingClientRect()

  globalMouse.set(
    ((event.clientX - rect.left) / rect.width) * 2 - 1,
    -((event.clientY - rect.top) / rect.height) * 2 + 1
  )

  // ✅ 패럴랙스: 카메라는 고정, 타겟만 회전(고개 돌리기)
  if (parallaxEnabled) {
    const yaw   =  -globalMouse.x * props.parallaxAngle   // 좌/우
    const pitch =  globalMouse.y * props.parallaxAngle   // 위/아래 (원하면 부호 반전)

    const dir   = new THREE.Vector3().subVectors(baseTargetPosition, baseCameraPosition)
    const up    = new THREE.Vector3(0, 1, 0)
    const right = new THREE.Vector3().crossVectors(dir, up).normalize()

    // Yaw → Pitch 순으로 소각 회전
    dir.applyQuaternion(new THREE.Quaternion().setFromAxisAngle(up,    yaw))
    dir.applyQuaternion(new THREE.Quaternion().setFromAxisAngle(right, pitch))

    const newTarget = new THREE.Vector3().addVectors(baseCameraPosition, dir)

    gsap.to(controls.target, {
      x: newTarget.x,
      y: newTarget.y,
      z: newTarget.z,
      duration: 0.4,
      ease: 'power1.out',
      overwrite: 'auto',
      onUpdate: () => {
        controls.update()
      }
    })
  }

  // 핫스팟 호버 스케일
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
canvas { display: block; }
</style>
