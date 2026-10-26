// Hide suggested posts from feed
(function() {

    // Hide suggested posts from feed
    if (window.isDesktopMode()) {
      // Suggested posts carry a "· Follow"/"· Join" button in the profile-name header.
      (() => {
        const hideSuggestedPosts = () => {
          if (window.location.pathname !== '/') return;
          document.querySelectorAll('[aria-posinset] [data-ad-rendering-role="profile_name"] div[role="button"]')
            .forEach(button => {
              const post = button.closest('[aria-posinset]');
              if (post) post.style.display = 'none';
            });
        };

        hideSuggestedPosts();
        new MutationObserver(hideSuggestedPosts).observe(document.body, { childList: true, subtree: true });
      })();
    } else {
      (function() {
        function processNode(node) {
          if (!(node instanceof HTMLElement)) return;
          if (window.location.pathname !== '/') return;

          const textSpans = node.querySelectorAll(
            'div[data-mcomponent="TextArea"][data-type="text"][data-tti-phase="-1"] .native-text > span'
          );

          textSpans.forEach(textSpan => {
            // The "· Follow"/"• Join" separator is either a bullet (U+2022) or a middle dot (U+00B7).
            if (['•', '·'].includes(textSpan.textContent.trim())) {
              const suggestedPostContainer = textSpan.closest('div[data-tracking-duration-id]');
              if (suggestedPostContainer) {
                suggestedPostContainer.style.display = 'none';
                const postGap = suggestedPostContainer.previousElementSibling;
                if (postGap) {
                  postGap.style.display = 'none';
                }
              }
            }
          });
        }

        const observer = new MutationObserver(mutations => {
          for (const mutation of mutations) {
            for (const node of mutation.addedNodes) {
              processNode(node);
            }
          }
        });

        observer.observe(document.body, {
          childList: true,
          subtree: true
        });

        processNode(document.body);
      })();
    }
})();

