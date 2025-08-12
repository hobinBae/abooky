<template>
  <div ref="container" style="width: 100vw; height: 100vh"></div>
</template>

<script setup lang="ts">
import * as THREE from 'three'
import { ref, onMounted, defineEmits, onUnmounted, defineExpose, onActivated, onDeactivated } from 'vue'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { GLTFLoader, type GLTF } from 'three/examples/jsm/loaders/GLTFLoader.js'
import { KTX2Loader } from 'three/examples/jsm/loaders/KTX2Loader.js'
import { MeshoptDecoder } from 'three/examples/jsm/libs/meshopt_decoder.module.js'
import { RGBELoader } from 'three/examples/jsm/loaders/RGBELoader.js'
import { RectAreaLightHelper } from 'three/examples/jsm/helpers/RectAreaLightHelper.js';
import { RectAreaLightUniformsLib } from 'three/examples/jsm/lights/RectAreaLightUniformsLib.js'


import { gsap } from 'gsap'

const emit = defineEmits(['loaded', 'hotspot', 'background-loaded', 'yard-animation-finished'])

defineExpose({ moveToYard, moveCameraTo, loadModel, getHotspotByName });

const container = ref<HTMLDivElement | null>(null)
let camera: THREE.PerspectiveCamera
let controls: OrbitControls
let scene: THREE.Scene
let renderer: THREE.WebGLRenderer
const hotspots: THREE.Object3D[] = []

let animationFrameId: number;
const isAnimating = ref(false);

const startAnimation = () => {
  if (!isAnimating.value) {
    isAnimating.value = true;
    animate();
  }
};

const stopAnimation = () => {
  if (isAnimating.value) {
    cancelAnimationFrame(animationFrameId);
    isAnimating.value = false;
  }
};

onMounted(() => {
  initScene();
  startAnimation();
});

onUnmounted(() => {
  window.removeEventListener('resize', onWindowResize);
  container.value?.removeEventListener('click', onCanvasClick);
  container.value?.removeEventListener('mousemove', onMouseMove);
  stopAnimation();
});

onActivated(() => {
  startAnimation();
});

onDeactivated(() => {
  stopAnimation();
});

function initScene() {
  scene = new THREE.Scene()

  camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 0.1, 1000)
  camera.position.set(7.3, 25, 30)

  renderer = new THREE.WebGLRenderer({ antialias: true, powerPreference: 'high-performance' })
  renderer.setPixelRatio(window.devicePixelRatio)
  renderer.setSize(window.innerWidth, window.innerHeight)
  renderer.outputColorSpace = THREE.SRGBColorSpace
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.2



  container.value?.appendChild(renderer.domElement)
  container.value?.addEventListener('click', onCanvasClick);
  container.value?.addEventListener('mousemove', onMouseMove);

  controls = new OrbitControls(camera, renderer.domElement)
  controls.target.set(0.3, 35, 25)
  controls.update()

  window.addEventListener('resize', onWindowResize)

  RectAreaLightUniformsLib.init()
  setupLights()
  loadHDR()
  // loadModel() // 자동 모델 로딩 제거
}

function onWindowResize() {
  camera.aspect = window.innerWidth / window.innerHeight
  camera.updateProjectionMatrix()
  renderer.setSize(window.innerWidth, window.innerHeight)
}

function setupLights() {
  // 디렉셔널 라이트 설정
  const dirLight = new THREE.DirectionalLight(0xffffff, 1.5);
  dirLight.position.set(30, 30, 10);
  dirLight.castShadow = true;
  dirLight.shadow.mapSize.width = 512;
  dirLight.shadow.mapSize.height = 512;
  dirLight.shadow.camera.near = 1;
  dirLight.shadow.camera.far = 100;
  dirLight.shadow.camera.left = -100;
  dirLight.shadow.camera.right = 100;
  dirLight.shadow.camera.top = 100;
  dirLight.shadow.camera.bottom = -100;
  scene.add(dirLight);

  // RectAreaLight
  // const areaLight = new THREE.RectAreaLight(0xeba64d, 12, 2.28, 2.28);
  // areaLight.position.set(7.3, 5, 25);
  // areaLight.rotation.set(
  //   THREE.MathUtils.degToRad(91.174),
  //   THREE.MathUtils.degToRad(180.48163),
  //   THREE.MathUtils.degToRad(-0.75941)
  // );
  // scene.add(areaLight);

  // PointLight 1
  const point1 = new THREE.PointLight(0xf0ab43, 10, 15);
  point1.position.set(-12, 3, -6.6);
  scene.add(point1);
  // scene.add(new THREE.PointLightHelper(point1));

  // PointLight 1a
  const point1a = new THREE.PointLight(0xf0ab43, 10, 15);
  point1a.position.set(-12, 3, 2);
  scene.add(point1a);
  // scene.add(new THREE.PointLightHelper(point1a));

  // 방 안에서 나오는 불빛을 위한 RectAreaLight 4개 추가
  // const windowLight1 = new THREE.RectAreaLight(0xffd580, 0.8, 2, 2.3);
  // windowLight1.position.set(-8, 2.5, -4);
  // windowLight1.lookAt(-8, 2.5, -5);
  // scene.add(windowLight1);
  // // scene.add(new RectAreaLightHelper(windowLight1));

  // const windowLight2 = new THREE.RectAreaLight(0xffd580, 0.8, 2, 2.3);
  // windowLight2.position.set(-5, 2.5, -4.2);
  // windowLight2.lookAt(-5, 2.5, -5);
  // scene.add(windowLight2);
  // // scene.add(new RectAreaLightHelper(windowLight2));

  // const windowLight3 = new THREE.RectAreaLight(0xffd580, 1, 1.3, 1.9);
  // windowLight3.position.set(1.55, 2.25, -4.3);
  // windowLight3.lookAt(1.55, 2.25, -5);
  // scene.add(windowLight3);
  // // scene.add(new RectAreaLightHelper(windowLight3));

  // const windowLight4 = new THREE.RectAreaLight(0xffd580, 1, 1.3, 1.9);
  // windowLight4.position.set(4.8, 2.25, -4.3);
  // windowLight4.lookAt(4.8, 2.25, -5);
  // scene.add(windowLight4);
  // // scene.add(new RectAreaLightHelper(windowLight4));

  // PointLight 4
  const point4 = new THREE.PointLight(0xf0ab43, 10, 15);
  point4.position.set(8.5, 3, -0.33191);
  scene.add(point4);
  // scene.add(new THREE.PointLightHelper(point4));
}

function loadHDR() {
  new RGBELoader().load('/3D/background.hdr', (texture: THREE.Texture) => {
    texture.mapping = THREE.EquirectangularReflectionMapping
    scene.background = texture
    scene.environment = texture
    emit('background-loaded') // 배경 로딩 완료 이벤트 발생
  })
}

function loadModel() {
  const ktx2Loader = new KTX2Loader()
    .setTranscoderPath('https://cdn.jsdelivr.net/gh/mrdoob/three.js@r165/examples/jsm/libs/basis/')
    .detectSupport(renderer)

  const loader = new GLTFLoader()
  loader.setKTX2Loader(ktx2Loader)
  loader.setMeshoptDecoder(MeshoptDecoder)

  loader.load('/3D/hanok_250806.glb', (gltf: GLTF) => {
    const hanok = gltf.scene
    hanok.scale.set(1.5, 1.5, 1.5) // 모델 크기 조절

    hanok.traverse((child: THREE.Object3D) => {
      if ((child as THREE.Mesh).isMesh) {
        const mesh = child as THREE.Mesh
        mesh.castShadow = true
        mesh.receiveShadow = true
        const material = mesh.material as THREE.MeshStandardMaterial
        if (material instanceof THREE.MeshStandardMaterial) {
          material.envMap = scene.environment;
          material.envMapIntensity = 0.4;
        }

        if (mesh.name.includes("Ground")) {
          material.color.multiplyScalar(0.4);
          mesh.castShadow = false;
        }
        if (mesh.name.includes("Bench")) {
          mesh.receiveShadow = false;
        }
      }
    })

    scene.add(hanok)

    // 셰이더 예열을 통해 카메라 이동 시 버벅임 현상 방지
    prewarmShaders(hanok);

    emit('loaded') // 로딩 및 예열 완료 알림
    createHotspots()
  })
}

function prewarmShaders(model: THREE.Object3D) {
  console.log("셰이더 예열 시작...");
  model.traverse((child: THREE.Object3D) => {
    if ((child as THREE.Mesh).isMesh) {
      // Frustum culling을 일시적으로 비활성화하여 모든 객체가 렌더링되도록 강제
      child.frustumCulled = false;
    }
  });

  // 한 프레임을 렌더링하여 모든 셰이더를 컴파일
  renderer.render(scene, camera);

  // Frustum culling을 다시 활성화하여 성능 최적화
  model.traverse((child: THREE.Object3D) => {
    if ((child as THREE.Mesh).isMesh) {
      child.frustumCulled = true;
    }
  });
  console.log("셰이더 예열 완료.");
}

function createHotspots() {
  const texture = new THREE.TextureLoader().load('/3D/star.png')

  // 첫번째 핫스팟 - 내서재
  hotspots.push(createHotspot(new THREE.Vector3(-9.5, 2, 1), texture, 'library', () => {
    console.log("내서재 핫스팟 클릭됨! 카메라 이동 시작.");
    moveCameraTo(-9.5, 3, 0, -11, 3, 0, () => {
      emit('hotspot', 'library')
    })
  }))

  // 두번째 핫스팟 - 서점
  hotspots.push(createHotspot(new THREE.Vector3(-2.5, 2, -6), texture, 'store', () => {
    console.log("서점 핫스팟 클릭됨! 카메라 이동 시작.");
    moveCameraTo(-2, 2, -6.5, -2, 2, -10.5, () => {
      emit('hotspot', 'store')
    })
  }))

  // 세번째 핫스팟 - 책만들기
  hotspots.push(createHotspot(new THREE.Vector3(8.5, 2, 3), texture, 'create', () => {
    console.log("책 만들기 핫스팟 클릭됨! 카메라 이동 시작.");
    controls.enabled = false;
    const tl = gsap.timeline({
      onUpdate: () => { controls.update() },
      onComplete: () => {
        controls.enabled = true;
        emit('hotspot', 'create');
        console.log("책 만들기 핫스팟: 카메라 이동 완료.");
      }
    })
    tl.to(camera.position, { x: 2, y: 3, z: 1, duration: 1 })
    tl.to(controls.target, { x: 10, y: 3, z: 1, duration: 1 }, '<')
    tl.to(camera.position, { x: 8.5, y: 2, z: 1, duration: 3 })
    tl.to(controls.target, { x: 8.5, y: 2, z: -3, duration: 3 }, '<');
  }))

  hotspots.forEach(h => scene.add(h))
}

function createHotspot(pos: THREE.Vector3, texture: THREE.Texture, name: string, onClick: () => void) {
  const material = new THREE.SpriteMaterial({ map: texture })
  const sprite = new THREE.Sprite(material)
  sprite.scale.set(1, 1, 1)
  sprite.position.copy(pos)
  sprite.name = name; // hotspot에 이름 할당
  sprite.userData.onClick = onClick;
  return sprite
}

function getHotspotByName(name: string) {
  return hotspots.find(h => h.name === name);
}

function moveCameraTo(px: number, py: number, pz: number, tx: number, ty: number, tz: number, onCompleteCallback?: () => void) {
  controls.enabled = false;
  const tl = gsap.timeline({
    onUpdate: () => { controls.update() },
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
        onUpdate: () => { controls.update() },
        onComplete: () => {
            controls.enabled = true;
            console.log("moveToYard: 마당으로 카메라 이동 완료.");
            emit('yard-animation-finished');
        }
    });
    tl.to(camera.position, { x: 7.3, y: 2.5, z: 30, duration: 4 });
    tl.to(controls.target, { x: 7.3, y: 2.5, z: 0, duration: 4 }, "<");
    tl.to(camera.position, { x: 5.5, y: 2.5, z: 14, duration: 2 });
    tl.to(controls.target, { x: 1, y: 3, z: 3.579, duration: 2 }, "<");
}

function animate() {
  if (!isAnimating.value) return;
  animationFrameId = requestAnimationFrame(animate);
  renderer.render(scene, camera);
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

let hoveredHotspot: THREE.Object3D | null = null;

function onMouseMove(event: MouseEvent) {
  if (!container.value) return;

  const rect = container.value.getBoundingClientRect();
  const mouse = new THREE.Vector2(
    ((event.clientX - rect.left) / rect.width) * 2 - 1,
    -((event.clientY - rect.top) / rect.height) * 2 + 1
  );

  const raycaster = new THREE.Raycaster();
  raycaster.setFromCamera(mouse, camera);

  const intersects = raycaster.intersectObjects(hotspots, true);

  if (intersects.length > 0) {
    const firstIntersect = intersects[0].object;
    if (hoveredHotspot !== firstIntersect) {
      // 이전에 호버된 핫스팟이 있으면 원래 크기로 되돌림
      if (hoveredHotspot) {
        gsap.to(hoveredHotspot.scale, { x: 1, y: 1, z: 1, duration: 0.3 });
      }
      // 새로 호버된 핫스팟을 확대
      hoveredHotspot = firstIntersect;
      gsap.to(hoveredHotspot.scale, { x: 1.2, y: 1.2, z: 1.2, duration: 0.3 });
    }
  } else {
    // 호버된 핫스팟이 없으면 이전에 호버된 핫스팟을 원래 크기로 되돌림
    if (hoveredHotspot) {
      gsap.to(hoveredHotspot.scale, { x: 1, y: 1, z: 1, duration: 0.3 });
      hoveredHotspot = null;
    }
  }
}
</script>

<style scoped>
canvas {
  display: block;
}
</style>
