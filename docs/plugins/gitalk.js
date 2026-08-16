/*!
 * Docsify Plugin: gitalk v5.0.0
 * https://docsify.js.org
 * (c) 2017-2026
 * MIT license
 */
(function() {
    "use strict";
    const window = globalThis;
    function install(hook) {
        const dom = Docsify.dom;
        hook.mounted((_ => {
            const div = dom.create("div");
            div.id = "gitalk-container";
            const main = dom.getNode("#main");
            div.style = `width: ${main.clientWidth}px; margin: 0 auto 20px;`;
            dom.appendTo(dom.find(".content"), div);
        }));
        hook.doneEach((_ => {
            const el = document.getElementById("gitalk-container");
            while (el.hasChildNodes()) {
                el.removeChild(el.firstChild);
            }
            window.gitalk.render("gitalk-container");
        }));
    }
    window.$docsify = window.$docsify || {};
    window.$docsify.plugins = [ install, ...window.$docsify.plugins || [] ];
})();
