import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls';
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader';
import { RGBELoader } from 'three/examples/jsm/loaders/RGBELoader';
import { gsap } from 'gsap';
import { RectAreaLightHelper } from 'three/examples/jsm/helpers/RectAreaLightHelper.js';
import { RectAreaLightUniformsLib } from 'three/examples/jsm/lights/RectAreaLightUniformsLib.js';


let camera, scene, renderer, controls;
let hanok, gate;
const raycaster = new THREE.Raycaster();
const mouse = new THREE.Vector2();
const hotspots = [];
let returnButton;

init();
createEnterButton();
animate();

// 초기 설정 및 장면 구성
function init() {
  scene = new THREE.Scene();
  scene.background = new THREE.Color(0xa0a0a0);

  camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 0.1, 1000);
  // 카메라 초기 위치 설정
  camera.position.set(6,10.5,38);

  // 렌더러 설정
  renderer = new THREE.WebGLRenderer({ antialias: true });
  renderer.setSize(window.innerWidth, window.innerHeight);
  // 추가: 밝기 및 색감 보정
  renderer.outputEncoding = THREE.sRGBEncoding;
  renderer.toneMapping = THREE.ACESFilmicToneMapping;
  renderer.toneMappingExposure = 0.8;
  renderer.shadowMap.enabled = true;
  renderer.shadowMap.type = THREE.PCFSoftShadowMap;
  document.body.appendChild(renderer.domElement);

  // 컨트롤 타겟(카메라가 바라보는 지점) 설정
  controls = new OrbitControls(camera, renderer.domElement);
  controls.target.set(6, 15, 0);
  controls.update();

  // HDR 환경맵 로드 및 적용
  new RGBELoader().setPath('/public/3D/').load('background.hdr', (texture) => {   // HDR 파일 경로
      texture.mapping = THREE.EquirectangularReflectionMapping;  // 환경 반사에 사용
      scene.environment = texture;   // PBR 머티리얼에 반사/빛 반응 적용
      scene.background = texture;    // 배경에도 적용
    });
    
  // 디렉셔널 라이트 설정
  const dirLight = new THREE.DirectionalLight(0xffffff, 1);
  dirLight.position.set(30, 30, 10);
  dirLight.castShadow = true;
  dirLight.shadow.mapSize.width = 2048;
  dirLight.shadow.mapSize.height = 2048;
  dirLight.shadow.camera.near = 1;
  dirLight.shadow.camera.far = 100;
  dirLight.shadow.camera.left = -100;
  dirLight.shadow.camera.right = 100;
  dirLight.shadow.camera.top = 100;
  dirLight.shadow.camera.bottom = -100;
  scene.add(dirLight);
  // 디렉셔널 라이트 위치 시각화용 구체
  // const dirLightHelper = new THREE.Mesh(
  //   new THREE.SphereGeometry(0.5, 16, 16),
  //   new THREE.MeshBasicMaterial({ color: 0xff0000 })
  // );
  // dirLightHelper.position.copy(dirLight.position);
  // scene.add(dirLightHelper);


  // Blender에서 설정한 광원들 직접 반영
  RectAreaLightUniformsLib.init();
  // RectAreaLight
  const areaLight = new THREE.RectAreaLight(0xeba64d, 12, 2.28, 2.28);
  areaLight.position.set(7.3, 5, 25);
  areaLight.rotation.set(
    THREE.MathUtils.degToRad(91.174),
    THREE.MathUtils.degToRad(180.48163),
    THREE.MathUtils.degToRad(-0.75941)
  );
  scene.add(areaLight);
  // scene.add(new RectAreaLightHelper(areaLight));

  // PointLight 1
  const point1 = new THREE.PointLight(0xf0ab43, 3, 9);
  point1.position.set(-12, 3, -6.6);
  scene.add(point1);
  // scene.add(new THREE.PointLightHelper(point1));

  // PointLight 1a
  const point1a = new THREE.PointLight(0xf0ab43, 3, 9);
  point1a.position.set(-12, 3, 2);
  scene.add(point1a);
  // scene.add(new THREE.PointLightHelper(point1a));

  // PointLight 2
  const point2 = new THREE.PointLight(0xf0ab43, 3, 7);
  point2.position.set(-7, 2, -4);
  scene.add(point2);
  // scene.add(new THREE.PointLightHelper(point2));

  // PointLight 3
  const point3 = new THREE.PointLight(0xf0ab43, 3, 7);
  point3.position.set(3.5, 2, -4.4);
  scene.add(point3);
  // scene.add(new THREE.PointLightHelper(point3));

  // PointLight 4
  const point4 = new THREE.PointLight(0xf0ab43, 3, 7);
  point4.position.set(9, 3, -0.33191);
  scene.add(point4);
  // scene.add(new THREE.PointLightHelper(point4));

  // 모델 로드 및 핫스팟 배치
  new GLTFLoader().load('/public/3D/hanok_0729_1755.glb', gltf => {
    hanok = gltf.scene;

    hanok.traverse(child => {
      if (child.isMesh && child.material) {
        // child.material.side = THREE.DoubleSide;
        child.castShadow = true;
        child.receiveShadow = true;

        // 밝기 조정
        // child.material.color.multiplyScalar(1.2);

        // 환경 반사 강도 조절
        child.material.envMapIntensity = 0.3;

        // 거칠기 / 메탈 조절
        // child.material.roughness = 0.5;
        // child.material.metalness = 0.0;

        if (child.name.includes("Ground")) {
          child.material.color.multiplyScalar(0.4); // 오브젝트 밝기 설정
          child.castShadow = false; // 바닥 그림자
          // child.receiveShadow = false; // 바닥 그림자
        } 
        if (child.name.includes("Bench")) {
          // child.material.color.multiplyScalar(1.1);
          // child.castShadow = false; // 바닥 그림자
          child.receiveShadow = false; // 바닥 그림자
        }
      }
    });

    hanok.scale.set(1.5, 1.5, 1.5);
    scene.add(hanok);

    createHotspot(new THREE.Vector3(-9.5, 2, 1), () => moveCameraTo(-9.5, 3, 0, -11, 3, 0));
    createHotspot(new THREE.Vector3(-2.5, 2, -6), () => moveCameraTo(-2, 2, -6.5, -2, 2, -10.5));
    createHotspot(new THREE.Vector3(8.5, 2, 3), () => {
      showReturnButton();
      const tl = gsap.timeline({ onUpdate: () => controls.update(), onComplete: () => controls.update() });
      tl.to(camera.position, { x: 2, y: 3, z: 1, duration: 1 });
      tl.to(controls.target, { x: 10, y: 3, z: 1, duration: 1 }, "<");
      tl.to(camera.position, { x: 8.5, y: 2, z: 1, duration: 3 });
      tl.to(controls.target, { x: 8.5, y: 2, z: -3, duration: 3 }, "<");
    });
  });

  createReturnButton();
  window.addEventListener('click', onClick);
  window.addEventListener('resize', onWindowResize);
}

// 핫스팟(별 아이콘) 생성 함수
function createHotspot(position, onClick) {
  const spriteMap = new THREE.TextureLoader().load('/public/3D/star.png');
  const sprite = new THREE.Sprite(new THREE.SpriteMaterial({ map: spriteMap }));
  sprite.scale.set(1, 1, 1);
  sprite.position.copy(position);
  sprite.userData.onClick = () => {
    showReturnButton();
    onClick();
  };
  scene.add(sprite);
  hotspots.push(sprite);
}

// 카메라와 컨트롤 타겟 이동 함수
function moveCameraTo(x, y, z, tx, ty, tz) {
  gsap.to(camera.position, {
    x, y, z,
    duration: 2.5,
    onUpdate: () => controls.update(),
    onComplete: () => controls.update()
  });

  gsap.to(controls.target, {
    x: tx, y: ty, z: tz,
    duration: 2.5,
    onUpdate: () => controls.update(),
    onComplete: () => controls.update()
  });
}

// 돌아가기 버튼 생성 및 스타일 적용
function createReturnButton() {
  returnButton = document.createElement('img');
  returnButton.src = '/public/3D/back_icon.png';
  returnButton.style.cssText = `
    position: absolute;
    bottom: 20px;
    left: 50%;
    transform: translateX(-50%);
    width: 60px;
    height: 60px;
    cursor: pointer;
    z-index: 100;
    display: none;
  `;
  returnButton.addEventListener('click', () => {
    moveCameraTo(5.5, 2.5, 14, 1, 3, 3.579); // 항상 마당 위치로 복귀
    hideReturnButton();
  });
  document.body.appendChild(returnButton);
}

// 들어가기 버튼 생성
function createEnterButton() {
  const enterButton = document.createElement('button');
  enterButton.textContent = '들어가기';
  enterButton.style.cssText = `
    position: absolute;
    top: 350px;
    left: 50%;
    transform: translateX(-50%);
    padding: 16px 32px;
    font-size: 1.2rem;
    border-radius: 8px;
    border: none;
    background: #2d5c2f;
    color: #fff;
    cursor: pointer;
    z-index: 200;
  `;
  enterButton.addEventListener('click', () => {
    enterButton.style.display = 'none';
    const tl = gsap.timeline({ onUpdate: () => controls.update(), onComplete: () => controls.update() });
    tl.to(camera.position, { x: 7.3, y: 2.5, z: 30, duration: 2 });
    tl.to(controls.target, { x: 7.3, y: 2.5, z: 0, duration: 2 }, "<");
    tl.to(camera.position, { x: 5.5, y: 2.5, z: 14, duration: 2 });
    tl.to(controls.target, { x: 1, y: 3, z: 3.579, duration: 2 }, "<");
  });
  document.body.appendChild(enterButton);
}

// 돌아가기 버튼 표시
function showReturnButton() {
  if (returnButton) returnButton.style.display = 'block';
}
// 돌아가기 버튼 숨김
function hideReturnButton() {
  if (returnButton) returnButton.style.display = 'none';
}

// 클릭 이벤트 처리
function onClick(event) {
  mouse.x = (event.clientX / window.innerWidth) * 2 - 1;
  mouse.y = -(event.clientY / window.innerHeight) * 2 + 1;
  raycaster.setFromCamera(mouse, camera);
  const intersects = raycaster.intersectObjects([...scene.children, ...hotspots], true);
  for (const intersect of intersects) {
    const obj = intersect.object;
    // 핫스팟 클릭 시 해당 핫스팟의 onClick 실행
    if (hotspots.includes(obj) && obj.userData.onClick) {
      obj.userData.onClick();
      return;
    }
  }
}

// 창 크기 변경 시 카메라 비율 및 렌더러 크기 갱신
function onWindowResize() {
  camera.aspect = window.innerWidth / window.innerHeight;
  camera.updateProjectionMatrix();
  renderer.setSize(window.innerWidth, window.innerHeight);
}

// 매 프레임마다 장면 렌더링 (애니메이션 루프)
function animate() {
  requestAnimationFrame(animate);
  renderer.render(scene, camera);
}