window.addEventListener("load", function () {
  // If bootstrap is missing, tabs cannot work
  if (!window.bootstrap) {
    console.error("Bootstrap JS not loaded -> tabs will not work.");
    return;
  }

  // Force-enable all Bootstrap tabs
  document.querySelectorAll('[data-bs-toggle="tab"]').forEach(function (triggerEl) {
    triggerEl.addEventListener("click", function (e) {
      e.preventDefault();
      bootstrap.Tab.getOrCreateInstance(triggerEl).show();
    });
  });
});