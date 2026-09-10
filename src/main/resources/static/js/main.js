// Main Interactive UI JavaScript

document.addEventListener("DOMContentLoaded", function () {
    // Sidebar toggle for mobile/desktop
    const sidebarCollapse = document.getElementById("sidebarCollapse");
    if (sidebarCollapse) {
        sidebarCollapse.addEventListener("click", function () {
            document.getElementById("sidebar").classList.toggle("active");
        });
    }

    // Auto-dismiss alert toasts after 5s
    const alerts = document.querySelectorAll(".alert-dismissible");
    alerts.forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });

    // Image Upload Preview
    const imageInputs = document.querySelectorAll(".image-preview-input");
    imageInputs.forEach(function (input) {
        input.addEventListener("change", function (event) {
            const targetPreviewId = input.getAttribute("data-preview-target");
            const previewImage = document.getElementById(targetPreviewId);
            if (previewImage && event.target.files && event.target.files[0]) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    previewImage.src = e.target.result;
                    previewImage.classList.remove("d-none");
                };
                reader.readAsDataURL(event.target.files[0]);
            }
        });
    });
});
