// ========== 主题初始化（子页面） ==========
(function() {
    var theme = localStorage.getItem('app-theme') || 'dark';
    document.documentElement.setAttribute('data-theme', theme);
})();

// 监听主页面换肤消息，实时切换主题
window.addEventListener('message', function(e) {
    if (e.data && e.data.type === 'theme-change') {
        document.documentElement.setAttribute('data-theme', e.data.theme);
    }
});

function showModal(title, content, readonly) {
    let modalContainer = document.getElementById('modal-container');
    var footerHtml = readonly
        ? '<button class="btn btn-primary" onclick="closeModal()">关闭</button>'
        : '<button class="btn btn-outline" onclick="closeModal()">取消</button><button class="btn btn-primary" onclick="saveModal()">保存</button>';
    
    modalContainer.innerHTML = `
        <div class="modal-overlay active" onclick="closeModal(event)">
            <div class="modal" onclick="event.stopPropagation()">
                <div class="modal-header">
                    <h3 class="modal-title">${title}</h3>
                    <button class="modal-close" onclick="closeModal()">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
                <div class="modal-body">
                    ${content}
                </div>
                <div class="modal-footer">
                    ${footerHtml}
                </div>
            </div>
        </div>
    `;
}

function closeModal(event) {
    if (event && event.target !== event.currentTarget) return;
    const overlay = document.querySelector('.modal-overlay');
    if (overlay) {
        overlay.classList.remove('active');
        setTimeout(() => {
            const container = document.getElementById('modal-container');
            if (container) {
                container.innerHTML = '';
            }
        }, 300);
    }
}

function saveModal() {
    alert('保存成功！');
    closeModal();
}

// ========== 通用分页 ==========
function renderPagination(totalItems, pageSize, currentPage, onPageChange) {
    const totalPages = Math.ceil(totalItems / pageSize) || 1;
    if (currentPage > totalPages) currentPage = totalPages;
    if (currentPage < 1) currentPage = 1;

    var startItem = (currentPage - 1) * pageSize + 1;
    var endItem = Math.min(currentPage * pageSize, totalItems);

    var html = '<div class="pagination-bar">';
    html += '<div class="pagination-info">共 <strong>' + totalItems + '</strong> 条，显示第 <strong>' + startItem + '</strong>-' + endItem + '</strong> 条</div>';
    html += '<div class="pagination-controls">';

    // 上一页
    html += '<button class="pagination-btn" ' + (currentPage <= 1 ? 'disabled' : '') + ' onclick="' + onPageChange + '(' + (currentPage - 1) + ')"><i class="fas fa-chevron-left"></i></button>';

    // 页码
    var pages = [];
    if (totalPages <= 7) {
        for (var i = 1; i <= totalPages; i++) pages.push(i);
    } else {
        pages.push(1);
        if (currentPage > 3) pages.push('...');
        var start = Math.max(2, currentPage - 1);
        var end = Math.min(totalPages - 1, currentPage + 1);
        for (var i = start; i <= end; i++) pages.push(i);
        if (currentPage < totalPages - 2) pages.push('...');
        pages.push(totalPages);
    }

    pages.forEach(function(p) {
        if (p === '...') {
            html += '<span class="pagination-ellipsis">...</span>';
        } else {
            html += '<button class="pagination-btn' + (p === currentPage ? ' active' : '') + '" onclick="' + onPageChange + '(' + p + ')">' + p + '</button>';
        }
    });

    // 下一页
    html += '<button class="pagination-btn" ' + (currentPage >= totalPages ? 'disabled' : '') + ' onclick="' + onPageChange + '(' + (currentPage + 1) + ')"><i class="fas fa-chevron-right"></i></button>';
    html += '</div>';
    html += '</div>';

    return html;
}
