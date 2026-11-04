/* ============================================================
   HandOver.AI - lightweight Three.js knowledge-node background.
   Used on the landing and login pages. Respects the OS reduced
   motion preference and the explicit "Motion" toggle.
   ============================================================ */
(function () {
    const canvas = document.getElementById('bg3d');
    if (!canvas || typeof THREE === 'undefined') return;

    const prefersReducedMotion = window.matchMedia &&
        window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    const stored = localStorage.getItem('ho-motion');
    let motionEnabled = stored === 'on' || (stored !== 'off' && !prefersReducedMotion);

    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(60, 1, 0.1, 1000);
    camera.position.z = 16;

    const renderer = new THREE.WebGLRenderer({ canvas: canvas, alpha: true, antialias: true });
    renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));

    const nodeGeometry = new THREE.SphereGeometry(0.09, 12, 12);
    const nodeMaterial = new THREE.MeshBasicMaterial({ color: 0x2dd4bf, transparent: true, opacity: 0.75 });
    const nodes = [];
    const positions = [];
    const COUNT = 46;

    for (let i = 0; i < COUNT; i++) {
        const mesh = new THREE.Mesh(nodeGeometry, nodeMaterial);
        mesh.position.set((Math.random() - 0.5) * 26, (Math.random() - 0.5) * 15, (Math.random() - 0.5) * 10);
        mesh.userData.speed = 0.0015 + Math.random() * 0.003;
        mesh.userData.phase = Math.random() * Math.PI * 2;
        scene.add(mesh);
        nodes.push(mesh);
        positions.push(mesh.position);
    }

    // Connect nearby nodes with faint lines.
    const linePositions = [];
    for (let i = 0; i < positions.length; i++) {
        for (let j = i + 1; j < positions.length; j++) {
            if (positions[i].distanceTo(positions[j]) < 5.2) {
                linePositions.push(
                    positions[i].x, positions[i].y, positions[i].z,
                    positions[j].x, positions[j].y, positions[j].z
                );
            }
        }
    }
    const lineGeometry = new THREE.BufferGeometry();
    lineGeometry.setAttribute('position', new THREE.Float32BufferAttribute(linePositions, 3));
    const lines = new THREE.LineSegments(
        lineGeometry,
        new THREE.LineBasicMaterial({ color: 0x2dd4bf, transparent: true, opacity: 0.16 })
    );
    scene.add(lines);

    function resize() {
        const width = canvas.clientWidth || window.innerWidth;
        const height = canvas.clientHeight || window.innerHeight;
        renderer.setSize(width, height, false);
        camera.aspect = width / Math.max(height, 1);
        camera.updateProjectionMatrix();
    }

    let elapsed = 0;
    let frameId = null;
    function renderFrame() {
        elapsed += 0.01;
        if (motionEnabled) {
            nodes.forEach((n) => {
                n.position.y += Math.sin(elapsed + n.userData.phase) * n.userData.speed;
                n.position.x += Math.cos(elapsed * 0.7 + n.userData.phase) * n.userData.speed * 0.6;
            });
            lines.rotation.y = Math.sin(elapsed * 0.15) * 0.05;
        }
        renderer.render(scene, camera);
        if (motionEnabled) frameId = requestAnimationFrame(renderFrame);
    }

    function refreshToggleLabel() {
        document.querySelectorAll('[data-motion-toggle]').forEach((btn) => {
            btn.setAttribute('aria-pressed', String(motionEnabled));
            btn.innerHTML = motionEnabled
                ? '<i class="bi bi-pause-circle me-1"></i> Pause background'
                : '<i class="bi bi-play-circle me-1"></i> Resume background';
        });
    }

    window.toggleBackgroundMotion = function () {
        motionEnabled = !motionEnabled;
        localStorage.setItem('ho-motion', motionEnabled ? 'on' : 'off');
        refreshToggleLabel();
        if (motionEnabled) {
            if (frameId === null) renderFrame();
        } else if (frameId !== null) {
            cancelAnimationFrame(frameId);
            frameId = null;
            renderFrame();
        }
    };

    canvas.setAttribute('aria-hidden', 'true');
    resize();
    window.addEventListener('resize', resize);
    refreshToggleLabel();
    renderFrame();
})();
