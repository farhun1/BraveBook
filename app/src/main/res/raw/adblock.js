(function () {
  // Robust sponsored-content hider.
  //
  // Strategy: find the human-visible "Sponsored"/"Ad" label (Facebook appends a
  // private-use glyph after it), then walk UP to the enclosing story/article
  // unit and hide the whole thing. This survives Facebook's frequent internal
  // class/structure changes much better than brittle selectors, and is
  // conservative: a unit is only hidden when the label is actually present.
  //
  // Injected only when the Remove-Ads setting is enabled (Kotlin gates this).
  // Runs locally in the page — no network calls, no data leaves the device.

  const specialChar = '󰞋';

  const sponsoredTexts = [
    "Sponsored", "Ad", "Gesponsert", "Sponsorlu", "Sponsorowane",
    "Ispoonsara godhameera", "Geborg", "Bersponsor", "Ditaja",
    "Disponsori", "Giisponsoran", "Sponzorováno", "Sponsoreret",
    "Publicidad", "May Sponsor", "Sponsorisée", "Sponsorisé", "Oipytyvôva",
    "Ɗaukar Nayin", "Sponzorirano", "Uterwa inkunga", "Sponsorizzato",
    "Imedhaminiwa", "Hirdetés", "Misy Mpiantoka", "Gesponsord",
    "Sponset", "Patrocinado", "Sponsorizat", "Sponzorované",
    "Sponsoroitu", "Sponsrat", "Được tài trợ", "Χορηγούμενη",
    "Спонсорирано", "Спонзорирано", "Ивээн тэтгэсэн", "Реклама",
    "Спонзорисано", "במימון", "سپانسرڈ", "دارای پشتیبانی مالی",
    "ስፖንሰር የተደረገ", "प्रायोजित", "ተደረገ", "प", "স্পনসর্ড",
    "ਪ੍ਰਯੋਜਿਤ", "પ્રાયોજિત", "ପ୍ରାୟୋଜିତ", "செய்யப்பட்ட செய்யப்பட்ட",
    "చేయబడినది చేయబడినది", "ಪ್ರಾಯೋಜಿಸಲಾಗಿದೆ", "ചെയ്‌തത് ചെയ്‌തത്",
    "ලද ලද ලද", "สนับสนุน สนับสนุน รับ สนับสนุน สนับสนุน",
    "ကြော်ငြာ ကြော်ငြာ", "ឧបត្ថម្ភ ឧបត្ថម្ភ ឧបត្ថម្ភ", "광고",
    "贊助", "赞助内容", "広告", "സ്‌പോൺസർ ചെയ്‌തത്",
    "Anzeige", "Peye", "Oglas"
  ];

  const sponsoredRegex = new RegExp(
    `(${sponsoredTexts.join('|')})\\s*${specialChar}`, 'i'
  );

  function isSponsoredLabel(text) {
    return sponsoredRegex.test(text || '');
  }

  // Walk up from a node to the enclosing story/article unit.
  function findStoryContainer(node) {
    let el = node;
    let depth = 0;
    while (el && el !== document.body && el.parentNode && depth < 40) {
      const isArticle =
        el.tagName === 'ARTICLE' || el.getAttribute('role') === 'article';
      const isPagelet = el.hasAttribute('data-pagelet');
      const parent = el.parentElement;
      const isFeedChild =
        !!parent &&
        (parent.getAttribute('data-type') === 'vscroller' ||
          parent.getAttribute('role') === 'feed');
      if (isArticle || isPagelet || isFeedChild) return el;
      el = parent;
      depth++;
    }
    return null;
  }

  function hideSponsored(container) {
    if (!container) return;
    container.style.display = 'none';
    container.setAttribute('data-bravebook-sponsored', 'true');
  }

  function scan(root) {
    if (!root) return;
    const walker = document.createTreeWalker(
      root, NodeFilter.SHOW_TEXT, null
    );
    let node;
    while ((node = walker.nextNode())) {
      if (!isSponsoredLabel(node.nodeValue)) continue;
      const container = findStoryContainer(node.parentElement || node);
      hideSponsored(container);
    }
  }

  function run() {
    scan(document.body);
  }

  run();

  const observer = new MutationObserver(mutations => {
    for (const m of mutations) {
      for (const node of m.addedNodes) {
        if (node.nodeType === 1) scan(node);
      }
    }
  });
  observer.observe(document.body, { childList: true, subtree: true });

  // Reels: sponsored reels use the same label inside vertically-snappable units.
  function containsSponsoredText(text) {
    const lower = (text || '').toLowerCase();
    return sponsoredTexts.some(word => {
      const wb = new RegExp(
        `\\b${word.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}\\b`, 'i'
      );
      return wb.test(lower);
    });
  }

  function removeReelAds(root) {
    root = root || document;
    root.querySelectorAll('div.vertically-snappable').forEach(container => {
      if (container.getAttribute('data-bravebook-sponsored') === 'true') return;
      const found = Array.from(container.querySelectorAll('span'))
        .some(s => containsSponsoredText(s.textContent || ''));
      if (found) hideSponsored(container);
    });
  }

  removeReelAds();

  const reelObserver = new MutationObserver(mutations => {
    for (const m of mutations) {
      for (const node of m.addedNodes) {
        if (node.nodeType !== 1) continue;
        if (node.matches && node.matches('div.vertically-snappable')) {
          removeReelAds(node.parentElement || document);
        } else if (node.querySelector && node.querySelector('div.vertically-snappable')) {
          removeReelAds(node);
        }
      }
    }
  });
  reelObserver.observe(document.body, { childList: true, subtree: true });
})();
