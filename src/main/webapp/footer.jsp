<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<hr/>
<footer class="main-footer">
<button id="btnToggleTheme" class="btn btn-primary rounded-circle shadow-lg position-fixed bottom-0 end-0 m-4 d-flex align-items-center justify-content-center" style="width: 50px; height: 50px; z-index: 1050;" title="Cambiar modo claro/oscuro">
    <i id="themeIcon" class="bi bi-moon-fill fs-5"></i>
</button>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        const btnToggle = document.getElementById('btnToggleTheme');
        const themeIcon = document.getElementById('themeIcon');

        const currentTheme = document.documentElement.getAttribute('data-bs-theme');
        if (currentTheme === 'dark') {
            themeIcon.classList.replace('bi-moon-fill', 'bi-sun-fill');
        }

        btnToggle.addEventListener('click', function () {
            let theme = document.documentElement.getAttribute('data-bs-theme');
            let newTheme = (theme === 'dark') ? 'light' : 'dark';

            document.documentElement.setAttribute('data-theme', newTheme);
            document.documentElement.setAttribute('data-bs-theme', newTheme);
            localStorage.setItem('theme', newTheme);

            if (newTheme === 'dark') {
                themeIcon.classList.replace('bi-moon-fill', 'bi-sun-fill');
            } else {
                themeIcon.classList.replace('bi-sun-fill', 'bi-moon-fill');
            }
        });
    });
</script>
    <div class="footer-content">
        <p>&copy; 2026 Sistema de Gestión de Biblioteca WillBook. Todos los derechos reservados.</p>
    </div>
</footer>