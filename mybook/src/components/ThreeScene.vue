<template>
  <div ref="container" style="width: 100vw; height: 100vh"></div>
</template>

<script setup lang="ts">
import * as THREE from 'three'
import { ref, onMounted, defineEmits, onUnmounted, defineExpose } from 'vue'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader'
import { RGBELoader } from 'three/examples/jsm/loaders/RGBELoader'
import { RectAreaLightUniformsLib } from 'three/examples/jsm/lights/RectAreaLightUniformsLib.js'
import { gsap } from 'gsap'

const emit = defineEmits(['loaded', 'hotspot'])

defineExpose({ moveToYard });

const container = ref<HTMLDivElement | null>(null)
let camera: THREE.PerspectiveCamera
let controls: OrbitControls
let scene: THREE.Scene
let renderer: THREE.WebGLRenderer
let hotspots: THREE.Object3D[] = []

let animationFrameId: number;

onMounted(() => {
  initScene()
})

onUnmounted(() => {
  window.removeEventListener('resize', onWindowResize)
  container.value?.removeEventListener('click', onCanvasClick);
  cancelAnimationFrame(animationFrameId);
})

function initScene() {
  scene = new THREE.Scene()

  camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 0.1, 1000)
  camera.position.set(6, 10.5, 38)

  renderer = new THREE.WebGLRenderer({ antialias: true })
  renderer.setSize(window.innerWidth, window.innerHeight)
  renderer.outputEncoding = THREE.sRGBEncoding
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 0.8

  container.value?.appendChild(renderer.domElement)
  container.value?.addEventListener('click', onCanvasClick);

  controls = new OrbitControls(camera, renderer.domElement)
  controls.target.set(6, 15, 0)
  controls.update()

  window.addEventListener('resize', onWindowResize)

  RectAreaLightUniformsLib.init()
  setupLights()
  loadHDR()
  loadModel()
  animate()
}

function onWindowResize() {
  camera.aspect = window.innerWidth / window.innerHeight
  camera.updateProjectionMatrix()
  renderer.setSize(window.innerWidth, window.innerHeight)
}

function setupLights() {
  // 마당 조명을 위한 사각광원
  const rectLight = new THREE.RectAreaLight(0xffffff, 5, 4, 4)
  rectLight.position.set(0, 5, 10)
  rectLight.lookAt(0, 0, 0)
  scene.add(rectLight)

  // 포인트 라이트 4개 배치 (빛 강조용)
  const pointLight1 = new THREE.PointLight(0xffff66, 400, 5)
  pointLight1.position.set(-8.02, 2.13, -1.24)
  const pointLight2 = new THREE.PointLight(0x66ffff, 400, 5)
  pointLight2.position.set(-3.78, 2.14, -7.52)
  const pointLight3 = new THREE.PointLight(0xff99cc, 400, 5)
  pointLight3.position.set(9.64, 2.09, 3.28)
  const pointLight4 = new THREE.PointLight(0xffffff, 600, 20)
  pointLight4.position.set(5, 15, 10)

  scene.add(pointLight1, pointLight2, pointLight3, pointLight4)
}

function loadHDR() {
  new RGBELoader().load('/3D/background.hdr', (texture) => {
    texture.mapping = THREE.EquirectangularReflectionMapping
    scene.background = texture
    scene.environment = texture
  })
}

function loadModel() {
  const loader = new GLTFLoader()
  loader.load('/3D/hanok_0729_1755.glb', (gltf) => {
    const hanok = gltf.scene
    hanok.scale.set(1.5, 1.5, 1.5) // 모델 크기 조절

    hanok.traverse((child) => {
      if ((child as THREE.Mesh).isMesh) {
        const mesh = child as THREE.Mesh
        mesh.castShadow = true
        mesh.receiveShadow = true
        const material = mesh.material as THREE.MeshStandardMaterial
        if (material) {
          material.envMapIntensity = 1
        }

        // 특정 오브젝트의 그림자 설정 끄기
        if (mesh.name.includes('Ground') || mesh.name.includes('Bench')) {
          mesh.receiveShadow = false
          mesh.castShadow = false
        }
      }
    })

    scene.add(hanok)
    emit('loaded') // 로딩 완료 알림
    createHotspots()
  })
}

function createHotspots() {
  const texture = new THREE.TextureLoader().load('/3D/star.png')

  // 첫번째 핫스팟 - 내서재
  hotspots.push(createHotspot(new THREE.Vector3(-9.5, 2, 1), texture, () => {
    console.log("내서재 핫스팟 클릭됨! 카메라 이동 시작.");
    moveCameraTo(-9.5, 3, 0, -11, 3, 0, () => emit('hotspot', 'library'))
  }))

  // 두번째 핫스팟 - 서점
  hotspots.push(createHotspot(new THREE.Vector3(-2.5, 2, -6), texture, () => {
    console.log("서점 핫스팟 클릭됨! 카메라 이동 시작.");
    moveCameraTo(-2, 2, -6.5, -2, 2, -10.5, () => emit('hotspot', 'store'))
  }))

  // 세번째 핫스팟 - 책만들기
  hotspots.push(createHotspot(new THREE.Vector3(8.5, 2, 3), texture, () => {
    console.log("책 만들기 핫스팟 클릭됨! 카메라 이동 시작.");
    controls.enabled = false;
    const tl = gsap.timeline({
      onUpdate: () => controls.update(),
      onComplete: () => {
        controls.enabled = true;
        emit('hotspot', 'create');
        console.log("책 만들기 핫스팟: 카메라 이동 완료.");
      }
    })
    tl.to(camera.position, { x: 2, y: 3, z: 1, duration: 1 })
    tl.to(controls.target, { x: 10, y: 3, z: 1, duration: 1 }, '<')
    tl.to(camera.position, { x: 8.5, y: 2, z: 1, duration: 3 })
    tl.to(controls.target, { x: 8.5, y: 2, z: -3, duration: 3 }, '<')
  }))

  hotspots.forEach(h => scene.add(h))
}

function createHotspot(pos: THREE.Vector3, texture: THREE.Texture, onClick: () => void) {
  const material = new THREE.SpriteMaterial({ map: texture })
  const sprite = new THREE.Sprite(material)
  sprite.scale.set(1, 1, 1)
  sprite.position.copy(pos)
  sprite.userData.onClick = onClick; // userData에 onClick 함수 저장
  return sprite
}

function moveCameraTo(px: number, py: number, pz: number, tx: number, ty: number, tz: number, onCompleteCallback?: () => void) {
  controls.enabled = false;
  const tl = gsap.timeline({
    onUpdate: () => controls.update(),
    onComplete: () => {
      controls.enabled = true;
      if (onCompleteCallback) {
        onCompleteCallback();
      }
      console.log("moveCameraTo: 카메라 이동 완료.");
    }
  })
  tl.to(camera.position, { x: px, y: py, z: pz, duration: 2 })
  tl.to(controls.target, { x: tx, y: ty, z: tz, duration: 2 }, '<')
}

function moveToYard() {
    console.log("moveToYard: 마당으로 카메라 이동 시작.");
    controls.enabled = false;
    const tl = gsap.timeline({
        onUpdate: () => controls.update(),
        onComplete: () => {
            controls.enabled = true;
            console.log("moveToYard: 마당으로 카메라 이동 완료.");
        }
    });
    tl.to(camera.position, { x: 7.3, y: 2.5, z: 30, duration: 2 });
    tl.to(controls.target, { x: 7.3, y: 2.5, z: 0, duration: 2 }, "<");
    tl.to(camera.position, { x: 5.5, y: 2.5, z: 14, duration: 2 });
    tl.to(controls.target, { x: 1, y: 3, z: 3.579, duration: 2 }, "<");
}

function animate() {
  animationFrameId = requestAnimationFrame(animate)
  renderer.render(scene, camera)
}

function onCanvasClick(event: MouseEvent) {
  console.log("--- 캔버스 클릭 감지 ---");

  if (!container.value) return;

  // 캔버스의 경계 정보를 가져옵니다.
  const rect = container.value.getBoundingClientRect();

  // 캔버스 기준의 정확한 마우스 좌표를 계산합니다.
  const mouse = new THREE.Vector2(
    ((event.clientX - rect.left) / rect.width) * 2 - 1,
    -((event.clientY - rect.top) / rect.height) * 2 + 1
  );

  console.log(`계산된 마우스 좌표: x=${mouse.x.toFixed(2)}, y=${mouse.y.toFixed(2)}`);

  const raycaster = new THREE.Raycaster()
  raycaster.setFromCamera(mouse, camera)

  const intersects = raycaster.intersectObjects(hotspots, true)
  console.log(`Raycaster가 ${intersects.length}개의 객체와 교차함.`);

  if (intersects.length > 0) {
    const firstIntersect = intersects[0].object;
    console.log("교차된 객체:", firstIntersect);
    if (firstIntersect.userData && typeof firstIntersect.userData.onClick === 'function') {
      console.log("객체의 userData에서 onClick 함수를 발견하여 실행합니다.");
      firstIntersect.userData.onClick();
    } else {
      console.log("오류: 교차된 객체의 userData에 onClick 함수가 없습니다.");
    }
  } else {
    console.log("교차된 객체가 없습니다.");
  }
}
</script>

<style scoped>
canvas {
  display: block;
}
</style>
