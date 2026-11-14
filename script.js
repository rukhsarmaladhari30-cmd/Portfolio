/**
 * RUK PORTFOLIO - HIGH-PERFORMANCE FRAME SEQUENCE & INTERACTIVE ENGINE
 */

document.addEventListener('DOMContentLoaded', () => {
  // --- CONFIGURATION ---
  const TOTAL_FRAMES = 299;
  const FRAME_PATH = (index) => `frames/ezgif-frame-${String(index).padStart(3, '0')}.jpg`;

  // Elements
  const canvas = document.getElementById('sequenceCanvas');
  const ctx = canvas.getContext('2d', { alpha: false });
  const preloader = document.getElementById('preloader');
  const preloadProgressBar = document.getElementById('preloadProgressBar');
  const preloadStatus = document.getElementById('preloadStatus');
  const storySection = document.getElementById('story');

  // Story Cards
  const cards = [
    document.getElementById('card-1'),
    document.getElementById('card-2'),
    document.getElementById('card-3'),
    document.getElementById('card-4')
  ];

  // HUD Elements
  const hudPlayBtn = document.getElementById('hudPlayBtn');
  const playIcon = document.getElementById('playIcon');
  const pauseIcon = document.getElementById('pauseIcon');
  const frameCounter = document.getElementById('frameCounter');
  const frameScrubber = document.getElementById('frameScrubber');
  const playbackSpeedBtn = document.getElementById('playbackSpeedBtn');
  const fullscreenBtn = document.getElementById('fullscreenBtn');

  // Sound Elements
  const soundToggle = document.getElementById('soundToggle');
  const triggerAudioBtn = document.getElementById('triggerAudioBtn');

  // Image cache
  const images = new Array(TOTAL_FRAMES + 1);
  let loadedCount = 0;
  let isFullyLoaded = false;

  // Animation & Rendering State
  let currentFrame = 1;
  let targetFrame = 1;
  let isPlaying = false;
  let playbackSpeed = 1; // 1x or 2x
  let playAnimId = null;

  // --- 1. CANVAS SIZING & RENDERING ---
  function resizeCanvas() {
    const dpr = Math.min(window.devicePixelRatio || 1, 2);
    canvas.width = window.innerWidth * dpr;
    canvas.height = window.innerHeight * dpr;
    canvas.style.width = `${window.innerWidth}px`;
    canvas.style.height = `${window.innerHeight}px`;
    ctx.scale(dpr, dpr);
    renderFrame(Math.round(currentFrame));
  }

  function renderFrame(index) {
    const clampedIndex = Math.max(1, Math.min(TOTAL_FRAMES, Math.round(index)));
    const img = images[clampedIndex];

    if (!img || !img.complete || img.naturalWidth === 0) return;

    const canvasWidth = window.innerWidth;
    const canvasHeight = window.innerHeight;

    // Draw using "cover" aspect ratio calculation
    const imgRatio = img.naturalWidth / img.naturalHeight;
    const canvasRatio = canvasWidth / canvasHeight;

    let renderW, renderH, offsetX, offsetY;

    if (canvasRatio > imgRatio) {
      renderW = canvasWidth;
      renderH = canvasWidth / imgRatio;
      offsetX = 0;
      offsetY = (canvasHeight - renderH) / 2;
    } else {
      renderH = canvasHeight;
      renderW = canvasHeight * imgRatio;
      offsetX = (canvasWidth - renderW) / 2;
      offsetY = 0;
    }

    ctx.drawImage(img, offsetX, offsetY, renderW, renderH);

    // Update HUD display
    frameCounter.textContent = `Frame: ${String(clampedIndex).padStart(3, '0')} / ${TOTAL_FRAMES}`;
    frameScrubber.value = clampedIndex;

    // Update synchronized story cards
    updateStoryCards(clampedIndex);
  }

  // --- 2. CARD TIMELINES (Synced with visual sequence) ---
  function updateStoryCards(frame) {
    const progress = (frame - 1) / (TOTAL_FRAMES - 1);

    // Card 1: 0% - 22%
    cards[0].classList.toggle('active', progress >= 0.0 && progress <= 0.22);
    // Card 2: 25% - 48%
    cards[1].classList.toggle('active', progress >= 0.25 && progress <= 0.48);
    // Card 3: 52% - 75%
    cards[2].classList.toggle('active', progress >= 0.52 && progress <= 0.75);
    // Card 4: 78% - 100%
    cards[3].classList.toggle('active', progress >= 0.78 && progress <= 1.0);
  }

  // --- 3. PROGRESSIVE FRAME PRELOADING ---
  function loadFrame(i) {
    return new Promise((resolve) => {
      const img = new Image();
      img.src = FRAME_PATH(i);
      img.onload = () => {
        images[i] = img;
        loadedCount++;
        const pct = Math.floor((loadedCount / TOTAL_FRAMES) * 100);
        preloadProgressBar.style.width = `${pct}%`;
        preloadStatus.textContent = `Loading visual experience... ${pct}%`;

        // Once initial 15 frames are ready, render frame 1 and dismiss preloader
        if (loadedCount === 15) {
          renderFrame(1);
          setTimeout(() => {
            preloader.classList.add('fade-out');
          }, 350);
        }

        if (loadedCount >= TOTAL_FRAMES) {
          isFullyLoaded = true;
          preloader.classList.add('fade-out');
        }
        resolve();
      };
      img.onerror = () => {
        loadedCount++;
        resolve();
      };
    });
  }

  async function preloadAllFrames() {
    // 1. First priority: load frames 1 to 20 immediately
    const priorityBatch = [];
    for (let i = 1; i <= 20; i++) {
      priorityBatch.push(loadFrame(i));
    }
    await Promise.all(priorityBatch);

    // 2. Load the rest progressively in parallel chunks
    const CHUNK_SIZE = 25;
    for (let i = 21; i <= TOTAL_FRAMES; i += CHUNK_SIZE) {
      const chunk = [];
      for (let j = i; j < i + CHUNK_SIZE && j <= TOTAL_FRAMES; j++) {
        chunk.push(loadFrame(j));
      }
      await Promise.all(chunk);
    }
  }

  // --- 4. SCROLL SYNCHRONIZATION WITH LERP ---
  let isScrollingOrDragging = false;

  function onScroll() {
    if (isPlaying) return; // Don't override while user is auto-playing

    const rect = storySection.getBoundingClientRect();
    const sectionHeight = storySection.offsetHeight - window.innerHeight;
    const scrolled = -rect.top;

    if (scrolled >= 0 && scrolled <= sectionHeight) {
      const progress = Math.max(0, Math.min(1, scrolled / sectionHeight));
      targetFrame = 1 + progress * (TOTAL_FRAMES - 1);
    }
  }

  // Smooth frame loop using linear interpolation
  function smoothLoop() {
    if (!isPlaying) {
      const diff = targetFrame - currentFrame;
      if (Math.abs(diff) > 0.04) {
        currentFrame += diff * 0.15; // Smooth spring lerp
        renderFrame(currentFrame);
      }
    }
    requestAnimationFrame(smoothLoop);
  }

  // --- 5. CINEMATIC AUTO-PLAY HUD CONTROLS ---
  function startAutoPlay() {
    isPlaying = true;
    playIcon.classList.add('hidden');
    pauseIcon.classList.remove('hidden');

    let lastTime = performance.now();
    const fps = 30 * playbackSpeed;
    const interval = 1000 / fps;

    function playStep(time) {
      if (!isPlaying) return;
      const elapsed = time - lastTime;

      if (elapsed >= interval) {
        lastTime = time - (elapsed % interval);
        currentFrame += 1;
        if (currentFrame > TOTAL_FRAMES) {
          currentFrame = 1; // loop
        }
        targetFrame = currentFrame;
        renderFrame(currentFrame);
      }
      playAnimId = requestAnimationFrame(playStep);
    }

    playAnimId = requestAnimationFrame(playStep);
  }

  function stopAutoPlay() {
    isPlaying = false;
    if (playAnimId) cancelAnimationFrame(playAnimId);
    playIcon.classList.remove('hidden');
    pauseIcon.classList.add('hidden');
  }

  hudPlayBtn.addEventListener('click', () => {
    if (isPlaying) {
      stopAutoPlay();
    } else {
      startAutoPlay();
    }
  });

  frameScrubber.addEventListener('input', (e) => {
    stopAutoPlay();
    const val = parseInt(e.target.value, 10);
    targetFrame = val;
    currentFrame = val;
    renderFrame(val);
  });

  playbackSpeedBtn.addEventListener('click', () => {
    if (playbackSpeed === 1) {
      playbackSpeed = 1.5;
      playbackSpeedBtn.textContent = '1.5x';
    } else if (playbackSpeed === 1.5) {
      playbackSpeed = 2;
      playbackSpeedBtn.textContent = '2x';
    } else {
      playbackSpeed = 1;
      playbackSpeedBtn.textContent = '1x';
    }
  });

  fullscreenBtn.addEventListener('click', () => {
    if (!document.fullscreenElement) {
      storySection.requestFullscreen?.() || document.documentElement.requestFullscreen();
    } else {
      document.exitFullscreen?.();
    }
  });

  // --- 6. GENERATIVE OCEAN WAVES & SUNSET SOUND SYNTHESIZER ---
  let audioCtx = null;
  let waveGain = null;
  let isSoundActive = false;

  function initOceanSound() {
    if (audioCtx) return;
    try {
      const AudioContext = window.AudioContext || window.webkitAudioContext;
      audioCtx = new AudioContext();

      // Pink/Brown noise generator for realistic ocean waves
      const bufferSize = audioCtx.sampleRate * 2;
      const noiseBuffer = audioCtx.createBuffer(1, bufferSize, audioCtx.sampleRate);
      const output = noiseBuffer.getChannelData(0);

      let b0 = 0, b1 = 0, b2 = 0, b3 = 0, b4 = 0, b5 = 0, b6 = 0;
      for (let i = 0; i < bufferSize; i++) {
        const white = Math.random() * 2 - 1;
        b0 = 0.99886 * b0 + white * 0.0555179;
        b1 = 0.99332 * b1 + white * 0.0750759;
        b2 = 0.96900 * b2 + white * 0.1538520;
        b3 = 0.86650 * b3 + white * 0.3104856;
        b4 = 0.55000 * b4 + white * 0.5329522;
        b5 = -0.7616 * b5 - white * 0.0168980;
        output[i] = (b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362) * 0.11;
        b6 = white * 0.115926;
      }

      const whiteNoise = audioCtx.createBufferSource();
      whiteNoise.buffer = noiseBuffer;
      whiteNoise.loop = true;

      // Low-pass filter for soothing muffled surf sound
      const filter = audioCtx.createBiquadFilter();
      filter.type = 'lowpass';
      filter.frequency.setValueAtTime(450, audioCtx.currentTime);

      // LFO to simulate rolling ocean swell
      const lfo = audioCtx.createOscillator();
      lfo.frequency.setValueAtTime(0.12, audioCtx.currentTime); // 1 swell every ~8 seconds

      const lfoGain = audioCtx.createGain();
      lfoGain.gain.setValueAtTime(250, audioCtx.currentTime);

      lfo.connect(filter.frequency);
      lfo.start();

      waveGain = audioCtx.createGain();
      waveGain.gain.setValueAtTime(0.001, audioCtx.currentTime);

      whiteNoise.connect(filter);
      filter.connect(waveGain);
      waveGain.connect(audioCtx.destination);
      whiteNoise.start();
    } catch (err) {
      console.warn('Web Audio not allowed or unsupported:', err);
    }
  }

  function toggleSound() {
    if (!audioCtx) initOceanSound();
    if (!audioCtx) return;

    if (audioCtx.state === 'suspended') {
      audioCtx.resume();
    }

    if (isSoundActive) {
      // Fade out
      waveGain.gain.exponentialRampToValueAtTime(0.0001, audioCtx.currentTime + 1);
      soundToggle.classList.add('muted');
      isSoundActive = false;
      if (triggerAudioBtn) triggerAudioBtn.textContent = 'Play Ocean Audio';
    } else {
      // Fade in
      waveGain.gain.exponentialRampToValueAtTime(0.25, audioCtx.currentTime + 1.2);
      soundToggle.classList.remove('muted');
      isSoundActive = true;
      if (triggerAudioBtn) triggerAudioBtn.textContent = 'Pause Ocean Audio';
    }
  }

  soundToggle.classList.add('muted');
  soundToggle.addEventListener('click', toggleSound);
  if (triggerAudioBtn) triggerAudioBtn.addEventListener('click', toggleSound);

  // --- 7. PROJECT FILTER TABS ---
  const filterTabs = document.querySelectorAll('.filter-tab');
  const projectCards = document.querySelectorAll('.project-card');

  filterTabs.forEach((tab) => {
    tab.addEventListener('click', () => {
      filterTabs.forEach((t) => t.classList.remove('active'));
      tab.classList.add('active');

      const filter = tab.getAttribute('data-filter');
      projectCards.forEach((card) => {
        if (filter === 'all' || card.getAttribute('data-category') === filter) {
          card.style.display = 'flex';
        } else {
          card.style.display = 'none';
        }
      });
    });
  });

  // --- 8. COPY EMAIL PILL ---
  const emailPill = document.getElementById('emailCopyPill');
  if (emailPill) {
    emailPill.addEventListener('click', () => {
      navigator.clipboard.writeText('ruk.portfolio@example.com').then(() => {
        const orig = emailPill.innerHTML;
        emailPill.innerHTML = `Copied to clipboard! ✓`;
        setTimeout(() => {
          emailPill.innerHTML = orig;
        }, 2200);
      });
    });
  }

  // --- 9. CONTACT FORM INTERACTION ---
  const contactForm = document.getElementById('contactForm');
  const formFeedback = document.getElementById('formFeedback');

  if (contactForm) {
    contactForm.addEventListener('submit', (e) => {
      e.preventDefault();
      const submitBtn = contactForm.querySelector('button[type="submit"]');
      submitBtn.disabled = true;
      submitBtn.innerHTML = `<span>Sending...</span>`;

      setTimeout(() => {
        submitBtn.disabled = false;
        submitBtn.innerHTML = `<span>Send Message</span>`;
        formFeedback.classList.remove('hidden');
        formFeedback.textContent = `Thank you! Your message has been received. Ruk will get back to you shortly.`;
        contactForm.reset();

        setTimeout(() => {
          formFeedback.classList.add('hidden');
        }, 5000);
      }, 900);
    });
  }

  // --- 10. ACTIVE NAVBAR SCROLL SPY ---
  const navLinks = document.querySelectorAll('.nav-link');
  const sections = document.querySelectorAll('section[id]');

  window.addEventListener('scroll', () => {
    let current = '';
    sections.forEach((section) => {
      const sectionTop = section.offsetTop - 120;
      if (window.scrollY >= sectionTop) {
        current = section.getAttribute('id');
      }
    });

    navLinks.forEach((link) => {
      link.classList.remove('active');
      if (link.getAttribute('href') === `#${current}`) {
        link.classList.add('active');
      }
    });
  }, { passive: true });

  // --- 11. INITIALIZATION ---
  window.addEventListener('resize', resizeCanvas);
  window.addEventListener('scroll', onScroll, { passive: true });

  resizeCanvas();
  smoothLoop();
  preloadAllFrames();
});

