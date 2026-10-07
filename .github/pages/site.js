(() => {
    "use strict";

    const root = document.documentElement;
    const versionBase = window.VERSION_BASE || "";

    function storageGet(key) {
        try { return localStorage.getItem(key); } catch (_) { return null; }
    }

    function storageSet(key, value) {
        try { localStorage.setItem(key, value); } catch (_) {}
    }

    // Das Theme selbst setzt schon ein Skript im <head> (ohne Aufblitzen).
    // Hier geht es nur um den Umschalter und um Wechsel der Systemeinstellung.
    function initTheme() {
        const button = document.getElementById("themeToggle");

        const update = () => {
            if (!button) return;
            const dark = root.getAttribute("data-bs-theme") === "dark";
            button.textContent = dark ? "☀ Hell" : "☾ Dunkel";
            button.setAttribute("aria-label", dark ? "Helles Design" : "Dunkles Design");
            button.setAttribute("title", dark ? "Helles Design" : "Dunkles Design");
        };

        // Solange nichts gewählt wurde, folgt die Seite dem System.
        window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", event => {
            if (storageGet("project-theme")) return;
            root.setAttribute("data-bs-theme", event.matches ? "dark" : "light");
            update();
        });

        button?.addEventListener("click", () => {
            const next = root.getAttribute("data-bs-theme") === "dark" ? "light" : "dark";
            root.setAttribute("data-bs-theme", next);
            storageSet("project-theme", next);
            update();
        });

        update();
    }

    function initVersionSelect() {
        const select = document.getElementById("versionSelect");
        select?.addEventListener("change", () => {
            if (select.value) window.location.href = select.value;
        });
    }

    function initSplitter() {
        const splitter = document.getElementById("sidebarSplitter");
        const sidebar = document.getElementById("appSidebar");
        if (!splitter || !sidebar) return;

        const saved = Number.parseInt(storageGet("sidebar-width") || "", 10);
        if (Number.isFinite(saved)) {
            document.documentElement.style.setProperty("--project-sidebar-width", `${saved}px`);
        }

        let dragging = false;

        splitter.addEventListener("pointerdown", event => {
            if (window.innerWidth < 992) return;
            dragging = true;
            splitter.classList.add("dragging");
            document.body.classList.add("sidebar-resizing");
            splitter.setPointerCapture?.(event.pointerId);
            event.preventDefault();
        });

        splitter.addEventListener("pointermove", event => {
            if (!dragging) return;
            const width = Math.max(240, Math.min(560, event.clientX));
            document.documentElement.style.setProperty("--project-sidebar-width", `${width}px`);
        });

        const stop = () => {
            if (!dragging) return;
            dragging = false;
            splitter.classList.remove("dragging");
            document.body.classList.remove("sidebar-resizing");
            const width = Number.parseInt(
                getComputedStyle(document.documentElement).getPropertyValue("--project-sidebar-width"),
                10
            );
            if (Number.isFinite(width)) storageSet("sidebar-width", String(width));
        };

        splitter.addEventListener("pointerup", stop);
        splitter.addEventListener("pointercancel", stop);
    }

    function initTree() {
        document.querySelectorAll("[data-tree-toggle]").forEach(button => {
            button.addEventListener("click", event => {
                event.preventDefault();
                event.stopPropagation();

                const targetId = button.getAttribute("data-tree-toggle");
                const target = document.getElementById(targetId);
                if (!target) return;

                const collapsed = target.classList.toggle("d-none");
                button.textContent = collapsed ? "▸" : "▾";
                button.setAttribute("aria-expanded", String(!collapsed));
            });
        });
    }

    // Umschalter zwischen Änderungen und komplettem Stand (nur bei PRs).
    // Alle Elemente mit dem Attribut zeigen entweder "changes" oder "full".
    function initViewToggle(toggleId, attribute, storageKey) {
        const toggle = document.getElementById(toggleId);
        if (!toggle) return;

        const apply = () => {
            document.querySelectorAll(`[${attribute}]`).forEach(element => {
                const full = element.getAttribute(attribute) === "full";
                element.classList.toggle("d-none", full !== toggle.checked);
            });
        };

        toggle.checked = storageGet(storageKey) === "1";
        toggle.addEventListener("change", () => {
            storageSet(storageKey, toggle.checked ? "1" : "0");
            apply();
        });

        apply();
    }

    let searchIndexPromise = null;

    async function loadSearchIndex() {
        if (searchIndexPromise) return searchIndexPromise;

        const base = versionBase.endsWith("/") || versionBase === "" ? versionBase : `${versionBase}/`;
        searchIndexPromise = fetch(`${base}search-index.json`,{ cache: "no-store" })
            .then(response => {
                if (!response.ok) throw new Error(`HTTP ${response.status}`);
                return response.json();
            });

        return searchIndexPromise;
    }

    function escapeHtml(value) {
        return String(value)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function buildContext(content, lineNumber, query) {
        const lines = content.split(/\r?\n/);
        const start = Math.max(0, lineNumber - 3);
        const end = Math.min(lines.length, lineNumber + 2);

        return lines.slice(start, end).map((line, offset) => {
            const number = start + offset + 1;
            const escaped = escapeHtml(line);
            if (number !== lineNumber) {
                return `${String(number).padStart(4, " ")}  ${escaped}`;
            }

            const escapedQuery = escapeHtml(query);
            const marker = new RegExp(
                escapedQuery.replace(/[.*+?^${}()|[\]\\]/g, "\\$&"),
                "ig"
            );
            const highlighted = escaped.replace(marker, match => `<span class="match">${match}</span>`);
            return `<strong>${String(number).padStart(4, " ")}</strong>  ${highlighted}`;
        }).join("\n");
    }

    function initSearch() {
        const input = document.getElementById("searchInput");
        const modalElement = document.getElementById("searchModal");
        const results = document.getElementById("searchResults");
        if (!input || !modalElement || !results) return;

        const modal = bootstrap.Modal.getOrCreateInstance(modalElement);
        let timer = null;

        const render = async () => {
            const query = input.value.trim();
            if (!query) {
                results.innerHTML = '<div class="search-empty">Suchbegriff eingeben.</div>';
                return;
            }

            results.innerHTML = '<div class="search-empty">Suche läuft …</div>';

            try {
                const index = await loadSearchIndex();
                const needle = query.toLowerCase();
                const matches = [];

                for (const file of index) {
                    const lines = file.content.split(/\r?\n/);
                    let countForFile = 0;

                    for (let i = 0; i < lines.length && countForFile < 10; i++) {
                        if (lines[i].toLowerCase().includes(needle)) {
                            matches.push({
                                file,
                                line: i + 1,
                                context: buildContext(file.content, i + 1, query)
                            });
                            countForFile++;
                        }
                    }
                }

                if (!matches.length) {
                    results.innerHTML = '<div class="search-empty">Keine Treffer.</div>';
                    return;
                }

                results.innerHTML = matches.map(item => `
                    <a class="list-group-item list-group-item-action search-result"
                       href="${escapeHtml(item.file.url)}">
                        <div class="fw-semibold">${escapeHtml(item.file.path)}</div>
                        <div class="small text-body-secondary mb-2">Zeile ${item.line}</div>
                        <pre class="search-context">${item.context}</pre>
                    </a>
                `).join("");
            } catch (error) {
                results.innerHTML = `
                    <div class="alert alert-danger mb-0">
                        Suche konnte nicht geladen werden: ${escapeHtml(error.message)}
                    </div>`;
            }
        };

        // Gesucht wird erst ab zwei Zeichen; bis dahin ist der Button gesperrt.
        const buttons = document.querySelectorAll("[data-open-search]");
        const searchable = () => input.value.trim().length >= 2;
        const updateButtons = () => {
            buttons.forEach(button => { button.disabled = !searchable(); });
        };

        const open = () => {
            if (!searchable()) return;
            modal.show();
            setTimeout(() => input.focus(), 150);
            render();
        };

        input.addEventListener("input", () => {
            updateButtons();
            clearTimeout(timer);
            timer = setTimeout(render, 150);
        });

        input.addEventListener("keydown", event => {
            if (event.key === "Escape") {
                modal.hide();
            } else if (event.key === "Enter") {
                event.preventDefault();
                open();
            }
        });

        buttons.forEach(button => {
            button.addEventListener("click", open);
        });

        // Der Browser kann beim Zurückblättern einen Suchbegriff wiederherstellen.
        updateButtons();
    }

    initTheme();
    initVersionSelect();
    initSplitter();
    initTree();
    initViewToggle("treeFullToggle", "data-tree-view", "pr-tree-full");
    initViewToggle("fileFullToggle", "data-diff-view", "pr-file-full");
    initSearch();
})();
