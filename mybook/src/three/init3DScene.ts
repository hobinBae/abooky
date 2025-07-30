// init3DScene.ts
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader'
import { RGBELoader } from 'three/examples/jsm/loaders/RGBELoader'
import { gsap } from 'gsap'
import { RectAreaLightUniformsLib } from 'three/examples/jsm/lights/RectAreaLightUniformsLib'

let camera: THREE.PerspectiveCamera
let scene: THREE.Scene
let renderer: THREE.WebGLRenderer
let controls: OrbitControls
let hanok: THREE.Object3D
let gate: THREE.Object3D | null = null
let raycaster = new THREE.Raycaster()
let mouse = new THREE.Vector2()
let returnButton: HTMLImageElement
let enterButton: HTMLButtonElement
const hotspots: THREE.Sprite[] = []
let gateOpened = false

export default function init3DScene(container: HTMLElement, onEnter: () => void, onHotspotNavigate: (index: number) => void) {
  scene = new THREE.Scene()

  camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 0.1, 1000)
  camera.position.set(6, 10.5, 38)

  renderer = new THREE.WebGLRenderer({ antialias: true })
  renderer.setSize(window.innerWidth, window.innerHeight)
  renderer.outputEncoding = THREE.sRGBEncoding
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 0.8
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  container.appendChild(renderer.domElement)

  controls = new OrbitControls(camera, renderer.domElement)
  controls.target.set(6, 15, 0)
  controls.update()

  new RGBELoader().setPath('/public/3D/').load('background.hdr', (texture) => {
    texture.mapping = THREE.EquirectangularReflectionMapping
    scene.environment = texture
    scene.background = texture
  })

  const dirLight = new THREE.DirectionalLight(0xffffff, 1)
  dirLight.position.set(30, 30, 10)
  dirLight.castShadow = true
  scene.add(dirLight)

  RectAreaLightUniformsLib.init()

  const areaLight = new THREE.RectAreaLight(0xeba64d, 12, 2.28, 2.28);
  areaLight.position.set(7.3, 5, 25);
  areaLight.rotation.set(
    THREE.MathUtils.degToRad(91.174),
    THREE.MathUtils.degToRad(180.48163),
    THREE.MathUtils.degToRad(-0.75941)
  );
  scene.add(areaLight);

  const pointLights = [
    [-12, 3, -6.6],
    [-12, 3, 2],
    [-7, 2, -4],
    [3.5, 2, -4.4],
    [9, 3, -0.33191]
  ]
  pointLights.forEach(([x, y, z]) => {
    const pl = new THREE.PointLight(0xf0ab43, 3, 9)
    pl.position.set(x, y, z)
    scene.add(pl)
  })

  new GLTFLoader().load('/public/3D/hanok_0729_1755.glb', gltf => {
    hanok = gltf.scene
    hanok.traverse(child => {
      if ((child as THREE.Mesh).isMesh && (child as THREE.Mesh).material) {
        const mesh = child as THREE.Mesh
        mesh.castShadow = true
        mesh.receiveShadow = true
        mesh.material.envMapIntensity = 0.3
        if (child.name.includes("Ground")) {
          mesh.material.color.multiplyScalar(0.4)
          mesh.castShadow = false
        }
        if (child.name.includes("Bench")) {
          mesh.receiveShadow = false
        }
      }
    })
    hanok.scale.set(1.5, 1.5, 1.5)
    scene.add(hanok)

    gate = hanok.getObjectByName("Gate")

    createHotspot(new THREE.Vector3(-9.5, 2, 1), () => {
      moveCameraTo(-9.5, 3, 0, -11, 3, 0)
      onHotspotNavigate(0)
    })
    createHotspot(new THREE.Vector3(-2.5, 2, -6), () => {
      moveCameraTo(-2, 2, -6.5, -2, 2, -10.5)
      onHotspotNavigate(1)
    })
    createHotspot(new THREE.Vector3(8.5, 2, 3), () => {
      showReturnButton()
      const tl = gsap.timeline({ onUpdate: () => controls.update(), onComplete: () => controls.update() })
      tl.to(camera.position, { x: 2, y: 3, z: 1, duration: 1 })
      tl.to(controls.target, { x: 10, y: 3, z: 1, duration: 1 }, "<")
      tl.to(camera.position, { x: 8.5, y: 2, z: 1, duration: 3 })
      tl.to(controls.target, { x: 8.5, y: 2, z: -3, duration: 3 }, "<")
      tl.call(() => onHotspotNavigate(2))
    })
  })

  createEnterButton(onEnter)
  createReturnButton()

  window.addEventListener('click', onClick)
  window.addEventListener('resize', onWindowResize)

  animate()
}

function createHotspot(position: THREE.Vector3, onClick: () => void) {
  const spriteMap = new THREE.TextureLoader().load('/public/3D/star.png')
  const sprite = new THREE.Sprite(new THREE.SpriteMaterial({ map: spriteMap }))
  sprite.scale.set(1, 1, 1)
  sprite.position.copy(position)
  sprite.userData.onClick = () => {
    showReturnButton()
    onClick()
  }
  scene.add(sprite)
  hotspots.push(sprite)
}

function showNavigationButton(text: string, route: string) {
  const button = document.createElement('button');
  button.textContent = text;
  button.style.cssText = `
    position: absolute;
    bottom: 100px;
    left: 50%;
    transform: translateX(-50%);
    padding: 12px 24px;
    font-size: 1rem;
    border-radius: 6px;
    border: none;
    background: #2d5c2f;
    color: #fff;
    cursor: pointer;
    z-index: 200;
  `;

  button.addEventListener('click', () => {
    window.location.href = route; // Vue Router를 사용할 경우 replace로 대체 가능
  });

  document.body.appendChild(button);
}


function moveCameraTo(x: number, y: number, z: number, tx: number, ty: number, tz: number) {
  gsap.to(camera.position, {
    x, y, z,
    duration: 2.5,
    onUpdate: () => controls.update(),
    onComplete: () => controls.update()
  })
  gsap.to(controls.target, {
    x: tx, y: ty, z: tz,
    duration: 2.5,
    onUpdate: () => controls.update(),
    onComplete: () => controls.update()
  })
}

function createReturnButton() {
  returnButton = document.createElement('img')
  returnButton.src = '/public/3D/back_icon.png'
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
  `
  returnButton.addEventListener('click', () => {
    moveCameraTo(5.5, 2.5, 14, 1, 3, 3.579)
    hideReturnButton()
  })
  document.body.appendChild(returnButton)
}

function createEnterButton(onEnter: () => void) {
  enterButton = document.createElement('button')
  enterButton.textContent = '들어가기'
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
  `
  enterButton.addEventListener('click', () => {
    enterButton.style.display = 'none'
    const tl = gsap.timeline({ onUpdate: () => controls.update(), onComplete: () => controls.update() })
    tl.to(camera.position, { x: 7.3, y: 2.5, z: 30, duration: 2 })
    tl.to(controls.target, { x: 7.3, y: 2.5, z: 0, duration: 2 }, "<")
    tl.to(camera.position, { x: 5.5, y: 2.5, z: 14, duration: 2 })
    tl.to(controls.target, { x: 1, y: 3, z: 3.579, duration: 2 }, "<")
    tl.call(onEnter)
  })
  document.body.appendChild(enterButton)
}

function showReturnButton() {
  if (returnButton) returnButton.style.display = 'block'
}
function hideReturnButton() {
  if (returnButton) returnButton.style.display = 'none'
}

function onClick(event: MouseEvent) {
  mouse.x = (event.clientX / window.innerWidth) * 2 - 1
  mouse.y = -(event.clientY / window.innerHeight) * 2 + 1
  raycaster.setFromCamera(mouse, camera)
  const intersects = raycaster.intersectObjects([...scene.children, ...hotspots], true)
  for (const intersect of intersects) {
    const obj = intersect.object
    if (hotspots.includes(obj as THREE.Sprite) && (obj as any).userData.onClick) {
      obj.userData.onClick()
      return
    }
    if ((obj === gate || gate?.children.includes(obj)) && !gateOpened) {
      gsap.to(gate.rotation, { y: -Math.PI / 2, duration: 1 })
      gateOpened = true
      return
    }
    if (!hotspots.includes(obj as THREE.Sprite) && gateOpened) {
      moveCameraTo(5.5, 2.5, 14, 1, 3, 3.579)
      gateOpened = false
      return
    }
  }
}

function onWindowResize() {
  camera.aspect = window.innerWidth / window.innerHeight
  camera.updateProjectionMatrix()
  renderer.setSize(window.innerWidth, window.innerHeight)
}

function animate() {
  requestAnimationFrame(animate)
  renderer.render(scene, camera)
}
