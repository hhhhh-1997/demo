// ========== 主题管理 ==========
const THEME_KEY = 'app-theme';

function getTheme() {
    return localStorage.getItem(THEME_KEY) || 'dark';
}

function applyTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem(THEME_KEY, theme);
    var icon = document.querySelector('.theme-toggle i');
    if (icon) {
        icon.className = theme === 'light' ? 'fas fa-sun' : 'fas fa-moon';
    }
    // 同步主题到 iframe 子页面
    var iframe = document.getElementById('page-iframe');
    if (iframe && iframe.contentWindow) {
        iframe.contentWindow.postMessage({ type: 'theme-change', theme: theme }, '*');
    }
}

function toggleTheme() {
    var current = getTheme();
    var next = current === 'dark' ? 'light' : 'dark';
    applyTheme(next);
}

// ========== 菜单折叠 ==========
function toggleMenu(element) {
    const groupTitle = element;
    const submenu = element.nextElementSibling;

    groupTitle.classList.toggle('expanded');
    submenu.classList.toggle('open');
}

document.addEventListener('DOMContentLoaded', function() {
    // 初始化主题
    applyTheme(getTheme());

    const menuItems = document.querySelectorAll('.menu-item, .submenu-item');
    const pageIframe = document.getElementById('page-iframe');

    // iframe 加载完成后同步主题
    pageIframe.addEventListener('load', function() {
        var theme = getTheme();
        try {
            pageIframe.contentWindow.postMessage({ type: 'theme-change', theme: theme }, '*');
        } catch(e) {}
    });

    menuItems.forEach(item => {
        item.addEventListener('click', function() {
            const page = this.getAttribute('data-page');
            if (page) {
                pageIframe.src = page;

                menuItems.forEach(m => m.classList.remove('active'));
                this.classList.add('active');

                const menuGroup = this.closest('.menu-group');
                if (menuGroup) {
                    const submenu = menuGroup.querySelector('.submenu');
                    const title = menuGroup.querySelector('.menu-group-title');
                    if (submenu && !submenu.classList.contains('open')) {
                        submenu.classList.add('open');
                        title.classList.add('expanded');
                    }
                }
            }
        });
    });
});
