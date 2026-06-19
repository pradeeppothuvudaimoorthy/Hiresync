/**
 * HireSync – main.js
 * Global utility scripts for the HireSync platform.
 */

document.addEventListener('DOMContentLoaded', function () {

    // ── Auto-dismiss Bootstrap alerts after 5 seconds ──
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) {
                bsAlert.close();
            }
        }, 5000);
    });

    // ── File input custom label updater ──
    const fileInputs = document.querySelectorAll('input[type="file"]');
    fileInputs.forEach(function (input) {
        input.addEventListener('change', function () {
            const fileName = this.files.length > 0 ? this.files[0].name : 'Choose file...';
            const label = this.closest('.input-group')?.querySelector('.input-group-text');
            if (label) {
                label.textContent = fileName;
            }
            // Also update any sibling label element
            const siblingLabel = this.nextElementSibling;
            if (siblingLabel && siblingLabel.classList.contains('custom-file-label')) {
                siblingLabel.textContent = fileName;
            }
        });
    });

    // ── Confirm dialogs for dangerous actions ──
    const confirmButtons = document.querySelectorAll('[data-confirm]');
    confirmButtons.forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            const message = this.getAttribute('data-confirm') || 'Are you sure?';
            if (!confirm(message)) {
                e.preventDefault();
                e.stopPropagation();
            }
        });
    });

    // ── Form validation visual feedback (Bootstrap) ──
    const forms = document.querySelectorAll('.needs-validation');
    forms.forEach(function (form) {
        form.addEventListener('submit', function (event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    });

    // ── Tooltip initialization ──
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    tooltipTriggerList.forEach(function (tooltipTriggerEl) {
        new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // ── Smooth scroll for anchor links ──
    document.querySelectorAll('a[href^="#"]').forEach(function (anchor) {
        anchor.addEventListener('click', function (e) {
            const targetId = this.getAttribute('href');
            if (targetId && targetId !== '#') {
                const target = document.querySelector(targetId);
                if (target) {
                    e.preventDefault();
                    target.scrollIntoView({ behavior: 'smooth', block: 'start' });
                }
            }
        });
    });

});
