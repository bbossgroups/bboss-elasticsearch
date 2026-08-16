/*!
 * Docsify Plugin: search v5.0.0
 * https://docsify.js.org
 * (c) 2017-2026
 * MIT license
 */
(function() {
    "use strict";
    function getAndRemoveConfig(str = "") {
        const config = {};
        if (str) {
            str = str.replace(/^('|")/, "").replace(/('|")$/, "").replace(/(?:^|\s):([\w-]+:?)=?([\w-%]+)?/g, ((m, key, value) => {
                if (key.indexOf(":") !== -1) {
                    return m;
                }
                value = value && value.replace(/&quot;/g, "") || true;
                if (value !== true && config[key] !== undefined) {
                    if (!Array.isArray(config[key]) && value !== config[key]) {
                        config[key] = [ config[key] ];
                    }
                    config[key].includes(value) || config[key].push(value);
                } else {
                    config[key] = value;
                }
                return "";
            })).trim();
        }
        return {
            str: str,
            config: config
        };
    }
    function removeAtag(str = "") {
        return str.replace(/(<\/?a.*?>)/gi, "");
    }
    function getAndRemoveDocsifyIgnoreConfig(content = "") {
        let ignoreAllSubs, ignoreSubHeading;
        if (/<!-- {docsify-ignore} -->/g.test(content)) {
            content = content.replace("\x3c!-- {docsify-ignore} --\x3e", "");
            ignoreSubHeading = true;
        }
        if (/{docsify-ignore}/g.test(content)) {
            content = content.replace("{docsify-ignore}", "");
            ignoreSubHeading = true;
        }
        if (/<!-- {docsify-ignore-all} -->/g.test(content)) {
            content = content.replace("\x3c!-- {docsify-ignore-all} --\x3e", "");
            ignoreAllSubs = true;
        }
        if (/{docsify-ignore-all}/g.test(content)) {
            content = content.replace("{docsify-ignore-all}", "");
            ignoreAllSubs = true;
        }
        return {
            content: content,
            ignoreAllSubs: ignoreAllSubs,
            ignoreSubHeading: ignoreSubHeading
        };
    }
    function escapeHtml(string) {
        const entityMap = {
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;"
        };
        return String(string).replace(/[&<>"']/g, (s => entityMap[s]));
    }
    function cached(fn) {
        const cache = Object.create(null);
        return function(str) {
            const key = isPrimitive(str) ? str : JSON.stringify(str);
            if (key in cache) {
                return cache[key];
            }
            return cache[key] = fn(str);
        };
    }
    function isPrimitive(value) {
        return typeof value === "string" || typeof value === "number";
    }
    const isAbsolutePath = cached((path => /(:|(\/{2}))/g.test(path)));
    const getParentPath = cached((path => {
        if (/\/$/g.test(path)) {
            return path;
        }
        const matchingParts = path.match(/(\S*\/)[^/]+$/);
        return matchingParts ? matchingParts[1] : "";
    }));
    const cleanPath = cached((path => path.replace(/^\/+/, "/").replace(/([^:])\/{2,}/g, "$1/")));
    function normaliseFragment(path) {
        return path.split("/").filter((p => p.indexOf("#") === -1)).join("/");
    }
    function getPath(...args) {
        return cleanPath(args.map(normaliseFragment).join("/"));
    }
    function z() {
        return {
            async: false,
            breaks: false,
            extensions: null,
            gfm: true,
            hooks: null,
            pedantic: false,
            renderer: null,
            silent: false,
            tokenizer: null,
            walkTokens: null
        };
    }
    var T = z();
    function G(l) {
        T = l;
    }
    var _ = {
        exec: () => null
    };
    function d(l, e = "") {
        let t = typeof l == "string" ? l : l.source, n = {
            replace: (s, r) => {
                let i = typeof r == "string" ? r : r.source;
                return i = i.replace(m.caret, "$1"), t = t.replace(s, i), n;
            },
            getRegex: () => new RegExp(t, e)
        };
        return n;
    }
    var Re = ((l = "") => {
        try {
            return !!new RegExp("(?<=1)(?<!1)" + l);
        } catch {
            return false;
        }
    })(), m = {
        codeRemoveIndent: /^(?: {1,4}| {0,3}\t)/gm,
        outputLinkReplace: /\\([\[\]])/g,
        indentCodeCompensation: /^(\s+)(?:```)/,
        beginningSpace: /^\s+/,
        endingHash: /#$/,
        startingSpaceChar: /^ /,
        endingSpaceChar: / $/,
        nonSpaceChar: /[^ ]/,
        newLineCharGlobal: /\n/g,
        tabCharGlobal: /\t/g,
        multipleSpaceGlobal: /\s+/g,
        blankLine: /^[ \t]*$/,
        doubleBlankLine: /\n[ \t]*\n[ \t]*$/,
        blockquoteStart: /^ {0,3}>/,
        blockquoteSetextReplace: /\n {0,3}((?:=+|-+) *)(?=\n|$)/g,
        blockquoteSetextReplace2: /^ {0,3}>[ \t]?/gm,
        listReplaceNesting: /^ {1,4}(?=( {4})*[^ ])/g,
        listIsTask: /^\[[ xX]\] +\S/,
        listReplaceTask: /^\[[ xX]\] +/,
        listTaskCheckbox: /\[[ xX]\]/,
        anyLine: /\n.*\n/,
        hrefBrackets: /^<(.*)>$/,
        tableDelimiter: /[:|]/,
        tableAlignChars: /^\||\| *$/g,
        tableRowBlankLine: /\n[ \t]*$/,
        tableAlignRight: /^ *-+: *$/,
        tableAlignCenter: /^ *:-+: *$/,
        tableAlignLeft: /^ *:-+ *$/,
        startATag: /^<a /i,
        endATag: /^<\/a>/i,
        startPreScriptTag: /^<(pre|code|kbd|script)(\s|>)/i,
        endPreScriptTag: /^<\/(pre|code|kbd|script)(\s|>)/i,
        startAngleBracket: /^</,
        endAngleBracket: />$/,
        pedanticHrefTitle: /^([^'"]*[^\s])\s+(['"])(.*)\2/,
        unicodeAlphaNumeric: /[\p{L}\p{N}]/u,
        escapeTest: /[&<>"']/,
        escapeReplace: /[&<>"']/g,
        escapeTestNoEncode: /[<>"']|&(?!(#\d{1,7}|#[Xx][a-fA-F0-9]{1,6}|\w+);)/,
        escapeReplaceNoEncode: /[<>"']|&(?!(#\d{1,7}|#[Xx][a-fA-F0-9]{1,6}|\w+);)/g,
        caret: /(^|[^\[])\^/g,
        percentDecode: /%25/g,
        findPipe: /\|/g,
        splitPipe: / \|/,
        slashPipe: /\\\|/g,
        carriageReturn: /\r\n|\r/g,
        spaceLine: /^ +$/gm,
        notSpaceStart: /^\S*/,
        endingNewline: /\n$/,
        listItemRegex: l => new RegExp(`^( {0,3}${l})((?:[\t ][^\\n]*)?(?:\\n|$))`),
        nextBulletRegex: l => new RegExp(`^ {0,${Math.min(3, l - 1)}}(?:[*+-]|\\d{1,9}[.)])((?:[ \t][^\\n]*)?(?:\\n|$))`),
        hrRegex: l => new RegExp(`^ {0,${Math.min(3, l - 1)}}((?:- *){3,}|(?:_ *){3,}|(?:\\* *){3,})(?:\\n+|$)`),
        fencesBeginRegex: l => new RegExp(`^ {0,${Math.min(3, l - 1)}}(?:\`\`\`|~~~)`),
        headingBeginRegex: l => new RegExp(`^ {0,${Math.min(3, l - 1)}}#`),
        htmlBeginRegex: l => new RegExp(`^ {0,${Math.min(3, l - 1)}}<(?:[a-z].*>|!--)`, "i"),
        blockquoteBeginRegex: l => new RegExp(`^ {0,${Math.min(3, l - 1)}}>`)
    }, Te = /^(?:[ \t]*(?:\n|$))+/, Oe = /^((?: {4}| {0,3}\t)[^\n]+(?:\n(?:[ \t]*(?:\n|$))*)?)+/, we = /^ {0,3}(`{3,}(?=[^`\n]*(?:\n|$))|~{3,})([^\n]*)(?:\n|$)(?:|([\s\S]*?)(?:\n|$))(?: {0,3}\1[~`]* *(?=\n|$)|$)/, I = /^ {0,3}((?:-[\t ]*){3,}|(?:_[ \t]*){3,}|(?:\*[ \t]*){3,})(?:\n+|$)/, ye = /^ {0,3}(#{1,6})(?=\s|$)(.*)(?:\n+|$)/, Q = / {0,3}(?:[*+-]|\d{1,9}[.)])/, ie = /^(?!bull |blockCode|fences|blockquote|heading|html|table)((?:.|\n(?!\s*?\n|bull |blockCode|fences|blockquote|heading|html|table))+?)\n {0,3}(=+|-+) *(?:\n+|$)/, oe = d(ie).replace(/bull/g, Q).replace(/blockCode/g, /(?: {4}| {0,3}\t)/).replace(/fences/g, / {0,3}(?:`{3,}|~{3,})/).replace(/blockquote/g, / {0,3}>/).replace(/heading/g, / {0,3}#{1,6}/).replace(/html/g, / {0,3}<[^\n>]+>\n/).replace(/\|table/g, "").getRegex(), Pe = d(ie).replace(/bull/g, Q).replace(/blockCode/g, /(?: {4}| {0,3}\t)/).replace(/fences/g, / {0,3}(?:`{3,}|~{3,})/).replace(/blockquote/g, / {0,3}>/).replace(/heading/g, / {0,3}#{1,6}/).replace(/html/g, / {0,3}<[^\n>]+>\n/).replace(/table/g, / {0,3}\|?(?:[:\- ]*\|)+[\:\- ]*\n/).getRegex(), j = /^([^\n]+(?:\n(?!hr|heading|lheading|blockquote|fences|list|html|table| +\n)[^\n]+)*)/, Se = /^[^\n]+/, F = /(?!\s*\])(?:\\[\s\S]|[^\[\]\\])+/, $e = d(/^ {0,3}\[(label)\]: *(?:\n[ \t]*)?([^<\s][^\s]*|<.*?>)(?:(?: +(?:\n[ \t]*)?| *\n[ \t]*)(title))? *(?:\n+|$)/).replace("label", F).replace("title", /(?:"(?:\\"?|[^"\\])*"|'[^'\n]*(?:\n[^'\n]+)*\n?'|\([^()]*\))/).getRegex(), Le = d(/^(bull)([ \t][^\n]+?)?(?:\n|$)/).replace(/bull/g, Q).getRegex(), v = "address|article|aside|base|basefont|blockquote|body|caption|center|col|colgroup|dd|details|dialog|dir|div|dl|dt|fieldset|figcaption|figure|footer|form|frame|frameset|h[1-6]|head|header|hr|html|iframe|legend|li|link|main|menu|menuitem|meta|nav|noframes|ol|optgroup|option|p|param|search|section|summary|table|tbody|td|tfoot|th|thead|title|tr|track|ul", U = /<!--(?:-?>|[\s\S]*?(?:-->|$))/, _e = d("^ {0,3}(?:<(script|pre|style|textarea)[\\s>][\\s\\S]*?(?:</\\1>[^\\n]*\\n+|$)|comment[^\\n]*(\\n+|$)|<\\?[\\s\\S]*?(?:\\?>\\n*|$)|<![A-Z][\\s\\S]*?(?:>\\n*|$)|<!\\[CDATA\\[[\\s\\S]*?(?:\\]\\]>\\n*|$)|</?(tag)(?: +|\\n|/?>)[\\s\\S]*?(?:(?:\\n[ \t]*)+\\n|$)|<(?!script|pre|style|textarea)([a-z][\\w-]*)(?:attribute)*? */?>(?=[ \\t]*(?:\\n|$))[\\s\\S]*?(?:(?:\\n[ \t]*)+\\n|$)|</(?!script|pre|style|textarea)[a-z][\\w-]*\\s*>(?=[ \\t]*(?:\\n|$))[\\s\\S]*?(?:(?:\\n[ \t]*)+\\n|$))", "i").replace("comment", U).replace("tag", v).replace("attribute", / +[a-zA-Z:_][\w.:-]*(?: *= *"[^"\n]*"| *= *'[^'\n]*'| *= *[^\s"'=<>`]+)?/).getRegex(), ae = d(j).replace("hr", I).replace("heading", " {0,3}#{1,6}(?:\\s|$)").replace("|lheading", "").replace("|table", "").replace("blockquote", " {0,3}>").replace("fences", " {0,3}(?:`{3,}(?=[^`\\n]*\\n)|~{3,})[^\\n]*\\n").replace("list", " {0,3}(?:[*+-]|1[.)])[ \\t]").replace("html", "</?(?:tag)(?: +|\\n|/?>)|<(?:script|pre|style|textarea|!--)").replace("tag", v).getRegex(), Me = d(/^( {0,3}> ?(paragraph|[^\n]*)(?:\n|$))+/).replace("paragraph", ae).getRegex(), K = {
        blockquote: Me,
        code: Oe,
        def: $e,
        fences: we,
        heading: ye,
        hr: I,
        html: _e,
        lheading: oe,
        list: Le,
        newline: Te,
        paragraph: ae,
        table: _,
        text: Se
    }, re = d("^ *([^\\n ].*)\\n {0,3}((?:\\| *)?:?-+:? *(?:\\| *:?-+:? *)*(?:\\| *)?)(?:\\n((?:(?! *\\n|hr|heading|blockquote|code|fences|list|html).*(?:\\n|$))*)\\n*|$)").replace("hr", I).replace("heading", " {0,3}#{1,6}(?:\\s|$)").replace("blockquote", " {0,3}>").replace("code", "(?: {4}| {0,3}\t)[^\\n]").replace("fences", " {0,3}(?:`{3,}(?=[^`\\n]*\\n)|~{3,})[^\\n]*\\n").replace("list", " {0,3}(?:[*+-]|1[.)])[ \\t]").replace("html", "</?(?:tag)(?: +|\\n|/?>)|<(?:script|pre|style|textarea|!--)").replace("tag", v).getRegex(), ze = {
        ...K,
        lheading: Pe,
        table: re,
        paragraph: d(j).replace("hr", I).replace("heading", " {0,3}#{1,6}(?:\\s|$)").replace("|lheading", "").replace("table", re).replace("blockquote", " {0,3}>").replace("fences", " {0,3}(?:`{3,}(?=[^`\\n]*\\n)|~{3,})[^\\n]*\\n").replace("list", " {0,3}(?:[*+-]|1[.)])[ \\t]").replace("html", "</?(?:tag)(?: +|\\n|/?>)|<(?:script|pre|style|textarea|!--)").replace("tag", v).getRegex()
    }, Ee = {
        ...K,
        html: d(`^ *(?:comment *(?:\\n|\\s*$)|<(tag)[\\s\\S]+?</\\1> *(?:\\n{2,}|\\s*$)|<tag(?:"[^"]*"|'[^']*'|\\s[^'"/>\\s]*)*?/?> *(?:\\n{2,}|\\s*$))`).replace("comment", U).replace(/tag/g, "(?!(?:a|em|strong|small|s|cite|q|dfn|abbr|data|time|code|var|samp|kbd|sub|sup|i|b|u|mark|ruby|rt|rp|bdi|bdo|span|br|wbr|ins|del|img)\\b)\\w+(?!:|[^\\w\\s@]*@)\\b").getRegex(),
        def: /^ *\[([^\]]+)\]: *<?([^\s>]+)>?(?: +(["(][^\n]+[")]))? *(?:\n+|$)/,
        heading: /^(#{1,6})(.*)(?:\n+|$)/,
        fences: _,
        lheading: /^(.+?)\n {0,3}(=+|-+) *(?:\n+|$)/,
        paragraph: d(j).replace("hr", I).replace("heading", ` *#{1,6} *[^\n]`).replace("lheading", oe).replace("|table", "").replace("blockquote", " {0,3}>").replace("|fences", "").replace("|list", "").replace("|html", "").replace("|tag", "").getRegex()
    }, Ae = /^\\([!"#$%&'()*+,\-./:;<=>?@\[\]\\^_`{|}~])/, Ce = /^(`+)([^`]|[^`][\s\S]*?[^`])\1(?!`)/, le = /^( {2,}|\\)\n(?!\s*$)/, Ie = /^(`+|[^`])(?:(?= {2,}\n)|[\s\S]*?(?:(?=[\\<!\[`*_]|\b_|$)|[^ ](?= {2,}\n)))/, E = /[\p{P}\p{S}]/u, H = /[\s\p{P}\p{S}]/u, W = /[^\s\p{P}\p{S}]/u, Be = d(/^((?![*_])punctSpace)/, "u").replace(/punctSpace/g, H).getRegex(), ue = /(?!~)[\p{P}\p{S}]/u, De = /(?!~)[\s\p{P}\p{S}]/u, qe = /(?:[^\s\p{P}\p{S}]|~)/u, ve = d(/link|precode-code|html/, "g").replace("link", /\[(?:[^\[\]`]|(?<a>`+)[^`]+\k<a>(?!`))*?\]\((?:\\[\s\S]|[^\\\(\)]|\((?:\\[\s\S]|[^\\\(\)])*\))*\)/).replace("precode-", Re ? "(?<!`)()" : "(^^|[^`])").replace("code", /(?<b>`+)[^`]+\k<b>(?!`)/).replace("html", /<(?! )[^<>]*?>/).getRegex(), pe = /^(?:\*+(?:((?!\*)punct)|([^\s*]))?)|^_+(?:((?!_)punct)|([^\s_]))?/, He = d(pe, "u").replace(/punct/g, E).getRegex(), Ze = d(pe, "u").replace(/punct/g, ue).getRegex(), ce = "^[^_*]*?__[^_*]*?\\*[^_*]*?(?=__)|[^*]+(?=[^*])|(?!\\*)punct(\\*+)(?=[\\s]|$)|notPunctSpace(\\*+)(?!\\*)(?=punctSpace|$)|(?!\\*)punctSpace(\\*+)(?=notPunctSpace)|[\\s](\\*+)(?!\\*)(?=punct)|(?!\\*)punct(\\*+)(?!\\*)(?=punct)|notPunctSpace(\\*+)(?=notPunctSpace)", Ge = d(ce, "gu").replace(/notPunctSpace/g, W).replace(/punctSpace/g, H).replace(/punct/g, E).getRegex(), Ne = d(ce, "gu").replace(/notPunctSpace/g, qe).replace(/punctSpace/g, De).replace(/punct/g, ue).getRegex(), Qe = d("^[^_*]*?\\*\\*[^_*]*?_[^_*]*?(?=\\*\\*)|[^_]+(?=[^_])|(?!_)punct(_+)(?=[\\s]|$)|notPunctSpace(_+)(?!_)(?=punctSpace|$)|(?!_)punctSpace(_+)(?=notPunctSpace)|[\\s](_+)(?!_)(?=punct)|(?!_)punct(_+)(?!_)(?=punct)", "gu").replace(/notPunctSpace/g, W).replace(/punctSpace/g, H).replace(/punct/g, E).getRegex(), je = d(/^~~?(?:((?!~)punct)|[^\s~])/, "u").replace(/punct/g, E).getRegex(), Fe = "^[^~]+(?=[^~])|(?!~)punct(~~?)(?=[\\s]|$)|notPunctSpace(~~?)(?!~)(?=punctSpace|$)|(?!~)punctSpace(~~?)(?=notPunctSpace)|[\\s](~~?)(?!~)(?=punct)|(?!~)punct(~~?)(?!~)(?=punct)|notPunctSpace(~~?)(?=notPunctSpace)", Ue = d(Fe, "gu").replace(/notPunctSpace/g, W).replace(/punctSpace/g, H).replace(/punct/g, E).getRegex(), Ke = d(/\\(punct)/, "gu").replace(/punct/g, E).getRegex(), We = d(/^<(scheme:[^\s\x00-\x1f<>]*|email)>/).replace("scheme", /[a-zA-Z][a-zA-Z0-9+.-]{1,31}/).replace("email", /[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+(@)[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)+(?![-_])/).getRegex(), Xe = d(U).replace("(?:--\x3e|$)", "--\x3e").getRegex(), Je = d("^comment|^</[a-zA-Z][\\w:-]*\\s*>|^<[a-zA-Z][\\w-]*(?:attribute)*?\\s*/?>|^<\\?[\\s\\S]*?\\?>|^<![a-zA-Z]+\\s[\\s\\S]*?>|^<!\\[CDATA\\[[\\s\\S]*?\\]\\]>").replace("comment", Xe).replace("attribute", /\s+[a-zA-Z:_][\w.:-]*(?:\s*=\s*"[^"]*"|\s*=\s*'[^']*'|\s*=\s*[^\s"'=<>`]+)?/).getRegex(), q = /(?:\[(?:\\[\s\S]|[^\[\]\\])*\]|\\[\s\S]|`+(?!`)[^`]*?`+(?!`)|``+(?=\])|[^\[\]\\`])*?/, Ve = d(/^!?\[(label)\]\(\s*(href)(?:(?:[ \t]+(?:\n[ \t]*)?|\n[ \t]*)(title))?\s*\)/).replace("label", q).replace("href", /<(?:\\.|[^\n<>\\])+>|[^ \t\n\x00-\x1f]*/).replace("title", /"(?:\\"?|[^"\\])*"|'(?:\\'?|[^'\\])*'|\((?:\\\)?|[^)\\])*\)/).getRegex(), he = d(/^!?\[(label)\]\[(ref)\]/).replace("label", q).replace("ref", F).getRegex(), ke = d(/^!?\[(ref)\](?:\[\])?/).replace("ref", F).getRegex(), Ye = d("reflink|nolink(?!\\()", "g").replace("reflink", he).replace("nolink", ke).getRegex(), se = /[hH][tT][tT][pP][sS]?|[fF][tT][pP]/, X = {
        _backpedal: _,
        anyPunctuation: Ke,
        autolink: We,
        blockSkip: ve,
        br: le,
        code: Ce,
        del: _,
        delLDelim: _,
        delRDelim: _,
        emStrongLDelim: He,
        emStrongRDelimAst: Ge,
        emStrongRDelimUnd: Qe,
        escape: Ae,
        link: Ve,
        nolink: ke,
        punctuation: Be,
        reflink: he,
        reflinkSearch: Ye,
        tag: Je,
        text: Ie,
        url: _
    }, et = {
        ...X,
        link: d(/^!?\[(label)\]\((.*?)\)/).replace("label", q).getRegex(),
        reflink: d(/^!?\[(label)\]\s*\[([^\]]*)\]/).replace("label", q).getRegex()
    }, N = {
        ...X,
        emStrongRDelimAst: Ne,
        emStrongLDelim: Ze,
        delLDelim: je,
        delRDelim: Ue,
        url: d(/^((?:protocol):\/\/|www\.)(?:[a-zA-Z0-9\-]+\.?)+[^\s<]*|^email/).replace("protocol", se).replace("email", /[A-Za-z0-9._+-]+(@)[a-zA-Z0-9-_]+(?:\.[a-zA-Z0-9-_]*[a-zA-Z0-9])+(?![-_])/).getRegex(),
        _backpedal: /(?:[^?!.,:;*_'"~()&]+|\([^)]*\)|&(?![a-zA-Z0-9]+;$)|[?!.,:;*_'"~)]+(?!$))+/,
        del: /^(~~?)(?=[^\s~])((?:\\[\s\S]|[^\\])*?(?:\\[\s\S]|[^\s~\\]))\1(?=[^~]|$)/,
        text: d(/^([`~]+|[^`~])(?:(?= {2,}\n)|(?=[a-zA-Z0-9.!#$%&'*+\/=?_`{\|}~-]+@)|[\s\S]*?(?:(?=[\\<!\[`*~_]|\b_|protocol:\/\/|www\.|$)|[^ ](?= {2,}\n)|[^a-zA-Z0-9.!#$%&'*+\/=?_`{\|}~-](?=[a-zA-Z0-9.!#$%&'*+\/=?_`{\|}~-]+@)))/).replace("protocol", se).getRegex()
    }, tt = {
        ...N,
        br: d(le).replace("{2,}", "*").getRegex(),
        text: d(N.text).replace("\\b_", "\\b_| {2,}\\n").replace(/\{2,\}/g, "*").getRegex()
    }, B = {
        normal: K,
        gfm: ze,
        pedantic: Ee
    }, A = {
        normal: X,
        gfm: N,
        breaks: tt,
        pedantic: et
    };
    var nt = {
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    }, de = l => nt[l];
    function O(l, e) {
        if (e) {
            if (m.escapeTest.test(l)) return l.replace(m.escapeReplace, de);
        } else if (m.escapeTestNoEncode.test(l)) return l.replace(m.escapeReplaceNoEncode, de);
        return l;
    }
    function J(l) {
        try {
            l = encodeURI(l).replace(m.percentDecode, "%");
        } catch {
            return null;
        }
        return l;
    }
    function V(l, e) {
        let t = l.replace(m.findPipe, ((r, i, o) => {
            let u = false, a = i;
            for (;--a >= 0 && o[a] === "\\"; ) u = !u;
            return u ? "|" : " |";
        })), n = t.split(m.splitPipe), s = 0;
        if (n[0].trim() || n.shift(), n.length > 0 && !n.at(-1)?.trim() && n.pop(), e) if (n.length > e) n.splice(e); else for (;n.length < e; ) n.push("");
        for (;s < n.length; s++) n[s] = n[s].trim().replace(m.slashPipe, "|");
        return n;
    }
    function $(l, e, t) {
        let n = l.length;
        if (n === 0) return "";
        let s = 0;
        for (;s < n; ) {
            let r = l.charAt(n - s - 1);
            if (r === e && true) s++; else break;
        }
        return l.slice(0, n - s);
    }
    function Y(l) {
        let e = l.split(`\n`), t = e.length - 1;
        for (;t >= 0 && m.blankLine.test(e[t]); ) t--;
        return e.length - t <= 2 ? l : e.slice(0, t + 1).join(`\n`);
    }
    function ge(l, e) {
        if (l.indexOf(e[1]) === -1) return -1;
        let t = 0;
        for (let n = 0; n < l.length; n++) if (l[n] === "\\") n++; else if (l[n] === e[0]) t++; else if (l[n] === e[1] && (t--, 
        t < 0)) return n;
        return t > 0 ? -2 : -1;
    }
    function fe(l, e = 0) {
        let t = e, n = "";
        for (let s of l) if (s === "\t") {
            let r = 4 - t % 4;
            n += " ".repeat(r), t += r;
        } else n += s, t++;
        return n;
    }
    function me(l, e, t, n, s) {
        let r = e.href, i = e.title || null, o = l[1].replace(s.other.outputLinkReplace, "$1");
        n.state.inLink = true;
        let u = {
            type: l[0].charAt(0) === "!" ? "image" : "link",
            raw: t,
            href: r,
            title: i,
            text: o,
            tokens: n.inlineTokens(o)
        };
        return n.state.inLink = false, u;
    }
    function rt(l, e, t) {
        let n = l.match(t.other.indentCodeCompensation);
        if (n === null) return e;
        let s = n[1];
        return e.split(`\n`).map((r => {
            let i = r.match(t.other.beginningSpace);
            if (i === null) return r;
            let [o] = i;
            return o.length >= s.length ? r.slice(s.length) : r;
        })).join(`\n`);
    }
    var w = class {
        options;
        rules;
        lexer;
        constructor(e) {
            this.options = e || T;
        }
        space(e) {
            let t = this.rules.block.newline.exec(e);
            if (t && t[0].length > 0) return {
                type: "space",
                raw: t[0]
            };
        }
        code(e) {
            let t = this.rules.block.code.exec(e);
            if (t) {
                let n = this.options.pedantic ? t[0] : Y(t[0]), s = n.replace(this.rules.other.codeRemoveIndent, "");
                return {
                    type: "code",
                    raw: n,
                    codeBlockStyle: "indented",
                    text: s
                };
            }
        }
        fences(e) {
            let t = this.rules.block.fences.exec(e);
            if (t) {
                let n = t[0], s = rt(n, t[3] || "", this.rules);
                return {
                    type: "code",
                    raw: n,
                    lang: t[2] ? t[2].trim().replace(this.rules.inline.anyPunctuation, "$1") : t[2],
                    text: s
                };
            }
        }
        heading(e) {
            let t = this.rules.block.heading.exec(e);
            if (t) {
                let n = t[2].trim();
                if (this.rules.other.endingHash.test(n)) {
                    let s = $(n, "#");
                    (this.options.pedantic || !s || this.rules.other.endingSpaceChar.test(s)) && (n = s.trim());
                }
                return {
                    type: "heading",
                    raw: $(t[0], `\n`),
                    depth: t[1].length,
                    text: n,
                    tokens: this.lexer.inline(n)
                };
            }
        }
        hr(e) {
            let t = this.rules.block.hr.exec(e);
            if (t) return {
                type: "hr",
                raw: $(t[0], `\n`)
            };
        }
        blockquote(e) {
            let t = this.rules.block.blockquote.exec(e);
            if (t) {
                let n = $(t[0], `\n`).split(`\n`), s = "", r = "", i = [];
                for (;n.length > 0; ) {
                    let o = false, u = [], a;
                    for (a = 0; a < n.length; a++) if (this.rules.other.blockquoteStart.test(n[a])) u.push(n[a]), 
                    o = true; else if (!o) u.push(n[a]); else break;
                    n = n.slice(a);
                    let c = u.join(`\n`), p = c.replace(this.rules.other.blockquoteSetextReplace, `\n    $1`).replace(this.rules.other.blockquoteSetextReplace2, "");
                    s = s ? `${s}\n${c}` : c, r = r ? `${r}\n${p}` : p;
                    let k = this.lexer.state.top;
                    if (this.lexer.state.top = true, this.lexer.blockTokens(p, i, true), this.lexer.state.top = k, 
                    n.length === 0) break;
                    let h = i.at(-1);
                    if (h?.type === "code") break;
                    if (h?.type === "blockquote") {
                        let R = h, f = R.raw + `\n` + n.join(`\n`), S = this.blockquote(f);
                        i[i.length - 1] = S, s = s.substring(0, s.length - R.raw.length) + S.raw, r = r.substring(0, r.length - R.text.length) + S.text;
                        break;
                    } else if (h?.type === "list") {
                        let R = h, f = R.raw + `\n` + n.join(`\n`), S = this.list(f);
                        i[i.length - 1] = S, s = s.substring(0, s.length - h.raw.length) + S.raw, r = r.substring(0, r.length - R.raw.length) + S.raw, 
                        n = f.substring(i.at(-1).raw.length).split(`\n`);
                        continue;
                    }
                }
                return {
                    type: "blockquote",
                    raw: s,
                    tokens: i,
                    text: r
                };
            }
        }
        list(e) {
            let t = this.rules.block.list.exec(e);
            if (t) {
                let n = t[1].trim(), s = n.length > 1, r = {
                    type: "list",
                    raw: "",
                    ordered: s,
                    start: s ? +n.slice(0, -1) : "",
                    loose: false,
                    items: []
                };
                n = s ? `\\d{1,9}\\${n.slice(-1)}` : `\\${n}`, this.options.pedantic && (n = s ? n : "[*+-]");
                let i = this.rules.other.listItemRegex(n), o = false;
                for (;e; ) {
                    let a = false, c = "", p = "";
                    if (!(t = i.exec(e)) || this.rules.block.hr.test(e)) break;
                    c = t[0], e = e.substring(c.length);
                    let k = fe(t[2].split(`\n`, 1)[0], t[1].length), h = e.split(`\n`, 1)[0], R = !k.trim(), f = 0;
                    if (this.options.pedantic ? (f = 2, p = k.trimStart()) : R ? f = t[1].length + 1 : (f = k.search(this.rules.other.nonSpaceChar), 
                    f = f > 4 ? 1 : f, p = k.slice(f), f += t[1].length), R && this.rules.other.blankLine.test(h) && (c += h + `\n`, 
                    e = e.substring(h.length + 1), a = true), !a) {
                        let S = this.rules.other.nextBulletRegex(f), ee = this.rules.other.hrRegex(f), te = this.rules.other.fencesBeginRegex(f), ne = this.rules.other.headingBeginRegex(f), xe = this.rules.other.htmlBeginRegex(f), be = this.rules.other.blockquoteBeginRegex(f);
                        for (;e; ) {
                            let Z = e.split(`\n`, 1)[0], C;
                            if (h = Z, this.options.pedantic ? (h = h.replace(this.rules.other.listReplaceNesting, "  "), 
                            C = h) : C = h.replace(this.rules.other.tabCharGlobal, "    "), te.test(h) || ne.test(h) || xe.test(h) || be.test(h) || S.test(h) || ee.test(h)) break;
                            if (C.search(this.rules.other.nonSpaceChar) >= f || !h.trim()) p += `\n` + C.slice(f); else {
                                if (R || k.replace(this.rules.other.tabCharGlobal, "    ").search(this.rules.other.nonSpaceChar) >= 4 || te.test(k) || ne.test(k) || ee.test(k)) break;
                                p += `\n` + h;
                            }
                            R = !h.trim(), c += Z + `\n`, e = e.substring(Z.length + 1), k = C.slice(f);
                        }
                    }
                    r.loose || (o ? r.loose = true : this.rules.other.doubleBlankLine.test(c) && (o = true)), 
                    r.items.push({
                        type: "list_item",
                        raw: c,
                        task: !!this.options.gfm && this.rules.other.listIsTask.test(p),
                        loose: false,
                        text: p,
                        tokens: []
                    }), r.raw += c;
                }
                let u = r.items.at(-1);
                if (u) u.raw = u.raw.trimEnd(), u.text = u.text.trimEnd(); else return;
                r.raw = r.raw.trimEnd();
                for (let a of r.items) {
                    this.lexer.state.top = false, a.tokens = this.lexer.blockTokens(a.text, []);
                    let c = a.tokens[0];
                    if (a.task && (c?.type === "text" || c?.type === "paragraph")) {
                        a.text = a.text.replace(this.rules.other.listReplaceTask, ""), c.raw = c.raw.replace(this.rules.other.listReplaceTask, ""), 
                        c.text = c.text.replace(this.rules.other.listReplaceTask, "");
                        for (let k = this.lexer.inlineQueue.length - 1; k >= 0; k--) if (this.rules.other.listIsTask.test(this.lexer.inlineQueue[k].src)) {
                            this.lexer.inlineQueue[k].src = this.lexer.inlineQueue[k].src.replace(this.rules.other.listReplaceTask, "");
                            break;
                        }
                        let p = this.rules.other.listTaskCheckbox.exec(a.raw);
                        if (p) {
                            let k = {
                                type: "checkbox",
                                raw: p[0] + " ",
                                checked: p[0] !== "[ ]"
                            };
                            a.checked = k.checked, r.loose ? a.tokens[0] && [ "paragraph", "text" ].includes(a.tokens[0].type) && "tokens" in a.tokens[0] && a.tokens[0].tokens ? (a.tokens[0].raw = k.raw + a.tokens[0].raw, 
                            a.tokens[0].text = k.raw + a.tokens[0].text, a.tokens[0].tokens.unshift(k)) : a.tokens.unshift({
                                type: "paragraph",
                                raw: k.raw,
                                text: k.raw,
                                tokens: [ k ]
                            }) : a.tokens.unshift(k);
                        }
                    } else a.task && (a.task = false);
                    if (!r.loose) {
                        let p = a.tokens.filter((h => h.type === "space")), k = p.length > 0 && p.some((h => this.rules.other.anyLine.test(h.raw)));
                        r.loose = k;
                    }
                }
                if (r.loose) for (let a of r.items) {
                    a.loose = true;
                    for (let c of a.tokens) c.type === "text" && (c.type = "paragraph");
                }
                return r;
            }
        }
        html(e) {
            let t = this.rules.block.html.exec(e);
            if (t) {
                let n = Y(t[0]);
                return {
                    type: "html",
                    block: true,
                    raw: n,
                    pre: t[1] === "pre" || t[1] === "script" || t[1] === "style",
                    text: n
                };
            }
        }
        def(e) {
            let t = this.rules.block.def.exec(e);
            if (t) {
                let n = t[1].toLowerCase().replace(this.rules.other.multipleSpaceGlobal, " "), s = t[2] ? t[2].replace(this.rules.other.hrefBrackets, "$1").replace(this.rules.inline.anyPunctuation, "$1") : "", r = t[3] ? t[3].substring(1, t[3].length - 1).replace(this.rules.inline.anyPunctuation, "$1") : t[3];
                return {
                    type: "def",
                    tag: n,
                    raw: $(t[0], `\n`),
                    href: s,
                    title: r
                };
            }
        }
        table(e) {
            let t = this.rules.block.table.exec(e);
            if (!t || !this.rules.other.tableDelimiter.test(t[2])) return;
            let n = V(t[1]), s = t[2].replace(this.rules.other.tableAlignChars, "").split("|"), r = t[3]?.trim() ? t[3].replace(this.rules.other.tableRowBlankLine, "").split(`\n`) : [], i = {
                type: "table",
                raw: $(t[0], `\n`),
                header: [],
                align: [],
                rows: []
            };
            if (n.length === s.length) {
                for (let o of s) this.rules.other.tableAlignRight.test(o) ? i.align.push("right") : this.rules.other.tableAlignCenter.test(o) ? i.align.push("center") : this.rules.other.tableAlignLeft.test(o) ? i.align.push("left") : i.align.push(null);
                for (let o = 0; o < n.length; o++) i.header.push({
                    text: n[o],
                    tokens: this.lexer.inline(n[o]),
                    header: true,
                    align: i.align[o]
                });
                for (let o of r) i.rows.push(V(o, i.header.length).map(((u, a) => ({
                    text: u,
                    tokens: this.lexer.inline(u),
                    header: false,
                    align: i.align[a]
                }))));
                return i;
            }
        }
        lheading(e) {
            let t = this.rules.block.lheading.exec(e);
            if (t) {
                let n = t[1].trim();
                return {
                    type: "heading",
                    raw: $(t[0], `\n`),
                    depth: t[2].charAt(0) === "=" ? 1 : 2,
                    text: n,
                    tokens: this.lexer.inline(n)
                };
            }
        }
        paragraph(e) {
            let t = this.rules.block.paragraph.exec(e);
            if (t) {
                let n = t[1].charAt(t[1].length - 1) === `\n` ? t[1].slice(0, -1) : t[1];
                return {
                    type: "paragraph",
                    raw: t[0],
                    text: n,
                    tokens: this.lexer.inline(n)
                };
            }
        }
        text(e) {
            let t = this.rules.block.text.exec(e);
            if (t) return {
                type: "text",
                raw: t[0],
                text: t[0],
                tokens: this.lexer.inline(t[0])
            };
        }
        escape(e) {
            let t = this.rules.inline.escape.exec(e);
            if (t) return {
                type: "escape",
                raw: t[0],
                text: t[1]
            };
        }
        tag(e) {
            let t = this.rules.inline.tag.exec(e);
            if (t) return !this.lexer.state.inLink && this.rules.other.startATag.test(t[0]) ? this.lexer.state.inLink = true : this.lexer.state.inLink && this.rules.other.endATag.test(t[0]) && (this.lexer.state.inLink = false), 
            !this.lexer.state.inRawBlock && this.rules.other.startPreScriptTag.test(t[0]) ? this.lexer.state.inRawBlock = true : this.lexer.state.inRawBlock && this.rules.other.endPreScriptTag.test(t[0]) && (this.lexer.state.inRawBlock = false), 
            {
                type: "html",
                raw: t[0],
                inLink: this.lexer.state.inLink,
                inRawBlock: this.lexer.state.inRawBlock,
                block: false,
                text: t[0]
            };
        }
        link(e) {
            let t = this.rules.inline.link.exec(e);
            if (t) {
                let n = t[2].trim();
                if (!this.options.pedantic && this.rules.other.startAngleBracket.test(n)) {
                    if (!this.rules.other.endAngleBracket.test(n)) return;
                    let i = $(n.slice(0, -1), "\\");
                    if ((n.length - i.length) % 2 === 0) return;
                } else {
                    let i = ge(t[2], "()");
                    if (i === -2) return;
                    if (i > -1) {
                        let u = (t[0].indexOf("!") === 0 ? 5 : 4) + t[1].length + i;
                        t[2] = t[2].substring(0, i), t[0] = t[0].substring(0, u).trim(), t[3] = "";
                    }
                }
                let s = t[2], r = "";
                if (this.options.pedantic) {
                    let i = this.rules.other.pedanticHrefTitle.exec(s);
                    i && (s = i[1], r = i[3]);
                } else r = t[3] ? t[3].slice(1, -1) : "";
                return s = s.trim(), this.rules.other.startAngleBracket.test(s) && (this.options.pedantic && !this.rules.other.endAngleBracket.test(n) ? s = s.slice(1) : s = s.slice(1, -1)), 
                me(t, {
                    href: s && s.replace(this.rules.inline.anyPunctuation, "$1"),
                    title: r && r.replace(this.rules.inline.anyPunctuation, "$1")
                }, t[0], this.lexer, this.rules);
            }
        }
        reflink(e, t) {
            let n;
            if ((n = this.rules.inline.reflink.exec(e)) || (n = this.rules.inline.nolink.exec(e))) {
                let s = (n[2] || n[1]).replace(this.rules.other.multipleSpaceGlobal, " "), r = t[s.toLowerCase()];
                if (!r) {
                    let i = n[0].charAt(0);
                    return {
                        type: "text",
                        raw: i,
                        text: i
                    };
                }
                return me(n, r, n[0], this.lexer, this.rules);
            }
        }
        emStrong(e, t, n = "") {
            let s = this.rules.inline.emStrongLDelim.exec(e);
            if (!s || !s[1] && !s[2] && !s[3] && !s[4] || s[4] && n.match(this.rules.other.unicodeAlphaNumeric)) return;
            if (!(s[1] || s[3] || "") || !n || this.rules.inline.punctuation.exec(n)) {
                let i = [ ...s[0] ].length - 1, o, u, a = i, c = 0, p = s[0][0] === "*" ? this.rules.inline.emStrongRDelimAst : this.rules.inline.emStrongRDelimUnd;
                for (p.lastIndex = 0, t = t.slice(-1 * e.length + i); (s = p.exec(t)) !== null; ) {
                    if (o = s[1] || s[2] || s[3] || s[4] || s[5] || s[6], !o) continue;
                    if (u = [ ...o ].length, s[3] || s[4]) {
                        a += u;
                        continue;
                    } else if ((s[5] || s[6]) && i % 3 && !((i + u) % 3)) {
                        c += u;
                        continue;
                    }
                    if (a -= u, a > 0) continue;
                    u = Math.min(u, u + a + c);
                    let k = [ ...s[0] ][0].length, h = e.slice(0, i + s.index + k + u);
                    if (Math.min(i, u) % 2) {
                        let f = h.slice(1, -1);
                        return {
                            type: "em",
                            raw: h,
                            text: f,
                            tokens: this.lexer.inlineTokens(f)
                        };
                    }
                    let R = h.slice(2, -2);
                    return {
                        type: "strong",
                        raw: h,
                        text: R,
                        tokens: this.lexer.inlineTokens(R)
                    };
                }
            }
        }
        codespan(e) {
            let t = this.rules.inline.code.exec(e);
            if (t) {
                let n = t[2].replace(this.rules.other.newLineCharGlobal, " "), s = this.rules.other.nonSpaceChar.test(n), r = this.rules.other.startingSpaceChar.test(n) && this.rules.other.endingSpaceChar.test(n);
                return s && r && (n = n.substring(1, n.length - 1)), {
                    type: "codespan",
                    raw: t[0],
                    text: n
                };
            }
        }
        br(e) {
            let t = this.rules.inline.br.exec(e);
            if (t) return {
                type: "br",
                raw: t[0]
            };
        }
        del(e, t, n = "") {
            let s = this.rules.inline.delLDelim.exec(e);
            if (!s) return;
            if (!(s[1] || "") || !n || this.rules.inline.punctuation.exec(n)) {
                let i = [ ...s[0] ].length - 1, o, u, a = i, c = this.rules.inline.delRDelim;
                for (c.lastIndex = 0, t = t.slice(-1 * e.length + i); (s = c.exec(t)) !== null; ) {
                    if (o = s[1] || s[2] || s[3] || s[4] || s[5] || s[6], !o || (u = [ ...o ].length, 
                    u !== i)) continue;
                    if (s[3] || s[4]) {
                        a += u;
                        continue;
                    }
                    if (a -= u, a > 0) continue;
                    u = Math.min(u, u + a);
                    let p = [ ...s[0] ][0].length, k = e.slice(0, i + s.index + p + u), h = k.slice(i, -i);
                    return {
                        type: "del",
                        raw: k,
                        text: h,
                        tokens: this.lexer.inlineTokens(h)
                    };
                }
            }
        }
        autolink(e) {
            let t = this.rules.inline.autolink.exec(e);
            if (t) {
                let n, s;
                return t[2] === "@" ? (n = t[1], s = "mailto:" + n) : (n = t[1], s = n), {
                    type: "link",
                    raw: t[0],
                    text: n,
                    href: s,
                    tokens: [ {
                        type: "text",
                        raw: n,
                        text: n
                    } ]
                };
            }
        }
        url(e) {
            let t;
            if (t = this.rules.inline.url.exec(e)) {
                let n, s;
                if (t[2] === "@") n = t[0], s = "mailto:" + n; else {
                    let r;
                    do {
                        r = t[0], t[0] = this.rules.inline._backpedal.exec(t[0])?.[0] ?? "";
                    } while (r !== t[0]);
                    n = t[0], t[1] === "www." ? s = "http://" + t[0] : s = t[0];
                }
                return {
                    type: "link",
                    raw: t[0],
                    text: n,
                    href: s,
                    tokens: [ {
                        type: "text",
                        raw: n,
                        text: n
                    } ]
                };
            }
        }
        inlineText(e) {
            let t = this.rules.inline.text.exec(e);
            if (t) {
                let n = this.lexer.state.inRawBlock;
                return {
                    type: "text",
                    raw: t[0],
                    text: t[0],
                    escaped: n
                };
            }
        }
    };
    var x = class l {
        tokens;
        options;
        state;
        inlineQueue;
        tokenizer;
        constructor(e) {
            this.tokens = [], this.tokens.links = Object.create(null), this.options = e || T, 
            this.options.tokenizer = this.options.tokenizer || new w, this.tokenizer = this.options.tokenizer, 
            this.tokenizer.options = this.options, this.tokenizer.lexer = this, this.inlineQueue = [], 
            this.state = {
                inLink: false,
                inRawBlock: false,
                top: true
            };
            let t = {
                other: m,
                block: B.normal,
                inline: A.normal
            };
            this.options.pedantic ? (t.block = B.pedantic, t.inline = A.pedantic) : this.options.gfm && (t.block = B.gfm, 
            this.options.breaks ? t.inline = A.breaks : t.inline = A.gfm), this.tokenizer.rules = t;
        }
        static get rules() {
            return {
                block: B,
                inline: A
            };
        }
        static lex(e, t) {
            return new l(t).lex(e);
        }
        static lexInline(e, t) {
            return new l(t).inlineTokens(e);
        }
        lex(e) {
            e = e.replace(m.carriageReturn, `\n`), this.blockTokens(e, this.tokens);
            for (let t = 0; t < this.inlineQueue.length; t++) {
                let n = this.inlineQueue[t];
                this.inlineTokens(n.src, n.tokens);
            }
            return this.inlineQueue = [], this.tokens;
        }
        blockTokens(e, t = [], n = false) {
            this.tokenizer.lexer = this, this.options.pedantic && (e = e.replace(m.tabCharGlobal, "    ").replace(m.spaceLine, ""));
            let s = 1 / 0;
            for (;e; ) {
                if (e.length < s) s = e.length; else {
                    this.infiniteLoopError(e.charCodeAt(0));
                    break;
                }
                let r;
                if (this.options.extensions?.block?.some((o => (r = o.call({
                    lexer: this
                }, e, t)) ? (e = e.substring(r.raw.length), t.push(r), true) : false))) continue;
                if (r = this.tokenizer.space(e)) {
                    e = e.substring(r.raw.length);
                    let o = t.at(-1);
                    r.raw.length === 1 && o !== void 0 ? o.raw += `\n` : t.push(r);
                    continue;
                }
                if (r = this.tokenizer.code(e)) {
                    e = e.substring(r.raw.length);
                    let o = t.at(-1);
                    o?.type === "paragraph" || o?.type === "text" ? (o.raw += (o.raw.endsWith(`\n`) ? "" : `\n`) + r.raw, 
                    o.text += `\n` + r.text, this.inlineQueue.at(-1).src = o.text) : t.push(r);
                    continue;
                }
                if (r = this.tokenizer.fences(e)) {
                    e = e.substring(r.raw.length), t.push(r);
                    continue;
                }
                if (r = this.tokenizer.heading(e)) {
                    e = e.substring(r.raw.length), t.push(r);
                    continue;
                }
                if (r = this.tokenizer.hr(e)) {
                    e = e.substring(r.raw.length), t.push(r);
                    continue;
                }
                if (r = this.tokenizer.blockquote(e)) {
                    e = e.substring(r.raw.length), t.push(r);
                    continue;
                }
                if (r = this.tokenizer.list(e)) {
                    e = e.substring(r.raw.length), t.push(r);
                    continue;
                }
                if (r = this.tokenizer.html(e)) {
                    e = e.substring(r.raw.length), t.push(r);
                    continue;
                }
                if (r = this.tokenizer.def(e)) {
                    e = e.substring(r.raw.length);
                    let o = t.at(-1);
                    o?.type === "paragraph" || o?.type === "text" ? (o.raw += (o.raw.endsWith(`\n`) ? "" : `\n`) + r.raw, 
                    o.text += `\n` + r.raw, this.inlineQueue.at(-1).src = o.text) : this.tokens.links[r.tag] || (this.tokens.links[r.tag] = {
                        href: r.href,
                        title: r.title
                    }, t.push(r));
                    continue;
                }
                if (r = this.tokenizer.table(e)) {
                    e = e.substring(r.raw.length), t.push(r);
                    continue;
                }
                if (r = this.tokenizer.lheading(e)) {
                    e = e.substring(r.raw.length), t.push(r);
                    continue;
                }
                let i = e;
                if (this.options.extensions?.startBlock) {
                    let o = 1 / 0, u = e.slice(1), a;
                    this.options.extensions.startBlock.forEach((c => {
                        a = c.call({
                            lexer: this
                        }, u), typeof a == "number" && a >= 0 && (o = Math.min(o, a));
                    })), o < 1 / 0 && o >= 0 && (i = e.substring(0, o + 1));
                }
                if (this.state.top && (r = this.tokenizer.paragraph(i))) {
                    let o = t.at(-1);
                    n && o?.type === "paragraph" ? (o.raw += (o.raw.endsWith(`\n`) ? "" : `\n`) + r.raw, 
                    o.text += `\n` + r.text, this.inlineQueue.pop(), this.inlineQueue.at(-1).src = o.text) : t.push(r), 
                    n = i.length !== e.length, e = e.substring(r.raw.length);
                    continue;
                }
                if (r = this.tokenizer.text(e)) {
                    e = e.substring(r.raw.length);
                    let o = t.at(-1);
                    o?.type === "text" ? (o.raw += (o.raw.endsWith(`\n`) ? "" : `\n`) + r.raw, o.text += `\n` + r.text, 
                    this.inlineQueue.pop(), this.inlineQueue.at(-1).src = o.text) : t.push(r);
                    continue;
                }
                if (e) {
                    this.infiniteLoopError(e.charCodeAt(0));
                    break;
                }
            }
            return this.state.top = true, t;
        }
        inline(e, t = []) {
            return this.inlineQueue.push({
                src: e,
                tokens: t
            }), t;
        }
        inlineTokens(e, t = []) {
            this.tokenizer.lexer = this;
            let n = e, s = null;
            if (this.tokens.links) {
                let a = Object.keys(this.tokens.links);
                if (a.length > 0) for (;(s = this.tokenizer.rules.inline.reflinkSearch.exec(n)) !== null; ) a.includes(s[0].slice(s[0].lastIndexOf("[") + 1, -1)) && (n = n.slice(0, s.index) + "[" + "a".repeat(s[0].length - 2) + "]" + n.slice(this.tokenizer.rules.inline.reflinkSearch.lastIndex));
            }
            for (;(s = this.tokenizer.rules.inline.anyPunctuation.exec(n)) !== null; ) n = n.slice(0, s.index) + "++" + n.slice(this.tokenizer.rules.inline.anyPunctuation.lastIndex);
            let r;
            for (;(s = this.tokenizer.rules.inline.blockSkip.exec(n)) !== null; ) r = s[2] ? s[2].length : 0, 
            n = n.slice(0, s.index + r) + "[" + "a".repeat(s[0].length - r - 2) + "]" + n.slice(this.tokenizer.rules.inline.blockSkip.lastIndex);
            n = this.options.hooks?.emStrongMask?.call({
                lexer: this
            }, n) ?? n;
            let i = false, o = "", u = 1 / 0;
            for (;e; ) {
                if (e.length < u) u = e.length; else {
                    this.infiniteLoopError(e.charCodeAt(0));
                    break;
                }
                i || (o = ""), i = false;
                let a;
                if (this.options.extensions?.inline?.some((p => (a = p.call({
                    lexer: this
                }, e, t)) ? (e = e.substring(a.raw.length), t.push(a), true) : false))) continue;
                if (a = this.tokenizer.escape(e)) {
                    e = e.substring(a.raw.length), t.push(a);
                    continue;
                }
                if (a = this.tokenizer.tag(e)) {
                    e = e.substring(a.raw.length), t.push(a);
                    continue;
                }
                if (a = this.tokenizer.link(e)) {
                    e = e.substring(a.raw.length), t.push(a);
                    continue;
                }
                if (a = this.tokenizer.reflink(e, this.tokens.links)) {
                    e = e.substring(a.raw.length);
                    let p = t.at(-1);
                    a.type === "text" && p?.type === "text" ? (p.raw += a.raw, p.text += a.text) : t.push(a);
                    continue;
                }
                if (a = this.tokenizer.emStrong(e, n, o)) {
                    e = e.substring(a.raw.length), t.push(a);
                    continue;
                }
                if (a = this.tokenizer.codespan(e)) {
                    e = e.substring(a.raw.length), t.push(a);
                    continue;
                }
                if (a = this.tokenizer.br(e)) {
                    e = e.substring(a.raw.length), t.push(a);
                    continue;
                }
                if (a = this.tokenizer.del(e, n, o)) {
                    e = e.substring(a.raw.length), t.push(a);
                    continue;
                }
                if (a = this.tokenizer.autolink(e)) {
                    e = e.substring(a.raw.length), t.push(a);
                    continue;
                }
                if (!this.state.inLink && (a = this.tokenizer.url(e))) {
                    e = e.substring(a.raw.length), t.push(a);
                    continue;
                }
                let c = e;
                if (this.options.extensions?.startInline) {
                    let p = 1 / 0, k = e.slice(1), h;
                    this.options.extensions.startInline.forEach((R => {
                        h = R.call({
                            lexer: this
                        }, k), typeof h == "number" && h >= 0 && (p = Math.min(p, h));
                    })), p < 1 / 0 && p >= 0 && (c = e.substring(0, p + 1));
                }
                if (a = this.tokenizer.inlineText(c)) {
                    e = e.substring(a.raw.length), a.raw.slice(-1) !== "_" && (o = a.raw.slice(-1)), 
                    i = true;
                    let p = t.at(-1);
                    p?.type === "text" ? (p.raw += a.raw, p.text += a.text) : t.push(a);
                    continue;
                }
                if (e) {
                    this.infiniteLoopError(e.charCodeAt(0));
                    break;
                }
            }
            return t;
        }
        infiniteLoopError(e) {
            let t = "Infinite loop on byte: " + e;
            if (this.options.silent) console.error(t); else throw new Error(t);
        }
    };
    var y = class {
        options;
        parser;
        constructor(e) {
            this.options = e || T;
        }
        space(e) {
            return "";
        }
        code({text: e, lang: t, escaped: n}) {
            let s = (t || "").match(m.notSpaceStart)?.[0], r = e.replace(m.endingNewline, "") + `\n`;
            return s ? '<pre><code class="language-' + O(s) + '">' + (n ? r : O(r, true)) + `</code></pre>\n` : "<pre><code>" + (n ? r : O(r, true)) + `</code></pre>\n`;
        }
        blockquote({tokens: e}) {
            return `<blockquote>\n${this.parser.parse(e)}</blockquote>\n`;
        }
        html({text: e}) {
            return e;
        }
        def(e) {
            return "";
        }
        heading({tokens: e, depth: t}) {
            return `<h${t}>${this.parser.parseInline(e)}</h${t}>\n`;
        }
        hr(e) {
            return `<hr>\n`;
        }
        list(e) {
            let t = e.ordered, n = e.start, s = "";
            for (let o = 0; o < e.items.length; o++) {
                let u = e.items[o];
                s += this.listitem(u);
            }
            let r = t ? "ol" : "ul", i = t && n !== 1 ? ' start="' + n + '"' : "";
            return "<" + r + i + `>\n` + s + "</" + r + `>\n`;
        }
        listitem(e) {
            return `<li>${this.parser.parse(e.tokens)}</li>\n`;
        }
        checkbox({checked: e}) {
            return "<input " + (e ? 'checked="" ' : "") + 'disabled="" type="checkbox"> ';
        }
        paragraph({tokens: e}) {
            return `<p>${this.parser.parseInline(e)}</p>\n`;
        }
        table(e) {
            let t = "", n = "";
            for (let r = 0; r < e.header.length; r++) n += this.tablecell(e.header[r]);
            t += this.tablerow({
                text: n
            });
            let s = "";
            for (let r = 0; r < e.rows.length; r++) {
                let i = e.rows[r];
                n = "";
                for (let o = 0; o < i.length; o++) n += this.tablecell(i[o]);
                s += this.tablerow({
                    text: n
                });
            }
            return s && (s = `<tbody>${s}</tbody>`), `<table>\n<thead>\n` + t + `</thead>\n` + s + `</table>\n`;
        }
        tablerow({text: e}) {
            return `<tr>\n${e}</tr>\n`;
        }
        tablecell(e) {
            let t = this.parser.parseInline(e.tokens), n = e.header ? "th" : "td";
            return (e.align ? `<${n} align="${e.align}">` : `<${n}>`) + t + `</${n}>\n`;
        }
        strong({tokens: e}) {
            return `<strong>${this.parser.parseInline(e)}</strong>`;
        }
        em({tokens: e}) {
            return `<em>${this.parser.parseInline(e)}</em>`;
        }
        codespan({text: e}) {
            return `<code>${O(e, true)}</code>`;
        }
        br(e) {
            return "<br>";
        }
        del({tokens: e}) {
            return `<del>${this.parser.parseInline(e)}</del>`;
        }
        link({href: e, title: t, tokens: n}) {
            let s = this.parser.parseInline(n), r = J(e);
            if (r === null) return s;
            e = r;
            let i = '<a href="' + e + '"';
            return t && (i += ' title="' + O(t) + '"'), i += ">" + s + "</a>", i;
        }
        image({href: e, title: t, text: n, tokens: s}) {
            s && (n = this.parser.parseInline(s, this.parser.textRenderer));
            let r = J(e);
            if (r === null) return O(n);
            e = r;
            let i = `<img src="${e}" alt="${O(n)}"`;
            return t && (i += ` title="${O(t)}"`), i += ">", i;
        }
        text(e) {
            return "tokens" in e && e.tokens ? this.parser.parseInline(e.tokens) : "escaped" in e && e.escaped ? e.text : O(e.text);
        }
    };
    var L = class {
        strong({text: e}) {
            return e;
        }
        em({text: e}) {
            return e;
        }
        codespan({text: e}) {
            return e;
        }
        del({text: e}) {
            return e;
        }
        html({text: e}) {
            return e;
        }
        text({text: e}) {
            return e;
        }
        link({text: e}) {
            return "" + e;
        }
        image({text: e}) {
            return "" + e;
        }
        br() {
            return "";
        }
        checkbox({raw: e}) {
            return e;
        }
    };
    var b = class l {
        options;
        renderer;
        textRenderer;
        constructor(e) {
            this.options = e || T, this.options.renderer = this.options.renderer || new y, this.renderer = this.options.renderer, 
            this.renderer.options = this.options, this.renderer.parser = this, this.textRenderer = new L;
        }
        static parse(e, t) {
            return new l(t).parse(e);
        }
        static parseInline(e, t) {
            return new l(t).parseInline(e);
        }
        parse(e) {
            this.renderer.parser = this;
            let t = "";
            for (let n = 0; n < e.length; n++) {
                let s = e[n];
                if (this.options.extensions?.renderers?.[s.type]) {
                    let i = s, o = this.options.extensions.renderers[i.type].call({
                        parser: this
                    }, i);
                    if (o !== false || ![ "space", "hr", "heading", "code", "table", "blockquote", "list", "html", "def", "paragraph", "text" ].includes(i.type)) {
                        t += o || "";
                        continue;
                    }
                }
                let r = s;
                switch (r.type) {
                  case "space":
                    {
                        t += this.renderer.space(r);
                        break;
                    }

                  case "hr":
                    {
                        t += this.renderer.hr(r);
                        break;
                    }

                  case "heading":
                    {
                        t += this.renderer.heading(r);
                        break;
                    }

                  case "code":
                    {
                        t += this.renderer.code(r);
                        break;
                    }

                  case "table":
                    {
                        t += this.renderer.table(r);
                        break;
                    }

                  case "blockquote":
                    {
                        t += this.renderer.blockquote(r);
                        break;
                    }

                  case "list":
                    {
                        t += this.renderer.list(r);
                        break;
                    }

                  case "checkbox":
                    {
                        t += this.renderer.checkbox(r);
                        break;
                    }

                  case "html":
                    {
                        t += this.renderer.html(r);
                        break;
                    }

                  case "def":
                    {
                        t += this.renderer.def(r);
                        break;
                    }

                  case "paragraph":
                    {
                        t += this.renderer.paragraph(r);
                        break;
                    }

                  case "text":
                    {
                        t += this.renderer.text(r);
                        break;
                    }

                  default:
                    {
                        let i = 'Token with "' + r.type + '" type was not found.';
                        if (this.options.silent) return console.error(i), "";
                        throw new Error(i);
                    }
                }
            }
            return t;
        }
        parseInline(e, t = this.renderer) {
            this.renderer.parser = this;
            let n = "";
            for (let s = 0; s < e.length; s++) {
                let r = e[s];
                if (this.options.extensions?.renderers?.[r.type]) {
                    let o = this.options.extensions.renderers[r.type].call({
                        parser: this
                    }, r);
                    if (o !== false || ![ "escape", "html", "link", "image", "strong", "em", "codespan", "br", "del", "text" ].includes(r.type)) {
                        n += o || "";
                        continue;
                    }
                }
                let i = r;
                switch (i.type) {
                  case "escape":
                    {
                        n += t.text(i);
                        break;
                    }

                  case "html":
                    {
                        n += t.html(i);
                        break;
                    }

                  case "link":
                    {
                        n += t.link(i);
                        break;
                    }

                  case "image":
                    {
                        n += t.image(i);
                        break;
                    }

                  case "checkbox":
                    {
                        n += t.checkbox(i);
                        break;
                    }

                  case "strong":
                    {
                        n += t.strong(i);
                        break;
                    }

                  case "em":
                    {
                        n += t.em(i);
                        break;
                    }

                  case "codespan":
                    {
                        n += t.codespan(i);
                        break;
                    }

                  case "br":
                    {
                        n += t.br(i);
                        break;
                    }

                  case "del":
                    {
                        n += t.del(i);
                        break;
                    }

                  case "text":
                    {
                        n += t.text(i);
                        break;
                    }

                  default:
                    {
                        let o = 'Token with "' + i.type + '" type was not found.';
                        if (this.options.silent) return console.error(o), "";
                        throw new Error(o);
                    }
                }
            }
            return n;
        }
    };
    var P = class {
        options;
        block;
        constructor(e) {
            this.options = e || T;
        }
        static passThroughHooks=new Set([ "preprocess", "postprocess", "processAllTokens", "emStrongMask" ]);
        static passThroughHooksRespectAsync=new Set([ "preprocess", "postprocess", "processAllTokens" ]);
        preprocess(e) {
            return e;
        }
        postprocess(e) {
            return e;
        }
        processAllTokens(e) {
            return e;
        }
        emStrongMask(e) {
            return e;
        }
        provideLexer(e = this.block) {
            return e ? x.lex : x.lexInline;
        }
        provideParser(e = this.block) {
            return e ? b.parse : b.parseInline;
        }
    };
    var D = class {
        defaults=z();
        options=this.setOptions;
        parse=this.parseMarkdown(true);
        parseInline=this.parseMarkdown(false);
        Parser=b;
        Renderer=y;
        TextRenderer=L;
        Lexer=x;
        Tokenizer=w;
        Hooks=P;
        constructor(...e) {
            this.use(...e);
        }
        walkTokens(e, t) {
            let n = [];
            for (let s of e) switch (n = n.concat(t.call(this, s)), s.type) {
              case "table":
                {
                    let r = s;
                    for (let i of r.header) n = n.concat(this.walkTokens(i.tokens, t));
                    for (let i of r.rows) for (let o of i) n = n.concat(this.walkTokens(o.tokens, t));
                    break;
                }

              case "list":
                {
                    let r = s;
                    n = n.concat(this.walkTokens(r.items, t));
                    break;
                }

              default:
                {
                    let r = s;
                    this.defaults.extensions?.childTokens?.[r.type] ? this.defaults.extensions.childTokens[r.type].forEach((i => {
                        let o = r[i].flat(1 / 0);
                        n = n.concat(this.walkTokens(o, t));
                    })) : r.tokens && (n = n.concat(this.walkTokens(r.tokens, t)));
                }
            }
            return n;
        }
        use(...e) {
            let t = this.defaults.extensions || {
                renderers: {},
                childTokens: {}
            };
            return e.forEach((n => {
                let s = {
                    ...n
                };
                if (s.async = this.defaults.async || s.async || false, n.extensions && (n.extensions.forEach((r => {
                    if (!r.name) throw new Error("extension name required");
                    if ("renderer" in r) {
                        let i = t.renderers[r.name];
                        i ? t.renderers[r.name] = function(...o) {
                            let u = r.renderer.apply(this, o);
                            return u === false && (u = i.apply(this, o)), u;
                        } : t.renderers[r.name] = r.renderer;
                    }
                    if ("tokenizer" in r) {
                        if (!r.level || r.level !== "block" && r.level !== "inline") throw new Error("extension level must be 'block' or 'inline'");
                        let i = t[r.level];
                        i ? i.unshift(r.tokenizer) : t[r.level] = [ r.tokenizer ], r.start && (r.level === "block" ? t.startBlock ? t.startBlock.push(r.start) : t.startBlock = [ r.start ] : r.level === "inline" && (t.startInline ? t.startInline.push(r.start) : t.startInline = [ r.start ]));
                    }
                    "childTokens" in r && r.childTokens && (t.childTokens[r.name] = r.childTokens);
                })), s.extensions = t), n.renderer) {
                    let r = this.defaults.renderer || new y(this.defaults);
                    for (let i in n.renderer) {
                        if (!(i in r)) throw new Error(`renderer '${i}' does not exist`);
                        if ([ "options", "parser" ].includes(i)) continue;
                        let o = i, u = n.renderer[o], a = r[o];
                        r[o] = (...c) => {
                            let p = u.apply(r, c);
                            return p === false && (p = a.apply(r, c)), p || "";
                        };
                    }
                    s.renderer = r;
                }
                if (n.tokenizer) {
                    let r = this.defaults.tokenizer || new w(this.defaults);
                    for (let i in n.tokenizer) {
                        if (!(i in r)) throw new Error(`tokenizer '${i}' does not exist`);
                        if ([ "options", "rules", "lexer" ].includes(i)) continue;
                        let o = i, u = n.tokenizer[o], a = r[o];
                        r[o] = (...c) => {
                            let p = u.apply(r, c);
                            return p === false && (p = a.apply(r, c)), p;
                        };
                    }
                    s.tokenizer = r;
                }
                if (n.hooks) {
                    let r = this.defaults.hooks || new P;
                    for (let i in n.hooks) {
                        if (!(i in r)) throw new Error(`hook '${i}' does not exist`);
                        if ([ "options", "block" ].includes(i)) continue;
                        let o = i, u = n.hooks[o], a = r[o];
                        P.passThroughHooks.has(i) ? r[o] = c => {
                            if (this.defaults.async && P.passThroughHooksRespectAsync.has(i)) return (async () => {
                                let k = await u.call(r, c);
                                return a.call(r, k);
                            })();
                            let p = u.call(r, c);
                            return a.call(r, p);
                        } : r[o] = (...c) => {
                            if (this.defaults.async) return (async () => {
                                let k = await u.apply(r, c);
                                return k === false && (k = await a.apply(r, c)), k;
                            })();
                            let p = u.apply(r, c);
                            return p === false && (p = a.apply(r, c)), p;
                        };
                    }
                    s.hooks = r;
                }
                if (n.walkTokens) {
                    let r = this.defaults.walkTokens, i = n.walkTokens;
                    s.walkTokens = function(o) {
                        let u = [];
                        return u.push(i.call(this, o)), r && (u = u.concat(r.call(this, o))), u;
                    };
                }
                this.defaults = {
                    ...this.defaults,
                    ...s
                };
            })), this;
        }
        setOptions(e) {
            return this.defaults = {
                ...this.defaults,
                ...e
            }, this;
        }
        lexer(e, t) {
            return x.lex(e, t ?? this.defaults);
        }
        parser(e, t) {
            return b.parse(e, t ?? this.defaults);
        }
        parseMarkdown(e) {
            return (n, s) => {
                let r = {
                    ...s
                }, i = {
                    ...this.defaults,
                    ...r
                }, o = this.onError(!!i.silent, !!i.async);
                if (this.defaults.async === true && r.async === false) return o(new Error("marked(): The async option was set to true by an extension. Remove async: false from the parse options object to return a Promise."));
                if (typeof n > "u" || n === null) return o(new Error("marked(): input parameter is undefined or null"));
                if (typeof n != "string") return o(new Error("marked(): input parameter is of type " + Object.prototype.toString.call(n) + ", string expected"));
                if (i.hooks && (i.hooks.options = i, i.hooks.block = e), i.async) return (async () => {
                    let u = i.hooks ? await i.hooks.preprocess(n) : n, c = await (i.hooks ? await i.hooks.provideLexer(e) : e ? x.lex : x.lexInline)(u, i), p = i.hooks ? await i.hooks.processAllTokens(c) : c;
                    i.walkTokens && await Promise.all(this.walkTokens(p, i.walkTokens));
                    let h = await (i.hooks ? await i.hooks.provideParser(e) : e ? b.parse : b.parseInline)(p, i);
                    return i.hooks ? await i.hooks.postprocess(h) : h;
                })().catch(o);
                try {
                    i.hooks && (n = i.hooks.preprocess(n));
                    let a = (i.hooks ? i.hooks.provideLexer(e) : e ? x.lex : x.lexInline)(n, i);
                    i.hooks && (a = i.hooks.processAllTokens(a)), i.walkTokens && this.walkTokens(a, i.walkTokens);
                    let p = (i.hooks ? i.hooks.provideParser(e) : e ? b.parse : b.parseInline)(a, i);
                    return i.hooks && (p = i.hooks.postprocess(p)), p;
                } catch (u) {
                    return o(u);
                }
            };
        }
        onError(e, t) {
            return n => {
                if (n.message += `\nPlease report this to https://github.com/markedjs/marked.`, 
                e) {
                    let s = "<p>An error occurred:</p><pre>" + O(n.message + "", true) + "</pre>";
                    return t ? Promise.resolve(s) : s;
                }
                if (t) return Promise.reject(n);
                throw n;
            };
        }
    };
    var M = new D;
    function g(l, e) {
        return M.parse(l, e);
    }
    g.options = g.setOptions = function(l) {
        return M.setOptions(l), g.defaults = M.defaults, G(g.defaults), g;
    };
    g.getDefaults = z;
    g.defaults = T;
    g.use = function(...l) {
        return M.use(...l), g.defaults = M.defaults, G(g.defaults), g;
    };
    g.walkTokens = function(l, e) {
        return M.walkTokens(l, e);
    };
    g.parseInline = M.parseInline;
    g.Parser = b;
    g.parser = b.parse;
    g.Renderer = y;
    g.TextRenderer = L;
    g.Lexer = x;
    g.lexer = x.lex;
    g.Tokenizer = w;
    g.Hooks = P;
    g.parse = g;
    g.options;
    g.setOptions;
    g.use;
    g.walkTokens;
    g.parseInline;
    b.parse;
    x.lex;
    const reEscapedHtml = /&(?:amp|lt|gt|quot|#(0+)?39);/g;
    const reHasEscapedHtml = RegExp(reEscapedHtml.source);
    const htmlUnescapes = {
        "&amp;": "&",
        "&lt;": "<",
        "&gt;": ">",
        "&quot;": '"',
        "&#39;": "'"
    };
    function unescape(string) {
        return string && reHasEscapedHtml.test(string) ? string.replace(reEscapedHtml, (entity => htmlUnescapes[entity] || "'")) : string || "";
    }
    const reUnescapedHtml = /[&<>"']/g;
    const reHasUnescapedHtml = RegExp(reUnescapedHtml.source);
    const htmlEscapes = {
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    };
    function escape(string) {
        return string && reHasUnescapedHtml.test(string) ? string.replace(reUnescapedHtml, (chr => htmlEscapes[chr])) : string || "";
    }
    function helpersCleanup(string) {
        return string && string.replace("!>", "").replace("?>", "");
    }
    const markdownToTxtRenderer = {
        space() {
            return "";
        },
        code({text: text}) {
            const code = text.replace(/\n$/, "");
            return escape(code);
        },
        blockquote({tokens: tokens}) {
            return this.parser?.parse(tokens) || "";
        },
        html() {
            return "";
        },
        heading({tokens: tokens}) {
            return this.parser?.parseInline(tokens) || "";
        },
        hr() {
            return "";
        },
        list(token) {
            let body = "";
            for (let j = 0; j < token.items.length; j++) {
                const item = token.items[j];
                body += this.listitem?.(item);
            }
            return body;
        },
        listitem(item) {
            let itemBody = "";
            if (item.task) {
                const checkbox = this.checkbox?.({
                    checked: !!item.checked
                });
                if (item.loose) {
                    if (item.tokens.length > 0 && item.tokens[0].type === "paragraph") {
                        item.tokens[0].text = checkbox + " " + item.tokens[0].text;
                        if (item.tokens[0].tokens && item.tokens[0].tokens.length > 0 && item.tokens[0].tokens[0].type === "text") {
                            item.tokens[0].tokens[0].text = checkbox + " " + item.tokens[0].tokens[0].text;
                        }
                    } else {
                        item.tokens.unshift({
                            type: "text",
                            raw: checkbox + " ",
                            text: checkbox + " "
                        });
                    }
                } else {
                    itemBody += checkbox + " ";
                }
            }
            itemBody += this.parser?.parse(item.tokens, !!item.loose);
            return `${itemBody || ""}`;
        },
        checkbox(options) {
            return "";
        },
        paragraph({tokens: tokens}) {
            return this.parser?.parseInline(tokens) || "";
        },
        table(token) {
            let header = "";
            let cell = "";
            for (let j = 0; j < token.header.length; j++) {
                cell += this.tablecell?.(token.header[j]);
            }
            header += this.tablerow?.({
                text: cell
            });
            let body = "";
            for (let j = 0; j < token.rows.length; j++) {
                const row = token.rows[j];
                cell = "";
                for (let k = 0; k < row.length; k++) {
                    cell += this.tablecell?.(row[k]);
                }
                body += this.tablerow?.({
                    text: cell
                });
            }
            return header + " " + body;
        },
        tablerow({text: text}) {
            return text;
        },
        tablecell(token) {
            return this.parser?.parseInline(token.tokens) || "";
        },
        strong({text: text}) {
            return text;
        },
        em({tokens: tokens}) {
            return this.parser?.parseInline(tokens) || "";
        },
        codespan({text: text}) {
            return text;
        },
        br() {
            return " ";
        },
        del({tokens: tokens}) {
            return this.parser?.parseInline(tokens);
        },
        link({tokens: tokens, href: href, title: title}) {
            return `${this.parser?.parseInline(tokens) || ""} ${href || ""} ${title || ""}`;
        },
        image({title: title, text: text, href: href}) {
            return `${text || ""} ${href || ""} ${title || ""}`;
        },
        text(token) {
            return token.tokens ? this.parser?.parseInline(token.tokens) || "" : token.text || "";
        }
    };
    const _marked = g.setOptions({
        renderer: markdownToTxtRenderer
    });
    function markdownToTxt(markdown) {
        const unmarked = _marked.parse(markdown);
        const unescaped = unescape(unmarked);
        const helpersCleaned = helpersCleanup(unescaped);
        return helpersCleaned.trim();
    }
    var commonjsGlobal = typeof globalThis !== "undefined" ? globalThis : typeof window !== "undefined" ? window : typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : {};
    function getDefaultExportFromCjs(x) {
        return x && x.__esModule && Object.prototype.hasOwnProperty.call(x, "default") ? x["default"] : x;
    }
    var dexie_min$1 = {
        exports: {}
    };
    var dexie_min = dexie_min$1.exports;
    var hasRequiredDexie_min;
    function requireDexie_min() {
        if (hasRequiredDexie_min) return dexie_min$1.exports;
        hasRequiredDexie_min = 1;
        (function(module, exports$1) {
            (function(e, t) {
                module.exports = t();
            })(dexie_min, (function() {
                var s = function(e, t) {
                    return (s = Object.setPrototypeOf || {
                        __proto__: []
                    } instanceof Array && function(e, t) {
                        e.__proto__ = t;
                    } || function(e, t) {
                        for (var n in t) Object.prototype.hasOwnProperty.call(t, n) && (e[n] = t[n]);
                    })(e, t);
                };
                var _ = function() {
                    return (_ = Object.assign || function(e) {
                        for (var t, n = 1, r = arguments.length; n < r; n++) for (var i in t = arguments[n]) Object.prototype.hasOwnProperty.call(t, i) && (e[i] = t[i]);
                        return e;
                    }).apply(this, arguments);
                };
                function i(e, t, n) {
                    for (var r, i = 0, o = t.length; i < o; i++) !r && i in t || ((r = r || Array.prototype.slice.call(t, 0, i))[i] = t[i]);
                    return e.concat(r || Array.prototype.slice.call(t));
                }
                var f = "undefined" != typeof globalThis ? globalThis : "undefined" != typeof self ? self : "undefined" != typeof window ? window : commonjsGlobal, x = Object.keys, k = Array.isArray;
                function a(t, n) {
                    return "object" != typeof n || x(n).forEach((function(e) {
                        t[e] = n[e];
                    })), t;
                }
                "undefined" == typeof Promise || f.Promise || (f.Promise = Promise);
                var c = Object.getPrototypeOf, n = {}.hasOwnProperty;
                function m(e, t) {
                    return n.call(e, t);
                }
                function r(t, n) {
                    "function" == typeof n && (n = n(c(t))), ("undefined" == typeof Reflect ? x : Reflect.ownKeys)(n).forEach((function(e) {
                        l(t, e, n[e]);
                    }));
                }
                var u = Object.defineProperty;
                function l(e, t, n, r) {
                    u(e, t, a(n && m(n, "get") && "function" == typeof n.get ? {
                        get: n.get,
                        set: n.set,
                        configurable: true
                    } : {
                        value: n,
                        configurable: true,
                        writable: true
                    }, r));
                }
                function o(t) {
                    return {
                        from: function(e) {
                            return t.prototype = Object.create(e.prototype), l(t.prototype, "constructor", t), 
                            {
                                extend: r.bind(null, t.prototype)
                            };
                        }
                    };
                }
                var h = Object.getOwnPropertyDescriptor;
                var d = [].slice;
                function b(e, t, n) {
                    return d.call(e, t, n);
                }
                function p(e, t) {
                    return t(e);
                }
                function y(e) {
                    if (!e) throw new Error("Assertion Failed");
                }
                function v(e) {
                    f.setImmediate ? setImmediate(e) : setTimeout(e, 0);
                }
                function O(e, t) {
                    if ("string" == typeof t && m(e, t)) return e[t];
                    if (!t) return e;
                    if ("string" != typeof t) {
                        for (var n = [], r = 0, i = t.length; r < i; ++r) {
                            var o = O(e, t[r]);
                            n.push(o);
                        }
                        return n;
                    }
                    var a = t.indexOf(".");
                    if (-1 !== a) {
                        var u = e[t.substr(0, a)];
                        return null == u ? void 0 : O(u, t.substr(a + 1));
                    }
                }
                function P(e, t, n) {
                    if (e && void 0 !== t && !("isFrozen" in Object && Object.isFrozen(e))) if ("string" != typeof t && "length" in t) {
                        y("string" != typeof n && "length" in n);
                        for (var r = 0, i = t.length; r < i; ++r) P(e, t[r], n[r]);
                    } else {
                        var o, a, u = t.indexOf(".");
                        -1 !== u ? (o = t.substr(0, u), "" === (a = t.substr(u + 1)) ? void 0 === n ? k(e) && !isNaN(parseInt(o)) ? e.splice(o, 1) : delete e[o] : e[o] = n : P(u = !(u = e[o]) || !m(e, o) ? e[o] = {} : u, a, n)) : void 0 === n ? k(e) && !isNaN(parseInt(t)) ? e.splice(t, 1) : delete e[t] : e[t] = n;
                    }
                }
                function g(e) {
                    var t, n = {};
                    for (t in e) m(e, t) && (n[t] = e[t]);
                    return n;
                }
                var t = [].concat;
                function w(e) {
                    return t.apply([], e);
                }
                var e = "BigUint64Array,BigInt64Array,Array,Boolean,String,Date,RegExp,Blob,File,FileList,FileSystemFileHandle,FileSystemDirectoryHandle,ArrayBuffer,DataView,Uint8ClampedArray,ImageBitmap,ImageData,Map,Set,CryptoKey".split(",").concat(w([ 8, 16, 32, 64 ].map((function(t) {
                    return [ "Int", "Uint", "Float" ].map((function(e) {
                        return e + t + "Array";
                    }));
                })))).filter((function(e) {
                    return f[e];
                })), K = new Set(e.map((function(e) {
                    return f[e];
                })));
                var E = null;
                function S(e) {
                    E = new WeakMap;
                    e = function e(t) {
                        if (!t || "object" != typeof t) return t;
                        var n = E.get(t);
                        if (n) return n;
                        if (k(t)) {
                            n = [], E.set(t, n);
                            for (var r = 0, i = t.length; r < i; ++r) n.push(e(t[r]));
                        } else if (K.has(t.constructor)) n = t; else {
                            var o, a = c(t);
                            for (o in n = a === Object.prototype ? {} : Object.create(a), E.set(t, n), t) m(t, o) && (n[o] = e(t[o]));
                        }
                        return n;
                    }(e);
                    return E = null, e;
                }
                var j = {}.toString;
                function A(e) {
                    return j.call(e).slice(8, -1);
                }
                var C = "undefined" != typeof Symbol ? Symbol.iterator : "@@iterator", T = "symbol" == typeof C ? function(e) {
                    var t;
                    return null != e && (t = e[C]) && t.apply(e);
                } : function() {
                    return null;
                };
                function q(e, t) {
                    t = e.indexOf(t);
                    return 0 <= t && e.splice(t, 1), 0 <= t;
                }
                var D = {};
                function I(e) {
                    var t, n, r, i;
                    if (1 === arguments.length) {
                        if (k(e)) return e.slice();
                        if (this === D && "string" == typeof e) return [ e ];
                        if (i = T(e)) {
                            for (n = []; !(r = i.next()).done; ) n.push(r.value);
                            return n;
                        }
                        if (null == e) return [ e ];
                        if ("number" != typeof (t = e.length)) return [ e ];
                        for (n = new Array(t); t--; ) n[t] = e[t];
                        return n;
                    }
                    for (t = arguments.length, n = new Array(t); t--; ) n[t] = arguments[t];
                    return n;
                }
                var B = "undefined" != typeof Symbol ? function(e) {
                    return "AsyncFunction" === e[Symbol.toStringTag];
                } : function() {
                    return false;
                }, R = [ "Unknown", "Constraint", "Data", "TransactionInactive", "ReadOnly", "Version", "NotFound", "InvalidState", "InvalidAccess", "Abort", "Timeout", "QuotaExceeded", "Syntax", "DataClone" ], F = [ "Modify", "Bulk", "OpenFailed", "VersionChange", "Schema", "Upgrade", "InvalidTable", "MissingAPI", "NoSuchDatabase", "InvalidArgument", "SubTransaction", "Unsupported", "Internal", "DatabaseClosed", "PrematureCommit", "ForeignAwait" ].concat(R), M = {
                    VersionChanged: "Database version changed by other database connection",
                    DatabaseClosed: "Database has been closed",
                    Abort: "Transaction aborted",
                    TransactionInactive: "Transaction has already completed or failed",
                    MissingAPI: "IndexedDB API missing. Please visit https://tinyurl.com/y2uuvskb"
                };
                function N(e, t) {
                    this.name = e, this.message = t;
                }
                function L(e, t) {
                    return e + ". Errors: " + Object.keys(t).map((function(e) {
                        return t[e].toString();
                    })).filter((function(e, t, n) {
                        return n.indexOf(e) === t;
                    })).join("\n");
                }
                function U(e, t, n, r) {
                    this.failures = t, this.failedKeys = r, this.successCount = n, this.message = L(e, t);
                }
                function V(e, t) {
                    this.name = "BulkError", this.failures = Object.keys(t).map((function(e) {
                        return t[e];
                    })), this.failuresByPos = t, this.message = L(e, this.failures);
                }
                o(N).from(Error).extend({
                    toString: function() {
                        return this.name + ": " + this.message;
                    }
                }), o(U).from(N), o(V).from(N);
                var z = F.reduce((function(e, t) {
                    return e[t] = t + "Error", e;
                }), {}), W = N, Y = F.reduce((function(e, n) {
                    var r = n + "Error";
                    function t(e, t) {
                        this.name = r, e ? "string" == typeof e ? (this.message = "".concat(e).concat(t ? "\n " + t : ""), 
                        this.inner = t || null) : "object" == typeof e && (this.message = "".concat(e.name, " ").concat(e.message), 
                        this.inner = e) : (this.message = M[n] || r, this.inner = null);
                    }
                    return o(t).from(W), e[n] = t, e;
                }), {});
                Y.Syntax = SyntaxError, Y.Type = TypeError, Y.Range = RangeError;
                var $ = R.reduce((function(e, t) {
                    return e[t + "Error"] = Y[t], e;
                }), {});
                var Q = F.reduce((function(e, t) {
                    return -1 === [ "Syntax", "Type", "Range" ].indexOf(t) && (e[t + "Error"] = Y[t]), 
                    e;
                }), {});
                function G() {}
                function X(e) {
                    return e;
                }
                function H(t, n) {
                    return null == t || t === X ? n : function(e) {
                        return n(t(e));
                    };
                }
                function J(e, t) {
                    return function() {
                        e.apply(this, arguments), t.apply(this, arguments);
                    };
                }
                function Z(i, o) {
                    return i === G ? o : function() {
                        var e = i.apply(this, arguments);
                        void 0 !== e && (arguments[0] = e);
                        var t = this.onsuccess, n = this.onerror;
                        this.onsuccess = null, this.onerror = null;
                        var r = o.apply(this, arguments);
                        return t && (this.onsuccess = this.onsuccess ? J(t, this.onsuccess) : t), n && (this.onerror = this.onerror ? J(n, this.onerror) : n), 
                        void 0 !== r ? r : e;
                    };
                }
                function ee(n, r) {
                    return n === G ? r : function() {
                        n.apply(this, arguments);
                        var e = this.onsuccess, t = this.onerror;
                        this.onsuccess = this.onerror = null, r.apply(this, arguments), e && (this.onsuccess = this.onsuccess ? J(e, this.onsuccess) : e), 
                        t && (this.onerror = this.onerror ? J(t, this.onerror) : t);
                    };
                }
                function te(i, o) {
                    return i === G ? o : function(e) {
                        var t = i.apply(this, arguments);
                        a(e, t);
                        var n = this.onsuccess, r = this.onerror;
                        this.onsuccess = null, this.onerror = null;
                        e = o.apply(this, arguments);
                        return n && (this.onsuccess = this.onsuccess ? J(n, this.onsuccess) : n), r && (this.onerror = this.onerror ? J(r, this.onerror) : r), 
                        void 0 === t ? void 0 === e ? void 0 : e : a(t, e);
                    };
                }
                function ne(e, t) {
                    return e === G ? t : function() {
                        return false !== t.apply(this, arguments) && e.apply(this, arguments);
                    };
                }
                function re(i, o) {
                    return i === G ? o : function() {
                        var e = i.apply(this, arguments);
                        if (e && "function" == typeof e.then) {
                            for (var t = this, n = arguments.length, r = new Array(n); n--; ) r[n] = arguments[n];
                            return e.then((function() {
                                return o.apply(t, r);
                            }));
                        }
                        return o.apply(this, arguments);
                    };
                }
                Q.ModifyError = U, Q.DexieError = N, Q.BulkError = V;
                var ie = "undefined" != typeof location && /^(http|https):\/\/(localhost|127\.0\.0\.1)/.test(location.href);
                function oe(e) {
                    ie = e;
                }
                var ae = {}, ue = 100, e = "undefined" == typeof Promise ? [] : function() {
                    var e = Promise.resolve();
                    if ("undefined" == typeof crypto || !crypto.subtle) return [ e, c(e), e ];
                    var t = crypto.subtle.digest("SHA-512", new Uint8Array([ 0 ]));
                    return [ t, c(t), e ];
                }(), R = e[0], F = e[1], e = e[2], F = F && F.then, se = R && R.constructor, ce = !!e;
                var le = function(e, t) {
                    be.push([ e, t ]), he && (queueMicrotask(Se), he = false);
                }, fe = true, he = true, de = [], pe = [], ye = X, ve = {
                    id: "global",
                    global: true,
                    ref: 0,
                    unhandleds: [],
                    onunhandled: G,
                    pgp: false,
                    env: {},
                    finalize: G
                }, me = ve, be = [], ge = 0, we = [];
                function _e(e) {
                    if ("object" != typeof this) throw new TypeError("Promises must be constructed via new");
                    this._listeners = [], this._lib = false;
                    var t = this._PSD = me;
                    if ("function" != typeof e) {
                        if (e !== ae) throw new TypeError("Not a function");
                        return this._state = arguments[1], this._value = arguments[2], void (false === this._state && Oe(this, this._value));
                    }
                    this._state = null, this._value = null, ++t.ref, function t(r, e) {
                        try {
                            e((function(n) {
                                if (null === r._state) {
                                    if (n === r) throw new TypeError("A promise cannot be resolved with itself.");
                                    var e = r._lib && je();
                                    n && "function" == typeof n.then ? t(r, (function(e, t) {
                                        n instanceof _e ? n._then(e, t) : n.then(e, t);
                                    })) : (r._state = !0, r._value = n, Pe(r)), e && Ae();
                                }
                            }), Oe.bind(null, r));
                        } catch (e) {
                            Oe(r, e);
                        }
                    }(this, e);
                }
                var xe = {
                    get: function() {
                        var u = me, t = Fe;
                        function e(n, r) {
                            var i = this, o = !u.global && (u !== me || t !== Fe), a = o && !Ue(), e = new _e((function(e, t) {
                                Ke(i, new ke(Qe(n, u, o, a), Qe(r, u, o, a), e, t, u));
                            }));
                            return this._consoleTask && (e._consoleTask = this._consoleTask), e;
                        }
                        return e.prototype = ae, e;
                    },
                    set: function(e) {
                        l(this, "then", e && e.prototype === ae ? xe : {
                            get: function() {
                                return e;
                            },
                            set: xe.set
                        });
                    }
                };
                function ke(e, t, n, r, i) {
                    this.onFulfilled = "function" == typeof e ? e : null, this.onRejected = "function" == typeof t ? t : null, 
                    this.resolve = n, this.reject = r, this.psd = i;
                }
                function Oe(e, t) {
                    var n, r;
                    pe.push(t), null === e._state && (n = e._lib && je(), t = ye(t), e._state = false, 
                    e._value = t, r = e, de.some((function(e) {
                        return e._value === r._value;
                    })) || de.push(r), Pe(e), n && Ae());
                }
                function Pe(e) {
                    var t = e._listeners;
                    e._listeners = [];
                    for (var n = 0, r = t.length; n < r; ++n) Ke(e, t[n]);
                    var i = e._PSD;
                    --i.ref || i.finalize(), 0 === ge && (++ge, le((function() {
                        0 == --ge && Ce();
                    }), []));
                }
                function Ke(e, t) {
                    if (null !== e._state) {
                        var n = e._state ? t.onFulfilled : t.onRejected;
                        if (null === n) return (e._state ? t.resolve : t.reject)(e._value);
                        ++t.psd.ref, ++ge, le(Ee, [ n, e, t ]);
                    } else e._listeners.push(t);
                }
                function Ee(e, t, n) {
                    try {
                        var r, i = t._value;
                        !t._state && pe.length && (pe = []), r = ie && t._consoleTask ? t._consoleTask.run((function() {
                            return e(i);
                        })) : e(i), t._state || -1 !== pe.indexOf(i) || function(e) {
                            var t = de.length;
                            for (;t; ) if (de[--t]._value === e._value) return de.splice(t, 1);
                        }(t), n.resolve(r);
                    } catch (e) {
                        n.reject(e);
                    } finally {
                        0 == --ge && Ce(), --n.psd.ref || n.psd.finalize();
                    }
                }
                function Se() {
                    $e(ve, (function() {
                        je() && Ae();
                    }));
                }
                function je() {
                    var e = fe;
                    return he = fe = false, e;
                }
                function Ae() {
                    var e, t, n;
                    do {
                        for (;0 < be.length; ) for (e = be, be = [], n = e.length, t = 0; t < n; ++t) {
                            var r = e[t];
                            r[0].apply(null, r[1]);
                        }
                    } while (0 < be.length);
                    he = fe = true;
                }
                function Ce() {
                    var e = de;
                    de = [], e.forEach((function(e) {
                        e._PSD.onunhandled.call(null, e._value, e);
                    }));
                    for (var t = we.slice(0), n = t.length; n; ) t[--n]();
                }
                function Te(e) {
                    return new _e(ae, false, e);
                }
                function qe(n, r) {
                    var i = me;
                    return function() {
                        var e = je(), t = me;
                        try {
                            return We(i, !0), n.apply(this, arguments);
                        } catch (e) {
                            r && r(e);
                        } finally {
                            We(t, false), e && Ae();
                        }
                    };
                }
                r(_e.prototype, {
                    then: xe,
                    _then: function(e, t) {
                        Ke(this, new ke(null, null, e, t, me));
                    },
                    catch: function(e) {
                        if (1 === arguments.length) return this.then(null, e);
                        var t = e, n = arguments[1];
                        return "function" == typeof t ? this.then(null, (function(e) {
                            return (e instanceof t ? n : Te)(e);
                        })) : this.then(null, (function(e) {
                            return (e && e.name === t ? n : Te)(e);
                        }));
                    },
                    finally: function(t) {
                        return this.then((function(e) {
                            return _e.resolve(t()).then((function() {
                                return e;
                            }));
                        }), (function(e) {
                            return _e.resolve(t()).then((function() {
                                return Te(e);
                            }));
                        }));
                    },
                    timeout: function(r, i) {
                        var o = this;
                        return r < 1 / 0 ? new _e((function(e, t) {
                            var n = setTimeout((function() {
                                return t(new Y.Timeout(i));
                            }), r);
                            o.then(e, t).finally(clearTimeout.bind(null, n));
                        })) : this;
                    }
                }), "undefined" != typeof Symbol && Symbol.toStringTag && l(_e.prototype, Symbol.toStringTag, "Dexie.Promise"), 
                ve.env = Ye(), r(_e, {
                    all: function() {
                        var o = I.apply(null, arguments).map(Ve);
                        return new _e((function(n, r) {
                            0 === o.length && n([]);
                            var i = o.length;
                            o.forEach((function(e, t) {
                                return _e.resolve(e).then((function(e) {
                                    o[t] = e, --i || n(o);
                                }), r);
                            }));
                        }));
                    },
                    resolve: function(n) {
                        return n instanceof _e ? n : n && "function" == typeof n.then ? new _e((function(e, t) {
                            n.then(e, t);
                        })) : new _e(ae, true, n);
                    },
                    reject: Te,
                    race: function() {
                        var e = I.apply(null, arguments).map(Ve);
                        return new _e((function(t, n) {
                            e.map((function(e) {
                                return _e.resolve(e).then(t, n);
                            }));
                        }));
                    },
                    PSD: {
                        get: function() {
                            return me;
                        },
                        set: function(e) {
                            return me = e;
                        }
                    },
                    totalEchoes: {
                        get: function() {
                            return Fe;
                        }
                    },
                    newPSD: Ne,
                    usePSD: $e,
                    scheduler: {
                        get: function() {
                            return le;
                        },
                        set: function(e) {
                            le = e;
                        }
                    },
                    rejectionMapper: {
                        get: function() {
                            return ye;
                        },
                        set: function(e) {
                            ye = e;
                        }
                    },
                    follow: function(i, n) {
                        return new _e((function(e, t) {
                            return Ne((function(n, r) {
                                var e = me;
                                e.unhandleds = [], e.onunhandled = r, e.finalize = J((function() {
                                    var t, e = this;
                                    t = function() {
                                        0 === e.unhandleds.length ? n() : r(e.unhandleds[0]);
                                    }, we.push((function e() {
                                        t(), we.splice(we.indexOf(e), 1);
                                    })), ++ge, le((function() {
                                        0 == --ge && Ce();
                                    }), []);
                                }), e.finalize), i();
                            }), n, e, t);
                        }));
                    }
                }), se && (se.allSettled && l(_e, "allSettled", (function() {
                    var e = I.apply(null, arguments).map(Ve);
                    return new _e((function(n) {
                        0 === e.length && n([]);
                        var r = e.length, i = new Array(r);
                        e.forEach((function(e, t) {
                            return _e.resolve(e).then((function(e) {
                                return i[t] = {
                                    status: "fulfilled",
                                    value: e
                                };
                            }), (function(e) {
                                return i[t] = {
                                    status: "rejected",
                                    reason: e
                                };
                            })).then((function() {
                                return --r || n(i);
                            }));
                        }));
                    }));
                })), se.any && "undefined" != typeof AggregateError && l(_e, "any", (function() {
                    var e = I.apply(null, arguments).map(Ve);
                    return new _e((function(n, r) {
                        0 === e.length && r(new AggregateError([]));
                        var i = e.length, o = new Array(i);
                        e.forEach((function(e, t) {
                            return _e.resolve(e).then((function(e) {
                                return n(e);
                            }), (function(e) {
                                o[t] = e, --i || r(new AggregateError(o));
                            }));
                        }));
                    }));
                })), se.withResolvers && (_e.withResolvers = se.withResolvers));
                var De = {
                    awaits: 0,
                    echoes: 0,
                    id: 0
                }, Ie = 0, Be = [], Re = 0, Fe = 0, Me = 0;
                function Ne(e, t, n, r) {
                    var i = me, o = Object.create(i);
                    o.parent = i, o.ref = 0, o.global = false, o.id = ++Me, ve.env, o.env = ce ? {
                        Promise: _e,
                        PromiseProp: {
                            value: _e,
                            configurable: true,
                            writable: true
                        },
                        all: _e.all,
                        race: _e.race,
                        allSettled: _e.allSettled,
                        any: _e.any,
                        resolve: _e.resolve,
                        reject: _e.reject
                    } : {}, t && a(o, t), ++i.ref, o.finalize = function() {
                        --this.parent.ref || this.parent.finalize();
                    };
                    r = $e(o, e, n, r);
                    return 0 === o.ref && o.finalize(), r;
                }
                function Le() {
                    return De.id || (De.id = ++Ie), ++De.awaits, De.echoes += ue, De.id;
                }
                function Ue() {
                    return !!De.awaits && (0 == --De.awaits && (De.id = 0), De.echoes = De.awaits * ue, 
                    true);
                }
                function Ve(e) {
                    return De.echoes && e && e.constructor === se ? (Le(), e.then((function(e) {
                        return Ue(), e;
                    }), (function(e) {
                        return Ue(), Xe(e);
                    }))) : e;
                }
                function ze() {
                    var e = Be[Be.length - 1];
                    Be.pop(), We(e, false);
                }
                function We(e, t) {
                    var n, r = me;
                    (t ? !De.echoes || Re++ && e === me : !Re || --Re && e === me) || queueMicrotask(t ? function(e) {
                        ++Fe, De.echoes && 0 != --De.echoes || (De.echoes = De.awaits = De.id = 0), Be.push(me), 
                        We(e, true);
                    }.bind(null, e) : ze), e !== me && (me = e, r === ve && (ve.env = Ye()), ce && (n = ve.env.Promise, 
                    t = e.env, (r.global || e.global) && (Object.defineProperty(f, "Promise", t.PromiseProp), 
                    n.all = t.all, n.race = t.race, n.resolve = t.resolve, n.reject = t.reject, t.allSettled && (n.allSettled = t.allSettled), 
                    t.any && (n.any = t.any))));
                }
                function Ye() {
                    var e = f.Promise;
                    return ce ? {
                        Promise: e,
                        PromiseProp: Object.getOwnPropertyDescriptor(f, "Promise"),
                        all: e.all,
                        race: e.race,
                        allSettled: e.allSettled,
                        any: e.any,
                        resolve: e.resolve,
                        reject: e.reject
                    } : {};
                }
                function $e(e, t, n, r, i) {
                    var o = me;
                    try {
                        return We(e, !0), t(n, r, i);
                    } finally {
                        We(o, false);
                    }
                }
                function Qe(t, n, r, i) {
                    return "function" != typeof t ? t : function() {
                        var e = me;
                        r && Le(), We(n, true);
                        try {
                            return t.apply(this, arguments);
                        } finally {
                            We(e, false), i && queueMicrotask(Ue);
                        }
                    };
                }
                function Ge(e) {
                    Promise === se && 0 === De.echoes ? 0 === Re ? e() : enqueueNativeMicroTask(e) : setTimeout(e, 0);
                }
                -1 === ("" + F).indexOf("[native code]") && (Le = Ue = G);
                var Xe = _e.reject;
                var He = String.fromCharCode(65535), Je = "Invalid key provided. Keys must be of type string, number, Date or Array<string | number | Date>.", Ze = "String expected.", et = [], tt = "__dbnames", nt = "readonly", rt = "readwrite";
                function it(e, t) {
                    return e ? t ? function() {
                        return e.apply(this, arguments) && t.apply(this, arguments);
                    } : e : t;
                }
                var ot = {
                    type: 3,
                    lower: -1 / 0,
                    lowerOpen: false,
                    upper: [ [] ],
                    upperOpen: false
                };
                function at(t) {
                    return "string" != typeof t || /\./.test(t) ? function(e) {
                        return e;
                    } : function(e) {
                        return void 0 === e[t] && t in e && delete (e = S(e))[t], e;
                    };
                }
                function ut() {
                    throw Y.Type();
                }
                function st(e, t) {
                    try {
                        var n = ct(e), r = ct(t);
                        if (n !== r) return "Array" === n ? 1 : "Array" === r ? -1 : "binary" === n ? 1 : "binary" === r ? -1 : "string" === n ? 1 : "string" === r ? -1 : "Date" === n ? 1 : "Date" !== r ? NaN : -1;
                        switch (n) {
                          case "number":
                          case "Date":
                          case "string":
                            return t < e ? 1 : e < t ? -1 : 0;

                          case "binary":
                            return function(e, t) {
                                for (var n = e.length, r = t.length, i = n < r ? n : r, o = 0; o < i; ++o) if (e[o] !== t[o]) return e[o] < t[o] ? -1 : 1;
                                return n === r ? 0 : n < r ? -1 : 1;
                            }(lt(e), lt(t));

                          case "Array":
                            return function(e, t) {
                                for (var n = e.length, r = t.length, i = n < r ? n : r, o = 0; o < i; ++o) {
                                    var a = st(e[o], t[o]);
                                    if (0 !== a) return a;
                                }
                                return n === r ? 0 : n < r ? -1 : 1;
                            }(e, t);
                        }
                    } catch (e) {}
                    return NaN;
                }
                function ct(e) {
                    var t = typeof e;
                    if ("object" != t) return t;
                    if (ArrayBuffer.isView(e)) return "binary";
                    e = A(e);
                    return "ArrayBuffer" === e ? "binary" : e;
                }
                function lt(e) {
                    return e instanceof Uint8Array ? e : ArrayBuffer.isView(e) ? new Uint8Array(e.buffer, e.byteOffset, e.byteLength) : new Uint8Array(e);
                }
                var ft = (ht.prototype._trans = function(e, r, t) {
                    var n = this._tx || me.trans, i = this.name, o = ie && "undefined" != typeof console && console.createTask && console.createTask("Dexie: ".concat("readonly" === e ? "read" : "write", " ").concat(this.name));
                    function a(e, t, n) {
                        if (!n.schema[i]) throw new Y.NotFound("Table " + i + " not part of transaction");
                        return r(n.idbtrans, n);
                    }
                    var u = je();
                    try {
                        var s = n && n.db._novip === this.db._novip ? n === me.trans ? n._promise(e, a, t) : Ne((function() {
                            return n._promise(e, a, t);
                        }), {
                            trans: n,
                            transless: me.transless || me
                        }) : function t(n, r, i, o) {
                            if (n.idbdb && (n._state.openComplete || me.letThrough || n._vip)) {
                                var a = n._createTransaction(r, i, n._dbSchema);
                                try {
                                    a.create(), n._state.PR1398_maxLoop = 3;
                                } catch (e) {
                                    return e.name === z.InvalidState && n.isOpen() && 0 < --n._state.PR1398_maxLoop ? (console.warn("Dexie: Need to reopen db"), 
                                    n.close({
                                        disableAutoOpen: !1
                                    }), n.open().then((function() {
                                        return t(n, r, i, o);
                                    }))) : Xe(e);
                                }
                                return a._promise(r, (function(e, t) {
                                    return Ne((function() {
                                        return me.trans = a, o(e, t, a);
                                    }));
                                })).then((function(e) {
                                    if ("readwrite" === r) try {
                                        a.idbtrans.commit();
                                    } catch (e) {}
                                    return "readonly" === r ? e : a._completion.then((function() {
                                        return e;
                                    }));
                                }));
                            }
                            if (n._state.openComplete) return Xe(new Y.DatabaseClosed(n._state.dbOpenError));
                            if (!n._state.isBeingOpened) {
                                if (!n._state.autoOpen) return Xe(new Y.DatabaseClosed);
                                n.open().catch(G);
                            }
                            return n._state.dbReadyPromise.then((function() {
                                return t(n, r, i, o);
                            }));
                        }(this.db, e, [ this.name ], a);
                        return o && (s._consoleTask = o, s = s.catch((function(e) {
                            return console.trace(e), Xe(e);
                        }))), s;
                    } finally {
                        u && Ae();
                    }
                }, ht.prototype.get = function(t, e) {
                    var n = this;
                    return t && t.constructor === Object ? this.where(t).first(e) : null == t ? Xe(new Y.Type("Invalid argument to Table.get()")) : this._trans("readonly", (function(e) {
                        return n.core.get({
                            trans: e,
                            key: t
                        }).then((function(e) {
                            return n.hook.reading.fire(e);
                        }));
                    })).then(e);
                }, ht.prototype.where = function(o) {
                    if ("string" == typeof o) return new this.db.WhereClause(this, o);
                    if (k(o)) return new this.db.WhereClause(this, "[".concat(o.join("+"), "]"));
                    var n = x(o);
                    if (1 === n.length) return this.where(n[0]).equals(o[n[0]]);
                    var e = this.schema.indexes.concat(this.schema.primKey).filter((function(t) {
                        if (t.compound && n.every((function(e) {
                            return 0 <= t.keyPath.indexOf(e);
                        }))) {
                            for (var e = 0; e < n.length; ++e) if (-1 === n.indexOf(t.keyPath[e])) return false;
                            return true;
                        }
                        return false;
                    })).sort((function(e, t) {
                        return e.keyPath.length - t.keyPath.length;
                    }))[0];
                    if (e && this.db._maxKey !== He) {
                        var t = e.keyPath.slice(0, n.length);
                        return this.where(t).equals(t.map((function(e) {
                            return o[e];
                        })));
                    }
                    !e && ie && console.warn("The query ".concat(JSON.stringify(o), " on ").concat(this.name, " would benefit from a ") + "compound index [".concat(n.join("+"), "]"));
                    var a = this.schema.idxByName;
                    function u(e, t) {
                        return 0 === st(e, t);
                    }
                    var r = n.reduce((function(e, t) {
                        var n = e[0], r = e[1], e = a[t], i = o[t];
                        return [ n || e, n || !e ? it(r, e && e.multi ? function(e) {
                            e = O(e, t);
                            return k(e) && e.some((function(e) {
                                return u(i, e);
                            }));
                        } : function(e) {
                            return u(i, O(e, t));
                        }) : r ];
                    }), [ null, null ]), t = r[0], r = r[1];
                    return t ? this.where(t.name).equals(o[t.keyPath]).filter(r) : e ? this.filter(r) : this.where(n).equals("");
                }, ht.prototype.filter = function(e) {
                    return this.toCollection().and(e);
                }, ht.prototype.count = function(e) {
                    return this.toCollection().count(e);
                }, ht.prototype.offset = function(e) {
                    return this.toCollection().offset(e);
                }, ht.prototype.limit = function(e) {
                    return this.toCollection().limit(e);
                }, ht.prototype.each = function(e) {
                    return this.toCollection().each(e);
                }, ht.prototype.toArray = function(e) {
                    return this.toCollection().toArray(e);
                }, ht.prototype.toCollection = function() {
                    return new this.db.Collection(new this.db.WhereClause(this));
                }, ht.prototype.orderBy = function(e) {
                    return new this.db.Collection(new this.db.WhereClause(this, k(e) ? "[".concat(e.join("+"), "]") : e));
                }, ht.prototype.reverse = function() {
                    return this.toCollection().reverse();
                }, ht.prototype.mapToClass = function(r) {
                    var e, t = this.db, n = this.name;
                    function i() {
                        return null !== e && e.apply(this, arguments) || this;
                    }
                    (this.schema.mappedClass = r).prototype instanceof ut && (function(e, t) {
                        if ("function" != typeof t && null !== t) throw new TypeError("Class extends value " + String(t) + " is not a constructor or null");
                        function n() {
                            this.constructor = e;
                        }
                        s(e, t), e.prototype = null === t ? Object.create(t) : (n.prototype = t.prototype, 
                        new n);
                    }(i, e = r), Object.defineProperty(i.prototype, "db", {
                        get: function() {
                            return t;
                        },
                        enumerable: false,
                        configurable: true
                    }), i.prototype.table = function() {
                        return n;
                    }, r = i);
                    for (var o = new Set, a = r.prototype; a; a = c(a)) Object.getOwnPropertyNames(a).forEach((function(e) {
                        return o.add(e);
                    }));
                    function u(e) {
                        if (!e) return e;
                        var t, n = Object.create(r.prototype);
                        for (t in e) if (!o.has(t)) try {
                            n[t] = e[t];
                        } catch (e) {}
                        return n;
                    }
                    return this.schema.readHook && this.hook.reading.unsubscribe(this.schema.readHook), 
                    this.schema.readHook = u, this.hook("reading", u), r;
                }, ht.prototype.defineClass = function() {
                    return this.mapToClass((function(e) {
                        a(this, e);
                    }));
                }, ht.prototype.add = function(t, n) {
                    var r = this, e = this.schema.primKey, i = e.auto, o = e.keyPath, a = t;
                    return o && i && (a = at(o)(t)), this._trans("readwrite", (function(e) {
                        return r.core.mutate({
                            trans: e,
                            type: "add",
                            keys: null != n ? [ n ] : null,
                            values: [ a ]
                        });
                    })).then((function(e) {
                        return e.numFailures ? _e.reject(e.failures[0]) : e.lastResult;
                    })).then((function(e) {
                        if (o) try {
                            P(t, o, e);
                        } catch (e) {}
                        return e;
                    }));
                }, ht.prototype.update = function(e, t) {
                    if ("object" != typeof e || k(e)) return this.where(":id").equals(e).modify(t);
                    e = O(e, this.schema.primKey.keyPath);
                    return void 0 === e ? Xe(new Y.InvalidArgument("Given object does not contain its primary key")) : this.where(":id").equals(e).modify(t);
                }, ht.prototype.put = function(t, n) {
                    var r = this, e = this.schema.primKey, i = e.auto, o = e.keyPath, a = t;
                    return o && i && (a = at(o)(t)), this._trans("readwrite", (function(e) {
                        return r.core.mutate({
                            trans: e,
                            type: "put",
                            values: [ a ],
                            keys: null != n ? [ n ] : null
                        });
                    })).then((function(e) {
                        return e.numFailures ? _e.reject(e.failures[0]) : e.lastResult;
                    })).then((function(e) {
                        if (o) try {
                            P(t, o, e);
                        } catch (e) {}
                        return e;
                    }));
                }, ht.prototype.delete = function(t) {
                    var n = this;
                    return this._trans("readwrite", (function(e) {
                        return n.core.mutate({
                            trans: e,
                            type: "delete",
                            keys: [ t ]
                        });
                    })).then((function(e) {
                        return e.numFailures ? _e.reject(e.failures[0]) : void 0;
                    }));
                }, ht.prototype.clear = function() {
                    var t = this;
                    return this._trans("readwrite", (function(e) {
                        return t.core.mutate({
                            trans: e,
                            type: "deleteRange",
                            range: ot
                        });
                    })).then((function(e) {
                        return e.numFailures ? _e.reject(e.failures[0]) : void 0;
                    }));
                }, ht.prototype.bulkGet = function(t) {
                    var n = this;
                    return this._trans("readonly", (function(e) {
                        return n.core.getMany({
                            keys: t,
                            trans: e
                        }).then((function(e) {
                            return e.map((function(e) {
                                return n.hook.reading.fire(e);
                            }));
                        }));
                    }));
                }, ht.prototype.bulkAdd = function(r, e, t) {
                    var o = this, a = Array.isArray(e) ? e : void 0, u = (t = t || (a ? void 0 : e)) ? t.allKeys : void 0;
                    return this._trans("readwrite", (function(e) {
                        var t = o.schema.primKey, n = t.auto, t = t.keyPath;
                        if (t && a) throw new Y.InvalidArgument("bulkAdd(): keys argument invalid on tables with inbound keys");
                        if (a && a.length !== r.length) throw new Y.InvalidArgument("Arguments objects and keys must have the same length");
                        var i = r.length, t = t && n ? r.map(at(t)) : r;
                        return o.core.mutate({
                            trans: e,
                            type: "add",
                            keys: a,
                            values: t,
                            wantResults: u
                        }).then((function(e) {
                            var t = e.numFailures, n = e.results, r = e.lastResult, e = e.failures;
                            if (0 === t) return u ? n : r;
                            throw new V("".concat(o.name, ".bulkAdd(): ").concat(t, " of ").concat(i, " operations failed"), e);
                        }));
                    }));
                }, ht.prototype.bulkPut = function(r, e, t) {
                    var o = this, a = Array.isArray(e) ? e : void 0, u = (t = t || (a ? void 0 : e)) ? t.allKeys : void 0;
                    return this._trans("readwrite", (function(e) {
                        var t = o.schema.primKey, n = t.auto, t = t.keyPath;
                        if (t && a) throw new Y.InvalidArgument("bulkPut(): keys argument invalid on tables with inbound keys");
                        if (a && a.length !== r.length) throw new Y.InvalidArgument("Arguments objects and keys must have the same length");
                        var i = r.length, t = t && n ? r.map(at(t)) : r;
                        return o.core.mutate({
                            trans: e,
                            type: "put",
                            keys: a,
                            values: t,
                            wantResults: u
                        }).then((function(e) {
                            var t = e.numFailures, n = e.results, r = e.lastResult, e = e.failures;
                            if (0 === t) return u ? n : r;
                            throw new V("".concat(o.name, ".bulkPut(): ").concat(t, " of ").concat(i, " operations failed"), e);
                        }));
                    }));
                }, ht.prototype.bulkUpdate = function(t) {
                    var h = this, n = this.core, r = t.map((function(e) {
                        return e.key;
                    })), i = t.map((function(e) {
                        return e.changes;
                    })), d = [];
                    return this._trans("readwrite", (function(e) {
                        return n.getMany({
                            trans: e,
                            keys: r,
                            cache: "clone"
                        }).then((function(c) {
                            var l = [], f = [];
                            t.forEach((function(e, t) {
                                var n = e.key, r = e.changes, i = c[t];
                                if (i) {
                                    for (var o = 0, a = Object.keys(r); o < a.length; o++) {
                                        var u = a[o], s = r[u];
                                        if (u === h.schema.primKey.keyPath) {
                                            if (0 !== st(s, n)) throw new Y.Constraint("Cannot update primary key in bulkUpdate()");
                                        } else P(i, u, s);
                                    }
                                    d.push(t), l.push(n), f.push(i);
                                }
                            }));
                            var s = l.length;
                            return n.mutate({
                                trans: e,
                                type: "put",
                                keys: l,
                                values: f,
                                updates: {
                                    keys: r,
                                    changeSpecs: i
                                }
                            }).then((function(e) {
                                var t = e.numFailures, n = e.failures;
                                if (0 === t) return s;
                                for (var r = 0, i = Object.keys(n); r < i.length; r++) {
                                    var o, a = i[r], u = d[Number(a)];
                                    null != u && (o = n[a], delete n[a], n[u] = o);
                                }
                                throw new V("".concat(h.name, ".bulkUpdate(): ").concat(t, " of ").concat(s, " operations failed"), n);
                            }));
                        }));
                    }));
                }, ht.prototype.bulkDelete = function(t) {
                    var r = this, i = t.length;
                    return this._trans("readwrite", (function(e) {
                        return r.core.mutate({
                            trans: e,
                            type: "delete",
                            keys: t
                        });
                    })).then((function(e) {
                        var t = e.numFailures, n = e.lastResult, e = e.failures;
                        if (0 === t) return n;
                        throw new V("".concat(r.name, ".bulkDelete(): ").concat(t, " of ").concat(i, " operations failed"), e);
                    }));
                }, ht);
                function ht() {}
                function dt(i) {
                    function t(e, t) {
                        if (t) {
                            for (var n = arguments.length, r = new Array(n - 1); --n; ) r[n - 1] = arguments[n];
                            return a[e].subscribe.apply(null, r), i;
                        }
                        if ("string" == typeof e) return a[e];
                    }
                    var a = {};
                    t.addEventType = u;
                    for (var e = 1, n = arguments.length; e < n; ++e) u(arguments[e]);
                    return t;
                    function u(e, n, r) {
                        if ("object" != typeof e) {
                            var i;
                            n = n || ne;
                            var o = {
                                subscribers: [],
                                fire: r = r || G,
                                subscribe: function(e) {
                                    -1 === o.subscribers.indexOf(e) && (o.subscribers.push(e), o.fire = n(o.fire, e));
                                },
                                unsubscribe: function(t) {
                                    o.subscribers = o.subscribers.filter((function(e) {
                                        return e !== t;
                                    })), o.fire = o.subscribers.reduce(n, r);
                                }
                            };
                            return a[e] = t[e] = o;
                        }
                        x(i = e).forEach((function(e) {
                            var t = i[e];
                            if (k(t)) u(e, i[e][0], i[e][1]); else {
                                if ("asap" !== t) throw new Y.InvalidArgument("Invalid event config");
                                var n = u(e, X, (function() {
                                    for (var e = arguments.length, t = new Array(e); e--; ) t[e] = arguments[e];
                                    n.subscribers.forEach((function(e) {
                                        v((function() {
                                            e.apply(null, t);
                                        }));
                                    }));
                                }));
                            }
                        }));
                    }
                }
                function pt(e, t) {
                    return o(t).from({
                        prototype: e
                    }), t;
                }
                function yt(e, t) {
                    return !(e.filter || e.algorithm || e.or) && (t ? e.justLimit : !e.replayFilter);
                }
                function vt(e, t) {
                    e.filter = it(e.filter, t);
                }
                function mt(e, t, n) {
                    var r = e.replayFilter;
                    e.replayFilter = r ? function() {
                        return it(r(), t());
                    } : t, e.justLimit = n && !r;
                }
                function bt(e, t) {
                    if (e.isPrimKey) return t.primaryKey;
                    var n = t.getIndexByKeyPath(e.index);
                    if (!n) throw new Y.Schema("KeyPath " + e.index + " on object store " + t.name + " is not indexed");
                    return n;
                }
                function gt(e, t, n) {
                    var r = bt(e, t.schema);
                    return t.openCursor({
                        trans: n,
                        values: !e.keysOnly,
                        reverse: "prev" === e.dir,
                        unique: !!e.unique,
                        query: {
                            index: r,
                            range: e.range
                        }
                    });
                }
                function wt(e, o, t, n) {
                    var a = e.replayFilter ? it(e.filter, e.replayFilter()) : e.filter;
                    if (e.or) {
                        var u = {}, r = function(e, t, n) {
                            var r, i;
                            a && !a(t, n, (function(e) {
                                return t.stop(e);
                            }), (function(e) {
                                return t.fail(e);
                            })) || ("[object ArrayBuffer]" === (i = "" + (r = t.primaryKey)) && (i = "" + new Uint8Array(r)), 
                            m(u, i) || (u[i] = true, o(e, t, n)));
                        };
                        return Promise.all([ e.or._iterate(r, t), _t(gt(e, n, t), e.algorithm, r, !e.keysOnly && e.valueMapper) ]);
                    }
                    return _t(gt(e, n, t), it(e.algorithm, a), o, !e.keysOnly && e.valueMapper);
                }
                function _t(e, r, i, o) {
                    var a = qe(o ? function(e, t, n) {
                        return i(o(e), t, n);
                    } : i);
                    return e.then((function(n) {
                        if (n) return n.start((function() {
                            var t = function() {
                                return n.continue();
                            };
                            r && !r(n, (function(e) {
                                return t = e;
                            }), (function(e) {
                                n.stop(e), t = G;
                            }), (function(e) {
                                n.fail(e), t = G;
                            })) || a(n.value, n, (function(e) {
                                return t = e;
                            })), t();
                        }));
                    }));
                }
                var xt = (kt.prototype.execute = function(e) {
                    var t = this["@@propmod"];
                    if (void 0 !== t.add) {
                        var n = t.add;
                        if (k(n)) return i(i([], k(e) ? e : [], true), n).sort();
                        if ("number" == typeof n) return (Number(e) || 0) + n;
                        if ("bigint" == typeof n) try {
                            return BigInt(e) + n;
                        } catch (e) {
                            return BigInt(0) + n;
                        }
                        throw new TypeError("Invalid term ".concat(n));
                    }
                    if (void 0 !== t.remove) {
                        var r = t.remove;
                        if (k(r)) return k(e) ? e.filter((function(e) {
                            return !r.includes(e);
                        })).sort() : [];
                        if ("number" == typeof r) return Number(e) - r;
                        if ("bigint" == typeof r) try {
                            return BigInt(e) - r;
                        } catch (e) {
                            return BigInt(0) - r;
                        }
                        throw new TypeError("Invalid subtrahend ".concat(r));
                    }
                    n = null === (n = t.replacePrefix) || void 0 === n ? void 0 : n[0];
                    return n && "string" == typeof e && e.startsWith(n) ? t.replacePrefix[1] + e.substring(n.length) : e;
                }, kt);
                function kt(e) {
                    this["@@propmod"] = e;
                }
                var Ot = (Pt.prototype._read = function(e, t) {
                    var n = this._ctx;
                    return n.error ? n.table._trans(null, Xe.bind(null, n.error)) : n.table._trans("readonly", e).then(t);
                }, Pt.prototype._write = function(e) {
                    var t = this._ctx;
                    return t.error ? t.table._trans(null, Xe.bind(null, t.error)) : t.table._trans("readwrite", e, "locked");
                }, Pt.prototype._addAlgorithm = function(e) {
                    var t = this._ctx;
                    t.algorithm = it(t.algorithm, e);
                }, Pt.prototype._iterate = function(e, t) {
                    return wt(this._ctx, e, t, this._ctx.table.core);
                }, Pt.prototype.clone = function(e) {
                    var t = Object.create(this.constructor.prototype), n = Object.create(this._ctx);
                    return e && a(n, e), t._ctx = n, t;
                }, Pt.prototype.raw = function() {
                    return this._ctx.valueMapper = null, this;
                }, Pt.prototype.each = function(t) {
                    var n = this._ctx;
                    return this._read((function(e) {
                        return wt(n, t, e, n.table.core);
                    }));
                }, Pt.prototype.count = function(e) {
                    var i = this;
                    return this._read((function(e) {
                        var t = i._ctx, n = t.table.core;
                        if (yt(t, true)) return n.count({
                            trans: e,
                            query: {
                                index: bt(t, n.schema),
                                range: t.range
                            }
                        }).then((function(e) {
                            return Math.min(e, t.limit);
                        }));
                        var r = 0;
                        return wt(t, (function() {
                            return ++r, false;
                        }), e, n).then((function() {
                            return r;
                        }));
                    })).then(e);
                }, Pt.prototype.sortBy = function(e, t) {
                    var n = e.split(".").reverse(), r = n[0], i = n.length - 1;
                    function o(e, t) {
                        return t ? o(e[n[t]], t - 1) : e[r];
                    }
                    var a = "next" === this._ctx.dir ? 1 : -1;
                    function u(e, t) {
                        return st(o(e, i), o(t, i)) * a;
                    }
                    return this.toArray((function(e) {
                        return e.sort(u);
                    })).then(t);
                }, Pt.prototype.toArray = function(e) {
                    var o = this;
                    return this._read((function(e) {
                        var t = o._ctx;
                        if ("next" === t.dir && yt(t, true) && 0 < t.limit) {
                            var n = t.valueMapper, r = bt(t, t.table.core.schema);
                            return t.table.core.query({
                                trans: e,
                                limit: t.limit,
                                values: true,
                                query: {
                                    index: r,
                                    range: t.range
                                }
                            }).then((function(e) {
                                e = e.result;
                                return n ? e.map(n) : e;
                            }));
                        }
                        var i = [];
                        return wt(t, (function(e) {
                            return i.push(e);
                        }), e, t.table.core).then((function() {
                            return i;
                        }));
                    }), e);
                }, Pt.prototype.offset = function(t) {
                    var e = this._ctx;
                    return t <= 0 || (e.offset += t, yt(e) ? mt(e, (function() {
                        var n = t;
                        return function(e, t) {
                            return 0 === n || (1 === n ? --n : t((function() {
                                e.advance(n), n = 0;
                            })), false);
                        };
                    })) : mt(e, (function() {
                        var e = t;
                        return function() {
                            return --e < 0;
                        };
                    }))), this;
                }, Pt.prototype.limit = function(e) {
                    return this._ctx.limit = Math.min(this._ctx.limit, e), mt(this._ctx, (function() {
                        var r = e;
                        return function(e, t, n) {
                            return --r <= 0 && t(n), 0 <= r;
                        };
                    }), true), this;
                }, Pt.prototype.until = function(r, i) {
                    return vt(this._ctx, (function(e, t, n) {
                        return !r(e.value) || (t(n), i);
                    })), this;
                }, Pt.prototype.first = function(e) {
                    return this.limit(1).toArray((function(e) {
                        return e[0];
                    })).then(e);
                }, Pt.prototype.last = function(e) {
                    return this.reverse().first(e);
                }, Pt.prototype.filter = function(t) {
                    var e;
                    return vt(this._ctx, (function(e) {
                        return t(e.value);
                    })), (e = this._ctx).isMatch = it(e.isMatch, t), this;
                }, Pt.prototype.and = function(e) {
                    return this.filter(e);
                }, Pt.prototype.or = function(e) {
                    return new this.db.WhereClause(this._ctx.table, e, this);
                }, Pt.prototype.reverse = function() {
                    return this._ctx.dir = "prev" === this._ctx.dir ? "next" : "prev", this._ondirectionchange && this._ondirectionchange(this._ctx.dir), 
                    this;
                }, Pt.prototype.desc = function() {
                    return this.reverse();
                }, Pt.prototype.eachKey = function(n) {
                    var e = this._ctx;
                    return e.keysOnly = !e.isMatch, this.each((function(e, t) {
                        n(t.key, t);
                    }));
                }, Pt.prototype.eachUniqueKey = function(e) {
                    return this._ctx.unique = "unique", this.eachKey(e);
                }, Pt.prototype.eachPrimaryKey = function(n) {
                    var e = this._ctx;
                    return e.keysOnly = !e.isMatch, this.each((function(e, t) {
                        n(t.primaryKey, t);
                    }));
                }, Pt.prototype.keys = function(e) {
                    var t = this._ctx;
                    t.keysOnly = !t.isMatch;
                    var n = [];
                    return this.each((function(e, t) {
                        n.push(t.key);
                    })).then((function() {
                        return n;
                    })).then(e);
                }, Pt.prototype.primaryKeys = function(e) {
                    var n = this._ctx;
                    if ("next" === n.dir && yt(n, true) && 0 < n.limit) return this._read((function(e) {
                        var t = bt(n, n.table.core.schema);
                        return n.table.core.query({
                            trans: e,
                            values: false,
                            limit: n.limit,
                            query: {
                                index: t,
                                range: n.range
                            }
                        });
                    })).then((function(e) {
                        return e.result;
                    })).then(e);
                    n.keysOnly = !n.isMatch;
                    var r = [];
                    return this.each((function(e, t) {
                        r.push(t.primaryKey);
                    })).then((function() {
                        return r;
                    })).then(e);
                }, Pt.prototype.uniqueKeys = function(e) {
                    return this._ctx.unique = "unique", this.keys(e);
                }, Pt.prototype.firstKey = function(e) {
                    return this.limit(1).keys((function(e) {
                        return e[0];
                    })).then(e);
                }, Pt.prototype.lastKey = function(e) {
                    return this.reverse().firstKey(e);
                }, Pt.prototype.distinct = function() {
                    var e = this._ctx, e = e.index && e.table.schema.idxByName[e.index];
                    if (!e || !e.multi) return this;
                    var n = {};
                    return vt(this._ctx, (function(e) {
                        var t = e.primaryKey.toString(), e = m(n, t);
                        return n[t] = true, !e;
                    })), this;
                }, Pt.prototype.modify = function(w) {
                    var n = this, r = this._ctx;
                    return this._write((function(d) {
                        var a, u, p;
                        p = "function" == typeof w ? w : (a = x(w), u = a.length, function(e) {
                            for (var t = false, n = 0; n < u; ++n) {
                                var r = a[n], i = w[r], o = O(e, r);
                                i instanceof xt ? (P(e, r, i.execute(o)), t = true) : o !== i && (P(e, r, i), t = true);
                            }
                            return t;
                        });
                        var y = r.table.core, e = y.schema.primaryKey, v = e.outbound, m = e.extractKey, b = 200, e = n.db._options.modifyChunkSize;
                        e && (b = "object" == typeof e ? e[y.name] || e["*"] || 200 : e);
                        function g(e, t) {
                            var n = t.failures, t = t.numFailures;
                            c += e - t;
                            for (var r = 0, i = x(n); r < i.length; r++) {
                                var o = i[r];
                                s.push(n[o]);
                            }
                        }
                        var s = [], c = 0, t = [];
                        return n.clone().primaryKeys().then((function(l) {
                            function f(s) {
                                var c = Math.min(b, l.length - s);
                                return y.getMany({
                                    trans: d,
                                    keys: l.slice(s, s + c),
                                    cache: "immutable"
                                }).then((function(e) {
                                    for (var n = [], t = [], r = v ? [] : null, i = [], o = 0; o < c; ++o) {
                                        var a = e[o], u = {
                                            value: S(a),
                                            primKey: l[s + o]
                                        };
                                        false !== p.call(u, u.value, u) && (null == u.value ? i.push(l[s + o]) : v || 0 === st(m(a), m(u.value)) ? (t.push(u.value), 
                                        v && r.push(l[s + o])) : (i.push(l[s + o]), n.push(u.value)));
                                    }
                                    return Promise.resolve(0 < n.length && y.mutate({
                                        trans: d,
                                        type: "add",
                                        values: n
                                    }).then((function(e) {
                                        for (var t in e.failures) i.splice(parseInt(t), 1);
                                        g(n.length, e);
                                    }))).then((function() {
                                        return (0 < t.length || h && "object" == typeof w) && y.mutate({
                                            trans: d,
                                            type: "put",
                                            keys: r,
                                            values: t,
                                            criteria: h,
                                            changeSpec: "function" != typeof w && w,
                                            isAdditionalChunk: 0 < s
                                        }).then((function(e) {
                                            return g(t.length, e);
                                        }));
                                    })).then((function() {
                                        return (0 < i.length || h && w === Kt) && y.mutate({
                                            trans: d,
                                            type: "delete",
                                            keys: i,
                                            criteria: h,
                                            isAdditionalChunk: 0 < s
                                        }).then((function(e) {
                                            return g(i.length, e);
                                        }));
                                    })).then((function() {
                                        return l.length > s + c && f(s + b);
                                    }));
                                }));
                            }
                            var h = yt(r) && r.limit === 1 / 0 && ("function" != typeof w || w === Kt) && {
                                index: r.index,
                                range: r.range
                            };
                            return f(0).then((function() {
                                if (0 < s.length) throw new U("Error modifying one or more objects", s, c, t);
                                return l.length;
                            }));
                        }));
                    }));
                }, Pt.prototype.delete = function() {
                    var i = this._ctx, n = i.range;
                    return yt(i) && (i.isPrimKey || 3 === n.type) ? this._write((function(e) {
                        var t = i.table.core.schema.primaryKey, r = n;
                        return i.table.core.count({
                            trans: e,
                            query: {
                                index: t,
                                range: r
                            }
                        }).then((function(n) {
                            return i.table.core.mutate({
                                trans: e,
                                type: "deleteRange",
                                range: r
                            }).then((function(e) {
                                var t = e.failures;
                                e.lastResult, e.results;
                                e = e.numFailures;
                                if (e) throw new U("Could not delete some values", Object.keys(t).map((function(e) {
                                    return t[e];
                                })), n - e);
                                return n - e;
                            }));
                        }));
                    })) : this.modify(Kt);
                }, Pt);
                function Pt() {}
                var Kt = function(e, t) {
                    return t.value = null;
                };
                function Et(e, t) {
                    return e < t ? -1 : e === t ? 0 : 1;
                }
                function St(e, t) {
                    return t < e ? -1 : e === t ? 0 : 1;
                }
                function jt(e, t, n) {
                    e = e instanceof Dt ? new e.Collection(e) : e;
                    return e._ctx.error = new (n || TypeError)(t), e;
                }
                function At(e) {
                    return new e.Collection(e, (function() {
                        return qt("");
                    })).limit(0);
                }
                function Ct(e, s, n, r) {
                    var i, c, l, f, h, d, p, y = n.length;
                    if (!n.every((function(e) {
                        return "string" == typeof e;
                    }))) return jt(e, Ze);
                    function t(e) {
                        i = "next" === e ? function(e) {
                            return e.toUpperCase();
                        } : function(e) {
                            return e.toLowerCase();
                        }, c = "next" === e ? function(e) {
                            return e.toLowerCase();
                        } : function(e) {
                            return e.toUpperCase();
                        }, l = "next" === e ? Et : St;
                        var t = n.map((function(e) {
                            return {
                                lower: c(e),
                                upper: i(e)
                            };
                        })).sort((function(e, t) {
                            return l(e.lower, t.lower);
                        }));
                        f = t.map((function(e) {
                            return e.upper;
                        })), h = t.map((function(e) {
                            return e.lower;
                        })), p = "next" === (d = e) ? "" : r;
                    }
                    t("next");
                    e = new e.Collection(e, (function() {
                        return Tt(f[0], h[y - 1] + r);
                    }));
                    e._ondirectionchange = function(e) {
                        t(e);
                    };
                    var v = 0;
                    return e._addAlgorithm((function(e, t, n) {
                        var r = e.key;
                        if ("string" != typeof r) return false;
                        var i = c(r);
                        if (s(i, h, v)) return true;
                        for (var o = null, a = v; a < y; ++a) {
                            var u = function(e, t, n, r, i, o) {
                                for (var a = Math.min(e.length, r.length), u = -1, s = 0; s < a; ++s) {
                                    var c = t[s];
                                    if (c !== r[s]) return i(e[s], n[s]) < 0 ? e.substr(0, s) + n[s] + n.substr(s + 1) : i(e[s], r[s]) < 0 ? e.substr(0, s) + r[s] + n.substr(s + 1) : 0 <= u ? e.substr(0, u) + t[u] + n.substr(u + 1) : null;
                                    i(e[s], c) < 0 && (u = s);
                                }
                                return a < r.length && "next" === o ? e + n.substr(e.length) : a < e.length && "prev" === o ? e.substr(0, n.length) : u < 0 ? null : e.substr(0, u) + r[u] + n.substr(u + 1);
                            }(r, i, f[a], h[a], l, d);
                            null === u && null === o ? v = a + 1 : (null === o || 0 < l(o, u)) && (o = u);
                        }
                        return t(null !== o ? function() {
                            e.continue(o + p);
                        } : n), false;
                    })), e;
                }
                function Tt(e, t, n, r) {
                    return {
                        type: 2,
                        lower: e,
                        upper: t,
                        lowerOpen: n,
                        upperOpen: r
                    };
                }
                function qt(e) {
                    return {
                        type: 1,
                        lower: e,
                        upper: e
                    };
                }
                var Dt = (Object.defineProperty(It.prototype, "Collection", {
                    get: function() {
                        return this._ctx.table.db.Collection;
                    },
                    enumerable: false,
                    configurable: true
                }), It.prototype.between = function(e, t, n, r) {
                    n = false !== n, r = true === r;
                    try {
                        return 0 < this._cmp(e, t) || 0 === this._cmp(e, t) && (n || r) && (!n || !r) ? At(this) : new this.Collection(this, (function() {
                            return Tt(e, t, !n, !r);
                        }));
                    } catch (e) {
                        return jt(this, Je);
                    }
                }, It.prototype.equals = function(e) {
                    return null == e ? jt(this, Je) : new this.Collection(this, (function() {
                        return qt(e);
                    }));
                }, It.prototype.above = function(e) {
                    return null == e ? jt(this, Je) : new this.Collection(this, (function() {
                        return Tt(e, void 0, true);
                    }));
                }, It.prototype.aboveOrEqual = function(e) {
                    return null == e ? jt(this, Je) : new this.Collection(this, (function() {
                        return Tt(e, void 0, false);
                    }));
                }, It.prototype.below = function(e) {
                    return null == e ? jt(this, Je) : new this.Collection(this, (function() {
                        return Tt(void 0, e, false, true);
                    }));
                }, It.prototype.belowOrEqual = function(e) {
                    return null == e ? jt(this, Je) : new this.Collection(this, (function() {
                        return Tt(void 0, e);
                    }));
                }, It.prototype.startsWith = function(e) {
                    return "string" != typeof e ? jt(this, Ze) : this.between(e, e + He, true, true);
                }, It.prototype.startsWithIgnoreCase = function(e) {
                    return "" === e ? this.startsWith(e) : Ct(this, (function(e, t) {
                        return 0 === e.indexOf(t[0]);
                    }), [ e ], He);
                }, It.prototype.equalsIgnoreCase = function(e) {
                    return Ct(this, (function(e, t) {
                        return e === t[0];
                    }), [ e ], "");
                }, It.prototype.anyOfIgnoreCase = function() {
                    var e = I.apply(D, arguments);
                    return 0 === e.length ? At(this) : Ct(this, (function(e, t) {
                        return -1 !== t.indexOf(e);
                    }), e, "");
                }, It.prototype.startsWithAnyOfIgnoreCase = function() {
                    var e = I.apply(D, arguments);
                    return 0 === e.length ? At(this) : Ct(this, (function(t, e) {
                        return e.some((function(e) {
                            return 0 === t.indexOf(e);
                        }));
                    }), e, He);
                }, It.prototype.anyOf = function() {
                    var t = this, i = I.apply(D, arguments), o = this._cmp;
                    try {
                        i.sort(o);
                    } catch (e) {
                        return jt(this, Je);
                    }
                    if (0 === i.length) return At(this);
                    var e = new this.Collection(this, (function() {
                        return Tt(i[0], i[i.length - 1]);
                    }));
                    e._ondirectionchange = function(e) {
                        o = "next" === e ? t._ascending : t._descending, i.sort(o);
                    };
                    var a = 0;
                    return e._addAlgorithm((function(e, t, n) {
                        for (var r = e.key; 0 < o(r, i[a]); ) if (++a === i.length) return t(n), false;
                        return 0 === o(r, i[a]) || (t((function() {
                            e.continue(i[a]);
                        })), false);
                    })), e;
                }, It.prototype.notEqual = function(e) {
                    return this.inAnyRange([ [ -1 / 0, e ], [ e, this.db._maxKey ] ], {
                        includeLowers: false,
                        includeUppers: false
                    });
                }, It.prototype.noneOf = function() {
                    var e = I.apply(D, arguments);
                    if (0 === e.length) return new this.Collection(this);
                    try {
                        e.sort(this._ascending);
                    } catch (e) {
                        return jt(this, Je);
                    }
                    var t = e.reduce((function(e, t) {
                        return e ? e.concat([ [ e[e.length - 1][1], t ] ]) : [ [ -1 / 0, t ] ];
                    }), null);
                    return t.push([ e[e.length - 1], this.db._maxKey ]), this.inAnyRange(t, {
                        includeLowers: false,
                        includeUppers: false
                    });
                }, It.prototype.inAnyRange = function(e, t) {
                    var o = this, a = this._cmp, u = this._ascending, n = this._descending, s = this._min, c = this._max;
                    if (0 === e.length) return At(this);
                    if (!e.every((function(e) {
                        return void 0 !== e[0] && void 0 !== e[1] && u(e[0], e[1]) <= 0;
                    }))) return jt(this, "First argument to inAnyRange() must be an Array of two-value Arrays [lower,upper] where upper must not be lower than lower", Y.InvalidArgument);
                    var r = !t || false !== t.includeLowers, i = t && true === t.includeUppers;
                    var l, f = u;
                    function h(e, t) {
                        return f(e[0], t[0]);
                    }
                    try {
                        (l = e.reduce((function(e, t) {
                            for (var n = 0, r = e.length; n < r; ++n) {
                                var i = e[n];
                                if (a(t[0], i[1]) < 0 && 0 < a(t[1], i[0])) {
                                    i[0] = s(i[0], t[0]), i[1] = c(i[1], t[1]);
                                    break;
                                }
                            }
                            return n === r && e.push(t), e;
                        }), [])).sort(h);
                    } catch (e) {
                        return jt(this, Je);
                    }
                    var d = 0, p = i ? function(e) {
                        return 0 < u(e, l[d][1]);
                    } : function(e) {
                        return 0 <= u(e, l[d][1]);
                    }, y = r ? function(e) {
                        return 0 < n(e, l[d][0]);
                    } : function(e) {
                        return 0 <= n(e, l[d][0]);
                    };
                    var v = p, e = new this.Collection(this, (function() {
                        return Tt(l[0][0], l[l.length - 1][1], !r, !i);
                    }));
                    return e._ondirectionchange = function(e) {
                        f = "next" === e ? (v = p, u) : (v = y, n), l.sort(h);
                    }, e._addAlgorithm((function(e, t, n) {
                        for (var r, i = e.key; v(i); ) if (++d === l.length) return t(n), false;
                        return !p(r = i) && !y(r) || (0 === o._cmp(i, l[d][1]) || 0 === o._cmp(i, l[d][0]) || t((function() {
                            f === u ? e.continue(l[d][0]) : e.continue(l[d][1]);
                        })), false);
                    })), e;
                }, It.prototype.startsWithAnyOf = function() {
                    var e = I.apply(D, arguments);
                    return e.every((function(e) {
                        return "string" == typeof e;
                    })) ? 0 === e.length ? At(this) : this.inAnyRange(e.map((function(e) {
                        return [ e, e + He ];
                    }))) : jt(this, "startsWithAnyOf() only works with strings");
                }, It);
                function It() {}
                function Bt(t) {
                    return qe((function(e) {
                        return Rt(e), t(e.target.error), false;
                    }));
                }
                function Rt(e) {
                    e.stopPropagation && e.stopPropagation(), e.preventDefault && e.preventDefault();
                }
                var Ft = "storagemutated", Mt = "x-storagemutated-1", Nt = dt(null, Ft), Lt = (Ut.prototype._lock = function() {
                    return y(!me.global), ++this._reculock, 1 !== this._reculock || me.global || (me.lockOwnerFor = this), 
                    this;
                }, Ut.prototype._unlock = function() {
                    if (y(!me.global), 0 == --this._reculock) for (me.global || (me.lockOwnerFor = null); 0 < this._blockedFuncs.length && !this._locked(); ) {
                        var e = this._blockedFuncs.shift();
                        try {
                            $e(e[1], e[0]);
                        } catch (e) {}
                    }
                    return this;
                }, Ut.prototype._locked = function() {
                    return this._reculock && me.lockOwnerFor !== this;
                }, Ut.prototype.create = function(t) {
                    var n = this;
                    if (!this.mode) return this;
                    var e = this.db.idbdb, r = this.db._state.dbOpenError;
                    if (y(!this.idbtrans), !t && !e) switch (r && r.name) {
                      case "DatabaseClosedError":
                        throw new Y.DatabaseClosed(r);

                      case "MissingAPIError":
                        throw new Y.MissingAPI(r.message, r);

                      default:
                        throw new Y.OpenFailed(r);
                    }
                    if (!this.active) throw new Y.TransactionInactive;
                    return y(null === this._completion._state), (t = this.idbtrans = t || (this.db.core || e).transaction(this.storeNames, this.mode, {
                        durability: this.chromeTransactionDurability
                    })).onerror = qe((function(e) {
                        Rt(e), n._reject(t.error);
                    })), t.onabort = qe((function(e) {
                        Rt(e), n.active && n._reject(new Y.Abort(t.error)), n.active = false, n.on("abort").fire(e);
                    })), t.oncomplete = qe((function() {
                        n.active = false, n._resolve(), "mutatedParts" in t && Nt.storagemutated.fire(t.mutatedParts);
                    })), this;
                }, Ut.prototype._promise = function(n, r, i) {
                    var o = this;
                    if ("readwrite" === n && "readwrite" !== this.mode) return Xe(new Y.ReadOnly("Transaction is readonly"));
                    if (!this.active) return Xe(new Y.TransactionInactive);
                    if (this._locked()) return new _e((function(e, t) {
                        o._blockedFuncs.push([ function() {
                            o._promise(n, r, i).then(e, t);
                        }, me ]);
                    }));
                    if (i) return Ne((function() {
                        var e = new _e((function(e, t) {
                            o._lock();
                            var n = r(e, t, o);
                            n && n.then && n.then(e, t);
                        }));
                        return e.finally((function() {
                            return o._unlock();
                        })), e._lib = true, e;
                    }));
                    var e = new _e((function(e, t) {
                        var n = r(e, t, o);
                        n && n.then && n.then(e, t);
                    }));
                    return e._lib = true, e;
                }, Ut.prototype._root = function() {
                    return this.parent ? this.parent._root() : this;
                }, Ut.prototype.waitFor = function(e) {
                    var t, r = this._root(), i = _e.resolve(e);
                    r._waitingFor ? r._waitingFor = r._waitingFor.then((function() {
                        return i;
                    })) : (r._waitingFor = i, r._waitingQueue = [], t = r.idbtrans.objectStore(r.storeNames[0]), 
                    function e() {
                        for (++r._spinCount; r._waitingQueue.length; ) r._waitingQueue.shift()();
                        r._waitingFor && (t.get(-1 / 0).onsuccess = e);
                    }());
                    var o = r._waitingFor;
                    return new _e((function(t, n) {
                        i.then((function(e) {
                            return r._waitingQueue.push(qe(t.bind(null, e)));
                        }), (function(e) {
                            return r._waitingQueue.push(qe(n.bind(null, e)));
                        })).finally((function() {
                            r._waitingFor === o && (r._waitingFor = null);
                        }));
                    }));
                }, Ut.prototype.abort = function() {
                    this.active && (this.active = false, this.idbtrans && this.idbtrans.abort(), this._reject(new Y.Abort));
                }, Ut.prototype.table = function(e) {
                    var t = this._memoizedTables || (this._memoizedTables = {});
                    if (m(t, e)) return t[e];
                    var n = this.schema[e];
                    if (!n) throw new Y.NotFound("Table " + e + " not part of transaction");
                    n = new this.db.Table(e, n, this);
                    return n.core = this.db.core.table(e), t[e] = n;
                }, Ut);
                function Ut() {}
                function Vt(e, t, n, r, i, o, a) {
                    return {
                        name: e,
                        keyPath: t,
                        unique: n,
                        multi: r,
                        auto: i,
                        compound: o,
                        src: (n && !a ? "&" : "") + (r ? "*" : "") + (i ? "++" : "") + zt(t)
                    };
                }
                function zt(e) {
                    return "string" == typeof e ? e : e ? "[" + [].join.call(e, "+") + "]" : "";
                }
                function Wt(e, t, n) {
                    return {
                        name: e,
                        primKey: t,
                        indexes: n,
                        mappedClass: null,
                        idxByName: (r = function(e) {
                            return [ e.name, e ];
                        }, n.reduce((function(e, t, n) {
                            n = r(t, n);
                            return n && (e[n[0]] = n[1]), e;
                        }), {}))
                    };
                    var r;
                }
                var Yt = function(e) {
                    try {
                        return e.only([ [] ]), Yt = function() {
                            return [ [] ];
                        }, [ [] ];
                    } catch (e) {
                        return Yt = function() {
                            return He;
                        }, He;
                    }
                };
                function $t(t) {
                    return null == t ? function() {} : "string" == typeof t ? 1 === (n = t).split(".").length ? function(e) {
                        return e[n];
                    } : function(e) {
                        return O(e, n);
                    } : function(e) {
                        return O(e, t);
                    };
                    var n;
                }
                function Qt(e) {
                    return [].slice.call(e);
                }
                var Gt = 0;
                function Xt(e) {
                    return null == e ? ":id" : "string" == typeof e ? e : "[".concat(e.join("+"), "]");
                }
                function Ht(e, i, t) {
                    function _(e) {
                        if (3 === e.type) return null;
                        if (4 === e.type) throw new Error("Cannot convert never type to IDBKeyRange");
                        var t = e.lower, n = e.upper, r = e.lowerOpen, e = e.upperOpen;
                        return void 0 === t ? void 0 === n ? null : i.upperBound(n, !!e) : void 0 === n ? i.lowerBound(t, !!r) : i.bound(t, n, !!r, !!e);
                    }
                    function n(e) {
                        var h, w = e.name;
                        return {
                            name: w,
                            schema: e,
                            mutate: function(e) {
                                var y = e.trans, v = e.type, m = e.keys, b = e.values, g = e.range;
                                return new Promise((function(t, e) {
                                    t = qe(t);
                                    var n = y.objectStore(w), r = null == n.keyPath, i = "put" === v || "add" === v;
                                    if (!i && "delete" !== v && "deleteRange" !== v) throw new Error("Invalid operation type: " + v);
                                    var o, a = (m || b || {
                                        length: 1
                                    }).length;
                                    if (m && b && m.length !== b.length) throw new Error("Given keys array must have same length as given values array.");
                                    if (0 === a) return t({
                                        numFailures: 0,
                                        failures: {},
                                        results: [],
                                        lastResult: void 0
                                    });
                                    function u(e) {
                                        ++l, Rt(e);
                                    }
                                    var s = [], c = [], l = 0;
                                    if ("deleteRange" === v) {
                                        if (4 === g.type) return t({
                                            numFailures: l,
                                            failures: c,
                                            results: [],
                                            lastResult: void 0
                                        });
                                        3 === g.type ? s.push(o = n.clear()) : s.push(o = n.delete(_(g)));
                                    } else {
                                        var r = i ? r ? [ b, m ] : [ b, null ] : [ m, null ], f = r[0], h = r[1];
                                        if (i) for (var d = 0; d < a; ++d) s.push(o = h && void 0 !== h[d] ? n[v](f[d], h[d]) : n[v](f[d])), 
                                        o.onerror = u; else for (d = 0; d < a; ++d) s.push(o = n[v](f[d])), o.onerror = u;
                                    }
                                    function p(e) {
                                        e = e.target.result, s.forEach((function(e, t) {
                                            return null != e.error && (c[t] = e.error);
                                        })), t({
                                            numFailures: l,
                                            failures: c,
                                            results: "delete" === v ? m : s.map((function(e) {
                                                return e.result;
                                            })),
                                            lastResult: e
                                        });
                                    }
                                    o.onerror = function(e) {
                                        u(e), p(e);
                                    }, o.onsuccess = p;
                                }));
                            },
                            getMany: function(e) {
                                var f = e.trans, h = e.keys;
                                return new Promise((function(t, e) {
                                    t = qe(t);
                                    for (var n, r = f.objectStore(w), i = h.length, o = new Array(i), a = 0, u = 0, s = function(e) {
                                        e = e.target;
                                        o[e._pos] = e.result, ++u === a && t(o);
                                    }, c = Bt(e), l = 0; l < i; ++l) null != h[l] && ((n = r.get(h[l]))._pos = l, n.onsuccess = s, 
                                    n.onerror = c, ++a);
                                    0 === a && t(o);
                                }));
                            },
                            get: function(e) {
                                var r = e.trans, i = e.key;
                                return new Promise((function(t, e) {
                                    t = qe(t);
                                    var n = r.objectStore(w).get(i);
                                    n.onsuccess = function(e) {
                                        return t(e.target.result);
                                    }, n.onerror = Bt(e);
                                }));
                            },
                            query: (h = s, function(f) {
                                return new Promise((function(n, e) {
                                    n = qe(n);
                                    var r, i, o, t = f.trans, a = f.values, u = f.limit, s = f.query, c = u === 1 / 0 ? void 0 : u, l = s.index, s = s.range, t = t.objectStore(w), l = l.isPrimaryKey ? t : t.index(l.name), s = _(s);
                                    if (0 === u) return n({
                                        result: []
                                    });
                                    h ? ((c = a ? l.getAll(s, c) : l.getAllKeys(s, c)).onsuccess = function(e) {
                                        return n({
                                            result: e.target.result
                                        });
                                    }, c.onerror = Bt(e)) : (r = 0, i = !a && "openKeyCursor" in l ? l.openKeyCursor(s) : l.openCursor(s), 
                                    o = [], i.onsuccess = function(e) {
                                        var t = i.result;
                                        return t ? (o.push(a ? t.value : t.primaryKey), ++r === u ? n({
                                            result: o
                                        }) : void t.continue()) : n({
                                            result: o
                                        });
                                    }, i.onerror = Bt(e));
                                }));
                            }),
                            openCursor: function(e) {
                                var c = e.trans, o = e.values, a = e.query, u = e.reverse, l = e.unique;
                                return new Promise((function(t, n) {
                                    t = qe(t);
                                    var e = a.index, r = a.range, i = c.objectStore(w), i = e.isPrimaryKey ? i : i.index(e.name), e = u ? l ? "prevunique" : "prev" : l ? "nextunique" : "next", s = !o && "openKeyCursor" in i ? i.openKeyCursor(_(r), e) : i.openCursor(_(r), e);
                                    s.onerror = Bt(n), s.onsuccess = qe((function(e) {
                                        var r, i, o, a, u = s.result;
                                        u ? (u.___id = ++Gt, u.done = false, r = u.continue.bind(u), i = (i = u.continuePrimaryKey) && i.bind(u), 
                                        o = u.advance.bind(u), a = function() {
                                            throw new Error("Cursor not stopped");
                                        }, u.trans = c, u.stop = u.continue = u.continuePrimaryKey = u.advance = function() {
                                            throw new Error("Cursor not started");
                                        }, u.fail = qe(n), u.next = function() {
                                            var e = this, t = 1;
                                            return this.start((function() {
                                                return t-- ? e.continue() : e.stop();
                                            })).then((function() {
                                                return e;
                                            }));
                                        }, u.start = function(e) {
                                            function t() {
                                                if (s.result) try {
                                                    e();
                                                } catch (e) {
                                                    u.fail(e);
                                                } else u.done = true, u.start = function() {
                                                    throw new Error("Cursor behind last entry");
                                                }, u.stop();
                                            }
                                            var n = new Promise((function(t, e) {
                                                t = qe(t), s.onerror = Bt(e), u.fail = e, u.stop = function(e) {
                                                    u.stop = u.continue = u.continuePrimaryKey = u.advance = a, t(e);
                                                };
                                            }));
                                            return s.onsuccess = qe((function(e) {
                                                s.onsuccess = t, t();
                                            })), u.continue = r, u.continuePrimaryKey = i, u.advance = o, t(), n;
                                        }, t(u)) : t(null);
                                    }), n);
                                }));
                            },
                            count: function(e) {
                                var t = e.query, i = e.trans, o = t.index, a = t.range;
                                return new Promise((function(t, e) {
                                    var n = i.objectStore(w), r = o.isPrimaryKey ? n : n.index(o.name), n = _(a), r = n ? r.count(n) : r.count();
                                    r.onsuccess = qe((function(e) {
                                        return t(e.target.result);
                                    })), r.onerror = Bt(e);
                                }));
                            }
                        };
                    }
                    var r, o, a, u = (o = t, a = Qt((r = e).objectStoreNames), {
                        schema: {
                            name: r.name,
                            tables: a.map((function(e) {
                                return o.objectStore(e);
                            })).map((function(t) {
                                var e = t.keyPath, n = t.autoIncrement, r = k(e), i = {}, n = {
                                    name: t.name,
                                    primaryKey: {
                                        name: null,
                                        isPrimaryKey: true,
                                        outbound: null == e,
                                        compound: r,
                                        keyPath: e,
                                        autoIncrement: n,
                                        unique: true,
                                        extractKey: $t(e)
                                    },
                                    indexes: Qt(t.indexNames).map((function(e) {
                                        return t.index(e);
                                    })).map((function(e) {
                                        var t = e.name, n = e.unique, r = e.multiEntry, e = e.keyPath, r = {
                                            name: t,
                                            compound: k(e),
                                            keyPath: e,
                                            unique: n,
                                            multiEntry: r,
                                            extractKey: $t(e)
                                        };
                                        return i[Xt(e)] = r;
                                    })),
                                    getIndexByKeyPath: function(e) {
                                        return i[Xt(e)];
                                    }
                                };
                                return i[":id"] = n.primaryKey, null != e && (i[Xt(e)] = n.primaryKey), n;
                            }))
                        },
                        hasGetAll: 0 < a.length && "getAll" in o.objectStore(a[0]) && !("undefined" != typeof navigator && /Safari/.test(navigator.userAgent) && !/(Chrome\/|Edge\/)/.test(navigator.userAgent) && [].concat(navigator.userAgent.match(/Safari\/(\d*)/))[1] < 604)
                    }), t = u.schema, s = u.hasGetAll, u = t.tables.map(n), c = {};
                    return u.forEach((function(e) {
                        return c[e.name] = e;
                    })), {
                        stack: "dbcore",
                        transaction: e.transaction.bind(e),
                        table: function(e) {
                            if (!c[e]) throw new Error("Table '".concat(e, "' not found"));
                            return c[e];
                        },
                        MIN_KEY: -1 / 0,
                        MAX_KEY: Yt(i),
                        schema: t
                    };
                }
                function Jt(e, t, n, r) {
                    var i = n.IDBKeyRange;
                    return n.indexedDB, {
                        dbcore: (r = Ht(t, i, r), e.dbcore.reduce((function(e, t) {
                            t = t.create;
                            return _(_({}, e), t(e));
                        }), r))
                    };
                }
                function Zt(n, e) {
                    var t = e.db, e = Jt(n._middlewares, t, n._deps, e);
                    n.core = e.dbcore, n.tables.forEach((function(e) {
                        var t = e.name;
                        n.core.schema.tables.some((function(e) {
                            return e.name === t;
                        })) && (e.core = n.core.table(t), n[t] instanceof n.Table && (n[t].core = e.core));
                    }));
                }
                function en(i, e, t, o) {
                    t.forEach((function(n) {
                        var r = o[n];
                        e.forEach((function(e) {
                            var t = function e(t, n) {
                                return h(t, n) || (t = c(t)) && e(t, n);
                            }(e, n);
                            (!t || "value" in t && void 0 === t.value) && (e === i.Transaction.prototype || e instanceof i.Transaction ? l(e, n, {
                                get: function() {
                                    return this.table(n);
                                },
                                set: function(e) {
                                    u(this, n, {
                                        value: e,
                                        writable: true,
                                        configurable: true,
                                        enumerable: true
                                    });
                                }
                            }) : e[n] = new i.Table(n, r));
                        }));
                    }));
                }
                function tn(n, e) {
                    e.forEach((function(e) {
                        for (var t in e) e[t] instanceof n.Table && delete e[t];
                    }));
                }
                function nn(e, t) {
                    return e._cfg.version - t._cfg.version;
                }
                function rn(n, r, i, e) {
                    var o = n._dbSchema;
                    i.objectStoreNames.contains("$meta") && !o.$meta && (o.$meta = Wt("$meta", hn("")[0], []), 
                    n._storeNames.push("$meta"));
                    var a = n._createTransaction("readwrite", n._storeNames, o);
                    a.create(i), a._completion.catch(e);
                    var u = a._reject.bind(a), s = me.transless || me;
                    Ne((function() {
                        return me.trans = a, me.transless = s, 0 !== r ? (Zt(n, i), t = r, ((e = a).storeNames.includes("$meta") ? e.table("$meta").get("version").then((function(e) {
                            return null != e ? e : t;
                        })) : _e.resolve(t)).then((function(e) {
                            return c = e, l = a, f = i, t = [], e = (s = n)._versions, h = s._dbSchema = ln(0, s.idbdb, f), 
                            0 !== (e = e.filter((function(e) {
                                return e._cfg.version >= c;
                            }))).length ? (e.forEach((function(u) {
                                t.push((function() {
                                    var t = h, e = u._cfg.dbschema;
                                    fn(s, t, f), fn(s, e, f), h = s._dbSchema = e;
                                    var n = an(t, e);
                                    n.add.forEach((function(e) {
                                        un(f, e[0], e[1].primKey, e[1].indexes);
                                    })), n.change.forEach((function(e) {
                                        if (e.recreate) throw new Y.Upgrade("Not yet support for changing primary key");
                                        var t = f.objectStore(e.name);
                                        e.add.forEach((function(e) {
                                            return cn(t, e);
                                        })), e.change.forEach((function(e) {
                                            t.deleteIndex(e.name), cn(t, e);
                                        })), e.del.forEach((function(e) {
                                            return t.deleteIndex(e);
                                        }));
                                    }));
                                    var r = u._cfg.contentUpgrade;
                                    if (r && u._cfg.version > c) {
                                        Zt(s, f), l._memoizedTables = {};
                                        var i = g(e);
                                        n.del.forEach((function(e) {
                                            i[e] = t[e];
                                        })), tn(s, [ s.Transaction.prototype ]), en(s, [ s.Transaction.prototype ], x(i), i), 
                                        l.schema = i;
                                        var o, a = B(r);
                                        a && Le();
                                        n = _e.follow((function() {
                                            var e;
                                            (o = r(l)) && a && (e = Ue.bind(null, null), o.then(e, e));
                                        }));
                                        return o && "function" == typeof o.then ? _e.resolve(o) : n.then((function() {
                                            return o;
                                        }));
                                    }
                                })), t.push((function(e) {
                                    var t, n, r = u._cfg.dbschema;
                                    t = r, n = e, [].slice.call(n.db.objectStoreNames).forEach((function(e) {
                                        return null == t[e] && n.db.deleteObjectStore(e);
                                    })), tn(s, [ s.Transaction.prototype ]), en(s, [ s.Transaction.prototype ], s._storeNames, s._dbSchema), 
                                    l.schema = s._dbSchema;
                                })), t.push((function(e) {
                                    s.idbdb.objectStoreNames.contains("$meta") && (Math.ceil(s.idbdb.version / 10) === u._cfg.version ? (s.idbdb.deleteObjectStore("$meta"), 
                                    delete s._dbSchema.$meta, s._storeNames = s._storeNames.filter((function(e) {
                                        return "$meta" !== e;
                                    }))) : e.objectStore("$meta").put(u._cfg.version, "version"));
                                }));
                            })), function e() {
                                return t.length ? _e.resolve(t.shift()(l.idbtrans)).then(e) : _e.resolve();
                            }().then((function() {
                                sn(h, f);
                            }))) : _e.resolve();
                            var s, c, l, f, t, h;
                        })).catch(u)) : (x(o).forEach((function(e) {
                            un(i, e, o[e].primKey, o[e].indexes);
                        })), Zt(n, i), void _e.follow((function() {
                            return n.on.populate.fire(a);
                        })).catch(u));
                        var e, t;
                    }));
                }
                function on(e, r) {
                    sn(e._dbSchema, r), r.db.version % 10 != 0 || r.objectStoreNames.contains("$meta") || r.db.createObjectStore("$meta").add(Math.ceil(r.db.version / 10 - 1), "version");
                    var t = ln(0, e.idbdb, r);
                    fn(e, e._dbSchema, r);
                    for (var n = 0, i = an(t, e._dbSchema).change; n < i.length; n++) {
                        var o = function(t) {
                            if (t.change.length || t.recreate) return console.warn("Unable to patch indexes of table ".concat(t.name, " because it has changes on the type of index or primary key.")), 
                            {
                                value: void 0
                            };
                            var n = r.objectStore(t.name);
                            t.add.forEach((function(e) {
                                ie && console.debug("Dexie upgrade patch: Creating missing index ".concat(t.name, ".").concat(e.src)), 
                                cn(n, e);
                            }));
                        }(i[n]);
                        if ("object" == typeof o) return o.value;
                    }
                }
                function an(e, t) {
                    var n, r = {
                        del: [],
                        add: [],
                        change: []
                    };
                    for (n in e) t[n] || r.del.push(n);
                    for (n in t) {
                        var i = e[n], o = t[n];
                        if (i) {
                            var a = {
                                name: n,
                                def: o,
                                recreate: false,
                                del: [],
                                add: [],
                                change: []
                            };
                            if ("" + (i.primKey.keyPath || "") != "" + (o.primKey.keyPath || "") || i.primKey.auto !== o.primKey.auto) a.recreate = true, 
                            r.change.push(a); else {
                                var u = i.idxByName, s = o.idxByName, c = void 0;
                                for (c in u) s[c] || a.del.push(c);
                                for (c in s) {
                                    var l = u[c], f = s[c];
                                    l ? l.src !== f.src && a.change.push(f) : a.add.push(f);
                                }
                                (0 < a.del.length || 0 < a.add.length || 0 < a.change.length) && r.change.push(a);
                            }
                        } else r.add.push([ n, o ]);
                    }
                    return r;
                }
                function un(e, t, n, r) {
                    var i = e.db.createObjectStore(t, n.keyPath ? {
                        keyPath: n.keyPath,
                        autoIncrement: n.auto
                    } : {
                        autoIncrement: n.auto
                    });
                    return r.forEach((function(e) {
                        return cn(i, e);
                    })), i;
                }
                function sn(t, n) {
                    x(t).forEach((function(e) {
                        n.db.objectStoreNames.contains(e) || (ie && console.debug("Dexie: Creating missing table", e), 
                        un(n, e, t[e].primKey, t[e].indexes));
                    }));
                }
                function cn(e, t) {
                    e.createIndex(t.name, t.keyPath, {
                        unique: t.unique,
                        multiEntry: t.multi
                    });
                }
                function ln(e, t, u) {
                    var s = {};
                    return b(t.objectStoreNames, 0).forEach((function(e) {
                        for (var t = u.objectStore(e), n = Vt(zt(a = t.keyPath), a || "", true, false, !!t.autoIncrement, a && "string" != typeof a, true), r = [], i = 0; i < t.indexNames.length; ++i) {
                            var o = t.index(t.indexNames[i]), a = o.keyPath, o = Vt(o.name, a, !!o.unique, !!o.multiEntry, false, a && "string" != typeof a, false);
                            r.push(o);
                        }
                        s[e] = Wt(e, n, r);
                    })), s;
                }
                function fn(e, t, n) {
                    for (var r = n.db.objectStoreNames, i = 0; i < r.length; ++i) {
                        var o = r[i], a = n.objectStore(o);
                        e._hasGetAll = "getAll" in a;
                        for (var u = 0; u < a.indexNames.length; ++u) {
                            var s = a.indexNames[u], c = a.index(s).keyPath, l = "string" == typeof c ? c : "[" + b(c).join("+") + "]";
                            !t[o] || (c = t[o].idxByName[l]) && (c.name = s, delete t[o].idxByName[l], t[o].idxByName[s] = c);
                        }
                    }
                    "undefined" != typeof navigator && /Safari/.test(navigator.userAgent) && !/(Chrome\/|Edge\/)/.test(navigator.userAgent) && f.WorkerGlobalScope && f instanceof f.WorkerGlobalScope && [].concat(navigator.userAgent.match(/Safari\/(\d*)/))[1] < 604 && (e._hasGetAll = false);
                }
                function hn(e) {
                    return e.split(",").map((function(e, t) {
                        var n = (e = e.trim()).replace(/([&*]|\+\+)/g, ""), r = /^\[/.test(n) ? n.match(/^\[(.*)\]$/)[1].split("+") : n;
                        return Vt(n, r || null, /\&/.test(e), /\*/.test(e), /\+\+/.test(e), k(r), 0 === t);
                    }));
                }
                var dn = (pn.prototype._parseStoresSpec = function(r, i) {
                    x(r).forEach((function(e) {
                        if (null !== r[e]) {
                            var t = hn(r[e]), n = t.shift();
                            if (n.unique = true, n.multi) throw new Y.Schema("Primary key cannot be multi-valued");
                            t.forEach((function(e) {
                                if (e.auto) throw new Y.Schema("Only primary key can be marked as autoIncrement (++)");
                                if (!e.keyPath) throw new Y.Schema("Index must have a name and cannot be an empty string");
                            })), i[e] = Wt(e, n, t);
                        }
                    }));
                }, pn.prototype.stores = function(e) {
                    var t = this.db;
                    this._cfg.storesSource = this._cfg.storesSource ? a(this._cfg.storesSource, e) : e;
                    var e = t._versions, n = {}, r = {};
                    return e.forEach((function(e) {
                        a(n, e._cfg.storesSource), r = e._cfg.dbschema = {}, e._parseStoresSpec(n, r);
                    })), t._dbSchema = r, tn(t, [ t._allTables, t, t.Transaction.prototype ]), en(t, [ t._allTables, t, t.Transaction.prototype, this._cfg.tables ], x(r), r), 
                    t._storeNames = x(r), this;
                }, pn.prototype.upgrade = function(e) {
                    return this._cfg.contentUpgrade = re(this._cfg.contentUpgrade || G, e), this;
                }, pn);
                function pn() {}
                function yn(e, t) {
                    var n = e._dbNamesDB;
                    return n || (n = e._dbNamesDB = new er(tt, {
                        addons: [],
                        indexedDB: e,
                        IDBKeyRange: t
                    })).version(1).stores({
                        dbnames: "name"
                    }), n.table("dbnames");
                }
                function vn(e) {
                    return e && "function" == typeof e.databases;
                }
                function mn(e) {
                    return Ne((function() {
                        return me.letThrough = true, e();
                    }));
                }
                function bn(e) {
                    return !("from" in e);
                }
                var gn = function(e, t) {
                    if (!this) {
                        var n = new gn;
                        return e && "d" in e && a(n, e), n;
                    }
                    a(this, arguments.length ? {
                        d: 1,
                        from: e,
                        to: 1 < arguments.length ? t : e
                    } : {
                        d: 0
                    });
                };
                function wn(e, t, n) {
                    var r = st(t, n);
                    if (!isNaN(r)) {
                        if (0 < r) throw RangeError();
                        if (bn(e)) return a(e, {
                            from: t,
                            to: n,
                            d: 1
                        });
                        var i = e.l, r = e.r;
                        if (st(n, e.from) < 0) return i ? wn(i, t, n) : e.l = {
                            from: t,
                            to: n,
                            d: 1,
                            l: null,
                            r: null
                        }, On(e);
                        if (0 < st(t, e.to)) return r ? wn(r, t, n) : e.r = {
                            from: t,
                            to: n,
                            d: 1,
                            l: null,
                            r: null
                        }, On(e);
                        st(t, e.from) < 0 && (e.from = t, e.l = null, e.d = r ? r.d + 1 : 1), 0 < st(n, e.to) && (e.to = n, 
                        e.r = null, e.d = e.l ? e.l.d + 1 : 1);
                        n = !e.r;
                        i && !e.l && _n(e, i), r && n && _n(e, r);
                    }
                }
                function _n(e, t) {
                    bn(t) || function e(t, n) {
                        var r = n.from, i = n.to, o = n.l, n = n.r;
                        wn(t, r, i), o && e(t, o), n && e(t, n);
                    }(e, t);
                }
                function xn(e, t) {
                    var n = kn(t), r = n.next();
                    if (r.done) return false;
                    for (var i = r.value, o = kn(e), a = o.next(i.from), u = a.value; !r.done && !a.done; ) {
                        if (st(u.from, i.to) <= 0 && 0 <= st(u.to, i.from)) return true;
                        st(i.from, u.from) < 0 ? i = (r = n.next(u.from)).value : u = (a = o.next(i.from)).value;
                    }
                    return false;
                }
                function kn(e) {
                    var n = bn(e) ? null : {
                        s: 0,
                        n: e
                    };
                    return {
                        next: function(e) {
                            for (var t = 0 < arguments.length; n; ) switch (n.s) {
                              case 0:
                                if (n.s = 1, t) for (;n.n.l && st(e, n.n.from) < 0; ) n = {
                                    up: n,
                                    n: n.n.l,
                                    s: 1
                                }; else for (;n.n.l; ) n = {
                                    up: n,
                                    n: n.n.l,
                                    s: 1
                                };

                              case 1:
                                if (n.s = 2, !t || st(e, n.n.to) <= 0) return {
                                    value: n.n,
                                    done: false
                                };

                              case 2:
                                if (n.n.r) {
                                    n.s = 3, n = {
                                        up: n,
                                        n: n.n.r,
                                        s: 0
                                    };
                                    continue;
                                }

                              case 3:
                                n = n.up;
                            }
                            return {
                                done: true
                            };
                        }
                    };
                }
                function On(e) {
                    var t, n, r = ((null === (t = e.r) || void 0 === t ? void 0 : t.d) || 0) - ((null === (n = e.l) || void 0 === n ? void 0 : n.d) || 0), i = 1 < r ? "r" : r < -1 ? "l" : "";
                    i && (t = "r" == i ? "l" : "r", n = _({}, e), r = e[i], e.from = r.from, e.to = r.to, 
                    e[i] = r[i], n[i] = r[t], (e[t] = n).d = Pn(n)), e.d = Pn(e);
                }
                function Pn(e) {
                    var t = e.r, e = e.l;
                    return (t ? e ? Math.max(t.d, e.d) : t.d : e ? e.d : 0) + 1;
                }
                function Kn(t, n) {
                    return x(n).forEach((function(e) {
                        t[e] ? _n(t[e], n[e]) : t[e] = function e(t) {
                            var n, r, i = {};
                            for (n in t) m(t, n) && (r = t[n], i[n] = !r || "object" != typeof r || K.has(r.constructor) ? r : e(r));
                            return i;
                        }(n[e]);
                    })), t;
                }
                function En(t, n) {
                    return t.all || n.all || Object.keys(t).some((function(e) {
                        return n[e] && xn(n[e], t[e]);
                    }));
                }
                r(gn.prototype, ((F = {
                    add: function(e) {
                        return _n(this, e), this;
                    },
                    addKey: function(e) {
                        return wn(this, e, e), this;
                    },
                    addKeys: function(e) {
                        var t = this;
                        return e.forEach((function(e) {
                            return wn(t, e, e);
                        })), this;
                    },
                    hasKey: function(e) {
                        var t = kn(this).next(e).value;
                        return t && st(t.from, e) <= 0 && 0 <= st(t.to, e);
                    }
                })[C] = function() {
                    return kn(this);
                }, F));
                var Sn = {}, jn = {}, An = false;
                function Cn(e) {
                    Kn(jn, e), An || (An = true, setTimeout((function() {
                        An = false, Tn(jn, !(jn = {}));
                    }), 0));
                }
                function Tn(e, t) {
                    void 0 === t && (t = false);
                    var n = new Set;
                    if (e.all) for (var r = 0, i = Object.values(Sn); r < i.length; r++) qn(a = i[r], e, n, t); else for (var o in e) {
                        var a, u = /^idb\:\/\/(.*)\/(.*)\//.exec(o);
                        u && (o = u[1], u = u[2], (a = Sn["idb://".concat(o, "/").concat(u)]) && qn(a, e, n, t));
                    }
                    n.forEach((function(e) {
                        return e();
                    }));
                }
                function qn(e, t, n, r) {
                    for (var i = [], o = 0, a = Object.entries(e.queries.query); o < a.length; o++) {
                        for (var u = a[o], s = u[0], c = [], l = 0, f = u[1]; l < f.length; l++) {
                            var h = f[l];
                            En(t, h.obsSet) ? h.subscribers.forEach((function(e) {
                                return n.add(e);
                            })) : r && c.push(h);
                        }
                        r && i.push([ s, c ]);
                    }
                    if (r) for (var d = 0, p = i; d < p.length; d++) {
                        var y = p[d], s = y[0], c = y[1];
                        e.queries.query[s] = c;
                    }
                }
                function Dn(f) {
                    var h = f._state, r = f._deps.indexedDB;
                    if (h.isBeingOpened || f.idbdb) return h.dbReadyPromise.then((function() {
                        return h.dbOpenError ? Xe(h.dbOpenError) : f;
                    }));
                    h.isBeingOpened = true, h.dbOpenError = null, h.openComplete = false;
                    var t = h.openCanceller, d = Math.round(10 * f.verno), p = false;
                    function e() {
                        if (h.openCanceller !== t) throw new Y.DatabaseClosed("db.open() was cancelled");
                    }
                    function y() {
                        return new _e((function(s, n) {
                            if (e(), !r) throw new Y.MissingAPI;
                            var c = f.name, l = h.autoSchema || !d ? r.open(c) : r.open(c, d);
                            if (!l) throw new Y.MissingAPI;
                            l.onerror = Bt(n), l.onblocked = qe(f._fireOnBlocked), l.onupgradeneeded = qe((function(e) {
                                var t;
                                v = l.transaction, h.autoSchema && !f._options.allowEmptyDB ? (l.onerror = Rt, v.abort(), 
                                l.result.close(), (t = r.deleteDatabase(c)).onsuccess = t.onerror = qe((function() {
                                    n(new Y.NoSuchDatabase("Database ".concat(c, " doesnt exist")));
                                }))) : (v.onerror = Bt(n), e = e.oldVersion > Math.pow(2, 62) ? 0 : e.oldVersion, 
                                m = e < 1, f.idbdb = l.result, p && on(f, v), rn(f, e / 10, v, n));
                            }), n), l.onsuccess = qe((function() {
                                v = null;
                                var e, t, n, r, i, o = f.idbdb = l.result, a = b(o.objectStoreNames);
                                if (0 < a.length) try {
                                    var u = o.transaction(1 === (r = a).length ? r[0] : r, "readonly");
                                    if (h.autoSchema) t = o, n = u, (e = f).verno = t.version / 10, n = e._dbSchema = ln(0, t, n), 
                                    e._storeNames = b(t.objectStoreNames, 0), en(e, [ e._allTables ], x(n), n); else if (fn(f, f._dbSchema, u), 
                                    ((i = an(ln(0, (i = f).idbdb, u), i._dbSchema)).add.length || i.change.some((function(e) {
                                        return e.add.length || e.change.length;
                                    }))) && !p) return console.warn("Dexie SchemaDiff: Schema was extended without increasing the number passed to db.version(). Dexie will add missing parts and increment native version number to workaround this."), 
                                    o.close(), d = o.version + 1, p = !0, s(y());
                                    Zt(f, u);
                                } catch (e) {}
                                et.push(f), o.onversionchange = qe((function(e) {
                                    h.vcFired = true, f.on("versionchange").fire(e);
                                })), o.onclose = qe((function(e) {
                                    f.on("close").fire(e);
                                })), m && (i = f._deps, u = c, o = i.indexedDB, i = i.IDBKeyRange, vn(o) || u === tt || yn(o, i).put({
                                    name: u
                                }).catch(G)), s();
                            }), n);
                        })).catch((function(e) {
                            switch (null == e ? void 0 : e.name) {
                              case "UnknownError":
                                if (0 < h.PR1398_maxLoop) return h.PR1398_maxLoop--, console.warn("Dexie: Workaround for Chrome UnknownError on open()"), 
                                y();
                                break;

                              case "VersionError":
                                if (0 < d) return d = 0, y();
                            }
                            return _e.reject(e);
                        }));
                    }
                    var n, i = h.dbReadyResolve, v = null, m = false;
                    return _e.race([ t, ("undefined" == typeof navigator ? _e.resolve() : !navigator.userAgentData && /Safari\//.test(navigator.userAgent) && !/Chrom(e|ium)\//.test(navigator.userAgent) && indexedDB.databases ? new Promise((function(e) {
                        function t() {
                            return indexedDB.databases().finally(e);
                        }
                        n = setInterval(t, 100), t();
                    })).finally((function() {
                        return clearInterval(n);
                    })) : Promise.resolve()).then(y) ]).then((function() {
                        return e(), h.onReadyBeingFired = [], _e.resolve(mn((function() {
                            return f.on.ready.fire(f.vip);
                        }))).then((function e() {
                            if (0 < h.onReadyBeingFired.length) {
                                var t = h.onReadyBeingFired.reduce(re, G);
                                return h.onReadyBeingFired = [], _e.resolve(mn((function() {
                                    return t(f.vip);
                                }))).then(e);
                            }
                        }));
                    })).finally((function() {
                        h.openCanceller === t && (h.onReadyBeingFired = null, h.isBeingOpened = false);
                    })).catch((function(e) {
                        h.dbOpenError = e;
                        try {
                            v && v.abort();
                        } catch (e) {}
                        return t === h.openCanceller && f._close(), Xe(e);
                    })).finally((function() {
                        h.openComplete = true, i();
                    })).then((function() {
                        var n;
                        return m && (n = {}, f.tables.forEach((function(t) {
                            t.schema.indexes.forEach((function(e) {
                                e.name && (n["idb://".concat(f.name, "/").concat(t.name, "/").concat(e.name)] = new gn(-1 / 0, [ [ [] ] ]));
                            })), n["idb://".concat(f.name, "/").concat(t.name, "/")] = n["idb://".concat(f.name, "/").concat(t.name, "/:dels")] = new gn(-1 / 0, [ [ [] ] ]);
                        })), Nt(Ft).fire(n), Tn(n, true)), f;
                    }));
                }
                function In(t) {
                    function e(e) {
                        return t.next(e);
                    }
                    var r = n(e), i = n((function(e) {
                        return t.throw(e);
                    }));
                    function n(n) {
                        return function(e) {
                            var t = n(e), e = t.value;
                            return t.done ? e : e && "function" == typeof e.then ? e.then(r, i) : k(e) ? Promise.all(e).then(r, i) : r(e);
                        };
                    }
                    return n(e)();
                }
                function Bn(e, t, n) {
                    for (var r = k(e) ? e.slice() : [ e ], i = 0; i < n; ++i) r.push(t);
                    return r;
                }
                var Rn = {
                    stack: "dbcore",
                    name: "VirtualIndexMiddleware",
                    level: 1,
                    create: function(f) {
                        return _(_({}, f), {
                            table: function(e) {
                                var a = f.table(e), t = a.schema, u = {}, s = [];
                                function c(e, t, n) {
                                    var r = Xt(e), i = u[r] = u[r] || [], o = null == e ? 0 : "string" == typeof e ? 1 : e.length, a = 0 < t, a = _(_({}, n), {
                                        name: a ? "".concat(r, "(virtual-from:").concat(n.name, ")") : n.name,
                                        lowLevelIndex: n,
                                        isVirtual: a,
                                        keyTail: t,
                                        keyLength: o,
                                        extractKey: $t(e),
                                        unique: !a && n.unique
                                    });
                                    return i.push(a), a.isPrimaryKey || s.push(a), 1 < o && c(2 === o ? e[0] : e.slice(0, o - 1), t + 1, n), 
                                    i.sort((function(e, t) {
                                        return e.keyTail - t.keyTail;
                                    })), a;
                                }
                                e = c(t.primaryKey.keyPath, 0, t.primaryKey);
                                u[":id"] = [ e ];
                                for (var n = 0, r = t.indexes; n < r.length; n++) {
                                    var i = r[n];
                                    c(i.keyPath, 0, i);
                                }
                                function l(e) {
                                    var t, n = e.query.index;
                                    return n.isVirtual ? _(_({}, e), {
                                        query: {
                                            index: n.lowLevelIndex,
                                            range: (t = e.query.range, n = n.keyTail, {
                                                type: 1 === t.type ? 2 : t.type,
                                                lower: Bn(t.lower, t.lowerOpen ? f.MAX_KEY : f.MIN_KEY, n),
                                                lowerOpen: true,
                                                upper: Bn(t.upper, t.upperOpen ? f.MIN_KEY : f.MAX_KEY, n),
                                                upperOpen: true
                                            })
                                        }
                                    }) : e;
                                }
                                return _(_({}, a), {
                                    schema: _(_({}, t), {
                                        primaryKey: e,
                                        indexes: s,
                                        getIndexByKeyPath: function(e) {
                                            return (e = u[Xt(e)]) && e[0];
                                        }
                                    }),
                                    count: function(e) {
                                        return a.count(l(e));
                                    },
                                    query: function(e) {
                                        return a.query(l(e));
                                    },
                                    openCursor: function(t) {
                                        var e = t.query.index, r = e.keyTail, n = e.isVirtual, i = e.keyLength;
                                        return n ? a.openCursor(l(t)).then((function(e) {
                                            return e && o(e);
                                        })) : a.openCursor(t);
                                        function o(n) {
                                            return Object.create(n, {
                                                continue: {
                                                    value: function(e) {
                                                        null != e ? n.continue(Bn(e, t.reverse ? f.MAX_KEY : f.MIN_KEY, r)) : t.unique ? n.continue(n.key.slice(0, i).concat(t.reverse ? f.MIN_KEY : f.MAX_KEY, r)) : n.continue();
                                                    }
                                                },
                                                continuePrimaryKey: {
                                                    value: function(e, t) {
                                                        n.continuePrimaryKey(Bn(e, f.MAX_KEY, r), t);
                                                    }
                                                },
                                                primaryKey: {
                                                    get: function() {
                                                        return n.primaryKey;
                                                    }
                                                },
                                                key: {
                                                    get: function() {
                                                        var e = n.key;
                                                        return 1 === i ? e[0] : e.slice(0, i);
                                                    }
                                                },
                                                value: {
                                                    get: function() {
                                                        return n.value;
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        });
                    }
                };
                function Fn(i, o, a, u) {
                    return a = a || {}, u = u || "", x(i).forEach((function(e) {
                        var t, n, r;
                        m(o, e) ? (t = i[e], n = o[e], "object" == typeof t && "object" == typeof n && t && n ? (r = A(t)) !== A(n) ? a[u + e] = o[e] : "Object" === r ? Fn(t, n, a, u + e + ".") : t !== n && (a[u + e] = o[e]) : t !== n && (a[u + e] = o[e])) : a[u + e] = void 0;
                    })), x(o).forEach((function(e) {
                        m(i, e) || (a[u + e] = o[e]);
                    })), a;
                }
                function Mn(e, t) {
                    return "delete" === t.type ? t.keys : t.keys || t.values.map(e.extractKey);
                }
                var Nn = {
                    stack: "dbcore",
                    name: "HooksMiddleware",
                    level: 2,
                    create: function(e) {
                        return _(_({}, e), {
                            table: function(r) {
                                var y = e.table(r), v = y.schema.primaryKey;
                                return _(_({}, y), {
                                    mutate: function(e) {
                                        var t = me.trans, n = t.table(r).hook, h = n.deleting, d = n.creating, p = n.updating;
                                        switch (e.type) {
                                          case "add":
                                            if (d.fire === G) break;
                                            return t._promise("readwrite", (function() {
                                                return a(e);
                                            }), true);

                                          case "put":
                                            if (d.fire === G && p.fire === G) break;
                                            return t._promise("readwrite", (function() {
                                                return a(e);
                                            }), true);

                                          case "delete":
                                            if (h.fire === G) break;
                                            return t._promise("readwrite", (function() {
                                                return a(e);
                                            }), true);

                                          case "deleteRange":
                                            if (h.fire === G) break;
                                            return t._promise("readwrite", (function() {
                                                return function n(r, i, o) {
                                                    return y.query({
                                                        trans: r,
                                                        values: false,
                                                        query: {
                                                            index: v,
                                                            range: i
                                                        },
                                                        limit: o
                                                    }).then((function(e) {
                                                        var t = e.result;
                                                        return a({
                                                            type: "delete",
                                                            keys: t,
                                                            trans: r
                                                        }).then((function(e) {
                                                            return 0 < e.numFailures ? Promise.reject(e.failures[0]) : t.length < o ? {
                                                                failures: [],
                                                                numFailures: 0,
                                                                lastResult: void 0
                                                            } : n(r, _(_({}, i), {
                                                                lower: t[t.length - 1],
                                                                lowerOpen: true
                                                            }), o);
                                                        }));
                                                    }));
                                                }(e.trans, e.range, 1e4);
                                            }), true);
                                        }
                                        return y.mutate(e);
                                        function a(c) {
                                            var e, t, n, l = me.trans, f = c.keys || Mn(v, c);
                                            if (!f) throw new Error("Keys missing");
                                            return "delete" !== (c = "add" === c.type || "put" === c.type ? _(_({}, c), {
                                                keys: f
                                            }) : _({}, c)).type && (c.values = i([], c.values)), c.keys && (c.keys = i([], c.keys)), 
                                            e = y, n = f, ("add" === (t = c).type ? Promise.resolve([]) : e.getMany({
                                                trans: t.trans,
                                                keys: n,
                                                cache: "immutable"
                                            })).then((function(u) {
                                                var s = f.map((function(e, t) {
                                                    var n, r, i, o = u[t], a = {
                                                        onerror: null,
                                                        onsuccess: null
                                                    };
                                                    return "delete" === c.type ? h.fire.call(a, e, o, l) : "add" === c.type || void 0 === o ? (n = d.fire.call(a, e, c.values[t], l), 
                                                    null == e && null != n && (c.keys[t] = e = n, v.outbound || P(c.values[t], v.keyPath, e))) : (n = Fn(o, c.values[t]), 
                                                    (r = p.fire.call(a, n, e, o, l)) && (i = c.values[t], Object.keys(r).forEach((function(e) {
                                                        m(i, e) ? i[e] = r[e] : P(i, e, r[e]);
                                                    })))), a;
                                                }));
                                                return y.mutate(c).then((function(e) {
                                                    for (var t = e.failures, n = e.results, r = e.numFailures, e = e.lastResult, i = 0; i < f.length; ++i) {
                                                        var o = (n || f)[i], a = s[i];
                                                        null == o ? a.onerror && a.onerror(t[i]) : a.onsuccess && a.onsuccess("put" === c.type && u[i] ? c.values[i] : o);
                                                    }
                                                    return {
                                                        failures: t,
                                                        results: n,
                                                        numFailures: r,
                                                        lastResult: e
                                                    };
                                                })).catch((function(t) {
                                                    return s.forEach((function(e) {
                                                        return e.onerror && e.onerror(t);
                                                    })), Promise.reject(t);
                                                }));
                                            }));
                                        }
                                    }
                                });
                            }
                        });
                    }
                };
                function Ln(e, t, n) {
                    try {
                        if (!t) return null;
                        if (t.keys.length < e.length) return null;
                        for (var r = [], i = 0, o = 0; i < t.keys.length && o < e.length; ++i) 0 === st(t.keys[i], e[o]) && (r.push(n ? S(t.values[i]) : t.values[i]), 
                        ++o);
                        return r.length === e.length ? r : null;
                    } catch (e) {
                        return null;
                    }
                }
                var Un = {
                    stack: "dbcore",
                    level: -1,
                    create: function(t) {
                        return {
                            table: function(e) {
                                var n = t.table(e);
                                return _(_({}, n), {
                                    getMany: function(t) {
                                        if (!t.cache) return n.getMany(t);
                                        var e = Ln(t.keys, t.trans._cache, "clone" === t.cache);
                                        return e ? _e.resolve(e) : n.getMany(t).then((function(e) {
                                            return t.trans._cache = {
                                                keys: t.keys,
                                                values: "clone" === t.cache ? S(e) : e
                                            }, e;
                                        }));
                                    },
                                    mutate: function(e) {
                                        return "add" !== e.type && (e.trans._cache = null), n.mutate(e);
                                    }
                                });
                            }
                        };
                    }
                };
                function Vn(e, t) {
                    return "readonly" === e.trans.mode && !!e.subscr && !e.trans.explicit && "disabled" !== e.trans.db._options.cache && !t.schema.primaryKey.outbound;
                }
                function zn(e, t) {
                    switch (e) {
                      case "query":
                        return t.values && !t.unique;

                      case "get":
                      case "getMany":
                      case "count":
                      case "openCursor":
                        return false;
                    }
                }
                var Wn = {
                    stack: "dbcore",
                    level: 0,
                    name: "Observability",
                    create: function(b) {
                        var g = b.schema.name, w = new gn(b.MIN_KEY, b.MAX_KEY);
                        return _(_({}, b), {
                            transaction: function(e, t, n) {
                                if (me.subscr && "readonly" !== t) throw new Y.ReadOnly("Readwrite transaction in liveQuery context. Querier source: ".concat(me.querier));
                                return b.transaction(e, t, n);
                            },
                            table: function(d) {
                                var p = b.table(d), y = p.schema, v = y.primaryKey, e = y.indexes, c = v.extractKey, l = v.outbound, m = v.autoIncrement && e.filter((function(e) {
                                    return e.compound && e.keyPath.includes(v.keyPath);
                                })), t = _(_({}, p), {
                                    mutate: function(a) {
                                        function u(e) {
                                            return e = "idb://".concat(g, "/").concat(d, "/").concat(e), n[e] || (n[e] = new gn);
                                        }
                                        var e, o, s, t = a.trans, n = a.mutatedParts || (a.mutatedParts = {}), r = u(""), i = u(":dels"), c = a.type, l = "deleteRange" === a.type ? [ a.range ] : "delete" === a.type ? [ a.keys ] : a.values.length < 50 ? [ Mn(v, a).filter((function(e) {
                                            return e;
                                        })), a.values ] : [], f = l[0], h = l[1], l = a.trans._cache;
                                        return k(f) ? (r.addKeys(f), (l = "delete" === c || f.length === h.length ? Ln(f, l) : null) || i.addKeys(f), 
                                        (l || h) && (e = u, o = l, s = h, y.indexes.forEach((function(t) {
                                            var n = e(t.name || "");
                                            function r(e) {
                                                return null != e ? t.extractKey(e) : null;
                                            }
                                            function i(e) {
                                                return t.multiEntry && k(e) ? e.forEach((function(e) {
                                                    return n.addKey(e);
                                                })) : n.addKey(e);
                                            }
                                            (o || s).forEach((function(e, t) {
                                                var n = o && r(o[t]), t = s && r(s[t]);
                                                0 !== st(n, t) && (null != n && i(n), null != t && i(t));
                                            }));
                                        })))) : f ? (h = {
                                            from: null !== (h = f.lower) && void 0 !== h ? h : b.MIN_KEY,
                                            to: null !== (h = f.upper) && void 0 !== h ? h : b.MAX_KEY
                                        }, i.add(h), r.add(h)) : (r.add(w), i.add(w), y.indexes.forEach((function(e) {
                                            return u(e.name).add(w);
                                        }))), p.mutate(a).then((function(o) {
                                            return !f || "add" !== a.type && "put" !== a.type || (r.addKeys(o.results), m && m.forEach((function(t) {
                                                for (var e = a.values.map((function(e) {
                                                    return t.extractKey(e);
                                                })), n = t.keyPath.findIndex((function(e) {
                                                    return e === v.keyPath;
                                                })), r = 0, i = o.results.length; r < i; ++r) e[r][n] = o.results[r];
                                                u(t.name).addKeys(e);
                                            }))), t.mutatedParts = Kn(t.mutatedParts || {}, n), o;
                                        }));
                                    }
                                }), e = function(e) {
                                    var t = e.query, e = t.index, t = t.range;
                                    return [ e, new gn(null !== (e = t.lower) && void 0 !== e ? e : b.MIN_KEY, null !== (t = t.upper) && void 0 !== t ? t : b.MAX_KEY) ];
                                }, f = {
                                    get: function(e) {
                                        return [ v, new gn(e.key) ];
                                    },
                                    getMany: function(e) {
                                        return [ v, (new gn).addKeys(e.keys) ];
                                    },
                                    count: e,
                                    query: e,
                                    openCursor: e
                                };
                                return x(f).forEach((function(s) {
                                    t[s] = function(i) {
                                        var e = me.subscr, t = !!e, n = Vn(me, p) && zn(s, i) ? i.obsSet = {} : e;
                                        if (t) {
                                            var r = function(e) {
                                                e = "idb://".concat(g, "/").concat(d, "/").concat(e);
                                                return n[e] || (n[e] = new gn);
                                            }, o = r(""), a = r(":dels"), e = f[s](i), t = e[0], e = e[1];
                                            if (("query" === s && t.isPrimaryKey && !i.values ? a : r(t.name || "")).add(e), 
                                            !t.isPrimaryKey) {
                                                if ("count" !== s) {
                                                    var u = "query" === s && l && i.values && p.query(_(_({}, i), {
                                                        values: false
                                                    }));
                                                    return p[s].apply(this, arguments).then((function(t) {
                                                        if ("query" === s) {
                                                            if (l && i.values) return u.then((function(e) {
                                                                e = e.result;
                                                                return o.addKeys(e), t;
                                                            }));
                                                            var e = i.values ? t.result.map(c) : t.result;
                                                            (i.values ? o : a).addKeys(e);
                                                        } else if ("openCursor" === s) {
                                                            var n = t, r = i.values;
                                                            return n && Object.create(n, {
                                                                key: {
                                                                    get: function() {
                                                                        return a.addKey(n.primaryKey), n.key;
                                                                    }
                                                                },
                                                                primaryKey: {
                                                                    get: function() {
                                                                        var e = n.primaryKey;
                                                                        return a.addKey(e), e;
                                                                    }
                                                                },
                                                                value: {
                                                                    get: function() {
                                                                        return r && o.addKey(n.primaryKey), n.value;
                                                                    }
                                                                }
                                                            });
                                                        }
                                                        return t;
                                                    }));
                                                }
                                                a.add(w);
                                            }
                                        }
                                        return p[s].apply(this, arguments);
                                    };
                                })), t;
                            }
                        });
                    }
                };
                function Yn(e, t, n) {
                    if (0 === n.numFailures) return t;
                    if ("deleteRange" === t.type) return null;
                    var r = t.keys ? t.keys.length : "values" in t && t.values ? t.values.length : 1;
                    if (n.numFailures === r) return null;
                    t = _({}, t);
                    return k(t.keys) && (t.keys = t.keys.filter((function(e, t) {
                        return !(t in n.failures);
                    }))), "values" in t && k(t.values) && (t.values = t.values.filter((function(e, t) {
                        return !(t in n.failures);
                    }))), t;
                }
                function $n(e, t) {
                    return n = e, (void 0 === (r = t).lower || (r.lowerOpen ? 0 < st(n, r.lower) : 0 <= st(n, r.lower))) && (e = e, 
                    void 0 === (t = t).upper || (t.upperOpen ? st(e, t.upper) < 0 : st(e, t.upper) <= 0));
                    var n, r;
                }
                function Qn(e, d, t, n, r, i) {
                    if (!t || 0 === t.length) return e;
                    var o = d.query.index, p = o.multiEntry, y = d.query.range, v = n.schema.primaryKey.extractKey, m = o.extractKey, a = (o.lowLevelIndex || o).extractKey, t = t.reduce((function(e, t) {
                        var n = e, r = [];
                        if ("add" === t.type || "put" === t.type) for (var i = new gn, o = t.values.length - 1; 0 <= o; --o) {
                            var a, u = t.values[o], s = v(u);
                            i.hasKey(s) || (a = m(u), (p && k(a) ? a.some((function(e) {
                                return $n(e, y);
                            })) : $n(a, y)) && (i.addKey(s), r.push(u)));
                        }
                        switch (t.type) {
                          case "add":
                            var c = (new gn).addKeys(d.values ? e.map((function(e) {
                                return v(e);
                            })) : e), n = e.concat(d.values ? r.filter((function(e) {
                                e = v(e);
                                return !c.hasKey(e) && (c.addKey(e), true);
                            })) : r.map((function(e) {
                                return v(e);
                            })).filter((function(e) {
                                return !c.hasKey(e) && (c.addKey(e), true);
                            })));
                            break;

                          case "put":
                            var l = (new gn).addKeys(t.values.map((function(e) {
                                return v(e);
                            })));
                            n = e.filter((function(e) {
                                return !l.hasKey(d.values ? v(e) : e);
                            })).concat(d.values ? r : r.map((function(e) {
                                return v(e);
                            })));
                            break;

                          case "delete":
                            var f = (new gn).addKeys(t.keys);
                            n = e.filter((function(e) {
                                return !f.hasKey(d.values ? v(e) : e);
                            }));
                            break;

                          case "deleteRange":
                            var h = t.range;
                            n = e.filter((function(e) {
                                return !$n(v(e), h);
                            }));
                        }
                        return n;
                    }), e);
                    return t === e ? e : (t.sort((function(e, t) {
                        return st(a(e), a(t)) || st(v(e), v(t));
                    })), d.limit && d.limit < 1 / 0 && (t.length > d.limit ? t.length = d.limit : e.length === d.limit && t.length < d.limit && (r.dirty = true)), 
                    i ? Object.freeze(t) : t);
                }
                function Gn(e, t) {
                    return 0 === st(e.lower, t.lower) && 0 === st(e.upper, t.upper) && !!e.lowerOpen == !!t.lowerOpen && !!e.upperOpen == !!t.upperOpen;
                }
                function Xn(e, t) {
                    return function(e, t, n, r) {
                        if (void 0 === e) return void 0 !== t ? -1 : 0;
                        if (void 0 === t) return 1;
                        if (0 === (t = st(e, t))) {
                            if (n && r) return 0;
                            if (n) return 1;
                            if (r) return -1;
                        }
                        return t;
                    }(e.lower, t.lower, e.lowerOpen, t.lowerOpen) <= 0 && 0 <= function(e, t, n, r) {
                        if (void 0 === e) return void 0 !== t ? 1 : 0;
                        if (void 0 === t) return -1;
                        if (0 === (t = st(e, t))) {
                            if (n && r) return 0;
                            if (n) return -1;
                            if (r) return 1;
                        }
                        return t;
                    }(e.upper, t.upper, e.upperOpen, t.upperOpen);
                }
                function Hn(n, r, i, e) {
                    n.subscribers.add(i), e.addEventListener("abort", (function() {
                        var e, t;
                        n.subscribers.delete(i), 0 === n.subscribers.size && (e = n, t = r, setTimeout((function() {
                            0 === e.subscribers.size && q(t, e);
                        }), 3e3));
                    }));
                }
                var Jn = {
                    stack: "dbcore",
                    level: 0,
                    name: "Cache",
                    create: function(k) {
                        var O = k.schema.name;
                        return _(_({}, k), {
                            transaction: function(g, w, e) {
                                var _, t, x = k.transaction(g, w, e);
                                return "readwrite" === w && (t = (_ = new AbortController).signal, e = function(b) {
                                    return function() {
                                        if (_.abort(), "readwrite" === w) {
                                            for (var t = new Set, e = 0, n = g; e < n.length; e++) {
                                                var r = n[e], i = Sn["idb://".concat(O, "/").concat(r)];
                                                if (i) {
                                                    var o = k.table(r), a = i.optimisticOps.filter((function(e) {
                                                        return e.trans === x;
                                                    }));
                                                    if (x._explicit && b && x.mutatedParts) for (var u = 0, s = Object.values(i.queries.query); u < s.length; u++) for (var c = 0, l = (d = s[u]).slice(); c < l.length; c++) En((p = l[c]).obsSet, x.mutatedParts) && (q(d, p), 
                                                    p.subscribers.forEach((function(e) {
                                                        return t.add(e);
                                                    }))); else if (0 < a.length) {
                                                        i.optimisticOps = i.optimisticOps.filter((function(e) {
                                                            return e.trans !== x;
                                                        }));
                                                        for (var f = 0, h = Object.values(i.queries.query); f < h.length; f++) for (var d, p, y, v = 0, m = (d = h[f]).slice(); v < m.length; v++) null != (p = m[v]).res && x.mutatedParts && (b && !p.dirty ? (y = Object.isFrozen(p.res), 
                                                        y = Qn(p.res, p.req, a, o, p, y), p.dirty ? (q(d, p), p.subscribers.forEach((function(e) {
                                                            return t.add(e);
                                                        }))) : y !== p.res && (p.res = y, p.promise = _e.resolve({
                                                            result: y
                                                        }))) : (p.dirty && q(d, p), p.subscribers.forEach((function(e) {
                                                            return t.add(e);
                                                        }))));
                                                    }
                                                }
                                            }
                                            t.forEach((function(e) {
                                                return e();
                                            }));
                                        }
                                    };
                                }, x.addEventListener("abort", e(false), {
                                    signal: t
                                }), x.addEventListener("error", e(false), {
                                    signal: t
                                }), x.addEventListener("complete", e(true), {
                                    signal: t
                                })), x;
                            },
                            table: function(c) {
                                var l = k.table(c), i = l.schema.primaryKey;
                                return _(_({}, l), {
                                    mutate: function(t) {
                                        var e = me.trans;
                                        if (i.outbound || "disabled" === e.db._options.cache || e.explicit || "readwrite" !== e.idbtrans.mode) return l.mutate(t);
                                        var n = Sn["idb://".concat(O, "/").concat(c)];
                                        if (!n) return l.mutate(t);
                                        e = l.mutate(t);
                                        return "add" !== t.type && "put" !== t.type || !(50 <= t.values.length || Mn(i, t).some((function(e) {
                                            return null == e;
                                        }))) ? (n.optimisticOps.push(t), t.mutatedParts && Cn(t.mutatedParts), e.then((function(e) {
                                            0 < e.numFailures && (q(n.optimisticOps, t), (e = Yn(0, t, e)) && n.optimisticOps.push(e), 
                                            t.mutatedParts && Cn(t.mutatedParts));
                                        })), e.catch((function() {
                                            q(n.optimisticOps, t), t.mutatedParts && Cn(t.mutatedParts);
                                        }))) : e.then((function(r) {
                                            var e = Yn(0, _(_({}, t), {
                                                values: t.values.map((function(e, t) {
                                                    var n;
                                                    if (r.failures[t]) return e;
                                                    e = null !== (n = i.keyPath) && void 0 !== n && n.includes(".") ? S(e) : _({}, e);
                                                    return P(e, i.keyPath, r.results[t]), e;
                                                }))
                                            }), r);
                                            n.optimisticOps.push(e), queueMicrotask((function() {
                                                return t.mutatedParts && Cn(t.mutatedParts);
                                            }));
                                        })), e;
                                    },
                                    query: function(t) {
                                        if (!Vn(me, l) || !zn("query", t)) return l.query(t);
                                        var i = "immutable" === (null === (o = me.trans) || void 0 === o ? void 0 : o.db._options.cache), e = me, n = e.requery, r = e.signal, o = function(e, t, n, r) {
                                            var i = Sn["idb://".concat(e, "/").concat(t)];
                                            if (!i) return [];
                                            if (!(t = i.queries[n])) return [ null, false, i, null ];
                                            var o = t[(r.query ? r.query.index.name : null) || ""];
                                            if (!o) return [ null, false, i, null ];
                                            switch (n) {
                                              case "query":
                                                var a = o.find((function(e) {
                                                    return e.req.limit === r.limit && e.req.values === r.values && Gn(e.req.query.range, r.query.range);
                                                }));
                                                return a ? [ a, true, i, o ] : [ o.find((function(e) {
                                                    return ("limit" in e.req ? e.req.limit : 1 / 0) >= r.limit && (!r.values || e.req.values) && Xn(e.req.query.range, r.query.range);
                                                })), false, i, o ];

                                              case "count":
                                                a = o.find((function(e) {
                                                    return Gn(e.req.query.range, r.query.range);
                                                }));
                                                return [ a, !!a, i, o ];
                                            }
                                        }(O, c, "query", t), a = o[0], e = o[1], u = o[2], s = o[3];
                                        return a && e ? a.obsSet = t.obsSet : (e = l.query(t).then((function(e) {
                                            var t = e.result;
                                            if (a && (a.res = t), i) {
                                                for (var n = 0, r = t.length; n < r; ++n) Object.freeze(t[n]);
                                                Object.freeze(t);
                                            } else e.result = S(t);
                                            return e;
                                        })).catch((function(e) {
                                            return s && a && q(s, a), Promise.reject(e);
                                        })), a = {
                                            obsSet: t.obsSet,
                                            promise: e,
                                            subscribers: new Set,
                                            type: "query",
                                            req: t,
                                            dirty: false
                                        }, s ? s.push(a) : (s = [ a ], (u = u || (Sn["idb://".concat(O, "/").concat(c)] = {
                                            queries: {
                                                query: {},
                                                count: {}
                                            },
                                            objs: new Map,
                                            optimisticOps: [],
                                            unsignaledParts: {}
                                        })).queries.query[t.query.index.name || ""] = s)), Hn(a, s, n, r), a.promise.then((function(e) {
                                            return {
                                                result: Qn(e.result, t, null == u ? void 0 : u.optimisticOps, l, a, i)
                                            };
                                        }));
                                    }
                                });
                            }
                        });
                    }
                };
                function Zn(e, r) {
                    return new Proxy(e, {
                        get: function(e, t, n) {
                            return "db" === t ? r : Reflect.get(e, t, n);
                        }
                    });
                }
                var er = (tr.prototype.version = function(t) {
                    if (isNaN(t) || t < .1) throw new Y.Type("Given version is not a positive number");
                    if (t = Math.round(10 * t) / 10, this.idbdb || this._state.isBeingOpened) throw new Y.Schema("Cannot add version when database is open");
                    this.verno = Math.max(this.verno, t);
                    var e = this._versions, n = e.filter((function(e) {
                        return e._cfg.version === t;
                    }))[0];
                    return n || (n = new this.Version(t), e.push(n), e.sort(nn), n.stores({}), this._state.autoSchema = false, 
                    n);
                }, tr.prototype._whenReady = function(e) {
                    var n = this;
                    return this.idbdb && (this._state.openComplete || me.letThrough || this._vip) ? e() : new _e((function(e, t) {
                        if (n._state.openComplete) return t(new Y.DatabaseClosed(n._state.dbOpenError));
                        if (!n._state.isBeingOpened) {
                            if (!n._state.autoOpen) return void t(new Y.DatabaseClosed);
                            n.open().catch(G);
                        }
                        n._state.dbReadyPromise.then(e, t);
                    })).then(e);
                }, tr.prototype.use = function(e) {
                    var t = e.stack, n = e.create, r = e.level, i = e.name;
                    i && this.unuse({
                        stack: t,
                        name: i
                    });
                    e = this._middlewares[t] || (this._middlewares[t] = []);
                    return e.push({
                        stack: t,
                        create: n,
                        level: null == r ? 10 : r,
                        name: i
                    }), e.sort((function(e, t) {
                        return e.level - t.level;
                    })), this;
                }, tr.prototype.unuse = function(e) {
                    var t = e.stack, n = e.name, r = e.create;
                    return t && this._middlewares[t] && (this._middlewares[t] = this._middlewares[t].filter((function(e) {
                        return r ? e.create !== r : !!n && e.name !== n;
                    }))), this;
                }, tr.prototype.open = function() {
                    var e = this;
                    return $e(ve, (function() {
                        return Dn(e);
                    }));
                }, tr.prototype._close = function() {
                    var n = this._state, e = et.indexOf(this);
                    if (0 <= e && et.splice(e, 1), this.idbdb) {
                        try {
                            this.idbdb.close();
                        } catch (e) {}
                        this.idbdb = null;
                    }
                    n.isBeingOpened || (n.dbReadyPromise = new _e((function(e) {
                        n.dbReadyResolve = e;
                    })), n.openCanceller = new _e((function(e, t) {
                        n.cancelOpen = t;
                    })));
                }, tr.prototype.close = function(e) {
                    var t = (void 0 === e ? {
                        disableAutoOpen: true
                    } : e).disableAutoOpen, e = this._state;
                    t ? (e.isBeingOpened && e.cancelOpen(new Y.DatabaseClosed), this._close(), e.autoOpen = false, 
                    e.dbOpenError = new Y.DatabaseClosed) : (this._close(), e.autoOpen = this._options.autoOpen || e.isBeingOpened, 
                    e.openComplete = false, e.dbOpenError = null);
                }, tr.prototype.delete = function(n) {
                    var i = this;
                    void 0 === n && (n = {
                        disableAutoOpen: true
                    });
                    var o = 0 < arguments.length && "object" != typeof arguments[0], a = this._state;
                    return new _e((function(r, t) {
                        function e() {
                            i.close(n);
                            var e = i._deps.indexedDB.deleteDatabase(i.name);
                            e.onsuccess = qe((function() {
                                var e, t, n;
                                e = i._deps, t = i.name, n = e.indexedDB, e = e.IDBKeyRange, vn(n) || t === tt || yn(n, e).delete(t).catch(G), 
                                r();
                            })), e.onerror = Bt(t), e.onblocked = i._fireOnBlocked;
                        }
                        if (o) throw new Y.InvalidArgument("Invalid closeOptions argument to db.delete()");
                        a.isBeingOpened ? a.dbReadyPromise.then(e) : e();
                    }));
                }, tr.prototype.backendDB = function() {
                    return this.idbdb;
                }, tr.prototype.isOpen = function() {
                    return null !== this.idbdb;
                }, tr.prototype.hasBeenClosed = function() {
                    var e = this._state.dbOpenError;
                    return e && "DatabaseClosed" === e.name;
                }, tr.prototype.hasFailed = function() {
                    return null !== this._state.dbOpenError;
                }, tr.prototype.dynamicallyOpened = function() {
                    return this._state.autoSchema;
                }, Object.defineProperty(tr.prototype, "tables", {
                    get: function() {
                        var t = this;
                        return x(this._allTables).map((function(e) {
                            return t._allTables[e];
                        }));
                    },
                    enumerable: false,
                    configurable: true
                }), tr.prototype.transaction = function() {
                    var e = function(e, t, n) {
                        var r = arguments.length;
                        if (r < 2) throw new Y.InvalidArgument("Too few arguments");
                        for (var i = new Array(r - 1); --r; ) i[r - 1] = arguments[r];
                        return n = i.pop(), [ e, w(i), n ];
                    }.apply(this, arguments);
                    return this._transaction.apply(this, e);
                }, tr.prototype._transaction = function(e, t, n) {
                    var r = this, i = me.trans;
                    i && i.db === this && -1 === e.indexOf("!") || (i = null);
                    var o, a, u = -1 !== e.indexOf("?");
                    e = e.replace("!", "").replace("?", "");
                    try {
                        if (a = t.map((function(e) {
                            e = e instanceof r.Table ? e.name : e;
                            if ("string" != typeof e) throw new TypeError("Invalid table argument to Dexie.transaction(). Only Table or String are allowed");
                            return e;
                        })), "r" == e || e === nt) o = nt; else {
                            if ("rw" != e && e != rt) throw new Y.InvalidArgument("Invalid transaction mode: " + e);
                            o = rt;
                        }
                        if (i) {
                            if (i.mode === nt && o === rt) {
                                if (!u) throw new Y.SubTransaction("Cannot enter a sub-transaction with READWRITE mode when parent transaction is READONLY");
                                i = null;
                            }
                            i && a.forEach((function(e) {
                                if (i && -1 === i.storeNames.indexOf(e)) {
                                    if (!u) throw new Y.SubTransaction("Table " + e + " not included in parent transaction.");
                                    i = null;
                                }
                            })), u && i && !i.active && (i = null);
                        }
                    } catch (n) {
                        return i ? i._promise(null, (function(e, t) {
                            t(n);
                        })) : Xe(n);
                    }
                    var s = function i(o, a, u, s, c) {
                        return _e.resolve().then((function() {
                            var e = me.transless || me, t = o._createTransaction(a, u, o._dbSchema, s);
                            if (t.explicit = true, e = {
                                trans: t,
                                transless: e
                            }, s) t.idbtrans = s.idbtrans; else try {
                                t.create(), t.idbtrans._explicit = !0, o._state.PR1398_maxLoop = 3;
                            } catch (e) {
                                return e.name === z.InvalidState && o.isOpen() && 0 < --o._state.PR1398_maxLoop ? (console.warn("Dexie: Need to reopen db"), 
                                o.close({
                                    disableAutoOpen: false
                                }), o.open().then((function() {
                                    return i(o, a, u, null, c);
                                }))) : Xe(e);
                            }
                            var n, r = B(c);
                            return r && Le(), e = _e.follow((function() {
                                var e;
                                (n = c.call(t, t)) && (r ? (e = Ue.bind(null, null), n.then(e, e)) : "function" == typeof n.next && "function" == typeof n.throw && (n = In(n)));
                            }), e), (n && "function" == typeof n.then ? _e.resolve(n).then((function(e) {
                                return t.active ? e : Xe(new Y.PrematureCommit("Transaction committed too early. See http://bit.ly/2kdckMn"));
                            })) : e.then((function() {
                                return n;
                            }))).then((function(e) {
                                return s && t._resolve(), t._completion.then((function() {
                                    return e;
                                }));
                            })).catch((function(e) {
                                return t._reject(e), Xe(e);
                            }));
                        }));
                    }.bind(null, this, o, a, i, n);
                    return i ? i._promise(o, s, "lock") : me.trans ? $e(me.transless, (function() {
                        return r._whenReady(s);
                    })) : this._whenReady(s);
                }, tr.prototype.table = function(e) {
                    if (!m(this._allTables, e)) throw new Y.InvalidTable("Table ".concat(e, " does not exist"));
                    return this._allTables[e];
                }, tr);
                function tr(e, t) {
                    var o = this;
                    this._middlewares = {}, this.verno = 0;
                    var n = tr.dependencies;
                    this._options = t = _({
                        addons: tr.addons,
                        autoOpen: true,
                        indexedDB: n.indexedDB,
                        IDBKeyRange: n.IDBKeyRange,
                        cache: "cloned"
                    }, t), this._deps = {
                        indexedDB: t.indexedDB,
                        IDBKeyRange: t.IDBKeyRange
                    };
                    n = t.addons;
                    this._dbSchema = {}, this._versions = [], this._storeNames = [], this._allTables = {}, 
                    this.idbdb = null, this._novip = this;
                    var a, r, u, i, s, c = {
                        dbOpenError: null,
                        isBeingOpened: false,
                        onReadyBeingFired: null,
                        openComplete: false,
                        dbReadyResolve: G,
                        dbReadyPromise: null,
                        cancelOpen: G,
                        openCanceller: null,
                        autoSchema: true,
                        PR1398_maxLoop: 3,
                        autoOpen: t.autoOpen
                    };
                    c.dbReadyPromise = new _e((function(e) {
                        c.dbReadyResolve = e;
                    })), c.openCanceller = new _e((function(e, t) {
                        c.cancelOpen = t;
                    })), this._state = c, this.name = e, this.on = dt(this, "populate", "blocked", "versionchange", "close", {
                        ready: [ re, G ]
                    }), this.on.ready.subscribe = p(this.on.ready.subscribe, (function(i) {
                        return function(n, r) {
                            tr.vip((function() {
                                var t, e = o._state;
                                e.openComplete ? (e.dbOpenError || _e.resolve().then(n), r && i(n)) : e.onReadyBeingFired ? (e.onReadyBeingFired.push(n), 
                                r && i(n)) : (i(n), t = o, r || i((function e() {
                                    t.on.ready.unsubscribe(n), t.on.ready.unsubscribe(e);
                                })));
                            }));
                        };
                    })), this.Collection = (a = this, pt(Ot.prototype, (function(e, t) {
                        this.db = a;
                        var n = ot, r = null;
                        if (t) try {
                            n = t();
                        } catch (e) {
                            r = e;
                        }
                        var i = e._ctx, t = i.table, e = t.hook.reading.fire;
                        this._ctx = {
                            table: t,
                            index: i.index,
                            isPrimKey: !i.index || t.schema.primKey.keyPath && i.index === t.schema.primKey.name,
                            range: n,
                            keysOnly: false,
                            dir: "next",
                            unique: "",
                            algorithm: null,
                            filter: null,
                            replayFilter: null,
                            justLimit: true,
                            isMatch: null,
                            offset: 0,
                            limit: 1 / 0,
                            error: r,
                            or: i.or,
                            valueMapper: e !== X ? e : null
                        };
                    }))), this.Table = (r = this, pt(ft.prototype, (function(e, t, n) {
                        this.db = r, this._tx = n, this.name = e, this.schema = t, this.hook = r._allTables[e] ? r._allTables[e].hook : dt(null, {
                            creating: [ Z, G ],
                            reading: [ H, X ],
                            updating: [ te, G ],
                            deleting: [ ee, G ]
                        });
                    }))), this.Transaction = (u = this, pt(Lt.prototype, (function(e, t, n, r, i) {
                        var o = this;
                        this.db = u, this.mode = e, this.storeNames = t, this.schema = n, this.chromeTransactionDurability = r, 
                        this.idbtrans = null, this.on = dt(this, "complete", "error", "abort"), this.parent = i || null, 
                        this.active = true, this._reculock = 0, this._blockedFuncs = [], this._resolve = null, 
                        this._reject = null, this._waitingFor = null, this._waitingQueue = null, this._spinCount = 0, 
                        this._completion = new _e((function(e, t) {
                            o._resolve = e, o._reject = t;
                        })), this._completion.then((function() {
                            o.active = false, o.on.complete.fire();
                        }), (function(e) {
                            var t = o.active;
                            return o.active = false, o.on.error.fire(e), o.parent ? o.parent._reject(e) : t && o.idbtrans && o.idbtrans.abort(), 
                            Xe(e);
                        }));
                    }))), this.Version = (i = this, pt(dn.prototype, (function(e) {
                        this.db = i, this._cfg = {
                            version: e,
                            storesSource: null,
                            dbschema: {},
                            tables: {},
                            contentUpgrade: null
                        };
                    }))), this.WhereClause = (s = this, pt(Dt.prototype, (function(e, t, n) {
                        if (this.db = s, this._ctx = {
                            table: e,
                            index: ":id" === t ? null : t,
                            or: n
                        }, this._cmp = this._ascending = st, this._descending = function(e, t) {
                            return st(t, e);
                        }, this._max = function(e, t) {
                            return 0 < st(e, t) ? e : t;
                        }, this._min = function(e, t) {
                            return st(e, t) < 0 ? e : t;
                        }, this._IDBKeyRange = s._deps.IDBKeyRange, !this._IDBKeyRange) throw new Y.MissingAPI;
                    }))), this.on("versionchange", (function(e) {
                        0 < e.newVersion ? console.warn("Another connection wants to upgrade database '".concat(o.name, "'. Closing db now to resume the upgrade.")) : console.warn("Another connection wants to delete database '".concat(o.name, "'. Closing db now to resume the delete request.")), 
                        o.close({
                            disableAutoOpen: false
                        });
                    })), this.on("blocked", (function(e) {
                        !e.newVersion || e.newVersion < e.oldVersion ? console.warn("Dexie.delete('".concat(o.name, "') was blocked")) : console.warn("Upgrade '".concat(o.name, "' blocked by other connection holding version ").concat(e.oldVersion / 10));
                    })), this._maxKey = Yt(t.IDBKeyRange), this._createTransaction = function(e, t, n, r) {
                        return new o.Transaction(e, t, n, o._options.chromeTransactionDurability, r);
                    }, this._fireOnBlocked = function(t) {
                        o.on("blocked").fire(t), et.filter((function(e) {
                            return e.name === o.name && e !== o && !e._state.vcFired;
                        })).map((function(e) {
                            return e.on("versionchange").fire(t);
                        }));
                    }, this.use(Un), this.use(Jn), this.use(Wn), this.use(Rn), this.use(Nn);
                    var l = new Proxy(this, {
                        get: function(e, t, n) {
                            if ("_vip" === t) return true;
                            if ("table" === t) return function(e) {
                                return Zn(o.table(e), l);
                            };
                            var r = Reflect.get(e, t, n);
                            return r instanceof ft ? Zn(r, l) : "tables" === t ? r.map((function(e) {
                                return Zn(e, l);
                            })) : "_createTransaction" === t ? function() {
                                return Zn(r.apply(this, arguments), l);
                            } : r;
                        }
                    });
                    this.vip = l, n.forEach((function(e) {
                        return e(o);
                    }));
                }
                var nr, F = "undefined" != typeof Symbol && "observable" in Symbol ? Symbol.observable : "@@observable", rr = (ir.prototype.subscribe = function(e, t, n) {
                    return this._subscribe(e && "function" != typeof e ? e : {
                        next: e,
                        error: t,
                        complete: n
                    });
                }, ir.prototype[F] = function() {
                    return this;
                }, ir);
                function ir(e) {
                    this._subscribe = e;
                }
                try {
                    nr = {
                        indexedDB: f.indexedDB || f.mozIndexedDB || f.webkitIndexedDB || f.msIndexedDB,
                        IDBKeyRange: f.IDBKeyRange || f.webkitIDBKeyRange
                    };
                } catch (e) {
                    nr = {
                        indexedDB: null,
                        IDBKeyRange: null
                    };
                }
                function or(h) {
                    var d, p = false, e = new rr((function(r) {
                        var i = B(h);
                        var o, a = false, u = {}, s = {}, e = {
                            get closed() {
                                return a;
                            },
                            unsubscribe: function() {
                                a || (a = true, o && o.abort(), c && Nt.storagemutated.unsubscribe(f));
                            }
                        };
                        r.start && r.start(e);
                        var c = false, l = function() {
                            return Ge(t);
                        };
                        var f = function(e) {
                            Kn(u, e), En(s, u) && l();
                        }, t = function() {
                            var t, n, e;
                            !a && nr.indexedDB && (u = {}, t = {}, o && o.abort(), o = new AbortController, 
                            e = function(e) {
                                var t = je();
                                try {
                                    i && Le();
                                    var n = Ne(h, e);
                                    return n = i ? n.finally(Ue) : n;
                                } finally {
                                    t && Ae();
                                }
                            }(n = {
                                subscr: t,
                                signal: o.signal,
                                requery: l,
                                querier: h,
                                trans: null
                            }), Promise.resolve(e).then((function(e) {
                                p = true, d = e, a || n.signal.aborted || (u = {}, function(e) {
                                    for (var t in e) if (m(e, t)) return;
                                    return 1;
                                }(s = t) || c || (Nt(Ft, f), c = true), Ge((function() {
                                    return !a && r.next && r.next(e);
                                })));
                            }), (function(e) {
                                p = false, [ "DatabaseClosedError", "AbortError" ].includes(null == e ? void 0 : e.name) || a || Ge((function() {
                                    a || r.error && r.error(e);
                                }));
                            })));
                        };
                        return setTimeout(l, 0), e;
                    }));
                    return e.hasValue = function() {
                        return p;
                    }, e.getValue = function() {
                        return d;
                    }, e;
                }
                var ar = er;
                function ur(e) {
                    var t = cr;
                    try {
                        cr = !0, Nt.storagemutated.fire(e), Tn(e, !0);
                    } finally {
                        cr = t;
                    }
                }
                r(ar, _(_({}, Q), {
                    delete: function(e) {
                        return new ar(e, {
                            addons: []
                        }).delete();
                    },
                    exists: function(e) {
                        return new ar(e, {
                            addons: []
                        }).open().then((function(e) {
                            return e.close(), true;
                        })).catch("NoSuchDatabaseError", (function() {
                            return false;
                        }));
                    },
                    getDatabaseNames: function(e) {
                        try {
                            return t = ar.dependencies, n = t.indexedDB, t = t.IDBKeyRange, (vn(n) ? Promise.resolve(n.databases()).then((function(e) {
                                return e.map((function(e) {
                                    return e.name;
                                })).filter((function(e) {
                                    return e !== tt;
                                }));
                            })) : yn(n, t).toCollection().primaryKeys()).then(e);
                        } catch (e) {
                            return Xe(new Y.MissingAPI);
                        }
                        var t, n;
                    },
                    defineClass: function() {
                        return function(e) {
                            a(this, e);
                        };
                    },
                    ignoreTransaction: function(e) {
                        return me.trans ? $e(me.transless, e) : e();
                    },
                    vip: mn,
                    async: function(t) {
                        return function() {
                            try {
                                var e = In(t.apply(this, arguments));
                                return e && "function" == typeof e.then ? e : _e.resolve(e);
                            } catch (e) {
                                return Xe(e);
                            }
                        };
                    },
                    spawn: function(e, t, n) {
                        try {
                            var r = In(e.apply(n, t || []));
                            return r && "function" == typeof r.then ? r : _e.resolve(r);
                        } catch (e) {
                            return Xe(e);
                        }
                    },
                    currentTransaction: {
                        get: function() {
                            return me.trans || null;
                        }
                    },
                    waitFor: function(e, t) {
                        t = _e.resolve("function" == typeof e ? ar.ignoreTransaction(e) : e).timeout(t || 6e4);
                        return me.trans ? me.trans.waitFor(t) : t;
                    },
                    Promise: _e,
                    debug: {
                        get: function() {
                            return ie;
                        },
                        set: function(e) {
                            oe(e);
                        }
                    },
                    derive: o,
                    extend: a,
                    props: r,
                    override: p,
                    Events: dt,
                    on: Nt,
                    liveQuery: or,
                    extendObservabilitySet: Kn,
                    getByKeyPath: O,
                    setByKeyPath: P,
                    delByKeyPath: function(t, e) {
                        "string" == typeof e ? P(t, e, void 0) : "length" in e && [].map.call(e, (function(e) {
                            P(t, e, void 0);
                        }));
                    },
                    shallowClone: g,
                    deepClone: S,
                    getObjectDiff: Fn,
                    cmp: st,
                    asap: v,
                    minKey: -1 / 0,
                    addons: [],
                    connections: et,
                    errnames: z,
                    dependencies: nr,
                    cache: Sn,
                    semVer: "4.0.11",
                    version: "4.0.11".split(".").map((function(e) {
                        return parseInt(e);
                    })).reduce((function(e, t, n) {
                        return e + t / Math.pow(10, 2 * n);
                    }))
                })), ar.maxKey = Yt(ar.dependencies.IDBKeyRange), "undefined" != typeof dispatchEvent && "undefined" != typeof addEventListener && (Nt(Ft, (function(e) {
                    cr || (e = new CustomEvent(Mt, {
                        detail: e
                    }), cr = true, dispatchEvent(e), cr = false);
                })), addEventListener(Mt, (function(e) {
                    e = e.detail;
                    cr || ur(e);
                })));
                var sr, cr = false, lr = function() {};
                return "undefined" != typeof BroadcastChannel && ((lr = function() {
                    (sr = new BroadcastChannel(Mt)).onmessage = function(e) {
                        return e.data && ur(e.data);
                    };
                })(), "function" == typeof sr.unref && sr.unref(), Nt(Ft, (function(e) {
                    cr || sr.postMessage(e);
                }))), "undefined" != typeof addEventListener && (addEventListener("pagehide", (function(e) {
                    if (!er.disableBfCache && e.persisted) {
                        ie && console.debug("Dexie: handling persisted pagehide"), null != sr && sr.close();
                        for (var t = 0, n = et; t < n.length; t++) n[t].close({
                            disableAutoOpen: false
                        });
                    }
                })), addEventListener("pageshow", (function(e) {
                    !er.disableBfCache && e.persisted && (ie && console.debug("Dexie: handling persisted pageshow"), 
                    lr(), ur({
                        all: new gn(-1 / 0, [ [] ])
                    }));
                }))), _e.rejectionMapper = function(e, t) {
                    return !e || e instanceof N || e instanceof TypeError || e instanceof SyntaxError || !e.name || !$[e.name] ? e : (t = new $[e.name](t || e.message, e), 
                    "stack" in e && l(t, "stack", {
                        get: function() {
                            return this.inner.stack;
                        }
                    }), t);
                }, oe(ie), _(er, Object.freeze({
                    __proto__: null,
                    Dexie: er,
                    liveQuery: or,
                    Entity: ut,
                    cmp: st,
                    PropModification: xt,
                    replacePrefix: function(e, t) {
                        return new xt({
                            replacePrefix: [ e, t ]
                        });
                    },
                    add: function(e) {
                        return new xt({
                            add: e
                        });
                    },
                    remove: function(e) {
                        return new xt({
                            remove: e
                        });
                    },
                    default: er,
                    RangeSet: gn,
                    mergeRanges: _n,
                    rangesOverlap: xn
                }), {
                    default: er
                }), er;
            }));
        })(dexie_min$1);
        return dexie_min$1.exports;
    }
    var dexie_minExports = requireDexie_min();
    var _Dexie = getDefaultExportFromCjs(dexie_minExports);
    const DexieSymbol = Symbol.for("Dexie");
    const Dexie = globalThis[DexieSymbol] || (globalThis[DexieSymbol] = _Dexie);
    if (_Dexie.semVer !== Dexie.semVer) {
        throw new Error(`Two different versions of Dexie loaded in the same app: ${_Dexie.semVer} and ${Dexie.semVer}`);
    }
    const {liveQuery: liveQuery, mergeRanges: mergeRanges, rangesOverlap: rangesOverlap, RangeSet: RangeSet, cmp: cmp, Entity: Entity, PropModification: PropModification, replacePrefix: replacePrefix, add: add, remove: remove} = Dexie;
    let INDEXES = [];
    const db = new Dexie("docsify");
    db.version(1).stores({
        search: "slug, title, body, path, indexKey",
        expires: "key, value"
    });
    async function saveData(maxAge, expireKey) {
        const records = [];
        Object.values(INDEXES).forEach((entry => {
            if (!entry || typeof entry !== "object") {
                return;
            }
            if ("slug" in entry) {
                records.push(entry);
                return;
            }
            Object.values(entry).forEach((item => {
                if (item && typeof item === "object" && "slug" in item) {
                    records.push(item);
                }
            }));
        }));
        INDEXES = records;
        await db.search.bulkPut(records);
        await db.expires.put({
            key: expireKey,
            value: Date.now() + maxAge
        });
    }
    async function getData(key, isExpireKey = false) {
        if (isExpireKey) {
            const item = await db.expires.get(key);
            return item ? item.value : 0;
        }
        const item = await db.search.where({
            indexKey: key
        }).toArray();
        return item ? item : null;
    }
    const LOCAL_STORAGE = {
        EXPIRE_KEY: "docsify.search.expires",
        INDEX_KEY: "docsify.search.index"
    };
    function resolveExpireKey(namespace) {
        return namespace ? `${LOCAL_STORAGE.EXPIRE_KEY}/${namespace}` : LOCAL_STORAGE.EXPIRE_KEY;
    }
    function resolveIndexKey(namespace) {
        return namespace ? `${LOCAL_STORAGE.INDEX_KEY}/${namespace}` : LOCAL_STORAGE.INDEX_KEY;
    }
    function getAllPaths(router) {
        const paths = [];
        Docsify.dom.findAll(".sidebar-nav a:not(.section-link):not([data-nosearch])").forEach((node => {
            const href = node.href;
            const originHref = node.getAttribute("href");
            const path = router.parse(href).path;
            if (path && paths.indexOf(path) === -1 && !Docsify.util.isAbsolutePath(originHref)) {
                paths.push(path);
            }
        }));
        return paths;
    }
    function getTableData(token) {
        if (!token.text && token.type === "table") {
            token.rows.unshift(token.header);
            token.text = token.rows.map((columns => columns.map((r => r.text)).join(" | "))).join(" |\n ");
        }
        return token.text;
    }
    function getListData(token) {
        if (!token.text && token.type === "list") {
            token.text = token.raw;
        }
        return token.text;
    }
    function extractFragmentContent(text, fragment, fullLine) {
        if (!fragment) {
            return text;
        }
        let fragmentRegex = `(?:###|\\/\\/\\/)\\s*\\[${fragment}\\]`;
        if (fullLine) {
            fragmentRegex = `.*${fragmentRegex}.*\n`;
        }
        const pattern = new RegExp(`(?:${fragmentRegex})([\\s\\S]*?)(?:${fragmentRegex})`);
        const match = text.match(pattern);
        return ((match || [])[1] || "").trim();
    }
    function collectEmbedRequests(raw = "", path, vm) {
        const tokens = window.marked.lexer(raw);
        const requests = [];
        const maybePushEmbed = inlineToken => {
            if (!inlineToken || inlineToken.type !== "link" && inlineToken.type !== "image") {
                return;
            }
            const {config: config} = getAndRemoveConfig(inlineToken.title || "");
            if (!config.include || !inlineToken.href) {
                return;
            }
            const href = isAbsolutePath(inlineToken.href) ? inlineToken.href : getPath(vm.router.getBasePath(), getParentPath(path), inlineToken.href);
            let type = "code";
            if (/\.(md|markdown)/.test(href)) {
                type = "markdown";
            } else if (/\.mmd/.test(href)) {
                type = "mermaid";
            }
            requests.push({
                url: href,
                type: type,
                fragment: config.fragment,
                omitFragmentLine: config.omitFragmentLine
            });
        };
        tokens.forEach((token => {
            if (token.type === "paragraph") {
                (token.tokens || []).forEach(maybePushEmbed);
            } else if (token.type === "table") {
                (token.header || []).forEach((cell => {
                    (cell.tokens || []).forEach(maybePushEmbed);
                }));
                (token.rows || []).forEach((row => {
                    row.forEach((cell => {
                        (cell.tokens || []).forEach(maybePushEmbed);
                    }));
                }));
            }
        }));
        return requests;
    }
    async function getEmbeddedContent(raw = "", path, vm) {
        const requests = collectEmbedRequests(raw, path, vm);
        if (!requests.length) {
            return "";
        }
        const results = await Promise.all(requests.map((request => new Promise((resolve => {
            Docsify.get(request.url, false, vm.config.requestHeaders).then((text => {
                let content = text || "";
                if (request.fragment) {
                    content = extractFragmentContent(content, request.fragment, request.omitFragmentLine);
                }
                resolve(request.type === "markdown" ? content : markdownToTxt(content));
            }), (() => resolve("")));
        })))));
        return results.filter(Boolean).join("\n");
    }
    function genIndex(path, content = "", router, depth, indexKey) {
        const tokens = window.marked.lexer(content);
        const slugify = window.Docsify.slugify;
        const index = {};
        let slug;
        let title = "";
        let pageTitle = "";
        tokens.forEach(((token, tokenIndex) => {
            if (token.type === "heading" && token.depth <= depth) {
                const {str: str, config: config} = getAndRemoveConfig(token.text);
                slug = router.toURL(path, {
                    id: slugify(config.id || token.text)
                });
                if (str) {
                    title = getAndRemoveDocsifyIgnoreConfig(str).content;
                    title = removeAtag(title.trim());
                }
                if (!pageTitle && title) {
                    pageTitle = title;
                }
                index[slug] = {
                    slug: slug,
                    title: title,
                    body: "",
                    path: path,
                    indexKey: indexKey
                };
            } else {
                if (tokenIndex === 0) {
                    slug = router.toURL(path);
                    index[slug] = {
                        slug: slug,
                        title: path !== "/" ? path.slice(1) : "Home Page",
                        body: markdownToTxt(token.text || ""),
                        path: path,
                        indexKey: indexKey
                    };
                }
                if (!slug) {
                    return;
                }
                if (!index[slug]) {
                    index[slug] = {
                        slug: slug,
                        title: "",
                        body: ""
                    };
                } else if (index[slug].body) {
                    token.text = getTableData(token);
                    token.text = getListData(token);
                    index[slug].body += "\n" + markdownToTxt(token.text || "");
                } else {
                    token.text = getTableData(token);
                    token.text = getListData(token);
                    index[slug].body = markdownToTxt(token.text || "");
                }
                index[slug].path = path;
                index[slug].indexKey = indexKey;
            }
        }));
        slugify.clear();
        Object.values(index).forEach((item => {
            item.pageTitle = pageTitle;
        }));
        return index;
    }
    function ignoreDiacriticalMarks(keyword) {
        if (keyword && keyword.normalize) {
            return keyword.normalize("NFD").replace(/[\u0300-\u036f]/g, "");
        }
        return keyword;
    }
    function search(query) {
        const matchingResults = [];
        query = query.trim();
        let keywords = query.split(/[\s\-，\\/]+/);
        if (keywords.length !== 1) {
            keywords = [ query, ...keywords ];
        }
        for (const post of INDEXES) {
            let matchesScore = 0;
            let resultStr = "";
            let handlePostTitle = "";
            let handlePostContent = "";
            const postTitle = post.title && post.title.trim();
            const postContent = post.body && post.body.trim();
            const postUrl = post.slug || "";
            if (postTitle) {
                keywords.forEach((keyword => {
                    const regEx = new RegExp(escapeHtml(ignoreDiacriticalMarks(keyword)).replace(/[|\\{}()[\]^$+*?.]/g, "\\$&"), "gi");
                    handlePostTitle = postTitle ? escapeHtml(ignoreDiacriticalMarks(postTitle)) : postTitle;
                    handlePostContent = postContent ? escapeHtml(ignoreDiacriticalMarks(postContent)) : postContent;
                    const indexTitle = postTitle ? handlePostTitle.search(regEx) : -1;
                    let indexContent = postContent ? handlePostContent.search(regEx) : -1;
                    if (indexTitle >= 0 || indexContent >= 0) {
                        matchesScore += indexTitle >= 0 ? 3 : indexContent >= 0 ? 2 : 0;
                        if (indexContent < 0) {
                            indexContent = 0;
                        }
                        const start = indexContent < 11 ? 0 : indexContent - 10;
                        let end = start === 0 ? 100 : indexContent + keyword.length + 90;
                        if (handlePostContent && end > handlePostContent.length) {
                            end = handlePostContent.length;
                        }
                        const matchContent = handlePostContent && handlePostContent.substring(start, end).replace(regEx, (word => `<mark>${word}</mark>`));
                        resultStr += matchContent;
                    }
                }));
                if (matchesScore > 0) {
                    const postPageTitle = post.pageTitle && post.pageTitle.trim();
                    const matchingPost = {
                        title: handlePostTitle,
                        content: postContent ? resultStr : "",
                        url: postUrl,
                        score: matchesScore,
                        page: postPageTitle ? escapeHtml(ignoreDiacriticalMarks(postPageTitle)) : ""
                    };
                    matchingResults.push(matchingPost);
                }
            }
        }
        return matchingResults.sort(((r1, r2) => r2.score - r1.score));
    }
    async function init$1(config, vm) {
        const isAuto = config.paths === "auto";
        const paths = isAuto ? getAllPaths(vm.router) : config.paths;
        let namespaceSuffix = "";
        if (paths.length && isAuto && config.pathNamespaces) {
            const path = paths[0];
            if (Array.isArray(config.pathNamespaces)) {
                namespaceSuffix = config.pathNamespaces.filter((prefix => path.slice(0, prefix.length) === prefix))[0] || namespaceSuffix;
            } else if (config.pathNamespaces instanceof RegExp) {
                const matches = path.match(config.pathNamespaces);
                if (matches) {
                    namespaceSuffix = matches[0];
                }
            }
            const isExistHome = paths.indexOf(namespaceSuffix + "/") === -1;
            const isExistReadme = paths.indexOf(namespaceSuffix + "/README") === -1;
            if (isExistHome && isExistReadme) {
                paths.unshift(namespaceSuffix + "/");
            }
        } else if (paths.indexOf("/") === -1 && paths.indexOf("/README") === -1) {
            paths.unshift("/");
        }
        const expireKey = resolveExpireKey(config.namespace) + namespaceSuffix;
        const indexKey = resolveIndexKey(config.namespace) + namespaceSuffix;
        const isExpired = await getData(expireKey, true) < Date.now();
        INDEXES = await getData(indexKey);
        if (isExpired) {
            INDEXES = [];
        } else if (!isAuto) {
            return;
        }
        const len = paths.length;
        let count = 0;
        const markComplete = async () => {
            if (len === ++count) {
                await saveData(config.maxAge, expireKey);
            }
        };
        paths.forEach((path => {
            const pathExists = Array.isArray(INDEXES) ? INDEXES.some((obj => obj.path === path)) : false;
            if (pathExists) {
                void markComplete();
                return;
            }
            Docsify.get(vm.router.getFile(path), false, vm.config.requestHeaders).then((async result => {
                const embeddedContent = await getEmbeddedContent(result, path, vm);
                const contentToIndex = embeddedContent ? `${result}\n${embeddedContent}` : result;
                INDEXES[path] = genIndex(path, contentToIndex, vm.router, config.depth, indexKey);
                return markComplete();
            }), (() => markComplete()));
        }));
    }
    var cssText = "/* prettier-ignore */\n:root {\n  --plugin-search-input-bg           : var(--form-element-bg);\n  --plugin-search-input-border-color : var(--sidebar-border-color);\n  --plugin-search-input-border-radius: var(--form-element-border-radius);\n  --plugin-search-input-color        : var(--form-element-color);\n  --plugin-search-kbd-bg             : var(--color-bg);\n  --plugin-search-kbd-border         : 1px solid var(--color-mono-3);\n  --plugin-search-kbd-border-radius  : 4px;\n  --plugin-search-kbd-color          : var(--color-mono-5);\n  --plugin-search-margin             : 10px;\n  --plugin-search-reset-bg           : var(--theme-color);\n  --plugin-search-reset-border       : transparent;\n  --plugin-search-reset-border-radius: var(--border-radius);\n  --plugin-search-reset-color        : #fff;\n}\n\n.search {\n  margin: var(--plugin-search-margin);\n}\n\n/* Input */\n/* ================================== */\n.search .input-wrap {\n  position: relative;\n}\n\n.search input {\n  width: 100%;\n  padding-inline-end: 36px;\n  border: 1px solid var(--plugin-search-input-border-color);\n  border-radius: var(--plugin-search-input-border-radius);\n  background: var(--plugin-search-input-bg);\n  color: var(--plugin-search-input-color);\n}\n\n.search input::-webkit-search-decoration,\n.search input::-webkit-search-cancel-button {\n  appearance: none;\n}\n\n.search .clear-button,\n.search .kbd-group {\n  visibility: hidden;\n  display: flex;\n  gap: 0.15em;\n  position: absolute;\n  right: 7px;\n  top: 50%;\n  opacity: 0;\n  translate: 0 -50%;\n  transition-property: opacity, visibility;\n  transition-duration: var(--duration-medium);\n}\n\n/* Note: invalid = empty, valid = not empty */\n.search input:valid ~ .clear-button,\n.search input:invalid:where(:focus, :hover) ~ .kbd-group,\n.search .kbd-group:hover {\n  visibility: visible;\n  opacity: 1;\n}\n\n.search .clear-button {\n  --_button-size: 20px;\n  --_content-size: 12px;\n\n  display: flex;\n  align-items: center;\n  justify-content: center;\n  height: var(--_button-size);\n  width: var(--_button-size);\n  border: var(--plugin-search-reset-border);\n  border-radius: var(--plugin-search-reset-border-radius);\n  background: var(--plugin-search-reset-bg);\n  cursor: pointer;\n}\n\n.search .clear-button::before,\n.search .clear-button::after {\n  content: '';\n  position: absolute;\n  height: 2px;\n  width: var(--_content-size);\n  color: var(--plugin-search-reset-color);\n  background: var(--plugin-search-reset-color);\n}\n\n.search .clear-button::before {\n  rotate: 45deg;\n}\n\n.search .clear-button::after {\n  rotate: -45deg;\n}\n\n.search kbd {\n  border: var(--plugin-search-kbd-border);\n  border-radius: var(--plugin-search-kbd-border-radius);\n  background: var(--plugin-search-kbd-bg);\n  color: var(--plugin-search-kbd-color);\n  font-size: var(--font-size-s);\n}\n\n/* Results */\n/* ================================== */\n.search a:hover {\n  color: var(--theme-color);\n}\n\n.search .results-panel:empty {\n  display: none;\n}\n\n/* Hide other sidebar items when results are shown */\n.search:has(.results-panel:not(:empty)) ~ * {\n  display: none;\n}\n\n/* Dim other sidebar items when no results are found */\n.search:where(:has(input:valid:focus), :has(.results-panel::empty)) ~ * {\n  opacity: 0.2;\n}\n\n.search .matching-post {\n  overflow: hidden;\n  padding: 1em 0 1.2em 0;\n  border-bottom: 1px solid var(--color-mono-2);\n}\n\n.search .matching-post:hover a {\n  text-decoration-color: transparent;\n}\n\n.search .matching-post:hover .title {\n  text-decoration: inherit;\n  text-decoration-color: var(--link-underline-color-hover);\n}\n\n.search .matching-post .title {\n  margin: 0 0 0.5em 0;\n  line-height: 1.4;\n}\n\n.search .matching-post .content {\n  margin: 0;\n  color: var(--color-mono-6);\n  font-size: var(--font-size-s);\n}\n\n.search .matching-post .search-breadcrumb {\n  margin: 0.35em 0 0 0;\n  color: var(--color-mono-7);\n  font-size: var(--font-size-s);\n  text-align: right;\n}\n\n.search .results-status {\n  margin-bottom: 0;\n  color: var(--color-mono-6);\n  font-size: var(--font-size-s);\n}\n\n.search .results-status:empty {\n  display: none;\n}\n";
    let NO_DATA_TEXT = "";
    let RESULT_SOURCE = "none";
    function stripEmoji(text) {
        return (text || "").replace(/(?:[\uD83C-\uD83E][\uDC00-\uDFFF])|[\u2600-\u27BF\u2B00-\u2BFF]|\uFE0E|\uFE0F|\u200D|\u20E3/g, "").replace(/\s+/g, " ").trim();
    }
    function safeDecode(uri) {
        try {
            return decodeURIComponent(uri);
        } catch {
            return uri;
        }
    }
    function findSidebarLink(url) {
        const base = safeDecode((url || "").split("?")[0]);
        return Docsify.dom.findAll(".sidebar-nav a").find((a => safeDecode((a.getAttribute("href") || "").split("?")[0]) === base));
    }
    function groupLabel(li) {
        for (const node of li.childNodes) {
            if (node.nodeType === Node.TEXT_NODE && node.textContent.trim()) {
                return node.textContent.trim();
            }
            if (node.nodeType === Node.ELEMENT_NODE) {
                if (node.tagName === "UL") {
                    break;
                }
                const text = node.textContent.trim();
                if (text) {
                    return text;
                }
            }
        }
        return "";
    }
    function getBreadcrumb(url) {
        const link = findSidebarLink(url);
        if (!link) {
            return null;
        }
        const parts = [ link.textContent.trim() ];
        let li = link.closest("li");
        while (li) {
            const parentLi = li.parentElement ? li.parentElement.closest("li") : null;
            if (parentLi) {
                const label = groupLabel(parentLi);
                if (label) {
                    parts.unshift(label);
                }
            }
            li = parentLi;
        }
        return parts;
    }
    function resultSourceHtml(post) {
        if (RESULT_SOURCE === "breadcrumb") {
            const parts = getBreadcrumb(post.url);
            if (parts && parts.length) {
                const crumbs = parts.map(((part, i) => {
                    const label = escapeHtml(stripEmoji(part));
                    return i === parts.length - 1 ? `<strong>${label}</strong>` : label;
                })).join(" › ");
                return `<p class="search-breadcrumb clamp-1">${crumbs}</p>`;
            }
            return post.page ? `<p class="search-breadcrumb clamp-1"><strong>${stripEmoji(post.page)}</strong></p>` : "";
        }
        if (RESULT_SOURCE === "page") {
            const page = post.page && post.page !== post.title ? post.page : "";
            return page ? `<p class="search-breadcrumb clamp-1"><strong>${stripEmoji(page)}</strong></p>` : "";
        }
        return "";
    }
    function tpl(vm, defaultValue = "") {
        const {insertAfter: insertAfter, insertBefore: insertBefore} = vm.config?.search || {};
        const html = `\n    <div class="input-wrap">\n      <input type="search" value="${defaultValue}" required aria-keyshortcuts="/ control+k meta+k" />\n      <button class="clear-button" title="Clear search">\n        <span class="visually-hidden">Clear search</span>\n      </button>\n      <div class="kbd-group">\n        <kbd title="Press / to search">/</kbd>\n        <kbd title="Press Control+K to search">⌃K</kbd>\n      </div>\n    </div>\n    <p class="results-status" aria-live="polite"></p>\n    <div class="results-panel"></div>\n  `;
        const sidebarElm = Docsify.dom.find(".sidebar");
        const searchElm = Docsify.dom.create("section", html);
        const insertElm = sidebarElm.querySelector(`:scope ${insertAfter || insertBefore || "> :first-child"}`);
        searchElm.classList.add("search");
        searchElm.setAttribute("role", "search");
        sidebarElm.insertBefore(searchElm, insertAfter ? insertElm.nextSibling : insertElm);
    }
    function doSearch(value) {
        const $search = Docsify.dom.find(".search");
        const $panel = Docsify.dom.find($search, ".results-panel");
        const $status = Docsify.dom.find(".search .results-status");
        if (!value) {
            $panel.innerHTML = "";
            $status.textContent = "";
            return;
        }
        const matches = search(value);
        let html = "";
        matches.forEach(((post, i) => {
            const content = post.content ? `...${post.content}...` : "";
            const title = (post.title || "").replace(/<[^>]+>/g, "");
            html += `\n      <div class="matching-post" aria-label="search result ${i + 1}">\n        <a href="${post.url}" title="${title}">\n          <p class="title clamp-1">${post.title}</p>\n          <p class="content clamp-2">${content}</p>\n          ${resultSourceHtml(post)}\n        </a>\n      </div>\n    `;
        }));
        $panel.innerHTML = html || "";
        $status.textContent = matches.length ? `Found ${matches.length} results` : NO_DATA_TEXT;
    }
    function bindEvents() {
        const $search = Docsify.dom.find(".search");
        const $input = Docsify.dom.find($search, "input");
        const $clear = Docsify.dom.find($search, ".clear-button");
        let timeId;
        Docsify.dom.on($search, "click", (e => [ "A", "H2", "P", "EM" ].indexOf(e.target.tagName) === -1 && e.stopPropagation()));
        Docsify.dom.on($input, "input", (e => {
            clearTimeout(timeId);
            timeId = setTimeout((_ => doSearch(e.target.value.trim())), 100);
        }));
        Docsify.dom.on($clear, "click", (e => {
            $input.value = "";
            doSearch();
        }));
    }
    function updatePlaceholder(text, path) {
        const $input = Docsify.dom.getNode('.search input[type="search"]');
        if (!$input) {
            return;
        }
        if (typeof text === "string") {
            $input.placeholder = text;
        } else {
            const match = Object.keys(text).filter((key => path.indexOf(key) > -1))[0];
            $input.placeholder = text[match];
        }
    }
    function updateNoData(text, path) {
        if (typeof text === "string") {
            NO_DATA_TEXT = text;
        } else {
            const match = Object.keys(text).filter((key => path.indexOf(key) > -1))[0];
            NO_DATA_TEXT = text[match];
        }
    }
    function init(opts, vm) {
        const sidebarElm = Docsify.dom.find(".sidebar");
        if (!sidebarElm) {
            return;
        }
        const keywords = vm.router.parse().query.s || "";
        RESULT_SOURCE = opts.resultSource || RESULT_SOURCE;
        Docsify.dom.style(cssText);
        tpl(vm, escapeHtml(keywords));
        bindEvents();
        keywords && setTimeout((_ => doSearch(keywords)), 500);
    }
    function update(opts, vm) {
        RESULT_SOURCE = opts.resultSource || RESULT_SOURCE;
        updatePlaceholder(opts.placeholder, vm.route.path);
        updateNoData(opts.noData, vm.route.path);
    }
    const CONFIG = {
        placeholder: "Type to search",
        noData: "No Results!",
        paths: "auto",
        depth: 2,
        maxAge: 864e5,
        namespace: undefined,
        pathNamespaces: undefined,
        keyBindings: [ "/", "meta+k", "ctrl+k" ],
        insertAfter: undefined,
        insertBefore: undefined,
        resultSource: "none"
    };
    const install = function(hook, vm) {
        const {util: util} = Docsify;
        const opts = vm.config.search || CONFIG;
        if (Array.isArray(opts)) {
            CONFIG.paths = opts;
        } else if (typeof opts === "object") {
            CONFIG.paths = Array.isArray(opts.paths) ? opts.paths : "auto";
            CONFIG.maxAge = util.isPrimitive(opts.maxAge) ? opts.maxAge : CONFIG.maxAge;
            CONFIG.placeholder = opts.placeholder || CONFIG.placeholder;
            CONFIG.noData = opts.noData || CONFIG.noData;
            CONFIG.depth = opts.depth || CONFIG.depth;
            CONFIG.namespace = opts.namespace || CONFIG.namespace;
            CONFIG.pathNamespaces = opts.pathNamespaces || CONFIG.pathNamespaces;
            CONFIG.keyBindings = opts.keyBindings || CONFIG.keyBindings;
            CONFIG.resultSource = opts.resultSource || CONFIG.resultSource;
        }
        const isAuto = CONFIG.paths === "auto";
        hook.init((() => {
            const {keyBindings: keyBindings} = vm.config;
            if (keyBindings.constructor === Object) {
                keyBindings.focusSearch = {
                    bindings: CONFIG.keyBindings,
                    callback(e) {
                        const sidebarElm = document.querySelector(".sidebar");
                        const sidebarToggleElm = document.querySelector(".sidebar-toggle");
                        const searchElm = sidebarElm?.querySelector('input[type="search"]');
                        const isSidebarHidden = (sidebarElm?.getBoundingClientRect().x ?? 0) < 0;
                        isSidebarHidden && sidebarToggleElm?.click();
                        setTimeout((() => searchElm?.focus()), isSidebarHidden ? 250 : 0);
                    }
                };
            }
        }));
        hook.mounted((_ => {
            init(CONFIG, vm);
            !isAuto && init$1(CONFIG, vm);
        }));
        hook.doneEach((_ => {
            update(CONFIG, vm);
            isAuto && init$1(CONFIG, vm);
        }));
    };
    window.$docsify = window.$docsify || {};
    window.$docsify.plugins = [ install, ...window.$docsify.plugins || [] ];
})();
