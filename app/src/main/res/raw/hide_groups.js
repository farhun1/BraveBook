// Hide group suggestions on search page
(() => {
  const hideLastGroupSuggestion = () => {
    if (window.location.href.includes('facebook.com/search')) {
      const parents = document.querySelectorAll('[data-is-h-scrollable]');
      const lastParent = parents[parents.length - 1]?.closest('.m.bg-s1');
      if (lastParent) lastParent.style.display = 'none';
    }
  };

  hideLastGroupSuggestion();
  new MutationObserver(hideLastGroupSuggestion).observe(document.body, { childList: true, subtree: true });
})();

// Hide on comments
(() => {
  const hideGroupContainer = () => {
    if (!window.location.href.includes('facebook.com/story.php?') && !window.location.href.includes('facebook.com/groups')) return;

    const groupContainer = document.querySelector('h3[data-tti-phase="-1"].m');
    if (groupContainer) {
      const parent = groupContainer.closest('.m.bg-s2');
      if (parent) parent.style.display = 'none';
    }
  };

  hideGroupContainer();

  new MutationObserver(hideGroupContainer).observe(document.body, {
    childList: true,
    subtree: true,
  });
})();

// Hide group posts from the main feed. Rescans the whole feed on every
// mutation because post headers render after their container.
(() => {
  // Desktop: the post header's profile-name link points to /groups/.
  const desktopGroupPosts = () =>
    [...document.querySelectorAll('[aria-posinset] [data-ad-rendering-role="profile_name"] a[href*="/groups/"]')]
      .map(a => a.closest('[aria-posinset]'));

  // Mobile renders no links. A group post's avatar is the square group icon
  // with the author's round avatar overlaid; other posts start with a round one.
  const mobileGroupPosts = () =>
    [...document.querySelectorAll('[data-tracking-duration-id]')].filter(p => {
      const [icon, overlay] = p.querySelectorAll('img');
      return icon && overlay && !icon.classList.contains('rounded') && overlay.classList.contains('rounded');
    });

  const hideGroupPosts = () => {
    if (window.location.pathname !== '/') return;
    (window.isDesktopMode() ? desktopGroupPosts() : mobileGroupPosts())
      .forEach(post => { if (post) post.style.display = 'none'; });
  };

  hideGroupPosts();
  new MutationObserver(hideGroupPosts).observe(document.body, { childList: true, subtree: true });
})();

